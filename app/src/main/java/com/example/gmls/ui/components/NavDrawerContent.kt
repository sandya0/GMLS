package com.example.gmls.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.gmls.ui.navigation.Screen
import com.example.gmls.ui.theme.Red

/**
 * Navigation drawer content component
 */
@Composable
fun NavDrawerContent(
    currentRoute: String?,
    onDestinationClicked: (route: String) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        NavigationItem(
            title = "Dashboard",
            icon = Icons.Filled.Dashboard,
            route = Screen.Dashboard.route
        ),
        NavigationItem(
            title = "Disasters",
            icon = Icons.Filled.Warning,
            route = Screen.DisasterList.route
        ),
        NavigationItem(
            title = "Map",
            icon = Icons.Filled.Map,
            route = Screen.Map.route
        ),
        NavigationItem(
            title = "Report Disaster",
            icon = Icons.Filled.AddAlert,
            route = Screen.DisasterReport.route
        ),
        NavigationItem(
            title = "Profile",
            icon = Icons.Filled.Person,
            route = Screen.Profile.route
        )
    )

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(300.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 24.dp)
    ) {
        // Header with app logo and name
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // App Logo
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(Red),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ER",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Emergency Response",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Disaster Management App",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Divider()

        Spacer(modifier = Modifier.height(16.dp))

        // Navigation items
        items.forEach { item ->
            NavDrawerItem(
                item = item,
                isSelected = currentRoute == item.route,
                onClick = { onDestinationClicked(item.route) }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Divider()

        Spacer(modifier = Modifier.height(16.dp))

        // Logout button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onLogout() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Logout,
                contentDescription = "Logout",
                tint = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.width(24.dp))

            Text(
                text = "Logout",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

/**
 * Individual navigation drawer item
 */
@Composable
fun NavDrawerItem(
    item: NavigationItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) {
        Red.copy(alpha = 0.12f)
    } else {
        Color.Transparent
    }

    val contentColor = if (isSelected) {
        Red
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.title,
            tint = contentColor
        )

        Spacer(modifier = Modifier.width(24.dp))

        Text(
            text = item.title,
            style = MaterialTheme.typography.titleMedium,
            color = contentColor
        )

        if (isSelected) {
            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Red)
            )
        }
    }
}

/**
 * Data class to hold navigation item information
 */
data class NavigationItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)