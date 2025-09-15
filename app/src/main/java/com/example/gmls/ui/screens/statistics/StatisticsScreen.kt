package com.example.gmls.ui.screens.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.gmls.R
import com.example.gmls.domain.model.Disaster
import com.example.gmls.domain.model.DisasterType
import com.example.gmls.domain.model.getDisplayName
import com.example.gmls.ui.theme.Info
import com.example.gmls.ui.theme.Red
import com.example.gmls.ui.theme.Success
import com.example.gmls.ui.theme.Warning
import java.util.*
import androidx.compose.ui.draw.clip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    disasters: List<Disaster>,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null
) {
    var selectedDisasterType by remember { mutableStateOf<DisasterType?>(null) }
    var selectedTimeRange by remember { mutableStateOf(TimeRange.LAST_7_DAYS) }

    val filteredDisasters = remember(disasters, selectedDisasterType, selectedTimeRange) {
        disasters.filter { disaster ->
            (selectedDisasterType == null || disaster.type == selectedDisasterType) &&
            selectedTimeRange.filter(disaster)
        }
    }

    val reportedCount = filteredDisasters.count { it.status == Disaster.Status.REPORTED }
    val resolvedCount = filteredDisasters.count { it.status == Disaster.Status.RESOLVED }
    val inProgressCount = filteredDisasters.count { it.status == Disaster.Status.IN_PROGRESS }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.statistics),
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    if (onBackClick != null) {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Outlined.ArrowBack, contentDescription = stringResource(R.string.close_search))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            FilterSection(
                selectedDisasterType = selectedDisasterType,
                onDisasterTypeSelected = { selectedDisasterType = it },
                selectedTimeRange = selectedTimeRange,
                onTimeRangeSelected = { selectedTimeRange = it }
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatisticsCard(
                    title = stringResource(R.string.reported),
                    count = reportedCount,
                    icon = Icons.Default.Flag,
                    color = Warning,
                    modifier = Modifier.weight(1f)
                )
                StatisticsCard(
                    title = stringResource(R.string.resolved),
                    count = resolvedCount,
                    icon = Icons.Default.CheckCircle,
                    color = Success,
                    modifier = Modifier.weight(1f)
                )
                StatisticsCard(
                    title = stringResource(R.string.in_progress),
                    count = inProgressCount,
                    icon = Icons.Default.LocationOn,
                    color = Info,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.recent_disasters),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            if (filteredDisasters.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_disasters_reported_in_category),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredDisasters, key = { it.id }) { disaster ->
                        DisasterListCard(disaster = disaster)
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterSection(
    selectedDisasterType: DisasterType?,
    onDisasterTypeSelected: (DisasterType?) -> Unit,
    selectedTimeRange: TimeRange,
    onTimeRangeSelected: (TimeRange) -> Unit
) {
    Column {
        Text(text = stringResource(R.string.filter_disasters), style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = selectedDisasterType == null,
                    onClick = { onDisasterTypeSelected(null) },
                    label = { Text(stringResource(R.string.all)) }
                )
            }
            items(DisasterType.values().size) { idx ->
                val type = DisasterType.values()[idx]
                FilterChip(
                    selected = selectedDisasterType == type,
                    onClick = { onDisasterTypeSelected(type) },
                    label = { Text(type.getDisplayName()) }
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(TimeRange.values().size) { idx ->
                val range = TimeRange.values()[idx]
                FilterChip(
                    selected = selectedTimeRange == range,
                    onClick = { onTimeRangeSelected(range) },
                    label = { Text(range.displayName) }
                )
            }
        }
    }
}

@Composable
private fun StatisticsCard(
    title: String,
    count: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, style = MaterialTheme.typography.bodyMedium, color = color)
            Text(text = count.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DisasterListCard(disaster: Disaster) {
    val (color, icon) = when (disaster.type) {
        DisasterType.EARTHQUAKE -> Pair(Color(0xFFE57373), Icons.Filled.Bolt)
        DisasterType.FLOOD -> Pair(Color(0xFF64B5F6), Icons.Filled.Water)
        DisasterType.WILDFIRE -> Pair(Color(0xFFFFB74D), Icons.Filled.LocalFireDepartment)
        DisasterType.LANDSLIDE -> Pair(Color(0xFF8D6E63), Icons.Filled.Terrain)
        DisasterType.VOLCANO -> Pair(Color(0xFFFF8A65), Icons.Filled.Volcano)
        DisasterType.TSUNAMI -> Pair(Color(0xFF4FC3F7), Icons.Filled.Waves)
        DisasterType.HURRICANE -> Pair(Color(0xFF9575CD), Icons.Filled.Storm)
        DisasterType.TORNADO -> Pair(Color(0xFF7986CB), Icons.Filled.AirlineSeatFlatAngled)
        DisasterType.OTHER -> Pair(Color(0xFF90A4AE), Icons.Filled.Warning)
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = disaster.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = disaster.location,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = disaster.formattedTimestamp,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Filled.People,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.people_affected, disaster.affectedCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            StatusBadge(disaster.status)
        }
    }
}

@Composable
fun StatusBadge(status: Disaster.Status) {
    val (backgroundColor, textColor, label) = when (status) {
        Disaster.Status.REPORTED -> Triple(Warning.copy(alpha = 0.2f), Warning, stringResource(R.string.reported))
        Disaster.Status.VERIFIED -> Triple(Info.copy(alpha = 0.2f), Info, stringResource(R.string.verified))
        Disaster.Status.IN_PROGRESS -> Triple(Info.copy(alpha = 0.2f), Info, stringResource(R.string.in_progress))
        Disaster.Status.RESOLVED -> Triple(Success.copy(alpha = 0.2f), Success, stringResource(R.string.resolved))
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
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}

// Time range filter options
enum class TimeRange(val displayName: String, val filter: (Disaster) -> Boolean) {
    LAST_7_DAYS("7 Hari Terakhir", { disaster ->
        val now = System.currentTimeMillis()
        val sevenDaysAgo = now - 7 * 24 * 60 * 60 * 1000L
        disaster.timestamp >= sevenDaysAgo
    }),
    LAST_30_DAYS("30 Hari Terakhir", { disaster ->
        val now = System.currentTimeMillis()
        val thirtyDaysAgo = now - 30 * 24 * 60 * 60 * 1000L
        disaster.timestamp >= thirtyDaysAgo
    }),
    ALL_TIME("Semua Waktu", { _ -> true })
} 
