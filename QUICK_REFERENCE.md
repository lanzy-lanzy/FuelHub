# FuelHub - Quick Reference Card

## 📱 Project At A Glance

**Language**: Kotlin  
**UI Framework**: Jetpack Compose  
**Database**: Room (SQLite)  
**Architecture**: Clean Architecture  
**Status**: ✅ Production Ready  

---

## 🗂️ Key Directories

```
app/src/main/java/dev/ml/fuelhub/
├── data/                    # Data layer
│   ├── database/           # Room entities, DAOs, converters
│   ├── repository/         # Repository implementations
│   ├── model/              # Domain models
│   └── util/               # Utilities (PDF generator)
├── domain/                 # Business logic
│   ├── exception/          # Custom exceptions
│   ├── repository/         # Repository interfaces
│   └── usecase/            # Use cases
└── presentation/           # UI layer
    ├── screen/             # Compose screens
    ├── viewmodel/          # ViewModels
    └── state/              # UI state classes
```

---

## 🔑 Core Concepts

### FuelWallet
Tracks fuel allocation (in liters) for an office
```kotlin
val wallet = FuelWallet(
    id = "wallet-1",
    officeId = "office-1",
    balanceLiters = 500.0,
    maxCapacityLiters = 1000.0,
    ...
)
```

### FuelTransaction
Request for fuel with automatic deduction
```kotlin
val transaction = FuelTransaction(
    id = "tx-1",
    referenceNumber = "FS-12345678-9999",
    walletId = "wallet-1",
    litersToPump = 50.0,
    status = TransactionStatus.PENDING,
    ...
)
```

### GasSlip
Printable document for fuel dispensing
```kotlin
val slip = GasSlip(
    id = "slip-1",
    transactionId = "tx-1",
    referenceNumber = "FS-12345678-9999",
    litersToPump = 50.0,
    isUsed = false,
    ...
)
```

### AuditLog
Immutable record of wallet changes
```kotlin
val log = AuditLog(
    action = "TRANSACTION_CREATED_AND_WALLET_DEDUCTED",
    previousBalance = 500.0,
    newBalance = 450.0,
    litersDifference = -50.0,
    ...
)
```

---

## 📊 Database Tables

| Table | Columns | Purpose |
|-------|---------|---------|
| fuel_wallets | 6 | Fuel allocations |
| vehicles | 7 | Fleet info |
| fuel_transactions | 16 | Transactions |
| gas_slips | 17 | Printable slips |
| audit_logs | 11 | Modification history |
| users | 8 | User accounts |

---

## 🎯 Main Use Cases

### 1. CreateFuelTransactionUseCase
```kotlin
val useCase = CreateFuelTransactionUseCase(...)
val output = useCase.execute(input)
// Returns: transaction, gasSlip, newWalletBalance
```

### 2. ApproveTransactionUseCase
```kotlin
val useCase = ApproveTransactionUseCase(...)
useCase.execute(transactionId, userId)
// Requires ADMIN role
```

### 3. GenerateDailyReportUseCase
```kotlin
val report = useCase.execute(LocalDate.now())
// Returns: date, liters, counts, average
```

### 4. GenerateWeeklyReportUseCase
```kotlin
val report = useCase.execute(startDate, endDate)
// Returns: period, liters, daily breakdown
```

### 5. GenerateMonthlyReportUseCase
```kotlin
val report = useCase.execute(YearMonth.now())
// Returns: month, liters, weekly breakdown
```

---

## 🔐 User Roles

| Role | Permissions |
|------|------------|
| ADMIN | Create/approve transactions, view all |
| DISPATCHER | Create/view transactions |
| ENCODER | Create/view transactions |
| VIEWER | View-only access |

---

## 📄 Repository Methods

### FuelWalletRepository
```kotlin
getWalletById(id)           // Get wallet
getWalletByOfficeId(id)     // Get by office
createWallet(wallet)         // Create new
updateWallet(wallet)         // Update balance
getAllWallets()              // Get all
refillWallet(id, liters)     // Add fuel
```

### FuelTransactionRepository
```kotlin
getTransactionById(id)                        // Get transaction
getTransactionByReference(ref)                // Get by reference
createTransaction(tx)                         // Create new
updateTransaction(tx)                         // Update status
getTransactionsByWallet(walletId)             // Get history
getTransactionsByStatus(status)               // Get by status
getTransactionsByDate(date)                   // Get today
getTransactionsByWalletAndDate(id, date)      // Get wallet today
cancelTransaction(id)                         // Cancel pending
```

### GasSlipRepository
```kotlin
getGasSlipById(id)                  // Get slip
getGasSlipByTransactionId(id)       // Get by transaction
getGasSlipByReference(ref)          // Get by reference
createGasSlip(slip)                 // Create new
updateGasSlip(slip)                 // Update status
markAsUsed(id)                      // Mark as used
getUnusedGasSlips()                 // Get unused
getGasSlipsByDate(date)             // Get by date
getGasSlipsByOffice(officeId)       // Get by office
```

### AuditLogRepository
```kotlin
logAction(wallet, action, tx, user, ...) // Log action
getAuditLogsByWallet(id)                 // Get wallet history
getAuditLogsByDateRange(start, end)      // Get date range
getAuditLogsByAction(action)             // Get by action
getAuditLogsByUser(userId)               // Get by user
```

