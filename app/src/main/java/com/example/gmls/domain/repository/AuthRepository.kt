package com.example.gmls.domain.repository

import com.example.gmls.domain.model.User
import com.example.gmls.ui.screens.auth.RegistrationData

interface AuthRepository {
    /**
     * Get the current authenticated user
     * @return User if logged in and document exists, null otherwise
     */
    suspend fun getCurrentUser(): User?

    /**
     * Get the current authenticated user safely (doesn't fail on permission errors)
     * @return User if logged in and document exists, null otherwise
     */
    suspend fun getCurrentUserSafe(): User?
    fun getCurrentUserId(): String?
    suspend fun login(email: String, password: String): User
    suspend fun register(registrationData: RegistrationData): User
    suspend fun logout()
} 
