package dev.ml.fuelhub.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.ml.fuelhub.ui.theme.*

/**
 * Custom Bottom Navigation Bar with Centered Action Button
 * Design: Bottom bar with icons on sides and prominent centered action button
 */
@Composable
fun CenteredActionBottomBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onCenterActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Main navigation bar background
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF0D5B6B) // Deep teal color from your design
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side icons
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Home
                    NavigationIconButton(
                        icon = Icons.Default.Home,
                        label = "Home",
                        isSelected = selectedTab == 0,
                        onClick = { onTabSelected(0) }
                    )

                    // Wallet
                    NavigationIconButton(
                        icon = Icons.Default.AccountBalance,
                        label = "Wallet",
                        isSelected = selectedTab == 1,
                        onClick = { onTabSelected(1) }
                    )
                }

                // Spacer for centered button
                Spacer(modifier = Modifier.width(24.dp))

                // Right side icons
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Gas Slips
                    NavigationIconButton(
                        icon = Icons.Default.LocalGasStation,
                        label = "Gas Slips",
                        isSelected = selectedTab == 2,
                        onClick = { onTabSelected(2) }
                    )

                    // Reports
                    NavigationIconButton(
                        icon = Icons.Default.BarChart,
                        label = "Reports",
                        isSelected = selectedTab == 3,
                        onClick = { onTabSelected(3) }
                    )
                }
            }
        }

        // Centered Action Button (Create Transaction)
        CenteredActionButton(
            onClick = onCenterActionClick,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
private fun NavigationIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentColor = if (isSelected) VibrantCyan else Color.White.copy(alpha = 0.6f)
    
    Column(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = contentColor
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun CenteredActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(68.dp)
            .offset(y = (-30).dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer circle with gradient
        Box(
            modifier = Modifier
                .size(68.dp)
                .shadow(16.dp, CircleShape)
                .background(
                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(ElectricBlue, VibrantCyan)
                    ),
                    shape = CircleShape
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            // Inner icon
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Create Transaction",
                modifier = Modifier.size(36.dp),
                tint = Color.White
            )
        }
    }
}
