package model;

public class TimeRequest {
    private String id;
    private String studentUID;
    private String studentName;
    private String parentUID;
    private String appName;
    private String appPackageName;
    private int requestedMinutes;
    private String reason;
    private long timestamp;
    private String status;

    public TimeRequest() {

    }

    public TimeRequest(String studentUID, String studentName, String parentUID, 
                      String appName, String appPackageName, int requestedMinutes, 
                      String reason, long timestamp) {
        this.studentUID = studentUID;
        this.studentName = studentName;
        this.parentUID = parentUID;
        this.appName = appName;
        this.appPackageName = appPackageName;
        this.requestedMinutes = requestedMinutes;
        this.reason = reason;
        this.timestamp = timestamp;
        this.status = "pending";
    }


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

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getParentUID() {
        return parentUID;
    }

    public void setParentUID(String parentUID) {
        this.parentUID = parentUID;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppPackageName() {
        return appPackageName;
    }

    public void setAppPackageName(String appPackageName) {
        this.appPackageName = appPackageName;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
