# Deploy Cloud Function to Send FCM Messages

## Why Cloud Function?

Currently:
```
Create Transaction → Save to Firestore → Done ✅
                     (notification created)
                     ✗ But NOT sent to device
```

After Cloud Function:
```
Create Transaction → Save to Firestore → Cloud Function triggers
                                          → Get token → Send FCM ✅
```

---

## 📋 Step-by-Step Cloud Function Deployment

### Step 1: Go to Firebase Console Functions

1. Open Firebase Console
2. Click **Functions** (left menu)
3. Click **Create function**

---

### Step 2: Configure Function

Fill in these fields:

**Function name:**
```
sendTransactionNotification
```

**Runtime:**
```
Node.js 18
```

**Trigger:**
- Region: `us-central1` (or your preferred region)
- Event type: Click dropdown → Select `Firestore`
- Choose: `Firestore`

**Collection path:**
```
notifications
```

**Event type:**
- Click dropdown
- Select: `onCreate` (on document create)

Then click **Create**

---

### Step 3: Replace Function Code

The editor will open with default code. **DELETE ALL OF IT** and paste:

```javascript
const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

exports.sendTransactionNotification = functions.firestore
    .document('notifications/{notificationId}')
    .onCreate(async (snap, context) => {
        const notification = snap.data();
        
        console.log(`📨 New notification created: ${notification.id}`);
        console.log(`📨 Sending to user: ${notification.userId}`);
        
        // Get user's FCM token from fcm_tokens collection
        try {
            const tokenDoc = await admin.firestore()
                .collection('fcm_tokens')
                .doc(notification.userId)
                .get();
            
            if (!tokenDoc.exists) {
                console.log(`❌ No FCM token found for user: ${notification.userId}`);
                return;
            }
            
            const token = tokenDoc.data().token;
            console.log(`✓ Token found: ${token.substring(0, 30)}...`);
            
            // Build FCM message
            const message = {
                notification: {
                    title: notification.title,
                    body: notification.body,
                },
                data: {
                    type: notification.notificationType,
                    transactionId: notification.transactionId || '',
                    referenceNumber: notification.referenceNumber || '',
                    driverName: notification.data?.driverName || '',
                },
                token: token,
            };
            
            // Send via FCM
            const response = await admin.messaging().send(message);
            console.log(`✓ Message sent successfully: ${response}`);
            
        } catch (error) {
            console.error(`❌ Error sending notification:`, error);
        }
    });
```

---

### Step 4: Deploy Function

Click **Deploy** button at bottom right

**Wait 2-3 minutes** for deployment to complete

You'll see:
```
✓ Function deployed successfully
```

---

## 🧪 Test the Cloud Function

### Step 1: Create Transaction in App
1. Open your app
2. Login as DISPATCHER user
3. Create a new fuel transaction
4. Submit

### Step 2: Watch Cloud Function Logs
1. Go back to Firebase Console
2. Functions → Click `sendTransactionNotification`
3. Click **Logs** tab
4. Wait 10 seconds

### Step 3: Check for Success Message

You should see in the logs:
```
📨 New notification created: 2f3babb7-0c9a-45ba-a7ad...
📨 Sending to user: user_id_456
✓ Token found: eGp0Ax7qLFI:APA91bE6a1X2k3j4X5...
✓ Message sent successfully: 0:1234567890123456
```

### Step 4: Check Device for Notification

On your GAS_STATION user device:
- Check notification center
- **Should see notification appear within 1-2 seconds!**

---

## ✅ Success Indicators

**In Cloud Function Logs:**
- ✅ "Message sent successfully"
- ✅ No errors

**On Device:**
- ✅ Notification appears in notification center
- ✅ Title: "New Transaction: FS-xxxxx"
- ✅ Body: Shows vehicle type and liters

---

## ❌ If It Fails

### Error: "No FCM token found"
**Cause**: User logged in before you added token storage code
**Fix**: 
1. Delete app: `adb uninstall dev.ml.fuelhub`
2. Rebuild: `./gradlew clean build`
3. Reinstall: `./gradlew installDebug`
4. Login again → Token will be stored
5. Try creating transaction again

### Error: "Permission denied"
**Cause**: Firestore rules block function
**Fix**:
1. Go to Firestore → Rules tab
2. Make sure you have:
```javascript
match /notifications/{notificationId} {
  allow create: if request.auth != null;
  allow read, update: if request.auth != null;
}

match /fcm_tokens/{userId} {
  allow read: if true;  // Cloud Function needs to read tokens
  allow write: if request.auth.uid == userId;
}
```
3. Click Publish

### Error: "Function failed"
**Cause**: Code error in function
**Fix**:
1. Check Cloud Function logs for exact error
2. Compare your code with the code block above
3. Make sure there are no typos
4. Redeploy

---

## 📊 Complete Flow After Cloud Function

```
1. User logs in
   → FCM token stored in Firestore ✅

2. DISPATCHER creates transaction
   → Transaction saved to Firestore ✅
   → Notification document created ✅
   
3. Cloud Function triggers (automatic)
   → Gets GAS_STATION user token
   → Sends FCM message
   → Message delivered to device ✅
   
4. GAS_STATION user's device
   → FuelHubMessagingService.onMessageReceived()
   → Shows notification in notification center ✅
   
5. GAS_STATION user
   → Taps notification
   → Opens app
   → Scans QR code
```

---

## ⏱️ Timeline

```
Deployment: 2-3 minutes
Testing: 5 minutes
Total: ~10 minutes

After this:
- ✅ Notifications send automatically
- ✅ No manual Firebase Console test needed
- ✅ Real app notifications working!
```

---

## 🔍 Monitor Function Performance

In Firebase Console:

**Functions → sendTransactionNotification:**
- Click **Metrics** tab
- See invocation count
- See error rate
- See execution time

**For debugging:**
- Click **Logs** tab
- Filter by your test timestamp
- See exact what happened

---

## 🚀 Next Steps After Successful Test

1. ✅ Token storage working
2. ✅ Cloud Function deployed
3. ✅ Notifications being sent
4. ✅ Device receiving notifications

**Everything is now working!** 🎉

---

## 📱 Full End-to-End Test

**Device 1 (DISPATCHER):**
1. Login
2. Create transaction

**Device 2 (GAS_STATION):**
1. Watch notification center
2. Notification should appear within 1-2 seconds
3. Tap notification
4. App opens to QR scanner

**Success**: Notification appears and app opens ✅

---

## Troubleshooting Reference

| Issue | Solution |
|-------|----------|
| No logs appear | Wait 30 sec, refresh console |
| "No token found" | Logout/login on test device |
| "Permission denied" | Check Firestore rules |
| Notification not showing | Check device notification settings |
| Function fails to deploy | Check for syntax errors in code |

---

**Ready to deploy? Go to Firebase Console → Functions → Create now!** 🚀
