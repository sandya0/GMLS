package com.example.gmls.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.gmls.ui.components.*
import com.example.gmls.ui.theme.Red
import java.util.*

data class RegistrationData(
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val dateOfBirth: Date? = null,
    val gender: String = "",
    val nationalId: String = "",
    val phoneNumber: String = "",
    val address: String = "",
    val bloodType: String = "",
    val medicalConditions: String = "",
    val disabilities: String = "",
    val emergencyContactName: String = "",
    val emergencyContactRelationship: String = "",
    val emergencyContactPhone: String = "",
    val householdMembers: Int = 1,
    val acceptedTerms: Boolean = false,
    val locationPermissionGranted: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    onRegister: (RegistrationData) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    var currentPage by remember { mutableStateOf(0) }
    var registrationData by remember { mutableStateOf(RegistrationData()) }
    var errors by remember { mutableStateOf(mapOf<String, String>()) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Top App Bar with back button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Go back"
                )
            }

            Text(
                text = "Create Account",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        // Progress indicator
        LinearProgressIndicator(
            progress = { (currentPage + 1) / 4f }, // 4 pages total
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            color = Red
        )

        // Form content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (currentPage) {
                0 -> BasicInfoPage(
                    registrationData = registrationData,
                    onDataChange = { registrationData = it },
                    errors = errors
                )
                1 -> PersonalDetailsPage(
                    registrationData = registrationData,
                    onDataChange = { registrationData = it },
                    errors = errors
                )
                2 -> MedicalInfoPage(
                    registrationData = registrationData,
                    onDataChange = { registrationData = it },
                    errors = errors
                )
                3 -> EmergencyContactPage(
                    registrationData = registrationData,
                    onDataChange = { registrationData = it },
                    errors = errors
                )
            }
        }

        // Navigation buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (currentPage > 0) {
                SecondaryButton(
                    text = "Previous",
                    onClick = { currentPage-- },
                    modifier = Modifier.weight(1f)
                )
            }

            if (currentPage < 3) {
                PrimaryButton(
                    text = "Next",
                    onClick = {
                        if (validateCurrentPage(currentPage, registrationData)) {
                            currentPage++
                            errors = emptyMap()
                        } else {
                            // Update errors
                            errors = getErrorsForPage(currentPage, registrationData)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            } else {
                PrimaryButton(
                    text = "Create Account",
                    onClick = {
                        if (validateCurrentPage(currentPage, registrationData) && registrationData.acceptedTerms) {
                            onRegister(registrationData)
                        } else {
                            errors = getErrorsForPage(currentPage, registrationData)
                        }
                    },
                    isLoading = isLoading,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun BasicInfoPage(
    registrationData: RegistrationData,
    onDataChange: (RegistrationData) -> Unit,
    errors: Map<String, String>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Basic Information",
            style = MaterialTheme.typography.headlineSmall
        )

        // Full Name
        DisasterTextField(
            value = registrationData.fullName,
            onValueChange = { onDataChange(registrationData.copy(fullName = it)) },
            label = "Full Name",
            leadingIcon = Icons.Filled.Person,
            isError = errors.containsKey("fullName"),
            errorMessage = errors["fullName"]
        )

        // Email
        DisasterTextField(
            value = registrationData.email,
            onValueChange = { onDataChange(registrationData.copy(email = it)) },
            label = "Email",
            leadingIcon = Icons.Filled.Email,
            keyboardType = KeyboardType.Email,
            isError = errors.containsKey("email"),
            errorMessage = errors["email"]
        )

        // Password
        DisasterTextField(
            value = registrationData.password,
            onValueChange = { onDataChange(registrationData.copy(password = it)) },
            label = "Password",
            leadingIcon = Icons.Filled.Lock,
            isPassword = true,
            isError = errors.containsKey("password"),
            errorMessage = errors["password"]
        )

        // Confirm Password
        DisasterTextField(
            value = registrationData.confirmPassword,
            onValueChange = { onDataChange(registrationData.copy(confirmPassword = it)) },
            label = "Confirm Password",
            leadingIcon = Icons.Filled.Lock,
            isPassword = true,
            imeAction = ImeAction.Done,
            isError = errors.containsKey("confirmPassword"),
            errorMessage = errors["confirmPassword"]
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalDetailsPage(
    registrationData: RegistrationData,
    onDataChange: (RegistrationData) -> Unit,
    errors: Map<String, String>
) {
    val genderOptions = listOf("Male", "Female", "Non-binary", "Prefer not to say")

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Personal Details",
            style = MaterialTheme.typography.headlineSmall
        )

        // Date of Birth
        // Note: In a real app, you would implement a proper date picker
        DisasterTextField(
            value = registrationData.dateOfBirth?.toString() ?: "",
            onValueChange = { /* Implement proper date parsing */ },
            label = "Date of Birth (MM/DD/YYYY)",
            leadingIcon = Icons.Filled.CalendarMonth,
            isError = errors.containsKey("dateOfBirth"),
            errorMessage = errors["dateOfBirth"]
        )

        // Gender
        Column {
            Text(
                text = "Gender",
                style = MaterialTheme.typography.bodyMedium
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                genderOptions.forEach { gender ->
                    FilterChip(
                        selected = registrationData.gender == gender,
                        onClick = { onDataChange(registrationData.copy(gender = gender)) },
                        label = { Text(gender) }
                    )
                }
            }

            if (errors.containsKey("gender")) {
                Text(
                    text = errors["gender"] ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        // National ID
        DisasterTextField(
            value = registrationData.nationalId,
            onValueChange = { onDataChange(registrationData.copy(nationalId = it)) },
            label = "National Identification Number (NIK)",
            leadingIcon = Icons.Filled.Badge,
            isError = errors.containsKey("nationalId"),
            errorMessage = errors["nationalId"]
        )

        // Phone Number
        DisasterTextField(
            value = registrationData.phoneNumber,
            onValueChange = { onDataChange(registrationData.copy(phoneNumber = it)) },
            label = "Phone Number",
            leadingIcon = Icons.Filled.Phone,
            keyboardType = KeyboardType.Phone,
            isError = errors.containsKey("phoneNumber"),
            errorMessage = errors["phoneNumber"]
        )

        // Address
        DisasterTextField(
            value = registrationData.address,
            onValueChange = { onDataChange(registrationData.copy(address = it)) },
            label = "Home Address",
            leadingIcon = Icons.Filled.Home,
            isError = errors.containsKey("address"),
            errorMessage = errors["address"]
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalInfoPage(
    registrationData: RegistrationData,
    onDataChange: (RegistrationData) -> Unit,
    errors: Map<String, String>
) {
    val bloodTypes = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Medical Information",
            style = MaterialTheme.typography.headlineSmall
        )

        // Blood Type
        Column {
            Text(
                text = "Blood Type",
                style = MaterialTheme.typography.bodyMedium
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                bloodTypes.take(4).forEach { bloodType ->
                    FilterChip(
                        selected = registrationData.bloodType == bloodType,
                        onClick = { onDataChange(registrationData.copy(bloodType = bloodType)) },
                        label = { Text(bloodType) }
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                bloodTypes.drop(4).forEach { bloodType ->
                    FilterChip(
                        selected = registrationData.bloodType == bloodType,
                        onClick = { onDataChange(registrationData.copy(bloodType = bloodType)) },
                        label = { Text(bloodType) }
                    )
                }
            }

            if (errors.containsKey("bloodType")) {
                Text(
                    text = errors["bloodType"] ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        // Medical Conditions (Optional)
        DisasterTextField(
            value = registrationData.medicalConditions,
            onValueChange = { onDataChange(registrationData.copy(medicalConditions = it)) },
            label = "Medical Conditions (Optional)",
            leadingIcon = Icons.Outlined.MedicalServices
        )

        // Disabilities (Optional)
        DisasterTextField(
            value = registrationData.disabilities,
            onValueChange = { onDataChange(registrationData.copy(disabilities = it)) },
            label = "Disabilities (Optional)",
            leadingIcon = Icons.Outlined.Accessible
        )

        // Household Members
        Column {
            Text(
                text = "Number of Household Members",
                style = MaterialTheme.typography.bodyMedium
            )

            Slider(
                value = registrationData.householdMembers.toFloat(),
                onValueChange = { onDataChange(registrationData.copy(householdMembers = it.toInt())) },
                valueRange = 1f..10f,
                steps = 9,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Text(
                text = registrationData.householdMembers.toString(),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun EmergencyContactPage(
    registrationData: RegistrationData,
    onDataChange: (RegistrationData) -> Unit,
    errors: Map<String, String>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Emergency Contact",
            style = MaterialTheme.typography.headlineSmall
        )

        // Emergency Contact Name
        DisasterTextField(
            value = registrationData.emergencyContactName,
            onValueChange = { onDataChange(registrationData.copy(emergencyContactName = it)) },
            label = "Emergency Contact Name",
            leadingIcon = Icons.Filled.ContactPhone,
            isError = errors.containsKey("emergencyContactName"),
            errorMessage = errors["emergencyContactName"]
        )

        // Emergency Contact Relationship
        DisasterTextField(
            value = registrationData.emergencyContactRelationship,
            onValueChange = { onDataChange(registrationData.copy(emergencyContactRelationship = it)) },
            label = "Relationship",
            leadingIcon = Icons.Filled.Group,
            isError = errors.containsKey("emergencyContactRelationship"),
            errorMessage = errors["emergencyContactRelationship"]
        )

        // Emergency Contact Phone
        DisasterTextField(
            value = registrationData.emergencyContactPhone,
            onValueChange = { onDataChange(registrationData.copy(emergencyContactPhone = it)) },
            label = "Emergency Contact Phone",
            leadingIcon = Icons.Filled.Call,
            keyboardType = KeyboardType.Phone,
            isError = errors.containsKey("emergencyContactPhone"),
            errorMessage = errors["emergencyContactPhone"]
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Location Permission
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = registrationData.locationPermissionGranted,
                onCheckedChange = { onDataChange(registrationData.copy(locationPermissionGranted = it)) }
            )

            Text(
                text = "Allow access to my location for disaster reporting",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // Terms and Conditions
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = registrationData.acceptedTerms,
                onCheckedChange = { onDataChange(registrationData.copy(acceptedTerms = it)) }
            )

            Text(
                text = "I agree to the Terms and Conditions",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (errors.containsKey("terms")) {
            Text(
                text = errors["terms"] ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 48.dp)
            )
        }
    }
}

private fun validateCurrentPage(page: Int, data: RegistrationData): Boolean {
    return when (page) {
        0 -> validateBasicInfo(data)
        1 -> validatePersonalDetails(data)
        2 -> validateMedicalInfo(data)
        3 -> validateEmergencyContact(data)
        else -> false
    }
}

private fun getErrorsForPage(page: Int, data: RegistrationData): Map<String, String> {
    val errors = mutableMapOf<String, String>()

    when (page) {
        0 -> {
            if (data.fullName.isBlank()) {
                errors["fullName"] = "Full name is required"
            }

            if (data.email.isBlank()) {
                errors["email"] = "Email is required"
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(data.email).matches()) {
                errors["email"] = "Please enter a valid email"
            }

            if (data.password.isBlank()) {
                errors["password"] = "Password is required"
            } else if (data.password.length < 8) {
                errors["password"] = "Password must be at least 8 characters"
            }

            if (data.confirmPassword.isBlank()) {
                errors["confirmPassword"] = "Please confirm your password"
            } else if (data.password != data.confirmPassword) {
                errors["confirmPassword"] = "Passwords do not match"
            }
        }
        1 -> {
            if (data.dateOfBirth == null) {
                errors["dateOfBirth"] = "Date of birth is required"
            }

            if (data.gender.isBlank()) {
                errors["gender"] = "Please select a gender"
            }

            if (data.nationalId.isBlank()) {
                errors["nationalId"] = "National ID is required"
            }

            if (data.phoneNumber.isBlank()) {
                errors["phoneNumber"] = "Phone number is required"
            }

            if (data.address.isBlank()) {
                errors["address"] = "Address is required"
            }
        }
        2 -> {
            if (data.bloodType.isBlank()) {
                errors["bloodType"] = "Please select a blood type"
            }
        }
        3 -> {
            if (data.emergencyContactName.isBlank()) {
                errors["emergencyContactName"] = "Emergency contact name is required"
            }

            if (data.emergencyContactRelationship.isBlank()) {
                errors["emergencyContactRelationship"] = "Relationship is required"
            }

            if (data.emergencyContactPhone.isBlank()) {
                errors["emergencyContactPhone"] = "Emergency contact phone is required"
            }

            if (!data.acceptedTerms) {
                errors["terms"] = "You must agree to the terms and conditions"
            }
        }
    }

    return errors
}

private fun validateBasicInfo(data: RegistrationData): Boolean {
    return data.fullName.isNotBlank() &&
            data.email.isNotBlank() &&
            android.util.Patterns.EMAIL_ADDRESS.matcher(data.email).matches() &&
            data.password.isNotBlank() &&
            data.password.length >= 8 &&
            data.confirmPassword.isNotBlank() &&
            data.password == data.confirmPassword
}

private fun validatePersonalDetails(data: RegistrationData): Boolean {
    return data.dateOfBirth != null &&
            data.gender.isNotBlank() &&
            data.nationalId.isNotBlank() &&
            data.phoneNumber.isNotBlank() &&
            data.address.isNotBlank()
}

private fun validateMedicalInfo(data: RegistrationData): Boolean {
    return data.bloodType.isNotBlank()
}

private fun validateEmergencyContact(data: RegistrationData): Boolean {
    return data.emergencyContactName.isNotBlank() &&
            data.emergencyContactRelationship.isNotBlank() &&
            data.emergencyContactPhone.isNotBlank()
}