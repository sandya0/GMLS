package com.example.gmls.data.mapper

import com.example.gmls.domain.model.User
import com.example.gmls.domain.model.EmergencyContact
import com.example.gmls.domain.model.HouseholdMember
import javax.inject.Inject
import javax.inject.Singleton
import java.util.Date

@Singleton
class UserFirebaseMapper @Inject constructor() {
    
    /**
     * Maps Firebase document data to User domain model
     */
    fun mapFromFirebase(id: String, data: Map<String, Any>): User {
        val emergencyContact = EmergencyContact(
            name = data["emergencyContactName"] as? String ?: "",
            relationship = data["emergencyContactRelationship"] as? String ?: "",
            phoneNumber = data["emergencyContactPhone"] as? String ?: ""
        )

        return User(
            id = id,
            email = data["email"] as? String ?: "",
            fullName = data["fullName"] as? String ?: "",
            phoneNumber = data["phoneNumber"] as? String ?: "",
            dateOfBirth = (data["dateOfBirth"] as? com.google.firebase.Timestamp)?.toDate() ?: data["dateOfBirth"] as? Date,
            gender = data["gender"] as? String ?: "",
            nationalId = data["nationalId"] as? String ?: "",
            familyCardNumber = data["familyCardNumber"] as? String ?: "",
            placeOfBirth = data["placeOfBirth"] as? String ?: "",
            religion = data["religion"] as? String ?: "",
            maritalStatus = data["maritalStatus"] as? String ?: "",
            familyRelationshipStatus = data["familyRelationshipStatus"] as? String ?: "",
            lastEducation = data["lastEducation"] as? String ?: "",
            createdByAdmin = data["createdByAdmin"] as? Boolean ?: false,
            occupation = data["occupation"] as? String ?: "",
            economicStatus = data["economicStatus"] as? String ?: "",
            latitude = (data["latitude"] as? Number)?.toDouble(),
            longitude = (data["longitude"] as? Number)?.toDouble(),
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
            createdAt = (data["createdAt"] as? com.google.firebase.Timestamp)?.toDate() ?: data["createdAt"] as? Date ?: Date(),
            updatedAt = (data["updatedAt"] as? com.google.firebase.Timestamp)?.toDate() ?: data["updatedAt"] as? Date ?: Date(),
            profilePictureUrl = data["profilePictureUrl"] as? String,
            isVerified = data["isVerified"] as? Boolean ?: false,
            isActive = data["isActive"] as? Boolean ?: true
        )
    }
    
    fun mapToUser(id: String, data: Map<String, Any>): User {
        return mapFromFirebase(id, data)
    }

    fun mapToFirestore(user: User): Map<String, Any?> {
        return mapOf(
            "email" to user.email,
            "fullName" to user.fullName,
            "phoneNumber" to user.phoneNumber,
            "dateOfBirth" to user.dateOfBirth,
            "gender" to user.gender,
            "nationalId" to user.nationalId,
            "familyCardNumber" to user.familyCardNumber,
            "placeOfBirth" to user.placeOfBirth,
            "religion" to user.religion,
            "maritalStatus" to user.maritalStatus,
            "familyRelationshipStatus" to user.familyRelationshipStatus,
            "lastEducation" to user.lastEducation,
            "createdByAdmin" to user.createdByAdmin,
            "occupation" to user.occupation,
            "economicStatus" to user.economicStatus,
            "latitude" to user.latitude,
            "longitude" to user.longitude,
            "address" to user.address,
            "bloodType" to user.bloodType,
            "medicalConditions" to user.medicalConditions,
            "disabilities" to user.disabilities,
            "emergencyContactName" to user.emergencyContact.name,
            "emergencyContactRelationship" to user.emergencyContact.relationship,
            "emergencyContactPhone" to user.emergencyContact.phoneNumber,
            "householdMembers" to user.householdMembers.map { member ->
                mapOf(
                    "name" to member.name,
                    "relationship" to member.relationship,
                    "age" to member.age,
                    "specialNeeds" to member.specialNeeds
                )
            },
            "locationPermissionGranted" to user.locationPermissionGranted,
            "role" to user.role,
            "createdAt" to user.createdAt,
            "updatedAt" to user.updatedAt,
            "profilePictureUrl" to user.profilePictureUrl,
            "isVerified" to user.isVerified,
            "isActive" to user.isActive
        )
    }
} 
