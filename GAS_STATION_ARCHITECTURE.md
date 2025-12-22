# Gas Station Login & Role-Based Access Architecture

## System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                      Gas Station Operator                        │
│                                                                   │
│  Opens App → LoginScreen → Enters Credentials → Sign In          │
└────────────────────┬────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────────┐
│                  Firebase Authentication                         │
│                                                                   │
│  Validates email/password against Auth database                  │
│  Returns User ID (abc123...)                                    │
└────────────────────┬────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────────┐
│              AuthViewModel.login() Success                       │
│                                                                   │
│  1. Sets isLoggedIn = true                                       │
│  2. Calls fetchUserRole(userId)                                  │
└────────────────────┬────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────────┐
│              Firestore - users Collection                        │
│                                                                   │
│  GET users/[userId]                                              │
│  Returns: {                                                       │
│    role: "GAS_STATION",                                          │
│    fullName: "John Operator"                                     │
│  }                                                                │
└────────────────────┬────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────────┐
│         AuthViewModel.fetchUserRole() Updates State             │
│                                                                   │
│  _uiState.userRole = "GAS_STATION"                              │
│  _uiState.userFullName = "John Operator"                        │
└────────────────────┬────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────────┐
│            MainActivity Navigation Logic                         │
│                                                                   │
│  when (userRole) {                                               │
│    "GAS_STATION" → GasStationScreen()                           │
│    "DISPATCHER" → HomeScreen()                                  │
│    "ADMIN" → HomeScreen()                                       │
│  }                                                                │
└────────────────────┬────────────────────────────────────────────┘
                     │
                     ▼
         ╔═══════════════════════════╗
         ║  Gas Station Screen       ║
         ║                           ║
         ║  ┌─────────────────────┐  ║
         ║  │  Scan QR Code       │  ║
         ║  └─────────────────────┘  ║
         ║  ┌─────────────────────┐  ║
         ║  │ Pending Txns List   │  ║
         ║  └─────────────────────┘  ║
         ║  ┌─────────────────────┐  ║
         ║  │ Confirm Dispensed   │  ║
         ║  └─────────────────────┘  ║
         ╚═══════════════════════════╝
```

---

## Data Flow

### 1. Account Creation

```
Admin / Firebase Console
         │
         ▼
Firebase Authentication
├─ Email: operator@station.com
├─ Password: securePassword123
└─ User ID: abc123xyz...
         │
         ▼
Firestore users Collection
├─ id: "abc123xyz"
├─ username: "pump-station-1"
├─ email: "operator@station.com"
├─ fullName: "John Operator"
├─ role: "GAS_STATION"
├─ officeId: "location-1"
├─ isActive: true
└─ createdAt: "2025-12-21T10:00:00"
```

### 2. Login Process

```
User Input (email, password)
         │
         ▼
AuthViewModel.login()
         │
         ▼
FirebaseAuthRepository.login(email, password)
         │
         ▼
Firebase Auth: signInWithEmailAndPassword()
         ├─ ✓ Success → Return userId
         └─ ✗ Error → Return error message
         │
         ▼
AuthViewModel State Updated
├─ isLoading: false
├─ isLoggedIn: true
├─ userId: "abc123xyz"
└─ Trigger: fetchUserRole()
         │
         ▼
FirebaseAuthRepository.getUserRole(userId)
         │
         ▼
Firestore Query: GET users/abc123xyz
         │
         ▼
Parse Response
├─ role: "GAS_STATION"
└─ fullName: "John Operator"
         │
         ▼
AuthViewModel State Updated
├─ userRole: "GAS_STATION"
└─ userFullName: "John Operator"
         │
         ▼
MainActivity Checks Role
         │
         ▼
Navigate to GasStationScreen
```

### 3. Gas Station Operations

```
GasStationScreen Loads
         │
         ▼
Load Pending Transactions
└─ Status IN (PENDING, APPROVED)
         │
         ▼
Display Transaction List
├─ Reference Number
├─ Vehicle
├─ Driver
├─ Fuel Type
└─ Quick Confirm Buttons
         │
         ▼
User clicks "Scan QR Code"
         │
         ▼
QRScannerDialog Opens
└─ Camera/QR Scanner UI
         │
         ▼
User scans QR
         │
         ▼
Raw QR Data Received
└─ "REF:TXN001|PLATE:ABC123|DRIVER:John|FUEL:DIESEL|LITERS:50.0|DATE:2025-12-21"
         │
         ▼
QRCodeScanner.parseQRCode()
         │
         ▼
