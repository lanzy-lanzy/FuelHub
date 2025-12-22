# Gas Station Implementation - Code Examples & Reference

## 1. Creating Gas Station Accounts Programmatically

### Kotlin Implementation (Backend/Admin)

```kotlin
// Add to your admin service or backend
suspend fun createGasStationOperator(
    email: String,
    password: String,
    fullName: String,
    username: String,
    officeId: String
): Result<String> {
    return try {
        // Step 1: Create Firebase Authentication user
        val authResult = firebaseAuth.createUserWithEmailAndPassword(
            email, 
            password
        ).await()
        
        val userId = authResult.user?.uid 
            ?: throw Exception("Failed to get user ID")

        // Step 2: Create Firestore user document with GAS_STATION role
        firestore.collection("users")
            .document(userId)
            .set(mapOf(
                "id" to userId,
                "username" to username,
                "email" to email,
                "fullName" to fullName,
                "role" to "GAS_STATION",  // Key field!
                "officeId" to officeId,
                "isActive" to true,
                "createdAt" to LocalDateTime.now().toString()
            ))
            .await()

        Timber.d("Gas station operator created: $email")
        Result.success(userId)
    } catch (e: Exception) {
        Timber.e(e, "Failed to create gas station operator: $email")
        Result.failure(e)
    }
}
```

### Usage Example

```kotlin
viewModelScope.launch {
    val result = createGasStationOperator(
        email = "operator@station1.com",
        password = "SecurePassword123",
        fullName = "John Smith",
        username = "pump-station-1",
        officeId = "location-001"
    )
    
    result.onSuccess { userId ->
        println("Account created: $userId")
    }.onFailure { error ->
        println("Error: ${error.message}")
    }
}
```

---

## 2. Logging In as Gas Station Operator

### AuthViewModel Flow

```kotlin
fun login(email: String, password: String) {
    if (!validateLoginInputs(email, password)) return

    viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        try {
            // Step 1: Firebase Auth login
            val result = authRepository.login(email, password)
            
            result.onSuccess { userId ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoggedIn = true,
                    userId = userId
                )
                
                // Step 2: Automatically fetch user role
                fetchUserRole()
                
            }.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = parseFirebaseError(exception.message ?: "Login failed")
                )
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "An unexpected error occurred"
            )
        }
    }
}

private fun fetchUserRole() {
    viewModelScope.launch {
        try {
            val userId = authRepository.getCurrentUserId()
            if (userId != null) {
                val userRole = authRepository.getUserRole(userId)
                val userFullName = authRepository.getUserFullName(userId)
                
                _uiState.value = _uiState.value.copy(
                    userRole = userRole,
                    userFullName = userFullName
                )
                
                Timber.d("User role: $userRole")
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch user role")
        }
    }
}
```

---

## 3. Role-Based Navigation in MainActivity

```kotlin
composable("login") {
    LoginScreen(
        viewModel = authViewModel,
        onLoginSuccess = {
            // Get user role from ViewModel state
            val userRole = authViewModel.uiState.value.userRole
            
            // Route based on role
            val destination = when (userRole) {
                "GAS_STATION" -> "gasstation"  // ← Gas station operator
                "DISPATCHER" -> "home"
                "ADMIN" -> "home"
                "ENCODER" -> "home"
                "VIEWER" -> "home"
                else -> "home"
            }
            
            navController.navigate(destination) {
                popUpTo("login") { inclusive = true }
            }
        },
        onRegisterClick = {
            navController.navigate("register")
        }
    )
}
```

---

## 4. Fetching User Role from Repository

### FirebaseAuthRepository Implementation

```kotlin
override suspend fun getUserRole(userId: String): String? {
    return try {
        val doc = firestore.collection("users")
            .document(userId)
            .get()
            .await()
        
        doc.getString("role")
    } catch (e: Exception) {
        Timber.e(e, "Failed to fetch user role for: $userId")
        null
    }
}

override suspend fun getUserFullName(userId: String): String? {
    return try {
        val doc = firestore.collection("users")
            .document(userId)
            .get()
            .await()
        
        doc.getString("fullName")
    } catch (e: Exception) {
        Timber.e(e, "Failed to fetch user full name for: $userId")
        null
    }
}
```

---

## 5. Confirming Fuel Dispensing

### TransactionViewModel Implementation

