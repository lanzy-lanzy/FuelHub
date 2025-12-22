package dev.ml.fuelhub.presentation.screen

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.ml.fuelhub.data.model.Vehicle
import dev.ml.fuelhub.data.model.FuelType
import dev.ml.fuelhub.presentation.viewmodel.VehicleManagementUiState
import dev.ml.fuelhub.presentation.viewmodel.VehicleManagementViewModel
import dev.ml.fuelhub.presentation.component.DrawerSwipeIndicator
import dev.ml.fuelhub.ui.theme.*
import timber.log.Timber

@Composable
fun VehicleManagementScreen(
    vehicleViewModel: VehicleManagementViewModel,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val uiState by vehicleViewModel.uiState.collectAsState()
    val selectedVehicle by vehicleViewModel.selectedVehicle.collectAsState()

    LaunchedEffect(Unit) {
        vehicleViewModel.loadDrivers()
    }

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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Manage Vehicles",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = VibrantCyan,
                        fontSize = 28.sp
                    )
                Button(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .height(56.dp)
                        .shadow(12.dp, RoundedCornerShape(28.dp)),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(28.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(ElectricBlue, VibrantCyan),
                                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                    end = androidx.compose.ui.geometry.Offset(1f, 1f)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Add Vehicle",
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                fontSize = 14.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }

            // Content
            when (uiState) {
                is VehicleManagementUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = VibrantCyan)
                    }
                }

                is VehicleManagementUiState.Success -> {
                    val vehicles = (uiState as VehicleManagementUiState.Success).vehicles
                    if (vehicles.isEmpty()) {
                        VehicleEmptyState(message = "No vehicles found. Add one to get started!")
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(vehicles) { vehicle ->
                                VehicleCard(
                                    vehicle = vehicle,
                                    isSelected = selectedVehicle?.id == vehicle.id,
                                    onSelect = { vehicleViewModel.selectVehicle(vehicle) },
                                    onEdit = {
                                        vehicleViewModel.selectVehicle(vehicle)
                                        showEditDialog = true
                                    },
                                    onDelete = {
                                        vehicleViewModel.selectVehicle(vehicle)
                                        showDeleteConfirm = true
                                    }
                                )
                            }
                        }
                    }
                }

                is VehicleManagementUiState.Error -> {
                    ErrorState(
                        message = (uiState as VehicleManagementUiState.Error).message,
                        onRetry = { vehicleViewModel.loadVehicles() }
                    )
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

    // Add Vehicle Dialog
    if (showAddDialog) {
        val drivers by vehicleViewModel.drivers.collectAsState()
        AddVehicleDialog(
            drivers = drivers,
            onDismiss = { showAddDialog = false },
            onAdd = { plateNumber, vehicleType, fuelType, driverName, driverId, assignedDriverIds, assignedDriverNames ->
                vehicleViewModel.addVehicle(plateNumber, vehicleType, fuelType, driverName, driverId, assignedDriverIds, assignedDriverNames)
                showAddDialog = false
            }
        )
    }

    // Edit Vehicle Dialog
    if (showEditDialog && selectedVehicle != null) {
        val drivers by vehicleViewModel.drivers.collectAsState()
        EditVehicleDialog(
            vehicle = selectedVehicle!!,
            drivers = drivers,
            onDismiss = {
                showEditDialog = false
                vehicleViewModel.clearSelection()
            },
            onUpdate = { updatedVehicle ->
                vehicleViewModel.updateVehicle(updatedVehicle)
                showEditDialog = false
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirm && selectedVehicle != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirm = false
                vehicleViewModel.clearSelection()
            },
            title = { Text("Delete Vehicle") },
            text = { Text("Are you sure you want to delete ${selectedVehicle!!.plateNumber}?") },
            confirmButton = {
                Button(
                    onClick = {
                        vehicleViewModel.deleteVehicle(selectedVehicle!!.id)
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                Button(onClick = {
                    showDeleteConfirm = false
                    vehicleViewModel.clearSelection()
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun VehicleCard(
    vehicle: Vehicle,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(isSelected) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clip(RoundedCornerShape(16.dp))
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = if (expanded) 12.dp else 0.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        vehicle.plateNumber,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        vehicle.vehicleType,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = VibrantCyan,
                    modifier = Modifier.size(24.dp)
                )
            }

            if (expanded) {
                Divider(color = SurfaceLight, modifier = Modifier.padding(vertical = 8.dp))
                
                // Display assigned drivers
                val assignedDriversDisplay = if (vehicle.assignedDriverNames.isNotEmpty()) {
                    vehicle.assignedDriverNames.joinToString(", ")
                } else {
                    vehicle.driverName  // Fallback to single driver for backward compatibility
                }
                DetailRow("Driver(s)", assignedDriversDisplay)
                
                DetailRow("Fuel Type", vehicle.fuelType.name)
                DetailRow("Type", vehicle.vehicleType)
                DetailRow("Status", if (vehicle.isActive) "Active" else "Inactive")

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onEdit,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricBlue
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit", fontSize = 12.sp)
                    }

                    Button(
                        onClick = onDelete,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ErrorRed
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun VehicleEmptyState(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.DirectionsCar,
                contentDescription = null,
                tint = VibrantCyan,
                modifier = Modifier.size(64.dp)
            )
            Text(
                message,
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun AddVehicleDialog(
    drivers: List<dev.ml.fuelhub.data.model.User>,
    onDismiss: () -> Unit,
    onAdd: (plateNumber: String, vehicleType: String, fuelType: FuelType, driverName: String, driverId: String?, assignedDriverIds: List<String>, assignedDriverNames: List<String>) -> Unit
) {
    var plateNumber by remember { mutableStateOf("") }
    var vehicleType by remember { mutableStateOf("") }
    var selectedDrivers by remember { mutableStateOf<List<dev.ml.fuelhub.data.model.User>>(emptyList()) }
    var driverDropdownExpanded by remember { mutableStateOf(false) }
    var fuelTypeExpanded by remember { mutableStateOf(false) }
    var selectedFuelType by remember { mutableStateOf(FuelType.GASOLINE) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Vehicle") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = plateNumber,
                    onValueChange = { plateNumber = it.uppercase() },
                    label = { Text("Plate Number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters)
                )

                // Editable Dropdown for Vehicle Type
                VehicleTypeInput(value = vehicleType, onValueChange = { vehicleType = it })

                // Multiple Driver selection
                Text("Assign Drivers (select one or more)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                
                // Display selected drivers as chips
                if (selectedDrivers.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(selectedDrivers) { driver ->
                            AssistChip(
                                onClick = { selectedDrivers = selectedDrivers.filter { it.id != driver.id } },
                                label = { Text("${driver.fullName} (${driver.username})") },
                                trailingIcon = { Icon(Icons.Default.Close, contentDescription = "Remove") }
                            )
                        }
                    }
                }
                
                // Driver selection dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { driverDropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (selectedDrivers.isEmpty()) "Select Drivers..." 
                            else "${selectedDrivers.size} driver(s) selected"
                        )
                    }
                    DropdownMenu(
                        expanded = driverDropdownExpanded,
                        onDismissRequest = { driverDropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        drivers.forEach { driver ->
                            val isSelected = selectedDrivers.any { it.id == driver.id }
                            DropdownMenuItem(
                                text = { 
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                                            Text(driver.fullName, fontWeight = FontWeight.Bold)
                                            Text(driver.username, fontSize = 12.sp, color = Color.Gray)
                                        }
                                        if (isSelected) {
                                            Icon(Icons.Default.Check, contentDescription = "Selected", tint = SuccessGreen)
                                        }
                                    }
                                },
                                onClick = {
                                    selectedDrivers = if (isSelected) {
                                        selectedDrivers.filter { it.id != driver.id }
                                    } else {
                                        selectedDrivers + driver
                                    }
                                }
                            )
                        }
                    }
                }
                
                // Fuel Type selection
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { fuelTypeExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(selectedFuelType.name)
                    }
                    DropdownMenu(
                        expanded = fuelTypeExpanded,
                        onDismissRequest = { fuelTypeExpanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FuelType.values().forEach { fuelType ->
                            DropdownMenuItem(
                                text = { Text(fuelType.name) },
                                onClick = {
                                    selectedFuelType = fuelType
                                    fuelTypeExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (plateNumber.isNotBlank() && vehicleType.isNotBlank() && selectedDrivers.isNotEmpty()) {
                        val primaryDriver = selectedDrivers.first()
                        onAdd(
                            plateNumber, 
                            vehicleType, 
                            selectedFuelType, 
                            primaryDriver.fullName, 
                            primaryDriver.id,
                            selectedDrivers.map { it.id },
                            selectedDrivers.map { it.fullName }
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
            ) {
                Text("Add", color = Color.White)
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditVehicleDialog(
    vehicle: Vehicle,
    drivers: List<dev.ml.fuelhub.data.model.User>,
    onDismiss: () -> Unit,
    onUpdate: (Vehicle) -> Unit
) {
    var vehicleType by remember { mutableStateOf(vehicle.vehicleType) }
    
    // Load existing assigned drivers
    var selectedDrivers by remember { 
        mutableStateOf(
            if (vehicle.assignedDriverIds.isNotEmpty()) {
                drivers.filter { vehicle.assignedDriverIds.contains(it.id) }
            } else if (vehicle.driverId != null) {
                // Backward compatibility: load from legacy driverId
                drivers.find { it.id == vehicle.driverId }?.let { listOf(it) } ?: emptyList()
            } else {
                emptyList()
            }
        )
    }
    var driverDropdownExpanded by remember { mutableStateOf(false) }
    var fuelTypeExpanded by remember { mutableStateOf(false) }
    var selectedFuelType by remember { mutableStateOf(vehicle.fuelType) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Vehicle") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Display current plate number as read-only
                OutlinedTextField(
                    value = vehicle.plateNumber,
                    onValueChange = {},
                    label = { Text("Plate Number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = false
                )

                // Editable Dropdown for Vehicle Type
                VehicleTypeInput(value = vehicleType, onValueChange = { vehicleType = it })

                // Multiple Driver selection
                Text("Assigned Drivers", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                
                // Display assigned drivers as chips
                if (selectedDrivers.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(selectedDrivers) { driver ->
                            AssistChip(
                                onClick = { selectedDrivers = selectedDrivers.filter { it.id != driver.id } },
                                label = { Text("${driver.fullName} (${driver.username})") },
                                trailingIcon = { Icon(Icons.Default.Close, contentDescription = "Remove") }
                            )
                        }
                    }
                }
                
                // Driver selection dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { driverDropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (selectedDrivers.isEmpty()) "Select Drivers..." 
                            else "${selectedDrivers.size} driver(s) assigned"
                        )
                    }
                    DropdownMenu(
                        expanded = driverDropdownExpanded,
                        onDismissRequest = { driverDropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        drivers.forEach { driver ->
                            val isSelected = selectedDrivers.any { it.id == driver.id }
                            DropdownMenuItem(
                                text = { 
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                                            Text(driver.fullName, fontWeight = FontWeight.Bold)
                                            Text(driver.username, fontSize = 12.sp, color = Color.Gray)
                                        }
                                        if (isSelected) {
                                            Icon(Icons.Default.Check, contentDescription = "Selected", tint = SuccessGreen)
                                        }
                                    }
                                },
                                onClick = {
                                    selectedDrivers = if (isSelected) {
                                        selectedDrivers.filter { it.id != driver.id }
                                    } else {
                                        selectedDrivers + driver
                                    }
                                }
                            )
                        }
                    }
                }
                
                // Fuel Type selection
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { fuelTypeExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(selectedFuelType.name)
                    }
                    DropdownMenu(
                        expanded = fuelTypeExpanded,
                        onDismissRequest = { fuelTypeExpanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FuelType.values().forEach { fuelType ->
                            DropdownMenuItem(
                                text = { Text(fuelType.name) },
                                onClick = {
                                    selectedFuelType = fuelType
                                    fuelTypeExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (vehicleType.isNotBlank() && selectedDrivers.isNotEmpty()) {
                        val primaryDriver = selectedDrivers.first()
                        onUpdate(
                            vehicle.copy(
                                vehicleType = vehicleType,
                                driverName = primaryDriver.fullName,
                                driverId = primaryDriver.id,
                                assignedDriverIds = selectedDrivers.map { it.id },
                                assignedDriverNames = selectedDrivers.map { it.fullName },
                                fuelType = selectedFuelType
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
            ) {
                Text("Update", color = Color.White)
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleTypeInput(
    value: String,
    onValueChange: (String) -> Unit
) {
    val options = listOf(
        "RESCUE VEHICLE",
        "GOVERNMENT VEHICLE",
        "MOTORCYCLE",
        "AMBULANCE",
        "FIRE TRUCK",
        "PATROL CAR",
        "SERVICE VEHICLE",
        "HEAVY EQUIPMENT",
        "DUMP TRUCK",
        "OTHERS"
    )
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                onValueChange(it.uppercase())
                expanded = true
            },
            label = { Text("Vehicle Type") },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            singleLine = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters)
        )

        val filteringOptions = options.filter { it.contains(value, ignoreCase = true) }

        if (filteringOptions.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                filteringOptions.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            onValueChange(selectionOption)
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
    }
}
