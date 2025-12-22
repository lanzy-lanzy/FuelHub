package dev.ml.fuelhub.presentation.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TextField
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import dev.ml.fuelhub.data.model.FuelWallet
import dev.ml.fuelhub.presentation.state.WalletUiState
import dev.ml.fuelhub.presentation.viewmodel.WalletViewModel
import dev.ml.fuelhub.ui.theme.*

/**
 * Premium Wallet Screen - Modern Design
 * Shows current balance, capacity, and refill capability with stunning visuals
 */
@Composable
fun WalletScreen(
    walletViewModel: WalletViewModel,
    walletId: String,
    onRefillClick: (walletId: String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by walletViewModel.uiState.collectAsState()
    
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
        when (uiState) {
            is WalletUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = VibrantCyan,
                        strokeWidth = 4.dp,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }
            
            is WalletUiState.Success -> {
                val wallet = (uiState as WalletUiState.Success).wallet
                WalletContent(
                    wallet = wallet,
                    walletViewModel = walletViewModel,
                    walletId = walletId,
                    onRefillClick = onRefillClick,
                    animatedOffset = animatedOffset
                )
            }
            
            is WalletUiState.Error -> {
                ErrorState(
                    message = (uiState as WalletUiState.Error).message,
                    onRetry = { walletViewModel.loadWallet(walletId) }
                )
            }
            
            else -> {
                InitialState(
                    onLoadWallet = { walletViewModel.loadWallet(walletId) }
                )
            }
        }
    }
}

@Composable
fun WalletContent(
    wallet: FuelWallet,
    walletViewModel: WalletViewModel,
    walletId: String,
    onRefillClick: (String) -> Unit,
    animatedOffset: Float
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        item {
            WalletHeader()
        }

        // Premium Wallet Card with Gradient
        item {
            PremiumWalletCardDetailed(
                wallet = wallet,
                animatedOffset = animatedOffset
            )
        }

        // Stats Row
        item {
            WalletStatsRow(wallet)
        }

        // Action Buttons
        item {
            WalletActionButtons(
                walletId = walletId,
                onRefillClick = onRefillClick,
                onRefresh = { walletViewModel.loadWallet(walletId) }
            )
        }

        // Recent Activity Section
        item {
            Text(
                "Recent Activity",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Sample transactions
        items(getSampleTransactions()) { transaction ->
            WalletTransactionCard(transaction)
        }
    }
}

@Composable
fun WalletHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Fuel Wallet",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = VibrantCyan,
                    fontSize = 32.sp
                )
                Text(
                    "Manage your fuel balance",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(ElectricBlue, VibrantCyan)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = "Wallet",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun PremiumWalletCardDetailed(
    wallet: FuelWallet,
    animatedOffset: Float
) {
    val percentage = (wallet.balanceLiters / wallet.maxCapacityLiters * 100).coerceIn(0.0, 100.0).toFloat()
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .shadow(16.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
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
                        end = Offset(animatedOffset + 600f, animatedOffset + 600f)
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Section
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
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "${String.format("%.2f", wallet.balanceLiters)} L",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 42.sp
                        )
                    }

                    // Circular Progress Indicator
                    CircularProgressIndicatorCustom(
                        percentage = percentage,
                        size = 80.dp
                    )
                }

                // Bottom Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            "Max Capacity",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            "${String.format("%.2f", wallet.maxCapacityLiters)} L",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "Wallet ID",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            wallet.id.take(8),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CircularProgressIndicatorCustom(
    percentage: Float,
    size: Dp
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(size)
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokeWidth = 8.dp.toPx()
            
            // Background circle
            drawArc(
                color = Color.White.copy(alpha = 0.3f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                size = Size(size.toPx() - strokeWidth, size.toPx() - strokeWidth),
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
            )
            
            // Progress arc
            drawArc(
                color = Color.White,
                startAngle = -90f,
                sweepAngle = (percentage / 100f) * 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                size = Size(size.toPx() - strokeWidth, size.toPx() - strokeWidth),
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
            )
        }
        
        Text(
            "${percentage.toInt()}%",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontSize = 16.sp
        )
    }
}

