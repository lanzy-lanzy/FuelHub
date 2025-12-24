package dev.ml.fuelhub.data.model

import java.time.LocalDateTime

/**
 * Represents a notification payload sent to users.
 * Used to track notification delivery and user interactions.
 */
data class NotificationPayload(
    val id: String,
    val userId: String,           // Target user ID
    val title: String,
    val body: String,
    val notificationType: NotificationType,
    val transactionId: String?,    // Associated transaction ID
    val referenceNumber: String?,  // Gas slip reference number
    val sentAt: LocalDateTime,
    val readAt: LocalDateTime? = null,
    val actionedAt: LocalDateTime? = null,
    val data: Map<String, String> = emptyMap()  // Extra data
)

enum class NotificationType {
    TRANSACTION_CREATED,      // New transaction requires scanning
    TRANSACTION_DISPENSED,    // Transaction successfully dispensed
    TRANSACTION_VERIFIED,     // Transaction verified by gas station
    TRANSACTION_APPROVED,     // Transaction approved
    TRANSACTION_CANCELLED,    // Transaction cancelled
    WALLET_LOW,              // Wallet balance low
    SYSTEM_ALERT
}
