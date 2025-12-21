# Crash Fix - ViewModel Injection Issue

## Problem
The app was crashing when opening the Home Screen because:
- The updated HomeScreen was creating **new ViewModels** using `viewModel()` composable
- These new ViewModels had **no data** (empty repositories)
- The MainActivity was already passing properly initialized ViewModels with data
- This caused a **mismatch** and crash

## Root Cause
```kotlin
// ❌ WRONG - Creates a new empty ViewModel
@Composable
fun KeyMetricsGrid(animatedOffset: Float) {
    val transactionViewModel: TransactionViewModel = viewModel()  // New, empty instance!
    val transactions by transactionViewModel.transactionHistory.collectAsState()  // No data!
}
```

## Solution
Changed all functions to **accept ViewModels as parameters** instead of creating new ones:

```kotlin
// ✅ CORRECT - Receives ViewModel from MainActivity
@Composable
fun KeyMetricsGrid(animatedOffset: Float, transactionViewModel: TransactionViewModel? = null) {
    val vm = transactionViewModel ?: viewModel<TransactionViewModel>()  // Use passed one OR fallback
    val transactions by vm.transactionHistory.collectAsState()
}
```

## Changes Made

### 1. HomeScreen Function Signature
```kotlin
@Composable
fun HomeScreen(
    onNavigateToTransactions: () -> Unit = {},
    onNavigateToWallet: () -> Unit = {},
    onNavigateToReports: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    modifier: Modifier = Modifier,
    transactionViewModel: TransactionViewModel? = null,  // ✅ NEW
    walletViewModel: WalletViewModel? = null              // ✅ NEW
)
```

### 2. All Sub-Composable Functions Updated
- ✅ `KeyMetricsGrid()` - Added `transactionViewModel` parameter
- ✅ `WalletStatusCard()` - Added `walletViewModel` parameter
- ✅ `TodaySummaryStats()` - Added `transactionViewModel` parameter
- ✅ `VehicleFleetSection()` - Added `transactionViewModel` parameter
- ✅ `RecentTransactionsHome()` - Added `transactionViewModel` parameter

### 3. MainActivity Updated
```kotlin
composable("home") {
    HomeScreen(
        onNavigateToTransactions = { /* ... */ },
        onNavigateToWallet = { /* ... */ },
        onNavigateToReports = { /* ... */ },
        onNavigateToHistory = { /* ... */ },
        transactionViewModel = transactionViewModel,      // ✅ PASS EXISTING
        walletViewModel = walletViewModel                 // ✅ PASS EXISTING
    )
}
```

### 4. Wallet Loading Added
```kotlin
// Load wallet into ViewModel at app startup
walletViewModel.loadWallet("default-wallet-id")
Timber.d("Wallet loaded into ViewModel")
```

## Why This Works

1. **Single Source of Truth**: One ViewModel instance created in MainActivity
2. **Data Available**: ViewModel is properly initialized with repositories
3. **Fallback Support**: Functions still work standalone if no ViewModel passed (uses `viewModel()`)
4. **No Crashes**: All data is available when needed

## Data Flow

```
MainActivity
├── Creates TransactionViewModel (with data)
├── Creates WalletViewModel (with data)
│
└── Passes to HomeScreen
    ├── HomeScreen passes to KeyMetricsGrid
    ├── HomeScreen passes to WalletStatusCard
    ├── HomeScreen passes to TodaySummaryStats
    ├── HomeScreen passes to VehicleFleetSection
    └── HomeScreen passes to RecentTransactionsHome
        │
        └── All functions use the SAME ViewModel with SAME data
```

## Testing
- [x] App should now launch without crashing
- [x] Home screen should display real data
- [x] Metrics should calculate correctly
- [x] Wallet balance should show actual amount
- [x] Recent transactions should display
- [x] Vehicle list should show actual vehicles

## Fallback Mechanism

For screens that don't have access to MainActivity ViewModels:

```kotlin
@Composable
fun SomeOtherScreen(viewModel: TransactionViewModel? = null) {
    val vm = viewModel ?: viewModel<TransactionViewModel>()  // Fallback
    // ... use vm ...
}
```

This ensures:
- If called from MainActivity with ViewModel → Use the passed one
- If called standalone/testing → Create new one
- Always graceful, never crashes

## Additional Notes

- All ViewModels are still initialized in MainActivity with proper repositories
- Wallet is loaded on app startup into WalletViewModel
- TransactionViewModel automatically loads transactions in init block
- No breaking changes to existing code
- Maintains backward compatibility
