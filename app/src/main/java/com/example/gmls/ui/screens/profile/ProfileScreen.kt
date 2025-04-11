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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gmls.domain.model.User
import com.example.gmls.ui.components.PrimaryButton
import com.example.gmls.ui.components.SecondaryButton
import com.example.gmls.ui.theme.Red

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackClick: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    user: User? = null,
    isLoading: Boolean = false
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Go back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Edit profile */ }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit profile"
                        )
                    }
                }
            )
        },
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
                        // If there's a profile picture, display it here
                        // Otherwise, show initials
                        Text(
                            text = user?.fullName?.take(2)?.uppercase() ?: "JD",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = Red
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // User Name
                    Text(
                        text = user?.fullName ?: "John Doe",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // User Email
                    Text(
                        text = user?.email ?: "johndoe@example.com",
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
                    ProfileField(
                        label = "Full Name",
                        value = user?.fullName ?: "John Doe",
                        icon = Icons.Outlined.Person
                    )

                    ProfileField(
                        label = "Email",
                        value = user?.email ?: "johndoe@example.com",
                        icon = Icons.Outlined.Email
                    )

                    ProfileField(
                        label = "Phone Number",
                        value = user?.phoneNumber ?: "+62 812-3456-7890",
                        icon = Icons.Outlined.Phone
                    )

                    ProfileField(
                        label = "Date of Birth",
                        value = "January 1, 1990",
                        icon = Icons.Outlined.CalendarMonth
                    )

                    ProfileField(
                        label = "Gender",
                        value = user?.gender ?: "Male",
                        icon = Icons.Outlined.Person
                    )

                    ProfileField(
                        label = "National ID",
                        value = user?.nationalId ?: "1234567890123456",
                        icon = Icons.Outlined.Badge
                    )
                }

                // Address Section
                ProfileSection(
                    title = "Address",
                    icon = Icons.Default.Home
                ) {
                    ProfileField(
                        label = "Home Address",
                        value = user?.address ?: "Jl. Sudirman No. 123, Jakarta Selatan, DKI Jakarta",
                        icon = Icons.Outlined.Home
                    )
                }

                // Medical Information Section
                ProfileSection(
                    title = "Medical Information",
                    icon = Icons.Default.MedicalServices
                ) {
                    ProfileField(
                        label = "Blood Type",
                        value = user?.bloodType ?: "O+",
                        icon = Icons.Outlined.BloodtypeOutlined
                    )

                    ProfileField(
                        label = "Medical Conditions",
                        value = user?.medicalConditions?.takeIf { it.isNotEmpty() } ?: "None",
                        icon = Icons.Outlined.MedicalInformation
                    )

                    ProfileField(
                        label = "Disabilities",
                        value = user?.disabilities?.takeIf { it.isNotEmpty() } ?: "None",
                        icon = Icons.Outlined.Accessible
                    )
                }

                // Emergency Contact Section
                ProfileSection(
                    title = "Emergency Contact",
                    icon = Icons.Default.ContactPhone
                ) {
                    ProfileField(
                        label = "Name",
                        value = user?.emergencyContact?.name ?: "Jane Doe",
                        icon = Icons.Outlined.Person
                    )

                    ProfileField(
                        label = "Relationship",
                        value = user?.emergencyContact?.relationship ?: "Spouse",
                        icon = Icons.Outlined.People
                    )

                    ProfileField(
                        label = "Phone Number",
                        value = user?.emergencyContact?.phoneNumber ?: "+62 812-9876-5432",
                        icon = Icons.Outlined.Phone
                    )
                }

                // Household Information Section
                ProfileSection(
                    title = "Household Information",
                    icon = Icons.Default.Group
                ) {
                    ProfileField(
                        label = "Number of Household Members",
                        value = user?.householdMembers?.toString() ?: "4",
                        icon = Icons.Outlined.Groups
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Logout Button
                SecondaryButton(
                    text = "Log Out",
                    onClick = onLogout,
                    icon = Icons.Default.Logout
                )
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