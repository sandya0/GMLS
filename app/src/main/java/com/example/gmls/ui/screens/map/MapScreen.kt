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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.example.gmls.ui.components.GlobalSnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Density
import com.google.android.gms.maps.model.BitmapDescriptor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import com.google.android.gms.maps.model.BitmapDescriptorFactory

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
    successMessage: String? = null
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

    val snackbarHostState = remember { SnackbarHostState() }

    // Show snackbar for error or success
    LaunchedEffect(errorMessage, successMessage, locationPermissions.allPermissionsGranted) {
        errorMessage?.let {
            scope.launch { snackbarHostState.showSnackbar(it) }
        }
        successMessage?.let {
            scope.launch { snackbarHostState.showSnackbar(it) }
        }
        if (!locationPermissions.allPermissionsGranted) {
            scope.launch { snackbarHostState.showSnackbar("Location permission denied") }
        }
    }

    // Precompute BitmapDescriptors for each DisasterType (non-composable)
    val disasterTypeIcons = mapOf(
        DisasterType.EARTHQUAKE to Icons.Filled.Bolt,
        DisasterType.FLOOD to Icons.Filled.Water,
        DisasterType.WILDFIRE to Icons.Filled.LocalFireDepartment,
        DisasterType.LANDSLIDE to Icons.Filled.Terrain,
        DisasterType.VOLCANO to Icons.Filled.Volcano,
        DisasterType.TSUNAMI to Icons.Filled.Waves,
        DisasterType.HURRICANE to Icons.Filled.Storm,
        DisasterType.TORNADO to Icons.Filled.AirlineSeatFlatAngled,
        DisasterType.OTHER to Icons.Filled.Warning
    )

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
                val density = LocalDensity.current
                val iconCache = remember { mutableMapOf<DisasterType, BitmapDescriptor?>() }
                filteredDisasters.forEach { disaster ->
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
                    val iconDescriptor = remember(disaster.type) {
                        iconCache[disaster.type] ?: imageVectorToBitmapDescriptorComposable(
                            disasterIcon,
                            density,
                            Color.Red,
                            36
                        ).also { iconCache[disaster.type] = it }
                    }
                    Marker(
                        state = MarkerState(position = LatLng(disaster.latitude, disaster.longitude)),
                        title = disaster.title,
                        snippet = disaster.location,
                        icon = iconDescriptor,
                        onClick = {
                            selectedDisaster = disaster
                            onDisasterClick(disaster)
                            false // return false to allow default behavior (show info window)
                        }
                    )
                }
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
                IconButton(onClick = onBackClick, modifier = Modifier.semantics { contentDescription = "Go back" }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = null
                    )
                }
            },
            actions = {
                IconButton(onClick = { isFilterExpanded = !isFilterExpanded }, modifier = Modifier.semantics { contentDescription = "Filter disasters" }) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null
                    )
                }

                IconButton(onClick = { /* Refresh map */ }, modifier = Modifier.semantics { contentDescription = "Refresh map" }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null
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
        // Optionally, draw the vector path here (not trivial with ImageVector)
        BitmapDescriptorFactory.fromBitmap(bitmap)
    } catch (e: Exception) {
        BitmapDescriptorFactory.defaultMarker()
    }
}

@Composable
fun imageVectorToBitmapDescriptorComposable(
    imageVector: ImageVector,
    density: Density,
    color: Color = Color.Red,
    sizeDp: Int = 36
): BitmapDescriptor? {
    val sizePx = with(density) { sizeDp.dp.roundToPx() }
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val painter = rememberVectorPainter(image = imageVector)
    val composeCanvas = androidx.compose.ui.graphics.Canvas(canvas)
    val drawScope = object : DrawScope {
        override val density: Float get() = density.density
        override val fontScale: Float get() = density.fontScale
        override val layoutDirection = androidx.compose.ui.unit.LayoutDirection.Ltr
        override fun drawIntoCanvas(block: (androidx.compose.ui.graphics.Canvas) -> Unit) {
            block(composeCanvas)
        }
        override val size: Size get() = Size(sizePx.toFloat(), sizePx.toFloat())
    }
    with(painter) {
        draw(drawScope, size = Size(sizePx.toFloat(), sizePx.toFloat()), colorFilter = ColorFilter.tint(color))
    }
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}