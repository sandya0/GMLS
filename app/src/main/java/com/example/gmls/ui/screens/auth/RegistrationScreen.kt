package com.example.gmls.ui.screens.auth

import android.app.DatePickerDialog
import android.content.Context
import android.widget.DatePicker
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.gmls.ui.components.*
import com.example.gmls.ui.theme.Red
import java.text.SimpleDateFormat
import java.util.*
import java.time.LocalDate
import java.time.ZoneId
import java.time.Period

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
            progress = { (currentPage + 1) / 4f },
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
                            errors = getErrorsForPage(currentPage, registrationData)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            } else {
                PrimaryButton(
                    text = "Create Account",
                    onClick = {
                        if (validateCurrentPage(currentPage, registrationData) &&
                            registrationData.acceptedTerms) {
                            onRegister(registrationData)
                        } else {
                            // Update errors for the last page, including terms check
                            val tempErrors = getErrorsForPage(currentPage, registrationData).toMutableMap()
                            if (!registrationData.acceptedTerms && !tempErrors.containsKey("terms")) {
                                tempErrors["terms"] = "You must agree to the terms and conditions"
                            }
                            errors = tempErrors
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
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Basic Information",
            style = MaterialTheme.typography.headlineSmall
        )

        DisasterTextField(
            value = registrationData.fullName,
            onValueChange = { onDataChange(registrationData.copy(fullName = it)) },
            label = "Full Name",
            leadingIcon = Icons.Filled.Person,
            isError = errors.containsKey("fullName"),
            errorMessage = errors["fullName"]
        )

        DisasterTextField(
            value = registrationData.email,
            onValueChange = { onDataChange(registrationData.copy(email = it)) },
            label = "Email",
            leadingIcon = Icons.Filled.Email,
            keyboardType = KeyboardType.Email,
            isError = errors.containsKey("email"),
            errorMessage = errors["email"]
        )

        DisasterTextField(
            value = registrationData.password,
            onValueChange = { onDataChange(registrationData.copy(password = it)) },
            label = "Password",
            leadingIcon = Icons.Filled.Lock,
            isPassword = true,
            isError = errors.containsKey("password"),
            errorMessage = errors["password"]
        )

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
    val context = LocalContext.current

    val dateFormatter = remember {
        SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    }

    var selectedDate by remember(registrationData.dateOfBirth) {
        mutableStateOf(registrationData.dateOfBirth ?: Calendar.getInstance().apply {
            add(Calendar.YEAR, -18) // Default to 18 years ago
        }.time)
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Personal Details",
            style = MaterialTheme.typography.headlineSmall
        )

        // Date of Birth Picker
        Column {
            Text(
                text = "Date of Birth",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        showDatePicker(
                            context = context,
                            initialDate = selectedDate,
                            onDateSelected = { newDate ->
                                selectedDate = newDate
                                onDataChange(registrationData.copy(dateOfBirth = newDate))
                            }
                        )
                    }
            ) {
                OutlinedTextField(
                    value = dateFormatter.format(selectedDate),
                    onValueChange = {},
                    label = { Text("MM/DD/YYYY") },
                    leadingIcon = {
                        Icon(Icons.Filled.CalendarMonth, contentDescription = "Calendar")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    enabled = false,
                    isError = errors.containsKey("dateOfBirth"),
                    trailingIcon = {
                        if (errors.containsKey("dateOfBirth")) {
                            Icon(
                                Icons.Filled.Error,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )
            }

            if (errors.containsKey("dateOfBirth")) {
                Text(
                    text = errors["dateOfBirth"] ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                )
            }
        }

        // Gender Selection
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

        DisasterTextField(
            value = registrationData.nationalId,
            onValueChange = { onDataChange(registrationData.copy(nationalId = it)) },
            label = "National ID",
            leadingIcon = Icons.Filled.Badge,
            isError = errors.containsKey("nationalId"),
            errorMessage = errors["nationalId"]
        )

        DisasterTextField(
            value = registrationData.phoneNumber,
            onValueChange = { onDataChange(registrationData.copy(phoneNumber = it)) },
            label = "Phone Number",
            leadingIcon = Icons.Filled.Phone,
            keyboardType = KeyboardType.Phone,
            isError = errors.containsKey("phoneNumber"),
            errorMessage = errors["phoneNumber"]
        )

        DisasterTextField(
            value = registrationData.address,
            onValueChange = { onDataChange(registrationData.copy(address = it)) },
            label = "Address",
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

        DisasterTextField(
            value = registrationData.medicalConditions,
            onValueChange = { onDataChange(registrationData.copy(medicalConditions = it)) },
            label = "Medical Conditions (Optional)",
            leadingIcon = Icons.Outlined.MedicalServices
        )

        DisasterTextField(
            value = registrationData.disabilities,
            onValueChange = { onDataChange(registrationData.copy(disabilities = it)) },
            label = "Disabilities (Optional)",
            leadingIcon = Icons.Outlined.Accessible
        )

        Column {
            Text(
                text = "Number of Household Members",
                style = MaterialTheme.typography.bodyMedium
            )

            Slider(
                value = registrationData.householdMembers.toFloat(),
                onValueChange = { onDataChange(registrationData.copy(householdMembers = it.toInt())) },
                valueRange = 1f..10f,
                steps = 9, // Results in 10 possible values (1 to 10)
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

        DisasterTextField(
            value = registrationData.emergencyContactName,
            onValueChange = { onDataChange(registrationData.copy(emergencyContactName = it)) },
            label = "Emergency Contact Name",
            leadingIcon = Icons.Filled.ContactPhone,
            isError = errors.containsKey("emergencyContactName"),
            errorMessage = errors["emergencyContactName"]
        )

        DisasterTextField(
            value = registrationData.emergencyContactRelationship,
            onValueChange = { onDataChange(registrationData.copy(emergencyContactRelationship = it)) },
            label = "Relationship",
            leadingIcon = Icons.Filled.Group,
            isError = errors.containsKey("emergencyContactRelationship"),
            errorMessage = errors["emergencyContactRelationship"]
        )

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

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = registrationData.locationPermissionGranted,
                onCheckedChange = { onDataChange(registrationData.copy(locationPermissionGranted = it)) }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Allow access to my location for disaster reporting",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = registrationData.acceptedTerms,
                onCheckedChange = { onDataChange(registrationData.copy(acceptedTerms = it)) }
            )
            Spacer(modifier = Modifier.width(8.dp))
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
                modifier = Modifier.padding(start = 48.dp) // Aligns with checkbox text roughly
            )
        }
    }
}

// This private helper function is used to show the DatePickerDialog
private fun showDatePicker(
    context: Context,
    initialDate: Date,
    onDateSelected: (Date) -> Unit
) {
    val calendar = Calendar.getInstance().apply {
        time = initialDate
    }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selectedCalendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
            }
            val selectedDate = selectedCalendar.time
            onDateSelected(selectedDate)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    // Set max date to current date
    datePickerDialog.datePicker.maxDate = System.currentTimeMillis()

    // Set min date to 100 years ago
    val minDate = Calendar.getInstance().apply {
        add(Calendar.YEAR, -100)
    }.timeInMillis
    datePickerDialog.datePicker.minDate = minDate

    datePickerDialog.show()
}


private fun validateCurrentPage(page: Int, data: RegistrationData): Boolean {
    return when (page) {
        0 -> validateBasicInfo(data)
        1 -> validatePersonalDetails(data)
        2 -> validateMedicalInfo(data)
        3 -> validateEmergencyContact(data) // Terms are validated separately before final submission
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
            } else {
                val birthLocalDate = data.dateOfBirth.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                val age = Period.between(birthLocalDate, LocalDate.now()).years

                if (age < 18) {
                    errors["dateOfBirth"] = "You must be at least 18 years old"
                } else if (age > 100) { // Assuming 100 is a reasonable upper limit
                    errors["dateOfBirth"] = "Please enter a valid birth date"
                }
            }

            if (data.gender.isBlank()) {
                errors["gender"] = "Please select a gender"
            }

            if (data.nationalId.isBlank()) {
                errors["nationalId"] = "National ID is required"
            }

            if (data.phoneNumber.isBlank()) {
                errors["phoneNumber"] = "Phone number is required"
            } else if (!android.util.Patterns.PHONE.matcher(data.phoneNumber).matches()) {
                // Basic phone validation, can be made more specific
                errors["phoneNumber"] = "Please enter a valid phone number"
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
            } else if (!android.util.Patterns.PHONE.matcher(data.emergencyContactPhone).matches()) {
                errors["emergencyContactPhone"] = "Please enter a valid phone number"
            }
            // The terms error is handled directly in the RegistrationScreen for the final button
            // or can be added here if validation for "Next" on page 3 should check it too.
            // For now, the final "Create Account" button checks it.
        }
    }
    return errors
}


// Extension function to convert Date to LocalDate
private fun Date.toLocalDate(): LocalDate {
    return this.toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
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
    val isDateValid: Boolean = data.dateOfBirth?.let {
        val birthLocalDate = it.toLocalDate()
        val age = Period.between(birthLocalDate, LocalDate.now()).years
        age in 18..100
    } ?: false

    return isDateValid &&
            data.gender.isNotBlank() &&
            data.nationalId.isNotBlank() &&
            data.phoneNumber.isNotBlank() && android.util.Patterns.PHONE.matcher(data.phoneNumber).matches() &&
            data.address.isNotBlank()
}

private fun validateMedicalInfo(data: RegistrationData): Boolean {
    // Medical conditions and disabilities are optional, so only blood type is mandatory here.
    return data.bloodType.isNotBlank()
}

private fun validateEmergencyContact(data: RegistrationData): Boolean {
    return data.emergencyContactName.isNotBlank() &&
            data.emergencyContactRelationship.isNotBlank() &&
            data.emergencyContactPhone.isNotBlank() && android.util.Patterns.PHONE.matcher(data.emergencyContactPhone).matches()
    // data.acceptedTerms is checked on the final submission in RegistrationScreen
}