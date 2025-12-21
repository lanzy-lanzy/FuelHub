# Firebase Firestore Migration Guide

## Overview
FuelHub has been migrated from Room Database to Firebase Firestore for cloud-based data management.

## Architecture

### Components

1. **FirebaseDataSource** (`data/firebase/FirebaseDataSource.kt`)
   - Central data access layer
   - All Firestore operations
   - Conversion utilities for data models
   - Real-time stream listeners

2. **Repository Implementations**
   - `FirebaseUserRepositoryImpl`
   - `FirebaseVehicleRepositoryImpl`
   - `FirebaseFuelWalletRepositoryImpl`
   - `FirebaseFuelTransactionRepositoryImpl`
   - `FirebaseGasSlipRepositoryImpl`
   - `FirebaseAuditLogRepositoryImpl`

3. **Dependency Injection**
   - `di/RepositoryModule.kt` - Provides repository instances

## Firestore Collections

```
drrfuel (Project ID)
├── users/
│   └── {userId}
│       ├── id: String
│       ├── username: String (unique)
│       ├── email: String
│       ├── fullName: String
│       ├── role: String (ADMIN, OFFICER, DRIVER)
│       ├── officeId: String
│       ├── isActive: Boolean
│       └── createdAt: Timestamp
│
├── vehicles/
│   └── {vehicleId}
│       ├── id: String
│       ├── plateNumber: String
│       ├── vehicleType: String
│       ├── fuelType: String (PETROL, DIESEL, etc.)
│       ├── driverName: String
│       ├── isActive: Boolean
│       └── createdAt: Timestamp
│
├── fuel_wallets/
│   └── {walletId}
│       ├── id: String
│       ├── officeId: String
│       ├── currentBalance: Double
│       └── createdAt: Timestamp
│
├── transactions/
│   └── {transactionId}
│       ├── id: String
│       ├── referenceNumber: String
│       ├── walletId: String
│       ├── vehicleId: String
│       ├── driverName: String
│       ├── vehicleType: String
│       ├── fuelType: String
│       ├── litersToPump: Double
│       ├── destination: String
│       ├── tripPurpose: String
│       ├── passengers: String (optional)
│       ├── status: String (PENDING, APPROVED, COMPLETED)
│       ├── createdBy: String
│       ├── approvedBy: String (optional)
│       ├── createdAt: Timestamp
│       ├── completedAt: Timestamp (optional)
│       └── notes: String (optional)
│
├── gas_slips/
│   └── {gasSlipId}
│       ├── id: String
│       ├── transactionId: String
│       ├── slipNumber: String
│       └── createdAt: Timestamp
│
└── audit_logs/
    └── {auditLogId}
        ├── id: String
        ├── userId: String
        ├── action: String
        ├── details: String (optional)
        └── timestamp: Timestamp
```

## Firestore Security Rules

Add these rules to Firestore Console:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users can read their own documents
    match /users/{userId} {
      allow read: if request.auth.uid == userId;
      allow write: if request.auth.uid == userId;
    }
    
    // Authenticated users can read vehicles, wallets, transactions, gas_slips
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

## Firestore Indexes

Create these composite indexes:

1. **Transactions by Wallet**
   - Collection: `transactions`
   - Fields: `walletId` (Ascending), `createdAt` (Descending)

2. **Transactions by Status**
   - Collection: `transactions`
   - Fields: `status` (Ascending), `createdAt` (Descending)

3. **Gas Slips by Transaction**
   - Collection: `gas_slips`
   - Fields: `transactionId` (Ascending), `createdAt` (Descending)

4. **Audit Logs by User**
   - Collection: `audit_logs`
   - Fields: `userId` (Ascending), `timestamp` (Descending)

## Usage Examples

### Create a User
```kotlin
val user = User(
    id = UUID.randomUUID().toString(),
    username = "john.doe",
    email = "john@example.com",
    fullName = "John Doe",
    role = UserRole.OFFICER,
    officeId = "office_001",
    createdAt = LocalDateTime.now()
)
FirebaseDataSource.createUser(user)
    .onSuccess { /* Handle success */ }
    .onFailure { /* Handle error */ }
```

### Get All Vehicles (Real-time)
```kotlin
FirebaseDataSource.getAllVehicles()
    .collect { vehicles ->
        // Update UI with vehicles
    }
```

### Get Transactions by Status
```kotlin
FirebaseDataSource.getTransactionsByStatus(TransactionStatus.PENDING)
    .collect { transactions ->
        // Update UI with pending transactions
    }
```

### Update a Transaction
```kotlin
val updated = transaction.copy(status = TransactionStatus.APPROVED)
FirebaseDataSource.updateTransaction(updated)
    .onSuccess { /* Handle success */ }
    .onFailure { /* Handle error */ }
```

## Dependency Injection Usage

In your ViewModels or use cases:

```kotlin
class TransactionViewModel : ViewModel() {
    private val transactionRepository: FuelTransactionRepository = 
        RepositoryModule.provideFuelTransactionRepository()
    
    // Use repository for all operations
    fun loadTransactions() {
        viewModelScope.launch {
            transactionRepository.getAllTransactions()
                .collect { transactions ->
                    // Update state
                }
        }
    }
}
```

## Migration from Room to Firebase

### Key Differences

1. **Asynchronous Operations**
   - Room: Synchronous queries with LiveData/Flow wrappers
   - Firebase: All operations are async by design

2. **Real-time Listeners**
   - Room: Observes local database changes
   - Firebase: Listens to cloud database changes in real-time

3. **Data Conversion**
   - Room: Automatic with type adapters
   - Firebase: Manual conversion in FirebaseDataSource

4. **Offline Support**
   - Room: Full offline capability
   - Firebase: Requires Firestore offline persistence configuration

## Firestore Offline Persistence

Enable offline persistence in your Application class:

```kotlin
class FuelHubApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Enable Firestore offline persistence
        try {
            Firebase.firestore.firestoreSettings = firestoreSettings {
                isPersistenceEnabled = true
            }
        } catch (e: Exception) {
            Timber.e(e, "Error enabling Firestore offline persistence")
        }
    }
}
```

## Advantages

1. **Cloud Storage** - Data synced across devices
2. **Real-time Updates** - Live data synchronization
3. **Scalability** - Automatic scaling
4. **Security** - Firestore security rules
5. **Analytics** - Firebase integration
6. **Offline Support** - With persistence enabled

## Performance Considerations

1. **Batch Operations** - Use batch writes for multiple documents
2. **Indexing** - Create necessary composite indexes
3. **Query Optimization** - Avoid unbounded reads
4. **Pagination** - Implement pagination for large datasets
5. **Caching** - Use local caching with offline persistence

## Troubleshooting

### Connection Issues
- Check Firebase Console for authentication
- Verify Firestore rules and indexes
- Enable offline persistence if needed

### Data Conversion Errors
- Check timestamp conversion in FirebaseDataSource
- Verify enum values match Firestore data
- Use Timber logs for debugging

### Performance Issues
- Check Firestore indexes in Console
- Monitor read/write operations
- Optimize queries with composite indexes

## Next Steps

1. Remove Room dependencies from build.gradle.kts
2. Delete Room DAOs and entity files
3. Update Application class for Firebase initialization
4. Test all CRUD operations
5. Monitor Firestore usage in Firebase Console
