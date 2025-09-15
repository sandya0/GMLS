package com.example.gmls.data.repository

import com.example.gmls.data.mapper.UserFirebaseMapper
import com.example.gmls.data.model.UserDto
import com.example.gmls.data.remote.FirebaseService
import com.example.gmls.domain.model.AdminStats
import com.example.gmls.domain.model.Disaster
import com.example.gmls.domain.model.User
import com.example.gmls.domain.model.UserStats
import com.example.gmls.domain.model.UserRoles
import com.example.gmls.domain.model.FirestoreCollections
import com.example.gmls.domain.model.ErrorMessages
import com.example.gmls.domain.repository.AdminRepository
import com.example.gmls.domain.validation.ValidationUtils
import com.example.gmls.domain.validation.ValidationResult
import com.example.gmls.ui.screens.auth.RegistrationData
import com.example.gmls.ui.viewmodels.AdminAuditLog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log

@Singleton
class AdminRepositoryImpl @Inject constructor(
    private val firebaseService: FirebaseService,
    private val userMapper: UserFirebaseMapper
) : AdminRepository {

    override suspend fun getAllUsers(includeAdminCreatedOnly: Boolean): List<User> {
        return try {
            // Validate admin privileges first
            if (!isCurrentUserAdmin()) {
                throw SecurityException(ErrorMessages.ADMIN_PERMISSION_DENIED)
            }
            
            val firestore = firebaseService.getFirestore()
            val snapshot = firestore.collection(FirestoreCollections.USERS).get().await()
            
            val users = snapshot.documents.mapNotNull { document ->
                try {
                    val data = document.data ?: return@mapNotNull null
                    userMapper.mapFromFirebase(document.id, data)
                } catch (e: Exception) {
                    null // Skip invalid documents
                }
            }
            
            if (includeAdminCreatedOnly) {
                // Filter users created by admin
                users.filter { it.createdByAdmin }
            } else {
                users
            }
        } catch (e: Exception) {
            if (e is SecurityException) throw e
            throw Exception("Failed to fetch users: ${e.message}")
        }
    }

    /**
     * Get all users including their location data for admin map
     */
    override suspend fun getAllUsersWithLocation(): List<User> {
        return try {
            // Validate admin privileges first
            if (!isCurrentUserAdmin()) {
                throw SecurityException(ErrorMessages.ADMIN_PERMISSION_DENIED)
            }
            
            val firestore = firebaseService.getFirestore()
            val snapshot = firestore.collection(FirestoreCollections.USERS).get().await()
            
            val users = snapshot.documents.mapNotNull { document ->
                try {
                    val data = document.data ?: return@mapNotNull null
                    val user = userMapper.mapFromFirebase(document.id, data)
                    
                    // Include location data if available
                    val latitude = (data["latitude"] as? Number)?.toDouble()
                    val longitude = (data["longitude"] as? Number)?.toDouble()
                    
                    user.copy(
                        latitude = latitude,
                        longitude = longitude
                    )
                } catch (e: Exception) {
                    null // Skip invalid documents
                }
            }
            
            users
        } catch (e: Exception) {
            if (e is SecurityException) throw e
            throw Exception("Failed to fetch users with location data: ${e.message}")
        }
    }

    override suspend fun getUserStats(): UserStats {
        return try {
            if (!isCurrentUserAdmin()) {
                throw SecurityException(ErrorMessages.ADMIN_PERMISSION_DENIED)
            }
            
            val firestore = firebaseService.getFirestore()
            val snapshot = firestore.collection(FirestoreCollections.USERS).get().await()
            
            val totalUsers = snapshot.size()
            val activeUsers = snapshot.documents.count { it.getBoolean("isActive") != false }
            val verifiedUsers = snapshot.documents.count { it.getBoolean("isVerified") == true }
            val adminUsers = snapshot.documents.count { it.getString("role") == UserRoles.ADMIN }
            
            UserStats(
                totalUsers = totalUsers,
                activeUsers = activeUsers,
                verifiedUsers = verifiedUsers,
                adminUsers = adminUsers
            )
        } catch (e: Exception) {
            if (e is SecurityException) throw e
            throw Exception("Failed to fetch user stats: ${e.message}")
        }
    }

    override suspend fun getDisasterStats(): AdminStats {
        return try {
            if (!isCurrentUserAdmin()) {
                throw SecurityException(ErrorMessages.ADMIN_PERMISSION_DENIED)
            }
            
            val firestore = firebaseService.getFirestore()
            val snapshot = firestore.collection(FirestoreCollections.DISASTERS).get().await()
            
            val totalDisasters = snapshot.size()
            val activeDisasters = snapshot.documents.count { 
                val status = it.getString("status")
                status == "REPORTED" || status == "VERIFIED" || status == "IN_PROGRESS"
            }
            val resolvedDisasters = snapshot.documents.count { it.getString("status") == "RESOLVED" }
            val pendingReports = snapshot.documents.count { it.getString("status") == "REPORTED" }
            
            AdminStats(
                totalDisasters = totalDisasters,
                activeDisasters = activeDisasters,
                resolvedDisasters = resolvedDisasters,
                pendingReports = pendingReports
            )
        } catch (e: Exception) {
            if (e is SecurityException) throw e
            throw Exception("Failed to fetch disaster stats: ${e.message}")
        }
    }

    override suspend fun updateUserVerifiedStatus(userId: String, isVerified: Boolean): Result<Unit> {
        return try {
            if (!isCurrentUserAdmin()) {
                return Result.failure(SecurityException(ErrorMessages.ADMIN_PERMISSION_DENIED))
            }
            
            // Validate user ID
            when (val validation = ValidationUtils.validateUserId(userId)) {
                is ValidationResult.Invalid -> return Result.failure(IllegalArgumentException(validation.message))
                ValidationResult.Valid -> {}
            }
            
            val firestore = firebaseService.getFirestore()
            val updates = mapOf(
                "isVerified" to isVerified,
                "updatedAt" to Date().time
            )
            
            firestore.collection(FirestoreCollections.USERS).document(userId)
                .update(updates)
                .await()
                
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserActiveStatus(userId: String, isActive: Boolean): Result<Unit> {
        return try {
            if (!isCurrentUserAdmin()) {
                return Result.failure(SecurityException(ErrorMessages.ADMIN_PERMISSION_DENIED))
            }
            
            // Validate user ID
            when (val validation = ValidationUtils.validateUserId(userId)) {
                is ValidationResult.Invalid -> return Result.failure(IllegalArgumentException(validation.message))
                ValidationResult.Valid -> {}
            }
            
            // Prevent admin from deactivating themselves
            val currentUserId = firebaseService.getCurrentUser()?.uid
            if (userId == currentUserId && !isActive) {
                return Result.failure(IllegalArgumentException(ErrorMessages.CANNOT_DEACTIVATE_SELF))
            }
            
            val firestore = firebaseService.getFirestore()
            val updates = mapOf(
                "isActive" to isActive,
                "updatedAt" to Date().time
            )
            
            firestore.collection(FirestoreCollections.USERS).document(userId)
                .update(updates)
                .await()
                
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun createUser(registrationData: RegistrationData): Result<String> {
        return try {
            if (!isCurrentUserAdmin()) {
                return Result.failure(SecurityException(ErrorMessages.ADMIN_PERMISSION_DENIED))
            }
            
            // Validate registration data
            val validationErrors = ValidationUtils.validateUserCreationData(
                registrationData.email,
                registrationData.password,
                registrationData.fullName,
                registrationData.phoneNumber,
                null // address not in RegistrationData
            )
            
            if (validationErrors.isNotEmpty()) {
                return Result.failure(IllegalArgumentException(validationErrors.joinToString(", ")))
            }
            
            val result = firebaseService.register(registrationData)
            
            if (result.isSuccess) {
                val user = result.getOrThrow()
                Result.success(user.uid)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Failed to create user"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getUserById(userId: String): User? {
        return try {
            if (!isCurrentUserAdmin()) {
                throw SecurityException(ErrorMessages.ADMIN_PERMISSION_DENIED)
            }
            
            // Validate user ID
            when (val validation = ValidationUtils.validateUserId(userId)) {
                is ValidationResult.Invalid -> return null
                ValidationResult.Valid -> {}
            }
            
            val firestore = firebaseService.getFirestore()
            val document = firestore.collection(FirestoreCollections.USERS).document(userId).get().await()
                
            if (document.exists()) {
                val data = document.data ?: return null
                userMapper.mapFromFirebase(document.id, data)
            } else {
                null
            }
        } catch (e: Exception) {
            if (e is SecurityException) throw e
            null
        }
    }
    
    override suspend fun deleteUser(userId: String): Result<Unit> {
        return try {
            if (!isCurrentUserAdmin()) {
                return Result.failure(SecurityException(ErrorMessages.ADMIN_PERMISSION_DENIED))
            }
            
            // Validate user ID
            when (val validation = ValidationUtils.validateUserId(userId)) {
                is ValidationResult.Invalid -> return Result.failure(IllegalArgumentException(validation.message))
                ValidationResult.Valid -> {}
            }
            
            // Prevent admin from deleting themselves
            val currentUserId = firebaseService.getCurrentUser()?.uid
            if (userId == currentUserId) {
                return Result.failure(IllegalArgumentException(ErrorMessages.CANNOT_DELETE_SELF))
            }
            
            val firestore = firebaseService.getFirestore()
            
            // Check if user exists before deletion
            val userDoc = firestore.collection(FirestoreCollections.USERS).document(userId).get().await()
            if (!userDoc.exists()) {
                return Result.failure(IllegalArgumentException(ErrorMessages.USER_NOT_FOUND))
            }
            
            // Delete user from Firestore
            firestore.collection(FirestoreCollections.USERS).document(userId).delete().await()
            
            // Log the deletion action
            val currentUser = firebaseService.getCurrentUser()
            if (currentUser != null) {
                val auditLog = AdminAuditLog(
                    id = UUID.randomUUID().toString(),
                    adminId = currentUser.uid,
                    adminName = currentUser.displayName ?: currentUser.email ?: "Admin Tidak Dikenal",
                    action = "DELETE_USER",
                    targetId = userId,
                    targetName = userDoc.getString("fullName") ?: "Unknown User",
                    targetType = "USER",
                    details = "User account deleted by admin",
                    timestamp = Date()
                )
                logAdminAction(auditLog)
            }
                
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createAdminUser(email: String, password: String, fullName: String): Result<String> {
        return try {
            if (!isCurrentUserAdmin()) {
                return Result.failure(SecurityException(ErrorMessages.ADMIN_PERMISSION_DENIED))
            }
            
            // Validate admin creation data
            val validationErrors = ValidationUtils.validateAdminCreationData(email, password, fullName)
            if (validationErrors.isNotEmpty()) {
                return Result.failure(IllegalArgumentException(validationErrors.joinToString(", ")))
            }
            
        // First create the user with Firebase Auth
        val registrationData = RegistrationData(
            email = email,
            password = password,
            fullName = fullName,
            // Default values for other fields
            phoneNumber = "",
            dateOfBirth = null,
            gender = "",
            locationPermissionGranted = false
        )
        
        val result = firebaseService.register(registrationData)
        
        if (result.isSuccess) {
            val userId = result.getOrThrow().uid
            
            // Now update the user's role to admin
            try {
                val updates = mapOf(
                        "role" to UserRoles.ADMIN,
                    "isVerified" to true,
                    "updatedAt" to Date().time
                )
                
                val firestore = firebaseService.getFirestore()
                    firestore.collection(FirestoreCollections.USERS).document(userId)
                    .update(updates)
                    .await()
                    
                    Result.success(userId)
            } catch (e: Exception) {
                    Result.failure(e)
                }
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Failed to create admin user"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logAdminAction(log: AdminAuditLog): Result<Unit> {
        return try {
            if (!isCurrentUserAdmin()) {
                return Result.failure(SecurityException(ErrorMessages.ADMIN_PERMISSION_DENIED))
            }
            
            val firestore = firebaseService.getFirestore()
            val logData = mapOf(
                "adminId" to log.adminId,
                "adminName" to log.adminName,
                "action" to log.action,
                "targetId" to log.targetId,
                "targetName" to log.targetName,
                "targetType" to log.targetType,
                "details" to log.details,
                "timestamp" to log.timestamp.time
            )
            
            firestore.collection(FirestoreCollections.ADMIN_AUDIT_LOGS)
                .document(log.id)
                .set(logData)
                .await()
                
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAdminAuditLogs(limit: Int): List<AdminAuditLog> {
        return try {
            if (!isCurrentUserAdmin()) {
                throw SecurityException(ErrorMessages.ADMIN_PERMISSION_DENIED)
            }
            
            val firestore = firebaseService.getFirestore()
            val snapshot = firestore.collection(FirestoreCollections.ADMIN_AUDIT_LOGS)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()
                
            snapshot.documents.mapNotNull { document ->
                try {
                val data = document.data ?: return@mapNotNull null
                AdminAuditLog(
                    id = document.id,
                    adminId = data["adminId"] as? String ?: "",
                    adminName = data["adminName"] as? String ?: "",
                    action = data["action"] as? String ?: "",
                    targetId = data["targetId"] as? String,
                    targetName = data["targetName"] as? String,
                        targetType = data["targetType"] as? String,
                    details = data["details"] as? String ?: "",
                        timestamp = Date((data["timestamp"] as? Long) ?: 0L)
                )
                } catch (e: Exception) {
                    null // Skip invalid documents
                }
            }
        } catch (e: Exception) {
            if (e is SecurityException) throw e
            emptyList()
        }
    }

    override fun observeAdminAuditLogs(limit: Int): Flow<List<AdminAuditLog>> = callbackFlow {
        try {
            // First check if current user is admin before setting up listener
            val currentUserId = firebaseService.getCurrentUser()?.uid
            if (currentUserId == null) {
                Log.w("AdminRepositoryImpl", "No authenticated user for audit logs")
                trySend(emptyList())
                close()
                return@callbackFlow
            }

            // Check if user is admin before setting up listener
            val firestore = firebaseService.getFirestore()
            val userDoc = firestore.collection(FirestoreCollections.USERS)
                .document(currentUserId)
                .get()
                .await()

            if (!userDoc.exists() || userDoc.getString("role") != UserRoles.ADMIN) {
                Log.w("AdminRepositoryImpl", "User is not admin, cannot observe audit logs")
                trySend(emptyList())
                close()
                return@callbackFlow
            }

            // Only set up listener if user is confirmed admin
            val listener = firestore.collection(FirestoreCollections.ADMIN_AUDIT_LOGS)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("AdminRepositoryImpl", "Error in audit logs listener", error)
                        close(error)
                        return@addSnapshotListener
                    }
                    
                    val logs = snapshot?.documents?.mapNotNull { document ->
                        try {
                            val data = document.data ?: return@mapNotNull null
                            AdminAuditLog(
                                id = document.id,
                                adminId = data["adminId"] as? String ?: "",
                                adminName = data["adminName"] as? String ?: "",
                                action = data["action"] as? String ?: "",
                                targetId = data["targetId"] as? String,
                                targetName = data["targetName"] as? String,
                                targetType = data["targetType"] as? String,
                                details = data["details"] as? String ?: "",
                                timestamp = Date((data["timestamp"] as? Long) ?: 0L)
                            )
                        } catch (e: Exception) {
                            Log.w("AdminRepositoryImpl", "Error parsing audit log", e)
                            null
                        }
                    } ?: emptyList()
                        
                    trySend(logs)
                }
                
            awaitClose { 
                listener.remove()
                Log.d("AdminRepositoryImpl", "Audit logs listener removed")
            }
        } catch (e: Exception) {
            Log.e("AdminRepositoryImpl", "Error setting up audit logs listener", e)
            trySend(emptyList())
            close(e)
        }
    }

    override suspend fun isCurrentUserAdmin(): Boolean {
        val currentUserId = firebaseService.getCurrentUser()?.uid ?: return false
        
        return try {
            val firestore = firebaseService.getFirestore()
            val document = firestore.collection(FirestoreCollections.USERS).document(currentUserId).get().await()
            
            if (document.exists()) {
                val role = document.getString("role")
                role == UserRoles.ADMIN
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getAdminAnalytics(): Map<String, Any> {
        return try {
            if (!isCurrentUserAdmin()) {
                throw SecurityException(ErrorMessages.ADMIN_PERMISSION_DENIED)
            }
            
            val firestore = firebaseService.getFirestore()
            
            // Get user analytics
            val usersSnapshot = firestore.collection(FirestoreCollections.USERS).get().await()
            val users = usersSnapshot.documents.size
            val verifiedUsers = usersSnapshot.documents.count { it.getBoolean("isVerified") == true }
            val activeUsers = usersSnapshot.documents.count { it.getBoolean("isActive") != false } // Default to true
            val adminUsers = usersSnapshot.documents.count { it.getString("role") == UserRoles.ADMIN }
            
            // Get disaster analytics
            val disastersSnapshot = firestore.collection(FirestoreCollections.DISASTERS).get().await()
            val disasters = disastersSnapshot.documents.size
            
            // Get recent activity (last 7 days)
            val sevenDaysAgo = Date().time - (7 * 24 * 60 * 60 * 1000)
            val recentUsers = usersSnapshot.documents.count { 
                (it.getTimestamp("createdAt")?.toDate()?.time ?: 0) > sevenDaysAgo 
            }
            val recentDisasters = disastersSnapshot.documents.count { 
                (it.getLong("timestamp") ?: 0) > sevenDaysAgo 
            }
            
            mapOf(
                "totalUsers" to users,
                "verifiedUsers" to verifiedUsers,
                "activeUsers" to activeUsers,
                "adminUsers" to adminUsers,
                "totalDisasters" to disasters,
                "recentUsers" to recentUsers,
                "recentDisasters" to recentDisasters,
                "lastUpdated" to Date().time
            )
        } catch (e: Exception) {
            if (e is SecurityException) throw e
            emptyMap()
        }
    }
} 
