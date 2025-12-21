# PDF Report Table Structure

## Summary Table (Unchanged - 2 Columns)

```
┌──────────────────────────────┬────────────────────┐
│ Metric                       │ Value              │
├──────────────────────────────┼────────────────────┤
│ Total Liters                 │ 75.0 L             │
│ Total Transactions           │ 3                  │
│ Completed                    │ 3                  │
│ Pending                      │ 0                  │
│ Failed                       │ 0                  │
│ Average Liters/Transaction   │ 25.0 L             │
│ Total Cost                   │ PHP 4672.50        │
│ Average Cost per Liter       │ PHP 62.30          │
└──────────────────────────────┴────────────────────┘
```

---

## Transaction Details Table (Updated - 8 Columns)

### Column Order & Format

```
Column #1: Date
├─ Format: yyyy-MM-dd
├─ Example: 2025-12-21
├─ Source: transaction.createdAt
└─ Purpose: Show when transaction occurred

Column #2: Reference
├─ Format: FS-XXXXXXXX-XXXX
├─ Example: FS-83892989-4456
├─ Source: transaction.referenceNumber
└─ Purpose: Unique transaction identifier

Column #3: Driver
├─ Format: Name text
├─ Example: GARY TENEBRO
├─ Source: transaction.driverFullName (or driverName if null)
└─ Purpose: Who performed the transaction

Column #4: Vehicle
├─ Format: Vehicle type
├─ Example: RESCUE VEHICLE RED
├─ Source: transaction.vehicleType
└─ Purpose: Which vehicle was used

Column #5: Liters
├─ Format: XX.XX
├─ Example: 25.00
├─ Source: transaction.litersToPump
└─ Purpose: Amount of fuel pumped

Column #6: Cost/L
├─ Format: PHP XX.XX
├─ Example: PHP 64.50
├─ Source: transaction.costPerLiter
└─ Purpose: Cost per liter of fuel

Column #7: Total Cost
├─ Format: PHP XX.XX
├─ Example: PHP 1612.50
├─ Source: transaction.getTotalCost() = litersToPump × costPerLiter
└─ Purpose: Total amount for transaction

Column #8: Status
├─ Format: Status name
├─ Example: COMPLETED
├─ Source: transaction.status.name
└─ Purpose: Transaction state
```

---

## Complete Table Example

```
TRANSACTION DETAILS TABLE (8 Columns)

┌──────────┬──────────────────┬──────────────┬──────────────────┬─────────┬────────────┬────────────┬──────────┐
│ Date     │ Reference        │ Driver       │ Vehicle          │ Liters  │ Cost/L     │ Total Cost │ Status   │
├──────────┼──────────────────┼──────────────┼──────────────────┼─────────┼────────────┼────────────┼──────────┤
│ 2025-12-21 │ FS-83892989-4496 │ GARY TENEBRO │ RESCUE VEHICLE   │ 25.00   │ PHP 64.50  │ PHP 1612.50│ COMPLETED│
│          │                  │ RED          │                  │         │            │            │          │
├──────────┼──────────────────┼──────────────┼──────────────────┼─────────┼────────────┼────────────┼──────────┤
│ 2025-12-20 │ FS-81418771-4656 │ JOHN DELA    │ DUMP TRUCK       │ 30.00   │ PHP 62.00  │ PHP 1860.00│ COMPLETED│
│          │                  │ CRUZ         │                  │         │            │            │          │
├──────────┼──────────────────┼──────────────┼──────────────────┼─────────┼────────────┼────────────┼──────────┤
│ 2025-12-19 │ FS-84521234-4789 │ MARIA        │ PICKUP TRUCK     │ 20.00   │ PHP 60.00  │ PHP 1200.00│ COMPLETED│
│          │                  │ GONZALES     │                  │         │            │            │          │
└──────────┴──────────────────┴──────────────┴──────────────────┴─────────┴────────────┴────────────┴──────────┘

Total Records: 3 (showing up to 50)
```

---

## Field Descriptions

### Date Column (NEW)

**Purpose**: Show when each transaction was created

**Format**: ISO 8601 Standard (YYYY-MM-DD)

**Examples**:
- 2025-12-21 (December 21, 2025)
- 2025-11-15 (November 15, 2025)
- 2025-12-01 (December 1, 2025)

**Benefits**:
- ✓ Chronological sorting
- ✓ Easy to read internationally
- ✓ Audit trail
- ✓ Compliance tracking

---

### Reference Column

**Purpose**: Unique identifier for each transaction

**Format**: FS-XXXXXXXX-XXXX

**Pattern**: FS-{timestamp}-{random}

**Examples**:
- FS-83892989-4496
- FS-81418771-4656
- FS-84521234-4789

**Properties**:
- ✓ Unique across all transactions
- ✓ Traceable back to exact time
- ✓ Includes random component for collision avoidance

---

### Driver Column

**Purpose**: Identify who performed the transaction

**Format**: Full name (preferred) or username

**Examples**:
- GARY TENEBRO
- JOHN DELA CRUZ
- MARIA GONZALES

**Notes**:
- Uses driverFullName if available
- Falls back to driverName if fullName is null
- Helps with accountability

---

