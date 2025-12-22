package dev.ml.fuelhub.presentation.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Sticky edge drawer swipe indicator - Non-intrusive left edge indicator
 * Positioned fixed at left edge, doesn't interfere with content
 * Subtle pulsing animation to suggest swipe interaction
 */
@Composable
fun DrawerSwipeIndicator(
    modifier: Modifier = Modifier,
    isVisible: Boolean = true,
    tintColor: Color = Color.White.copy(alpha = 0.5f)
) {
    if (!isVisible) return
    
    val infiniteTransition = rememberInfiniteTransition(label = "stickyEdgeArrow")
    
    // Subtle pulse animation for alpha - less distracting
    val alphaPulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alphaPulse"
    )
    
    // Horizontal slide animation - subtle movement
    val slideOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "slideOffset"
    )
    
    // Fixed position at left edge, vertically centered
    Box(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(align = Alignment.CenterStart)
    ) {
        Row(
            modifier = modifier
                .align(Alignment.CenterStart)
                .padding(start = 8.dp)
                .alpha(alphaPulse),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy((-8).dp)
        ) {
            // First arrow - leading
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .offset(x = slideOffset.dp),
                tint = tintColor.copy(alpha = 0.6f)
            )
            
            // Second arrow - overlapping for visual interest
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .offset(x = (slideOffset * 0.5f).dp),
                tint = tintColor
            )
            
            // Third arrow - subtle trailing
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .offset(x = (slideOffset * 0.25f).dp),
                tint = tintColor.copy(alpha = 0.4f)
            )
        }
    }
}
