package com.example.gmls.domain.validation

import com.example.gmls.domain.model.ErrorMessages
import com.example.gmls.domain.model.ValidationConstants

/**
 * Validation result sealed class
 */
sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val message: String) : ValidationResult()
}

/**
 * Utility class for input validation across the application
 */
object ValidationUtils {
    
    /**
     * Validate email address
     */
    fun validateEmail(email: String?): ValidationResult {
        return when {
            email.isNullOrBlank() -> ValidationResult.Invalid(ErrorMessages.EMAIL_REQUIRED)
            email.length < ValidationConstants.EMAIL_MIN_LENGTH -> ValidationResult.Invalid(ErrorMessages.EMAIL_TOO_SHORT)
            email.length > ValidationConstants.EMAIL_MAX_LENGTH -> ValidationResult.Invalid(ErrorMessages.EMAIL_TOO_LONG)
            !ValidationConstants.EMAIL_REGEX.matches(email) -> ValidationResult.Invalid(ErrorMessages.EMAIL_INVALID)
            else -> ValidationResult.Valid
        }
    }
    
    /**
     * Validate password strength
     */
    fun validatePassword(password: String?): ValidationResult {
        return when {
            password.isNullOrBlank() -> ValidationResult.Invalid(ErrorMessages.PASSWORD_REQUIRED)
            password.length < ValidationConstants.PASSWORD_MIN_LENGTH -> ValidationResult.Invalid(ErrorMessages.PASSWORD_TOO_SHORT)
            password.length > ValidationConstants.PASSWORD_MAX_LENGTH -> ValidationResult.Invalid(ErrorMessages.PASSWORD_TOO_LONG)
            !ValidationConstants.PASSWORD_REGEX.matches(password) -> ValidationResult.Invalid(ErrorMessages.PASSWORD_WEAK)
            else -> ValidationResult.Valid
        }
    }
    
    /**
     * Validate full name
     */
    fun validateFullName(name: String?): ValidationResult {
        return when {
            name.isNullOrBlank() -> ValidationResult.Invalid(ErrorMessages.NAME_REQUIRED)
            name.length < ValidationConstants.NAME_MIN_LENGTH -> ValidationResult.Invalid(ErrorMessages.NAME_TOO_SHORT)
            name.length > ValidationConstants.NAME_MAX_LENGTH -> ValidationResult.Invalid(ErrorMessages.NAME_TOO_LONG)
            !ValidationConstants.NAME_REGEX.matches(name) -> ValidationResult.Invalid(ErrorMessages.NAME_INVALID)
            else -> ValidationResult.Valid
        }
    }
    
    /**
     * Validate phone number
     */
    fun validatePhoneNumber(phone: String?): ValidationResult {
        if (phone.isNullOrBlank()) {
            return ValidationResult.Valid // Phone is optional in some contexts
        }
        
        val cleanPhone = phone.replace(Regex("[\\s()-]"), "")
        return when {
            cleanPhone.length < ValidationConstants.PHONE_MIN_LENGTH -> ValidationResult.Invalid(ErrorMessages.PHONE_TOO_SHORT)
            cleanPhone.length > ValidationConstants.PHONE_MAX_LENGTH -> ValidationResult.Invalid(ErrorMessages.PHONE_TOO_LONG)
            !ValidationConstants.PHONE_REGEX.matches(cleanPhone) -> ValidationResult.Invalid(ErrorMessages.PHONE_INVALID)
            else -> ValidationResult.Valid
        }
    }
    
    /**
     * Validate address
     */
    fun validateAddress(address: String?): ValidationResult {
        return when {
            address.isNullOrBlank() -> ValidationResult.Invalid(ErrorMessages.ADDRESS_REQUIRED)
            address.length < ValidationConstants.ADDRESS_MIN_LENGTH -> ValidationResult.Invalid(ErrorMessages.ADDRESS_TOO_SHORT)
            address.length > ValidationConstants.ADDRESS_MAX_LENGTH -> ValidationResult.Invalid(ErrorMessages.ADDRESS_TOO_LONG)
            else -> ValidationResult.Valid
        }
    }
    
    /**
     * Validate user ID format
     */
    fun validateUserId(userId: String?): ValidationResult {
        return when {
            userId.isNullOrBlank() -> ValidationResult.Invalid("ID pengguna diperlukan")
            userId.length < 10 -> ValidationResult.Invalid("Format ID pengguna tidak valid")
            !userId.matches(Regex("^[a-zA-Z0-9_-]+$")) -> ValidationResult.Invalid("ID pengguna mengandung karakter yang tidak valid")
            else -> ValidationResult.Valid
        }
    }
    
