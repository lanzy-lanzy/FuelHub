# Firestore Permission Error - QUICK FIX

## Error Message
```
Failed to add driver: PERMISSION_DENIED: Missing or insufficient permissions.
```

## Root Cause
Firestore security rules are either missing or blocking all access.

## Solution

### Option 1: Testing (Development Only)
In Firebase Console → Firestore Database → Rules, set:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if true;
    }
  }
}
```

**Warning**: This allows ALL access. Only use for testing/development.

### Option 2: Recommended (Authentication Required)
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

This requires users to be authenticated.

### Option 3: Production (Role-Based)
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Allow authenticated users to read/write
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth.uid == userId || isAdmin();
    }
    
    match /vehicles/{vehicleId} {
      allow read, write: if request.auth != null && hasPermission('ADMIN');
    }
    
    match /fuel_wallets/{walletId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && hasPermission('ADMIN');
    }
    
    match /transactions/{transactionId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null;
    }
    
    match /gas_slips/{slipId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null;
    }
    
    match /audit_logs/{logId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null;
    }
  }
  
  function isAdmin() {
    return getRole() == 'ADMIN';
  }
  
  function hasPermission(role) {
    return getRole() == role;
  }
  
  function getRole() {
    return get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role;
  }
}
```

## Steps to Fix Now

### 1. Open Firebase Console
```
https://console.firebase.google.com
```

### 2. Select Project "drrfuel"

### 3. Go to Firestore Database

### 4. Click "Rules" tab

### 5. Replace with this temporary rule:
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if true;
    }
  }
}
```

### 6. Click "Publish"

### 7. Restart the app

## Verify the Fix

After publishing rules:
1. Close and reopen the app
2. Try to add a driver
3. Error should be gone

## Making Collections

If you get "Collection doesn't exist" errors:

### In Firebase Console:
1. Click **Start Collection**
2. Collection ID: `users`
3. Click **Auto ID** for first document
4. Add any field (it will accept empty docs)
5. Save

Repeat for:
- `vehicles`
- `fuel_wallets`
- `transactions`
- `gas_slips`
- `audit_logs`

## Or Using Code

Add this to initialize collections:

```kotlin
// In FuelHubApplication or MainActivity
private fun initializeCollections() {
    val db = Firebase.firestore
    val collections = listOf(
        "users", "vehicles", "fuel_wallets", 
        "transactions", "gas_slips", "audit_logs"
    )
    
    collections.forEach { collection ->
        db.collection(collection)
            .limit(1)
            .get()
            .addOnFailureListener { exception ->
                if (exception?.message?.contains("does not exist") == true) {
                    // Collection doesn't exist, create it
                    db.collection(collection)
                        .document("__init__")
                        .set(mapOf("initialized" to true))
                        .addOnSuccessListener {
                            db.collection(collection)
                                .document("__init__")
                                .delete()
                        }
                }
            }
    }
}
```

## Checklist

- [ ] Firebase project created
- [ ] Firestore Database enabled
- [ ] google-services.json placed in app/ folder
- [ ] Build and run app
- [ ] Security rules published
- [ ] Collections created
- [ ] Add driver - success!

## Still Getting Errors?

Check:
1. **google-services.json** - Valid and in app/ folder?
2. **Internet Permission** - Added to AndroidManifest.xml?
3. **Firebase Rules** - Published and correct?
4. **Collections** - Exist in Firestore?
5. **App Restart** - Fully closed and reopened?

## Test Data

Once working, you can add test data:

### Add Test Driver
In Firebase Console → Firestore → users:
```
Document ID: driver-001
{
  "id": "driver-001",
  "username": "driver01",
  "email": "driver@test.com",
  "fullName": "Test Driver",
  "role": "DRIVER",
  "officeId": "office-001",
  "isActive": true,
  "createdAt": (current timestamp)
}
```

### Add Test Wallet
In Firebase Console → Firestore → fuel_wallets:
```
Document ID: wallet-001
{
  "id": "wallet-001",
  "officeId": "office-001",
  "balanceLiters": 500.0,
  "maxCapacityLiters": 1000.0,
  "lastUpdated": (current timestamp),
  "createdAt": (current timestamp)
}
```

## Next Steps

1. Fix security rules (use Option 1 for testing)
2. Create collections
3. Restart app
4. Add test data
5. Test CRUD operations
6. Configure proper security rules for production

## Production Checklist

Before going live:
- [ ] Implement proper authentication
- [ ] Set up role-based security rules
- [ ] Enable Firestore backup
- [ ] Configure Cloud Functions (optional)
- [ ] Set up monitoring/alerts
- [ ] Test security rules thoroughly
- [ ] Enable audit logging
- [ ] Document access policies

Good luck! 🚀
