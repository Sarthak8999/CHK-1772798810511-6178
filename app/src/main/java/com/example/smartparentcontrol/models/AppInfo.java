package com.example.smartparentcontrol.models;

import android.graphics.drawable.Drawable;

/**
 * AppInfo Model
 * Represents an installed app with usage and limit information
 */
public class AppInfo {
    
    private String appName;
    private String packageName;
    private Drawable icon;
    private long usageTime; // in milliseconds
    private long timeLimit; // in milliseconds
    private boolean isBlocked;
    private boolean isSystemApp;

    // Empty constructor
    public AppInfo() {
    }

    public AppInfo(String appName, String packageName, Drawable icon) {
        this.appName = appName;
        this.packageName = packageName;
        this.icon = icon;
        this.usageTime = 0;
        this.timeLimit = 0;
        this.isBlocked = false;
        this.isSystemApp = false;
    }

    public AppInfo(String appName, String packageName, Drawable icon, long usageTime, long timeLimit) {
        this.appName = appName;
        this.packageName = packageName;
        this.icon = icon;
        this.usageTime = usageTime;
        this.timeLimit = timeLimit;
        this.isBlocked = false;
        this.isSystemApp = false;
    }

    // Getters and Setters
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

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
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

    public boolean isSystemApp() {
        return isSystemApp;
    }

    public void setSystemApp(boolean systemApp) {
        isSystemApp = systemApp;
    }

    // Helper methods
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

    public String getFormattedRemainingTime() {
        long remaining = getRemainingTime();
        if (remaining == Long.MAX_VALUE) {
            return "Unlimited";
        }
        if (remaining <= 0) {
            return "Time's up";
        }

        long seconds = remaining / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        if (hours > 0) {
            long remainingMinutes = minutes % 60;
            if (remainingMinutes > 0) {
                return hours + "h " + remainingMinutes + "m left";
            }
            return hours + "h left";
        } else if (minutes > 0) {
            return minutes + "m left";
        } else {
            return seconds + "s left";
        }
    }

    public int getUsagePercentage() {
        if (timeLimit <= 0) {
            return 0;
        }
        return (int) ((usageTime * 100) / timeLimit);
    }

    @Override
    public String toString() {
        return "AppInfo{" +
                "appName='" + appName + '\'' +
                ", packageName='" + packageName + '\'' +
                ", usageTime=" + getFormattedUsageTime() +
                ", timeLimit=" + getFormattedTimeLimit() +
                ", isBlocked=" + isBlocked +
                '}';
    }
}
