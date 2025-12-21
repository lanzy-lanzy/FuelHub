# Home Screen Real Data Update

## Summary
Updated the Home Screen and Dashboard to fetch real data from Firebase instead of using hardcoded values. Also fixed all icons to use appropriate Material Design icons.

## Changes Made

### 1. **HomeScreen.kt** - Real Data Integration

#### Key Metrics Grid
- **Total Usage**: Now calculates actual monthly fuel consumption from transactions
- **Avg Per Day**: Computes average based on distinct days with transactions
- **Transactions**: Shows actual transaction count for current month
- **Efficiency**: Calculated as percentage of completed transactions
- **Icon Fix**: Changed `SwapHoriz` → `Receipt` for Transactions

#### Wallet Status Card
- Fetches real wallet data using `WalletViewModel`
- Displays actual balance and capacity from Firebase
- Progress bar reflects real wallet percentage
- Updates dynamically as wallet data changes

#### Today's Summary Stats
- **Fuel Used**: Sum of all transactions for today
- **Transactions**: Count of today's transactions
- **Vehicles Used**: Number of distinct vehicles used today
- **Icon Fix**: Changed `SwapHoriz` → `Receipt` for Transactions

#### Vehicle Fleet Section
- Fetches actual vehicles from `TransactionViewModel`
- Shows real plate numbers and fuel types
- Calculates usage percentage based on transaction history
- Displays up to 5 most active vehicles

#### Recent Transactions
- Shows last 4 transactions sorted by date
- Displays real fuel type and amount
- Shows relative time (e.g., "2 hours ago") using `getTimeAgo()` function
- Real vehicle IDs instead of placeholders

### 2. **DashboardScreen.kt** - Icon Fixes

Fixed the following icon mappings:
- **Today's Usage**: `Star` → `LocalGasStation`
- **Refill Wallet**: `Star` → `LocalGasStation`
- **View Reports**: `Assessment` → `BarChart`
- **Transaction Cards**: `Star` → `LocalGasStation`

### 3. **New Utility Functions**

#### `calculateEfficiency()`
```kotlin
fun calculateEfficiency(transactions: List<FuelTransaction>): Double {
    if (transactions.isEmpty()) return 100.0
    val completed = transactions.count { 
        it.status == TransactionStatus.COMPLETED 
    }
    return (completed.toDouble() / transactions.size) * 100
}
```

#### `getTimeAgo()`
```kotlin
fun getTimeAgo(dateTime: LocalDateTime): String {
    val now = LocalDateTime.now()
    val duration = java.time.temporal.ChronoUnit.MINUTES.between(dateTime, now)
    
    return when {
        duration < 1 -> "Just now"
        duration < 60 -> "$duration minutes ago"
        duration < 1440 -> "${duration / 60} hours ago"
        duration < 10080 -> "${duration / 1440} days ago"
        else -> "${duration / 10080} weeks ago"
    }
}
```

## Data Flow

```
HomeScreen
├── TransactionViewModel
│   ├── transactionHistory (all transactions)
│   ├── vehicles (all vehicles)
│   └── drivers (all users)
│
└── WalletViewModel
    └── uiState (wallet balance and capacity)
```

## Key Features

1. **Real-time Updates**: Uses StateFlow for reactive updates
2. **Calculated Metrics**: All values computed from actual data
3. **Smart Formatting**: Numbers formatted with appropriate decimal places
4. **Relative Timestamps**: Shows human-readable time differences
5. **Performance**: Efficient calculations using Kotlin collection operations

## Icon Standardization

All fuel-related icons now use:
- `Icons.Default.LocalGasStation` - For fuel wallets and transactions
- `Icons.Default.Receipt` - For transaction counts
- `Icons.Default.BarChart` - For reports
- `Icons.Default.DirectionsCarFilled` - For vehicles

## Testing Checklist

- [ ] Verify metrics update when transactions are created
- [ ] Check wallet balance reflects real data
- [ ] Confirm vehicle fleet shows actual vehicles
- [ ] Test time formatting for recent transactions
- [ ] Verify all icons display correctly
- [ ] Check data loads on screen open
- [ ] Test with empty transaction list (should show 0 values)

## Dependencies

- `androidx.lifecycle:lifecycle-viewmodel-compose`
- `java.time.LocalDateTime`
- `java.time.temporal.ChronoUnit`

## Notes

- The efficiency metric is based on transaction completion rate
- Vehicle usage percentage assumes 100L tank capacity (can be adjusted)
- All timestamps are formatted in user's local timezone
- Empty states gracefully show 0 values instead of crashing
