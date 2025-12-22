package dev.ml.fuelhub.presentation.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.ml.fuelhub.data.model.FuelTransaction
import dev.ml.fuelhub.data.util.QRCodeScanner
import dev.ml.fuelhub.data.util.ScannedTransaction
import dev.ml.fuelhub.presentation.viewmodel.TransactionViewModel
import dev.ml.fuelhub.ui.theme.*
import timber.log.Timber
import kotlinx.coroutines.delay

/**
 * Gas Station Operator Screen
 * Allows scanning QR codes from transactions and confirming fuel dispensing
 */
@Composable
fun GasStationScreen(
    transactionViewModel: TransactionViewModel? = null,
    onNavigateBack: () -> Unit = {}
) {
    val vm = transactionViewModel ?: viewModel<TransactionViewModel>()
    val transactions by vm.transactionHistory.collectAsState()
    
    var scannedQRCode by remember { mutableStateOf("") }
    var scannedTransaction by remember { mutableStateOf<ScannedTransaction?>(null) }
    var matchedTransaction by remember { mutableStateOf<FuelTransaction?>(null) }
    var showScanner by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    var showCancelledDialog by remember { mutableStateOf(false) }
    var cancelledSlipMessage by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var showLogoutDialog by remember { mutableStateOf(false) }
    
    // Load transaction history when screen is first displayed
    LaunchedEffect(Unit) {
        Timber.d("=== GAS STATION SCREEN LOADED ===")
        // Use direct Firestore sync for gas station (never cached)
        vm.loadTransactionsFromFirestoreDirect()
        Timber.d("Direct Firestore sync initiated from gas station screen")
    }
    
    // Log transactions whenever they change
    LaunchedEffect(transactions) {
        Timber.d("=== TRANSACTIONS UPDATED ===")
        Timber.d("Total transactions available: ${transactions.size}")
        if (transactions.isNotEmpty()) {
            transactions.forEach { tx ->
                Timber.d("Transaction: ref=${tx.referenceNumber}, status=${tx.status.name}, vehicle=${tx.vehicleId}")
            }
        } else {
            Timber.w("NO TRANSACTIONS LOADED FROM SERVER!")
        }
    }
    
    LaunchedEffect(scannedQRCode) {
        if (scannedQRCode.isNotEmpty()) {
            Timber.d("🔍 === QR SCAN RECEIVED ===")
            Timber.d("📱 Raw scanned data: '$scannedQRCode'")
            val parsed = QRCodeScanner.parseQRCode(scannedQRCode)
            
            if (parsed != null) {
                Timber.d("✓ QR Code parsed successfully")
                if (QRCodeScanner.isValidTransaction(parsed)) {
                    Timber.d("✓ QR Code validation passed")
                    scannedTransaction = parsed
                    val refNumber = parsed.referenceNumber.trim()
                    Timber.d("QR Code parsed: '$refNumber'")
                    Timber.d("Available transactions count: ${transactions.size}")
                    Timber.d("Available transaction refs: ${transactions.map { "'${it.referenceNumber.trim()}'" }}")
                    
                    // Find matching transaction by reference number (case-insensitive, trimmed)
                    var foundTransaction: FuelTransaction? = transactions.find { 
                        it.referenceNumber.trim().equals(refNumber, ignoreCase = false)
                    }
                    
                    if (foundTransaction == null) {
                        Timber.w("Transaction not found. Looking for: '$refNumber'")
                        errorMessage = "Transaction not found: $refNumber. Syncing from server..."
                        // Force direct Firestore sync (not cached)
                        vm.loadTransactionsFromFirestoreDirect()
                        // Wait 2 seconds for server sync to complete
                        delay(2000)
                        foundTransaction = transactions.find { 
                            it.referenceNumber.trim().equals(refNumber, ignoreCase = false)
                        }
                        if (foundTransaction != null) {
                            Timber.d("Transaction found after refresh!")
                            errorMessage = ""
                            matchedTransaction = foundTransaction
                            showConfirmDialog = true
                        } else {
                            Timber.w("Transaction still not found after refresh")
                            errorMessage = "Transaction not found: $refNumber"
                        }
                    } else {
                        Timber.d("Transaction found: ${foundTransaction.referenceNumber}")
                        // Check if gas slip is cancelled via Firebase
                        vm.checkGasSlipStatus(foundTransaction.id) { gasSlipStatus ->
                            if (gasSlipStatus == "CANCELLED") {
                                Timber.w("Gas slip is cancelled: ${foundTransaction.referenceNumber}")
                                cancelledSlipMessage = "This slip (${foundTransaction.referenceNumber}) has been cancelled and cannot be dispensed."
                                showCancelledDialog = true
                            } else {
                                matchedTransaction = foundTransaction
                                showConfirmDialog = true
                            }
                        }
                    }
                } else {
                    errorMessage = "Invalid QR code data - validation failed"
                    Timber.e("QR Code validation failed")
                }
            } else {
                errorMessage = "Invalid QR code data - parsing failed"
                Timber.e("Failed to parse QR code")
            }
            scannedQRCode = ""
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlue)
            .systemBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        GasStationHeader(
            onNavigateBack = onNavigateBack,
            onLogout = { showLogoutDialog = true }
        )
        
        // Instructions
        InstructionCard()
        
        // Scan Button
        ScanQRButton(onClick = { showScanner = true })
        
        // Active Transactions List
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Pending Transactions",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        
        if (transactions.isEmpty()) {
            EmptyStateCard("No pending transactions")
        } else {
            val pendingTransactions = transactions.filter { 
                it.status.name in listOf("PENDING", "APPROVED") 
            }
            
            if (pendingTransactions.isEmpty()) {
                EmptyStateCard("All transactions have been dispensed")
            } else {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    pendingTransactions.take(10).forEach { transaction ->
                        TransactionCard(
                            transaction = transaction,
                            onConfirm = {
                                matchedTransaction = transaction
                                showConfirmDialog = true
                            }
                        )
                    }
                }
            }
        }
        
        // Error Message
        if (errorMessage.isNotEmpty()) {
            ErrorBanner(
                message = errorMessage,
                onDismiss = { errorMessage = "" }
            )
        }
    }
    
    // QR Code Scanner - Real Camera
    if (showScanner) {
        QRScannerCameraScreen(
            onCodeScanned = { code ->
                scannedQRCode = code
                showScanner = false
            },
            onDismiss = { showScanner = false }
        )
    }
    
    // Confirmation Dialog
    if (showConfirmDialog && matchedTransaction != null) {
        ConfirmDispensedDialog(
            transaction = matchedTransaction!!,
            scannedData = scannedTransaction,
            onConfirm = {
                vm.confirmFuelDispensed(matchedTransaction!!.id, matchedTransaction!!)
                showConfirmDialog = false
                showSuccess = true
                matchedTransaction = null
                scannedTransaction = null
            },
            onDismiss = {
                showConfirmDialog = false
                matchedTransaction = null
                scannedTransaction = null
            }
        )
    }
    
    // Success Dialog
    if (showSuccess) {
        SuccessDialog(
            onDismiss = { showSuccess = false }
        )
    }
    
    // Cancelled Slip Dialog
    if (showCancelledDialog) {
        CancelledSlipDialog(
            message = cancelledSlipMessage,
            onDismiss = { showCancelledDialog = false }
        )
    }
    
    // Logout Dialog
    if (showLogoutDialog) {
        LogoutConfirmationDialog(
            onConfirm = {
                showLogoutDialog = false
                onNavigateBack()
            },
            onDismiss = { showLogoutDialog = false }
        )
    }
}

