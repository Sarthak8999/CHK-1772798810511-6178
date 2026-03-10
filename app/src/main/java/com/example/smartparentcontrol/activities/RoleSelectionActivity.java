package com.example.smartparentcontrol.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartparentcontrol.R;
import com.example.smartparentcontrol.utils.PreferenceManager;

public class RoleSelectionActivity extends AppCompatActivity {

    private static final String TAG = "RoleSelectionActivity";
    
    private Button parentButton;
    private Button studentButton;
    private Button loginButton;
    
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.d(TAG, "onCreate started");
        
        try {
            setContentView(R.layout.activity_role_selection);
            Log.d(TAG, "Layout set successfully");
            
            preferenceManager = new PreferenceManager(this);
            Log.d(TAG, "PreferenceManager initialized");
            
            if (preferenceManager.isLoggedIn()) {
                Log.d(TAG, "User is logged in, navigating to dashboard");
                navigateToDashboard(preferenceManager.getUserRole());
                return;
            }

            parentButton = findViewById(R.id.parentButton);
            studentButton = findViewById(R.id.studentButton);
            loginButton = findViewById(R.id.loginButton);
            
            Log.d(TAG, "Buttons initialized");

            parentButton.setOnClickListener(v -> {
                Log.d(TAG, "Parent button clicked");
                try {
                    Intent intent = new Intent(RoleSelectionActivity.this, RegisterActivity.class);
                    intent.putExtra("role", "parent");
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Error starting RegisterActivity", e);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

            studentButton.setOnClickListener(v -> {
                Log.d(TAG, "Student button clicked");
                try {
                    Intent intent = new Intent(RoleSelectionActivity.this, RegisterActivity.class);
                    intent.putExtra("role", "student");
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Error starting RegisterActivity", e);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
            
            loginButton.setOnClickListener(v -> {
                Log.d(TAG, "Login button clicked");
                try {
                    Intent intent = new Intent(RoleSelectionActivity.this, LoginActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Error starting LoginActivity", e);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
            
            Log.d(TAG, "onCreate completed successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Error loading screen: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void navigateToDashboard(String role) {
        try {
            Intent intent;
            if ("parent".equals(role)) {
                intent = new Intent(this, ParentDashboardActivity.class);
            } else {
                intent = new Intent(this, StudentDashboardActivity.class);
            }
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to dashboard", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
