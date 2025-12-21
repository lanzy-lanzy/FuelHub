# Reports Navigation Setup - Copy & Paste

This file contains exact code snippets you can copy and paste to integrate the enhanced Reports screen.

## 1. Navigation Composable

Find your navigation setup file (usually `NavGraph.kt`, `Navigation.kt`, or in `MainActivity.kt`):

### OLD CODE (Replace this)
```kotlin
composable("reports") {
    ReportScreen(
        dailyReportUseCase = // ...,
        weeklyReportUseCase = // ...,
        monthlyReportUseCase = // ...
    )
}
```

### NEW CODE (With this)
```kotlin
composable("reports") {
    val reportsViewModel: ReportsViewModel = hiltViewModel()
    ReportScreenEnhanced(viewModel = reportsViewModel)
}
```

## 2. Dependency Injection Module

Create a new file: `app/src/main/java/dev/ml/fuelhub/di/ReportsModule.kt`

```kotlin
package dev.ml.fuelhub.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.ml.fuelhub.domain.usecase.GenerateDailyReportUseCase
import dev.ml.fuelhub.domain.usecase.GenerateWeeklyReportUseCase
import dev.ml.fuelhub.domain.usecase.GenerateMonthlyReportUseCase
import dev.ml.fuelhub.domain.repository.FuelTransactionRepository
import dev.ml.fuelhub.presentation.viewmodel.ReportsViewModel
import javax.inject.Singleton

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
        dailyReportUseCase = dailyReportUseCase,
        weeklyReportUseCase = weeklyReportUseCase,
        monthlyReportUseCase = monthlyReportUseCase,
        transactionRepository = transactionRepository
    )
}
```

## 3. AndroidManifest.xml Updates

Find your `AndroidManifest.xml` (in `app/src/main/`):

### Add Permissions
```xml
<!-- For file storage -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE" />

<!-- For printing -->
<uses-permission android:name="android.permission.PRINT" />
```

### Add FileProvider (inside `<application>` tag)
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

## 4. Create FileProvider Configuration

Create new file: `app/src/main/res/xml/file_paths.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<paths xmlns:android="http://schemas.android.com/apk/res/android">
    <external-files-path
        name="documents"
        path="Documents/FuelHubReports" />
</paths>
```

## 5. Import Statements for Navigation

Add these imports to your navigation file:

```kotlin
import dev.ml.fuelhub.presentation.screen.ReportScreenEnhanced
import dev.ml.fuelhub.presentation.viewmodel.ReportsViewModel
import androidx.hilt.navigation.compose.hiltViewModel
```

## 6. Navigation Button/Menu Item

Find where you navigate to Reports (button, menu, etc.):

### From Button
```kotlin
Button(onClick = {
    navController.navigate("reports")
}) {
    Text("Reports")
}
```

### From Menu Item
```kotlin
MenuItem(
    onClick = {
        navController.navigate("reports")
        closeMenu() // if using dropdown menu
    },
    label = "Reports"
)
```

## 7. Build.gradle.kts Verification

Make sure your `app/build.gradle.kts` has these dependencies (should already be there):

```kotlin
dependencies {
    // Hilt for dependency injection
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-compiler:2.48")
    
    // iText for PDF
    implementation("com.itextpdf:itext7-core:7.2.5")
    
    // Jetpack Compose (should already be there)
    implementation(libs.androidx.compose.material3)
    
    // Lifecycle/ViewModel (should already be there)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
}
```

## 8. Navigation Graph Example (Complete)

If you need a complete example of the navigation setup:

```kotlin
// NavGraph.kt or similar
@Composable
fun FuelHubNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier
    ) {
        composable("home") {
            HomeScreen(
                onNavigateToReports = { navController.navigate("reports") }
            )
        }
        
        composable("reports") {
            val reportsViewModel: ReportsViewModel = hiltViewModel()
            ReportScreenEnhanced(viewModel = reportsViewModel)
        }
        
        composable("transactions") {
            TransactionScreen()
        }
        
        // Other routes...
    }
}
```

## 9. Activity Setup Example

If using with Activity:

```kotlin
// MainActivity.kt
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FuelHubTheme {
                val navController = rememberNavController()
                FuelHubNavGraph(navController = navController)
            }
        }
    }
}
```

## 10. Runtime Permissions (Optional but Recommended)

For handling storage permissions at runtime:

```kotlin
// In your Activity or ViewModel
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build

fun requestStoragePermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        // Android 13+
        if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_MEDIA_DOCUMENTS
        ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.READ_MEDIA_DOCUMENTS),
                STORAGE_PERMISSION_CODE
            )
        }
    } else {
        // Android 12 and below
        if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                STORAGE_PERMISSION_CODE
            )
        }
    }
}

override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<String>,
    grantResults: IntArray
) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (requestCode == STORAGE_PERMISSION_CODE) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission granted
            generateReport()
        } else {
            // Permission denied
            showMessage("Storage permission required for PDF export")
        }
    }
}

companion object {
    private const val STORAGE_PERMISSION_CODE = 100
}
```

## Checklist Before Building

- [ ] Created `ReportsModule.kt`
- [ ] Updated navigation to use `ReportScreenEnhanced`
- [ ] Added imports for ViewModel and Screen
- [ ] Added permissions to `AndroidManifest.xml`
- [ ] Created `file_paths.xml` 
- [ ] Added FileProvider to `AndroidManifest.xml`
- [ ] Verified iText dependency in `build.gradle.kts`
- [ ] Synchronized Gradle files
- [ ] No import errors in IDE
- [ ] Added `@AndroidEntryPoint` to MainActivity if using Hilt

## Build & Run

```bash
# Clean build
./gradlew clean

# Build
./gradlew build

# Run
./gradlew installDebug
```

## Verification

After building and running:

1. ✅ Navigate to Reports screen
2. ✅ All three tabs visible (Daily, Weekly, Monthly)
3. ✅ Filter button clickable
4. ✅ Export button clickable
5. ✅ Data displays from Firestore
6. ✅ Filters update results in real-time
7. ✅ PDF export works and saves file

## Common Errors & Fixes

### Error: "Unresolved reference: ReportsViewModel"
**Fix**: Ensure `ReportsModule.kt` is created and in `di` package

### Error: "Cannot find symbol: ReportScreenEnhanced"
**Fix**: Verify file path is `presentation/screen/ReportScreenEnhanced.kt`

### Error: "Class ReportsViewModel is not bound in Hilt"
**Fix**: Ensure `@InstallIn(SingletonComponent::class)` is on `ReportsModule`

### Error: "hiltViewModel not found"
**Fix**: Add import: `import androidx.hilt.navigation.compose.hiltViewModel`

### Error: "File provider not declared"
**Fix**: Add FileProvider to AndroidManifest and create file_paths.xml

## Testing Navigation

```kotlin
@Test
fun testReportsNavigation() {
    val navController = TestNavHostController(context = ApplicationProvider.getApplicationContext())
    navController.setGraph(R.navigation.nav_graph)
    
    navController.navigate("reports")
    
    assertTrue(navController.currentDestination?.route?.contains("reports") ?: false)
}
```

## Notes

- Don't remove the old `ReportScreen.kt` if other code references it
- `ReportScreenEnhanced` is backwards compatible with existing infrastructure
- All data is fetched in real-time from Firestore
- PDFs are stored locally on device

---

**Ready to Integrate?** Copy sections 1-9 above and paste into your project files.

**Questions?** Refer to `REPORTS_INTEGRATION_GUIDE.md` for detailed explanation.
