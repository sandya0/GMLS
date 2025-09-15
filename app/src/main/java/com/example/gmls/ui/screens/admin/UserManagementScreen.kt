package com.example.gmls.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.gmls.R
import com.example.gmls.domain.model.User
import com.example.gmls.ui.viewmodels.AdminViewModel
import com.example.gmls.ui.components.ConfirmationDialog
import com.example.gmls.ui.components.DropdownField
import com.example.gmls.ui.components.IndonesianAddress
import com.example.gmls.ui.components.IndonesianAddressSelector
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
    paddingValues: PaddingValues,
    users: List<User>,
    adminViewModel: AdminViewModel,
    onUserClick: (User) -> Unit,
    onVerifyUser: (User) -> Unit,
    onToggleUserStatus: (User) -> Unit,
    onBackClick: () -> Unit = {}
) {
    val adminState by adminViewModel.adminState.collectAsState()
    var showAddUserDialog by remember { mutableStateOf(false) }
    var showUserDetails by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<User?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var filterRole by remember { mutableStateOf<String?>(null) }
    var filterStatus by remember { mutableStateOf<String?>(null) }
    var sortBy by remember { mutableStateOf("name") }
    var showFilters by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Use the passed users from AdminViewModel
    val allUsers = users
    
    // Apply filters and search
    val filteredUsers = allUsers.filter { user ->
        val matchesSearch = searchQuery.isEmpty() ||
                user.fullName.contains(searchQuery, ignoreCase = true) ||
                user.email.contains(searchQuery, ignoreCase = true) ||
                user.phoneNumber.contains(searchQuery, ignoreCase = true)
        
        val matchesRole = filterRole == null || user.role == filterRole
        val matchesStatus = when (filterStatus) {
            "active" -> user.isActive
            "inactive" -> !user.isActive
            "verified" -> user.isVerified
            "unverified" -> !user.isVerified
            else -> true
        }
        
        matchesSearch && matchesRole && matchesStatus
    }
    
    // Apply sorting
    val sortedUsers = when (sortBy) {
        "name" -> filteredUsers.sortedBy { it.fullName }
        "email" -> filteredUsers.sortedBy { it.email }
        "date" -> filteredUsers.sortedByDescending { it.createdAt }
        "status" -> filteredUsers.sortedWith(compareBy({ !it.isActive }, { !it.isVerified }))
        else -> filteredUsers
    }
    
    // Handle errors
    LaunchedEffect(adminState.error) {
        adminState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                actionLabel = "Tutup",
                duration = SnackbarDuration.Long
            )
            adminViewModel.clearMessages()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.user_management)) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { showAddUserDialog = true },
                    icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                    text = { Text(stringResource(R.string.add_user_button)) }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Header Section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.user_management_title),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = stringResource(R.string.users_count_format, sortedUsers.size, allUsers.size),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                IconButton(onClick = { showFilters = !showFilters }) {
                                    Icon(
                                        Icons.Default.FilterList,
                                        contentDescription = stringResource(R.string.filters_label),
                                        tint = if (filterRole != null || filterStatus != null) 
                                            MaterialTheme.colorScheme.primary 
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = { adminViewModel.loadAllData() }) {
                                    Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.refresh_data))
                                }
                            }
                        }
                    }
                }
                
                // Search bar
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text(stringResource(R.string.search_users_placeholder)) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.clear_search))
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        // Filter chips
                        if (showFilters) {
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text(
                                text = stringResource(R.string.filters_label),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Role filter
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                FilterChip(
                                    selected = filterRole == null,
                                    onClick = { filterRole = null },
                                    label = { Text(stringResource(R.string.all_roles_filter)) }
                                )
                                FilterChip(
                                    selected = filterRole == "user",
                                    onClick = { filterRole = if (filterRole == "user") null else "user" },
                                    label = { Text(stringResource(R.string.users_filter)) }
                                )
                                FilterChip(
                                    selected = filterRole == "admin",
                                    onClick = { filterRole = if (filterRole == "admin") null else "admin" },
                                    label = { Text(stringResource(R.string.admins_filter)) }
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Status filter
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                FilterChip(
                                    selected = filterStatus == null,
                                    onClick = { filterStatus = null },
                                    label = { Text(stringResource(R.string.all_status_filter)) }
                                )
                                FilterChip(
                                    selected = filterStatus == "active",
                                    onClick = { filterStatus = if (filterStatus == "active") null else "active" },
                                    label = { Text(stringResource(R.string.active_filter)) }
                                )
                                FilterChip(
                                    selected = filterStatus == "verified",
                                    onClick = { filterStatus = if (filterStatus == "verified") null else "verified" },
                                    label = { Text(stringResource(R.string.verified_filter)) }
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Clear filters button
                            OutlinedButton(
                                onClick = {
                                    filterRole = null
                                    filterStatus = null
                                }
                            ) {
                                Text(stringResource(R.string.clear_filters))
                            }
                        }
                    }
                }
                
                // User list
                if (adminState.isLoading && sortedUsers.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(sortedUsers, key = { it.id }) { user ->
                            EnhancedUserItem(
                                user = user,
                                onClick = {
                                    selectedUser = user
                                    showUserDetails = true
                                },
                                onVerify = { onVerifyUser(user) },
                                onToggleStatus = { onToggleUserStatus(user) }
                            )
                        }
                    }
                }
            }
            
            // User Details Dialog
            if (showUserDetails && selectedUser != null) {
                UserDetailsDialog(
                    user = selectedUser!!,
                    onDismiss = { 
                        showUserDetails = false
                        selectedUser = null
                    },
                    onVerify = { onVerifyUser(selectedUser!!) },
                    onToggleStatus = { onToggleUserStatus(selectedUser!!) }
                )
            }
            
            // Add User Dialog
            if (showAddUserDialog) {
                EnhancedAddUserDialog(
                    onDismiss = { showAddUserDialog = false },
                    onConfirm = { userData ->
                        // Extract required fields
                        val email = userData["email"] ?: ""
                        val fullName = userData["fullName"] ?: ""
                        val phoneNumber = userData["phoneNumber"] ?: ""
                        
                        // Extract all additional data fields and filter out empty values
                        val additionalData = userData.filter { (key, value) ->
                            key !in listOf("email", "fullName", "phoneNumber") && value.isNotBlank()
                        }
                        
                        // Use adminViewModel instead of viewModel
                        // Note: AdminViewModel doesn't have createUser method, so we'll just close the dialog for now
                        // This would need to be implemented in AdminViewModel
                        showAddUserDialog = false
                    }
                )
            }
        }
    }
}

