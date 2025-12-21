# FuelHub Developer Guide

## Quick Start

### 1. Project Setup
The project is built with Android Compose and uses the following stack:
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Database**: Room (SQLite)
- **Architecture**: Clean Architecture
- **Async**: Coroutines
- **Logging**: Timber

### 2. Building the Project

```bash
# Clone repository
git clone <repo-url>
cd FuelHub

# Build debug APK
./gradlew assembleDebug

# Build release APK (requires signing key)
./gradlew assembleRelease

# Run tests
./gradlew test
```

### 3. Project Structure

```
app/src/main/java/dev/ml/fuelhub/
├── MainActivity.kt                  # Entry point
├── data/                           # Data layer
│   ├── database/                   # Room database
│   │   ├── FuelHubDatabase.kt     # Database singleton
│   │   ├── converter/              # Type converters
│   │   ├── dao/                    # Data Access Objects
│   │   └── entity/                 # Entity classes
│   ├── model/                      # Domain models
│   ├── repository/                 # Repository implementations
│   └── util/                       # Utilities (PDF generator)
├── domain/                         # Business logic
│   ├── exception/                  # Custom exceptions
│   ├── repository/                 # Repository interfaces
│   └── usecase/                    # Use cases
├── presentation/                   # UI layer
│   ├── screen/                     # Compose screens
│   ├── viewmodel/                  # ViewModels
│   └── state/                      # UI state classes
└── ui/                            # Theme & styling
```

---

## Core Concepts

### 1. Fuel Wallet

A wallet represents the fuel allocation (in liters) for a MDRRMO office.

```kotlin
// Create wallet
val wallet = FuelWallet(
    id = "wallet-1",
    officeId = "office-1",
    balanceLiters = 500.0,
    maxCapacityLiters = 1000.0,
    lastUpdated = LocalDateTime.now(),
    createdAt = LocalDateTime.now()
)

// Create wallet in repository
walletRepository.createWallet(wallet)

// Get wallet balance
val wallet = walletRepository.getWalletById("wallet-1")
println("Balance: ${wallet?.balanceLiters} L")

// Refill wallet
walletRepository.refillWallet("wallet-1", additionalLiters = 200.0)
```

### 2. Fuel Transaction

A transaction represents a fuel request that gets deducted from the wallet.

```kotlin
// Create transaction
val input = CreateFuelTransactionUseCase.CreateTransactionInput(
    vehicleId = "vehicle-1",
    litersToPump = 50.0,
    destination = "Hospital",
    tripPurpose = "Emergency response",
    passengers = "Driver, Medic",
    createdBy = "user-1",
    walletId = "wallet-1"
)

val output = createFuelTransactionUseCase.execute(input)
println("Transaction: ${output.transaction.referenceNumber}")
println("Gas Slip ID: ${output.gasSlip.id}")
println("New Wallet Balance: ${output.newWalletBalance} L")
```

### 3. Gas Slip

A gas slip is a printable document that can be presented at gas stations.

```kotlin
// Generate PDF
val pdfPath = pdfGenerator.generateGasSlipPdf(gasSlip)
println("PDF saved to: $pdfPath")

// Mark as used
gasSlipRepository.markAsUsed("slip-1")

// Get unused slips
val unusedSlips = gasSlipRepository.getUnusedGasSlips()
```

### 4. Audit Logging

Every wallet modification is logged for accountability.

```kotlin
// Get audit logs for wallet
val logs = auditLogRepository.getAuditLogsByWallet("wallet-1")
logs.forEach { log ->
    println("${log.action}: ${log.previousBalance} → ${log.newBalance}")
}

// Get user activity
val userLogs = auditLogRepository.getAuditLogsByUser("user-1")

// Get action history
val deductionLogs = auditLogRepository.getAuditLogsByAction("TRANSACTION_CREATED_AND_WALLET_DEDUCTED")
```

---

## Common Workflows

### Workflow 1: Create a Fuel Transaction

```kotlin
// 1. Create use case
val useCase = CreateFuelTransactionUseCase(
    walletRepository,
    transactionRepository,
    gasSlipRepository,
    auditLogRepository,
    vehicleRepository,
    userRepository
)

// 2. Prepare input
val input = CreateFuelTransactionUseCase.CreateTransactionInput(
    vehicleId = "ambulance-01",
    litersToPump = 50.0,
    destination = "General Hospital",
    tripPurpose = "Emergency medical transport",
    passengers = "Patient, Driver, Medic",
    createdBy = "dispatcher-001",
    walletId = "office-wallet-1"
)

// 3. Execute (in coroutine)
viewModelScope.launch {
    try {
        val output = useCase.execute(input)
        println("Success! Ref: ${output.transaction.referenceNumber}")
        // Display gas slip
        showGasSlip(output.gasSlip)
    } catch (e: InsufficientFuelException) {
        showError("Not enough fuel in wallet")
    } catch (e: UnauthorizedException) {
        showError("User not authorized to create transactions")
    }
}
```

