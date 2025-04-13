package com.example.gmls.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import com.example.gmls.domain.model.Disaster
import com.example.gmls.domain.model.DisasterFirebaseMapper
import com.example.gmls.domain.model.DisasterType
import com.example.gmls.ui.screens.auth.RegistrationData
import com.example.gmls.ui.screens.disaster.DisasterReport
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for handling Firebase operations
 */
@Singleton
class FirebaseService @Inject constructor(
    private val context: Context,
    private val disasterMapper: DisasterFirebaseMapper
) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    private val maxRetries = 3
    private val initialRetryDelay = 1000L // 1 second

    val authStateFlow: Flow<FirebaseUser?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser).isSuccess
        }
        auth.addAuthStateListener(authStateListener)
        awaitClose { auth.removeAuthStateListener(authStateListener) }
    }

    /**
     * Check if network is available
     */
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /**
     * Execute a Firebase operation with retry mechanism
     */
    private suspend fun <T> executeWithRetry(
        retryCount: Int = maxRetries,
        operation: suspend () -> T
    ): Result<T> = withContext(Dispatchers.IO) {
        if (!isNetworkAvailable()) {
            return@withContext Result.failure(NoInternetException())
        }

        var currentDelay = initialRetryDelay
        repeat(retryCount) { attempt ->
            try {
                return@withContext Result.success(operation())
            } catch (e: Exception) {
                if (attempt == retryCount - 1) {
                    return@withContext Result.failure(e)
                }
                when (e) {
                    is FirebaseNetworkException -> {
                        delay(currentDelay)
                        currentDelay *= 2 // Exponential backoff
                    }
                    is FirebaseAuthInvalidCredentialsException,
                    is FirebaseAuthInvalidUserException,
                    is IllegalArgumentException -> {
                        return@withContext Result.failure(e)
                    }
                    else -> {
                        if (attempt == retryCount - 1) {
                            return@withContext Result.failure(e)
                        }
                        delay(currentDelay)
                        currentDelay *= 2
                    }
                }
            }
        }
        Result.failure(Exception("Max retries exceeded"))
    }

    // Authentication

    /**
     * Login with email and password
     * @return FirebaseUser if successful
     */
    suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return executeWithRetry {
            auth.signInWithEmailAndPassword(email, password).await()
            // Return the currentUser from the auth instance directly
            auth.currentUser ?: throw Exception("Authentication failed, user is null after sign in")
        }
    }

    /**
     * Register a new user
     * @return FirebaseUser if successful
     */
    suspend fun register(userData: RegistrationData): Result<FirebaseUser> {
        return executeWithRetry {
            try {
                // Create authentication account
                val authResult = auth.createUserWithEmailAndPassword(userData.email, userData.password).await()
                val user = authResult.user ?: throw Exception("User creation failed")

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

                try {
                    firestore.collection("users").document(user.uid).set(userMap).await()
                } catch (e: Exception) {
                    // If Firestore update fails, delete the auth user to maintain consistency
                    user.delete().await()
                    throw Exception("Failed to save user data: ${e.message}")
                }

                user
            } catch (e: Exception) {
                when (e) {
                    is FirebaseAuthInvalidCredentialsException -> throw Exception("Invalid email or password format")
                    is FirebaseNetworkException -> throw Exception("Network error. Please check your connection")
                    else -> throw Exception("Registration failed: ${e.message}")
                }
            }
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
        return executeWithRetry {
            val snapshot = firestore.collection("disasters")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.map { document ->
                disasterMapper.mapToDisaster(document.id, document.data as Map<String, Any>)
            }
        }
    }

    /**
     * Get disasters filtered by type
     * @param type The disaster type to filter by
     * @return List of disasters of the specified type
     */
    suspend fun getDisastersByType(type: DisasterType): Result<List<Disaster>> {
        return executeWithRetry {
            val snapshot = firestore.collection("disasters")
                .whereEqualTo("type", type.name)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.map { document ->
                disasterMapper.mapToDisaster(document.id, document.data as Map<String, Any>)
            }
        }
    }

    /**
     * Get a specific disaster by ID
     * @param id The disaster ID
     * @return Disaster if found
     */
    suspend fun getDisasterById(id: String): Result<Disaster> {
        return executeWithRetry {
            val document = firestore.collection("disasters").document(id).get().await()
            if (!document.exists()) {
                throw NoSuchElementException("Disaster with ID $id not found")
            }
            disasterMapper.mapToDisaster(document.id, document.data as Map<String, Any>)
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
        return executeWithRetry {
            // Validate data
            validateDisasterReport(report)

            // Upload images
            val imageUrls = uploadImages(report.images)

            // Create disaster document
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
                "reportedBy" to (auth.currentUser?.uid ?: throw NotAuthenticatedException()),
                "updatedAt" to Date().time
            )

            val documentRef = firestore.collection("disasters").add(disasterMap).await()

            // Update user's reported disasters
            firestore.collection("users").document(auth.currentUser!!.uid)
                .collection("reportedDisasters")
                .document(documentRef.id)
                .set(mapOf("timestamp" to Date().time))
                .await()

            documentRef.id
        }
    }

    /**
     * Upload images to Firebase Storage
     * @param images List of image URIs
     * @return List of download URLs
     */
    private suspend fun uploadImages(images: List<Uri>): List<String> {
        return images.map { imageUri ->
            executeWithRetry {
                val fileName = "${UUID.randomUUID()}.jpg"
                val storageRef = storage.reference.child("disaster_images/$fileName")

                try {
                    storageRef.putFile(imageUri).await()
                    storageRef.downloadUrl.await().toString()
                } catch (e: StorageException) {
                    throw ImageUploadException("Failed to upload image", e)
                }
            }.getOrThrow()
        }
    }

    private fun validateDisasterReport(report: DisasterReport) {
        if (report.title.isBlank()) throw ValidationException("Title cannot be empty")
        if (report.description.isBlank()) throw ValidationException("Description cannot be empty")
        if (!report.useCurrentLocation && report.location.isBlank()) throw ValidationException("Location cannot be empty")
        if (report.affectedCount < 0) throw ValidationException("Affected count cannot be negative")
    }

    /**
     * Update the status of a disaster
     * @param disasterId The disaster ID
     * @param status The new status
     */
    suspend fun updateDisasterStatus(disasterId: String, status: Disaster.Status): Result<Unit> {
        return executeWithRetry {
            firestore.collection("disasters").document(disasterId)
                .update(
                    mapOf(
                        "status" to status.name,
                        "updatedAt" to Date().time
                    )
                )
                .await()
        }
    }

    // User profile operations

    /**
     * Get a user's profile
     * @param userId The user ID (defaults to current user)
     * @return Map of user data
     */
    suspend fun getUserProfile(userId: String = auth.currentUser?.uid ?: ""): Result<Map<String, Any>> {
        return executeWithRetry {
            if (userId.isEmpty()) {
                throw IllegalStateException("No user is currently logged in")
            }

            val document = firestore.collection("users").document(userId).get().await()

            if (document.exists()) {
                document.data as Map<String, Any>
            } else {
                throw NoSuchElementException("User profile not found")
            }
        }
    }

    /**
     * Update a user's profile
     * @param updates Map of fields to update
     */
    suspend fun updateUserProfile(updates: Map<String, Any>): Result<Unit> {
        return executeWithRetry {
            val userId = auth.currentUser?.uid
                ?: throw IllegalStateException("No user is currently logged in")

            val updatedMap = updates.toMutableMap().apply {
                put("updatedAt", Date().time)
            }

            firestore.collection("users").document(userId)
                .update(updatedMap)
                .await()
        }
    }

    // Push notification token management

    /**
     * Save the FCM token for push notifications
     * @param token The FCM token
     */
    suspend fun saveUserFCMToken(token: String): Result<Unit> {
        return executeWithRetry {
            val userId = auth.currentUser?.uid
                ?: throw IllegalStateException("No user is currently logged in")

            firestore.collection("users").document(userId)
                .update("fcmToken", token)
                .await()
        }
    }

    // Add these public methods
    suspend fun <T> executeFirestoreOperation(operation: suspend () -> T): Result<T> {
        return executeWithRetry { operation() }
    }

    fun getFirestore(): FirebaseFirestore = firestore
}

class NoInternetException : Exception("No internet connection available")
class NotAuthenticatedException : Exception("User not authenticated")
class ValidationException(message: String) : Exception(message)
class ImageUploadException(message: String, cause: Throwable) : Exception(message, cause)