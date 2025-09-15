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
    val createdByAdmin: Boolean = false,
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
    val profilePictureUrl: String? = null,
    val isVerified: Boolean = false,
    val isActive: Boolean = true
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
    val id: String = "",
    val name: String = "",
    val relationship: String = "",
    val age: Int = 0,
    val specialNeeds: String = "",
    val gender: String = "",
    val bloodType: String = "",
    val medicalConditions: List<String> = emptyList(),
    val disabilities: List<String> = emptyList()
)