### Workflow 2: Generate Daily Report

```kotlin
viewModelScope.launch {
    val reportUseCase = GenerateDailyReportUseCase(transactionRepository)
    val report = reportUseCase.execute(LocalDate.now())
    
    println("Date: ${report.date}")
    println("Total Liters: ${report.totalLitersConsumed} L")
    println("Completed: ${report.completedTransactions}")
    println("Pending: ${report.pendingTransactions}")
    println("Average: ${report.averageLitersPerTransaction} L/transaction")
}
```

### Workflow 3: Print Gas Slip

```kotlin
// Generate PDF
val pdfGenerator = GasSlipPdfGenerator(context)
val pdfPath = pdfGenerator.generateGasSlipPdf(gasSlip)

// Print via Android Print API
val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
val jobName = "Gas Slip - ${gasSlip.referenceNumber}"
val printDocumentAdapter = PrintDocumentAdapter() // Implement for PDF
printManager.print(jobName, printDocumentAdapter, PrintAttributes())

// Mark as used
gasSlipRepository.markAsUsed(gasSlip.id)
```

---

## Error Handling

### Exception Hierarchy

```
FuelHubException (base)
├── InsufficientFuelException
├── TransactionValidationException
├── UnauthorizedException
├── EntityNotFoundException
├── TransactionProcessingException
└── DatabaseException
```

### Handling Errors in UI

```kotlin
override fun execute(input: CreateTransactionInput) {
    viewModelScope.launch {
        _uiState.value = TransactionUiState.Loading
        try {
            val output = useCase.execute(input)
            _uiState.value = TransactionUiState.Success(
                output.transaction,
                output.gasSlip,
                output.newWalletBalance
            )
        } catch (e: InsufficientFuelException) {
            _uiState.value = TransactionUiState.Error("Insufficient fuel: ${e.message}")
        } catch (e: UnauthorizedException) {
            _uiState.value = TransactionUiState.Error("Unauthorized: ${e.message}")
        } catch (e: Exception) {
            _uiState.value = TransactionUiState.Error("Error: ${e.message}")
        }
    }
}
```

---

## Database Queries

### Get Transaction History for a Wallet

```kotlin
val transactions = transactionRepository.getTransactionsByWallet("wallet-1")
transactions.forEach { tx ->
    println("${tx.referenceNumber}: ${tx.litersToPump}L for ${tx.tripPurpose}")
}
```

### Get Daily Fuel Usage

```kotlin
val today = LocalDate.now()
val todayTransactions = transactionRepository.getTransactionsByDate(today)
val totalLiters = todayTransactions
    .filter { it.status == TransactionStatus.COMPLETED }
    .sumOf { it.litersToPump }
println("Today's consumption: $totalLiters L")
```

### Get Wallet Balance History

```kotlin
val auditLogs = auditLogRepository.getAuditLogsByWallet("wallet-1")
auditLogs.forEach { log ->
    println("${log.timestamp}: ${log.description}")
    println("  Before: ${log.previousBalance}L, After: ${log.newBalance}L")
}
```

### Get User Activity Report

```kotlin
val userActivity = auditLogRepository.getAuditLogsByUser("user-1")
userActivity.forEach { log ->
    println("${log.timestamp}: ${log.action} by ${log.performedBy}")
}
```

---

## Testing

### Unit Testing Repository

```kotlin
@Test
fun testWalletCreation() = runTest {
    val wallet = FuelWallet(
        id = "test-wallet",
        officeId = "test-office",
        balanceLiters = 500.0,
        maxCapacityLiters = 1000.0,
        lastUpdated = LocalDateTime.now(),
        createdAt = LocalDateTime.now()
    )
    
    val created = walletRepository.createWallet(wallet)
    assertEquals(wallet.id, created.id)
    assertEquals(wallet.balanceLiters, created.balanceLiters)
}
```

### Integration Testing Use Case

