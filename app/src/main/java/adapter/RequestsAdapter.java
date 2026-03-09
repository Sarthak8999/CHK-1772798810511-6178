package adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

            // Set student name with null safety
            if (request.getStudentName() != null) {
                studentName.setText("Student: " + request.getStudentName());
            } else {
                studentName.setText("Student: Unknown");
            }

            // Set app name with null safety
            if (request.getAppName() != null) {
                appName.setText("App: " + request.getAppName());
            } else {
                appName.setText("App: Unknown");
            }

            // Set requested time
            requestedTime.setText("Requested: +" + request.getRequestedMinutes() + " minutes");

            // Set reason with null safety
            if (request.getReason() != null && !request.getReason().isEmpty()) {
                reason.setText("Reason: " + request.getReason());
                reason.setVisibility(View.VISIBLE);
            } else {
                reason.setText("Reason: No reason provided");
            }

            // Set timestamp
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy - hh:mm a", Locale.getDefault());
            timestamp.setText("Time: " + sdf.format(new Date(request.getTimestamp())));

            // Set status badge
            String status = request.getStatus() != null ? request.getStatus() : "pending";
            statusBadge.setText(status.substring(0, 1).toUpperCase() + status.substring(1));
            
            // Update status badge color based on status
            switch (status.toLowerCase()) {
                case "approved":
                    statusBadge.setBackgroundColor(itemView.getContext().getResources()
                            .getColor(android.R.color.holo_green_dark));
                    approveButton.setEnabled(false);
                    rejectButton.setEnabled(false);
                    break;
                case "rejected":
                    statusBadge.setBackgroundColor(itemView.getContext().getResources()
                            .getColor(android.R.color.holo_red_dark));
                    approveButton.setEnabled(false);
                    rejectButton.setEnabled(false);
                    break;
                default:
                    statusBadge.setBackgroundColor(itemView.getContext().getResources()
                            .getColor(android.R.color.holo_orange_dark));
                    approveButton.setEnabled(true);
                    rejectButton.setEnabled(true);
                    break;
            }

            // Set click listeners
            approveButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onApprove(request);
                }
            });

            rejectButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onReject(request);
                }
            });
        }
    }
}
