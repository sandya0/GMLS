package com.example.gmls.ui.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.gmls.R
import com.example.gmls.domain.model.Disaster
import com.example.gmls.domain.model.DisasterType
import com.example.gmls.domain.model.getDisplayName
import com.example.gmls.domain.model.User
import com.example.gmls.ui.components.DisasterTypeChip
import com.example.gmls.ui.components.EmergencyAlertCard
import com.example.gmls.ui.components.EmergencySeverity
import com.example.gmls.ui.components.LogoutConfirmationDialog
import com.example.gmls.ui.components.LocationTrackingCard
import com.example.gmls.ui.components.EnhancedLoadingIndicator
import com.example.gmls.ui.components.EmptyStateComponent
import com.example.gmls.ui.components.ErrorStateComponent
import com.example.gmls.ui.components.SuccessAnimation
import com.example.gmls.ui.theme.Info
import com.example.gmls.ui.theme.Red
import com.example.gmls.ui.theme.Success
import com.example.gmls.ui.theme.Warning
import com.example.gmls.ui.viewmodels.ProfileViewModel
import com.example.gmls.ui.viewmodels.DashboardViewModel
import com.example.gmls.ui.viewmodels.NotificationViewModel
import java.util.*
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.Surface
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Badge

// Helper function to determine EmergencySeverity based on DisasterType
fun getEmergencySeverityForDisasterType(disasterType: DisasterType): EmergencySeverity {
    return when (disasterType) {
        DisasterType.FLOOD -> EmergencySeverity.HIGH
        DisasterType.EARTHQUAKE -> EmergencySeverity.CRITICAL
        DisasterType.WILDFIRE -> EmergencySeverity.HIGH
        DisasterType.LANDSLIDE -> EmergencySeverity.MEDIUM
        DisasterType.VOLCANO -> EmergencySeverity.CRITICAL
        DisasterType.TSUNAMI -> EmergencySeverity.CRITICAL
        DisasterType.HURRICANE -> EmergencySeverity.HIGH
        DisasterType.TORNADO -> EmergencySeverity.HIGH
        DisasterType.OTHER -> EmergencySeverity.MEDIUM
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    disasters: List<Disaster>,
    onDisasterClick: (Disaster) -> Unit,
    onProfileClick: () -> Unit,
    onMenuClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onReportDisaster: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
    dashboardViewModel: DashboardViewModel = hiltViewModel(),
    notificationViewModel: NotificationViewModel = hiltViewModel()
) {
    val profileState by viewModel.profileState.collectAsState()
    val dashboardState by dashboardViewModel.dashboardState.collectAsState()
    val user = profileState.user
    val scope = rememberCoroutineScope()
    
    // State for search functionality
    var isSearchMode by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
    // State for dismissed emergency alerts
    var dismissedAlerts by remember { mutableStateOf(setOf<String>()) }
    
    // Show loading screen for initial load
    if (profileState.isLoading && user == null) {
        EnhancedLoadingIndicator(
            message = stringResource(R.string.loading_dashboard)
        )
        return
    }

    // Show error state if profile failed to load
    if (profileState.error != null && user == null) {
        ErrorStateComponent(
            title = stringResource(R.string.failed_to_load_dashboard),
            message = profileState.error ?: stringResource(R.string.unknown_error_occurred),
            onRetry = { viewModel.refreshProfile() }
        )
        return
    }

    val filteredDisasters = remember(disasters, dashboardState.selectedDisasterType) {
        if (dashboardState.selectedDisasterType == null) {
            disasters.take(5) // Show only recent disasters
        } else {
            disasters.filter { it.type == dashboardState.selectedDisasterType }.take(5)
        }
    }

    val displayedDisasters = remember(filteredDisasters, searchQuery) {
        if (searchQuery.isBlank()) {
            filteredDisasters
        } else {
            filteredDisasters.filter { disaster ->
                disaster.location.contains(searchQuery, ignoreCase = true) ||
                disaster.type.name.contains(searchQuery, ignoreCase = true) ||
                disaster.description.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            if (isSearchMode) {
                SearchTopAppBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { 
                        searchQuery = it
                        dashboardViewModel.updateSearchQuery(it)
                    },
                    onCloseClick = { 
                        isSearchMode = false
                        searchQuery = ""
                        dashboardViewModel.updateSearchQuery("")
                    },
                    isSearching = dashboardState.isSearching
                )
            } else {
                DashboardTopAppBar(
                    onMenuClick = onMenuClick,
                    onSearchClick = { isSearchMode = true },
                    onNotificationsClick = onNotificationsClick,
                    onRefreshClick = { dashboardViewModel.refreshData() },
                    isRefreshing = dashboardState.isRefreshing,
                    notificationCount = 0 // You can get this from notificationViewModel if needed
                )
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(stringResource(R.string.report_disaster_fab)) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                onClick = onReportDisaster,
                containerColor = Red,
                contentColor = Color.White
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (isSearchMode) {
                // Search mode content
                if (searchQuery.isNotEmpty()) {
                    SearchResultsSection(
                        searchResults = dashboardState.searchResults,
                        isSearching = dashboardState.isSearching,
                        onDisasterClick = onDisasterClick,
                        modifier = Modifier.padding(paddingValues)
                    )
                } else {
                    // Show search suggestions or empty state
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Mulai mengetik untuk mencari bencana...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                // Normal dashboard content
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        // Profile section
                        ProfileSection(
                            user = user,
                            onProfileClick = onProfileClick,
                            onLogoutClick = onLogoutClick
                        )
                    }

                    item {
                        // Location tracking section
                        LocationTrackingCard(
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    }

                    // Emergency alerts section
                    item {
                        EmergencyAlertsSection(
                            disasters = disasters,
                            onDisasterClick = onDisasterClick,
                            dismissedAlerts = dismissedAlerts,
                            onDismissAlert = { disasterId ->
                                dismissedAlerts = dismissedAlerts + disasterId
                            }
                        )
                    }

                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    }

                    // Recent disasters section
                    item {
                        Text(
                            "Bencana Terbaru",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(displayedDisasters, key = { it.id }) { disaster ->
                        DisasterCard(
                            disaster = disaster,
                            onClick = { onDisasterClick(disaster) }
                        )
                    }

                    if (displayedDisasters.isEmpty()) {
                        item {
                            EmptyStateComponent(
                                title = stringResource(R.string.no_disasters_found_dashboard),
                                description = if (searchQuery.isNotEmpty()) {
                                    stringResource(R.string.no_disasters_match_criteria)
                                } else {
                                    stringResource(R.string.no_recent_disasters_area)
                                },
                                icon = Icons.Default.Search
                            )
                        }
                    }
                }
            }

            // Success animation for actions
            if (dashboardState.showSuccessMessage) {
                SuccessAnimation(
                    message = stringResource(R.string.action_completed_successfully),
                    onComplete = { dashboardViewModel.clearSuccessMessage() },
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
private fun ProfileSection(
    user: User?,
    onProfileClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Enhanced profile card with modern design
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onProfileClick)
            .semantics(mergeDescendants = true) {
                                    contentDescription = "Bagian profil. Selamat datang ${user?.fullName ?: "Pengguna"}. Ketuk untuk melihat atau edit profil."
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Red.copy(alpha = 0.1f),
                            Red.copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    )
                )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                    .padding(20.dp)
        ) {
                // Enhanced profile avatar with gradient background
            Box(
                modifier = Modifier
                        .size(72.dp)
                    .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Red,
                                    Red.copy(alpha = 0.8f)
                                )
                            ),
                            shape = CircleShape
                        )
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = CircleShape
                            )
                            .padding(2.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Red,
                                        Red.copy(alpha = 0.9f)
                                    )
                                ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user?.fullName?.take(2)?.uppercase() ?: "U",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = Color.White
                    )
                }
            }
                
                Spacer(modifier = Modifier.width(20.dp))
                
                // Enhanced user info section
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                Text(
                    text = stringResource(R.string.welcome_back),
                        style = MaterialTheme.typography.labelLarge.copy(
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
                Text(
                    text = user?.fullName ?: stringResource(R.string.user),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.25.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    // Status indicator row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        // Verification status
                        if (user?.isVerified == true) {
                            Surface(
                                shape = CircleShape,
                                color = Success.copy(alpha = 0.2f),
                                modifier = Modifier.padding(0.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Terverifikasi",
                                        tint = Success,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Terverifikasi",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = Success
                                    )
                                }
                            }
                        }
                        
                        // Active status
                        Surface(
                            shape = CircleShape,
                            color = if (user?.isActive == true) 
                                Success.copy(alpha = 0.2f) 
                            else 
                                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            color = if (user?.isActive == true) Success else Red,
                                            shape = CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (user?.isActive == true) "Aktif" else "Tidak Aktif",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = if (user?.isActive == true) Success else Red
                                )
                            }
                        }
                    }
                }
                
                // Enhanced logout button
                Surface(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                    onClick = { showLogoutDialog = true }
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .semantics { contentDescription = "Keluar" }
            ) {
                Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = null,
                            tint = Red,
                            modifier = Modifier.size(20.dp)
                )
                    }
                }
            }
        }
    }

    if (showLogoutDialog) {
        LogoutConfirmationDialog(
            onConfirm = {
                showLogoutDialog = false
                onLogoutClick()
            },
            onDismiss = {
                showLogoutDialog = false
            }
        )
    }
}

