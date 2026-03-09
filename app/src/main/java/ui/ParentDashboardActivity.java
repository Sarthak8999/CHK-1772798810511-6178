package ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartparentcontrol.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ParentDashboardActivity extends AppCompatActivity {

    TextView welcomeText;
    TextView todayUsageText;
    TextView weeklyUsageText;
    TextView totalAppsText;
    TextView referralCodeText;

    Button viewAppsButton;
    Button setLimitsButton;
    Button viewUsageButton;

    BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_dashboard);

        welcomeText = findViewById(R.id.welcomeText);
        todayUsageText = findViewById(R.id.todayUsageText);
        weeklyUsageText = findViewById(R.id.weeklyUsageText);
        totalAppsText = findViewById(R.id.totalAppsText);
        referralCodeText = findViewById(R.id.referralCodeText);

        viewAppsButton = findViewById(R.id.viewAppsButton);
        setLimitsButton = findViewById(R.id.setLimitsButton);
        viewUsageButton = findViewById(R.id.viewUsageButton);

        bottomNav = findViewById(R.id.bottomNav);

        welcomeText.setText("Welcome Parent");

        todayUsageText.setText("0m");
        weeklyUsageText.setText("0m");
        totalAppsText.setText("0");

        viewAppsButton.setOnClickListener(v ->
                Toast.makeText(this,"View Apps Clicked",Toast.LENGTH_SHORT).show());

        setLimitsButton.setOnClickListener(v ->
                Toast.makeText(this,"Set Limits Clicked",Toast.LENGTH_SHORT).show());

        viewUsageButton.setOnClickListener(v ->
                Toast.makeText(this,"Usage Clicked",Toast.LENGTH_SHORT).show());

        bottomNav.setOnItemSelectedListener(item -> {

            if(item.getItemId()==R.id.nav_dashboard)
            {
                return true;
            }

            if(item.getItemId()==R.id.nav_settings)
            {
                Toast.makeText(this,"Settings",Toast.LENGTH_SHORT).show();
                return true;
            }

            return false;
        });
    }
}