# Firebase Firestore - Quick Start Guide

## What Was Changed

✅ **Completely replaced Room Database with Firebase Firestore**

- Driver/User Management → Firebase `users` collection
- Vehicle Management → Firebase `vehicles` collection
- Wallet Management → Firebase `fuel_wallets` collection
- Transaction Creation → Firebase `transactions` collection
- Gas Slip Generation → Firebase `gas_slips` collection
- Audit Logs → Firebase `audit_logs` collection

## Key Files Created

### 1. Firebase Repositories (6 files)
```
data/repository/Firebase[Entity]RepositoryImpl.kt
```
- FirebaseUserRepositoryImpl.kt
- FirebaseVehicleRepositoryImpl.kt
- FirebaseFuelWalletRepositoryImpl.kt
- FirebaseFuelTransactionRepositoryImpl.kt
- FirebaseGasSlipRepositoryImpl.kt
- FirebaseAuditLogRepositoryImpl.kt

### 2. Core Infrastructure
- `FirebaseDataSource.kt` - Central database access layer
- `RepositoryModule.kt` - Updated DI configuration
- `FuelHubApplication.kt` - Firebase initialization
- `MainActivity.kt` - Updated with DI

## Using the System

### Creating a Driver
```kotlin
val userRepository = RepositoryModule.provideUserRepository()
val newUser = User(
    id = UUID.randomUUID().toString(),
    username = "driver01",
    email = "driver@example.com",
    fullName = "John Driver",
    role = UserRole.DRIVER,
    officeId = "office001",
    isActive = true,
    createdAt = LocalDateTime.now()
)
val createdUser = userRepository.createUser(newUser)
```

### Managing Vehicles
```kotlin
val vehicleRepository = RepositoryModule.provideVehicleRepository()
val newVehicle = Vehicle(
    id = UUID.randomUUID().toString(),
    plateNumber = "ABC-123",
    vehicleType = "Bus",
    fuelType = FuelType.DIESEL,
    driverName = "John Driver",
    isActive = true,
    createdAt = LocalDateTime.now()
)
val createdVehicle = vehicleRepository.createVehicle(newVehicle)
```

### Managing Wallets
```kotlin
val walletRepository = RepositoryModule.provideFuelWalletRepository()
val refilled = walletRepository.refillWallet("wallet001", 100.0)
// Automatically respects max capacity constraints
```

### Creating Transactions
```kotlin
val transactionRepository = RepositoryModule.provideFuelTransactionRepository()
val transaction = FuelTransaction(
    id = UUID.randomUUID().toString(),
    referenceNumber = "TXN-2024-001",
    walletId = "wallet001",
    vehicleId = "vehicle001",
    driverName = "John Driver",
    vehicleType = "Bus",
    fuelType = FuelType.DIESEL,
    litersToPump = 50.0,
    destination = "City Center",
    tripPurpose = "Official Travel",
    status = TransactionStatus.PENDING,
    createdBy = "admin001",
    createdAt = LocalDateTime.now()
)
val created = transactionRepository.createTransaction(transaction)
```

### Generating Gas Slips
```kotlin
val gasSlipRepository = RepositoryModule.provideGasSlipRepository()
val gasSlip = GasSlip(
    id = UUID.randomUUID().toString(),
    transactionId = transaction.id,
    referenceNumber = "GAS-2024-001",
    driverName = transaction.driverName,
    vehicleType = transaction.vehicleType,
    vehiclePlateNumber = "ABC-123",
    destination = transaction.destination,
    tripPurpose = transaction.tripPurpose,
    passengers = "3 persons",
    fuelType = transaction.fuelType,
    litersToPump = transaction.litersToPump,
    transactionDate = LocalDateTime.now(),
    mdrrmoOfficeId = "office001",
    mdrrmoOfficeName = "MDRRMO Office",
    generatedAt = LocalDateTime.now(),
    isUsed = false
)
val created = gasSlipRepository.createGasSlip(gasSlip)

// Mark as used when dispensed
val used = gasSlipRepository.markAsUsed(gasSlip.id)
```

### Audit Logging
```kotlin
val auditLogRepository = RepositoryModule.provideAuditLogRepository()
auditLogRepository.logAction(
    walletId = "wallet001",
    action = "REFILL",
    transactionId = null,
    performedBy = "admin001",
    previousBalance = 100.0,
    newBalance = 200.0,
    litersDifference = 100.0,
    description = "Wallet refilled by admin",
    ipAddress = "192.168.1.1"
)
```

## Firestore Collections

### users
```
{
  id: "user-001",
  username: "driver01",
  email: "driver@email.com",
  fullName: "John Driver",
  role: "DRIVER",
  officeId: "office001",
  isActive: true,
  createdAt: timestamp
}
```

### vehicles
```
{
  id: "vehicle-001",
  plateNumber: "ABC-123",
  vehicleType: "Bus",
  fuelType: "DIESEL",
  driverName: "John Driver",
  isActive: true,
  createdAt: timestamp
}
```

### fuel_wallets
```
{
  id: "wallet-001",
  officeId: "office001",
  balanceLiters: 500.0,
  maxCapacityLiters: 1000.0,
  lastUpdated: timestamp,
  createdAt: timestamp
}
```

