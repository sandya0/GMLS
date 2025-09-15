package com.example.gmls.util

import android.util.Log
import com.example.gmls.data.remote.FirebaseService
import com.example.gmls.domain.model.FirestoreCollections
import com.example.gmls.domain.model.UserRoles
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Context
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility class to help bootstrap the first admin user and fix permission issues
 */
@Singleton
class AdminBootstrap @Inject constructor(
    private val firebaseService: FirebaseService,
    private val context: Context
) {
    companion object {
        private const val TAG = "AdminBootstrap"
    }

    /**
     * Check if the current user needs to be upgraded to admin
     * This is useful for the initial setup when no admin exists
     */
    suspend fun checkAndBootstrapAdmin(): Result<Boolean> {
        return try {
            // First check if Google Play Services is available
            if (!GooglePlayServicesHelper.isGooglePlayServicesAvailable(context)) {
                Log.w(TAG, "Google Play Services not available, skipping admin bootstrap")
                return Result.success(false)
            }
            
            val currentUser = firebaseService.getCurrentUser()
            if (currentUser == null) {
                Log.d(TAG, "No current user found")
                return Result.success(false)
            }

            val firestore = firebaseService.getFirestore()
            val userId = currentUser.uid

            try {
                // Check if user document exists
                val userDoc = firestore.collection(FirestoreCollections.USERS)
                    .document(userId)
                    .get()
                    .await()

                if (!userDoc.exists()) {
                    Log.d(TAG, "User document doesn't exist, creating it")
                    // Create user document with user role by default
                    val userData = mapOf(
                        "email" to (currentUser.email ?: ""),
                        "fullName" to (currentUser.displayName ?: ""),
                        "role" to UserRoles.USER, // Default to user role
                        "isVerified" to false, // New users are not verified by default
                        "isActive" to true,
                        "createdAt" to Date().time,
                        "updatedAt" to Date().time,
                        "phoneNumber" to null,
                        "address" to null
                    )

                    firestore.collection(FirestoreCollections.USERS)
                        .document(userId)
                        .set(userData)
                        .await()

                    Log.d(TAG, "Admin user document created successfully")
                    return Result.success(true)
                } else {
                    Log.d(TAG, "User exists, no bootstrap needed.")
                    return Result.success(false)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in checkAndBootstrapAdmin", e)
                Result.failure(e)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in checkAndBootstrapAdmin", e)
            Result.failure(e)
        }
    }

    /**
     * Check if current user has admin privileges
     */
    suspend fun isCurrentUserAdmin(): Boolean {
        return try {
            val currentUser = firebaseService.getCurrentUser()
            if (currentUser == null) {
                Log.d(TAG, "No current user")
                return false
            }

            val firestore = firebaseService.getFirestore()
            val userDoc = firestore.collection(FirestoreCollections.USERS)
                .document(currentUser.uid)
                .get()
                .await()

            if (userDoc.exists()) {
                val role = userDoc.getString("role")
                val isAdmin = role == UserRoles.ADMIN
                Log.d(TAG, "User role: $role, isAdmin: $isAdmin")
                return isAdmin
            } else {
                Log.d(TAG, "User document doesn't exist")
                return false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking admin status", e)
            return false
        }
    }

    /**
     * Get current user information
     */
    suspend fun getCurrentUserInfo(): Result<Map<String, Any?>> {
        return try {
            val currentUser = firebaseService.getCurrentUser()
            if (currentUser == null) {
                return Result.failure(IllegalStateException("No authenticated user"))
            }

            val firestore = firebaseService.getFirestore()
            val userDoc = firestore.collection(FirestoreCollections.USERS)
                .document(currentUser.uid)
                .get()
                .await()

            if (userDoc.exists()) {
                val userData = userDoc.data ?: emptyMap()
                Log.d(TAG, "Current user data: $userData")
                Result.success(userData)
            } else {
                Log.d(TAG, "User document doesn't exist")
                Result.success(emptyMap())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current user info", e)
            Result.failure(e)
        }
    }

    /**
     * Fix common permission issues
     */
    suspend fun fixPermissionIssues(): Result<String> {
        return try {
            val currentUser = firebaseService.getCurrentUser()
            if (currentUser == null) {
                return Result.failure(IllegalStateException("No authenticated user"))
            }

            val firestore = firebaseService.getFirestore()
            val userId = currentUser.uid
            val issues = mutableListOf<String>()

            // Check if user document exists
            val userDoc = firestore.collection(FirestoreCollections.USERS)
                .document(userId)
                .get()
                .await()

            if (!userDoc.exists()) {
                issues.add(context.getString(com.example.gmls.R.string.user_document_missing))
                // Create user document
                val userData = mapOf(
                    "email" to (currentUser.email ?: ""),
                    "fullName" to (currentUser.displayName ?: context.getString(com.example.gmls.R.string.default_user)),
                    "role" to UserRoles.USER, // Default to user role
                    "isVerified" to false,
                    "isActive" to true,
                    "createdAt" to Date().time,
                    "updatedAt" to Date().time
                )

                firestore.collection(FirestoreCollections.USERS)
                    .document(userId)
                    .set(userData)
                    .await()
                
                issues.add(context.getString(com.example.gmls.R.string.created_user_document))
            } else {
                // Check required fields
                val data = userDoc.data ?: emptyMap()
                val requiredFields = listOf("email", "fullName", "role", "isVerified", "isActive")
                val missingFields = requiredFields.filter { !data.containsKey(it) }
                
                if (missingFields.isNotEmpty()) {
                    issues.add(context.getString(com.example.gmls.R.string.missing_fields_format, missingFields.joinToString(", ")))
                    
                    // Add missing fields
                    val updates = mutableMapOf<String, Any>()
                    missingFields.forEach { field ->
                        when (field) {
                            "email" -> updates[field] = currentUser.email ?: ""
                            "fullName" -> updates[field] = currentUser.displayName ?: context.getString(com.example.gmls.R.string.default_user)
                            "role" -> updates[field] = UserRoles.USER
                            "isVerified" -> updates[field] = false
                            "isActive" -> updates[field] = true
                        }
                    }
                    updates["updatedAt"] = Date().time

                    firestore.collection(FirestoreCollections.USERS)
                        .document(userId)
                        .update(updates)
                        .await()
                    
                    issues.add(context.getString(com.example.gmls.R.string.added_missing_fields))
                }
            }

            val summary = if (issues.isEmpty()) {
                context.getString(com.example.gmls.R.string.no_permission_issues_found)
            } else {
                context.getString(com.example.gmls.R.string.fixed_issues_format, issues.joinToString("; "))
            }

            Log.d(TAG, summary)
            Result.success(summary)
        } catch (e: Exception) {
            Log.e(TAG, "Error fixing permission issues", e)
            Result.failure(e)
        }
    }
} 
