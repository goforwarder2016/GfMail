package com.gf.mail.presentation.ui.components.animations

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.gf.mail.domain.model.Email
import com.gf.mail.presentation.ui.components.EmailListItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AnimatedEmailCard(
    email: Email,
    isSelected: Boolean = false,
    isRead: Boolean = false,
    onEmailClick: () -> Unit = {},
    onEmailLongClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    animationEnabled: Boolean = true
) {
    var isVisible by remember { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }
    
    // Entrance animation
    val entranceAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = if (animationEnabled) 300 else 0,
            easing = EaseOutCubic
        ),
        label = "entrance_alpha"
    )
    
    val entranceOffset by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 20.dp,
        animationSpec = tween(
            durationMillis = if (animationEnabled) 300 else 0,
            easing = EaseOutCubic
        ),
        label = "entrance_offset"
    )
    
    // Selection animation
    val selectionScale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "selection_scale"
    )
    
    // Press animation
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(100),
        label = "press_scale"
    )
    
    // Read state animation
    val readAlpha by animateFloatAsState(
        targetValue = if (isRead) 0.6f else 1f,
        animationSpec = tween(
            durationMillis = if (animationEnabled) 200 else 0
        ),
        label = "read_alpha"
    )
    
    // Combined scale
    val combinedScale = selectionScale * pressScale
    
    // Trigger entrance animation
    LaunchedEffect(Unit) {
        delay(50) // Staggered entrance
        isVisible = true
    }
    
    Box(
        modifier = modifier
            .graphicsLayer {
                alpha = entranceAlpha * readAlpha
                scaleX = combinedScale
                scaleY = combinedScale
            }
            .offset(y = entranceOffset)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface
            )
    ) {
        EmailListItem(
            email = email,
            isSelected = isSelected,
            onClick = {
                isPressed = true
                onEmailClick()
                // Reset press state after animation
                kotlinx.coroutines.GlobalScope.launch {
                    delay(100)
                    isPressed = false
                }
            },
            onSelectionChange = { onEmailLongClick() },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun AnimatedEmailList(
    emails: List<Email>,
    selectedEmails: Set<String> = emptySet(),
    onEmailClick: (Email) -> Unit = {},
    onEmailLongClick: (Email) -> Unit = {},
    modifier: Modifier = Modifier,
    animationEnabled: Boolean = true
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(emails) { index, email ->
            AnimatedEmailCard(
                email = email,
                isSelected = selectedEmails.contains(email.id),
                isRead = email.isRead,
                onEmailClick = { onEmailClick(email) },
                onEmailLongClick = { onEmailLongClick(email) },
                animationEnabled = animationEnabled
            )
        }
    }
}

/**
 * Animated loading shimmer effect
 */
@Composable
fun AnimatedShimmer(
    modifier: Modifier = Modifier,
    isLoading: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_alpha"
    )
    
    if (isLoading) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(80.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha)
                )
        )
    }
}

/**
 * Animated progress indicator with custom styling
 */
@Composable
fun AnimatedProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(
            durationMillis = 500,
            easing = EaseOutCubic
        ),
        label = "progress"
    )
    
    LinearProgressIndicator(
        progress = animatedProgress,
        modifier = modifier,
        color = color,
        trackColor = color.copy(alpha = 0.2f)
    )
}