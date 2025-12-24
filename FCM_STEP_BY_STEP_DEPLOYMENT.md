# FCM Deployment - Step by Step (Beginner Friendly)

## Prerequisites Check

Before starting, make sure you have:

- [ ] Firebase project created
- [ ] Google Services JSON downloaded (already done ✅)
- [ ] Android app already builds and runs
- [ ] User can login

---

## STEP 1: Install Required Tools

### 1.1 Install Node.js (if not already installed)

**Windows:**
1. Go to https://nodejs.org/
2. Download LTS version
3. Install it (accept all defaults)
4. Verify in terminal:
   ```bash
   node --version
   npm --version
   ```

### 1.2 Install Firebase CLI

**All Platforms:**
```bash
npm install -g firebase-tools
```

Verify:
```bash
firebase --version
```

---

## STEP 2: Set Up Firebase Functions Locally

### 2.1 Login to Firebase

```bash
firebase login
```

This will:
- Open browser to Google sign-in
- Ask you to sign in with your Firebase account
- Close browser when done

Verify:
```bash
firebase projects:list
```

You should see your project listed.

### 2.2 Initialize Firebase Functions (if not done)

In your Firebase project directory (where you have `firebase.json`):

```bash
firebase init functions
```

**When prompted:**
- Select your project: (choose your FuelHub project)
- Language: JavaScript
- ESLint: No
- Install dependencies: Yes

This creates a `functions/` folder.

---

## STEP 3: Add Cloud Functions Code

### 3.1 Copy Function Code

1. Open `FCM_SEND_CLOUD_FUNCTION.js` file (in FuelHub root)
2. Copy ALL the code
3. Navigate to `functions/index.js`
4. Replace entire contents with the copied code
5. Save

### 3.2 Install Dependencies

In the `functions/` folder, make sure these are in `package.json`:

```json
{
  "dependencies": {
    "firebase-admin": "^11.11.0",
    "firebase-functions": "^4.4.0"
  }
}
```

If missing, manually add them to `functions/package.json`, then run:

```bash
cd functions
npm install
cd ..
```

---

## STEP 4: Update Firestore Rules

### 4.1 Go to Firestore Console

1. Firebase Console → Your Project
2. Left sidebar → Firestore Database
3. Click "Rules" tab

### 4.2 Replace Rules

Replace everything with:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Allow users to read/write their own data
    match /users/{userId} {
      allow read, update: if request.auth.uid == userId;
      allow create: if request.auth.uid == userId;
      
      match /notifications/{notificationId} {
        allow read, write: if request.auth.uid == userId;
      }
    }
    
    // Allow users to manage their own FCM token
    match /fcm_tokens/{userId} {
      allow read, write: if request.auth.uid == userId;
    }
    
    // Allow reading/writing transactions
    match /transactions/{transactionId} {
      allow read, write: if request.auth != null;
    }
    
    // Allow notifications collection
    match /notifications/{notificationId} {
      allow read, create: if request.auth != null;
      allow update, delete: if request.auth.uid == resource.data.userId;
    }
  }
}
```

Click "Publish"

---

## STEP 5: Deploy Cloud Functions

### 5.1 Deploy

From your project root (where `firebase.json` is):

```bash
firebase deploy --only functions
```

### 5.2 Watch for Success

You should see:
```
✔ Deploy complete!

Function URL (sendNotification): https://us-central1-your-project.cloudfunctions.net/sendNotification
```

If you see errors, check the troubleshooting section below.

---

## STEP 6: Rebuild Your Android App

### 6.1 In Android Studio

1. File → Sync Now
2. Or: Build → Rebuild Project
3. Wait for completion

### 6.2 Or from Terminal

```bash
cd path/to/FuelHub
./gradlew clean
./gradlew build
```

---

## STEP 7: Test the Implementation

### 7.1 Install App on Device/Emulator

```bash
./gradlew installDebug
```

Or in Android Studio: Run → Run 'app'

### 7.2 Login to App

1. Launch app
2. Login with test account
3. App should start normally

### 7.3 Get Your FCM Token

**Option A: From Logcat**
1. In Android Studio, open Logcat (bottom panel)
2. Search for "New FCM Token"
3. Look for message like:
   ```
   D/FuelHubMessagingService: New FCM Token: eO6mN5i_k8X...
   ```
4. Copy the full token

**Option B: From Firestore**
1. Firebase Console → Firestore
2. Expand `fcm_tokens` collection
3. Click your user ID
4. Copy the `token` field value

### 7.4 Send Test Notification

1. Firebase Console → Functions
2. Click `sendNotification` function
3. Go to "Testing" tab
4. Paste this JSON:
   ```json
   {
     "token": "PASTE_YOUR_TOKEN_HERE",
     "title": "Test Notification",
     "body": "Your FCM is working!",
     "type": "SYSTEM_ALERT"
   }
   ```
5. Click "Call Function"
6. Check your device - notification should appear!

---

## STEP 8: Verify Everything Works

### Checklist:

- [ ] App builds without errors
- [ ] App launches after login
- [ ] Can see FCM token in Firestore (`fcm_tokens` collection)
- [ ] Can see token in SharedPreferences (via Android Studio)
- [ ] Test notification received successfully
- [ ] Notification shows in system tray
- [ ] Can tap notification and app opens
- [ ] Notification stored in Firestore

If all checked, you're done! ✅

---

## Troubleshooting

### Issue: "Cloud Functions not found" when deploying

**Solution:**
```bash
# Make sure you're in the right directory
cd path/to/your/firebase/project

