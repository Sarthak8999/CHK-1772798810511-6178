package ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartparentcontrol.R;

import java.util.List;

import adapter.InstalledAppsAdapter;
import model.AppInfo;
import service.InstalledAppsService;
import service.UsageStatsService;
import utils.PreferenceManager;

public class InstalledAppsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private InstalledAppsAdapter adapter;
    private InstalledAppsService appsService;
    private UsageStatsService usageStatsService;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_installed_apps);

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);

        appsService = new InstalledAppsService(this);
        usageStatsService = new UsageStatsService(this);
        preferenceManager = new PreferenceManager(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InstalledAppsAdapter(this::onAppClick);
        recyclerView.setAdapter(adapter);

        loadInstalledApps();
    }

    private void loadInstalledApps() {
        progressBar.setVisibility(View.VISIBLE);

        new Thread(() -> {
            List<AppInfo> apps = appsService.getInstalledApps();

            for (AppInfo app : apps) {
                long usage = usageStatsService.getAppUsageToday(app.getPackageName());
                app.setUsageTime(usage);
            }

            if ("student".equals(preferenceManager.getUserRole())) {
                String parentUID = preferenceManager.getParentUID();
                String studentUID = preferenceManager.getUserId();
                if (parentUID != null && studentUID != null) {
                    appsService.uploadInstalledAppsToFirebase(parentUID, studentUID, apps);
                }
            }

            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                adapter.setAppList(apps);
            });
        }).start();
    }

    private void onAppClick(AppInfo appInfo) {
        if ("parent".equals(preferenceManager.getUserRole())) {
            String studentUID = getIntent().getStringExtra("student_uid");
            if (studentUID != null) {
                Intent intent = new Intent(this, SetAppTimeLimitActivity.class);
                intent.putExtra("package_name", appInfo.getPackageName());
                intent.putExtra("student_uid", studentUID);
                startActivity(intent);
            }
        } else {
            Toast.makeText(this,
                    appInfo.getAppName() + "\nUsage: " + appInfo.getFormattedUsageTime(),
                    Toast.LENGTH_LONG).show();
        }
    }
}
