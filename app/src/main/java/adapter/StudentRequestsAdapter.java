package adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartparentcontrol.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import model.TimeRequest;

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

            // Set app name with null safety
            if (request.getAppName() != null) {
                requestType.setText("Request: " + request.getAppName());
            } else {
                requestType.setText("Request: Unknown App");
            }

            // Set requested time
            requestedTime.setText("Time: +" + request.getRequestedMinutes() + " minutes");

            // Set timestamp
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy - hh:mm a", Locale.getDefault());
            timestamp.setText("Sent: " + sdf.format(new Date(request.getTimestamp())));

            // Set status badge
            String status = request.getStatus() != null ? request.getStatus() : "pending";
            statusBadge.setText(status.substring(0, 1).toUpperCase() + status.substring(1));
            
            // Update status badge color based on status
            switch (status.toLowerCase()) {
                case "approved":
                    statusBadge.setBackgroundColor(itemView.getContext().getResources()
                            .getColor(android.R.color.holo_green_dark));
                    break;
                case "rejected":
                    statusBadge.setBackgroundColor(itemView.getContext().getResources()
                            .getColor(android.R.color.holo_red_dark));
                    break;
                default:
                    statusBadge.setBackgroundColor(itemView.getContext().getResources()
                            .getColor(android.R.color.holo_orange_dark));
                    break;
            }

            // Set click listener for the entire item
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRequestClick(request);
                }
            });
        }
    }
}
