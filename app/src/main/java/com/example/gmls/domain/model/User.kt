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
    val familyCardNumber: String = "",
    val placeOfBirth: String = "",
    val religion: String = "",
    val maritalStatus: String = "",
    val familyRelationshipStatus: String = "",
    val lastEducation: String = "",
    val occupation: String = "",
    val economicStatus: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
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
        // Emergency contact: support both nested map and flat fields
        val emergencyContact: EmergencyContact = when (val ec = data["emergencyContact"]) {
            is Map<*, *> -> EmergencyContact(
                name = ec["name"] as? String ?: "",
                relationship = ec["relationship"] as? String ?: "",
                phoneNumber = ec["phoneNumber"] as? String ?: ""
            )
            else -> EmergencyContact(
                name = data["emergencyContactName"] as? String ?: "",
                relationship = data["emergencyContactRelationship"] as? String ?: "",
                phoneNumber = data["emergencyContactPhone"] as? String ?: ""
            )
        }
        return User(
            id = id,
            fullName = data["fullName"] as? String ?: "",
            email = data["email"] as? String ?: "",
            phoneNumber = data["phoneNumber"] as? String ?: "",
            dateOfBirth = data["dateOfBirth"] as? Date,
            gender = data["gender"] as? String ?: "",
            nationalId = data["nationalId"] as? String ?: "",
            familyCardNumber = data["familyCardNumber"] as? String ?: "",
            placeOfBirth = data["placeOfBirth"] as? String ?: "",
            religion = data["religion"] as? String ?: "",
            maritalStatus = data["maritalStatus"] as? String ?: "",
            familyRelationshipStatus = data["familyRelationshipStatus"] as? String ?: "",
            lastEducation = data["lastEducation"] as? String ?: "",
            occupation = data["occupation"] as? String ?: "",
            economicStatus = data["economicStatus"] as? String ?: "",
            latitude = data["latitude"] as? Double,
            longitude = data["longitude"] as? Double,
            address = data["address"] as? String ?: "",
            bloodType = data["bloodType"] as? String ?: "",
            medicalConditions = (data["medicalConditions"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            disabilities = (data["disabilities"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            emergencyContact = emergencyContact,
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
        map["familyCardNumber"] = user.familyCardNumber
        map["placeOfBirth"] = user.placeOfBirth
        map["religion"] = user.religion
        map["maritalStatus"] = user.maritalStatus
        map["familyRelationshipStatus"] = user.familyRelationshipStatus
        map["lastEducation"] = user.lastEducation
        map["occupation"] = user.occupation
        map["economicStatus"] = user.economicStatus
        user.latitude?.let { map["latitude"] = it }
        user.longitude?.let { map["longitude"] = it }
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