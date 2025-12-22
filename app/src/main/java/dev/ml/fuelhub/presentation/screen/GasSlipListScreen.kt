package dev.ml.fuelhub.presentation.screen

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.PaddingValues
import dev.ml.fuelhub.data.model.GasSlip
import dev.ml.fuelhub.data.model.FuelType
import dev.ml.fuelhub.data.model.GasSlipStatus
import dev.ml.fuelhub.presentation.viewmodel.GasSlipUiState
import dev.ml.fuelhub.presentation.viewmodel.GasSlipManagementViewModel
import dev.ml.fuelhub.presentation.component.DrawerSwipeIndicator
import dev.ml.fuelhub.ui.theme.*
import timber.log.Timber
import java.time.format.DateTimeFormatter

@Composable
fun GasSlipListScreen(
    gasSlipViewModel: GasSlipManagementViewModel,
    onGasSlipSelected: (GasSlip) -> Unit = {},
    onPrintClick: (GasSlip) -> Unit = {},
    onShareClick: (GasSlip) -> Unit = {},
    onCancelClick: (gasSlipId: String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("ALL") }
    var showAdvancedFilters by remember { mutableStateOf(false) }
    var showBulkActionBar by remember { mutableStateOf(false) }
    
    val uiState by gasSlipViewModel.uiState.collectAsState()
    val allGasSlips by gasSlipViewModel.allGasSlips.collectAsState()
    val searchQuery by gasSlipViewModel.searchQuery.collectAsState()
    val sortByLatest by gasSlipViewModel.sortByLatest.collectAsState()
    val isBulkActionMode by gasSlipViewModel.isBulkActionMode.collectAsState()
    val selectedForBulkAction by gasSlipViewModel.selectedForBulkAction.collectAsState()
    
    val filteredGasSlips = gasSlipViewModel.getFilteredAndSearchedGasSlips()
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlue)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                if (isBulkActionMode) {
                // Bulk Action Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .background(
                            color = VibrantCyan.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "${selectedForBulkAction.size} selected",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = VibrantCyan
                        )
                        Text(
                            "Bulk Actions Available",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    IconButton(
                        onClick = { gasSlipViewModel.toggleBulkActionMode() },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Exit Bulk Mode",
                            tint = ErrorRed,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Gas Slips",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = VibrantCyan,
                            fontSize = 28.sp
                        )
                        Text(
                            "Manage fuel slips & dispensing",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    IconButton(
                        onClick = { gasSlipViewModel.loadAllGasSlips() },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(ElectricBlue, VibrantCyan)
                                )
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            
            // Search Bar
            TextField(
                value = searchQuery,
                onValueChange = { gasSlipViewModel.updateSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(12.dp)),
                placeholder = {
                    Text(
                        "Search by reference, driver, vehicle, destination...",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = VibrantCyan
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { gasSlipViewModel.updateSearchQuery("") }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = TextSecondary
                            )
                        }
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = SurfaceDark,
                    unfocusedContainerColor = SurfaceDark,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedIndicatorColor = VibrantCyan,
                    unfocusedIndicatorColor = SurfaceLight
                ),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium
            )
            
            // Bulk Action Toolbar
            if (isBulkActionMode && selectedForBulkAction.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .background(
                            color = SuccessGreen.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            gasSlipViewModel.selectAll(filteredGasSlips.map { it.id })
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Select All", fontSize = 11.sp)
                    }
                    
                    Button(
                        onClick = { gasSlipViewModel.bulkMarkPendingAsDispensed() },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Mark Dispensed", fontSize = 11.sp)
                    }
                }
            }
            
            // Custom Filter Tabs + Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("ALL", "PENDING", "USED").forEach { filter ->
                            val isSelected = selectedFilter == filter
                            
                            if (isSelected) {
                                Button(
                                    onClick = { 
                                        selectedFilter = filter
                                        gasSlipViewModel.setFilterStatus(filter)
                                    },
                                    modifier = Modifier
                                        .height(36.dp)
                                        .weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = VibrantCyan,
                                        contentColor = DeepBlue
                                    ),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text(
                                        text = filter,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { 
                                        selectedFilter = filter
                                        gasSlipViewModel.setFilterStatus(filter)
                                    },
                                    modifier = Modifier
                                        .height(36.dp)
                                        .weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, TextSecondary.copy(alpha = 0.5f)),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = Color.Transparent,
                                        contentColor = TextSecondary
                                    ),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text(
                                        text = filter,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Advanced Filters Button
                IconButton(
                    onClick = { showAdvancedFilters = !showAdvancedFilters },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (showAdvancedFilters) VibrantCyan else SurfaceDark
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Filters",
                        tint = if (showAdvancedFilters) DeepBlue else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                // Sort Button
                IconButton(
                    onClick = { gasSlipViewModel.setSortByLatest(!sortByLatest) },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceDark)
                ) {
                    Icon(
                        imageVector = if (sortByLatest) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                        contentDescription = "Sort",
                        tint = VibrantCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                // Bulk Actions Button
                IconButton(
                    onClick = { gasSlipViewModel.toggleBulkActionMode() },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isBulkActionMode) ElectricBlue else SurfaceDark)
                ) {
                    Icon(
                        imageVector = Icons.Default.SelectAll,
                        contentDescription = "Bulk Actions",
                        tint = if (isBulkActionMode) Color.White else VibrantCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            // Advanced Filters Panel
            if (showAdvancedFilters) {
                AdvancedFiltersPanel(
                    gasSlipViewModel = gasSlipViewModel,
                    onDismiss = { showAdvancedFilters = false }
                )
            }
            
            // Content
            when (uiState) {
                is GasSlipUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = VibrantCyan)
                    }
                }
                
                is GasSlipUiState.Success -> {
                    if (filteredGasSlips.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Receipt,
                                    contentDescription = null,
                                    tint = VibrantCyan,
                                    modifier = Modifier.size(64.dp)
                                )
                                Text(
                                    "No gas slips found",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(filteredGasSlips) { gasSlip ->
                                GasSlipCard(
                                    gasSlip = gasSlip,
                                    onSelect = { onGasSlipSelected(gasSlip) },
                                    onPrint = { onPrintClick(gasSlip) },
                                    onShare = { onShareClick(gasSlip) },
                                    onCancel = { onCancelClick(gasSlip.id) },
                                    isBulkActionMode = isBulkActionMode,
                                    isSelected = selectedForBulkAction.contains(gasSlip.id),
                                    onSelectForBulk = { gasSlipViewModel.toggleSelection(gasSlip.id) }
                                )
                            }
                        }
                    }
                }
                
                is GasSlipUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = ErrorRed,
                                modifier = Modifier.size(64.dp)
                            )
                            Text(
                                (uiState as GasSlipUiState.Error).message,
                                style = MaterialTheme.typography.bodyLarge,
                                color = ErrorRed,
                                fontWeight = FontWeight.Medium
                            )
                            Button(
                                onClick = { gasSlipViewModel.loadAllGasSlips() },
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                            ) {
                                Text("Retry", color = Color.White)
                            }
                        }
                    }
                }
                
                else -> {}
            }
            }
            
            // Floating Drawer Swipe Indicator
            DrawerSwipeIndicator(
                tintColor = VibrantCyan.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun GasSlipCard(
    gasSlip: GasSlip,
    onSelect: () -> Unit,
    onPrint: () -> Unit,
    onShare: () -> Unit,
    onCancel: () -> Unit = {},
    isBulkActionMode: Boolean = false,
    isSelected: Boolean = false,
    onSelectForBulk: () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clip(RoundedCornerShape(16.dp))
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clickable {
                if (isBulkActionMode) {
                    onSelectForBulk()
                } else {
                    expanded = !expanded
                }
            }
            .background(
                if (isSelected) VibrantCyan.copy(alpha = 0.1f) else Color.Transparent
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) SurfaceDark.copy(alpha = 0.8f) else SurfaceDark
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, VibrantCyan) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = if (expanded) 12.dp else 0.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isBulkActionMode) {
                    androidx.compose.material3.Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onSelectForBulk() },
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        gasSlip.referenceNumber,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        gasSlip.driverName,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                
                // Status Badge
                val statusColor = when (gasSlip.status) {
                    GasSlipStatus.PENDING -> AccentOrange
                    GasSlipStatus.DISPENSED -> SuccessGreen
                    GasSlipStatus.USED -> VibrantCyan
                    GasSlipStatus.CANCELLED -> ErrorRed
                }
                Box(
                    modifier = Modifier
                        .background(
                            color = statusColor,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        gasSlip.getStatusBadge(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 10.sp
                    )
                }
                
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = VibrantCyan,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Expanded Content
            if (expanded) {
                Divider(color = SurfaceLight, modifier = Modifier.padding(vertical = 8.dp))
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GasSlipDetailRow("Vehicle", "${gasSlip.vehiclePlateNumber} - ${gasSlip.vehicleType}")
                    GasSlipDetailRow("Fuel Type", gasSlip.fuelType.name)
                    GasSlipDetailRow("Liters", "${gasSlip.litersToPump} L")
                    GasSlipDetailRow("Destination", gasSlip.destination)
                    GasSlipDetailRow("Purpose", gasSlip.tripPurpose)
                    GasSlipDetailRow(
                        "Date",
                        gasSlip.transactionDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                    )
                }
                
                // Action Buttons
                if (!isBulkActionMode) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // First row: View, Print, Share
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onSelect,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("View", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                            
                            Button(
                                onClick = onPrint,
                                enabled = gasSlip.status != GasSlipStatus.CANCELLED,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = VibrantCyan,
                                    disabledContainerColor = VibrantCyan.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Print,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = if (gasSlip.status == GasSlipStatus.CANCELLED) DeepBlue.copy(alpha = 0.5f) else DeepBlue
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Print",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (gasSlip.status == GasSlipStatus.CANCELLED) DeepBlue.copy(alpha = 0.5f) else DeepBlue
                                )
                            }
                            
                            Button(
                                onClick = onShare,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        
                        // Cancel button (if pending) - full width
                        if (gasSlip.status == GasSlipStatus.PENDING) {
                            Button(
                                onClick = onCancel,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Cancel Slip", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GasSlipDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )
        Text(
            value,
            style = MaterialTheme.typography.labelSmall,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun AdvancedFiltersPanel(
    gasSlipViewModel: GasSlipManagementViewModel,
    onDismiss: () -> Unit
) {
    val fuelTypeFilter by gasSlipViewModel.fuelTypeFilter.collectAsState()
    val vehicleFilter by gasSlipViewModel.vehicleFilter.collectAsState()
    val driverFilter by gasSlipViewModel.driverFilter.collectAsState()
    val allGasSlips by gasSlipViewModel.allGasSlips.collectAsState()
    
    var expandedFuelType by remember { mutableStateOf(false) }
    var expandedVehicle by remember { mutableStateOf(false) }
    var expandedDriver by remember { mutableStateOf(false) }
    
    val uniqueFuelTypes = remember(allGasSlips) {
        allGasSlips.map { it.fuelType.name }.distinct().sorted()
    }
    
    val uniqueVehicles = remember(allGasSlips) {
        allGasSlips.map { it.vehiclePlateNumber }.distinct().sorted()
    }
    
    val uniqueDrivers = remember(allGasSlips) {
        allGasSlips.mapNotNull { it.driverFullName ?: it.driverName }.distinct().sorted()
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .clip(RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Advanced Filters",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = VibrantCyan
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Divider(color = SurfaceLight, thickness = 1.dp)
            
            // Fuel Type Filter
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { expandedFuelType = !expandedFuelType },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (expandedFuelType) ElectricBlue else SurfaceLight
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (fuelTypeFilter != null) "Fuel Type: $fuelTypeFilter" else "Fuel Type",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Icon(
                            imageVector = if (expandedFuelType) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                if (expandedFuelType) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Clear button
                        if (fuelTypeFilter != null) {
                            Button(
                                onClick = { gasSlipViewModel.setFuelTypeFilter(null) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(32.dp),
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ErrorRed.copy(alpha = 0.2f)
                                ),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    "Clear Filter",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ErrorRed,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.sp
                                )
                            }
                        }
                        
                        uniqueFuelTypes.forEach { fuelType ->
                            Button(
                                onClick = { gasSlipViewModel.setFuelTypeFilter(fuelType) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(32.dp),
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (fuelTypeFilter == fuelType) VibrantCyan else SurfaceLight
                                ),
                                contentPadding = PaddingValues(0.dp),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                            ) {
                                Text(
                                    fuelType,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (fuelTypeFilter == fuelType) DeepBlue else TextSecondary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
            
            // Vehicle Filter
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { expandedVehicle = !expandedVehicle },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (expandedVehicle) ElectricBlue else SurfaceLight
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (vehicleFilter != null) "Vehicle: $vehicleFilter" else "Vehicle Plate",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Icon(
                            imageVector = if (expandedVehicle) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                if (expandedVehicle) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (vehicleFilter != null) {
                            Button(
                                onClick = { gasSlipViewModel.setVehicleFilter(null) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(32.dp),
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ErrorRed.copy(alpha = 0.2f)
                                ),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    "Clear Filter",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ErrorRed,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.sp
                                )
                            }
                        }
                        
                        uniqueVehicles.forEach { vehicle ->
                            Button(
                                onClick = { gasSlipViewModel.setVehicleFilter(vehicle) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(32.dp),
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (vehicleFilter == vehicle) VibrantCyan else SurfaceLight
                                ),
                                contentPadding = PaddingValues(0.dp),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                            ) {
                                Text(
                                    vehicle,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (vehicleFilter == vehicle) DeepBlue else TextSecondary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
            
            // Driver Filter
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { expandedDriver = !expandedDriver },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (expandedDriver) ElectricBlue else SurfaceLight
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (driverFilter != null) "Driver: $driverFilter" else "Driver Name",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Icon(
                            imageVector = if (expandedDriver) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                if (expandedDriver) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (driverFilter != null) {
                            Button(
                                onClick = { gasSlipViewModel.setDriverFilter(null) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(32.dp),
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ErrorRed.copy(alpha = 0.2f)
                                ),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    "Clear Filter",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ErrorRed,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.sp
                                )
                            }
                        }
                        
                        uniqueDrivers.forEach { driver ->
                            Button(
                                onClick = { gasSlipViewModel.setDriverFilter(driver) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(32.dp),
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (driverFilter == driver) VibrantCyan else SurfaceLight
                                ),
                                contentPadding = PaddingValues(0.dp),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                            ) {
                                Text(
                                    driver,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (driverFilter == driver) DeepBlue else TextSecondary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
            
            // Clear All Filters Button
            Button(
                onClick = { gasSlipViewModel.clearAllFilters() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ErrorRed.copy(alpha = 0.15f)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ClearAll,
                    contentDescription = null,
                    tint = ErrorRed,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Clear All Filters",
                    style = MaterialTheme.typography.labelMedium,
                    color = ErrorRed,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
