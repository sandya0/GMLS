package com.example.gmls.domain.repository

import com.example.gmls.domain.model.User
import com.example.gmls.ui.screens.auth.RegistrationData
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for user-related operations
 */
interface UserRepository {
    /**
     * Register a new user
     * @return User ID if successful
     */
    suspend fun registerUser(userData: RegistrationData): Result<String>

    /**
     * Login a user with email and password
     * @return User ID if successful
     */
    suspend fun loginUser(email: String, password: String): Result<String>

    /**
     * Logout the current user
     */
    fun logoutUser()

    /**
     * Get the current logged-in user ID
     * @return User ID or null if not logged in
     */
    fun getCurrentUserId(): String?

    /**
     * Check if a user is currently logged in
     * @return true if a user is logged in
     */
    fun isUserLoggedIn(): Boolean

    /**
     * Get a user's profile information
     * @param userId The ID of the user
     * @return User data if successful
     */
    suspend fun getUserProfile(userId: String): Result<User>

    /**
     * Observe changes to a user's profile
     * @param userId The ID of the user
     * @return Flow of User data updates
     */
    fun observeUserProfile(userId: String): Flow<User?>

    /**
     * Update a user's profile information
     * @param userId The ID of the user
     * @param updates Map of fields to update
     */
    suspend fun updateUserProfile(userId: String, updates: Map<String, Any>): Result<Unit>

    /**
     * Save the FCM token for push notifications
     * @param token The FCM token
     */
    suspend fun saveFCMToken(token: String): Result<Unit>
}