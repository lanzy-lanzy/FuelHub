# Gas Slip Cancellation & Bulk Actions Implementation

## Overview
Added two major features to the Gas Slip management screen:
1. **Cancellation**: Individual gas slips can now be cancelled
2. **Bulk Actions**: Mark multiple pending gas slips as dispensed at once

## Features Implemented

### 1. Cancellation Feature
- **Per-Slip Cancel Button**: Available on pending slips in both the detail and list screens
- **Status Update**: Cancelled slips get `CANCELLED` status
- **Visual Indicator**: Cancelled slips display with red status badge and disabled actions
- **Permissions**: Only pending slips can be cancelled

**UI Locations:**
- GasSlipScreen: Secondary action row (red "Cancel" button)
- GasSlipListScreen: Cancel button in expanded card (only for pending slips)

### 2. Bulk Mark as Dispensed
- **Selection Mode**: Toggle bulk action mode with the select-all button in toolbar
- **Multi-Select**: Click cards in bulk mode to select/deselect individual slips
- **Bulk Toolbar**: Shows selected count and action buttons when items are selected
- **Actions:**
  - **Select All**: Select all filtered slips
  - **Mark Dispensed**: Mark all selected slips as dispensed in one action
- **Status Updates**: Marks selected slips as `DISPENSED` and updates associated transactions to `COMPLETED`

**UI Locations:**
- GasSlipListScreen: 
  - Bulk action mode toggle button (select icon in toolbar)
  - Checkboxes appear on cards in bulk mode
  - Green toolbar shows when items selected with "Select All" and "Mark Dispensed" buttons
  - Header changes to show count of selected items

## Modified Files

### Core Files
1. **app/src/main/java/dev/ml/fuelhub/domain/repository/GasSlipRepository.kt**
   - Added `cancelGasSlip(gasSlipId: String): GasSlip?`
   - Added `bulkMarkPendingAsDispensed(gasSlipIds: List<String>): Boolean`

2. **app/src/main/java/dev/ml/fuelhub/presentation/viewmodel/GasSlipManagementViewModel.kt**
   - Added `BulkActionSuccess` state to `GasSlipUiState`
   - Added selection state flows:
     - `_selectedForBulkAction: Set<String>`
     - `_isBulkActionMode: Boolean`
   - Added selection management functions:
     - `toggleBulkActionMode()`
     - `toggleSelection(gasSlipId: String)`
     - `selectAll(gasSlipIds: List<String>)`
     - `clearSelection()`
   - Added action functions:
     - `cancelGasSlip(gasSlipId: String)`
     - `bulkMarkPendingAsDispensed()`

3. **app/src/main/java/dev/ml/fuelhub/presentation/screen/GasSlipScreen.kt**
   - Added callbacks: `onCancelClick`, `onMarkDispensedClick`
   - Added secondary action row with:
     - Green "Mark Dispensed" button (for pending slips)
     - Red "Cancel" button (for pending slips)
   - Updated print button to check cancelled status

4. **app/src/main/java/dev/ml/fuelhub/presentation/screen/GasSlipListScreen.kt**
   - Updated header to show bulk mode indicator with selection count
   - Added bulk action toolbar with:
     - Select All button
     - Mark Dispensed button
   - Modified GasSlipCard to support:
     - Selection mode with checkbox
     - Visual selection feedback (border, background color)
     - Conditional button rendering (no action buttons in bulk mode)
     - Cancel button for pending slips
   - Added bulk actions button to main toolbar
   - Updated `onCancelClick` callback passing

5. **app/src/main/java/dev/ml/fuelhub/data/repository/FirebaseGasSlipRepositoryImpl.kt**
   - Implemented `cancelGasSlip()`: Sets status to CANCELLED
   - Implemented `bulkMarkPendingAsDispensed()`:
     - Only marks PENDING slips
     - Updates status to DISPENSED
     - Sets dispensedAt timestamp
     - Updates associated transaction to COMPLETED
     - Handles errors gracefully

## User Workflow

### Cancelling a Single Slip
1. Open gas slip detail or expand card in list
2. Click red "Cancel" button (only visible for pending slips)
3. Slip status changes to CANCELLED
4. All actions except viewing become disabled

### Bulk Marking as Dispensed
1. Click the select-all icon in the toolbar to enter bulk mode
2. Header changes to show "X selected"
3. Click individual cards to select/deselect (checkboxes appear)
4. Green toolbar appears with:
   - "Select All" - quickly select all filtered slips
   - "Mark Dispensed" - execute bulk action
5. Click "Mark Dispensed" to update all selected slips
6. Exit bulk mode by clicking the close button in the header

## Status Transitions

```
PENDING
  ├─ (Cancel) → CANCELLED
  ├─ (Mark as Dispensed individually) → DISPENSED
  └─ (Bulk Mark Dispensed) → DISPENSED

DISPENSED (no further actions)
USED (legacy, no further actions)
CANCELLED (no further actions)
```

## API Integration

All operations use Firestore through `FirebaseDataSource`:
- Cancellations update the GasSlip document status field
- Bulk operations loop through selected IDs and update each:
  - GasSlip document (status, dispensedAt, dispensedLiters)
  - Associated FuelTransaction document (status=COMPLETED, completedAt)

## Visual Design

- **Cancellation Button**: Red background (Color 0xFFf44336)
- **Mark Dispensed Button**: Green background (Color 0xFF4CAF50)
- **Selection Highlight**: Cyan border (2dp) with cyan background tint (alpha 0.1f)
- **Status Badges**:
  - PENDING: Orange
  - DISPENSED: Green
  - USED: Cyan
  - CANCELLED: Red
- **Bulk Mode Header**: Cyan background with count display

## Error Handling

- Transaction failures logged but don't fail the operation
- Bulk actions gracefully skip slips that aren't in PENDING status
- User feedback via `BulkActionSuccess` state or error messages
- Empty selection prevents bulk action execution
