package com.example.gmls.ui.screens.map

import android.Manifest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.gmls.domain.model.Disaster
import com.example.gmls.domain.model.DisasterType
import com.example.gmls.ui.components.DisasterTypeChip
import com.example.gmls.ui.theme.Red
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.android.gms.maps.CameraUpdateFactory

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    disasters: List<Disaster>,
    onDisasterClick: (Disaster) -> Unit,
    onBackClick: () -> Unit,
    onFilterChange: (DisasterType?) -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedDisasterType by remember { mutableStateOf<DisasterType?>(null) }
    var selectedDisaster by remember { mutableStateOf<Disaster?>(null) }
    var isFilterExpanded by remember { mutableStateOf(false) }
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    
    val defaultLocation = LatLng(-6.200000, 106.816666) // Jakarta

    // Location permissions state
    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // Observe permission state
    LaunchedEffect(locationPermissions.allPermissionsGranted) {
        if (locationPermissions.allPermissionsGranted) {
            // Get current location when permission is granted
            try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                val location = fusedLocationClient.lastLocation.await()
                location?.let {
                    currentLocation = LatLng(it.latitude, it.longitude)
                }
            } catch (e: Exception) {
                // Handle location error
            }
        }
    }

    val filteredDisasters = remember(disasters, selectedDisasterType) {
        if (selectedDisasterType == null) {
            disasters
        } else {
            disasters.filter { it.type == selectedDisasterType }
        }
    }

    val disasterTypes = remember { DisasterType.values().toList() }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            currentLocation ?: defaultLocation,
            15f
        )
    }

    // Animate camera to user location when it becomes available
    LaunchedEffect(currentLocation) {
        currentLocation?.let { location ->
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(location, 15f)
            )
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        if (locationPermissions.allPermissionsGranted) {
            // Show map when permission is granted
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = true)
            ) {
                // Add markers for disasters here
            }
        } else {
            // Show permission request UI
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOff,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Location Permission Required",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Please grant location permission to view the map and nearby disasters",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { locationPermissions.launchMultiplePermissionRequest() }
                ) {
                    Text("Grant Permission")
                }
            }
        }

        // Top app bar
        CenterAlignedTopAppBar(
            title = { Text("Disaster Map") },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Go back"
                    )
                }
            },
            actions = {
                IconButton(onClick = { isFilterExpanded = !isFilterExpanded }) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter disasters"
                    )
                }

                IconButton(onClick = { /* Refresh map */ }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh map"
                    )
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            ),
            modifier = Modifier.shadow(4.dp)
        )

        // Filter panel
        AnimatedVisibility(
            visible = isFilterExpanded,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 64.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Filter by Disaster Type",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DisasterTypeChip(
                            text = "All",
                            selected = selectedDisasterType == null,
                            onClick = {
                                selectedDisasterType = null
                                onFilterChange(null)
                            }
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        disasterTypes.take(3).forEach { type ->
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        disasterTypes.drop(3).take(3).forEach { type ->
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        disasterTypes.drop(6).forEach { type ->
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
        }

        // Bottom disaster info panel
        AnimatedVisibility(
            visible = selectedDisaster != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            selectedDisaster?.let { disaster ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = when(disaster.type) {
                                        DisasterType.EARTHQUAKE -> Icons.Filled.Bolt
                                        DisasterType.FLOOD -> Icons.Filled.Water
                                        DisasterType.WILDFIRE -> Icons.Filled.LocalFireDepartment
                                        DisasterType.LANDSLIDE -> Icons.Filled.Terrain
                                        DisasterType.VOLCANO -> Icons.Filled.Volcano
                                        DisasterType.TSUNAMI -> Icons.Filled.Waves
                                        DisasterType.HURRICANE -> Icons.Filled.Storm
                                        DisasterType.TORNADO -> Icons.Filled.AirlineSeatFlatAngled
                                        DisasterType.OTHER -> Icons.Filled.Warning
                                    },
                                    contentDescription = null,
                                    tint = Red
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = disaster.title,
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Red.copy(alpha = 0.1f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = disaster.type.displayName,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Red
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = disaster.description,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            Text(
                                text = disaster.location,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            Text(
                                text = disaster.formattedTimestamp,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { onDisasterClick(disaster) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Red
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("View Details")
                        }
                    }
                }
            }
        }

        // Map controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    val currentZoom = cameraPositionState.position.zoom
                    scope.launch {
                        cameraPositionState.animate(
                            update = CameraUpdateFactory.zoomTo(currentZoom + 1)
                        )
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Zoom in",
                    modifier = Modifier.size(20.dp)
                )
            }

            FloatingActionButton(
                onClick = {
                    val currentZoom = cameraPositionState.position.zoom
                    scope.launch {
                        cameraPositionState.animate(
                            update = CameraUpdateFactory.zoomTo(currentZoom - 1)
                        )
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Zoom out",
                    modifier = Modifier.size(20.dp)
                )
            }

            FloatingActionButton(
                onClick = {
                    val target = currentLocation ?: defaultLocation
                    scope.launch {
                        cameraPositionState.animate(
                            update = CameraUpdateFactory.newLatLngZoom(target, 15f)
                        )
                    }
                },
                containerColor = Red,
                contentColor = Color.White,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "My location",
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Loading indicator
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Red)
            }
        }
    }
}