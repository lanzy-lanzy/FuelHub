package dev.ml.fuelhub.domain.usecase

import dev.ml.fuelhub.data.model.TransactionStatus
import dev.ml.fuelhub.domain.exception.UnauthorizedException
import dev.ml.fuelhub.domain.exception.TransactionProcessingException
import dev.ml.fuelhub.domain.exception.EntityNotFoundException
import dev.ml.fuelhub.domain.repository.FuelTransactionRepository
import dev.ml.fuelhub.domain.repository.UserRepository
import dev.ml.fuelhub.domain.repository.AuditLogRepository
import java.time.LocalDateTime

/**
 * Use case for approving a fuel transaction.
 * Only admins can approve transactions.
 */
class ApproveTransactionUseCase(
    private val transactionRepository: FuelTransactionRepository,
    private val userRepository: UserRepository,
    private val auditLogRepository: AuditLogRepository
) {
    
    data class ApproveTransactionInput(
        val transactionId: String,
        val approvedBy: String  // User ID
    )

    suspend fun execute(input: ApproveTransactionInput) {
        // Validate that approver is an admin
        val approver = userRepository.getUserById(input.approvedBy)
            ?: throw UnauthorizedException("Approver not found")

        if (approver.role != dev.ml.fuelhub.data.model.UserRole.ADMIN) {
            throw UnauthorizedException("Only admins can approve transactions")
        }

        // Get transaction
        val transaction = transactionRepository.getTransactionById(input.transactionId)
            ?: throw EntityNotFoundException("Transaction not found")

        if (transaction.status != TransactionStatus.PENDING) {
            throw TransactionProcessingException("Transaction is not in PENDING status")
        }

        // Update transaction status
        val updatedTransaction = transaction.copy(
            status = TransactionStatus.APPROVED,
            approvedBy = input.approvedBy,
            completedAt = LocalDateTime.now()
        )

        transactionRepository.updateTransaction(updatedTransaction)

        // Log audit
        auditLogRepository.logAction(
            walletId = transaction.walletId,
            action = "TRANSACTION_APPROVED",
            transactionId = input.transactionId,
            performedBy = input.approvedBy,
            previousBalance = 0.0,
            newBalance = 0.0,
            litersDifference = 0.0,
            description = "Transaction ${input.transactionId} approved"
        )
    }
}
