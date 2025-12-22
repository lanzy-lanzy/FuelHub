# Cancel Slip Dialog Implementation

## Overview
Added functional cancel confirmation dialogs to both GasSlipScreen and GasSlipDetailScreen. When users tap the Cancel button, a confirmation dialog now appears before the slip is cancelled.

## Features Implemented

### 1. GasSlipScreen Cancel Dialog
**File**: `app/src/main/java/dev/ml/fuelhub/presentation/screen/GasSlipScreen.kt`

- Added state management: `var showCancelDialog by remember { mutableStateOf(false) }`
- Cancel button now triggers dialog instead of direct action
- Dialog includes:
  - **Header**: Red warning icon + "Cancel Slip?" title
  - **Message**: Confirmation text, reference number, and warning
  - **Actions**:
    - Red "Cancel Slip" button (confirms cancellation)
    - "Keep Slip" button (dismisses dialog)

**Dialog Content**:
```
Title: ⚠️ Cancel Slip?
Message:
  "Are you sure you want to cancel this fuel slip?
   Reference: FS-XXXXXXXX-XXXX
   This action cannot be undone. The slip will be marked as 
   cancelled and cannot be dispensed."
```

### 2. GasSlipDetailScreen Cancel Dialog
**File**: `app/src/main/java/dev/ml/fuelhub/presentation/screen/GasSlipDetailScreen.kt`

- Added state management: `var showCancelDialog by remember { mutableStateOf(false) }`
- Styled to match detail screen theme (dark mode)
- Cancel button appears alongside "Mark Dispensed" button
- Same dialog with context-aware messaging
- Better integration with receipt-style detail layout

**Dialog Content** (same as GasSlipScreen):
```
Title: ⚠️ Cancel Slip?
Message:
  "Are you sure you want to cancel this fuel slip?
   Reference: FS-XXXXXXXX-XXXX
   This action cannot be undone. The slip will be marked as 
   cancelled and cannot be dispensed."
```

## User Flow

### Cancel Slip Process:
1. User taps red "Cancel" button (only visible for PENDING slips)
2. Confirmation dialog appears with:
   - Warning icon and title
   - Reference number for confirmation
   - Clear warning about irreversibility
3. User chooses:
   - **"Cancel Slip"** → Slip is cancelled, Firebase updated, dialog closes
   - **"Keep Slip"** → Dialog closes, slip remains unchanged

### Dialog Behavior:
- Clicking outside dialog dismisses it
- Both buttons properly styled and accessible
- Reference number clearly displayed for user verification
- Prevents accidental cancellations

## Technical Implementation

### State Management
```kotlin
var showCancelDialog by remember { mutableStateOf(false) }
```
- Local state within composable
- Toggles on button click
- Resets when dialog action completes

### Dialog Components
```kotlin
AlertDialog(
    onDismissRequest = { showCancelDialog = false },
    title = { /* Warning icon + title */ },
    text = { /* Confirmation details */ },
    confirmButton = { /* Cancel Slip button */ },
    dismissButton = { /* Keep Slip button */ },
    containerColor = SurfaceDark,
    textContentColor = TextPrimary
)
```

### Action Flow
1. User taps Cancel button → `showCancelDialog = true`
2. Dialog appears with options
3. User confirms → `onCancelClick(gasSlip.id)` called
4. ViewModel's `cancelGasSlip()` updates Firebase
5. Dialog closes → `showCancelDialog = false`

## Styling

### Colors
- **Title/Icon**: Red (0xFFf44336 or 0xFFFF6B6B)
- **Confirm Button**: Red background, white text
- **Dismiss Button**: Secondary text color
- **Container**: Dark theme (SurfaceDark)

### Icons
- Warning icon: `Icons.Default.WarningAmber` (28dp)
- Color: Red to indicate destructive action

### Typography
- **Title**: Bold, 16sp
- **Message**: Body medium/small
- **Reference**: Bold, highlighted for verification

## Integration Points

### Called From
- User taps red "Cancel" button in list view or detail view
- Button click sets `showCancelDialog = true`
- Dialog manages confirmation flow
- On confirm, calls `onCancelClick()` callback to ViewModel

### Callbacks
- `onCancelClick(gasSlipId: String)` - passed from parent
- ViewModel's `cancelGasSlip()` handles Firebase update
- Repository updates slip status to CANCELLED

## Testing Checklist
- [ ] Cancel button appears for PENDING slips only
- [ ] Tapping Cancel button shows dialog
- [ ] Dialog displays correct reference number
- [ ] "Cancel Slip" button confirms and closes dialog
- [ ] "Keep Slip" button closes without action
- [ ] Slip is actually cancelled in Firebase (status changed)
- [ ] Cancelled slip cannot be re-cancelled
- [ ] Dialog theme matches screen (dark/light mode)
- [ ] Text is readable and clear
- [ ] Multiple cancellations work correctly

## Files Modified
1. `GasSlipScreen.kt` - Added cancel dialog
2. `GasSlipDetailScreen.kt` - Added cancel dialog
3. No changes to ViewModels or Repositories (already functional)

## Future Enhancements
- Add toast/snackbar notification after successful cancellation
- Add reason/comment field for why slip was cancelled
- Add audit log tracking of who cancelled which slips
- Add ability to uncanccel slips (with audit trail)
