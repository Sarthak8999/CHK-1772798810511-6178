package com.example.smartparentcontrol.models;

/**
 * Firestore App Limit Model
 * Represents time limit data for apps
 */
public class AppLimit {
    
    private String userId;
    private String packageName;
    private int timeLimitMinutes;
    private long updatedAt;

    // Empty constructor required for Firestore
    public AppLimit() {
    }

    public AppLimit(String userId, String packageName, int timeLimitMinutes, long updatedAt) {
        this.userId = userId;
        this.packageName = packageName;
        this.timeLimitMinutes = timeLimitMinutes;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public int getTimeLimitMinutes() {
        return timeLimitMinutes;
    }

    public void setTimeLimitMinutes(int timeLimitMinutes) {
        this.timeLimitMinutes = timeLimitMinutes;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public long getTimeLimitMillis() {
        return timeLimitMinutes * 60 * 1000L;
    }
}
