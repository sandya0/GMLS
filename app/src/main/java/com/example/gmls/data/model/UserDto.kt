package com.example.gmls.data.model

import java.util.Date

data class UserDto(
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
    val emergencyContact: EmergencyContactDto = EmergencyContactDto(),
    val householdMembers: List<HouseholdMemberDto> = emptyList(),
    val locationPermissionGranted: Boolean = false,
    val role: String = "user",
    val createdAt: Date = Date(),
    val createdByAdmin: Boolean = false,
    val updatedAt: Date = Date(),
    val isVerified: Boolean = false,
    val isActive: Boolean = true
)

data class EmergencyContactDto(
    val name: String = "",
    val relationship: String = "",
    val phoneNumber: String = ""
)

data class HouseholdMemberDto(
    val name: String = "",
    val relationship: String = "",
    val age: Int = 0,
    val specialNeeds: String = ""
) 
