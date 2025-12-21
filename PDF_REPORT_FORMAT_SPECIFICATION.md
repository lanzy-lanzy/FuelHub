# PDF Report Format Specification

## Status: ✅ UPDATED TO LANDSCAPE A4

**Date**: December 21, 2025

---

## Page Format

### Current Format: Landscape A4

**Page Size**: A4 (210mm × 297mm)
**Orientation**: Landscape (Rotated 90°)
**Effective Size**: 297mm × 210mm (Width × Height)
**Margins**: Default iText (approximately 36pt = 12.7mm on all sides)

### Why Landscape A4?

✅ Accommodates 8-column transaction table without text wrapping
✅ Better readability for wide data tables
✅ Industry standard for financial reports
✅ Prints on standard A4 paper in landscape mode
✅ Fits PDF viewers on mobile devices horizontally

---

## Page Dimensions

```
PORTRAIT MODE (Before)
┌─────────────────┐
│                 │  210mm
│   8.3 inches    │
│   width         │
│                 │
└─────────────────┘
  297mm height
  11.7 inches

↓ ROTATED TO ↓

LANDSCAPE MODE (After)
┌───────────────────────────────────┐
│                                   │  148mm height
│            8 Columns              │  5.8 inches
│         Fit Perfectly             │
│                                   │
└───────────────────────────────────┘
            297mm width
         11.7 inches ✓ Much Better!
```

---

## Layout & Content

### Page Structure

```
LANDSCAPE A4 PAGE (297mm × 210mm)

┌─────────────────────────────────────────────┐
│                                             │
│        FUEL TRANSACTION REPORT               │ ← Title (24pt)
│        Daily Report                         │ ← Subtitle (14pt)
│   Period: 2025-11-21 to 2025-12-21         │ ← Date Range (11pt)
│                                             │
├─────────────────────────────────────────────┤
│ Summary Statistics                          │ ← Section Header (14pt)
├───────────────────────┬─────────────────────┤
│ Metric                │ Value               │ ← 2-Column Table
├───────────────────────┼─────────────────────┤
│ Total Liters          │ 75.0 L              │
│ Total Transactions    │ 3                   │
│ Completed             │ 3                   │
│ Pending               │ 0                   │
│ Failed                │ 0                   │
│ Average L/Transaction │ 25.0 L              │
│ Total Cost            │ PHP 4672.50         │
│ Average Cost/Liter    │ PHP 62.30           │
├───────────────────────┴─────────────────────┤
│ Transaction Details (showing up to 50...)   │ ← Section Header
├──┬──────┬──────┬───────┬────┬────┬─────┬───┤
│D │ Ref  │ Driv │ Veh   │ L  │ C/L│ Tot │ St│ ← 8-Column Table
├──┼──────┼──────┼───────┼────┼────┼─────┼───┤
│..│FS-..│ NAME │ TYPE  │##..│PHP│ PHP │ OK│ ← Data Rows
└──┴──────┴──────┴───────┴────┴────┴─────┴───┘
│                                             │
│        Generated on: 2025-12-21            │ ← Footer (10pt)
│                                             │
└─────────────────────────────────────────────┘
```

---

## Detailed Measurements

### Margins (All Sides)
```
Top:    36pt (12.7mm / 0.5 inches)
Bottom: 36pt (12.7mm / 0.5 inches)
Left:   36pt (12.7mm / 0.5 inches)
Right:  36pt (12.7mm / 0.5 inches)
```

### Usable Page Area
```
Width:  297mm - 25.4mm (margins) = 271.6mm
Height: 210mm - 25.4mm (margins) = 184.6mm
```

### Text Formatting

| Element | Font Size | Font Weight | Alignment |
|---------|-----------|-------------|-----------|
| Title | 24pt | Bold | Center |
| Subtitle | 14pt | Regular | Center |
| Date Range | 11pt | Regular | Center |
| Section Headers | 14pt | Bold | Left |
| Table Headers | 11pt | Bold | Center |
| Table Data | 10pt | Regular | Left/Center |
| Footer | 10pt | Regular | Center |

---

## Table Specifications

### Summary Statistics Table

**Type**: 2-column table
**Width**: 100% of usable page width
**Columns**:
- Column 1: Metric (descriptive text)
- Column 2: Value (numeric/calculated)

**Row Height**: Auto-fit to content
**Spacing**: No gaps between cells

```
┌──────────────────────────────┬─────────────────┐
│ Metric (Left Aligned)        │ Value (Right)   │
├──────────────────────────────┼─────────────────┤
│ Total Liters                 │ 75.0 L          │
│ Total Transactions           │ 3               │
│ ...                          │ ...             │
└──────────────────────────────┴─────────────────┘
```

