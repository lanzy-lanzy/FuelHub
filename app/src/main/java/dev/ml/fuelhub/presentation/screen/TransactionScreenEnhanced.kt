package dev.ml.fuelhub.presentation.screen

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.ml.fuelhub.data.model.FuelType
import dev.ml.fuelhub.data.model.Vehicle
import dev.ml.fuelhub.presentation.state.TransactionUiState
import dev.ml.fuelhub.presentation.viewmodel.TransactionViewModel
import dev.ml.fuelhub.presentation.component.DrawerSwipeIndicator
import dev.ml.fuelhub.ui.theme.*
import timber.log.Timber

/**
 * Enhanced Transaction Screen with Real Data Functionality
 * - Fully integrated with TransactionViewModel
 * - Real database operations
 * - Complete form validation
 * - Success/Error state handling
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionScreenEnhanced(
    transactionViewModel: TransactionViewModel,
    availableVehicles: List<Vehicle> = emptyList(),
    onTransactionCreated: () -> Unit = {},
    pdfPrintManager: dev.ml.fuelhub.data.util.PdfPrintManager? = null,
    gasSlipViewModel: dev.ml.fuelhub.presentation.viewmodel.GasSlipManagementViewModel? = null,
    modifier: Modifier = Modifier
) {
    // Form states
    var selectedVehicle by remember { mutableStateOf<Vehicle?>(null) }
    var selectedDriver by remember { mutableStateOf<String?>(null) }
    var driverName by remember { mutableStateOf("") }
    var vehicleId by remember { mutableStateOf("") }
    var vehiclesDropdownExpanded by remember { mutableStateOf(false) }
    var driversDropdownExpanded by remember { mutableStateOf(false) }
    var destination by remember { mutableStateOf("") }
    var tripPurpose by remember { mutableStateOf("") }
    var passengers by remember { mutableStateOf("") }
    var litersToPump by remember { mutableStateOf("") }
    var costPerLiter by remember { mutableStateOf("") }
    var selectedFuelType by remember { mutableStateOf(FuelType.GASOLINE) }
    var fuelTypeExpanded by remember { mutableStateOf(false) }
    
    // Error states
    var showValidationError by remember { mutableStateOf(false) }
    var validationErrorMessage by remember { mutableStateOf("") }
    
    // UI State from ViewModel
    val uiState by transactionViewModel.uiState.collectAsState()

    // Animated gradient
    val infiniteTransition = rememberInfiniteTransition(label = "txnGradient")
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "txnOffset"
    )

    // Get available drivers for selected vehicle
    fun getAvailableDrivers(): List<String> {
        if (selectedVehicle == null) return emptyList()
        // Use assigned drivers if available, otherwise use primary driver
        return if (selectedVehicle!!.assignedDriverNames.isNotEmpty()) {
            selectedVehicle!!.assignedDriverNames
        } else {
            listOf(selectedVehicle!!.driverName)
        }
    }

    // Validation function
    fun validateForm(): Boolean {
        return when {
            selectedVehicle == null -> {
                validationErrorMessage = "Please select a vehicle"
                false
            }
            selectedDriver == null || selectedDriver!!.isBlank() -> {
                validationErrorMessage = "Please select a driver"
                false
            }
            litersToPump.isBlank() -> {
                validationErrorMessage = "Liters to pump is required"
                false
            }
            litersToPump.toDoubleOrNull() == null || litersToPump.toDouble() <= 0 -> {
                validationErrorMessage = "Liters must be a positive number"
                false
            }
            litersToPump.toDouble() > 500 -> {
                validationErrorMessage = "Cannot pump more than 500 liters per transaction"
                false
            }
            costPerLiter.isBlank() -> {
                validationErrorMessage = "Cost per liter is required"
                false
            }
            costPerLiter.toDoubleOrNull() == null || costPerLiter.toDouble() <= 0 -> {
                validationErrorMessage = "Cost per liter must be a positive number"
                false
            }
            destination.isBlank() -> {
                validationErrorMessage = "Destination is required"
                false
            }
            tripPurpose.isBlank() -> {
                validationErrorMessage = "Trip purpose is required"
                false
            }
            else -> {
                validationErrorMessage = ""
                true
            }
        }
    }

    // Submit handler
    fun submitTransaction() {
        if (!validateForm()) {
            showValidationError = true
            Timber.w("Form validation failed: $validationErrorMessage")
            return
        }
        
        showValidationError = false
        
        // Create transaction using selected vehicle and driver
        selectedVehicle?.let { vehicle ->
            transactionViewModel.createTransaction(
                vehicleId = vehicle.id,
                litersToPump = litersToPump.toDouble(),
                costPerLiter = costPerLiter.toDouble(),
                destination = destination,
                tripPurpose = tripPurpose,
                passengers = if (passengers.isBlank()) null else passengers,
                createdBy = selectedDriver ?: vehicle.driverName, // Use selected driver or fall back to primary
                walletId = "default-wallet-id"
            )
            
            val totalCost = litersToPump.toDouble() * costPerLiter.toDouble()
            Timber.d("Transaction submission initiated: Vehicle=${vehicle.plateNumber}, Driver=$selectedDriver, Liters=$litersToPump, CostPerLiter=$costPerLiter, Total=₱$totalCost")
        }
    }

    // Reset form
    fun resetForm() {
        selectedVehicle = null
        selectedDriver = null
        driverName = ""
        vehicleId = ""
        destination = ""
        tripPurpose = ""
        passengers = ""
        litersToPump = ""
        costPerLiter = ""
        selectedFuelType = FuelType.GASOLINE
        showValidationError = false
        transactionViewModel.resetState()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlue)
    ) {
        when (uiState) {
            is TransactionUiState.Loading -> {
                LoadingState()
            }
            
            is TransactionUiState.Success -> {
                val success = uiState as TransactionUiState.Success
                SuccessState(
                    referenceNumber = success.transaction.referenceNumber,
                    newBalance = success.newWalletBalance,
                    fuelAmount = success.transaction.litersToPump,
                    gasSlip = success.gasSlip,
                    onDismiss = {
                        // Refresh gas slips before navigating away
                        gasSlipViewModel?.loadAllGasSlips()
                        resetForm()
                        onTransactionCreated()
                    },
                    onPrint = { gasSlip ->
                        pdfPrintManager?.generateAndPrintGasSlip(gasSlip)
                        gasSlipViewModel?.loadAllGasSlips()
                        Timber.d("Gas slip printed and list refreshed: ${gasSlip.referenceNumber}")
                    }
                )
            }
            
            is TransactionUiState.Error -> {
                TransactionErrorState(
                    message = (uiState as TransactionUiState.Error).message,
                    onRetry = { transactionViewModel.resetState() }
                )
            }
            
            else -> {
                // Idle state - show form
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Header
                        TransactionHeaderEnhanced()

                    // Validation Error Alert
                    if (showValidationError && validationErrorMessage.isNotEmpty()) {
                        ValidationErrorCard(validationErrorMessage)
                    }

                    // Fuel Type Selector
                    FuelTypeSelectorEnhanced(
                        selectedFuelType = selectedFuelType,
                        onFuelTypeSelected = { selectedFuelType = it },
                        animatedOffset = animatedOffset
                    )

                    // Main Form Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(12.dp, RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                "Transaction Details",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = VibrantCyan
                            )

                            // Vehicle Selector Dropdown
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    "Select Vehicle *",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedButton(
                                        onClick = { vehiclesDropdownExpanded = true },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        if (selectedVehicle != null) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(8.dp)
                                            ) {
                                                Text(
                                                    "${selectedVehicle!!.plateNumber} - ${selectedVehicle!!.vehicleType}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = TextPrimary
                                                )
                                                Text(
                                                    selectedVehicle!!.fuelType.name,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = TextSecondary,
                                                    fontSize = 12.sp
                                                )
                                            }
                                        } else {
                                            Text(
                                                "Choose a vehicle",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(8.dp),
                                                color = TextSecondary
                                            )
                                        }
                                    }
                                    DropdownMenu(
                                        expanded = vehiclesDropdownExpanded,
                                        onDismissRequest = { vehiclesDropdownExpanded = false },
                                        modifier = Modifier
                                            .fillMaxWidth(0.95f)
                                            .padding(horizontal = 4.dp),
                                        containerColor = SurfaceDark
                                    ) {
                                        if (availableVehicles.isEmpty()) {
                                            DropdownMenuItem(
                                                text = { Text("No vehicles available", color = TextSecondary) },
                                                onClick = {}
                                            )
                                        } else {
                                            availableVehicles.forEach { vehicle ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Column(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(vertical = 4.dp)
                                                        ) {
                                                            Text(
                                                                "${vehicle.plateNumber} - ${vehicle.vehicleType}",
                                                                fontWeight = FontWeight.Bold,
                                                                color = VibrantCyan,
                                                                style = MaterialTheme.typography.bodyMedium,
                                                                fontSize = 14.sp
                                                            )
                                                            Text(
                                                                "${vehicle.fuelType.name} • Driver: ${vehicle.driverName}",
                                                                fontSize = 13.sp,
                                                                color = TextPrimary,
                                                                style = MaterialTheme.typography.bodySmall
                                                            )
                                                        }
                                                    },
                                                    onClick = {
                                                        selectedVehicle = vehicle
                                                        selectedFuelType = vehicle.fuelType // Auto-select fuel type
                                                        // Auto-select first available driver
                                                        val drivers = if (vehicle.assignedDriverNames.isNotEmpty()) {
                                                            vehicle.assignedDriverNames
                                                        } else {
                                                            listOf(vehicle.driverName)
                                                        }
                                                        selectedDriver = drivers.firstOrNull()
                                                        vehiclesDropdownExpanded = false
                                                    },
                                                    modifier = Modifier.padding(horizontal = 8.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Driver Selector Dropdown (only show if vehicle is selected)
                            if (selectedVehicle != null) {
                                val availableDrivers = getAvailableDrivers()
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        "Select Driver *",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        OutlinedButton(
                                            onClick = { driversDropdownExpanded = true },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text(
                                                selectedDriver ?: "Choose a driver",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(8.dp),
                                                textAlign = TextAlign.Start,
                                                color = if (selectedDriver != null) TextPrimary else TextSecondary
                                            )
                                        }
                                        DropdownMenu(
                                            expanded = driversDropdownExpanded,
                                            onDismissRequest = { driversDropdownExpanded = false },
                                            modifier = Modifier
                                                .fillMaxWidth(0.95f)
                                                .padding(horizontal = 4.dp),
                                            containerColor = SurfaceDark
                                        ) {
                                            if (availableDrivers.isEmpty()) {
                                                DropdownMenuItem(
                                                    text = { Text("No drivers available", color = TextSecondary) },
                                                    onClick = {}
                                                )
                                            } else {
                                                availableDrivers.forEach { driver ->
                                                    DropdownMenuItem(
                                                        text = { 
                                                            Text(
                                                                driver, 
                                                                fontWeight = FontWeight.Bold,
                                                                color = VibrantCyan,
                                                                fontSize = 14.sp
                                                            ) 
                                                        },
                                                        onClick = {
                                                            selectedDriver = driver
                                                            driversDropdownExpanded = false
                                                        },
                                                        modifier = Modifier
                                                            .padding(horizontal = 8.dp)
                                                            .padding(vertical = 6.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateContentSize(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                PremiumTextFieldEnhanced(
                                    value = litersToPump,
                                    onValueChange = { litersToPump = it },
                                    label = "Liters to Pump *",
                                    keyboardType = KeyboardType.Decimal,
                                    placeholder = "0.00",
                                    modifier = Modifier.weight(1f)
                                )

                                CostPerLiterDropdown(
                                    selectedValue = costPerLiter,
                                    onValueSelected = { costPerLiter = it },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Show total cost calculation
                            if (litersToPump.isNotBlank() && costPerLiter.isNotBlank()) {
                                val liters = litersToPump.toDoubleOrNull() ?: 0.0
                                val costPer = costPerLiter.toDoubleOrNull() ?: 0.0
                                val totalCost = liters * costPer
                                
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(SurfaceDark.copy(alpha = 0.8f), RoundedCornerShape(8.dp)),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "Total Cost",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextSecondary
                                        )
                                        Text(
                                            "₱${String.format("%.2f", totalCost)}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFFFFB74D)
                                        )
                                    }
                                }
                            }

                            // Destination with Dropdown
                            TransactionDropdownInput(
                                value = destination,
                                onValueChange = { destination = it },
                                label = "Destination *",
                                options = listOf(
                                    "OZAMIS CITY",
                                    "PAGADIAN CITY",
                                    "MOLAVE",
                                    "MAHAYAG MEDICARE",
                                    "VARIOUS BRGY.",
                                    "OTHERS"
                                ),
                                placeholder = "Select or type destination"
                            )

                            // Trip Purpose with Dropdown
                            TransactionDropdownInput(
                                value = tripPurpose,
                                onValueChange = { tripPurpose = it },
                                label = "Purpose of Trip *",
                                options = listOf(
                                    "RESCUE",
                                    "LGU OFFICIAL TRIP",
                                    "TRANSPORT PATIENT",
                                    "PICKUP PATIENT",
                                    "OTHERS"
                                ),
                                placeholder = "Select or type purpose"
                            )

                            PremiumTextFieldEnhanced(
                                value = passengers,
                                onValueChange = { passengers = it.uppercase() },
                                label = "Passengers (Optional)",
                                placeholder = "Number of passengers"
                            )
                        }
                    }

                    // Submit Button
                    TransactionSubmitButton(
                        enabled = selectedVehicle != null && selectedDriver != null && litersToPump.isNotBlank(),
                        onClick = { submitTransaction() }
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    }
                    
                    // Floating Drawer Swipe Indicator
                    DrawerSwipeIndicator(
                        tintColor = VibrantCyan.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionHeaderEnhanced() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Text(
            "New Fuel Transaction",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = VibrantCyan,
            fontSize = 28.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Create and manage fuel transactions",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}

@Composable
fun FuelTypeSelectorEnhanced(
    selectedFuelType: FuelType,
    onFuelTypeSelected: (FuelType) -> Unit,
    animatedOffset: Float
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Fuel Type",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FuelTypeCard(
                fuelType = FuelType.GASOLINE,
                isSelected = selectedFuelType == FuelType.GASOLINE,
                onClick = { onFuelTypeSelected(FuelType.GASOLINE) },
                modifier = Modifier.weight(1f)
            )
            FuelTypeCard(
                fuelType = FuelType.DIESEL,
                isSelected = selectedFuelType == FuelType.DIESEL,
                onClick = { onFuelTypeSelected(FuelType.DIESEL) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun FuelTypeCard(
    fuelType: FuelType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gradient = when (fuelType) {
        FuelType.GASOLINE -> listOf(AccentOrange, AccentAmber)
        FuelType.DIESEL -> listOf(ElectricBlue, VibrantCyan)
    }

    Card(
        modifier = modifier
            .height(100.dp)
            .shadow(if (isSelected) 12.dp else 4.dp, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) SurfaceDark else SurfaceDark.copy(alpha = 0.5f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isSelected) {
                        Brush.linearGradient(gradient.map { it.copy(alpha = 0.3f) })
                    } else {
                        Brush.linearGradient(gradient.map { it.copy(alpha = 0.1f) })
                    }
                )
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalGasStation,
                    contentDescription = fuelType.name,
                    tint = gradient[0],
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    fuelType.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                if (isSelected) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Selected",
                        tint = gradient[0],
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PremiumTextFieldEnhanced(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    placeholder: String = "",
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp) // Increased height for bigger text
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceDark),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 20.sp, // Bigger text
                fontWeight = FontWeight.Bold
            ),
            placeholder = {
                Text(
                    placeholder,
                    color = TextTertiary,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Characters // Auto CAPS
            ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = SurfaceDark,
                unfocusedContainerColor = SurfaceDark,
                focusedIndicatorColor = ElectricBlue,
                unfocusedIndicatorColor = TextTertiary.copy(alpha = 0.3f),
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDropdownInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    options: List<String>,
    placeholder: String = ""
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = value,
                onValueChange = {
                    onValueChange(it.uppercase())
                    expanded = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceDark)
                    .menuAnchor(),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                ),
                placeholder = {
                    Text(
                        placeholder,
                        color = TextTertiary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Characters
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = SurfaceDark,
                    unfocusedContainerColor = SurfaceDark,
                    focusedIndicatorColor = ElectricBlue,
                    unfocusedIndicatorColor = TextTertiary.copy(alpha = 0.3f),
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                singleLine = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                shape = RoundedCornerShape(12.dp)
            )

            val filteringOptions = options.filter { it.contains(value, ignoreCase = true) }
            if (filteringOptions.isNotEmpty()) {
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(SurfaceDark)
                ) {
                    filteringOptions.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    selectionOption,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                ) 
                            },
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
}

@Composable
fun ValidationErrorCard(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = WarningYellow.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = "Error",
                tint = WarningYellow,
                modifier = Modifier.size(24.dp)
            )
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = WarningYellow,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun TransactionSubmitButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(8.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = ElectricBlue,
            disabledContainerColor = ElectricBlue.copy(alpha = 0.5f)
        )
    ) {
        Text(
            "Create Transaction",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontSize = 16.sp
        )
    }
}

@Composable
fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                color = VibrantCyan,
                strokeWidth = 4.dp,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Processing Transaction...",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun SuccessState(
    referenceNumber: String,
    newBalance: Double,
    fuelAmount: Double,
    gasSlip: dev.ml.fuelhub.data.model.GasSlip? = null,
    onDismiss: () -> Unit,
    onPrint: ((dev.ml.fuelhub.data.model.GasSlip) -> Unit)? = null
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .shadow(12.dp, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(SuccessGreen.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = SuccessGreen,
                        modifier = Modifier.size(48.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    "Transaction Successful!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    "Ref: $referenceNumber",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Details
                SuccessDetailRow("Fuel Pumped", "$fuelAmount L")
                Spacer(modifier = Modifier.height(8.dp))
                SuccessDetailRow("New Balance", "$newBalance L")
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Print Button
                if (gasSlip != null && onPrint != null) {
                    Button(
                        onClick = { onPrint.invoke(gasSlip) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VibrantCyan)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Print,
                            contentDescription = "Print",
                            tint = DeepBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Print Gas Slip", fontWeight = FontWeight.Bold, color = DeepBlue)
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) {
                    Text("Create New Transaction", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SuccessDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextSecondary)
        Text(value, fontWeight = FontWeight.Bold, color = TextPrimary)
    }
}

@Composable
fun TransactionErrorState(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .shadow(12.dp, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = "Error",
                    tint = WarningYellow,
                    modifier = Modifier.size(64.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    "Transaction Failed",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onRetry,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                ) {
                    Text("Try Again", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CostPerLiterDropdown(
    selectedValue: String,
    onValueSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Generate price options from 60.00 to 65.50 in 0.50 increments
    val priceOptions = mutableListOf<String>()
    var price = 60.00
    while (price <= 65.50) {
        priceOptions.add(String.format("%.2f", price))
        price += 0.50
    }
    
    var expanded by remember { mutableStateOf(false) }
    
    Column(modifier = modifier) {
        Text(
            "Cost per Liter *",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Box(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { expanded = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .border(1.dp, if (selectedValue.isNotEmpty()) ElectricBlue else TextTertiary.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark)
            ) {
                Text(
                    selectedValue.ifEmpty { "Select price..." },
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp),
                    fontWeight = FontWeight.Bold,
                    color = if (selectedValue.isNotEmpty()) TextPrimary else TextSecondary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
            
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .padding(horizontal = 4.dp),
                containerColor = SurfaceDark
            ) {
                priceOptions.forEach { price ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "₱$price per Liter",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                if (selectedValue == price) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = VibrantCyan,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        },
                        onClick = {
                            onValueSelected(price)
                            expanded = false
                        },
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }
    }
}
