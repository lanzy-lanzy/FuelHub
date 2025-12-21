# Quick Reference - Home Screen Real Data

## Data Sources

```kotlin
// Get Transaction Data
val transactionViewModel = viewModel<TransactionViewModel>()
val transactions by transactionViewModel.transactionHistory.collectAsState()

// Get Wallet Data
val walletViewModel = viewModel<WalletViewModel>()
val walletState by walletViewModel.uiState.collectAsState()
```

## Common Calculations

### Today's Transactions
```kotlin
val today = LocalDate.now()
val todayTransactions = transactions.filter { 
    it.createdAt.toLocalDate() == today 
}
```

### Monthly Transactions
```kotlin
val monthStart = LocalDate.now().withDayOfMonth(1)
val monthTransactions = transactions.filter { 
    it.createdAt.toLocalDate() >= monthStart 
}
```

### Sum Fuel Liters
```kotlin
val totalLiters = transactions.sumOf { it.litersToPump }
```

### Distinct Vehicles Used
```kotlin
val vehicleCount = transactions.map { it.vehicleId }.distinct().size
```

### Calculate Efficiency
```kotlin
val completed = transactions.count { 
    it.status == TransactionStatus.COMPLETED 
}
val efficiency = (completed.toDouble() / transactions.size) * 100
```

## Formatting Helpers

### Format as Liters
```kotlin
"%.1f L".format(12.345)    // "12.3 L"
"%.2f L".format(12.345)    // "12.35 L"
```

### Format as Percentage
```kotlin
"%.0f%%".format(94.567)    // "95%"
"%.1f%%".format(94.567)    // "94.6%"
```

### Time Ago Format
```kotlin
getTimeAgo(transaction.createdAt)
// Returns: "Just now", "5 minutes ago", "2 hours ago", etc.
```

## UI Patterns

### Fetch and Display Pattern
```kotlin
@Composable
fun MetricsSection() {
    val viewModel = viewModel<TransactionViewModel>()
    val transactions by viewModel.transactionHistory.collectAsState()
    
    val metric = transactions.sumOf { it.litersToPump }
    
    Text("Total: %.1f L".format(metric))
}
```

### Safe Wallet Access Pattern
```kotlin
val walletViewModel = viewModel<WalletViewModel>()
val walletState by walletViewModel.uiState.collectAsState()

val wallet = when (walletState) {
    is WalletUiState.Success -> (walletState as WalletUiState.Success).wallet
    else -> null
}

val balance = wallet?.balanceLiters ?: 0.0
```

## Icon Quick Reference

| Function | Icon |
|----------|------|
| Fuel Amount | `Icons.Default.LocalGasStation` |
| Transaction Count | `Icons.Default.Receipt` |
| Trending/Growth | `Icons.AutoMirrored.Filled.TrendingUp` |
| Efficiency/Check | `Icons.Default.CheckCircle` |
| Reports/Analytics | `Icons.Default.BarChart` |
| Vehicle | `Icons.Default.DirectionsCarFilled` |

## State Management Tips

1. **Always use collectAsState()** for reactive updates
2. **Handle null cases** with default values
3. **Filter by date** using LocalDate comparisons
4. **Sort by date** using sortedByDescending for recent-first
5. **Limit results** using take(n) to avoid overwhelming UI

## Performance Notes

- Transactions list can be large - consider pagination for "Recent Transactions"
- Filtering and sorting happens on main thread - consider moving to ViewModel
- collectAsState() triggers recomposition - memoize expensive calculations
- Use distinctBy() to get unique items efficiently

## Common Bugs to Avoid

❌ **Don't do this:**
```kotlin
val transactions = transactions.filter { ... } // Infinite loop!
```

✅ **Do this instead:**
```kotlin
val filteredTransactions = transactions.filter { ... }
```

❌ **Don't forget null checks:**
```kotlin
val balance = wallet.balanceLiters // NPE if wallet is null
```

✅ **Use elvis operator:**
```kotlin
val balance = wallet?.balanceLiters ?: 0.0
```

## Debugging Tips

### Check if data is loading
```kotlin
when (walletState) {
    is WalletUiState.Loading -> Text("Loading...")
    is WalletUiState.Success -> {/* show data */}
    is WalletUiState.Error -> Text("Error: ${(walletState as WalletUiState.Error).message}")
    else -> {}
}
```

### Log transaction count
```kotlin
Timber.d("Loaded ${transactions.size} transactions")
```

### Verify date filtering
```kotlin
Timber.d("Month transactions: ${monthTransactions.size}")
Timber.d("Total fuel: ${monthlyUsage}")
```

## Future Enhancement Ideas

- [ ] Add pull-to-refresh for data
- [ ] Implement pagination for recent transactions
- [ ] Add data caching layer
- [ ] Create separate ViewModel for dashboard metrics
- [ ] Add offline data support
- [ ] Implement real-time listeners for live updates