Create ScannedTransaction Object
├─ referenceNumber: "TXN001"
├─ vehiclePlate: "ABC123"
├─ driverName: "John"
├─ fuelType: "DIESEL"
├─ liters: 50.0
└─ date: "2025-12-21"
         │
         ▼
Find Matching Transaction
└─ WHERE referenceNumber = "TXN001"
         │
         ▼
Show ConfirmDispensedDialog
├─ Show Scanned Data ✓
├─ Show Database Data
└─ Match verification
         │
         ▼
User clicks "Confirm Dispensed"
         │
         ▼
TransactionViewModel.confirmFuelDispensed()
         │
         ▼
Update Transaction
├─ status: DISPENSED (was APPROVED)
├─ completedAt: LocalDateTime.now()
└─ Save to Firestore
         │
         ▼
Show SuccessDialog
         │
         ▼
Refresh Transaction List
└─ Remove from pending list
```

---

## Role Hierarchy & Permissions

```
┌─────────────────────────────────────────────────────────────┐
│                        Roles Pyramid                         │
└─────────────────────────────────────────────────────────────┘

                        ▲
                       ╱ ╲
                      ╱   ╲
                    ADMIN  ← Full System Access
                    ╱       ╲
                   ╱         ╲
            DISPATCHER    ENCODER  ← Create Transactions
           ╱              ╲
          ╱                ╲
     GAS_STATION        VIEWER   ← Read-Only
         │                  │
         │                  │
    Confirm              View
    Dispensing          Reports


┌─────────────────────────────────────────────────────────────┐
│              GAS_STATION Permissions                         │
└─────────────────────────────────────────────────────────────┘

✅ Can:
   ├─ Login to app
   ├─ Access Gas Station Screen
   ├─ View pending transactions
   ├─ Scan QR codes
   ├─ Confirm fuel dispensing
   └─ Update transaction status to DISPENSED

❌ Cannot:
   ├─ Create new transactions
   ├─ Access reports screen
   ├─ View wallet balance
   ├─ Manage vehicles
   ├─ Manage drivers
   └─ View system analytics
```

---

## Component Interactions

### AuthViewModel
```
State:
├─ isLoading: Boolean
├─ isLoggedIn: Boolean
├─ userId: String?
├─ userRole: String?          ← NEW
├─ userFullName: String?      ← NEW
└─ error: String?

Methods:
├─ login(email, password)
├─ logout()
├─ fetchUserRole()            ← NEW
└─ resetPassword(email)
```

### FirebaseAuthRepository
```
Methods:
├─ login(email, password)
├─ register(email, password, fullName, username)
├─ logout()
├─ observeAuthState()
├─ resetPassword(email)
├─ getUserRole(userId)        ← NEW
└─ getUserFullName(userId)    ← NEW
```

### MainActivity
```
Navigation:
├─ "login" → LoginScreen
├─ "register" → RegisterScreen
├─ "home" → HomeScreen (DISPATCHER, ADMIN, VIEWER)
├─ "gasstation" → GasStationScreen (GAS_STATION) ← NEW
├─ "transaction" → TransactionScreenEnhanced
├─ "wallet" → WalletScreenEnhanced
├─ "reports" → ReportScreenEnhanced
└─ ...other routes...

Role-Based Routing:
when (userRole) {
    "GAS_STATION" → "gasstation"
    else → "home"
}
```

---

## Database Schema

### Firestore Collection: `users`

```
Document: {userId}
├─ id: String                     ← Firebase Auth UID
├─ username: String               ← Unique username
├─ email: String                  ← Email address
├─ fullName: String               ← Display name
├─ role: String                   ← ADMIN|DISPATCHER|GAS_STATION|etc
├─ officeId: String               ← Location identifier
├─ isActive: Boolean              ← Account status
└─ createdAt: String              ← Creation timestamp

Indexes (Recommended):
├─ role, isActive (for querying by role)
├─ officeId, role (for location-specific operators)
└─ email (for lookups)
```

### Firestore Collection: `transactions`

```
Document: {transactionId}
├─ id: String
├─ referenceNumber: String
├─ status: String                 ← PENDING→APPROVED→DISPENSED→COMPLETED
├─ vehicleId: String
├─ driverName: String
├─ fuelType: String
├─ litersToPump: Double
├─ createdAt: Timestamp
├─ completedAt: Timestamp?        ← Set when DISPENSED
└─ ...other fields...
```

---

## State Transitions

### User Login State

```
┌──────────┐
│  Initial │
│(Not Auth)│
└────┬─────┘
     │
     │ login(email, password)
     ▼
