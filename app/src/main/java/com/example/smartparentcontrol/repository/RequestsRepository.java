package com.example.smartparentcontrol.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

/**
 * Repository for managing time extension requests in Firestore
 */
public class RequestsRepository {

    private static final String TAG = "RequestsRepository";
    private static final String COLLECTION_REQUESTS = "requests";

    private final FirebaseFirestore firestore;

    public RequestsRepository() {
        this.firestore = FirebaseFirestore.getInstance();
    }

    /**
     * Create a new time extension request
     */
    public Task<DocumentReference> createRequest(String studentId, String appName, 
                                                  String packageName, int requestedMinutes, 
                                                  String reason, String type) {
        Map<String, Object> request = new HashMap<>();
        request.put("studentId", studentId);
        request.put("appName", appName);
        request.put("packageName", packageName);
        request.put("requestedMinutes", requestedMinutes);
        request.put("reason", reason);
        request.put("type", type);
        request.put("status", "pending");
        request.put("createdAt", System.currentTimeMillis());

        return firestore.collection(COLLECTION_REQUESTS)
                .add(request);
    }

    /**
     * Get all requests for a student
     */
    public Task<QuerySnapshot> getStudentRequests(String studentId) {
        return firestore.collection(COLLECTION_REQUESTS)
                .whereEqualTo("studentId", studentId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get();
    }

    /**
     * Get all pending requests for a parent (all their students)
     */
    public Task<QuerySnapshot> getPendingRequests() {
        return firestore.collection(COLLECTION_REQUESTS)
                .whereEqualTo("status", "pending")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get();
    }

    /**
     * Approve a request
     */
    public Task<Void> approveRequest(String requestId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "approved");
        updates.put("respondedAt", System.currentTimeMillis());

        return firestore.collection(COLLECTION_REQUESTS)
                .document(requestId)
                .update(updates);
    }

    /**
     * Reject a request
     */
    public Task<Void> rejectRequest(String requestId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "rejected");
        updates.put("respondedAt", System.currentTimeMillis());

        return firestore.collection(COLLECTION_REQUESTS)
                .document(requestId)
                .update(updates);
    }

    /**
     * Delete a request
     */
    public Task<Void> deleteRequest(String requestId) {
        return firestore.collection(COLLECTION_REQUESTS)
                .document(requestId)
                .delete();
    }

    /**
     * Get request by ID
     */
    public Task<com.google.firebase.firestore.DocumentSnapshot> getRequest(String requestId) {
        return firestore.collection(COLLECTION_REQUESTS)
                .document(requestId)
                .get();
    }

    /**
     * Get collection reference for real-time updates
     */
    public CollectionReference getRequestsCollection() {
        return firestore.collection(COLLECTION_REQUESTS);
    }

    /**
     * Query for student's requests (real-time listener)
     */
    public Query getStudentRequestsQuery(String studentId) {
        return firestore.collection(COLLECTION_REQUESTS)
                .whereEqualTo("studentId", studentId)
                .orderBy("createdAt", Query.Direction.DESCENDING);
    }

    /**
     * Query for pending requests (real-time listener)
     */
    public Query getPendingRequestsQuery() {
        return firestore.collection(COLLECTION_REQUESTS)
                .whereEqualTo("status", "pending")
                .orderBy("createdAt", Query.Direction.DESCENDING);
    }

    /**
     * Update request status
     */
    public Task<Void> updateRequestStatus(String requestId, String status) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status);
        updates.put("updatedAt", System.currentTimeMillis());

        return firestore.collection(COLLECTION_REQUESTS)
                .document(requestId)
                .update(updates);
    }
}
