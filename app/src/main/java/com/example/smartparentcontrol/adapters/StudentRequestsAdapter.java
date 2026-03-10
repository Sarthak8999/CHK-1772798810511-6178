package com.example.smartparentcontrol.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
 * RecyclerView Adapter for Student's request history screen
 * Shows student's own requests with status
 */
public class StudentRequestsAdapter extends RecyclerView.Adapter<StudentRequestsAdapter.ViewHolder> {

    private List<TimeRequest> requestList;
    private OnRequestClickListener listener;

    public interface OnRequestClickListener {
        void onRequestClick(TimeRequest request);
    }

    public StudentRequestsAdapter() {
        this.requestList = new ArrayList<>();
    }

    public StudentRequestsAdapter(OnRequestClickListener listener) {
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

    public void setOnRequestClickListener(OnRequestClickListener listener) {
        this.listener = listener;
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

    public void addRequest(TimeRequest request) {
        requestList.add(0, request); // Add to top
        notifyItemInserted(0);
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

    public void clearRequests() {
        requestList.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TimeRequest request = requestList.get(position);
        holder.bind(request, listener);
    }

    @Override
    public int getItemCount() {
        return requestList != null ? requestList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView requestType;
        TextView requestedTime;
        TextView timestamp;
        TextView statusBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            requestType = itemView.findViewById(R.id.requestType);
            requestedTime = itemView.findViewById(R.id.requestedTime);
            timestamp = itemView.findViewById(R.id.timestamp);
            statusBadge = itemView.findViewById(R.id.statusBadge);
        }

        void bind(TimeRequest request, OnRequestClickListener listener) {
            if (request == null) {
                return;
            }

            // Set app name with type
            if (request.getAppName() != null && !request.getAppName().isEmpty()) {
                String typeDisplay = request.getTypeDisplayName();
                requestType.setText(typeDisplay + ": " + request.getAppName());
            } else {
                requestType.setText("Request: Unknown App");
            }

            // Set requested time with formatted display
            requestedTime.setText("Time: " + request.getFormattedRequestedTime());

            // Set timestamp
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy - hh:mm a", Locale.getDefault());
            timestamp.setText("Sent: " + sdf.format(new Date(request.getTimestamp())));

            // Set status badge with color
            String status = request.getStatus() != null ? request.getStatus() : "pending";
            statusBadge.setText(status.substring(0, 1).toUpperCase() + status.substring(1));
            
            // Update status badge color
            if (request.isPending()) {
                statusBadge.setBackgroundColor(Color.parseColor("#FF9800")); // Orange
                statusBadge.setTextColor(Color.WHITE);
            } else if (request.isApproved()) {
                statusBadge.setBackgroundColor(Color.parseColor("#4CAF50")); // Green
                statusBadge.setTextColor(Color.WHITE);
            } else if (request.isRejected()) {
                statusBadge.setBackgroundColor(Color.parseColor("#F44336")); // Red
                statusBadge.setTextColor(Color.WHITE);
            }

            // Set click listener for the entire item
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRequestClick(request);
                }
            });

            // Visual feedback for different statuses
            if (request.isPending()) {
                itemView.setAlpha(1.0f);
            } else {
                itemView.setAlpha(0.8f);
            }
        }
    }
}
