package dev.ml.fuelhub.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dev.ml.fuelhub.data.model.NotificationPayload
import dev.ml.fuelhub.data.model.NotificationType
import dev.ml.fuelhub.domain.repository.NotificationRepository
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.time.LocalDateTime
import java.util.UUID

/**
 * Firebase implementation of NotificationRepository.
 * Handles FCM token management and notification storage in Firestore.
 */
class FirebaseNotificationRepositoryImpl(
    private val firestore: FirebaseFirestore
) : NotificationRepository {

    companion object {
        const val NOTIFICATIONS_COLLECTION = "notifications"
        const val FCM_TOKENS_COLLECTION = "fcm_tokens"
        const val USERS_COLLECTION = "users"
    }

    /**
     * Send a notification to a specific user.
     * Stores notification in Firestore and sends via FCM.
     */
    override suspend fun sendNotification(
        userId: String,
        title: String,
        body: String,
        notificationType: NotificationType,
        transactionId: String?,
        referenceNumber: String?,
        data: Map<String, String>
    ): Boolean = try {
        val notificationId = UUID.randomUUID().toString()
        val now = LocalDateTime.now()

        val notificationData = mapOf(
            "id" to notificationId,
            "userId" to userId,
            "title" to title,
            "body" to body,
            "notificationType" to notificationType.name,
            "transactionId" to (transactionId ?: ""),
            "referenceNumber" to (referenceNumber ?: ""),
            "sentAt" to now.toString(),
            "readAt" to "",
            "actionedAt" to "",
            "data" to data
        )

        // Store notification in Firestore
        firestore.collection(NOTIFICATIONS_COLLECTION)
            .document(notificationId)
            .set(notificationData)
            .await()

        // Also store in user's notification sub-collection for easier querying
        firestore.collection(USERS_COLLECTION)
            .document(userId)
            .collection("notifications")
            .document(notificationId)
            .set(notificationData)
            .await()

        Timber.d("Notification sent: $notificationId to user: $userId")

        // Send via FCM if token is available
        val token = getUserFcmToken(userId)
        if (!token.isNullOrEmpty()) {
            sendViaFcm(token, title, body, notificationType, data)
        }

        true
    } catch (e: Exception) {
        Timber.e(e, "Failed to send notification to user: $userId")
        false
    }

    /**
     * Send notification to all users with a specific role.
     * Queries for all users with the role and sends notifications to each.
     */
    override suspend fun sendNotificationToRole(
        role: String,
        title: String,
        body: String,
        notificationType: NotificationType,
        data: Map<String, String>
    ): Boolean = try {
        // Query all users with the specific role
        val users = firestore.collection(USERS_COLLECTION)
            .whereEqualTo("role", role)
            .whereEqualTo("isActive", true)
            .get()
            .await()

        var successCount = 0
        for (userDoc in users.documents) {
            val userId = userDoc.id
            val notificationId = UUID.randomUUID().toString()
            val now = LocalDateTime.now()

            val notificationData = mapOf(
                "id" to notificationId,
                "userId" to userId,
                "title" to title,
                "body" to body,
                "notificationType" to notificationType.name,
                "transactionId" to "",
                "referenceNumber" to "",
                "sentAt" to now.toString(),
                "readAt" to "",
                "actionedAt" to "",
                "data" to data
            )

            try {
                // Store notification
                firestore.collection(NOTIFICATIONS_COLLECTION)
                    .document(notificationId)
                    .set(notificationData)
                    .await()

                firestore.collection(USERS_COLLECTION)
                    .document(userId)
                    .collection("notifications")
                    .document(notificationId)
                    .set(notificationData)
                    .await()

                // Send via FCM
                val token = getUserFcmToken(userId)
                if (!token.isNullOrEmpty()) {
                    sendViaFcm(token, title, body, notificationType, data)
                    successCount++
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to send notification to user: $userId")
            }
        }

        Timber.d("Notifications sent to $successCount users with role: $role")
        successCount > 0
    } catch (e: Exception) {
        Timber.e(e, "Failed to send notifications to role: $role")
        false
    }

    /**
     * Get user's FCM token from Firestore.
     */
    override suspend fun getUserFcmToken(userId: String): String? = try {
        val doc = firestore.collection(FCM_TOKENS_COLLECTION)
            .document(userId)
            .get()
            .await()

        doc.getString("token")
    } catch (e: Exception) {
        Timber.e(e, "Failed to get FCM token for user: $userId")
        null
    }

    /**
     * Store/update user's FCM token in Firestore.
     */
    override suspend fun storeUserFcmToken(userId: String, token: String): Boolean = try {
        firestore.collection(FCM_TOKENS_COLLECTION)
            .document(userId)
            .set(mapOf(
                "userId" to userId,
                "token" to token,
                "updatedAt" to LocalDateTime.now().toString()
            ))
            .await()

        // Also update in user document
        firestore.collection(USERS_COLLECTION)
            .document(userId)
            .update("fcmToken", token)
            .await()

        Timber.d("FCM token stored for user: $userId")
        true
    } catch (e: Exception) {
        Timber.e(e, "Failed to store FCM token for user: $userId")
        false
    }

    /**
     * Get notification history for a user.
     */
    override suspend fun getNotificationHistory(userId: String, limit: Int): List<NotificationPayload> = try {
        val docs = firestore.collection(USERS_COLLECTION)
            .document(userId)
            .collection("notifications")
            .orderBy("sentAt", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get()
            .await()

        docs.documents.mapNotNull { doc ->
            try {
                NotificationPayload(
                    id = doc.getString("id") ?: return@mapNotNull null,
                    userId = userId,
                    title = doc.getString("title") ?: "",
                    body = doc.getString("body") ?: "",
                    notificationType = NotificationType.valueOf(doc.getString("notificationType") ?: "SYSTEM_ALERT"),
                    transactionId = doc.getString("transactionId")?.takeIf { it.isNotEmpty() },
                    referenceNumber = doc.getString("referenceNumber")?.takeIf { it.isNotEmpty() },
                    sentAt = LocalDateTime.parse(doc.getString("sentAt") ?: LocalDateTime.now().toString()),
                    readAt = doc.getString("readAt")?.takeIf { it.isNotEmpty() }?.let { LocalDateTime.parse(it) },
                    actionedAt = doc.getString("actionedAt")?.takeIf { it.isNotEmpty() }?.let { LocalDateTime.parse(it) },
                    data = @Suppress("UNCHECKED_CAST")
                    (doc.get("data") as? Map<String, String>) ?: emptyMap()
                )
            } catch (e: Exception) {
                Timber.e(e, "Failed to parse notification from document: ${doc.id}")
                null
            }
        }
    } catch (e: Exception) {
        Timber.e(e, "Failed to get notification history for user: $userId")
        emptyList()
    }

    /**
     * Mark notification as read.
     */
    override suspend fun markNotificationAsRead(notificationId: String): Boolean = try {
        val now = LocalDateTime.now()

        // Find the notification and update it
        val doc = firestore.collection(NOTIFICATIONS_COLLECTION)
            .document(notificationId)
            .get()
            .await()

        if (doc.exists()) {
            val userId = doc.getString("userId") ?: return false

            firestore.collection(NOTIFICATIONS_COLLECTION)
                .document(notificationId)
                .update("readAt", now.toString())
                .await()

            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection("notifications")
                .document(notificationId)
                .update("readAt", now.toString())
                .await()

            Timber.d("Notification marked as read: $notificationId")
            true
        } else {
            false
        }
    } catch (e: Exception) {
        Timber.e(e, "Failed to mark notification as read: $notificationId")
        false
    }

    /**
     * Delete a notification.
     */
    override suspend fun deleteNotification(notificationId: String): Boolean = try {
        val doc = firestore.collection(NOTIFICATIONS_COLLECTION)
            .document(notificationId)
            .get()
            .await()

        if (doc.exists()) {
            val userId = doc.getString("userId") ?: return false

            firestore.collection(NOTIFICATIONS_COLLECTION)
                .document(notificationId)
                .delete()
                .await()

            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection("notifications")
                .document(notificationId)
                .delete()
                .await()

            Timber.d("Notification deleted: $notificationId")
            true
        } else {
            false
        }
    } catch (e: Exception) {
        Timber.e(e, "Failed to delete notification: $notificationId")
        false
    }

    /**
     * Send notification via FCM.
     * Calls the sendNotification Cloud Function.
     */
    private fun sendViaFcm(
        token: String,
        title: String,
        body: String,
        notificationType: NotificationType,
        data: Map<String, String>
    ) {
        // Get the sendNotification Cloud Function
        val functions = com.google.firebase.functions.FirebaseFunctions.getInstance()
        
        // Prepare the payload
        val payload = mapOf(
            "token" to token,
            "title" to title,
            "body" to body,
            "type" to notificationType.name,
            "transactionId" to (data["transactionId"] ?: ""),
            "referenceNumber" to (data["referenceNumber"] ?: "")
        )

        // Call the Cloud Function
        functions.getHttpsCallable("sendNotification")
            .call(payload)
            .addOnSuccessListener { result ->
                Timber.d("FCM sent successfully for type: ${notificationType.name}")
            }
            .addOnFailureListener { error ->
                Timber.e(error, "Failed to send FCM for type: ${notificationType.name}")
            }
    }
}
