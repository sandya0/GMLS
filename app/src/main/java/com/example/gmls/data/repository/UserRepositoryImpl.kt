package com.example.gmls.data.repository

import com.example.gmls.data.remote.FirebaseService
import com.example.gmls.domain.model.User
import com.example.gmls.data.mapper.UserFirebaseMapper
import com.example.gmls.domain.model.EmergencyContact
import com.example.gmls.domain.model.HouseholdMember
import com.example.gmls.domain.repository.UserRepository
import com.example.gmls.ui.screens.auth.RegistrationData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import java.util.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose

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

    override suspend fun updateUserProfile(userId: String, updates: Map<String, Any>): Result<Unit> {
        return if (firebaseService.getCurrentUser()?.uid == userId) {
            firebaseService.updateUserProfile(updates)
        } else {
            try {
                firebaseService.getFirestore().collection("users").document(userId)
                    .update(updates + ("updatedAt" to java.util.Date().time))
                    .await()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun updateUserLocation(userId: String, latitude: Double, longitude: Double): Result<Unit> {
        return try {
            val updates = mapOf(
                "latitude" to latitude,
                "longitude" to longitude,
                "updatedAt" to java.util.Date().time,
                "lastLocationUpdate" to java.util.Date().time
            )
            
            firebaseService.getFirestore().collection("users").document(userId)
                .update(updates)
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveFCMToken(token: String): Result<Unit> {
        return firebaseService.saveUserFCMToken(token)
    }

    override fun getAuthStateFlow(): Flow<FirebaseUser?> {
        return firebaseService.authStateFlow
    }

    override fun observeUserProfile(userId: String): kotlinx.coroutines.flow.Flow<User?> {
        return kotlinx.coroutines.flow.callbackFlow {
            val docRef = firebaseService.getFirestore().collection("users").document(userId)
            val listener = docRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val data = snapshot.data ?: return@addSnapshotListener
                    trySend(userMapper.mapToUser(userId, data))
                } else {
                    trySend(null)
                }
            }
            awaitClose { listener.remove() }
        }
    }

    // Method for admin to fetch all users
    override suspend fun getAllUsers(): List<User> {
        val result = firebaseService.getAllUsers()
        if (result.isSuccess) {
            return result.getOrThrow().mapNotNull { data ->
                val id = data["id"] as? String ?: return@mapNotNull null
                userMapper.mapToUser(id, data)
            }
        } else {
            return emptyList()
        }
    }
    
    // Method to get only admin-created users
    suspend fun getAdminCreatedUsers(): List<User> {
        return getAllUsers().filter { it.createdByAdmin }
    }
    
    // Method to update user status (active/inactive)
    suspend fun updateUserStatus(userId: String, isActive: Boolean): Result<Unit> {
        return updateUserProfile(userId, mapOf("isActive" to isActive))
    }
    
    // Method to delete a user
    suspend fun deleteUser(userId: String): Result<Unit> {
        return try {
            // Delete user document from Firestore
            firebaseService.getFirestore().collection("users").document(userId)
                .delete()
                .await()
                
            // If the user is also in Authentication, an admin with proper permissions would delete them
            // This would typically require Firebase Admin SDK or a backend function
            // For now, we'll just return success for the Firestore deletion
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Method to create a new user (admin function)
    suspend fun createUser(email: String, password: String, fullName: String, phone: String): Result<String> {
        return try {
            // This would typically be done with Firebase Admin SDK or a backend function
            // For now, we'll simulate it with regular Firebase Auth and then update the profile
            val authResult = firebaseService.register(RegistrationData(
                email = email,
                password = password,
                fullName = fullName,
                phoneNumber = phone
            ))
            
            if (authResult.isSuccess) {
                val userId = authResult.getOrThrow().uid
                // Mark this user as created by admin
                firebaseService.getFirestore().collection("users").document(userId)
                    .update(mapOf(
                        "createdByAdmin" to true,
                        "updatedAt" to java.util.Date().time
                    ))
                    .await()
                
                Result.success(userId)
            } else {
                // Convert the authResult to Result<String> to match the return type
                Result.failure(authResult.exceptionOrNull() ?: Exception("Failed to create user"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
