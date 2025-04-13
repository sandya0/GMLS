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
import com.example.gmls.domain.model.User
import com.example.gmls.ui.components.PrimaryButton
import com.example.gmls.ui.components.SecondaryButton
import com.example.gmls.ui.theme.Red
import com.example.gmls.ui.viewmodels.ProfileViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.gmls.ui.components.GlobalSnackbarHost
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.launch

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

    // Editable fields state
    var fullName by remember { mutableStateOf(user?.fullName ?: "") }
    var email by remember { mutableStateOf(user?.email ?: "") }
    var phone by remember { mutableStateOf(user?.phoneNumber ?: "") }
    var address by remember { mutableStateOf(user?.address ?: "") }
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
    val genderOptions = listOf("Male", "Female", "Other")
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
                title = { Text("My Profile") },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.  semantics { contentDescription = "Go back" }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    if (canEdit && !isEditing) {
                        IconButton(onClick = { isEditing = true }, modifier = Modifier.semantics { contentDescription = "Edit profile" }) {
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
                            // TODO: Use Coil/Image loader for profile picture
                            // CoilImage(url = user!!.profilePictureUrl!!)
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
                        text = user?.fullName ?: "Not set",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = user?.email ?: "Not set",
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
                    title = "Personal Information",
                    icon = Icons.Default.Person
                ) {
                    if (isEditing) {
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Full Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Phone Number") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = nationalId,
                            onValueChange = { nationalId = it },
                            label = { Text("National ID") },
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
                                label = { Text("Gender") },
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
                            value = dateOfBirth?.toString() ?: "Not set",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Date of Birth") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { showDatePicker = true }) {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = "Pick date")
                                }
                            }
                        )
                        if (showDatePicker) {
                            // TODO: Implement a date picker dialog and update dateOfBirth
                        }
                        OutlinedTextField(
                            value = familyCardNumber,
                            onValueChange = { familyCardNumber = it },
                            label = { Text("Family Card Number (Nomor KK)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = placeOfBirth,
                            onValueChange = { placeOfBirth = it },
                            label = { Text("Place of Birth") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = religion,
                            onValueChange = { religion = it },
                            label = { Text("Religion") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = maritalStatus,
                            onValueChange = { maritalStatus = it },
                            label = { Text("Marital Status") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = familyRelationshipStatus,
                            onValueChange = { familyRelationshipStatus = it },
                            label = { Text("Family Relationship Status") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = lastEducation,
                            onValueChange = { lastEducation = it },
                            label = { Text("Last Education") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = occupation,
                            onValueChange = { occupation = it },
                            label = { Text("Occupation") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = economicStatus,
                            onValueChange = { economicStatus = it },
                            label = { Text("Economic Status") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = latitude,
                            onValueChange = { latitude = it },
                            label = { Text("Latitude") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = longitude,
                            onValueChange = { longitude = it },
                            label = { Text("Longitude") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    } else {
                        ProfileField(label = "Full Name", value = user?.fullName ?: "Not set", icon = Icons.Outlined.Person)
                        ProfileField(label = "Email", value = user?.email ?: "Not set", icon = Icons.Outlined.Email)
                        ProfileField(label = "Phone Number", value = user?.phoneNumber ?: "Not set", icon = Icons.Outlined.Phone)
                        ProfileField(label = "Date of Birth", value = user?.dateOfBirth?.toString() ?: "Not set", icon = Icons.Outlined.CalendarMonth)
                        ProfileField(label = "Gender", value = user?.gender ?: "Not set", icon = Icons.Outlined.Person)
                        ProfileField(label = "National ID", value = user?.nationalId ?: "Not set", icon = Icons.Outlined.Badge)
                        ProfileField(label = "Family Card Number (Nomor KK)", value = user?.familyCardNumber ?: "Not set", icon = Icons.Outlined.Badge)
                        ProfileField(label = "Place of Birth", value = user?.placeOfBirth ?: "Not set", icon = Icons.Outlined.Place)
                        ProfileField(label = "Religion", value = user?.religion ?: "Not set", icon = Icons.Outlined.AccountBalance)
                        ProfileField(label = "Marital Status", value = user?.maritalStatus ?: "Not set", icon = Icons.Outlined.FamilyRestroom)
                        ProfileField(label = "Family Relationship Status", value = user?.familyRelationshipStatus ?: "Not set", icon = Icons.Outlined.People)
                        ProfileField(label = "Last Education", value = user?.lastEducation ?: "Not set", icon = Icons.Outlined.School)
                        ProfileField(label = "Occupation", value = user?.occupation ?: "Not set", icon = Icons.Outlined.Work)
                        ProfileField(label = "Economic Status", value = user?.economicStatus ?: "Not set", icon = Icons.Outlined.Money)
                        ProfileField(label = "Latitude", value = user?.latitude?.toString() ?: "Not set", icon = Icons.Outlined.LocationOn)
                        ProfileField(label = "Longitude", value = user?.longitude?.toString() ?: "Not set", icon = Icons.Outlined.LocationOn)
                    }
                }
                // Address Section
                ProfileSection(title = "Address", icon = Icons.Default.Home) {
                    if (isEditing) {
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("Home Address") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        ProfileField(label = "Home Address", value = user?.address ?: "Not set", icon = Icons.Outlined.Home)
                    }
                }
                // Medical Information Section
                ProfileSection(title = "Medical Information", icon = Icons.Default.MedicalServices) {
                    if (isEditing) {
                        // Blood type dropdown
                        var bloodTypeExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = bloodTypeExpanded,
                            onExpandedChange = { bloodTypeExpanded = !bloodTypeExpanded }
                        ) {
                            OutlinedTextField(
                                value = bloodType,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Blood Type") },
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
                            label = { Text("Medical Conditions (comma separated)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = disabilities,
                            onValueChange = { disabilities = it },
                            label = { Text("Disabilities (comma separated)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        ProfileField(label = "Blood Type", value = user?.bloodType ?: "Not set", icon = Icons.Outlined.Bloodtype)
                        ProfileField(label = "Medical Conditions", value = user?.medicalConditions?.joinToString(", ") ?: "Not set", icon = Icons.Outlined.MedicalInformation)
                        ProfileField(label = "Disabilities", value = user?.disabilities?.joinToString(", ") ?: "Not set", icon = Icons.Outlined.Accessible)
                    }
                }
                // Emergency Contact Section
                ProfileSection(title = "Emergency Contact", icon = Icons.Default.ContactPhone) {
                    if (isEditing) {
                        OutlinedTextField(
                            value = emergencyContactName,
                            onValueChange = { emergencyContactName = it },
                            label = { Text("Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = emergencyContactRelationship,
                            onValueChange = { emergencyContactRelationship = it },
                            label = { Text("Relationship") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = emergencyContactPhone,
                            onValueChange = { emergencyContactPhone = it },
                            label = { Text("Phone Number") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        ProfileField(label = "Name", value = user?.emergencyContact?.name ?: "Not set", icon = Icons.Outlined.Person)
                        ProfileField(label = "Relationship", value = user?.emergencyContact?.relationship ?: "Not set", icon = Icons.Outlined.People)
                        ProfileField(label = "Phone Number", value = user?.emergencyContact?.phoneNumber ?: "Not set", icon = Icons.Outlined.Phone)
                    }
                }
                // Household Information Section
                ProfileSection(title = "Household Information", icon = Icons.Default.Group) {
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
                                        label = { Text("Name") },
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
                                        label = { Text("Relationship") },
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
                                        label = { Text("Age") },
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
                                        label = { Text("Special Needs") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                        TextButton(onClick = {
                                            householdMembers = householdMembers.filterIndexed { i, _ -> i != index }
                                        }) {
                                            Text("Remove")
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = {
                            householdMembers = householdMembers + com.example.gmls.domain.model.HouseholdMember()
                        }, modifier = Modifier.fillMaxWidth()) {
                            Text("Add Household Member")
                        }
                    } else {
                        if (user?.householdMembers.isNullOrEmpty()) {
                            ProfileField(label = "Household Members", value = "None", icon = Icons.Outlined.Groups)
                        } else {
                            user?.householdMembers?.forEach { member ->
                                ProfileField(label = "Name", value = member.name, icon = Icons.Outlined.Person)
                                ProfileField(label = "Relationship", value = member.relationship, icon = Icons.Outlined.People)
                                ProfileField(label = "Age", value = member.age.toString(), icon = Icons.Outlined.Cake)
                                ProfileField(label = "Special Needs", value = member.specialNeeds, icon = Icons.Outlined.MedicalInformation)
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                if (isEditing) {
                    Button(onClick = saveProfile, modifier = Modifier.fillMaxWidth()) {
                        Text("Save Changes")
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onLogout, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                    Text("Logout")
                }
            }
        }
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