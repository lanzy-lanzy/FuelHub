package dev.ml.fuelhub.presentation.screen

import androidx.compose.animation.animateContentSize
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.ml.fuelhub.domain.usecase.GenerateDailyReportUseCase
import dev.ml.fuelhub.domain.usecase.GenerateWeeklyReportUseCase
import dev.ml.fuelhub.domain.usecase.GenerateMonthlyReportUseCase
import dev.ml.fuelhub.presentation.component.DrawerSwipeIndicator
import dev.ml.fuelhub.ui.theme.*
import timber.log.Timber
import java.time.LocalDate
import java.time.YearMonth

/**
 * Premium Reports Screen - Modern Design
 * Displays daily, weekly, and monthly analytics with stunning visuals
 */
@Composable
fun ReportScreen(
    dailyReportUseCase: GenerateDailyReportUseCase,
    weeklyReportUseCase: GenerateWeeklyReportUseCase,
    monthlyReportUseCase: GenerateMonthlyReportUseCase,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    
    // Animated gradient
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlue)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            ReportsHeader()

            // Tab Selection with Premium Design
            PremiumTabSelector(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                animatedOffset = animatedOffset
            )

            // Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (selectedTab) {
                    0 -> DailyReportContent(dailyReportUseCase, animatedOffset)
                    1 -> WeeklyReportContent(weeklyReportUseCase, animatedOffset)
                    2 -> MonthlyReportContent(monthlyReportUseCase, animatedOffset)
                }
            }
        }

        // Floating Drawer Swipe Indicator
        DrawerSwipeIndicator(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 32.dp)
        )
    }
}