@Composable
private fun DashboardStatusSection(disasters: List<Disaster>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp) // Spacing between cards
    ) {
        DashboardStatusCard(
            title = stringResource(R.string.reported),
            count = disasters.count { it.status == Disaster.Status.REPORTED },
            icon = Icons.Default.Flag,
            color = Warning, // Use theme color
            tooltip = stringResource(R.string.disasters_you_reported),
            modifier = Modifier.weight(1f)
        )
        DashboardStatusCard(
            title = stringResource(R.string.resolved),
            count = disasters.count { it.status == Disaster.Status.RESOLVED },
            icon = Icons.Default.CheckCircle,
            color = Success, // Use theme color
            tooltip = stringResource(R.string.disasters_resolved),
            modifier = Modifier.weight(1f)
        )
        DashboardStatusCard(
            title = stringResource(R.string.in_progress),
            count = disasters.count { it.status == Disaster.Status.IN_PROGRESS },
            icon = Icons.Default.LocationOn, // Or a more suitable icon for "In Progress"
            color = Info,    // Use theme color
            tooltip = stringResource(R.string.disasters_in_progress),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun DisasterFilterSection(
    disasterTypes: List<DisasterType>,
    selectedDisasterType: DisasterType?,
    onTypeSelected: (DisasterType?) -> Unit
) {
    Column {
        Text(
            text = stringResource(R.string.filter_by_disaster_type),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp) // Add some vertical padding for chips
        ) {
            item {
                DisasterTypeChip(
                    text = stringResource(R.string.all),
                    selected = selectedDisasterType == null,
                    onClick = { onTypeSelected(null) }
                )
            }
            items(disasterTypes) { type ->
                DisasterTypeChip(
                    // Using localized display name
                    text = type.getDisplayName(),
                    selected = selectedDisasterType == type,
                    onClick = { onTypeSelected(type) }
                )
            }
        }
    }
}

@Composable
private fun RecentDisastersSection(
    filteredDisasters: List<Disaster>,
    onDisasterClick: (Disaster) -> Unit
) {
    Text(
        text = stringResource(R.string.recent_disasters),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )
    if (filteredDisasters.isEmpty()) {
        Text(
            text = stringResource(R.string.no_disasters_reported_in_category),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp)
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(), // This might be too much if inside a Column that's not weighted.
            // Consider a fixed height or weight if it's inside a scrollable Column.
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredDisasters, key = { it.id }) { disaster -> // Add key for better performance
                // AnimatedVisibility with visible = true provides an "enter" animation for items.
                // This is generally fine if that's the desired effect for all items.
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(300)), // Adjusted duration
                    exit = fadeOut(animationSpec = tween(200))  // Adjusted duration
                ) {
                    DisasterCard(
                        disaster = disaster,
                        onClick = { onDisasterClick(disaster) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardTopAppBar(
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onRefreshClick: () -> Unit,
    isRefreshing: Boolean,
    notificationCount: Int,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    // Enhanced top app bar with modern design
    CenterAlignedTopAppBar(
        title = {
            // Simplified GMLS title only
                Text(
                text = "GMLS",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp
                ),
                color = Red,
                modifier = Modifier.semantics {
                    contentDescription = "GMLS - Gugus Mitigasi Lebak Selatan"
                }
            )
        },
        navigationIcon = {
            // Enhanced menu button with modern styling
            Surface(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                onClick = onMenuClick,
                interactionSource = interactionSource
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                        contentDescription = stringResource(R.string.menu),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                )
                }
            }
        },
        actions = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                // Enhanced refresh button with loading state
                Surface(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape),
                    color = if (isRefreshing) 
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    else 
                        Color.Transparent,
                    onClick = if (!isRefreshing) onRefreshClick else { {} }
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
            ) {
                if (isRefreshing) {
                    CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Red
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.refresh),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
                
                // Enhanced search button
                Surface(
                        modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape),
                    color = Color.Transparent,
                    onClick = onSearchClick
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(R.string.search),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                
                // Enhanced notifications with modern badge design
                Surface(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape),
                    color = if (notificationCount > 0) 
                        Red.copy(alpha = 0.1f) 
                    else 
                        Color.Transparent,
                    onClick = onNotificationsClick
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                if (notificationCount > 0) {
                    BadgedBox(
                        badge = {
                                    Badge(
                                        containerColor = Red,
                                        contentColor = Color.White,
                                        modifier = Modifier.semantics { 
                                            contentDescription = "$notificationCount new notifications" 
                                        }
                                    ) {
                                Text(
                                            text = if (notificationCount > 99) "99+" else notificationCount.toString(),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                )
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = Red,
                                    modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    Icon(
                                imageVector = Icons.Default.NotificationsNone,
                        contentDescription = stringResource(R.string.notifications),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                    )
                        }
                    }
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
                    )
                )
        ),
        scrollBehavior = scrollBehavior
    )
}

