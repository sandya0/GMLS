package com.example.gmls.domain.model

/**
 * Application-wide constants for roles, validation, and configuration
 */
object UserRoles {
    const val ADMIN = "admin"
    const val USER = "user"
}

object UserStatus {
    const val ACTIVE = "active"
    const val INACTIVE = "inactive"
    const val VERIFIED = "verified"
    const val UNVERIFIED = "unverified"
}

object ValidationConstants {
    // Email validation
    const val EMAIL_MIN_LENGTH = 5
    const val EMAIL_MAX_LENGTH = 254
    val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    
    // Password validation
    const val PASSWORD_MIN_LENGTH = 8
    const val PASSWORD_MAX_LENGTH = 128
    val PASSWORD_REGEX = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{8,}$")
    
    // Name validation
    const val NAME_MIN_LENGTH = 2
    const val NAME_MAX_LENGTH = 100
    val NAME_REGEX = Regex("^[a-zA-Z\\s'-]{2,100}$")
    
    // Phone validation
    const val PHONE_MIN_LENGTH = 10
    const val PHONE_MAX_LENGTH = 15
    val PHONE_REGEX = Regex("^[+]?[1-9]\\d{1,14}$")
    
    // Address validation
    const val ADDRESS_MIN_LENGTH = 10
    const val ADDRESS_MAX_LENGTH = 500
    
    // Search query validation
    const val SEARCH_QUERY_MAX_LENGTH = 100
    
    // User ID validation
    const val USER_ID_MIN_LENGTH = 10
    const val USER_ID_MAX_LENGTH = 50
    
    // Admin audit log
    const val AUDIT_LOG_MAX_ENTRIES = 1000
    const val AUDIT_LOG_RETENTION_DAYS = 90
}

object AdminPermissions {
    const val CREATE_USER = "create_user"
    const val UPDATE_USER = "update_user"
    const val DELETE_USER = "delete_user"
    const val VERIFY_USER = "verify_user"
    const val VIEW_ALL_USERS = "view_all_users"
    const val CREATE_ADMIN = "create_admin"
    const val VIEW_AUDIT_LOGS = "view_audit_logs"
    const val MANAGE_DISASTERS = "manage_disasters"
}

object FirestoreCollections {
    const val USERS = "users"
    const val DISASTERS = "disasters"
    const val ADMIN_AUDIT_LOGS = "admin_audit_logs"
    const val USER_SESSIONS = "user_sessions"
} 
