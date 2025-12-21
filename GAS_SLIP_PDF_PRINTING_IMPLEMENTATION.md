# Gas Slip PDF Generation, Printing & Export Implementation

## Overview
Complete implementation of gas slip PDF generation, printing, sharing, and management. After creating a transaction, a gas slip is automatically generated and users can print, export, and view all gas slips from a dedicated screen.

## Components Created

### 1. GasSlipManagementViewModel
**File**: `presentation/viewmodel/GasSlipManagementViewModel.kt`

**Responsibility**: Manages gas slip state, loading, filtering, and user interactions

**Features**:
- Load all gas slips from repository
- Filter by status (ALL, PENDING, USED)
- Load gas slips by specific date
- Load gas slips by office
- Select/deselect gas slips
- Mark gas slips as used
- UI state management (Loading, Success, Error)

**UI States**:
- `Idle` - Initial state
- `Loading` - Fetching data
- `Success` - Data loaded with list of gas slips
- `Error` - Error occurred with message
- `PrintSuccess` - Print completed
- `ExportSuccess` - Export completed

**StateFlows Exposed**:
- `uiState` - Current UI state
- `selectedGasSlip` - Currently selected gas slip
- `filterByStatus` - Active filter (ALL/PENDING/USED)
- `allGasSlips` - Complete list of gas slips

### 2. PdfPrintManager
**File**: `data/util/PdfPrintManager.kt`

**Responsibility**: Handle all PDF operations including generation, printing, sharing, and file management

**Methods**:

#### `generateAndPrintGasSlip(gasSlip: GasSlip): Boolean`
- Generates PDF from gas slip
- Initiates print job automatically
- Returns success/failure status

#### `generatePdfOnly(gasSlip: GasSlip): String?`
- Generates PDF without printing
- Returns file path if successful
- Useful for sharing or manual printing later

#### `openPdfInViewer(filePath: String)`
- Opens PDF in device's PDF viewer app
- User can then print, share, or save

#### `sharePdfFile(filePath: String)`
- Opens share dialog for the PDF
- User can send via email, messaging, etc.

#### `getAllGeneratedPdfs(): List<File>`
- Retrieves all PDF files from documents folder
- Sorted by modification date (newest first)
- Used to populate the gas slip list

#### `deletePdfFile(filePath: String): Boolean`
- Deletes a specific PDF file
- Returns success/failure status

#### `getPdfFileSizeMb(filePath: String): Double`
- Returns file size in MB
- Can be displayed in UI for reference

#### `pdfFileExists(filePath: String): Boolean`
- Checks if a PDF file exists
- Useful for validation

### 3. GasSlipListScreen
**File**: `presentation/screen/GasSlipListScreen.kt`

**Responsibility**: Display all gas slips with filtering, printing, and sharing capabilities

**UI Components**:

#### Header Section
- Title "Gas Slips"
- Refresh button to reload gas slips

#### Filter Chips
- Three filters: ALL, PENDING, USED
- Dynamically filters displayed gas slips

#### Gas Slip Cards
Each card displays:
- Reference Number (prominent)
- Driver Name (subtitle)
- Status Badge (USED in green, PENDING in orange)
- Expand/Collapse for details

#### Expanded Card Details
When expanded, shows:
- Vehicle Plate & Type
- Fuel Type
- Liters to pump
- Destination
- Trip Purpose
- Transaction Date/Time
- Action Buttons:
  - **View** (Info) - View full details
  - **Print** (Cyan) - Generate PDF and print (disabled if already used)
  - **Share** (Green) - Generate PDF and share via email/messaging

#### Empty State
- Shows when no gas slips match current filter
- Receipt icon + message

#### Error State
- Shows error message if loading fails
- Retry button to reload

### 4. Updated MainActivity.kt

**Changes**:
- Added `GasSlipManagementViewModel` initialization
- Added `PdfPrintManager` initialization
- New navigation tab "Gas Slips" (tab index 5)
- Reports tab moved to index 6
- New route "gasslips" in NavHost

**New Navigation Item**:
```kotlin
NavigationBarItem(
    icon = { Icon(Icons.Default.Receipt, "Gas Slips") },
    label = { Text("Gas Slips") },
    selected = selectedTab == 5,
    onClick = { 
        selectedTab = 5
        navController.navigate("gasslips")
    }
)
```

**Gas Slip Route Handler**:
```kotlin
composable("gasslips") {
    GasSlipListScreen(
        gasSlipViewModel = gasSlipManagementViewModel,
        onGasSlipSelected = { gasSlip -> /* Handle selection */ },
        onPrintClick = { gasSlip -> 
            pdfPrintManager.generateAndPrintGasSlip(gasSlip)
        },
        onShareClick = { gasSlip ->
            val pdfPath = pdfPrintManager.generatePdfOnly(gasSlip)
            pdfPrintManager.sharePdfFile(pdfPath)
        }
    )
}
```

### 5. AndroidManifest.xml Updates

**Permissions Added**:
```xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

**FileProvider Configuration**:
```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

### 6. file_paths.xml (New Resource File)
**Location**: `res/xml/file_paths.xml`

Configures paths that can be accessed by FileProvider:
- External Documents directory
- External files directory
- External cache directory

