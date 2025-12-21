# Gas Slip Full Name Update

## Summary
Updated the gas slip print feature to display the driver's full name instead of username/driver name.

## Changes Made

### 1. GasSlip Model (`data/model/GasSlip.kt`)
- Added new optional field: `driverFullName: String? = null`
- This field stores the driver's full name fetched from the User model
- Falls back gracefully if the field is not available (backward compatible)

### 2. CreateFuelTransactionUseCase (`domain/usecase/CreateFuelTransactionUseCase.kt`)
- Updated gas slip creation logic to fetch and include `driverFullName`
- When creating a GasSlip, the use case now captures the full name from the User object if available
- Passes `driverFullName` to the GasSlip constructor

### 3. GasSlipPdfGenerator (`data/util/GasSlipPdfGenerator.kt`)
- Updated PDF generation to use `driverFullName` when available
- Displays full name in uppercase on the slip
- Falls back to `driverName` if `driverFullName` is null (backward compatibility)
- Change on "Driver:" row in the details grid

### 4. FirebaseDataSource (`data/firebase/FirebaseDataSource.kt`)
- Updated `toFirestoreMap()` function to include `driverFullName` when saving to Firestore
- Updated `toGasSlip()` mapping function to read `driverFullName` from Firestore documents
- Ensures full name is persisted and retrieved from the database

## Data Flow

1. When a transaction is created, the `CreateFuelTransactionUseCase` fetches the user by ID
2. The user's `fullName` is extracted and included in the GasSlip
3. The GasSlip (with full name) is saved to Firestore via `FirebaseDataSource`
4. When printing, `GasSlipPdfGenerator` reads the `driverFullName` and displays it on the PDF
5. If full name is not available, the system gracefully falls back to the driver name

## Backward Compatibility

- The `driverFullName` field is optional (nullable with default `null`)
- Existing gas slips without this field will continue to work
- The PDF generator has fallback logic to use `driverName` if `driverFullName` is not available
- No breaking changes to the existing data structure or APIs

## Build Status

✅ Clean build successful - no compilation errors
