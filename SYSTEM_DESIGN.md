# FuelHub - MDRRMO Fuel Wallet & Gas Slip Transaction System

## Architecture Overview

The system is built using **Clean Architecture** with separation of concerns across multiple layers:

```
┌─────────────────────────────────────────┐
│      Presentation Layer (Compose UI)    │
├─────────────────────────────────────────┤
│    ViewModel / MVVM Pattern             │
├─────────────────────────────────────────┤
│      Domain Layer (Use Cases)           │
│   - CreateFuelTransactionUseCase        │
│   - ApproveTransactionUseCase           │
│   - GenerateReportsUseCase              │
├─────────────────────────────────────────┤
│      Repository Layer (Interfaces)      │
│   - FuelWalletRepository                │
│   - FuelTransactionRepository           │
│   - GasSlipRepository                   │
│   - AuditLogRepository                  │
├─────────────────────────────────────────┤
│      Data Layer (Implementation)        │
│   - Room Database                       │
│   - Local Caching                       │
└─────────────────────────────────────────┘
```

## Core Components

### 1. Data Models

#### FuelWallet
- **Purpose**: Tracks the allocated fuel budget in liters
- **Key Fields**:
  - `id`: Unique identifier
  - `officeId`: Associated MDRRMO office
  - `balanceLiters`: Current fuel allocation
  - `maxCapacityLiters`: Maximum wallet capacity
  - `lastUpdated`: Timestamp of last transaction

#### FuelTransaction
- **Purpose**: Represents a fuel request that results in a gas slip
- **Key Fields**:
  - `id`: Unique transaction identifier
  - `referenceNumber`: Traceable reference (FS-TIMESTAMP-RANDOM)
  - `walletId`: Associated fuel wallet
  - `vehicleId`: Vehicle information
  - `litersToPump`: Requested fuel amount
  - `status`: PENDING, APPROVED, COMPLETED, CANCELLED, FAILED
  - `createdBy`: User who initiated transaction

#### GasSlip
- **Purpose**: Printable/dispendable document for gas stations
- **Key Fields**: All transaction details plus:
  - `isUsed`: Whether fuel has been dispensed
  - `usedAt`: Timestamp of fuel dispensing
  - Formatted for printing (thermal printer or PDF)

#### AuditLog
- **Purpose**: Immutable record of all wallet modifications
- **Tracks**:
  - Previous and new balance
  - Action performed
  - User who performed action
  - Timestamp

### 2. Transaction Workflow

```
User Creates Transaction
        ↓
Validate Authorization (User Role)
        ↓
Validate Transaction Input
  - Liters > 0
  - Required fields present
  - Vehicle exists and is active
        ↓
Check Fuel Wallet Balance
        ↓
    ├─ INSUFFICIENT ──→ Reject (InsufficientFuelException)
    │
    └─ SUFFICIENT ──→ Create Transaction Record
        ↓
    Deduct from Fuel Wallet (ATOMIC)
        ↓
    Generate Gas Slip
        ↓
    Log Audit Trail
        ↓
    Return Transaction + GasSlip
```

### 3. Use Cases

#### CreateFuelTransactionUseCase
Executes the complete transaction workflow:
1. Validates user authorization
2. Validates transaction input
3. Checks wallet balance
4. Creates transaction record
5. Deducts fuel (atomic operation)
6. Generates gas slip
7. Logs audit trail

**Throws**:
- `UnauthorizedException`: User not authorized
- `InsufficientFuelException`: Wallet balance insufficient
- `TransactionValidationException`: Input validation failed
- `EntityNotFoundException`: Vehicle or wallet not found

#### ApproveTransactionUseCase
Allows admins to approve pending transactions (optional approval workflow).

### 4. Repository Layer

**Abstractions**:
- `FuelWalletRepository`: Wallet CRUD and refill operations
- `FuelTransactionRepository`: Transaction CRUD and querying
- `GasSlipRepository`: Gas slip management and printing
- `AuditLogRepository`: Immutable audit trail
- `VehicleRepository`: Vehicle management
- `UserRepository`: User management and role-based access

## Business Rules

### Fuel Wallet Rules
✅ Balance is tracked in **liters** (not monetary value)  
✅ Wallet deduction is **atomic and irreversible**  
✅ Balance updated only after successful transaction creation  
✅ All modifications logged for audit  

### Transaction Rules
✅ Cannot exceed remaining wallet balance  
✅ Fuel type must match vehicle requirements  
✅ Only authorized users can create transactions  
✅ Each gas slip is unique and traceable  
✅ Reference number: `FS-{TIMESTAMP}-{RANDOM}`  

### Role-Based Access Control
- **ADMIN**: Can create, approve, and view all transactions
- **DISPATCHER**: Can create and view transactions
- **ENCODER**: Can create and view transactions
- **VIEWER**: Read-only access to reports

## Gas Slip Contents (Printable)

Every generated gas slip includes:
- Driver Name
- Vehicle Type / Plate Number
- Destination
- Purpose of Trip
- Passengers (optional)
- Fuel Type (Gasoline or Diesel)
- Liters to Dispense
- Transaction Date & Reference Number
- MDRRMO Office Identification

## System Features

### Wallet Management
- Real-time balance monitoring
- Automatic deduction on transaction completion
- Wallet refill capability
- Balance history tracking

### Transaction Management
- Create, approve, and track transactions
- Generate unique, printable gas slips
- Cancel or reject transactions
- Search by date, vehicle, driver, status

### Reporting
- Daily fuel usage reports
- Weekly/monthly aggregations
- Vehicle-specific consumption analysis
- User activity audit trail
- Unused gas slip tracking

### Audit & Compliance
- Immutable audit logs
- User action tracking
- Balance change history
- Timestamp-based verification
- IP address logging (optional)

## Data Integrity

### Atomicity
- Wallet deductions are atomic
- Transaction state ensures no partial updates
- Database transactions for multi-step operations

### Consistency
- Balance can never go negative
- Each transaction creates exactly one gas slip
- Audit log entries are immutable

### Isolation
- Concurrent transactions don't interfere
- Database-level locking for wallet updates
- Optimistic locking for gas slips

### Durability
- All changes persisted to database
- Audit trail permanently stored
- Recovery mechanism for failed transactions

## Implementation Notes

### Database
- **Room ORM** for SQLite
- Tables: wallets, transactions, gas_slips, audit_logs, vehicles, users

### Concurrency
- Kotlin Coroutines for async operations
- Database transaction isolation
- Locking for critical wallet operations

### Security
- User authentication required
- Role-based authorization checks
- Input validation and sanitization
- Audit logging for accountability

## Future Enhancements

1. **Cloud Sync**: Sync wallets/transactions to backend
2. **Offline-First**: Full offline capability with sync
3. **Advanced Analytics**: Fuel consumption predictions
4. **Integration**: Connect to gas station partner APIs
5. **Mobile Wallet**: QR-code based gas slip scanning
6. **SMS Notifications**: Transaction alerts
7. **Multi-Language**: Localization support
