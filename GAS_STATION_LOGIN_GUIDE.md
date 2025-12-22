# Gas Station Operator - Login & Account Setup Guide

## Overview
This guide explains how to create gas station operator accounts and implement their role-based access.

---

## 1. Account Creation Methods

### Method A: Admin Dashboard (Recommended)
**Requires:** Admin access to Firestore console

1. **Go to Firebase Console**
   - Navigate to https://console.firebase.google.com/
   - Select your FuelHub project
   - Go to **Firestore Database** → **Collections** → **users**

2. **Create New Document**
   - Click **+ Add Document**
   - Set Document ID: `user-unique-id` (e.g., `gs-operator-001`)
   - Add the following fields:

```json
{
  "id": "user-unique-id",
  "username": "station-name",
  "email": "operator@gasstation.com",
  "fullName": "Operator Full Name",
  "role": "GAS_STATION",
  "officeId": "gas-station-location-1",
  "isActive": true,
  "createdAt": "2025-12-21T10:30:00"
}
```

3. **Create Authentication User**
   - Go to **Authentication** → **Users** → **Add User**
   - Email: `operator@gasstation.com`
   - Password: Strong password (at least 6 chars)
   - Copy the generated **User ID** to the Firestore document

---

### Method B: Self-Registration (Through App)

**For controlled self-registration with role validation:**

1. **User clicks "Sign Up"**
2. **Fills in registration form:**
   - Email
   - Password
   - Full Name
   - Username

3. **Default role is set to DISPATCHER**
4. **Admin must update role to GAS_STATION** in Firestore

---

### Method C: Programmatic Account Creation

**For bulk account creation or automated onboarding:**

Create a backend cloud function (Firebase Functions) to create accounts:

```kotlin
// In backend (Cloud Functions or admin script)
val auth = FirebaseAuth.getInstance()
val firestore = FirebaseFirestore.getInstance()

// Create auth user
val userRecord = auth.createUser()
    .setEmail("operator@gasstation.com")
    .setPassword("strongPassword123")
    .build()

// Create Firestore document
firestore.collection("users")
    .document(userRecord.uid)
    .set(mapOf(
        "id" to userRecord.uid,
        "username" to "station-name",
        "email" to "operator@gasstation.com",
        "fullName" to "Operator Name",
        "role" to "GAS_STATION",
        "officeId" to "location-id",
        "isActive" to true,
        "createdAt" to FieldValue.serverTimestamp()
    ))
```

---

## 2. Login Flow

### Gas Station Operator Login

**Step 1: Open App**
- User sees LoginScreen
- Enters email and password

**Step 2: Firebase Authentication**
```kotlin
FirebaseAuth.signInWithEmailAndPassword(email, password)
```

**Step 3: Role-Based Routing**
```
AuthViewModel.init() 
  → Observes auth state
  → Calls fetchUserRole()
  → Gets role from Firestore "users" collection
  → Updates UI State with role: "GAS_STATION"

User clicks "Sign In"
  → AuthViewModel.login() called
  → Firebase validates credentials
  → If success: 
     - Navigates based on role
     - GAS_STATION → Goes to GasStationScreen
     - Other roles → Goes to HomeScreen
```

**Step 4: Access Control**
```kotlin
// In MainActivity navigation
when (userRole) {
    "GAS_STATION" -> "gasstation" route
    else -> "home" route
}
```

---

## 3. Database Structure

### Firestore Collection: `users`

```
users/
├── user-id-123/
│   ├── id: "user-id-123"
│   ├── username: "pump-station-1"
│   ├── email: "operator@station.com"
│   ├── fullName: "John Operator"
│   ├── role: "GAS_STATION"
│   ├── officeId: "pump-location-01"
│   ├── isActive: true
│   └── createdAt: "2025-12-21T10:30:00"
│
├── user-id-456/
│   ├── id: "user-id-456"
│   ├── username: "dispatcher-1"
│   ├── email: "dispatcher@company.com"
│   ├── fullName: "Jane Dispatcher"
│   ├── role: "DISPATCHER"
│   ├── officeId: "office-main"
│   ├── isActive: true
│   └── createdAt: "2025-12-20T09:15:00"
```

---

## 4. User Roles & Permissions

### Available Roles
```kotlin
enum class UserRole {
    ADMIN,         // Full system access
    DISPATCHER,    // Create/view transactions
    ENCODER,       // Create/view transactions
    GAS_STATION,   // Scan QR & confirm dispensing ✓
    VIEWER         // Read-only access
}
```

### GAS_STATION Role Permissions
- ✅ Login to app
- ✅ Access Gas Station Screen
- ✅ Scan QR codes
- ✅ View pending transactions
- ✅ Confirm fuel dispensing
- ✅ Update transaction status to DISPENSED
- ❌ Create transactions
- ❌ Access reports
- ❌ Manage users

