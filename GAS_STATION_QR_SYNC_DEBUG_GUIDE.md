# Gas Station QR Code Sync Issue - Debug Guide

## Problem
When gas station operator scans a QR code, it says "Transaction not found" even though the transaction was created in the main app.

## Root Cause Analysis
The issue is likely one of these:

1. **Transaction not being saved to Firestore** - Data stays local only
2. **Network connectivity issue** - Gas station can't reach Firestore
3. **Data sync delay** - Transaction saved but not visible immediately
4. **Office/Workspace filtering** - Transactions from different offices not synced

## How to Debug

### Step 1: Check Transaction Creation Logs
When creating a transaction in the main app, look for logs like:
```
💾 SAVING TRANSACTION TO FIRESTORE: FS-12345678-1234
✓ TRANSACTION SAVED: FS-12345678-1234
```

If you don't see these, the transaction is NOT being saved to Firestore.

### Step 2: Check Gas Station Load Logs
When opening the gas station screen, look for:
```
=== GAS STATION SCREEN LOADED ===
Transaction refresh initiated from gas station screen
=== LOADING TRANSACTIONS FROM SERVER ===
✓ Loaded N transactions from server
  - FS-12345678-1234 | VEHICLE-001 | PENDING
```

If you see:
```
⚠️ NO TRANSACTIONS FOUND IN FIRESTORE!
```
Then the transaction was never saved to Firestore in the first place.

### Step 3: Check Network Connectivity
- Ensure both main app and gas station have internet
- Check Firestore status in Firebase Console
- Verify Firestore rules allow read/write

### Step 4: Check Timing
The transaction must be saved BEFORE the gas station tries to load it.

## Solutions to Try

### Solution 1: Ensure Transaction Syncs Immediately
Add this to TransactionScreenEnhanced after successful creation:
```kotlin
// Force a refresh after creation
LaunchedEffect(uiState) {
    if (uiState is TransactionUiState.Success) {
        delay(2000)  // Wait 2 seconds for Firestore to sync
        transactionViewModel.refreshTransactions()
    }
}
```

### Solution 2: Add Real-time Sync
Instead of just loading once, subscribe to transaction updates:
```kotlin
// In TransactionViewModel
val transactionUpdates = transactionRepository.getAllTransactions()
    .collect { 
        _transactionHistory.value = it.sortedByDescending { tx -> tx.createdAt }
    }
```

### Solution 3: Verify Firestore Rules
The Firestore rules should allow gas station users to read all transactions:
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /transactions/{document=**} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && 
                      (request.auth.token.role == 'ADMIN' || 
                       request.auth.token.role == 'GAS_STATION');
    }
  }
}
```

### Solution 4: Add Explicit Wait in Gas Station
The gas station should wait longer for transactions to load:
```kotlin
// Increase the retry delay
delay(3000)  // Change from 1500 to 3000
```

## Quick Checklist

- [ ] Transaction logs show "✓ TRANSACTION SAVED"
- [ ] Gas station logs show transactions loaded
- [ ] Network is connected
- [ ] Gas slip reference number matches QR code exactly
- [ ] Both apps connected to same Firestore project
- [ ] No whitespace in reference number comparison
- [ ] Transaction status is PENDING or APPROVED (not DISPENSED/COMPLETED)

## If Still Not Working

1. Open Android Studio Logcat
2. Filter by `Timber` or `FUEL`
3. Look for the reference number
4. Check if "SAVED" log appears
5. Check if gas station refresh logs appear
6. Post the complete logs for debugging

## Expected Logs Flow

```
[Main App] 💾 SAVING TRANSACTION TO FIRESTORE: FS-12345678-1234
[Main App] ✓ TRANSACTION SAVED: FS-12345678-1234
[Main App] Transaction created successfully: FS-12345678-1234

[Gas Station] === GAS STATION SCREEN LOADED ===
[Gas Station] Transaction refresh initiated from gas station screen
[Gas Station] === LOADING TRANSACTIONS FROM SERVER ===
[Gas Station] ✓ Loaded 1 transactions from server
[Gas Station]   - FS-12345678-1234 | VEHICLE-001 | PENDING

[Gas Station] QR Code parsed: 'FS-12345678-1234'
[Gas Station] Available transactions count: 1
[Gas Station] Available transaction refs: ['FS-12345678-1234']
[Gas Station] Transaction found: FS-12345678-1234
```

If you don't see this flow, identify where it breaks and report those specific logs.
