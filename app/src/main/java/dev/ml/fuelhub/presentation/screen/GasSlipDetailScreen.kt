package dev.ml.fuelhub.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.ml.fuelhub.data.model.GasSlip
import dev.ml.fuelhub.data.model.GasSlipStatus
import dev.ml.fuelhub.ui.theme.*
import java.time.format.DateTimeFormatter

@Composable
fun GasSlipDetailScreen(
    gasSlip: GasSlip,
    onPrint: (GasSlip) -> Unit,
    onMarkDispensed: (GasSlip) -> Unit = {},
    onCancel: (String) -> Unit = {},
    onBack: () -> Unit
) {
    var showCancelDialog by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlue)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header with back button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = VibrantCyan,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Text(
                    "Gas Slip Details",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = VibrantCyan
                )
                Spacer(modifier = Modifier.width(40.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Receipt-like card container
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .shadow(8.dp, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Office Header
                    Text(
                        gasSlip.mdrrmoOfficeName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = VibrantCyan,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "FUEL DISPENSING SLIP",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )

                    Divider(
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .fillMaxWidth(0.8f),
                        color = SurfaceLight
                    )

                    // Reference and Status in one row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Reference",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Text(
                                gasSlip.referenceNumber,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = VibrantCyan
                            )
                        }

                        // Status Badge (Prominent)
                        val statusColor = when (gasSlip.status) {
                            GasSlipStatus.PENDING -> AccentOrange
                            GasSlipStatus.DISPENSED -> SuccessGreen
                            GasSlipStatus.USED -> VibrantCyan
                            GasSlipStatus.CANCELLED -> ErrorRed
                        }
                        Box(
                            modifier = Modifier
                                .background(statusColor, RoundedCornerShape(8.dp))
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                gasSlip.getStatusBadge(),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }

                    Divider(
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .fillMaxWidth(0.8f),
                        color = SurfaceLight
                    )

                    // Vehicle Section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Text(
                            "VEHICLE INFORMATION",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = VibrantCyan,
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        DetailRowReceipt("Driver", gasSlip.driverName)
                        DetailRowReceipt("Plate Number", gasSlip.vehiclePlateNumber)
                        DetailRowReceipt("Vehicle Type", gasSlip.vehicleType)
                    }

                    Divider(
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .fillMaxWidth(0.8f),
                        color = SurfaceLight
                    )

                    // Fuel Section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Text(
                            "FUEL ALLOCATION",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = VibrantCyan,
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        DetailRowReceipt("Fuel Type", gasSlip.fuelType.name)
                        DetailRowReceipt("Liters Allocated", "${gasSlip.litersToPump} L")

                        if (gasSlip.status == GasSlipStatus.DISPENSED && gasSlip.dispensedLiters != null) {
                            DetailRowReceipt("Liters Dispensed", "${gasSlip.dispensedLiters} L")
                        }
                    }

                    Divider(
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .fillMaxWidth(0.8f),
                        color = SurfaceLight
                    )

                    // Trip Details Section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Text(
                            "TRIP DETAILS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = VibrantCyan,
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        DetailRowReceipt("Destination", gasSlip.destination)
                        DetailRowReceipt("Purpose", gasSlip.tripPurpose)

                        if (!gasSlip.passengers.isNullOrBlank()) {
                            DetailRowReceipt("Passengers", gasSlip.passengers)
                        }
                    }

                    Divider(
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .fillMaxWidth(0.8f),
                        color = SurfaceLight
                    )

                    // Dates Section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Text(
                            "DATES",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = VibrantCyan,
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        DetailRowReceipt(
                            "Generated",
                            gasSlip.generatedAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                        )

                        if (gasSlip.status == GasSlipStatus.DISPENSED && gasSlip.dispensedAt != null) {
                            DetailRowReceipt(
                                "Dispensed",
                                gasSlip.dispensedAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                            )
                        }
                    }

                    Divider(
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .fillMaxWidth(0.8f),
                        color = SurfaceLight
                    )

                    // Footer
                    Text(
                        "Thank you for using FuelHub",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        fontSize = 9.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Mark as Dispensed Button (if pending)
                if (gasSlip.status == GasSlipStatus.PENDING) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { onMarkDispensed(gasSlip) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Mark Dispensed", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        
                        Button(
                            onClick = { showCancelDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B6B))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cancel", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                // Print Button
                Button(
                    onClick = { onPrint(gasSlip) },
                    enabled = gasSlip.status != GasSlipStatus.CANCELLED,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VibrantCyan,
                        disabledContainerColor = VibrantCyan.copy(alpha = 0.5f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Print,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = if (gasSlip.status == GasSlipStatus.CANCELLED) DeepBlue.copy(alpha = 0.5f) else DeepBlue
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Print Slip",
                        fontWeight = FontWeight.Bold,
                        color = if (gasSlip.status == GasSlipStatus.CANCELLED) DeepBlue.copy(alpha = 0.5f) else DeepBlue
                    )
                }

                // Close Button
                Button(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
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
                        tint = Color(0xFFFF6B6B),
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        "Cancel Slip?",
                        color = Color(0xFFFF6B6B),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Are you sure you want to cancel this fuel slip?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                    Text(
                        "Reference: ${gasSlip.referenceNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "This action cannot be undone. The slip will be marked as cancelled and cannot be dispensed.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCancel(gasSlip.id)
                        showCancelDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B6B))
                ) {
                    Text("Cancel Slip", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Keep Slip", color = TextSecondary)
                }
            },
            containerColor = SurfaceDark,
            textContentColor = TextPrimary
        )
    }
}

@Composable
fun DetailRowReceipt(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}
