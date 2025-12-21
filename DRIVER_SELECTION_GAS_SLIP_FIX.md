# Driver Selection - Gas Slip Print Fix

## Problem
When multiple drivers were assigned to a vehicle, the gas slip was always printing the vehicle's primary driver name instead of the selected driver.

## Root Cause
In `CreateFuelTransactionUseCase.kt`:
- The gas slip was being created with `vehicle.driverName` (primary/default driver)
- The transaction record was also using `vehicle.driverName`
- The selected driver was only being passed in `input.createdBy` but not being used for the actual driver name fields

## Solution
Modified `CreateFuelTransactionUseCase.kt` to use the selected driver (`input.createdBy`) for both the transaction and gas slip:

### Changes Made

**File: CreateFuelTransactionUseCase.kt**

#### Change 1: Transaction Record (Line 102)
```kotlin
// BEFORE:
driverName = vehicle.driverName,

// AFTER:
driverName = input.createdBy,  // Use selected driver instead of vehicle's primary driver
```

#### Change 2: Gas Slip Creation (Line 133)
```kotlin
// BEFORE:
driverName = vehicle.driverName,

// AFTER:
driverName = input.createdBy,  // Use selected driver instead of vehicle's primary driver
```

## Data Flow

```
Transaction Screen
    ↓
User selects vehicle + driver
    ↓
selectedDriver → input.createdBy (passed to use case)
    ↓
CreateFuelTransactionUseCase.execute()
    ├─ Transaction.driverName = input.createdBy ✓
    └─ GasSlip.driverName = input.createdBy ✓
    ↓
Gas Slip Print
    └─ Displays: SELECTED DRIVER NAME
```

## Result

Now when creating a transaction with a multi-driver vehicle:
1. User selects vehicle (e.g., "MD1 - RESCUE VEHICLE RED")
2. Driver dropdown auto-populates with all assigned drivers
3. User selects a specific driver (e.g., "ARNEL RUPENTA")
4. Transaction is created with that driver
5. Gas slip is printed showing that SELECTED driver (not the primary driver)

## Backward Compatibility

✅ Single-driver vehicles work as before
✅ Multiple-driver vehicles now show the correct selected driver
✅ Existing transactions unaffected
✅ Gas slip PDF display logic unchanged (still uses `driverFullName` as primary display with fallback to `driverName`)

## Testing Notes

- Create transaction with multi-driver vehicle
- Select any driver (not just the first/primary)
- Print gas slip
- Verify printed driver name matches selected driver
