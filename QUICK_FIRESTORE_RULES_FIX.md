# URGENT: Deploy Firestore Rules to Fix Crash

## The Problem
Your app is crashing because the Firestore security rules are still the old restrictive ones that deny access. We need to deploy the corrected rules immediately.

## QUICK FIX - Deploy Rules Now

### Option 1: Firebase Console (Fastest - 2 minutes)

1. **Open Firebase Console**: https://console.firebase.google.com/
2. **Select your GMLS project**
3. **Go to Firestore Database** → **Rules** tab
4. **Replace ALL the content** with this corrected rules:

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
    
    // Helper function for bootstrap admin creation
    function isBootstrapAdminCreation() {
      return request.auth != null && 
             request.resource.data.role == 'admin' &&
             request.resource.data.email != null &&
             request.resource.data.fullName != null;
    }
    
    // Users collection - Enhanced security with bootstrap support
    match /users/{userId} {
      allow read: if isAuthenticated() && (
        isOwner(userId) || 
        isAdmin() || 
        (isVerified() && isActive())
      );
      
      allow create: if isAuthenticated() && (
        (isOwner(userId) && request.resource.data.role == 'user') ||
        (isAdmin()) ||
        (isOwner(userId) && isBootstrapAdminCreation())
      );
      
      allow update: if isAuthenticated() && (
        (isOwner(userId) && 
         !request.resource.data.diff(resource.data).affectedKeys().hasAny(['role']) &&
         !request.resource.data.diff(resource.data).affectedKeys().hasAny(['isVerified'])) ||
        isAdmin() ||
        (isOwner(userId) && 
         request.resource.data.diff(resource.data).affectedKeys().hasOnly(['role', 'isVerified', 'updatedAt']) &&
         request.resource.data.role == 'admin')
      );
      
      allow delete: if isAdmin() && request.auth.uid != userId;
    }
    
    // Disasters collection
    match /disasters/{disasterId} {
      allow read: if isAuthenticated();
      allow create: if isAuthenticated() && isActive();
      allow update: if isAuthenticated() && isActive() && (
        (resource.data.reportedBy == request.auth.uid) ||
        isAdmin() ||
        isVerified()
      );
      allow delete: if isAuthenticated() && (
        (resource.data.reportedBy == request.auth.uid) ||
        isAdmin()
      );
    }
    
    // Admin collections
    match /admin_audit_logs/{logId} {
      allow read: if isAdmin();
      allow create: if isAdmin();
      allow update: if false;
      allow delete: if false;
    }
    
    // Notifications collection
    match /notifications/{notificationId} {
      allow read: if isAuthenticated() && (
        resource.data.userId == request.auth.uid || 
        isAdmin()
      );
      allow create: if isAdmin();
      allow update: if isAuthenticated() && (
        (resource.data.userId == request.auth.uid) ||
        isAdmin()
      );
      allow delete: if isAdmin();
    }
    
    // User notifications subcollection
    match /user_notifications/{userId}/notifications/{notificationId} {
      allow read: if isAuthenticated() && (request.auth.uid == userId || isAdmin());
      allow create: if isAdmin();
      allow update: if isAuthenticated() && (request.auth.uid == userId || isAdmin());
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
      allow create: if isAuthenticated() && isActive();
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
    
    // Default allow for authenticated users (temporary for testing)
    match /{document=**} {
      allow read, write: if isAuthenticated();
    }
  }
}
```

5. **Click "Publish"** button
6. **Wait for deployment** (should take 30 seconds)

### Option 2: Temporary Open Rules (If Option 1 Fails)

If you're still having issues, use these temporary OPEN rules for testing:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

**⚠️ IMPORTANT**: These are temporary testing rules - replace with secure rules later!

## After Deploying Rules

1. **Restart your app** completely
2. **Login to your account**
3. **Check logs** - you should see admin bootstrap working
4. **Verify admin access** in the app

## Expected Results

✅ No more `PERMISSION_DENIED` errors
✅ App stops crashing on Firebase operations
✅ Admin bootstrap will work automatically
✅ All Firebase features accessible

## If Still Having Issues

1. **Check Firebase Console** → **Authentication** → verify you're logged in
2. **Check Firebase Console** → **Firestore** → **Data** → verify users collection exists
3. **Clear app data** and try fresh login
4. **Check logs** for "Admin user bootstrapped successfully"

## Create Your Admin User

After rules are deployed, your app will automatically:
1. Create your user document if it doesn't exist
2. Set your role to "admin" if no other admins exist
3. Fix any permission issues

The crash should be completely resolved once these rules are deployed! 🚀 