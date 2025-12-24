# Nothing Sent - Quick Diagnostic

## ❌ Issue: Cloud Function Test Shows "Nothing Sent"

When you test, it says sent but nothing arrives on device.

---

## 🔍 Quick Diagnosis Checklist

### Check 1: Is Your Cloud Function Actually Deployed?

**Go to Firebase Console:**
1. Click **Functions** (left menu)
2. Look for `sendTransactionNotification` function
3. Is there a **green checkmark**? 
   - YES ✅ → Continue to Check 2
   - NO ❌ → Deploy it again (see below)

**If you see RED ERROR:**
- Function failed to deploy
- Click function → See error message
- Common error: Syntax error in code
- **Fix**: Redeploy with correct code from `FCM_CLOUD_FUNCTION_SETUP.md`

---

### Check 2: Is Function Running?

**In Firebase Console:**
1. Click **Functions** → `sendTransactionNotification`
2. Click **Logs** tab
3. Create a transaction in your app
4. Wait 10 seconds and **Refresh the logs**

**You should see:**
```
📨 New notification created: 2f3babb7...
📨 Sending to user: user_id_456
✓ Token found: eGp0Ax7qLFI:APA91...
✓ Message sent successfully: 0:1234567
```

**If you see NOTHING:**
- Function didn't trigger
- Go to Check 3

**If you see ERROR:**
- Function ran but failed
- Read the error (see Troubleshooting below)

---

### Check 3: Is Token Actually Stored?

**In Firebase Console Firestore:**
1. Go to Firestore Database
2. Look for **fcm_tokens** collection
3. Do you see YOUR user document?
   - YES ✅ → Continue to Check 4
   - NO ❌ → Token not stored (see Fix below)

**If NO tokens exist:**
- You didn't rebuild after code changes
- **FIX**: 
  ```bash
  ./gradlew clean build
  ./gradlew installDebug
  ```
- Login again
- Wait 5 seconds
- Check Firestore again

---

### Check 4: Is Notification Actually Being Created?

**In Firebase Console Firestore:**
1. Go to `notifications` collection
2. After creating transaction, wait 5 seconds
3. Do you see a NEW document?
   - YES ✅ → Notification created (good!)
   - NO ❌ → Transaction creation has issues

**If YES (notification created):**
- But Cloud Function didn't run
- Check Cloud Function Logs again for errors
- See Troubleshooting section

---

## 🛠️ Most Likely Fix

**99% of the time, it's one of these:**

### Problem 1: Cloud Function Not Deployed
```
Firebase Console → Functions
→ Create function named: sendTransactionNotification
→ Runtime: Node.js 18
→ Trigger: Firestore, notifications, onCreate
→ Paste code from FCM_CLOUD_FUNCTION_SETUP.md
→ Deploy
```

### Problem 2: Cloud Function Deployed But Code Wrong
```
Delete the function
Redeploy with EXACT code from FCM_CLOUD_FUNCTION_SETUP.md
(copy/paste, no changes)
```

### Problem 3: Firestore Rules Block Function
```
Go to Firestore → Rules tab

Make sure you have:
match /fcm_tokens/{userId} {
  allow read: if true;
  allow write: if request.auth.uid == userId;
}
```

### Problem 4: Token Not Stored
```
Rebuild app: ./gradlew clean build
Reinstall: ./gradlew installDebug
Login again
Check fcm_tokens collection in Firestore
```

---

## 🧪 Test the Cloud Function DIRECTLY

Don't use the Firebase test. Test with your app:

**Step 1: Verify Token is Stored**
1. Open app
2. Login
3. Go to Firebase Console → Firestore → fcm_tokens
4. You should see document with your user ID

**Step 2: Create Transaction**
1. In app, create a transaction (as DISPATCHER)
2. Wait 5 seconds
3. Check notifications collection in Firestore
4. Should see NEW notification document

**Step 3: Check Cloud Function Logs**
1. Firebase Console → Functions → sendTransactionNotification
2. Click Logs tab
3. Should see messages like:
   ```
   📨 New notification created...
   ✓ Message sent successfully...
   ```

**Step 4: Check Device**
1. Look at GAS_STATION user device
2. Pull down notification center
3. Should see notification

---

## ❌ Common Errors in Cloud Function Logs

### Error: "No FCM token found for user"
```
Cause: User document doesn't have token
Fix: Logout/login again with rebuilt app
```

### Error: "Permission denied"
```
Cause: Firestore rules
Fix: Update rules in Firestore → Rules tab
Allow Cloud Function to read fcm_tokens
```

### Error: "Cannot read property 'token'"
```
Cause: Token document exists but no 'token' field
Fix: Check what's in fcm_tokens collection
Verify storeUserFcmToken() saves token field correctly
```

### Error: "Function execution took too long"
```
Cause: Function timeout
Fix: Usually temporary, try again
If persistent: function code has issue
```

---

## ✅ Step-by-Step: Start Over

If confused, do this:

**Step 1: Clean Rebuild (5 min)**
```bash
./gradlew clean build
./gradlew installDebug
```

**Step 2: Fresh Login**
1. Delete app completely
2. Reinstall
3. Login
4. Wait 5 seconds
5. Check Firestore for fcm_tokens collection

**Step 3: Verify Token Exists**
- Firebase Console → Firestore
- fcm_tokens collection
- Your user document should exist with token field

**Step 4: Redeploy Cloud Function**
- Firebase Console → Functions
- Delete old function if exists
- Create new: sendTransactionNotification
- Paste EXACT code from FCM_CLOUD_FUNCTION_SETUP.md
- Deploy
- Wait 3 minutes

**Step 5: Test**
1. Create transaction in app
2. Check Cloud Function Logs
3. Check device notification center

---

## 📋 Verification Checklist

Before saying "nothing sent", verify:

- [ ] Cloud Function shows green checkmark (deployed)
- [ ] fcm_tokens collection has documents with tokens
- [ ] notifications collection has documents (from your transactions)
- [ ] Cloud Function Logs show no errors
- [ ] Device notification settings: FuelHub notifications enabled
- [ ] Device: Not in Do Not Disturb mode
- [ ] Device: Volume is not muted

If all checked, notifications should work.

---

## 🎯 Final Test Procedure

**Device 1 (DISPATCHER):**
```
1. Rebuild: ./gradlew clean build
2. Install: ./gradlew installDebug
3. Open app
4. Login as DISPATCHER user
5. Create transaction
6. Wait 5 seconds
```

**Firebase Console (your computer):**
```
1. Go to Functions → sendTransactionNotification → Logs
2. Watch for messages
3. Should see "Message sent successfully"
```

**Device 2 (GAS_STATION):**
```
1. Logged in separately
2. Notification should appear within 1-2 seconds
3. Check notification center
```

**Expected Result:**
```
✓ Cloud Function log: "Message sent successfully"
✓ Device 2: Notification appears in notification center
✓ Can tap notification to open app
```

---

## Need More Help?

Post the error from **Cloud Function Logs** and I'll help fix it specifically.

Go to:
1. Firebase Console
2. Functions → sendTransactionNotification
3. Logs tab
4. Copy the error message
