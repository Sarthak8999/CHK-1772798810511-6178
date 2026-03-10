package ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartparentcontrol.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import service.InstalledAppsService;
import utils.PreferenceManager;

public class SetAppTimeLimitActivity extends AppCompatActivity {

    private TextView appNameText;
    private EditText limitMinutesInput;
    private Button saveLimitButton;
    private Button removeLimitButton;
    
    private String packageName;
    private String studentUID;
    private PreferenceManager preferenceManager;
    private DatabaseReference limitsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_app_time_limit);

        // Get app info from intent
        packageName = getIntent().getStringExtra("package_name");
        studentUID = getIntent().getStringExtra("student_uid");

        if (packageName == null || studentUID == null) {
            Toast.makeText(this, "Error: Missing app information", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        appNameText = findViewById(R.id.appNameText);
        limitMinutesInput = findViewById(R.id.limitMinutesInput);
        saveLimitButton = findViewById(R.id.saveLimitButton);
        removeLimitButton = findViewById(R.id.removeLimitButton);

        // Initialize Firebase
        preferenceManager = new PreferenceManager(this);
        String parentUID = preferenceManager.getUserId();
        
        limitsRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(parentUID)
                .child("studentsData")
                .child(studentUID)
                .child("appLimits")
                .child(packageName);

        // Set app name
        InstalledAppsService appsService = new InstalledAppsService(this);
        String appName = appsService.getAppName(packageName);
        appNameText.setText("Set Time Limit for " + appName);

        // Load existing limit
        loadExistingLimit();

        // Save button
        saveLimitButton.setOnClickListener(v -> saveLimit());

        // Remove button
        removeLimitButton.setOnClickListener(v -> removeLimit());
    }

    private void loadExistingLimit() {
        limitsRef.child("limitMinutes").get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                Long minutes = snapshot.getValue(Long.class);
                if (minutes != null) {
                    limitMinutesInput.setText(String.valueOf(minutes));
                }
            }
        });
    }

    private void saveLimit() {
        String minutesStr = limitMinutesInput.getText().toString().trim();
        
        if (minutesStr.isEmpty()) {
            limitMinutesInput.setError("Enter time limit in minutes");
            return;
        }

        try {
            int minutes = Integer.parseInt(minutesStr);
            
            if (minutes <= 0) {
                limitMinutesInput.setError("Must be greater than 0");
                return;
            }

            // Save to Firebase
            Map<String, Object> limitData = new HashMap<>();
            limitData.put("limitMinutes", minutes);
            limitData.put("packageName", packageName);
            limitData.put("updatedAt", System.currentTimeMillis());

            limitsRef.setValue(limitData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Time limit set: " + minutes + " minutes", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to save limit", Toast.LENGTH_SHORT).show();
                    });

        } catch (NumberFormatException e) {
            limitMinutesInput.setError("Invalid number");
        }
    }

    private void removeLimit() {
        limitsRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Time limit removed", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to remove limit", Toast.LENGTH_SHORT).show();
                });
    }
}
