package com.example.gmls.ui.screens.disaster

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.gmls.domain.model.Disaster
import com.example.gmls.domain.model.DisasterType
import com.example.gmls.ui.components.DisasterReportFAB
import com.example.gmls.ui.components.DisasterTypeChip
import com.example.gmls.ui.theme.Red

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisasterListScreen(
    disasters: List<Disaster>,
    onDisasterClick: (Disaster) -> Unit,
    onBackClick: () -> Unit,
    onReportDisaster: () -> Unit = {},
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    var selectedDisasterType by remember { mutableStateOf<DisasterType?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredDisasters = remember(disasters, selectedDisasterType, searchQuery) {
        var result = disasters

        // Filter by type if selected
        if (selectedDisasterType != null) {
            result = result.filter { it.type == selectedDisasterType }
        }

        // Filter by search query if not empty
        if (searchQuery.isNotEmpty()) {
            val query = searchQuery.lowercase()
            result = result.filter {
                it.title.lowercase().contains(query) ||
                        it.description.lowercase().contains(query) ||
                        it.location.lowercase().contains(query)
            }
        }

        result
    }

    val disasterTypes = remember { DisasterType.values().toList() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Disaster Reports") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Go back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Implement search toggle */ }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search disasters"
                        )
                    }

                    IconButton(onClick = { /* Implement filter menu */ }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter disasters"
                        )
                    }
                }
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
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Search bar (animated visibility controlled by search button)
                AnimatedVisibility(
                    visible = false, // Change to search state
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        placeholder = { Text("Search disasters...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear search"
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Red,
                            focusedLabelColor = Red,
                            cursorColor = Red
                        )
                    )
                }

                // Disaster type filter chips
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        DisasterTypeChip(
                            text = "All",
                            selected = selectedDisasterType == null,
                            onClick = { selectedDisasterType = null }
                        )
                    }

                    items(disasterTypes) { type ->
                        DisasterTypeChip(
                            text = type.displayName,
                            selected = selectedDisasterType == type,
                            onClick = { selectedDisasterType = type }
                        )
                    }
                }

                // Disaster list
                if (filteredDisasters.isEmpty()) {
                    // Empty state
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = if (searchQuery.isNotEmpty())
                                    "No disasters found matching \"$searchQuery\""
                                else if (selectedDisasterType != null)
                                    "No ${selectedDisasterType!!.displayName} disasters reported"
                                else
                                    "No disasters reported yet",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = onReportDisaster,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Red
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text("Report a Disaster")
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredDisasters) { disaster ->
                            DisasterListItem(
                                disaster = disaster,
                                onClick = { onDisasterClick(disaster) }
                            )
                        }
                    }
                }
            }

            // Loading indicator
            if (isLoading) {
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
fun DisasterListItem(
    disaster: Disaster,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val disasterColor = DisasterType.getColorForType(disaster.type)
    val disasterIcon = DisasterType.getIconForType(disaster.type)

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
            // Disaster type icon
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

            // Disaster details
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

            // Status badge
            DisasterStatusBadge(disaster.status)
        }
    }
}

@Composable
fun DisasterStatusBadge(status: Disaster.Status) {
    val (backgroundColor, textColor, label) = when (status) {
        Disaster.Status.REPORTED -> Triple(
            Color(0xFFFFF59D).copy(alpha = 0.3f),  // Light Yellow background
            Color(0xFFFFD600),                     // Amber text
            "Reported"
        )
        Disaster.Status.VERIFIED -> Triple(
            Color(0xFF90CAF9).copy(alpha = 0.3f),  // Light Blue background
            Color(0xFF1976D2),                     // Blue text
            "Verified"
        )
        Disaster.Status.IN_PROGRESS -> Triple(
            Color(0xFFFFCC80).copy(alpha = 0.3f),  // Light Orange background
            Color(0xFFFF9800),                     // Orange text
            "In Progress"
        )
        Disaster.Status.RESOLVED -> Triple(
            Color(0xFFA5D6A7).copy(alpha = 0.3f),  // Light Green background
            Color(0xFF43A047),                     // Green text
            "Resolved"
        )
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