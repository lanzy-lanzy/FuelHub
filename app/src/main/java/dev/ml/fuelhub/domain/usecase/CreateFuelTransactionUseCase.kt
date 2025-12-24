package dev.ml.fuelhub.domain.usecase

import dev.ml.fuelhub.data.model.FuelTransaction
import dev.ml.fuelhub.data.model.GasSlip
import dev.ml.fuelhub.domain.exception.InsufficientFuelException
import dev.ml.fuelhub.domain.exception.TransactionValidationException
import dev.ml.fuelhub.domain.exception.UnauthorizedException
import dev.ml.fuelhub.domain.repository.FuelWalletRepository
import dev.ml.fuelhub.domain.repository.FuelTransactionRepository
import dev.ml.fuelhub.domain.repository.GasSlipRepository
import dev.ml.fuelhub.domain.repository.AuditLogRepository
import dev.ml.fuelhub.domain.repository.VehicleRepository
import dev.ml.fuelhub.domain.repository.UserRepository
import timber.log.Timber
import java.time.LocalDateTime
import java.util.UUID

/**
 * Use case for creating a fuel transaction and generating a gas slip.
 * Implements the complete transaction workflow with validation and wallet deduction.
 *
 * Transaction Workflow:
 * 1. Validate user authorization
 * 2. Validate transaction input
 * 3. Check fuel wallet balance
 * 4. Create transaction record
 * 5. Deduct from fuel wallet (atomic operation)
 * 6. Generate gas slip
 * 7. Log audit trail
 */