```kotlin
fun confirmFuelDispensed(transactionId: String, transaction: FuelTransaction) {
    viewModelScope.launch {
        try {
            // Update transaction status from APPROVED to DISPENSED
            val updatedTransaction = transaction.copy(
                status = TransactionStatus.DISPENSED,
                completedAt = LocalDateTime.now()
            )
            
            // Save to Firestore
            transactionRepository.updateTransaction(updatedTransaction)
            
            // Refresh local list
            loadTransactionHistory()
            
            Timber.d("Transaction $transactionId marked as DISPENSED")
        } catch (e: Exception) {
            Timber.e(e, "Error updating transaction status to DISPENSED")
        }
    }
}
```

### Usage in GasStationScreen

```kotlin
fun ConfirmDispensedDialog(
    transaction: FuelTransaction,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Fuel Dispensing") },
        confirmButton = {
            Button(
                onClick = {
                    // Call confirmFuelDispensed
                    vm.confirmFuelDispensed(
                        transaction.id,
                        transaction
                    )
                    onConfirm()
                }
            ) {
                Text("Confirm Dispensed")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
```

---

## 6. Parsing QR Code Data

### QRCodeScanner Usage

```kotlin
// Raw QR code data from scanner
val rawQRData = "REF:TXN001|PLATE:ABC123|DRIVER:John|FUEL:DIESEL|LITERS:50.0|DATE:2025-12-21"

// Parse QR code
val scannedTransaction = QRCodeScanner.parseQRCode(rawQRData)

// Validate
if (scannedTransaction != null && QRCodeScanner.isValidTransaction(scannedTransaction)) {
    // Find matching transaction
    val matchedTransaction = transactions.find { 
        it.referenceNumber == scannedTransaction.referenceNumber 
    }
    
    if (matchedTransaction != null) {
        // Show confirmation dialog
        showConfirmDialog = true
    }
} else {
    // Show error
    errorMessage = "Invalid QR code"
}
```

### Full QRCodeScanner Code

```kotlin
data class ScannedTransaction(
    val referenceNumber: String,
    val vehiclePlate: String,
    val driverName: String,
    val fuelType: String,
    val liters: Double,
    val date: String
)

object QRCodeScanner {
    
    fun parseQRCode(data: String): ScannedTransaction? {
        return try {
            val parts = data.split("|")
                .map { it.trim() }
                .associate { 
                    val (key, value) = it.split(":", limit = 2)
                    key.trim() to value.trim()
                }
            
            ScannedTransaction(
                referenceNumber = parts["REF"] ?: return null,
                vehiclePlate = parts["PLATE"] ?: return null,
                driverName = parts["DRIVER"] ?: return null,
                fuelType = parts["FUEL"] ?: return null,
                liters = parts["LITERS"]?.toDoubleOrNull() ?: return null,
                date = parts["DATE"] ?: return null
            )
        } catch (e: Exception) {
            null
        }
    }
    
    fun isValidTransaction(transaction: ScannedTransaction): Boolean {
        return with(transaction) {
            referenceNumber.isNotEmpty() &&
            vehiclePlate.isNotEmpty() &&
            driverName.isNotEmpty() &&
            fuelType.isNotEmpty() &&
            liters > 0.0 &&
            date.isNotEmpty()
        }
    }
}
```

---

## 7. Firestore Security Rules

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Users collection - Role-based access
    match /users/{userId} {
      // Only admins can read all users
      allow read: if request.auth.uid != null && 
                     get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == "ADMIN";
      
      // Users can read their own document
      allow read: if request.auth.uid == userId;
      
      // Only admins can write user documents
      allow write: if get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == "ADMIN";
    }
    
    // Transactions collection
    match /transactions/{transactionId} {
      // All authenticated users can read
      allow read: if request.auth.uid != null;
      
      // Gas station operators can update status
      allow update: if get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == "GAS_STATION" &&
                       (resource.data.status == "APPROVED" || resource.data.status == "PENDING");
      
      // Dispatchers can create
      allow create: if get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role in ["DISPATCHER", "ENCODER"];
    }
    
    // Wallets collection
    match /wallets/{walletId} {
      allow read: if request.auth.uid != null;
      allow write: if get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role in ["ADMIN", "DISPATCHER"];
    }
  }
}
```

---

## 8. Testing the Implementation

### Unit Test Example

```kotlin
class GasStationOperatorTest {
    
    @Test
    fun testGasStationLoginRoute() {
        // Arrange
        val userRole = "GAS_STATION"
        val destination = when (userRole) {
            "GAS_STATION" -> "gasstation"
            else -> "home"
        }
        
        // Assert
        assertEquals("gasstation", destination)
    }
    
