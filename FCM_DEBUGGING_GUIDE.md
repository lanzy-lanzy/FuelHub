# FCM Push Notifications - Debugging Guide

## ❌ Notifications Not Working - Step-by-Step Debug

### Quick Diagnosis Flowchart

```
Are you getting ANY errors in logcat?
├─ YES → Go to: SECTION 1 (Errors in logs)
└─ NO → Continue below

Are FCM tokens stored in Firestore?
├─ NO → Go to: SECTION 2 (Tokens not storing)
└─ YES → Continue below

Are notifications created in Firestore?
├─ NO → Go to: SECTION 3 (Notifications not created)
└─ YES → Continue below

Is the device receiving notifications?
├─ NO → Go to: SECTION 4 (Device not receiving)
└─ YES → Notifications should be working!
```

---

## SECTION 1: Check Logcat for Errors

### Step 1: Enable Debug Logging
```bash
# Open terminal in Android Studio or command line
adb logcat | grep -i "fcm\|notification\|firestore" > debug.log

# Or filter specifically:
adb logcat | grep FuelHubMessagingService
adb logcat | grep NotificationRepository
adb logcat | grep "CreateFuelTransactionUseCase"
```

### Step 2: Create a Transaction and Watch Logs
1. Run app
2. Create a fuel transaction
3. Watch the logcat output
4. Look for ERROR or Exception messages

### Common Errors & Fixes

**Error**: `"user not found"`
```
Fix: Make sure you're logged in as a user with DISPATCHER or ENCODER role
```

**Error**: `"wallet not found"`
```
Fix: Verify the vehicle has an associated fuel wallet
```

**Error**: `"Failed to send notification"`
```
Fix: Check Firestore security rules - might be blocking writes
```

**Error**: `"Firebase not initialized"`
```
Fix: Ensure google-services.json is in app/ directory and Firebase is initialized
```

---

## SECTION 2: FCM Tokens Not Storing

### Verify: Is Token Being Stored?

**Check Firestore for tokens:**
1. Go to Firebase Console
2. Click "Firestore Database"
3. Look for `fcm_tokens` collection
4. Check if documents exist

**If no documents exist:**

### Issue 2A: Token Storage Not Called
**Symptom**: No documents in fcm_tokens collection

**Fix**: Add token storage to your login flow

```kotlin
// In your LoginActivity or AuthViewModel
// After successful Firebase authentication:

private fun storeFcmToken(userId: String) {
    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            val token = task.result
            lifecycleScope.launch {
                notificationRepository.storeUserFcmToken(userId, token)
            }
        }
    }
}

// Call after login succeeds
```

### Issue 2B: Permission Not Granted (Android 13+)

**Symptom**: POST_NOTIFICATIONS permission not granted

**Fix**: Add permission request to login screen

```kotlin
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    if (ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.POST_NOTIFICATIONS
    ) != PackageManager.PERMISSION_GRANTED
    ) {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            NOTIFICATION_PERMISSION_CODE
        )
    }
}
```

**Check**: Did you accept the permission when prompted?
- Go to device Settings → Apps → FuelHub → Permissions
- Enable "Notifications"

### Issue 2C: Firestore Rules Blocking Write

**Symptom**: Token stores for you but not for gas station user

**Check Firestore Rules:**
1. Firebase Console → Firestore → Rules tab
2. Look for:
```javascript
match /fcm_tokens/{userId} {
  allow read, write: if request.auth.uid == userId;
}
```

**If missing or wrong:**
```javascript
// Correct rules:
match /fcm_tokens/{userId} {
  allow read, write: if request.auth.uid == userId;
}
```

---

## SECTION 3: Notifications Not Created in Firestore

### Verify: Are Notifications Being Created?

**Check Firestore:**
1. Firebase Console → Firestore Database
2. Look for `notifications` collection
3. After creating transaction, check if new document appears

**If no notification documents:**

### Issue 3A: Logcat Shows Error

**Check logcat for error messages:**
```bash
adb logcat | grep "SendTransactionCreatedNotification"
adb logcat | grep "NotificationRepository"
```

**Look for patterns:**
- "Failed to send notification"
- "Exception"
- "Firebase error"

### Issue 3B: GAS_STATION Users Query Returns Empty

**Problem**: No GAS_STATION users exist in database

**Fix**: Verify users exist with correct role

```bash
# In Firebase Console → Firestore:
1. Go to "users" collection
2. Check if gas station users exist
3. Verify their "role" field = "GAS_STATION"
4. Verify "isActive" = true
```

**If users don't exist:**
1. Create test users with GAS_STATION role
2. Make sure they're active
3. Make sure they have FCM tokens stored

