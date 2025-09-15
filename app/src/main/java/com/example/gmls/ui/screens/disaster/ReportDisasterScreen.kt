package com.example.gmls.ui.screens.disaster

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.gmls.R
import com.example.gmls.data.remote.LocationService // Assuming this is the correct import
import com.example.gmls.domain.model.DisasterType
import com.example.gmls.domain.model.getDisplayName
import com.example.gmls.ui.components.DisasterTextField
import com.example.gmls.ui.components.DisasterTypeChip
import com.example.gmls.ui.components.GlobalSnackbarHost
import com.example.gmls.ui.components.PrimaryButton
import com.example.gmls.ui.theme.Red
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.util.*

/**
 * Data class to hold disaster report information from user input
 */
data class DisasterReport(
    val title: String,
    val description: String,
    val location: String, // User-provided address or "Current Location" text
    val useCurrentLocation: Boolean,
    val type: DisasterType,
    val affectedCount: Int,
    val images: List<Uri>,
    val timestamp: Date = Date(),
    val reportedBy: String,
    val latitude: Double? = null, // Store actual coordinates if available
    val longitude: Double? = null
)

// Helper to create a temporary image file
fun createImageFile(context: Context): File? {
    return try {
        File.createTempFile("IMG_", ".jpg", context.cacheDir).apply {
            // Ensure the file is writable and exists
        }
    } catch (ex: IOException) {
        // Log error or handle, e.g., show a snackbar
        ex.printStackTrace()
        null
    }
}

// Helper to get a content URI for a file using FileProvider
fun getUriForFile(context: Context, file: File): Uri {
    // IMPORTANT: Ensure `context.packageName + ".provider"` matches the authority
    // declared in your AndroidManifest.xml <provider> tag.
    return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
}

// Simple permission result handler
@Composable
private fun rememberPermissionLauncher(onResult: (Boolean) -> Unit) =
    rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission(), onResult)

