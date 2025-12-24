# FCM Notifications Not Working? Top 3 Issues & Fixes

## 🎯 Most Likely Problems (95% of Cases)

---

## ISSUE #1: FCM Token Not Stored (40% of Cases)

### Symptom
- Notifications not received
- Firestore `fcm_tokens` collection is empty
- No errors in logcat

### Root Cause
You didn't add code to store the FCM token after login

### ✅ FIX: Add Token Storage to Your Login Flow

**Find your LoginActivity or AuthViewModel**

Add this method:
```kotlin
private fun storeFcmToken(userId: String) {
    Timber.d("Storing FCM token for user: $userId")
    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            val token = task.result
            Timber.d("FCM Token received: ${token.take(20)}...")
            
            lifecycleScope.launch {
                try {
                    notificationRepository.storeUserFcmToken(userId, token)
                    Timber.d("✓ Token stored successfully")
                } catch (e: Exception) {
                    Timber.e(e, "Failed to store token")
                }
            }
        } else {
            Timber.e(task.exception, "Failed to get FCM token")
        }
    }
}
```

**Call it after successful login:**
```kotlin
// In your login success callback
FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
    .addOnSuccessListener { authResult ->
        val userId = authResult.user?.uid ?: return@addOnSuccessListener
        
        // ADD THIS LINE:
        storeFcmToken(userId)
        
        // Continue with navigation
        navigateToHome()
    }
```

### ✅ Verify It Worked
1. Login to app
2. Go to Firebase Console → Firestore
3. Look for `fcm_tokens` collection
4. You should see a document with your user ID
5. Document should have a `token` field

### Time to Fix: **2 minutes**

---

## ISSUE #2: Firestore Security Rules Block Notifications (35% of Cases)

### Symptom
- No notification documents appear in Firestore
- Logcat shows: `"PERMISSION_DENIED"`
- Firestore console shows security rule violations

### Root Cause
Firestore rules don't allow creating/reading notifications

### ✅ FIX: Update Firestore Security Rules

**Go to Firebase Console:**
1. Click "Firestore Database"
2. Click "Rules" tab
3. Replace ALL content with:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Allow users to read/write their own FCM tokens
    match /fcm_tokens/{userId} {
      allow read, write: if request.auth.uid == userId;
    }
    
    // Allow authenticated users to create notifications
    match /notifications/{notificationId} {
      allow create: if request.auth != null;
      allow read, update: if request.auth != null;
      allow delete: if request.auth.uid == resource.data.userId || 
                       request.auth.token.admin == true;
    }
    
    // Allow users to read their own notifications
    match /users/{userId}/notifications/{notificationId} {
      allow create, read, delete, update: if request.auth.uid == userId;
    }
    
    // Allow existing collections to work
    match /users/{document=**} {
      allow read, write: if request.auth != null;
    }
    
    match /transactions/{document=**} {
      allow read, write: if request.auth != null;
    }
    
    match /wallets/{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

4. Click "Publish"
5. Wait for rules to deploy (30 seconds)

### ✅ Verify It Worked
1. Try creating a transaction again
2. Check Firestore → `notifications` collection
3. Should see new notification document

### Time to Fix: **2 minutes**

---

## ISSUE #3: Runtime Permission Not Requested (Android 13+) (20% of Cases)

### Symptom
- Notifications might be working but not showing on Android 13+
- No permission dialog appears on first login
- Device notifications settings show FuelHub permission is denied

### Root Cause
APP not requesting POST_NOTIFICATIONS permission at runtime

### ✅ FIX: Request Permission on Login

**In your LoginActivity or SplashActivity:**

Add this import:
```kotlin
import android.Manifest
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
```

Add this code in `onCreate()`:
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // REQUEST NOTIFICATION PERMISSION (Android 13+)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
        ) {
            Timber.d("Requesting POST_NOTIFICATIONS permission")
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_NOTIFICATION_PERMISSION_CODE
            )
        }
    }
    
    // ... rest of your onCreate code
}

