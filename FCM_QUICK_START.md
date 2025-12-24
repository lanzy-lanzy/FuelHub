# FCM Quick Start Guide

## What Was Implemented

Two-way push notifications for FuelHub:
- **Transaction Created** → Gas Station users notified of new fuel requests
- **Transaction Verified** → Drivers notified of successful fuel dispensing

## Setup Checklist

### 1. Firebase Project Configuration
- [ ] Verify `google-services.json` is in `app/` directory
- [ ] Firebase project has Cloud Messaging enabled
- [ ] Note the **Sender ID** from Firebase Console > Cloud Messaging

### 2. Add Runtime Permission Request (Android 13+)
Add to your authentication/login screen:

```kotlin
import android.Manifest
import android.os.Build
import androidx.core.app.ActivityCompat

// In your Activity/Fragment
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    ActivityCompat.requestPermissions(
        this,
        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
        NOTIFICATION_PERMISSION_CODE
    )
}
```

### 3. Store FCM Token on Login
Add to your authentication handler:

```kotlin
import com.google.firebase.messaging.FirebaseMessaging

// After successful login
FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
    if (task.isSuccessful) {
        val token = task.result
        // Store in repository
        viewModelScope.launch {
            notificationRepository.storeUserFcmToken(userId, token)
        }
    }
}
```

### 4. Build and Test
```bash
# Build the project
./gradlew build

# Test on device
./gradlew installDebug
```

## How It Works

### When Transaction is Created
```
1. Driver/Dispatcher creates transaction
2. CreateFuelTransactionUseCase executes
3. SendTransactionCreatedNotificationUseCase fires
4. Sends to all GAS_STATION users:
   - Reference number
   - Vehicle type
   - Liters needed
   - Driver name
```

### When Transaction is Verified
```
1. Gas Station user scans QR code
2. Transaction marked as DISPENSED
3. SendTransactionVerifiedNotificationUseCase fires
4. Sends to driver:
   - Confirmation message
   - Liters dispensed
   - Station name
```

## Testing

### Manual Test via Firebase Console

1. Go to Firebase Console → Cloud Messaging
2. Click "Send your first message"
3. Fill in:
   - Title: "Test Notification"
   - Body: "This is a test"
4. Select Android
5. Input your package ID: `dev.ml.fuelhub`
6. Click "Send test message"
7. Select your device

### Automated Test
```kotlin
// Create transaction to trigger notification
val input = CreateFuelTransactionUseCase.CreateTransactionInput(
    vehicleId = "vehicle1",
    litersToPump = 20.0,
    costPerLiter = 65.50,
    destination = "Location",
    tripPurpose = "Delivery",
    createdBy = "dispatcher1",
    walletId = "wallet1"
)

viewModelScope.launch {
    try {
        createFuelTransactionUseCase.execute(input)
        // Gas Station users should get notified
    } catch (e: Exception) {
        // Handle error
    }
}
```

## Key Files

| File | Purpose |
|------|---------|
| `service/FuelHubMessagingService.kt` | Receives FCM messages, displays notifications |
| `domain/repository/NotificationRepository.kt` | Interface for notification operations |
| `data/repository/FirebaseNotificationRepositoryImpl.kt` | Firebase implementation |
| `domain/usecase/SendTransactionCreatedNotificationUseCase.kt` | Triggers notification on transaction creation |
| `domain/usecase/SendTransactionVerifiedNotificationUseCase.kt` | Triggers notification on verification |

## Firestore Rules

Make sure your Firestore rules allow reading/writing notifications:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // FCM Tokens
    match /fcm_tokens/{userId} {
      allow read, write: if request.auth.uid == userId;
    }
    
    // Notifications
    match /notifications/{notificationId} {
      allow read, write: if request.auth != null;
    }
    
    // User notifications sub-collection
    match /users/{userId}/notifications/{notificationId} {
      allow read, write: if request.auth.uid == userId;
    }
  }
}
```

## Debugging

### Enable Timber Logs
```kotlin
// Check logcat for FCM messages
Timber.d("FCM message received")
Timber.d("Notification displayed")
Timber.d("FCM token stored")
```

### Check Stored Tokens
In Firebase Console:
1. Go to Firestore
2. Click `fcm_tokens` collection
3. Verify documents exist for logged-in users

### Verify Notifications Sent
In Firebase Console:
1. Go to Firestore
2. Click `notifications` collection
3. Should see entries when transactions are created

## Common Issues & Fixes

| Issue | Fix |
|-------|-----|
| No notifications | Verify FCM token is stored for user |
| App crashes on notification | Check MainActivity exists and is exported |
| Token not storing | Ensure Firestore rules allow writes to fcm_tokens |
| Notifications not showing | Request POST_NOTIFICATIONS permission at runtime |

## Next Steps

1. **Test on Real Device** - Emulator FCM can be unreliable
2. **Implement Cloud Functions** - For production message sending
3. **Add Notification History UI** - Show users past notifications
4. **User Preferences** - Let users control notification types
5. **Analytics** - Track notification delivery and engagement

## Support Files

- 📖 `FCM_NOTIFICATIONS_SETUP.md` - Detailed technical guide
- 🚀 This file - Quick start reference

---

**Status**: ✅ Implementation Complete
**Build**: Ready to compile and test
