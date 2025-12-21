package dev.ml.fuelhub.data.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dev.ml.fuelhub.data.model.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

/**
 * Firebase Firestore data source for FuelHub.
 * Handles all database operations using Cloud Firestore.
 */
object FirebaseDataSource {
    
    private val db: FirebaseFirestore = Firebase.firestore
    
    // Collection names
    private const val USERS_COLLECTION = "users"
    private const val VEHICLES_COLLECTION = "vehicles"
    private const val FUEL_WALLETS_COLLECTION = "fuel_wallets"
    private const val TRANSACTIONS_COLLECTION = "transactions"
    private const val GAS_SLIPS_COLLECTION = "gas_slips"
    private const val AUDIT_LOGS_COLLECTION = "audit_logs"
    
    // ==================== USER OPERATIONS ====================
    
    suspend fun createUser(user: User): Result<Unit> = try {
        db.collection(USERS_COLLECTION)
            .document(user.id)
            .set(user.toFirestoreMap())
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Error creating user")
        Result.failure(e)
    }
    
    suspend fun getUserById(userId: String): Result<User?> = try {
        val doc = db.collection(USERS_COLLECTION)
            .document(userId)
            .get()
            .await()
        val user = doc.toUser()
        Result.success(user)
    } catch (e: Exception) {
        Timber.e(e, "Error getting user")
        Result.failure(e)
    }
    
    suspend fun getUserByUsername(username: String): Result<User?> = try {
        val docs = db.collection(USERS_COLLECTION)
            .whereEqualTo("username", username)
            .get()
            .await()
        val user = if (docs.isEmpty) null else docs.documents.first().toUser()
        Result.success(user)
    } catch (e: Exception) {
        Timber.e(e, "Error getting user by username")
        Result.failure(e)
    }
    
