# Quick Start Testing Guide

## Build & Run

### Build APK
```bash
gradlew.bat assembleDebug
```

### Install APK
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Run App
- Click app icon on device
- Should launch HomeScreen

---

## Testing TransactionScreenEnhanced

### Access the Screen
1. Tap "Home" tab → see dashboard
2. Tap "Transaction" tab (second tab)
3. TransactionScreenEnhanced loads

### Test Form Validation

**Test 1: Empty Fields**
- Don't fill any fields
- Submit button disabled (grayed out)
- ✅ Expected: Button stays disabled

**Test 2: Missing Driver Name**
- Fill: Vehicle ID, Liters, Destination, Purpose
- Click Submit
- ❌ Expected: Error "Driver name is required"

**Test 3: Invalid Liters**
- Driver: "John"
- Vehicle: "ABC123"
- Liters: "0"
- Destination: "Downtown"
- Purpose: "Delivery"
- Click Submit
- ❌ Expected: Error "Liters must be a positive number"

**Test 4: Too Much Fuel**
- Liters: "600"
- Others: filled
- Click Submit
- ❌ Expected: Error "Cannot pump more than 500 liters..."

### Test Successful Transaction

**Valid Input:**
- Driver Name: "John Doe"
- Vehicle: "VEH001"
- Liters: "50"
- Fuel Type: "Gasoline"
- Destination: "Gas Station"
- Purpose: "Maintenance"
- Passengers: "2"

**Click Submit:**
1. ⏳ Loading state appears
2. ✅ Success state shows:
   - Reference number (e.g., REF-2025120-001)
   - Fuel pumped: 50.00 L
   - New balance: (calculated)
3. Click "Create New Transaction"
   - Form clears
   - Back to Idle state

### Test Error Handling

**Insufficient Balance Error:**
- If wallet has < 500L
- Enter 600L to pump
- Click Submit
- ❌ Expected: "Insufficient fuel" error
- Click "Try Again"
- ✅ Form resets

---

## Testing WalletScreenEnhanced

### Access the Screen
1. Tap "Home" tab
2. Tap "Wallet" tab (third tab)
3. WalletScreenEnhanced loads

### Test Wallet Display

**On Screen Load:**
1. ⏳ Loading indicator appears (briefly)
2. ✅ Wallet displays:
   - Current balance (e.g., "1245.75 L")
   - Max capacity (e.g., "2000.00 L")
   - Usage percentage (e.g., "62.3% Full")
   - Linear progress bar
   - Color based on usage (Yellow >80%, Green <80%)

**Wallet Information:**
- Wallet ID displayed
- Office ID shown
- Created date visible
- Balance and capacity details

### Test Refill Function

**Test 1: Open Refill Dialog**
1. Tap "Refill" button (bottom right of card)
2. ✅ Dialog appears with:
   - Text field for amount
   - Instructions
   - Refill & Cancel buttons

**Test 2: Invalid Refill Amount**
- Leave field empty
- Tap "Refill"
- ❌ Expected: Error "Amount is required"

**Test 3: Non-Numeric Input**
- Enter: "abc"
- Tap "Refill"
- ❌ Expected: Error "Amount must be a valid number"

**Test 4: Zero Amount**
- Enter: "0"
- Tap "Refill"
- ❌ Expected: Error "Amount must be greater than zero"

**Test 5: Too Much Amount**
- Enter: "6000"
- Tap "Refill"
- ❌ Expected: Error "Cannot refill more than 5000 liters..."

### Test Successful Refill

**Valid Refill:**
1. Tap "Refill" button
2. Enter: "100"
3. Tap "Refill"
4. ⏳ Loading state
5. ✅ Dialog closes
6. ✅ Balance updates:
   - Old: 1245.75 L
   - New: 1345.75 L
7. ✅ Progress bar updates
8. ✅ Recent transactions updated

### Test Error Handling

**Refill Error:**
1. Tap "Refill"
2. Enter valid amount
3. Click "Refill"
4. If error occurs:
   - ❌ Error state displayed
   - Tap "Try Again"
   - ✅ Dialog re-opens

---

## Testing Navigation

### Test Tab Navigation

**Home Tab:**
1. Tap "Home" (first tab)
2. ✅ HomeScreen displays
3. Tab highlights green

**Transaction Tab:**
1. Tap "Transaction" (second tab)
2. ✅ TransactionScreenEnhanced displays
3. Tab highlights
4. Form ready

**Wallet Tab:**
1. Tap "Wallet" (third tab)
2. ✅ WalletScreenEnhanced displays
3. Wallet data loads

**Reports Tab:**
1. Tap "Reports" (fourth tab)
2. ✅ ReportScreen displays

