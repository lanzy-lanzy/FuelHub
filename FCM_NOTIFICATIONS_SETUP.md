# FCM Push Notifications Implementation Guide

## Overview

Firebase Cloud Messaging (FCM) has been integrated into FuelHub to provide real-time push notifications for transaction events. Users will be notified when:

1. **A new transaction is created** - Gas Station users get notified of new fuel transactions to scan
2. **A transaction is verified/dispensed** - Drivers get notified when their transaction has been successfully processed

## Architecture

### Components Created

#### 1. **Data Models**
- **NotificationPayload.kt** - Represents notification data
- **NotificationType** - Enum for notification types

#### 2. **Services**
- **FuelHubMessagingService.kt** - Firebase Messaging Service that:
  - Receives incoming FCM messages
  - Displays system notifications
  - Creates notification channels for Android 8+

#### 3. **Repositories**
- **NotificationRepository (interface)** - Defines notification operations
- **FirebaseNotificationRepositoryImpl** - Firebase implementation handling:
  - Sending notifications to individual users
  - Sending notifications to users by role
  - Storing FCM tokens
  - Managing notification history

#### 4. **Use Cases**
- **SendTransactionCreatedNotificationUseCase** - Triggered when a transaction is created
  - Sends notifications to all GAS_STATION role users
  - Includes transaction details in the notification
  
- **SendTransactionVerifiedNotificationUseCase** - Triggered when gas station verifies/dispensed the transaction
  - Sends notifications to the specific driver
  - Confirms successful fuel dispensing

#### 5. **Dependency Injection**
- Updated **RepositoryModule.kt** - Provides NotificationRepository
- Updated **UseCaseModule.kt** - Provides notification use cases
- Integrated notification sending into CreateFuelTransactionUseCase

### Firebase Configuration

#### Android Manifest Changes
```xml
<!-- Added FCM Service -->
<service
    android:name=".service.FuelHubMessagingService"
    android:exported="false">
    <intent-filter>
        <action android:name="com.google.firebase.MESSAGING_EVENT" />
    </intent-filter>
</service>

<!-- Added notification permission -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

#### Gradle Dependencies
```gradle
implementation("com.google.firebase:firebase-messaging-ktx")
```

## Firestore Database Schema

### Collections Structure

#### `fcm_tokens` Collection
Stores FCM tokens for each user:
```
{
    userId: String,
    token: String,
    updatedAt: String (ISO DateTime)
}
```

#### `notifications` Collection
Centralized notification log:
```
{
    id: String,
    userId: String,
    title: String,
    body: String,
    notificationType: String (enum name),
    transactionId: String?,
    referenceNumber: String?,
    sentAt: String (ISO DateTime),
    readAt: String?,
    actionedAt: String?,
    data: Map<String, String>
}
```

#### `users/{userId}/notifications` Sub-collection
User-specific notification history (mirrors main notifications collection for query efficiency).

## Notification Flow

### Scenario 1: Transaction Created Notification

```
Driver creates transaction
    ↓
CreateFuelTransactionUseCase.execute()
    ↓
Transaction saved to Firestore
    ↓
SendTransactionCreatedNotificationUseCase.execute()
    ↓
Query all GAS_STATION role users
    ↓
For each user:
  - Store notification in Firestore
  - Get user's FCM token
  - Send FCM message
    ↓
Gas Station user receives notification with:
  - Transaction reference number
  - Vehicle type & liters
  - Driver name
  - QR code to scan
```

### Scenario 2: Transaction Verified/Dispensed Notification

```
Gas Station scans QR code
    ↓
Transaction marked as DISPENSED
    ↓
SendTransactionVerifiedNotificationUseCase.execute()
    ↓
Store notification in Firestore
    ↓
Get driver's FCM token
    ↓
Send FCM message to driver
    ↓
Driver receives notification:
  - Confirmation of successful dispensing
  - Liters dispensed
  - Gas station name