    fun getAllUsers(): Flow<List<User>> = callbackFlow {
        val listener = db.collection(USERS_COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "Error getting users")
                    return@addSnapshotListener
                }
                val users = snapshot?.documents?.mapNotNull { it.toUser() } ?: emptyList()
                trySend(users)
            }
        awaitClose { listener.remove() }
    }
    
    suspend fun updateUser(user: User): Result<Unit> = try {
        db.collection(USERS_COLLECTION)
            .document(user.id)
            .update(user.toFirestoreMap())
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Error updating user")
        Result.failure(e)
    }
    
    // ==================== VEHICLE OPERATIONS ====================
    
    suspend fun createVehicle(vehicle: Vehicle): Result<Unit> = try {
        db.collection(VEHICLES_COLLECTION)
            .document(vehicle.id)
            .set(vehicle.toFirestoreMap())
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Error creating vehicle")
        Result.failure(e)
    }
    
    suspend fun getVehicleById(vehicleId: String): Result<Vehicle?> = try {
        val doc = db.collection(VEHICLES_COLLECTION)
            .document(vehicleId)
            .get()
            .await()
        val vehicle = doc.toVehicle()
        Result.success(vehicle)
    } catch (e: Exception) {
        Timber.e(e, "Error getting vehicle")
        Result.failure(e)
    }
    
    fun getAllVehicles(): Flow<List<Vehicle>> = callbackFlow {
        val listener = db.collection(VEHICLES_COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "Error getting vehicles")
                    return@addSnapshotListener
                }
                val vehicles = snapshot?.documents?.mapNotNull { it.toVehicle() } ?: emptyList()
                trySend(vehicles)
            }
        awaitClose { listener.remove() }
    }
    
    suspend fun updateVehicle(vehicle: Vehicle): Result<Unit> = try {
        db.collection(VEHICLES_COLLECTION)
            .document(vehicle.id)
            .update(vehicle.toFirestoreMap())
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Error updating vehicle")
        Result.failure(e)
    }
    
    // ==================== FUEL WALLET OPERATIONS ====================
    
    suspend fun createFuelWallet(wallet: FuelWallet): Result<Unit> = try {
        db.collection(FUEL_WALLETS_COLLECTION)
            .document(wallet.id)
            .set(wallet.toFirestoreMap())
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Error creating fuel wallet")
        Result.failure(e)
    }
    
    suspend fun getFuelWalletById(walletId: String): Result<FuelWallet?> = try {
        val doc = db.collection(FUEL_WALLETS_COLLECTION)
            .document(walletId)
            .get()
            .await()
        val wallet = doc.toFuelWallet()
        Result.success(wallet)
    } catch (e: Exception) {
        Timber.e(e, "Error getting fuel wallet")
        Result.failure(e)
    }
    
    fun getAllFuelWallets(): Flow<List<FuelWallet>> = callbackFlow {
        val listener = db.collection(FUEL_WALLETS_COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "Error getting fuel wallets")
                    return@addSnapshotListener
                }
                val wallets = snapshot?.documents?.mapNotNull { it.toFuelWallet() } ?: emptyList()
                trySend(wallets)
            }
        awaitClose { listener.remove() }
    }
    
    suspend fun updateFuelWallet(wallet: FuelWallet): Result<Unit> = try {
        db.collection(FUEL_WALLETS_COLLECTION)
            .document(wallet.id)
            .update(wallet.toFirestoreMap())
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Error updating fuel wallet")
        Result.failure(e)
    }
    
    // ==================== TRANSACTION OPERATIONS ====================
    
    suspend fun createTransaction(transaction: FuelTransaction): Result<Unit> = try {
        db.collection(TRANSACTIONS_COLLECTION)
            .document(transaction.id)
            .set(transaction.toFirestoreMap())
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Error creating transaction")
        Result.failure(e)
    }
    
    suspend fun getTransactionById(transactionId: String): Result<FuelTransaction?> = try {
        val doc = db.collection(TRANSACTIONS_COLLECTION)
            .document(transactionId)
            .get()
            .await()
        val transaction = doc.toFuelTransaction()
        Result.success(transaction)
    } catch (e: Exception) {
        Timber.e(e, "Error getting transaction")
        Result.failure(e)
    }
    
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
    
    fun getAllTransactions(): Flow<List<FuelTransaction>> = callbackFlow {
        val listener = db.collection(TRANSACTIONS_COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "Error getting transactions")
                    return@addSnapshotListener
                }
                val transactions = snapshot?.documents?.mapNotNull { it.toFuelTransaction() } ?: emptyList()
                trySend(transactions)
            }
        awaitClose { listener.remove() }
    }
    
    fun getTransactionsByWallet(walletId: String): Flow<List<FuelTransaction>> = callbackFlow {
        val listener = db.collection(TRANSACTIONS_COLLECTION)
            .whereEqualTo("walletId", walletId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "Error getting wallet transactions")
                    return@addSnapshotListener
                }
                val transactions = snapshot?.documents?.mapNotNull { it.toFuelTransaction() } ?: emptyList()
                trySend(transactions)
            }
        awaitClose { listener.remove() }
    }
    
    fun getTransactionsByStatus(status: TransactionStatus): Flow<List<FuelTransaction>> = callbackFlow {
        val listener = db.collection(TRANSACTIONS_COLLECTION)
            .whereEqualTo("status", status.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "Error getting transactions by status")
                    return@addSnapshotListener
                }
                val transactions = snapshot?.documents?.mapNotNull { it.toFuelTransaction() } ?: emptyList()
                trySend(transactions)
            }
        awaitClose { listener.remove() }
    }
    
    suspend fun updateTransaction(transaction: FuelTransaction): Result<Unit> = try {
        db.collection(TRANSACTIONS_COLLECTION)
            .document(transaction.id)
            .update(transaction.toFirestoreMap())
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Error updating transaction")
        Result.failure(e)
    }
    
    // ==================== GAS SLIP OPERATIONS ====================
    
    suspend fun createGasSlip(gasSlip: GasSlip): Result<Unit> = try {
        db.collection(GAS_SLIPS_COLLECTION)
            .document(gasSlip.id)
            .set(gasSlip.toFirestoreMap())
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Error creating gas slip")
        Result.failure(e)
    }
    
    suspend fun getGasSlipById(gasSlipId: String): Result<GasSlip?> = try {
        val doc = db.collection(GAS_SLIPS_COLLECTION)
            .document(gasSlipId)
            .get()
            .await()
        val gasSlip = doc.toGasSlip()
        Result.success(gasSlip)
    } catch (e: Exception) {
        Timber.e(e, "Error getting gas slip")
        Result.failure(e)
    }
    
    suspend fun getAllGasSlipsOneTime(): List<GasSlip> = try {
        Timber.d("Fetching gas slips one-time from Firestore")
        val docs = db.collection(GAS_SLIPS_COLLECTION)
            .get()
            .await()
        val gasSlips = docs.documents.mapNotNull { doc ->
            try {
                doc.toGasSlip()
            } catch (e: Exception) {
                Timber.w(e, "Failed to convert document to GasSlip: ${doc.id}")
                null
            }
        }
        Timber.d("One-time fetch returned ${gasSlips.size} gas slips")
        gasSlips
    } catch (e: Exception) {
        Timber.e(e, "Error in getAllGasSlipsOneTime: ${e.message}")
        emptyList()
    }
    
    fun getAllGasSlips(): Flow<List<GasSlip>> = callbackFlow {
        try {
            val listener = db.collection(GAS_SLIPS_COLLECTION)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Timber.e(error, "Error getting gas slips: ${error.message}")
                        // Send empty list on error to prevent crash
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    val gasSlips = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            doc.toGasSlip()
                        } catch (e: Exception) {
                            Timber.w(e, "Failed to convert document to GasSlip: ${doc.id}")
                            null
                        }
                    } ?: emptyList()
                    trySend(gasSlips)
                }
            awaitClose { 
                try {
                    listener.remove()
                } catch (e: Exception) {
                    Timber.e(e, "Error removing listener")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error initializing getAllGasSlips flow: ${e.message}")
            trySend(emptyList())
            awaitClose()
        }
    }
    
    fun getGasSlipsByTransaction(transactionId: String): Flow<List<GasSlip>> = callbackFlow {
        val listener = db.collection(GAS_SLIPS_COLLECTION)
            .whereEqualTo("transactionId", transactionId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "Error getting transaction gas slips")
                    return@addSnapshotListener
                }
                val gasSlips = snapshot?.documents?.mapNotNull { it.toGasSlip() } ?: emptyList()
                trySend(gasSlips)
            }
        awaitClose { listener.remove() }
    }
    
    suspend fun updateGasSlip(gasSlip: GasSlip): Result<Unit> = try {
        db.collection(GAS_SLIPS_COLLECTION)
            .document(gasSlip.id)
            .update(gasSlip.toFirestoreMap())
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Error updating gas slip")
        Result.failure(e)
    }
    
    // ==================== AUDIT LOG OPERATIONS ====================
    
    suspend fun createAuditLog(auditLog: AuditLog): Result<Unit> = try {
        db.collection(AUDIT_LOGS_COLLECTION)
            .document(auditLog.id)
            .set(auditLog.toFirestoreMap())
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Error creating audit log")
        Result.failure(e)
    }
    
    fun getAllAuditLogs(): Flow<List<AuditLog>> = callbackFlow {
        val listener = db.collection(AUDIT_LOGS_COLLECTION)
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "Error getting audit logs")
                    return@addSnapshotListener
                }
                val logs = snapshot?.documents?.mapNotNull { it.toAuditLog() } ?: emptyList()
                trySend(logs)
            }
        awaitClose { listener.remove() }
    }
    
    // ==================== CONVERSION UTILITIES ====================
    
    private fun User.toFirestoreMap() = mapOf(
        "id" to id,
        "username" to username,
        "email" to email,
        "fullName" to fullName,
        "role" to role.name,
        "officeId" to officeId,
        "isActive" to isActive,
        "createdAt" to createdAt.toDate()
    )
    
    private fun com.google.firebase.firestore.DocumentSnapshot.toUser(): User? {
        return try {
            User(
                id = getString("id") ?: return null,
                username = getString("username") ?: return null,
                email = getString("email") ?: return null,
                fullName = getString("fullName") ?: return null,
                role = UserRole.valueOf(getString("role") ?: return null),
                officeId = getString("officeId") ?: return null,
                isActive = getBoolean("isActive") ?: true,
                createdAt = (getTimestamp("createdAt")?.toDate() ?: Date()).toLocalDateTime()
            )
        } catch (e: Exception) {
            Timber.e(e, "Error converting User")
            null
        }
    }
    
    private fun Vehicle.toFirestoreMap() = mapOf(
        "id" to id,
        "plateNumber" to plateNumber,
        "vehicleType" to vehicleType,
        "fuelType" to fuelType.name,
        "driverName" to driverName,
        "driverId" to driverId,
        "assignedDriverIds" to assignedDriverIds,  // NEW: Multiple drivers
        "assignedDriverNames" to assignedDriverNames,  // NEW: Multiple drivers
        "isActive" to isActive,
        "createdAt" to createdAt.toDate()
    )
    
    private fun com.google.firebase.firestore.DocumentSnapshot.toVehicle(): Vehicle? {
        return try {
            Vehicle(
                id = getString("id") ?: return null,
                plateNumber = getString("plateNumber") ?: return null,
                vehicleType = getString("vehicleType") ?: return null,
                fuelType = FuelType.valueOf(getString("fuelType") ?: return null),
                driverName = getString("driverName") ?: return null,
                driverId = getString("driverId"),
                assignedDriverIds = (get("assignedDriverIds") as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),  // NEW
                assignedDriverNames = (get("assignedDriverNames") as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),  // NEW
                isActive = getBoolean("isActive") ?: true,
                createdAt = (getTimestamp("createdAt")?.toDate() ?: Date()).toLocalDateTime()
            )
        } catch (e: Exception) {
            Timber.e(e, "Error converting Vehicle")
            null
        }
    }
    
    private fun FuelWallet.toFirestoreMap() = mapOf(
        "id" to id,
        "officeId" to officeId,
        "balanceLiters" to balanceLiters,
        "maxCapacityLiters" to maxCapacityLiters,
        "lastUpdated" to lastUpdated.toDate(),
        "createdAt" to createdAt.toDate()
    )
    
    private fun com.google.firebase.firestore.DocumentSnapshot.toFuelWallet(): FuelWallet? {
        return try {
            FuelWallet(
                id = getString("id") ?: return null,
                officeId = getString("officeId") ?: return null,
                balanceLiters = getDouble("balanceLiters") ?: 0.0,
                maxCapacityLiters = getDouble("maxCapacityLiters") ?: 0.0,
                lastUpdated = (getTimestamp("lastUpdated")?.toDate() ?: Date()).toLocalDateTime(),
                createdAt = (getTimestamp("createdAt")?.toDate() ?: Date()).toLocalDateTime()
            )
        } catch (e: Exception) {
            Timber.e(e, "Error converting FuelWallet")
            null
        }
    }
    
    private fun FuelTransaction.toFirestoreMap() = mapOf(
        "id" to id,
        "referenceNumber" to referenceNumber,
        "walletId" to walletId,
        "vehicleId" to vehicleId,
        "driverName" to driverName,
        "driverFullName" to driverFullName,
        "vehicleType" to vehicleType,
        "fuelType" to fuelType.name,
        "litersToPump" to litersToPump,
        "costPerLiter" to costPerLiter,
        "destination" to destination,
        "tripPurpose" to tripPurpose,
        "passengers" to passengers,
        "status" to status.name,
        "createdBy" to createdBy,
        "approvedBy" to approvedBy,
        "createdAt" to createdAt.toDate(),
        "completedAt" to (completedAt?.toDate()),
        "notes" to notes
    )
    
    private fun com.google.firebase.firestore.DocumentSnapshot.toFuelTransaction(): FuelTransaction? {
        return try {
            FuelTransaction(
                id = getString("id") ?: return null,
                referenceNumber = getString("referenceNumber") ?: return null,
                walletId = getString("walletId") ?: return null,
                vehicleId = getString("vehicleId") ?: return null,
                driverName = getString("driverName") ?: return null,
                driverFullName = getString("driverFullName"),
                vehicleType = getString("vehicleType") ?: return null,
                fuelType = FuelType.valueOf(getString("fuelType") ?: return null),
                litersToPump = getDouble("litersToPump") ?: 0.0,
                costPerLiter = getDouble("costPerLiter") ?: 0.0,
                destination = getString("destination") ?: return null,
                tripPurpose = getString("tripPurpose") ?: return null,
                passengers = getString("passengers"),
                status = TransactionStatus.valueOf(getString("status") ?: "PENDING"),
                createdBy = getString("createdBy") ?: return null,
                approvedBy = getString("approvedBy"),
                createdAt = (getTimestamp("createdAt")?.toDate() ?: Date()).toLocalDateTime(),
                completedAt = getTimestamp("completedAt")?.toDate()?.toLocalDateTime(),
                notes = getString("notes")
            )
        } catch (e: Exception) {
            Timber.e(e, "Error converting FuelTransaction")
            null
        }
    }
    
    private fun GasSlip.toFirestoreMap() = mapOf(
        "id" to id,
        "transactionId" to transactionId,
        "referenceNumber" to referenceNumber,
        "driverName" to driverName,
        "driverFullName" to driverFullName,
        "vehicleType" to vehicleType,
        "vehiclePlateNumber" to vehiclePlateNumber,
        "destination" to destination,
        "tripPurpose" to tripPurpose,
        "passengers" to passengers,
        "fuelType" to fuelType.name,
        "litersToPump" to litersToPump,
        "transactionDate" to transactionDate.toDate(),
        "mdrrmoOfficeId" to mdrrmoOfficeId,
        "mdrrmoOfficeName" to mdrrmoOfficeName,
        "generatedAt" to generatedAt.toDate(),
        "status" to status.name,
        "dispensedAt" to (dispensedAt?.toDate()),
        "dispensedLiters" to dispensedLiters,
        "isUsed" to isUsed,
        "usedAt" to (usedAt?.toDate())
    )
    
    private fun com.google.firebase.firestore.DocumentSnapshot.toGasSlip(): GasSlip? {
        return try {
            val id = getString("id")
            val transactionId = getString("transactionId")
            val referenceNumber = getString("referenceNumber")
            val driverName = getString("driverName")
            val vehicleType = getString("vehicleType")
            val vehiclePlateNumber = getString("vehiclePlateNumber")
            val destination = getString("destination")
            val tripPurpose = getString("tripPurpose")
            val fuelTypeStr = getString("fuelType")
            val mdrrmoOfficeId = getString("mdrrmoOfficeId")
            val mdrrmoOfficeName = getString("mdrrmoOfficeName")
            
            // Check required fields
            if (id == null || transactionId == null || referenceNumber == null || 
                driverName == null || vehicleType == null || vehiclePlateNumber == null ||
                destination == null || tripPurpose == null || fuelTypeStr == null ||
                mdrrmoOfficeId == null || mdrrmoOfficeName == null) {
                Timber.w("Missing required fields in GasSlip document: $id")
                return null
            }
            
            GasSlip(
                id = id,
                transactionId = transactionId,
                referenceNumber = referenceNumber,
                driverName = driverName,
                driverFullName = getString("driverFullName"),
                vehicleType = vehicleType,
                vehiclePlateNumber = vehiclePlateNumber,
                destination = destination,
                tripPurpose = tripPurpose,
                passengers = getString("passengers"),
                fuelType = try {
                    FuelType.valueOf(fuelTypeStr)
                } catch (e: IllegalArgumentException) {
                    Timber.w("Invalid fuel type: $fuelTypeStr")
                    return null
                },
                litersToPump = getDouble("litersToPump") ?: 0.0,
                transactionDate = (getTimestamp("transactionDate")?.toDate() ?: Date()).toLocalDateTime(),
                mdrrmoOfficeId = mdrrmoOfficeId,
                mdrrmoOfficeName = mdrrmoOfficeName,
                generatedAt = (getTimestamp("generatedAt")?.toDate() ?: Date()).toLocalDateTime(),
                status = try {
                    dev.ml.fuelhub.data.model.GasSlipStatus.valueOf(getString("status") ?: "PENDING")
                } catch (e: IllegalArgumentException) {
                    dev.ml.fuelhub.data.model.GasSlipStatus.PENDING
                },
                dispensedAt = getTimestamp("dispensedAt")?.toDate()?.toLocalDateTime(),
                dispensedLiters = getDouble("dispensedLiters"),
                isUsed = getBoolean("isUsed") ?: false,
                usedAt = getTimestamp("usedAt")?.toDate()?.toLocalDateTime()
            )
        } catch (e: Exception) {
            Timber.e(e, "Error converting GasSlip")
            null
        }
    }
    
    private fun AuditLog.toFirestoreMap() = mapOf(
        "id" to id,
        "walletId" to walletId,
        "action" to action,
        "transactionId" to transactionId,
        "performedBy" to performedBy,
        "previousBalance" to previousBalance,
        "newBalance" to newBalance,
        "litersDifference" to litersDifference,
        "description" to description,
        "timestamp" to timestamp.toDate(),
        "ipAddress" to ipAddress
    )
    
    private fun com.google.firebase.firestore.DocumentSnapshot.toAuditLog(): AuditLog? {
        return try {
            AuditLog(
                id = getString("id") ?: return null,
                walletId = getString("walletId") ?: return null,
                action = getString("action") ?: return null,
                transactionId = getString("transactionId"),
                performedBy = getString("performedBy") ?: return null,
                previousBalance = getDouble("previousBalance") ?: 0.0,
                newBalance = getDouble("newBalance") ?: 0.0,
                litersDifference = getDouble("litersDifference") ?: 0.0,
                description = getString("description") ?: return null,
                timestamp = (getTimestamp("timestamp")?.toDate() ?: Date()).toLocalDateTime(),
                ipAddress = getString("ipAddress")
            )
        } catch (e: Exception) {
            Timber.e(e, "Error converting AuditLog")
            null
        }
    }
    
    // ==================== HELPER FUNCTIONS ====================
    
    private fun LocalDateTime.toDate(): Date {
        return Date.from(this.atZone(ZoneId.systemDefault()).toInstant())
    }
    
    private fun Date.toLocalDateTime(): LocalDateTime {
        return LocalDateTime.ofInstant(this.toInstant(), ZoneId.systemDefault())
    }
}
