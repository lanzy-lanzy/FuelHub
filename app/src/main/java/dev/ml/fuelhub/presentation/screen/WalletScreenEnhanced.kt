package dev.ml.fuelhub.presentation.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.ml.fuelhub.presentation.state.WalletUiState
import dev.ml.fuelhub.presentation.viewmodel.WalletViewModel
import dev.ml.fuelhub.presentation.component.DrawerSwipeIndicator
import dev.ml.fuelhub.ui.theme.*
import timber.log.Timber

/**
 * Enhanced Wallet Screen with Real Data Functionality
 * - Fully integrated with WalletViewModel
 * - Real wallet balance updates
 * - Refill operations
 * - Transaction history
 * - Balance visualization
 */
@Composable
fun WalletScreenEnhanced(
    walletViewModel: WalletViewModel,
    walletId: String,
    onRefillClick: (walletId: String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by walletViewModel.uiState.collectAsState()
    
    // Refill dialog state
    var showRefillDialog by remember { mutableStateOf(false) }
    var refillAmount by remember { mutableStateOf("") }
    var refillValidationError by remember { mutableStateOf("") }

    // Animated gradient
    val infiniteTransition = rememberInfiniteTransition(label = "walletGradient")
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "walletOffset"
    )

    // Load wallet on composition
    LaunchedEffect(walletId) {
        walletViewModel.loadWallet(walletId)
        Timber.d("Loading wallet: $walletId")
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlue)
    ) {
        when (uiState) {
            is WalletUiState.Loading -> {
                WalletLoadingState()
            }

            is WalletUiState.Success -> {
                val wallet = (uiState as WalletUiState.Success).wallet
                WalletContentEnhanced(
                    wallet = wallet,
                    walletViewModel = walletViewModel,
                    onRefillClick = { showRefillDialog = true },
                    animatedOffset = animatedOffset
                )
            }

            is WalletUiState.Error -> {
                WalletErrorState(
                    message = (uiState as WalletUiState.Error).message,
                    onRetry = { walletViewModel.loadWallet(walletId) }
                )
            }

            else -> {
                WalletInitialState(
                    onLoadWallet = { walletViewModel.loadWallet(walletId) }
                )
            }
        }

        // Refill Dialog
        if (showRefillDialog) {
            RefillWalletDialog(
                onDismiss = { showRefillDialog = false },
                onConfirm = { amount ->
                    if (validateRefillAmount(amount).also { refillValidationError = it }.isEmpty()) {
                        walletViewModel.refillWallet(walletId, amount.toDouble())
                        showRefillDialog = false
                        refillAmount = ""
                        Timber.d("Refill initiated: $amount L")
                    }
                },
                refillAmount = refillAmount,
                onAmountChange = { refillAmount = it },
                validationError = refillValidationError
            )
        }
    }
}

