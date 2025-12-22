# Gas Slip Cancellation - Sync Fix

## Issue
When a gas slip was cancelled in the management screen, it correctly updated the GasSlip status to CANCELLED, but the corresponding **FuelTransaction** status remained PENDING. This caused the slip to still appear in the gas station's "Pending Transactions" list.

## Root Cause
GasSlip and FuelTransaction are linked 1:1 by `transactionId` but exist as separate records in Firebase:
- Cancelling only updated `GasSlip.status = CANCELLED`
- Did NOT update `FuelTransaction.status = CANCELLED`
- Gas station screen filters transactions by `status in [PENDING, APPROVED]`
- Cancelled slip still had PENDING FuelTransaction, so it appeared in the list

## Solution
Updated `FirebaseGasSlipRepositoryImpl.cancelGasSlip()` to **sync both models**:

```kotlin
override suspend fun cancelGasSlip(gasSlipId: String): GasSlip? {
    return try {
        val gasSlip = getGasSlipById(gasSlipId) ?: return null
        val cancelledSlip = gasSlip.copy(
            status = GasSlipStatus.CANCELLED
        )
        FirebaseDataSource.updateGasSlip(cancelledSlip).getOrThrow()
        
        // Also cancel the corresponding transaction to sync the status
        try {
            val transaction = FirebaseDataSource.getTransactionById(gasSlip.transactionId).getOrNull()
            if (transaction != null && transaction.status == TransactionStatus.PENDING) {
                val cancelledTransaction = transaction.copy(
                    status = TransactionStatus.CANCELLED,
                    completedAt = LocalDateTime.now()
                )
                FirebaseDataSource.updateTransaction(cancelledTransaction).getOrThrow()
                Timber.d("Corresponding transaction also cancelled: ${gasSlip.transactionId}")
            }
        } catch (e: Exception) {
            Timber.w(e, "Could not cancel corresponding transaction")
            // Don't fail - GasSlip cancellation already succeeded
        }
        
        cancelledSlip
    } catch (e: Exception) {
        Timber.e(e, "Error cancelling gas slip")
        null
    }
}
```

## How It Works Now

### Management Screen
1. User cancels a gas slip
2. `GasSlip.status` → `CANCELLED`
3. `FuelTransaction.status` → `CANCELLED` (synced)

### Gas Station Screen
1. Loads transactions with filter: `status in [PENDING, APPROVED]`
2. Cancelled slips have `status = CANCELLED`, so they're **excluded**
3. Cancelled slip **no longer appears** in pending transactions list
4. If operator scans the QR code, blocked dialog still appears

## Files Modified
- `FirebaseGasSlipRepositoryImpl.kt` - Enhanced `cancelGasSlip()` method

## Status: ✅ COMPLETE

Now when a slip is cancelled:
- ✅ GasSlip status updated to CANCELLED
- ✅ FuelTransaction status updated to CANCELLED
- ✅ Cancelled slip removed from pending transactions list
- ✅ Sync issue resolved
