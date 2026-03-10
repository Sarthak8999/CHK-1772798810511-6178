package com.example.smartparentcontrol.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartparentcontrol.R;
import com.example.smartparentcontrol.models.AppInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView Adapter for displaying installed apps
 * Shows app icon, name, package name, and usage time
 */
public class InstalledAppsAdapter extends RecyclerView.Adapter<InstalledAppsAdapter.ViewHolder> {

    private List<AppInfo> appList;
    private OnAppClickListener listener;

    public interface OnAppClickListener {
        void onAppClick(AppInfo appInfo);
    }

    public InstalledAppsAdapter(OnAppClickListener listener) {
        this.appList = new ArrayList<>();
        this.listener = listener;
    }

    public void setAppList(List<AppInfo> appList) {
        if (appList != null) {
            this.appList = appList;
        } else {
            this.appList = new ArrayList<>();
        }
        notifyDataSetChanged();
    }

    public void updateApp(AppInfo updatedApp) {
        for (int i = 0; i < appList.size(); i++) {
            if (appList.get(i).getPackageName().equals(updatedApp.getPackageName())) {
                appList.set(i, updatedApp);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void addApp(AppInfo appInfo) {
        appList.add(appInfo);
        notifyItemInserted(appList.size() - 1);
    }

    public void removeApp(String packageName) {
        for (int i = 0; i < appList.size(); i++) {
            if (appList.get(i).getPackageName().equals(packageName)) {
                appList.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    public void clearApps() {
        appList.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_app, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppInfo appInfo = appList.get(position);
        holder.bind(appInfo);
    }

    @Override
    public int getItemCount() {
        return appList != null ? appList.size() : 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView appIcon;
        TextView appName;
        TextView packageName;
        TextView usageTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            appIcon = itemView.findViewById(R.id.appIcon);
            appName = itemView.findViewById(R.id.appName);
            packageName = itemView.findViewById(R.id.packageName);
            usageTime = itemView.findViewById(R.id.usageTime);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onAppClick(appList.get(position));
                }
            });
        }

        void bind(AppInfo appInfo) {
            if (appInfo == null) {
                return;
            }

            // Set app icon
            if (appInfo.getIcon() != null) {
                appIcon.setImageDrawable(appInfo.getIcon());
            } else {
                appIcon.setImageResource(android.R.drawable.sym_def_app_icon);
            }

            // Set app name
            if (appInfo.getAppName() != null) {
                appName.setText(appInfo.getAppName());
            } else {
                appName.setText("Unknown App");
            }

            // Set package name
            if (appInfo.getPackageName() != null) {
                packageName.setText(appInfo.getPackageName());
            } else {
                packageName.setText("");
            }

            // Set usage time
            if (appInfo.getUsageTime() > 0) {
                usageTime.setVisibility(View.VISIBLE);
                String usageText = "Usage: " + appInfo.getFormattedUsageTime();
                
                // Add time limit info if available
                if (appInfo.getTimeLimit() > 0) {
                    usageText += " / " + appInfo.getFormattedTimeLimit();
                    
                    // Change color if limit exceeded
                    if (appInfo.hasExceededLimit()) {
                        usageTime.setTextColor(itemView.getContext().getResources()
                                .getColor(android.R.color.holo_red_dark));
                    } else {
                        usageTime.setTextColor(itemView.getContext().getResources()
                                .getColor(android.R.color.holo_blue_dark));
                    }
                } else {
                    usageTime.setTextColor(itemView.getContext().getResources()
                            .getColor(android.R.color.holo_blue_dark));
                }
                
                usageTime.setText(usageText);
            } else {
                usageTime.setVisibility(View.GONE);
            }

            // Visual indicator for blocked apps
            if (appInfo.isBlocked()) {
                itemView.setAlpha(0.6f);
            } else {
                itemView.setAlpha(1.0f);
            }
        }
    }
}
