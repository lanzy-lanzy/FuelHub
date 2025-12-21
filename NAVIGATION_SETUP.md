# FuelHub Navigation Setup

## Overview
The app now uses a 4-tab bottom navigation system with HomeScreen as the primary entry point.

## Navigation Structure

### Bottom Navigation Tabs (in order)
1. **Home** (Icon: Home) - Index 0
   - Route: `"home"`
   - Screen: `HomeScreen`
   - Start Destination: YES
   
2. **Transaction** (Icon: Receipt) - Index 1
   - Route: `"transaction"`
   - Screen: `TransactionScreen`
   
3. **Wallet** (Icon: Settings) - Index 2
   - Route: `"wallet"`
   - Screen: `WalletScreen`
   
4. **Reports** (Icon: Info) - Index 3
   - Route: `"reports"`
   - Screen: `ReportScreen`

### Additional Routes (No Tab)
- **Gas Slip**: Route `"gasSlip"` - Accessible from other screens

## Navigation Flow

```
HomeScreen (Start)
├─→ New Transaction → TransactionScreen
├─→ Refill Wallet → WalletScreen
├─→ View Reports → ReportScreen
└─→ History → (Future: TransactionHistoryScreen)

Bottom Navigation allows quick switching between all 4 main tabs
```

## HomeScreen Integration

### Callback Functions
The HomeScreen accepts 4 navigation callbacks:
```kotlin
HomeScreen(
    onNavigateToTransactions = { /* navigate to transaction tab */ },
    onNavigateToWallet = { /* navigate to wallet tab */ },
    onNavigateToReports = { /* navigate to reports tab */ },
    onNavigateToHistory = { /* navigate to history screen */ }
)
```

### Current Implementation (MainActivity.kt)
```kotlin
HomeScreen(
    onNavigateToTransactions = {
        selectedTab = 1
        navController.navigate("transaction")
    },
    onNavigateToWallet = {
        selectedTab = 2
        navController.navigate("wallet")
    },
    onNavigateToReports = {
        selectedTab = 3
        navController.navigate("reports")
    },
    onNavigateToHistory = {
        Timber.d("History navigation clicked")
    }
)
```

## Tab Selection Logic
- When a navigation item is clicked, `selectedTab` state updates
- This highlights the current tab in the bottom navigation
- The NavHost routes to the appropriate composable
- `popUpTo()` ensures proper back stack handling

## User Flow

1. **App Launches** → HomeScreen displayed with bottom navigation
2. **User Clicks Button in HomeScreen** → Navigates to specific tab
3. **Bottom Navigation Highlights** → Shows current location
4. **User Clicks Bottom Tab** → Routes to that screen directly
5. **Back Navigation** → Properly handled by NavHost back stack

## Starting Destination
- **Previous**: `"transaction"` (TransactionScreen)
- **Current**: `"home"` (HomeScreen)
- **Reason**: HomeScreen provides comprehensive overview and central hub for navigation

## Adding New Screens

To add a new screen:

1. **Create Screen Composable**
   ```kotlin
   @Composable
   fun MyNewScreen() { ... }
   ```

2. **Add to NavHost in MainActivity**
   ```kotlin
   composable("myroute") {
       MyNewScreen(...)
   }
   ```

3. **For Bottom Tab**: Add NavigationBarItem
   ```kotlin
   NavigationBarItem(
       icon = { Icon(...) },
       label = { Text("Label") },
       selected = selectedTab == 4,
       onClick = {
           selectedTab = 4
           navController.navigate("myroute")
       }
   )
   ```

4. **For Bottom Tab**: Update selection logic indices

## Important Notes

- Bottom navigation is always visible (part of Scaffold)
- Content is displayed with padding from bottom bar
- Tab state is maintained with `mutableIntStateOf`
- Navigation handles pop-up to avoid back stack issues
- Each main screen has its own navigation callbacks for internal navigation

## File Structure

```
MainActivity.kt
├── FuelHubApp() - Main composable with navigation
│   ├── NavController setup
│   ├── Scaffold with bottom navigation
│   ├── NavHost
│   │   ├── HomeScreen route
│   │   ├── TransactionScreen route
│   │   ├── WalletScreen route
│   │   ├── ReportScreen route
│   │   └── GasSlipScreen route
```

## Testing Navigation

From HomeScreen, you should see:
- 4 navigation tabs at the bottom
- Home tab highlighted by default
- Quick action buttons that navigate to other tabs
- Clicking any button updates selected tab
- Bottom navigation switches between screens

---

**Last Updated**: Dec 20, 2025  
**Status**: ✅ Complete  
**Build**: Passing
