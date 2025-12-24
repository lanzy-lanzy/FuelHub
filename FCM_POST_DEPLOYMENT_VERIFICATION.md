# FCM Post-Deployment Verification Checklist

## ✅ Verification Steps (After Deployment)

Complete these steps to verify your FCM implementation is working.

---

## Step 1: Cloud Functions Deployment Verification

### 1.1 Check Deployed Functions

Run in terminal:
```bash
firebase functions:list
```

**Expected output:**
```
sendNotification
sendNotificationToRole
onTransactionCreated
onTransactionVerified
```

- [ ] All 4 functions listed
- [ ] No error messages

### 1.2 Check Firestore Collections Were Created

Firebase Console → Firestore Database

- [ ] Collection `fcm_tokens` exists (empty initially)
- [ ] Collection `notifications` exists (empty initially)

---

## Step 2: App Build Verification

### 2.1 Clean Build

```bash
cd path/to/FuelHub
./gradlew clean
./gradlew build
```

Expected: Build completes without errors

- [ ] Build succeeds
- [ ] No compilation errors
- [ ] "BUILD SUCCESSFUL" message

### 2.2 Install on Device

```bash
./gradlew installDebug
```

Or in Android Studio: Run → Run 'app'

- [ ] App installs successfully
- [ ] App launches without crashing

---

## Step 3: Token Storage Verification

### 3.1 Check Token is Generated and Stored

1. **Launch app**
   - [ ] App opens
   - [ ] Can login

2. **Check Logcat for Token**
   - Open Logcat in Android Studio (bottom panel)
   - Filter: `FuelHubMessaging`
   - Look for message: `D/FuelHubMessagingService: New FCM Token: eO6mN...`
   - [ ] Token appears (should be ~150 characters)

3. **Check Firestore Storage**
   - Firebase Console → Firestore
   - Collection: `fcm_tokens`
   - Should see document with your user ID
   - Document has field `token` with value
   - [ ] Token exists in Firestore
   - [ ] Token matches Logcat token

4. **Check SharedPreferences** (Optional)
   - Android Studio → Device File Explorer
   - Path: `/data/data/dev.ml.fuelhub/shared_prefs/fuelhub_fcm.xml`
   - Should contain: `<string name="fcm_token">eO6mN...</string>`
   - [ ] Token in SharedPreferences

### 3.2 Copy Token for Testing

Copy the FCM token (from Logcat or Firestore) - you'll need it next.

Example: `eO6mN5i_k8X2j5K...`

---

## Step 4: Test Notification Sending

### 4.1 Send Test Notification

1. **Go to Firebase Console**
   - Firebase Console → Select your project
   - Left sidebar → Functions
   - Click `sendNotification` function

2. **Open Testing Tab**
   - Click "Testing" tab

3. **Create Test Payload**
   - Replace `YOUR_TOKEN_HERE` with your token from Step 3
   - Paste this JSON:

   ```json
   {
     "token": "YOUR_TOKEN_HERE",
     "title": "Test Notification",
     "body": "FCM is working perfectly!",
     "type": "SYSTEM_ALERT"
   }
   ```

4. **Click "Call Function"**
   - [ ] Function call succeeds
   - [ ] Response shows `{ success: true }`
   - [ ] No error in response

### 4.2 Verify Notification Received

After calling function:

1. **Check Device Notification Tray**
   - Swipe down from top of device screen
   - Look for notification titled "Test Notification"
   - [ ] Notification appears in tray
   - [ ] Title is "Test Notification"
   - [ ] Body shows "FCM is working perfectly!"
   - [ ] Notification has dismiss and tap options

2. **Test Notification Tap**
   - Tap the notification
   - [ ] App opens
   - [ ] App doesn't crash
   - [ ] You're directed to MainActivity (or can navigate normally)

3. **Test Notification Dismiss**
   - Swipe down notification tray
   - Swipe left/right to dismiss notification
   - [ ] Notification dismisses
   - [ ] No errors

---

## Step 5: Cloud Function Logs Verification

### 5.1 Check Function Execution Logs

Run:
```bash
firebase functions:log
```

Look for output similar to:
```
D  sendNotification: New request received
D  sendNotification: Message sent to token: eO6mN...
D  sendNotification: Success
```

- [ ] Logs show function was called
- [ ] Logs show message was sent
- [ ] No error logs

### 5.2 Check for Any Errors

