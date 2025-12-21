# Driver & Vehicle Management Feature

## Overview
Added complete driver and vehicle management screens with CRUD operations, plus integrated dropdowns in the Transaction screen for easy selection.

## Files Created

### 1. ViewModels
- **DriverManagementViewModel.kt** - Manages driver CRUD operations
  - Load drivers from database
  - Add new drivers
  - Update existing drivers
  - Delete drivers (deactivate)
  - Selection management

- **VehicleManagementViewModel.kt** - Manages vehicle CRUD operations
  - Load vehicles from database
  - Add new vehicles
  - Update existing vehicles
  - Delete vehicles (deactivate)
  - Selection management

### 2. Screens
- **DriverManagementScreen.kt**
  - List all drivers with expandable cards
  - Add new driver dialog
  - Edit driver dialog
  - Delete confirmation dialog
  - Features:
    - Username, email, full name, office ID management
    - Role assignment (ADMIN, DISPATCHER, ENCODER, VIEWER)
    - Active/inactive status display

- **VehicleManagementScreen.kt**
  - List all vehicles with expandable cards
  - Add new vehicle dialog
  - Edit vehicle dialog
  - Delete confirmation dialog
  - Features:
    - Plate number, vehicle type, fuel type
    - Driver assignment
    - Active/inactive status display

### 3. Updated Screens
- **TransactionScreenEnhanced.kt**
  - Replaced text input for driver/vehicle with dropdown selector
  - Shows plate number + driver name in selection button
  - Shows vehicle details (type, fuel type) in dropdown items
  - Better form validation using selected vehicle object
  - Cleaner form with just: Vehicle, Liters, Destination, Purpose, Passengers

## Features

### Driver Management
```
Add Driver:
- Username (unique identifier)
- Email
- Full Name
- Office ID
- Role (ADMIN, DISPATCHER, ENCODER, VIEWER)

Edit Driver:
- Update full name
- Update email
- Keep username read-only

Delete Driver:
- Soft delete (deactivate)
- Confirmation dialog
```

### Vehicle Management
```
Add Vehicle:
- Plate Number
- Vehicle Type (Truck, Van, Sedan, etc.)
- Driver Name
- Fuel Type (GASOLINE, DIESEL)

Edit Vehicle:
- Update vehicle type
- Update driver assignment
- Update fuel type
- Plate number read-only

Delete Vehicle:
- Soft delete (deactivate)
- Confirmation dialog
```

### Transaction Screen Updates
```
Before:
- Text input for driver name
- Text input for vehicle ID
- No validation against actual vehicles
- Free-form entry error-prone

After:
- Dropdown selector for vehicles
- Shows: Plate Number - Driver Name
- Dropdown details: Vehicle Type, Fuel Type
- Validation against selected vehicle
- Automatic driver assignment from vehicle
```

## Navigation Integration

Add to MainActivity:
```kotlin
val driverManagementViewModel = DriverManagementViewModel(userRepository)
val vehicleManagementViewModel = VehicleManagementViewModel(vehicleRepository)

// In NavHost:
composable("drivers") {
    DriverManagementScreen(driverManagementViewModel)
}

composable("vehicles") {
    VehicleManagementScreen(vehicleManagementViewModel)
}

composable("transaction") {
    val vehicles = vehicleManagementViewModel.uiState.collectAsState()
    TransactionScreenEnhanced(
        transactionViewModel = transactionViewModel,
        availableVehicles = (vehicles.value as? VehicleManagementUiState.Success)?.vehicles ?: emptyList()
    )
}
```

Add navigation items to NavigationBar to access:
- Home
- Transaction
- Wallet  
- Drivers (new)
- Vehicles (new)
- Reports

## UI Components

### Driver Card (Expandable)
```
┌─────────────────────────────────┐
│ John Doe              ▼          │
│ @johndoe                        │
├─────────────────────────────────┤
│ Email: john@email.com           │
│ Role: DISPATCHER                │
│ Office: mdrrmo-office-1         │
│ Status: Active                  │
│                                 │
│ [Edit Button] [Delete Button]   │
└─────────────────────────────────┘
```

### Vehicle Card (Expandable)
```
┌─────────────────────────────────┐
│ ABC-123              ▼          │
│ Truck                           │
├─────────────────────────────────┤
│ Driver: John Doe                │
│ Fuel Type: GASOLINE             │
│ Type: Truck                     │
│ Status: Active                  │
│                                 │
│ [Edit Button] [Delete Button]   │
└─────────────────────────────────┘
```

### Transaction Vehicle Dropdown
```
┌──────────────────────────────────┐
│ ABC-123 - John Doe         ▼     │
├──────────────────────────────────┤
│ ✓ ABC-123 - John Doe             │
│   Truck (GASOLINE)               │
│                                  │
│   DEF-456 - Jane Smith           │
│   Van (DIESEL)                   │
│                                  │
│   GHI-789 - Bob Johnson          │
│   Sedan (GASOLINE)               │
└──────────────────────────────────┘
```

## State Management

### DriverManagementUiState
- Idle
- Loading
- Success(drivers: List<User>)
- Error(message: String)
- DriverSaved
- DriverDeleted

### VehicleManagementUiState
- Idle
- Loading
- Success(vehicles: List<Vehicle>)
- Error(message: String)
- VehicleSaved
- VehicleDeleted

## Database Integration
- Uses existing UserRepository for drivers
- Uses existing VehicleRepository for vehicles
- All operations are suspend functions (coroutines)
- Error handling with Timber logging
- Idempotent operations where applicable

## Validation
- Driver: username, email, full name, office ID required
- Vehicle: plate number, vehicle type, driver name required
- Dropdowns prevent invalid vehicle selection in transactions
- Form cannot submit without vehicle selection

## Next Steps
1. Update MainActivity to instantiate ViewModels
2. Add navigation routes for driver/vehicle screens
3. Add navigation buttons to home screen or nav bar
4. Test CRUD operations
5. Test transaction creation with vehicle dropdown