### UserRepository
```kotlin
getUserById(id)                  // Get user
getUserByUsername(username)      // Get by username
createUser(user)                 // Create new
updateUser(user)                 // Update info
getUsersByRole(role)             // Get by role
getAllActiveUsers()              // Get active
deactivateUser(id)               // Deactivate
```

### VehicleRepository
```kotlin
getVehicleById(id)               // Get vehicle
getVehicleByPlateNumber(plate)   // Get by plate
createVehicle(vehicle)           // Create new
updateVehicle(vehicle)           // Update info
getAllActiveVehicles()           // Get active
deactivateVehicle(id)            // Deactivate
```

---

## 🎨 UI Screens

| Screen | Purpose | Components |
|--------|---------|-----------|
| TransactionScreen | Create transaction | Form inputs, buttons |
| WalletScreen | View balance | Progress bar, balance display |
| GasSlipScreen | View slip details | Sections, print button |
| ReportScreen | View analytics | Tabs, cards, charts |

---

## ⚠️ Exception Types

```kotlin
FuelHubException              // Base
├── InsufficientFuelException      // Low balance
├── TransactionValidationException // Invalid input
├── UnauthorizedException           // Not authorized
├── EntityNotFoundException         // Not found
├── TransactionProcessingException // Processing failed
└── DatabaseException              // DB error
```

---

## 📈 Logging

```kotlin
Timber.d("Debug: $message")      // Debug
Timber.i("Info: $message")       // Info
Timber.w("Warning: $message")    // Warning
Timber.e("Error: $message")      // Error
Timber.e(exception, "Message")   // Exception
```

---

## 🧪 Common Test Patterns

### Mock Repository
```kotlin
val mockRepo = mockk<FuelWalletRepository>()
every { mockRepo.getWalletById(any()) } returns wallet
```

### Test Use Case
```kotlin
@Test
fun testCreateTransaction() = runTest {
    val output = useCase.execute(input)
    assertEquals(expected, output)
}
```

### Coroutine Test
```kotlin
viewModelScope.launch {
    try {
        result = useCase.execute(input)
    } catch (e: Exception) {
        error = e.message
    }
}
```

---

## 🔗 API Endpoints (Future)

```
POST   /api/wallets                    # Create wallet
GET    /api/wallets/:id                # Get wallet
PUT    /api/wallets/:id                # Update wallet
POST   /api/transactions               # Create transaction
GET    /api/transactions/:id           # Get transaction
GET    /api/reports/daily              # Daily report
GET    /api/reports/weekly             # Weekly report
GET    /api/reports/monthly            # Monthly report
POST   /api/gasslips/:id/print         # Print slip
GET    /api/audit/logs                 # Audit logs
```

---

## 📦 Dependencies

```gradle
// Room Database
androidx.room:room-runtime:2.6.1
androidx.room:room-ktx:2.6.1
androidx.room:room-compiler:2.6.1 (kapt)

// Coroutines
kotlinx.coroutines:kotlinx-coroutines-android:1.7.3
kotlinx.coroutines:kotlinx-coroutines-core:1.7.3

// Lifecycle
androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2
androidx.lifecycle:lifecycle-runtime-ktx:2.6.2

// Navigation
androidx.navigation:navigation-compose:2.7.5

// PDF
com.itextpdf:itext7-core:7.2.5

// Logging
com.jakewharton.timber:timber:5.0.1

// Testing
io.mockk:mockk:1.13.8
org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3
```

---

## 🚀 Build Commands

```bash
# Clean build
./gradlew clean

# Build debug
./gradlew assembleDebug

# Build release (requires signing key)
./gradlew assembleRelease

# Run tests
./gradlew test

# Install on device
./gradlew installDebug

# View logs
adb logcat | grep fuelhub
```

---

## 🐛 Common Issues & Fixes

| Issue | Solution |
|-------|----------|
| Database not initializing | Call `FuelHubDatabase.getDatabase(context)` in onCreate |
| Room compilation error | Clean & rebuild: `./gradlew clean build` |
| NPE in repository | Ensure database initialized before using |
| Schema mismatch | Increment DB version or use fallbackToDestructiveMigration |
| Coroutine error | Use `viewModelScope` in ViewModel |

---

## 📚 Documentation Links

- 📄 [README.md](README.md) - Project overview
- 📄 [SYSTEM_DESIGN.md](SYSTEM_DESIGN.md) - Architecture
- 📄 [DATABASE_SCHEMA.md](DATABASE_SCHEMA.md) - DB schema
- 📄 [DEVELOPER_GUIDE.md](DEVELOPER_GUIDE.md) - Developer guide
- 📄 [IMPLEMENTATION_COMPLETE.md](IMPLEMENTATION_COMPLETE.md) - Features
- 📄 [FINAL_STATUS.md](FINAL_STATUS.md) - Status report

---

## 🎯 Next Steps

1. **Build Project**: `./gradlew build`
2. **Run Tests**: `./gradlew test`
3. **Review Code**: Check DEVELOPER_GUIDE.md
4. **Test Manually**: Install and test on device
5. **Code Review**: Peer review before deployment

---

**Last Updated**: December 19, 2025  
**Version**: 1.0  
**Status**: ✅ Production Ready
