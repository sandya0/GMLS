package com.example.gmls.ui.screens.map

import android.Manifest
import android.graphics.Bitmap
import android.graphics.Canvas
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.gmls.domain.model.Disaster
import com.example.gmls.domain.model.DisasterType
import com.example.gmls.domain.model.getDisplayName
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.example.gmls.ui.components.GlobalSnackbarHost
import androidx.compose.material3.SnackbarHostState
import com.google.android.gms.maps.model.BitmapDescriptor
import androidx.compose.ui.graphics.toArgb
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MapStyleOptions
import kotlinx.coroutines.delay
import androidx.compose.runtime.DisposableEffect
import com.example.gmls.domain.model.User
import androidx.compose.ui.res.stringResource
import com.example.gmls.R

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    disasters: List<Disaster>,
    onDisasterClick: (Disaster) -> Unit,
    onBackClick: () -> Unit,
    onFilterChange: (DisasterType?) -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    successMessage: String? = null,
    focusDisaster: Disaster? = null,
    users: List<User>? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedDisasterType by remember { mutableStateOf<DisasterType?>(null) }
    var selectedDisaster by remember { mutableStateOf<Disaster?>(focusDisaster) }
    var isFilterExpanded by remember { mutableStateOf(false) }
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    var mapLoaded by remember { mutableStateOf(false) }
    
    val defaultLocation = LatLng(-6.200000, 106.816666) // Jakarta
    
    // Location permissions state
    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // Map properties
    val mapProperties by remember(locationPermissions.allPermissionsGranted) {
        mutableStateOf(
            MapProperties(
                isMyLocationEnabled = locationPermissions.allPermissionsGranted,
                mapType = MapType.NORMAL,
                isTrafficEnabled = false
            )
        )
    }

    // Map UI settings
    val mapUiSettings by remember {
        mutableStateOf(
            MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false,
                mapToolbarEnabled = true,
                compassEnabled = true,
                indoorLevelPickerEnabled = false,
                rotationGesturesEnabled = true,
                tiltGesturesEnabled = true,
                scrollGesturesEnabled = true,
                zoomGesturesEnabled = true
            )
        )
    }

    // Create CameraPositionState for controlling map camera
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            focusDisaster?.let { LatLng(it.latitude, it.longitude) } ?: defaultLocation,
            focusDisaster?.let { 15f } ?: 10f
        )
    }

    // Effect to update camera position and selection when focusDisaster changes
    LaunchedEffect(focusDisaster) {
        focusDisaster?.let { disaster ->
            selectedDisaster = disaster
            // Ensure we're on the main thread for camera updates
            delay(300) // Small delay to ensure smooth transition
            cameraPositionState.animate(
                update = CameraUpdateFactory.newCameraPosition(
                    CameraPosition.fromLatLngZoom(
                        LatLng(disaster.latitude, disaster.longitude),
                        15f
                    )
                ),
                durationMs = 1000
            )
            // Clear any active filters to ensure the focused disaster is visible
            selectedDisasterType = null
            onFilterChange(null)
        }
    }

    // Observe permission state and get location
    LaunchedEffect(locationPermissions.allPermissionsGranted) {
        if (locationPermissions.allPermissionsGranted) {
            try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                val location = fusedLocationClient.lastLocation.await()
                location?.let {
                    currentLocation = LatLng(it.latitude, it.longitude)
                    // Animate camera to user location
                    cameraPositionState.animate(
                        update = CameraUpdateFactory.newLatLngZoom(
                            LatLng(it.latitude, it.longitude),
                            15f
                        ),
                        durationMs = 1000
                    )
                }
            } catch (e: Exception) {
                // Handle location error
            }
        }
    }

    // Handle map loading timeout
    LaunchedEffect(Unit) {
        delay(5000) // Wait for 5 seconds
        if (!mapLoaded) {
            mapLoaded = true // Force map to show even if loading callback hasn't triggered
        }
    }

    val filteredDisasters = remember(disasters, selectedDisasterType) {
        if (selectedDisasterType == null) {
            disasters
        } else {
            disasters.filter { it.type == selectedDisasterType }
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    // Show snackbar for error or success
    LaunchedEffect(errorMessage, successMessage) {
        errorMessage?.let {
            scope.launch { snackbarHostState.showSnackbar(it) }
        }
        successMessage?.let {
            scope.launch { snackbarHostState.showSnackbar(it) }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = mapUiSettings,
            onMapLoaded = {
                mapLoaded = true
                // Ensure proper focus after map is loaded
                focusDisaster?.let { disaster ->
                    scope.launch {
                        delay(500) // Small delay to ensure map is ready
                        cameraPositionState.animate(
                            update = CameraUpdateFactory.newCameraPosition(
                                CameraPosition.fromLatLngZoom(
                                    LatLng(disaster.latitude, disaster.longitude),
                                    15f
                                )
                            ),
                            durationMs = 1000
                        )
                    }
                }
            }
        ) {
            // Add markers for disasters
            filteredDisasters.forEach { disaster ->
                val position = LatLng(disaster.latitude, disaster.longitude)
                val isSelected = disaster.id == selectedDisaster?.id
                
                Marker(
                    state = MarkerState(position = position),
                    title = disaster.title,
                    snippet = disaster.location,
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
                        scope.launch {
                            // Animate to the selected disaster
                            cameraPositionState.animate(
                                update = CameraUpdateFactory.newCameraPosition(
                                    CameraPosition.fromLatLngZoom(position, 15f)
                                ),
                                durationMs = 500
                            )
                        }
                        onDisasterClick(disaster)
                        true
                    }
                )
            }
            // User markers (admin only)
            users?.forEach { user ->
                if (user.latitude != null && user.longitude != null) {
                    Marker(
                        state = MarkerState(position = LatLng(user.latitude, user.longitude)),
                        title = user.fullName,
                                                    snippet = "Pengguna: ${user.email}",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                    )
                }
            }
        }

        // Loading overlay
        AnimatedVisibility(
            visible = !mapLoaded,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.loading_map),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        // Top app bar with semi-transparent background
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding(),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            shadowElevation = 4.dp
        ) {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.disaster_map_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.go_back_description)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { isFilterExpanded = !isFilterExpanded }
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = stringResource(R.string.filter_disasters_description)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }

        // Filter dropdown
        AnimatedVisibility(
            visible = isFilterExpanded,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 64.dp, end = 8.dp)
        ) {
            Card(
                modifier = Modifier
                    .width(200.dp)
                    .padding(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    // Add "All" option
                    DisasterTypeChip(
                        text = stringResource(R.string.all_filter),
                        selected = selectedDisasterType == null,
                        onClick = {
                            selectedDisasterType = null
                            onFilterChange(null)
                            isFilterExpanded = false
                        }
                    )
                    
                    DisasterType.values().forEach { type ->
                        DisasterTypeChip(
                            text = type.getDisplayName(),
                            selected = selectedDisasterType == type,
                            onClick = {
                                selectedDisasterType = if (selectedDisasterType == type) null else type
                                onFilterChange(selectedDisasterType)
                                isFilterExpanded = false
                            }
                        )
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
                    scope.launch {
                        cameraPositionState.animate(
                            update = CameraUpdateFactory.zoomBy(1f),
                            durationMs = 300
                        )
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.zoom_in_description))
            }

            FloatingActionButton(
                onClick = {
                    scope.launch {
                        cameraPositionState.animate(
                            update = CameraUpdateFactory.zoomBy(-1f),
                            durationMs = 300
                        )
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Icon(Icons.Default.Remove, contentDescription = stringResource(R.string.zoom_out_description))
            }

            FloatingActionButton(
                onClick = {
                    scope.launch {
                        val target = currentLocation ?: defaultLocation
                        cameraPositionState.animate(
                            update = CameraUpdateFactory.newLatLngZoom(target, 15f),
                            durationMs = 1000
                        )
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = stringResource(R.string.my_location_description))
            }
        }

        // Loading indicator for other operations
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        // Permission request
        if (!locationPermissions.allPermissionsGranted) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.location_permission_required_title),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.location_permission_required_message),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { locationPermissions.launchMultiplePermissionRequest() }
                        ) {
                            Text(stringResource(R.string.grant_permission))
                        }
                    }
                }
            }
        }

        GlobalSnackbarHost(snackbarHostState)
    }
}

fun imageVectorToBitmapDescriptor(
    context: android.content.Context,
    imageVector: ImageVector,
    color: Color = Color.Red,
    sizeDp: Int = 36
): BitmapDescriptor {
    return try {
        val density = context.resources.displayMetrics.density
        val sizePx = (sizeDp * density).toInt()
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
            this.color = color.toArgb()
            style = android.graphics.Paint.Style.FILL
        }
        // Draw background
        canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx / 2f, paint)
        BitmapDescriptorFactory.fromBitmap(bitmap)
    } catch (e: Exception) {
        BitmapDescriptorFactory.defaultMarker()
    }
}
