package com.example.gmls.data.mapper

import com.example.gmls.data.model.EmergencyContactDto
import com.example.gmls.data.model.HouseholdMemberDto
import com.example.gmls.data.model.UserDto
import com.example.gmls.domain.model.EmergencyContact
import com.example.gmls.domain.model.HouseholdMember
import com.example.gmls.domain.model.User
import com.example.gmls.data.local.UserEntity
import com.example.gmls.data.local.HouseholdMemberEntity
import com.example.gmls.data.local.UserWithHouseholdMembers
import java.util.Date
import java.util.UUID

fun UserDto.toUser(): User {
    return User(
        id = id,
        email = email,
        fullName = fullName,
        phoneNumber = phoneNumber,
        dateOfBirth = dateOfBirth,
        gender = gender,
        nationalId = nationalId,
        address = address,
        bloodType = bloodType,
        medicalConditions = medicalConditions,
        disabilities = disabilities,
        emergencyContact = emergencyContact.toEmergencyContact(),
        householdMembers = householdMembers.map { it.toHouseholdMember() },
        locationPermissionGranted = locationPermissionGranted,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isVerified = isVerified
    )
}

fun User.toUserDto(): UserDto {
    return UserDto(
        id = id,
        email = email,
        fullName = fullName,
        phoneNumber = phoneNumber,
        dateOfBirth = dateOfBirth,
        gender = gender,
        nationalId = nationalId,
        address = address,
        bloodType = bloodType,
        medicalConditions = medicalConditions,
        disabilities = disabilities,
        emergencyContact = emergencyContact.toEmergencyContactDto(),
        householdMembers = householdMembers.map { it.toHouseholdMemberDto() },
        locationPermissionGranted = locationPermissionGranted,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isVerified = isVerified
    )
}

private fun EmergencyContactDto.toEmergencyContact(): EmergencyContact {
    return EmergencyContact(
        name = name,
        relationship = relationship,
        phoneNumber = phoneNumber
    )
}

private fun EmergencyContact.toEmergencyContactDto(): EmergencyContactDto {
    return EmergencyContactDto(
        name = name,
        relationship = relationship,
        phoneNumber = phoneNumber
    )
}

private fun HouseholdMemberDto.toHouseholdMember(): HouseholdMember {
    return HouseholdMember(
        name = name,
        relationship = relationship,
        age = age,
        specialNeeds = specialNeeds
    )
}

private fun HouseholdMember.toHouseholdMemberDto(): HouseholdMemberDto {
    return HouseholdMemberDto(
        name = name,
        relationship = relationship,
        age = age,
        specialNeeds = specialNeeds
    )
}

/**
 * Convert UserWithHouseholdMembers to User domain model
 */
fun UserWithHouseholdMembers.toUser(): User {
    return User(
        id = user.id,
        email = user.email,
        fullName = user.fullName,
        phoneNumber = user.phoneNumber,
        dateOfBirth = user.dateOfBirth?.let { Date(it) },
        gender = user.gender,
        nationalId = user.nationalId,
        address = user.address,
        bloodType = user.bloodType,
        medicalConditions = user.medicalConditions.split(",").filter { it.isNotBlank() },
        disabilities = user.disabilities.split(",").filter { it.isNotBlank() },
        emergencyContact = EmergencyContact(
            name = user.emergencyContactName,
            relationship = user.emergencyContactRelationship,
            phoneNumber = user.emergencyContactPhone
        ),
        householdMembers = householdMembers.map { it.toHouseholdMember() },
        profilePictureUrl = user.profilePictureUrl,
        isVerified = user.isVerified
    )
}

/**
 * Convert UserEntity to User domain model (for backward compatibility)
 */
fun UserEntity.toUser(): User {
    return User(
        id = id,
        email = email,
        fullName = fullName,
        phoneNumber = phoneNumber,
        dateOfBirth = dateOfBirth?.let { Date(it) },
        gender = gender,
        nationalId = nationalId,
        address = address,
        bloodType = bloodType,
        medicalConditions = medicalConditions.split(",").filter { it.isNotBlank() },
        disabilities = disabilities.split(",").filter { it.isNotBlank() },
        emergencyContact = EmergencyContact(
            name = emergencyContactName,
            relationship = emergencyContactRelationship,
            phoneNumber = emergencyContactPhone
        ),
        householdMembers = emptyList(), // Will be loaded separately when needed
        profilePictureUrl = profilePictureUrl,
        isVerified = isVerified
    )
}

fun User.toUserEntity(): UserEntity {
    return UserEntity(
        id = id,
        email = email,
        fullName = fullName,
        phoneNumber = phoneNumber,
        dateOfBirth = dateOfBirth?.time,
        gender = gender,
        nationalId = nationalId,
        address = address,
        bloodType = bloodType,
        medicalConditions = medicalConditions.joinToString(","),
        disabilities = disabilities.joinToString(","),
        emergencyContactName = emergencyContact.name,
        emergencyContactRelationship = emergencyContact.relationship,
        emergencyContactPhone = emergencyContact.phoneNumber,
        householdMembers = householdMembers.size,
        profilePictureUrl = profilePictureUrl,
        isVerified = isVerified
    )
}

/**
 * Convert HouseholdMemberEntity to HouseholdMember domain model
 */
fun HouseholdMemberEntity.toHouseholdMember(): HouseholdMember {
    return HouseholdMember(
        id = id,
        name = name,
        relationship = relationship,
        age = age,
        specialNeeds = specialNeeds,
        gender = gender,
        bloodType = bloodType,
        medicalConditions = medicalConditions.split(",").filter { it.isNotBlank() },
        disabilities = disabilities.split(",").filter { it.isNotBlank() }
    )
}

/**
 * Convert HouseholdMember domain model to HouseholdMemberEntity
 */
fun HouseholdMember.toHouseholdMemberEntity(userId: String): HouseholdMemberEntity {
    return HouseholdMemberEntity(
        id = id.ifEmpty { UUID.randomUUID().toString() },
        userId = userId,
        name = name,
        relationship = relationship,
        age = age,
        specialNeeds = specialNeeds,
        gender = gender,
        bloodType = bloodType,
        medicalConditions = medicalConditions.joinToString(","),
        disabilities = disabilities.joinToString(","),
        updatedAt = System.currentTimeMillis()
    )
}

/**
 * Convert list of HouseholdMembers to HouseholdMemberEntities
 */
fun List<HouseholdMember>.toHouseholdMemberEntities(userId: String): List<HouseholdMemberEntity> {
    return map { it.toHouseholdMemberEntity(userId) }
} 
