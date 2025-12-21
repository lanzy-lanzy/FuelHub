# Reports Data Cache Refresh Fix

## Problem
After deleting transactions in Firebase Firestore, the Reports screen was still showing the old deleted transactions (23 pending records). This was a caching issue where the app's offline cache wasn't being updated with the server deletions.

## Root Cause
The app uses Firebase Firestore's offline persistence feature, which caches data locally. When data is deleted directly in Firebase Firestore (through the web console, etc.), the app's local cache isn't immediately aware of these changes.

## Solution Implemented

### 1. Added Server-Only Fetch Method
**File:** `app/src/main/java/dev/ml/fuelhub/data/firebase/FirebaseDataSource.kt`

Created a new method that forces Firestore to fetch from the server, bypassing the local cache:

```kotlin
suspend fun getAllTransactionsFromServer(): Result<List<FuelTransaction>> = try {
    // Force fetch from server (bypass cache)
    val docs = db.collection(TRANSACTIONS_COLLECTION)
        .get(com.google.firebase.firestore.Source.SERVER)
        .await()
    val transactions = docs.documents.mapNotNull { it.toFuelTransaction() }
    Result.success(transactions)
} catch (e: Exception) {
    Timber.e(e, "Error getting transactions from server")
    Result.failure(e)
}
```

Key: Uses `Source.SERVER` to bypass cache

### 2. Updated Repository Interface
**File:** `app/src/main/java/dev/ml/fuelhub/domain/repository/FuelTransactionRepository.kt`

Added new method to the interface:

```kotlin
/**
 * Force refresh transactions from server (bypass cache).
 */
suspend fun getAllTransactionsFromServer(): List<FuelTransaction>
```

### 3. Implemented in Repository
**File:** `app/src/main/java/dev/ml/fuelhub/data/repository/FirebaseFuelTransactionRepositoryImpl.kt`

Implemented the server fetch method:

```kotlin
override suspend fun getAllTransactionsFromServer(): List<FuelTransaction> {
    return try {
        FirebaseDataSource.getAllTransactionsFromServer().getOrNull() ?: emptyList()
    } catch (e: Exception) {
        Timber.e(e, "Error getting all transactions from server: ${e.message}")
        emptyList()
    }
}
```

### 4. Added Refresh Function to ViewModel
**File:** `app/src/main/java/dev/ml/fuelhub/presentation/viewmodel/ReportsViewModel.kt`

Added a public `refreshData()` function that users can call:

```kotlin
fun refreshData() {
    Timber.d("Refreshing reports data from Firebase (server)")
    viewModelScope.launch {
        try {
            _isLoading.value = true
            // Fetch directly from server to bypass cache
            val transactions = transactionRepository.getAllTransactionsFromServer()
            val vehicles = transactions.map { it.vehicleId }.distinct()
            val drivers = transactions.map { it.driverName }.distinct()
            
            _availableVehicles.value = vehicles
            _availableDrivers.value = drivers
            
            // Re-apply current filters
            applyFilters()
            _errorMessage.value = null
            Timber.d("Data refreshed successfully from server - ${transactions.size} transactions")
        } catch (e: Exception) {
            Timber.e(e, "Error refreshing data: ${e.message}")
            _errorMessage.value = "Error refreshing data: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }
}
```

### 5. Added Refresh Button to UI
**File:** `app/src/main/java/dev/ml/fuelhub/presentation/screen/ReportScreenEnhanced.kt`

Added a green refresh button in the header with Refresh icon:

```kotlin
// Refresh Button
IconButton(
    onClick = onRefreshClick,
    modifier = Modifier
        .size(48.dp)
        .clip(RoundedCornerShape(12.dp))
        .background(
            brush = Brush.linearGradient(
                colors = listOf(SuccessGreen.copy(alpha = 0.8f), NeonTeal.copy(alpha = 0.8f))
            )
        )
) {
    Icon(
        imageVector = Icons.Default.Refresh,
        contentDescription = "Refresh Data",
        tint = Color.White,
        modifier = Modifier.size(20.dp)
    )
}
```

Button location: Top of Reports screen, before Filter and Export buttons

## How to Use

### For Users
When you see stale data in the Reports screen:
1. Click the green **Refresh** button (with circular arrow icon) in the header
2. Wait for the data to reload from Firebase Firestore
3. The reports will now show the latest data, including any deletions

### For Developers
To programmatically refresh:
```kotlin
viewModel.refreshData()
```

## Data Flow

```
User clicks Refresh Button
    ↓
ReportScreenEnhanced calls viewModel.refreshData()
    ↓
ReportsViewModel fetches from server (bypasses cache)
    ↓
FirebaseDataSource.getAllTransactionsFromServer()
    ├─ Source.SERVER: Force server fetch
    └─ Bypass offline cache
    ↓
Update available filters (vehicles, drivers)
    ↓
Re-apply current filters
    ↓
UI displays fresh data
```

## Technical Details

### Firestore Source Options
- **Source.CACHE** - Uses local cache only (fast, offline)
- **Source.SERVER** - Fetches from server, bypasses cache (guarantees fresh data)
- **Source.DEFAULT** - Uses cache if available, falls back to server

### Benefits of This Approach
- ✅ Fresh data from server when needed
- ✅ Maintains offline cache for normal operations
- ✅ User can manually refresh if needed
- ✅ No automatic polling (saves bandwidth)
- ✅ Graceful error handling

## Files Modified

1. `app/src/main/java/dev/ml/fuelhub/data/firebase/FirebaseDataSource.kt`
   - Added `getAllTransactionsFromServer()` method

2. `app/src/main/java/dev/ml/fuelhub/domain/repository/FuelTransactionRepository.kt`
   - Added `getAllTransactionsFromServer()` interface method

3. `app/src/main/java/dev/ml/fuelhub/data/repository/FirebaseFuelTransactionRepositoryImpl.kt`
   - Implemented `getAllTransactionsFromServer()` method

4. `app/src/main/java/dev/ml/fuelhub/presentation/viewmodel/ReportsViewModel.kt`
   - Added `refreshData()` public function

5. `app/src/main/java/dev/ml/fuelhub/presentation/screen/ReportScreenEnhanced.kt`
   - Added refresh button to UI
   - Updated header to accept `onRefreshClick` callback

## Testing Checklist

- [ ] Delete a transaction in Firebase Firestore web console
- [ ] Check Reports screen - should still show deleted transaction (cached)
- [ ] Click the green Refresh button
- [ ] Wait for refresh to complete (loading indicator)
- [ ] Deleted transaction should now be gone
- [ ] Pending count should decrease accordingly

## Future Enhancements

- Auto-refresh when Reports tab is opened
- Pull-to-refresh gesture
- Periodic auto-refresh in background
- Show "Last updated" timestamp
- Cache expiration strategy
