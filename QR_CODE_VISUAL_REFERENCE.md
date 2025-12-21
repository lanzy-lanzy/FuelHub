# QR Code Visual Reference & Positioning Guide

## PDF Page Layout (3.5" x 5" Index Card)

### Detailed Measurements

```
┌─────────────────────────────────────────────────────────┐
│  10pt margin on all sides                               │
│  (0.14" = ~10 points)                                   │
│                                                          │
│  ┌───────────────────────────────────────────────────┐  │
│  │                                                   │  │
│  │        FUEL DISPENSING SLIP                      │  │
│  │        (11pt, Helvetica Bold, Centered)         │  │
│  │                                                   │  │
│  │        REF: FS-88605592-9264                      │  │
│  │        (7pt, Gray, Centered)                     │  │
│  │                                                   │  │
│  │  ┌───────────────────────────────────────────┐  │  │
│  │  │     FUEL ALLOCATION (8pt)                 │  │  │
│  │  │                                           │  │  │
│  │  │     DIESEL              25.0L             │  │  │
│  │  │  (12pt, Bold)        (14pt, Bold)         │  │  │
│  │  │                                           │  │  │
│  │  └───────────────────────────────────────────┘  │  │
│  │                                                   │  │
│  │  Plate:       CH936B                             │  │
│  │  Vehicle:     AMBULANCE PTV PCSO                │  │
│  │  Driver:      ALDREN UROT                        │  │
│  │  Purpose:     TRANSPORT PATIENT                │  │
│  │  Destination: OZAMIS CITY                       │  │
│  │                                                   │  │
│  │  Authorized By:        Recipient Signature:     │  │
│  │  [Signature]           __________________       │  │
│  │                        DEC 21, 2025             │  │
│  │                                                   │  │
│  │     ╔═══════════════════════╗                   │  │
│  │     ║    SECURITY CODE      ║ ← Label (5pt Gray)│  │
│  │     ║                       ║                    │  │
│  │     ║     ┌─────────────┐   ║                    │  │
│  │     ║     │             │   ║                    │  │
│  │     ║     │  [QR CODE]  │   ║ ← 100x100pt      │  │
│  │     ║     │             │   ║                    │  │
│  │     ║     └─────────────┘   ║                    │  │
│  │     ║                       ║                    │  │
│  │     ╚═══════════════════════╝                   │  │
│  │                                                   │  │
│  │          FuelHub System • Official Receipt        │  │
│  │          (5pt, Gray, Centered)                   │  │
│  │                                                   │  │
│  └───────────────────────────────────────────────────┘  │
│                                                          │
│  ◢◢◢◢ Outer Border (1pt Black)                         │
│                                                          │
└─────────────────────────────────────────────────────────┘

Page Size: 252pt wide × 360pt tall (3.5" × 5")
Margin: 10pt all sides (effective: 232pt × 340pt)
```

---

## Component Positioning

### Header Section (Top)
- Logo: 40×40pt, centered
- Title: "FUEL DISPENSING SLIP" - 11pt bold, centered, 2pt margin top
- Reference: "REF: FS-88605592-9264" - 7pt gray, centered, 8pt margin bottom

### Fuel Allocation Box (Highlighted)
- Full width with 1pt border and light gray background
- Two columns: Fuel Type | Liters
- Fuel Type: 12pt bold, centered
- Liters: 14pt bold, centered
- 6pt padding inside box, 8pt margin bottom

### Details Grid Section
- Two columns: Labels (0.8 width) | Values (2 width)
- 8pt font for all
- 2pt padding per cell
- Subtle alternating row background (98% gray)
- 8pt margin bottom

### Signature Section (2-Column Layout)
- **Left Column (Authorized By)**
  - Label: "Authorized By:" - 6pt gray
  - Content: 75×40pt signature image or line
  - 4pt padding, no border
  
