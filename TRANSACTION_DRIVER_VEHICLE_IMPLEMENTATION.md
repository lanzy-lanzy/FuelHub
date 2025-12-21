# Transaction Creation with Dynamic Driver & Vehicle Selection

## Overview
Implemented transaction creation flow with dynamic driver and vehicle selection using dropdowns instead of manual text entry. This ensures data consistency and allows proper linking of transactions to drivers and vehicles.

## Changes Made

### 1. TransactionViewModel.kt
**Location**: `app/src/main/java/dev/ml/fuelhub/presentation/viewmodel/TransactionViewModel.kt`

#### Dependencies Added
- `UserRepository` - to load drivers
- `VehicleRepository` - to load vehicles
- `User` and `Vehicle` models imported

#### Constructor Updates
```kotlin
class TransactionViewModel(
    private val createFuelTransactionUseCase: CreateFuelTransactionUseCase,
    private val userRepository: UserRepository,
    private val vehicleRepository: VehicleRepository
) : ViewModel()
```

#### New State Flows
```kotlin
private val _drivers = MutableStateFlow<List<User>>(emptyList())
val drivers: StateFlow<List<User>> = _drivers

private val _vehicles = MutableStateFlow<List<Vehicle>>(emptyList())
val vehicles: StateFlow<List<Vehicle>> = _vehicles
```

#### New Functions

**loadDrivers()**
- Fetches all active users from `UserRepository`
- Called in `init` block on ViewModel creation
- Updates `_drivers` StateFlow
- Logs count of drivers loaded

**loadVehicles()**
- Fetches all active vehicles from `VehicleRepository`
- Called in `init` block on ViewModel creation
- Updates `_vehicles` StateFlow
- Logs count of vehicles loaded

### 2. TransactionScreen.kt
**Location**: `app/src/main/java/dev/ml/fuelhub/presentation/screen/TransactionScreen.kt`

#### Function Signature Updated
```kotlin
@Composable
fun TransactionScreen(
    transactionViewModel: TransactionViewModel? = null,
    onTransactionCreated: (Map<String, String>) -> Unit = {},
    modifier: Modifier = Modifier
)
```

#### Imports Added
```kotlin
import dev.ml.fuelhub.data.model.User
import dev.ml.fuelhub.data.model.Vehicle
```

#### State Variables Changed

**Old Approach**:
- `driverName: String` - Text field
- `vehicleType: String` - Text field

**New Approach**:
- `selectedDriver: User?` - Object holding full driver info
- `driverDropdownExpanded: Boolean` - Dropdown state
- `selectedVehicle: Vehicle?` - Object holding full vehicle info
- `vehicleDropdownExpanded: Boolean` - Dropdown state

#### Data Collection from ViewModel
```kotlin
val drivers = transactionViewModel?.drivers?.collectAsState()?.value ?: emptyList()
val vehicles = transactionViewModel?.vehicles?.collectAsState()?.value ?: emptyList()
```

#### UI Components

**Driver Selection Dropdown**
- Button shows selected driver's username
- Placeholder: "Select Driver"
- DropdownMenu populated with all available drivers from `drivers` list
- Displays driver's username when selected

**Vehicle Selection Dropdown**
- Button shows selected vehicle's plate number
- Placeholder: "Select Vehicle"
- DropdownMenu populated with all available vehicles from `vehicles` list
- Displays both plate number and vehicle type (e.g., "ABC-123 - Truck")

#### Form Submission Logic

**Old Validation**:
```kotlin
enabled = driverName.isNotBlank() && vehicleType.isNotBlank() && ...
```

**New Validation**:
```kotlin
enabled = selectedDriver != null && selectedVehicle != null && ...
```

**Data Passed to Callback**:
```kotlin
val transactionData = mapOf(
    "driverId" to selectedDriver!!.id,
    "driverName" to selectedDriver!!.username,
    "vehicleId" to selectedVehicle!!.id,
    "vehicleType" to selectedVehicle!!.vehicleType,
    "plateNumber" to selectedVehicle!!.plateNumber,
    "destination" to destination,
    "tripPurpose" to tripPurpose,
    "passengers" to passengers,
    "litersToPump" to litersToPump,
    "fuelType" to selectedFuelType.name
)
```

#### Clear Button Updated
```kotlin
onClick = {
    selectedDriver = null
    selectedVehicle = null
    destination = ""
    tripPurpose = ""
    passengers = ""
    litersToPump = ""
}
```

## Data Flow

### Initialization
1. TransactionScreen receives TransactionViewModel
2. ViewModel's `init` block calls `loadDrivers()` and `loadVehicles()`
3. Drivers and vehicles loaded from Firebase Firestore via repositories
4. Data stored in StateFlows `_drivers` and `_vehicles`

### User Interaction
1. User opens TransactionScreen
2. Dropdowns display available drivers and vehicles
3. User selects driver from dropdown → `selectedDriver` updated
4. User selects vehicle from dropdown → `selectedVehicle` updated
5. User enters destination, trip purpose, liters to pump
6. User clicks "Create Transaction"
7. Validation checks that `selectedDriver != null` and `selectedVehicle != null`
8. Transaction data is compiled with both IDs and names
9. `onTransactionCreated` callback invoked with complete data

## Benefits

✅ **Data Integrity**: Users cannot enter invalid driver/vehicle names
✅ **Relationship Tracking**: Transaction linked to actual driver and vehicle IDs
✅ **Better UX**: Dropdown is easier than free text entry
✅ **Dynamic Lists**: Drivers/vehicles loaded from database, always current
✅ **Complete Data**: Callback includes both IDs and display names
✅ **Consistent Naming**: Driver username from User object, not typed text
✅ **Vehicle Details**: Full vehicle info accessible (type, plate, fuel type, etc.)

## Integration with Transaction Creation

When a transaction is created using this screen:

1. **driverId** - Links transaction to specific driver record
2. **vehicleId** - Links transaction to specific vehicle record
3. **driverName** - Display name (from User.username)
4. **vehicleType** - Display name (from Vehicle.vehicleType)
5. **plateNumber** - Vehicle plate for reference
6. **litersToPump** - Fuel amount
7. **destination** - Trip destination
8. **tripPurpose** - Business purpose
9. **passengers** - Optional passenger info
10. **fuelType** - GASOLINE or DIESEL

## Firebase Integration

- `UserRepository.getAllActiveUsers()` fetches active drivers from Firestore
- `VehicleRepository.getAllActiveVehicles()` fetches active vehicles from Firestore
- Transaction objects save with both `vehicleId` and `driverId` for proper relationships
- Can look up driver/vehicle details by ID from Firestore when displaying transaction history

## Testing Checklist

- [ ] Ensure users are created in system (for driver dropdown)
- [ ] Ensure vehicles are created in system (for vehicle dropdown)
- [ ] Open Transaction screen - both dropdowns should show options
- [ ] Select a driver from dropdown
- [ ] Select a vehicle from dropdown
- [ ] Verify liters, destination, purpose filled in
- [ ] Click "Create Transaction" button
- [ ] Verify transaction created with correct driver/vehicle IDs
- [ ] Check Firestore that transaction has vehicleId and driverId fields
- [ ] Test Clear button resets selections to null
- [ ] Verify button is disabled until driver and vehicle selected
