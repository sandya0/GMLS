package com.example.gmls.domain.model

data class DisasterReport(
    val type: DisasterType,
    val description: String,
    val severity: String,
    val location: String,
    val reporterId: String,
    val timestamp: Long = System.currentTimeMillis()
) 