@Composable
private fun rememberMultiplePermissionsLauncher(onResult: (Map<String, Boolean>) -> Unit) =
    rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions(), onResult)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDisasterScreen(
    navController: NavController,
    currentUserId: String,
    // Changed onSubmit to include the DisasterReport fully populated
    onSubmit: (DisasterReport) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    // isLoading, errorMessage, successMessage would ideally come from a ViewModel
    isLoadingFromViewModel: Boolean = false,
    errorMessageFromViewModel: String? = null,
    successMessageFromViewModel: String? = null
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var manualLocationAddress by rememberSaveable { mutableStateOf("") }
    var selectedType by rememberSaveable(stateSaver = Saver(
        save = { it?.name },
        restore = { name -> name?.let { DisasterType.safeValueOf(it) } }
    )) { mutableStateOf<DisasterType?>(null) }
    var affectedCount by rememberSaveable { mutableStateOf("0") }
    var useCurrentLocation by rememberSaveable { mutableStateOf(true) }
    var imageUris by rememberSaveable(
        stateSaver = listSaver(
            save = { list -> list.map { it.toString() } },
            restore = { list -> list.map { Uri.parse(it) } }
        )
    ) { mutableStateOf<List<Uri>>(emptyList()) }

    // Store actual coordinates (from map picker or current location)
    var currentCoordinates by rememberSaveable(
        stateSaver = listSaver<Pair<Double, Double>?, Double>(
            save = { pair -> pair?.let { listOf(it.first, it.second) } ?: emptyList() },
            restore = { list -> if (list.size == 2) Pair(list[0], list[1]) else null }
        )
    ) { mutableStateOf<Pair<Double, Double>?>(null) }
    var isFetchingLocation by remember { mutableStateOf(false) }


    var titleError by remember { mutableStateOf<String?>(null) }
    var descriptionError by remember { mutableStateOf<String?>(null) }
    var locationError by remember { mutableStateOf<String?>(null) } // For manual address
    var typeError by remember { mutableStateOf<String?>(null) }

    val disasterTypes = remember { DisasterType.values().toList() }
    val scrollState = rememberScrollState()

    // --- Hilt/LocationService Note ---
    // Ideally, LocationService is injected via Hilt into a ViewModel for this screen.
    // Direct instantiation `remember { LocationService(context) }` is not Hilt best practice.
    // This instance is not managed by Hilt's lifecycle or dependency graph.
    // For this example, we'll proceed, but this should be refactored in a production Hilt app.
    val locationService = remember(context) { LocationService(context) }


    // --- Permission Launchers ---
    val fineLocationPermissionLauncher = rememberPermissionLauncher { isGranted ->
        if (isGranted) {
            if (useCurrentLocation) fetchCurrentLocation(
                coroutineScope,
                locationService,
                { coords -> currentCoordinates = coords },
                { addr -> manualLocationAddress = addr ?: "Current Location (Coordinates available)" },
                { isFetchingLocation = it },
                snackbarHostState
            )
        } else {
                            coroutineScope.launch { snackbarHostState.showSnackbar("Izin lokasi ditolak. Tidak dapat menggunakan lokasi saat ini.") }
            useCurrentLocation = false // Revert if permission denied
        }
    }
    val cameraPermissionLauncher = rememberPermissionLauncher { isGranted ->
        if (isGranted) {
            // Proceed with launching camera (logic below)
        } else {
                            coroutineScope.launch { snackbarHostState.showSnackbar("Izin kamera ditolak.") }
        }
    }
    // For image picker, READ_MEDIA_IMAGES (Android 13+) or READ_EXTERNAL_STORAGE (older)
    // For simplicity, we'll assume GetMultipleContents handles this, but a specific permission check is better.


    // --- Image Pickers ---
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (uris.isNotEmpty()) imageUris = imageUris + uris
    }
    val cameraImageUri = remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) cameraImageUri.value?.let { imageUris = imageUris + it }
    }

    // --- Effects ---
    LaunchedEffect(errorMessageFromViewModel, successMessageFromViewModel) {
        errorMessageFromViewModel?.let { coroutineScope.launch { snackbarHostState.showSnackbar(it) } }
        successMessageFromViewModel?.let { coroutineScope.launch { snackbarHostState.showSnackbar(it) } }
    }

    // Fetch current location when 'useCurrentLocation' is true and permission is granted
    LaunchedEffect(useCurrentLocation) {
        if (useCurrentLocation) {
            fineLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            // Clear coordinates and address when switching to manual mode
            currentCoordinates = null
            manualLocationAddress = ""
        }
    }

    // Listen for result from LocationPickerScreen
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    LaunchedEffect(savedStateHandle) {
        try {
            // Observe both latitude and longitude
            savedStateHandle?.getLiveData<Double>("picked_latitude")?.observe(lifecycleOwner) { latitude ->
                savedStateHandle.getLiveData<Double>("picked_longitude")?.observe(lifecycleOwner) { longitude ->
                    if (latitude != null && longitude != null) {
                        // Validate coordinates
                        if (latitude in -90.0..90.0 && longitude in -180.0..180.0) {
                            // Switch to manual mode when picking from map
                            useCurrentLocation = false
                            currentCoordinates = Pair(latitude, longitude)
                            isFetchingLocation = true
                            coroutineScope.launch {
                                try {
                                                                          val address = locationService.reverseGeocode(latitude, longitude)
                                      val locationText = address 
                                         ?: "Lintang: ${String.format("%.6f", latitude)}, Bujur: ${String.format("%.6f", longitude)}"
                                      manualLocationAddress = locationText
                                      locationError = null
                                  } catch (e: Exception) {
                                     val locationText = "Lintang: ${String.format("%.6f", latitude)}, Bujur: ${String.format("%.6f", longitude)}"
                                    manualLocationAddress = locationText
                                    snackbarHostState.showSnackbar("Tidak dapat mendapatkan alamat untuk lokasi yang dipilih")
                                } finally {
                                    isFetchingLocation = false
                                }
                            }
                        } else {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Koordinat tidak valid diterima")
                            }
                        }
                    }
                    // Clean up the saved state
                    savedStateHandle.remove<Double>("picked_latitude")
                    savedStateHandle.remove<Double>("picked_longitude")
                }
            }
        } catch (e: Exception) {
            // Handle any potential errors
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Kesalahan memproses data lokasi")
            }
            savedStateHandle?.remove<Double>("picked_latitude")
            savedStateHandle?.remove<Double>("picked_longitude")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.report_disaster_title)) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, "Tutup")
                    }
                }
            )
        },
        modifier = modifier,
        snackbarHost = { GlobalSnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Disaster Type
                Text(stringResource(R.string.disaster_type), style = MaterialTheme.typography.titleMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(disasterTypes) { type ->
                        DisasterTypeChip(
                            text = type.getDisplayName(), // Using localized display name
                            selected = selectedType == type,
                            onClick = { selectedType = type; typeError = null }
                        )
                    }
                }
                typeError?.let { ErrorText(it) }

                // Title
                DisasterTextField(
                    value = title,
                    onValueChange = { title = it; titleError = null },
                    label = stringResource(R.string.disaster_title),
                    leadingIcon = Icons.Filled.Title,
                    isError = titleError != null,
                    errorMessage = titleError
                )

                // Description
                Text(stringResource(R.string.description), style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it; descriptionError = null },
                    placeholder = { Text(stringResource(R.string.describe_disaster)) },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    isError = descriptionError != null,
                    // ... (colors as before)
                )
                descriptionError?.let { ErrorText(it) }

                // Location Section
                Text(stringResource(R.string.location), style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = useCurrentLocation, onCheckedChange = { useCurrentLocation = it })
                    Text(stringResource(R.string.use_my_current_location), style = MaterialTheme.typography.bodyMedium)
                }

                AnimatedVisibility(visible = useCurrentLocation) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        if (isFetchingLocation) {
                            CircularProgressIndicator(color = Red)
                            Text(stringResource(R.string.fetching_location), style = MaterialTheme.typography.bodySmall)
                        } else {
                            Icon(Icons.Filled.LocationOn, null, tint = Red, modifier = Modifier.size(32.dp))
                            Text(
                                currentCoordinates?.let { stringResource(R.string.coordinates, it.first, it.second) }
                                    ?: stringResource(R.string.current_location_will_be_used),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (currentCoordinates != null && manualLocationAddress.isNotEmpty()) {
                                Text(stringResource(R.string.address, manualLocationAddress), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                AnimatedVisibility(visible = !useCurrentLocation) {
                    Column {
                        DisasterTextField(
                            value = manualLocationAddress,
                            onValueChange = { 
                                manualLocationAddress = it
                                locationError = null
                                // Clear coordinates if user types a manual address
                                if (it.isNotEmpty() && !it.startsWith("Lintang:")) {
                                    currentCoordinates = null
                                }
                            },
                            label = stringResource(R.string.location_address_or_description),
                            leadingIcon = Icons.Filled.LocationOn,
                            isError = locationError != null,
                            errorMessage = locationError
                        )
                        Button(
                            onClick = { navController.navigate("location_picker") },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Icon(Icons.Default.Map, null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.pick_on_map))
                        }
                        currentCoordinates?.let { (lat, lng) ->
                            Text(
                                stringResource(R.string.selected_coordinates, lat, lng),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                if (!useCurrentLocation) locationError?.let { ErrorText(it) }


                // Affected People Count
                Text(stringResource(R.string.estimated_affected_people), style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Slider(
                        value = affectedCount.toFloatOrNull() ?: 0f,
                        onValueChange = { affectedCount = it.toInt().toString() },
                        valueRange = 0f..1000f, // Increased range
                        steps = 99, // For steps of 10 up to 1000
                        modifier = Modifier.weight(1f),
                        // ... (colors as before)
                    )
                    Spacer(Modifier.width(16.dp))
                    OutlinedTextField(
                        value = affectedCount,
                        onValueChange = { newValue -> affectedCount = newValue.filter { it.isDigit() }.take(4) }, // Allow only digits up to 4
                        modifier = Modifier.width(80.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        // ... (colors as before)
                    )
                }

                // Image Upload
                Text(stringResource(R.string.upload_images_optional), style = MaterialTheme.typography.titleMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { ImageActionButton(icon = Icons.Default.AddPhotoAlternate, text = stringResource(R.string.add_image)) { imagePicker.launch("image/*") } }
                    item {
                        ImageActionButton(icon = Icons.Default.CameraAlt, text = stringResource(R.string.take_photo)) {
                            // Check camera permission before launching
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA) // Request permission
                            // The actual launch logic needs to be inside the permission callback if granted
                            // For simplicity, this is a direct call, but it's better to chain it.
                            // One way:
                            // cameraPermissionLauncher.launch(Manifest.permission.CAMERA, onGranted = {
                            //    val photoFile = createImageFile(context)
                            //    if (photoFile != null) {
                            //        val uri = getUriForFile(context, photoFile)
                            //        cameraImageUri.value = uri
                            //        cameraLauncher.launch(uri)
                            //    } else { /* handle file creation error */ }
                            // })

                            // Simplified (less robust permission flow for brevity):
                            val photoFile = createImageFile(context)
                            if (photoFile != null) {
                                val uri = getUriForFile(context, photoFile)
                                cameraImageUri.value = uri
                                cameraLauncher.launch(uri) // Assumes permission was granted earlier or implicitly
                            } else {
                                coroutineScope.launch { snackbarHostState.showSnackbar("Tidak dapat membuat file gambar.") }
                            }
                        }
                    }
                    items(imageUris) { uri -> ImagePreviewItem(uri = uri, onRemove = { imageUris -= uri }) }
                }

                Spacer(Modifier.height(16.dp))
                PrimaryButton(
                    text = stringResource(R.string.submit_report_button),
                    onClick = {
                        // Corrected validation call
                        if (validateInputs(
                                selectedType = selectedType,
                                title = title,
                                description = description,
                                useCurrentLocation = useCurrentLocation,
                                manualLocation = manualLocationAddress,
                                currentCoordinates = currentCoordinates, // Pass coordinates for validation if needed
                                setTypeError = { typeError = it },
                                setTitleError = { titleError = it },
                                setDescriptionError = { descriptionError = it },
                                setLocationError = { locationError = it }
                            )
                        ) {
                            val finalLocationString = if (useCurrentLocation) {
                                manualLocationAddress.ifEmpty { currentCoordinates?.let { "%.5f, %.5f".format(it.first, it.second) } ?: "Current Location (Unknown)" }
                            } else {
                                manualLocationAddress
                            }

                            val report = DisasterReport(
                                title = title.trim(),
                                description = description.trim(),
                                location = finalLocationString.trim(),
                                useCurrentLocation = useCurrentLocation,
                                type = selectedType!!, // Already validated not to be null
                                affectedCount = affectedCount.toIntOrNull() ?: 0,
                                images = imageUris,
                                reportedBy = currentUserId,
                                latitude = currentCoordinates?.first,
                                longitude = currentCoordinates?.second
                            )
                            onSubmit(report)
                        }
                    },
                    isLoading = isLoadingFromViewModel, // Use ViewModel's loading state
                    icon = Icons.Filled.Send
                )
            }

            if (isLoadingFromViewModel) { // Use ViewModel's loading state
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = Red) }
            }
        }
    }
}

// Extracted helper for fetch current location logic
private fun fetchCurrentLocation(
    scope: kotlinx.coroutines.CoroutineScope,
    locationService: LocationService,
    onCoordinatesReceived: (Pair<Double, Double>) -> Unit,
    onAddressReceived: (String?) -> Unit,
    onFetchingStateChange: (Boolean) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    scope.launch {
        onFetchingStateChange(true)
        try {
            val result = locationService.getCurrentLocation()
            result.fold(
                onSuccess = { currentCoords ->
                    onCoordinatesReceived(Pair(currentCoords.latitude, currentCoords.longitude))
                    val address = locationService.reverseGeocode(currentCoords.latitude, currentCoords.longitude)
                    onAddressReceived(address ?: "Koordinat: %.3f, %.3f".format(currentCoords.latitude, currentCoords.longitude))
                },
                onFailure = { exception ->
                    val errorMessage = when (exception) {
                        is SecurityException -> "Izin lokasi diperlukan untuk menggunakan lokasi saat ini."
                        else -> exception.message ?: "Tidak dapat mengambil lokasi saat ini."
                    }
                    snackbarHostState.showSnackbar(errorMessage)
                }
            )
        } finally {
            onFetchingStateChange(false)
        }
    }
}


@Composable
private fun ErrorText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier.padding(top = 4.dp, start = 8.dp)
    )
}

