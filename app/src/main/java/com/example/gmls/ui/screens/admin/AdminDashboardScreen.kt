package com.example.gmls.ui.screens.admin

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.* 
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.gmls.R
import androidx.navigation.NavController
import com.example.gmls.domain.model.Disaster
import com.example.gmls.domain.model.User
import com.example.gmls.domain.model.SeverityLevel
import com.example.gmls.ui.components.DisasterTypeChip
import com.example.gmls.ui.components.NavDrawerContent
import com.example.gmls.ui.navigation.Screen
import com.example.gmls.ui.screens.map.MapScreen
import com.example.gmls.ui.theme.Info
import com.example.gmls.ui.theme.Red
import com.example.gmls.ui.theme.Success
import com.example.gmls.ui.theme.Warning
import com.example.gmls.ui.theme.AccentBlue
import com.example.gmls.ui.viewmodels.AuthViewModel
import com.example.gmls.ui.viewmodels.AdminViewModel
import com.example.gmls.ui.viewmodels.AdminState
import com.example.gmls.ui.viewmodels.AdminAuditLog
import com.example.gmls.ui.components.UserAnalyticsCard
import com.example.gmls.ui.components.DisasterAnalyticsCard
import com.example.gmls.ui.components.AdminAuditLogsCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import java.text.SimpleDateFormat
import kotlin.math.min
import com.example.gmls.ui.components.EnhancedLoadingIndicator
import com.example.gmls.ui.components.EmptyStateComponent
import com.example.gmls.ui.components.ErrorStateComponent
import com.example.gmls.ui.components.SkeletonListItem
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import com.example.gmls.domain.model.DisasterType
import com.example.gmls.domain.model.displayName
import com.example.gmls.ui.screens.analytics.AnalyticsScreen
import com.example.gmls.ui.viewmodels.AddUserByAdminFormData
import com.example.gmls.ui.screens.admin.AdminAuditLogScreen
import com.example.gmls.ui.screens.admin.AddUserByAdminScreen

// --- Main Admin Dashboard Screen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    paddingValues: PaddingValues,
    adminState: AdminState,
    adminViewModel: AdminViewModel,
    onNavigateToUserManagement: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToDisasterManagement: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAuditLogs: () -> Unit,
    onNavigateToAddUser: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(stringResource(R.string.logout_confirmation_title)) },
            text = { Text(stringResource(R.string.logout_confirmation_message)) },
            confirmButton = {
                Button(
                    onClick = { showLogoutDialog = false; onLogout() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.logout_button))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    AdminDashboardContent(
        paddingValues = paddingValues,
        adminState = adminState,
        adminViewModel = adminViewModel,
        onNavigateToUserManagement = onNavigateToUserManagement,
        onNavigateToAnalytics = onNavigateToAnalytics,
        onNavigateToMap = onNavigateToMap,
        onNavigateToDisasterManagement = onNavigateToDisasterManagement,
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToAuditLogs = onNavigateToAuditLogs,
        onNavigateToAddUser = onNavigateToAddUser,
        onLogout = { showLogoutDialog = true }
    )
}

// --- Top App Bar --- (No changes)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTopAppBar(title: String, onNavigationIconClick: () -> Unit) {
    CenterAlignedTopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onNavigationIconClick) {
                Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.menu))
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    )
}