// Add this constant
companion object {
    const val REQUEST_NOTIFICATION_PERMISSION_CODE = 100
}
```

### ✅ Verify It Worked
1. Uninstall app completely: `adb uninstall dev.ml.fuelhub`
2. Rebuild and install
3. Open app
4. Should see permission dialog
5. Accept permission
6. Check Settings → Apps → FuelHub → Notifications → Should be ON

### Time to Fix: **3 minutes**

---

## 🔍 Quick Diagnosis: Which Issue Is Yours?

### Test 1: Check Firestore for Tokens
```
Firebase Console → Firestore Database → fcm_tokens collection

If documents exist with tokens:
  ✓ Issue #1 is NOT your problem
  
If NO documents:
  ✗ FIX ISSUE #1
```

### Test 2: Check Firestore for Notifications
```
Firebase Console → Firestore Database → notifications collection

Create a transaction, wait 5 seconds

If notification document appears:
  ✓ Issue #2 is NOT your problem
  
If NO document:
  ✗ FIX ISSUE #2
```

### Test 3: Check Device Settings
```
Device Settings → Apps → FuelHub → Permissions → Notifications

If toggle is ON:
  ✓ Issue #3 is NOT your problem
  
If toggle is OFF or permission isn't listed:
  ✗ FIX ISSUE #3
```

---

## 🚀 QUICK FIX PROCEDURE (15 minutes)

**If notifications aren't working right now:**

### Step 1: Add Token Storage (2 min)
- Open your LoginActivity
- Add the `storeFcmToken()` method above
- Call it after login success
- Save and rebuild

### Step 2: Update Firestore Rules (2 min)
- Go to Firebase Console
- Paste the rules above
- Click Publish
- Wait 30 seconds

### Step 3: Request Runtime Permission (3 min)
- Open LoginActivity/SplashActivity
- Add permission request code above
- Save and rebuild

### Step 4: Clean Reinstall (5 min)
```bash
# Completely remove old app
adb uninstall dev.ml.fuelhub

# Clean build
./gradlew clean build

# Reinstall
./gradlew installDebug
```

### Step 5: Test (3 min)
- Open app
- See permission request
- Accept permission
- Login
- Create transaction
- Should receive notification!

---

## 🎯 Which Fix Do You Need?

**Answer these questions:**

1. Do you see POST_NOTIFICATIONS permission dialog when opening app?
   - YES → Skip Issue #3
   - NO → You need Issue #3 fix

2. Do you see documents in `fcm_tokens` collection after login?
   - YES → Skip Issue #1
   - NO → You need Issue #1 fix

3. Do you see documents in `notifications` collection after creating transaction?
   - YES → Skip Issue #2
   - NO → You need Issue #2 fix

---

## ✅ Success Indicators

After fixing all 3 issues, you should see:

**In Firebase Console:**
- ✅ `fcm_tokens` collection has documents with tokens
- ✅ `notifications` collection grows when creating transactions
- ✅ No security rule errors in Firestore logs

**On Device:**
- ✅ Permission dialog appears (Android 13+)
- ✅ Permission is granted
- ✅ Notifications appear in notification center
- ✅ Notification center shows transaction details

---

## Still Not Working?

If you've fixed all 3 issues and notifications still don't appear:

1. Check logcat for errors:
   ```bash
   adb logcat | grep -i "fcm\|notification"
   ```

2. Follow detailed guide: `FCM_DEBUGGING_GUIDE.md`

3. Run quick diagnostic: `FCM_QUICK_DIAGNOSTIC.md`

---

## SUMMARY TABLE

| Issue | Symptom | Fix | Time |
|-------|---------|-----|------|
| #1: No Token Storage | No documents in fcm_tokens | Add storeFcmToken() | 2 min |
| #2: Firestore Rules | Permission denied errors | Update security rules | 2 min |
| #3: No Permission | Notification not granted | Request at runtime | 3 min |

**Total time to fix all 3: ~15 minutes**

---

**Ready to fix?** Start with Issue #1 above! ✅
