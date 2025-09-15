package com.example.gmls.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gmls.R
import com.example.gmls.ui.navigation.Screen
import com.example.gmls.ui.theme.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.res.stringResource
import com.example.gmls.domain.model.User
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState

/**
 * Modern scrollable navigation drawer content component with enhanced design
 */
@Composable
fun NavDrawerContent(
    currentRoute: String?,
    onDestinationClicked: (route: String) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    currentUser: User? = null
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    val isAdmin = currentUser?.role == "admin"
    val scrollState = rememberScrollState()

    // Regular user navigation items with modern icons
    val regularItems = listOf(
        NavigationItem(
            title = stringResource(R.string.dashboard),
            icon = Icons.Filled.Dashboard,
            route = Screen.Dashboard.route,
            description = stringResource(R.string.dashboard_description)
        ),
        NavigationItem(
            title = stringResource(R.string.disasters),
            icon = Icons.Filled.Warning,
            route = Screen.DisasterList.route,
            description = stringResource(R.string.disasters_description)
        ),
        NavigationItem(
            title = stringResource(R.string.map),
            icon = Icons.Filled.Map,
            route = Screen.Map.route,
            description = stringResource(R.string.map_description)
        ),
        NavigationItem(
            title = stringResource(R.string.report_disaster),
            icon = Icons.Filled.AddAlert,
            route = Screen.ReportDisaster.route,
            description = stringResource(R.string.report_disaster_description)
        ),
        NavigationItem(
            title = stringResource(R.string.profile),
            icon = Icons.Filled.Person,
            route = Screen.Profile.route,
            description = stringResource(R.string.profile_description)
        ),
        NavigationItem(
            title = stringResource(R.string.analytics),
            icon = Icons.Filled.Analytics,
            route = Screen.UserAnalytics.route,
            description = stringResource(R.string.view_disaster_analytics)
        ),
        NavigationItem(
            title = stringResource(R.string.resources),
            icon = Icons.AutoMirrored.Filled.MenuBook,
            route = Screen.Resources.route,
            description = stringResource(R.string.resources_description)
        ),
        NavigationItem(
            title = stringResource(R.string.settings),
            icon = Icons.Filled.Settings,
            route = Screen.Settings.route,
            description = stringResource(R.string.settings_description)
        ),
        NavigationItem(
            title = stringResource(R.string.emergency_contacts_nav),
            icon = Icons.Filled.ContactPhone,
            route = Screen.Emergency.route,
            description = stringResource(R.string.emergency_contacts_nav)
        )
    )
    
    // Admin-specific navigation items
    val adminItems = listOf(
        NavigationItem(
            title = stringResource(R.string.admin_dashboard),
            icon = Icons.Filled.Dashboard,
            route = Screen.AdminDashboard.route,
            description = stringResource(R.string.admin_control_panel)
        ),
        NavigationItem(
            title = stringResource(R.string.user_management),
            icon = Icons.Filled.People,
            route = Screen.AdminUsers.route,
            description = stringResource(R.string.manage_users)
        ),
        NavigationItem(
            title = stringResource(R.string.admin_map),
            icon = Icons.Filled.Map,
            route = Screen.AdminMap.route,
            description = stringResource(R.string.monitor_locations)
        ),
        NavigationItem(
            title = stringResource(R.string.disaster_management),
            icon = Icons.Filled.Warning,
            route = Screen.AdminDisasters.route,
            description = stringResource(R.string.manage_disasters)
        ),
        NavigationItem(
            title = stringResource(R.string.analytics),
            icon = Icons.Filled.Analytics,
            route = Screen.AdminAnalytics.route,
            description = stringResource(R.string.view_analytics)
        ),
        NavigationItem(
            title = stringResource(R.string.system_settings),
            icon = Icons.Filled.Settings,
            route = Screen.AdminSettings.route,
            description = stringResource(R.string.system_configuration)
        )
    )
    
    // Choose which items to display based on user role
    val items = if (isAdmin) adminItems else regularItems

    // Scrollable column with modern design
    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(340.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    )
                )
            )
            .verticalScroll(scrollState)
            .padding(vertical = 16.dp)
    ) {
        // Modern header with app branding
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
            // Modern app logo container
            Box(
                modifier = Modifier
                    .size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(R.drawable.logo_gmls)
                        .crossfade(true)
                        .memoryCacheKey("app_logo")
                        .build(),
                    contentDescription = stringResource(R.string.gmls_app_name),
                    modifier = Modifier.size(56.dp),
                    contentScale = ContentScale.Fit
                )
            }

                Spacer(modifier = Modifier.height(16.dp))

            // App title with modern typography
                Text(
                    text = stringResource(R.string.gmls_app_name),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.2.sp
                )

                Text(
                    text = stringResource(R.string.gmls_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
                )
                
            // Modern admin badge with enhanced styling
                if (isAdmin) {
                Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = MaterialTheme.shapes.medium,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.admin_badge),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                    }
                }
            }

            // Enhanced user info section
            currentUser?.let { user ->
                Spacer(modifier = Modifier.height(20.dp))
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Subtle divider line
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = stringResource(R.string.welcome_back_user),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = user.fullName.takeIf { it.isNotBlank() } ?: user.email,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Navigation section with modern header
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Navigation,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.navigation_header),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
        )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Navigation items with enhanced styling
        items.forEach { item ->
            ModernNavDrawerItem(
                item = item,
                isSelected = currentRoute == item.route,
                onClick = { onDestinationClicked(item.route) }
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))

        // Modern logout section
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Enhanced logout button
        val logoutInteractionSource = remember { MutableInteractionSource() }
        val isLogoutPressed by logoutInteractionSource.collectIsPressedAsState()
        val logoutScale by animateFloatAsState(
            targetValue = if (isLogoutPressed) 0.96f else 1f,
            animationSpec = tween(100), label = "logout_scale"
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .scale(logoutScale)
                .clickable(
                    interactionSource = logoutInteractionSource,
                    indication = null
                ) { showLogoutDialog = true },
            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.12f),
            shape = MaterialTheme.shapes.medium,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                    shape = CircleShape
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
            )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
            Text(
                text = stringResource(R.string.logout_option),
                style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(R.string.logout_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Modern logout confirmation dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.logout_confirmation_dialog_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            text = {
                Text(
                    text = stringResource(R.string.logout_confirmation_message),
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 24.sp
                )
            },
            confirmButton = {
                PrimaryButton(
                    text = stringResource(R.string.logout_dialog_button),
                    onClick = {
                showLogoutDialog = false
                onLogout()
            },
                    modifier = Modifier.widthIn(min = 100.dp)
                )
            },
            dismissButton = {
                SecondaryButton(
                    text = stringResource(R.string.cancel_dialog_button),
                    onClick = { showLogoutDialog = false },
                    modifier = Modifier.widthIn(min = 100.dp)
                )
            },
            shape = MaterialTheme.shapes.extraLarge
        )
    }
}

