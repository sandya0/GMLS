package com.example.gmls.domain.validation

import com.example.gmls.domain.model.ErrorMessages
import org.junit.Test
import org.junit.Assert.*

class ValidationUtilsTest {

    @Test
    fun `validateEmail should return Valid for valid email`() {
        val result = ValidationUtils.validateEmail("test@example.com")
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `validateEmail should return Invalid for null email`() {
        val result = ValidationUtils.validateEmail(null)
        assertTrue(result is ValidationResult.Invalid)
        assertEquals(ErrorMessages.EMAIL_REQUIRED, (result as ValidationResult.Invalid).message)
    }

    @Test
    fun `validateEmail should return Invalid for empty email`() {
        val result = ValidationUtils.validateEmail("")
        assertTrue(result is ValidationResult.Invalid)
        assertEquals(ErrorMessages.EMAIL_REQUIRED, (result as ValidationResult.Invalid).message)
    }

    @Test
    fun `validateEmail should return Invalid for email without at symbol`() {
        val result = ValidationUtils.validateEmail("testexample.com")
        assertTrue(result is ValidationResult.Invalid)
        assertEquals(ErrorMessages.EMAIL_INVALID, (result as ValidationResult.Invalid).message)
    }

    @Test
    fun `validateEmail should return Invalid for email without domain`() {
        val result = ValidationUtils.validateEmail("test@")
        assertTrue(result is ValidationResult.Invalid)
        assertEquals(ErrorMessages.EMAIL_INVALID, (result as ValidationResult.Invalid).message)
    }

    @Test
    fun `validatePassword should return Valid for strong password`() {
        val result = ValidationUtils.validatePassword("StrongPass123!")
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `validatePassword should return Invalid for null password`() {
        val result = ValidationUtils.validatePassword(null)
        assertTrue(result is ValidationResult.Invalid)
        assertEquals(ErrorMessages.PASSWORD_REQUIRED, (result as ValidationResult.Invalid).message)
    }

    @Test
    fun `validatePassword should return Invalid for short password`() {
        val result = ValidationUtils.validatePassword("123")
        assertTrue(result is ValidationResult.Invalid)
        assertEquals(ErrorMessages.PASSWORD_TOO_SHORT, (result as ValidationResult.Invalid).message)
    }

    @Test
    fun `validatePassword should return Invalid for weak password`() {
        val result = ValidationUtils.validatePassword("password123")
        assertTrue(result is ValidationResult.Invalid)
        assertEquals(ErrorMessages.PASSWORD_WEAK, (result as ValidationResult.Invalid).message)
    }

    @Test
    fun `validateFullName should return Valid for valid name`() {
        val result = ValidationUtils.validateFullName("John Doe")
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `validateFullName should return Invalid for null name`() {
        val result = ValidationUtils.validateFullName(null)
        assertTrue(result is ValidationResult.Invalid)
        assertEquals(ErrorMessages.NAME_REQUIRED, (result as ValidationResult.Invalid).message)
    }

    @Test
    fun `validateFullName should return Invalid for short name`() {
        val result = ValidationUtils.validateFullName("A")
        assertTrue(result is ValidationResult.Invalid)
        assertEquals(ErrorMessages.NAME_TOO_SHORT, (result as ValidationResult.Invalid).message)
    }

    @Test
    fun `validateFullName should return Invalid for name with numbers`() {
        val result = ValidationUtils.validateFullName("John123")
        assertTrue(result is ValidationResult.Invalid)
        assertEquals(ErrorMessages.NAME_INVALID, (result as ValidationResult.Invalid).message)
    }

    @Test
    fun `validatePhoneNumber should return Valid for valid phone`() {
        val result = ValidationUtils.validatePhoneNumber("+1234567890")
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `validatePhoneNumber should return Valid for null phone`() {
        // Phone is optional in some contexts
        val result = ValidationUtils.validatePhoneNumber(null)
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `validatePhoneNumber should return Invalid for short phone`() {
        val result = ValidationUtils.validatePhoneNumber("123")
        assertTrue(result is ValidationResult.Invalid)
        assertEquals(ErrorMessages.PHONE_TOO_SHORT, (result as ValidationResult.Invalid).message)
    }

    @Test
    fun `validatePhoneNumber should return Invalid for phone with letters`() {
        val result = ValidationUtils.validatePhoneNumber("123abc4567")
        assertTrue(result is ValidationResult.Invalid)
        assertEquals(ErrorMessages.PHONE_INVALID, (result as ValidationResult.Invalid).message)
    }

    @Test
    fun `validateUserId should return Valid for valid user ID`() {
        val result = ValidationUtils.validateUserId("user123456789")
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `validateUserId should return Invalid for null user ID`() {
        val result = ValidationUtils.validateUserId(null)
        assertTrue(result is ValidationResult.Invalid)
        assertEquals("User ID is required", (result as ValidationResult.Invalid).message)
    }

    @Test
    fun `validateUserId should return Invalid for short user ID`() {
        val result = ValidationUtils.validateUserId("123")
        assertTrue(result is ValidationResult.Invalid)
        assertEquals("Invalid user ID format", (result as ValidationResult.Invalid).message)
    }

    @Test
    fun `validateAdminCreationData should return empty list for valid data`() {
        val errors = ValidationUtils.validateAdminCreationData(
            "admin@example.com",
            "AdminPass123!",
            "Admin User"
        )
        assertTrue(errors.isEmpty())
    }

    @Test
    fun `validateAdminCreationData should return errors for invalid data`() {
        val errors = ValidationUtils.validateAdminCreationData(
            "invalid-email",
            "weak",
            "A"
        )
        assertEquals(3, errors.size)
        assertTrue(errors.contains(ErrorMessages.EMAIL_INVALID))
        assertTrue(errors.contains(ErrorMessages.PASSWORD_TOO_SHORT))
        assertTrue(errors.contains(ErrorMessages.NAME_TOO_SHORT))
    }

    @Test
    fun `validateUserCreationData should return empty list for valid data`() {
        val errors = ValidationUtils.validateUserCreationData(
            "user@example.com",
            "UserPass123!",
            "John Doe",
            "+1234567890",
            "123 Main St, City, State"
        )
        assertTrue(errors.isEmpty())
    }

    @Test
    fun `validateUserCreationData should return errors for invalid data`() {
        val errors = ValidationUtils.validateUserCreationData(
            "",
            "",
            "",
            "123",
            "short"
        )
        assertEquals(5, errors.size)
    }

    @Test
    fun `sanitizeInput should remove HTML tags`() {
        val input = "<script>alert('xss')</script>Hello World"
        val result = ValidationUtils.sanitizeInput(input)
        assertEquals("Hello World", result)
    }

    @Test
    fun `sanitizeInput should remove dangerous characters`() {
        val input = "Hello<>&\"'World"
        val result = ValidationUtils.sanitizeInput(input)
        assertEquals("HelloWorld", result)
    }

    @Test
    fun `sanitizeInput should trim whitespace`() {
        val input = "  Hello World  "
        val result = ValidationUtils.sanitizeInput(input)
        assertEquals("Hello World", result)
    }

    @Test
    fun `sanitizeInput should limit length`() {
        val input = "a".repeat(1500)
        val result = ValidationUtils.sanitizeInput(input)
        assertEquals(1000, result.length)
    }

    @Test
    fun `validateSearchQuery should return Valid for valid query`() {
        val result = ValidationUtils.validateSearchQuery("john doe")
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `validateSearchQuery should return Invalid for long query`() {
        val result = ValidationUtils.validateSearchQuery("a".repeat(150))
        assertTrue(result is ValidationResult.Invalid)
        assertEquals("Search query too long", (result as ValidationResult.Invalid).message)
    }

    @Test
    fun `validateSearchQuery should return Invalid for query with dangerous characters`() {
        val result = ValidationUtils.validateSearchQuery("john<script>")
        assertTrue(result is ValidationResult.Invalid)
        assertEquals("Search query contains invalid characters", (result as ValidationResult.Invalid).message)
    }

    @Test
    fun `isValidRole should return true for admin role`() {
        assertTrue(ValidationUtils.isValidRole("admin"))
    }

    @Test
    fun `isValidRole should return true for user role`() {
        assertTrue(ValidationUtils.isValidRole("user"))
    }

    @Test
    fun `isValidRole should return false for invalid role`() {
        assertFalse(ValidationUtils.isValidRole("superuser"))
        assertFalse(ValidationUtils.isValidRole(null))
    }

    @Test
    fun `isValidUserStatus should return true for active status`() {
        assertTrue(ValidationUtils.isValidUserStatus("active"))
    }

    @Test
    fun `isValidUserStatus should return true for inactive status`() {
        assertTrue(ValidationUtils.isValidUserStatus("inactive"))
    }

    @Test
    fun `isValidUserStatus should return false for invalid status`() {
        assertFalse(ValidationUtils.isValidUserStatus("pending"))
        assertFalse(ValidationUtils.isValidUserStatus(null))
    }
} 