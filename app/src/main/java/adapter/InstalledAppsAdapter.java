package adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.smartparentcontrol.R;
import com.smartparentcontrol.model.AppInfo;

import java.util.ArrayList;
import java.util.List;

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
        this.appList = appList;
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
        return appList.size();
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

            appIcon.setImageDrawable(appInfo.getAppIcon());
            appName.setText(appInfo.getAppName());
            packageName.setText(appInfo.getPackageName());

            if (appInfo.getUsageTime() > 0) {

                usageTime.setVisibility(View.VISIBLE);
                usageTime.setText("Usage: " + appInfo.getFormattedUsageTime());

            } else {

                usageTime.setVisibility(View.GONE);

            }
        }
    }
}