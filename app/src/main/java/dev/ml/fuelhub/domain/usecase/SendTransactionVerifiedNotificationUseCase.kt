package dev.ml.fuelhub.domain.usecase

import dev.ml.fuelhub.data.model.NotificationType
import dev.ml.fuelhub.domain.repository.NotificationRepository
import timber.log.Timber

/**
 * Use case for sending notifications when a transaction is verified/dispensed by gas station.
 * Notifies the driver that their transaction has been successfully dispensed.
 */
class SendTransactionVerifiedNotificationUseCase(
    private val notificationRepository: NotificationRepository
) {

    data class Input(
        val driverId: String,              // User ID of the driver
        val transactionId: String,
        val referenceNumber: String,
        val vehicleType: String,
        val litersPumped: Double,
        val gasStationName: String = "Gas Station",
        val timestamp: String? = null
    )

    suspend fun execute(input: Input): Boolean = try {
        val title = "✓ Fuel Dispensed: ${input.referenceNumber}"
        val body = "Your transaction has been successfully verified and dispensed:\n\n" +
                "Reference: ${input.referenceNumber}\n" +
                "Vehicle: ${input.vehicleType}\n" +
                "Liters Dispensed: ${input.litersPumped}L\n" +
                "Station: ${input.gasStationName}"

        val data = mapOf(
            "type" to "TRANSACTION_DISPENSED",
            "transactionId" to input.transactionId,
            "referenceNumber" to input.referenceNumber,
            "vehicleType" to input.vehicleType,
            "litersPumped" to input.litersPumped.toString(),
            "gasStationName" to input.gasStationName
        )

        // Send notification to the driver
        val success = notificationRepository.sendNotification(
            userId = input.driverId,
            title = title,
            body = body,
            notificationType = NotificationType.TRANSACTION_DISPENSED,
            transactionId = input.transactionId,
            referenceNumber = input.referenceNumber,
            data = data
        )

        if (success) {
            Timber.d("Transaction verified notification sent to driver: ${input.driverId}")
        } else {
            Timber.w("Failed to send transaction verified notification to: ${input.driverId}")
        }

        success
    } catch (e: Exception) {
        Timber.e(e, "Error sending transaction verified notification")
        false
    }
}
