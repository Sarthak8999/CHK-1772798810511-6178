package com.example.smartparentcontrol.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import com.example.smartparentcontrol.models.AppLimit;
import com.example.smartparentcontrol.repository.AppLimitsRepository;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import ui.TimeLimitBlockActivity;
import utils.PreferenceManager;

/**
 * AppUsageMonitorService
 * Foreground service that monitors app usage and enforces time limits
 * 
 * Features:
 * - Detects current foreground app using UsageStatsManager
 * - Tracks daily usage time for each app
 * - Compares usage with Firestore limits
 * - Launches block screen when limit exceeded
 * - Runs checks every 5 seconds
 * - Real-time limit updates from Firestore
 */
public class AppUsageMonitorService extends Service {

    private static final String TAG = "AppUsageMonitorService";
    private static final String CHANNEL_ID = "app_monitoring_channel";
    private static final int NOTIFICATION_ID = 1001;
    private static final long CHECK_INTERVAL = 5000; // 5 seconds

    private Handler handler;
    private Runnable monitoringRunnable;
    private UsageStatsManager usageStatsManager;
    private PreferenceManager preferenceManager;
    private AppLimitsRepository limitsRepository;
    
    private Map<String, Long> appLimits = new HashMap<>(); // packageName -> limit in milliseconds
    private Map<String, Long> lastBlockTime = new HashMap<>(); // packageName -> last block timestamp
    private String currentForegroundApp = null;
    private boolean isMonitoring = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");

        initializeComponents();
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification());
        loadAppLimits();
        startMonitoring();
    }

    private void initializeComponents() {
        preferenceManager = new PreferenceManager(this);
        usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        limitsRepository = new AppLimitsRepository();
        handler = new Handler();
    }

    /**
     * Load app limits from Firestore and listen for changes
     */
    private void loadAppLimits() {
        String userId = preferenceManager.getUserId();
        
        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "User ID not found");
            return;
        }

        // Listen for real-time limit updates
        limitsRepository.listenToAppLimits(userId, new AppLimitsRepository.AppLimitsListener() {
            @Override
            public void onAppLimitsChanged(List<AppLimit> limits) {
                appLimits.clear();
                
                for (AppLimit limit : limits) {
                    long limitMillis = limit.getTimeLimitMinutes() * 60 * 1000L;
                    appLimits.put(limit.getPackageName(), limitMillis);
                }
                
                Log.d(TAG, "App limits updated: " + appLimits.size() + " apps with limits");
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to load limits: " + error);
            }
        });
    }

    /**
     * Start monitoring loop
     */
    private void startMonitoring() {
        if (isMonitoring) {
            return;
        }
        
        isMonitoring = true;
        Log.d(TAG, "Monitoring started");
        
        monitoringRunnable = new Runnable() {
            @Override
            public void run() {
                checkCurrentApp();
                handler.postDelayed(this, CHECK_INTERVAL);
            }
        };
        
        handler.post(monitoringRunnable);
    }

    /**
     * Stop monitoring loop
     */
    private void stopMonitoring() {
        if (handler != null && monitoringRunnable != null) {
            handler.removeCallbacks(monitoringRunnable);
            isMonitoring = false;
            Log.d(TAG, "Monitoring stopped");
        }
    }

    /**
     * Check current foreground app and enforce limits
     */
    private void checkCurrentApp() {
        String foregroundApp = getCurrentForegroundApp();
        
        if (foregroundApp == null || foregroundApp.equals(getPackageName())) {
            // No app or our own app is in foreground
            return;
        }
        
        currentForegroundApp = foregroundApp;
        
        // Check if this app has a limit
        if (appLimits.containsKey(foregroundApp)) {
            long usageToday = getAppUsageToday(foregroundApp);
            long limit = appLimits.get(foregroundApp);
            
            Log.d(TAG, String.format("App: %s, Usage: %dms, Limit: %dms", 
                foregroundApp, usageToday, limit));
            
            // Check if limit exceeded
            if (usageToday >= limit) {
                // Check if we recently blocked this app (prevent spam)
                if (shouldBlockApp(foregroundApp)) {
                    blockApp(foregroundApp, usageToday, limit);
                }
            }
        }
    }

    /**
     * Get current foreground app using UsageStatsManager
     */
    private String getCurrentForegroundApp() {
        long currentTime = System.currentTimeMillis();
        
        // Query usage stats for last 10 seconds
        List<UsageStats> stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                currentTime - 10000, // Last 10 seconds
                currentTime
        );

        if (stats != null && !stats.isEmpty()) {
            // Sort by last time used
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

    /**
     * Get total usage time for an app today
     */
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

    /**
     * Get start of current day timestamp
     */
    private long getStartOfDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    /**
     * Check if we should block the app (prevent spam blocking)
     */
    private boolean shouldBlockApp(String packageName) {
        Long lastBlock = lastBlockTime.get(packageName);
        
        if (lastBlock == null) {
            return true;
        }
        
        // Only block again if 10 seconds have passed
        long timeSinceLastBlock = System.currentTimeMillis() - lastBlock;
        return timeSinceLastBlock > 10000;
    }

    /**
     * Block the app by launching block screen
     */
    private void blockApp(String packageName, long usage, long limit) {
        Log.d(TAG, "Blocking app: " + packageName);
        
        // Record block time
        lastBlockTime.put(packageName, System.currentTimeMillis());
        
        // Launch block activity
        Intent blockIntent = new Intent(this, TimeLimitBlockActivity.class);
        blockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | 
                           Intent.FLAG_ACTIVITY_CLEAR_TOP |
                           Intent.FLAG_ACTIVITY_NO_HISTORY);
        blockIntent.putExtra("blocked_app", packageName);
        blockIntent.putExtra("usage_time", usage);
        blockIntent.putExtra("time_limit", limit);
        
        startActivity(blockIntent);
    }

    /**
     * Create notification for foreground service
     */
    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, getClass());
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, 
                PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("App Monitoring Active")
                .setContentText("Monitoring app usage and enforcing limits")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setShowWhen(false)
                .build();
    }

    /**
     * Create notification channel for Android O+
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "App Usage Monitoring",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Monitors app usage and enforces time limits");
            channel.setShowBadge(false);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");
        return START_STICKY; // Restart service if killed
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopMonitoring();
        Log.d(TAG, "Service destroyed");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // Not a bound service
    }

    /**
     * Get monitoring status
     */
    public boolean isMonitoring() {
        return isMonitoring;
    }

    /**
     * Get current foreground app
     */
    public String getCurrentApp() {
        return currentForegroundApp;
    }

    /**
     * Get number of apps with limits
     */
    public int getLimitedAppsCount() {
        return appLimits.size();
    }
}