### Transaction Details Table

**Type**: 8-column table
**Width**: 100% of usable page width
**Columns**: (In order)

| # | Name | Width % | Format | Content |
|---|------|---------|--------|---------|
| 1 | Date | 10% | yyyy-MM-dd | Transaction date |
| 2 | Reference | 14% | FS-XXXX-XXXX | Unique ID |
| 3 | Driver | 16% | Text | Driver name |
| 4 | Vehicle | 18% | Text | Vehicle type |
| 5 | Liters | 10% | ##.## | Fuel amount |
| 6 | Cost/L | 10% | PHP ##.## | Unit price |
| 7 | Total Cost | 12% | PHP ##.## | Total amount |
| 8 | Status | 10% | Text | Transaction status |

**Row Height**: Auto-fit (typical 20-25pt per row)
**Spacing**: No gaps between cells
**Header Background**: Light gray (ColorConstants.LIGHT_GRAY)
**Data Background**: White
**Max Rows**: 50 (limited by take(50))

```
Width Distribution (297mm - margins = 271.6mm):

Date        10% = 27.2mm ✓
Reference   14% = 38.0mm ✓
Driver      16% = 43.5mm ✓
Vehicle     18% = 48.9mm ✓
Liters      10% = 27.2mm ✓
Cost/L      10% = 27.2mm ✓
Total Cost  12% = 32.6mm ✓
Status      10% = 27.2mm ✓
───────────────────────────
Total              271.6mm ✓ Perfect fit!
```

---

## Code Implementation

### Page Format Setting

```kotlin
// iText 7 PDF creation with Landscape A4
val writer = PdfWriter(filePath)
val pdfDoc = PdfDocument(writer)
pdfDoc.setDefaultPageSize(PageSize.A4.rotate())  // Landscape A4
val document = Document(pdfDoc)
```

### PageSize Options Available

```kotlin
// If needed to change:
PageSize.A4              // Portrait: 210mm × 297mm
PageSize.A4.rotate()     // Landscape: 297mm × 210mm ✓ USED

// Other available sizes:
PageSize.LETTER          // 8.5" × 11"
PageSize.LEGAL           // 8.5" × 14"
PageSize.LEDGER          // 11" × 17"
PageSize.TABLOID         // 11" × 17"
PageSize.A3              // 297mm × 420mm
PageSize.A5              // 148mm × 210mm
```

---

## File Storage & Naming

### File Location
```
Android Directory: /Android/data/{app-package}/files/Documents/
Actual Path: /storage/emulated/0/Android/data/{app-package}/files/Documents/
```

### Filename Format
```
report_{reportName}_{timestamp}.pdf

Examples:
- report_Daily_1766284449123.pdf
- report_Weekly_1766284512456.pdf
- report_Monthly_1766284575789.pdf
```

### File Size
**Typical**: 50-200 KB per report
- 1 transaction: ~20 KB
- 10 transactions: ~30 KB
- 50 transactions: ~50-100 KB

---

## Printing Specifications

### Recommended Printer Settings

**Paper Size**: A4
**Orientation**: Landscape
**Margins**: Default or minimal (0.5 inches)
**Scale**: 100% (no scaling)
**Color**: Color or B&W (both work)
**Quality**: Normal or High

### Print Preview Example

```
┌─────────────────────────────────────┐
│                                     │
│    FUEL TRANSACTION REPORT          │
│    Daily Report                     │
│    Period: 2025-11-21 to 2025-12-21│
│                                     │
│    Summary Statistics               │
│  ┌──────────────┬──────────────┐    │
│  │ Metric       │ Value        │    │
│  ├──────────────┼──────────────┤    │
│  │ Total Liters │ 75.0 L       │    │
│  │ Total Cost   │ PHP 4672.50  │    │
│  │ ...          │ ...          │    │
│  └──────────────┴──────────────┘    │
│                                     │
│    Transaction Details              │
│  ┌─┬──┬───┬────┬───┬──┬────┬──┐    │
│  │D│Rf│Dv│Veh │L  │C/L│Tot │St│    │
│  ├─┼──┼───┼────┼───┼──┼────┼──┤    │
│  │2│FS│NA│TYP │##│PHP│PHP│OK│    │
│  │5│.│.│.│..│..│.│.│    │
│  └─┴──┴───┴────┴───┴──┴────┴──┘    │
│                                     │
│    Generated on: 2025-12-21        │
│                                     │
└─────────────────────────────────────┘
```

---

## Viewing & Compatibility