- **Right Column (Recipient)**
  - Label: "Recipient Signature:" - 6pt gray, right aligned
  - Signature line: "________________" - 6pt, right aligned
  - Date: "DEC 21, 2025" - 6pt, right aligned
  - 4pt padding, no border
  - Vertical alignment: bottom
  - 10pt margin top

### QR Code Section (NEW - Between Signatures and Footer)
- Label: "SECURITY CODE" - 5pt gray, centered, 4pt margin bottom
- QR Image: 100×100pt, centered, 4pt margin top
- Encoded Data: Transaction details in pipe-separated format

### Footer Section (Bottom)
- "FuelHub System • Official Receipt" - 5pt gray, centered
- Fixed position at 15pt from bottom
- Full width minus 20pt margins

---

## Exact Positioning Code Reference

### From GasSlipPdfGenerator.kt

#### Signature Table Creation (Line 158-160)
```kotlin
val signatureTable = Table(UnitValue.createPercentArray(floatArrayOf(1f, 1f)))
    .setWidth(UnitValue.createPercentValue(100f))
    .setMarginTop(10f)  // 10pt space from previous section
```

#### QR Code Section (Line 196-218)
```kotlin
// QR Code Label
val qrLabel = Paragraph("SECURITY CODE")
    .setFont(regularFont)
    .setFontSize(5f)  // 5pt font
    .setTextAlignment(TextAlignment.CENTER)
    .setFontColor(DeviceGray.GRAY)
    .setMarginBottom(4f)  // 4pt space before image

// QR Image
qrImage.setWidth(100f)  // 100 points wide
qrImage.setHeight(100f)  // 100 points tall
qrImage.setHorizontalAlignment(HorizontalAlignment.CENTER)
qrImage.setMarginTop(4f)  // 4pt space after label
```

---

## Visual Mockup - Actual PDF

```
╔══════════════════════════════════════════════════════════╗
║                                                          ║
║             [MDRRMO LOGO - 40×40pt]                     ║
║                                                          ║
║        FUEL DISPENSING SLIP                             ║
║     REF: FS-88605592-9264                                ║
║                                                          ║
║  ╔────────────────────────────────────────────────────╗ ║
║  ║           FUEL ALLOCATION                          ║ ║
║  ║    DIESEL                              25.0L       ║ ║
║  ╚────────────────────────────────────────────────────╝ ║
║                                                          ║
║  Plate:          CH936B                                 ║
║  Vehicle:        AMBULANCE PTV PCSO                     ║
║  Driver:         ALDREN UROT                            ║
║  Purpose:        TRANSPORT PATIENT                     ║
║  Destination:    OZAMIS CITY                            ║
║                                                          ║
║  Authorized By:              Recipient Signature:       ║
║                                                          ║
║  [SIGNATURE]                 __________________         ║
║                              DEC 21, 2025               ║
║                                                          ║
║                  SECURITY CODE                          ║
║                  ┌──────────────┐                       ║
║                  │              │                       ║
║                  │   [QR CODE]  │  100×100pt            ║
║                  │              │                       ║
║                  └──────────────┘                       ║
║                                                          ║
║         FuelHub System • Official Receipt               ║
║                                                          ║
║  ─────────────────────────────────────────────────────  ║
║              1pt Black Border                            ║
╚══════════════════════════════════════════════════════════╝

Dimensions: 3.5" wide × 5" tall (252pt × 360pt)
```

---

## QR Code Specifications