@Composable
fun GasStationHeader(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                "Gas Station",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Text(
                "Fuel Verification",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = VibrantCyan,
                fontSize = 28.sp
            )
        }
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logout Button
            IconButton(
                onClick = onLogout,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1F4D6D).copy(alpha = 0.6f))
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = "Logout",
                    tint = Color(0xFFFF6B6B),
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Back Button
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceDark)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun InstructionCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "How to Use",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = VibrantCyan
            )
            Text(
                "1. Tap the QR Scanner button below\n2. Scan the transaction QR code\n3. Verify transaction details\n4. Confirm fuel dispensing",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun ScanQRButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        colors = ButtonDefaults.buttonColors(
            containerColor = ElectricBlue
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.QrCode2,
                contentDescription = "Scan QR",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
            Text(
                "Scan QR Code",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun EmptyStateCard(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp)
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = SuccessGreen,
                modifier = Modifier.size(48.dp)
            )
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun TransactionCard(
    transaction: FuelTransaction,
    onConfirm: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        transaction.referenceNumber,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = VibrantCyan
                    )
                    Text(
                        transaction.vehicleId,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                StatusBadge(transaction.status.name)
            }
            
            // Transaction Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TransactionDetailItem("Driver", transaction.driverFullName ?: transaction.driverName, modifier = Modifier.weight(1f))
                TransactionDetailItem("Fuel", "${transaction.litersToPump} L ${transaction.fuelType.name}", modifier = Modifier.weight(1f))
            }
            
            // Confirm Button
            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "Confirm Dispensed",
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun TransactionDetailItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = TextTertiary,
            fontWeight = FontWeight.Medium
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

@Composable
fun StatusBadge(status: String) {
    val backgroundColor = when (status) {
        "PENDING" -> WarningYellow.copy(alpha = 0.2f)
        "APPROVED" -> ElectricBlue.copy(alpha = 0.2f)
        "DISPENSED" -> SuccessGreen.copy(alpha = 0.2f)
        else -> TextSecondary.copy(alpha = 0.2f)
    }
    
    val textColor = when (status) {
        "PENDING" -> WarningYellow
        "APPROVED" -> ElectricBlue
        "DISPENSED" -> SuccessGreen
        else -> TextSecondary
    }
    
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(8.dp, 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            status,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
        )
    }
}



