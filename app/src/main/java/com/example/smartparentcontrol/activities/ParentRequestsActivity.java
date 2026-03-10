package com.example.smartparentcontrol.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartparentcontrol.R;
import com.example.smartparentcontrol.adapters.RequestsAdapter;
import com.example.smartparentcontrol.models.AppLimit;
import com.example.smartparentcontrol.models.TimeRequest;
import com.example.smartparentcontrol.repository.AppLimitsRepository;
import com.example.smartparentcontrol.repository.RequestsRepository;

import java.util.List;

import utils.PreferenceManager;

/**
 * ParentRequestsActivity
 * Allows parents to view and manage student time extension requests
 * 
 * Features:
 * - View all pending requests
 * - Approve requests (extends time limit)
 * - Reject requests
 * - Real-time updates from Firestore
 * - Automatic time limit extension on approval
 */
public class ParentRequestsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyView;
    private RequestsAdapter adapter;
    
    private PreferenceManager preferenceManager;
    private RequestsRepository requestsRepository;
    private AppLimitsRepository limitsRepository;
    
    private String parentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_requests);

        initViews();
        setupToolbar();
        initializeComponents();
        
        if (loadUserInfo()) {
            setupRecyclerView();
            loadRequests();
        } else {
            Toast.makeText(this, "Error: User information not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Student Requests");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void initializeComponents() {
        preferenceManager = new PreferenceManager(this);
        requestsRepository = new RequestsRepository();
        limitsRepository = new AppLimitsRepository();
    }

    private boolean loadUserInfo() {
        parentId = preferenceManager.getUserId();
        return parentId != null;
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RequestsAdapter(new RequestsAdapter.OnRequestActionListener() {
            @Override
            public void onApprove(TimeRequest request) {
                confirmApprove(request);
            }
            
            @Override
            public void onReject(TimeRequest request) {
                confirmReject(request);
            }
        });
        recyclerView.setAdapter(adapter);
    }

    /**
     * Load parent's requests from Firestore with real-time updates
     */
    private void loadRequests() {
        progressBar.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        
        requestsRepository.listenToParentRequests(parentId, 
            new RequestsRepository.RequestsListener() {
                @Override
                public void onRequestsChanged(List<TimeRequest> requests) {
                    progressBar.setVisibility(View.GONE);
                    
                    if (requests.isEmpty()) {
                        emptyView.setVisibility(View.VISIBLE);
                        emptyView.setText("No requests from students");
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        emptyView.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        adapter.setRequestList(requests);
                    }
                }
                
                @Override
                public void onError(String error) {
                    progressBar.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                    emptyView.setText("Error loading requests");
                    Toast.makeText(ParentRequestsActivity.this, 
                        "Error: " + error, 
                        Toast.LENGTH_SHORT).show();
                }
            });
    }

    /**
     * Confirm approval with dialog
     */
    private void confirmApprove(TimeRequest request) {
        String message = "Approve " + request.getFormattedRequestedTime() + 
                        " for " + request.getAppName() + "?";
        
        if (request.getReason() != null && !request.getReason().isEmpty()) {
            message += "\n\nReason: " + request.getReason();
        }
        
        new AlertDialog.Builder(this)
            .setTitle("Approve Request")
            .setMessage(message)
            .setPositiveButton("Approve", (dialog, which) -> approveRequest(request))
            .setNegativeButton("Cancel", null)
            .show();
    }

    /**
     * Confirm rejection with dialog
     */
    private void confirmReject(TimeRequest request) {
        String message = "Reject request for " + request.getAppName() + "?";
        
        new AlertDialog.Builder(this)
            .setTitle("Reject Request")
            .setMessage(message)
            .setPositiveButton("Reject", (dialog, which) -> rejectRequest(request))
            .setNegativeButton("Cancel", null)
            .show();
    }

    /**
     * Approve request and extend time limit
     */
    private void approveRequest(TimeRequest request) {
        progressBar.setVisibility(View.VISIBLE);
        
        // First, update request status
        requestsRepository.updateRequestStatus(request.getRequestId(), "approved", 
            new RequestsRepository.OperationCallback() {
                @Override
                public void onSuccess() {
                    // Then extend time limit
                    extendTimeLimit(request);
                }
                
                @Override
                public void onFailure(String error) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ParentRequestsActivity.this, 
                        "Failed to approve: " + error, 
                        Toast.LENGTH_SHORT).show();
                }
            });
    }

    /**
     * Extend time limit in Firestore
     */
    private void extendTimeLimit(TimeRequest request) {
        String studentId = request.getStudentId();
        String packageName = request.getPackageName();
        int additionalMinutes = request.getRequestedMinutes();
        
        // Get current limit
        limitsRepository.getAppLimit(studentId, packageName, 
            new AppLimitsRepository.AppLimitCallback() {
                @Override
                public void onSuccess(AppLimit currentLimit) {
                    int newLimit;
                    
                    if (currentLimit != null) {
                        // Add to existing limit
                        newLimit = currentLimit.getTimeLimitMinutes() + additionalMinutes;
                    } else {
                        // Set new limit
                        newLimit = additionalMinutes;
                    }
                    
                    // Update limit in Firestore
                    limitsRepository.setAppLimit(studentId, packageName, newLimit, 
                        new AppLimitsRepository.OperationCallback() {
                            @Override
                            public void onSuccess() {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(ParentRequestsActivity.this, 
                                    "Request approved and limit extended", 
                                    Toast.LENGTH_SHORT).show();
                            }
                            
                            @Override
                            public void onFailure(String error) {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(ParentRequestsActivity.this, 
                                    "Approved but failed to extend limit: " + error, 
                                    Toast.LENGTH_SHORT).show();
                            }
                        });
                }
                
                @Override
                public void onFailure(String error) {
                    // If no current limit, just set the requested minutes as new limit
                    limitsRepository.setAppLimit(studentId, packageName, additionalMinutes, 
                        new AppLimitsRepository.OperationCallback() {
                            @Override
                            public void onSuccess() {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(ParentRequestsActivity.this, 
                                    "Request approved and limit set", 
                                    Toast.LENGTH_SHORT).show();
                            }
                            
                            @Override
                            public void onFailure(String error2) {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(ParentRequestsActivity.this, 
                                    "Approved but failed to set limit: " + error2, 
                                    Toast.LENGTH_SHORT).show();
                            }
                        });
                }
            });
    }

    /**
     * Reject request
     */
    private void rejectRequest(TimeRequest request) {
        progressBar.setVisibility(View.VISIBLE);
        
        requestsRepository.updateRequestStatus(request.getRequestId(), "rejected", 
            new RequestsRepository.OperationCallback() {
                @Override
                public void onSuccess() {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ParentRequestsActivity.this, 
                        "Request rejected", 
                        Toast.LENGTH_SHORT).show();
                }
                
                @Override
                public void onFailure(String error) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ParentRequestsActivity.this, 
                        "Failed to reject: " + error, 
                        Toast.LENGTH_SHORT).show();
                }
            });
    }
}
