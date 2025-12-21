# QR Code Feature - Quick Test Guide

## 5-Minute Quick Start

### Step 1: Build & Run (2 minutes)
```bash
cd c:\Users\gerla\AndroidStudioProjects\FuelHub
gradlew.bat clean build --no-daemon
```

### Step 2: Launch Application
1. Open Android Studio
2. Run the app on emulator or device
3. Navigate to Gas Slip creation screen

### Step 3: Create Gas Slip (2 minutes)
1. Fill in transaction details:
   - Vehicle Plate: CH936B
   - Vehicle Type: AMBULANCE
   - Driver: ALDREN UROT
   - Fuel Type: DIESEL
   - Liters: 25.0
   - Destination: OZAMIS CITY
   - Purpose: TRANSPORT PATIENT

2. Click "Generate Gas Slip" or "Create Slip"

### Step 4: View PDF (1 minute)
1. Open generated PDF
2. Scroll to find "SECURITY CODE" section
3. Verify QR code appears between signatures
4. Check code is centered and clearly visible

### Step 5: Test Scanning (Optional)
1. Point phone camera at QR code in PDF
2. Let camera detect code (1-2 seconds)
3. Tap notification to open data
4. Verify decoded data matches transaction details

---

## Complete Testing Checklist

### Visual Inspection Tests

#### Test 1: QR Code Presence
- [ ] PDF contains QR code
- [ ] Located after signature section
- [ ] Before footer
- [ ] Clearly visible and centered

#### Test 2: Sizing & Alignment
- [ ] QR code is square (100×100pt)
- [ ] Properly centered horizontally
- [ ] Clear spacing around code
- [ ] No overlapping with other content

#### Test 3: Label Visibility
- [ ] "SECURITY CODE" label visible above QR
- [ ] Label is centered
- [ ] Text is readable size (5pt)
- [ ] Color is appropriate (gray)

#### Test 4: Overall Layout
- [ ] All transaction details present
- [ ] Signatures section intact
- [ ] Footer not affected
- [ ] Margins correct
- [ ] Border intact

---

## Scanning Tests

### Test 5: Smartphone Camera
**Device**: iPhone, Android, or other smartphone  
**Procedure**:
1. Open PDF on screen or print it
2. Point camera at QR code
3. Keep still for 1-2 seconds
4. Observe notification appearing

**Expected Result**: Camera recognizes code and offers to scan

**Verification**: ✅ Pass / ❌ Fail

---

### Test 6: Dedicated QR App
**App Options**:
- Google Lens (Android)
- Snapchat
- QR Code Reader
- Similar third-party apps

**Procedure**:
1. Open QR reader app
2. Point at QR code in PDF
3. Tap to scan
4. View decoded data

**Expected Result**: App scans code successfully

**Verification**: ✅ Pass / ❌ Fail

---

### Test 7: Data Verification
**After successful scan**:

Compare decoded data with PDF:

```
Decoded Data (from QR):        Printed Data (on slip):
────────────────────────────────────────────────────────
REF: FS-88605592-9264    →     REF: FS-88605592-9264 ✅
PLATE: CH936B            →     Plate: CH936B ✅
DRIVER: ALDREN UROT      →     Driver: ALDREN UROT ✅
FUEL: DIESEL             →     FUEL ALLOCATION: DIESEL ✅
LITERS: 25.0             →     25.0L ✅
DATE: 2025-12-21 14:30   →     Generated timestamp ✅
```

**All fields match**: ✅ Pass / ❌ Fail

---

## Advanced Testing

### Test 8: Multiple Transactions
**Procedure**:
1. Create 3-5 different gas slips
2. Generate PDFs for each
3. Verify each contains different QR code
4. Scan each QR code
5. Verify data differences reflect transaction differences

**Expected Result**: Each has unique, correct QR code

**Verification**: ✅ Pass / ❌ Fail

---

### Test 9: Edge Cases

#### Test 9a: Long Names
- Driver Name: Very long name (20+ characters)
- Expected: QR code still scans successfully
- Result: ✅ Pass / ❌ Fail

#### Test 9b: Special Characters
- Destination: "OZAMIS CITY - MAIN HQ"
- Expected: QR code encodes correctly
- Result: ✅ Pass / ❌ Fail

#### Test 9c: Various Fuel Types
- Test with: DIESEL, PETROL, GASOLINE, etc.
- Expected: Each encodes correctly
- Result: ✅ Pass / ❌ Fail

#### Test 9d: Different Liter Amounts
- Test with: 5.0, 25.0, 50.0, 100.5 liters
- Expected: All encode correctly
- Result: ✅ Pass / ❌ Fail

---

### Test 10: Print Quality
**Procedure**:
1. Print PDF to physical paper/receipt
2. Let ink dry completely (1 minute)
3. Scan printed QR code with phone
4. Verify scan success rate

**Expected Result**: 
- Prints clearly and darkly
- Scans reliably from printed version
- No smudging or quality loss

