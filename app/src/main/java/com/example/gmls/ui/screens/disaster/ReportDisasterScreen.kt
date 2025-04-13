package com.example.gmls.ui.screens.disaster

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.gmls.domain.model.DisasterType
import com.example.gmls.ui.components.DisasterTextField
import com.example.gmls.ui.components.DisasterTypeChip
import com.example.gmls.ui.components.PrimaryButton
import com.example.gmls.ui.theme.Red
import java.util.*
import com.example.gmls.ui.components.GlobalSnackbarHost
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.launch
import android.content.ContentProvider
import java.io.File
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import dagger.hilt.android.EntryPointAccessors
import com.example.gmls.data.remote.LocationService

/**
 * Data class to hold disaster report information from user input
 */
data class DisasterReport(
    val title: String,
    val description: String,
    val location: String,
    val useCurrentLocation: Boolean,
    val type: DisasterType,
    val affectedCount: Int,
    val images: List<Uri>,
    val timestamp: Date = Date(),
    val reportedBy: String
)

fun createImageFile(context: android.content.Context): java.io.File {
    return java.io.File.createTempFile("IMG_", ".jpg", context.cacheDir)
}

fun getUriForFile(context: android.content.Context, file: java.io.File): Uri {
    return FileProvider.getUriForFile(context, context.packageName + ".provider", file)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDisasterScreen(
    navController: NavController,
    currentUserId: String,
    onSubmit: (DisasterReport, Pair<Double, Double>?) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    successMessage: String? = null
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var location by rememberSaveable { mutableStateOf("") }
    var selectedType by rememberSaveable(stateSaver = Saver(
        save = { it?.name },
        restore = { it?.let { name -> DisasterType.valueOf(name) } }
    )) { mutableStateOf<DisasterType?>(null) }
    var affectedCount by rememberSaveable { mutableStateOf("0") }
    var useCurrentLocation by rememberSaveable { mutableStateOf(true) }
    var imageUris by rememberSaveable(
        stateSaver = listSaver(
            save = { it.map { uri -> uri.toString() } },
            restore = { it.map { uriStr -> Uri.parse(uriStr) } }
        )
    ) { mutableStateOf<List<Uri>>(emptyList()) }
    var pickedLatLng by rememberSaveable(
        stateSaver = listSaver<Pair<Double, Double>? , Double>(
            save = { it?.let { listOf(it.first, it.second) } ?: emptyList() },
            restore = { list -> if (list.size == 2) Pair(list[0], list[1]) else null }
        )
    ) { mutableStateOf<Pair<Double, Double>?>(null) }

    var titleError by remember { mutableStateOf<String?>(null) }
    var descriptionError by remember { mutableStateOf<String?>(null) }
    var locationError by remember { mutableStateOf<String?>(null) }
    var typeError by remember { mutableStateOf<String?>(null) }

    val disasterTypes = remember { DisasterType.values().toList() }
    val scrollState = rememberScrollState()

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            imageUris = imageUris + uris
        }
    }

    // Camera launcher
    val cameraImageUri = remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && cameraImageUri.value != null) {
            imageUris = imageUris + listOf(cameraImageUri.value!!)
        }
    }

    // Hilt-injected LocationService
    val locationService = remember { com.example.gmls.data.remote.LocationService(context) }

    // Show snackbar for error or success
    LaunchedEffect(errorMessage, successMessage) {
        errorMessage?.let {
            coroutineScope.launch { snackbarHostState.showSnackbar(it) }
        }
        successMessage?.let {
            coroutineScope.launch { snackbarHostState.showSnackbar(it) }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Report Disaster") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }
            )
        },
        modifier = modifier,
        snackbarHost = { GlobalSnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Disaster Type Selector
                Column {
                    Text(
                        text = "Disaster Type",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(disasterTypes) { type ->
                            DisasterTypeChip(
                                text = type.displayName,
                                selected = selectedType == type,
                                onClick = {
                                    selectedType = type
                                    typeError = null
                                }
                            )
                        }
                    }

                    if (typeError != null) {
                        Text(
                            text = typeError ?: "Please select a disaster type",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 4.dp, start = 8.dp)
                        )
                    }
                }

                // Title
                DisasterTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        titleError = null
                    },
                    label = "Disaster Title",
                    leadingIcon = Icons.Filled.Title,
                    isError = titleError != null,
                    errorMessage = titleError
                )

                // Description
                Column {
                    Text(
                        text = "Description",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = {
                            description = it
                            descriptionError = null
                        },
                        placeholder = { Text("Describe the disaster and situation...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Red,
                            focusedLabelColor = Red,
                            cursorColor = Red,
                            errorBorderColor = MaterialTheme.colorScheme.error,
                            errorLabelColor = MaterialTheme.colorScheme.error,
                            errorCursorColor = MaterialTheme.colorScheme.error
                        ),
                        isError = descriptionError != null
                    )

                    if (descriptionError != null) {
                        Text(
                            text = descriptionError ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 4.dp, start = 8.dp)
                        )
                    }
                }

                // Location
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Location",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = useCurrentLocation,
                                onCheckedChange = { useCurrentLocation = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Red
                                )
                            )

                            Text(
                                text = "Use my location",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    AnimatedVisibility(visible = !useCurrentLocation) {
                        DisasterTextField(
                            value = location,
                            onValueChange = {
                                location = it
                                locationError = null
                            },
                            label = "Location Address",
                            leadingIcon = Icons.Filled.LocationOn,
                            isError = locationError != null,
                            errorMessage = locationError
                        )
                    }

                    AnimatedVisibility(visible = useCurrentLocation) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                // This would be a map in a real app
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.LocationOn,
                                        contentDescription = null,
                                        tint = Red,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Using your current location",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }

                    if (!useCurrentLocation) {
                        Button(
                            onClick = {
                                navController.navigate("location_picker")
                            },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Icon(Icons.Default.Map, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Pick on Map")
                        }
                        pickedLatLng?.let { (lat, lng) ->
                            Text("Selected: $lat, $lng", style = MaterialTheme.typography.bodySmall)
                        }
                        // Listen for result from LocationPickerScreen
                        val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
                        LaunchedEffect(savedStateHandle) {
                            savedStateHandle?.getLiveData<Pair<Double, Double>>("picked_location")?.observe(lifecycleOwner) { result ->
                                pickedLatLng = result
                                if (result != null) {
                                    // Reverse geocode coordinates to address
                                    coroutineScope.launch {
                                        val address = locationService.reverseGeocode(result.first, result.second)
                                        location = address ?: "Lat: ${result.first}, Lng: ${result.second}"
                                    }
                                }
                                savedStateHandle.remove<Pair<Double, Double>>("picked_location")
                            }
                        }
                    }
                }

                // Affected People Count
                Column {
                    Text(
                        text = "Estimated Number of Affected People",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Slider(
                            value = affectedCount.toFloatOrNull() ?: 0f,
                            onValueChange = { affectedCount = it.toInt().toString() },
                            valueRange = 0f..100f,
                            steps = 10,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = Red,
                                activeTrackColor = Red,
                                inactiveTrackColor = Red.copy(alpha = 0.3f)
                            )
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        OutlinedTextField(
                            value = affectedCount,
                            onValueChange = { newValue ->
                                if (newValue.isEmpty() || newValue.toIntOrNull() != null) {
                                    affectedCount = newValue
                                }
                            },
                            singleLine = true,
                            modifier = Modifier.width(80.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Red,
                                focusedLabelColor = Red,
                                cursorColor = Red
                            )
                        )
                    }
                }

                // Image Upload
                Column {
                    Text(
                        text = "Upload Images",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Add image button
                        item {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outline,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        imagePicker.launch("image/*")
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add image",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = "Add Image",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Take photo button
                        item {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outline,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        // Move file/uri creation out of the Composable lambda
                                        val photoFile = createImageFile(context)
                                        val uri = getUriForFile(context, photoFile)
                                        cameraImageUri.value = uri
                                        cameraLauncher.launch(uri)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "Take photo",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Take Photo",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Selected images
                        items(imageUris) { uri ->
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(uri)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Disaster image",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                // Remove button
                                IconButton(
                                    onClick = {
                                        imageUris = imageUris.filter { it != uri }
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove image",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Submit Button
                PrimaryButton(
                    text = "Submit Report",
                    onClick = {
                        if (validateInputs(
                                selectedType,
                                title,
                                description,
                                useCurrentLocation,
                                location,
                                { typeError = it },
                                { titleError = it },
                                { descriptionError = it },
                                { locationError = it }
                            )) {
                            val report = DisasterReport(
                                title = title,
                                description = description,
                                location = if (useCurrentLocation) "Current Location" else location,
                                useCurrentLocation = useCurrentLocation,
                                type = selectedType!!,
                                affectedCount = affectedCount.toIntOrNull() ?: 0,
                                images = imageUris,
                                reportedBy = currentUserId // Add this
                            )
                            onSubmit(report, pickedLatLng)
                        }
                    },
                    isLoading = isLoading,
                    icon = Icons.Filled.Send
                )
            }

            // Loading indicator overlay
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
}

// Validation logic
private fun validateInputs(
    selectedType: DisasterType?,
    title: String,
    description: String,
    useCurrentLocation: Boolean,
    location: String,
    setTypeError: (String?) -> Unit,
    setTitleError: (String?) -> Unit,
    setDescriptionError: (String?) -> Unit,
    setLocationError: (String?) -> Unit
): Boolean {
    var isValid = true

    if (selectedType == null) {
        setTypeError("Please select a disaster type")
        isValid = false
    } else {
        setTypeError(null)
    }

    if (title.isBlank()) {
        setTitleError("Please enter a disaster title")
        isValid = false
    } else {
        setTitleError(null)
    }

    if (description.isBlank()) {
        setDescriptionError("Please provide a description")
        isValid = false
    } else {
        setDescriptionError(null)
    }

    if (!useCurrentLocation && location.isBlank()) {
        setLocationError("Please provide a location")
        isValid = false
    } else {
        setLocationError(null)
    }

    return isValid
}