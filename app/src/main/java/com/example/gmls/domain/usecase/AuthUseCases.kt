package com.example.gmls.domain.usecase

import com.example.gmls.domain.repository.UserRepository
import com.example.gmls.ui.screens.auth.RegistrationData
import javax.inject.Inject

/**
 * Use case for logging in a user
 */
class LoginUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<String> {
        return userRepository.loginUser(email, password)
    }
}

/**
 * Use case for registering a new user
 */
class RegisterUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userData: RegistrationData): Result<String> {
        return userRepository.registerUser(userData)
    }
}

/**
 * Use case for logging out the current user
 */
class LogoutUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke() {
        userRepository.logoutUser()
    }
}

/**
 * Use case for checking if a user is logged in
 */
class IsUserLoggedInUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(): Boolean {
        return userRepository.isUserLoggedIn()
    }
}

/**
 * Use case for getting the current user ID
 */
class GetCurrentUserIdUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(): String? {
        return userRepository.getCurrentUserId()
    }
}