**Verification**: ✅ Pass / ❌ Fail

---

### Test 11: PDF Compatibility
Test opening PDF in various viewers:

- [ ] Adobe Reader - opens and displays correctly
- [ ] Chrome/Firefox - web viewer shows QR
- [ ] Mobile PDF app - mobile viewing works
- [ ] Print preview - print preview shows QR
- [ ] Email attachment - opens in mail app

---

### Test 12: Performance

#### Build Time
- [ ] Gradle build completes without errors
- [ ] Build time reasonable (<30 seconds)

#### PDF Generation Time
- [ ] Gas slip PDF generates in <5 seconds
- [ ] QR code generation is fast (<200ms)
- [ ] No noticeable lag or slowdown

#### Memory Usage
- [ ] No memory leaks
- [ ] Memory usage returns to normal after PDF generation
- [ ] Can generate multiple PDFs in sequence

---

## Troubleshooting Guide

### Issue: QR Code Not Visible in PDF

**Check**:
- [ ] PDF opens without errors
- [ ] Page scrolls to show full content
- [ ] Zoom in to verify QR code is there
- [ ] Check if Adobe Reader is rendering correctly

**Solutions**:
1. Try different PDF viewer
2. Regenerate PDF
3. Check system resources (memory/disk space)

---

### Issue: QR Code Won't Scan

**Check**:
- [ ] Camera is clean and focused
- [ ] QR code is not blurry or distorted
- [ ] Lighting is adequate
- [ ] Phone camera working (test with other QR code)

**Solutions**:
1. Try different scanning app
2. Try different phone/device
3. Check PDF print quality (if printed)
4. Zoom in on PDF and try again

---

### Issue: Scanned Data is Incorrect

**Check**:
- [ ] All data fields match transaction
- [ ] Date format is correct (YYYY-MM-DD HH:MM)
- [ ] No special characters mangling

**Solutions**:
1. Regenerate PDF
2. Try scanning again
3. Check if QR code generation has error (check logs)
4. Verify transaction details in system

---

### Issue: PDF Generation Fails

**Check**:
- [ ] Build was successful
- [ ] No compilation errors
- [ ] System has disk space
- [ ] Permissions are correct

**Solutions**:
1. Clean build: `gradlew clean build`
2. Check Timber logs for errors
3. Verify file system permissions
4. Restart application

---

## Test Report Template

### Gas Slip QR Code Testing Report

**Date**: _______________  
**Tester**: _______________  
**Device**: _______________  

### Test Results

| Test # | Test Name | Result | Notes |
|--------|-----------|--------|-------|
| 1 | QR Code Presence | ✅/❌ | _____________ |
| 2 | Sizing & Alignment | ✅/❌ | _____________ |
| 3 | Label Visibility | ✅/❌ | _____________ |
| 4 | Overall Layout | ✅/❌ | _____________ |
| 5 | Smartphone Camera | ✅/❌ | _____________ |
| 6 | Dedicated QR App | ✅/❌ | _____________ |
| 7 | Data Verification | ✅/❌ | _____________ |
| 8 | Multiple Transactions | ✅/❌ | _____________ |
| 9a | Long Names | ✅/❌ | _____________ |
| 9b | Special Characters | ✅/❌ | _____________ |
| 9c | Various Fuel Types | ✅/❌ | _____________ |
| 9d | Different Amounts | ✅/❌ | _____________ |
| 10 | Print Quality | ✅/❌ | _____________ |
| 11 | PDF Compatibility | ✅/❌ | _____________ |
| 12 | Performance | ✅/❌ | _____________ |

### Overall Result

**Status**: 
- [ ] ✅ ALL TESTS PASSED - Ready for production
- [ ] ⚠️ MINOR ISSUES - Minor cosmetic issues found
- [ ] ❌ CRITICAL ISSUES - Functional problems found

**Issues Found**:
```
_________________________________________________________________
_________________________________________________________________
_________________________________________________________________
```

**Recommendations**:
```
_________________________________________________________________
_________________________________________________________________
```

**Tester Signature**: ____________________  **Date**: ____________________

---

## Quick Verification (30 seconds)

If you have limited time:

1. Build app: ✅
2. Create gas slip: ✅
3. Generate PDF: ✅
4. Open PDF and look for "SECURITY CODE": ✅
5. See QR code below label: ✅
6. Take phone photo - camera recognizes code: ✅

**If all 6 steps pass → Feature is working correctly ✅**

---

## Continuous Testing

### Post-Deployment Monitoring
- Monitor error logs for QR code errors
- Check Timber logs daily first week
- Verify user feedback on scanning
- Monitor PDF generation times

### Regular Testing
- Weekly: Generate sample PDFs and scan
- Monthly: Test with various devices
- Quarterly: Test with latest OS versions

---

**Last Updated**: December 21, 2025  
**Feature Status**: Ready for Testing  
**Estimated Testing Time**: 30-60 minutes
