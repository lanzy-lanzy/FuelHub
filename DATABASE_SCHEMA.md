# FuelHub Database Schema

## Tables

### 1. fuel_wallets
Tracks allocated fuel budgets for each MDRRMO office.

```sql
CREATE TABLE fuel_wallets (
    id TEXT PRIMARY KEY,
    office_id TEXT NOT NULL,
    balance_liters REAL NOT NULL,
    max_capacity_liters REAL NOT NULL,
    last_updated TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    UNIQUE(office_id),
    CHECK(balance_liters >= 0),
    CHECK(balance_liters <= max_capacity_liters)
);
```

**Columns**:
- `id`: Primary key (UUID)
- `office_id`: Foreign key to office
- `balance_liters`: Current fuel allocation in liters
- `max_capacity_liters`: Maximum wallet capacity
- `last_updated`: Timestamp of last modification
- `created_at`: Wallet creation timestamp

**Constraints**:
- Balance must be non-negative
- Balance cannot exceed max capacity
- One wallet per office

---

### 2. vehicles
Tracks MDRRMO fleet vehicles.

```sql
CREATE TABLE vehicles (
    id TEXT PRIMARY KEY,
    plate_number TEXT NOT NULL,
    vehicle_type TEXT NOT NULL,
    fuel_type TEXT NOT NULL,
    driver_name TEXT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL,
    UNIQUE(plate_number),
    CHECK(fuel_type IN ('GASOLINE', 'DIESEL'))
);
```

**Columns**:
- `id`: Primary key (UUID)
- `plate_number`: Vehicle plate number (unique)
- `vehicle_type`: Type of vehicle (e.g., "Ambulance", "Fire Truck")
- `fuel_type`: GASOLINE or DIESEL
- `driver_name`: Assigned driver name
- `is_active`: Whether vehicle is available
- `created_at`: Vehicle registration timestamp

---

### 3. fuel_transactions
Records all fuel transaction requests.

```sql
CREATE TABLE fuel_transactions (
    id TEXT PRIMARY KEY,
    reference_number TEXT NOT NULL,
    wallet_id TEXT NOT NULL,
    vehicle_id TEXT NOT NULL,
    driver_name TEXT NOT NULL,
    vehicle_type TEXT NOT NULL,
    fuel_type TEXT NOT NULL,
    liters_to_pump REAL NOT NULL,
    destination TEXT NOT NULL,
    trip_purpose TEXT NOT NULL,
    passengers TEXT,
    status TEXT NOT NULL,
    created_by TEXT NOT NULL,
    approved_by TEXT,
    created_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    notes TEXT,
    UNIQUE(reference_number),
    FOREIGN KEY(wallet_id) REFERENCES fuel_wallets(id),
    FOREIGN KEY(vehicle_id) REFERENCES vehicles(id),
    FOREIGN KEY(created_by) REFERENCES users(id),
    FOREIGN KEY(approved_by) REFERENCES users(id),
    CHECK(liters_to_pump > 0),
    CHECK(status IN ('PENDING', 'APPROVED', 'COMPLETED', 'CANCELLED', 'FAILED')),
    CHECK(fuel_type IN ('GASOLINE', 'DIESEL'))
);
```

**Columns**:
- `id`: Primary key (UUID)
- `reference_number`: Unique traceable reference (FS-TIMESTAMP-RANDOM)
- `wallet_id`: Associated fuel wallet
- `vehicle_id`: Associated vehicle
- `driver_name`: Driver name (denormalized)
- `vehicle_type`: Vehicle type (denormalized)
- `fuel_type`: Fuel type (denormalized)
- `liters_to_pump`: Amount of fuel requested
- `destination`: Trip destination
- `trip_purpose`: Purpose of the trip
- `passengers`: Optional comma-separated passenger names
- `status`: PENDING, APPROVED, COMPLETED, CANCELLED, or FAILED
- `created_by`: User ID who created transaction
- `approved_by`: User ID who approved (if applicable)
- `created_at`: Transaction creation timestamp
- `completed_at`: Transaction completion timestamp
- `notes`: Optional notes

**Indices**:
- `UNIQUE(reference_number)`: Fast lookup by reference
- `INDEX(wallet_id, created_at)`: Wallet transaction history
- `INDEX(vehicle_id)`: Vehicle transaction lookup
- `INDEX(status, created_at)`: Status-based queries

---

### 4. gas_slips
Printable gas slip documents.

```sql
CREATE TABLE gas_slips (
    id TEXT PRIMARY KEY,
    transaction_id TEXT NOT NULL,
    reference_number TEXT NOT NULL,
    driver_name TEXT NOT NULL,
    vehicle_type TEXT NOT NULL,
    vehicle_plate_number TEXT NOT NULL,
    destination TEXT NOT NULL,
    trip_purpose TEXT NOT NULL,
    passengers TEXT,
    fuel_type TEXT NOT NULL,
    liters_to_pump REAL NOT NULL,
    transaction_date TIMESTAMP NOT NULL,
    mdrrmo_office_id TEXT NOT NULL,
    mdrrmo_office_name TEXT NOT NULL,
    generated_at TIMESTAMP NOT NULL,
    is_used BOOLEAN NOT NULL DEFAULT 0,
    used_at TIMESTAMP,
    UNIQUE(reference_number),
    FOREIGN KEY(transaction_id) REFERENCES fuel_transactions(id),
    CHECK(fuel_type IN ('GASOLINE', 'DIESEL')),
    CHECK(liters_to_pump > 0)
);
```

