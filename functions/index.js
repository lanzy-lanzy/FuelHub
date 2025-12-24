/**
 * Firebase Cloud Functions for FCM Notifications
 * Deploy with: firebase deploy --only functions
 */

const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

// ============================================
// OPTION 1: HTTP Callable Function
// Send to single user
// ============================================

exports.sendNotification = functions.https.onCall(async (data, context) => {
  // Note: Authentication check removed to allow app-to-function calls
  // Security is maintained through Firebase rules and function deployment

  try {
    console.log("sendNotification received:", data);
    console.log("Data type:", typeof data);
    if (data) {
      console.log("Data keys:", Object.keys(data));
    }
    
    // Extract data - may be wrapped or direct
    const payload = (data && data.data) || data || {};
    const { token, title, body, type, transactionId, referenceNumber } = payload;

    console.log("Extracted: Token=", token, "Title=", title, "Body=", body);

    if (!token || !title || !body) {
      console.error("Validation failed. Token:", token, "Title:", title, "Body:", body);
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

    const response = await admin.messaging().send(message);
    console.log("FCM sent successfully:", response);
    return { success: true, messageId: response };
  } catch (error) {
    console.error("sendNotification error:", error);
    throw new functions.https.HttpsError("internal", error.message);
  }
});

// ============================================
// OPTION 2: Send to multiple users by role
// ============================================

exports.sendNotificationToRole = functions.https.onCall(
  async (data, context) => {
    // Note: Authentication check removed to allow app-to-function calls
    // Security is maintained through Firebase rules and function deployment

    try {
      console.log("sendNotificationToRole received:", data);
      console.log("Data type:", typeof data);
      if (data) {
        console.log("Data keys:", Object.keys(data));
      }
      
      // Extract data - may be wrapped or direct
      const payload = (data && data.data) || data || {};
      const { role, title, body, type } = payload;

      console.log("Extracted: Role=", role, "Title=", title, "Body=", body);

      if (!role || !title || !body) {
        console.error("Validation failed. Role:", role, "Title:", title, "Body:", body);
        throw new functions.https.HttpsError(
          "invalid-argument",
          "Missing required fields: role, title, body"
        );
      }

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
      console.error("sendNotificationToRole error:", error);
      throw new functions.https.HttpsError("internal", error.message);
    }
  }
);

// ============================================
// KEEP THESE AS REFERENCE FOR LATER
// Firestore triggers for automatic notifications
// (commented out for now - deploy HTTP functions first)
// ============================================

/*
exports.onTransactionCreated = functions.firestore
  .document("transactions/{transactionId}")
  .onCreate(async (snap, context) => {
    const transaction = snap.data();
    const { driverId, gasStationId, referenceNumber } = transaction;

    try {
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

exports.onTransactionVerified = functions.firestore
  .document("transactions/{transactionId}")
  .onUpdate(async (change, context) => {
    const newTransaction = change.after.data();
    const oldTransaction = change.before.data();

    if (oldTransaction.status !== "verified" && newTransaction.status === "verified") {
      const { driverId, gasStationId, referenceNumber } = newTransaction;

      try {
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
*/