┌──────────────┐
│  Loading     │
│(Validating)  │
└────┬─────────┘
     │
     ├─ ✓ Success
     │  └─→ Fetch Role
     │       │
     │       ▼
     │    ┌──────────────┐
     │    │Logged In     │  ◄── AuthViewModel Updates
     │    │+ Role        │       userRole = "GAS_STATION"
     │    │+ FullName    │
     │    └──────────────┘
     │
     └─ ✗ Error
        └─→ Show Error Message
```

### Transaction Status Transitions

```
       ┌────────────────┐
       │    PENDING     │ ◄── Created by DISPATCHER
       └────────┬───────┘
                │
         approve()
                │
                ▼
       ┌────────────────┐
       │   APPROVED     │ ◄── Approved by ADMIN/DISPATCHER
       └────────┬───────┘
                │
      confirmFuelDispensed()
                │
                ▼
       ┌────────────────┐
       │   DISPENSED    │ ◄── Confirmed by GAS_STATION ✓
       └────────┬───────┘
                │
        complete()
                │
                ▼
       ┌────────────────┐
       │   COMPLETED    │ ◄── Wallet deducted
       └────────────────┘
```

---

## Security Flow

```
┌───────────────────────────────────────────┐
│  Firestore Security Rules                 │
└───────────────────────────────────────────┘

1. Authentication Check
   ├─ request.auth.uid exists?
   └─ ✓ Proceed / ✗ Deny

2. Document Access Check
   ├─ Can user read this document?
   ├─ Can user write to this document?
   └─ Depends on role

3. Field-Level Security
   ├─ Role field read by ADMIN only
   ├─ Transaction status updateable by GAS_STATION
   └─ Wallet balance read-only for operators

4. Collection-Level Rules
   ├─ users: Own doc read, Admin write
   ├─ transactions: Role-based access
   └─ wallets: Owner read, Admin write
```

---

## Integration Timeline

```
Time    Event                          Component
────    ─────                          ─────────
T0      User opens app                 App
T1      Navigates to login             LoginScreen
T2      Enters credentials             LoginScreen
T3      Clicks "Sign In"               LoginScreen
T4      AuthViewModel.login()          AuthViewModel
T5      Firebase validates             FirebaseAuth
T6      Returns userId                 FirebaseAuth
T7      Sets isLoggedIn=true           AuthViewModel
T8      Calls fetchUserRole()          AuthViewModel
T9      Queries Firestore              Firestore
T10     Returns role + fullName        Firestore
T11     Updates UI State               AuthViewModel
T12     MainActivity checks role       MainActivity
T13     Routes based on role           MainActivity
T14     Loads GasStationScreen         GasStationScreen
T15     User sees pending txns         GasStationScreen
```

---

## Key Files & Their Roles

```
┌─────────────────────────────────────────────┐
│  Authentication & Authorization             │
├─────────────────────────────────────────────┤
│ AuthViewModel                               │
│ ├─ Manages login/logout flow               │
│ ├─ Fetches user role                       │
│ └─ Updates UI State                        │
│                                             │
│ FirebaseAuthRepository                     │
│ ├─ Communicates with Firebase Auth         │
│ ├─ Queries Firestore for user details      │
│ └─ Manages auth state                      │
│                                             │
│ LoginScreen                                 │
│ ├─ UI for login form                       │
│ ├─ Calls AuthViewModel.login()             │
│ └─ Navigates on success                    │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│  Gas Station Operations                     │
├─────────────────────────────────────────────┤
│ GasStationScreen                            │
│ ├─ Main UI for operators                   │
│ ├─ Displays pending transactions           │
│ └─ Manages confirmation dialogs            │
│                                             │
│ QRCodeScanner                               │
│ ├─ Parses QR data                          │
│ ├─ Validates transaction                   │
│ └─ Returns structured data                 │
│                                             │
│ TransactionViewModel                       │
│ ├─ Loads transactions                      │
│ ├─ Updates status to DISPENSED             │
│ └─ Syncs with Firestore                    │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│  Navigation & Routing                       │
├─────────────────────────────────────────────┤
│ MainActivity                                │
│ ├─ Manages NavHost                         │
│ ├─ Checks user role                        │
│ ├─ Routes to appropriate screen            │
│ └─ Handles deep links                      │
└─────────────────────────────────────────────┘
```

---

## Summary

**Gas Station Login System provides:**

1. ✅ Role-based authentication
2. ✅ Automatic role detection
3. ✅ Screen routing based on role
4. ✅ Secure Firestore integration
5. ✅ User-friendly UI
6. ✅ Real-time transaction management
7. ✅ QR code verification
8. ✅ Status tracking

All components are integrated and production-ready.
