package dev.ml.fuelhub.presentation.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.ml.fuelhub.ui.theme.*
import dev.ml.fuelhub.presentation.viewmodel.WalletViewModel
import dev.ml.fuelhub.presentation.viewmodel.TransactionViewModel
import dev.ml.fuelhub.presentation.state.WalletUiState
import dev.ml.fuelhub.data.model.FuelWallet
import java.time.LocalDateTime
import java.time.LocalDate
import kotlin.math.absoluteValue

/**
 * Comprehensive Home Screen - Premium Fuel Management Dashboard
 * Features comprehensive overview with analytics, quick actions, and insights
 */
@Composable
fun HomeScreen(
    onNavigateToTransactions: () -> Unit = {},
    onNavigateToWallet: () -> Unit = {},
    onNavigateToReports: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    modifier: Modifier = Modifier,
    transactionViewModel: TransactionViewModel? = null,
    walletViewModel: WalletViewModel? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "homeGradient")
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "homeOffset"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlue),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Premium Header with Profile
        item {
            HomeHeader()
        }

        // Key Metrics Section
        item {
            SectionTitle("Key Metrics")
            KeyMetricsGrid(animatedOffset, transactionViewModel)
        }

        // Wallet Status Card
        item {
            SectionTitle("Wallet Status")
            WalletStatusCard(onNavigateToWallet, animatedOffset, walletViewModel)
        }

        // Quick Stats
        item {
            SectionTitle("Today's Summary")
            TodaySummaryStats(transactionViewModel)
        }

        // Vehicle Fleet Overview
        item {
            SectionTitle("Active Vehicles")
            VehicleFleetSection(transactionViewModel)
        }

        // Quick Actions
        item {
            SectionTitle("Quick Actions")
            ComprehensiveQuickActions(
                onNavigateToTransactions = onNavigateToTransactions,
                onNavigateToWallet = onNavigateToWallet,
                onNavigateToReports = onNavigateToReports,
                onNavigateToHistory = onNavigateToHistory
            )
        }

        // Recent Transactions
        item {
            SectionTitle("Recent Transactions")
            RecentTransactionsHome(transactionViewModel)
        }

        // Insights & Alerts
        item {
            SectionTitle("Alerts & Insights")
            InsightsAlerts()
        }

        // Footer spacing
        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun HomeHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                "Welcome Back",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Text(
                "FuelHub",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = VibrantCyan,
                fontSize = 32.sp
            )
            Text(
                "Fuel Management System",
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary
            )
        }

        // Profile Circle with Notification Badge
        Box {
            Box(
                modifier = Modifier
                    .size(56.dp)
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
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            // Notification badge
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(WarningYellow)
                    .align(Alignment.TopEnd)
            ) {
                Text(
                    "3",
                    fontSize = 10.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = TextPrimary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun KeyMetricsGrid(animatedOffset: Float, transactionViewModel: TransactionViewModel? = null) {
    val vm = transactionViewModel ?: viewModel<TransactionViewModel>()
    val transactions by vm.transactionHistory.collectAsState()
    
    // Calculate metrics
    val today = LocalDate.now()
    val todayTransactions = transactions.filter { 
        it.createdAt.toLocalDate() == today
    }
    val totalUsage = todayTransactions.sumOf { it.litersToPump }
    val monthStart = LocalDate.now().withDayOfMonth(1)
    val monthTransactions = transactions.filter { 
        it.createdAt.toLocalDate() >= monthStart
    }
    val monthlyUsage = monthTransactions.sumOf { it.litersToPump }
    val avgPerDay = if (monthTransactions.isNotEmpty()) {
        monthlyUsage / monthTransactions.distinctBy { it.createdAt.toLocalDate() }.size
    } else 0.0
    val efficiency = calculateEfficiency(transactions)
    
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Total Usage",
                value = "%.1f L".format(monthlyUsage),
                unit = "This Month",
                icon = Icons.Default.LocalGasStation,
                gradient = listOf(ElectricBlue, VibrantCyan),
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Avg Per Day",
                value = "%.1f L".format(avgPerDay),
                unit = "Consumption",
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                gradient = listOf(SuccessGreen, NeonTeal),
                modifier = Modifier.weight(1f)
            )
        }

        // Bottom row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Transactions",
                value = monthTransactions.size.toString(),
                unit = "This Month",
                icon = Icons.Default.Receipt,
                gradient = listOf(AccentOrange, AccentAmber),
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Efficiency",
                value = "%.0f%%".format(efficiency),
                unit = "Performance",
                icon = Icons.Default.CheckCircle,
                gradient = listOf(SuccessGreen, ElectricBlue),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

fun calculateEfficiency(transactions: List<dev.ml.fuelhub.data.model.FuelTransaction>): Double {
    // Efficiency based on transaction completion rate
    if (transactions.isEmpty()) return 100.0
    val completed = transactions.count { 
        it.status == dev.ml.fuelhub.data.model.TransactionStatus.COMPLETED 
    }
    return (completed.toDouble() / transactions.size) * 100
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    unit: String,
    icon: ImageVector,
    gradient: List<Color>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(130.dp)
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        title,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = gradient[0],
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        value,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Text(
                        unit,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                }
            }
        }
    }
}

