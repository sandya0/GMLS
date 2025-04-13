package com.example.gmls.data.repository

import com.example.gmls.data.remote.FirebaseService
import com.example.gmls.domain.model.User
import com.example.gmls.domain.model.UserFirebaseMapper
import com.example.gmls.domain.model.EmergencyContact
import com.example.gmls.domain.model.HouseholdMember
import com.example.gmls.domain.repository.UserRepository
import com.example.gmls.ui.screens.auth.RegistrationData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import java.util.*

class UserRepositoryImpl @Inject constructor(
    private val firebaseService: FirebaseService,
    private val userMapper: UserFirebaseMapper
) : UserRepository {
    override suspend fun login(email: String, password: String): Result<String> {
        return firebaseService.login(email, password).map { it.uid }
    }

    override suspend fun register(registrationData: RegistrationData): Result<String> {
        return firebaseService.register(registrationData).map { it.uid }
    }

    override suspend fun logout() {
        firebaseService.logout()
    }

    override fun isUserLoggedIn(): Boolean {
        return firebaseService.getCurrentUser() != null
    }

    override fun getCurrentUserId(): String? {
        return firebaseService.getCurrentUser()?.uid
    }

    override suspend fun getUserProfile(userId: String): User {
        return firebaseService.executeFirestoreOperation {
            val userDoc = firebaseService.getFirestore()
                .collection("users")
                .document(userId)
                .get()
                .await()

            if (!userDoc.exists()) {
                throw Exception("User not found")
            }

            val data = userDoc.data ?: throw Exception("User data is null")
            userMapper.mapToUser(userId, data)
        }.getOrThrow()
    }

    override fun observeUserProfile(userId: String): Flow<User?> {
        // TODO: Implement this using Firestore
        throw NotImplementedError("Not yet implemented")
    }

    override suspend fun updateUserProfile(userId: String, updates: Map<String, Any>): Result<Unit> {
        // TODO: Implement this using Firestore
        throw NotImplementedError("Not yet implemented")
    }

    override suspend fun saveFCMToken(token: String): Result<Unit> {
        // TODO: Implement this using Firestore
        throw NotImplementedError("Not yet implemented")
    }

    override fun getAuthStateFlow(): Flow<FirebaseUser?> {
        return firebaseService.authStateFlow
    }
}