@Composable
fun ReportsHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                "Reports & Analytics",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = VibrantCyan,
                fontSize = 32.sp
            )
            Text(
                "Track your fuel consumption",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { },
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(ElectricBlue.copy(alpha = 0.8f), VibrantCyan.copy(alpha = 0.8f))
                        )
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh Reports",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(AccentOrange, AccentAmber)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Assessment,
                    contentDescription = "Reports",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun PremiumTabSelector(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    animatedOffset: Float
) {
    val tabs = listOf(
        TabItem("Daily", Icons.Default.Today),
        TabItem("Weekly", Icons.Default.DateRange),
        TabItem("Monthly", Icons.Default.DateRange)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        tabs.forEachIndexed { index, tab ->
            PremiumTab(
                label = tab.label,
                icon = tab.icon,
                isSelected = selectedTab == index,
                onClick = { onTabSelected(index) },
                animatedOffset = animatedOffset,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

data class TabItem(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@Composable
fun PremiumTab(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    animatedOffset: Float,
    modifier: Modifier = Modifier
) {
    val gradient = if (isSelected) {
        listOf(ElectricBlue, VibrantCyan)
    } else {
        listOf(SurfaceLight, SurfaceLight)
    }

    Card(
        modifier = modifier
            .height(80.dp)
            .shadow(if (isSelected) 12.dp else 4.dp, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color.Transparent else SurfaceDark
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (isSelected) {
                        Modifier.background(
                            Brush.linearGradient(
                                colors = gradient,
                                start = Offset(animatedOffset, animatedOffset),
                                end = Offset(animatedOffset + 300f, animatedOffset + 300f)
                            )
                        )
                    } else {
                        Modifier.background(SurfaceDark)
                    }
                )
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isSelected) Color.White else TextSecondary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.White else TextSecondary
                )
            }
        }
    }
}

@Composable
fun DailyReportContent(
    useCase: GenerateDailyReportUseCase,
    animatedOffset: Float
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var report by remember { mutableStateOf<GenerateDailyReportUseCase.DailyReportOutput?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    
    LaunchedEffect(selectedDate) {
        isLoading = true
        Timber.d("Loading daily report for: $selectedDate")
        try {
            val result = useCase.execute(selectedDate)
            Timber.d("Daily report loaded: liters=${result.totalLitersConsumed}, count=${result.transactionCount}")
            report = result
        } catch (e: Exception) {
            Timber.e(e, "Error loading daily report for $selectedDate: ${e.message}")
        } finally {
            isLoading = false
        }
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            DateSelectorCard(
                date = selectedDate.toString(),
                onDateClick = { /* Date picker */ },
                animatedOffset = animatedOffset
            )
        }

        item {
            // Summary Stats
            if (isLoading || report == null) {
                // Loading placeholder
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MiniStatCard(
                        title = "Total Liters",
                        value = "Loading...",
                        icon = Icons.Default.LocalGasStation,
                        gradient = listOf(ElectricBlue, VibrantCyan),
                        modifier = Modifier.weight(1f)
                    )
                    MiniStatCard(
                        title = "Transactions",
                        value = "Loading...",
                        icon = Icons.Default.Receipt,
                        gradient = listOf(AccentOrange, AccentAmber),
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MiniStatCard(
                        title = "Total Liters",
                        value = "${report!!.totalLitersConsumed.toInt()} L",
                        icon = Icons.Default.LocalGasStation,
                        gradient = listOf(ElectricBlue, VibrantCyan),
                        modifier = Modifier.weight(1f)
                    )
                    MiniStatCard(
                        title = "Transactions",
                        value = report!!.transactionCount.toString(),
                        icon = Icons.Default.Receipt,
                        gradient = listOf(AccentOrange, AccentAmber),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item {
            if (report != null) {
                DetailedReportCard(
                    title = "Daily Summary - ${selectedDate}",
                    items = listOf(
                        ReportItem("Total Liters", "%.2f L".format(report!!.totalLitersConsumed), SuccessGreen),
                        ReportItem("Transactions", report!!.transactionCount.toString(), VibrantCyan),
                        ReportItem("Completed", report!!.completedTransactions.toString(), SuccessGreen),
                        ReportItem("Pending", report!!.pendingTransactions.toString(), WarningYellow),
                        ReportItem("Failed", report!!.failedTransactions.toString(), ErrorRed),
                        ReportItem("Avg per Transaction", "%.2f L".format(report!!.averageLitersPerTransaction), ElectricBlue)
                    ),
                    animatedOffset = animatedOffset
                )
            }
        }

        item {
            Text(
                "Transaction Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        items(getSampleDailyTransactions()) { transaction ->
            TransactionBreakdownCard(transaction)
        }
    }
}

@Composable
fun WeeklyReportContent(
    useCase: GenerateWeeklyReportUseCase,
    animatedOffset: Float
) {
    var startDate by remember { mutableStateOf(LocalDate.now().minusDays(6)) }
    var report by remember { mutableStateOf<GenerateWeeklyReportUseCase.WeeklyReportOutput?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    
    LaunchedEffect(startDate) {
        isLoading = true
        Timber.d("Loading weekly report from: $startDate")
        try {
            val result = useCase.execute(startDate)
            Timber.d("Weekly report loaded: liters=${result.totalLitersConsumed}, days=${result.dailyBreakdown.size}")
            report = result
        } catch (e: Exception) {
            Timber.e(e, "Error loading weekly report: ${e.message}")
        } finally {
            isLoading = false
        }
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            DateSelectorCard(
                date = "Week of ${startDate}",
                onDateClick = { /* Date picker */ },
                animatedOffset = animatedOffset
            )
        }

        item {
            if (isLoading || report == null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MiniStatCard(
                        title = "Total Liters",
                        value = "Loading...",
                        icon = Icons.Default.Star,
                        gradient = listOf(ElectricBlue, VibrantCyan),
                        modifier = Modifier.weight(1f)
                    )
                    MiniStatCard(
                        title = "Avg Daily",
                        value = "Loading...",
                        icon = Icons.Default.Info,
                        gradient = listOf(SuccessGreen, NeonTeal),
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MiniStatCard(
                        title = "Total Liters",
                        value = "${report!!.totalLitersConsumed.toInt()} L",
                        icon = Icons.Default.Star,
                        gradient = listOf(ElectricBlue, VibrantCyan),
                        modifier = Modifier.weight(1f)
                    )
                    MiniStatCard(
                        title = "Avg Daily",
                        value = "%.1f L".format(report!!.averageDailyConsumption),
                        icon = Icons.Default.Info,
                        gradient = listOf(SuccessGreen, NeonTeal),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item {
            if (report != null) {
                DetailedReportCard(
                    title = "Weekly Summary",
                    items = listOf(
                        ReportItem("Total Liters", "%.2f L".format(report!!.totalLitersConsumed), SuccessGreen),
                        ReportItem("Total Transactions", report!!.totalTransactions.toString(), VibrantCyan),
                        ReportItem("Completed", report!!.completedTransactions.toString(), SuccessGreen),
                        ReportItem("Pending", report!!.pendingTransactions.toString(), WarningYellow),
                        ReportItem("Avg Daily", "%.2f L".format(report!!.averageDailyConsumption), ElectricBlue)
                    ),
                    animatedOffset = animatedOffset
                )
            }
        }

        item {
            Text(
                "Daily Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        if (report != null) {
            items(report!!.dailyBreakdown.toList()) { (date, liters) ->
                DailyBreakdownCard(DayBreakdown(date.toString(), "${"%.1f".format(liters)} L", (liters / 210).toFloat().coerceIn(0f, 1f)))
            }
        } else {
            items(getSampleWeeklyBreakdown()) { day ->
                DailyBreakdownCard(day)
            }
        }
    }
}

@Composable
fun MonthlyReportContent(
    useCase: GenerateMonthlyReportUseCase,
    animatedOffset: Float
) {
    var selectedMonth by remember { mutableStateOf(YearMonth.now()) }
    var report by remember { mutableStateOf<GenerateMonthlyReportUseCase.MonthlyReportOutput?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    
    LaunchedEffect(selectedMonth) {
        isLoading = true
        Timber.d("Loading monthly report for: $selectedMonth")
        try {
            val result = useCase.execute(selectedMonth)
            Timber.d("Monthly report loaded: liters=${result.totalLitersConsumed}, weeks=${result.weeklyBreakdown.size}")
            report = result
        } catch (e: Exception) {
            Timber.e(e, "Error loading monthly report: ${e.message}")
        } finally {
            isLoading = false
        }
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            DateSelectorCard(
                date = selectedMonth.toString(),
                onDateClick = { /* Month picker */ },
                animatedOffset = animatedOffset
            )
        }

        item {
            if (isLoading || report == null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MiniStatCard(
                        title = "Total Liters",
                        value = "Loading...",
                        icon = Icons.Default.Star,
                        gradient = listOf(ElectricBlue, VibrantCyan),
                        modifier = Modifier.weight(1f)
                    )
                    MiniStatCard(
                        title = "Avg Daily",
                        value = "Loading...",
                        icon = Icons.Default.Info,
                        gradient = listOf(SuccessGreen, NeonTeal),
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MiniStatCard(
                        title = "Total Liters",
                        value = "${report!!.totalLitersConsumed.toInt()} L",
                        icon = Icons.Default.Star,
                        gradient = listOf(ElectricBlue, VibrantCyan),
                        modifier = Modifier.weight(1f)
                    )
                    MiniStatCard(
                        title = "Avg Daily",
                        value = "%.1f L".format(report!!.averageDailyConsumption),
                        icon = Icons.Default.Info,
                        gradient = listOf(SuccessGreen, NeonTeal),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item {
            if (report != null) {
                DetailedReportCard(
                    title = "Monthly Summary - ${selectedMonth}",
                    items = listOf(
                        ReportItem("Total Liters", "%.2f L".format(report!!.totalLitersConsumed), SuccessGreen),
                        ReportItem("Total Transactions", report!!.totalTransactions.toString(), VibrantCyan),
                        ReportItem("Completed", report!!.completedTransactions.toString(), SuccessGreen),
                        ReportItem("Pending", report!!.pendingTransactions.toString(), WarningYellow),
                        ReportItem("Cancelled", report!!.cancelledTransactions.toString(), ErrorRed),
                        ReportItem("Avg Daily", "%.2f L".format(report!!.averageDailyConsumption), ElectricBlue)
                    ),
                    animatedOffset = animatedOffset
                )
            }
        }

        item {
            Text(
                "Weekly Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        if (report != null) {
            items(report!!.weeklyBreakdown.toList()) { (weekLabel, liters) ->
                WeeklyBreakdownCard(DayBreakdown(weekLabel, "${"%.1f".format(liters)} L", (liters / 1400).toFloat().coerceIn(0f, 1f)))
            }
        } else {
            items(getSampleMonthlyBreakdown()) { week ->
                WeeklyBreakdownCard(week)
            }
        }
    }
}

@Composable
fun DateSelectorCard(
    date: String,
    onDateClick: () -> Unit,
    animatedOffset: Float
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .clickable { onDateClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(GradientStart, GradientMid),
                        start = Offset(animatedOffset, animatedOffset),
                        end = Offset(animatedOffset + 400f, animatedOffset + 400f)
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Selected Period",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        date,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Select Date",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun MiniStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradient: List<Color>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .shadow(6.dp, RoundedCornerShape(16.dp)),
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
                    modifier = Modifier.size(28.dp)
                )
                Column {
                    Text(
                        value,
                        style = MaterialTheme.typography.titleLarge,
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

data class ReportItem(val label: String, val value: String, val color: Color)

@Composable
fun DetailedReportCard(
    title: String,
    items: List<ReportItem>,
    animatedOffset: Float
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceDark
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = VibrantCyan
            )

            items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        item.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Text(
                        item.value,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = item.color
                    )
                }
                if (item != items.last()) {
                    Divider(
                        color = SurfaceLight,
                        thickness = 1.dp
                    )
                }
            }
        }
    }
}

data class DayBreakdown(val date: String, val liters: String, val percentage: Float)

fun getSampleDailyTransactions() = listOf(
    DayBreakdown("Gasoline - Vehicle #1234", "50.0 L", 0.8f),
    DayBreakdown("Diesel - Vehicle #5678", "75.5 L", 1.0f),
    DayBreakdown("Premium - Vehicle #9012", "40.0 L", 0.6f)
)

fun getSampleWeeklyBreakdown() = listOf(
    DayBreakdown("Monday", "176.4 L", 0.9f),
    DayBreakdown("Tuesday", "198.2 L", 1.0f),
    DayBreakdown("Wednesday", "165.8 L", 0.8f),
    DayBreakdown("Thursday", "187.5 L", 0.95f),
    DayBreakdown("Friday", "210.3 L", 1.0f),
    DayBreakdown("Saturday", "145.2 L", 0.7f),
    DayBreakdown("Sunday", "151.1 L", 0.75f)
)

fun getSampleMonthlyBreakdown() = listOf(
    DayBreakdown("Week 1", "1,234.5 L", 0.9f),
    DayBreakdown("Week 2", "1,456.8 L", 1.0f),
    DayBreakdown("Week 3", "1,321.2 L", 0.85f),
    DayBreakdown("Week 4", "1,419.6 L", 0.95f)
)

@Composable
fun TransactionBreakdownCard(transaction: DayBreakdown) {
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
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    ElectricBlue.copy(alpha = 0.3f),
                                    VibrantCyan.copy(alpha = 0.3f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalGasStation,
                        contentDescription = null,
                        tint = VibrantCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column(
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        transaction.date,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        "Fuel Delivery",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
            Text(
                transaction.liters,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = SuccessGreen
            )
        }
    }
}

@Composable
fun DailyBreakdownCard(day: DayBreakdown) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceDark
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    day.date,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    day.liters,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = VibrantCyan
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(SurfaceLight)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(day.percentage)
                        .fillMaxHeight()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(ElectricBlue, VibrantCyan)
                            )
                        )
                )
            }
        }
    }
}

@Composable
fun WeeklyBreakdownCard(week: DayBreakdown) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceDark
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    week.date,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    week.liters,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = AccentOrange
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(SurfaceLight)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(week.percentage)
                        .fillMaxHeight()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(AccentOrange, AccentAmber)
                            )
                        )
                )
            }
        }
    }
}
