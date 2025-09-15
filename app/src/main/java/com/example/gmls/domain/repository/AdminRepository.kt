package com.example.gmls.domain.repository

import com.example.gmls.domain.model.AdminStats
import com.example.gmls.domain.model.Disaster
import com.example.gmls.domain.model.User
import com.example.gmls.domain.model.UserStats
import com.example.gmls.ui.screens.auth.RegistrationData
import com.example.gmls.ui.viewmodels.AdminAuditLog
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for admin-specific operations
 */
interface AdminRepository {
    /**
     * Get all users (admin only)
     * @param includeAdminCreatedOnly If true, returns only users created by admin
     * @return List of users
     */
    suspend fun getAllUsers(includeAdminCreatedOnly: Boolean = false): List<User>
    
    /**
     * Get all users including their location data for admin map
     * @return List of users with location data
     */
    suspend fun getAllUsersWithLocation(): List<User>
    
    /**
     * Get user statistics
     * @return User statistics
     */
    suspend fun getUserStats(): UserStats
    
    /**
     * Get disaster statistics
     * @return Disaster statistics
     */
    suspend fun getDisasterStats(): AdminStats

    /**
     * Update a user's verified status
     * @param userId The ID of the user
     * @param isVerified The new verified status
     */
    suspend fun updateUserVerifiedStatus(userId: String, isVerified: Boolean): Result<Unit>

    /**
     * Update a user's active status
     * @param userId The ID of the user
     * @param isActive The new active status
     */
    suspend fun updateUserActiveStatus(userId: String, isActive: Boolean): Result<Unit>

    /**
     * Create a new user as admin
     * @param registrationData User registration data
     * @return Result with user ID if successful
     */
    suspend fun createUser(registrationData: RegistrationData): Result<String>

    /**
     * Get user by ID
     * @param userId The ID of the user to retrieve
     * @return User if found, null otherwise
     */
    suspend fun getUserById(userId: String): User?

    /**
     * Delete a user
     * @param userId The ID of the user to delete
     * @return Result indicating success or failure
     */
    suspend fun deleteUser(userId: String): Result<Unit>

    /**
     * Create a new admin user
     * @param email Email of the new admin
     * @param password Password for the new admin
     * @param fullName Full name of the new admin
     */
    suspend fun createAdminUser(email: String, password: String, fullName: String): Result<String>

    /**
     * Log an admin action
     * @param log The audit log entry
     */
    suspend fun logAdminAction(log: AdminAuditLog): Result<Unit>

    /**
     * Get admin audit logs
     * @param limit Maximum number of logs to retrieve
     * @return List of audit logs
     */
    suspend fun getAdminAuditLogs(limit: Int = 100): List<AdminAuditLog>

    /**
     * Observe admin audit logs in real-time
     * @param limit Maximum number of logs to observe
     * @return Flow of audit logs
     */
    fun observeAdminAuditLogs(limit: Int = 20): Flow<List<AdminAuditLog>>

    /**
     * Check if the current user has admin privileges
     * @return true if the user is an admin
     */
    suspend fun isCurrentUserAdmin(): Boolean

    /**
     * Get admin analytics data
     * @return Map of analytics data
     */
    suspend fun getAdminAnalytics(): Map<String, Any>
} 
