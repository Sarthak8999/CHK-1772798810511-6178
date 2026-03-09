package com.studentparent.monitor.model;

public class TimeRequest {
    
    // Private fields for Firebase compatibility
    private String id;
    private String studentUID;
    private String parentUID;
    private String studentName;
    private String appName;
    private int requestedMinutes;
    private String reason;
    private String status;
    private long timestamp;

    // Empty constructor required for Firebase Realtime Database
    public TimeRequest() {
        // Default constructor
    }

    // Full constructor with all fields
    public TimeRequest(String id, String studentUID, String parentUID, String studentName, 
                      String appName, int requestedMinutes, String reason, 
                      String status, long timestamp) {
        this.id = id;
        this.studentUID = studentUID;
        this.parentUID = parentUID;
        this.studentName = studentName;
        this.appName = appName;
        this.requestedMinutes = requestedMinutes;
        this.reason = reason;
        this.status = status != null ? status : "pending";
        this.timestamp = timestamp;
    }

    // Constructor without id (for creating new requests)
    public TimeRequest(String studentUID, String parentUID, String studentName, 
                      String appName, int requestedMinutes, String reason, long timestamp) {
        this.studentUID = studentUID;
        this.parentUID = parentUID;
        this.studentName = studentName;
        this.appName = appName;
        this.requestedMinutes = requestedMinutes;
        this.reason = reason;
        this.status = "pending";
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStudentUID() {
        return studentUID;
    }

    public void setStudentUID(String studentUID) {
        this.studentUID = studentUID;
    }

    public String getParentUID() {
        return parentUID;
    }

    public void setParentUID(String parentUID) {
        this.parentUID = parentUID;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
