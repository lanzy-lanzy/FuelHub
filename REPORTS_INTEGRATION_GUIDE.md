# Reports Screen Integration Guide

## Quick Start

### Step 1: Update Navigation
Navigate to your navigation composable and add the route:

```kotlin
// In your NavGraph or MainNavigation.kt
composable("reports_enhanced") {
    val reportsViewModel: ReportsViewModel = hiltViewModel()
    ReportScreenEnhanced(viewModel = reportsViewModel)
}
```

### Step 2: Replace Old Reports Screen Navigation
Update navigation from old screen:
```kotlin
// OLD
composable("reports") {
    ReportScreen(...)
}

// NEW
composable("reports") {
    val reportsViewModel: ReportsViewModel = hiltViewModel()
    ReportScreenEnhanced(viewModel = reportsViewModel)
}
```

### Step 3: Setup Dependency Injection (Hilt)
Add to your `RepositoryModule.kt` or similar:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object ReportsModule {
    
    @Provides
    @Singleton
    fun provideReportsViewModel(
        dailyReportUseCase: GenerateDailyReportUseCase,
        weeklyReportUseCase: GenerateWeeklyReportUseCase,
        monthlyReportUseCase: GenerateMonthlyReportUseCase,
        transactionRepository: FuelTransactionRepository
    ): ReportsViewModel = ReportsViewModel(
        dailyReportUseCase,
        weeklyReportUseCase,
        monthlyReportUseCase,
        transactionRepository
    )
}
```

### Step 4: Add Required Permissions
Update `AndroidManifest.xml`:

```xml
<!-- For PDF export to external storage -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />

<!-- For Android 11+ (API 30+) -->
<uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE" />

<!-- Inside <application> tag -->
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

Create `res/xml/file_paths.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<paths xmlns:android="http://schemas.android.com/apk/res/android">
    <external-files-path name="documents" path="Documents/FuelHubReports" />
</paths>
```

### Step 5: Handle Runtime Permissions
Add permission handling in your Activity/Fragment:

```kotlin
private fun requestStoragePermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        // Android 13+
        requestPermission(android.Manifest.permission.READ_MEDIA_DOCUMENTS)
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        // Android 11-12
        requestPermission(android.Manifest.permission.MANAGE_EXTERNAL_STORAGE)
    } else {
        // Android 10 and below
        requestPermissions(
            arrayOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            STORAGE_PERMISSION_CODE
        )
    }
}
```

## Features Overview

### 1. Filter Panel
- **Date Range**: Today, This Week, This Month, Last Month, Custom
- **Fuel Types**: Multi-select from GASOLINE, DIESEL, PREMIUM, etc.
- **Transaction Status**: Multi-select from COMPLETED, PENDING, FAILED, CANCELLED
- **Vehicles**: Multi-select from available vehicles
- **Drivers**: Multi-select from available drivers
- **Liter Range**: Min/Max liter filter
- **Text Search**: Search by reference, driver, or vehicle

### 2. Export Options
- **PDF Export**: Full formatted report with statistics
- **Print**: Direct printing (requires print service)
- **Share**: Share via email, messaging, etc.

### 3. Report Views
- **Daily**: Single day transactions and statistics
- **Weekly**: 7-day period breakdown
- **Monthly**: 4-week summary

### 4. Statistics Display
Shows real-time calculations:
- Total liters consumed
- Transaction count
- Completed/Pending/Failed breakdown
- Average liters per transaction

## Complete Example Navigation Setup

```kotlin
// MainActivity.kt or similar
fun navigateToReports(navController: NavController) {
    navController.navigate("reports")
}

// NavGraph setup
NavGraph(
    navController = navController,
    startDestination = "home"
) {
    composable("home") {
        HomeScreen()
    }
    
    composable("reports") {
        val reportsViewModel: ReportsViewModel = hiltViewModel()
        ReportScreenEnhanced(viewModel = reportsViewModel)
    }
    
    // Other routes...
}
```

## Testing the Implementation

### Test 1: Basic Navigation
1. Navigate to Reports screen
2. Verify all 3 tabs (Daily, Weekly, Monthly) are visible
3. Confirm data loads and statistics display

### Test 2: Filtering
1. Open filter panel
2. Select date range (e.g., "This Week")
3. Verify transaction list updates
4. Change fuel type filter
5. Confirm list re-filters

### Test 3: Search
1. Enter search text (reference number, driver, vehicle)
2. Verify list filters in real-time
3. Test with partial matches

### Test 4: Export
1. Click Export button
2. Select "Export as PDF"
3. Verify PDF file created in Documents/FuelHubReports/
4. Open and verify content

