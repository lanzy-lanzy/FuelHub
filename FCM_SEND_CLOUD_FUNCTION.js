/**
 * Firebase Cloud Function to send FCM notifications
 * Deploy with: firebase deploy --only functions
 * 
 * This function is triggered via HTTP to send push notifications
 * through Firebase Cloud Messaging (FCM).
 */

const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

// ============================================
// OPTION 1: HTTP Callable Function
// Recommended for testing and Android app integration
// ============================================

exports.sendNotification = functions.https.onCall(async (data, context) => {
  // Verify user is authenticated
  if (!context.auth) {
    throw new functions.https.HttpsError(
      "unauthenticated",
      "User must be authenticated"
    );
  }

  const { token, title, body, type, transactionId, referenceNumber } = data;

  if (!token || !title || !body) {
    throw new functions.https.HttpsError(
      "invalid-argument",
      "Missing required fields: token, title, body"
    );
  }

  const message = {
    token: token,
    notification: {
      title: title,
      body: body,
    },
    data: {
      type: type || "SYSTEM_ALERT",
      transactionId: transactionId || "",
      referenceNumber: referenceNumber || "",
    },
    android: {
      priority: "high",
      notification: {
        sound: "default",
        channelId: "transaction_notifications",
      },
    },
  };

  try {
    const response = await admin.messaging().send(message);
    console.log("FCM sent successfully:", response);
    return { success: true, messageId: response };
  } catch (error) {
    console.error("FCM send error:", error);
    throw new functions.https.HttpsError("internal", error.message);
  }
});

// ============================================
// OPTION 2: Send to multiple users (by role)
// ============================================

exports.sendNotificationToRole = functions.https.onCall(
  async (data, context) => {
    if (!context.auth) {
      throw new functions.https.HttpsError(
        "unauthenticated",
        "User must be authenticated"
      );
    }

    const { role, title, body, type } = data;

    if (!role || !title || !body) {
      throw new functions.https.HttpsError(
        "invalid-argument",
        "Missing required fields: role, title, body"
      );
    }

    try {
      // Get all users with the specified role
      const usersSnapshot = await admin
        .firestore()
        .collection("users")
        .where("role", "==", role)
        .where("isActive", "==", true)
        .get();

      const messages = [];

      for (const userDoc of usersSnapshot.docs) {
        const userId = userDoc.id;

        // Get FCM token
        const tokenDoc = await admin
          .firestore()
          .collection("fcm_tokens")
          .doc(userId)
          .get();

        if (tokenDoc.exists) {
          const token = tokenDoc.data().token;

          const message = {
            token: token,
            notification: {
              title: title,
              body: body,
            },
            data: {
              type: type || "SYSTEM_ALERT",
            },
            android: {
              priority: "high",
              notification: {
                sound: "default",
              },
            },
          };

          messages.push(
            admin
              .messaging()
              .send(message)
              .catch((error) => {
                console.error(`Failed to send to ${userId}:`, error);
              })
          );
        }
      }

      const results = await Promise.allSettled(messages);
      const successCount = results.filter(
        (r) => r.status === "fulfilled"
      ).length;

      console.log(
        `Sent ${successCount} notifications to ${role} users out of ${usersSnapshot.size}`
      );
      return { success: true, sent: successCount, total: usersSnapshot.size };
    } catch (error) {
      console.error("Batch send error:", error);
      throw new functions.https.HttpsError("internal", error.message);
    }
  }
);

// ============================================
// OPTION 3: On Transaction Created
// Firestore trigger to auto-notify on transaction creation
// ============================================

exports.onTransactionCreated = functions.firestore
  .document("transactions/{transactionId}")
  .onCreate(async (snap, context) => {
    const transaction = snap.data();
    const { driverId, gasStationId, referenceNumber } = transaction;

    try {
      // Get driver's FCM token
      const driverTokenDoc = await admin
        .firestore()
        .collection("fcm_tokens")
        .doc(driverId)
        .get();

      if (driverTokenDoc.exists) {
        const driverToken = driverTokenDoc.data().token;

        const message = {
          token: driverToken,
          notification: {
            title: "New Transaction",
            body: `A new transaction #${referenceNumber} has been created for you`,
          },
          data: {
            type: "TRANSACTION_CREATED",
            transactionId: context.params.transactionId,
            referenceNumber: referenceNumber,
          },
          android: {
            priority: "high",
            notification: {
              sound: "default",
              channelId: "transaction_notifications",
            },
          },
        };

        await admin.messaging().send(message);
        console.log("Transaction created notification sent to driver:", driverId);
      }

      return null;
    } catch (error) {
      console.error("Transaction notification error:", error);
      return null;
    }
  });

// ============================================
// OPTION 4: On Transaction Verified
// Notify both driver and gas station staff
// ============================================

exports.onTransactionVerified = functions.firestore
  .document("transactions/{transactionId}")
  .onUpdate(async (change, context) => {
    const newTransaction = change.after.data();
    const oldTransaction = change.before.data();

    // Check if status changed to verified
    if (oldTransaction.status !== "verified" && newTransaction.status === "verified") {
      const { driverId, gasStationId, referenceNumber } = newTransaction;

      try {
        // Notify driver
        const driverTokenDoc = await admin
          .firestore()
          .collection("fcm_tokens")
          .doc(driverId)
          .get();

        if (driverTokenDoc.exists) {
          const driverToken = driverTokenDoc.data().token;
          const driverMessage = {
            token: driverToken,
            notification: {
              title: "Transaction Verified",
              body: `Your transaction #${referenceNumber} has been verified`,
            },
            data: {
              type: "TRANSACTION_VERIFIED",
              transactionId: context.params.transactionId,
              referenceNumber: referenceNumber,
            },
            android: {
              priority: "high",
              notification: {
                sound: "default",
              },
            },
          };

          await admin.messaging().send(driverMessage);
        }

        // Notify gas station staff
        const gasStationTokenDoc = await admin
          .firestore()
          .collection("fcm_tokens")
          .doc(gasStationId)
          .get();

        if (gasStationTokenDoc.exists) {
          const gasStationToken = gasStationTokenDoc.data().token;
          const gasStationMessage = {
            token: gasStationToken,
            notification: {
              title: "Transaction Verified",
              body: `Transaction #${referenceNumber} has been verified`,
            },
            data: {
              type: "TRANSACTION_VERIFIED",
              transactionId: context.params.transactionId,
            },
            android: {
              priority: "high",
              notification: {
                sound: "default",
              },
            },
          };

          await admin.messaging().send(gasStationMessage);
        }

        console.log("Transaction verified notification sent");
        return null;
      } catch (error) {
        console.error("Transaction verification notification error:", error);
        return null;
      }
    }

    return null;
  });
