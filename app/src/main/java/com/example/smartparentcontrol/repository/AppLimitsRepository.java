package com.example.smartparentcontrol.repository;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

/**
 * Repository for managing app time limits in Firestore
 */
public class AppLimitsRepository {

    private static final String TAG = "AppLimitsRepository";
    private static final String COLLECTION_APP_LIMITS = "app_limits";

    private final FirebaseFirestore firestore;

    public AppLimitsRepository() {
        this.firestore = FirebaseFirestore.getInstance();
    }

    /**
     * Set time limit for an app
     */
    public Task<Void> setAppLimit(String userId, String packageName, int timeLimitMinutes) {
        String documentId = userId + "_" + packageName.replace(".", "_");
        
        Map<String, Object> limitData = new HashMap<>();
        limitData.put("userId", userId);
        limitData.put("packageName", packageName);
        limitData.put("timeLimitMinutes", timeLimitMinutes);
        limitData.put("updatedAt", System.currentTimeMillis());

        return firestore.collection(COLLECTION_APP_LIMITS)
                .document(documentId)
                .set(limitData);
    }

    /**
     * Get time limit for a specific app
     */
    public Task<DocumentSnapshot> getAppLimit(String userId, String packageName) {
        String documentId = userId + "_" + packageName.replace(".", "_");
        
        return firestore.collection(COLLECTION_APP_LIMITS)
                .document(documentId)
                .get();
    }

    /**
     * Get all app limits for a user
     */
    public Task<QuerySnapshot> getAllAppLimits(String userId) {
        return firestore.collection(COLLECTION_APP_LIMITS)
                .whereEqualTo("userId", userId)
                .get();
    }

    /**
     * Remove time limit for an app
     */
    public Task<Void> removeAppLimit(String userId, String packageName) {
        String documentId = userId + "_" + packageName.replace(".", "_");
        
        return firestore.collection(COLLECTION_APP_LIMITS)
                .document(documentId)
                .delete();
    }

    /**
     * Update time limit for an app
     */
    public Task<Void> updateAppLimit(String userId, String packageName, int newTimeLimitMinutes) {
        String documentId = userId + "_" + packageName.replace(".", "_");
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("timeLimitMinutes", newTimeLimitMinutes);
        updates.put("updatedAt", System.currentTimeMillis());

        return firestore.collection(COLLECTION_APP_LIMITS)
                .document(documentId)
                .update(updates);
    }

    /**
     * Check if app has a limit set
     */
    public Task<Boolean> hasAppLimit(String userId, String packageName) {
        String documentId = userId + "_" + packageName.replace(".", "_");
        
        return firestore.collection(COLLECTION_APP_LIMITS)
                .document(documentId)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        return task.getResult().exists();
                    }
                    return false;
                });
    }

    /**
     * Get collection reference for real-time updates
     */
    public CollectionReference getAppLimitsCollection() {
        return firestore.collection(COLLECTION_APP_LIMITS);
    }

    /**
     * Query for real-time listener
     */
    public Query getAppLimitsQuery(String userId) {
        return firestore.collection(COLLECTION_APP_LIMITS)
                .whereEqualTo("userId", userId);
    }
}