// --- Admin Dashboard Content ---
@Composable
fun AdminDashboardContent(
    paddingValues: PaddingValues,
    adminState: AdminState,
    adminViewModel: AdminViewModel,
    onNavigateToUserManagement: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToDisasterManagement: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAuditLogs: () -> Unit,
    onNavigateToAddUser: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
    ) {
        // Enhanced Admin Profile Card
        AdminProfileCard(adminState.users.find { it.role == "admin" })
        Spacer(modifier = Modifier.height(16.dp))

        // Status Messages
        val errorMsg = adminState.error
        if (errorMsg != null) {
            ErrorCard(message = errorMsg)
            Spacer(modifier = Modifier.height(16.dp))
        }
        val successMsg = adminState.success
        if (successMsg != null) {
            SuccessCard(message = successMsg)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // System Overview Section
        Text(
            text = stringResource(R.string.system_overview),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        // Enhanced Key Metrics Row
        EnhancedStatusCardsRow(adminState.users, adminState.disasters)
        Spacer(modifier = Modifier.height(16.dp))

        // Recent Activity Summary
        RecentActivitySummary(
            adminState.disasters,
            adminState.users,
            adminState.auditLogs
        )
        Spacer(modifier = Modifier.height(20.dp))

        // Enhanced Analytics Dashboard Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.analytics_dashboard),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            // Export Analytics Button
            OutlinedButton(
                onClick = { 
                    // Trigger analytics export through adminViewModel
                    adminViewModel.exportAnalyticsData()
                },
                modifier = Modifier.height(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FileDownload,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.export_excel), style = MaterialTheme.typography.labelMedium)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        
        // Enhanced Analytics Cards
        EnhancedAnalyticsSection(
            adminState.users,
            adminState.disasters,
            adminState
        )
        Spacer(modifier = Modifier.height(20.dp))

        // Recent Disasters Section
        RecentDisastersSection(
            disasters = adminState.disasters,
            selectedDisasterType = null,
            onSelectedDisasterTypeChange = { /* No-op for now */ }
        )
        Spacer(modifier = Modifier.height(20.dp))

        // Enhanced User Management Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.user_management_section),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            TextButton(
                onClick = { /* Navigate to full user management */ }
            ) {
                Text(stringResource(R.string.view_all))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        
        // Enhanced Users requiring attention
        EnhancedUsersRequiringAttention(
            adminState.users,
            { },
            { },
            { }
        )
        Spacer(modifier = Modifier.height(20.dp))

        // System Activity Section
        Text(
            text = stringResource(R.string.system_activity),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        AdminAuditLogsCard(
            auditLogs = adminState.auditLogs,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))
        
        // Admin Navigation Section
        Text(
            text = stringResource(R.string.admin_actions),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        AdminNavigationGrid(
            onNavigateToUserManagement = onNavigateToUserManagement,
            onNavigateToAnalytics = onNavigateToAnalytics,
            onNavigateToMap = onNavigateToMap,
            onNavigateToDisasterManagement = onNavigateToDisasterManagement,
            onNavigateToSettings = onNavigateToSettings,
            onNavigateToAuditLogs = onNavigateToAuditLogs,
            onNavigateToAddUser = onNavigateToAddUser,
            onLogout = onLogout
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// --- ErrorCard, SuccessCard, AdminProfileCard, StatusCardsRow, StatusCard, DisasterFilterChips ---
// (No structural changes in these components, they depend on their parameters)
@Composable
fun ErrorCard(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
fun SuccessCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Success.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Berhasil",
                tint = Success
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Success
            )
        }
    }
}

@Composable
fun AdminProfileCard(adminUser: User?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Red.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = adminUser?.fullName?.take(2)?.uppercase() ?: "AD",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.welcome_back_admin),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = adminUser?.fullName ?: "Administrator",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun EnhancedStatusCardsRow(users: List<User>, disasters: List<Disaster>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatusCard(
            title = stringResource(R.string.total_users),
            description = "${users.count { it.isVerified }} ${stringResource(R.string.verified)}",
            statusColor = AccentBlue,
            icon = Icons.Default.People,
            count = users.size,
            modifier = Modifier.weight(1f)
        )
        StatusCard(
            title = stringResource(R.string.active_disasters),
            description = "${disasters.count { it.status == Disaster.Status.IN_PROGRESS }} ${stringResource(R.string.ongoing)}",
            statusColor = Warning,
            icon = Icons.Default.Warning,
            count = disasters.count { it.status == Disaster.Status.IN_PROGRESS },
            modifier = Modifier.weight(1f)
        )
        StatusCard(
            title = stringResource(R.string.resolved),
            description = stringResource(R.string.this_month),
            statusColor = Success,
            icon = Icons.Default.CheckCircle,
            count = disasters.count { it.status == Disaster.Status.RESOLVED },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatusCard(
    title: String,
    description: String,
    statusColor: Color,
    icon: ImageVector,
    count: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(130.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = statusColor, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text("$count", style = MaterialTheme.typography.headlineSmall, color = statusColor)
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun DisasterFilterChips(
    disasters: List<Disaster>,
    selectedDisasterType: String?,
    onSelectedDisasterTypeChange: (String?) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        item {
            FilterChip(
                label = { Text(stringResource(R.string.all_filter)) },
                selected = selectedDisasterType == null,
                onClick = { onSelectedDisasterTypeChange(null) }
            )
        }
        items(disasters.map { it.type.name }.distinct()) { typeName ->
            FilterChip(
                label = { Text(typeName.toTitleCase()) },
                selected = selectedDisasterType == typeName,
                onClick = { onSelectedDisasterTypeChange(if (selectedDisasterType == typeName) null else typeName) }
            )
        }
    }
}


@Composable
fun UserListItem(
    user: User,
    onUserClick: (User) -> Unit,
    onVerifyUser: (User) -> Unit,
    onToggleUserStatus: (User) -> Unit,
    showAdminControls: Boolean = true
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onUserClick(user) },
        colors = CardDefaults.cardColors(
            containerColor = when {
                user.role == "admin" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                !user.isActive -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                !user.isVerified -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = when {
                            !user.isActive -> Color.Gray
                            user.isVerified -> Success
                            else -> MaterialTheme.colorScheme.secondary
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                when {
                    !user.isActive -> Icon(Icons.Default.PersonOff, contentDescription = "Tidak Aktif", tint = Color.White)
                    user.isVerified -> Icon(Icons.Default.Check, contentDescription = "Terverifikasi", tint = Color.White)
                    else -> Text(
                        text = user.fullName.take(2).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    user.fullName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (user.role == "admin") FontWeight.Bold else FontWeight.Normal
                )
                Text(user.email, style = MaterialTheme.typography.bodySmall)
                Text(
                    text = "Peran: ${user.role.toTitleCase()} - ${
                        when {
                            !user.isActive -> "Tidak Aktif"
                            user.isVerified -> "Terverifikasi"
                            else -> "Belum Terverifikasi"
                        }
                    }",
                    style = MaterialTheme.typography.bodySmall,
                    color = when {
                        !user.isActive -> Red
                        !user.isVerified -> MaterialTheme.colorScheme.secondary
                        else -> LocalContentColor.current
                    }
                )
            }

            if (showAdminControls && user.role != "admin") {
                UserActionButtons(user, onVerifyUser, onToggleUserStatus, onUserClick) // onViewDetails was onUserClick
            } else if (user.role == "admin") {
                Icon(Icons.Default.AdminPanelSettings, contentDescription = "Administrator", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserActionButtons(
    user: User,
    onVerifyUser: (User) -> Unit,
    onToggleUserStatus: (User) -> Unit,
    onViewDetails: (User) -> Unit // Renamed from onUserClick for clarity if it's for detail view specifically
) {
    Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
        if (user.isActive && !user.isVerified) {
            TooltipBox(
                positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                tooltip = { PlainTooltip { Text(stringResource(R.string.verify_user_tooltip_text)) } },
                state = rememberTooltipState()
            ) {
                IconButton(onClick = { onVerifyUser(user) }) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = stringResource(R.string.verify_user_action), tint = Success)
                }
            }
        }
        TooltipBox(
            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
            tooltip = { PlainTooltip { Text(if (user.isActive) stringResource(R.string.block_user_tooltip) else stringResource(R.string.restore_user_tooltip)) } },
            state = rememberTooltipState()
        ) {
            IconButton(onClick = { onToggleUserStatus(user) }) {
                Icon(
                    if (user.isActive) Icons.Default.Block else Icons.Default.Restore,
                    contentDescription = if (user.isActive) stringResource(R.string.block_user_action) else stringResource(R.string.restore_user_action),
                    tint = if (user.isActive) Red else Success
                )
            }
        }
        TooltipBox(
            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
            tooltip = { PlainTooltip { Text(stringResource(R.string.view_details_tooltip_text)) } },
            state = rememberTooltipState()
        ) {
            IconButton(onClick = { onViewDetails(user) }) { // Use distinct callback if action is different
                Icon(Icons.Default.Info, contentDescription = stringResource(R.string.view_details_action), tint = Info)
            }
        }
    }
}

@Composable
fun UserDetailItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value.ifEmpty { stringResource(R.string.not_provided) },
            style = MaterialTheme.typography.bodyMedium
        )
    }
    Divider(modifier = Modifier.padding(vertical = 4.dp))
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisasterManagementScreen(
    paddingValues: PaddingValues,
    disasters: List<Disaster>,
    onDisasterClick: (Disaster) -> Unit,
    adminViewModel: AdminViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf<String?>(null) }
    var selectedStatusFilter by remember { mutableStateOf<String?>(null) } // Renamed to avoid conflict with Disaster.Status
    var showStatusDialog by remember { mutableStateOf(false) }
    var selectedDisasterForStatusChange by remember { mutableStateOf<Disaster?>(null) } // Renamed

    val filteredDisasters = try {
        disasters.filter { disaster ->
            val matchesSearch = searchQuery.isEmpty() ||
                    disaster.location.contains(searchQuery, ignoreCase = true) ||
                    disaster.type.name.contains(searchQuery, ignoreCase = true)

            val matchesType = selectedType == null || disaster.type.name == selectedType
            val matchesStatus = selectedStatusFilter == null || disaster.status.name == selectedStatusFilter

            matchesSearch && matchesType && matchesStatus
        }
    } catch (e: Exception) {
        emptyList<Disaster>()
    }

    val disasterTypes = try {
        disasters.mapNotNull { it.type.name }.distinct()
    } catch (e: Exception) {
        emptyList<String>()
    }
    
    val statusTypes = try {
        Disaster.Status.values().map { it.name }
    } catch (e: Exception) {
        listOf("REPORTED", "VERIFIED", "IN_PROGRESS", "RESOLVED") // Fallback
    }

    Column(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
        Text(stringResource(R.string.manage_disaster_reports_title), style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text(stringResource(R.string.search_disasters_location_type_label)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = if (searchQuery.isNotEmpty()) {
                {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.clear_search))
                    }
                }
            } else null,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
            item {
                FilterChip(
                    label = { Text(stringResource(R.string.all_filter)) },
                    selected = selectedType == null,
                    onClick = { selectedType = null }
                )
            }
            items(disasterTypes) { type ->
                FilterChip(
                    label = { Text(type.toTitleCase()) },
                    selected = selectedType == type,
                    onClick = { selectedType = if (selectedType == type) null else type }
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
            item {
                FilterChip(
                    label = { Text(stringResource(R.string.all_filter)) },
                    selected = selectedStatusFilter == null,
                    onClick = { selectedStatusFilter = null }
                )
            }
            items(statusTypes) { statusName ->
                FilterChip(
                    label = { Text(statusName.toTitleCase()) },
                    selected = selectedStatusFilter == statusName,
                    onClick = { selectedStatusFilter = if (selectedStatusFilter == statusName) null else statusName }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.disasters_found_format, filteredDisasters.size),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (filteredDisasters.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.SearchOff,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.no_disasters_found),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (disasters.isEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            stringResource(R.string.no_disasters_reported_yet),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filteredDisasters, key = { it.id }) { disaster ->
                    if (disaster.id.isNotBlank()) {
                        DisasterListItemWithActions(
                            disaster = disaster,
                            onClick = { onDisasterClick(disaster) },
                            onStatusChange = {
                                selectedDisasterForStatusChange = disaster
                                showStatusDialog = true
                            }
                        )
                    } else {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                "Kesalahan memuat item bencana",
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }
    }

    if (showStatusDialog && selectedDisasterForStatusChange != null) {
        val currentDisaster = selectedDisasterForStatusChange!!
        AlertDialog(
            onDismissRequest = { showStatusDialog = false },
            title = { Text(stringResource(R.string.update_disaster_status_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.update_status_for_disaster, currentDisaster.location))
                    Spacer(modifier = Modifier.height(16.dp))

                    val statusOptions = try {
                        Disaster.Status.values().toList()
                    } catch (e: Exception) {
                        emptyList()
                    }
                    
                    if (statusOptions.isNotEmpty()) {
                        statusOptions.forEach { status ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        adminViewModel.updateDisasterStatus(currentDisaster, status)
                                        showStatusDialog = false
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = currentDisaster.status == status,
                                    onClick = {
                                        adminViewModel.updateDisasterStatus(currentDisaster, status)
                                        showStatusDialog = false
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(status.name.toTitleCase())
                            }
                        }
                    } else {
                        Text(
                            "Kesalahan memuat opsi status",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {}, // Actions are directly on RadioButton click
            dismissButton = {
                TextButton(onClick = { showStatusDialog = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}

@Composable
fun DisasterListItemWithActions(disaster: Disaster, onClick: () -> Unit, onStatusChange: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val icon = when (disaster.type.name.lowercase(Locale.getDefault())) {
                    "flood" -> Icons.Default.Waves
                    "earthquake" -> Icons.Default.Public
                    "wildfire" -> Icons.Default.LocalFireDepartment
                    "hurricane" -> Icons.Default.Air
                    "tsunami" -> Icons.Default.Water
                    else -> Icons.Default.Warning
                }

                Icon(
                    imageVector = icon,
                    contentDescription = "Jenis Bencana",
                    tint = Warning,
                    modifier = Modifier.size(32.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = disaster.type.name.toTitleCase(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = "Lokasi: ${disaster.location}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onClick) {
                    Text(stringResource(R.string.view_details_button))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onStatusChange,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(stringResource(R.string.update_status_button))
                }
            }
        }
    }
}

fun String.toTitleCase(): String {
    return this.lowercase(Locale.getDefault())
        .split(" ", "_")
        .joinToString(" ") { word ->
            word.replaceFirstChar { 
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
            }
        }
}

@Composable
fun RecentActivitySummary(
    disasters: List<Disaster>,
    users: List<User>,
    auditLogs: List<AdminAuditLog>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Timeline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.recent_activity),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ActivityMetric(
                    label = stringResource(R.string.new_reports),
                    value = disasters.count { 
                        val dayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
                        it.timestamp > dayAgo
                    }.toString(),
                    icon = Icons.Default.NewReleases,
                    color = Warning
                )
                
                ActivityMetric(
                    label = stringResource(R.string.new_users),
                    value = users.count { user ->
                        val weekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
                        user.createdAt.time > weekAgo
                    }.toString(),
                    icon = Icons.Default.PersonAdd,
                    color = AccentBlue
                )
                
                ActivityMetric(
                    label = stringResource(R.string.admin_actions),
                    value = auditLogs.count { auditLog ->
                        val dayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
                        auditLog.timestamp.time > dayAgo
                    }.toString(),
                    icon = Icons.Default.AdminPanelSettings,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun ActivityMetric(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            color = color.copy(alpha = 0.15f),
            shape = CircleShape
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun RecentDisastersSection(
    disasters: List<Disaster>,
    selectedDisasterType: String?,
    onSelectedDisasterTypeChange: (String?) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.recent_disasters),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "${disasters.size} ${stringResource(R.string.total_text)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        DisasterFilterChips(
            disasters = disasters,
            selectedDisasterType = selectedDisasterType,
            onSelectedDisasterTypeChange = onSelectedDisasterTypeChange
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        val filteredDisasters = if (selectedDisasterType != null) {
            disasters.filter { it.type.name == selectedDisasterType }
        } else {
            disasters
        }.sortedByDescending { it.timestamp }.take(5)
        
        if (filteredDisasters.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.no_disasters_found),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    filteredDisasters.forEachIndexed { index, disaster ->
                        DisasterSummaryItem(disaster = disaster)
                        if (index < filteredDisasters.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DisasterSummaryItem(disaster: Disaster) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Status indicator
        Surface(
            modifier = Modifier.size(12.dp),
            color = when (disaster.status) {
                Disaster.Status.IN_PROGRESS -> Warning
                Disaster.Status.RESOLVED -> Success
                Disaster.Status.REPORTED -> AccentBlue
                Disaster.Status.VERIFIED -> AccentBlue
                else -> MaterialTheme.colorScheme.outline
            },
            shape = CircleShape
        ) {}
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = disaster.type.name.toTitleCase(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = "Lokasi: ${disaster.location}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Text(
            text = disaster.status.name,
            style = MaterialTheme.typography.labelSmall,
            color = when (disaster.status) {
                Disaster.Status.IN_PROGRESS -> Warning
                Disaster.Status.RESOLVED -> Success
                Disaster.Status.REPORTED -> AccentBlue
                Disaster.Status.VERIFIED -> AccentBlue
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun EnhancedUsersRequiringAttention(
    users: List<User>,
    onUserClick: (User) -> Unit,
    onVerifyUser: (User) -> Unit,
    onToggleUserStatus: (User) -> Unit
) {
    val unverifiedUsers = users.filter { !it.isVerified }.take(3)
    val inactiveUsers = users.filter { !it.isActive }.take(2)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PriorityHigh,
                    contentDescription = null,
                    tint = Warning,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.users_requiring_attention),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (unverifiedUsers.isEmpty() && inactiveUsers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.all_users_good_standing),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Success
                    )
                }
            } else {
                if (unverifiedUsers.isNotEmpty()) {
                    Text(
                        text = "${stringResource(R.string.unverified_users)} (${unverifiedUsers.size})",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    unverifiedUsers.forEach { user ->
                        UserAttentionItem(
                            user = user,
                            issue = stringResource(R.string.needs_verification),
                            actionText = stringResource(R.string.verify_button_text),
                            onActionClick = { onVerifyUser(user) },
                            onUserClick = onUserClick
                        )
                    }
                }
                
                if (inactiveUsers.isNotEmpty()) {
                    if (unverifiedUsers.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    
                    Text(
                        text = "${stringResource(R.string.inactive_users)} (${inactiveUsers.size})",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    inactiveUsers.forEach { user ->
                        UserAttentionItem(
                            user = user,
                            issue = stringResource(R.string.account_inactive),
                            actionText = stringResource(R.string.activate_button),
                            onActionClick = { onToggleUserStatus(user) },
                            onUserClick = onUserClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UserAttentionItem(
    user: User,
    issue: String,
    actionText: String,
    onActionClick: () -> Unit,
    onUserClick: (User) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onUserClick(user) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = user.fullName.take(1).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = user.fullName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = issue,
                style = MaterialTheme.typography.bodySmall,
                color = Warning
            )
        }
        
        OutlinedButton(
            onClick = onActionClick,
            modifier = Modifier.height(32.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                text = actionText,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
fun EnhancedAnalyticsSection(
    users: List<User>,
    disasters: List<Disaster>,
    adminState: AdminState
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ModernKPICard(
                title = stringResource(R.string.total_incidents),
                value = disasters.size.toString(),
                change = "+12%",
                isPositive = false,
                icon = Icons.Default.Report,
                color = Red,
                modifier = Modifier.weight(1f)
            )
            ModernKPICard(
                title = stringResource(R.string.active_users),
                value = users.count { it.isActive }.toString(),
                change = "+8%",
                isPositive = true,
                icon = Icons.Default.People,
                color = AccentBlue,
                modifier = Modifier.weight(1f)
            )
            ModernKPICard(
                title = stringResource(R.string.response_rate),
                value = "${calculateResponseRate(disasters)}%",
                change = "+5%",
                isPositive = true,
                icon = Icons.Default.TrendingUp,
                color = Success,
                modifier = Modifier.weight(1f)
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DisasterStatusChart(disasters = disasters)
                DisasterTypeBreakdown(disasters = disasters)
            }
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                UserVerificationStatus(users = users)
                SystemHealthIndicators(disasters = disasters, users = users)
            }
        }
        
        MonthlyTrendsChart(disasters = disasters)
        
        QuickStatsRow(disasters = disasters, users = users)
    }
}

@Composable
fun AnalyticsMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ModernKPICard(
    title: String,
    value: String,
    change: String,
    isPositive: Boolean,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = color.copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                    contentDescription = null,
                    tint = if (isPositive) Success else Red,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = change,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isPositive) Success else Red,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.vs_last_month),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun DisasterStatusChart(disasters: List<Disaster>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.system_health_dashboard),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.DonutSmall,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val statusCounts = disasters.groupBy { it.status }.mapValues { it.value.size }
            val total = disasters.size.toFloat()
            
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusProgressBar("Dilaporkan", statusCounts[Disaster.Status.REPORTED] ?: 0, total, Warning)
                StatusProgressBar("Terverifikasi", statusCounts[Disaster.Status.VERIFIED] ?: 0, total, AccentBlue)
                StatusProgressBar("Sedang Ditangani", statusCounts[Disaster.Status.IN_PROGRESS] ?: 0, total, Red)
                StatusProgressBar("Teratasi", statusCounts[Disaster.Status.RESOLVED] ?: 0, total, Success)
            }
        }
    }
}

@Composable
fun StatusProgressBar(label: String, count: Int, total: Float, color: Color) {
    val progress = if (total > 0) count / total else 0f
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.1f)
        )
    }
}

@Composable
fun DisasterTypeBreakdown(disasters: List<Disaster>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.disaster_type_breakdown),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val typeGroups = disasters.groupBy { it.type }.mapValues { it.value.size }
            val sortedTypes = typeGroups.toList().sortedByDescending { it.second }.take(4)
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sortedTypes) { (type, count) ->
                    DisasterTypeItem(
                        type = when (type) {
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
                        count = count,
                        percentage = (count.toFloat() / disasters.size * 100).toInt(),
                        icon = getDisasterTypeIcon(type)
                    )
                }
            }
        }
    }
}

@Composable
fun DisasterTypeItem(type: String, count: Int, percentage: Int, icon: ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = type,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$count insiden",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = "$percentage%",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun UserVerificationStatus(users: List<User>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.user_verification_status),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.People,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            val verifiedUsers = users.count { it.isVerified }
            val activeUsers = users.count { it.isActive }
            val adminUsers = users.count { it.role == "admin" }
            
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                UserStatusMetric(stringResource(R.string.verified_users), verifiedUsers, users.size, Success)
                UserStatusMetric(stringResource(R.string.active_users), activeUsers, users.size, AccentBlue)
                UserStatusMetric(stringResource(R.string.admin_users), adminUsers, users.size, Red)
            }
        }
    }
}

@Composable
fun UserStatusMetric(label: String, count: Int, total: Int, color: Color) {
    val percentage = if (total > 0) (count.toFloat() / total * 100).toInt() else 0
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = stringResource(R.string.users_count_format, count, total),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = "$percentage%",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun SystemHealthIndicators(disasters: List<Disaster>, users: List<User>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.system_health_dashboard),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.HealthAndSafety,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HealthIndicator(
                    stringResource(R.string.response_time_metric),
                    "${calculateAverageResponseTime(disasters)}${stringResource(R.string.hours_suffix)}",
                    stringResource(R.string.excellent_status),
                    Success
                )
                HealthIndicator(
                    stringResource(R.string.active_incidents_metric),
                    disasters.count { it.status == Disaster.Status.IN_PROGRESS }.toString(),
                    if (disasters.count { it.status == Disaster.Status.IN_PROGRESS } < 5) stringResource(R.string.good_status) else stringResource(R.string.high),
                    if (disasters.count { it.status == Disaster.Status.IN_PROGRESS } < 5) Success else Warning
                )
                HealthIndicator(
                    stringResource(R.string.system_load_metric),
                    stringResource(R.string.normal),
                    stringResource(R.string.stable),
                    Success
                )
            }
        }
    }
}

@Composable
fun HealthIndicator(metric: String, value: String, status: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = metric,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = status,
                style = MaterialTheme.typography.bodySmall,
                color = color
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun MonthlyTrendsChart(disasters: List<Disaster>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.monthly_trends_chart_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row {
                    Text(
                        text = stringResource(R.string.last_6_months_label),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ShowChart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                val months = listOf("Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                val values = calculateMonthlyDisasterTrend(disasters)
                
                months.forEachIndexed { index, month ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height((values[index] * 3).dp)
                                .background(
                                    color = AccentBlue.copy(alpha = 0.7f),
                                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = month,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = values[index].toString(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickStatsRow(disasters: List<Disaster>, users: List<User>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickStatCard(
            stringResource(R.string.critical_alerts_stat),
            disasters.count { it.severityLevel == SeverityLevel.CRITICAL }.toString(),
            Icons.Default.PriorityHigh,
            Red,
            modifier = Modifier.weight(1f)
        )
        QuickStatCard(
            stringResource(R.string.avg_response_stat),
            "${calculateAverageResponseTime(disasters)}${stringResource(R.string.hours_suffix)}",
            Icons.Default.Timer,
            AccentBlue,
            modifier = Modifier.weight(1f)
        )
        QuickStatCard(
            stringResource(R.string.new_users_stat),
            users.count { 
                System.currentTimeMillis() - it.createdAt.time < 7L * 24 * 60 * 60 * 1000 // Last 7 days
            }.toString(),
            Icons.Default.PersonAdd,
            Success,
            modifier = Modifier.weight(1f)
        )
        QuickStatCard(
            stringResource(R.string.resolved_today_stat),
            disasters.count { 
                it.status == Disaster.Status.RESOLVED && 
                System.currentTimeMillis() - it.timestamp < 24L * 60 * 60 * 1000
            }.toString(),
            Icons.Default.CheckCircle,
            Success,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun QuickStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.05f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

private fun calculateResponseRate(disasters: List<Disaster>): Int {
    if (disasters.isEmpty()) return 0
    val respondedDisasters = disasters.count { it.status != Disaster.Status.REPORTED }
    return (respondedDisasters.toFloat() / disasters.size * 100).toInt()
}

private fun calculateAverageResponseTime(disasters: List<Disaster>): Int {
    if (disasters.isEmpty()) return 0
    val resolvedDisasters = disasters.filter { it.status == Disaster.Status.RESOLVED }
    if (resolvedDisasters.isEmpty()) return 0
    
    return resolvedDisasters.mapNotNull { disaster ->
        val reportTime = disaster.timestamp
        val currentTime = System.currentTimeMillis()
        val timeDiffHours = ((currentTime - reportTime) / (1000 * 60 * 60)).toInt()
        
        min(timeDiffHours, 168)
    }.average().toInt()
}

private fun calculateMonthlyDisasterTrend(disasters: List<Disaster>): List<Int> {
    val calendar = java.util.Calendar.getInstance()
    val currentMonth = calendar.get(java.util.Calendar.MONTH)
    val currentYear = calendar.get(java.util.Calendar.YEAR)
    
    val monthlyData = mutableListOf<Int>()
    
    for (i in 5 downTo 0) {
        calendar.set(java.util.Calendar.YEAR, currentYear)
        calendar.set(java.util.Calendar.MONTH, currentMonth - i)
        calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        val monthStart = calendar.timeInMillis
        
        calendar.add(java.util.Calendar.MONTH, 1)
        calendar.add(java.util.Calendar.MILLISECOND, -1)
        val monthEnd = calendar.timeInMillis
        
        val monthCount = disasters.count { disaster ->
            disaster.timestamp in monthStart..monthEnd
        }
        
        monthlyData.add(monthCount)
    }
    
    return monthlyData
}

private fun getDisasterTypeIcon(type: DisasterType): ImageVector {
    return when (type) {
        DisasterType.EARTHQUAKE -> Icons.Default.Bolt
        DisasterType.FLOOD -> Icons.Default.Water
        DisasterType.WILDFIRE -> Icons.Default.LocalFireDepartment
        DisasterType.LANDSLIDE -> Icons.Default.Terrain
        DisasterType.VOLCANO -> Icons.Default.Volcano
        DisasterType.TSUNAMI -> Icons.Default.Waves
        DisasterType.HURRICANE -> Icons.Default.Storm
        DisasterType.TORNADO -> Icons.Default.AirlineSeatFlatAngled
        DisasterType.OTHER -> Icons.Default.Warning
    }
}

@Composable
fun AdminNavDrawerContent(
    currentScreen: String,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    adminState: AdminState
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.admin_control_panel),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        NavigationDrawerItem(
            label = { Text(stringResource(R.string.admin_dashboard)) },
            selected = currentScreen == Screen.AdminDashboard.route,
            onClick = { onNavigate(Screen.AdminDashboard.route) }
        )
        
        NavigationDrawerItem(
            label = { Text(stringResource(R.string.user_management)) },
            selected = currentScreen == Screen.UserManagement.route,
            onClick = { onNavigate(Screen.UserManagement.route) }
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text(stringResource(R.string.logout_button))
        }
    }
}

@Composable
fun AdminSettingsScreen(paddingValues: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.system_settings),
            style = MaterialTheme.typography.titleLarge
        )
    }
}

@Composable
fun AdminNavigationGrid(
    onNavigateToUserManagement: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToDisasterManagement: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAuditLogs: () -> Unit,
    onNavigateToAddUser: () -> Unit,
    onLogout: () -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.height(400.dp)
    ) {
        item {
            AdminNavigationCard(
                title = stringResource(R.string.user_management),
                description = stringResource(R.string.manage_users),
                icon = Icons.Default.People,
                color = AccentBlue,
                onClick = onNavigateToUserManagement
            )
        }
        
        item {
            AdminNavigationCard(
                title = stringResource(R.string.analytics),
                description = stringResource(R.string.view_analytics),
                icon = Icons.Default.Analytics,
                color = Success,
                onClick = onNavigateToAnalytics
            )
        }
        
        item {
            AdminNavigationCard(
                title = stringResource(R.string.admin_map),
                description = stringResource(R.string.monitor_locations),
                icon = Icons.Default.Map,
                color = Warning,
                onClick = onNavigateToMap
            )
        }
        
        item {
            AdminNavigationCard(
                title = stringResource(R.string.disaster_management),
                description = stringResource(R.string.manage_disasters),
                icon = Icons.Default.Warning,
                color = Red,
                onClick = onNavigateToDisasterManagement
            )
        }
        
        item {
            AdminNavigationCard(
                title = stringResource(R.string.add_new_user_title),
                description = stringResource(R.string.create_new_accounts),
                icon = Icons.Default.PersonAdd,
                color = Info,
                onClick = onNavigateToAddUser
            )
        }
        
        item {
            AdminNavigationCard(
                title = stringResource(R.string.system_settings),
                description = stringResource(R.string.system_configuration),
                icon = Icons.Default.Settings,
                color = MaterialTheme.colorScheme.secondary,
                onClick = onNavigateToSettings
            )
        }
    }
}

@Composable
fun AdminNavigationCard(
    title: String,
    description: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}