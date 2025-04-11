package com.example.gmls.ui.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.gmls.domain.model.Disaster
import com.example.gmls.domain.model.DisasterType
import com.example.gmls.ui.components.DisasterReportFAB
import com.example.gmls.ui.components.DisasterTypeChip
import com.example.gmls.ui.components.EmergencyAlertCard
import com.example.gmls.ui.components.EmergencySeverity
import com.example.gmls.ui.components.StatusCard
import com.example.gmls.ui.theme.Info
import com.example.gmls.ui.theme.Red
import com.example.gmls.ui.theme.Success
import com.example.gmls.ui.theme.Warning
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    disasters: List<Disaster>,
    onDisasterClick: (Disaster) -> Unit,
    onReportDisaster: () -> Unit,
    onSearchClick: () -> Unit,
    onFilterChange: (DisasterType) -> Unit,
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedDisasterType by remember { mutableStateOf<DisasterType?>(null) }

    val filteredDisasters = remember(disasters, selectedDisasterType) {
        if (selectedDisasterType == null) {
            disasters
        } else {
            disasters.filter { it.type == selectedDisasterType }
        }
    }

    val disasterTypes = remember { DisasterType.values().toList() }

    Scaffold(
        topBar = {
            DashboardTopAppBar(
                onMenuClick = { scope.launch { drawerState.open() } },
                onSearchClick = onSearchClick,
                onNotificationsClick = onNotificationsClick,
                onProfileClick = onProfileClick
            )
        },
        floatingActionButton = {
            DisasterReportFAB(
                onClick = onReportDisaster
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Main content
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Emergency alert (if any)
                item {
                    EmergencyAlertCard(
                        title = "Flood Warning",
                        message = "Heavy rainfall expected in your area. Stay vigilant and prepare for possible evacuation.",
                        severity = EmergencySeverity.HIGH,
                        timestamp = "10 min ago",
                        onDismiss = { /* TODO: Handle dismiss */ }
                    )
                }

                // User Status Card
                item {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(Red),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "JD",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = Color.White
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column {
                                    Text(
                                        text = "Welcome back,",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = "John Doe",
                                        style = MaterialTheme.typography.headlineMedium
                                    )
                                }

                                Spacer(modifier = Modifier.weight(1f))

                                IconButton(
                                    onClick = onProfileClick,
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Settings,
                                        contentDescription = "Settings"
                                    )
                                }
                            }

                            Text(
                                text = "Your Disaster Response Status",
                                style = MaterialTheme.typography.titleMedium
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                StatusIndicator(
                                    count = 3,
                                    label = "Reported",
                                    color = Info,
                                    icon = Icons.Filled.Report,
                                    modifier = Modifier.weight(1f)
                                )

                                StatusIndicator(
                                    count = 7,
                                    label = "Assisted",
                                    color = Success,
                                    icon = Icons.Filled.VolunteerActivism,
                                    modifier = Modifier.weight(1f)
                                )

                                StatusIndicator(
                                    count = 2,
                                    label = "Near You",
                                    color = Warning,
                                    icon = Icons.Filled.LocationOn,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Disaster type filter
                item {
                    Column {
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
                                    onClick = {
                                        selectedDisasterType = null
                                        onFilterChange(DisasterType.EARTHQUAKE) // Placeholder, since All isn't a DisasterType
                                    }
                                )
                            }

                            items(disasterTypes) { type ->
                                DisasterTypeChip(
                                    text = type.displayName,
                                    selected = selectedDisasterType == type,
                                    onClick = {
                                        selectedDisasterType = type
                                        onFilterChange(type)
                                    }
                                )
                            }
                        }
                    }
                }

                // Recent disasters section
                item {
                    Text(
                        text = "Recent Disasters",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                // Disaster list
                if (filteredDisasters.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No disasters reported in this category",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(filteredDisasters) { disaster ->
                        DisasterCard(
                            disaster = disaster,
                            onClick = { onDisasterClick(disaster) }
                        )
                    }
                }
            }

            // Loading indicator
            AnimatedVisibility(
                visible = isLoading,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Red)
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
fun StatusIndicator(
    count: Int,
    label: String,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleLarge
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
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