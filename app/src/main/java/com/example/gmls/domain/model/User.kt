package com.example.gmls.domain.model

import java.util.Date

/**
 * Domain model representing a user in the application
 */
data class User(
    val id: String = "",
    val email: String = "",
    val fullName: String = "",
    val phoneNumber: String = "",
    val dateOfBirth: Date? = null,
    val gender: String = "",
    val nationalId: String = "",
    val address: String = "",
    val bloodType: String = "",
    val medicalConditions: List<String> = emptyList(),
    val disabilities: List<String> = emptyList(),
    val emergencyContact: EmergencyContact = EmergencyContact(),
    val householdMembers: List<HouseholdMember> = emptyList(),
    val locationPermissionGranted: Boolean = false,
    val role: String = "user",
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val profilePictureUrl: String? = null
)

/**
 * Emergency contact information for a user
 */
data class EmergencyContact(
    val name: String = "",
    val relationship: String = "",
    val phoneNumber: String = ""
)

data class HouseholdMember(
    val name: String = "",
    val relationship: String = "",
    val age: Int = 0,
    val specialNeeds: String = ""
)

/**
 * Firebase data mapper for User objects
 */
class UserFirebaseMapper {
    fun mapToUser(id: String, data: Map<String, Any>): User {
        return User(
            id = id,
            fullName = data["fullName"] as? String ?: "",
            email = data["email"] as? String ?: "",
            phoneNumber = data["phoneNumber"] as? String ?: "",
            dateOfBirth = data["dateOfBirth"] as? Date,
            gender = data["gender"] as? String ?: "",
            nationalId = data["nationalId"] as? String ?: "",
            address = data["address"] as? String ?: "",
            bloodType = data["bloodType"] as? String ?: "",
            medicalConditions = (data["medicalConditions"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            disabilities = (data["disabilities"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            emergencyContact = EmergencyContact(
                name = data["emergencyContactName"] as? String ?: "",
                relationship = data["emergencyContactRelationship"] as? String ?: "",
                phoneNumber = data["emergencyContactPhone"] as? String ?: ""
            ),
            householdMembers = (data["householdMembers"] as? List<*>)?.mapNotNull { member ->
                (member as? Map<*, *>)?.let { map ->
                    HouseholdMember(
                        name = map["name"] as? String ?: "",
                        relationship = map["relationship"] as? String ?: "",
                        age = (map["age"] as? Number)?.toInt() ?: 0,
                        specialNeeds = map["specialNeeds"] as? String ?: ""
                    )
                }
            } ?: emptyList(),
            locationPermissionGranted = data["locationPermissionGranted"] as? Boolean ?: false,
            role = data["role"] as? String ?: "user",
            createdAt = data["createdAt"] as? Date ?: Date(),
            updatedAt = data["updatedAt"] as? Date ?: Date(),
            profilePictureUrl = data["profilePictureUrl"] as? String
        )
    }

    fun mapToFirebaseObject(user: User): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        map["fullName"] = user.fullName
        map["email"] = user.email
        map["phoneNumber"] = user.phoneNumber
        user.dateOfBirth?.let { map["dateOfBirth"] = it }
        map["gender"] = user.gender
        map["nationalId"] = user.nationalId
        map["address"] = user.address
        map["bloodType"] = user.bloodType
        map["medicalConditions"] = user.medicalConditions
        map["disabilities"] = user.disabilities
        map["emergencyContactName"] = user.emergencyContact.name
        map["emergencyContactRelationship"] = user.emergencyContact.relationship
        map["emergencyContactPhone"] = user.emergencyContact.phoneNumber
        map["householdMembers"] = user.householdMembers.map { member ->
            mapOf(
                "name" to member.name,
                "relationship" to member.relationship,
                "age" to member.age,
                "specialNeeds" to member.specialNeeds
            )
        }
        map["locationPermissionGranted"] = user.locationPermissionGranted
        map["role"] = user.role
        map["updatedAt"] = Date()
        user.profilePictureUrl?.let { map["profilePictureUrl"] = it }
        return map
    }
}