### transactions
```
{
  id: "txn-001",
  referenceNumber: "TXN-2024-001",
  walletId: "wallet-001",
  vehicleId: "vehicle-001",
  driverName: "John Driver",
  vehicleType: "Bus",
  fuelType: "DIESEL",
  litersToPump: 50.0,
  destination: "City Center",
  tripPurpose: "Official Travel",
  passengers: "3 persons",
  status: "PENDING",
  createdBy: "admin001",
  approvedBy: null,
  createdAt: timestamp,
  completedAt: null,
  notes: null
}
```

### gas_slips
```
{
  id: "slip-001",
  transactionId: "txn-001",
  referenceNumber: "GAS-2024-001",
  driverName: "John Driver",
  vehicleType: "Bus",
  vehiclePlateNumber: "ABC-123",
  destination: "City Center",
  tripPurpose: "Official Travel",
  passengers: "3 persons",
  fuelType: "DIESEL",
  litersToPump: 50.0,
  transactionDate: timestamp,
  mdrrmoOfficeId: "office001",
  mdrrmoOfficeName: "MDRRMO Office",
  generatedAt: timestamp,
  isUsed: false,
  usedAt: null
}
```

### audit_logs
```
{
  id: "audit-001",
  walletId: "wallet-001",
  action: "REFILL",
  transactionId: null,
  performedBy: "admin001",
  previousBalance: 100.0,
  newBalance: 200.0,
  litersDifference: 100.0,
  description: "Wallet refilled",
  timestamp: timestamp,
  ipAddress: "192.168.1.1"
}
```

## Setup Steps

### 1. Firebase Project
```
1. Go to firebase.google.com
2. Create new project "drrfuel"
3. Enable Firestore Database
4. Download google-services.json
5. Place in app/ folder
```

### 2. Build the App
```bash
./gradlew clean build
```

### 3. Create Collections in Firestore
- Create empty collections: users, vehicles, fuel_wallets, transactions, gas_slips, audit_logs

### 4. Create Indexes
In Firebase Console → Firestore → Indexes:
- `transactions`: walletId + status (Ascending, Ascending)
- `transactions`: walletId + createdAt (Ascending, Descending)
- `gas_slips`: transactionId (Ascending)
- `audit_logs`: walletId + timestamp (Ascending, Descending)

### 5. Set Security Rules
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

### 6. Run the App
```bash
./gradlew installDebug
```

## Important Features

### ✅ Offline Support
- App works offline
- Changes sync automatically when online
- 100 MB local cache

### ✅ Real-Time Updates
- All data syncs instantly
- Multiple devices stay in sync
- Flow-based reactive updates

### ✅ Data Safety
- Atomic wallet updates
- Immutable audit logs
- Capacity constraints enforced
- Status validation

### ✅ Error Handling
- All operations wrapped in Result
- Proper exception logging
- Graceful error messages

## Common Operations

### Get User
```kotlin
val user = userRepository.getUserById("user-001")
val user = userRepository.getUserByUsername("driver01")
```

### Update User
```kotlin
val updated = userRepository.updateUser(user.copy(fullName = "Jane Driver"))
```

### Deactivate User
```kotlin
userRepository.deactivateUser("user-001")
```

### Get Wallet
```kotlin
val wallet = walletRepository.getWalletById("wallet-001")
```

### Check Balance
```kotlin
val balance = wallet.balanceLiters
```

### Get Transactions
```kotlin
val pending = transactionRepository.getTransactionsByStatus(TransactionStatus.PENDING)
val byWallet = transactionRepository.getTransactionsByWallet("wallet-001")
```

### Approve Transaction
```kotlin
val approved = transaction.copy(
    status = TransactionStatus.APPROVED,
    approvedBy = "approver-001"
)
transactionRepository.updateTransaction(approved)
```

### Cancel Transaction
```kotlin
transactionRepository.cancelTransaction("txn-001") // Only if PENDING
```

## Troubleshooting

### Compilation Errors
- Ensure google-services.json is in app/ folder
- Run `./gradlew clean build`

### Runtime Errors
- Check Firestore security rules
- Verify collections exist
- Check internet connection

### Data Not Syncing
- Ensure authentication is configured
- Check Firebase console for errors
- Verify indexes are created

### Offline Issues
- Clear app cache
- Reinstall app
- Check persistence settings

## Documentation

For detailed information, see:
- `FIREBASE_COMPLETE_MIGRATION.md` - Full migration guide
- `FIREBASE_MIGRATION_SUMMARY.md` - Architecture overview
- `FIRESTORE_IMPLEMENTATION_CHECKLIST.md` - Testing procedures
- `FIREBASE_FILES_CREATED.md` - File structure

## Key Differences from Room

| Feature | Room | Firestore |
|---------|------|-----------|
| Storage | Local SQLite | Cloud Database |
| Sync | Manual | Automatic |
| Offline | No | Yes |
| Real-Time | No | Yes |
| Scalability | Limited | Unlimited |
| Cost | Free | Pay per use |
| Maintenance | Manual | Automatic |
| Global Access | No | Yes |
| Backups | Manual | Automatic |
| Authentication | N/A | Built-in |

## Support

Need help? Check:
1. Firebase documentation: https://firebase.google.com/docs/firestore
2. Code comments in Firebase implementations
3. Domain repository interfaces for expected behavior
4. Firestore security rules documentation

## Next Steps

- [ ] Set up Firebase project
- [ ] Download google-services.json
- [ ] Create Firestore collections
- [ ] Create indexes
- [ ] Build and run app
- [ ] Test CRUD operations
- [ ] Verify offline support
- [ ] Monitor Firestore usage

Good to go! 🚀
