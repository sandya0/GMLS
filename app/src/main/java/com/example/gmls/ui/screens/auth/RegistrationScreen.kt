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
import androidx.compose.ui.res.stringResource
import com.example.gmls.R
import java.text.SimpleDateFormat
import java.util.*
import java.time.LocalDate
import java.time.ZoneId
import java.time.Period
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.gmls.ui.viewmodels.AuthViewModel
import com.example.gmls.ui.viewmodels.AuthState
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.launch
import com.example.gmls.ui.components.IndonesianAddress
import com.example.gmls.ui.components.IndonesianAddressSelector

data class RegistrationData(
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val dateOfBirth: Date? = null,
    val gender: String = "",
    val nationalId: String = "",
    val familyCardNumber: String = "",
    val placeOfBirth: String = "",
    val religion: String = "",
    val maritalStatus: String = "",
    val familyRelationshipStatus: String = "",
    val lastEducation: String = "",
    val occupation: String = "",
    val economicStatus: String = "",
    val phoneNumber: String = "",
    val address: String = "", // Deprecated - use indonesianAddress instead
    val indonesianAddress: IndonesianAddress = IndonesianAddress(),
    val bloodType: String = "",
    val medicalConditions: String = "",
    val disabilities: String = "",
    val emergencyContactName: String = "",
    val emergencyContactRelationship: String = "",
    val emergencyContactPhone: String = "",
    val householdMembers: Int = 1,
    val acceptedTerms: Boolean = false,
    val locationPermissionGranted: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null
) {
    // Helper function to get combined address for backward compatibility
    fun getFullAddress(): String {
        return if (indonesianAddress.provinsi.isNotEmpty()) {
            indonesianAddress.toFullAddress()
        } else {
            address
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    onRegister: (RegistrationData) -> Unit,
    onNavigateBack: () -> Unit,
    onRegistrationSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    var currentPage by remember { mutableStateOf(0) }
    var registrationData by remember { mutableStateOf(RegistrationData()) }
    var errors by remember { mutableStateOf(mapOf<String, String>()) }
    var locationRequested by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val authState by authViewModel.authState.collectAsState()
    var registrationError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Permission launcher for location
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            coroutineScope.launch {
                authViewModel.fetchAndSetLocation(registrationData.copy(locationPermissionGranted = true)) {
                    registrationData = it.copy(locationPermissionGranted = true)
                }
            }
        } else {
            coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Izin lokasi ditolak. Tidak dapat menggunakan lokasi.")
            }
            registrationData = registrationData.copy(locationPermissionGranted = false)
        }
    }

    // Observe authState for registration result
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                onRegistrationSuccess()
            }
            is AuthState.Error -> {
                registrationError = (authState as AuthState.Error).message
            }
            else -> {}
        }
    }

    // Fetch location when permission is granted and on last page
    LaunchedEffect(registrationData.locationPermissionGranted, currentPage) {
        if (currentPage == 4 && registrationData.locationPermissionGranted && !locationRequested) {
            locationRequested = true
            authViewModel.fetchAndSetLocation(registrationData) {
                registrationData = it
            }
        }
    }
    if (currentPage != 4 && locationRequested) locationRequested = false

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
                    contentDescription = stringResource(R.string.go_back)
                )
            }

            Text(
                text = stringResource(R.string.create_account_title),
                style = MaterialTheme.typography.headlineMedium
            )
        }

        // Progress indicator
        LinearProgressIndicator(
            progress = { (currentPage + 1) / 5f },
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
                3 -> AdditionalInfoPage(
                    registrationData = registrationData,
                    onDataChange = { registrationData = it },
                    errors = errors
                )
                4 -> EmergencyContactPage(
                    registrationData = registrationData,
                    onDataChange = { newData ->
                        // If user is requesting location permission
                        if (!registrationData.locationPermissionGranted && newData.locationPermissionGranted) {
                            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        } else {
                            registrationData = newData
                        }
                    },
                    errors = errors
                )
            }
        }

        if (registrationError != null) {
            Text(
                text = registrationError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
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
                    text = stringResource(R.string.previous),
                    onClick = { currentPage-- },
                    modifier = Modifier.weight(1f)
                )
            }

            if (currentPage < 4) {
                PrimaryButton(
                    text = stringResource(R.string.next),
                    onClick = {
                        if (validateCurrentPage(currentPage, registrationData)) {
                            currentPage++
                            errors = emptyMap()
                        } else {
                            errors = getErrorsForPage(currentPage, registrationData, context)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            } else {
                PrimaryButton(
                    text = stringResource(R.string.create_account),
                    onClick = {
                        registrationError = null
                        val tempErrors = getErrorsForPage(currentPage, registrationData, context).toMutableMap()
                        if (!registrationData.acceptedTerms) {
                            tempErrors["terms"] = context.getString(R.string.terms_agreement_required)
                        }
                        
                        if (tempErrors.isEmpty() && validateCurrentPage(currentPage, registrationData)) {
                            if (registrationData.locationPermissionGranted &&
                                (registrationData.latitude == null || registrationData.longitude == null)) {
                                // Fetch location before registering
                                authViewModel.fetchAndSetLocation(registrationData) {
                                    authViewModel.register(it)
                                }
                            } else {
                                authViewModel.register(registrationData)
                            }
                        } else {
                            errors = tempErrors
                        }
                    },
                    isLoading = isLoading || authState is AuthState.Loading,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    // Show snackbar host for feedback
    GlobalSnackbarHost(snackbarHostState)
}

@Composable
fun BasicInfoPage(
    registrationData: RegistrationData,
    onDataChange: (RegistrationData) -> Unit,
    errors: Map<String, String>
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = stringResource(R.string.basic_information),
            style = MaterialTheme.typography.headlineSmall
        )

        DisasterTextField(
            value = registrationData.fullName,
            onValueChange = { onDataChange(registrationData.copy(fullName = it)) },
            label = stringResource(R.string.full_name),
            leadingIcon = Icons.Filled.Person,
            isError = errors.containsKey("fullName"),
            errorMessage = errors["fullName"]
        )

        DisasterTextField(
            value = registrationData.email,
            onValueChange = { onDataChange(registrationData.copy(email = it)) },
            label = stringResource(R.string.email),
            leadingIcon = Icons.Filled.Email,
            keyboardType = KeyboardType.Email,
            isError = errors.containsKey("email"),
            errorMessage = errors["email"]
        )

        DisasterTextField(
            value = registrationData.password,
            onValueChange = { onDataChange(registrationData.copy(password = it)) },
            label = stringResource(R.string.password),
            leadingIcon = Icons.Filled.Lock,
            isPassword = true,
            isError = errors.containsKey("password"),
            errorMessage = errors["password"]
        )

        DisasterTextField(
            value = registrationData.confirmPassword,
            onValueChange = { onDataChange(registrationData.copy(confirmPassword = it)) },
            label = stringResource(R.string.confirm_password),
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
    val genderOptions = listOf(
        stringResource(R.string.male),
        stringResource(R.string.female)
    )
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
            text = stringResource(R.string.personal_details),
            style = MaterialTheme.typography.headlineSmall
        )

        // Date of Birth Picker
        Column {
            Text(
                text = stringResource(R.string.date_of_birth),
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
                    label = { Text(stringResource(R.string.date_format_placeholder)) },
                    leadingIcon = {
                        Icon(Icons.Filled.CalendarMonth, contentDescription = "Kalender")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    enabled = false,
                    isError = errors.containsKey("dateOfBirth"),
                    trailingIcon = {
                        if (errors.containsKey("dateOfBirth")) {
                            Icon(
                                Icons.Filled.Error,
                                contentDescription = "Kesalahan",
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
                text = stringResource(R.string.gender),
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
            onValueChange = { onDataChange(registrationData.copy(nationalId = it.filter { c -> c.isDigit() }.take(16))) },
            label = stringResource(R.string.national_id),
            leadingIcon = Icons.Filled.Badge,
            keyboardType = KeyboardType.Number,
            isError = errors.containsKey("nationalId"),
            errorMessage = errors["nationalId"]
        )

        DisasterTextField(
            value = registrationData.familyCardNumber,
            onValueChange = { onDataChange(registrationData.copy(familyCardNumber = it.filter { c -> c.isDigit() }.take(16))) },
            label = stringResource(R.string.family_card_number),
            leadingIcon = Icons.Filled.Badge,
            keyboardType = KeyboardType.Number,
            isError = errors.containsKey("familyCardNumber"),
            errorMessage = errors["familyCardNumber"]
        )

        DisasterTextField(
            value = registrationData.placeOfBirth,
            onValueChange = { onDataChange(registrationData.copy(placeOfBirth = it)) },
            label = stringResource(R.string.place_of_birth),
            leadingIcon = Icons.Filled.Place,
            isError = errors.containsKey("placeOfBirth"),
            errorMessage = errors["placeOfBirth"]
        )

        DisasterTextField(
            value = registrationData.phoneNumber,
            onValueChange = { onDataChange(registrationData.copy(phoneNumber = it)) },
            label = stringResource(R.string.phone_number),
            leadingIcon = Icons.Filled.Phone,
            keyboardType = KeyboardType.Phone,
            isError = errors.containsKey("phoneNumber"),
            errorMessage = errors["phoneNumber"]
        )

        // Indonesian Address Section
        Column {
            Text(
                text = stringResource(R.string.complete_address_indo),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            IndonesianAddressSelector(
                address = registrationData.indonesianAddress,
                onAddressChange = { newAddress ->
                    onDataChange(
                        registrationData.copy(
                            indonesianAddress = newAddress,
                            address = newAddress.toFullAddress() // For backward compatibility
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            isError = errors.containsKey("address"),
            errorMessage = errors["address"]
        )
        }
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
            text = stringResource(R.string.medical_information),
            style = MaterialTheme.typography.headlineSmall
        )

        Column {
            Text(
                text = stringResource(R.string.blood_type),
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
                // Do not show error
            }
        }

        DisasterTextField(
            value = registrationData.medicalConditions,
            onValueChange = { onDataChange(registrationData.copy(medicalConditions = it)) },
            label = stringResource(R.string.medical_conditions_optional),
            leadingIcon = Icons.Outlined.MedicalServices
        )

        DisasterTextField(
            value = registrationData.disabilities,
            onValueChange = { onDataChange(registrationData.copy(disabilities = it)) },
            label = stringResource(R.string.disabilities_optional),
            leadingIcon = Icons.Outlined.Accessible
        )

        Column {
            Text(
                text = stringResource(R.string.number_of_household_members),
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
fun AdditionalInfoPage(
    registrationData: RegistrationData,
    onDataChange: (RegistrationData) -> Unit,
    errors: Map<String, String>
) {
    val religions = listOf(
        stringResource(R.string.islam),
        stringResource(R.string.christian),
        stringResource(R.string.catholic),
        stringResource(R.string.hindu),
        stringResource(R.string.buddhist),
        stringResource(R.string.confucian),
        stringResource(R.string.other)
    )
    val maritalStatuses = listOf(
        stringResource(R.string.single),
        stringResource(R.string.married),
        stringResource(R.string.divorced),
        stringResource(R.string.widowed)
    )
    val familyRelationships = listOf(
        stringResource(R.string.head_of_family),
        stringResource(R.string.wife),
        stringResource(R.string.child),
        stringResource(R.string.parent),
        stringResource(R.string.other)
    )
    val educations = listOf(
        stringResource(R.string.no_school),
        stringResource(R.string.elementary),
        stringResource(R.string.junior_high),
        stringResource(R.string.senior_high),
        stringResource(R.string.diploma),
        stringResource(R.string.bachelor),
        stringResource(R.string.postgraduate)
    )
    val occupations = listOf(
        stringResource(R.string.student),
        stringResource(R.string.college_student),
        stringResource(R.string.civil_servant),
        stringResource(R.string.military_police),
        stringResource(R.string.private_employee),
        stringResource(R.string.entrepreneur),
        stringResource(R.string.farmer),
        stringResource(R.string.fisherman),
        stringResource(R.string.other)
    )
    val economicStatuses = listOf(
        stringResource(R.string.very_poor),
        stringResource(R.string.poor),
        stringResource(R.string.middle_class),
        stringResource(R.string.wealthy)
    )

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(text = stringResource(R.string.additional_information), style = MaterialTheme.typography.headlineSmall)
        DropdownField(
            value = registrationData.religion,
            onValueChange = { onDataChange(registrationData.copy(religion = it)) },
            label = stringResource(R.string.religion_label),
            options = religions,
            isError = errors.containsKey("religion"),
            errorMessage = errors["religion"]
        )
        DropdownField(
            value = registrationData.maritalStatus,
            onValueChange = { onDataChange(registrationData.copy(maritalStatus = it)) },
            label = stringResource(R.string.marital_status_label),
            options = maritalStatuses,
            isError = errors.containsKey("maritalStatus"),
            errorMessage = errors["maritalStatus"]
        )
        DropdownField(
            value = registrationData.familyRelationshipStatus,
            onValueChange = { onDataChange(registrationData.copy(familyRelationshipStatus = it)) },
            label = stringResource(R.string.family_relationship_label),
            options = familyRelationships,
            isError = errors.containsKey("familyRelationshipStatus"),
            errorMessage = errors["familyRelationshipStatus"]
        )
        DropdownField(
            value = registrationData.lastEducation,
            onValueChange = { onDataChange(registrationData.copy(lastEducation = it)) },
            label = "Last Education (Pendidikan Terakhir)",
            options = educations,
            isError = errors.containsKey("lastEducation"),
            errorMessage = errors["lastEducation"]
        )
        DropdownField(
            value = registrationData.occupation,
            onValueChange = { onDataChange(registrationData.copy(occupation = it)) },
            label = "Occupation (Jenis Pekerjaan)",
            options = occupations,
            isError = errors.containsKey("occupation"),
            errorMessage = errors["occupation"]
        )
        DropdownField(
            value = registrationData.economicStatus,
            onValueChange = { onDataChange(registrationData.copy(economicStatus = it)) },
            label = "Economic Status (Status Ekonomi)",
            options = economicStatuses,
            isError = errors.containsKey("economicStatus"),
            errorMessage = errors["economicStatus"]
        )
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
            text = stringResource(R.string.emergency_contact),
            style = MaterialTheme.typography.headlineSmall
        )

        DisasterTextField(
            value = registrationData.emergencyContactName,
            onValueChange = { onDataChange(registrationData.copy(emergencyContactName = it)) },
            label = stringResource(R.string.emergency_contact_name),
            leadingIcon = Icons.Filled.ContactPhone,
            isError = errors.containsKey("emergencyContactName"),
            errorMessage = errors["emergencyContactName"]
        )

        DisasterTextField(
            value = registrationData.emergencyContactRelationship,
            onValueChange = { onDataChange(registrationData.copy(emergencyContactRelationship = it)) },
            label = stringResource(R.string.relationship),
            leadingIcon = Icons.Filled.Group,
            isError = errors.containsKey("emergencyContactRelationship"),
            errorMessage = errors["emergencyContactRelationship"]
        )

        DisasterTextField(
            value = registrationData.emergencyContactPhone,
            onValueChange = { onDataChange(registrationData.copy(emergencyContactPhone = it)) },
            label = stringResource(R.string.emergency_contact_phone),
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
                onCheckedChange = { checked ->
                    if (checked) {
                        // Use a callback to parent to request permission
                        onDataChange(registrationData.copy(locationPermissionGranted = true))
                    } else {
                        onDataChange(registrationData.copy(locationPermissionGranted = false, latitude = null, longitude = null))
                    }
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.location_access_agreement),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (errors.containsKey("locationPermission")) {
            Text(
                text = errors["locationPermission"] ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 48.dp)
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
                text = stringResource(R.string.terms_agreement),
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
        3 -> validateAdditionalInfo(data)
        4 -> validateEmergencyContact(data) // Terms are validated separately before final submission
        else -> false
    }
}

private fun getErrorsForPage(page: Int, data: RegistrationData, context: Context): Map<String, String> {
    val errors = mutableMapOf<String, String>()

    when (page) {
        0 -> {
            if (data.fullName.isBlank()) {
                errors["fullName"] = context.getString(R.string.full_name_required)
            }

            if (data.email.isBlank()) {
                errors["email"] = context.getString(R.string.email_required)
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(data.email).matches()) {
                errors["email"] = context.getString(R.string.valid_email_required)
            }

            if (data.password.isBlank()) {
                errors["password"] = context.getString(R.string.password_required)
            } else if (data.password.length < 8) {
                errors["password"] = context.getString(R.string.password_min_length)
            }

            if (data.confirmPassword.isBlank()) {
                errors["confirmPassword"] = context.getString(R.string.confirm_password_required)
            } else if (data.password != data.confirmPassword) {
                errors["confirmPassword"] = context.getString(R.string.passwords_do_not_match)
            }
        }
        1 -> {
            if (data.dateOfBirth == null) {
                errors["dateOfBirth"] = context.getString(R.string.date_of_birth_required)
            } else {
                val birthLocalDate = data.dateOfBirth.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                val age = Period.between(birthLocalDate, LocalDate.now()).years

                if (age < 18) {
                    errors["dateOfBirth"] = context.getString(R.string.must_be_18_years_old)
                } else if (age > 100) {
                    errors["dateOfBirth"] = context.getString(R.string.date_of_birth_required)
                }
            }

            if (data.gender.isBlank()) {
                errors["gender"] = context.getString(R.string.gender_required)
            } else if (data.gender != context.getString(R.string.male) && data.gender != context.getString(R.string.female)) {
                errors["gender"] = context.getString(R.string.gender_required)
            }

            if (data.nationalId.isBlank()) {
                errors["nationalId"] = context.getString(R.string.national_id_required)
            } else if (!data.nationalId.matches(Regex("\\d{16}"))) {
                errors["nationalId"] = context.getString(R.string.national_id_16_digits)
            }

            if (data.familyCardNumber.isBlank()) {
                errors["familyCardNumber"] = context.getString(R.string.family_card_required)
            } else if (!data.familyCardNumber.matches(Regex("\\d{16}"))) {
                errors["familyCardNumber"] = context.getString(R.string.family_card_16_digits)
            }

            if (data.phoneNumber.isBlank()) {
                errors["phoneNumber"] = context.getString(R.string.phone_number_required)
            } else if (!android.util.Patterns.PHONE.matcher(data.phoneNumber).matches()) {
                errors["phoneNumber"] = context.getString(R.string.valid_phone_required)
            }

            // Validate Indonesian address
            val isAddressValid = data.indonesianAddress.provinsi.isNotBlank() && 
                                data.indonesianAddress.kabupatenKota.isNotBlank()

            if (!isAddressValid) {
                errors["address"] = context.getString(R.string.address_required)
            }
        }
        2 -> {
            // Blood type is now optional, so do not add error if blank
        }
        3 -> {
            if (data.religion.isBlank()) {
                errors["religion"] = context.getString(R.string.religion_required)
            }
            if (data.maritalStatus.isBlank()) {
                errors["maritalStatus"] = context.getString(R.string.marital_status_required)
            }
            if (data.familyRelationshipStatus.isBlank()) {
                errors["familyRelationshipStatus"] = context.getString(R.string.family_relationship_required)
            }
            if (data.lastEducation.isBlank()) {
                errors["lastEducation"] = context.getString(R.string.education_required)
            }
            if (data.occupation.isBlank()) {
                errors["occupation"] = context.getString(R.string.occupation_required)
            }
            if (data.economicStatus.isBlank()) {
                errors["economicStatus"] = context.getString(R.string.economic_status_required)
            }
        }
        4 -> {
            if (data.emergencyContactName.isBlank()) {
                errors["emergencyContactName"] = context.getString(R.string.emergency_contact_name_required)
            }

            if (data.emergencyContactRelationship.isBlank()) {
                errors["emergencyContactRelationship"] = context.getString(R.string.emergency_contact_relationship_required)
            }

            if (data.emergencyContactPhone.isBlank()) {
                errors["emergencyContactPhone"] = context.getString(R.string.emergency_contact_phone_required)
            } else if (!android.util.Patterns.PHONE.matcher(data.emergencyContactPhone).matches()) {
                errors["emergencyContactPhone"] = context.getString(R.string.valid_phone_required)
            }
            if (!data.locationPermissionGranted) {
                errors["locationPermission"] = context.getString(R.string.location_permission_required)
            }
            // The terms error is handled directly in the RegistrationScreen for the final button
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

    val isNIKValid = data.nationalId.matches(Regex("\\d{16}"))
    val isKKValid = data.familyCardNumber.matches(Regex("\\d{16}"))
    val isGenderValid = data.gender.isNotBlank()
    
    // Validate Indonesian address
    val isAddressValid = data.indonesianAddress.provinsi.isNotBlank() && 
                        data.indonesianAddress.kabupatenKota.isNotBlank()

    return isDateValid &&
            isGenderValid &&
            isNIKValid &&
            isKKValid &&
            data.phoneNumber.isNotBlank() && android.util.Patterns.PHONE.matcher(data.phoneNumber).matches() &&
            isAddressValid
}

private fun validateMedicalInfo(data: RegistrationData): Boolean {
    return true // All fields optional on this page
}

private fun validateAdditionalInfo(data: RegistrationData): Boolean {
    return data.religion.isNotBlank() &&
            data.maritalStatus.isNotBlank() &&
            data.familyRelationshipStatus.isNotBlank() &&
            data.lastEducation.isNotBlank() &&
            data.occupation.isNotBlank() &&
            data.economicStatus.isNotBlank()
}

private fun validateEmergencyContact(data: RegistrationData): Boolean {
    return data.emergencyContactName.isNotBlank() &&
            data.emergencyContactRelationship.isNotBlank() &&
            data.emergencyContactPhone.isNotBlank() && android.util.Patterns.PHONE.matcher(data.emergencyContactPhone).matches()
    // data.acceptedTerms is checked on the final submission in RegistrationScreen
}