@Composable
fun DashboardStatusCard(
    title: String,
    count: Int,
    icon: ImageVector,
    color: Color,
    tooltip: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(horizontal = 4.dp), // Keep padding if it's for individual card spacing within the Row
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth() // Ensure cards take equal space if weighted
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp)) // Title describes it
                Spacer(modifier = Modifier.width(4.dp))
                // Tooltip can be implemented with a clickable icon if needed,
                // or rely on TalkBack to read the `tooltip` from a `semantics` modifier on the Card.
                // For simplicity, the Info icon here acts as a visual cue.
                Icon(
                    Icons.Outlined.Info, // Assuming this is the standard info icon
                    contentDescription = tooltip, // This makes the tooltip accessible
                    tint = color.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = title, style = MaterialTheme.typography.bodyMedium, color = color)
            Text(text = count.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisasterCard(
    disaster: Disaster,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // It's good practice to derive these from a centralized mapping or theme extension
    // if they are used in multiple places or need to be theme-dependent.
    val disasterVisuals = remember(disaster.type) {
        when (disaster.type) {
            DisasterType.EARTHQUAKE -> Pair(Color(0xFFE57373), Icons.Filled.Bolt)
            DisasterType.FLOOD -> Pair(Color(0xFF64B5F6), Icons.Filled.Water)
            DisasterType.WILDFIRE -> Pair(Color(0xFFFFB74D), Icons.Filled.LocalFireDepartment)
            DisasterType.LANDSLIDE -> Pair(Color(0xFF8D6E63), Icons.Filled.Terrain)
            DisasterType.VOLCANO -> Pair(Color(0xFFFF8A65), Icons.Filled.Volcano)
            DisasterType.TSUNAMI -> Pair(Color(0xFF4FC3F7), Icons.Filled.Waves)
            DisasterType.HURRICANE -> Pair(Color(0xFF9575CD), Icons.Filled.Storm)
            DisasterType.TORNADO -> Pair(Color(0xFF7986CB), Icons.Filled.AirlineSeatFlatAngled) // Consider a more common tornado icon if available
            DisasterType.OTHER -> Pair(Color(0xFF90A4AE), Icons.Filled.Warning)
        }
    }
    val disasterColor = disasterVisuals.first
    val disasterIcon = disasterVisuals.second

    ElevatedCard(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant // Or surface for less elevation emphasis
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp, pressedElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(disasterColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = disasterIcon,
                    contentDescription = "${disaster.type.name.lowercase().replaceFirstChar { it.titlecase() }} icon", // Descriptive
                    tint = disasterColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = disaster.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = disaster.location,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AccessTime,
                        contentDescription = null, // Text describes it
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = disaster.getFormattedTimestamp(LocalContext.current),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp)) // Adjusted spacer
                    Icon(
                        imageVector = Icons.Filled.People,
                        contentDescription = null, // Text describes it
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.people_affected, disaster.affectedCount),
                        style = MaterialTheme.typography.labelSmall,
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
                shape = MaterialTheme.shapes.small // Consistent shape
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Medium // Slightly bolder for badges
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTopAppBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onCloseClick: () -> Unit,
    isSearching: Boolean,
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    TopAppBar(
        title = {
            TextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text(stringResource(R.string.search_disasters)) },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                modifier = Modifier.fillMaxWidth()
            )
        },
        navigationIcon = {
            IconButton(onClick = onCloseClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.close_search))
            }
        },
        actions = {
            if (isSearching) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(16.dp)
                        .size(24.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        scrollBehavior = scrollBehavior
    )
}

