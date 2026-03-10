package com.example.smartparentcontrol.models;

/**
 * Firestore Time Extension Request Model
 * Represents a student's request for more app time
 */
public class TimeExtensionRequest {
    
    private String requestId;
    private String studentId;
    private String appName;
    private String packageName;
    private int requestedMinutes;
    private String reason;
    private String type; // "time_extension", "unblock", etc.
    private String status; // "pending", "approved", "rejected"
    private long createdAt;
    private long respondedAt;

    // Empty constructor required for Firestore
    public TimeExtensionRequest() {
    }

    public TimeExtensionRequest(String requestId, String studentId, String appName, 
                               String packageName, int requestedMinutes, String reason, 
                               String type, String status, long createdAt) {
        this.requestId = requestId;
        this.studentId = studentId;
        this.appName = appName;
        this.packageName = packageName;
        this.requestedMinutes = requestedMinutes;
        this.reason = reason;
        this.type = type;
        this.status = status;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
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

    public int getRequestedMinutes() {
        return requestedMinutes;
    }

    public void setRequestedMinutes(int requestedMinutes) {
        this.requestedMinutes = requestedMinutes;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getRespondedAt() {
        return respondedAt;
    }

    public void setRespondedAt(long respondedAt) {
        this.respondedAt = respondedAt;
    }

    public boolean isPending() {
        return "pending".equalsIgnoreCase(status);
    }

    public boolean isApproved() {
        return "approved".equalsIgnoreCase(status);
    }

    public boolean isRejected() {
        return "rejected".equalsIgnoreCase(status);
    }
}
