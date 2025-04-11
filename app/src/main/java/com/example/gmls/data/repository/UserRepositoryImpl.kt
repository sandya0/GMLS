package com.example.gmls.data.repository

import com.example.gmls.data.remote.FirebaseService
import com.example.gmls.domain.model.User
import com.example.gmls.domain.model.UserFirebaseMapper
import com.example.gmls.domain.repository.UserRepository
import com.example.gmls.ui.screens.auth.RegistrationData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Implementation of UserRepository that uses Firebase for data storage
 */
class UserRepositoryImpl @Inject constructor(
    private val firebaseService: FirebaseService,
    private val userMapper: UserFirebaseMapper
) : UserRepository {

    override suspend fun registerUser(userData: RegistrationData): Result<String> {
        return try {
            val result = firebaseService.register(userData)
            if (result.isSuccess) {
                Result.success(result.getOrThrow().uid)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Registration failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loginUser(email: String, password: String): Result<String> {
        return try {
            val result = firebaseService.login(email, password)
            if (result.isSuccess) {
                Result.success(result.getOrThrow().uid)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun logoutUser() {
        firebaseService.logout()
    }

    override fun getCurrentUserId(): String? {
        return firebaseService.getCurrentUser()?.uid
    }

    override fun isUserLoggedIn(): Boolean {
        return firebaseService.getCurrentUser() != null
    }

    override suspend fun getUserProfile(userId: String): Result<User> {
        return try {
            val result = firebaseService.getUserProfile(userId)
            if (result.isSuccess) {
                val userData = result.getOrThrow()
                Result.success(userMapper.mapToUser(userId, userData))
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Failed to get user profile"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeUserProfile(userId: String): Flow<User?> = flow {
        val result = getUserProfile(userId)
        if (result.isSuccess) {
            emit(result.getOrThrow())
        } else {
            emit(null)
        }
    }

    override suspend fun updateUserProfile(userId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            firebaseService.updateUserProfile(updates)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveFCMToken(token: String): Result<Unit> {
        return try {
            firebaseService.saveUserFCMToken(token)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}