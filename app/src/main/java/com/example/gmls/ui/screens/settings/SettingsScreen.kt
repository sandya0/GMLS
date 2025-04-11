package com.example.gmls.ui.screens.settings

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.gmls.ui.theme.Red

/**
 * Screen for app settings
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var locationPermissionGranted by remember { mutableStateOf(true) }
    var darkModeEnabled by remember { mutableStateOf(false) }
    var dataUsageRestricted by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Go back"
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
            // Notifications Section
            SettingsSection(title = "Notifications", icon = Icons.Default.Notifications) {
                // Enable/disable notifications
                SettingsSwitch(
                    title = "Enable Notifications",
                    description = "Receive alerts about disasters near you",
                    icon = Icons.Outlined.NotificationsActive,
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )

                // Emergency alerts
                SettingsSwitch(
                    title = "Emergency Alerts",
                    description = "Receive critical emergency alerts",
                    icon = Icons.Outlined.Warning,
                    checked = true,
                    enabled = notificationsEnabled,
                    onCheckedChange = { /* Cannot be disabled for safety reasons */ }
                )

                // Disaster notifications
                SettingsSwitch(
                    title = "Disaster Reports",
                    description = "Updates on reported disasters",
                    icon = Icons.Outlined.Report,
                    checked = true,
                    enabled = notificationsEnabled,
                    onCheckedChange = { /* Implementation */ }
                )

                // System notifications
                SettingsSwitch(
                    title = "System Notifications",
                    description = "App updates and profile changes",
                    icon = Icons.Outlined.Settings,
                    checked = true,
                    enabled = notificationsEnabled,
                    onCheckedChange = { /* Implementation */ }
                )
            }

            // Location Section
            SettingsSection(title = "Location", icon = Icons.Default.LocationOn) {
                // Location permission
                SettingsSwitch(
                    title = "Location Access",
                    description = "Allow app to access your location",
                    icon = Icons.Outlined.MyLocation,
                    checked = locationPermissionGranted,
                    onCheckedChange = { locationPermissionGranted = it }
                )

                // Background location
                SettingsSwitch(
                    title = "Background Location",
                    description = "Allow location tracking in background",
                    icon = Icons.Outlined.LocationSearching,
                    checked = false,
                    enabled = locationPermissionGranted,
                    onCheckedChange = { /* Implementation */ }
                )

                // Location accuracy
                SettingsItem(
                    title = "Location Accuracy",
                    description = "High (uses more battery)",
                    icon = Icons.Outlined.Tune,
                    onClick = { /* Show location accuracy dialog */ }
                )
            }

            // Appearance Section
            SettingsSection(title = "Appearance", icon = Icons.Default.Palette) {
                // Dark mode
                SettingsSwitch(
                    title = "Dark Theme",
                    description = "Use dark theme for the app",
                    icon = Icons.Outlined.DarkMode,
                    checked = darkModeEnabled,
                    onCheckedChange = { darkModeEnabled = it }
                )
            }

            // Data Usage Section
            SettingsSection(title = "Data Usage", icon = Icons.Default.DataUsage) {
                // Restrict data usage
                SettingsSwitch(
                    title = "Restrict Data Usage",
                    description = "Use less mobile data (reduced quality images)",
                    icon = Icons.Outlined.DataSaverOn,
                    checked = dataUsageRestricted,
                    onCheckedChange = { dataUsageRestricted = it }
                )

                // Offline mode
                SettingsItem(
                    title = "Offline Mode",
                    description = "Manage offline data storage",
                    icon = Icons.Outlined.OfflinePin,
                    onClick = { /* Show offline settings */ }
                )
            }

            // Account Section
            SettingsSection(title = "Account", icon = Icons.Default.AccountCircle) {
                // Edit profile
                SettingsItem(
                    title = "Edit Profile",
                    description = "Update your personal information",
                    icon = Icons.Outlined.Edit,
                    onClick = { /* Navigate to profile edit */ }
                )

                // Change password
                SettingsItem(
                    title = "Change Password",
                    description = "Update your account password",
                    icon = Icons.Outlined.Lock,
                    onClick = { /* Show change password dialog */ }
                )

                // Logout
                SettingsItem(
                    title = "Logout",
                    description = "Sign out from your account",
                    icon = Icons.Outlined.Logout,
                    onClick = onLogout,
                    titleColor = MaterialTheme.colorScheme.error
                )
            }

            // About Section
            SettingsSection(title = "About", icon = Icons.Default.Info) {
                // App version
                SettingsInfo(
                    title = "App Version",
                    value = "1.0.0 (1)"
                )

                // Terms of service
                SettingsItem(
                    title = "Terms of Service",
                    description = "View our terms of service",
                    icon = Icons.Outlined.Description,
                    onClick = { /* Open terms of service */ }
                )

                // Privacy policy
                SettingsItem(
                    title = "Privacy Policy",
                    description = "View our privacy policy",
                    icon = Icons.Outlined.PrivacyTip,
                    onClick = { /* Open privacy policy */ }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * A section header for settings
 */
@Composable
fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Section header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Red
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = Red
            )
        }

        // Section content in a Card
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
                modifier = Modifier.padding(vertical = 8.dp),
                content = content
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * A settings item with a switch
 */
@Composable
fun SettingsSwitch(
    title: String,
    description: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = if (enabled) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Red,
                checkedTrackColor = Red.copy(alpha = 0.5f),
                checkedBorderColor = Red,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline
            )
        )
    }
}

/**
 * A clickable settings item
 */
@Composable
fun SettingsItem(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    titleColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = titleColor
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

/**
 * A non-clickable settings info item
 */
@Composable
fun SettingsInfo(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(40.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}