package com.example.smartparentcontrol.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartparentcontrol.R;
import com.example.smartparentcontrol.adapters.StudentRequestsAdapter;
import com.example.smartparentcontrol.models.TimeRequest;
import com.example.smartparentcontrol.repository.RequestsRepository;
import com.example.smartparentcontrol.utils.AppScanner;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import utils.PreferenceManager;

/**
 * StudentRequestsActivity
 * Displays student's request history and allows creating new requests
 * 
 * Features:
 * - View all requests (pending, approved, rejected)
 * - Real-time updates from Firestore
 * - Create new time extension requests
 * - Create unblock requests
 * - Status-based filtering
 * - Request details dialog
 */
public class StudentRequestsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyView;
    private FloatingActionButton fabNewRequest;
    private StudentRequestsAdapter adapter;
    
    private PreferenceManager preferenceManager;
    private RequestsRepository requestsRepository;
    private AppScanner appScanner;
    
    private String studentId;
    private String parentId;
    private String studentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_requests);

        initViews();
        setupToolbar();
        initializeComponents();
        
        if (loadUserInfo()) {
            setupRecyclerView();
            setupFab();
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
        fabNewRequest = findViewById(R.id.fabNewRequest);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Requests");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void initializeComponents() {
        preferenceManager = new PreferenceManager(this);
        requestsRepository = new RequestsRepository();
        appScanner = new AppScanner(this);
    }

    private boolean loadUserInfo() {
        studentId = preferenceManager.getUserId();
        parentId = preferenceManager.getParentUID();
        studentName = preferenceManager.getUserName();
        
        return studentId != null && parentId != null;
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StudentRequestsAdapter(this::showRequestDetails);
        recyclerView.setAdapter(adapter);
    }

    private void setupFab() {
        fabNewRequest.setOnClickListener(v -> showNewRequestDialog());
    }

    /**
     * Load student's requests from Firestore with real-time updates
     */
    private void loadRequests() {
        progressBar.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        
        requestsRepository.listenToStudentRequests(studentId, 
            new RequestsRepository.RequestsListener() {
                @Override
                public void onRequestsChanged(List<TimeRequest> requests) {
                    progressBar.setVisibility(View.GONE);
                    
                    if (requests.isEmpty()) {
                        emptyView.setVisibility(View.VISIBLE);
                        emptyView.setText("No requests yet\nTap + to create a request");
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
                    Toast.makeText(StudentRequestsActivity.this, 
                        "Error: " + error, 
                        Toast.LENGTH_SHORT).show();
                }
            });
    }

    /**
     * Show dialog to create new request
     */
    private void showNewRequestDialog() {
        View dialogView = LayoutInflater.from(this)
            .inflate(R.layout.dialog_new_request, null);
        
        EditText appNameInput = dialogView.findViewById(R.id.appNameInput);
        EditText minutesInput = dialogView.findViewById(R.id.minutesInput);
        EditText reasonInput = dialogView.findViewById(R.id.reasonInput);
        
        new AlertDialog.Builder(this)
            .setTitle("New Request")
            .setMessage("Request additional time for an app")
            .setView(dialogView)
            .setPositiveButton("Send Request", (dialog, which) -> {
                String appName = appNameInput.getText().toString().trim();
                String minutesStr = minutesInput.getText().toString().trim();
                String reason = reasonInput.getText().toString().trim();
                
                if (appName.isEmpty()) {
                    Toast.makeText(this, "Please enter app name", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                if (minutesStr.isEmpty()) {
                    Toast.makeText(this, "Please enter minutes", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                try {
                    int minutes = Integer.parseInt(minutesStr);
                    if (minutes <= 0) {
                        Toast.makeText(this, "Minutes must be greater than 0", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    createRequest(appName, minutes, reason);
                    
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Invalid number", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    /**
     * Create and send request to Firestore
     */
    private void createRequest(String appName, int minutes, String reason) {
        // Create request
        TimeRequest request = new TimeRequest(
            studentId,
            parentId,
            studentName != null ? studentName : "Student",
            appName,
            "unknown", // Package name unknown for manual requests
            minutes,
            reason != null && !reason.isEmpty() ? reason : "No reason provided",
            "time_extension"
        );
        
        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        
        // Send to Firestore
        requestsRepository.createRequest(request, new RequestsRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(StudentRequestsActivity.this, 
                    "Request sent successfully", 
                    Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(StudentRequestsActivity.this, 
                    "Failed to send request: " + error, 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Show request details dialog
     */
    private void showRequestDetails(TimeRequest request) {
        String status = request.getStatus();
        String statusColor = request.isApproved() ? "✓ Approved" : 
                           request.isRejected() ? "✗ Rejected" : 
                           "⏳ Pending";
        
        String message = "App: " + request.getAppName() + "\n" +
                        "Time Requested: " + request.getFormattedRequestedTime() + "\n" +
                        "Reason: " + request.getReason() + "\n" +
                        "Status: " + statusColor + "\n" +
                        "Sent: " + new java.text.SimpleDateFormat("dd MMM yyyy - hh:mm a", 
                            java.util.Locale.getDefault()).format(new java.util.Date(request.getTimestamp()));
        
        new AlertDialog.Builder(this)
            .setTitle("Request Details")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show();
    }
}
