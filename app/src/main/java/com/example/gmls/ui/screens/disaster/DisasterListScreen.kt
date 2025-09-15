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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.gmls.domain.model.Disaster
import com.example.gmls.domain.model.DisasterType
import com.example.gmls.domain.model.displayName
import com.example.gmls.domain.model.getDisplayName
import com.example.gmls.ui.components.DisasterReportFAB
import com.example.gmls.ui.components.DisasterTypeChip
import com.example.gmls.ui.theme.Red
import com.example.gmls.ui.components.GlobalSnackbarHost
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource
import com.example.gmls.R
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.ui.text.font.FontWeight
import com.example.gmls.ui.theme.Info
import com.example.gmls.ui.theme.Success
import com.example.gmls.ui.theme.Warning
import java.util.*
import androidx.compose.material.icons.outlined.Close
import androidx.compose.ui.window.Dialog
import java.text.SimpleDateFormat
import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.outlined.Info
import com.example.gmls.ui.screens.dashboard.DashboardStatusCard
import com.example.gmls.ui.components.EnhancedLoadingIndicator
import com.example.gmls.ui.components.EmptyStateComponent
import com.example.gmls.ui.components.ErrorStateComponent
import com.example.gmls.ui.components.SkeletonListItem
import androidx.hilt.navigation.compose.hiltViewModel

// Add time range enum for filtering
private enum class TimeRange(val displayName: String) {
    LAST_7_DAYS("7 Hari Terakhir"),
    LAST_30_DAYS("30 Hari Terakhir"),
    ALL_TIME("Semua Waktu"),
    CUSTOM("Kustom"),
    TODAY("Hari Ini"),
    WEEK("Minggu Ini"),
    MONTH("Bulan Ini")
}

// Helper functions for time filtering
private fun isToday(timestamp: Long): Boolean {
    val today = Calendar.getInstance()
    val date = Calendar.getInstance().apply { timeInMillis = timestamp }
    return today.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
           today.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR)
}

private fun isThisWeek(timestamp: Long): Boolean {
    val now = System.currentTimeMillis()
    val weekAgo = now - 7 * 24 * 60 * 60 * 1000L
    return timestamp >= weekAgo
}

private fun isThisMonth(timestamp: Long): Boolean {
    val thisMonth = Calendar.getInstance()
    val date = Calendar.getInstance().apply { timeInMillis = timestamp }
    return thisMonth.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
           thisMonth.get(Calendar.MONTH) == date.get(Calendar.MONTH)
}

// Disaster severity enum
enum class DisasterSeverity(val displayName: String) {
    LOW("Rendah"), 
    MEDIUM("Sedang"), 
    HIGH("Tinggi"), 
    CRITICAL("Kritis")
}

