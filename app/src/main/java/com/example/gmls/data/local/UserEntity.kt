package com.example.gmls.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Embedded
import androidx.room.Relation
import androidx.room.Index

@Entity(tableName = "user_profile")
data class UserEntity(
    @PrimaryKey val id: String,
    val email: String,
    val fullName: String,
    val phoneNumber: String,
    val dateOfBirth: Long?,
    val gender: String,
    val nationalId: String,
    val address: String,
    val bloodType: String,
    val medicalConditions: String,
    val disabilities: String,
    val emergencyContactName: String,
    val emergencyContactRelationship: String,
    val emergencyContactPhone: String,
    val householdMembers: Int,
    val profilePictureUrl: String?,
    val isVerified: Boolean = false,
    val createdByAdmin: Boolean = false
)

@Entity(
    tableName = "household_members",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class HouseholdMemberEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val relationship: String,
    val age: Int,
    val specialNeeds: String,
    val gender: String = "",
    val bloodType: String = "",
    val medicalConditions: String = "",
    val disabilities: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Data class to hold user with household members
 */
data class UserWithHouseholdMembers(
    @Embedded val user: UserEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "userId"
    )
    val householdMembers: List<HouseholdMemberEntity>
) 
