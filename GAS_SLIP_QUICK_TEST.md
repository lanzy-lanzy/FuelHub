# Gas Slip Quick Test Guide

## Quick Test (2 minutes)

### Step 1: Create Transaction
1. Open app
2. Tap **Transaction** tab
3. Select a vehicle
4. Enter values:
   - Liters: `25`
   - Destination: `District Office`
   - Purpose: `Emergency Relief`
5. Tap **Submit Transaction**
6. Verify success dialog appears ✅

### Step 2: Print Gas Slip
1. In success dialog, tap **Print Gas Slip** button
2. Verify PDF opens/prints ✅
3. Tap **Create New Transaction** button to dismiss

### Step 3: View Gas Slip
1. Tap **Gas Slips** tab at bottom
2. Verify the newly created slip appears ✅
3. Tap the card to expand and see details ✅

### Step 4: Test Filters
1. While viewing slips, tap **PENDING** filter
2. Verify new slip is shown ✅
3. Tap **USED** filter
4. Verify list is empty ✅
5. Tap **ALL** to see all

## Expected Results

| Test | Expected | Status |
|------|----------|--------|
| Transaction submitted | Success dialog appears | ✅ |
| Reference number | Matches displayed number | ✅ |
| Balance updated | Shows new balance | ✅ |
| Print button | Generates PDF | ✅ |
| Gas slip visible | Appears in list | ✅ |
| Slip details | Vehicle & fuel info correct | ✅ |
| Filter PENDING | Slip visible | ✅ |
| Filter USED | Slip hidden | ✅ |
| Expand card | Shows all details | ✅ |

## Logcat Commands

To monitor gas slip operations in Android Studio:

1. **Show all logs**: Don't filter
2. **Gas slip logs only**:
   ```
   Search: "gas slip" OR "GasSlip" OR "Fetching all"
   ```
3. **Error logs**:
   ```
   Search: "error" (set to Error level)
   ```

## What You Should See

✅ Success dialog after transaction
✅ PDF print dialog opens
✅ Gas slip in list with vehicle plate number
✅ Expandable card shows full details
✅ Filters work correctly
✅ No crashes or warnings

## What Would Be Wrong

❌ Success dialog but no slip in list
- Run refresh (reload app or tap refresh button)
- Check Firestore console for documents

❌ Slip appears with wrong data
- Check CreateFuelTransactionUseCase is setting correct values
- Verify Firestore documents have correct fields

❌ Print button crashes
- Check PdfPrintManager is initialized
- Verify android.permission.WRITE_EXTERNAL_STORAGE in manifest

❌ Filter doesn't work
- Check isUsed field in Firestore
- Verify data conversion in toGasSlip()

## Debug Tips

1. **Restart app** if data not updating
2. **Pull down to refresh** on Gas Slips screen
3. **Clear app data** if getting stuck (Settings → Apps → FuelHub → Storage → Clear)
4. **Check internet connection** - Firestore needs connectivity
5. **Check Firebase Console** to verify documents are actually saved

## Key Fields to Verify in Firestore

When you open a gas slip document in Firestore Console:

```
✅ Should have all these fields:
- id: "uuid-string"
- referenceNumber: "FS-timestamp-number" 
- driverName: "name from vehicle"
- vehicleType: "e.g., Sedan, Truck"
- vehiclePlateNumber: "ABC-123"
- destination: "your input"
- tripPurpose: "your input"
- fuelType: "GASOLINE" or "DIESEL"
- litersToPump: 25.0 (your input)
- transactionDate: "2025-12-20 15:30:00"
- isUsed: false
- mdrrmoOfficeName: "MDRRMO Office"
```

## If Gas Slip Still Doesn't Show

1. **Verify data is in Firestore**
   - Firebase Console → Firestore
   - Click "gas_slips" collection
   - Should see document with reference number

2. **Check for errors in Logcat**
   - Filter by package: `dev.ml.fuelhub`
   - Search for: "error" or "Error"
   - Look for "Missing required fields"

3. **Force refresh**
   - Navigate away from Gas Slips tab
   - Tap refresh button (circular arrow)
   - Navigate back to Gas Slips tab

4. **Restart app**
   - Kill app process
   - Reopen app
   - Tap Gas Slips tab
   - ViewModel init should load data

5. **Check Firestore Rules**
   - Firebase Console → Firestore → Rules
   - Should allow read access to gas_slips collection
   - If blocked: all reads will fail silently

## Success Indicators

When everything is working:

1. ✅ Logcat shows: `"Fetching all gas slips from Firestore"`
2. ✅ Logcat shows: `"One-time fetch returned X gas slips"`
3. ✅ UI transitions to Gas Slips screen
4. ✅ Gas slip card appears in list
5. ✅ No error messages in Logcat
6. ✅ Tap card to expand and see full details
7. ✅ Filters work (PENDING/USED/ALL)

---

**If all tests pass: Gas Slip feature is working correctly! 🎉**
