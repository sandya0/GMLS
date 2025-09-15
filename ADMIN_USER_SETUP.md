# Admin User Setup Guide

## Quick Fix for PERMISSION_DENIED Errors

The permission errors are happening because your Firestore rules expect admin users to have a specific document structure. Here's how to fix it:

## Step 1: Deploy the Updated Rules

First, deploy the updated Firestore rules I just created:

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your GMLS project
3. Navigate to **Firestore Database** → **Rules**
4. Copy the contents of the updated `firestore.rules` file
5. Paste into the rules editor and click **Publish**

## Step 2: Verify/Create Admin User Document

### Option A: Using Firebase Console (Recommended)

1. Go to **Firestore Database** → **Data**
2. Navigate to the `users` collection
3. Find your admin user document (using your user ID)
4. Ensure it has this structure:

```json
{
  "email": "your-admin@email.com",
  "fullName": "Your Admin Name",
  "role": "admin",
  "isActive": true,
  "isVerified": true,
  "createdAt": "2024-01-01T00:00:00.000Z",
  "updatedAt": "2024-01-01T00:00:00.000Z",
  "phoneNumber": "+1234567890",
  "address": "Your Address"
}
```

**Important Fields:**
- `role`: Must be exactly `"admin"` (not `"ADMIN"` or anything else)
- `fullName`: Use this instead of `name`
- `isActive`: Use this instead of `status`
- `isVerified`: Should be `true` for admin users

### Option B: Using Your App

If you don't have an admin user yet, you can create one:

1. **First, temporarily modify your Firestore rules** to allow user creation:
   ```javascript
   // Add this temporary rule to allow initial admin creation
   match /users/{userId} {
     allow create: if request.auth != null;
     // ... other rules
   }
   ```

2. **Register a new user** through your app

3. **Manually update the user document** in Firebase Console:
   - Change `role` from `"user"` to `"admin"`
   - Set `isVerified` to `true`

4. **Restore the original rules** after creating the admin

## Step 3: Test Admin Access

After setting up the admin user:

1. **Login** with your admin account
2. **Try accessing** the admin dashboard
3. **Check the logs** for any remaining permission errors

## Step 4: Common Issues and Solutions

### Issue: Still getting "Missing or insufficient permissions"
**Solution**: 
- Verify the user document exists in `/users/{userId}`
- Check that `role` field is exactly `"admin"`
- Ensure the user is properly authenticated

### Issue: "User not found" errors
**Solution**:
- Make sure the user document was created in Firestore
- Check that the document ID matches the Firebase Auth UID

### Issue: Rules validation errors
**Solution**:
- Ensure all required fields are present: `email`, `fullName`, `role`
- Check field types match expectations (strings, booleans, etc.)

## Step 5: Verify Everything Works

Test these admin functions:
- [ ] View all users
- [ ] Create new users
- [ ] Update user information
- [ ] View disasters
- [ ] Access admin analytics

## Emergency Fallback

If you're still having issues, you can temporarily use these permissive rules for testing:

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

**⚠️ WARNING**: Only use these rules temporarily for testing. They're not secure for production!

## Next Steps

Once your admin user is working:
1. Create additional admin users through the app
2. Test all admin functionality
3. Monitor Firebase Console for any remaining errors
4. Set up proper backup and monitoring

## Support

If you're still having issues:
1. Check the browser console for detailed error messages
2. Look at Firebase Console → Firestore → Usage for permission errors
3. Test with the Firestore Rules Simulator in Firebase Console ght 