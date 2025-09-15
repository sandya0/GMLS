package com.example.gmls.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.gmls.R
import com.example.gmls.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Modern loading indicator with smooth animations
 */
@Composable
fun ModernLoadingIndicator(
    modifier: Modifier = Modifier,
    size: Int = 48,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading_rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = LinearEasing
            )
        ), label = "rotation"
    )

    Box(
        modifier = modifier.size(size.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = color,
            strokeWidth = 3.dp,
            modifier = Modifier
                .size(size.dp)
                .scale(0.8f)
        )
    }
}

/**
 * Enhanced loading screen with message and branding
 */
@Composable
fun EnhancedLoadingIndicator(
    message: String = "Memuat...",
    modifier: Modifier = Modifier,
    showProgress: Boolean = false,
    progress: Float = 0f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading_pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ), label = "alpha_pulse"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Modern loading animation
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (showProgress) {
                CircularProgressIndicator(
                    progress = { progress },
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(60.dp)
                )
            } else {
                ModernLoadingIndicator(
                    size = 60,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Loading message
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = alpha),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )

        if (showProgress) {
            Spacer(modifier = Modifier.height(16.dp))
                Text(
                text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Compact loading indicator for inline use
 */
@Composable
fun CompactLoadingIndicator(
    modifier: Modifier = Modifier,
    text: String? = null,
    size: Int = 24
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        ModernLoadingIndicator(
            size = size,
            color = MaterialTheme.colorScheme.primary
        )
        
        text?.let {
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Skeleton loading placeholder for content
 */
@Composable
fun SkeletonLoader(
    modifier: Modifier = Modifier,
    height: Int = 16,
    cornerRadius: Int = 8
) {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton_shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ), label = "skeleton_alpha"
    )

    Box(
        modifier = modifier
            .height(height.dp)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius.dp))
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha)
            )
    )
}

/**
 * Card skeleton for loading states
 */
@Composable
fun CardSkeleton(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar skeleton
                SkeletonLoader(
                    modifier = Modifier.size(48.dp),
                    height = 48,
                    cornerRadius = 24
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                // Title skeleton
                    SkeletonLoader(
                        modifier = Modifier.fillMaxWidth(0.7f),
                        height = 16,
                        cornerRadius = 8
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Subtitle skeleton
                    SkeletonLoader(
                        modifier = Modifier.fillMaxWidth(0.5f),
                        height = 14,
                        cornerRadius = 7
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Content skeleton
            repeat(3) { index ->
                SkeletonLoader(
                    modifier = Modifier.fillMaxWidth(
                        when (index) {
                            0 -> 1f
                            1 -> 0.8f
                            else -> 0.6f
                        }
                    ),
                    height = 14,
                    cornerRadius = 7
                )
                if (index < 2) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

/**
 * Backward compatibility alias for SkeletonListItem
 */
@Composable
fun SkeletonListItem(
    modifier: Modifier = Modifier
) {
    CardSkeleton(modifier = modifier)
}

/**
 * Empty state component with modern design
 */
@Composable
fun EmptyStateComponent(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        icon?.let {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
    ) {
        Icon(
                    imageVector = it,
            contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(40.dp)
        )
            }
        
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
            Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
            )
        
        if (actionText != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(24.dp))
            
            PrimaryButton(
                text = actionText,
                onClick = onActionClick,
                modifier = Modifier.widthIn(max = 200.dp)
                )
        }
    }
}

/**
 * Error state component with retry functionality
 */
@Composable
fun ErrorStateComponent(
    title: String,
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    retryText: String = stringResource(R.string.try_again_button)
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.errorContainer),
            contentAlignment = Alignment.Center
            ) {
                Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.Warning,
                    contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(40.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold
                )
                
        Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        PrimaryButton(
            text = retryText,
            onClick = onRetry,
            modifier = Modifier.widthIn(max = 200.dp)
        )
    }
}

/**
 * Success animation component
 */
@Composable
fun SuccessAnimation(
    message: String,
    modifier: Modifier = Modifier,
    onComplete: (() -> Unit)? = null
) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ), label = "success_scale"
    )
    
    LaunchedEffect(Unit) {
        delay(2000)
        onComplete?.invoke()
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.tertiaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(40.dp)
            )
        }
            
        Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = message,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Pull-to-refresh indicator
 */
@Composable
fun PullToRefreshIndicator(
    isRefreshing: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isRefreshing,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut(),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier.padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = Red
                )
                Text(
                    text = stringResource(R.string.loading_again),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
} 
