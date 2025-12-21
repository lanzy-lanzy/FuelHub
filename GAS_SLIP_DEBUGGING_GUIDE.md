# Gas Slip Fetching - Debugging Guide

## Latest Changes (Fixed)

### Changed from Flow-based fetching to One-Time Query
- **Why**: Flow's `first()` might not wait for the snapshot listener to initialize and get data
- **Solution**: Added `getAllGasSlipsOneTime()` that uses a direct Firestore query instead of a listener
- **Benefits**:
  - More reliable for initial load
  - Ensures we get current data from Firestore
  - Better timeout handling
  - Clearer error messages

### Code Changes:
1. **FirebaseDataSource.kt**: Added `getAllGasSlipsOneTime()` method
2. **FirebaseGasSlipRepositoryImpl.kt**: Now uses `getAllGasSlipsOneTime()` instead of `.first()`

## How Gas Slip Flow Should Work

```
Transaction Created → Gas Slip Saved to Firestore
                  ↓
         User navigates to Gas Slips tab
                  ↓
         GasSlipManagementViewModel.init()
                  ↓
         loadAllGasSlips() called
                  ↓
         FirebaseDataSource.getAllGasSlipsOneTime()
                  ↓
         Query Firestore for gas_slips collection
                  ↓
         Convert documents to GasSlip objects
                  ↓
         Update UI with results
                  ↓
         GasSlipListScreen displays list or "No gas slips found"
```

## If Gas Slips Still Don't Show

### Step 1: Check Firestore Data
1. Go to Firebase Console → Firestore Database
2. Look for `gas_slips` collection
3. Verify documents exist with proper structure:
   ```
   ✅ Should have:
   - id
   - transactionId
   - referenceNumber
   - driverName
   - vehicleType
   - vehiclePlateNumber
   - destination
   - tripPurpose
   - passengers (optional)
   - fuelType
   - litersToPump
   - transactionDate
   - mdrrmoOfficeId
   - mdrrmoOfficeName
   - generatedAt
   - isUsed (true/false)
   - usedAt (optional, nullable)
   ```

### Step 2: Check Logcat for Errors
Look for these log messages in Android Studio Logcat:
```
Timber.d("Fetching all gas slips from Firestore")
Timber.d("One-time fetch returned X gas slips")
Timber.d("Loaded X gas slips")
```

**If you see errors**, look for:
```
Timber.e("Error in getAllGasSlipsOneTime:")
Timber.e("Error loading gas slips:")
Timber.w("Missing required fields in GasSlip document:")
Timber.w("Invalid fuel type:")
```

### Step 3: Check Firestore Permissions
Your Firestore rules should allow reading the `gas_slips` collection:

```javascript
// In Firebase Console → Firestore → Rules
match /gas_slips/{document=**} {
  allow read: if request.auth != null;
  allow write: if request.auth != null;
}
```

Or if you want to test without auth:
```javascript
match /gas_slips/{document=**} {
  allow read, write: if true;
}
```

### Step 4: Manual Verification
1. Create a transaction successfully (should see "Transaction Successful!" dialog)
2. Click "Print Gas Slip" button (optional)
3. Dismiss the dialog - it should navigate back to home
4. Click the "Gas Slips" tab at the bottom
5. Should see the newly created gas slip in the list

### Step 5: Add Debug Logging
If gas slips still don't appear, add this temporary code to TransactionScreenEnhanced.kt:

```kotlin
.onPrint = { gasSlip ->
    Timber.d("PRINT ACTION: Gas slip ID = ${gasSlip.id}")
    Timber.d("PRINT ACTION: Reference = ${gasSlip.referenceNumber}")
    pdfPrintManager?.generateAndPrintGasSlip(gasSlip)
    
    // Add delay to ensure Firestore has time to persist
    Thread.sleep(500)
    
    gasSlipViewModel?.loadAllGasSlips()
    Timber.d("PRINT ACTION: Refresh triggered")
}
```

## Common Issues and Solutions

### Issue 1: "No gas slips found" even after transaction
- **Check**: Are gas slips being created in Firestore?
- **Fix**: Check Firestore console to verify documents exist
- **Also check**: Is Firestore initialized correctly in FuelHubApplication?

### Issue 2: ViewModel shows Error state
- **Check**: Read Logcat for the error message
- **Likely causes**:
  - Firestore permissions not allowing reads
  - Network connectivity issue
  - Invalid data in Firestore (missing required fields)

### Issue 3: Seeing "Loading..." forever
- **Check**: Is the query hanging?
- **Fix**: The new implementation doesn't use timeout, but if Firestore is unreachable it might hang
- **Solution**: Check internet connection, verify Firestore is initialized

### Issue 4: Data appears after manual refresh button
- **Check**: The refresh button at top-right of Gas Slips screen
- **This means**: Initial load is failing but the query works when triggered manually
- **Fix**: Ensure ViewModel init is called properly

## Testing Steps

1. **Clear all data**: Delete all documents from `gas_slips` collection in Firebase Console
2. **Create transaction**: Complete a transaction from start to finish
3. **Print immediately**: Click "Print Gas Slip" button in success dialog
4. **Navigate to Gas Slips**: Use bottom navigation
5. **Verify**: New gas slip appears in the list

## Success Indicators

When everything works, you should see in Logcat:
```
D/Timber: Fetching all gas slips from Firestore
D/Timber: One-time fetch returned 1 gas slips
D/Timber: Loaded 1 gas slips
```

And the UI should show the gas slip card with:
- Reference number
- Driver name
- Status badge (PENDING or USED)
- Expandable details with vehicle info, fuel type, liters, etc.

## Database Schema

Gas Slip documents should have this structure in Firestore:

```kotlin
// From GasSlip.kt
id: String                    // UUID
transactionId: String         // Link to transaction
referenceNumber: String       // Human-readable ref like "FS-93222486-1133"
driverName: String           // From vehicle
vehicleType: String          // From vehicle
vehiclePlateNumber: String   // From vehicle
destination: String          // Where fuel is needed
tripPurpose: String          // Why fuel is needed
passengers: String?          // Optional, number/names
fuelType: FuelType           // GASOLINE, DIESEL, LPG
litersToPump: Double         // Amount of fuel
transactionDate: LocalDateTime // When transaction happened
mdrrmoOfficeId: String       // Office ID from wallet
mdrrmoOfficeName: String     // Office name
generatedAt: LocalDateTime   // When gas slip was created
isUsed: Boolean              // false for pending, true for used
usedAt: LocalDateTime?       // When gas slip was used
```

## Next Steps if Issue Persists

1. Check `CreateFuelTransactionUseCase.kt` - verify `gasSlipRepository.createGasSlip()` is called
2. Check `GasSlipRepositoryImpl.kt` - verify `FirebaseDataSource.createGasSlip()` actually writes to Firestore
3. Add Firestore write listener to see if documents are actually being saved
4. Consider adding a test transaction that creates a gas slip with hardcoded data