@Composable
private fun EnhancedUserItem(
    user: User,
    onClick: () -> Unit,
    onVerify: () -> Unit,
    onToggleStatus: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User avatar
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            !user.isActive -> MaterialTheme.colorScheme.errorContainer
                            user.role == "admin" -> MaterialTheme.colorScheme.primaryContainer
                            user.isVerified -> MaterialTheme.colorScheme.secondaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.fullName.take(2).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        !user.isActive -> MaterialTheme.colorScheme.onErrorContainer
                        user.role == "admin" -> MaterialTheme.colorScheme.onPrimaryContainer
                        user.isVerified -> MaterialTheme.colorScheme.onSecondaryContainer
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // User info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = user.fullName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (user.role == "admin") FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    
                    if (user.role == "admin") {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.AdminPanelSettings,
                            contentDescription = "Administrator",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    
                    if (user.isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.Verified,
                                                            contentDescription = "Terverifikasi",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = user.phoneNumber.ifEmpty { "Tidak ada telepon" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Status indicator
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when {
                            !user.isActive -> MaterialTheme.colorScheme.errorContainer
                            user.isVerified -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                            else -> MaterialTheme.colorScheme.secondaryContainer
                        }
                    ) {
                        Text(
                            text = when {
                                !user.isActive -> "Tidak Aktif"
                                user.isVerified -> "Terverifikasi"
                                else -> "Tertunda"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = when {
                                !user.isActive -> MaterialTheme.colorScheme.onErrorContainer
                                user.isVerified -> Color(0xFF4CAF50)
                                else -> MaterialTheme.colorScheme.onSecondaryContainer
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            
            // Action button
            IconButton(onClick = onClick) {
                Icon(
                    Icons.Default.ChevronRight,
                                            contentDescription = "Lihat detail",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun UserDetailsDialog(
    user: User,
    onDismiss: () -> Unit,
    onVerify: () -> Unit,
    onToggleStatus: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.user_details_title)) },
        text = { 
            Column {
                Text(stringResource(R.string.user_name_format, user.fullName))
                Text(stringResource(R.string.user_email_format, user.email))
                Text(stringResource(R.string.user_phone_format, user.phoneNumber))
                Text(stringResource(R.string.user_role_format, user.role))
                Text(stringResource(R.string.user_status_format, if (user.isActive) stringResource(R.string.active_status) else stringResource(R.string.inactive_status)))
                Text(stringResource(R.string.user_verified_format, if (user.isVerified) stringResource(R.string.yes) else stringResource(R.string.no)))
            }
        },
        confirmButton = {
            if (!user.isVerified) {
                Button(onClick = {
                    onVerify()
                    onDismiss()
                }) {
                    Text(stringResource(R.string.verify_button))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close_button))
            }
        }
    )
}

@Composable
private fun EnhancedAddUserDialog(
    onDismiss: () -> Unit,
    onConfirm: (Map<String, String>) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_new_user_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text(stringResource(R.string.full_name_label)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(stringResource(R.string.email_label)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text(stringResource(R.string.phone_number_label)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(mapOf(
                        "fullName" to fullName,
                        "email" to email,
                        "phoneNumber" to phoneNumber
                    ))
                },
                enabled = fullName.isNotBlank() && email.isNotBlank() && phoneNumber.isNotBlank()
            ) {
                Text(stringResource(R.string.add_user_dialog_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
