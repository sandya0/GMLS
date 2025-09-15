package com.example.gmls.data.repository

import com.example.gmls.domain.model.User
import com.example.gmls.domain.repository.AuthRepository
import com.example.gmls.ui.screens.auth.RegistrationData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import com.example.gmls.data.mapper.UserFirebaseMapper
import com.example.gmls.domain.model.EmergencyContact

@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userMapper: UserFirebaseMapper
) : AuthRepository {

    override suspend fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser ?: return null
        
        // Attempt to get a fresh token to ensure the session is still valid
        try {
            firebaseUser.getIdToken(true).await()
        } catch (e: Exception) {
            // If token refresh fails, the session might be invalid
            // But don't return null yet, still attempt to get the user data
        }
        
        return try {
            val userDoc = firestore.collection("users").document(firebaseUser.uid).get().await()
            if (!userDoc.exists()) return null
            val data = userDoc.data ?: return null
            val user = userMapper.mapToUser(firebaseUser.uid, data)
            
            // If we successfully retrieved user data, update the local cache
            user
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getCurrentUserSafe(): User? {
        val firebaseUser = auth.currentUser ?: return null
        
        return try {
            // Don't attempt token refresh in safe mode
            val userDoc = firestore.collection("users").document(firebaseUser.uid).get().await()
            if (!userDoc.exists()) return null
            val data = userDoc.data ?: return null
            userMapper.mapToUser(firebaseUser.uid, data)
        } catch (e: Exception) {
            // Log but don't throw on permission errors
            if (e.message?.contains("PERMISSION_DENIED") == true) {
                android.util.Log.w("FirebaseAuthRepository", "Permission denied accessing user document for ${firebaseUser.uid}")
            } else {
                android.util.Log.w("FirebaseAuthRepository", "Error getting user document safely", e)
            }
            null
        }
    }

    override fun getCurrentUserId(): String? = auth.currentUser?.uid

    override suspend fun login(email: String, password: String): User {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        val userDoc = firestore.collection("users").document(result.user!!.uid).get().await()
        if (!userDoc.exists()) throw Exception("User data not found")
        val data = userDoc.data ?: throw Exception("User data is null")
        return userMapper.mapToUser(result.user!!.uid, data)
    }

    override suspend fun register(registrationData: RegistrationData): User {
        val result = auth.createUserWithEmailAndPassword(registrationData.email, registrationData.password).await()
        val user = User(
            id = result.user!!.uid,
            email = registrationData.email,
            fullName = registrationData.fullName,
            phoneNumber = registrationData.phoneNumber,
            dateOfBirth = registrationData.dateOfBirth,
            gender = registrationData.gender,
            nationalId = registrationData.nationalId,
            familyCardNumber = registrationData.familyCardNumber,
            placeOfBirth = registrationData.placeOfBirth,
            religion = registrationData.religion,
            maritalStatus = registrationData.maritalStatus,
            familyRelationshipStatus = registrationData.familyRelationshipStatus,
            lastEducation = registrationData.lastEducation,
            occupation = registrationData.occupation,
            economicStatus = registrationData.economicStatus,
            latitude = registrationData.latitude,
            longitude = registrationData.longitude,
            address = registrationData.address,
            bloodType = registrationData.bloodType,
            medicalConditions = listOf(registrationData.medicalConditions),
            disabilities = listOf(registrationData.disabilities),
            emergencyContact = EmergencyContact(
                name = registrationData.emergencyContactName,
                relationship = registrationData.emergencyContactRelationship,
                phoneNumber = registrationData.emergencyContactPhone
            ),
            householdMembers = emptyList(),
            locationPermissionGranted = registrationData.locationPermissionGranted,
            role = "user",
            createdAt = java.util.Date(),
            updatedAt = java.util.Date()
        )
        
        val userData = userMapper.mapToFirestore(user)
        firestore.collection("users").document(user.id).set(userData).await()
        return user
    }

    override suspend fun logout() {
        auth.signOut()
    }
} 
