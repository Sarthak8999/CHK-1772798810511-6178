package com.example.smartparentcontrol.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.example.smartparentcontrol.services.AppUsageMonitorService;

/**
 * MonitoringServiceHelper
 * Utility class to start and stop the monitoring service
 */
public class MonitoringServiceHelper {

    private static final String TAG = "MonitoringServiceHelper";

    /**
     * Start the monitoring service
     * @param context Application context
     */
    public static void startMonitoringService(Context context) {
        try {
            Intent serviceIntent = new Intent(context, AppUsageMonitorService.class);
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
            
            Log.d(TAG, "Monitoring service started");
        } catch (Exception e) {
            Log.e(TAG, "Failed to start monitoring service", e);
        }
    }

    /**
     * Stop the monitoring service
     * @param context Application context
     */
    public static void stopMonitoringService(Context context) {
        try {
            Intent serviceIntent = new Intent(context, AppUsageMonitorService.class);
            context.stopService(serviceIntent);
            Log.d(TAG, "Monitoring service stopped");
        } catch (Exception e) {
            Log.e(TAG, "Failed to stop monitoring service", e);
        }
    }

    /**
     * Restart the monitoring service
     * @param context Application context
     */
    public static void restartMonitoringService(Context context) {
        stopMonitoringService(context);
        
        // Wait a moment before restarting
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        startMonitoringService(context);
    }
}