// Helper function to determine disaster severity
private fun getDisasterSeverity(disaster: Disaster): DisasterSeverity {
    return when (disaster.type) {
        DisasterType.TSUNAMI, DisasterType.EARTHQUAKE -> DisasterSeverity.CRITICAL
        DisasterType.WILDFIRE, DisasterType.HURRICANE -> DisasterSeverity.HIGH
        DisasterType.FLOOD -> DisasterSeverity.MEDIUM
        else -> DisasterSeverity.LOW
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisasterListScreen(
    disasters: List<Disaster>,
    onDisasterClick: (Disaster) -> Unit,
    onBackClick: () -> Unit,
    onReportDisaster: () -> Unit,
    isLoading: Boolean = false
) {
    var selectedType by remember { mutableStateOf<DisasterType?>(null) }
    var selectedTimeRange by remember { mutableStateOf(TimeRange.ALL_TIME) }
    var selectedSeverity by remember { mutableStateOf<DisasterSeverity?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showFilters by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()

    // Show loading screen for initial load
    if (isLoading && disasters.isEmpty()) {
        EnhancedLoadingIndicator(
            message = stringResource(R.string.loading_disasters)
        )
        return
    }

    val filteredDisasters = remember(disasters, selectedType, selectedTimeRange, selectedSeverity, searchQuery) {
        disasters.filter { disaster ->
            val matchesType = selectedType == null || disaster.type == selectedType
            val matchesTimeRange = when (selectedTimeRange) {
                TimeRange.TODAY -> isToday(disaster.timestamp)
                TimeRange.WEEK -> isThisWeek(disaster.timestamp)
                TimeRange.MONTH -> isThisMonth(disaster.timestamp)
                TimeRange.ALL_TIME -> true
                else -> true
            }
            val matchesSeverity = selectedSeverity == null || getDisasterSeverity(disaster) == selectedSeverity
            val matchesSearch = searchQuery.isEmpty() || 
                disaster.location.contains(searchQuery, ignoreCase = true) ||
                disaster.type.name.contains(searchQuery, ignoreCase = true) ||
                disaster.description.contains(searchQuery, ignoreCase = true)
            
            matchesType && matchesTimeRange && matchesSeverity && matchesSearch
        }
    }

    val reportedCount = filteredDisasters.count { it.status == Disaster.Status.REPORTED }
    val resolvedCount = filteredDisasters.count { it.status == Disaster.Status.RESOLVED }
    val inProgressCount = filteredDisasters.count { it.status == Disaster.Status.IN_PROGRESS }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                    title = {
                    Text(
                        stringResource(R.string.disasters_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    ) 
                },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.go_back)
                            )
                        }
                    },
                    actions = {
                    IconButton(onClick = { showFilters = !showFilters }) {
                            Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = stringResource(R.string.filter_disasters),
                            tint = if (showFilters) Red else LocalContentColor.current
                            )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(stringResource(R.string.report_disaster_button)) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                onClick = onReportDisaster,
                containerColor = Red,
                contentColor = Color.White
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
                // Search bar
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text(stringResource(R.string.search_disasters_placeholder)) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        },
                        trailingIcon = if (searchQuery.isNotEmpty()) {
                            {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.clear_search))
                                }
                            }
                        } else null,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                // Filter chips (show when filters are expanded)
                if (showFilters) {
            item {
                        FilterSection(
                            selectedType = selectedType,
                            onTypeSelected = { selectedType = it },
                            selectedTimeRange = selectedTimeRange,
                            onTimeRangeSelected = { selectedTimeRange = it },
                            selectedSeverity = selectedSeverity,
                            onSeveritySelected = { selectedSeverity = it }
                        )
                    }
                }

                // Statistics cards
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DashboardStatusCard(
                            title = stringResource(R.string.reported),
                            count = reportedCount,
                            icon = Icons.Default.Flag,
                            color = Warning,
                            tooltip = stringResource(R.string.recently_reported_disasters),
                            modifier = Modifier.weight(1f)
                        )
                        DashboardStatusCard(
                            title = stringResource(R.string.in_progress),
                            count = inProgressCount,
                            icon = Icons.Default.LocationOn,
                            color = Info,
                            tooltip = stringResource(R.string.disasters_being_handled),
                            modifier = Modifier.weight(1f)
                        )
                        DashboardStatusCard(
                            title = stringResource(R.string.resolved),
                            count = resolvedCount,
                            icon = Icons.Default.CheckCircle,
                            color = Success,
                            tooltip = stringResource(R.string.successfully_resolved_disasters),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Results count
            item {
                Text(
                        text = "${filteredDisasters.size} disaster(s) found",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                // Empty state
                if (filteredDisasters.isEmpty() && !isLoading) {
                    item {
                        EmptyStateComponent(
                            title = if (searchQuery.isNotEmpty()) stringResource(R.string.no_disasters_found_list) else stringResource(R.string.no_disasters_reported),
                            description = if (searchQuery.isNotEmpty()) {
                                stringResource(R.string.try_adjusting_search_filters)
                            } else {
                                stringResource(R.string.no_disasters_reported_area)
                            },
                            icon = if (searchQuery.isNotEmpty()) Icons.Default.SearchOff else Icons.Default.Warning,
                            actionText = stringResource(R.string.report_disaster_button),
                            onActionClick = onReportDisaster
                        )
                }
            } else {
                    // Disaster list
                    items(filteredDisasters, key = { it.id }) { disaster ->
                    DisasterListItem(
                        disaster = disaster,
                        onClick = { onDisasterClick(disaster) }
                    )
                    }
                }
            }
        }
    }
}