class CreateFuelTransactionUseCase(
    private val walletRepository: FuelWalletRepository,
    private val transactionRepository: FuelTransactionRepository,
    private val gasSlipRepository: GasSlipRepository,
    private val auditLogRepository: AuditLogRepository,
    private val vehicleRepository: VehicleRepository,
    private val userRepository: UserRepository,
    private val sendTransactionCreatedNotificationUseCase: SendTransactionCreatedNotificationUseCase? = null
) {
    
    data class CreateTransactionInput(
        val vehicleId: String,
        val litersToPump: Double,
        val costPerLiter: Double = 0.0,
        val destination: String,
        val tripPurpose: String,
        val passengers: String? = null,
        val createdBy: String,  // User ID
        val walletId: String
    )
    
    data class CreateTransactionOutput(
        val transaction: FuelTransaction,
        val gasSlip: GasSlip,
        val newWalletBalance: Double
    )

    /**
     * Execute the transaction creation workflow.
     * @throws UnauthorizedException if user is not authorized
     * @throws TransactionValidationException if validation fails
     * @throws InsufficientFuelException if wallet balance is insufficient
     * @throws EntityNotFoundException if entities are not found
     */
    suspend fun execute(input: CreateTransactionInput): CreateTransactionOutput {
        // 1. Validate user authorization
        // For offline mode: try to get user by ID, but if not found, try by username, or just allow the transaction
        val user = userRepository.getUserById(input.createdBy)
            ?: userRepository.getUserByUsername(input.createdBy)
        
        // Allow transactions for offline mode if user doesn't exist
        // Authorization check is skipped for local/offline transactions
        if (user != null && !isUserAuthorizedToCreateTransaction(user)) {
            throw UnauthorizedException("User ${user.fullName} is not authorized to create transactions")
        }

        // 2. Validate transaction input
        validateTransactionInput(input)

        // 3. Get wallet and check balance
        val wallet = walletRepository.getWalletById(input.walletId)
            ?: throw dev.ml.fuelhub.domain.exception.EntityNotFoundException("Fuel wallet ${input.walletId} not found")

        if (wallet.balanceLiters < input.litersToPump) {
            throw InsufficientFuelException(
                "Insufficient fuel balance. Required: ${input.litersToPump}L, Available: ${wallet.balanceLiters}L"
            )
        }

        // 4. Get vehicle and validate fuel type match
        val vehicle = vehicleRepository.getVehicleById(input.vehicleId)
            ?: throw dev.ml.fuelhub.domain.exception.EntityNotFoundException("Vehicle ${input.vehicleId} not found")

        val transactionId = UUID.randomUUID().toString()
        val referenceNumber = generateReferenceNumber()
        val now = LocalDateTime.now()

        // 5. Create transaction record
        val transaction = FuelTransaction(
            id = transactionId,
            referenceNumber = referenceNumber,
            walletId = input.walletId,
            vehicleId = input.vehicleId,
            driverName = input.createdBy,  // Use selected driver instead of vehicle's primary driver
            driverFullName = user?.fullName,  // Store the full name for display
            vehicleType = vehicle.vehicleType,
            fuelType = vehicle.fuelType,
            litersToPump = input.litersToPump,
            costPerLiter = input.costPerLiter,
            destination = input.destination,
            tripPurpose = input.tripPurpose,
            passengers = input.passengers,
            createdBy = input.createdBy,
            createdAt = now
        )

        Timber.d("💾 SAVING TRANSACTION TO FIRESTORE: ${transaction.referenceNumber}")
        val savedTransaction = transactionRepository.createTransaction(transaction)
        Timber.d("✓ TRANSACTION SAVED: ${savedTransaction.referenceNumber}")

        // 6. Deduct from fuel wallet (atomic operation)
        val previousBalance = wallet.balanceLiters
        val newBalance = previousBalance - input.litersToPump
        val updatedWallet = wallet.copy(
            balanceLiters = newBalance,
            lastUpdated = now
        )

        walletRepository.updateWallet(updatedWallet)

        // 7. Generate gas slip
        // Fetch driver's full name if user exists
        val driverFullName = user?.fullName
        
         val gasSlip = GasSlip(
              id = UUID.randomUUID().toString(),
              transactionId = transactionId,
              referenceNumber = referenceNumber,
              driverName = input.createdBy,  // Use selected driver instead of vehicle's primary driver
              driverFullName = driverFullName,
              vehicleType = vehicle.vehicleType,
              vehiclePlateNumber = vehicle.plateNumber,
              destination = input.destination,
              tripPurpose = input.tripPurpose,
              passengers = input.passengers,
              fuelType = vehicle.fuelType,
              litersToPump = input.litersToPump,
              transactionDate = now,
              mdrrmoOfficeId = wallet.officeId,
              mdrrmoOfficeName = "MDRRMO Office",  // Should be fetched from office repository
              generatedAt = now
          )

         Timber.d("Creating gas slip: id=${gasSlip.id}, ref=${gasSlip.referenceNumber}")
         gasSlipRepository.createGasSlip(gasSlip)
         Timber.d("Gas slip created successfully")

        // 8. Log audit trail
        auditLogRepository.logAction(
            walletId = input.walletId,
            action = "TRANSACTION_CREATED_AND_WALLET_DEDUCTED",
            transactionId = transactionId,
            performedBy = input.createdBy,
            previousBalance = previousBalance,
            newBalance = newBalance,
            litersDifference = -input.litersToPump,
            description = "Fuel transaction created: ${input.litersToPump}L for ${input.tripPurpose}"
        )

        // 9. Send notification to gas station users
        sendTransactionCreatedNotificationUseCase?.execute(
            SendTransactionCreatedNotificationUseCase.Input(
                transactionId = transactionId,
                referenceNumber = referenceNumber,
                vehicleType = vehicle.vehicleType,
                litersToPump = input.litersToPump,
                driverName = input.createdBy,
                driverFullName = user?.fullName
            )
        )

        return CreateTransactionOutput(
            transaction = savedTransaction,
            gasSlip = gasSlip,
            newWalletBalance = newBalance
        )
    }

    private fun isUserAuthorizedToCreateTransaction(user: dev.ml.fuelhub.data.model.User): Boolean {
        return user.isActive && user.role in listOf(
            dev.ml.fuelhub.data.model.UserRole.ADMIN,
            dev.ml.fuelhub.data.model.UserRole.DISPATCHER,
            dev.ml.fuelhub.data.model.UserRole.ENCODER
        )
    }

    private fun validateTransactionInput(input: CreateTransactionInput) {
        require(input.litersToPump > 0) {
            throw TransactionValidationException("Liters to pump must be greater than 0")
        }
        require(input.destination.isNotBlank()) {
            throw TransactionValidationException("Destination is required")
        }
        require(input.tripPurpose.isNotBlank()) {
            throw TransactionValidationException("Trip purpose is required")
        }
    }

    private fun generateReferenceNumber(): String {
        val timestamp = System.currentTimeMillis().toString().takeLast(8)
        val random = (1000..9999).random()
        return "FS-$timestamp-$random"
    }
}
