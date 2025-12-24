# FCM Quick Diagnostic - Is My Setup Correct?

## 5-Minute Setup Verification

Run through these checks to identify what's missing:

---

## CHECK 1: Firebase Configuration Files

**Verify `app/google-services.json` exists:**
```
File path: app/google-services.json
File exists? _____ (YES / NO)
```

**If NO:**
1. Go to Firebase Console
2. Project Settings → Download google-services.json
3. Place in `app/` folder
4. Rebuild project

---

## CHECK 2: Android Manifest

**Open `app/src/main/AndroidManifest.xml`**

**Look for this permission:**
```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```
Found? _____ (YES / NO)

**Look for this service:**
```xml
<service
    android:name=".service.FuelHubMessagingService"
    android:exported="false">
    <intent-filter>
        <action android:name="com.google.firebase.MESSAGING_EVENT" />
    </intent-filter>
</service>
```
Found? _____ (YES / NO)

**If either NO:** Add them to AndroidManifest.xml

---

## CHECK 3: build.gradle.kts

**Open `app/build.gradle.kts`**

**Look for this dependency:**
```gradle
implementation("com.google.firebase:firebase-messaging-ktx")
```
Found? _____ (YES / NO)

**If NO:** Add it to the Firebase dependencies section and sync gradle

---

## CHECK 4: Code Files Exist

**Check these files exist:**
```
✓ data/model/NotificationPayload.kt
✓ service/FuelHubMessagingService.kt
✓ domain/repository/NotificationRepository.kt
✓ data/repository/FirebaseNotificationRepositoryImpl.kt
✓ domain/usecase/SendTransactionCreatedNotificationUseCase.kt
✓ domain/usecase/SendTransactionVerifiedNotificationUseCase.kt
```

All present? _____ (YES / NO)

---

## CHECK 5: Dependency Injection

**Open `di/RepositoryModule.kt`**

**Look for:**
```kotlin
@Provides
@Singleton
fun provideNotificationRepository(): NotificationRepository = 
    FirebaseNotificationRepositoryImpl(FirebaseFirestore.getInstance())
```
Found? _____ (YES / NO)

**Open `di/UseCaseModule.kt`**

**Look for:**
```kotlin
@Provides
@Singleton
fun provideSendTransactionCreatedNotificationUseCase(...)
```
Found? _____ (YES / NO)

---

## CHECK 6: Token Storage on Login

**In your authentication/login code:**

**Look for:**
```kotlin
FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
    if (task.isSuccessful) {
        notificationRepository.storeUserFcmToken(userId, task.result)
    }
}
```

Found? _____ (YES / NO)

**If NO:** You need to add this! See FCM_DEBUGGING_GUIDE.md → SECTION 2

---

## CHECK 7: Runtime Permission Request

**In your login/auth activity:**

**Look for:**
```kotlin
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    ActivityCompat.requestPermissions(this, 
        arrayOf(Manifest.permission.POST_NOTIFICATIONS), 
        NOTIFICATION_PERMISSION_CODE)
}
```

Found? _____ (YES / NO)

**If NO on Android 13+ device:** Notifications won't show

---

## CHECK 8: Firestore Security Rules

**Go to Firebase Console:**
1. Firestore Database
2. Rules tab
3. Look for:

```javascript
match /fcm_tokens/{userId} {
  allow read, write: if request.auth.uid == userId;
}

match /notifications/{notificationId} {
  allow create: if request.auth != null;
  allow read: if request.auth != null;
}
```

All rules present? _____ (YES / NO)

**If NO:** Add these rules and click "Publish"

---

## CHECK 9: Firestore Collections Created

**Go to Firebase Console → Firestore Database**

Check these collections exist:
```
✓ users (should already exist)
✓ fcm_tokens (will be created after first login)
✓ notifications (will be created after first transaction)
✓ transactions (should already exist)
```

---

## CHECK 10: Test User Data

**In Firebase Console → Firestore:**

1. Go to `users` collection
2. Find a user with role = "GAS_STATION"
3. Verify:
   - [ ] id field exists
   - [ ] role = "GAS_STATION"
   - [ ] isActive = true

**If no GAS_STATION user:**
Create one in Firestore for testing

---

## CHECK 11: Build Succeeds

**Run:**
```bash
./gradlew build
```

**Result:**
- [ ] BUILD SUCCESSFUL
- [ ] BUILD FAILED (fix errors first)

---

## VERIFICATION RESULTS

Count your YES answers:

```
_____ / 11 checks passed
```

### Scoring:
- **11/11**: ✅ Setup complete! Problem is elsewhere
- **10/11**: ⚠️ Missing 1 thing - add it and retry
- **9/11**: ⚠️ Missing a few things - see detailed guide
- **<9**: ❌ Setup incomplete - follow FCM_QUICK_START.md first

---

## WHAT TO DO NEXT

### If 11/11 passed:
1. Open `FCM_DEBUGGING_GUIDE.md`
2. Run through SECTION 1 (Logcat errors)
3. Follow diagnostic flowchart

### If <11/11 passed:
1. Note which checks failed
2. Use the fix instructions above
3. Rebuild and test
4. Come back to this checklist

---

## REAL-TIME TEST

**After fixing any issues:**

1. Clear app cache:
   ```bash
   adb shell pm clear dev.ml.fuelhub
   ```

2. Reinstall:
   ```bash
   ./gradlew installDebug
   ```

3. Test flow:
   - [ ] Open app
   - [ ] See permission request (Android 13+)
   - [ ] Accept permission
   - [ ] Login as DISPATCHER user
   - [ ] Create transaction
   - [ ] Check if gas station user receives notification

---

## QUICK DIAGNOSTIC COMMAND

Run this to check file existence:

```bash
# Linux/Mac
find app/src -name "NotificationPayload.kt"
find app/src -name "FuelHubMessagingService.kt"
find app/src -name "NotificationRepository.kt"
find app/src -name "FirebaseNotificationRepositoryImpl.kt"

# Windows PowerShell
Get-ChildItem -Path app\src -Filter "NotificationPayload.kt" -Recurse
Get-ChildItem -Path app\src -Filter "FuelHubMessagingService.kt" -Recurse
```

---

## SUMMARY WORKSHEET

Fill this out:

```
TODAY'S STATUS:

Files present:          _____ / 6 code files
Configuration:          _____ / 5 config items
Code integration:       _____ / 3 places
Firestore setup:        _____ / 4 collections
Permissions:            _____ / 2 settings

OVERALL SETUP:          _____ % complete

Next action: ________________

Date checked: ________________
```

---

## BEFORE CONTACTING SUPPORT

Make sure you've:
- [ ] Completed all 11 checks above
- [ ] Fixed any failing checks
- [ ] Followed FCM_DEBUGGING_GUIDE.md
- [ ] Checked logcat for errors
- [ ] Verified Firestore documents
- [ ] Tested on actual device (if possible)

---

**Status**: Use this to diagnose your setup ✅
