package dev.ml.fuelhub.domain.repository

import dev.ml.fuelhub.data.model.NotificationPayload
import dev.ml.fuelhub.data.model.NotificationType

/**
 * Repository interface for managing notifications.
 * Handles sending, storing, and retrieving notifications.
 */
interface NotificationRepository {

    /**
     * Send a notification to a specific user.
     *
     * @param userId Target user ID
     * @param title Notification title
     * @param body Notification body
     * @param notificationType Type of notification
     * @param transactionId Associated transaction ID (if any)
     * @param referenceNumber Gas slip reference number (if any)
     * @param data Additional data to send
     */
    suspend fun sendNotification(
        userId: String,
        title: String,
        body: String,
        notificationType: NotificationType,
        transactionId: String? = null,
        referenceNumber: String? = null,
        data: Map<String, String> = emptyMap()
    ): Boolean

    /**
     * Send notification to all users with a specific role.
     */
    suspend fun sendNotificationToRole(
        role: String,
        title: String,
        body: String,
        notificationType: NotificationType,
        data: Map<String, String> = emptyMap()
    ): Boolean

    /**
     * Get user's FCM token.
     */
    suspend fun getUserFcmToken(userId: String): String?

    /**
     * Update/store user's FCM token.
     */
    suspend fun storeUserFcmToken(userId: String, token: String): Boolean

    /**
     * Get notification history for a user.
     */
    suspend fun getNotificationHistory(userId: String, limit: Int = 50): List<NotificationPayload>

    /**
     * Mark notification as read.
     */
    suspend fun markNotificationAsRead(notificationId: String): Boolean

    /**
     * Delete a notification.
     */
    suspend fun deleteNotification(notificationId: String): Boolean
}