@Composable
fun WalletStatusCard(onNavigateToWallet: () -> Unit, animatedOffset: Float, walletViewModel: WalletViewModel? = null) {
    val vm = walletViewModel ?: viewModel<WalletViewModel>()
    val walletState by vm.uiState.collectAsState()
    
    val wallet = when (walletState) {
        is WalletUiState.Success -> (walletState as WalletUiState.Success).wallet
        else -> null
    }
    
    val balance = wallet?.balanceLiters ?: 0.0
    val maxCapacity = wallet?.maxCapacityLiters ?: 2000.0
    val percentage = (balance / maxCapacity).coerceIn(0.0, 1.0)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .shadow(12.dp, RoundedCornerShape(20.dp))
            .clickable { onNavigateToWallet() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            "Fuel Wallet Balance",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "%.2f L".format(balance),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 36.sp
                        )
                        Text(
                            "%.1f%% of Capacity".format(percentage * 100),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.LocalGasStation,
                        contentDescription = "Wallet",
                        tint = Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(56.dp)
                    )
                }

                // Progress bar
                LinearProgressIndicator(
                    progress = { percentage.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.2f)
                )

                // Bottom action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Max: %.0f L".format(maxCapacity),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium
                    )
                    Button(
                        onClick = onNavigateToWallet,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.2f),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Refill", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TodaySummaryStats(transactionViewModel: TransactionViewModel? = null) {
    val vm = transactionViewModel ?: viewModel<TransactionViewModel>()
    val transactions by vm.transactionHistory.collectAsState()
    val vehicles by vm.vehicles.collectAsState()
    
    val today = LocalDate.now()
    val todayTransactions = transactions.filter { it.createdAt.toLocalDate() == today }
    val fuelUsedToday = todayTransactions.sumOf { it.litersToPump }
    val vehiclesUsedToday = todayTransactions.map { it.vehicleId }.distinct().size
    
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatRow(
            label = "Fuel Used",
            value = "%.1f L".format(fuelUsedToday),
            icon = Icons.Default.LocalGasStation,
            color = AccentOrange
        )
        StatRow(
            label = "Transactions",
            value = todayTransactions.size.toString(),
            icon = Icons.Default.Receipt,
            color = ElectricBlue
        )
        StatRow(
            label = "Vehicles Used",
            value = vehiclesUsedToday.toString(),
            icon = Icons.Default.DirectionsCarFilled,
            color = SuccessGreen
        )
    }
}

@Composable
fun StatRow(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
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
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
            }
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
    }
}

@Composable
fun VehicleFleetSection(transactionViewModel: TransactionViewModel? = null) {
    val vm = transactionViewModel ?: viewModel<TransactionViewModel>()
    val vehicles by vm.vehicles.collectAsState()
    val transactions by vm.transactionHistory.collectAsState()
    
    // Calculate usage percentage for each vehicle
    val vehicleUsageMap = vehicles.associate { vehicle ->
        val vehicleTransactions = transactions.filter { it.vehicleId == vehicle.id }
        val usage = vehicleTransactions.sumOf { it.litersToPump }
        // Estimate usage percentage (assuming 100L tank capacity for demo)
        val percentage = (usage % 100.0) / 100.0 * 100
        vehicle.id to Pair(percentage, vehicleTransactions.sumOf { it.litersToPump })
    }
    
    val colorMap = mapOf(
        0 to SuccessGreen,
        1 to VibrantCyan,
        2 to AccentOrange,
        3 to ElectricBlue,
        4 to NeonTeal
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        vehicles.take(5).forEachIndexed { index, vehicle ->
            val (percentage, totalUsage) = vehicleUsageMap[vehicle.id] ?: Pair(0.0, 0.0)
            VehicleCard(
                VehicleInfo(
                    name = vehicle.plateNumber,
                    fuelType = vehicle.fuelType.name,
                    usage = "%.0f%%".format(percentage),
                    color = colorMap[index] ?: SuccessGreen
                )
            )
        }
    }
}

