package com.example.gmls.ui.screens.map


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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.gmls.domain.model.Disaster
import com.example.gmls.domain.model.DisasterType
import com.example.gmls.ui.components.DisasterTypeChip
import com.example.gmls.ui.theme.Red

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    disasters: List<Disaster>,
    onDisasterClick: (Disaster) -> Unit,
    onBackClick: () -> Unit,
    onFilterChange: (DisasterType?) -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    var selectedDisasterType by remember { mutableStateOf<DisasterType?>(null) }
    var selectedDisaster by remember { mutableStateOf<Disaster?>(null) }
    var isFilterExpanded by remember { mutableStateOf(false) }

    val filteredDisasters = remember(disasters, selectedDisasterType) {
        if (selectedDisasterType == null) {
            disasters
        } else {
            disasters.filter { it.type == selectedDisasterType }
        }
    }

    val disasterTypes = remember { DisasterType.values().toList() }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Map content
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // In a real app, this would be a Google Maps integration
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFFE0E0E0)
            ) {
                Text(
                    text = "Map View with ${filteredDisasters.size} disasters displayed",
                    style = MaterialTheme.typography.bodyLarge
                )
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
                onClick = { /* Zoom in */ },
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
                onClick = { /* Zoom out */ },
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
                onClick = { /* My location */ },
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

// Mock disaster for preview
val mockDisaster = Disaster(
    id = "1",
    title = "Flash Flood",
    description = "Heavy rainfall has caused flash flooding in the area. Several streets are submerged.",
    location = "Jakarta, Indonesia",
    type = DisasterType.FLOOD,
    timestamp = System.currentTimeMillis(),
    affectedCount = 250,
    images = listOf(),
    status = Disaster.Status.VERIFIED,
    latitude = -6.2088,
    longitude = 106.8456,
    ReportedBy = "system"
)

// Preview function
@Composable
fun MapScreenPreview() {
    MapScreen(
        disasters = listOf(mockDisaster),
        onDisasterClick = {},
        onBackClick = {},
        onFilterChange = {}
    )
}