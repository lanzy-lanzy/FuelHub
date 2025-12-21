# Reports Screen - Customization Examples

Copy and paste these examples to customize the Reports feature.

## 1. Change Default Filter Date Range

**File**: `presentation/viewmodel/ReportsViewModel.kt`

**OLD CODE:**
```kotlin
private val _filterState = MutableStateFlow(ReportFilterState())
```

**NEW CODE (Last 60 days):**
```kotlin
private val _filterState = MutableStateFlow(
    ReportFilterState(
        dateFrom = LocalDate.now().minusDays(60),
        dateTo = LocalDate.now()
    )
)
```

**NEW CODE (Last 3 months):**
```kotlin
private val _filterState = MutableStateFlow(
    ReportFilterState(
        dateFrom = LocalDate.now().minusMonths(3),
        dateTo = LocalDate.now()
    )
)
```

## 2. Add Minimum Daily Liters Filter

**In `ReportFilterState`:**
```kotlin
data class ReportFilterState(
    // ... existing properties
    val minDailyLiters: Double = 10.0  // Add this
)
```

**Update filter method:**
```kotlin
fun updateMinDailyLiters(min: Double) {
    _filterState.value = _filterState.value.copy(minDailyLiters = min)
    applyFilters()
}
```

**Apply in filtering logic:**
```kotlin
val filtered = allTransactions.filter { transaction ->
    // ... existing filters
    (transaction.litersToPump >= filters.minDailyLiters)
}
```

## 3. Add Cost Range Filter

**In `ReportFilterState`:**
```kotlin
data class ReportFilterState(
    // ... existing properties
    val minCost: Double = 0.0,
    val maxCost: Double = Double.MAX_VALUE
)
```

**Add to ViewModel:**
```kotlin
fun updateCostRange(min: Double, max: Double) {
    _filterState.value = _filterState.value.copy(
        minCost = min,
        maxCost = max
    )
    applyFilters()
}
```

**In filtering logic (assuming $2 per liter):**
```kotlin
val filtered = allTransactions.filter { transaction ->
    val cost = transaction.litersToPump * 2.0 // Price per liter
    cost in filters.minCost..filters.maxCost
}
```

## 4. Add Trip Purpose Filter

**In `ReportFilterState`:**
```kotlin
data class ReportFilterState(
    // ... existing properties
    val selectedPurposes: Set<String> = emptySet()
)
```

**Add to ViewModel:**
```kotlin
fun updateTripPurposes(purposes: Set<String>) {
    _filterState.value = _filterState.value.copy(selectedPurposes = purposes)
    applyFilters()
}
```

**Fetch available purposes:**
```kotlin
private fun fetchAvailableFilters() {
    viewModelScope.launch {
        try {
            val transactions = transactionRepository.getAllTransactions()
            // ... existing code
            val purposes = transactions.map { it.tripPurpose }.distinct()
            // Store in StateFlow
        } catch (e: Exception) {
            Timber.e(e, "Error fetching filters")
        }
    }
}
```

## 5. Change PDF Color Scheme

**File**: `utils/PdfReportGenerator.kt`

**OLD CODE:**
```kotlin
headerTable.addCell(createHeaderCell("Total Liters", boldFont))
```

**NEW CODE (Blue header):**
```kotlin
private fun createHeaderCell(text: String, font: PdfFont): Cell {
    return Cell().add(
        Paragraph(text)
            .setFont(font)
            .setFontSize(10f)
            .setTextAlignment(TextAlignment.CENTER)
    ).setBackgroundColor(com.itextpdf.kernel.colors.DeviceRgb(33, 150, 243)) // Blue
}
```

**Dark theme header:**
```kotlin
.setBackgroundColor(com.itextpdf.kernel.colors.DeviceRgb(33, 33, 33)) // Dark gray
```

**Green header:**
```kotlin
.setBackgroundColor(com.itextpdf.kernel.colors.DeviceRgb(76, 175, 80)) // Green
```

## 6. Add Watermark to PDF

**In `PdfReportGenerator.generateReport()` before `document.close()`:**

