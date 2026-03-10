package ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartparentcontrol.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import adapter.InstalledAppsAdapter;
import model.AppInfo;
import service.InstalledAppsService;
import service.UsageStatsService;

public class UsageStatsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView totalUsageText;
    private InstalledAppsAdapter adapter;
    private UsageStatsService usageStatsService;
    private InstalledAppsService appsService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usage_stats);

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        totalUsageText = findViewById(R.id.totalUsageText);

        usageStatsService = new UsageStatsService(this);
        appsService = new InstalledAppsService(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InstalledAppsAdapter(null);
        recyclerView.setAdapter(adapter);

        loadUsageStats();
    }

    private void loadUsageStats() {
        progressBar.setVisibility(View.VISIBLE);

        new Thread(() -> {
            Map<String, Long> usageMap = usageStatsService.getTodayUsageStats();
            List<AppInfo> appList = new ArrayList<>();
            long totalUsage = 0;

            for (Map.Entry<String, Long> entry : usageMap.entrySet()) {
                String packageName = entry.getKey();
                long usage = entry.getValue();
                totalUsage += usage;

                String appName = appsService.getAppName(packageName);
                AppInfo appInfo = new AppInfo(appName, packageName, null);
                appInfo.setUsageTime(usage);
                appList.add(appInfo);
            }

            appList.sort((a, b) -> Long.compare(b.getUsageTime(), a.getUsageTime()));

            long finalTotalUsage = totalUsage;
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                adapter.setAppList(appList);
                totalUsageText.setText("Total Screen Time Today: " +
                        UsageStatsService.formatUsageTime(finalTotalUsage));
            });
        }).start();
    }
}
