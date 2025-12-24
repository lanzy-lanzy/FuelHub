package dev.ml.fuelhub.data.service

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import dev.ml.fuelhub.data.firebase.FirebaseDataSource
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Service for sending push notifications via Firebase Cloud Functions.
 * Handles notifications to gas station operators when transactions are created.
 */
class NotificationService(
    private val functions: FirebaseFunctions,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseDataSource: FirebaseDataSource
) {

    /**
     * Send notification to a specific user by FCM token.
     * 
     * @param token FCM token of the recipient
     * @param title Notification title
     * @param body Notification body
     * @param type Type of notification (for categorization)
     * @param transactionId ID of the transaction (optional)
     * @param referenceNumber Transaction reference number (optional)
     */
    suspend fun sendNotificationByToken(
        token: String,
        title: String,
        body: String,
        type: String = "SYSTEM_ALERT",
        transactionId: String = "",
        referenceNumber: String = ""
    ) = suspendCancellableCoroutine { continuation ->
        try {
            functions.getHttpsCallable("sendNotification")
                .call(
                    mapOf(
                        "token" to token,
                        "title" to title,
                        "body" to body,
                        "type" to type,
                        "transactionId" to transactionId,
                        "referenceNumber" to referenceNumber
                    )
                )
                .addOnSuccessListener { result ->
                    Timber.d("Notification sent successfully: $title")
                    continuation.resume(Unit)
                }
                .addOnFailureListener { error ->
                    Timber.e(error, "Failed to send notification: ${error.message}")
                    continuation.resumeWithException(error)
                }
        } catch (e: Exception) {
            Timber.e(e, "Error calling sendNotification function")
            continuation.resumeWithException(e)
        }
    }

    /**
     * Send notification to all gas station operators.
     * 
     * @param title Notification title
     * @param body Notification body
     * @param type Type of notification
     */
    suspend fun sendNotificationToGasStations(
        title: String,
        body: String,
        type: String = "SYSTEM_ALERT"
    ) = suspendCancellableCoroutine { continuation ->
        try {
            Timber.d("=== SENDING NOTIFICATION TO GAS STATIONS ===")
            Timber.d("Title: $title")
            Timber.d("Body: $body")
            Timber.d("Type: $type")
            
            functions.getHttpsCallable("sendNotificationToRole")
                .call(
                    mapOf(
                        "role" to "GAS_STATION",
                        "title" to title,
                        "body" to body,
                        "type" to type
                    )
                )
                .addOnSuccessListener { result ->
                    val data = result.data as? Map<*, *>
                    val sent = (data?.get("sent") as? Number)?.toInt() ?: 0
                    Timber.d("✓ Notification sent to $sent gas station operators")
                    continuation.resume(Unit)
                }
                .addOnFailureListener { error ->
                    Timber.e(error, "✗ Failed to send notification to gas stations: ${error.message}")
                    continuation.resumeWithException(error)
                }
        } catch (e: Exception) {
            Timber.e(e, "Error calling sendNotificationToRole function")
            continuation.resumeWithException(e)
        }
    }

    /**
     * Notify gas stations about a new transaction.
     * Called when a transaction is created by a fleet manager/dispatcher.
     * 
     * @param transactionId ID of the created transaction
     * @param referenceNumber Transaction reference number
     * @param vehiclePlateNumber Vehicle plate for context
     * @param litersToPump Amount of fuel to pump
     */
    suspend fun notifyGasStationsOnTransactionCreated(
        transactionId: String,
        referenceNumber: String,
        vehiclePlateNumber: String,
        litersToPump: Double
    ) {
        try {
            val title = "New Fuel Transaction"
            val body = "Transaction #$referenceNumber: Pump $litersToPump L for vehicle $vehiclePlateNumber"

            sendNotificationToGasStations(
                title = title,
                body = body,
                type = "TRANSACTION_CREATED"
            )
            Timber.d("Gas station notification queued for transaction: $referenceNumber")
        } catch (e: Exception) {
            Timber.e(e, "Error notifying gas stations about new transaction")
        }
    }

    /**
     * Notify gas stations that a transaction has been verified/dispensed.
     * 
     * @param transactionId ID of the transaction
     * @param referenceNumber Transaction reference number
     * @param status Current status
     */
    suspend fun notifyGasStationsOnTransactionUpdate(
        transactionId: String,
        referenceNumber: String,
        status: String
    ) {
        try {
            val title = "Transaction Status Update"
            val body = "Transaction #$referenceNumber is now $status"

            sendNotificationToGasStations(
                title = title,
                body = body,
                type = "TRANSACTION_$status"
            )
            Timber.d("Gas station notification queued for transaction update: $referenceNumber")
        } catch (e: Exception) {
            Timber.e(e, "Error notifying gas stations about transaction update")
        }
    }

    /**
     * Send notification to a specific gas station by user ID.
     * This will fetch the user's FCM token from Firestore and send the notification.
     * 
     * @param gasStationUserId ID of the gas station user
     * @param title Notification title
     * @param body Notification body
     * @param type Type of notification
     * @param transactionId ID of related transaction (optional)
     * @param referenceNumber Transaction reference (optional)
     */
    suspend fun sendNotificationToGasStation(
        gasStationUserId: String,
        title: String,
        body: String,
        type: String = "SYSTEM_ALERT",
        transactionId: String = "",
        referenceNumber: String = ""
    ) {
        try {
            // Get the FCM token from Firestore
            val token = firebaseDataSource.getFcmToken(gasStationUserId)
            
            if (token.isNotEmpty()) {
                sendNotificationByToken(
                    token = token,
                    title = title,
                    body = body,
                    type = type,
                    transactionId = transactionId,
                    referenceNumber = referenceNumber
                )
            } else {
                Timber.w("No FCM token found for gas station user: $gasStationUserId")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error sending notification to gas station $gasStationUserId")
        }
    }
}
