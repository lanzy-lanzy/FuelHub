# Quick Gas Station Operator Setup (5 Minutes)

## Create a Gas Station Operator Account

### Step 1: Open Firebase Console
Visit: https://console.firebase.google.com/

Select your **FuelHub** project

### Step 2: Create Authentication User
1. Go to **Authentication** (left sidebar)
2. Click **Add User** (blue button)
3. Enter:
   - **Email:** `operator@gasstation.com`
   - **Password:** `GasStation123`
4. Click **Create User**
5. **Copy the User ID** (format: abc123def456...)

### Step 3: Create Firestore User Document
1. Go to **Firestore Database** (left sidebar)
2. Click on **users** collection
3. Click **+ Add Document**
4. In **Document ID** field, paste the User ID from Step 2
5. Click **Auto ID** dropdown and paste your User ID
6. Add these fields by clicking **Add field**:

| Field | Type | Value |
|-------|------|-------|
| `id` | String | (paste User ID) |
| `username` | String | `station-01` |
| `email` | String | `operator@gasstation.com` |
| `fullName` | String | `John Operator` |
| `role` | String | `GAS_STATION` |
| `officeId` | String | `location-1` |
| `isActive` | Boolean | `true` |
| `createdAt` | String | `2025-12-21T10:00:00` |

7. Click **Save**

---

## Test Login

### On Your App
1. Open FuelHub app
2. Click **Sign In**
3. Enter credentials:
   - Email: `operator@gasstation.com`
   - Password: `GasStation123`
4. Click **Sign In**

### Expected Result
✅ Should navigate directly to **Gas Station Screen**

---

## Verify Setup

### Check Firestore Document
Firestore → users → [your-document-id]
Should show:
```
role: "GAS_STATION" ✓
isActive: true ✓
email: "operator@gasstation.com" ✓
```

### Test Gas Station Features
1. Click **Scan QR Code** button
2. Should open scanner dialog
3. Scan a transaction (or use test QR):
   ```
   REF:TXN001|PLATE:ABC123|DRIVER:John|FUEL:DIESEL|LITERS:50.0|DATE:2025-12-21
   ```
4. Should show transaction details
5. Click **Confirm Dispensed**
6. Should show success dialog

---

## Common Issues

| Issue | Solution |
|-------|----------|
| Login fails | Check password is correct |
| Goes to Home instead of Gas Station | Make sure role is exactly `GAS_STATION` (case-sensitive) |
| Can't find transaction | Create a test transaction first in Transactions screen |
| QR scanner not working | Use simulated QR code for testing |

---

## Create Multiple Accounts

Repeat Steps 1-3 for each operator:
- `operator2@gasstation.com` → Station 2
- `operator3@gasstation.com` → Station 3
- etc.

Just change `username`, `fullName`, and `officeId` values.

---

## Test Transactions

To fully test the gas station feature:

1. **First login as DISPATCHER** and create a transaction:
   - Username: `dispatcher@company.com` (create this account too)
   - Create a transaction with a vehicle

2. **Then login as GAS_STATION operator** and:
   - See the pending transaction
   - Scan/confirm the QR code
   - Watch status change to DISPENSED

---

## Next: Create More Accounts

### For testing multiple roles, create:

1. **Admin Account**
   - Email: `admin@fuelhub.com`
   - Password: `Admin123`
   - Role: `ADMIN`

2. **Dispatcher Account**
   - Email: `dispatcher@fuelhub.com`
   - Password: `Dispatcher123`
   - Role: `DISPATCHER`

3. **Gas Station Accounts** (1-5)
   - Email: `station1@fuelhub.com` through `station5@fuelhub.com`
   - Password: `GasStation123`
   - Role: `GAS_STATION`

---

## Production Checklist

Before going live:

- [ ] Create all operator accounts in Firebase
- [ ] Test login for each operator
- [ ] Verify navigation to correct screen
- [ ] Test transaction creation and confirmation
- [ ] Set up Firestore security rules
- [ ] Disable user self-registration (optional)
- [ ] Enable password reset
- [ ] Test on actual devices
- [ ] Document operator usernames/passwords for training

---

## Quick Reference

### Credentials Template
```
Email: operator[N]@gasstation.com
Password: GasStation123
Role: GAS_STATION
Username: pump-station-[N]
```

### Firestore Fields
```json
{
  "id": "USER_ID_FROM_AUTH",
  "username": "pump-station-01",
  "email": "operator@gasstation.com",
  "fullName": "Operator Name",
  "role": "GAS_STATION",
  "officeId": "location-1",
  "isActive": true,
  "createdAt": "2025-12-21T10:00:00"
}
```

Done! Your gas station operator is ready to login and use the app.
