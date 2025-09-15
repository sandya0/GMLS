package com.example.gmls.ui.screens.debug

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.gmls.R
import com.example.gmls.utils.CrashPrevention
import com.example.gmls.utils.CrashLog
import com.example.gmls.utils.CrashStats

/**
 * Debug screen for viewing crash reports and system health
 * Only available in debug builds
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrashReportScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var crashStats by remember { mutableStateOf<CrashStats?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showClearDialog by remember { mutableStateOf(false) }

    // Load crash data
    LaunchedEffect(Unit) {
        try {
            crashStats = CrashPrevention.getCrashStats()
        } catch (e: Exception) {
            // Handle error loading crash stats
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.crash_reports_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showClearDialog = true },
                        enabled = crashStats?.crashCount ?: 0 > 0
                    ) {
                                                        Icon(Icons.Default.Delete, contentDescription = "Hapus data crash")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // System Health Overview
                item {
                    SystemHealthCard(crashStats)
                }

                // Crash Statistics
                item {
                    CrashStatsCard(crashStats)
                }

                // Crash Logs
                crashStats?.crashLogs?.let { logs ->
                    if (logs.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.recent_crashes_title),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        items(logs) { crashLog ->
                            CrashLogCard(crashLog)
                        }
                    }
                }

                // No crashes message
                if (crashStats?.crashLogs?.isEmpty() == true) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(R.string.no_crashes_recorded),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = stringResource(R.string.app_running_smoothly),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Clear confirmation dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(stringResource(R.string.clear_crash_data_title)) },
            text = { Text(stringResource(R.string.clear_crash_data_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        CrashPrevention.clearCrashData()
                        crashStats = CrashPrevention.getCrashStats()
                        showClearDialog = false
                    }
                ) {
                    Text(stringResource(R.string.clear_button))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun SystemHealthCard(crashStats: CrashStats?) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isHealthy = (crashStats?.crashCount ?: 0) < 3
                Icon(
                    if (isHealthy) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isHealthy) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.system_health),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            val healthStatus = when {
                crashStats == null -> stringResource(R.string.unknown_status)
                crashStats.crashCount == 0 -> stringResource(R.string.excellent_status)
                crashStats.crashCount < 3 -> stringResource(R.string.good_status)
                crashStats.crashCount < 5 -> stringResource(R.string.fair_status)
                else -> stringResource(R.string.poor_status)
            }
            
            Text(
                text = stringResource(R.string.status_prefix, healthStatus),
                style = MaterialTheme.typography.bodyLarge
            )
            
            if (CrashPrevention.isAppUnstable()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.app_unstable_warning),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun CrashStatsCard(crashStats: CrashStats?) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.crash_statistics),
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    label = stringResource(R.string.total_crashes),
                    value = "${crashStats?.crashCount ?: 0}"
                )
                StatItem(
                    label = stringResource(R.string.last_crash),
                    value = crashStats?.getLastCrashFormatted() ?: stringResource(R.string.never)
                )
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun CrashLogCard(crashLog: CrashLog) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = crashLog.exception,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = crashLog.source,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = crashLog.getFormattedTime(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) stringResource(R.string.collapse) else stringResource(R.string.expand)
                    )
                }
            }
            
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = stringResource(R.string.message_label),
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = crashLog.message.ifEmpty { stringResource(R.string.no_message_default) },
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 8.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = stringResource(R.string.stack_trace_label),
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = crashLog.stackTrace,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(start = 8.dp),
                    maxLines = if (expanded) Int.MAX_VALUE else 5,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
} 
