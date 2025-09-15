package com.example.gmls.domain.model

data class UserStats(
    val totalUsers: Int = 0,
    val activeUsers: Int = 0,
    val verifiedUsers: Int = 0,
    val adminUsers: Int = 0
)

data class AdminStats(
    val totalDisasters: Int = 0,
    val activeDisasters: Int = 0,
    val resolvedDisasters: Int = 0,
    val pendingReports: Int = 0
)

data class AdminDashboardData(
    val userStats: UserStats = UserStats(),
    val disasterStats: AdminStats = AdminStats(),
    val recentUsers: List<User> = emptyList(),
    val recentDisasters: List<Disaster> = emptyList()
)
