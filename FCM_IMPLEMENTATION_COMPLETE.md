# FCM Implementation - Complete Setup

## Status: READY TO IMPLEMENT

### What's Already Done in the App:
✅ Firebase Cloud Messaging dependency added  
✅ FCM service (`FuelHubMessagingService`) created and declared in manifest  
✅ Notification channels configured (Transaction & Alerts)  
✅ FCM token storage in SharedPreferences and Firestore  
✅ FCM token sent to Firestore when user logs in  
✅ Notification repository ready to send via FCM  
✅ Firebase Functions dependency added  

---

## SETUP STEPS

### Step 1: Deploy Cloud Functions

**Prerequisites:**
- Firebase CLI installed (`npm install -g firebase-tools`)
- Node.js installed
- Google Cloud project set up

**Steps:**

1. In your Firebase project folder, initialize Cloud Functions:
```bash
firebase init functions
```

2. Select your Firebase project and language (JavaScript)

3. Copy the content from `FCM_SEND_CLOUD_FUNCTION.js` to your `functions/index.js` file

4. Install dependencies in functions folder:
```bash
cd functions
npm install
```

5. Deploy:
```bash
firebase deploy --only functions
```

**Verify in Firebase Console:**
- Go to Firebase Console → Functions
- You should see 4 functions deployed:
  - `sendNotification` (HTTP Callable)
  - `sendNotificationToRole` (HTTP Callable)
  - `onTransactionCreated` (Firestore trigger)
  - `onTransactionVerified` (Firestore trigger)

---

### Step 2: Update Firestore Security Rules

Your Firestore rules need to allow:
1. Token storage
2. Notification creation

Add these rules:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Allow users to store their own FCM token
    match /fcm_tokens/{userId} {
      allow read, write: if request.auth.uid == userId;
    }
    
    // Allow notification access
    match /notifications/{notificationId} {
      allow read, create: if request.auth != null;
      allow update, delete: if request.auth.uid == resource.data.userId;
    }
    
    match /users/{userId}/notifications/{notificationId} {
      allow read, create, update, delete: if request.auth.uid == userId;
    }
  }
}
```

---

### Step 3: Rebuild Your App

In Android Studio:

```bash
# Clean build
./gradlew clean

# Build
./gradlew build

# Or install on device
./gradlew installDebug
```

---

## TESTING FCM

### Manual Test via Cloud Functions:

1. Go to Firebase Console → Functions
2. Click on `sendNotification`
3. Click "Testing" tab
4. Set up test payload:

```json
{
  "token": "YOUR_DEVICE_FCM_TOKEN",
  "title": "Test Notification",
  "body": "This is a test notification",
  "type": "SYSTEM_ALERT"
}
```

5. Click "Call Function"

### Find Your Device's FCM Token:

In Android Logcat (after app starts):
```
D/FuelHubMessagingService: New FCM Token: eO6mN...
```

Or in SharedPreferences:
```kotlin
val sharedPref = context.getSharedPreferences("fuelhub_fcm", Context.MODE_PRIVATE)
val token = sharedPref.getString("fcm_token", "")
Log.d("FCM Token", token)
```

### Debug Checklist:

- [ ] Check logcat for "FCM Token received" message
- [ ] Verify token is stored in `fcm_tokens` collection in Firestore
- [ ] Send test notification via Cloud Function
- [ ] Verify notification appears in system tray
- [ ] Check notification is stored in Firestore `notifications` collection
- [ ] Test with app in background
- [ ] Test with app in foreground

---

## REAL-WORLD USAGE

### When Transaction Created:

The `onTransactionCreated` Cloud Function automatically:
1. Gets driver's FCM token
2. Sends notification: "New transaction #XYZ created"
3. Driver receives push notification

### When Transaction Verified:

The `onTransactionVerified` Cloud Function automatically:
1. Notifies driver: "Your transaction verified"
2. Notifies gas station staff: "Transaction verified"

### Manual Notification Send:

From your app code:

```kotlin
// In ViewModel or Repository
notificationRepository.sendNotification(
    userId = "driverId",
    title = "New Transaction",
    body = "You have a new transaction to process",
    notificationType = NotificationType.TRANSACTION_CREATED,
    transactionId = "tx_123",
    referenceNumber = "REF_456"
)
```

---

## TROUBLESHOOTING

### Issue: "Cloud Function not found"

**Fix:** Ensure Cloud Functions are deployed:
```bash
firebase deploy --only functions
firebase functions:list
```

### Issue: No notifications received

**Check:**
1. Device has internet connection
2. FCM token exists in Firestore
3. Cloud Function logs for errors: `firebase functions:log`
4. App has POST_NOTIFICATIONS permission (Android 13+)

### Issue: Notification permission not granted

**Already fixed in:** `AndroidManifest.xml` has `POST_NOTIFICATIONS` permission  
**User approval:** Required for Android 13+, handled at runtime

### Issue: Duplicate notifications

**Cause:** Receiving in both foreground and background  
**Fix:** Check `onMessageReceived` logic in `FuelHubMessagingService`

---

## FILES CHANGED

1. ✅ `app/build.gradle.kts` - Added Firebase Functions
2. ✅ `app/src/main/java/dev/ml/fuelhub/data/repository/FirebaseNotificationRepositoryImpl.kt` - Implemented sendViaFcm
3. ✅ `app/src/main/java/dev/ml/fuelhub/service/FuelHubMessagingService.kt` - Already complete
4. ⏳ `FCM_SEND_CLOUD_FUNCTION.js` - Copy to your Firebase functions folder

---

## NEXT STEPS

1. ✅ Read this guide
2. ⏳ Deploy Cloud Functions
3. ⏳ Update Firestore security rules
4. ⏳ Rebuild app
5. ⏳ Test manually
6. ⏳ Monitor Cloud Function logs during testing
7. ✅ Done!

---

## CLOUD FUNCTION DETAILS

### sendNotification (HTTP Callable)
- **Input:** token, title, body, type, transactionId, referenceNumber
- **Output:** { success: true, messageId: "..." }
- **Usage:** Called from app when sending notification to single user

### sendNotificationToRole (HTTP Callable)
- **Input:** role, title, body, type
- **Output:** { success: true, sent: 5, total: 8 }
- **Usage:** Send notification to all users with specific role

### onTransactionCreated (Firestore Trigger)
- **Trigger:** New document in `transactions` collection
- **Action:** Send notification to driver
- **Auto:** No code needed in app

### onTransactionVerified (Firestore Trigger)
- **Trigger:** Transaction status changes to "verified"
- **Action:** Notify driver and gas station staff
- **Auto:** No code needed in app

---

## SECURITY NOTES

✅ Cloud Functions require Firebase Authentication  
✅ FCM tokens are user-specific and auto-managed  
✅ Tokens auto-refresh when needed  
✅ Firestore rules restrict token access to user only  

---

**Status:** Ready to deploy! 🚀