```

## Implementation Steps

### Step 1: Set Up Firebase Project
1. Go to Firebase Console (console.firebase.google.com)
2. Select your FuelHub project
3. Go to Cloud Messaging
4. Note the Sender ID (shown on the page)
5. Your google-services.json already has this configured

### Step 2: Store FCM Tokens
When a user logs in, store their FCM token:

```kotlin
// In your authentication/login flow
FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
    if (task.isSuccessful) {
        val token = task.result
        // Store in Firestore
        notificationRepository.storeUserFcmToken(userId, token)
    }
}
```

### Step 3: Trigger Notifications
Notifications are automatically triggered when:
- A transaction is created (goes to gas station users)
- A transaction is verified (goes to the driver)

### Step 4: Test Notifications
Use Firebase Console to send test notifications:
1. Firebase Console → Cloud Messaging
2. Create new campaign
3. Select Android
4. Send test message to your device

## Testing the Notifications

### Test Transaction Created Notification
1. Log in as DISPATCHER/ENCODER
2. Create a new fuel transaction
3. Check if all GAS_STATION users receive notification

### Test Transaction Verified Notification
1. Log in as GAS_STATION user
2. Scan transaction QR code
3. Mark transaction as DISPENSED
4. Driver should receive notification

## Configuration & Customization

### Change Notification Titles/Bodies
Edit the use cases:
- `SendTransactionCreatedNotificationUseCase.kt`
- `SendTransactionVerifiedNotificationUseCase.kt`

### Add New Notification Types
1. Add new enum value to `NotificationType`
2. Create new use case class
3. Handle in `FuelHubMessagingService.onMessageReceived()`

### Customize Notification Channels
Edit `FuelHubMessagingService.createNotificationChannels()`:
```kotlin
// Change channel names, importance, sound, etc.
val transactionChannel = NotificationChannel(
    CHANNEL_TRANSACTION,
    "Custom Channel Name",
    NotificationManager.IMPORTANCE_HIGH
)
```

## Notification Permissions

### Android 13+ (API 33+)
Apps must request POST_NOTIFICATIONS permission at runtime:

```kotlin
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    ActivityCompat.requestPermissions(
        this,
        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
        NOTIFICATION_PERMISSION_CODE
    )
}
```

This is already declared in AndroidManifest.xml. You may need to add runtime permission request in your auth/login screen.

## Cloud Functions (Optional - For Production)

For a more robust implementation, use Firebase Cloud Functions to send FCM messages:

```javascript
// functions/index.js
const functions = require('firebase-functions');
const admin = require('firebase-admin');

exports.sendTransactionNotification = functions.firestore
    .document('transactions/{transactionId}')
    .onCreate(async (snap, context) => {
        const transaction = snap.data();
        
        // Get all GAS_STATION users' tokens
        const gasStationUsers = await admin.firestore()
            .collection('fcm_tokens')
            .where('role', '==', 'GAS_STATION')
            .get();
        
        const tokens = gasStationUsers.docs.map(doc => doc.data().token);
        
        const message = {
            notification: {
                title: `New Transaction: ${transaction.referenceNumber}`,
                body: `${transaction.vehicleType} needs ${transaction.litersToPump}L`
            },
            data: {
                type: 'TRANSACTION_CREATED',
                transactionId: transaction.id,
                // ... other data
            }
        };
        
        return admin.messaging().sendMulticast({
            tokens: tokens,
            ...message
        });
    });
```

## Troubleshooting

### Notifications Not Received
1. Check FCM token is stored in Firestore
2. Ensure app has POST_NOTIFICATIONS permission
3. Check notification channels are created (Android 8+)
4. Review Timber logs: `Timber.d("FCM message received...")`

### Tokens Not Stored
1. Ensure user logs in successfully
2. Check Firebase rules allow writing to fcm_tokens collection
3. Verify NotificationRepository.storeUserFcmToken() is called

### App Crashes on Notification
1. Check FuelHubMessagingService logs
2. Verify Intent extras are properly handled in MainActivity
3. Check if MainActivity exists and is exported

## Files Modified

### New Files
- `data/model/NotificationPayload.kt`
- `service/FuelHubMessagingService.kt`
- `domain/repository/NotificationRepository.kt`
- `data/repository/FirebaseNotificationRepositoryImpl.kt`
- `domain/usecase/SendTransactionCreatedNotificationUseCase.kt`
- `domain/usecase/SendTransactionVerifiedNotificationUseCase.kt`

### Modified Files
- `app/build.gradle.kts` - Added Firebase Messaging dependency
- `app/src/main/AndroidManifest.xml` - Added service and permission
- `domain/usecase/CreateFuelTransactionUseCase.kt` - Integrated notification sending
- `di/RepositoryModule.kt` - Added NotificationRepository provider
- `di/UseCaseModule.kt` - Added notification use case providers

## Next Steps

1. **Implement Runtime Permissions** - Add notification permission request at runtime for Android 13+
2. **Test with Real Devices** - Test on actual Android device with Firebase project
3. **Implement Cloud Functions** - For production, use Cloud Functions for reliable message sending
4. **Monitor Delivery** - Check Firestore logs and notification history
5. **User Settings** - Consider adding user preferences for notification types

## References

- [Firebase Cloud Messaging Docs](https://firebase.google.com/docs/cloud-messaging)
- [FCM Android Implementation](https://firebase.google.com/docs/cloud-messaging/android/client)
- [Firebase Firestore Rules](https://firebase.google.com/docs/firestore/security/start)
