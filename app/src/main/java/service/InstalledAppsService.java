package service;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.AppInfo;

public class InstalledAppsService {

    private static final String TAG = "InstalledAppsService";
    private Context context;
    private PackageManager packageManager;

    public InstalledAppsService(Context context) {
        this.context = context;
        this.packageManager = context.getPackageManager();
    }

    /**
     * Fetch all installed non-system apps
     */
    public List<AppInfo> getInstalledApps() {
        List<AppInfo> appList = new ArrayList<>();
        List<ApplicationInfo> packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo appInfo : packages) {
            // Filter out system apps
            if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                try {
                    String appName = packageManager.getApplicationLabel(appInfo).toString();
                    String packageName = appInfo.packageName;
                    
                    AppInfo app = new AppInfo(
                            appName,
                            packageName,
                            packageManager.getApplicationIcon(appInfo)
                    );
                    
                    appList.add(app);
                } catch (Exception e) {
                    Log.e(TAG, "Error loading app: " + appInfo.packageName, e);
                }
            }
        }

        Log.d(TAG, "Found " + appList.size() + " installed apps");
        return appList;
    }

    /**
     * Upload installed apps to Firebase
     */
    public void uploadInstalledAppsToFirebase(String parentUID, String studentUID, List<AppInfo> apps) {
        DatabaseReference appsRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(parentUID)
                .child("studentsData")
                .child(studentUID)
                .child("installedApps");

        Map<String, Object> appsMap = new HashMap<>();
        
        for (AppInfo app : apps) {
            Map<String, Object> appData = new HashMap<>();
            appData.put("appName", app.getAppName());
            appData.put("packageName", app.getPackageName());
            appData.put("lastUpdated", System.currentTimeMillis());
            
            appsMap.put(app.getPackageName().replace(".", "_"), appData);
        }

        appsRef.setValue(appsMap)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Apps uploaded successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to upload apps", e));
    }

    /**
     * Get app name from package name
     */
    public String getAppName(String packageName) {
        try {
            ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 0);
            return packageManager.getApplicationLabel(appInfo).toString();
        } catch (PackageManager.NameNotFoundException e) {
            return packageName;
        }
    }
}