```kotlin
@Test
fun testCreateFuelTransaction() = runTest {
    // Setup mock repositories
    val mockWalletRepo = mockk<FuelWalletRepository>()
    val mockTxRepo = mockk<FuelTransactionRepository>()
    // ... other mocks
    
    val useCase = CreateFuelTransactionUseCase(
        mockWalletRepo, mockTxRepo, // ...
    )
    
    // Test execution
    val output = useCase.execute(input)
    
    // Verify behavior
    verify { mockWalletRepo.updateWallet(any()) }
    verify { mockTxRepo.createTransaction(any()) }
}
```

---

## Logging

The project uses Timber for logging. All critical operations are logged:

```kotlin
// In use cases
Timber.d("Transaction created: ${transaction.referenceNumber}")
Timber.e("Error creating transaction: ${exception.message}")

// In repositories
Timber.d("Wallet balance updated: $walletId")

// In ViewModels
Timber.i("User navigated to reports screen")
```

### View Logs

```bash
# Logcat filter for FuelHub
adb logcat | grep "dev.ml.fuelhub"

# Live logs in Android Studio
# View > Tool Windows > Logcat
```

---

## Performance Tips

### 1. Database Optimization
- Queries use indexed columns (office_id, wallet_id, reference_number)
- Avoid N+1 queries by using JOIN when possible
- Limit result sets for large tables

### 2. Coroutines
- Always use `viewModelScope` for ViewModel coroutines
- Never block the main thread with database operations
- Use `withContext(Dispatchers.IO)` for database queries

### 3. Compose UI
- Use `LazyColumn` for large lists
- Memoize expensive computations with `remember`
- Avoid unnecessary recompositions

---

## Adding New Features

### 1. Create a New Use Case

```kotlin
class MyNewUseCase(
    private val repository: MyRepository
) {
    data class Input(val param: String)
    data class Output(val result: String)
    
    suspend fun execute(input: Input): Output {
        // Implementation
        return Output(result = "Done")
    }
}
```

### 2. Create Repository Interface

```kotlin
interface MyRepository {
    suspend fun getData(id: String): Data?
    suspend fun saveData(data: Data): Data
}
```

### 3. Implement Repository

```kotlin
class MyRepositoryImpl(private val dao: MyDao) : MyRepository {
    override suspend fun getData(id: String): Data? {
        return dao.getById(id)?.toDomainModel()
    }
    
    override suspend fun saveData(data: Data): Data {
        val entity = data.toEntity()
        dao.insert(entity)
        return data
    }
}
```

### 4. Create ViewModel

```kotlin
class MyViewModel(
    private val useCase: MyNewUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState
    
    fun execute(param: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val output = useCase.execute(MyNewUseCase.Input(param))
                _uiState.value = UiState.Success(output)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
```

### 5. Create UI Screen

```kotlin
@Composable
fun MyScreen(viewModel: MyViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    
    when (uiState) {
        is UiState.Idle -> { /* show empty state */ }
        is UiState.Loading -> { /* show loader */ }
        is UiState.Success -> { /* show data */ }
        is UiState.Error -> { /* show error */ }
    }
}
```

---

## Troubleshooting

### Build Issues

**Issue**: `kapt` compilation errors
```
Solution: Clean and rebuild
./gradlew clean
./gradlew build
```

**Issue**: Room database version conflict
```
Solution: Ensure all Room dependencies match:
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")
kapt("androidx.room:room-compiler:2.6.1")
```

### Runtime Issues

**Issue**: Database schema mismatch
```
Solution: Add fallbackToDestructiveMigration() in Database
fallbackToDestructiveMigration() // Only for development!
```

**Issue**: NullPointerException on repository access
```
Solution: Ensure database is initialized in MainActivity
val database = FuelHubDatabase.getDatabase(context)
```

---

## Useful Commands

```bash
# Build debug
./gradlew assembleDebug

# Run tests
./gradlew test

# Generate APK
./gradlew assembleRelease

# Run formatter
./gradlew spotlessApply

# Check code quality
./gradlew lint

# Clear build cache
./gradlew clean
```

---

## Resources

- [Android Compose Docs](https://developer.android.com/jetpack/compose)
- [Room Database Guide](https://developer.android.com/training/data-storage/room)
- [Coroutines Guide](https://kotlinlang.org/docs/coroutines-overview.html)
- [Timber Logging](https://github.com/JakeWharton/timber)

---

## Contact & Support

For issues or questions:
1. Check existing issues in repository
2. Review SYSTEM_DESIGN.md for architecture details
3. Check IMPLEMENTATION_COMPLETE.md for feature overview
4. Review code comments and KDoc documentation
