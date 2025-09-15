package com.example.gmls.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import com.example.gmls.domain.model.User
import com.example.gmls.ui.components.PrimaryButton
import com.example.gmls.ui.components.SecondaryButton
import com.example.gmls.ui.components.LogoutConfirmationDialog
import com.example.gmls.ui.theme.Red
import com.example.gmls.ui.viewmodels.ProfileViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.gmls.ui.components.GlobalSnackbarHost
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.launch
import com.example.gmls.ui.components.IndonesianAddress
import com.example.gmls.ui.components.IndonesianAddressSelector
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import java.util.Date
import com.example.gmls.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackClick: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    viewModel: ProfileViewModel = hiltViewModel(),
    errorMessage: String? = null,
    successMessage: String? = null
) {
    val profileState by viewModel.profileState.collectAsState()
    val user = profileState.user
    var isEditing by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Editable fields state
    var fullName by remember { mutableStateOf(user?.fullName ?: "") }
    var email by remember { mutableStateOf(user?.email ?: "") }
    var phone by remember { mutableStateOf(user?.phoneNumber ?: "") }
    var address by remember { mutableStateOf(user?.address ?: "") }
    var indonesianAddress by remember { 
        mutableStateOf(
            if (user?.address?.isNotEmpty() == true) {
                IndonesianAddress.fromString(user.address)
            } else {
                IndonesianAddress()
            }
        )
    }
    var gender by remember { mutableStateOf(user?.gender ?: "") }
    var nationalId by remember { mutableStateOf(user?.nationalId ?: "") }
    var bloodType by remember { mutableStateOf(user?.bloodType ?: "") }
    var medicalConditions by remember { mutableStateOf(user?.medicalConditions?.joinToString(", ") ?: "") }
    var disabilities by remember { mutableStateOf(user?.disabilities?.joinToString(", ") ?: "") }
    var dateOfBirth by remember { mutableStateOf(user?.dateOfBirth) }
    var emergencyContactName by remember { mutableStateOf(user?.emergencyContact?.name ?: "") }
    var emergencyContactRelationship by remember { mutableStateOf(user?.emergencyContact?.relationship ?: "") }
    var emergencyContactPhone by remember { mutableStateOf(user?.emergencyContact?.phoneNumber ?: "") }
    var householdMembers by remember { mutableStateOf(user?.householdMembers ?: emptyList()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var familyCardNumber by remember { mutableStateOf(user?.familyCardNumber ?: "") }
    var placeOfBirth by remember { mutableStateOf(user?.placeOfBirth ?: "") }
    var religion by remember { mutableStateOf(user?.religion ?: "") }
    var maritalStatus by remember { mutableStateOf(user?.maritalStatus ?: "") }
    var familyRelationshipStatus by remember { mutableStateOf(user?.familyRelationshipStatus ?: "") }
    var lastEducation by remember { mutableStateOf(user?.lastEducation ?: "") }
    var occupation by remember { mutableStateOf(user?.occupation ?: "") }
    var economicStatus by remember { mutableStateOf(user?.economicStatus ?: "") }
    var latitude by remember { mutableStateOf(user?.latitude?.toString() ?: "") }
    var longitude by remember { mutableStateOf(user?.longitude?.toString() ?: "") }

    val canEdit = user != null
    val genderOptions = listOf(
        stringResource(R.string.male),
        stringResource(R.string.female),
        stringResource(R.string.other)
    )
    val bloodTypeOptions = listOf("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-")

    val saveProfile: () -> Unit = {
        if (canEdit) {
            @Suppress("UNCHECKED_CAST")
            viewModel.updateProfile(mapOf(
                "fullName" to fullName,
                "email" to email,
                "phoneNumber" to phone,
                "address" to address,
                "gender" to gender,
                "nationalId" to nationalId,
                "familyCardNumber" to familyCardNumber,
                "placeOfBirth" to placeOfBirth,
                "religion" to religion,
                "maritalStatus" to maritalStatus,
                "familyRelationshipStatus" to familyRelationshipStatus,
                "lastEducation" to lastEducation,
                "occupation" to occupation,
                "economicStatus" to economicStatus,
                "latitude" to latitude.toDoubleOrNull(),
                "longitude" to longitude.toDoubleOrNull(),
                "bloodType" to bloodType,
                "medicalConditions" to medicalConditions.split(",").map { it.trim() },
                "disabilities" to disabilities.split(",").map { it.trim() },
                "dateOfBirth" to dateOfBirth,
                "emergencyContactName" to emergencyContactName,
                "emergencyContactRelationship" to emergencyContactRelationship,
                "emergencyContactPhone" to emergencyContactPhone,
                "householdMembers" to householdMembers
            ) as Map<String, Any>)
            isEditing = false
        }
    }

    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Show snackbar for error or success
    LaunchedEffect(errorMessage, successMessage) {
        errorMessage?.let {
            coroutineScope.launch { snackbarHostState.showSnackbar(it) }
        }
        successMessage?.let {
            coroutineScope.launch { snackbarHostState.showSnackbar(it) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.my_profile)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.semantics { contentDescription = "Kembali" }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    if (canEdit && !isEditing) {
                        IconButton(onClick = { isEditing = true }, modifier = Modifier.semantics { contentDescription = "Edit profil" }) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                        }
                    }
                }
            )
        },
        snackbarHost = { GlobalSnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {
            // Profile Header with Avatar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Red,
                                Red.copy(alpha = 0.7f)
                            )
                        )
                    )
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile Avatar
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!user?.profilePictureUrl.isNullOrEmpty()) {
                            // Implemented: Use Coil image loader for profile picture
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(user!!.profilePictureUrl!!)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = stringResource(R.string.profile_photo_description),
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = user?.fullName?.take(2)?.uppercase() ?: "JD",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = Red
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = user?.fullName ?: stringResource(R.string.not_set),
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = user?.email ?: stringResource(R.string.not_set),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
            // Profile Sections
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Personal Information Section
                ProfileSection(
                    title = stringResource(R.string.personal_information),
                    icon = Icons.Default.Person
                ) {
                    if (isEditing) {
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text(stringResource(R.string.full_name_label)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text(stringResource(R.string.email_label)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text(stringResource(R.string.phone_number_label)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = nationalId,
                            onValueChange = { nationalId = it },
                            label = { Text(stringResource(R.string.national_id_label)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        // Gender dropdown
                        var genderExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = genderExpanded,
                            onExpandedChange = { genderExpanded = !genderExpanded }
                        ) {
                            OutlinedTextField(
                                value = gender,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(stringResource(R.string.gender_label)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = genderExpanded,
                                onDismissRequest = { genderExpanded = false }
                            ) {
                                genderOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            gender = option
                                            genderExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        // Date of Birth picker
                        OutlinedTextField(
                            value = dateOfBirth?.toString() ?: stringResource(R.string.not_set),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.date_of_birth_label)) },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { showDatePicker = true }) {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = stringResource(R.string.select_date_description))
                                }
                            }
                        )
                        if (showDatePicker) {
                            // Implemented: Date picker dialog
                            val datePickerState = rememberDatePickerState()
                            DatePickerDialog(
                                onDismissRequest = { showDatePicker = false },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            datePickerState.selectedDateMillis?.let { millis ->
                                                dateOfBirth = Date(millis)
                                            }
                                            showDatePicker = false
                                        }
                                    ) {
                                        Text(stringResource(R.string.ok))
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDatePicker = false }) {
                                        Text(stringResource(R.string.cancel))
                                    }
                                }
                            ) {
                                DatePicker(state = datePickerState)
                            }
                        }
                        OutlinedTextField(
                            value = familyCardNumber,
                            onValueChange = { familyCardNumber = it },
                            label = { Text(stringResource(R.string.family_card_number_label)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = placeOfBirth,
                            onValueChange = { placeOfBirth = it },
                            label = { Text(stringResource(R.string.place_of_birth_label)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = religion,
                            onValueChange = { religion = it },
                            label = { Text(stringResource(R.string.religion_dropdown_label)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = maritalStatus,
                            onValueChange = { maritalStatus = it },
                            label = { Text(stringResource(R.string.marital_status_dropdown_label)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = familyRelationshipStatus,
                            onValueChange = { familyRelationshipStatus = it },
                            label = { Text(stringResource(R.string.family_relationship_dropdown_label)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = lastEducation,
                            onValueChange = { lastEducation = it },
                            label = { Text(stringResource(R.string.last_education_label)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = occupation,
                            onValueChange = { occupation = it },
                            label = { Text(stringResource(R.string.occupation_dropdown_label)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = economicStatus,
                            onValueChange = { economicStatus = it },
                            label = { Text(stringResource(R.string.economic_status_dropdown_label)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = latitude,
                            onValueChange = { latitude = it },
                            label = { Text(stringResource(R.string.latitude_label)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = longitude,
                            onValueChange = { longitude = it },
                            label = { Text(stringResource(R.string.longitude_label)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    } else {
                        ProfileField(label = stringResource(R.string.full_name_field), value = user?.fullName ?: stringResource(R.string.not_set), icon = Icons.Outlined.Person)
                        ProfileField(label = stringResource(R.string.email_field), value = user?.email ?: stringResource(R.string.not_set), icon = Icons.Outlined.Email)
                        ProfileField(label = stringResource(R.string.phone_number_field), value = user?.phoneNumber ?: stringResource(R.string.not_set), icon = Icons.Outlined.Phone)
                        ProfileField(label = stringResource(R.string.birth_date_field), value = user?.dateOfBirth?.toString() ?: stringResource(R.string.not_set), icon = Icons.Outlined.CalendarMonth)
                        ProfileField(label = stringResource(R.string.gender_field), value = user?.gender ?: stringResource(R.string.not_set), icon = Icons.Outlined.Person)
                        ProfileField(label = stringResource(R.string.national_id_field), value = user?.nationalId ?: stringResource(R.string.not_set), icon = Icons.Outlined.Badge)
                        ProfileField(label = stringResource(R.string.family_card_field), value = user?.familyCardNumber ?: stringResource(R.string.not_set), icon = Icons.Outlined.Badge)
                        ProfileField(label = stringResource(R.string.place_of_birth_field), value = user?.placeOfBirth ?: stringResource(R.string.not_set), icon = Icons.Outlined.Place)
                        ProfileField(label = stringResource(R.string.religion_field), value = user?.religion ?: stringResource(R.string.not_set), icon = Icons.Outlined.AccountBalance)
                        ProfileField(label = stringResource(R.string.marital_status_field), value = user?.maritalStatus ?: stringResource(R.string.not_set), icon = Icons.Outlined.FamilyRestroom)
                        ProfileField(label = stringResource(R.string.family_relationship_field), value = user?.familyRelationshipStatus ?: stringResource(R.string.not_set), icon = Icons.Outlined.People)
                        ProfileField(label = stringResource(R.string.education_field), value = user?.lastEducation ?: stringResource(R.string.not_set), icon = Icons.Outlined.School)
                        ProfileField(label = stringResource(R.string.occupation_field), value = user?.occupation ?: stringResource(R.string.not_set), icon = Icons.Outlined.Work)
                        ProfileField(label = stringResource(R.string.economic_status_field), value = user?.economicStatus ?: stringResource(R.string.not_set), icon = Icons.Outlined.Money)
                        ProfileField(label = stringResource(R.string.latitude_field), value = user?.latitude?.toString() ?: stringResource(R.string.not_set), icon = Icons.Outlined.LocationOn)
                        ProfileField(label = stringResource(R.string.longitude_field), value = user?.longitude?.toString() ?: stringResource(R.string.not_set), icon = Icons.Outlined.LocationOn)
                    }
                }
                // Address Section
                ProfileSection(title = stringResource(R.string.address_section), icon = Icons.Default.Home) {
                    if (isEditing) {
                        IndonesianAddressSelector(
                            address = indonesianAddress,
                            onAddressChange = { newAddress ->
                                indonesianAddress = newAddress
                                address = newAddress.toFullAddress() // For backward compatibility
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        ProfileField(label = stringResource(R.string.home_address_field), value = user?.address ?: stringResource(R.string.not_set), icon = Icons.Outlined.Home)
                    }
                }
                // Medical Information Section
                ProfileSection(title = stringResource(R.string.medical_information), icon = Icons.Default.MedicalServices) {
                    if (isEditing) {
                        // Blood type dropdown
                        var bloodTypeExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = bloodTypeExpanded,
                            onExpandedChange = { bloodTypeExpanded = !bloodTypeExpanded }
                        ) {
                            OutlinedTextField(
                                value = bloodType,
                                onValueChange = { bloodType = it },
                                label = { Text(stringResource(R.string.blood_type_label)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bloodTypeExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = bloodTypeExpanded,
                                onDismissRequest = { bloodTypeExpanded = false }
                            ) {
                                bloodTypeOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            bloodType = option
                                            bloodTypeExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = medicalConditions,
                            onValueChange = { medicalConditions = it },
                            label = { Text(stringResource(R.string.medical_conditions_label)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = disabilities,
                            onValueChange = { disabilities = it },
                            label = { Text(stringResource(R.string.disabilities_label)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        ProfileField(label = stringResource(R.string.blood_type_field), value = user?.bloodType ?: stringResource(R.string.not_set), icon = Icons.Outlined.Bloodtype)
                        ProfileField(label = stringResource(R.string.medical_conditions_field), value = user?.medicalConditions?.joinToString(", ") ?: stringResource(R.string.not_set), icon = Icons.Outlined.MedicalInformation)
                        ProfileField(label = stringResource(R.string.disabilities_field), value = user?.disabilities?.joinToString(", ") ?: stringResource(R.string.not_set), icon = Icons.Outlined.Accessible)
                    }
                }
                // Emergency Contact Section
                ProfileSection(title = stringResource(R.string.emergency_contact_section), icon = Icons.Default.ContactPhone) {
                    if (isEditing) {
                        OutlinedTextField(
                            value = emergencyContactName,
                            onValueChange = { emergencyContactName = it },
                            label = { Text(stringResource(R.string.emergency_contact_name_label)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = emergencyContactRelationship,
                            onValueChange = { emergencyContactRelationship = it },
                            label = { Text(stringResource(R.string.emergency_contact_relationship_label)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = emergencyContactPhone,
                            onValueChange = { emergencyContactPhone = it },
                            label = { Text(stringResource(R.string.emergency_contact_phone_label)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        ProfileField(label = stringResource(R.string.emergency_contact_name_field), value = user?.emergencyContact?.name ?: stringResource(R.string.not_set), icon = Icons.Outlined.Person)
                        ProfileField(label = stringResource(R.string.emergency_contact_relationship_field), value = user?.emergencyContact?.relationship ?: stringResource(R.string.not_set), icon = Icons.Outlined.People)
                        ProfileField(label = stringResource(R.string.emergency_contact_phone_field), value = user?.emergencyContact?.phoneNumber ?: stringResource(R.string.not_set), icon = Icons.Outlined.Phone)
                    }
                }
                // Household Information Section
                ProfileSection(title = stringResource(R.string.household_information_section), icon = Icons.Default.Group) {
                    if (isEditing) {
                        for ((index, member) in householdMembers.withIndex()) {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(Modifier.padding(8.dp)) {
                                    OutlinedTextField(
                                        value = member.name,
                                        onValueChange = { newName ->
                                            householdMembers = householdMembers.mapIndexed { i, m ->
                                                if (i == index) m.copy(name = newName) else m
                                            }
                                        },
                                        label = { Text(stringResource(R.string.name_label)) },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = member.relationship,
                                        onValueChange = { newRel ->
                                            householdMembers = householdMembers.mapIndexed { i, m ->
                                                if (i == index) m.copy(relationship = newRel) else m
                                            }
                                        },
                                        label = { Text(stringResource(R.string.relationship_label)) },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = if (member.age == 0) "" else member.age.toString(),
                                        onValueChange = { ageStr ->
                                            val age = ageStr.toIntOrNull() ?: 0
                                            householdMembers = householdMembers.mapIndexed { i, m ->
                                                if (i == index) m.copy(age = age) else m
                                            }
                                        },
                                        label = { Text(stringResource(R.string.age_label)) },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = member.specialNeeds,
                                        onValueChange = { newNeeds ->
                                            householdMembers = householdMembers.mapIndexed { i, m ->
                                                if (i == index) m.copy(specialNeeds = newNeeds) else m
                                            }
                                        },
                                        label = { Text(stringResource(R.string.special_needs_label)) },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                        TextButton(onClick = {
                                            householdMembers = householdMembers.filterIndexed { i, _ -> i != index }
                                        }) {
                                            Text(stringResource(R.string.remove_member))
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = {
                            householdMembers = householdMembers + com.example.gmls.domain.model.HouseholdMember()
                        }, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.add_household_member))
                        }
                    } else {
                        if (user?.householdMembers.isNullOrEmpty()) {
                            ProfileField(label = stringResource(R.string.household_members_label), value = stringResource(R.string.none_label), icon = Icons.Outlined.Groups)
                        } else {
                            user?.householdMembers?.forEach { member ->
                                ProfileField(label = stringResource(R.string.name_label), value = member.name, icon = Icons.Outlined.Person)
                                ProfileField(label = stringResource(R.string.relationship_label), value = member.relationship, icon = Icons.Outlined.People)
                                ProfileField(label = stringResource(R.string.age_label), value = member.age.toString(), icon = Icons.Outlined.Cake)
                                ProfileField(label = stringResource(R.string.special_needs_label), value = member.specialNeeds, icon = Icons.Outlined.MedicalInformation)
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                if (isEditing) {
                    Button(onClick = saveProfile, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.save_changes))
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.logout_button))
                }
            }
        }
    }

    if (showLogoutDialog) {
        LogoutConfirmationDialog(
            onConfirm = {
                showLogoutDialog = false
                onLogout()
            },
            onDismiss = {
                showLogoutDialog = false
            }
        )
    }
}

@Composable
fun ProfileSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Section Header
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Red
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Red
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section Content
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = content
            )
        }
    }
}

@Composable
fun ProfileField(
    label: String,
    value: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