```kotlin
// Add watermark
val watermark = Paragraph("CONFIDENTIAL")
    .setFont(boldFont)
    .setFontSize(60f)
    .setOpacity(0.1f)
    .setRotationAngle(45.0)
    .setTextAlignment(TextAlignment.CENTER)
document.add(watermark)
```

## 7. Add Company Logo to PDF

**In `PdfReportGenerator.generateReport()` after header:**

```kotlin
// Add logo (assuming logo is in drawable)
try {
    val logoStream = context.resources.openRawResource(R.drawable.company_logo)
    val imageData = ImageData.create(logoStream.readBytes())
    val image = Image(imageData)
        .setWidth(100f)
        .setHeight(50f)
    document.add(image)
} catch (e: Exception) {
    Timber.e(e, "Error adding logo to PDF")
}
```

## 8. Change Filter Panel Background Color

**File**: `presentation/screen/ReportScreenEnhanced.kt`

**OLD CODE:**
```kotlin
Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
)
```

**NEW CODE:**
```kotlin
Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A237E)) // Deep blue
)
```

## 9. Add Filters to Quick Access Buttons

**Add below `ReportsHeaderEnhanced`:**

```kotlin
@Composable
fun QuickFilterButtons(viewModel: ReportsViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AssistChip(
            onClick = {
                viewModel.updateDateRange(LocalDate.now(), LocalDate.now())
            },
            label = { Text("Today") }
        )
        AssistChip(
            onClick = {
                viewModel.updateDateRange(
                    LocalDate.now().minusDays(7),
                    LocalDate.now()
                )
            },
            label = { Text("This Week") }
        )
        AssistChip(
            onClick = {
                viewModel.updateStatuses(setOf(TransactionStatus.COMPLETED))
            },
            label = { Text("Completed Only") }
        )
    }
}
```

**Then add to main screen:**
```kotlin
Column(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(16.dp)
) {
    ReportsHeaderEnhanced(...)
    QuickFilterButtons(viewModel)  // Add this
    // ... rest of content
}
```

## 10. Customize Transaction Table Columns

**In `DetailedTransactionsList` composable:**

**OLD CODE:**
```kotlin
Transaction(
    referenceNumber = transaction.referenceNumber,
    driver = transaction.driverName,
    liters = transaction.litersToPump,
    status = transaction.status.name
)
```

**NEW CODE (Add more columns):**
```kotlin
TransactionDetailRow(
    transaction = transaction,
    showCost = true,        // Show calculated cost
    showNotes = true,       // Show transaction notes
    showApprover = true     // Show who approved
)

@Composable
fun TransactionDetailRow(
    transaction: FuelTransaction,
    showCost: Boolean = false,
    showNotes: Boolean = false,
    showApprover: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(transaction.referenceNumber, fontWeight = FontWeight.Bold)
            Text(transaction.status.name)
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("${transaction.driverName} • ${transaction.vehicleId}", fontSize = 12.sp)
            Text("${String.format("%.2f", transaction.litersToPump)} L", color = SuccessGreen)
            if (showCost) {
                Text("$${String.format("%.2f", transaction.litersToPump * 2.0)}", color = ElectricBlue)
            }
        }
        
        if (showNotes && !transaction.notes.isNullOrEmpty()) {
            Text(transaction.notes!!, fontSize = 10.sp, color = TextSecondary)
        }
        
        if (showApprover && !transaction.approvedBy.isNullOrEmpty()) {
            Text("Approved by: ${transaction.approvedBy}", fontSize = 10.sp, color = SuccessGreen)
        }
    }
}
```

## 11. Add Export Format Options

**In `ExportMenuContent`:**