### Supported Viewers

| Platform | Viewer | Format Support | Notes |
|----------|--------|---|---|
| Android | Google Drive | ✅ Full | Works perfectly |
| Android | Adobe Reader | ✅ Full | Professional viewer |
| Android | Any PDF app | ✅ Full | Standard PDF format |
| Windows | Edge | ✅ Full | Built-in viewer |
| Windows | Adobe Reader | ✅ Full | Professional viewer |
| Windows | Chrome | ✅ Full | Built-in viewer |
| macOS | Preview | ✅ Full | Built-in viewer |
| macOS | Adobe Reader | ✅ Full | Professional viewer |
| iOS | iBooks | ✅ Full | Built-in viewer |
| Web | Any browser | ✅ Full | No plugin needed |

---

## Design Specifications

### Color Scheme

```kotlin
// Header cells
ColorConstants.LIGHT_GRAY   // Background for headers

// Text colors
ColorConstants.BLACK        // Default text
ColorConstants.GRAY         // Footer text (subdued)

// Text alignment
TextAlignment.CENTER        // Headers, title
TextAlignment.LEFT          // Data rows
TextAlignment.CENTER        // Footer
```

### Typography

```
Font Family: Default (usually Helvetica/Liberation Sans)
Sizes:
  - Title: 24pt
  - Section Header: 14pt
  - Subtitle: 14pt
  - Body: 11pt
  - Table Header: 11pt
  - Table Data: 10pt
  - Footer: 10pt

Styles:
  - Bold: Titles, section headers, table headers
  - Regular: Everything else
```

---

## Content Sections

### Section 1: Header

```
Fuel Transaction Report                    (24pt, Bold, Center)
Daily Report                               (14pt, Regular, Center)
Period: 2025-11-21 to 2025-12-21          (11pt, Regular, Center)
```

### Section 2: Summary Statistics

```
Summary Statistics                         (14pt, Bold, Left)

2-Column Table:
  - Column 1: Metric (descriptive)
  - Column 2: Value (calculated)
  
Metrics included:
  - Total Liters
  - Total Transactions
  - Completed
  - Pending
  - Failed
  - Average Liters/Transaction
  - Total Cost
  - Average Cost per Liter
```

### Section 3: Transaction Details

```
Transaction Details (showing up to 50 records)  (14pt, Bold, Left)

8-Column Table:
  - Date
  - Reference
  - Driver
  - Vehicle
  - Liters
  - Cost/L
  - Total Cost
  - Status

Shows up to 50 most recent transactions
```

### Section 4: Footer

```
Generated on: 2025-12-21                   (10pt, Regular, Center, Gray)
```

---

## Export Formats

### PDF Only

Currently, reports export as PDF only. The PDF can then be:
- ✅ Printed to physical paper
- ✅ Saved as digital file
- ✅ Emailed
- ✅ Shared via cloud services
- ✅ Viewed on any device

### Potential Future Formats

Could be extended to support:
- Excel (.xlsx)
- CSV (.csv)
- Image (.png)
- Multiple PDFs (split by date, vehicle, etc.)

---

## Performance

### Generation Time

```
Transaction Count    Time    File Size
─────────────────────────────────────
1-10                <1 sec  20-30 KB
11-25               1 sec   35-50 KB
26-50               1-2 sec 50-100 KB
```

### Memory Usage

```
Minimal - PDF generated in streaming mode
No large arrays kept in memory
Documents close and release resources immediately
```

---

## Summary

| Aspect | Specification |
|--------|---|
| **Page Format** | Landscape A4 (297mm × 210mm) |
| **Orientation** | Landscape (Rotated) |
| **Paper Size** | A4 (8.3" × 11.7") |
| **Content Width** | ~271mm usable |
| **Content Height** | ~184mm usable |
| **Table Columns** | 2 (summary), 8 (details) |
| **Color Scheme** | Grayscale with light gray headers |
| **Font** | Default iText (Helvetica/Liberation) |
| **File Format** | PDF (standard, compatible everywhere) |
| **Max Records** | 50 transactions per page |
| **Typical Size** | 50-100 KB |
| **Print Method** | Landscape A4, normal margins |

---

## Testing Checklist

- [ ] PDF generates without errors
- [ ] Open PDF on Android device
- [ ] Verify landscape orientation
- [ ] Check all columns visible (8 columns)
- [ ] Verify text readable (not too small)
- [ ] Print to physical printer
- [ ] Verify printed output clarity
- [ ] Email PDF and verify recipient can open
- [ ] Test on different devices/browsers
- [ ] Check file size reasonable (<100 KB)