### Issue 3C: Firestore Rules Blocking Notification Write

**Check rules for notifications collection:**
```javascript
match /notifications/{notificationId} {
  allow create: if request.auth != null;
  allow read, update: if request.auth != null;
}
```

**If this rule is missing**, add it to your Firestore Rules.

### Issue 3D: SendTransactionCreatedNotificationUseCase Not Being Called

**Verify in code:**

Check `CreateFuelTransactionUseCase.kt` has this at the end:

```kotlin
// 9. Send notification to gas station users
sendTransactionCreatedNotificationUseCase?.execute(
    SendTransactionCreatedNotificationUseCase.Input(
        transactionId = transactionId,
        referenceNumber = referenceNumber,
        vehicleType = vehicle.vehicleType,
        litersToPump = input.litersToPump,
        driverName = input.createdBy,
        driverFullName = user?.fullName
    )
)
```

**If missing:**
1. Open `CreateFuelTransactionUseCase.kt`
2. Add the code above before `return CreateTransactionOutput`

---

## SECTION 4: Device Not Receiving Notifications

### Issue 4A: Device Notifications Disabled

**Check device settings:**
1. Settings → Apps → FuelHub
2. Tap "Permissions" → "Notifications"
3. Toggle "Allow notifications" ON

**Or:**
1. Settings → Notifications → FuelHub
2. Make sure notifications are enabled

### Issue 4B: Do Not Disturb Mode Enabled

**Check device:**
1. Pull down notification shade
2. Look for "Do Not Disturb" icon
3. Tap to disable

**Or:**
1. Settings → Notifications → Do Not Disturb
2. Turn OFF

### Issue 4C: Notification Channel Not Enabled

**Android 8+ requires notification channels:**

```kotlin
// Should be in FuelHubMessagingService.onCreate()
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    val channel = NotificationChannel(
        "transaction_notifications",
        "Transaction Notifications",
        NotificationManager.IMPORTANCE_HIGH
    )
    notificationManager.createNotificationChannel(channel)
}
```

**Check if this code exists:**
1. Open `FuelHubMessagingService.kt`
2. Verify `createNotificationChannels()` method exists
3. Verify it's called in `onCreate()`

### Issue 4D: FCM Service Not Receiving Messages

**Check AndroidManifest.xml:**
```xml
<service
    android:name=".service.FuelHubMessagingService"
    android:exported="false">
    <intent-filter>
        <action android:name="com.google.firebase.MESSAGING_EVENT" />
    </intent-filter>
</service>
```

**If missing or wrong:**
1. Open `AndroidManifest.xml`
2. Add the service definition above (inside `<application>` tag)

### Issue 4E: Google Services JSON Missing

**Check file exists:**
1. Look for `app/google-services.json`
2. If missing:
   - Go to Firebase Console
   - Project Settings → Download google-services.json
   - Place in `app/` directory
   - Rebuild

---

## COMPLETE DEBUG CHECKLIST

### Tier 1: Basic Setup
- [ ] `app/google-services.json` exists
- [ ] Firebase Messaging dependency in gradle
- [ ] `FuelHubMessagingService` registered in manifest
- [ ] `POST_NOTIFICATIONS` permission in manifest
- [ ] App compiles without errors
- [ ] App installs on device

### Tier 2: Authentication & Tokens
- [ ] Can log in successfully
- [ ] Permission dialog appears (Android 13+)
- [ ] Accept permission
- [ ] Check Firestore → fcm_tokens collection
- [ ] Your user document exists with token

### Tier 3: Transaction Creation
- [ ] Can create transaction successfully
- [ ] No errors in logcat during creation
- [ ] Check Firestore → notifications collection
- [ ] At least 1 notification document created

### Tier 4: Multi-Device Test
- [ ] Have 2+ devices
- [ ] Device 1: Login as DISPATCHER
- [ ] Device 2: Login as GAS_STATION user
- [ ] Device 1: Create transaction
- [ ] Device 2: Should receive notification
- [ ] Check Device 2 notification settings are enabled

---

## DETAILED VERIFICATION STEPS

### Step A: Verify Firestore Collections Exist
```
Firebase Console → Firestore Database
Check these collections exist:
  ✓ users
  ✓ fcm_tokens
  ✓ notifications
  ✓ transactions
```

### Step B: Verify User Document
```
Firebase Console → Firestore → users collection
Look for your user document with:
  ✓ id: (your user ID)
  ✓ role: "DISPATCHER" or "GAS_STATION"
  ✓ isActive: true
  ✓ email: (your email)
```

