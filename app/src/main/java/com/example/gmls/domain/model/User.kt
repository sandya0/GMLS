package com.example.gmls.domain.model


import java.util.Date

/**
 * Domain model representing a user in the application
 */
data class User(
    val id: String,
    val fullName: String,
    val email: String,
    val phoneNumber: String,
    val dateOfBirth: Date?,
    val gender: String,
    val nationalId: String,
    val address: String,
    val bloodType: String,
    val medicalConditions: String,
    val disabilities: String,
    val emergencyContact: EmergencyContact,
    val householdMembers: Int,
    val locationPermissionGranted: Boolean,
    val createdAt: Date,
    val updatedAt: Date
)

/**
 * Emergency contact information for a user
 */
data class EmergencyContact(
    val name: String,
    val relationship: String,
    val phoneNumber: String
)

/**
 * Firebase data mapper for User objects
 */
class UserFirebaseMapper {
    fun mapToUser(id: String, data: Map<String, Any>): User {
        return User(
            id = id,
            fullName = data["fullName"] as String,
            email = data["email"] as String,
            phoneNumber = data["phoneNumber"] as String,
            dateOfBirth = data["dateOfBirth"] as? Date,
            gender = data["gender"] as String,
            nationalId = data["nationalId"] as String,
            address = data["address"] as String,
            bloodType = data["bloodType"] as String,
            medicalConditions = data["medicalConditions"] as? String ?: "",
            disabilities = data["disabilities"] as? String ?: "",
            emergencyContact = EmergencyContact(
                name = data["emergencyContactName"] as String,
                relationship = data["emergencyContactRelationship"] as String,
                phoneNumber = data["emergencyContactPhone"] as String
            ),
            householdMembers = (data["householdMembers"] as Number).toInt(),
            locationPermissionGranted = data["locationPermissionGranted"] as Boolean,
            createdAt = data["createdAt"] as Date,
            updatedAt = data["updatedAt"] as? Date ?: data["createdAt"] as Date
        )
    }

    fun mapToFirebaseObject(user: User): Map<String, Any> {
        return mapOf(
            "fullName" to user.fullName,
            "email" to user.email,
            "phoneNumber" to user.phoneNumber,
            "dateOfBirth" to user.dateOfBirth,
            "gender" to user.gender,
            "nationalId" to user.nationalId,
            "address" to user.address,
            "bloodType" to user.bloodType,
            "medicalConditions" to user.medicalConditions,
            "disabilities" to user.disabilities,
            "emergencyContactName" to user.emergencyContact.name,
            "emergencyContactRelationship" to user.emergencyContact.relationship,
            "emergencyContactPhone" to user.emergencyContact.phoneNumber,
            "householdMembers" to user.householdMembers,
            "locationPermissionGranted" to user.locationPermissionGranted,
            "updatedAt" to Date()
        )
    }
}