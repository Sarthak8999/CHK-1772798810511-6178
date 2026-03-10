package com.example.smartparentcontrol.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.example.smartparentcontrol.models.AppInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * AppScanner Utility
 * Provides methods to scan and retrieve installed applications
 */
public class AppScanner {

    private static final String TAG = "AppScanner";
    private Context context;
    private PackageManager packageManager;

    public AppScanner(Context context) {
        this.context = context;
        this.packageManager = context.getPackageManager();
    }

    /**
     * Get all installed non-system apps
     * @return List of AppInfo objects
     */
    public List<AppInfo> getInstalledApps() {
        return getInstalledApps(false);
    }

    /**
     * Get all installed apps with option to include system apps
     * @param includeSystemApps Whether to include system apps
     * @return List of AppInfo objects
     */
    public List<AppInfo> getInstalledApps(boolean includeSystemApps) {
        List<AppInfo> appList = new ArrayList<>();
        
        try {
            List<ApplicationInfo> packages = packageManager.getInstalledApplications(
                    PackageManager.GET_META_DATA);

            Log.d(TAG, "Total packages found: " + packages.size());

            for (ApplicationInfo appInfo : packages) {
                boolean isSystemApp = (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
                
                // Skip system apps unless requested
                if (isSystemApp && !includeSystemApps) {
                    continue;
                }

                try {
                    String appName = packageManager.getApplicationLabel(appInfo).toString();
                    String packageName = appInfo.packageName;
                    
                    AppInfo app = new AppInfo(
                            appName,
                            packageName,
                            packageManager.getApplicationIcon(appInfo)
                    );
                    
                    app.setSystemApp(isSystemApp);
                    appList.add(app);
                    
                } catch (Exception e) {
                    Log.e(TAG, "Error loading app: " + appInfo.packageName, e);
                }
            }

            Log.d(TAG, "Loaded " + appList.size() + " apps (includeSystem=" + includeSystemApps + ")");
            
        } catch (Exception e) {
            Log.e(TAG, "Error getting installed apps", e);
        }

        return appList;
    }

    /**
     * Get app name from package name
     * @param packageName Package name of the app
     * @return App name or package name if not found
     */
    public String getAppName(String packageName) {
        try {
            ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 0);
            return packageManager.getApplicationLabel(appInfo).toString();
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "App not found: " + packageName);
            return packageName;
        }
    }

    /**
     * Get AppInfo for a specific package
     * @param packageName Package name of the app
     * @return AppInfo object or null if not found
     */
    public AppInfo getAppInfo(String packageName) {
        try {
            ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 0);
            String appName = packageManager.getApplicationLabel(appInfo).toString();
            boolean isSystemApp = (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
            
            AppInfo app = new AppInfo(
                    appName,
                    packageName,
                    packageManager.getApplicationIcon(appInfo)
            );
            
            app.setSystemApp(isSystemApp);
            return app;
            
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "App not found: " + packageName);
            return null;
        }
    }

    /**
     * Check if an app is installed
     * @param packageName Package name to check
     * @return true if installed, false otherwise
     */
    public boolean isAppInstalled(String packageName) {
        try {
            packageManager.getApplicationInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /**
     * Check if an app is a system app
     * @param packageName Package name to check
     * @return true if system app, false otherwise
     */
    public boolean isSystemApp(String packageName) {
        try {
            ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 0);
            return (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /**
     * Get count of installed non-system apps
     * @return Number of installed apps
     */
    public int getInstalledAppCount() {
        return getInstalledApps(false).size();
    }

    /**
     * Get count of all installed apps including system apps
     * @return Total number of installed apps
     */
    public int getTotalAppCount() {
        return getInstalledApps(true).size();
    }
}
