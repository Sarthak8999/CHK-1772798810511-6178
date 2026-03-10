package com.example.smartparentcontrol.activities;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.smartparentcontrol.R;
import com.example.smartparentcontrol.models.AppInfo;
import com.example.smartparentcontrol.repository.AppLimitsRepository;
import com.example.smartparentcontrol.utils.AppScanner;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import utils.PreferenceManager;

/**
 * SetTimeLimitActivity
 * Allows parents to set daily usage limits for apps
 * Saves limits to Firestore collection: app_limits
 * 
 * Features:
 * - Set time limit in minutes
 * - Quick select chips (15, 30, 60, 120 minutes)
 * - Load existing limits
 * - Remove limits
 * - Firestore integration
 */
public class SetTimeLimitActivity extends AppCompatActivity {

    private ImageView appIcon;
    private TextView appNameText;
    private TextView packageNameText;
    private EditText limitMinutesInput;
    private ChipGroup quickSelectChips;
    private Button saveLimitButton;
    private Button removeLimitButton;
    private ProgressBar progressBar;
    private TextView currentLimitText;
    
    private String packageName;
    private String appName;
    private String studentUID;
    private PreferenceManager preferenceManager;
    private AppLimitsRepository limitsRepository;
    private AppScanner appScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_app_time_limit);

        // Get app info from intent
        packageName = getIntent().getStringExtra("package_name");
        appName = getIntent().getStringExtra("app_name");
        studentUID = getIntent().getStringExtra("student_uid");

        if (packageName == null) {
            Toast.makeText(this, "Error: Missing app information", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        
        preferenceManager = new PreferenceManager(this);
        limitsRepository = new AppLimitsRepository();
        appScanner = new AppScanner(this);

        // Get student UID if not provided
        if (studentUID == null) {
            studentUID = preferenceManager.getUserId();
        }

        loadAppInfo();
        setupQuickSelectChips();
        loadExistingLimit();

        saveLimitButton.setOnClickListener(v -> saveLimit());
        removeLimitButton.setOnClickListener(v -> confirmRemoveLimit());
    }

    private void initViews() {
        appIcon = findViewById(R.id.appIcon);
        appNameText = findViewById(R.id.appNameText);
        packageNameText = findViewById(R.id.packageNameText);
        limitMinutesInput = findViewById(R.id.limitMinutesInput);
        quickSelectChips = findViewById(R.id.quickSelectChips);
        saveLimitButton = findViewById(R.id.saveLimitButton);
        removeLimitButton = findViewById(R.id.removeLimitButton);
        progressBar = findViewById(R.id.progressBar);
        currentLimitText = findViewById(R.id.currentLimitText);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Set Time Limit");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void loadAppInfo() {
        // Load app information
        AppInfo appInfo = appScanner.getAppInfo(packageName);
        
        if (appInfo != null) {
            // Set app icon
            if (appInfo.getIcon() != null) {
                appIcon.setImageDrawable(appInfo.getIcon());
            }
            
            // Set app name
            if (appName == null || appName.isEmpty()) {
                appName = appInfo.getAppName();
            }
            appNameText.setText(appName);
            
            // Set package name
            packageNameText.setText(packageName);
        } else {
            // Fallback if app not found
            appNameText.setText(appName != null ? appName : "Unknown App");
            packageNameText.setText(packageName);
            appIcon.setImageResource(android.R.drawable.sym_def_app_icon);
        }
    }

    private void setupQuickSelectChips() {
        int[] quickMinutes = {15, 30, 60, 120, 180};
        String[] quickLabels = {"15 min", "30 min", "1 hour", "2 hours", "3 hours"};
        
        for (int i = 0; i < quickMinutes.length; i++) {
            Chip chip = new Chip(this);
            chip.setText(quickLabels[i]);
            chip.setCheckable(true);
            
            final int minutes = quickMinutes[i];
            chip.setOnClickListener(v -> {
                limitMinutesInput.setText(String.valueOf(minutes));
                // Uncheck other chips
                for (int j = 0; j < quickSelectChips.getChildCount(); j++) {
                    Chip otherChip = (Chip) quickSelectChips.getChildAt(j);
                    if (otherChip != chip) {
                        otherChip.setChecked(false);
                    }
                }
            });
            
            quickSelectChips.addView(chip);
        }
    }

    private void loadExistingLimit() {
        progressBar.setVisibility(View.VISIBLE);
        currentLimitText.setVisibility(View.GONE);
        
        limitsRepository.getAppLimit(studentUID, packageName, 
            new AppLimitsRepository.AppLimitCallback() {
                @Override
                public void onSuccess(com.example.smartparentcontrol.models.AppLimit limit) {
                    progressBar.setVisibility(View.GONE);
                    
                    if (limit != null) {
                        // Display existing limit
                        int minutes = limit.getTimeLimitMinutes();
                        limitMinutesInput.setText(String.valueOf(minutes));
                        
                        currentLimitText.setText("Current limit: " + formatMinutes(minutes));
                        currentLimitText.setVisibility(View.VISIBLE);
                        
                        removeLimitButton.setEnabled(true);
                        removeLimitButton.setVisibility(View.VISIBLE);
                    } else {
                        currentLimitText.setText("No limit set");
                        currentLimitText.setVisibility(View.VISIBLE);
                        
                        removeLimitButton.setEnabled(false);
                        removeLimitButton.setVisibility(View.GONE);
                    }
                }
                
                @Override
                public void onFailure(String error) {
                    progressBar.setVisibility(View.GONE);
                    currentLimitText.setText("No limit set");
                    currentLimitText.setVisibility(View.VISIBLE);
                    removeLimitButton.setEnabled(false);
                    removeLimitButton.setVisibility(View.GONE);
                }
            });
    }

    private void saveLimit() {
        String minutesStr = limitMinutesInput.getText().toString().trim();
        
        if (minutesStr.isEmpty()) {
            limitMinutesInput.setError("Enter time limit in minutes");
            limitMinutesInput.requestFocus();
            return;
        }

        try {
            int minutes = Integer.parseInt(minutesStr);
            
            if (minutes <= 0) {
                limitMinutesInput.setError("Must be greater than 0");
                limitMinutesInput.requestFocus();
                return;
            }
            
            if (minutes > 1440) { // 24 hours
                limitMinutesInput.setError("Cannot exceed 24 hours (1440 minutes)");
                limitMinutesInput.requestFocus();
                return;
            }

            // Show progress
            progressBar.setVisibility(View.VISIBLE);
            saveLimitButton.setEnabled(false);

            // Save to Firestore using repository
            limitsRepository.setAppLimit(studentUID, packageName, minutes, 
                new AppLimitsRepository.OperationCallback() {
                    @Override
                    public void onSuccess() {
                        progressBar.setVisibility(View.GONE);
                        saveLimitButton.setEnabled(true);
                        
                        Toast.makeText(SetTimeLimitActivity.this, 
                            "Time limit set: " + formatMinutes(minutes), 
                            Toast.LENGTH_SHORT).show();
                        
                        finish();
                    }
                    
                    @Override
                    public void onFailure(String error) {
                        progressBar.setVisibility(View.GONE);
                        saveLimitButton.setEnabled(true);
                        
                        Toast.makeText(SetTimeLimitActivity.this, 
                            "Failed to save limit: " + error, 
                            Toast.LENGTH_SHORT).show();
                    }
                });

        } catch (NumberFormatException e) {
            limitMinutesInput.setError("Invalid number");
            limitMinutesInput.requestFocus();
        }
    }

    private void confirmRemoveLimit() {
        new AlertDialog.Builder(this)
            .setTitle("Remove Time Limit")
            .setMessage("Are you sure you want to remove the time limit for " + appName + "?")
            .setPositiveButton("Remove", (dialog, which) -> removeLimit())
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void removeLimit() {
        progressBar.setVisibility(View.VISIBLE);
        removeLimitButton.setEnabled(false);

        limitsRepository.removeAppLimit(studentUID, packageName, 
            new AppLimitsRepository.OperationCallback() {
                @Override
                public void onSuccess() {
                    progressBar.setVisibility(View.GONE);
                    removeLimitButton.setEnabled(true);
                    
                    Toast.makeText(SetTimeLimitActivity.this, 
                        "Time limit removed", 
                        Toast.LENGTH_SHORT).show();
                    
                    finish();
                }
                
                @Override
                public void onFailure(String error) {
                    progressBar.setVisibility(View.GONE);
                    removeLimitButton.setEnabled(true);
                    
                    Toast.makeText(SetTimeLimitActivity.this, 
                        "Failed to remove limit: " + error, 
                        Toast.LENGTH_SHORT).show();
                }
            });
    }

    private String formatMinutes(int minutes) {
        if (minutes < 60) {
            return minutes + " minute" + (minutes != 1 ? "s" : "");
        } else {
            int hours = minutes / 60;
            int remainingMinutes = minutes % 60;
            
            String result = hours + " hour" + (hours != 1 ? "s" : "");
            if (remainingMinutes > 0) {
                result += " " + remainingMinutes + " minute" + (remainingMinutes != 1 ? "s" : "");
            }
            return result;
        }
    }
}
