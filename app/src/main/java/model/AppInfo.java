package model;

import android.graphics.drawable.Drawable;

public class AppInfo {
    private String appName;
    private String packageName;
    private Drawable appIcon;
    private long usageTime;
    private long timeLimit;
    private boolean isBlocked;

    public AppInfo() {

    }

    public AppInfo(String appName, String packageName, Drawable appIcon) {
        this.appName = appName;
        this.packageName = packageName;
        this.appIcon = appIcon;
        this.usageTime = 0;
        this.timeLimit = 0;
        this.isBlocked = false;
    }

    public AppInfo(String appName, String packageName, Drawable appIcon, long usageTime) {
        this.appName = appName;
        this.packageName = packageName;
        this.appIcon = appIcon;
        this.usageTime = usageTime;
        this.timeLimit = 0;
        this.isBlocked = false;
    }


    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(Drawable appIcon) {
        this.appIcon = appIcon;
    }

    public long getUsageTime() {
        return usageTime;
    }

    public void setUsageTime(long usageTime) {
        this.usageTime = usageTime;
    }

    public long getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(long timeLimit) {
        this.timeLimit = timeLimit;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }


    public String getFormattedUsageTime() {
        if (usageTime <= 0) {
            return "0m";
        }

        long seconds = usageTime / 1000;
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

    // Helper method to format time limit
    public String getFormattedTimeLimit() {
        if (timeLimit <= 0) {
            return "No limit";
        }

        long seconds = timeLimit / 1000;
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


    public boolean hasExceededLimit() {
        return timeLimit > 0 && usageTime >= timeLimit;
    }


    public long getRemainingTime() {
        if (timeLimit <= 0) {
            return Long.MAX_VALUE;
        }
        long remaining = timeLimit - usageTime;
        return remaining > 0 ? remaining : 0;
    }
}
