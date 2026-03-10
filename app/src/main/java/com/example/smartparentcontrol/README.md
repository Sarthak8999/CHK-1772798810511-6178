# Smart Parent Control - Package Structure

## 📁 Project Organization

This package follows a clean architecture pattern with proper separation of concerns.

### Package Structure:

```
com.example.smartparentcontrol/
├── MainActivity.java                 # Splash Screen Activity
├── activities/                       # All Activity classes
│   ├── RoleSelectionActivity.java    # ✓ Role selection screen
│   ├── InstalledAppsActivity.java    # ✓ App scanner with PackageManager
│   ├── SetTimeLimitActivity.java     # ✓ Set app time limits (Firestore)
│   ├── TimeLimitBlockActivity.java   # ✓ Block screen with request dialog
│   ├── StudentRequestsActivity.java  # ✓ Student request history & creation
│   └── ParentRequestsActivity.java   # ✓ Parent request management (approve/reject)
├── adapters/                         # RecyclerView Adapters ✓ COMPLETE
│   ├── InstalledAppsAdapter.java     # ✓ Displays installed apps list
│   ├── RequestsAdapter.java          # ✓ Parent request approvals
│   └── StudentRequestsAdapter.java   # ✓ Student request history
├── models/                           # Data Models ✓ COMPLETE
│   ├── User.java                     # ✓ User model with role management
│   ├── AppInfo.java                  # ✓ App info with usage tracking
│   ├── AppLimit.java                 # ✓ Time limit configuration
│   ├── TimeRequest.java              # ✓ Time extension request
│   ├── FirestoreUser.java            # ✓ Firestore user data
│   └── TimeExtensionRequest.java     # ✓ Firestore request model
├── repository/                       # Data Repository Layer ✓ COMPLETE
│   ├── AuthRepository.java           # ✓ Authentication operations
│   ├── AppLimitsRepository.java      # ✓ App limits management
│   └── RequestsRepository.java       # ✓ Request handling
├── services/                         # Background Services ✓
│   └── AppUsageMonitorService.java   # ✓ Foreground monitoring service
├── utils/                            # Utility Classes ✓
│   ├── AppScanner.java               # ✓ App scanning utility
│   └── MonitoringServiceHelper.java  # ✓ Service start/stop helper
├── viewmodel/                        # ViewModel Classes (MVVM)
│   └── (Ready for implementations)
└── ui/
    └── theme/                        # UI Theme files
```

## 🔄 Migration Plan

### Current Structure (Root Level):
- `adapter/` - Contains InstalledAppsAdapter, RequestsAdapter, StudentRequestsAdapter
- `model/` - Contains AppInfo, TimeRequest, User
- `service/` - Contains AppMonitoringService, InstalledAppsService, UsageStatsService
- `ui/` - Contains all Activity classes
- `utils/` - Contains PreferenceManager

### Future Structure (Organized):
All classes will gradually be moved to the proper package structure under `com.example.smartparentcontrol`

## 📝 Notes:
- Existing files in root packages (`adapter/`, `model/`, `service/`, `ui/`, `utils/`) are kept for backward compatibility
- New files should be added to the organized structure under `com.example.smartparentcontrol`
- Gradual migration can be done without breaking existing functionality
