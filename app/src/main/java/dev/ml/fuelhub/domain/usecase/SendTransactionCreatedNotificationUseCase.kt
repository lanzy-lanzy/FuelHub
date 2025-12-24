package dev.ml.fuelhub.domain.usecase

import dev.ml.fuelhub.data.model.NotificationType
import dev.ml.fuelhub.data.model.UserRole
import dev.ml.fuelhub.domain.repository.NotificationRepository
import timber.log.Timber

/**
 * Use case for sending notifications when a new transaction is created.
 * Notifies all GAS_STATION users that a new transaction requires scanning.
 */
class SendTransactionCreatedNotificationUseCase(
    private val notificationRepository: NotificationRepository
) {

    data class Input(
        val transactionId: String,
        val referenceNumber: String,
        val vehicleType: String,
        val litersToPump: Double,
        val driverName: String,
        val driverFullName: String? = null
    )

    suspend fun execute(input: Input): Boolean = try {
        val title = "New Transaction: ${input.referenceNumber}"
        val body = "New fuel transaction created:\n" +
                "Vehicle: ${input.vehicleType}\n" +
                "Liters: ${input.litersToPump}L\n" +
                "Driver: ${input.driverFullName ?: input.driverName}\n\n" +
                "Please scan the QR code to dispense fuel."

        val data = mapOf(
            "type" to "TRANSACTION_CREATED",
            "transactionId" to input.transactionId,
            "referenceNumber" to input.referenceNumber,
            "vehicleType" to input.vehicleType,
            "litersToPump" to input.litersToPump.toString(),
            "driverName" to (input.driverFullName ?: input.driverName)
        )

        // Send notification to all GAS_STATION users
        val success = notificationRepository.sendNotificationToRole(
            role = UserRole.GAS_STATION.name,
            title = title,
            body = body,
            notificationType = NotificationType.TRANSACTION_CREATED,
            data = data
        )

        if (success) {
            Timber.d("Transaction created notification sent for: ${input.referenceNumber}")
        } else {
            Timber.w("Failed to send transaction created notification for: ${input.referenceNumber}")
        }

        success
    } catch (e: Exception) {
        Timber.e(e, "Error sending transaction created notification")
        false
    }
}
