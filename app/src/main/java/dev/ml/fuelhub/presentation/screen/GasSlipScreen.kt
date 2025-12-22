package dev.ml.fuelhub.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.ml.fuelhub.data.model.GasSlip
import dev.ml.fuelhub.data.model.GasSlipStatus

/**
 * Composable screen for displaying gas slip details.
 * Shows all information needed for fuel dispensing at partner gas stations.
 */
@Composable
fun GasSlipScreen(
    gasSlip: GasSlip,
    onPrintClick: (gasSlip: GasSlip) -> Unit = {},
    onBackClick: () -> Unit = {},
    onCancelClick: (gasSlipId: String) -> Unit = {},
    onMarkDispensedClick: (gasSlipId: String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showCancelDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "MDRRMO GAS SLIP",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    gasSlip.mdrrmoOfficeName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )
                Text(
                    "Reference: ${gasSlip.referenceNumber}",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        // Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Fuel Information Card
            GasSlipSection(
                title = "FUEL INFORMATION",
                content = {
                    GasSlipRow("Fuel Type:", gasSlip.fuelType.name)
                    GasSlipRow("Liters to Dispense:", "${gasSlip.litersToPump} L")
                    GasSlipRow("Date:", gasSlip.transactionDate.toString())
                }
            )
            
            // Vehicle Information Card
            GasSlipSection(
                title = "VEHICLE INFORMATION",
                content = {
                    GasSlipRow("Vehicle Type:", gasSlip.vehicleType)
                    GasSlipRow("Plate Number:", gasSlip.vehiclePlateNumber)
                }
            )
            
            // Driver Information Card
            GasSlipSection(
                title = "DRIVER INFORMATION",
                content = {
                    GasSlipRow("Driver Name:", gasSlip.driverName)
                    if (gasSlip.passengers != null && gasSlip.passengers.isNotBlank()) {
                        GasSlipRow("Passengers:", gasSlip.passengers)
                    }
                }
            )
            
            // Trip Details Card
            GasSlipSection(
                title = "TRIP DETAILS",
                content = {
                    GasSlipRow("Destination:", gasSlip.destination)
                    GasSlipRow("Purpose:", gasSlip.tripPurpose)
                }
            )
            
            // Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (gasSlip.isUsed) {
                        Color(0xFFE8F5E9)
                    } else {
                        Color(0xFFFFF3E0)
                    }
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Text(
                        if (gasSlip.isUsed) "USED" else "PENDING",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (gasSlip.isUsed) Color(0xFF2E7D32) else Color(0xFFE65100)
                    )
                    if (gasSlip.isUsed && gasSlip.usedAt != null) {
                        Text(
                            "Used at: ${gasSlip.usedAt}",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Actions - Primary Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onBackClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("Back")
                }
                
                Button(
                    onClick = { onPrintClick(gasSlip) },
                    enabled = gasSlip.status != GasSlipStatus.CANCELLED,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("Print Slip")
                }
            }
            
            // Actions - Secondary Row
            if (gasSlip.status != GasSlipStatus.CANCELLED) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onMarkDispensedClick(gasSlip.id) },
                        enabled = gasSlip.status == GasSlipStatus.PENDING,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Text("Mark Dispensed")
                    }
                    
                    Button(
                        onClick = { showCancelDialog = true },
                        enabled = gasSlip.status == GasSlipStatus.PENDING,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFf44336)
                        )
                    ) {
                        Text("Cancel")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
    
    // Cancel Confirmation Dialog
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = null,
                        tint = Color(0xFFf44336),
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        "Cancel Slip?",
                        color = Color(0xFFf44336),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Are you sure you want to cancel this fuel slip?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Reference: ${gasSlip.referenceNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "This action cannot be undone. The slip will be marked as cancelled and cannot be dispensed.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCancelClick(gasSlip.id)
                        showCancelDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFf44336))
                ) {
                    Text("Cancel Slip", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Keep Slip")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
fun GasSlipSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Divider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outline
        )
        content()
    }
}

@Composable
fun GasSlipRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}
