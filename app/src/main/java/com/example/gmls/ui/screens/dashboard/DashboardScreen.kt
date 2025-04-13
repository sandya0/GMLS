package com.example.gmls.ui.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.gmls.domain.model.Disaster
import com.example.gmls.domain.model.DisasterType
import com.example.gmls.domain.model.User
import com.example.gmls.ui.components.DisasterReportFAB
import com.example.gmls.ui.components.DisasterTypeChip
import com.example.gmls.ui.components.EmergencyAlertCard
import com.example.gmls.ui.components.EmergencySeverity
import com.example.gmls.ui.components.StatusCard
import com.example.gmls.ui.theme.Info
import com.example.gmls.ui.theme.Red
import com.example.gmls.ui.theme.Success
import com.example.gmls.ui.theme.Warning
import com.example.gmls.ui.viewmodels.ProfileViewModel
import kotlinx.coroutines.launch
import java.util.*
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    disasters: List<Disaster>,
    onDisasterClick: (Disaster) -> Unit,
    onProfileClick: () -> Unit,
    onMenuClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onReportDisaster: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profileState by viewModel.profileState.collectAsState()
    val user = profileState.user
    var selectedDisasterType by remember { mutableStateOf<DisasterType?>(null) }
    val filteredDisasters = remember(disasters, selectedDisasterType) {
        if (selectedDisasterType == null) disasters else disasters.filter { it.type == selectedDisasterType }
    }
    val disasterTypes = remember { DisasterType.values().toList() }

    Scaffold(
        topBar = {
            DashboardTopAppBar(
                onMenuClick = onMenuClick,
                onSearchClick = {},
                onNotificationsClick = onNotificationsClick,
                onProfileClick = onProfileClick
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Report Disaster") },
                icon = { Icon(Icons.Default.Add, contentDescription = "Report Disaster") },
                onClick = onReportDisaster,
                containerColor = Red,
                contentColor = Color.White
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Profile Section as Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                colors = CardDefaults.cardColors(containerColor = Red.copy(alpha = 0.08f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .semantics { contentDescription = "Profile section. Tap to view or edit profile." }
                        .clickable(onClick = onProfileClick)
                ) {
                    // Profile Picture
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user?.fullName?.take(2)?.uppercase() ?: "U",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        // Badge for verification
                        if (user != null) {
                            Icon(
                                imageVector = Icons.Outlined.VerifiedUser,
                                contentDescription = "Verified profile",
                                tint = Success,
                                modifier = Modifier
                                    .size(20.dp)
                                    .align(Alignment.BottomEnd)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Welcome back,",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = user?.fullName ?: "User",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onLogoutClick, modifier = Modifier.semantics { contentDescription = "Logout" }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            // Status indicators as cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DashboardStatusCard(
                    title = "Reported",
                    count = disasters.count { it.status.name == "REPORTED" },
                    icon = Icons.Default.Flag,
                    color = Warning,
                    tooltip = "Disasters you have reported.",
                    modifier = Modifier.weight(1f)
                )
                DashboardStatusCard(
                    title = "Assisted",
                    count = disasters.count { it.status.name == "RESOLVED" },
                    icon = Icons.Default.CheckCircle,
                    color = Success,
                    tooltip = "Disasters you have assisted.",
                    modifier = Modifier.weight(1f)
                )
                DashboardStatusCard(
                    title = "Near You",
                    count = disasters.count { it.status.name == "IN_PROGRESS" },
                    icon = Icons.Default.LocationOn,
                    color = Info,
                    tooltip = "Disasters near your location.",
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            // Emergency alert if there's a flood disaster
            disasters.firstOrNull { it.type.name == "FLOOD" }?.let { floodDisaster ->
                EmergencyAlertCard(
                    title = floodDisaster.title,
                    message = floodDisaster.description,
                    severity = EmergencySeverity.HIGH,
                    timestamp = floodDisaster.formattedTimestamp,
                    onDismiss = { /* TODO: Handle dismiss */ }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            // Disaster type filter
            Text(
                text = "Filter by Disaster Type",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    DisasterTypeChip(
                        text = "All",
                        selected = selectedDisasterType == null,
                        onClick = { selectedDisasterType = null }
                    )
                }
                items(disasterTypes) { type ->
                    DisasterTypeChip(
                        text = type.name,
                        selected = selectedDisasterType == type,
                        onClick = { selectedDisasterType = type }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            // Recent disasters
            Text(
                text = "Recent Disasters",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            if (filteredDisasters.isEmpty()) {
                Text(
                    text = "No disasters reported in this category",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredDisasters) { disaster ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(animationSpec = tween(500)),
                            exit = fadeOut(animationSpec = tween(300))
                        ) {
                            DisasterCard(
                                disaster = disaster,
                                onClick = { onDisasterClick(disaster) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardTopAppBar(
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Emergency",
                    style = MaterialTheme.typography.titleLarge,
                    color = Red
                )
                Text(
                    text = "Response",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu"
                )
            }
        },
        actions = {
            IconButton(onClick = onSearchClick) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            }

            IconButton(onClick = onNotificationsClick) {
                BadgedBox(
                    badge = {
                        Badge {
                            Text("3")
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications"
                    )
                }
            }
        }
    )
}

@Composable
fun DashboardStatusCard(
    title: String,
    count: Int,
    icon: ImageVector,
    color: Color,
    tooltip: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(horizontal = 4.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Outlined.Info, contentDescription = tooltip, tint = color, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = title, style = MaterialTheme.typography.bodyMedium, color = color)
            Text(text = count.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisasterCard(
    disaster: Disaster,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val disasterColor = when (disaster.type) {
        DisasterType.EARTHQUAKE -> Color(0xFFE57373)
        DisasterType.FLOOD -> Color(0xFF64B5F6)
        DisasterType.WILDFIRE -> Color(0xFFFFB74D)
        DisasterType.LANDSLIDE -> Color(0xFF8D6E63)
        DisasterType.VOLCANO -> Color(0xFFFF8A65)
        DisasterType.TSUNAMI -> Color(0xFF4FC3F7)
        DisasterType.HURRICANE -> Color(0xFF9575CD)
        DisasterType.TORNADO -> Color(0xFF7986CB)
        DisasterType.OTHER -> Color(0xFF90A4AE)
    }

    val disasterIcon = when (disaster.type) {
        DisasterType.EARTHQUAKE -> Icons.Filled.Bolt
        DisasterType.FLOOD -> Icons.Filled.Water
        DisasterType.WILDFIRE -> Icons.Filled.LocalFireDepartment
        DisasterType.LANDSLIDE -> Icons.Filled.Terrain
        DisasterType.VOLCANO -> Icons.Filled.Volcano
        DisasterType.TSUNAMI -> Icons.Filled.Waves
        DisasterType.HURRICANE -> Icons.Filled.Storm
        DisasterType.TORNADO -> Icons.Filled.AirlineSeatFlatAngled
        DisasterType.OTHER -> Icons.Filled.Warning
    }

    ElevatedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(disasterColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = disasterIcon,
                    contentDescription = null,
                    tint = disasterColor,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = disaster.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = disaster.location,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = disaster.formattedTimestamp,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Icon(
                        imageVector = Icons.Filled.People,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "${disaster.affectedCount} affected",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            StatusBadge(disaster.status)
        }
    }
}

@Composable
fun StatusBadge(status: Disaster.Status) {
    val (backgroundColor, textColor, label) = when (status) {
        Disaster.Status.REPORTED -> Triple(Warning.copy(alpha = 0.2f), Warning, "Reported")
        Disaster.Status.VERIFIED -> Triple(Info.copy(alpha = 0.2f), Info, "Verified")
        Disaster.Status.IN_PROGRESS -> Triple(Info.copy(alpha = 0.2f), Info, "In Progress")
        Disaster.Status.RESOLVED -> Triple(Success.copy(alpha = 0.2f), Success, "Resolved")
    }

    Box(
        modifier = Modifier
            .background(
                color = backgroundColor,
                shape = MaterialTheme.shapes.small
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = textColor
        )
    }
}