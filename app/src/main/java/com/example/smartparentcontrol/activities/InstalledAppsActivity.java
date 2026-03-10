package com.example.smartparentcontrol.activities;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartparentcontrol.R;
import com.example.smartparentcontrol.adapters.InstalledAppsAdapter;
import com.example.smartparentcontrol.models.AppInfo;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import service.UsageStatsService;
import ui.SetAppTimeLimitActivity;
import utils.PreferenceManager;

/**
 * InstalledAppsActivity
 * Displays all installed non-system apps using PackageManager
 * Features:
 * - Fetches installed apps with icons
 * - Filters out system apps
 * - Shows app usage time
 * - Search functionality
 * - Click to set time limits
 */
public class InstalledAppsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyView;
    private SearchView searchView;
    private InstalledAppsAdapter adapter;
    private UsageStatsService usageStatsService;
    private PreferenceManager preferenceManager;
    
    private List<AppInfo> allApps = new ArrayList<>();
    private List<AppInfo> filteredApps = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_installed_apps);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupSearch();
        
        usageStatsService = new UsageStatsService(this);
        preferenceManager = new PreferenceManager(this);

        loadInstalledApps();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);
        searchView = findViewById(R.id.searchView);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Installed Apps");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InstalledAppsAdapter(this::onAppClick);
        recyclerView.setAdapter(adapter);
    }

    private void setupSearch() {
        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    filterApps(query);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    filterApps(newText);
                    return true;
                }
            });
        }
    }

    private void loadInstalledApps() {
        progressBar.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);

        // Load apps in background thread
        new Thread(() -> {
            List<AppInfo> apps = fetchInstalledApps();
            
            // Load usage stats for each app
            for (AppInfo app : apps) {
                long usage = usageStatsService.getAppUsageToday(app.getPackageName());
                app.setUsageTime(usage);
            }

            // Upload to Firebase if student
            if ("student".equalsIgnoreCase(preferenceManager.getUserRole())) {
                uploadAppsToFirebase(apps);
            }

            // Update UI on main thread
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                allApps = apps;
                filteredApps = new ArrayList<>(apps);
                
                if (apps.isEmpty()) {
                    emptyView.setVisibility(View.VISIBLE);
                    emptyView.setText("No apps found");
                    recyclerView.setVisibility(View.GONE);
                } else {
                    emptyView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    adapter.setAppList(filteredApps);
                }
            });
        }).start();
    }

    /**
     * Fetch all installed non-system apps using PackageManager
     */
    private List<AppInfo> fetchInstalledApps() {
        List<AppInfo> appList = new ArrayList<>();
        PackageManager packageManager = getPackageManager();
        
        try {
            List<ApplicationInfo> packages = packageManager.getInstalledApplications(
                    PackageManager.GET_META_DATA);

            for (ApplicationInfo appInfo : packages) {
                // Filter out system apps
                boolean isSystemApp = (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
                
                if (!isSystemApp) {
                    try {
                        String appName = packageManager.getApplicationLabel(appInfo).toString();
                        String packageName = appInfo.packageName;
                        
                        AppInfo app = new AppInfo(
                                appName,
                                packageName,
                                packageManager.getApplicationIcon(appInfo)
                        );
                        
                        app.setSystemApp(false);
                        appList.add(app);
                        
                    } catch (Exception e) {
                        // Skip apps that cause errors
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return appList;
    }

    /**
     * Upload installed apps to Firebase for parent monitoring
     */
    private void uploadAppsToFirebase(List<AppInfo> apps) {
        String parentUID = preferenceManager.getParentUID();
        String studentUID = preferenceManager.getUserId();
        
        if (parentUID == null || studentUID == null) {
            return;
        }

        DatabaseReference appsRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(parentUID)
                .child("studentsData")
                .child(studentUID)
                .child("installedApps");

        Map<String, Object> appsMap = new HashMap<>();
        
        for (AppInfo app : apps) {
            Map<String, Object> appData = new HashMap<>();
            appData.put("appName", app.getAppName());
            appData.put("packageName", app.getPackageName());
            appData.put("usageTime", app.getUsageTime());
            appData.put("lastUpdated", System.currentTimeMillis());
            
            // Replace dots with underscores for Firebase key
            String key = app.getPackageName().replace(".", "_");
            appsMap.put(key, appData);
        }

        appsRef.setValue(appsMap)
                .addOnSuccessListener(aVoid -> {
                    // Apps uploaded successfully
                })
                .addOnFailureListener(e -> {
                    // Failed to upload apps
                });
    }

    /**
     * Filter apps based on search query
     */
    private void filterApps(String query) {
        if (query == null || query.trim().isEmpty()) {
            filteredApps = new ArrayList<>(allApps);
        } else {
            filteredApps = new ArrayList<>();
            String lowerQuery = query.toLowerCase().trim();
            
            for (AppInfo app : allApps) {
                if (app.getAppName().toLowerCase().contains(lowerQuery) ||
                    app.getPackageName().toLowerCase().contains(lowerQuery)) {
                    filteredApps.add(app);
                }
            }
        }
        
        adapter.setAppList(filteredApps);
        
        if (filteredApps.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            emptyView.setText("No apps match your search");
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Handle app click - navigate to set time limit
     */
    private void onAppClick(AppInfo appInfo) {
        String userRole = preferenceManager.getUserRole();
        
        if ("parent".equalsIgnoreCase(userRole)) {
            // Parent can set time limits
            String studentUID = getIntent().getStringExtra("student_uid");
            
            Intent intent = new Intent(this, SetAppTimeLimitActivity.class);
            intent.putExtra("package_name", appInfo.getPackageName());
            intent.putExtra("app_name", appInfo.getAppName());
            
            if (studentUID != null) {
                intent.putExtra("student_uid", studentUID);
            }
            
            startActivity(intent);
            
        } else if ("student".equalsIgnoreCase(userRole)) {
            // Student can only view app info
            String message = appInfo.getAppName() + "\n" +
                    "Package: " + appInfo.getPackageName() + "\n" +
                    "Usage Today: " + appInfo.getFormattedUsageTime();
            
            if (appInfo.getTimeLimit() > 0) {
                message += "\nLimit: " + appInfo.getFormattedTimeLimit();
                message += "\nRemaining: " + appInfo.getFormattedRemainingTime();
            }
            
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh apps when returning to this activity
        if (!allApps.isEmpty()) {
            loadInstalledApps();
        }
    }
}