### Test 5: Empty State
1. Set very restrictive filters
2. Verify empty state message displays
3. Reset filters
4. Confirm data reappears

## Common Issues & Solutions

### Issue: "Activity was destroyed, but coroutines were still running"
**Solution**: Ensure viewModel scope is properly cancelled:
```kotlin
override fun onDestroy() {
    viewModel.coroutineContext.cancel()
    super.onDestroy()
}
```

### Issue: PDF not saving
**Solution**: Verify permissions and directory:
```kotlin
// Check permission
if (ContextCompat.checkSelfPermission(
    context,
    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
) != PackageManager.PERMISSION_GRANTED) {
    requestPermission()
}

// Create directory
val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
dir?.mkdirs()
```

### Issue: Data not loading
**Solution**: Check Firestore rules and connectivity:
```kotlin
// In Firestore Console, ensure rules allow reads:
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /transactions/{document=**} {
      allow read: if request.auth != null;
      allow write: if request.auth != null;
    }
  }
}
```

### Issue: Filters not updating
**Solution**: Verify LiveData/StateFlow collection:
```kotlin
// Ensure you're collecting the StateFlow
val filterState by viewModel.filterState.collectAsState()
val filteredData by viewModel.filteredData.collectAsState()
```

## Performance Tips

### 1. Optimize Date Ranges
- Default to last 30 days instead of all-time
- Encourage users to use specific date ranges
- Warn when selecting large ranges (>1 year)

### 2. Pagination
- Show first 50 transactions, indicate total count
- Implement scroll-to-load for more results
- Cache loaded results

### 3. PDF Generation
- Show progress indicator during generation
- Limit to 1000 rows maximum
- Use separate thread for PDF generation

### 4. Filter Debouncing
Current implementation filters on every change. Consider debouncing:
```kotlin
val debouncedFilters = filterState.debounce(300L)
```

## Customization Examples

### Change Default Filter Date Range
```kotlin
// In ReportsViewModel.kt
private val _filterState = MutableStateFlow(
    ReportFilterState(
        dateFrom = LocalDate.now().minusMonths(3), // Change here
        dateTo = LocalDate.now()
    )
)
```

### Add New Filter Type
1. Add property to `ReportFilterState`:
```kotlin
data class ReportFilterState(
    // ... existing properties
    val minAmount: Double = 0.0
)
```

2. Add update function in `ReportsViewModel`:
```kotlin
fun updateAmountRange(min: Double) {
    _filterState.value = _filterState.value.copy(minAmount = min)
    applyFilters()
}
```

3. Update filter logic in `applyFilters()`:
```kotlin
val filtered = allTransactions.filter { transaction ->
    // ... existing filters
    (transaction.litersToPump * pricePerLiter >= filters.minAmount)
}
```

4. Add UI component in `FiltersPanelContent`:
```kotlin
OutlinedTextField(
    value = filterState.minAmount.toString(),
    onValueChange = { viewModel.updateAmountRange(it.toDoubleOrNull() ?: 0.0) },
    label = { Text("Minimum Amount") }
)
```

### Customize PDF Styling
Edit `PdfReportGenerator.kt`:
```kotlin
// Change header color
val headerColor = DeviceRgb(59, 89, 152) // Facebook blue

// Change font
val boldFont = PdfFontFactory.createFont(StandardFonts.COURIER_BOLD)

// Add logo/watermark
val image = ImageData.create("path/to/logo.png")
document.add(Image(image).setWidth(100f))
```

## Troubleshooting Checklist

- [ ] All imports are correct and files are in right packages
- [ ] Hilt dependency injection is properly configured
- [ ] Navigation route is properly registered
- [ ] Permissions are added to AndroidManifest.xml
- [ ] FileProvider is configured correctly
- [ ] iText dependency is in build.gradle.kts
- [ ] Firestore rules allow reading transactions
- [ ] Device storage has enough space for PDFs
- [ ] Logcat shows no errors about missing dependencies

## Next Steps

1. **Test thoroughly**: Run all test cases above
2. **Monitor Firestore**: Check query performance in Firebase console
3. **Gather feedback**: User testing for filter usability
4. **Optimize**: Based on performance metrics
5. **Enhance**: Add features from "Future Enhancements" section

## Support & Documentation

- **iText Documentation**: https://itextpdf.com/en/products/itext-7-core
- **Firebase Firestore**: https://firebase.google.com/docs/firestore
- **Jetpack Compose**: https://developer.android.com/jetpack/compose
- **Kotlin Coroutines**: https://kotlinlang.org/docs/coroutines-overview.html