/**
 * Modern navigation drawer item with enhanced styling and animations
 */
@Composable
private fun ModernNavDrawerItem(
    item: NavigationItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val containerColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.primaryContainer
            else -> Color.Transparent
        },
        animationSpec = tween(200), label = "container_color"
    )
    
    val contentColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(200), label = "content_color"
    )

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(100), label = "scale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() },
        color = containerColor,
        shape = MaterialTheme.shapes.medium,
        shadowElevation = if (isSelected) 3.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon container with background
            Surface(
                modifier = Modifier.size(40.dp),
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                },
                shape = CircleShape
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
    ) {
        Icon(
            imageVector = item.icon,
                        contentDescription = null,
                        tint = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            contentColor
                        },
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text content
            Column(
                modifier = Modifier.weight(1f)
            ) {
        Text(
            text = item.title,
            style = MaterialTheme.typography.titleMedium,
                    color = contentColor,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                )
                if (item.description.isNotEmpty()) {
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor.copy(alpha = 0.7f)
                    )
                }
            }

            // Selection indicator
        if (isSelected) {
                Surface(
                    modifier = Modifier.size(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                ) {}
            }
        }
    }
}

/**
 * Enhanced navigation item data class
 */
data class NavigationItem(
    val title: String,
    val icon: ImageVector,
    val route: String,
    val description: String = ""
)