```kotlin
Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(12.dp)
) {
    Text("Export Report", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = VibrantCyan)
    
    // PDF Export
    ExportButton(
        icon = Icons.Default.PictureAsPdf,
        label = "Export as PDF",
        description = "High-quality PDF format",
        color = Color(0xFFD32F2F),
        onClick = { viewModel.generatePdfReport("report_${System.currentTimeMillis()}.pdf") }
    )
    
    // CSV Export
    ExportButton(
        icon = Icons.Default.TableChart,
        label = "Export as CSV",
        description = "Spreadsheet compatible format",
        color = ElectricBlue,
        onClick = { generateCsvReport(viewModel) }
    )
    
    // JSON Export
    ExportButton(
        icon = Icons.Default.Code,
        label = "Export as JSON",
        description = "Structured data format",
        color = SuccessGreen,
        onClick = { generateJsonReport(viewModel) }
    )
}
```

## 12. Add Dark/Light Theme Toggle

**In Reports screen:**

```kotlin
var isDarkMode by remember { mutableStateOf(true) }

Box(
    modifier = modifier
        .fillMaxSize()
        .background(if (isDarkMode) DeepBlue else Color.White)
) {
    Column {
        ReportsHeaderEnhanced(
            onFilterClick = { /* ... */ },
            onExportClick = { /* ... */ },
            animatedOffset = animatedOffset,
            isDarkMode = isDarkMode,
            onThemeToggle = { isDarkMode = !isDarkMode }
        )
        // ... rest of content
    }
}
```

## 13. Add Report Generation Progress

**In ViewModel:**

```kotlin
private val _generationProgress = MutableStateFlow(0f)
val generationProgress: StateFlow<Float> = _generationProgress

fun generatePdfReport(filename: String): String? {
    return try {
        _generationProgress.value = 0f
        
        val data = _filteredData.value
        
        _generationProgress.value = 0.3f // Header generation
        // ... create header
        
        _generationProgress.value = 0.6f // Data processing
        // ... process transactions
        
        _generationProgress.value = 0.9f // File writing
        // ... write to file
        
        _generationProgress.value = 1f
        filename
    } catch (e: Exception) {
        null
    }
}
```

**In UI:**

```kotlin
if (isGeneratingPdf) {
    LinearProgressIndicator(
        progress = generationProgress,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
}
```

## 14. Add Email Export Feature

**Add to ViewModel:**

```kotlin
fun getEmailShareContent(): String {
    val data = _filteredData.value
    return """
    Fuel Transaction Report
    Generated: ${LocalDateTime.now()}
    
    Statistics:
    - Total Liters: ${String.format("%.2f", data.totalLiters)} L
    - Total Transactions: ${data.transactionCount}
    - Completed: ${data.completedCount}
    - Average per Transaction: ${String.format("%.2f", data.averageLitersPerTransaction)} L
    
    Report Period: ${_filterState.value.dateFrom} to ${_filterState.value.dateTo}
    """.trimIndent()
}
```

**In UI:**

```kotlin
ExportButton(
    icon = Icons.Default.Email,
    label = "Email Report",
    description = "Send report via email",
    color = Color(0xFF1976D2),
    onClick = {
        val emailContent = viewModel.getEmailShareContent()
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf("manager@company.com"))
            putExtra(Intent.EXTRA_SUBJECT, "Monthly Fuel Report")
            putExtra(Intent.EXTRA_TEXT, emailContent)
        }
        context.startActivity(Intent.createChooser(intent, "Send Email"))
    }
)
```

## 15. Add Report Caching

**In ViewModel:**

```kotlin
private var cachedReports = mutableMapOf<String, ReportFilteredData>()

fun getCachedReport(dateRange: String): ReportFilteredData? {
    return cachedReports[dateRange]
}

fun cacheCurrentReport(key: String) {
    cachedReports[key] = _filteredData.value
}
```

**Add cache clearing:**

```kotlin
fun clearReportCache() {
    cachedReports.clear()
}
```

---

## Summary

These examples show how to:
- ✅ Customize default values
- ✅ Add new filters
- ✅ Change styling and colors
- ✅ Add new export formats
- ✅ Enhance features with progress tracking
- ✅ Integrate with email/sharing
- ✅ Add caching for performance

**For more examples**: Check the main implementation files and modify as needed.

**Need help?** Refer to the full documentation or the code comments in each file.