If you see errors in logs:
- [ ] Note the error message
- [ ] Check [FCM_IMPLEMENTATION_COMPLETE.md](FCM_IMPLEMENTATION_COMPLETE.md#troubleshooting) for solutions

---

## Step 6: Firestore Notification Storage Verification

### 6.1 Check Notification Stored

After sending test notification:

1. **Go to Firestore**
   - Firebase Console → Firestore Database
   - Collection: `notifications`
   - Should have a new document (not empty collection)

2. **View the Document**
   - Click the document
   - Should contain:
     - [ ] `id` field (UUID)
     - [ ] `userId` field (your user ID)
     - [ ] `title` field ("Test Notification")
     - [ ] `body` field ("FCM is working...")
     - [ ] `notificationType` field ("SYSTEM_ALERT")
     - [ ] `sentAt` field (timestamp)

---

## Step 7: User Collection Verification

### 7.1 Check User has FCM Token Stored

1. **Go to Firestore**
   - Collection: `users`
   - Document: [your-user-id]
   - Should have field: `fcmToken`

2. **Verify Token**
   - [ ] `fcmToken` field exists
   - [ ] Value matches token from Step 3

---

## Step 8: Test Automatic Notifications

### 8.1 Create Transaction and Verify Auto-Notification

This tests the `onTransactionCreated` Cloud Function.

1. **Create a Transaction in Firestore**
   - Go to Firestore
   - Collection: `transactions`
   - Click "Add Document"
   - Document ID: `test_transaction_123` (can be any ID)
   - Add fields:

   ```
   driverId: [your-user-id]
   gasStationId: test_station
   referenceNumber: TEST_001
   status: pending
   liters: 50
   amount: 2500
   createdAt: [current timestamp]
   ```

   - Click "Save"

2. **Wait 5-10 seconds**

3. **Check Device Notification**
   - Should receive automatic notification:
     - Title: "New Transaction" (or similar)
     - Body mentions transaction or reference number
   - [ ] Automatic notification received

4. **Check Cloud Function Logs**
   ```bash
   firebase functions:log | grep onTransactionCreated
   ```
   - [ ] Function was triggered
   - [ ] No errors

---

## Step 9: Test Automatic Verification Notifications

### 9.1 Update Transaction Status

This tests the `onTransactionVerified` Cloud Function.

1. **Update the Transaction**
   - Go to Firestore → transactions → test_transaction_123
   - Edit field: `status` → change to `verified`
   - Click "Save"

2. **Wait 5-10 seconds**

3. **Check Device Notification**
   - Should receive notification about verification
   - [ ] Verification notification received

4. **Check Cloud Function Logs**
   ```bash
   firebase functions:log | grep onTransactionVerified
   ```
   - [ ] Function was triggered
   - [ ] No errors

---

## Step 10: Test Background Notifications

### 10.1 Kill App and Test

1. **Close/Kill the App**
   - Android Studio: Click ⏹️ Stop button
   - Or: Device Settings → Apps → FuelHub → Stop

2. **App is now in background**

3. **Send Another Test Notification**
   - Firebase Console → Functions → sendNotification → Testing
   - Same payload as before
   - Click "Call Function"

4. **Check Device Notification**
   - [ ] Notification appears even though app is closed
   - [ ] Title is correct
   - [ ] Body is correct

5. **Tap Notification**
   - [ ] App launches
   - [ ] App opens correctly

---

## Summary Checklist

### Prerequisites ✅
- [ ] Node.js installed
- [ ] Firebase CLI installed
- [ ] Logged into Firebase

### Deployment ✅
- [ ] Cloud Functions deployed
- [ ] Firestore rules updated
- [ ] App rebuilt

### Verification (This Checklist) ✅
- [ ] Functions deployment verified
- [ ] App builds without errors
- [ ] App installs on device
- [ ] FCM token generated
- [ ] Token stored in Firestore
- [ ] Token stored in SharedPreferences
- [ ] Test notification sent
- [ ] Notification received on device
- [ ] Notification can be tapped
- [ ] Cloud Function logs show success
- [ ] Notification stored in Firestore
- [ ] User has fcmToken field
- [ ] Automatic transaction notification works
- [ ] Automatic verification notification works
- [ ] Background notifications work

---

## ✅ SUCCESS!

If all items above are checked, your FCM implementation is **COMPLETE AND WORKING**.

---

## 🆘 If Something Failed

### Notification Not Received?

1. Check permission: Device Settings → Apps → FuelHub → Notifications
2. Check token: Logcat for "New FCM Token"
3. Check Firestore: fcm_tokens/{userId} has token field
4. Check logs: `firebase functions:log`
5. Try restarting device

### Cloud Function Error?

1. Check logs: `firebase functions:log`
2. Check function code: Compare with FCM_SEND_CLOUD_FUNCTION.js
3. Redeploy: `firebase deploy --only functions`
4. Check Node version: `node --version` (should be 14+)

### App Won't Build?

1. Clean: `./gradlew clean`
2. Rebuild: `./gradlew build`
3. Check dependencies in build.gradle.kts
4. Sync: Android Studio → File → Sync Now

### Still Having Issues?

1. Review [FCM_IMPLEMENTATION_COMPLETE.md](FCM_IMPLEMENTATION_COMPLETE.md#troubleshooting)
2. Check [FCM_STEP_BY_STEP_DEPLOYMENT.md](FCM_STEP_BY_STEP_DEPLOYMENT.md#troubleshooting)
3. Review [FCM_ARCHITECTURE_VISUAL.txt](FCM_ARCHITECTURE_VISUAL.txt) to understand flow

---

## 📝 Notes

**Device Token Found:** _____________________

**Test Notification Sent at:** _____________________

**First Notification Received at:** _____________________

**All Tests Passed:** ☐ Yes

---

## 🎉 Next Steps

Your FCM implementation is now **FULLY FUNCTIONAL**.

You can now:

1. **Send notifications from your app code:**
   ```kotlin
   notificationRepository.sendNotification(
       userId = "driver123",
       title = "New Transaction",
       body = "You have a new gas slip",
       notificationType = NotificationType.TRANSACTION_CREATED
   )
   ```

2. **Automatic notifications are working:**
   - Transaction created → driver notified
   - Transaction verified → both notified

3. **Monitor in production:**
   ```bash
   firebase functions:log
   ```

---

**Status: VERIFIED & READY FOR PRODUCTION** ✅
