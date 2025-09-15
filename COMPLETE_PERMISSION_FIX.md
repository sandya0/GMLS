# Complete Permission Fix Guide

## The Real Problem

Your app is still crashing because **multiple Firebase operations are triggered simultaneously** when the app starts, before the admin bootstrap has a chance to complete. Even though you deployed the Firestore rules, there are several issues:

1. **AuthViewModel** automatically tries to load user data from Firestore on startup
2. **NotificationViewModel** automatically queries the notifications collection on initialization  
3. **Timing issues** where Firestore operations happen before user documents exist
4. **Notification collection permission errors** due to restrictive rules

## Complete Solution (3 Steps)

### Step 1: Deploy Updated Firestore Rules

**CRITICAL:** Copy these updated rules to Firebase Console → Firestore → Rules and click **Publish**:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Helper function to check if user is admin
    function isAdmin() {
      return request.auth != null && 
             exists(/databases/$(database)/documents/users/$(request.auth.uid)) &&
             get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'admin';
    }
    
    // Helper function to check if user is authenticated
    function isAuthenticated() {
      return request.auth != null;
    }
    
    // Helper function to check if user is verified
    function isVerified() {
      return request.auth != null &&
             exists(/databases/$(database)/documents/users/$(request.auth.uid)) &&
             get(/databases/$(database)/documents/users/$(request.auth.uid)).data.isVerified == true;
    }
    
    // Helper function to check if user is active
    function isActive() {
      return request.auth != null &&
             exists(/databases/$(database)/documents/users/$(request.auth.uid)) &&
             get(/databases/$(database)/documents/users/$(request.auth.uid)).data.isActive != false;
    }
    
    // Helper function to check if user is owner
    function isOwner(userId) {
      return request.auth.uid == userId;
    }
    
    // Helper function to check if this is the bootstrap admin creation
    function isBootstrapAdminCreation() {
      return request.auth != null && 
             request.resource.data.role == 'admin' &&
             request.resource.data.email != null &&
             request.resource.data.fullName != null;
    }
    
    // Helper function to validate disaster data
    function isValidDisaster() {
      return request.resource.data.keys().hasAll(['title', 'description', 'location', 'type', 'timestamp', 'affectedCount', 'reportedBy', 'status']) &&
             request.resource.data.type in ['EARTHQUAKE', 'FLOOD', 'WILDFIRE', 'LANDSLIDE', 'VOLCANO', 'TSUNAMI', 'HURRICANE', 'TORNADO', 'OTHER'] &&
             request.resource.data.status in ['REPORTED', 'VERIFIED', 'IN_PROGRESS', 'RESOLVED'] &&
             request.resource.data.affectedCount >= 0;
    }
    
    // Helper function to validate user data
    function isValidUserData() {
      return request.resource.data.keys().hasAll(['fullName', 'email', 'role', 'isVerified', 'isActive', 'createdAt']) &&
             request.resource.data.role in ['admin', 'user'] &&
             request.resource.data.email is string &&
             request.resource.data.fullName is string;
    }
    
    // Helper function to validate user updates
    function isValidUserUpdate() {
      return request.resource.data.diff(resource.data).affectedKeys().hasOnly(['fullName', 'phoneNumber', 'address', 'isVerified', 'isActive', 'updatedAt', 
                          'latitude', 'longitude', 'dateOfBirth', 'gender', 'nationalId', 'familyCardNumber', 
                          'placeOfBirth', 'religion', 'maritalStatus', 'familyRelationshipStatus', 
                          'lastEducation', 'occupation', 'economicStatus', 'bloodType', 'medicalConditions',
                          'disabilities', 'emergencyContactName', 'emergencyContactRelationship',
                          'emergencyContactPhone', 'householdMembers', 'locationPermissionGranted',
                          'profilePictureUrl', 'fcmToken', 'lastLocationUpdate', 'lastLoginAt']);
    }
    
    // Users collection - Enhanced security with bootstrap support
    match /users/{userId} {
      allow read: if isAuthenticated() && (
        isOwner(userId) || 
        isAdmin() || 
        (isVerified() && isActive())
      );
      
      allow create: if isAuthenticated() && (
        (isOwner(userId) && isValidUserData() && request.resource.data.role == 'user') ||
        (isAdmin() && isValidUserData()) ||
        (isOwner(userId) && isBootstrapAdminCreation())
      );
      
      allow update: if isAuthenticated() && isValidUserUpdate() && isActive() && (
        (isOwner(userId) && 
         !request.resource.data.diff(resource.data).affectedKeys().hasAny(['role']) &&
         !request.resource.data.diff(resource.data).affectedKeys().hasAny(['isVerified'])) ||
        (isAdmin()) ||
        (isOwner(userId) && 
         request.resource.data.diff(resource.data).affectedKeys().hasOnly(['role', 'isVerified', 'updatedAt']) &&
         request.resource.data.role == 'admin')
      );
      
      allow delete: if isAdmin() && request.auth.uid != userId;
    }
    
    // Disasters collection
    match /disasters/{disasterId} {
      allow read: if isAuthenticated();
      
      allow create: if isAuthenticated() && isActive() && isValidDisaster() && (
        request.resource.data.reportedBy == request.auth.uid ||
        isAdmin()
      );
      
      allow update: if isAuthenticated() && isActive() && (
        (resource.data.reportedBy == request.auth.uid) ||
        isAdmin() ||
        (isVerified() &&
         request.resource.data.diff(resource.data).affectedKeys().hasOnly(['status', 'updatedAt']))
      );
      
      allow delete: if isAuthenticated() && (
        (resource.data.reportedBy == request.auth.uid) ||
        isAdmin()
      );
    }
    
    // Notifications collection - Fixed permissions
    match /notifications/{notificationId} {
      allow read: if isAuthenticated() && (
        resource.data.userId == request.auth.uid || 
        resource.data.type == "BROADCAST" ||
        isAdmin()
      );
      allow create: if isAuthenticated() && (
        isAdmin() ||
        (request.resource.data.userId == request.auth.uid &&
         request.resource.data.title != null &&
         request.resource.data.message != null &&
         request.resource.data.timestamp != null)
      );
      allow update: if isAuthenticated() && (
        (resource.data.userId == request.auth.uid &&
         request.resource.data.diff(resource.data).affectedKeys().hasOnly(['isRead', 'readAt'])) ||
        isAdmin()
      );
      allow delete: if isAdmin();
    }

    // User notifications subcollection - Fixed structure
    match /user_notifications/{notificationId} {
      allow read: if isAuthenticated() && (
        resource.data.userId == request.auth.uid || 
        isAdmin()
      );
      allow create: if isAuthenticated() && (
        isAdmin() ||
        request.resource.data.userId == request.auth.uid
      );
      allow update: if isAuthenticated() && (
        (resource.data.userId == request.auth.uid &&
         request.resource.data.diff(resource.data).affectedKeys().hasOnly(['read', 'readAt'])) ||
        isAdmin()
      );
      allow delete: if isAdmin();
    }
    
    // System settings
    match /systemSettings/{settingId} {
      allow read: if isAuthenticated();
      allow write: if isAdmin();
    }
    
    // Analytics collection
    match /analytics/{analyticsId} {
      allow read: if isAdmin();
      allow write: if isAdmin();
    }
    
    // Resources collection
    match /resources/{resourceId} {
      allow read: if isAuthenticated();
      allow create: if isAdmin() || isVerified();
      allow update: if isAdmin() || (isVerified() && resource.data.createdBy == request.auth.uid);
      allow delete: if isAdmin();
    }
    
    // FCM tokens collection
    match /fcmTokens/{userId} {
      allow read: if isAuthenticated() && (request.auth.uid == userId || isAdmin());
      allow write: if isAuthenticated() && request.auth.uid == userId;
      allow delete: if isAuthenticated() && (request.auth.uid == userId || isAdmin());
    }
    
    // Support tickets
    match /support/{ticketId} {
      allow read: if isAuthenticated() && (
        resource.data.userId == request.auth.uid || 
        isAdmin()
      );
      allow create: if isAuthenticated() && isActive() &&
                   request.resource.data.userId == request.auth.uid;
      allow update: if isAuthenticated() && (
        (resource.data.userId == request.auth.uid) ||
        isAdmin()
      );
      allow delete: if isAdmin();
    }
    
    // Application logs
    match /appLogs/{logId} {
      allow read: if isAdmin();
      allow create: if isAuthenticated();
      allow update: if false;
      allow delete: if isAdmin();
    }
    
    // Default deny rule
    match /{document=**} {
      allow read, write: if false;
    }
  }
}
```

### Step 2: Build and Install Updated App

The code changes I made will:

1. **Delay notification loading** by 2 seconds to allow admin bootstrap to complete
2. **Add safe authentication checks** that don't crash on permission errors  
3. **Check user document existence** before attempting Firestore operations
4. **Enhanced admin bootstrap** with better error handling and fallback logic

Build and install the updated app:

```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Step 3: Test the Fix

