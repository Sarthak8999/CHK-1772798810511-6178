package com.example.smartparentcontrol.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartparentcontrol.R;
import com.example.smartparentcontrol.models.AppInfo;
import com.example.smartparentcontrol.models.TimeRequest;
import com.example.smartparentcontrol.repository.RequestsRepository;
import com.example.smartparentcontrol.utils.AppScanner;

import utils.PreferenceManager;

/**
 * TimeLimitBlockActivity
 * Displayed when a student exceeds their daily app usage limit
 * 
 * Features:
 * - Shows blocked app information
 * - Displays usage and limit details
 * - Request more time dialog
 * - Exit to home functionality
 * - Prevents back navigation to blocked app
 */
public class TimeLimitBlockActivity extends AppCompatActivity {

    private ImageView appIcon;
    private ImageView blockIcon;
    private TextView blockedAppName;
    private TextView messageText;
    private TextView usageInfoText;
    private Button requestTimeButton;
    private Button exitButton;
    
    private String blockedPackageName;
    private String appName;
    private long usageTime;
    private long timeLimit;
    
    private PreferenceManager preferenceManager;
    private AppScanner appScanner;
    private RequestsRepository requestsRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_limit_block);

        // Get blocked app info from intent
        blockedPackageName = getIntent().getStringExtra("blocked_app");
        usageTime = getIntent().getLongExtra("usage_time", 0);
        timeLimit = getIntent().getLongExtra("time_limit", 0);

        if (blockedPackageName == null) {
            finish();
            return;
        }

        initViews();
        initializeComponents();
        loadAppInfo();
        setupButtons();
    }

    private void initViews() {
        appIcon = findViewById(R.id.appIcon);
        blockIcon = findViewById(R.id.blockIcon);
        blockedAppName = findViewById(R.id.blockedAppName);
        messageText = findViewById(R.id.messageText);
        usageInfoText = findViewById(R.id.usageInfoText);
        requestTimeButton = findViewById(R.id.requestTimeButton);
        exitButton = findViewById(R.id.exitButton);
    }

    private void initializeComponents() {
        preferenceManager = new PreferenceManager(this);
        appScanner = new AppScanner(this);
        requestsRepository = new RequestsRepository();
    }

    private void loadAppInfo() {
        // Load app information
        AppInfo appInfo = appScanner.getAppInfo(blockedPackageName);
        
        if (appInfo != null) {
            appName = appInfo.getAppName();
            
            // Set app icon
            if (appInfo.getIcon() != null) {
                appIcon.setImageDrawable(appInfo.getIcon());
                appIcon.setVisibility(View.VISIBLE);
            } else {
                appIcon.setVisibility(View.GONE);
            }
        } else {
            appName = blockedPackageName;
            appIcon.setVisibility(View.GONE);
        }
        
        // Set app name
        blockedAppName.setText(appName);
        
        // Set message
        messageText.setText("You have reached your daily time limit for this app.");
        
        // Set usage info
        if (usageTime > 0 && timeLimit > 0) {
            String usageInfo = String.format("Usage: %s / Limit: %s",
                formatTime(usageTime),
                formatTime(timeLimit));
            usageInfoText.setText(usageInfo);
            usageInfoText.setVisibility(View.VISIBLE);
        } else {
            usageInfoText.setVisibility(View.GONE);
        }
    }

    private void setupButtons() {
        // Request more time button
        requestTimeButton.setOnClickListener(v -> showRequestDialog());
        
        // Exit button
        exitButton.setOnClickListener(v -> exitToHome());
    }

    /**
     * Show dialog to request more time
     */
    private void showRequestDialog() {
        View dialogView = LayoutInflater.from(this)
            .inflate(R.layout.dialog_request_time, null);
        
        EditText minutesInput = dialogView.findViewById(R.id.minutesInput);
        EditText reasonInput = dialogView.findViewById(R.id.reasonInput);
        
        new AlertDialog.Builder(this)
            .setTitle("Request More Time")
            .setMessage("Request additional time for " + appName)
            .setView(dialogView)
            .setPositiveButton("Send Request", (dialog, which) -> {
                String minutesStr = minutesInput.getText().toString().trim();
                String reason = reasonInput.getText().toString().trim();
                
                if (minutesStr.isEmpty()) {
                    Toast.makeText(this, "Please enter minutes", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                try {
                    int minutes = Integer.parseInt(minutesStr);
                    if (minutes <= 0) {
                        Toast.makeText(this, "Minutes must be greater than 0", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    sendTimeRequest(minutes, reason);
                    
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Invalid number", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    /**
     * Send time extension request to parent
     */
    private void sendTimeRequest(int minutes, String reason) {
        String studentId = preferenceManager.getUserId();
        String parentId = preferenceManager.getParentUID();
        String studentName = preferenceManager.getUserName();
        
        if (studentId == null || parentId == null) {
            Toast.makeText(this, "Error: User information not found", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Create request
        TimeRequest request = new TimeRequest(
            studentId,
            parentId,
            studentName != null ? studentName : "Student",
            appName,
            blockedPackageName,
            minutes,
            reason != null && !reason.isEmpty() ? reason : "No reason provided",
            "time_extension"
        );
        
        // Show progress
        requestTimeButton.setEnabled(false);
        requestTimeButton.setText("Sending...");
        
        // Send to Firestore
        requestsRepository.createRequest(request, new RequestsRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(TimeLimitBlockActivity.this, 
                        "Request sent to parent", 
                        Toast.LENGTH_SHORT).show();
                    
                    // Exit to home after sending request
                    exitToHome();
                });
            }
            
            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    requestTimeButton.setEnabled(true);
                    requestTimeButton.setText("Request More Time");
                    
                    Toast.makeText(TimeLimitBlockActivity.this, 
                        "Failed to send request: " + error, 
                        Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    /**
     * Exit to home screen
     */
    private void exitToHome() {
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
        finish();
    }

    /**
     * Format time in milliseconds to readable string
     */
    private String formatTime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        if (hours > 0) {
            long remainingMinutes = minutes % 60;
            if (remainingMinutes > 0) {
                return hours + "h " + remainingMinutes + "m";
            }
            return hours + "h";
        } else if (minutes > 0) {
            return minutes + "m";
        } else {
            return seconds + "s";
        }
    }

    @Override
    public void onBackPressed() {
        // Prevent going back to blocked app
        exitToHome();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Finish activity when user leaves (prevents staying in background)
        finish();
    }
}
