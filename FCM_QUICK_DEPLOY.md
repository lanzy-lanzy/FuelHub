# FCM Quick Deploy Guide

## 3 Simple Steps

### 1️⃣ Deploy Cloud Functions

```bash
# Install Firebase CLI (one time)
npm install -g firebase-tools

# Login to Firebase
firebase login

# In your Firebase project directory, initialize functions (if not already done)
firebase init functions

# Copy the Cloud Function code
# From: FCM_SEND_CLOUD_FUNCTION.js
# To: functions/index.js

# Install dependencies
cd functions
npm install
cd ..

# Deploy
firebase deploy --only functions
```

**Expected output:**
```
✔ Deploy complete!

Function URL (sendNotification): https://us-central1-your-project.cloudfunctions.net/sendNotification
```

---

### 2️⃣ Rebuild & Test Your App

```bash
# In Android Studio or terminal
./gradlew clean
./gradlew installDebug

# Or just build in Android Studio
```

---

### 3️⃣ Verify & Send Test Notification

**Get your device's FCM token:**

Option A - From Logcat:
- Run app
- Open Logcat
- Search for "New FCM Token:"
- Copy the token

Option B - From Firestore:
- Firebase Console → Firestore
- Navigate to `fcm_tokens` collection
- Find your user ID
- Copy the token

**Send test notification:**
- Firebase Console → Functions
- Click `sendNotification`
- Go to "Testing" tab
- Paste this:

```json
{
  "token": "paste_your_token_here",
  "title": "Test Notification",
  "body": "FCM is working!",
  "type": "SYSTEM_ALERT"
}
```

- Click "Call Function"
- Check your phone for notification

---

## Done! 🎉

Your FCM implementation is now complete and working.

**What happens automatically now:**
- ✅ When a transaction is created, driver gets notified
- ✅ When a transaction is verified, both driver and gas station staff are notified
- ✅ You can send custom notifications from code

---

## If Something Goes Wrong

### No notification received?

Check these (in order):

1. **Device FCM token stored?**
   ```
   Firestore → fcm_tokens → your_user_id → should have 'token' field
   ```

2. **Cloud Function deployed?**
   ```bash
   firebase functions:list
   ```

3. **App has permission?**
   ```
   Settings → Apps → FuelHub → Notifications → Allow
   ```

4. **Check Cloud Function logs:**
   ```bash
   firebase functions:log
   ```

---

## Cloud Functions Already Included

No need to create them - they're in `FCM_SEND_CLOUD_FUNCTION.js`:

- `sendNotification` - Send to single user
- `sendNotificationToRole` - Send to all users with a role  
- `onTransactionCreated` - Auto-notify when transaction created
- `onTransactionVerified` - Auto-notify when transaction verified

---

**That's it! Your push notifications are ready.** ✅