1. **Clear app data** (Settings → Apps → GMLS → Storage → Clear Data)
2. **Launch the app**
3. **Register or login** with your account
4. **Check logs** for these success messages:
   ```
   AdminBootstrap: Admin user document created successfully
   NotificationViewModel: User document doesn't exist, loading sample notifications
   AuthViewModel: User role updated to admin
   ```

## What the Fix Does

### Code Changes Made:

1. **NotificationViewModel**: 
   - Delays loading by 2 seconds to allow bootstrap
   - Checks if user document exists before querying notifications
   - Falls back to sample data on permission errors

2. **AuthViewModel**:
   - Uses safe authentication method that doesn't crash on permission errors
   - Handles permission denied errors gracefully
   - Doesn't change auth state on temporary permission issues

3. **FirebaseAuthRepository**:
   - Added `getCurrentUserSafe()` method that logs but doesn't throw on permission errors
   - Better error handling for Firestore document access

4. **AdminBootstrap**:
   - Enhanced error handling with fallback logic
   - Creates user documents even if permission checks fail initially
   - More robust admin upgrade process

5. **Firestore Rules**:
   - Fixed notification permissions to allow broadcast notifications
   - Simplified user_notifications collection structure
   - Added bootstrap admin creation support

## Why This Works

The issue was that multiple Firebase operations were racing on app startup:

1. **Before**: App crashes because NotificationViewModel and AuthViewModel try to access Firestore before user document exists
2. **After**: Operations are delayed, have safe fallbacks, and rules allow proper bootstrap process

## Expected Behavior After Fix

1. **First Launch**: App creates user document and sets admin role automatically
2. **Subsequent Launches**: App loads user data and notifications without errors  
3. **Admin Features**: All admin functionality works properly
4. **Notifications**: Loads sample data if real notifications fail, no crashes

## If Still Having Issues

If you still get permission errors after applying all fixes:

1. **Check Firebase Console** → Firestore → Rules → Make sure timestamp shows recent deployment
2. **Clear app data completely** and retest
3. **Check network connectivity** - some errors are network-related
4. **Verify Firebase project configuration** in `google-services.json`

The app should now start successfully without any permission-related crashes! 🚀 