## Data Flow

### Gas Slip Generation & Printing

```
Transaction Created
    ↓
Gas Slip Created (via UseCase)
    ↓
Gas Slip Saved to Firestore
    ↓
User Navigates to Gas Slips Screen
    ↓
GasSlipManagementViewModel.loadAllGasSlips()
    ↓
GasSlipRepository.getUnusedGasSlips()
    ↓
Fetch from Firebase Firestore
    ↓
Populate UI StateFlow
    ↓
GasSlipListScreen Displays Gas Slips
    ↓
User Clicks Print
    ↓
PdfPrintManager.generateAndPrintGasSlip()
    ↓
GasSlipPdfGenerator.generateGasSlipPdf()
    ↓
PDF Saved to Documents folder
    ↓
Print Dialog Opens
    ↓
User Confirms Print
    ↓
Print Job Sent to Printer/Virtual Printer
```

### Gas Slip Sharing

```
User Clicks Share on Gas Slip
    ↓
PdfPrintManager.generatePdfOnly()
    ↓
PDF Generated (if not already exists)
    ↓
PdfPrintManager.sharePdfFile()
    ↓
FileProvider Creates Content URI
    ↓
Share Dialog Opens
    ↓
User Selects Email/Messaging App
    ↓
PDF Shared
```

## File Storage

**Location**: `context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)`

**File Naming**: `gas_slip_{referenceNumber}.pdf`

**Example**: `gas_slip_GS-2025-01-001.pdf`

**Access**:
- Private to application
- Survives app uninstall (backed up)
- Can be accessed by FileProvider for sharing
- Printer applications can access via intent

## PDF Content Structure

The PDF generated includes:

### Header
- Title: "MDRRMO FUEL SLIP"
- Office Name

### Information Sections
1. **Reference & Date**
   - Reference Number
   - Transaction Date

2. **Fuel Information**
   - Fuel Type (GASOLINE/DIESEL)
   - Liters to Dispense

3. **Vehicle Information**
   - Vehicle Type
   - Plate Number

4. **Driver Information**
   - Driver Name
   - Passengers (if any)

5. **Trip Details**
   - Destination
   - Purpose

6. **Status**
   - PENDING or USED (color-coded)
   - Used timestamp (if applicable)

7. **Footer**
   - Official disclaimer

## Integration Points

### With Transaction Creation
After a transaction is successfully created via `CreateFuelTransactionUseCase`:
1. Gas slip is automatically created in `createGasSlip()`
2. Data saved to Firestore
3. User navigates to Gas Slips screen
4. Can immediately print or share the new gas slip

### With Firestore
- Gas slips loaded from `gasSlipRepository`
- Repository queries Firestore collection
- Supports date-based, office-based queries
- Status tracked (used/unused)

## User Workflow

### Printing a Gas Slip
1. Navigate to "Gas Slips" tab
2. Filter by status if needed (optional)
3. Find desired gas slip in list
4. Tap card to expand details
5. Click "Print" button
6. System generates PDF
7. Print dialog appears
8. Select printer or save as PDF
9. Confirm print

### Sharing a Gas Slip
1. Navigate to "Gas Slips" tab
2. Find desired gas slip
3. Expand card
4. Click "Share" button
5. System generates PDF
6. Share dialog appears
7. Select email or messaging app
8. PDF sent to recipient

### Viewing Details
1. Navigate to "Gas Slips" tab
2. Expand card to see all details
3. Status badge shows if used or pending
4. Color-coded for quick identification

## Permissions Required

### Runtime Permissions (API 30+)
- `READ_EXTERNAL_STORAGE` - Read stored PDFs
- `WRITE_EXTERNAL_STORAGE` - Save generated PDFs

### Request at Runtime
```kotlin
// In Activity or Fragment
val permissions = arrayOf(
    Manifest.permission.READ_EXTERNAL_STORAGE,
    Manifest.permission.WRITE_EXTERNAL_STORAGE
)
ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)
```

## PDF Generation Library
Uses **iText 7** library:
- Professional PDF generation
- Custom styling and layout
- Support for tables, paragraphs, etc.
- Colors, fonts, formatting

## Testing Checklist

- [ ] Transaction created successfully
- [ ] Gas slip generated and saved to Firestore
- [ ] Gas Slips screen accessible from navigation
- [ ] Gas slips load and display correctly
- [ ] Filter buttons (ALL, PENDING, USED) work
- [ ] Cards expand/collapse with details
- [ ] Print button initiates print dialog
- [ ] PDF file created in Documents folder
- [ ] Share button opens share dialog
- [ ] Can share via email or messaging
- [ ] Permissions requested properly
- [ ] Empty state shows when no slips match filter
- [ ] Error state shows with retry button
- [ ] Refresh button reloads gas slips
- [ ] Used gas slips show USED badge in green
- [ ] Pending gas slips show PENDING badge in orange
- [ ] Print button disabled for used gas slips

## Future Enhancements

- Archive/Delete gas slips
- Search functionality
- Date range filtering
- Export all gas slips as ZIP
- Digital signature support
- Email notification when ready
- Print queue management
- Re-print history tracking