# Check if firebase.json exists
ls firebase.json

# Try deploying again
firebase deploy --only functions
```

---

### Issue: "Error: Unable to find firebase.json"

**Solution:**
```bash
# Make sure you initialized Firebase in correct directory
firebase init

# Or use --project flag
firebase deploy --only functions --project=your-project-id
```

---

### Issue: "Function ... not found" when calling from app

**Solution:**
1. Check functions deployed:
   ```bash
   firebase functions:list
   ```
2. Should show:
   - sendNotification
   - sendNotificationToRole
   - onTransactionCreated
   - onTransactionVerified

If missing, redeploy:
```bash
firebase deploy --only functions
```

---

### Issue: No notification received on device

**Check in order:**

1. **Does FCM token exist?**
   - Firebase Console → Firestore → fcm_tokens → your user ID
   - Should have a `token` field

2. **Is token stored in app?**
   - Android Studio → View → Tool Windows → Logcat
   - Search "New FCM Token"
   - If not seen, app didn't receive token from Firebase

3. **Is notification permission granted?**
   - Device Settings → Apps → FuelHub → Permissions → Notifications
   - Toggle "Allow notifications"

4. **Check Cloud Function logs:**
   ```bash
   firebase functions:log
   ```
   - Look for errors

5. **Try restarting device**

---

### Issue: "Missing billing information"

**Solution:**
- Firestore free tier (1GB storage) is enough for testing
- Only Firebase Functions on Blaze plan costs money (but free tier: 2 million invocations/month)

---

### Issue: Emulator doesn't receive notifications

**Known Issue:** Firebase Cloud Messaging doesn't work on Android emulators without Google Play Services

**Solution:** Test on a real device or use emulator with Google APIs

---

## Advanced Testing

### Send Notification to All Drivers

Use `sendNotificationToRole` function:

```json
{
  "role": "driver",
  "title": "System Alert",
  "body": "Maintenance in progress",
  "type": "SYSTEM_ALERT"
}
```

---

### Monitor Cloud Function Logs

```bash
firebase functions:log

# Or with filtering
firebase functions:log --limit=50
```

---

### Test Transaction Auto-Notification

1. Create a transaction in Firestore manually:
   ```
   transactions/{transactionId}
   driverId: "your_driver_id"
   referenceNumber: "TEST_001"
   status: "pending"
   ```

2. Driver should receive notification automatically

3. Change status to "verified"

4. Driver and station staff should receive notification

---

## Post-Deployment Checklist

After everything is working:

- [ ] Cloud Functions deployed successfully
- [ ] Firestore Rules updated
- [ ] App rebuilt with new dependencies
- [ ] Test notification sent and received
- [ ] Token stored in Firestore
- [ ] No errors in Cloud Function logs

---

## Next Steps (In Your App)

Now you can:

1. **Send from code:**
   ```kotlin
   notificationRepository.sendNotification(
       userId = "driver123",
       title = "New Transaction",
       body = "You have a new gas slip",
       notificationType = NotificationType.TRANSACTION_CREATED
   )
   ```

2. **Automatic notifications work:**
   - When transaction created → driver notified
   - When transaction verified → both notified

3. **Monitor:**
   - Check Cloud Function logs
   - View notifications in Firestore
   - Check device notification history

---

## Summary

You've successfully:
1. ✅ Deployed Firebase Cloud Functions
2. ✅ Updated Firestore security rules
3. ✅ Rebuilt Android app
4. ✅ Tested push notifications
5. ✅ Verified FCM token storage
6. ✅ Confirmed notification delivery

Your FCM implementation is **COMPLETE** and **WORKING**! 🎉

---

**Questions?** Check the other FCM documentation files:
- `FCM_SETUP_SUMMARY.md` - Quick overview
- `FCM_ARCHITECTURE_VISUAL.txt` - Visual diagrams
- `FCM_IMPLEMENTATION_COMPLETE.md` - Detailed reference