### Step C: Verify FCM Token
```
Firebase Console → Firestore → fcm_tokens collection
Look for document with:
  ✓ Document ID: (your user ID)
  ✓ token: (long string starting with...)
  ✓ updatedAt: (recent timestamp)
```

### Step D: Verify Transaction Created
```
Firebase Console → Firestore → transactions collection
After creating transaction, should see:
  ✓ New document with your transaction ID
  ✓ status: "PENDING"
  ✓ referenceNumber: "FS-xxxxx-xxxxx"
  ✓ createdBy: (your user ID)
```

### Step E: Verify Notification Created
```
Firebase Console → Firestore → notifications collection
After creating transaction, should see:
  ✓ New document
  ✓ userId: (GAS_STATION user ID)
  ✓ notificationType: "TRANSACTION_CREATED"
  ✓ title: "New Transaction: FS-xxxxx-xxxxx"
  ✓ sentAt: (recent timestamp)
```

---

## MULTI-DEVICE TESTING

### Setup for Testing
**Device 1 (Dispatcher):**
1. Install app
2. Login as user with DISPATCHER role
3. Accept permission
4. Wait for token to store

**Device 2 (Gas Station):**
1. Install app
2. Login as user with GAS_STATION role
3. Accept permission
4. Wait for token to store

### Test Procedure
1. On Device 1: Create transaction
2. Watch Device 2 for notification
3. Check Firestore for notification document
4. Check Device 2 notification settings

### Expected Results
- Device 2 should see notification in notification center within 5 seconds
- Firestore should show notification document
- Tapping notification should open app

---

## ADVANCED DEBUGGING

### Enable Firebase Debug Logging
```kotlin
// In MainActivity.onCreate()
if (BuildConfig.DEBUG) {
    Firebase.crashlytics.setCrashlyticsCollectionEnabled(true)
}

// For Firestore
FirebaseFirestore.getInstance().firestoreSettings = 
    firestoreSettings {
        isPersistenceEnabled = true
    }
```

### Manual FCM Test
1. Firebase Console → Cloud Messaging
2. Click "Create new campaign"
3. Fill in test message
4. Select your app (Android)
5. Enter package: `dev.ml.fuelhub`
6. Click "Send test message"
7. Select your device
8. Notification should appear

### Monitor Firestore Activity
```bash
# Watch Firestore for changes
Firebase Console → Firestore Database
Refresh every few seconds while testing
```

---

## COMMON SOLUTIONS

### Solution 1: Restart App
```
If stuck in weird state:
1. Force stop app (Settings → Apps → FuelHub → Force Stop)
2. Clear cache (Settings → Apps → FuelHub → Storage → Clear Cache)
3. Reopen app
```

### Solution 2: Rebuild Project
```bash
./gradlew clean
./gradlew build
./gradlew installDebug
```

### Solution 3: Reset Firebase Emulator
```bash
# If using emulator:
firebase emulators:start
```

### Solution 4: Update Firestore Rules
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

**WARNING**: This allows all authenticated users to read/write everything (for testing only!)

---

## QUICK FIX CHECKLIST

If notifications still not working after all above:

1. [ ] Delete app completely
   ```bash
   adb uninstall dev.ml.fuelhub
   ```

2. [ ] Clean and rebuild
   ```bash
   ./gradlew clean build
   ```

3. [ ] Reinstall fresh
   ```bash
   ./gradlew installDebug
   ```

4. [ ] Test again

5. [ ] If still not working, check:
   - Firebase project is same as in google-services.json
   - Firestore database exists and is not in test mode (or test mode rules allow writes)
   - Users exist with correct roles
   - All code files are present

---

## STILL NOT WORKING?

### Get More Info with Logging

Add to `SendTransactionCreatedNotificationUseCase.kt`:

```kotlin
Timber.d("🔍 DEBUG: Sending notification...")
Timber.d("🔍 Users to notify: ${usersToNotify.size}")
for (user in usersToNotify) {
    Timber.d("🔍 Notifying: ${user.id} (${user.role})")
}
```

Add to `FirebaseNotificationRepositoryImpl.kt`:

```kotlin
Timber.d("📝 Storing notification: $notificationId")
Timber.d("📝 Getting token for: $userId")
val token = getUserFcmToken(userId)
Timber.d("📝 Token: ${token?.take(20)}...")
```

Watch logcat for 🔍 and 📝 symbols to track execution.

---

## Contact Information

If you've completed all steps above and still have issues:

1. Check your Firestore Rules are correct
2. Verify google-services.json is correct
3. Make sure internet connection is active
4. Try on actual device (not emulator if possible)
5. Check Firebase project quota limits

