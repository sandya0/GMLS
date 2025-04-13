package com.example.gmls.domain.usecase

import com.example.gmls.domain.model.User
import com.example.gmls.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting a user's profile
 */
class GetUserProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String): User {
        return userRepository.getUserProfile(userId)
    }
}

/**
 * Use case for observing changes to a user's profile
 */
class ObserveUserProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(userId: String): Flow<User?> {
        return userRepository.observeUserProfile(userId)
    }
}

/**
 * Use case for updating a user's profile
 */
class UpdateUserProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(updates: Map<String, Any>): Result<Unit> {
        val userId = userRepository.getCurrentUserId() ?: return Result.failure(Exception("User not logged in"))
        return userRepository.updateUserProfile(userId, updates)
    }
}

/**
 * Use case for saving the FCM token for push notifications
 */
class SaveFCMTokenUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(token: String): Result<Unit> {
        return userRepository.saveFCMToken(token)
    }
}

class GetCurrentUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Result<User> {
        return try {
            val userId = userRepository.getCurrentUserId() 
                ?: return Result.failure(Exception("No user logged in"))
            Result.success(userRepository.getUserProfile(userId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}