@Composable
fun SearchResultsSection(
    searchResults: List<Disaster>,
    isSearching: Boolean,
    onDisasterClick: (Disaster) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            isSearching -> {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            searchResults.isEmpty() -> {
                Text(
                    text = stringResource(R.string.no_disasters_found),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(searchResults, key = { it.id }) { disaster ->
                        DisasterCard(
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
private fun EmergencyAlertsSection(
    disasters: List<Disaster>,
    onDisasterClick: (Disaster) -> Unit,
    dismissedAlerts: Set<String>,
    onDismissAlert: (String) -> Unit
) {
    val emergencyDisasters = disasters.filter { disaster ->
        val severity = getEmergencySeverityForDisasterType(disaster.type)
        severity == EmergencySeverity.HIGH || severity == EmergencySeverity.CRITICAL
    }.take(3)

    if (emergencyDisasters.isNotEmpty()) {
        Column {
            Text(
                "Peringatan Darurat",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Red
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            emergencyDisasters.forEach { disaster ->
                if (disaster.id !in dismissedAlerts) {
                    EmergencyAlertCard(
                        title = "Peringatan ${disaster.type.getDisplayName()}",
                        message = "Situasi darurat dilaporkan di ${disaster.location}",
                        severity = getEmergencySeverityForDisasterType(disaster.type),
                        timestamp = Date(disaster.timestamp),
                        onClick = { onDisasterClick(disaster) },
                        onDismiss = { onDismissAlert(disaster.id) },
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmergencyAlertCard(
    title: String,
    message: String,
    severity: EmergencySeverity,
    timestamp: Date,
    onClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = when (severity) {
                EmergencySeverity.CRITICAL -> Red.copy(alpha = 0.15f)
                EmergencySeverity.HIGH -> Red.copy(alpha = 0.1f)
                else -> Warning.copy(alpha = 0.1f)
            }
        ),
        border = BorderStroke(
            1.dp, 
            when (severity) {
                EmergencySeverity.CRITICAL -> Red.copy(alpha = 0.4f)
                EmergencySeverity.HIGH -> Red.copy(alpha = 0.3f)
                else -> Warning.copy(alpha = 0.3f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Header with dismiss button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 12.dp, end = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Severity badge
                Surface(
                    color = when (severity) {
                        EmergencySeverity.CRITICAL -> Red.copy(alpha = 0.2f)
                        EmergencySeverity.HIGH -> Red.copy(alpha = 0.15f)
                        else -> Warning.copy(alpha = 0.2f)
                    },
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (severity) {
                                EmergencySeverity.CRITICAL -> Icons.Default.Warning
                                EmergencySeverity.HIGH -> Icons.Default.Warning
                                else -> Icons.Default.Info
                            },
                            contentDescription = null,
                            tint = when (severity) {
                                EmergencySeverity.CRITICAL -> Red
                                EmergencySeverity.HIGH -> Red
                                else -> Warning
                            },
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when (severity) {
                                EmergencySeverity.CRITICAL -> "Kritis"
                                EmergencySeverity.HIGH -> "Prioritas Tinggi"
                                else -> "Peringatan"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = when (severity) {
                                EmergencySeverity.CRITICAL -> Red
                                EmergencySeverity.HIGH -> Red
                                else -> Warning
                            },
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                // Dismiss button
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                                                                contentDescription = "Tutup peringatan",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            
            // Main content
            Row(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Alert icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = when (severity) {
                                EmergencySeverity.CRITICAL -> Red.copy(alpha = 0.2f)
                                EmergencySeverity.HIGH -> Red.copy(alpha = 0.15f)
                                else -> Warning.copy(alpha = 0.2f)
                            },
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (severity) {
                            EmergencySeverity.CRITICAL -> Icons.Default.Warning
                            EmergencySeverity.HIGH -> Icons.Default.Warning
                            else -> Icons.Default.Info
                        },
                        contentDescription = null,
                        tint = when (severity) {
                            EmergencySeverity.CRITICAL -> Red
                            EmergencySeverity.HIGH -> Red
                            else -> Warning
                        },
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Alert content
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = when (severity) {
                            EmergencySeverity.CRITICAL -> Red
                            EmergencySeverity.HIGH -> Red
                            else -> Warning
                        },
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // Navigate icon
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                                                            contentDescription = "Lihat detail",
                    tint = when (severity) {
                        EmergencySeverity.CRITICAL -> Red
                        EmergencySeverity.HIGH -> Red
                        else -> Warning
                    },
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
