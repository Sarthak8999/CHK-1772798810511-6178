package ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartparentcontrol.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import ui.RoleSelectionActivity;

public class StudentDashboardActivity extends AppCompatActivity {

    private static final String TAG = "StudentDashboard";

    TextView welcomeText;
    TextView todayUsageText;
    TextView remainingTimeText;

    ProgressBar screenTimeProgress;

    Button requestAccessButton;

    BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        initViews();
        setupClickListeners();
        loadDashboardData();
    }

    private void initViews() {

        welcomeText = findViewById(R.id.welcomeText);
        todayUsageText = findViewById(R.id.todayUsageText);
        remainingTimeText = findViewById(R.id.remainingTimeText);
        screenTimeProgress = findViewById(R.id.screenTimeProgress);
        requestAccessButton = findViewById(R.id.requestAccessButton);
        bottomNav = findViewById(R.id.bottomNav);

        welcomeText.setText("Welcome Student");

    }

    private void loadDashboardData() {

        try {

            long todayUsage = 60; // demo value (minutes)

            todayUsageText.setText("Today Usage : " + todayUsage + " min");

            long dailyLimit = 240; // 4 hours

            long remaining = dailyLimit - todayUsage;

            remainingTimeText.setText("Remaining Time : " + remaining + " min");

            int progress = (int) ((todayUsage * 100) / dailyLimit);

            screenTimeProgress.setProgress(progress);

        }
        catch (Exception e)
        {
            Log.e(TAG,"Dashboard error",e);
        }

    }

    private void setupClickListeners() {

        requestAccessButton.setOnClickListener(v -> {

            Toast.makeText(this,"Request sent to parent",Toast.LENGTH_SHORT).show();

        });

        bottomNav.setOnItemSelectedListener(item -> {

            if(item.getItemId()==R.id.nav_dashboard)
            {
                return true;
            }

            if(item.getItemId()==R.id.nav_requests)
            {
                Toast.makeText(this,"Requests Screen",Toast.LENGTH_SHORT).show();
                return true;
            }

            if(item.getItemId()==R.id.nav_dashboard)
            {
                Toast.makeText(this,"Installed Apps",Toast.LENGTH_SHORT).show();
                return true;
            }

            if(item.getItemId()==R.id.nav_settings)
            {
                showLogout();
                return true;
            }

            return false;

        });

    }

    private void showLogout()
    {

        Toast.makeText(this,"Logout",Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, RoleSelectionActivity.class);

        startActivity(intent);

        finish();

    }

}