### Generated Bitmap
- Size: 200×200 pixels (before PDF scaling)
- Format: PNG with 100% compression quality
- Color: Black (#000000) and White (#FFFFFF)
- Error Correction: Level H (HIGH)

### PDF Embedded Size
- Size: 100×100 points
- Margin Top: 4pt
- Alignment: Centered horizontal
- Scale: ~50% of original bitmap

### Scannable Data
```
REF:FS-88605592-9264|PLATE:CH936B|DRIVER:ALDREN UROT|FUEL:DIESEL|LITERS:25.0|DATE:2025-12-21 14:30
```

---

## Spacing Reference (in Points)

```
Page Margin:              10pt (all sides)
Effective Width:          232pt
Effective Height:         340pt

Header Logo:              40×40pt
Title Margin Bottom:      0pt
Ref Margin Bottom:        8pt
Fuel Box Margin Bottom:   8pt
Details Box Margin Bottom: 8pt

Signature Table Top:      10pt
QR Label Margin Bottom:   4pt
QR Image Height:          100pt
QR Image Margin Top:      4pt

Footer Fixed Position:    15pt from bottom
```

---

## Color Scheme

| Element | Color | RGB | Hex |
|---------|-------|-----|-----|
| Text (Default) | Black | (0, 0, 0) | #000000 |
| Gray Text | DeviceGray.GRAY | (128, 128, 128) | #808080 |
| Gray Background | DeviceGray(0.98f) | (250, 250, 250) | #FAFAFA |
| Boxes | DeviceGray(0.95f) | (242, 242, 242) | #F2F2F2 |
| Borders | Black | (0, 0, 0) | #000000 |
| QR Code | Black/White | (0,0,0)/(255,255,255) | #000000/#FFFFFF |

---

## Font Specifications

| Element | Font | Size | Weight | Color | Alignment |
|---------|------|------|--------|-------|-----------|
| Title | Helvetica | 11pt | Bold | Black | Center |
| Reference | Helvetica | 7pt | Regular | Gray | Center |
| Section Headers | Helvetica | 8pt | Bold | Black | Left/Center |
| Body Text | Helvetica | 8pt | Regular | Black | Left/Right |
| Labels | Helvetica | 6pt | Bold | Black | Left |
| Gray Labels | Helvetica | 6pt | Regular | Gray | Left/Right |
| QR Label | Helvetica | 5pt | Regular | Gray | Center |
| Footer | Helvetica | 5pt | Regular | Gray | Center |

---

## Comparison: Before & After

### Before QR Code Feature
```
Authorized By: \_____  Recipient: _____
               DEC 21  DEC 21
```

### After QR Code Feature
```
Authorized By: \_____  Recipient: _____
               DEC 21  DEC 21

        SECURITY CODE
        ┌─────────────┐
        │  [QR CODE]  │
        └─────────────┘
```

---

## Print Quality Considerations

### DPI Conversion
- 72 DPI (PDF standard) = 1 point
- QR Code: 100×100 pt = 100×100 px at 72 DPI
- Print Quality: Excellent at standard print resolutions

### Printer Compatibility
- ✅ Works with all PDF-capable printers
- ✅ Works with mobile thermal printers
- ✅ Works with standard office printers
- ✅ Works with receipt printers

### Paper Size
- Designed for: 3.5" × 5" index cards
- Margins: Proper clearance on all sides
- Scaling: Works with standard A4 or letter paper

---

## Mobile Scanning Recommendations

### Optimal Scanning Distance
- **Minimum**: 2-3 inches from camera
- **Maximum**: 12-18 inches from camera
- **Optimal**: 6-8 inches from camera

### Lighting Conditions
- ✅ Works in bright light
- ✅ Works in dim light (with flash)
- ✅ Works in various angles
- ⚠ May struggle in very dark conditions without flash

### QR Code Size
- 100×100 pt QR code is optimal for mobile scanning
- Error correction Level H handles partial obstruction
- Recommended minimum size: 80×80 pt
- Current implementation: 100×100 pt ✅

---

## Accessibility Notes

### For Visually Impaired Users
- QR code contains machine-readable data
- Printed slip still has human-readable text
- All information is duplicated (both visible and in QR)
- No accessibility barriers introduced

### For Different Devices
- Works on iOS (native camera app)
- Works on Android (native camera app)
- Works with dedicated QR readers
- No platform limitations

---

**Visual Reference Last Updated**: December 21, 2025  
**PDF Rendering Engine**: iText 7.2.5  
**QR Code Library**: ZXing 3.5.1  
**Status**: ✅ FINAL