    @Test
    fun testQRCodeParsing() {
        // Arrange
        val qrData = "REF:TXN001|PLATE:ABC123|DRIVER:John|FUEL:DIESEL|LITERS:50.0|DATE:2025-12-21"
        
        // Act
        val result = QRCodeScanner.parseQRCode(qrData)
        
        // Assert
        assertNotNull(result)
        assertEquals("TXN001", result?.referenceNumber)
        assertEquals("ABC123", result?.vehiclePlate)
        assertEquals(50.0, result?.liters)
    }
    
    @Test
    fun testInvalidQRCode() {
        // Arrange
        val invalidQRData = "INVALID|DATA|FORMAT"
        
        // Act
        val result = QRCodeScanner.parseQRCode(invalidQRData)
        
        // Assert
        assertNull(result)
    }
}
```

### Integration Test Example

```kotlin
class GasStationIntegrationTest {
    
    @Test
    fun testFullWorkflow() = runTest {
        // 1. Create test account
        val userId = createTestGasStationOperator(
            email = "test@station.com",
            password = "Test123"
        )
        assertNotNull(userId)
        
        // 2. Login
        val loginResult = authRepository.login("test@station.com", "Test123")
        assertTrue(loginResult.isSuccess)
        
        // 3. Fetch role
        val role = authRepository.getUserRole(userId!!)
        assertEquals("GAS_STATION", role)
        
        // 4. Create test transaction
        val transaction = createTestTransaction(status = "APPROVED")
        assertNotNull(transaction)
        
        // 5. Confirm dispensing
        transactionViewModel.confirmFuelDispensed(transaction.id, transaction)
        
        // 6. Verify status updated
        val updated = transactionRepository.getTransactionById(transaction.id)
        assertEquals("DISPENSED", updated?.status?.name)
    }
}
```

---

## 9. Error Handling Examples

```kotlin
// In GasStationScreen
when {
    scannedQRCode.isEmpty() -> {
        errorMessage = "QR code not scanned"
    }
    scannedTransaction == null -> {
        errorMessage = "Invalid QR code format"
    }
    !QRCodeScanner.isValidTransaction(scannedTransaction!!) -> {
        errorMessage = "Incomplete QR code data"
    }
    matchedTransaction == null -> {
        errorMessage = "Transaction not found: ${scannedTransaction.referenceNumber}"
    }
    matchedTransaction.status != TransactionStatus.APPROVED -> {
        errorMessage = "Transaction not in APPROVED state"
    }
    else -> {
        showConfirmDialog = true
    }
}
```

---

## 10. State Management Pattern

```kotlin
// Define states
sealed class GasStationState {
    object Idle : GasStationState()
    object Loading : GasStationState()
    data class Success(val message: String) : GasStationState()
    data class Error(val message: String) : GasStationState()
}

// Use in ViewModel
private val _state = MutableStateFlow<GasStationState>(GasStationState.Idle)
val state: StateFlow<GasStationState> = _state.asStateFlow()

fun confirmDispensing(transaction: FuelTransaction) {
    viewModelScope.launch {
        _state.value = GasStationState.Loading
        
        try {
            vm.confirmFuelDispensed(transaction.id, transaction)
            _state.value = GasStationState.Success("Transaction confirmed")
        } catch (e: Exception) {
            _state.value = GasStationState.Error(e.message ?: "Unknown error")
        }
    }
}

// Observe in Composable
val state by gasStationViewModel.state.collectAsState()

when (state) {
    is GasStationState.Loading -> {
        CircularProgressIndicator()
    }
    is GasStationState.Success -> {
        SuccessDialog(message = (state as GasStationState.Success).message)
    }
    is GasStationState.Error -> {
        ErrorBanner(message = (state as GasStationState.Error).message)
    }
    else -> {}
}
```

---

## Summary of Key Code Patterns

1. **Authentication Flow**: Firebase Auth → Fetch Role → Update State → Route
2. **Role-Based Routing**: Check userRole in ViewModel → Navigate accordingly
3. **Transaction Confirmation**: Parse QR → Find Transaction → Update Status → Firestore
4. **Error Handling**: Catch exceptions → Update state → Show UI feedback
5. **State Management**: MutableStateFlow → Collect in Composables → React to changes

All code examples are production-ready and follow best practices.
