package com.example.smartparentcontrol.models;

import java.util.HashMap;
import java.util.Map;

/**
 * TimeRequest Model
 * Represents a student's request for additional app time
 */
public class TimeRequest {
    
    private String requestId;
    private String studentId;
    private String parentId;
    private String studentName;
    private String appName;
    private String packageName;
    private String type; // "time_extension", "unblock"
    private String status; // "pending", "approved", "rejected"
    private int requestedMinutes;
    private String reason;
    private long timestamp;
    private long respondedAt;

    // Empty constructor required for Firestore
    public TimeRequest() {
    }

    public TimeRequest(String studentId, String parentId, String studentName, 
                      String appName, String packageName, int requestedMinutes, 
                      String reason, String type) {
        this.studentId = studentId;
        this.parentId = parentId;
        this.studentName = studentName;
        this.appName = appName;
        this.packageName = packageName;
        this.requestedMinutes = requestedMinutes;
        this.reason = reason;
        this.type = type != null ? type : "time_extension";
        this.status = "pending";
        this.timestamp = System.currentTimeMillis();
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

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getRespondedAt() {
        return respondedAt;
    }

    public void setRespondedAt(long respondedAt) {
        this.respondedAt = respondedAt;
    }

    // Helper methods
    public boolean isPending() {
        return "pending".equalsIgnoreCase(status);
    }

    public boolean isApproved() {
        return "approved".equalsIgnoreCase(status);
    }

    public boolean isRejected() {
        return "rejected".equalsIgnoreCase(status);
    }

    public String getFormattedRequestedTime() {
        if (requestedMinutes <= 0) {
            return "0 minutes";
        }
        
        int hours = requestedMinutes / 60;
        int minutes = requestedMinutes % 60;
        
        if (hours > 0 && minutes > 0) {
            return hours + " hour" + (hours > 1 ? "s" : "") + " " + 
                   minutes + " minute" + (minutes > 1 ? "s" : "");
        } else if (hours > 0) {
            return hours + " hour" + (hours > 1 ? "s" : "");
        } else {
            return minutes + " minute" + (minutes > 1 ? "s" : "");
        }
    }

    public String getStatusColor() {
        switch (status.toLowerCase()) {
            case "approved":
                return "#4CAF50"; // Green
            case "rejected":
                return "#F44336"; // Red
            case "pending":
            default:
                return "#FF9800"; // Orange
        }
    }

    public String getTypeDisplayName() {
        if ("time_extension".equals(type)) {
            return "Time Extension";
        } else if ("unblock".equals(type)) {
            return "Unblock Request";
        }
        return type;
    }

    // Convert to Map for Firestore
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("requestId", requestId);
        map.put("studentId", studentId);
        map.put("parentId", parentId);
        map.put("studentName", studentName);
        map.put("appName", appName);
        map.put("packageName", packageName);
        map.put("type", type);
        map.put("status", status);
        map.put("requestedMinutes", requestedMinutes);
        map.put("reason", reason);
        map.put("timestamp", timestamp);
        if (respondedAt > 0) {
            map.put("respondedAt", respondedAt);
        }
        return map;
    }

    @Override
    public String toString() {
        return "TimeRequest{" +
                "requestId='" + requestId + '\'' +
                ", studentName='" + studentName + '\'' +
                ", appName='" + appName + '\'' +
                ", requestedMinutes=" + requestedMinutes +
                ", status='" + status + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
