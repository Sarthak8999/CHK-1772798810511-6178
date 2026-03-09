package model;

import java.util.HashMap;
import java.util.Map;

public class User {
    private String uid;
    private String email;
    private String fullName;
    private String role;
    private String referralCode;
    private String parentUID;
    private Map<String, Boolean> students;
    private long registrationDate;

    public User() {

        this.students = new HashMap<>();
    }

    public User(String uid, String email, String fullName, String role) {
        this.uid = uid;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.students = new HashMap<>();
        this.registrationDate = System.currentTimeMillis();
    }


    public User(String uid, String email, String fullName, String role, String referralCode) {
        this.uid = uid;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.referralCode = referralCode;
        this.students = new HashMap<>();
        this.registrationDate = System.currentTimeMillis();
    }


    public User(String uid, String email, String fullName, String role, String parentUID, boolean isStudent) {
        this.uid = uid;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.parentUID = parentUID;
        this.students = new HashMap<>();
        this.registrationDate = System.currentTimeMillis();
    }


    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getReferralCode() {
        return referralCode;
    }

    public void setReferralCode(String referralCode) {
        this.referralCode = referralCode;
    }

    public String getParentUID() {
        return parentUID;
    }

    public void setParentUID(String parentUID) {
        this.parentUID = parentUID;
    }

    public Map<String, Boolean> getStudents() {
        return students;
    }

    public void setStudents(Map<String, Boolean> students) {
        this.students = students;
    }

    public long getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(long registrationDate) {
        this.registrationDate = registrationDate;
    }


    public boolean isParent() {
        return "parent".equalsIgnoreCase(role);
    }

    public boolean isStudent() {
        return "student".equalsIgnoreCase(role);
    }

    public void addStudent(String studentUID) {
        if (students == null) {
            students = new HashMap<>();
        }
        students.put(studentUID, true);
    }

    public void removeStudent(String studentUID) {
        if (students != null) {
            students.remove(studentUID);
        }
    }

    public boolean hasStudent(String studentUID) {
        return students != null && students.containsKey(studentUID);
    }

    public int getStudentCount() {
        return students != null ? students.size() : 0;
    }

    // Convert to Map for Firebase
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("uid", uid);
        map.put("email", email);
        map.put("fullName", fullName);
        map.put("role", role);
        map.put("registrationDate", registrationDate);

        if (referralCode != null) {
            map.put("referralCode", referralCode);
        }

        if (parentUID != null) {
            map.put("parentUID", parentUID);
        }

        if (students != null && !students.isEmpty()) {
            map.put("students", students);
        }

        return map;
    }
}
