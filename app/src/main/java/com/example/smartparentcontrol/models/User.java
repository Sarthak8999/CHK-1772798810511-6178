package com.example.smartparentcontrol.models;

import java.util.HashMap;
import java.util.Map;

/**
 * User Model
 * Represents a user in the Smart Parent Control app
 */
public class User {
    
    private String userId;
    private String name;
    private String email;
    private String role; // "Parent" or "Student"
    private long createdAt;
    private long lastLoginAt;

    // Empty constructor required for Firestore
    public User() {
    }

    public User(String userId, String name, String email, String role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
        this.createdAt = System.currentTimeMillis();
        this.lastLoginAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(long lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    // Helper methods
    public boolean isParent() {
        return "Parent".equalsIgnoreCase(role);
    }

    public boolean isStudent() {
        return "Student".equalsIgnoreCase(role);
    }

    public String getDisplayRole() {
        return role != null ? role : "Unknown";
    }

    // Convert to Map for Firestore
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("name", name);
        map.put("email", email);
        map.put("role", role);
        map.put("createdAt", createdAt);
        map.put("lastLoginAt", lastLoginAt);
        return map;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