    /**
     * Validate admin creation data
     */
    fun validateAdminCreationData(
        email: String?,
        password: String?,
        fullName: String?
    ): List<String> {
        val errors = mutableListOf<String>()
        
        when (val emailResult = validateEmail(email)) {
            is ValidationResult.Invalid -> errors.add(emailResult.message)
            ValidationResult.Valid -> {}
        }
        
        when (val passwordResult = validatePassword(password)) {
            is ValidationResult.Invalid -> errors.add(passwordResult.message)
            ValidationResult.Valid -> {}
        }
        
        when (val nameResult = validateFullName(fullName)) {
            is ValidationResult.Invalid -> errors.add(nameResult.message)
            ValidationResult.Valid -> {}
        }
        
        return errors
    }
    
    /**
     * Validate user creation data
     */
    fun validateUserCreationData(
        email: String?,
        password: String?,
        fullName: String?,
        phoneNumber: String?,
        address: String?
    ): List<String> {
        val errors = mutableListOf<String>()
        
        when (val emailResult = validateEmail(email)) {
            is ValidationResult.Invalid -> errors.add(emailResult.message)
            ValidationResult.Valid -> {}
        }
        
        when (val passwordResult = validatePassword(password)) {
            is ValidationResult.Invalid -> errors.add(passwordResult.message)
            ValidationResult.Valid -> {}
        }
        
        when (val nameResult = validateFullName(fullName)) {
            is ValidationResult.Invalid -> errors.add(nameResult.message)
            ValidationResult.Valid -> {}
        }
        
        when (val phoneResult = validatePhoneNumber(phoneNumber)) {
            is ValidationResult.Invalid -> errors.add(phoneResult.message)
            ValidationResult.Valid -> {}
        }
        
        if (!address.isNullOrBlank()) {
            when (val addressResult = validateAddress(address)) {
                is ValidationResult.Invalid -> errors.add(addressResult.message)
                ValidationResult.Valid -> {}
            }
        }
        
        return errors
    }
    
    /**
     * Validate user update data
     */
    fun validateUserUpdateData(
        fullName: String?,
        phoneNumber: String?,
        address: String?
    ): List<String> {
        val errors = mutableListOf<String>()
        
        if (!fullName.isNullOrBlank()) {
            when (val nameResult = validateFullName(fullName)) {
                is ValidationResult.Invalid -> errors.add(nameResult.message)
                ValidationResult.Valid -> {}
            }
        }
        
        if (!phoneNumber.isNullOrBlank()) {
            when (val phoneResult = validatePhoneNumber(phoneNumber)) {
                is ValidationResult.Invalid -> errors.add(phoneResult.message)
                ValidationResult.Valid -> {}
            }
        }
        
        if (!address.isNullOrBlank()) {
            when (val addressResult = validateAddress(address)) {
                is ValidationResult.Invalid -> errors.add(addressResult.message)
                ValidationResult.Valid -> {}
            }
        }
        
        return errors
    }
    
    /**
     * Sanitize input string to prevent injection attacks
     */
    fun sanitizeInput(input: String?): String {
        if (input.isNullOrBlank()) return ""
        
        return input
            .trim()
            .replace(Regex("<[^>]*>"), "") // Remove HTML tags
            .replace(Regex("[<>\"'&]"), "") // Remove potentially dangerous characters
            .take(1000) // Limit length to prevent DoS
    }
    
    /**
     * Validate search query
     */
    fun validateSearchQuery(query: String?): ValidationResult {
        return when {
            query.isNullOrBlank() -> ValidationResult.Valid
            query.length > 100 -> ValidationResult.Invalid("Kueri pencarian terlalu panjang")
            query.contains(Regex("[<>\"'&]")) -> ValidationResult.Invalid("Kueri pencarian mengandung karakter yang tidak valid")
            else -> ValidationResult.Valid
        }
    }
    
    /**
     * Check if string is a valid role
     */
    fun isValidRole(role: String?): Boolean {
        return role in listOf("admin", "user")
    }
    
    /**
     * Check if string is a valid status
     */
    fun isValidUserStatus(status: String?): Boolean {
        return status in listOf("active", "inactive")
    }
} 
