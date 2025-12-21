# Integration Steps for Driver & Vehicle Management

## Step 1: Update MainActivity.kt

Add imports at the top:
```kotlin
import dev.ml.fuelhub.presentation.viewmodel.DriverManagementViewModel
import dev.ml.fuelhub.presentation.viewmodel.VehicleManagementViewModel
import dev.ml.fuelhub.presentation.screen.DriverManagementScreen
import dev.ml.fuelhub.presentation.screen.VehicleManagementScreen
import dev.ml.fuelhub.presentation.viewmodel.VehicleManagementUiState
```

In `onCreate()`, after initializing other ViewModels (around line 85):
```kotlin
// Initialize Management ViewModels
val driverManagementViewModel = DriverManagementViewModel(userRepository)
val vehicleManagementViewModel = VehicleManagementViewModel(vehicleRepository)
```

In `FuelHubApp()` composable, update the function signature to accept new parameters:
```kotlin
@Composable
fun FuelHubApp(
    transactionViewModel: TransactionViewModel,
    walletViewModel: WalletViewModel,
    driverManagementViewModel: DriverManagementViewModel,  // ADD THIS
    vehicleManagementViewModel: VehicleManagementViewModel, // ADD THIS
    generateDailyReportUseCase: GenerateDailyReportUseCase,
    generateWeeklyReportUseCase: GenerateWeeklyReportUseCase,
    generateMonthlyReportUseCase: GenerateMonthlyReportUseCase
)
```

Update the function call in setContent:
```kotlin
FuelHubApp(
    transactionViewModel = transactionViewModel,
    walletViewModel = walletViewModel,
    driverManagementViewModel = driverManagementViewModel,    // ADD THIS
    vehicleManagementViewModel = vehicleManagementViewModel,  // ADD THIS
    generateDailyReportUseCase = generateDailyReportUseCase,
    generateWeeklyReportUseCase = generateWeeklyReportUseCase,
    generateMonthlyReportUseCase = generateMonthlyReportUseCase
)
```

## Step 2: Add Navigation Routes

In `FuelHubApp()`, update the NavHost to include new routes (before the closing NavHost brace):

```kotlin
composable("drivers") {
    DriverManagementScreen(driverManagementViewModel)
}

composable("vehicles") {
    VehicleManagementScreen(vehicleManagementViewModel)
}
```

Update the "transaction" route to pass available vehicles:
```kotlin
composable("transaction") {
    val vehicleState by vehicleManagementViewModel.uiState.collectAsState()
    val availableVehicles = when (vehicleState) {
        is VehicleManagementUiState.Success -> (vehicleState as VehicleManagementUiState.Success).vehicles
        else -> emptyList()
    }
    
    TransactionScreenEnhanced(
        transactionViewModel = transactionViewModel,
        availableVehicles = availableVehicles,  // ADD THIS PARAMETER
        onTransactionCreated = {
            Timber.d("Transaction created successfully")
            selectedTab = 0
            navController.navigate("home") {
                popUpTo("home") { inclusive = true }
            }
        }
    )
}
```

## Step 3: Add Navigation Bar Items (Optional)

To add Drivers and Vehicles to the bottom navigation bar, update the NavigationBar to have more items. 

Currently there are 4 items (Home, Transaction, Wallet, Reports). You can:

**Option A: Replace Reports with a menu**
- Keep: Home, Transaction, Wallet, Drivers, Vehicles

**Option B: Add Management menu**
- Add a Settings/Admin icon that opens a submenu

Here's an example for Option A (add after the Wallet item):

```kotlin
NavigationBarItem(
    icon = { Icon(Icons.Default.Person, "Drivers") },
    label = { Text("Drivers") },
    selected = selectedTab == 3,
    onClick = {
        selectedTab = 3
        navController.navigate("drivers") {
            popUpTo("drivers") { inclusive = true }
        }
    }
)
NavigationBarItem(
    icon = { Icon(Icons.Default.DirectionsCar, "Vehicles") },
    label = { Text("Vehicles") },
    selected = selectedTab == 4,
    onClick = {
        selectedTab = 4
        navController.navigate("vehicles") {
            popUpTo("vehicles") { inclusive = true }
        }
    }
)
NavigationBarItem(
    icon = { Icon(Icons.Default.Info, "Reports") },
    label = { Text("Reports") },
    selected = selectedTab == 5,
    onClick = {
        selectedTab = 5
        navController.navigate("reports") {
            popUpTo("reports") { inclusive = true }
        }
    }
)
```

Also update the initial selectedTab state:
```kotlin
var selectedTab by remember { mutableIntStateOf(0) }
```

## Step 4: Test

1. Run the app
2. Create a new driver:
   - Tap "Drivers" tab
   - Tap "Add Driver" button
   - Fill in: Full Name, Username, Email, Office ID
   - Submit

3. Create a new vehicle:
   - Tap "Vehicles" tab
   - Tap "Add Vehicle" button
   - Fill in: Plate Number, Vehicle Type, Driver Name, Fuel Type
   - Submit

4. Create a transaction:
   - Tap "Transaction" tab
   - Tap the vehicle dropdown
   - Select a vehicle (shows: PLATE - DRIVER NAME)
   - Fill in remaining fields
   - Submit

## Expected Flow

```
Home Screen → Create Transaction
↓
Select Vehicle Dropdown → Shows all created vehicles
↓
Fill Details (Liters, Destination, Purpose)
↓
Submit → Transaction created with selected vehicle data
↓
Driver name auto-filled from vehicle.driverName
Vehicle ID auto-filled from vehicle.id
```

## Troubleshooting

**Issue: "No vehicles available" in dropdown**
- Solution: Create vehicles first in the Vehicles screen

**Issue: Dropdown not showing**
- Check: availableVehicles list is passed correctly
- Check: vehicleManagementViewModel.uiState is Success state

**Issue: Driver/Vehicle not saved**
- Check: All required fields are filled
- Check: User has write permissions to database
- Check: Timber logs for error messages

## Files Modified
- MainActivity.kt (add ViewModels, routes, navigation items)
- TransactionScreenEnhanced.kt (already updated with dropdown)

## Files Created
- DriverManagementScreen.kt
- DriverManagementViewModel.kt
- VehicleManagementScreen.kt
- VehicleManagementViewModel.kt