**Columns**:
- `id`: Primary key (UUID)
- `transaction_id`: Associated transaction
- `reference_number`: Same as transaction reference
- `driver_name`: Driver name
- `vehicle_type`: Vehicle type
- `vehicle_plate_number`: Vehicle plate
- `destination`: Trip destination
- `trip_purpose`: Trip purpose
- `passengers`: Optional passengers
- `fuel_type`: GASOLINE or DIESEL
- `liters_to_pump`: Fuel amount
- `transaction_date`: Transaction timestamp
- `mdrrmo_office_id`: Office identification
- `mdrrmo_office_name`: Office name
- `generated_at`: Gas slip generation timestamp
- `is_used`: Whether fuel has been dispensed
- `used_at`: Timestamp when fuel was dispensed

**Indices**:
- `UNIQUE(reference_number)`: Fast lookup
- `INDEX(is_used, generated_at)`: Find unused slips
- `INDEX(mdrrmo_office_id)`: Office-specific slips

---

### 5. audit_logs
Immutable audit trail for all modifications.

```sql
CREATE TABLE audit_logs (
    id TEXT PRIMARY KEY,
    wallet_id TEXT NOT NULL,
    action TEXT NOT NULL,
    transaction_id TEXT,
    performed_by TEXT NOT NULL,
    previous_balance REAL NOT NULL,
    new_balance REAL NOT NULL,
    liters_difference REAL NOT NULL,
    description TEXT NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    ip_address TEXT,
    FOREIGN KEY(wallet_id) REFERENCES fuel_wallets(id),
    FOREIGN KEY(transaction_id) REFERENCES fuel_transactions(id),
    FOREIGN KEY(performed_by) REFERENCES users(id)
);
```

**Columns**:
- `id`: Primary key (UUID)
- `wallet_id`: Associated wallet
- `action`: Action type (e.g., TRANSACTION_CREATED_AND_WALLET_DEDUCTED)
- `transaction_id`: Associated transaction (if applicable)
- `performed_by`: User ID who performed action
- `previous_balance`: Balance before action
- `new_balance`: Balance after action
- `liters_difference`: Change in liters (positive or negative)
- `description`: Human-readable description
- `timestamp`: Action timestamp
- `ip_address`: Optional IP address for security

**Indices**:
- `INDEX(wallet_id, timestamp)`: Wallet history
- `INDEX(performed_by, timestamp)`: User activity
- `INDEX(action, timestamp)`: Action history

**Constraints**:
- All entries are append-only (no updates or deletes)
- Timestamp immutable once created

---

### 6. users
System users with role-based access control.

```sql
CREATE TABLE users (
    id TEXT PRIMARY KEY,
    username TEXT NOT NULL,
    email TEXT NOT NULL,
    full_name TEXT NOT NULL,
    role TEXT NOT NULL,
    office_id TEXT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL,
    UNIQUE(username),
    UNIQUE(email),
    CHECK(role IN ('ADMIN', 'DISPATCHER', 'ENCODER', 'VIEWER'))
);
```

**Columns**:
- `id`: Primary key (UUID)
- `username`: Unique username
- `email`: Unique email
- `full_name`: User full name
- `role`: ADMIN, DISPATCHER, ENCODER, or VIEWER
- `office_id`: Associated office
- `is_active`: Whether user is active
- `created_at`: User creation timestamp

**Indices**:
- `UNIQUE(username)`: Fast authentication lookup
- `UNIQUE(email)`: Email verification
- `INDEX(office_id)`: Office users
- `INDEX(role)`: Role-based queries

---

## Query Patterns

### Get Current Wallet Balance
```sql
SELECT balance_liters FROM fuel_wallets 
WHERE office_id = ?
```

### Get Transaction History for Wallet
```sql
SELECT * FROM fuel_transactions 
WHERE wallet_id = ? AND created_at >= ?
ORDER BY created_at DESC
```

### Get Unused Gas Slips
```sql
SELECT * FROM gas_slips 
WHERE is_used = 0 AND mdrrmo_office_id = ?
ORDER BY generated_at DESC
```

### Get Daily Fuel Usage
```sql
SELECT 
    SUM(liters_to_pump) as total_liters,
    COUNT(*) as transaction_count
FROM fuel_transactions 
WHERE wallet_id = ? AND DATE(created_at) = ?
  AND status = 'COMPLETED'
```

### Get Audit Trail for Balance Changes
```sql
SELECT * FROM audit_logs 
WHERE wallet_id = ? AND DATE(timestamp) = ?
ORDER BY timestamp DESC
```

### Get User Activity
```sql
SELECT * FROM audit_logs 
WHERE performed_by = ? AND DATE(timestamp) >= ?
ORDER BY timestamp DESC
```

---

## Data Integrity Constraints

1. **Referential Integrity**
   - Foreign keys enforced at database level
   - Cascade delete for transactions when wallet deleted

2. **Domain Constraints**
   - Fuel type enum validation
   - Liter amounts must be positive
   - Status enum validation
   - Role enum validation

3. **Atomicity**
   - Wallet updates wrapped in transactions
   - Transaction + GasSlip creation atomic
   - Audit log creation synchronized with changes

4. **Audit Trail**
   - Append-only audit logs
   - All modifications tracked
   - User accountability enforced

---

## Performance Optimization

### Indices
- Primary keys: Automatic
- Foreign keys: Automatic
- Frequently queried columns: Manual indices
- Composite indices for common WHERE + ORDER BY combinations

### Denormalization
- Driver name, vehicle type in transactions (for query performance)
- Fuel type duplicated (reduces joins)

### Partitioning Strategy
- Consider date-based partitioning for audit_logs if > 1M rows
- Archive old gas slips (> 1 year) to separate table

---

## Backup & Recovery

### Backup Strategy
- Daily automated backups
- Keep last 30 days of backups
- Off-device backup storage

### Recovery
- Point-in-time recovery from audit logs
- Transaction rollback via audit trail
- Wallet balance recalculation from transaction history