---

## 5. Implementation Changes

### Files Modified

#### AuthViewModel.kt
```kotlin
// Added state fields
userRole: String?
userFullName: String?

// Added method
fetchUserRole() {
    // Fetches user role from Firestore
    // Updates UI state
}
```

#### FirebaseAuthRepository.kt
```kotlin
// Added methods
suspend fun getUserRole(userId: String): String?
suspend fun getUserFullName(userId: String): String?
```

#### MainActivity.kt
```kotlin
// Added role-based routing
when (userRole) {
    "GAS_STATION" -> navigate to gasstation
    else -> navigate to home
}
```

---

## 6. Testing Login

### Test Account Setup

1. **Create in Firebase Console:**
   - Email: `gasstation@test.com`
   - Password: `Test123456`
   - Role: `GAS_STATION`

2. **Firestore Document:**
```json
{
  "id": "test-gas-station-001",
  "username": "pump-station-1",
  "email": "gasstation@test.com",
  "fullName": "Test Operator",
  "role": "GAS_STATION",
  "officeId": "location-1",
  "isActive": true,
  "createdAt": "2025-12-21T10:00:00"
}
```

3. **Test Login Flow**
   - Open app
   - Click "Sign In"
   - Enter: `gasstation@test.com` / `Test123456`
   - Should navigate to GasStationScreen

---

## 7. User Management Backend

### Create New GAS_STATION Account (Admin Script)

```kotlin
// Example in admin panel or backend service
suspend fun createGasStationOperator(
    email: String,
    password: String,
    fullName: String,
    username: String,
    officeId: String
): Result<String> {
    return try {
        // Create Firebase Auth user
        val authResult = firebaseAuth.createUserWithEmailAndPassword(
            email, 
            password
        ).await()
        val userId = authResult.user?.uid ?: throw Exception("Failed to create user")

        // Create Firestore document with GAS_STATION role
        firestore.collection("users")
            .document(userId)
            .set(mapOf(
                "id" to userId,
                "username" to username,
                "email" to email,
                "fullName" to fullName,
                "role" to "GAS_STATION",  // ← Set to GAS_STATION
                "officeId" to officeId,
                "isActive" to true,
                "createdAt" to LocalDateTime.now().toString()
            ))
            .await()

        Result.success(userId)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

---

## 8. Multi-Station Setup

For multiple gas station locations:

### Firestore Structure
```
users/
├── pump-001-op1/  → Station 1, Operator 1
├── pump-001-op2/  → Station 1, Operator 2
├── pump-002-op1/  → Station 2, Operator 1
└── pump-003-op1/  → Station 3, Operator 1

officeId values:
- "pump-station-location-001"
- "pump-station-location-002"
- "pump-station-location-003"
```

### Query Operators by Location
```kotlin
firestore.collection("users")
    .whereEqualTo("role", "GAS_STATION")
    .whereEqualTo("officeId", "pump-station-location-001")
    .get()
```

---

## 9. Security Rules

### Firestore Security Rules
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Users collection
    match /users/{userId} {
      // Only admins can read all users
      allow read: if request.auth.uid != null && 
                    get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == "ADMIN";
      // Users can read their own document
      allow read: if request.auth.uid == userId;
      // Only admins can write
      allow write: if get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == "ADMIN";
    }
    
    // Transactions collection
    match /transactions/{transactionId} {
      // Gas station operators can update status
      allow write: if get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == "GAS_STATION" &&
                      resource.data.status in ["APPROVED", "PENDING"];
      allow read: if request.auth.uid != null;
    }
  }
}
```

---

## 10. Quick Checklist

- [ ] Create Firestore document with `role: "GAS_STATION"`
- [ ] Create Firebase Auth user with matching UID
- [ ] Test login with gas station credentials
- [ ] Verify navigation to GasStationScreen
- [ ] Test QR code scanning
- [ ] Verify transaction status updates
- [ ] Check Firestore updates
- [ ] Test logout functionality

---

## 11. Troubleshooting

### Issue: Can't find user role
**Solution:** 
- Verify Firestore document exists
- Check UID matches Firebase Auth user
- Verify `role` field is exactly `GAS_STATION`

### Issue: Goes to HomeScreen instead of GasStationScreen
**Solution:**
- Check `userRole` is being fetched in AuthViewModel
- Verify role is `GAS_STATION` (case-sensitive)
- Check MainActivity routing logic

### Issue: Login fails
**Solution:**
- Verify email/password in Firebase Auth
- Check Firestore rules allow user to read their own document
- Review auth error in Timber logs

---

## 12. Next Steps

1. Create test gas station accounts in Firebase Console
2. Test login flow with different roles
3. Set up admin panel for account management
4. Implement user management UI in app
5. Add account deactivation feature
6. Set up audit logging
