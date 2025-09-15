package com.example.gmls.ui.screens.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.gmls.R
import com.example.gmls.ui.components.PrimaryButton
import com.example.gmls.ui.theme.*
import com.example.gmls.ui.viewmodels.AnalyticsViewModel

/**
 * Main Analytics Screen with disaster response analytics based on actual GMLS data
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    onBackClick: () -> Unit,
    analyticsViewModel: AnalyticsViewModel = hiltViewModel(),
    isAdminView: Boolean = false
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    
    val analyticsState by analyticsViewModel.analyticsState.collectAsState()

    // Filter tabs based on user role
    val allTabs = listOf(
        AnalyticsTab("Ringkasan", Icons.Default.Dashboard),
        AnalyticsTab("Geographic", Icons.Default.Map),
        AnalyticsTab("Trends", Icons.Default.TrendingUp),
        AnalyticsTab("Users", Icons.Default.People)
    )
    
    val tabs = if (isAdminView) allTabs else allTabs.dropLast(1) // Remove Users tab for non-admin users

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.analytics_dashboard_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { analyticsViewModel.refreshData() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Segarkan"
                        )
                    }
                    IconButton(onClick = { analyticsViewModel.exportData() }) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "Ekspor"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Row
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = tab.title,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Medium
                                )
                            }
                        }
                    )
                }
            }

            // Content based on selected tab - Now with individual scrolling
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                when (selectedTab) {
                    0 -> OverviewAnalytics(analyticsState)
                    1 -> GeographicAnalytics(analyticsState)
                    2 -> TrendsAnalytics(analyticsState)
                    3 -> if (isAdminView) UserAnalytics(analyticsState) else TrendsAnalytics(analyticsState)
                }
            }
        }
    }
}

/**
 * Overview Analytics Tab
 */
@Composable
private fun OverviewAnalytics(analyticsState: AnalyticsState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        // Key Metrics Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.main_metrics),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.height(260.dp)
                ) {
                    item {
                        MetricCard(
                            title = "Total Bencana",
                            value = analyticsState.totalDisasters.toString(),
                            icon = Icons.Default.Warning,
                            color = Red,
                            trend = "${analyticsState.activeIncidents} aktif"
                        )
                    }
                    item {
                        MetricCard(
                            title = "Insiden Aktif",
                            value = analyticsState.activeIncidents.toString(),
                            icon = Icons.Default.Emergency,
                            color = Warning,
                            trend = "Sedang ditangani"
                        )
                    }
                    item {
                        MetricCard(
                            title = "Kasus Diselesaikan",
                            value = analyticsState.resolvedIncidents.toString(),
                            icon = Icons.Default.CheckCircle,
                            color = Success,
                            trend = "Berhasil ditangani"
                        )
                    }
                    item {
                        MetricCard(
                            title = "Orang Terdampak",
                            value = analyticsState.totalAffectedPeople.toString(),
                            icon = Icons.Default.People,
                            color = AccentBlue,
                            trend = "Rata-rata: ${analyticsState.avgAffectedCount}"
                        )
                    }
                }
            }
        }

        // Recent Activity
        Text(
            text = stringResource(R.string.recent_activity_analytics),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                if (analyticsState.recentActivities.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.no_recent_activity),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    analyticsState.recentActivities.forEach { activity ->
                        ActivityItem(activity)
                        if (activity != analyticsState.recentActivities.last()) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }
        }

        // Disaster Type Distribution
        Text(
            text = stringResource(R.string.disaster_type_distribution),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        DisasterTypeChart(analyticsState.disasterTypeDistribution)

        // Status Distribution
        Text(
            text = stringResource(R.string.status_distribution),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        StatusDistributionChart(analyticsState.statusDistribution)
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Geographic Analytics Tab
 */
@Composable
private fun GeographicAnalytics(analyticsState: AnalyticsState) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.geographic_distribution),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Location Statistics
            Text(
                text = stringResource(R.string.disasters_by_location),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (analyticsState.locationStats.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOff,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.no_location_data_available),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        if (analyticsState.locationStats.isNotEmpty()) {
            items(analyticsState.locationStats.size) { index ->
                LocationStatCard(analyticsState.locationStats[index])
            }
        }
    }
}

/**
 * Trends Analytics Tab
 */
@Composable
private fun TrendsAnalytics(analyticsState: AnalyticsState) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.disaster_trends),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Monthly Trends
            MonthlyTrendChart(analyticsState.monthlyTrends)
        }

        item {
            // Severity Distribution
            Text(
                text = stringResource(R.string.severity_distribution),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            SeverityDistributionChart(analyticsState.severityDistribution)
        }
    }
}

/**
 * User Analytics Tab
 */
@Composable
private fun UserAnalytics(analyticsState: AnalyticsState) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.user_analytics),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // User Statistics
            UserStatisticsCard(
                totalUsers = analyticsState.totalUsers,
                verifiedUsers = analyticsState.verifiedUsers,
                adminUsers = analyticsState.adminUsers,
                usersWithLocation = analyticsState.usersWithLocation
            )
        }

        item {
            // User Metrics
            Text(
                text = stringResource(R.string.user_metrics),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.height(160.dp)
            ) {
                item {
                    MetricCard(
                        title = stringResource(R.string.verification_rate),
                        value = if (analyticsState.totalUsers > 0) 
                            "${(analyticsState.verifiedUsers * 100) / analyticsState.totalUsers}%" 
                            else "0%",
                        icon = Icons.Default.Verified,
                        color = Success,
                        trend = "${analyticsState.verifiedUsers} ${stringResource(R.string.verified_text)}"
                    )
                }
                item {
                    MetricCard(
                        title = stringResource(R.string.location_enabled),
                        value = if (analyticsState.totalUsers > 0) 
                            "${(analyticsState.usersWithLocation * 100) / analyticsState.totalUsers}%" 
                            else "0%",
                        icon = Icons.Default.LocationOn,
                        color = AccentBlue,
                        trend = "${analyticsState.usersWithLocation} ${stringResource(R.string.users_text_analytics)}"
                    )
                }
                item {
                    MetricCard(
                        title = stringResource(R.string.admin_users_analytics),
                        value = analyticsState.adminUsers.toString(),
                        icon = Icons.Default.AdminPanelSettings,
                        color = Warning,
                        trend = stringResource(R.string.system_administrators)
                    )
                }
                item {
                    MetricCard(
                        title = stringResource(R.string.regular_users),
                        value = (analyticsState.totalUsers - analyticsState.adminUsers).toString(),
                        icon = Icons.Default.Person,
                        color = Gray700,
                        trend = stringResource(R.string.community_members)
                    )
                }
            }
        }
    }
}

// Data classes and helper composables
data class AnalyticsTab(
    val title: String,
    val icon: ImageVector
) 
