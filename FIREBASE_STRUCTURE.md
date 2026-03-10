# Firebase Backend Structure

## 🔥 Firebase Services Used

1. **Firebase Authentication** - User authentication with email/password
2. **Cloud Firestore** - NoSQL database for app data
3. **Firebase Analytics** - App usage analytics

## 📊 Firestore Database Structure

### Collection: `users`
Stores user profile information

```
users/
  {userId}/
    userId: string
    name: string
    email: string
    role: string ("Parent" or "Student")
    createdAt: timestamp
```

**Example:**
```json
{
  "userId": "abc123xyz",
  "name": "John Doe",
  "email": "john@example.com",
  "role": "Parent",
  "createdAt": 1234567890000
}
```

### Collection: `app_limits`
Stores time limits set by parents for student apps

```
app_limits/
  {userId}_{packageName}/
    userId: string
    packageName: string
    timeLimitMinutes: number
    updatedAt: timestamp
```

**Document ID Format:** `{userId}_{packageName}`
- Example: `student123_com_whatsapp`

**Example:**
```json
{
  "userId": "student123",
  "packageName": "com.whatsapp",
  "timeLimitMinutes": 30,
  "updatedAt": 1234567890000
}
```

### Collection: `requests`
Stores time extension requests from students

```
requests/
  {requestId}/
    requestId: string (auto-generated)
    studentId: string
    appName: string
    packageName: string
    requestedMinutes: number
    reason: string
    type: string ("time_extension", "unblock")
    status: string ("pending", "approved", "rejected")
    createdAt: timestamp
    respondedAt: timestamp (optional)
```

**Example:**
```json
{
  "requestId": "req_xyz789",
  "studentId": "student123",
  "appName": "WhatsApp",
  "packageName": "com.whatsapp",
  "requestedMinutes": 15,
  "reason": "Need to complete homework discussion",
  "type": "time_extension",
  "status": "pending",
  "createdAt": 1234567890000
}
```

## 🔐 Security Rules

### Firestore Security Rules (to be implemented):

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Users collection
    match /users/{userId} {
      allow read: if request.auth != null && request.auth.uid == userId;
      allow write: if request.auth != null && request.auth.uid == userId;
    }
    
    // App limits collection
    match /app_limits/{limitId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && 
                     get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == "Parent";
    }
    
    // Requests collection
    match /requests/{requestId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null && 
                      get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == "Student";
      allow update: if request.auth != null && 
                      get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == "Parent";
      allow delete: if request.auth != null;
    }
  }
}
```

## 📱 Repository Classes

### 1. AuthRepository
Handles all authentication operations:
- `registerUser(email, password)` - Register new user
- `loginUser(email, password)` - Login existing user
- `getCurrentUser()` - Get current logged in user
- `logout()` - Logout user
- `createUserProfile(userId, name, email, role)` - Create Firestore profile
- `getUserProfile(userId)` - Get user profile
- `updateUserProfile(userId, updates)` - Update profile

### 2. AppLimitsRepository
Manages app time limits:
- `setAppLimit(userId, packageName, timeLimitMinutes)` - Set time limit
- `getAppLimit(userId, packageName)` - Get specific limit
- `getAllAppLimits(userId)` - Get all limits for user
- `removeAppLimit(userId, packageName)` - Remove limit
- `updateAppLimit(userId, packageName, newLimit)` - Update limit

### 3. RequestsRepository
Manages time extension requests:
- `createRequest(studentId, appName, ...)` - Create new request
- `getStudentRequests(studentId)` - Get student's requests
- `getPendingRequests()` - Get all pending requests
- `approveRequest(requestId)` - Approve a request
- `rejectRequest(requestId)` - Reject a request
- `deleteRequest(requestId)` - Delete a request

## 🎯 User Roles

### Parent Role
**Permissions:**
- ✅ Set app time limits for students
- ✅ View all student requests
- ✅ Approve/reject time extension requests
- ✅ View student app usage
- ✅ Manage student accounts

### Student Role
**Permissions:**
- ✅ View their own app usage
- ✅ Request time extensions
- ✅ View their request history
- ❌ Cannot modify time limits
- ❌ Cannot approve requests

## 🔄 Real-time Updates

All repositories support real-time listeners using Firestore's snapshot listeners:

```java
// Example: Listen for app limit changes
appLimitsRepository.getAppLimitsQuery(userId)
    .addSnapshotListener((snapshots, error) -> {
        if (error != null) {
            Log.e(TAG, "Listen failed", error);
            return;
        }
        
        // Handle updates
        for (DocumentChange dc : snapshots.getDocumentChanges()) {
            switch (dc.getType()) {
                case ADDED:
                    // New limit added
                    break;
                case MODIFIED:
                    // Limit updated
                    break;
                case REMOVED:
                    // Limit removed
                    break;
            }
        }
    });
```

## 📦 Dependencies Added

```gradle
// Firebase BOM
implementation(platform('com.google.firebase:firebase-bom:32.7.0'))

// Firebase Services
implementation 'com.google.firebase:firebase-auth'
implementation 'com.google.firebase:firebase-firestore:24.10.0'
implementation 'com.google.firebase:firebase-analytics'
```

## ✅ Implementation Checklist

- [x] Firebase Authentication setup
- [x] Firestore database structure defined
- [x] Repository classes created
- [x] Data models created
- [x] Dependencies added to build.gradle
- [ ] Security rules to be deployed
- [ ] Integration with existing activities
- [ ] Real-time listeners implementation
- [ ] Error handling and offline support

## 🚀 Next Steps

1. Deploy Firestore security rules
2. Update existing activities to use repositories
3. Implement real-time listeners in activities
4. Add offline data persistence
5. Implement data synchronization
6. Add error handling and retry logic
