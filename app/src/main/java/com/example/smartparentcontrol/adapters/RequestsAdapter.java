package com.example.smartparentcontrol.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartparentcontrol.R;
import com.example.smartparentcontrol.models.TimeRequest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView Adapter for Parent's request approval screen
 * Shows student requests with approve/reject buttons
 */
public class RequestsAdapter extends RecyclerView.Adapter<RequestsAdapter.ViewHolder> {

    private List<TimeRequest> requestList;
    private OnRequestActionListener listener;

    public interface OnRequestActionListener {
        void onApprove(TimeRequest request);
        void onReject(TimeRequest request);
    }

    public RequestsAdapter(OnRequestActionListener listener) {
        this.requestList = new ArrayList<>();
        this.listener = listener;
    }

    public void setRequestList(List<TimeRequest> requestList) {
        if (requestList != null) {
            this.requestList = requestList;
        } else {
            this.requestList = new ArrayList<>();
        }
        notifyDataSetChanged();
    }

    public void updateRequest(TimeRequest updatedRequest) {
        for (int i = 0; i < requestList.size(); i++) {
            if (requestList.get(i).getRequestId() != null && 
                requestList.get(i).getRequestId().equals(updatedRequest.getRequestId())) {
                requestList.set(i, updatedRequest);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void removeRequest(String requestId) {
        for (int i = 0; i < requestList.size(); i++) {
            if (requestList.get(i).getRequestId() != null && 
                requestList.get(i).getRequestId().equals(requestId)) {
                requestList.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    public void addRequest(TimeRequest request) {
        requestList.add(0, request); // Add to top
        notifyItemInserted(0);
    }

    public void clearRequests() {
        requestList.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TimeRequest request = requestList.get(position);
        holder.bind(request);
    }

    @Override
    public int getItemCount() {
        return requestList != null ? requestList.size() : 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView studentName;
        TextView appName;
        TextView requestedTime;
        TextView reason;
        TextView timestamp;
        TextView statusBadge;
        Button approveButton;
        Button rejectButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            studentName = itemView.findViewById(R.id.studentName);
            appName = itemView.findViewById(R.id.appName);
            requestedTime = itemView.findViewById(R.id.requestedTime);
            reason = itemView.findViewById(R.id.reason);
            timestamp = itemView.findViewById(R.id.timestamp);
            statusBadge = itemView.findViewById(R.id.statusBadge);
            approveButton = itemView.findViewById(R.id.approveButton);
            rejectButton = itemView.findViewById(R.id.rejectButton);
        }

        void bind(TimeRequest request) {
            if (request == null) {
                return;
            }

            // Set student name
            if (request.getStudentName() != null && !request.getStudentName().isEmpty()) {
                studentName.setText("Student: " + request.getStudentName());
            } else {
                studentName.setText("Student: Unknown");
            }

            // Set app name
            if (request.getAppName() != null && !request.getAppName().isEmpty()) {
                appName.setText("App: " + request.getAppName());
            } else {
                appName.setText("App: Unknown");
            }

            // Set requested time with formatted display
            requestedTime.setText("Requested: " + request.getFormattedRequestedTime());

            // Set reason
            if (request.getReason() != null && !request.getReason().isEmpty()) {
                reason.setText("Reason: " + request.getReason());
                reason.setVisibility(View.VISIBLE);
            } else {
                reason.setText("Reason: No reason provided");
                reason.setVisibility(View.VISIBLE);
            }

            // Set timestamp
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy - hh:mm a", Locale.getDefault());
            timestamp.setText("Time: " + sdf.format(new Date(request.getTimestamp())));

            // Set status badge
            String status = request.getStatus() != null ? request.getStatus() : "pending";
            statusBadge.setText(status.substring(0, 1).toUpperCase() + status.substring(1));
            
            // Update status badge color and button states
            if (request.isPending()) {
                statusBadge.setBackgroundColor(Color.parseColor("#FF9800")); // Orange
                approveButton.setEnabled(true);
                rejectButton.setEnabled(true);
                approveButton.setVisibility(View.VISIBLE);
                rejectButton.setVisibility(View.VISIBLE);
            } else if (request.isApproved()) {
                statusBadge.setBackgroundColor(Color.parseColor("#4CAF50")); // Green
                approveButton.setEnabled(false);
                rejectButton.setEnabled(false);
                approveButton.setVisibility(View.GONE);
                rejectButton.setVisibility(View.GONE);
            } else if (request.isRejected()) {
                statusBadge.setBackgroundColor(Color.parseColor("#F44336")); // Red
                approveButton.setEnabled(false);
                rejectButton.setEnabled(false);
                approveButton.setVisibility(View.GONE);
                rejectButton.setVisibility(View.GONE);
            }

            // Set click listeners
            approveButton.setOnClickListener(v -> {
                if (listener != null && request.isPending()) {
                    listener.onApprove(request);
                }
            });

            rejectButton.setOnClickListener(v -> {
                if (listener != null && request.isPending()) {
                    listener.onReject(request);
                }
            });
        }
    }
}