### Test Quick Actions from Home

**New Transaction Button:**
1. Home screen
2. Tap "New Transaction" button
3. ✅ Switches to Transaction tab
4. Form ready

**Refill Wallet Button:**
1. Home screen
2. Tap "Refill Wallet" button
3. ✅ Switches to Wallet tab
4. Ready to refill

---

## Testing Edge Cases

### Transaction Edge Cases

| Case | Input | Expected |
|------|-------|----------|
| Min fuel | 0.01 L | ✅ Valid |
| Max fuel | 500 L | ✅ Valid |
| Max+1 fuel | 500.1 L | ❌ Error |
| Special chars in name | "John@Doe" | ✅ Valid |
| Long vehicle ID | "VEH-" + 50 chars | ✅ Valid |
| Decimal liters | "50.5 L" | ✅ Valid |
| Negative liters | "-50" | ❌ Error |

### Wallet Edge Cases

| Case | Input | Expected |
|------|-------|----------|
| Min refill | 0.01 L | ✅ Valid |
| Max refill | 5000 L | ✅ Valid |
| Max+1 refill | 5000.1 L | ❌ Error |
| Decimal amount | "100.5 L" | ✅ Valid |
| Very large | "999999" | ❌ Error |
| Zero | "0" | ❌ Error |
| Negative | "-100" | ❌ Error |

---

## Logging & Debugging

### Enable Timber Logs
Logs display in Logcat with tags:
- `TransactionViewModel`
- `WalletViewModel`
- `FuelHubApp`
- `CreateFuelTransactionUseCase`

### Example Logs
```
D/TransactionViewModel: Transaction created successfully: REF-2025120-001
D/WalletViewModel: Wallet loaded: default-wallet-id
D/WalletViewModel: Wallet refilled: default-wallet-id, added: 100.0 L
```

### Check Database
```bash
adb shell
sqlite3 /data/data/dev.ml.fuelhub/databases/fuelhub.db
.tables  # List all tables
SELECT * FROM fuel_wallet;
SELECT * FROM fuel_transaction;
SELECT * FROM gas_slip;
```

---

## Common Issues & Solutions

### Issue: Form not validating
**Solution**: Check browser console for Timber logs

### Issue: Transaction not saving
**Solution**: 
1. Check wallet balance > liters
2. Verify database initialized
3. Check Logcat for exceptions

### Issue: Wallet not loading
**Solution**:
1. Ensure wallet ID exists
2. Check database connection
3. Verify wallet entity created

### Issue: Dialog won't close after refill
**Solution**:
1. Check if refill amount valid
2. Verify database write succeeded
3. Restart app

---

## Performance Checks

✅ **Good Signs**
- Form fields respond instantly
- Animations are smooth
- Loading indicators appear
- Success/error states fast
- Navigation smooth
- No crashes

❌ **Bad Signs**
- Form lag (>500ms)
- Jittery animations
- No loading indicator
- Slow state updates
- App crashes
- Database locks

---

## Success Criteria

### TransactionScreenEnhanced ✅
- [x] Form accepts all inputs
- [x] Validation works (all 6 rules)
- [x] Submit saves to database
- [x] Success state shows details
- [x] Error handling works
- [x] Fuel type selection works
- [x] Form resets after success
- [x] Navigation works

### WalletScreenEnhanced ✅
- [x] Wallet loads automatically
- [x] Balance displays correctly
- [x] Progress bar updates
- [x] Refill dialog functional
- [x] Refill saves to database
- [x] Balance updates after refill
- [x] Error handling works
- [x] Recent transactions visible

---

## Test Results Template

**Date**: ___________  
**Tester**: ___________  
**Device**: ___________  
**Android Version**: ___________  
**Build**: ___________

### TransactionScreenEnhanced
- [x] Form validation: **PASS** / **FAIL**
- [x] Submit & save: **PASS** / **FAIL**
- [x] Success screen: **PASS** / **FAIL**
- [x] Error handling: **PASS** / **FAIL**
- [x] Navigation: **PASS** / **FAIL**

### WalletScreenEnhanced
- [x] Load wallet: **PASS** / **FAIL**
- [x] Display balance: **PASS** / **FAIL**
- [x] Refill dialog: **PASS** / **FAIL**
- [x] Refill & save: **PASS** / **FAIL**
- [x] Error handling: **PASS** / **FAIL**

### Overall
- [x] Build: **SUCCESS** / **FAILURE**
- [x] Crashes: **NONE** / **YES**
- [x] Performance: **GOOD** / **SLOW**
- [x] UI/UX: **GOOD** / **NEEDS_WORK**

---

**Last Updated**: Dec 20, 2025  
**Status**: Ready for Testing  