@Composable
fun WalletContentEnhanced(
    wallet: dev.ml.fuelhub.data.model.FuelWallet,
    walletViewModel: WalletViewModel,
    onRefillClick: () -> Unit,
    animatedOffset: Float
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            item {
                WalletHeaderEnhanced()
            }

        // Balance Card
         item {
             BalanceCardEnhanced(
                 balance = wallet.balanceLiters,
                 maxCapacity = wallet.maxCapacityLiters,
                 currency = "L",
                 animatedOffset = animatedOffset,
                 onRefillClick = onRefillClick
             )
         }

        // Quick Stats
        item {
            WalletStatsEnhanced(wallet)
        }

        // Refill Section
        item {
            RefillSection(onRefillClick = onRefillClick)
        }

        // Wallet Info
        item {
            WalletInfoSection(wallet)
        }

        // Recent Transactions
        item {
            RecentTransactionsWallet()
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
        }

        // Floating Drawer Swipe Indicator
        DrawerSwipeIndicator(
            tintColor = VibrantCyan.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun WalletHeaderEnhanced() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Text(
            "Fuel Wallet",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = VibrantCyan,
            fontSize = 28.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Manage your fuel balance and transactions",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}

@Composable
fun BalanceCardEnhanced(
    balance: Double,
    maxCapacity: Double,
    currency: String,
    animatedOffset: Float,
    onRefillClick: (() -> Unit)? = null
) {
    val usagePercentage = (balance / maxCapacity).coerceIn(0.0, 1.0)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .shadow(12.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            GradientStart,
                            GradientMid,
                            GradientEnd
                        ),
                        start = Offset(animatedOffset, animatedOffset),
                        end = Offset(animatedOffset + 500f, animatedOffset + 500f)
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            "Available Balance",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "${"%.2f".format(balance)} L",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 36.sp
                        )
                        Text(
                            "of ${"%.2f".format(maxCapacity)} L capacity",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.LocalGasStation,
                        contentDescription = "Wallet",
                        tint = Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(56.dp)
                    )
                }

                // Progress bar with percentage
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LinearProgressIndicator(
                        progress = { usagePercentage.toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = when {
                            usagePercentage >= 0.8 -> WarningYellow
                            usagePercentage >= 0.5 -> SuccessGreen
                            else -> ElectricBlue
                        },
                        trackColor = Color.White.copy(alpha = 0.2f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "${"%.1f".format(usagePercentage * 100)}% Full",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                // Action button
                Button(
                    onClick = { onRefillClick?.invoke() },
                    modifier = Modifier
                        .align(Alignment.End)
                        .height(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.2f),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Refill",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Refill", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun WalletStatsEnhanced(wallet: dev.ml.fuelhub.data.model.FuelWallet) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            "Wallet Details",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
        
        StatItemWallet(
            label = "Wallet ID",
            value = wallet.id,
            color = ElectricBlue
        )
        
        StatItemWallet(
            label = "Office",
            value = wallet.officeId,
            color = SuccessGreen
        )
        
        StatItemWallet(
            label = "Created",
            value = wallet.createdAt.toString().take(10),
            color = AccentOrange
        )
    }
}

@Composable
fun StatItemWallet(
    label: String,
    value: String,
    color: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
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
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
            }
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
    }
}

@Composable
fun RefillSection(onRefillClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .clickable { onRefillClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Brush.linearGradient(
                listOf(SuccessGreen.copy(alpha = 0.2f), NeonTeal.copy(alpha = 0.2f))
            ).let { SurfaceDark }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Ready to Refill?",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Add fuel to your wallet",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Refill",
                tint = SuccessGreen,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun WalletInfoSection(wallet: dev.ml.fuelhub.data.model.FuelWallet) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Wallet Information",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )

        InfoCard("Office ID", wallet.officeId)
        InfoCard("Balance", "${"%.2f".format(wallet.balanceLiters)} L")
        InfoCard("Capacity", "${"%.2f".format(wallet.maxCapacityLiters)} L")
        InfoCard("Last Updated", wallet.lastUpdated.toString().take(10))
    }
}

@Composable
fun InfoCard(label: String, value: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(10.dp)),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
    }
}

@Composable
fun RecentTransactionsWallet() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "Recent Activity",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )

        // Mock data - replace with real data from ViewModel
        val transactions = listOf(
            Triple("Refill", "+100.00 L", SuccessGreen),
            Triple("Transaction", "-50.00 L", AccentOrange),
            Triple("Transaction", "-75.00 L", VibrantCyan)
        )

        transactions.forEach { (type, amount, color) ->
            TransactionRowWallet(type, amount, color)
        }
    }
}

@Composable
fun TransactionRowWallet(type: String, amount: String, color: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalGasStation,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(type, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Text(amount, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun RefillWalletDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    refillAmount: String,
    onAmountChange: (String) -> Unit,
    validationError: String
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Refill Wallet",
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Enter the amount of fuel to add to your wallet",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                
                TextField(
                    value = refillAmount,
                    onValueChange = onAmountChange,
                    label = { Text("Amount (Liters)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = SurfaceDark,
                        unfocusedContainerColor = SurfaceDark
                    )
                )
                
                if (validationError.isNotEmpty()) {
                    Text(
                        validationError,
                        color = WarningYellow,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(refillAmount) },
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
            ) {
                Text("Refill", color = Color.White)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = TextTertiary)
            ) {
                Text("Cancel", color = Color.White)
            }
        },
        containerColor = SurfaceDark,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary
    )
}

@Composable
fun WalletLoadingState() {
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
                "Loading Wallet...",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun WalletErrorState(message: String, onRetry: () -> Unit) {
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
                    "Oops! Error Loading Wallet",
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
fun WalletInitialState(onLoadWallet: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = onLoadWallet,
            modifier = Modifier
                .height(48.dp)
                .widthIn(min = 200.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VibrantCyan),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Load Wallet", fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

fun validateRefillAmount(amount: String): String {
    return when {
        amount.isBlank() -> "Amount is required"
        amount.toDoubleOrNull() == null -> "Amount must be a valid number"
        amount.toDouble() <= 0 -> "Amount must be greater than zero"
        amount.toDouble() > 5000 -> "Cannot refill more than 5000 liters per transaction"
        else -> ""
    }
}
