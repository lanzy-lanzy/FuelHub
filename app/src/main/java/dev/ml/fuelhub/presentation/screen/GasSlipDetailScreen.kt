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
    onBack: () -> Unit
) {
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
                    Button(
                        onClick = { onMarkDispensed(gasSlip) },
                        modifier = Modifier
                            .fillMaxWidth()
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
                        Text("Mark as Dispensed", fontWeight = FontWeight.Bold)
                    }
                }

                // Print Button
                Button(
                    onClick = { onPrint(gasSlip) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VibrantCyan)
                ) {
                    Icon(
                        imageVector = Icons.Default.Print,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = DeepBlue
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Print Slip", fontWeight = FontWeight.Bold, color = DeepBlue)
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
