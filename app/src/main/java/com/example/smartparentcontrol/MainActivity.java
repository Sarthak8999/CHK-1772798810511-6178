package com.example.smartparentcontrol;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import ui.ParentDashboardActivity;
import ui.RoleSelectionActivity;
import ui.StudentDashboardActivity;
import utils.PreferenceManager;

public class MainActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2000; // 2 seconds
    
    private ImageView logoImage;
    private TextView appNameText;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        logoImage = findViewById(R.id.logoImage);
        appNameText = findViewById(R.id.appNameText);
        
        // Initialize PreferenceManager
        preferenceManager = new PreferenceManager(this);

        // Start animations
        startAnimations();

        // Navigate after delay
        new Handler().postDelayed(this::checkLoginAndNavigate, SPLASH_DURATION);
    }

    private void startAnimations() {
        // Fade in animation for logo
        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        fadeIn.setDuration(1000);
        logoImage.startAnimation(fadeIn);
        
        // Slide up animation for text
        Animation slideUp = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        slideUp.setDuration(1000);
        slideUp.setStartOffset(500);
        appNameText.startAnimation(slideUp);
    }

    private void checkLoginAndNavigate() {
        Intent intent;
        
        if (preferenceManager.isLoggedIn()) {
            // User is logged in - go to appropriate dashboard
            String role = preferenceManager.getUserRole();
            
            if ("parent".equals(role)) {
                intent = new Intent(this, ParentDashboardActivity.class);
            } else {
                intent = new Intent(this, StudentDashboardActivity.class);
            }
        } else {
            // User not logged in - go to role selection
            intent = new Intent(this, RoleSelectionActivity.class);
        }
        
        startActivity(intent);
        finish(); // Close splash screen
        
        // Add transition animation
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
