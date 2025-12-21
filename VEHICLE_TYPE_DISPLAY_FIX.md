# Vehicle Type Display Fix in Reports

## Problem
The reports (exported PDF, print, and share) were displaying the vehicle's unique ID (UUID) instead of the vehicle type, making reports difficult to read. For example:
- **Before**: `892a998f-174d-4f52-9693-dd9f762f3006`
- **After**: `Van`, `Truck`, `Motorcycle`, etc.

## Root Cause
The code was using `transaction.vehicleId` (the database UUID) instead of `transaction.vehicleType` (the human-readable vehicle type like "Van", "Truck", etc.).

## Solution Implemented

### 1. Updated ReportPdfGenerator
**File:** `app/src/main/java/dev/ml/fuelhub/data/util/ReportPdfGenerator.kt`

Changed the PDF report generation to use `vehicleType`:
```kotlin
// Before
transTable.addCell(createDataCell(transaction.vehicleId))

// After
transTable.addCell(createDataCell(transaction.vehicleType))
```

This affects:
- Export as PDF reports
- Print reports
- Shared PDF reports

### 2. Updated ReportScreenEnhanced
**File:** `app/src/main/java/dev/ml/fuelhub/presentation/screen/ReportScreenEnhanced.kt`

Updated the on-screen transaction display to also show vehicle type:
```kotlin
// Before
"${transaction.driverName} • ${transaction.vehicleId}"

// After
"${transaction.driverName} • ${transaction.vehicleType}"
```

This affects:
- Live report display on the Reports screen
- Quick preview when viewing transactions

## Data Model
The FuelTransaction model includes both fields:
```kotlin
data class FuelTransaction(
    val vehicleId: String,      // UUID - unique identifier (e.g., 892a998f-174d-4f52-9693-dd9f762f3006)
    val vehicleType: String,    // Human-readable type (e.g., "Van", "Truck", "Motorcycle")
    // ... other fields
)
```

## Impact
All report formats now display the vehicle type instead of UUID:

### Before (confusing)
| Reference | Driver | Vehicle | Liters | Status |
|-----------|--------|---------|--------|--------|
| FS-157582... | aldren | 892a998f-174d-4f52-9693-dd9f762f3006 | 25.00 | PENDING |

### After (clear)
| Reference | Driver | Vehicle | Liters | Status |
|-----------|--------|---------|--------|--------|
| FS-157582... | aldren | Van | 25.00 | PENDING |

## Files Modified
1. `app/src/main/java/dev/ml/fuelhub/data/util/ReportPdfGenerator.kt`
2. `app/src/main/java/dev/ml/fuelhub/presentation/screen/ReportScreenEnhanced.kt`

## Testing
- ✅ Export as PDF - Shows vehicle type in PDF
- ✅ Print Report - Shows vehicle type in printed PDF
- ✅ Share Report - Shows vehicle type in shared PDF
- ✅ On-screen display - Shows vehicle type in live report

## Backward Compatibility
This change is fully backward compatible as both `vehicleId` and `vehicleType` are already stored in the FuelTransaction model.