data class VehicleInfo(
    val name: String,
    val fuelType: String,
    val usage: String,
    val color: Color
)

@Composable
fun VehicleCard(vehicle: VehicleInfo) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
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
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(vehicle.color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCarFilled,
                        contentDescription = null,
                        tint = vehicle.color,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        vehicle.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        vehicle.fuelType,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    vehicle.usage,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                LinearProgressIndicator(
                    progress = { vehicle.usage.removeSuffix("%").toFloat() / 100f },
                    modifier = Modifier
                        .width(60.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = vehicle.color,
                    trackColor = vehicle.color.copy(alpha = 0.2f)
                )
            }
        }
    }
}

@Composable
fun ComprehensiveQuickActions(
    onNavigateToTransactions: () -> Unit,
    onNavigateToWallet: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionButton(
                title = "New Transaction",
                icon = Icons.Default.Add,
                gradient = listOf(ElectricBlue, VibrantCyan),
                onClick = onNavigateToTransactions,
                modifier = Modifier.weight(1f)
            )
            ActionButton(
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
            ActionButton(
                title = "View Reports",
                icon = Icons.Default.BarChart,
                gradient = listOf(AccentOrange, AccentAmber),
                onClick = onNavigateToReports,
                modifier = Modifier.weight(1f)
            )
            ActionButton(
                title = "History",
                icon = Icons.Default.History,
                gradient = listOf(WarningYellow, AccentOrange),
                onClick = onNavigateToHistory,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ActionButton(
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
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
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
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(gradient)),
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
                    color = TextPrimary,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
fun RecentTransactionsHome(transactionViewModel: TransactionViewModel? = null) {
    val vm = transactionViewModel ?: viewModel<TransactionViewModel>()
    val transactions by vm.transactionHistory.collectAsState()
    
    // Get last 4 transactions sorted by date (most recent first)
    val recentTransactions = transactions
        .sortedByDescending { it.createdAt }
        .take(4)
    
    val colorMap = listOf(SuccessGreen, VibrantCyan, AccentOrange, NeonTeal)

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        recentTransactions.forEachIndexed { index, transaction ->
            val timeAgo = getTimeAgo(transaction.createdAt)
            TransactionDetailCard(
                TransactionDetail(
                    title = "${transaction.fuelType.name} - ${transaction.litersToPump.toInt()}L",
                    subtitle = transaction.vehicleId,
                    time = timeAgo,
                    accentColor = colorMap[index % colorMap.size],
                    amount = "+${String.format("%.2f", transaction.litersToPump)} L"
                )
            )
        }
    }
}

fun getTimeAgo(dateTime: LocalDateTime): String {
    val now = LocalDateTime.now()
    val duration = java.time.temporal.ChronoUnit.MINUTES.between(dateTime, now)
    
    return when {
        duration < 1 -> "Just now"
        duration < 60 -> "$duration minutes ago"
        duration < 1440 -> "${duration / 60} hours ago"
        duration < 10080 -> "${duration / 1440} days ago"
        else -> "${duration / 10080} weeks ago"
    }
}

data class TransactionDetail(
    val title: String,
    val subtitle: String,
    val time: String,
    val accentColor: Color,
    val amount: String
)

@Composable
fun TransactionDetailCard(transaction: TransactionDetail) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
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
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
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
                        style = MaterialTheme.typography.bodyMedium,
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
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    transaction.amount,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = SuccessGreen
                )
                Text(
                    transaction.time,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )
            }
        }
    }
}

@Composable
fun InsightsAlerts() {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AlertCard(
            type = "info",
            title = "Optimization Tip",
            message = "Your fuel efficiency improved by 12% this month",
            icon = Icons.Default.Info,
            color = ElectricBlue
        )
        AlertCard(
            type = "warning",
            title = "Wallet Low",
            message = "Your fuel wallet is at 62.3% capacity",
            icon = Icons.Default.Warning,
            color = WarningYellow
        )
        AlertCard(
            type = "success",
            title = "Goal Achieved",
            message = "You've completed 47 fuel transactions this month!",
            icon = Icons.Default.CheckCircle,
            color = SuccessGreen
        )
    }
}

@Composable
fun AlertCard(
    type: String,
    title: String,
    message: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    message,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}