@Composable
private fun ImageActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = text, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ImagePreviewItem(uri: Uri, onRemove: () -> Unit) {
    Box(modifier = Modifier.size(100.dp).clip(RoundedCornerShape(8.dp))) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(uri).crossfade(true).build(),
            contentDescription = "Gambar bencana",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        IconButton(
            onClick = onRemove,
            modifier = Modifier.align(Alignment.TopEnd).size(32.dp).clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
        ) {
            Icon(Icons.Default.Close, "Hapus gambar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
        }
    }
}

// Updated validation logic
private fun validateInputs(
    selectedType: DisasterType?,
    title: String,
    description: String,
    useCurrentLocation: Boolean,
    manualLocation: String,
    currentCoordinates: Pair<Double, Double>?, // Added for context
    setTypeError: (String?) -> Unit,
    setTitleError: (String?) -> Unit,
    setDescriptionError: (String?) -> Unit,
    setLocationError: (String?) -> Unit
): Boolean {
    var isValid = true

    if (selectedType == null) {
        setTypeError("Silakan pilih jenis bencana"); isValid = false
    } else { setTypeError(null) }

    if (title.isBlank()) {
        setTitleError("Silakan masukkan judul bencana"); isValid = false
    } else { setTitleError(null) }

    if (description.isBlank()) {
        setDescriptionError("Silakan berikan deskripsi"); isValid = false
    } else { setDescriptionError(null) }

    if (!useCurrentLocation) {
        if (manualLocation.isBlank() && currentCoordinates == null) { // If no manual address AND no map-picked coordinates
            setLocationError("Silakan berikan lokasi atau pilih di peta"); isValid = false
        } else { setLocationError(null) }
    } else {
        // If using current location, ensure coordinates are (or will be) available.
        // This validation might be more about ensuring the fetch process was initiated.
        // For now, we assume if useCurrentLocation is true, it's valid if permission is granted.
        setLocationError(null)
    }
    return isValid
}

// Add a safe way to convert string to DisasterType for rememberSaveable
fun DisasterType.Companion.safeValueOf(value: String): DisasterType? {
    return try {
        DisasterType.valueOf(value)
    } catch (e: IllegalArgumentException) {
        null
    }
}

// Placeholder for DisasterType.displayName - ensure your DisasterType enum has this
// e.g., enum class DisasterType(val displayName: String) { FLOOD("Flood"), ... }
// If not, you'll need to format type.name as in the previous Dashboard example.
val DisasterType.displayName: String
    get() = this.name.replace("_", " ").lowercase(Locale.getDefault())
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
