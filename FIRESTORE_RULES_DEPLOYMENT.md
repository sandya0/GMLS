# Firestore Security Rules Deployment Guide

## Overview
The updated Firestore security rules fix the `PERMISSION_DENIED` errors by implementing proper admin authentication and granular permissions for the GMLS application.

## Key Changes Made

### 1. Admin Authentication Function
```javascript
function isAdmin() {
  return request.auth != null && 
         exists(/databases/$(database)/documents/users/$(request.auth.uid)) &&
         get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'admin';
}
```

### 2. Enhanced Permissions Structure
- **Users Collection**: Admins can manage all users, users can manage their own profiles
- **Disasters Collection**: Admins can manage all disasters, users can manage their own reports
- **Admin-Only Collections**: `adminLogs`, `analytics`, `auditTrail`
- **Shared Collections**: `notifications`, `emergencyContacts`, `resources`

### 3. Data Validation Functions
- `isValidUserData()`: Validates user creation data
- `isValidUserUpdate()`: Validates user update operations
- Proper field validation for all operations

## Deployment Steps

### Option 1: Firebase Console (Recommended)
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your GMLS project
3. Navigate to **Firestore Database** → **Rules**
4. Copy the contents of `firestore.rules` file
5. Paste into the rules editor
6. Click **Publish** to deploy

### Option 2: Firebase CLI
```bash
# Install Firebase CLI if not already installed
npm install -g firebase-tools

# Login to Firebase
firebase login

# Initialize Firebase in your project (if not done)
firebase init firestore

# Deploy the rules
firebase deploy --only firestore:rules
```

## Testing the Rules

### 1. Admin User Setup
Ensure your admin users have the correct document structure:
```javascript
// Document: /users/{adminUserId}
{
  "email": "admin@example.com",
  "name": "Admin User",
  "role": "admin",        // This is crucial!
  "status": "active",
  "createdAt": timestamp,
  "lastLoginAt": timestamp
}
```

### 2. Regular User Setup
```javascript
// Document: /users/{userId}
{
  "email": "user@example.com",
  "name": "Regular User",
  "role": "user",         // This is crucial!
  "status": "active",
  "createdAt": timestamp,
  "lastLoginAt": timestamp
}
```

## Common Issues and Solutions

### Issue 1: Still Getting PERMISSION_DENIED
**Solution**: Ensure the user document exists in Firestore with the correct `role` field.

### Issue 2: Admin Functions Not Working
**Solution**: Verify that:
- The admin user document exists in `/users/{adminUserId}`
- The document has `role: "admin"`
- The user is properly authenticated

### Issue 3: Data Validation Errors
**Solution**: Ensure all required fields are present when creating/updating documents:
- Users: `email`, `name`, `role`, `status`
- Disasters: `title`, `description`, `location`, `severity`, `status`, `reportedBy`, `reportedAt`

## Security Features

### 1. Role-Based Access Control (RBAC)
- Admin users can perform all operations
- Regular users have limited permissions
- Proper role validation on every request

### 2. Data Validation
- Required fields validation
- Data type validation
- Enum value validation for roles and statuses

### 3. Audit Trail Protection
- Admin logs are protected
- Audit trail is admin-only
- Analytics data is secured

### 4. User Data Protection
- Users can only modify their own data
- Admins can modify any user data
- Proper field-level update validation

## Monitoring

After deployment, monitor the Firestore usage in Firebase Console:
1. Go to **Firestore Database** → **Usage**
2. Check for any remaining permission errors
3. Monitor read/write operations

## Rollback Plan

If issues occur, you can quickly rollback:
1. Go to Firebase Console → Firestore → Rules
2. Click on the **History** tab
3. Select a previous version
4. Click **Restore**

## Next Steps

1. Deploy the rules using one of the methods above
2. Test admin operations in your app
3. Verify regular user operations still work
4. Monitor for any remaining permission issues
5. Update your app code if needed to match the new rule structure

## Support

If you encounter issues:
1. Check the Firebase Console for detailed error messages
2. Verify user document structure matches the expected format
3. Test with both admin and regular user accounts
4. Check the Firestore simulator in Firebase Console for rule testing 