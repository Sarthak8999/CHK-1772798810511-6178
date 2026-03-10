package com.example.smartparentcontrol.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Repository for Firebase Authentication operations
 */
public class AuthRepository {

    private static final String TAG = "AuthRepository";
    private static final String COLLECTION_USERS = "users";

    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore firestore;

    public AuthRepository() {
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
    }

    /**
     * Register a new user with email and password
     */
    public Task<AuthResult> registerUser(String email, String password) {
        return firebaseAuth.createUserWithEmailAndPassword(email, password);
    }

    /**
     * Login user with email and password
     */
    public Task<AuthResult> loginUser(String email, String password) {
        return firebaseAuth.signInWithEmailAndPassword(email, password);
    }

    /**
     * Get current logged in user
     */
    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    /**
     * Logout current user
     */
    public void logout() {
        firebaseAuth.signOut();
    }

    /**
     * Create user profile in Firestore
     */
    public Task<Void> createUserProfile(String userId, String name, String email, String role) {
        Map<String, Object> user = new HashMap<>();
        user.put("userId", userId);
        user.put("name", name);
        user.put("email", email);
        user.put("role", role);
        user.put("createdAt", System.currentTimeMillis());

        return firestore.collection(COLLECTION_USERS)
                .document(userId)
                .set(user);
    }

    /**
     * Get user profile from Firestore
     */
    public Task<DocumentSnapshot> getUserProfile(String userId) {
        return firestore.collection(COLLECTION_USERS)
                .document(userId)
                .get();
    }

    /**
     * Update user profile
     */
    public Task<Void> updateUserProfile(String userId, Map<String, Object> updates) {
        return firestore.collection(COLLECTION_USERS)
                .document(userId)
                .update(updates);
    }

    /**
     * Check if user is logged in
     */
    public boolean isUserLoggedIn() {
        return firebaseAuth.getCurrentUser() != null;
    }

    /**
     * Get current user ID
     */
    public String getCurrentUserId() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    /**
     * Send password reset email
     */
    public Task<Void> sendPasswordResetEmail(String email) {
        return firebaseAuth.sendPasswordResetEmail(email);
    }

    /**
     * Delete user account
     */
    public Task<Void> deleteUserAccount() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            
            // Delete Firestore profile first
            return firestore.collection(COLLECTION_USERS)
                    .document(userId)
                    .delete()
                    .continueWithTask(task -> user.delete());
        }
        return null;
    }
}
