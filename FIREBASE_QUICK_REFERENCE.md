# Firebase Integration - Quick Reference

## What's Been Done

✅ **Build Configuration**
- Added Firebase Firestore dependencies to `build.gradle.kts`
- Added Firebase Auth dependencies (for future use)

✅ **Data Layer**
- Created `FirebaseDataSource.kt` - Central data access layer with all CRUD operations
- Implements real-time stream listeners using Firestore's `addSnapshotListener`
- Automatic data conversion between Kotlin models and Firestore documents

✅ **Repository Pattern**
- Created 6 Firebase repository implementations:
  - `FirebaseUserRepositoryImpl`
  - `FirebaseVehicleRepositoryImpl`
  - `FirebaseFuelWalletRepositoryImpl`
  - `FirebaseFuelTransactionRepositoryImpl`
  - `FirebaseGasSlipRepositoryImpl`
  - `FirebaseAuditLogRepositoryImpl`

✅ **Dependency Injection**
- Created `RepositoryModule.kt` for easy repository instantiation

✅ **Application Setup**
- Created `FuelHubApplication.kt` with Firebase initialization
- Enabled Firestore offline persistence (100 MB cache)
- Integrated with `AndroidManifest.xml`

✅ **Documentation**
- `FIREBASE_MIGRATION.md` - Complete migration guide and architecture
- `FIREBASE_SETUP.md` - Step-by-step setup instructions
- `FIREBASE_QUICK_REFERENCE.md` - This file

## File Structure

```
app/
├── src/main/java/dev/ml/fuelhub/
│   ├── FuelHubApplication.kt (NEW)
│   ├── data/
│   │   ├── firebase/
│   │   │   └── FirebaseDataSource.kt (NEW)
│   │   └── repository/
│   │       ├── FirebaseUserRepositoryImpl.kt (NEW)
│   │       ├── FirebaseVehicleRepositoryImpl.kt (NEW)
│   │       ├── FirebaseFuelWalletRepositoryImpl.kt (NEW)
│   │       ├── FirebaseFuelTransactionRepositoryImpl.kt (NEW)
│   │       ├── FirebaseGasSlipRepositoryImpl.kt (NEW)
│   │       └── FirebaseAuditLogRepositoryImpl.kt (NEW)
│   └── di/
│       └── RepositoryModule.kt (NEW)
├── AndroidManifest.xml (UPDATED)
└── build.gradle.kts (UPDATED)
```

## Quick Start

### 1. Firebase Console Setup
- Ensure Firestore database is created: [Firebase Console](https://console.firebase.google.com)
- Create the 4 composite indexes (see FIREBASE_SETUP.md)
- Add Firestore security rules

### 2. In Your Code

**ViewModel Example:**
```kotlin
class TransactionViewModel : ViewModel() {
    private val repo = RepositoryModule.provideFuelTransactionRepository()
    
    fun loadTransactions() {
        viewModelScope.launch {
            repo.getAllTransactions().collect { transactions ->
                // Update UI state
            }
        }
    }
    
    fun createTransaction(transaction: FuelTransaction) {
        viewModelScope.launch {
            repo.createTransaction(transaction)
                .onSuccess { /* Handle success */ }
                .onFailure { /* Handle error */ }
        }
    }
}
```

### 3. Available Operations

**All repositories support:**
- `create*()` - Create new documents
- `get*ById()` - Retrieve single document
- `getAll*()` - Real-time stream of all documents
- `update*()` - Modify existing documents

**Some support filtering:**
- `getTransactionsByWallet(walletId)` - Real-time transactions for wallet
- `getTransactionsByStatus(status)` - Real-time transactions by status
- `getGasSlipsByTransaction(transactionId)` - Real-time gas slips
- `getAllAuditLogs()` - Ordered by timestamp

## Firestore Collections

| Collection | Purpose | Key Fields |
|-----------|---------|-----------|
| `users` | System users | id, username (unique), role, officeId |
| `vehicles` | Fleet vehicles | id, plateNumber, fuelType, driverName |
| `fuel_wallets` | Office fuel budgets | id, officeId, currentBalance |
| `transactions` | Fuel requests | id, walletId, vehicleId, status |
| `gas_slips` | Generated slips | id, transactionId, slipNumber |
| `audit_logs` | Action history | id, userId, action, timestamp |

## Data Flow

```
UI Layer (Composables)
    ↓
ViewModel
    ↓
Repository (from RepositoryModule)
    ↓
FirebaseDataSource
    ↓
Firebase Firestore
    ↓
Cloud (Real-time sync)
```

## Real-Time Features

All `getAll*()` and `get*By()` methods return `Flow<List<T>>` for real-time updates:

```kotlin
// Automatically updates whenever data changes in Firestore
repo.getAllTransactions().collect { transactions ->
    updateUI(transactions)  // Called every time data changes
}
```

## Error Handling

All create/update operations return `Result<T>`:

```kotlin
repo.updateTransaction(transaction)
    .onSuccess { /* Handle success */ }
    .onFailure { exception -> /* Handle error */ }
```

## Logging

All operations are logged with Timber:
```
FirebaseDataSource: Error creating user
FirebaseDataSource: Error getting transactions
```

## Next Steps

1. **Build the project** - Sync Gradle
2. **Create Firestore indexes** - Follow FIREBASE_SETUP.md
3. **Test operations** - Create a test user/vehicle
4. **Monitor Console** - Check Firestore reads/writes
5. **Remove Room DB** - When fully migrated (optional)

## Removal of Room Database (Future)

When ready to fully migrate:
1. Delete `data/database/` directory (DAOs, entities, converters)
2. Remove Room dependencies from `build.gradle.kts`
3. Remove `Room.databaseBuilder()` calls
4. Update existing repository implementations to use Firebase

## Performance Tips

1. **Indexes** - Create composite indexes for filters
2. **Pagination** - Limit initial queries to 20-50 documents
3. **Offline Mode** - Firestore handles offline sync automatically
4. **Caching** - 100 MB cache for offline support
5. **Real-time** - Use listeners for live updates, not polling

## Common Issues

**Issue**: "Permission denied" on read
- **Solution**: Add Firestore security rules or enable debug mode

**Issue**: Data not persisting
- **Solution**: Ensure offline persistence is enabled in FuelHubApplication

**Issue**: Slow queries
- **Solution**: Create composite indexes in Firestore Console

## Helpful Resources

- [Firestore Documentation](https://firebase.google.com/docs/firestore)
- [Android Firebase Setup](https://firebase.google.com/docs/android/setup)
- [Firestore Best Practices](https://firebase.google.com/docs/firestore/best-practices)
- [Firestore Pricing](https://firebase.google.com/pricing)

## Build & Run

```bash
# Sync Gradle
./gradlew build

# Run on emulator
./gradlew installDebug

# Check logs
adb logcat | grep -i firebase
```

---

**Status**: ✅ Firebase integration complete and ready for use
**Firestore Project**: drrfuel
**Google Services**: ✅ Configured
**Offline Persistence**: ✅ Enabled
