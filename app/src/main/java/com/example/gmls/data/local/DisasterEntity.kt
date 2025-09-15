package com.example.gmls.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "disasters")
data class DisasterEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val location: String,
    val type: String,
    val timestamp: Long,
    val affectedCount: Int,
    val images: List<String>,
    val status: String,
    val latitude: Double,
    val longitude: Double,
    val reportedBy: String,
    val updatedAt: Long
) 