@Composable
fun WalletStatsRow(wallet: FuelWallet) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCardSmall(
            title = "Used Today",
            value = "45.2 L",
            icon = Icons.Default.Info,
            gradient = listOf(AccentOrange, AccentAmber),
            modifier = Modifier.weight(1f)
        )
        StatCardSmall(
            title = "Remaining",
            value = "${String.format("%.1f", wallet.balanceLiters)} L",
            icon = Icons.Default.Star,
            gradient = listOf(SuccessGreen, NeonTeal),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatCardSmall(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradient: List<Color>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .shadow(6.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceDark
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = gradient.map { it.copy(alpha = 0.15f) }
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = gradient[0],
                    modifier = Modifier.size(28.dp)
                )
                Column {
                    Text(
                        value,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        title,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun WalletActionButtons(
    walletId: String,
    onRefillClick: (String) -> Unit,
    onRefresh: () -> Unit
) {
    var showRefillDialog by remember { mutableStateOf(false) }
    var refillAmount by remember { mutableStateOf("") }
    
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = { showRefillDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(8.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(SuccessGreen, NeonTeal)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Refill Wallet",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        }

        OutlinedButton(
            onClick = onRefresh,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = Brush.linearGradient(
                    colors = listOf(VibrantCyan, ElectricBlue)
                )
            ),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = VibrantCyan
            )
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Refresh Balance", fontWeight = FontWeight.SemiBold)
        }
    }
    
    if (showRefillDialog) {
        RefillDialog(
            onConfirm = { amount ->
                if (amount.isNotBlank() && amount.toDoubleOrNull() != null) {
                    onRefillClick(walletId)
                    refillAmount = ""
                    showRefillDialog = false
                }
            },
            onDismiss = {
                refillAmount = ""
                showRefillDialog = false
            },
            refillAmount = refillAmount,
            onAmountChange = { refillAmount = it }
        )
    }
}

data class WalletTransaction(
    val type: String,
    val amount: String,
    val time: String,
    val isDebit: Boolean
)

fun getSampleTransactions() = listOf(
    WalletTransaction("Fuel Transaction", "-50.0 L", "2 hours ago", true),
    WalletTransaction("Wallet Refill", "+500.0 L", "1 day ago", false),
    WalletTransaction("Fuel Transaction", "-75.5 L", "2 days ago", true)
)

@Composable
fun WalletTransactionCard(transaction: WalletTransaction) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceDark
        )
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (transaction.isDebit)
                                AccentOrange.copy(alpha = 0.2f)
                            else
                                SuccessGreen.copy(alpha = 0.2f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (transaction.isDebit) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                        contentDescription = null,
                        tint = if (transaction.isDebit) AccentOrange else SuccessGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        transaction.type,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        transaction.time,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
            Text(
                transaction.amount,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if (transaction.isDebit) AccentOrange else SuccessGreen
            )
        }
    }
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = ErrorRed,
                modifier = Modifier.size(64.dp)
            )
            Text(
                "Error",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ErrorRed
                )
            ) {
                Text("Retry")
            }
        }
    }
}

@Composable
fun InitialState(onLoadWallet: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = onLoadWallet,
            modifier = Modifier
                .height(56.dp)
                .shadow(8.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(ElectricBlue, VibrantCyan)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Load Wallet",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun RefillDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    refillAmount: String,
    onAmountChange: (String) -> Unit
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
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Enter the amount of fuel (in liters) you want to refill",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                TextField(
                    value = refillAmount,
                    onValueChange = onAmountChange,
                    label = {
                        Text("Amount (L)", color = TextSecondary)
                    },
                    placeholder = {
                        Text("e.g., 500", color = TextSecondary)
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = SurfaceDark,
                        focusedContainerColor = SurfaceDark,
                        unfocusedTextColor = TextPrimary,
                        focusedTextColor = TextPrimary,
                        cursorColor = VibrantCyan,
                        focusedBorderColor = VibrantCyan,
                        unfocusedBorderColor = TextSecondary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(refillAmount) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = SuccessGreen
                )
            ) {
                Text("Refill", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = VibrantCyan)
            }
        },
        containerColor = SurfaceDark,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary
    )
}
