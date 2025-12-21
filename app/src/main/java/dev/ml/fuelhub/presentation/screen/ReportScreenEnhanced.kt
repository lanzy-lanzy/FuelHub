package dev.ml.fuelhub.presentation.screen

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.YearMonth
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import dev.ml.fuelhub.data.model.FuelType
import dev.ml.fuelhub.data.model.TransactionStatus
import dev.ml.fuelhub.presentation.viewmodel.ReportsViewModel
import dev.ml.fuelhub.ui.theme.*
import timber.log.Timber
import java.text.NumberFormat
import java.util.Locale

/**
 * Format a number with thousands separator comma
 */
fun formatNumberWithComma(value: Double): String {
    val formatter = NumberFormat.getInstance(Locale.US)
    formatter.minimumFractionDigits = 0
    formatter.maximumFractionDigits = 0
    return formatter.format(value.toLong())
}

/**
 * Enhanced Reports Screen with advanced filtering and export capabilities
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ReportScreenEnhanced(
    viewModel: ReportsViewModel,
    modifier: Modifier = Modifier,
    pdfPrintManager: dev.ml.fuelhub.data.util.PdfPrintManager? = null
) {
    var selectedTab by remember { mutableStateOf(0) }
    var showFiltersPanel by remember { mutableStateOf(false) }
    var showExportMenu by remember { mutableStateOf(false) }
    
    val isLoading by viewModel.isLoading.collectAsState()
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isLoading,
        onRefresh = { viewModel.refreshData() }
    )
    
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
            .pullRefresh(pullRefreshState)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with Controls
            ReportsHeaderEnhanced(
                onFilterClick = { showFiltersPanel = !showFiltersPanel },
                onExportClick = { showExportMenu = !showExportMenu },
                animatedOffset = animatedOffset
            )

            // Filters Panel (Collapsible)
            if (showFiltersPanel) {
                FiltersPanelContent(viewModel)
            }

            // Export Menu
            if (showExportMenu) {
                ExportMenuContent(
                    viewModel = viewModel,
                    pdfPrintManager = pdfPrintManager,
                    selectedTab = selectedTab
                )
            }

            // Tab Selection
            PremiumTabSelector(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                animatedOffset = animatedOffset
            )

            // Content Area
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (selectedTab) {
                    0 -> DailyReportContentEnhanced(viewModel, animatedOffset)
                    1 -> WeeklyReportContentEnhanced(viewModel, animatedOffset)
                    2 -> MonthlyReportContentEnhanced(viewModel, animatedOffset)
                }
            }
        }
        
        // Pull-to-refresh indicator
        PullRefreshIndicator(
            refreshing = isLoading,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = VibrantCyan,
            contentColor = DeepBlue
        )
    }
}

@Composable
fun ReportsHeaderEnhanced(
    onFilterClick: () -> Unit,
    onExportClick: () -> Unit,
    animatedOffset: Float
) {
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
                "Advanced Fuel Tracking",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Filter Button
            IconButton(
                onClick = onFilterClick,
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
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filters",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Export Button
            IconButton(
                onClick = onExportClick,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(AccentOrange, AccentAmber)
                        )
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Export",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun FiltersPanelContent(viewModel: ReportsViewModel) {
    val filterState by viewModel.filterState.collectAsState()
    val availableVehicles by viewModel.availableVehicles.collectAsState()
    val availableDrivers by viewModel.availableDrivers.collectAsState()

    var expandedSection by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Filters",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = VibrantCyan
            )

            // Date Range Filter
            ExpandableFilterSection(
                title = "Date Range",
                isExpanded = expandedSection == "date",
                onToggle = { expandedSection = if (expandedSection == "date") null else "date" }
            ) {
                DateRangeFilterContent(
                    from = filterState.dateFrom,
                    to = filterState.dateTo,
                    onDateRangeChange = { from, to -> viewModel.updateDateRange(from, to) }
                )
            }

            // Fuel Type Filter
            ExpandableFilterSection(
                title = "Fuel Types",
                isExpanded = expandedSection == "fuel",
                onToggle = { expandedSection = if (expandedSection == "fuel") null else "fuel" }
            ) {
                FuelTypeFilterContent(
                    selectedTypes = filterState.selectedFuelTypes,
                    onSelectionChange = { viewModel.updateFuelTypes(it) }
                )
            }

            // Status Filter
            ExpandableFilterSection(
                title = "Transaction Status",
                isExpanded = expandedSection == "status",
                onToggle = { expandedSection = if (expandedSection == "status") null else "status" }
            ) {
                StatusFilterContent(
                    selectedStatuses = filterState.selectedStatuses,
                    onSelectionChange = { viewModel.updateStatuses(it) }
                )
            }

            // Vehicle Filter
            if (availableVehicles.isNotEmpty()) {
                ExpandableFilterSection(
                    title = "Vehicles (${filterState.selectedVehicles.size})",
                    isExpanded = expandedSection == "vehicle",
                    onToggle = { expandedSection = if (expandedSection == "vehicle") null else "vehicle" }
                ) {
                    MultiSelectFilterContent(
                        items = availableVehicles,
                        selected = filterState.selectedVehicles,
                        onSelectionChange = { viewModel.updateVehicles(it) }
                    )
                }
            }

            // Driver Filter
            if (availableDrivers.isNotEmpty()) {
                ExpandableFilterSection(
                    title = "Drivers (${filterState.selectedDrivers.size})",
                    isExpanded = expandedSection == "driver",
                    onToggle = { expandedSection = if (expandedSection == "driver") null else "driver" }
                ) {
                    MultiSelectFilterContent(
                        items = availableDrivers,
                        selected = filterState.selectedDrivers,
                        onSelectionChange = { viewModel.updateDrivers(it) }
                    )
                }
            }

            // Liter Range Filter
            ExpandableFilterSection(
                title = "Liter Range",
                isExpanded = expandedSection == "liters",
                onToggle = { expandedSection = if (expandedSection == "liters") null else "liters" }
            ) {
                LiterRangeFilterContent(
                    min = filterState.minLiters,
                    max = filterState.maxLiters,
                    onRangeChange = { min, max -> viewModel.updateLiterRange(min, max) }
                )
            }

            // Search
            OutlinedTextField(
                value = filterState.searchKeyword,
                onValueChange = { viewModel.updateSearchKeyword(it) },
                label = { Text("Search (Ref, Driver, Vehicle)") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true
            )

            // Reset Button
            Button(
                onClick = { viewModel.resetFilters() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF666666)
                )
            ) {
                Text("Reset All Filters")
            }
        }
    }
}

@Composable
fun ExportMenuContent(
    viewModel: ReportsViewModel,
    pdfPrintManager: dev.ml.fuelhub.data.util.PdfPrintManager?,
    selectedTab: Int
) {
    val filterState by viewModel.filterState.collectAsState()
    val filteredData by viewModel.filteredData.collectAsState()
    
    val tabNames = listOf("Daily", "Weekly", "Monthly")
    val reportName = tabNames.getOrElse(selectedTab) { "Report" }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Export Report",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = VibrantCyan
            )

            // PDF Export
            ExportButton(
                icon = Icons.Default.PictureAsPdf,
                label = "Export as PDF",
                description = "Save filtered report as PDF document",
                color = Color(0xFFD32F2F),
                onClick = {
                    if (pdfPrintManager != null) {
                        val reportPdfGenerator = dev.ml.fuelhub.data.util.ReportPdfGenerator(pdfPrintManager.context)
                        val pdfPath = reportPdfGenerator.generateReportPdf(
                            reportName = reportName,
                            dateFrom = filterState.dateFrom,
                            dateTo = filterState.dateTo,
                            filteredData = filteredData
                        )
                        if (pdfPath != null) {
                            // Open the PDF to show user it was exported
                            pdfPrintManager.openPdfInViewer(pdfPath)
                            Timber.d("PDF exported and opened: $pdfPath")
                        } else {
                            Timber.e("Failed to generate PDF report")
                        }
                    } else {
                        Timber.w("PDF Print Manager not available")
                    }
                }
            )

            // Print
            ExportButton(
                icon = Icons.Default.Print,
                label = "Print Report",
                description = "Print filtered report directly",
                color = ElectricBlue,
                onClick = {
                    if (pdfPrintManager != null) {
                        val reportPdfGenerator = dev.ml.fuelhub.data.util.ReportPdfGenerator(pdfPrintManager.context)
                        val pdfPath = reportPdfGenerator.generateReportPdf(
                            reportName = reportName,
                            dateFrom = filterState.dateFrom,
                            dateTo = filterState.dateTo,
                            filteredData = filteredData
                        )
                        if (pdfPath != null) {
                            pdfPrintManager.sharePdfFile(pdfPath) // Opens PDF viewer for printing
                            Timber.d("Print initiated for: $pdfPath")
                        } else {
                            Timber.e("Failed to generate PDF for printing")
                        }
                    } else {
                        Timber.w("PDF Print Manager not available")
                    }
                }
            )

            // Share
            ExportButton(
                icon = Icons.Default.Share,
                label = "Share Report",
                description = "Share report via email or messaging",
                color = SuccessGreen,
                onClick = {
                    if (pdfPrintManager != null) {
                        val reportPdfGenerator = dev.ml.fuelhub.data.util.ReportPdfGenerator(pdfPrintManager.context)
                        val pdfPath = reportPdfGenerator.generateReportPdf(
                            reportName = reportName,
                            dateFrom = filterState.dateFrom,
                            dateTo = filterState.dateTo,
                            filteredData = filteredData
                        )
                        if (pdfPath != null) {
                            pdfPrintManager.sharePdfFile(pdfPath)
                            Timber.d("Share initiated for: $pdfPath")
                        } else {
                            Timber.e("Failed to generate PDF for sharing")
                        }
                    } else {
                        Timber.w("PDF Print Manager not available")
                    }
                }
            )
        }
    }
}

@Composable
fun ExpandableFilterSection(
    title: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier
                    .size(24.dp)
                    .animateContentSize()
            )
        }

        if (isExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1A1A2E))
                    .padding(12.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun DateRangeFilterContent(
    from: LocalDate,
    to: LocalDate,
    onDateRangeChange: (LocalDate, LocalDate) -> Unit
) {
    var showFromDatePicker by remember { mutableStateOf(false) }
    var showToDatePicker by remember { mutableStateOf(false) }
    var selectedFromDate by remember { mutableStateOf(from) }
    var selectedToDate by remember { mutableStateOf(to) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // From Date Picker
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                "From Date",
                style = MaterialTheme.typography.labelSmall,
                color = VibrantCyan,
                fontWeight = FontWeight.SemiBold
            )
            Button(
                onClick = { showFromDatePicker = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SurfaceLight.copy(alpha = 0.5f),
                    contentColor = TextPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    selectedFromDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // To Date Picker
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                "To Date",
                style = MaterialTheme.typography.labelSmall,
                color = VibrantCyan,
                fontWeight = FontWeight.SemiBold
            )
            Button(
                onClick = { showToDatePicker = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SurfaceLight.copy(alpha = 0.5f),
                    contentColor = TextPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    selectedToDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Quick Select Buttons
        Text(
            "Quick Select",
            style = MaterialTheme.typography.labelSmall,
            color = VibrantCyan,
            fontWeight = FontWeight.SemiBold
        )
        
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Today", "This Week", "This Month", "Last Month").forEach { period ->
                AssistChip(
                    onClick = {
                        when (period) {
                            "Today" -> {
                                selectedFromDate = LocalDate.now()
                                selectedToDate = LocalDate.now()
                                onDateRangeChange(LocalDate.now(), LocalDate.now())
                            }
                            "This Week" -> {
                                selectedFromDate = LocalDate.now().minusDays(7)
                                selectedToDate = LocalDate.now()
                                onDateRangeChange(
                                    LocalDate.now().minusDays(7),
                                    LocalDate.now()
                                )
                            }
                            "This Month" -> {
                                selectedFromDate = LocalDate.now().minusDays(30)
                                selectedToDate = LocalDate.now()
                                onDateRangeChange(
                                    LocalDate.now().minusDays(30),
                                    LocalDate.now()
                                )
                            }
                            "Last Month" -> {
                                selectedFromDate = LocalDate.now().minusDays(60)
                                selectedToDate = LocalDate.now().minusDays(30)
                                onDateRangeChange(
                                    LocalDate.now().minusDays(60),
                                    LocalDate.now().minusDays(30)
                                )
                            }
                        }
                    },
                    label = { Text(period, fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // From Date Picker Dialog
    if (showFromDatePicker) {
        DatePickerDialog(
            initialDate = selectedFromDate,
            onDateSelected = { newDate ->
                selectedFromDate = newDate
                onDateRangeChange(newDate, selectedToDate)
                showFromDatePicker = false
            },
            onDismiss = { showFromDatePicker = false }
        )
    }

    // To Date Picker Dialog
    if (showToDatePicker) {
        DatePickerDialog(
            initialDate = selectedToDate,
            onDateSelected = { newDate ->
                selectedToDate = newDate
                onDateRangeChange(selectedFromDate, newDate)
                showToDatePicker = false
            },
            onDismiss = { showToDatePicker = false }
        )
    }
}

@Composable
fun DatePickerDialog(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    var currentDate by remember { mutableStateOf(initialDate) }
    var currentMonth by remember { mutableStateOf(YearMonth.of(initialDate.year, initialDate.month)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Select Date",
                style = MaterialTheme.typography.headlineSmall,
                color = VibrantCyan
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Month and Year Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceDark, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { 
                        currentMonth = currentMonth.minusMonths(1)
                    }) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Previous month",
                            tint = VibrantCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        currentMonth.atDay(1).format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { 
                        currentMonth = currentMonth.plusMonths(1)
                    }) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Next month",
                            tint = VibrantCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Calendar Grid
                val firstDay = currentMonth.atDay(1)
                val lastDay = currentMonth.atEndOfMonth()
                val daysInMonth = lastDay.dayOfMonth
                val firstDayOfWeek = firstDay.dayOfWeek.value % 7

                Column {
                    // Day headers (Sun-Sat)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                            Text(
                                day,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Calendar Days
                    val weeks = mutableListOf<List<Int>>()
                    val days = mutableListOf<Int>()

                    // Add empty days for days before month starts
                    repeat(firstDayOfWeek) { days.add(0) }

                    // Add days of month
                    for (day in 1..daysInMonth) {
                        days.add(day)
                        if (days.size == 7) {
                            weeks.add(days.toList())
                            days.clear()
                        }
                    }
                    if (days.isNotEmpty()) {
                        weeks.add(days)
                    }

                    weeks.forEach { week ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            week.forEach { day ->
                                if (day == 0) {
                                    Spacer(modifier = Modifier.weight(1f))
                                } else {
                                    val dayDate = currentMonth.atDay(day)
                                    val isSelected = dayDate == currentDate
                                    val isToday = dayDate == LocalDate.now()

                                    Button(
                                        onClick = { currentDate = dayDate },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(36.dp)
                                            .padding(2.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = when {
                                                isSelected -> VibrantCyan
                                                isToday -> ElectricBlue.copy(alpha = 0.3f)
                                                else -> SurfaceDark
                                            },
                                            contentColor = when {
                                                isSelected -> DeepBlue
                                                else -> TextPrimary
                                            }
                                        ),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text(
                                            day.toString(),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Selected date display
                Text(
                    "Selected: ${currentDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = VibrantCyan,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onDateSelected(currentDate) },
                colors = ButtonDefaults.buttonColors(containerColor = VibrantCyan)
            ) {
                Text("Confirm", color = DeepBlue, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                border = androidx.compose.foundation.BorderStroke(1.dp, TextSecondary)
            ) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = SurfaceDark,
        titleContentColor = VibrantCyan
    )
}

@Composable
fun FuelTypeFilterContent(
    selectedTypes: Set<FuelType>,
    onSelectionChange: (Set<FuelType>) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FuelType.values().forEach { fuelType ->
            FilterChip(
                selected = fuelType in selectedTypes,
                onClick = {
                    val updated = selectedTypes.toMutableSet()
                    if (fuelType in updated) updated.remove(fuelType) else updated.add(fuelType)
                    onSelectionChange(updated)
                },
                label = { Text(fuelType.name) }
            )
        }
    }
}

@Composable
fun StatusFilterContent(
    selectedStatuses: Set<TransactionStatus>,
    onSelectionChange: (Set<TransactionStatus>) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TransactionStatus.values().forEach { status ->
            FilterChip(
                selected = status in selectedStatuses,
                onClick = {
                    val updated = selectedStatuses.toMutableSet()
                    if (status in updated) updated.remove(status) else updated.add(status)
                    onSelectionChange(updated)
                },
                label = { Text(status.name) }
            )
        }
    }
}

@Composable
fun MultiSelectFilterContent(
    items: List<String>,
    selected: Set<String>,
    onSelectionChange: (Set<String>) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { item ->
            FilterChip(
                selected = item in selected,
                onClick = {
                    val updated = selected.toMutableSet()
                    if (item in updated) updated.remove(item) else updated.add(item)
                    onSelectionChange(updated)
                },
                label = { Text(item, maxLines = 1) }
            )
        }
    }
}

@Composable
fun LiterRangeFilterContent(
    min: Double,
    max: Double,
    onRangeChange: (Double, Double) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Min: $min L - Max: $max L", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
    }
}

@Composable
fun ExportButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    description: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A2E))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(label, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// Content Composables for each report type
@Composable
fun DailyReportContentEnhanced(
    viewModel: ReportsViewModel,
    animatedOffset: Float
) {
    val filteredData by viewModel.filteredData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            SummaryStatsRow(filteredData, animatedOffset)
        }

        item {
            if (filteredData.transactions.isNotEmpty()) {
                DetailedTransactionsList(filteredData.transactions)
            } else {
                EmptyStateContent("No transactions found with current filters")
            }
        }
    }
}

@Composable
fun WeeklyReportContentEnhanced(
    viewModel: ReportsViewModel,
    animatedOffset: Float
) {
    val filteredData by viewModel.filteredData.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            SummaryStatsRow(filteredData, animatedOffset)
        }

        item {
            if (filteredData.transactions.isNotEmpty()) {
                DetailedTransactionsList(filteredData.transactions)
            } else {
                EmptyStateContent("No transactions found with current filters")
            }
        }
    }
}

@Composable
fun MonthlyReportContentEnhanced(
    viewModel: ReportsViewModel,
    animatedOffset: Float
) {
    val filteredData by viewModel.filteredData.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            SummaryStatsRow(filteredData, animatedOffset)
        }

        item {
            if (filteredData.transactions.isNotEmpty()) {
                DetailedTransactionsList(filteredData.transactions)
            } else {
                EmptyStateContent("No transactions found with current filters")
            }
        }
    }
}

@Composable
fun SummaryStatsRow(filteredData: dev.ml.fuelhub.presentation.viewmodel.ReportFilteredData, animatedOffset: Float) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MiniStatCard(
                title = "Total Liters",
                value = "${String.format("%.1f", filteredData.totalLiters)} L",
                icon = Icons.Default.LocalGasStation,
                gradient = listOf(ElectricBlue, VibrantCyan),
                modifier = Modifier.weight(1f)
            )
            MiniStatCard(
                 title = "Total Cost",
                 value = "₱${formatNumberWithComma(filteredData.totalCost)}.${String.format("%02d", (filteredData.totalCost % 1 * 100).toInt())}",
                 icon = Icons.Default.AttachMoney,
                 gradient = listOf(Color(0xFFFFB74D), Color(0xFFFFA726)),
                 modifier = Modifier.weight(1f)
             )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MiniStatCard(
                 title = "Transactions",
                 value = formatNumberWithComma(filteredData.transactionCount.toDouble()),
                 icon = Icons.Default.Receipt,
                 gradient = listOf(AccentOrange, AccentAmber),
                 modifier = Modifier.weight(1f)
             )
            MiniStatCard(
                 title = "Completed",
                 value = formatNumberWithComma(filteredData.completedCount.toDouble()),
                 icon = Icons.Default.CheckCircle,
                 gradient = listOf(SuccessGreen, NeonTeal),
                 modifier = Modifier.weight(1f)
             )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MiniStatCard(
                 title = "Pending",
                 value = formatNumberWithComma(filteredData.pendingCount.toDouble()),
                 icon = Icons.Default.Schedule,
                 gradient = listOf(WarningYellow, AccentOrange),
                 modifier = Modifier.weight(1f)
             )
            MiniStatCard(
                 title = "Avg Cost/Liter",
                 value = if (filteredData.totalLiters > 0) {
                     val avgCost = filteredData.totalCost / filteredData.totalLiters
                     "₱${formatNumberWithComma(avgCost)}.${String.format("%02d", (avgCost % 1 * 100).toInt())}"
                 } else {
                     "₱0.00"
                 },
                 icon = Icons.Default.TrendingUp,
                 gradient = listOf(Color(0xFF81C784), Color(0xFF66BB6A)),
                 modifier = Modifier.weight(1f)
             )
        }
    }
}

@Composable
fun DetailedTransactionsList(transactions: List<dev.ml.fuelhub.data.model.FuelTransaction>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                "Detailed Transactions (${transactions.size} records)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = VibrantCyan,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            transactions.take(50).forEach { transaction ->
                TransactionDetailRow(transaction)
                if (transaction != transactions.take(50).last()) {
                    Divider(color = SurfaceLight, thickness = 0.5.dp)
                }
            }

            if (transactions.size > 50) {
                Text(
                    "Showing 50 of ${transactions.size} records",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }
}

@Composable
fun TransactionDetailRow(transaction: dev.ml.fuelhub.data.model.FuelTransaction) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    transaction.referenceNumber,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    "${transaction.driverFullName ?: transaction.driverName} • ${transaction.vehicleType}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "${String.format("%.2f", transaction.litersToPump)} L",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = SuccessGreen
                )
                Text(
                    transaction.status.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = when (transaction.status) {
                        TransactionStatus.COMPLETED -> SuccessGreen
                        TransactionStatus.PENDING -> WarningYellow
                        else -> ErrorRed
                    }
                )
            }
        }

        // Cost Information Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceLight.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                     "Cost per Liter: ₱${formatNumberWithComma(transaction.costPerLiter)}.${String.format("%02d", (transaction.costPerLiter % 1 * 100).toInt())}",
                     style = MaterialTheme.typography.labelSmall,
                     color = TextSecondary
                 )
                 }
                 Text(
                     "Total: ₱${formatNumberWithComma(transaction.getTotalCost())}.${String.format("%02d", (transaction.getTotalCost() % 1 * 100).toInt())}",
                     style = MaterialTheme.typography.bodySmall,
                     fontWeight = FontWeight.SemiBold,
                     color = Color(0xFFFFB74D)
                 )
        }
    }
}

@Composable
fun EmptyStateContent(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SearchOff,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(64.dp)
            )
            Text(
                message,
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
