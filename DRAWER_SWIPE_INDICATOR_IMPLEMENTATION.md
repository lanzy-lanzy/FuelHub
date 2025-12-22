# Drawer Swipe Indicator Implementation

## Overview
Added **floating animated drawer swipe indicators** to all main screens to help users discover the left-side navigation drawer functionality. The indicators are positioned on the left side of the screen, vertically centered, displaying dual animated arrows with pulsing text that prompts users to swipe to open the menu.

## Component Created
**File**: `app/src/main/java/dev/ml/fuelhub/presentation/component/DrawerSwipeIndicator.kt`

### Features:
- ✅ **Dual animated arrows** that slide sequentially with staggered timing
- ✅ Positioned **left-center** of screen as a floating overlay
- ✅ **Pulsing alpha animation** for smooth attention-grabbing effect
- ✅ First arrow is fully opaque, second arrow at 60% opacity for depth
- ✅ Customizable tint color (defaults to white with 70% opacity)
- ✅ Text label: "Swipe to open"
- ✅ Can be toggled visible/invisible via parameter
- ✅ Larger icon size (32.dp) for better visibility
- ✅ Fills entire screen, allowing overlay positioning

### Component Signature:
```kotlin
@Composable
fun DrawerSwipeIndicator(
    modifier: Modifier = Modifier,
    isVisible: Boolean = true,
    tintColor: Color = Color.White.copy(alpha = 0.7f)
)
```

## Screens Updated

### 1. HomeScreen ✅
- **File**: `app/src/main/java/dev/ml/fuelhub/presentation/screen/HomeScreen.kt`
- **Layout**: Wrapped LazyColumn in Box for floating overlay
- **Position**: Left-center, floating above content
- **Color**: `VibrantCyan.copy(alpha = 0.7f)`

### 2. WalletScreenEnhanced ✅
- **File**: `app/src/main/java/dev/ml/fuelhub/presentation/screen/WalletScreenEnhanced.kt`
- **Layout**: Wrapped LazyColumn in Box for floating overlay
- **Position**: Left-center, floating above content
- **Color**: `VibrantCyan.copy(alpha = 0.7f)`

### 3. TransactionScreenEnhanced ✅
- **File**: `app/src/main/java/dev/ml/fuelhub/presentation/screen/TransactionScreenEnhanced.kt`
- **Layout**: Wrapped Column in Box for floating overlay
- **Position**: Left-center, floating above form
- **Color**: `VibrantCyan.copy(alpha = 0.7f)`

### 4. ReportScreenEnhanced ✅
- **File**: `app/src/main/java/dev/ml/fuelhub/presentation/screen/ReportScreenEnhanced.kt`
- **Layout**: Floating inside existing Box structure
- **Position**: Left-center, above pull-to-refresh indicator
- **Color**: `VibrantCyan.copy(alpha = 0.7f)`

### 5. DriverManagementScreen ✅
- **File**: `app/src/main/java/dev/ml/fuelhub/presentation/screen/DriverManagementScreen.kt`
- **Layout**: Wrapped Column in Box for floating overlay
- **Position**: Left-center, floating above content
- **Color**: `VibrantCyan.copy(alpha = 0.7f)`

### 6. VehicleManagementScreen ✅
- **File**: `app/src/main/java/dev/ml/fuelhub/presentation/screen/VehicleManagementScreen.kt`
- **Layout**: Wrapped Column in Box for floating overlay
- **Position**: Left-center, floating above content
- **Color**: `VibrantCyan.copy(alpha = 0.7f)`

### 7. GasSlipListScreen ✅
- **File**: `app/src/main/java/dev/ml/fuelhub/presentation/screen/GasSlipListScreen.kt`
- **Layout**: Wrapped Column in Box for floating overlay
- **Position**: Left-center, floating above content
- **Color**: `VibrantCyan.copy(alpha = 0.7f)`

### 8. SettingsScreen ✅
- **File**: `app/src/main/java/dev/ml/fuelhub/presentation/screen/SettingsScreen.kt`
- **Layout**: Wrapped Column in Box for floating overlay
- **Position**: Left-center, floating above content
- **Color**: `VibrantCyan.copy(alpha = 0.7f)`

## Design Details

### Animations:
- **Arrow 1 Slide**: Animates 0-12dp over 1200ms, main leading arrow
- **Arrow 2 Slide**: Animates -8 to 4dp over 1200ms, offset by 150ms delay
- **Alpha Pulse**: 1500ms animation ranging 30-100% opacity for pulsing effect
- **Easing**: `EaseInOutQuad` for smooth, natural motion
- Sequential staggered timing creates visual flow toward the drawer

### Visual Hierarchy:
- **First Arrow**: 32.dp icon at full tint color
- **Second Arrow**: 32.dp icon at 60% of tint color (depth effect)
- **Text**: 13.sp "Swipe to open" at 80% of tint opacity
- **Overall Opacity**: 30-100% pulsing for attention
- **Positioning**: Left-center fixed overlay via Box contentAlignment

### Styling:
- Uses consistent `VibrantCyan` color across all screens
- Matches the app's premium theme color palette
- Positioned as floating overlay, doesn't interfere with content
- Eye-catching dual arrows guide users to swipe left
- 16.dp left padding prevents edge sticking

## User Experience Benefits

1. **Discovery**: Hints to users that a navigation drawer exists
2. **Onboarding**: Helps new users find menu options quickly
3. **Subtle Animation**: Draws attention without being obnoxious
4. **Consistent**: Present on all major screens (except login/register)
5. **Non-intrusive**: Small, positioned at top, doesn't block content

## Testing Checklist
- [ ] Verify indicator appears on all 8 screens
- [ ] Confirm animations are smooth and not jittery
- [ ] Test that drawer opens when swiping left to right
- [ ] Verify indicator doesn't break screen layouts
- [ ] Check indicator color matches app theme
- [ ] Test on different screen sizes/orientations
- [ ] Verify performance (no FPS drops)

## Future Enhancements
- Could add a "dismiss" button to hide indicator permanently
- Could save preference to hide after first 3 swipes
- Could add haptic feedback when swiping
- Could customize animation speed per screen