@Composable
fun ConfirmDispensedDialog(
    transaction: FuelTransaction,
    scannedData: ScannedTransaction?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Confirm Fuel Dispensing", color = TextPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ConfirmationRow("Reference", transaction.referenceNumber)
                ConfirmationRow("Vehicle", transaction.vehicleId)
                ConfirmationRow("Driver", transaction.driverFullName ?: transaction.driverName)
                ConfirmationRow("Fuel Type", transaction.fuelType.name)
                ConfirmationRow("Amount", "${transaction.litersToPump} Liters")
                
                if (scannedData != null) {
                    Divider(color = TextSecondary.copy(alpha = 0.3f), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        "QR Code Verified ✓",
                        style = MaterialTheme.typography.bodySmall,
                        color = SuccessGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
            ) {
                Text("Confirm Dispensed", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = SurfaceDark,
        textContentColor = TextPrimary
    )
}

@Composable
fun ConfirmationRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ErrorBanner(message: String, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF8B0000))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    message,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
            IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun SuccessDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Success", color = SuccessGreen, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier.size(56.dp)
                )
                Text(
                    "Transaction marked as dispensed",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "The status has been updated and wallet will be deducted.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
            ) {
                Text("OK", color = Color.White)
            }
        },
        containerColor = SurfaceDark,
        textContentColor = TextPrimary
    )
}

@Composable
fun LogoutConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = null,
                    tint = Color(0xFFFF6B6B),
                    modifier = Modifier
                        .size(28.dp)
                        .padding(end = 8.dp)
                )
                Text(
                    "Logout",
                    color = Color(0xFFFF6B6B),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = null,
                    tint = Color(0xFFFF6B6B),
                    modifier = Modifier.size(56.dp)
                )
                Text(
                    "Are you sure you want to logout?",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SurfaceDark.copy(alpha = 0.7f)
                ),
                modifier = Modifier.fillMaxWidth(0.4f)
            ) {
                Text("Cancel", color = TextPrimary)
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF6B6B)
                ),
                modifier = Modifier.fillMaxWidth(0.4f)
            ) {
                Text("Logout", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = SurfaceDark,
        textContentColor = TextPrimary,
        tonalElevation = 8.dp,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        SurfaceDark,
                        SurfaceDark.copy(alpha = 0.9f)
                    )
                )
            )
    )
}

@Composable
fun CancelledSlipDialog(
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Block,
                    contentDescription = null,
                    tint = Color(0xFFFF6B6B),
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    "Slip Cancelled",
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
                    message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
                Divider(color = TextSecondary.copy(alpha = 0.3f), thickness = 1.dp)
                Text(
                    "This fuel slip cannot be used for dispensing. Please contact your administrator if this was done in error.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B6B))
            ) {
                Text("Understood", color = Color.White)
            }
        },
        containerColor = SurfaceDark,
        textContentColor = TextPrimary
    )
}