### Vehicle Column

**Purpose**: Identify which vehicle was serviced

**Format**: Vehicle type/designation

**Examples**:
- RESCUE VEHICLE RED
- DUMP TRUCK
- PICKUP TRUCK

**Information**:
- Vehicle type/class
- May include distinguishing features
- Used for fleet tracking

---

### Liters Column

**Purpose**: Show fuel quantity pumped

**Format**: XX.XX (decimal with 2 places)

**Examples**:
- 25.00
- 30.00
- 20.50

**Validation**:
- ✓ Positive number only
- ✓ Max 500 liters per transaction
- ✓ Required field

---

### Cost/L Column (NEW IN REPORTS)

**Purpose**: Show unit cost of fuel

**Format**: PHP XX.XX

**Examples**:
- PHP 64.50
- PHP 62.00
- PHP 60.00

**Source**:
- User selected from dropdown (₱60.00-₱65.50)
- Saved in costPerLiter field
- Now persisted to Firebase

---

### Total Cost Column (NEW IN REPORTS)

**Purpose**: Show total amount for transaction

**Formula**: Liters × Cost per Liter

**Examples**:
- 25.00 × 64.50 = PHP 1612.50
- 30.00 × 62.00 = PHP 1860.00
- 20.00 × 60.00 = PHP 1200.00

**Calculation**:
- Done via transaction.getTotalCost()
- Automatic conversion to PHP currency

---

### Status Column

**Purpose**: Show transaction completion state

**Possible Values**:
- COMPLETED (✓ Transaction finished)
- PENDING (⏳ Awaiting completion)
- CANCELLED (✗ Transaction cancelled)
- FAILED (✗ Transaction failed)

**Color Coding** (in UI):
- ✓ COMPLETED → Green
- ⏳ PENDING → Yellow
- ✗ FAILED → Red

---

## PDF Generation Code

### Table Creation
```kotlin
val transTable = Table(8)  // 8 columns
    .setWidth(UnitValue.createPercentValue(100f))  // Full width
```

### Header Row
```kotlin
transTable.addHeaderCell(createHeaderCell("Date"))          // Column 1
transTable.addHeaderCell(createHeaderCell("Reference"))     // Column 2
transTable.addHeaderCell(createHeaderCell("Driver"))        // Column 3
transTable.addHeaderCell(createHeaderCell("Vehicle"))       // Column 4
transTable.addHeaderCell(createHeaderCell("Liters"))        // Column 5
transTable.addHeaderCell(createHeaderCell("Cost/L"))        // Column 6
transTable.addHeaderCell(createHeaderCell("Total Cost"))    // Column 7
transTable.addHeaderCell(createHeaderCell("Status"))        // Column 8
```

### Data Rows
```kotlin
filteredData.transactions.take(50).forEach { transaction ->
    // Column 1: Date
    val transactionDate = transaction.createdAt
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    transTable.addCell(createDataCell(transactionDate))
    
    // Column 2: Reference
    transTable.addCell(createDataCell(transaction.referenceNumber))
    
    // Column 3: Driver
    val driverDisplay = transaction.driverFullName ?: transaction.driverName
    transTable.addCell(createDataCell(driverDisplay))
    
    // Column 4: Vehicle
    transTable.addCell(createDataCell(transaction.vehicleType))
    
    // Column 5: Liters
    transTable.addCell(createDataCell(String.format("%.2f", transaction.litersToPump)))
    
    // Column 6: Cost/L
    transTable.addCell(createDataCell(String.format("PHP %.2f", transaction.costPerLiter)))
    
    // Column 7: Total Cost
    transTable.addCell(createDataCell(String.format("PHP %.2f", transaction.getTotalCost())))
    
    // Column 8: Status
    transTable.addCell(createDataCell(transaction.status.name))
}
```

---

## Width Distribution

**Table Width**: 100% of page

**Estimated Column Widths** (proportional):

```
Total: 100%

Date:        10%  (e.g., 2025-12-21)
Reference:   14%  (e.g., FS-83892989-4496)
Driver:      16%  (e.g., GARY TENEBRO)
Vehicle:     18%  (e.g., RESCUE VEHICLE RED)
Liters:      10%  (e.g., 25.00)
Cost/L:      10%  (e.g., PHP 64.50)
Total Cost:  12%  (e.g., PHP 1612.50)
Status:      10%  (e.g., COMPLETED)
```

*(Note: PDF library handles column widths automatically)*

---

## Data Retrieval Query

```
FROM: Firestore transactions collection
FILTER: By date range (dateFrom to dateTo)
SELECT: All transaction documents
CONVERT: DocumentSnapshot → FuelTransaction object
PROCESS: Calculate totals and aggregates
LIMIT: First 50 records for PDF display
ORDER: By createdAt (default Firestore order)
```

---

## Summary

The PDF report now includes a comprehensive 8-column transaction table with:
- ✅ Date of transaction
- ✅ Reference number for tracking
- ✅ Driver identification
- ✅ Vehicle information
- ✅ Fuel quantity
- ✅ Unit cost
- ✅ Total transaction cost
- ✅ Completion status

All data is sourced from Firebase Firestore and formatted professionally for export, printing, and sharing.

