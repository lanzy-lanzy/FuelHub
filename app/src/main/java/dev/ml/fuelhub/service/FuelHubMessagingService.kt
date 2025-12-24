package dev.ml.fuelhub.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dev.ml.fuelhub.MainActivity
import dev.ml.fuelhub.R
import timber.log.Timber

/**
 * Firebase Cloud Messaging Service for handling incoming push notifications.
 *
 * Receives notifications for:
 * - Transaction created (for gas station users)
 * - Transaction dispensed/verified (for drivers)
 */
class FuelHubMessagingService : FirebaseMessagingService() {

    companion object {
        const val CHANNEL_TRANSACTION = "transaction_notifications"
        const val CHANNEL_ALERTS = "system_alerts"
        const val CHANNEL_ID_TRANSACTION = 1001
        const val CHANNEL_ID_ALERTS = 1002
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    /**
     * Called when a message is received from FCM.
     * Handles both data and notification payloads.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Timber.d("FCM message received from: ${remoteMessage.from}")

        // Handle data payload
        if (remoteMessage.data.isNotEmpty()) {
            Timber.d("Data payload: ${remoteMessage.data}")
            handleDataPayload(remoteMessage.data)
        }

        // Handle notification payload
        remoteMessage.notification?.let {
            Timber.d("Notification payload - Title: ${it.title}, Body: ${it.body}")
            showNotification(
                title = it.title ?: "FuelHub",
                body = it.body ?: "New notification",
                data = remoteMessage.data
            )
        }
    }

    /**
     * Called when a new token is generated.
     * This token should be stored in Firestore for the current user.
     */
    override fun onNewToken(token: String) {
        Timber.d("New FCM Token: $token")
        // Store this token in Firestore against the current user
        val sharedPref = getSharedPreferences("fuelhub_fcm", Context.MODE_PRIVATE)
        sharedPref.edit().putString("fcm_token", token).apply()

        // Send token to Firestore
        sendTokenToServer(token)
    }

    /**
     * Handle data payload from FCM message.
     */
    private fun handleDataPayload(data: Map<String, String>) {
        val notificationType = data["type"] ?: return
        val transactionId = data["transactionId"]
        val referenceNumber = data["referenceNumber"]
        val title = data["title"] ?: "FuelHub"
        val body = data["body"] ?: "Notification"

        Timber.d("Handling notification type: $notificationType")

        // Store notification in local database if needed
        // This can be used to display a notification history screen

        when (notificationType) {
            "TRANSACTION_CREATED" -> {
                // Gas station user has a new transaction to scan
                showNotification(
                    title = title,
                    body = body,
                    data = data,
                    channelId = CHANNEL_TRANSACTION,
                    notificationId = CHANNEL_ID_TRANSACTION
                )
            }
            "TRANSACTION_DISPENSED" -> {
                // Driver is notified that their transaction has been verified
                showNotification(
                    title = title,
                    body = body,
                    data = data,
                    channelId = CHANNEL_TRANSACTION,
                    notificationId = CHANNEL_ID_TRANSACTION
                )
            }
            "TRANSACTION_VERIFIED" -> {
                showNotification(
                    title = title,
                    body = body,
                    data = data,
                    channelId = CHANNEL_TRANSACTION,
                    notificationId = CHANNEL_ID_TRANSACTION
                )
            }
            "SYSTEM_ALERT" -> {
                showNotification(
                    title = title,
                    body = body,
                    data = data,
                    channelId = CHANNEL_ALERTS,
                    notificationId = CHANNEL_ID_ALERTS
                )
            }
            else -> {
                Timber.w("Unknown notification type: $notificationType")
            }
        }
    }

    /**
     * Display a notification in the system notification bar.
     */
    private fun showNotification(
        title: String,
        body: String,
        data: Map<String, String>,
        channelId: String = CHANNEL_TRANSACTION,
        notificationId: Int = CHANNEL_ID_TRANSACTION
    ) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Add data to intent for deep linking if needed
            data.forEach { (key, value) ->
                putExtra(key, value)
            }
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notificationBuilder.build())

        Timber.d("Notification displayed: $title")
    }

    /**
     * Create notification channels for Android 8+.
     * Required for showing notifications on Android Oreo and above.
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Transaction notifications channel
            val transactionChannel = NotificationChannel(
                CHANNEL_TRANSACTION,
                "Transaction Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for transaction status changes"
                enableVibration(true)
                setShowBadge(true)
            }

            // System alerts channel
            val alertsChannel = NotificationChannel(
                CHANNEL_ALERTS,
                "System Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Important system notifications and alerts"
                enableVibration(true)
                setShowBadge(true)
            }

            notificationManager.createNotificationChannel(transactionChannel)
            notificationManager.createNotificationChannel(alertsChannel)

            Timber.d("Notification channels created")
        }
    }

    /**
     * Send FCM token to the server (Firestore).
     * Stores the token in fcm_tokens collection for the current user.
     */
    private fun sendTokenToServer(token: String) {
        try {
            val firebaseAuth = com.google.firebase.auth.FirebaseAuth.getInstance()
            val userId = firebaseAuth.currentUser?.uid
            
            if (userId != null) {
                val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                firestore.collection("fcm_tokens")
                    .document(userId)
                    .set(mapOf(
                        "token" to token,
                        "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                    ))
                    .addOnSuccessListener {
                        Timber.d("FCM token saved to Firestore for user: $userId")
                    }
                    .addOnFailureListener { error ->
                        Timber.e(error, "Failed to save FCM token to Firestore")
                    }
            } else {
                Timber.w("No user logged in. Token will be saved when user logs in.")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error sending token to server: ${e.message}")
        }
    }
}
