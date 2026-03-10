package service;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.util.Log;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsageStatsService {

    private static final String TAG = "UsageStatsService";
    private Context context;
    private UsageStatsManager usageStatsManager;

    public UsageStatsService(Context context) {
        this.context = context;
        this.usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
    }

    /**
     * Get usage stats for today
     */
    public Map<String, Long> getTodayUsageStats() {
        Map<String, Long> usageMap = new HashMap<>();
        
        long endTime = System.currentTimeMillis();
        long startTime = getStartOfDay();

        List<UsageStats> stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTime,
                endTime
        );

        if (stats != null) {
            for (UsageStats usageStats : stats) {
                String packageName = usageStats.getPackageName();
                long totalTime = usageStats.getTotalTimeInForeground();
                
                if (totalTime > 0) {
                    usageMap.put(packageName, totalTime);
                }
            }
        }

        Log.d(TAG, "Retrieved usage stats for " + usageMap.size() + " apps");
        return usageMap;
    }

    /**
     * Get usage for a specific app today
     */
    public long getAppUsageToday(String packageName) {
        long endTime = System.currentTimeMillis();
        long startTime = getStartOfDay();

        List<UsageStats> stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTime,
                endTime
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
     * Format usage time to readable string
     */
    public static String formatUsageTime(long milliseconds) {
        if (milliseconds <= 0) {
            return "0m";
        }

        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        if (hours > 0) {
            long remainingMinutes = minutes % 60;
            if (remainingMinutes > 0) {
                return hours + "h " + remainingMinutes + "m";
            }
            return hours + "h";
        } else if (minutes > 0) {
            return minutes + "m";
        } else {
            return seconds + "s";
        }
    }

    /**
     * Get start of today (midnight)
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
     * Check if usage stats permission is granted
     */
    public static boolean hasUsageStatsPermission(Context context) {
        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        long currentTime = System.currentTimeMillis();
        List<UsageStats> stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                currentTime - 1000 * 60,
                currentTime
        );
        return stats != null && !stats.isEmpty();
    }
}
