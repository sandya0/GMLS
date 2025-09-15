package com.example.gmls.ui.components

import android.Manifest
import android.os.Build
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import android.location.LocationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.shouldShowRationale
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.gmls.R
import com.example.gmls.ui.theme.*
import com.example.gmls.ui.viewmodels.LocationTrackingViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.shouldShowRationale
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationTrackingCard(
    modifier: Modifier = Modifier,
    locationTrackingViewModel: LocationTrackingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val trackingState by locationTrackingViewModel.trackingState.collectAsState()

    // Permission states
    val basicLocationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    val backgroundLocationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        rememberMultiplePermissionsState(
            permissions = listOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        )
    } else null

    val hasBasicLocationPermission = basicLocationPermissions.allPermissionsGranted
    val hasBackgroundLocationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        backgroundLocationPermission?.allPermissionsGranted ?: false
    } else true

    val permissionDeniedPermanently = basicLocationPermissions.permissions.any { !it.status.isGranted && !it.status.shouldShowRationale } ||
            (backgroundLocationPermission?.permissions?.any { !it.status.isGranted && !it.status.shouldShowRationale } ?: false)

    // Check if location services are enabled
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val isLocationServiceEnabled = remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val listener = object : android.location.LocationListener {
            override fun onLocationChanged(location: android.location.Location) {}
            override fun onProviderEnabled(provider: String) { isLocationServiceEnabled.value = true }
            override fun onProviderDisabled(provider: String) { isLocationServiceEnabled.value = false }
        }
        // Check initial state
        isLocationServiceEnabled.value = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        // No need to register for updates if we just need the status
        onDispose { }
    }

    // Clear error when permissions are granted or location service is enabled
    LaunchedEffect(hasBasicLocationPermission, hasBackgroundLocationPermission, isLocationServiceEnabled.value) {
        if (hasBasicLocationPermission && hasBackgroundLocationPermission && isLocationServiceEnabled.value &&
            trackingState.error?.contains("permission", ignoreCase = true) == true ||
            trackingState.error?.contains("layanan lokasi", ignoreCase = true) == true) {
            locationTrackingViewModel.clearError()
        }
    }

    // Auto-retry location tracking when all conditions are met
    LaunchedEffect(hasBasicLocationPermission, hasBackgroundLocationPermission, isLocationServiceEnabled.value) {
        if (hasBasicLocationPermission && hasBackgroundLocationPermission && isLocationServiceEnabled.value &&
            !trackingState.isTracking &&
            (trackingState.error?.contains("permission", ignoreCase = true) == true ||
                    trackingState.error?.contains("layanan lokasi", ignoreCase = true) == true)) {
            kotlinx.coroutines.delay(500) // Small delay to allow state to settle
            locationTrackingViewModel.clearError()
            locationTrackingViewModel.toggleLocationTracking()
        }
    }

    val cardAlpha by animateFloatAsState(
        targetValue = if (trackingState.isTracking) 1f else 0.8f,
        animationSpec = tween(300), label = "card_alpha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(cardAlpha),
        colors = CardDefaults.cardColors(
            containerColor = when {
                trackingState.isTracking -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                trackingState.error != null -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                color = when {
                                    trackingState.isTracking -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    trackingState.error != null -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                                    else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                },
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when {
                                trackingState.isTracking -> Icons.Default.LocationOn
                                trackingState.error != null -> Icons.Default.LocationOff
                                else -> Icons.Default.MyLocation
                            },
                            contentDescription = null,
                            tint = when {
                                trackingState.isTracking -> MaterialTheme.colorScheme.primary
                                trackingState.error != null -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.primary
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    Column {
                        Text(
                            text = stringResource(R.string.location_sharing_label),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = when {
                                trackingState.isTracking -> stringResource(R.string.active_sharing_location)
                                trackingState.error != null -> trackingState.error!! // Display specific error from ViewModel
                                !isLocationServiceEnabled.value -> stringResource(R.string.location_service_disabled_text)
                                !hasBasicLocationPermission -> stringResource(R.string.location_permission_required_text)
                                backgroundLocationPermission != null && !hasBackgroundLocationPermission -> stringResource(R.string.ready_background_optional)
                                else -> stringResource(R.string.ready_to_share_text)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = when {
                                trackingState.isTracking -> MaterialTheme.colorScheme.primary
                                trackingState.error != null -> MaterialTheme.colorScheme.error
                                !isLocationServiceEnabled.value -> MaterialTheme.colorScheme.error
                                !hasBasicLocationPermission -> MaterialTheme.colorScheme.tertiary
                                backgroundLocationPermission != null && !hasBackgroundLocationPermission -> MaterialTheme.colorScheme.secondary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
                
                if (trackingState.isTracking) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.live_status),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = trackingState.isTracking && trackingState.currentLocation != null
            ) {
                trackingState.currentLocation?.let { location ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "${String.format("%.4f", location.latitude)}, ${String.format("%.4f", location.longitude)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    fontWeight = FontWeight.Medium
                                )
                                trackingState.lastUpdateTime?.let { time ->
                                    Text(
                                        text = stringResource(R.string.updated_at_time, SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(time))),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.GpsFixed,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(visible = trackingState.error != null) {
                trackingState.error?.let { error ->
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = error, // Display the exact error from ViewModel
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Action button - prioritized based on state
            when {
                !isLocationServiceEnabled.value -> {
                    Button(
                        onClick = {
                            // Direct user to settings to enable location services
                            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.GpsOff,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.enable_location_services),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                permissionDeniedPermanently -> {
                    Button(
                        onClick = {
                            // Direct user to app settings to grant permissions manually
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.grant_permissions_in_settings),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                !hasBasicLocationPermission -> {
                    Button(
                        onClick = {
                            basicLocationPermissions.launchMultiplePermissionRequest()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.grant_location_permission),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                backgroundLocationPermission != null && !hasBackgroundLocationPermission -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (trackingState.error != null) {
                                    locationTrackingViewModel.clearError()
                                }
                                locationTrackingViewModel.toggleLocationTracking()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (trackingState.isTracking)
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.primary
                            ),
                            enabled = !(trackingState.error?.contains("User not logged in") == true)
                        ) {
                            Icon(
                                imageVector = if (trackingState.isTracking) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (trackingState.isTracking) stringResource(R.string.stop_sharing_text) else stringResource(R.string.start_sharing_text),
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = stringResource(R.string.enable_background_location_description),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                backgroundLocationPermission?.launchMultiplePermissionRequest()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.enable_background_location),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                else -> {
                    Button(
                        onClick = {
                            Log.d("LocationTrackingCard", "=== START SHARING CLICKED ===")
                            Log.d("LocationTrackingCard", "Basic location permission: $hasBasicLocationPermission")
                            Log.d("LocationTrackingCard", "Background location permission: $hasBackgroundLocationPermission")
                            Log.d("LocationTrackingCard", "Location service enabled: ${isLocationServiceEnabled.value}")
                            Log.d("LocationTrackingCard", "Current tracking state: ${trackingState.isTracking}")
                            Log.d("LocationTrackingCard", "Current error: ${trackingState.error}")

                            if (trackingState.error != null) {
                                Log.d("LocationTrackingCard", "Clearing previous error")
                                locationTrackingViewModel.clearError()
                            }

                            Log.d("LocationTrackingCard", "Calling toggleLocationTracking()")
                            locationTrackingViewModel.toggleLocationTracking()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (trackingState.isTracking)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.primary
                        ),
                        enabled = !(trackingState.error?.contains("User not logged in") == true)
                    ) {
                        Icon(
                            imageVector = if (trackingState.isTracking) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (trackingState.isTracking) stringResource(R.string.stop_sharing_text) else stringResource(R.string.start_sharing_text),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
} 
