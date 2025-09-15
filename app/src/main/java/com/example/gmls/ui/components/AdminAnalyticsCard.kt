package com.example.gmls.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gmls.R
import com.example.gmls.ui.theme.Info
import com.example.gmls.ui.theme.Red
import com.example.gmls.ui.theme.Success
import com.example.gmls.ui.theme.Warning
import com.example.gmls.ui.viewmodels.DisasterAnalytics
import com.example.gmls.ui.viewmodels.MonthlyDisasterCount
import com.example.gmls.ui.viewmodels.UserAnalytics
import kotlin.math.cos
import kotlin.math.sin

/**
 * Component for displaying user analytics on admin dashboard
 */
@Composable
fun UserAnalyticsCard(userAnalytics: UserAnalytics, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.user_analytics_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // User statistics
            Row(modifier = Modifier.fillMaxWidth()) {
                AnalyticItem(
                    title = stringResource(R.string.total_users_label),
                    value = userAnalytics.totalUsers.toString(),
                    color = Info,
                    modifier = Modifier.weight(1f)
                )
                AnalyticItem(
                    title = stringResource(R.string.verified_label),
                    value = "${userAnalytics.verifiedUsers}",
                    color = Success,
                    modifier = Modifier.weight(1f)
                )
                AnalyticItem(
                    title = stringResource(R.string.active_label),
                    value = "${userAnalytics.activeUsers}",
                    color = Success,
                    modifier = Modifier.weight(1f)
                )
                AnalyticItem(
                    title = stringResource(R.string.admin_label),
                    value = "${userAnalytics.adminUsers}",
                    color = Red,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Recent registrations
            Row(modifier = Modifier.fillMaxWidth()) {
                AnalyticItem(
                    title = stringResource(R.string.new_users_7d),
                    value = userAnalytics.recentRegistrations.toString(),
                    color = Info,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Location distribution
            if (userAnalytics.usersByLocation.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.user_distribution_by_location),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                userAnalytics.usersByLocation.entries.sortedByDescending { it.value }.take(5).forEach { (location, count) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val percentage = if (userAnalytics.totalUsers > 0) {
                            count.toFloat() / userAnalytics.totalUsers.toFloat() * 100f
                        } else 0f
                        
                        Text(
                            text = location,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Text(
                            text = count.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.width(40.dp),
                            textAlign = TextAlign.End
                        )
                        
                        Text(
                            text = String.format("%.1f%%", percentage),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.width(60.dp),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
}

/**
 * Component for displaying disaster analytics on admin dashboard
 */
@Composable
fun DisasterAnalyticsCard(disasterAnalytics: DisasterAnalytics, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.disaster_analytics_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Disaster statistics
            Row(modifier = Modifier.fillMaxWidth()) {
                AnalyticItem(
                    title = stringResource(R.string.total_disasters_label),
                    value = disasterAnalytics.totalDisasters.toString(),
                    color = Warning,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Disaster types
            if (disasterAnalytics.disastersByType.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.disasters_by_type),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                disasterAnalytics.disastersByType.entries.sortedByDescending { it.value }.forEach { (type, count) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val percentage = if (disasterAnalytics.totalDisasters > 0) {
                            count.toFloat() / disasterAnalytics.totalDisasters.toFloat() * 100f
                        } else 0f
                        
                        Text(
                            text = type.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Text(
                            text = count.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.width(40.dp),
                            textAlign = TextAlign.End
                        )
                        
                        Text(
                            text = String.format("%.1f%%", percentage),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.width(60.dp),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Trend chart
            if (disasterAnalytics.disasterTrend.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.monthly_disaster_trend),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                DisasterTrendChart(
                    trendData = disasterAnalytics.disasterTrend,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
            }
        }
    }
}

/**
 * Component for displaying an analytics item
 */
@Composable
fun AnalyticItem(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(color.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Component for displaying a trend chart
 */
@Composable
fun DisasterTrendChart(trendData: List<MonthlyDisasterCount>, modifier: Modifier = Modifier) {
    if (trendData.isEmpty()) return
    
    val maxCount = trendData.maxOf { it.count }
    
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val padding = 20f
            
            val chartWidth = width - (padding * 2)
            val chartHeight = height - (padding * 2)
            
            // Draw axis lines
            drawLine(
                color = Color.Gray,
                start = androidx.compose.ui.geometry.Offset(padding, height - padding),
                end = androidx.compose.ui.geometry.Offset(width - padding, height - padding),
                strokeWidth = 2f
            )
            
            drawLine(
                color = Color.Gray,
                start = androidx.compose.ui.geometry.Offset(padding, height - padding),
                end = androidx.compose.ui.geometry.Offset(padding, padding),
                strokeWidth = 2f
            )
            
            // Draw trend line
            val path = Path()
            val segmentWidth = chartWidth / (trendData.size - 1).coerceAtLeast(1)
            
            trendData.forEachIndexed { index, data ->
                val x = padding + (index * segmentWidth)
                val y = height - padding - (data.count / maxCount.toFloat() * chartHeight)
                
                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
                
                // Draw point
                drawCircle(
                    color = Warning,
                    radius = 5f,
                    center = androidx.compose.ui.geometry.Offset(x, y)
                )
                
                // Draw month label as simple text
                // Skip month label drawing as it requires nativeCanvas
                // We'll implement a simpler approach without using nativeCanvas
            }
            
            drawPath(
                path = path,
                color = Warning,
                style = Stroke(width = 3f, cap = StrokeCap.Round)
            )
        }
    }
}

/**
 * Component for displaying audit logs on admin dashboard
 */
@Composable
fun AdminAuditLogsCard(auditLogs: List<com.example.gmls.ui.viewmodels.AdminAuditLog>, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.admin_activity_logs),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (auditLogs.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_admin_activity_logs),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            } else {
                Column {
                    auditLogs.take(10).forEach { log ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            // Time
                            val dateFormat = java.text.SimpleDateFormat("MM/dd HH:mm", java.util.Locale.getDefault())
                            val timeString = dateFormat.format(log.timestamp)
                            
                            Text(
                                text = timeString,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.width(70.dp)
                            )
                            
                            // Action
                            val actionText = log.action.replace("_", " ").replaceFirstChar { it.uppercase() }
                            val actionColor = when {
                                log.action.contains("create") -> Success
                                log.action.contains("delete") -> Red
                                log.action.contains("deactivate") -> Warning
                                else -> Info
                            }
                            
                            Text(
                                text = actionText,
                                style = MaterialTheme.typography.bodySmall,
                                color = actionColor,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.width(90.dp)
                            )
                            
                            // Details
                            Text(
                                text = log.details,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        if (auditLogs.indexOf(log) < auditLogs.size - 1) {
                            Divider(
                                modifier = Modifier.padding(vertical = 4.dp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminAnalyticsCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector,
    iconColor: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    progress: Float? = null,
    trend: String? = null,
    trendPositive: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    var animationPlayed by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = if (animationPlayed && progress != null) progress else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "progress_animation"
    )
    
    LaunchedEffect(Unit) {
        animationPlayed = true
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    if (subtitle != null) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    if (trend != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (trendPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                contentDescription = null,
                                tint = if (trendPositive) Color(0xFF4CAF50) else Color(0xFFF44336),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = trend,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (trendPositive) Color(0xFF4CAF50) else Color(0xFFF44336)
                            )
                        }
                    }
                }
                
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            if (progress != null) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = animatedProgress,
                        modifier = Modifier.size(60.dp),
                        strokeWidth = 6.dp,
                        color = iconColor,
                        trackColor = iconColor.copy(alpha = 0.2f)
                    )
                    
                    Text(
                        text = "${(animatedProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun AdminStatsGrid(
    totalUsers: Int,
    activeUsers: Int,
    verifiedUsers: Int,
    adminUsers: Int,
    totalDisasters: Int,
    activeDisasters: Int,
    resolvedDisasters: Int,
    recentUsers: Int,
    recentDisasters: Int,
    onUserStatsClick: () -> Unit = {},
    onDisasterStatsClick: () -> Unit = {},
    onActivityClick: () -> Unit = {}
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        item {
            AdminAnalyticsCard(
                title = stringResource(R.string.total_users_label),
                value = totalUsers.toString(),
                subtitle = stringResource(R.string.active_users_count, activeUsers),
                icon = Icons.Default.People,
                iconColor = MaterialTheme.colorScheme.primary,
                progress = if (totalUsers > 0) activeUsers.toFloat() / totalUsers else 0f,
                trend = stringResource(R.string.new_users_this_week, recentUsers),
                trendPositive = recentUsers >= 0,
                onClick = onUserStatsClick
            )
        }
        
        item {
            AdminAnalyticsCard(
                title = stringResource(R.string.verified_users),
                value = verifiedUsers.toString(),
                subtitle = stringResource(R.string.of_total_format, totalUsers),
                icon = Icons.Default.VerifiedUser,
                iconColor = Color(0xFF4CAF50),
                progress = if (totalUsers > 0) verifiedUsers.toFloat() / totalUsers else 0f,
                onClick = onUserStatsClick
            )
        }
        
        item {
            AdminAnalyticsCard(
                title = stringResource(R.string.total_disasters_label),
                value = totalDisasters.toString(),
                subtitle = stringResource(R.string.active_disasters_count, activeDisasters),
                icon = Icons.Default.Warning,
                iconColor = Color(0xFFFF9800),
                progress = if (totalDisasters > 0) resolvedDisasters.toFloat() / totalDisasters else 0f,
                trend = stringResource(R.string.new_disasters_this_week, recentDisasters),
                trendPositive = recentDisasters <= 0, // Fewer disasters is better
                onClick = onDisasterStatsClick
            )
        }
        
        item {
            AdminAnalyticsCard(
                title = stringResource(R.string.admin_users),
                value = adminUsers.toString(),
                subtitle = stringResource(R.string.system_administrators),
                icon = Icons.Default.AdminPanelSettings,
                iconColor = Color(0xFF9C27B0),
                onClick = onUserStatsClick
            )
        }
        
        item {
            AdminAnalyticsCard(
                title = stringResource(R.string.resolved_issues),
                value = resolvedDisasters.toString(),
                subtitle = stringResource(R.string.of_total_format, totalDisasters),
                icon = Icons.Default.CheckCircle,
                iconColor = Color(0xFF4CAF50),
                progress = if (totalDisasters > 0) resolvedDisasters.toFloat() / totalDisasters else 0f,
                onClick = onDisasterStatsClick
            )
        }
        
        item {
            AdminAnalyticsCard(
                title = stringResource(R.string.recent_activity_title),
                value = (recentUsers + recentDisasters).toString(),
                subtitle = stringResource(R.string.events_this_week),
                icon = Icons.Default.Timeline,
                iconColor = Color(0xFF2196F3),
                trend = stringResource(R.string.last_7_days),
                onClick = onActivityClick
            )
        }
    }
} 
