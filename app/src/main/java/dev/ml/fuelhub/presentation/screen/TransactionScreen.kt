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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.ml.fuelhub.data.model.FuelType
import dev.ml.fuelhub.data.model.User
import dev.ml.fuelhub.data.model.Vehicle
import dev.ml.fuelhub.ui.theme.*

/**
 * Premium Transaction Screen - Modern Design
 * Collects all required information for fuel slip generation with stunning UI
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionScreen(
    transactionViewModel: dev.ml.fuelhub.presentation.viewmodel.TransactionViewModel? = null,
    onTransactionCreated: (Map<String, String>) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedDriver by remember { mutableStateOf<dev.ml.fuelhub.data.model.User?>(null) }
    var driverDropdownExpanded by remember { mutableStateOf(false) }
    var selectedVehicle by remember { mutableStateOf<dev.ml.fuelhub.data.model.Vehicle?>(null) }
    var vehicleDropdownExpanded by remember { mutableStateOf(false) }
    var destination by remember { mutableStateOf("") }
    var tripPurpose by remember { mutableStateOf("") }
    var passengers by remember { mutableStateOf("") }
    var litersToPump by remember { mutableStateOf("") }
    var fuelTypeExpanded by remember { mutableStateOf(false) }
    var selectedFuelType by remember { mutableStateOf(FuelType.GASOLINE) }
    
    val drivers = transactionViewModel?.drivers?.collectAsState()?.value ?: emptyList()
    val vehicles = transactionViewModel?.vehicles?.collectAsState()?.value ?: emptyList()

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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            TransactionHeader()

            // Fuel Type Selector (Prominent)
            FuelTypeSelector(
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
                        "Transaction Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = VibrantCyan
                    )

                    // Driver Dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { driverDropdownExpanded = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = VibrantCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                selectedDriver?.username ?: "Select Driver",
                                color = if (selectedDriver != null) TextPrimary else TextSecondary
                            )
                        }
                        DropdownMenu(
                            expanded = driverDropdownExpanded,
                            onDismissRequest = { driverDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            drivers.forEach { driver ->
                                DropdownMenuItem(
                                    text = { Text(driver.username) },
                                    onClick = {
                                        selectedDriver = driver
                                        driverDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Vehicle Dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { vehicleDropdownExpanded = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = VibrantCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                selectedVehicle?.plateNumber ?: "Select Vehicle",
                                color = if (selectedVehicle != null) TextPrimary else TextSecondary
                            )
                        }
                        DropdownMenu(
                            expanded = vehicleDropdownExpanded,
                            onDismissRequest = { vehicleDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            vehicles.forEach { vehicle ->
                                DropdownMenuItem(
                                    text = { Text("${vehicle.plateNumber} - ${vehicle.vehicleType}") },
                                    onClick = {
                                        selectedVehicle = vehicle
                                        vehicleDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    PremiumTextField(
                        value = litersToPump,
                        onValueChange = { litersToPump = it },
                        label = "Liters to Pump",
                        icon = Icons.Default.Star,
                        keyboardType = KeyboardType.Decimal
                    )

                    PremiumTextField(
                        value = destination,
                        onValueChange = { destination = it },
                        label = "Destination",
                        icon = Icons.Default.Place
                    )

                    PremiumTextField(
                        value = tripPurpose,
                        onValueChange = { tripPurpose = it },
                        label = "Purpose of Trip",
                        icon = Icons.Default.Description
                    )

                    PremiumTextField(
                        value = passengers,
                        onValueChange = { passengers = it },
                        label = "Passengers (optional)",
                        icon = Icons.Default.Group
                    )
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        selectedDriver = null
                        selectedVehicle = null
                        destination = ""
                        tripPurpose = ""
                        passengers = ""
                        litersToPump = ""
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.linearGradient(
                            colors = listOf(TextSecondary, TextTertiary)
                        )
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TextPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clear", fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = {
                        if (selectedDriver != null && selectedVehicle != null) {
                            val transactionData = mapOf(
                                "driverId" to selectedDriver!!.id,
                                "driverName" to selectedDriver!!.username,
                                "vehicleId" to selectedVehicle!!.id,
                                "vehicleType" to selectedVehicle!!.vehicleType,
                                "plateNumber" to selectedVehicle!!.plateNumber,
                                "destination" to destination,
                                "tripPurpose" to tripPurpose,
                                "passengers" to passengers,
                                "litersToPump" to litersToPump,
                                "fuelType" to selectedFuelType.name
                            )
                            onTransactionCreated(transactionData)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .shadow(8.dp, RoundedCornerShape(16.dp)),
                    enabled = selectedDriver != null && selectedVehicle != null &&
                            destination.isNotBlank() && tripPurpose.isNotBlank() && litersToPump.isNotBlank(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        disabledContainerColor = SurfaceLight
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                if (selectedDriver != null && selectedVehicle != null &&
                                    destination.isNotBlank() && tripPurpose.isNotBlank() && litersToPump.isNotBlank()
                                ) {
                                    Brush.linearGradient(
                                        colors = listOf(ElectricBlue, VibrantCyan, NeonTeal)
                                    )
                                } else {
                                    Brush.linearGradient(
                                        colors = listOf(SurfaceLight, SurfaceLight)
                                    )
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Create Transaction",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun TransactionHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            "New Transaction",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary,
            fontSize = 32.sp
        )
        Text(
            "Fill in the details below",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}

@Composable
fun FuelTypeSelector(
    selectedFuelType: FuelType,
    onFuelTypeSelected: (FuelType) -> Unit,
    animatedOffset: Float
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Select Fuel Type",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FuelType.values().forEach { fuelType ->
                FuelTypeCard(
                    fuelType = fuelType,
                    isSelected = selectedFuelType == fuelType,
                    onClick = { onFuelTypeSelected(fuelType) },
                    modifier = Modifier.weight(1f),
                    animatedOffset = animatedOffset
                )
            }
        }
    }
}

@Composable
fun FuelTypeCard(
    fuelType: FuelType,
    isSelected: Boolean,
    onClick: () -> Unit,
    animatedOffset: Float,
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
                        Modifier.background(
                            Brush.linearGradient(
                                colors = gradient.map { it.copy(alpha = 0.1f) }
                            )
                        )
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
                    imageVector = Icons.Default.Star,
                    contentDescription = fuelType.name,
                    tint = if (isSelected) Color.White else gradient[0],
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    fuelType.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.White else TextPrimary
                )
            }
        }
    }
}

@Composable
fun PremiumTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                label,
                color = TextSecondary
            )
        },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = VibrantCyan
            )
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = VibrantCyan,
            unfocusedBorderColor = SurfaceLight,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            cursorColor = VibrantCyan,
            focusedContainerColor = SurfaceLight.copy(alpha = 0.3f),
            unfocusedContainerColor = SurfaceLight.copy(alpha = 0.1f)
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true
    )
}
