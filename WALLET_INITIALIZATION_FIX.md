# Wallet Initialization Fix

## Problem
Transactions and wallet screens were failing with errors:
- "Fuel wallet default-wallet-id not found" (Transaction screen)
- "Wallet not found" (Wallet screen)

The app was hardcoding the wallet ID as "default-wallet-id" but no wallet existed in the database.

## Root Cause
The app had no initialization code to create the default wallet when it first launches. Users had to manually create a wallet in the database, which isn't practical for an offline mobile app.

## Solution
Added automatic default wallet initialization in `MainActivity.onCreate()`:

```kotlin
// Create default wallet if it doesn't exist
lifecycleScope.launch {
    try {
        val existingWallet = walletRepository.getWalletById("default-wallet-id")
        if (existingWallet == null) {
            val defaultWallet = FuelWallet(
                id = "default-wallet-id",
                officeId = "mdrrmo-office-1",
                balanceLiters = 1000.0,
                maxCapacityLiters = 5000.0,
                lastUpdated = LocalDateTime.now(),
                createdAt = LocalDateTime.now()
            )
            walletRepository.createWallet(defaultWallet)
            Timber.d("Default wallet created successfully")
        }
    } catch (e: Exception) {
        Timber.e(e, "Error initializing default wallet")
    }
}
```

### Changes Made:
1. Added imports: `lifecycleScope`, `launch`, `FuelWallet`, `LocalDateTime`
2. Added initialization code in `onCreate()` before `setContent()`
3. Checks if wallet "default-wallet-id" exists
4. If not found, creates a new wallet with:
   - Initial balance: **1000 liters**
   - Max capacity: **5000 liters**
   - Office ID: **mdrrmo-office-1**

## How It Works
1. When app launches, `MainActivity.onCreate()` is called
2. Initialization code checks if default wallet exists
3. If it doesn't exist, creates it automatically
4. If it already exists, skips creation (idempotent)
5. Wallet is ready to use for transactions and viewing

## Files Modified
- `app/src/main/java/dev/ml/fuelhub/MainActivity.kt`
  - Added imports (lines 6-7, 30-31)
  - Added wallet initialization logic (lines 90-111)

## Testing
After app restart:
- Transaction screen should work without "wallet not found" errors
- Wallet screen should load with 1000L balance
- Transactions should deduct from wallet balance
- Wallet can be refilled using the refill feature

## Configuration
To change default wallet settings, edit the values in `MainActivity.kt`:
- `balanceLiters = 1000.0` → Change initial balance
- `maxCapacityLiters = 5000.0` → Change max capacity
- `officeId = "mdrrmo-office-1"` → Change office ID
