package dev.ml.fuelhub.presentation.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.ml.fuelhub.ui.theme.*

/**
 * Main Dashboard Screen - Premium Design
 * Features: Quick stats, recent transactions, wallet overview, and quick actions
 */
@Composable
fun DashboardScreen(
    onNavigateToTransactions: () -> Unit = {},
    onNavigateToWallet: () -> Unit = {},
    onNavigateToReports: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Animation for gradient
    val infiniteTransition = rememberInfiniteTransition(label = "gradient")
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlue),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header Section
        item {
            DashboardHeader()
        }

        // Quick Stats Cards
        item {
            Text(
                "Overview",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            QuickStatsSection(animatedOffset)
        }

        // Wallet Card
        item {
            Text(
                "Fuel Wallet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            PremiumWalletCard(
                onNavigateToWallet = onNavigateToWallet,
                animatedOffset = animatedOffset
            )
        }

        // Quick Actions
        item {
            Text(
                "Quick Actions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            QuickActionsGrid(
                onNavigateToTransactions = onNavigateToTransactions,
                onNavigateToWallet = onNavigateToWallet,
                onNavigateToReports = onNavigateToReports
            )
        }

        // Recent Transactions
        item {
            Text(
                "Recent Activity",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            RecentTransactionsSection()
        }
    }
}

@Composable
fun DashboardHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                "FuelHub",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = VibrantCyan,
                fontSize = 32.sp
            )
            Text(
                "Fuel Management Dashboard",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }

        // Notification Icon
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(ElectricBlue, VibrantCyan)
                    )
                )
                .clickable { },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notifications",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun QuickStatsSection(animatedOffset: Float) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            title = "Today's Usage",
            value = "245.5 L",
            icon = Icons.Default.LocalGasStation,
            gradient = listOf(ElectricBlue, VibrantCyan),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Transactions",
            value = "12",
            icon = Icons.Default.Receipt,
            gradient = listOf(AccentOrange, AccentAmber),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    gradient: List<Color>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(120.dp)
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceDark
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = gradient.map { it.copy(alpha = 0.1f) },
                        start = Offset(0f, 0f),
                        end = Offset(300f, 300f)
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = gradient[0],
                    modifier = Modifier.size(32.dp)
                )
                Column {
                    Text(
                        value,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        title,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun PremiumWalletCard(
    onNavigateToWallet: () -> Unit,
    animatedOffset: Float
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .shadow(12.dp, RoundedCornerShape(20.dp))
            .clickable { onNavigateToWallet() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            GradientStart,
                            GradientMid,
                            GradientEnd
                        ),
                        start = Offset(animatedOffset, animatedOffset),
                        end = Offset(animatedOffset + 500f, animatedOffset + 500f)
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "Available Balance",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "1,245.75 L",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 36.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.LocalGasStation,
                        contentDescription = "Wallet",
                        tint = Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(48.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Max Capacity",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            "2,000 L",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Button(
                        onClick = onNavigateToWallet,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.2f),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("View Details", fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionsGrid(
    onNavigateToTransactions: () -> Unit,
    onNavigateToWallet: () -> Unit,
    onNavigateToReports: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                title = "New Transaction",
                icon = Icons.Default.Add,
                gradient = listOf(ElectricBlue, VibrantCyan),
                onClick = onNavigateToTransactions,
                modifier = Modifier.weight(1f)
            )
            QuickActionCard(
                title = "Refill Wallet",
                icon = Icons.Default.LocalGasStation,
                gradient = listOf(SuccessGreen, NeonTeal),
                onClick = onNavigateToWallet,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                title = "View Reports",
                icon = Icons.Default.BarChart,
                gradient = listOf(AccentOrange, AccentAmber),
                onClick = onNavigateToReports,
                modifier = Modifier.weight(1f)
            )
            QuickActionCard(
                title = "History",
                icon = Icons.Default.History,
                gradient = listOf(WarningYellow, AccentAmber),
                onClick = { },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    icon: ImageVector,
    gradient: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .shadow(6.dp, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceDark
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = gradient.map { it.copy(alpha = 0.15f) }
                    )
                )
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(gradient)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
        }
    }
}

@Composable
fun RecentTransactionsSection() {
    val sampleTransactions = listOf(
        TransactionItem("Gasoline - 50L", "Vehicle #1234", "2 hours ago", SuccessGreen),
        TransactionItem("Diesel - 75L", "Vehicle #5678", "5 hours ago", VibrantCyan),
        TransactionItem("Gasoline - 40L", "Vehicle #9012", "1 day ago", AccentOrange)
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        sampleTransactions.forEach { transaction ->
            TransactionCard(transaction)
        }
    }
}

data class TransactionItem(
    val title: String,
    val subtitle: String,
    val time: String,
    val accentColor: Color
)

@Composable
fun TransactionCard(transaction: TransactionItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceDark
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(transaction.accentColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalGasStation,
                        contentDescription = null,
                        tint = transaction.accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        transaction.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        transaction.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
            Text(
                transaction.time,
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary
            )
        }
    }
}
