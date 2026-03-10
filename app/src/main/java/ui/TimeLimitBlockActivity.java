package ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartparentcontrol.R;

import service.InstalledAppsService;

public class TimeLimitBlockActivity extends AppCompatActivity {

    private TextView blockedAppName;
    private TextView messageText;
    private Button requestTimeButton;
    private Button goHomeButton;
    
    private String blockedPackageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_limit_block);

        // Get blocked app info
        blockedPackageName = getIntent().getStringExtra("blocked_app");

        // Initialize views
        blockedAppName = findViewById(R.id.blockedAppName);
        messageText = findViewById(R.id.messageText);
        requestTimeButton = findViewById(R.id.requestTimeButton);
        goHomeButton = findViewById(R.id.goHomeButton);

        // Set app name
        if (blockedPackageName != null) {
            InstalledAppsService appsService = new InstalledAppsService(this);
            String appName = appsService.getAppName(blockedPackageName);
            blockedAppName.setText(appName);
            messageText.setText("You have reached your daily time limit for this app.");
        }

        // Request more time button
        requestTimeButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, StudentRequestsActivity.class);
            intent.putExtra("request_app", blockedPackageName);
            startActivity(intent);
            finish();
        });

        // Go home button
        goHomeButton.setOnClickListener(v -> {
            Intent homeIntent = new Intent(Intent.ACTION_MAIN);
            homeIntent.addCategory(Intent.CATEGORY_HOME);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(homeIntent);
            finish();
        });

        // Prevent back button
        preventBackNavigation();
    }

    private void preventBackNavigation() {
        // Keep checking if user tries to go back to blocked app
        new Handler().postDelayed(() -> {
            if (!isFinishing()) {
                preventBackNavigation();
            }
        }, 1000);
    }

    @Override
    public void onBackPressed() {
        // Prevent going back to blocked app
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(homeIntent);
    }
}
