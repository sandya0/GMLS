package com.example.gmls.ui.screens.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.gmls.R
import com.example.gmls.domain.model.Disaster
import com.example.gmls.domain.model.DisasterType
import com.example.gmls.domain.model.getDisplayName
import com.example.gmls.domain.model.displayName
import com.example.gmls.domain.model.User
import com.example.gmls.ui.components.DisasterTypeChip
import com.example.gmls.ui.theme.*
import com.example.gmls.ui.viewmodels.AdminViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMapScreen(
    disasters: List<Disaster>,
    users: List<User>,
    modifier: Modifier = Modifier,
    drawerState: DrawerState,
    adminViewModel: AdminViewModel = hiltViewModel(),
    onBackClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Map state
    var selectedDisaster by remember { mutableStateOf<Disaster?>(null) }
    var selectedUser by remember { mutableStateOf<User?>(null) }
    var showUserLocations by remember { mutableStateOf(true) }
    var showDisasterLocations by remember { mutableStateOf(true) }
    var showOnlineUsersOnly by remember { mutableStateOf(false) }
    var selectedDisasterType by remember { mutableStateOf<DisasterType?>(null) }
    var showControlPanel by remember { mutableStateOf(true) }
    
    val defaultLocation = LatLng(-6.200000, 106.816666) // Jakarta
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 10f)
    }

    // Get filtered data
    val filteredDisasters = if (selectedDisasterType != null) {
        disasters.filter { it.type == selectedDisasterType }
    } else {
        disasters
    }

    val displayedUsers = if (showOnlineUsersOnly) {
        adminViewModel.getOnlineUsers()
    } else {
        adminViewModel.getUsersWithRecentLocations()
    }

    // Start observing user locations when screen loads
    LaunchedEffect(Unit) {
        adminViewModel.startObservingUserLocations()
    }

    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                mapType = MapType.NORMAL,
                isTrafficEnabled = false
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false,
                mapToolbarEnabled = true,
                compassEnabled = true,
                scrollGesturesEnabled = true,
                zoomGesturesEnabled = true,
                tiltGesturesEnabled = true,
                rotationGesturesEnabled = true
            )
        ) {
            // Disaster markers
            if (showDisasterLocations) {
                filteredDisasters.forEach { disaster ->
                    val position = LatLng(disaster.latitude, disaster.longitude)
                    
                    Marker(
                        state = MarkerState(position = position),
                        title = disaster.title,
                        snippet = "${disaster.type.name} - ${disaster.location}",
                        icon = BitmapDescriptorFactory.defaultMarker(
                            when (disaster.type) {
                                DisasterType.EARTHQUAKE -> BitmapDescriptorFactory.HUE_ORANGE
                                DisasterType.FLOOD -> BitmapDescriptorFactory.HUE_BLUE
                                DisasterType.WILDFIRE -> BitmapDescriptorFactory.HUE_RED
                                DisasterType.LANDSLIDE -> BitmapDescriptorFactory.HUE_YELLOW
                                DisasterType.VOLCANO -> BitmapDescriptorFactory.HUE_MAGENTA
                                DisasterType.TSUNAMI -> BitmapDescriptorFactory.HUE_CYAN
                                DisasterType.HURRICANE -> BitmapDescriptorFactory.HUE_VIOLET
                                DisasterType.TORNADO -> BitmapDescriptorFactory.HUE_ROSE
                                DisasterType.OTHER -> BitmapDescriptorFactory.HUE_GREEN
                            }
                        ),
                        onClick = {
                            selectedDisaster = disaster
                            selectedUser = null
                            scope.launch {
                                cameraPositionState.animate(
                                    update = com.google.android.gms.maps.CameraUpdateFactory.newCameraPosition(
                                        CameraPosition.fromLatLngZoom(position, 15f)
                                    ),
                                    durationMs = 500
                                )
                            }
                            true
                        }
                    )
                }
            }

            // User markers
            if (showUserLocations) {
                displayedUsers.forEach { user ->
                    if (user.latitude != null && user.longitude != null) {
                        val position = LatLng(user.latitude, user.longitude)
                        
                        Marker(
                            state = MarkerState(position = position),
                            title = user.fullName,
                            snippet = "Pengguna: ${user.email}",
                            icon = BitmapDescriptorFactory.defaultMarker(
                                if (adminViewModel.isUserOnline(user.id)) 
                                    BitmapDescriptorFactory.HUE_GREEN 
                                else 
                                    BitmapDescriptorFactory.HUE_AZURE
                            ),
                            onClick = {
                                selectedUser = user
                                selectedDisaster = null
                                scope.launch {
                                    cameraPositionState.animate(
                                        update = com.google.android.gms.maps.CameraUpdateFactory.newCameraPosition(
                                            CameraPosition.fromLatLngZoom(position, 15f)
                                        ),
                                        durationMs = 500
                                    )
                                }
                                true
                            }
                        )
                    }
                }
            }
        }

        // Back button (if provided)
        onBackClick?.let { backClick ->
            FloatingActionButton(
                onClick = backClick,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .zIndex(10f),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                                            contentDescription = "Kembali"
                )
            }
        }

        // Control panel toggle button
        FloatingActionButton(
            onClick = { showControlPanel = !showControlPanel },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .zIndex(10f),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(
                imageVector = if (showControlPanel) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                contentDescription = if (showControlPanel) "Sembunyikan Kontrol" else "Tampilkan Kontrol"
            )
        }

        // Enhanced Control panel - Fixed position, non-swipeable
        AnimatedVisibility(
            visible = showControlPanel,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = if (onBackClick != null) 80.dp else 16.dp, top = 16.dp)
                .zIndex(5f)
        ) {
            Card(
                modifier = Modifier.widthIn(max = 280.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                            text = stringResource(R.string.controls_label),
                    style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        thickness = 0.5.dp
                )

                // Statistics
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatItem(
                            label = stringResource(R.string.online_label),
                        value = adminViewModel.getOnlineUsers().size.toString(),
                            color = Success,
                            icon = Icons.Default.Circle
                    )
                    StatItem(
                            label = stringResource(R.string.disasters_layer),
                        value = disasters.size.toString(),
                            color = Warning,
                            icon = Icons.Default.Warning
                    )
                }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        thickness = 0.5.dp
                    )

                    // Layer Controls
                    Text(
                        text = stringResource(R.string.layers_label),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Disaster locations toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                                imageVector = Icons.Default.Warning,
                        contentDescription = null,
                                tint = Red,
                                modifier = Modifier.size(16.dp)
                    )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.disasters_layer),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    Switch(
                            checked = showDisasterLocations,
                            onCheckedChange = { showDisasterLocations = it },
                            modifier = Modifier.size(32.dp)
                    )
                }

                    // User locations toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                                imageVector = Icons.Default.People,
                        contentDescription = null,
                                tint = AccentBlue,
                                modifier = Modifier.size(16.dp)
                    )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.users_layer),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    Switch(
                            checked = showUserLocations,
                            onCheckedChange = { showUserLocations = it },
                            modifier = Modifier.size(32.dp)
                    )
                }

                    // Online users only toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                                imageVector = Icons.Default.Circle,
                        contentDescription = null,
                                tint = Success,
                        modifier = Modifier.size(16.dp)
                    )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.online_only_filter),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    Switch(
                        checked = showOnlineUsersOnly,
                            onCheckedChange = { showOnlineUsersOnly = it },
                            modifier = Modifier.size(32.dp)
                    )
                }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        thickness = 0.5.dp
                    )

                // Disaster type filter
                    Text(
                        text = stringResource(R.string.filter_label),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Disaster type chips - more compact
                    val disasterTypes = DisasterType.values().toList()
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row {
                        FilterChip(
                            selected = selectedDisasterType == null,
                            onClick = { selectedDisasterType = null },
                                label = { 
                                    Text(
                                        stringResource(R.string.all_filter),
                                        style = MaterialTheme.typography.labelSmall
                                    ) 
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.SelectAll,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                modifier = Modifier.height(28.dp)
                            )
                        }
                        
                        disasterTypes.chunked(2).forEach { rowTypes ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                rowTypes.forEach { type ->
                        FilterChip(
                                        selected = selectedDisasterType == type,
                            onClick = { 
                                            selectedDisasterType = if (selectedDisasterType == type) null else type 
                                        },
                                        label = { 
                                            Text(
                                                text = type.getDisplayName(),
                                                style = MaterialTheme.typography.labelSmall
                                            ) 
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(28.dp)
                                    )
                                }
                                // Fill remaining space if odd number
                                if (rowTypes.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Selected item details - Enhanced
        AnimatedVisibility(
            visible = selectedDisaster != null || selectedUser != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .zIndex(5f)
        ) {
            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .widthIn(max = 380.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedDisaster != null) "Detail Bencana" else "Detail Pengguna",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(
                            onClick = {
                                selectedDisaster = null
                                selectedUser = null
                            }
                        ) {
                            Icon(
                                Icons.Default.Close, 
                                                                    contentDescription = "Tutup",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                    selectedDisaster?.let { disaster ->
                        DisasterDetailsContent(disaster)
                    }

                    selectedUser?.let { user ->
                        UserDetailsContent(user, adminViewModel.isUserOnline(user.id))
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    color: Color,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = value,
                style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DisasterDetailsContent(disaster: Disaster) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = disaster.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = disaster.location,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
        DisasterTypeChip(
                text = when (disaster.type) {
                    DisasterType.EARTHQUAKE -> "Gempa Bumi"
                    DisasterType.FLOOD -> "Banjir"
                    DisasterType.WILDFIRE -> "Kebakaran Hutan"
                    DisasterType.LANDSLIDE -> "Tanah Longsor"
                    DisasterType.VOLCANO -> "Gunung Berapi"
                    DisasterType.TSUNAMI -> "Tsunami"
                    DisasterType.HURRICANE -> "Badai"
                    DisasterType.TORNADO -> "Tornado"
                    DisasterType.OTHER -> "Lainnya"
                },
                selected = false,
                onClick = { },
                modifier = Modifier.padding(vertical = 4.dp)
        )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Status:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = when (disaster.status) {
                    Disaster.Status.REPORTED -> "Dilaporkan"
                    Disaster.Status.VERIFIED -> "Terverifikasi"
                    Disaster.Status.IN_PROGRESS -> "Sedang Berlangsung"
                    Disaster.Status.RESOLVED -> "Teratasi"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = when (disaster.status) {
                    Disaster.Status.REPORTED -> Warning
                    Disaster.Status.VERIFIED -> AccentBlue
                    Disaster.Status.IN_PROGRESS -> Red
                    Disaster.Status.RESOLVED -> Success
                }
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.affected_label),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.people_affected, disaster.affectedCount),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }
        
        Text(
            text = "Dilaporkan: ${SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(disaster.timestamp))}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun UserDetailsContent(user: User, isOnline: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = user.fullName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                color = if (isOnline) Success else MaterialTheme.colorScheme.outline,
                shape = CircleShape,
                modifier = Modifier.size(8.dp)
            ) {}
        }
        
        Text(
            text = user.email,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
        Text(
                text = "Peran:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
        )
        Text(
                text = user.role.uppercase(),
                style = MaterialTheme.typography.bodyMedium,
                color = if (user.role == "admin") Warning else MaterialTheme.colorScheme.onSurface
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Status:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                                    text = if (isOnline) "Daring" else "Luring",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isOnline) Success else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        if (user.isVerified) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Verified,
                                                contentDescription = "Terverifikasi",
                    tint = Success,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.verified_user_label),
                    style = MaterialTheme.typography.bodySmall,
                    color = Success
            )
            }
        }
    }
} 
