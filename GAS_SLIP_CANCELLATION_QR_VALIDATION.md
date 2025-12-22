# Gas Slip Cancellation & QR Code Validation

## Overview
Implemented two features for gas slip management:
1. **Cancellation UI**: Users can cancel pending slips in detail screen
2. **QR Code Validation**: When scanning cancelled slips at gas station, show warning dialog

## Features Implemented

### 1. Cancellation in Detail Screen
- Added **Cancel button** in GasSlipDetailScreen for pending slips
- Cancel button appears alongside "Mark Dispensed" button
- Button styled in red (Color 0xFFFF6B6B) for visibility
- Only visible when slip status is PENDING
- Calls `onCancel(gasSlipId)` callback

**Location**: `GasSlipDetailScreen.kt`
- Mark Dispensed and Cancel buttons appear side-by-side for pending slips
- Print button becomes disabled (grayed out) when slip is CANCELLED
- Clean, readable button layout with proper spacing

### 2. QR Code Cancelled Slip Detection
When a cancelled slip is scanned at the gas station:
1. QR code is parsed and reference number is extracted
2. Transaction is looked up in Firebase
3. **New**: Gas slip status is checked via `checkGasSlipStatus()`
4. If status is `CANCELLED`:
   - Red dialog appears: "Slip Cancelled"
   - Message: "This slip (REFERENCE#) has been cancelled and cannot be dispensed."
   - Additional context: "Please contact your administrator if this was done in error"
   - User must click "Understood" to dismiss
5. If status is PENDING or DISPENSED:
   - Standard confirmation dialog appears for dispensing

**Location**: `GasStationScreen.kt`
- New state variables: `showCancelledDialog`, `cancelledSlipMessage`
- New composable: `CancelledSlipDialog()`
- Integrated into QR scan flow at line 111-125

## Modified Files

### 1. **GasSlipDetailScreen.kt**
- Added `onCancel` callback parameter
- Added cancel button for pending slips
- Cancel button positioned next to "Mark Dispensed" button
- Updated print button to be disabled when slip is CANCELLED
- Button colors: Red (Cancel), Green (Mark Dispensed), Cyan (Print)

### 2. **GasStationScreen.kt**
- Added `showCancelledDialog` state
- Added `cancelledSlipMessage` state
- Added `checkGasSlipStatus()` call in QR scan handler (line 114-125)
- Added `CancelledSlipDialog` composable (red, blocked icon design)
- Integrated cancelled slip dialog into UI flow

### 3. **TransactionViewModel.kt**
- Added `checkGasSlipStatus(transactionId, callback)` method
- Queries Firestore for gas slip by transaction ID
- Calls callback with status string: "CANCELLED", "PENDING", "DISPENSED", "USED", "NOT_FOUND", or "ERROR"
- Handles async/coroutine operations properly

## User Workflows

### Cancelling a Slip (Detail Screen)
1. User opens gas slip detail screen
2. For pending slips, sees "Mark Dispensed" and "Cancel" buttons
3. Clicks red "Cancel" button
4. Slip status changes to CANCELLED in Firebase
5. Print button becomes disabled (grayed out)

### Scanning Cancelled Slip (Gas Station)
1. Gas station operator scans QR code
2. System checks gas slip status in Firebase
3. If CANCELLED:
   - Red dialog appears: "Slip Cancelled"
   - Shows reference number and cancellation message
   - User cannot proceed with dispensing
4. If PENDING:
   - Normal confirmation dialog appears
   - User can confirm dispensing

## Technical Details

### Gas Slip Status Enum
```
PENDING    → Can be dispensed or cancelled
DISPENSED  → Fuel has been given out
USED       → Legacy status (kept for compatibility)
CANCELLED  → Cannot be dispensed
```

### Dialog Design
**CancelledSlipDialog**:
- Title: "Slip Cancelled" with red block icon (Size 28dp)
- Message: Shows reference number and reason
- Divider for visual separation
- Additional context text in italics
- Red "Understood" button to dismiss
- Dark theme color scheme

### Flow Diagram
```
QR Code Scan
    ↓
Parse & Extract Reference
    ↓
Find Transaction in Firebase
    ↓
Check Gas Slip Status
    ├→ CANCELLED → Show CancelledSlipDialog ✗
    │
    └→ PENDING/DISPENSED → Show ConfirmDispensedDialog ✓
```

## Error Handling
- If gas slip not found: `callback("NOT_FOUND")` - uses standard error flow
- If check fails: `callback("ERROR")` - logged and handled gracefully
- Cancelled check happens BEFORE confirmation dialog
- All operations logged for audit trail

## Visual Design

### Buttons (Detail Screen)
- **Mark Dispensed**: Green (SuccessGreen), 48dp height
- **Cancel**: Red (0xFFFF6B6B), 48dp height
- **Print**: Cyan (VibrantCyan), disabled state shows grayed text/icon

### Dialogs
- **Title Bar**: Red block icon (28dp) with "Slip Cancelled" text
- **Message**: Full reference number for identification
- **Support Text**: Italicized guidance for user action
- **Button**: Red "Understood" button to dismiss

## Testing Checklist
- [ ] Cancel button appears for PENDING slips only
- [ ] Cancel button works and updates Firebase
- [ ] Print button disables when slip is CANCELLED
- [ ] Scanning PENDING slip shows confirmation dialog
- [ ] Scanning CANCELLED slip shows cancellation dialog
- [ ] Scanning DISPENSED slip shows confirmation dialog
- [ ] Dialog messages are clear and helpful
