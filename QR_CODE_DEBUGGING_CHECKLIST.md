# QR Code Debugging Checklist

## If "Transaction Not Found" Still Appears After Fix

Follow this step-by-step checklist to identify the exact problem.

### Step 1: Verify Transaction Creation
**Action:** Create a test transaction in the main app  
**Expected Logcat:**
```
💾 SAVING TRANSACTION TO FIRESTORE: FS-XXXXXXXX-XXXX
✓ TRANSACTION SAVED: FS-XXXXXXXX-XXXX
Transaction created successfully: FS-XXXXXXXX-XXXX
```

**If you don't see this:**
- ❌ Transaction creation is failing
- Check for exceptions in logcat
- Verify Firebase is connected
- Check Firestore rules allow writes

---

### Step 2: Verify Gas Slip & QR Code Creation
**Action:** Check that the gas slip was created with QR code  
**Expected Logcat:**
```
📱 QR Code Data Generated: REF:FS-XXXXXXXX-XXXX|PLATE:XXX|DRIVER:xxx|FUEL:DIESEL|LITERS:25.0|DATE:...
✓ Gas slip created successfully
```

**If you don't see this:**
- ❌ Gas slip creation is failing
- QR code is not being generated
- Check gas slip PDF was created
- Verify transaction reference was passed correctly

---

### Step 3: Verify Firestore Contains Transaction
**Action:** Open Firebase Console → Firestore Database → transactions collection  
**Expected:** Document with id = transaction ID, fields include `referenceNumber`

**If empty:**
- ❌ Transaction was never written to Firestore
- Go back to Step 1
- Check if using local-only mode

---

### Step 4: Verify Gas Station Can Read Firestore
**Action:** Open gas station screen, check logs  
**Expected Logcat:**
```
=== GAS STATION SCREEN LOADED ===
Direct Firestore sync initiated from gas station screen
=== LOADING TRANSACTIONS FROM SERVER ===
🔄 FORCE FIRESTORE SYNC - Direct server fetch
✓ Direct Firestore fetch completed: 1 transactions
  → FS-XXXXXXXX-XXXX (PENDING)
```

**If shows "0 transactions":**
- ❌ Firestore returned no data
- Check network connectivity on device
- Check Firestore rules - gas station role might not have read permission
- Check if transactions are in correct Firestore collection

**If shows error:**
- ❌ Firestore fetch failed
- Check network connectivity
- Check Firebase project is same on both apps

---

### Step 5: Verify QR Code Can Be Scanned
**Action:** Open gas station, tap "Scan QR Code", point at gas slip  
**Expected Logcat:**
```
🔍 === QR SCAN RECEIVED ===
📱 Raw scanned data: 'REF:FS-XXXXXXXX-XXXX|PLATE:...'
✓ QR Code parsed successfully
📱 Parsing QR Code Raw Data: 'REF:FS-XXXXXXXX-XXXX|...'
📱 QR Parts after split: [REF:FS-XXXXXXXX-XXXX, ...]
✓ QR Code parsed successfully: REF=FS-XXXXXXXX-XXXX
✓ QR Transaction validation passed
```

**If you see "parsing failed":**
- ❌ QR data is malformed
- QR code is damaged in PDF
- Print a new gas slip

**If you see "validation failed":**
- ❌ One or more required fields missing
- Check the printed gas slip
- Fields needed: REF, PLATE, DRIVER, FUEL, LITERS, DATE

---

### Step 6: Verify Transaction Lookup
**Action:** After QR successfully parses, check next log  
**Expected Logcat:**
```
QR Code parsed: 'FS-XXXXXXXX-XXXX'
Available transactions count: 1
Available transaction refs: ['FS-XXXXXXXX-XXXX']
Transaction found: FS-XXXXXXXX-XXXX
```

**If shows "count: 0":**
- ❌ No transactions loaded from Firestore
- Go back to Step 4
- Firestore sync failed

**If shows count: N but your ref not in list:**
- ❌ Reference number mismatch
- Compare QR data vs Firestore exactly
- Check for extra spaces (QR might have spaces)
- Check case sensitivity

**If reference is in list but "Transaction not found":**
- ❌ String matching algorithm failed
- Check if reference has trailing spaces
- Verify both are trimmed
- Check if reference exactly matches

---

### Step 7: Verify Confirmation Dialog
**Action:** If transaction found, dialog should appear  
**Expected:** Dialog showing transaction details

**If dialog doesn't appear:**
- ❌ showConfirmDialog flag not set
- Check if previous step failed
- Check logcat for any errors

---

## Common Issues & Solutions

| Issue | Symptom | Solution |
|-------|---------|----------|
| **QR not generated** | No "QR Code Data Generated" log | Recreate gas slip after code changes |
| **Firestore not syncing** | "0 transactions" in gas station | Check internet, check Firestore rules |
| **QR malformed** | "parsing failed" when scanning | Print new gas slip |
| **Missing field in QR** | "validation failed" message | Check gas slip has all info printed |
| **Reference mismatch** | In list but "not found" | Check for spaces: `' FS-xxx'` vs `'FS-xxx'` |
| **Cache issue** | Old transaction showing | Restart gas station app |

---

## What To Do If Still Stuck

### Collect These Logs
1. Create a transaction - capture the log output
2. Open gas station - capture the log output
3. Scan QR - capture the complete scan log
4. Include these three parts in your report

### Expected Complete Log Output Should Look Like:

```
[CREATE TRANSACTION]
💾 SAVING TRANSACTION TO FIRESTORE: FS-31492687-6132
✓ TRANSACTION SAVED: FS-31492687-6132
📱 QR Code Data Generated: REF:FS-31492687-6132|PLATE:MD1|DRIVER:ARNEL RUPENTA|FUEL:DIESEL|LITERS:25.0|DATE:2025-12-21 11:41

[OPEN GAS STATION]
=== GAS STATION SCREEN LOADED ===
Direct Firestore sync initiated
🔄 FORCE FIRESTORE SYNC - Direct server fetch
✓ Direct Firestore fetch completed: 1 transactions
  → FS-31492687-6132 (PENDING)

[SCAN QR]
🔍 === QR SCAN RECEIVED ===
📱 Raw scanned data: 'REF:FS-31492687-6132|PLATE:MD1|...'
✓ QR Code parsed successfully
✓ QR Transaction validation passed
Transaction found: FS-31492687-6132
```

If your logs break at any point above, report the section where it breaks.

---

## Quick Self-Check

Before declaring "Transaction Not Found bug", verify:

- [ ] Transaction logged as SAVED? (Step 1)
- [ ] QR Code logged as generated? (Step 2)
- [ ] Firestore Console shows transaction? (Step 3)
- [ ] Gas station loaded transactions count > 0? (Step 4)
- [ ] QR parsed without error? (Step 5)
- [ ] Available transactions count shows your transaction? (Step 6)
- [ ] Reference number matches exactly (no spaces)? (Step 6)

If all above are checked, the fix is working correctly.
If any are unchecked, the issue is at that step - debug that step first.
