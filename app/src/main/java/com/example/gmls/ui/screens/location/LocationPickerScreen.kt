package com.example.gmls.ui.screens.location

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.gmls.R
import com.example.gmls.domain.model.LocationData
import com.example.gmls.ui.components.GlobalSnackbarHost
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerScreen(
    onLocationPicked: (Double, Double) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val defaultLocation = LatLng(-6.200000, 106.816666) // Jakarta
    var pickedLocation by remember { mutableStateOf<LocationData?>(null) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 5f)
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var isProcessingPick by remember { mutableStateOf(false) }
    var isLoadingLocation by remember { mutableStateOf(false) }

    // Location permission state
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission = isGranted
        if (isGranted) {
            // Get location when permission is granted
            scope.launch {
                getCurrentLocation(context, snackbarHostState)?.let { location ->
                    pickedLocation = location
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(
                        LatLng(location.latitude, location.longitude),
                        15f
                    )
                }
            }
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Izin lokasi diperlukan untuk menggunakan fitur ini")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.pick_location_on_map)) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cancel))
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (!isLoadingLocation) {
                                if (hasLocationPermission) {
                                    scope.launch {
                                        isLoadingLocation = true
                                        try {
                                            getCurrentLocation(context, snackbarHostState)?.let { location ->
                                                pickedLocation = location
                                                cameraPositionState.position = CameraPosition.fromLatLngZoom(
                                                    LatLng(location.latitude, location.longitude),
                                                    15f
                                                )
                                            }
                                        } finally {
                                            isLoadingLocation = false
                                        }
                                    }
                                } else {
                                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                }
                            }
                        }
                    ) {
                        if (isLoadingLocation) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Default.MyLocation,
                                contentDescription = "Gunakan Lokasi Saya"
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { GlobalSnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = hasLocationPermission,
                    mapType = MapType.NORMAL
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    myLocationButtonEnabled = false,
                    mapToolbarEnabled = false,
                    zoomGesturesEnabled = true,
                    scrollGesturesEnabled = true
                ),
                onMapClick = { latLng -> 
                    if (!isProcessingPick && !isLoadingLocation) {
                        try {
                            val location = LocationData(latLng.latitude, latLng.longitude)
                            if (location.isValid()) {
                                pickedLocation = location
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Koordinat tidak valid. Silakan pilih lokasi yang berbeda.")
                                }
                            }
                        } catch (e: Exception) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Kesalahan memilih lokasi. Silakan coba lagi.")
                            }
                        }
                    }
                }
            ) {
                pickedLocation?.let { location ->
                    Marker(
                        state = MarkerState(position = LatLng(location.latitude, location.longitude)),
                        title = "Lokasi Terpilih",
                        snippet = "Lintang: ${String.format("%.6f", location.latitude)}, Bujur: ${String.format("%.6f", location.longitude)}"
                    )
                }
            }
            
            // Instructions overlay
            if (pickedLocation == null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    shape = MaterialTheme.shapes.medium,
                    shadowElevation = 4.dp
                ) {
                    Text(
                        "Ketuk pada peta atau gunakan tombol lokasi untuk memilih lokasi",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Show selected coordinates text
            pickedLocation?.let { location ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 80.dp, start = 16.dp, end = 16.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    shape = MaterialTheme.shapes.medium,
                    shadowElevation = 4.dp
                ) {
                    Text(
                        "Terpilih: ${String.format("%.6f", location.latitude)}, ${String.format("%.6f", location.longitude)}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            pickedLocation?.let { location ->
                ExtendedFloatingActionButton(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    onClick = {
                        scope.launch {
                            try {
                                if (location.isValid()) {
                                    // This is your navigation/home‑page callback.
                                    onLocationPicked(location.latitude, location.longitude)
                                } else {
                                    snackbarHostState.showSnackbar(
                                        "Koordinat tidak valid. Silakan pilih lokasi yang berbeda."
                                    )
                                }
                            } catch (t: Throwable) {
                                // 1) Log it so you can see the full stack trace in Logcat:
                                android.util.Log.e("LocationPicker", "Error in onLocationPicked", t)
                                // 2) Surface the message so you know what happened:
                                snackbarHostState.showSnackbar(
                                    "Terjadi kesalahan: ${t::class.simpleName} – lihat Logcat untuk detail"
                                )
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.Check, contentDescription = "Konfirmasi Lokasi") },
                    text = { Text(stringResource(R.string.confirm_location)) }
                )
            }


        }
    }
}

private suspend fun getCurrentLocation(
    context: android.content.Context,
    snackbarHostState: SnackbarHostState
): LocationData? {
    return try {
        // Explicit permission check before accessing location
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            snackbarHostState.showSnackbar("Izin lokasi tidak diberikan")
            return null
        }
        
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val location = fusedLocationClient.lastLocation.await()
        
        location?.let {
            LocationData(it.latitude, it.longitude)
        } ?: run {
            snackbarHostState.showSnackbar("Tidak dapat mendapatkan lokasi saat ini. Silakan coba lagi atau pilih secara manual.")
            null
        }
    } catch (e: Exception) {
        if (e !is CancellationException) {
            snackbarHostState.showSnackbar("Kesalahan mendapatkan lokasi: ${e.message}")
        }
        null
    }
} 