@Composable
fun DisasterListItem(
    disaster: Disaster,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val disasterColor = when (disaster.type) {
        DisasterType.EARTHQUAKE -> Color(0xFFE57373)
        DisasterType.FLOOD -> Color(0xFF64B5F6)
        DisasterType.WILDFIRE -> Color(0xFFFFB74D)
        DisasterType.LANDSLIDE -> Color(0xFF8D6E63)
        DisasterType.VOLCANO -> Color(0xFFFF8A65)
        DisasterType.TSUNAMI -> Color(0xFF4FC3F7)
        DisasterType.HURRICANE -> Color(0xFF9575CD)
        DisasterType.TORNADO -> Color(0xFF7986CB)
        DisasterType.OTHER -> Color(0xFF90A4AE)
    }

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

    ElevatedCard(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp, pressedElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Disaster type icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(disasterColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = disasterIcon,
                    contentDescription = null,
                    tint = disasterColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Disaster details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
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
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = disaster.location,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.width(2.dp))

                    Text(
                        text = disaster.formattedTimestamp,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(
                        imageVector = Icons.Filled.People,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.width(2.dp))

                    Text(
                        text = stringResource(R.string.affected_count, disaster.affectedCount),
                        style = MaterialTheme.typography.labelSmall,
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
            "Dilaporkan"
        )
        Disaster.Status.VERIFIED -> Triple(
            Color(0xFF90CAF9).copy(alpha = 0.3f),  // Light Blue background
            Color(0xFF1976D2),                     // Blue text
            "Terverifikasi"
        )
        Disaster.Status.IN_PROGRESS -> Triple(
            Color(0xFFFFCC80).copy(alpha = 0.3f),  // Light Orange background
            Color(0xFFFF9800),                     // Orange text
            "Sedang Berlangsung"
        )
        Disaster.Status.RESOLVED -> Triple(
            Color(0xFFA5D6A7).copy(alpha = 0.3f),  // Light Green background
            Color(0xFF43A047),                     // Green text
            "Teratasi"
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

@Composable
private fun FilterSection(
    selectedType: DisasterType?,
    onTypeSelected: (DisasterType?) -> Unit,
    selectedTimeRange: TimeRange,
    onTimeRangeSelected: (TimeRange) -> Unit,
    selectedSeverity: DisasterSeverity?,
    onSeveritySelected: (DisasterSeverity?) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Disaster type filters
        Text(
                            "Jenis Bencana",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedType == null,
                    onClick = { onTypeSelected(null) },
                    label = { Text(stringResource(R.string.all_types_filter)) }
                )
            }
            items(DisasterType.values()) { type ->
                FilterChip(
                    selected = selectedType == type,
                    onClick = { onTypeSelected(if (selectedType == type) null else type) },
                    label = { Text(type.getDisplayName()) }
                )
            }
        }

        // Time range filters
        Text(
                            "Rentang Waktu",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(listOf(TimeRange.ALL_TIME, TimeRange.TODAY, TimeRange.WEEK, TimeRange.MONTH)) { range ->
                FilterChip(
                    selected = selectedTimeRange == range,
                    onClick = { onTimeRangeSelected(range) },
                    label = { Text(range.displayName) }
                )
            }
        }

        // Severity filters
        Text(
            "Severity",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedSeverity == null,
                    onClick = { onSeveritySelected(null) },
                    label = { Text(stringResource(R.string.all_levels_filter)) }
                )
            }
            items(DisasterSeverity.values()) { severity ->
                FilterChip(
                    selected = selectedSeverity == severity,
                    onClick = { onSeveritySelected(if (selectedSeverity == severity) null else severity) },
                    label = { Text(severity.displayName) }
                )
            }
        }
    }
}

@Composable
private fun DisasterCard(
    disaster: Disaster,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
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
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = disaster.location,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = remember(disaster.timestamp) {
                            val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                            dateFormat.format(Date(disaster.timestamp))
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Status chip
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = when (disaster.status) {
                        Disaster.Status.REPORTED -> Warning.copy(alpha = 0.1f)
                        Disaster.Status.VERIFIED -> Info.copy(alpha = 0.1f)
                        Disaster.Status.IN_PROGRESS -> Red.copy(alpha = 0.1f)
                        Disaster.Status.RESOLVED -> Success.copy(alpha = 0.1f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Text(
                        text = when (disaster.status) {
                            Disaster.Status.REPORTED -> "Dilaporkan"
                            Disaster.Status.VERIFIED -> "Terverifikasi"
                            Disaster.Status.IN_PROGRESS -> "Sedang Berlangsung"
                            Disaster.Status.RESOLVED -> "Teratasi"
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = when (disaster.status) {
                            Disaster.Status.REPORTED -> Warning
                            Disaster.Status.VERIFIED -> Info
                            Disaster.Status.IN_PROGRESS -> Red
                            Disaster.Status.RESOLVED -> Success
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
            
            if (disaster.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = disaster.description.take(100) + if (disaster.description.length > 100) "..." else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
