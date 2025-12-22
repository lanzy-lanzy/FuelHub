# Gas Station Logout NavController Crash - Fixed

## Problem
App crashed when gas station user clicked logout button with error:
```
java.lang.IllegalArgumentException: Cannot navigate to NavDeepLinkRequest{ uri=android-app://androidx.navigation/login }. 
Navigation graph has not been set for NavController
```

## Root Cause
In `MainActivity.kt`, the `NavHost` (which defines all navigation routes) was only being initialized inside an `if` block that checked `if (currentRoute != "gasstation")`. 

This meant:
1. When a gas station user logs in, their `currentRoute` is set to "gasstation"
2. The condition `if (currentRoute != "gasstation")` evaluates to `false`
3. The `NavHost` never gets created
4. When logout button is clicked, `navController.navigate("login")` fails because the navigation graph was never set

## Solution
Moved `FuelHubNavHost` initialization **outside** the conditional block so it's always created, regardless of the current route:

### Before (Broken)
```kotlin
var currentRoute by remember { mutableStateOf(startDestination) }

if (currentRoute != "gasstation") {
    // ... drawer and scaffold UI ...
    Scaffold {
        FuelHubNavHost(...)  // Only created for non-gas-station
    }
} else {
    GasStationScreen(onNavigateBack = {
        navController.navigate("login")  // CRASH: NavHost never created!
    })
}
```

### After (Fixed)
```kotlin
var currentRoute by remember { mutableStateOf(startDestination) }

// Initialize NavHost FIRST (always executed)
FuelHubNavHost(
    navController = navController,
    startDestination = startDestination,
    modifier = Modifier.fillMaxSize(),
    currentRoute = currentRoute,
    onRouteChange = { newRoute -> currentRoute = newRoute },
    // ... other parameters ...
    showDrawerUI = currentRoute != "gasstation"
)

// Then conditionally show drawer UI only for non-gas-station
if (currentRoute != "gasstation") {
    ModalNavigationDrawer(...)
}
```

## Changes Made
1. **Moved FuelHubNavHost outside conditional** - Now initialized before any route-based UI decisions
2. **Added `showDrawerUI` parameter** - Allows NavHost to know whether drawer UI should be shown
3. **Simplified conditional block** - Only drawer/scaffold UI is conditional, navigation graph always exists

## Result
- Gas station logout now works correctly
- Navigation graph is available for all routes
- Gas station users can successfully logout and navigate to login screen
