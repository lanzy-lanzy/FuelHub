# Firebase Setup Instructions

## Prerequisites
- Google account
- Firebase project created
- google-services.json already configured ✅

## Step 1: Verify Google Services Configuration

Your `app/google-services.json` is already configured for:
- **Project ID**: `drrfuel`
- **Package Name**: `dev.ml.fuelhub`
- **API Key**: Already set up

## Step 2: Enable Required Services in Firebase Console

1. Go to [Firebase Console](https://console.firebase.google.com)
2. Select **drrfuel** project
3. Navigate to **Build**

### Enable Firestore Database
- Click **Firestore Database**
- Click **Create database**
- Choose region: `us-central1` (or nearest)
- Start in **Production mode** (then add security rules)
- Click **Create**

### Enable Authentication (Optional - for future features)
- Click **Authentication**
- Click **Get Started**
- Enable **Email/Password** provider (optional)

## Step 3: Create Firestore Security Rules

1. In Firestore Console, go to **Rules** tab
2. Replace with:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Allow all authenticated users to read and write
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
    
    // For development only - allow all reads/writes (REMOVE IN PRODUCTION)
    // match /{document=**} {
    //   allow read, write: if true;
    // }
  }
}
```

3. Click **Publish**

## Step 4: Create Firestore Indexes

Go to **Firestore Database** → **Indexes** tab and create these composite indexes:

### Index 1: Transactions by Wallet
- **Collection**: `transactions`
- **Fields to index**:
  - `walletId` (Ascending)
  - `createdAt` (Descending)
- Click **Create Index**

### Index 2: Transactions by Status
- **Collection**: `transactions`
- **Fields to index**:
  - `status` (Ascending)
  - `createdAt` (Descending)
- Click **Create Index**

### Index 3: Gas Slips by Transaction
- **Collection**: `gas_slips`
- **Fields to index**:
  - `transactionId` (Ascending)
  - `createdAt` (Descending)
- Click **Create Index**

### Index 4: Audit Logs
- **Collection**: `audit_logs`
- **Fields to index**:
  - `userId` (Ascending)
  - `timestamp` (Descending)
- Click **Create Index**

Wait for indexes to build (usually 5-10 minutes).

## Step 5: Update Android App

### 1. Update build.gradle.kts (Already done ✅)
```gradle
implementation("com.google.firebase:firebase-firestore-ktx")
implementation("com.google.firebase:firebase-auth-ktx")
```

### 2. Create Application Class

Create `app/src/main/java/dev/ml/fuelhub/FuelHubApplication.kt`:

```kotlin
package dev.ml.fuelhub

import android.app.Application
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.ktx.Firebase
import timber.log.Timber

class FuelHubApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        
        // Enable Firestore offline persistence
        try {
            val settings = firestoreSettings {
                isPersistenceEnabled = true
                // Set cache size to 100 MB (optional)
                setCacheSizeBytes(100 * 1024 * 1024)
            }
            Firebase.firestore.firestoreSettings = settings
            Timber.d("Firestore offline persistence enabled")
        } catch (e: Exception) {
            Timber.e(e, "Error enabling Firestore offline persistence")
        }
    }
}
```

### 3. Update AndroidManifest.xml

Add to `app/src/main/AndroidManifest.xml`:

```xml
<application
    android:name=".FuelHubApplication"
    ...>
    <!-- Your existing manifest content -->
</application>
```

Also add internet permission (if not already present):
```xml
<uses-permission android:name="android.permission.INTERNET" />
```

## Step 6: Update Repository Initialization

In your ViewModels or Activities, use `RepositoryModule`:

```kotlin
import dev.ml.fuelhub.di.RepositoryModule

class TransactionViewModel : ViewModel() {
    private val transactionRepository = 
        RepositoryModule.provideFuelTransactionRepository()
    private val walletRepository = 
        RepositoryModule.provideFuelWalletRepository()
    
    // Use repositories for all operations
}
```

## Step 7: Verify Setup

### Check Firestore Console
1. Go to **Firestore Database** → **Data** tab
2. You should see empty collections ready for data

### Test with Android Studio
1. Run the app on emulator or device
2. Check Android Studio **Logcat**
3. Look for: `"Firestore offline persistence enabled"`
4. Check Firebase Console for real-time updates

## Step 8: Remove Room Database (When Ready)

Once Firebase is working:

1. Delete `/app/src/main/java/dev/ml/fuelhub/data/database/` directory
2. Delete `/app/src/main/java/dev/ml/fuelhub/data/database/entity/` directory
3. Delete `/app/src/main/java/dev/ml/fuelhub/data/database/dao/` directory
4. Delete `/app/src/main/java/dev/ml/fuelhub/data/database/converter/` directory
5. Remove Room dependencies from `build.gradle.kts`
6. Sync Gradle

## Dependencies Added

```gradle
// Firebase Firestore
implementation("com.google.firebase:firebase-firestore-ktx")

// Firebase Authentication (for future use)
implementation("com.google.firebase:firebase-auth-ktx")

// Already had Firebase BOM
implementation(platform("com.google.firebase:firebase-bom:34.7.0"))
```

## Firestore Pricing

- **Reads**: $0.06 per 100K reads
- **Writes**: $0.18 per 100K writes
- **Deletes**: $0.02 per 100K deletes
- **Free tier**: 50K reads/day, 20K writes/day, 20K deletes/day

For development/testing, use production quota.

## Troubleshooting

### Issue: "Permission denied" errors
- Check Firestore security rules
- Verify authentication is enabled
- Check user permissions

### Issue: Firestore not initializing
- Verify google-services.json is in correct location
- Check BuildConfig.DEBUG in logs
- Ensure internet permission is granted

### Issue: Offline persistence not working
- Verify `isPersistenceEnabled = true` is set
- Check device storage space
- Clear app data and try again

## Next Steps

1. ✅ Build.gradle updated with Firebase dependencies
2. ✅ FirebaseDataSource created
3. ✅ Repository implementations created
4. ⏳ Create Application class
5. ⏳ Update AndroidManifest.xml
6. ⏳ Test all operations
7. ⏳ Monitor in Firebase Console

## Support

- [Firebase Console](https://console.firebase.google.com)
- [Firestore Documentation](https://firebase.google.com/docs/firestore)
- [Android Firebase Guide](https://firebase.google.com/docs/android/setup)
