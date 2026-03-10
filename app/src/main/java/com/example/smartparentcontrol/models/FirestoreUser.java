package com.example.smartparentcontrol.models;

/**
 * Firestore User Model
 * Represents user data stored in Firestore
 */
public class FirestoreUser {
    
    private String userId;
    private String name;
    private String email;
    private String role; // "Parent" or "Student"
    private long createdAt;

    // Empty constructor required for Firestore
    public FirestoreUser() {
    }

    public FirestoreUser(String userId, String name, String email, String role, long createdAt) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
        this.createdAt = createdAt;
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

    public boolean isParent() {
        return "Parent".equalsIgnoreCase(role);
    }

    public boolean isStudent() {
        return "Student".equalsIgnoreCase(role);
    }
}
