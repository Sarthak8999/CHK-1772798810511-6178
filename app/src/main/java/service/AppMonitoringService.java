package service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.smartparentcontrol.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import ui.TimeLimitBlockActivity;
import utils.PreferenceManager;

public class AppMonitoringService extends Service {

    private static final String TAG = "AppMonitoringService";
    private static final String CHANNEL_ID = "app_monitoring_channel";
    private static final int NOTIFICATION_ID = 1001;
    private static final long CHECK_INTERVAL = 5000; // Check every 5 seconds

    private Handler handler;
    private Runnable monitoringRunnable;
    private UsageStatsManager usageStatsManager;
    private PreferenceManager preferenceManager;
    private DatabaseReference limitsRef;
    private Map<String, Long> appLimits = new HashMap<>();
    private String currentBlockedApp = null;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");

        preferenceManager = new PreferenceManager(this);
        usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        handler = new Handler();

        // Initialize Firebase reference
        String studentUID = preferenceManager.getUserId();
        String parentUID = preferenceManager.getParentUID();

        if (parentUID != null && !parentUID.isEmpty()) {
            limitsRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(parentUID)
                    .child("studentsData")
                    .child(studentUID)
                    .child("appLimits");

            // Listen for limit changes
            listenForLimitChanges();
        }

        startForeground(NOTIFICATION_ID, createNotification());
        startMonitoring();
    }

    private void listenForLimitChanges() {
        limitsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                appLimits.clear();
                for (DataSnapshot appSnapshot : snapshot.getChildren()) {
                    String packageName = appSnapshot.getKey();
                    Long limitMinutes = appSnapshot.child("limitMinutes").getValue(Long.class);
                    if (limitMinutes != null) {
                        appLimits.put(packageName, limitMinutes * 60 * 1000); // Convert to milliseconds
                    }
                }
                Log.d(TAG, "App limits updated: " + appLimits.size() + " apps");
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Failed to load limits", error.toException());
            }
        });
    }

    private void startMonitoring() {
        monitoringRunnable = new Runnable() {
            @Override
            public void run() {
                checkCurrentApp();
                handler.postDelayed(this, CHECK_INTERVAL);
            }
        };
        handler.post(monitoringRunnable);
    }

    private void checkCurrentApp() {
        String currentApp = getCurrentForegroundApp();
        
        if (currentApp != null && !currentApp.equals(getPackageName())) {
            // Check if this app has a limit
            if (appLimits.containsKey(currentApp)) {
                long usageToday = getAppUsageToday(currentApp);
                long limit = appLimits.get(currentApp);

                Log.d(TAG, "App: " + currentApp + ", Usage: " + usageToday + "ms, Limit: " + limit + "ms");

                if (usageToday >= limit && !currentApp.equals(currentBlockedApp)) {
                    // Block the app
                    blockApp(currentApp);
                }
            }
        }
    }

    private String getCurrentForegroundApp() {
        long currentTime = System.currentTimeMillis();
        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        
        List<UsageStats> stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                currentTime - 1000 * 10, // Last 10 seconds
                currentTime
        );

        if (stats != null && !stats.isEmpty()) {
            SortedMap<Long, UsageStats> sortedStats = new TreeMap<>();
            for (UsageStats usageStats : stats) {
                sortedStats.put(usageStats.getLastTimeUsed(), usageStats);
            }
            
            if (!sortedStats.isEmpty()) {
                return sortedStats.get(sortedStats.lastKey()).getPackageName();
            }
        }
        
        return null;
    }

    private long getAppUsageToday(String packageName) {
        long currentTime = System.currentTimeMillis();
        long startOfDay = getStartOfDay();

        List<UsageStats> stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startOfDay,
                currentTime
        );

        if (stats != null) {
            for (UsageStats usageStats : stats) {
                if (usageStats.getPackageName().equals(packageName)) {
                    return usageStats.getTotalTimeInForeground();
                }
            }
        }

        return 0;
    }

    private long getStartOfDay() {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
        calendar.set(java.util.Calendar.MINUTE, 0);
        calendar.set(java.util.Calendar.SECOND, 0);
        calendar.set(java.util.Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private void blockApp(String packageName) {
        Log.d(TAG, "Blocking app: " + packageName);
        currentBlockedApp = packageName;

        Intent blockIntent = new Intent(this, TimeLimitBlockActivity.class);
        blockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        blockIntent.putExtra("blocked_app", packageName);
        startActivity(blockIntent);

        // Reset after 3 seconds
        handler.postDelayed(() -> currentBlockedApp = null, 3000);
    }

    private Notification createNotification() {
        createNotificationChannel();

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("App Monitoring Active")
                .setContentText("Monitoring app usage and limits")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "App Monitoring",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Monitors app usage and enforces time limits");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null && monitoringRunnable != null) {
            handler.removeCallbacks(monitoringRunnable);
        }
        Log.d(TAG, "Service destroyed");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
