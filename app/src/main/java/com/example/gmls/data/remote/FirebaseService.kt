package com.example.gmls.data.remote

import android.net.Uri
import com.example.gmls.domain.model.Disaster
import com.example.gmls.domain.model.DisasterFirebaseMapper
import com.example.gmls.domain.model.DisasterType
import com.example.gmls.ui.screens.auth.RegistrationData
import com.example.gmls.ui.screens.disaster.DisasterReport
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for handling Firebase operations
 */
@Singleton
class FirebaseService @Inject constructor(
    private val disasterMapper: DisasterFirebaseMapper
) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    // Authentication

    /**
     * Login with email and password
     * @return FirebaseUser if successful
     */
    suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(authResult.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Register a new user
     * @return FirebaseUser if successful
     */
    suspend fun register(userData: RegistrationData): Result<FirebaseUser> {
        return try {
            // Create authentication account
            val authResult = auth.createUserWithEmailAndPassword(userData.email, userData.password).await()
            val user = authResult.user!!

            // Save additional user data to Firestore
            val userMap = mapOf(
                "fullName" to userData.fullName,
                "email" to userData.email,
                "dateOfBirth" to userData.dateOfBirth,
                "gender" to userData.gender,
                "nationalId" to userData.nationalId,
                "phoneNumber" to userData.phoneNumber,
                "address" to userData.address,
                "bloodType" to userData.bloodType,
                "medicalConditions" to userData.medicalConditions,
                "disabilities" to userData.disabilities,
                "emergencyContactName" to userData.emergencyContactName,
                "emergencyContactRelationship" to userData.emergencyContactRelationship,
                "emergencyContactPhone" to userData.emergencyContactPhone,
                "householdMembers" to userData.householdMembers,
                "locationPermissionGranted" to userData.locationPermissionGranted,
                "createdAt" to Date()
            )

            firestore.collection("users").document(user.uid).set(userMap).await()

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Logout the current user
     */
    fun logout() {
        auth.signOut()
    }

    /**
     * Get the current user
     * @return FirebaseUser or null if not logged in
     */
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    // Disaster operations

    /**
     * Get all disasters
     * @return List of disasters
     */
    suspend fun getAllDisasters(): Result<List<Disaster>> {
        return try {
            val snapshot = firestore.collection("disasters")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            val disasters = snapshot.documents.map { document ->
                disasterMapper.mapToDisaster(document.id, document.data as Map<String, Any>)
            }

            Result.success(disasters)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get disasters filtered by type
     * @param type The disaster type to filter by
     * @return List of disasters of the specified type
     */
    suspend fun getDisastersByType(type: DisasterType): Result<List<Disaster>> {
        return try {
            val snapshot = firestore.collection("disasters")
                .whereEqualTo("type", type.name)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            val disasters = snapshot.documents.map { document ->
                disasterMapper.mapToDisaster(document.id, document.data as Map<String, Any>)
            }

            Result.success(disasters)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get a specific disaster by ID
     * @param id The disaster ID
     * @return Disaster if found
     */
    suspend fun getDisasterById(id: String): Result<Disaster> {
        return try {
            val document = firestore.collection("disasters").document(id).get().await()

            if (document.exists()) {
                val disaster = disasterMapper.mapToDisaster(document.id, document.data as Map<String, Any>)
                Result.success(disaster)
            } else {
                Result.failure(NoSuchElementException("Disaster with ID $id not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Report a new disaster
     * @param report The disaster report data
     * @param latitude The latitude coordinate
     * @param longitude The longitude coordinate
     * @return Disaster ID if successful
     */
    suspend fun reportDisaster(report: DisasterReport, latitude: Double, longitude: Double): Result<String> {
        return try {
            // Upload images to Firebase Storage
            val imageUrls = uploadImages(report.images)

            // Create disaster document in Firestore
            val disasterMap = mapOf(
                "title" to report.title,
                "description" to report.description,
                "location" to report.location,
                "type" to report.type.name,
                "timestamp" to report.timestamp.time,
                "affectedCount" to report.affectedCount,
                "images" to imageUrls,
                "status" to Disaster.Status.REPORTED.name,
                "latitude" to latitude,
                "longitude" to longitude,
                "reportedBy" to (auth.currentUser?.uid ?: "anonymous"),
                "updatedAt" to Date().time
            )

            val documentRef = firestore.collection("disasters").add(disasterMap).await()

            // Update user's reported disasters
            auth.currentUser?.uid?.let { userId ->
                firestore.collection("users").document(userId)
                    .collection("reportedDisasters")
                    .document(documentRef.id)
                    .set(mapOf("timestamp" to Date().time))
                    .await()
            }

            Result.success(documentRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Upload images to Firebase Storage
     * @param images List of image URIs
     * @return List of download URLs
     */
    private suspend fun uploadImages(images: List<Uri>): List<String> {
        val imageUrls = mutableListOf<String>()

        for (imageUri in images) {
            val fileName = "${UUID.randomUUID()}.jpg"
            val storageRef = storage.reference.child("disaster_images/$fileName")

            storageRef.putFile(imageUri).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()

            imageUrls.add(downloadUrl)
        }

        return imageUrls
    }

    /**
     * Update the status of a disaster
     * @param disasterId The disaster ID
     * @param status The new status
     */
    suspend fun updateDisasterStatus(disasterId: String, status: Disaster.Status): Result<Unit> {
        return try {
            firestore.collection("disasters").document(disasterId)
                .update(
                    mapOf(
                        "status" to status.name,
                        "updatedAt" to Date().time
                    )
                )
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // User profile operations

    /**
     * Get a user's profile
     * @param userId The user ID (defaults to current user)
     * @return Map of user data
     */
    suspend fun getUserProfile(userId: String = auth.currentUser?.uid ?: ""): Result<Map<String, Any>> {
        return try {
            if (userId.isEmpty()) {
                return Result.failure(IllegalStateException("No user is currently logged in"))
            }

            val document = firestore.collection("users").document(userId).get().await()

            if (document.exists()) {
                Result.success(document.data as Map<String, Any>)
            } else {
                Result.failure(NoSuchElementException("User profile not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update a user's profile
     * @param updates Map of fields to update
     */
    suspend fun updateUserProfile(updates: Map<String, Any>): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Result.failure(IllegalStateException("No user is currently logged in"))

            val updatedMap = updates.toMutableMap().apply {
                put("updatedAt", Date().time)
            }

            firestore.collection("users").document(userId)
                .update(updatedMap)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Push notification token management

    /**
     * Save the FCM token for push notifications
     * @param token The FCM token
     */
    suspend fun saveUserFCMToken(token: String): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Result.failure(IllegalStateException("No user is currently logged in"))

            firestore.collection("users").document(userId)
                .update("fcmToken", token)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}