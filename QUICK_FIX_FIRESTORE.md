# Quick Fix for PERMISSION_DENIED Error - 3 Steps

## The Problem
Firestore security rules are blocking write access.

## The Solution (3 minutes)

### Step 1: Open Firebase Console
Go to: https://console.firebase.google.com

### Step 2: Navigate to Security Rules
1. Select project **drrfuel**
2. Click **Firestore Database**
3. Click **Rules** tab (not "Indexes")

### Step 3: Copy-Paste This Rule

**DELETE everything in the Rules editor** and replace with:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if true;
    }
  }
}
```

### Step 4: Publish
Click **Publish** button

### Step 5: Restart App
Close the FuelHub app completely and reopen it.

---

## Done! 🎉

The error should be gone. You can now:
- ✅ Add drivers
- ✅ Add vehicles  
- ✅ Create transactions
- ✅ Manage wallets
- ✅ Generate gas slips

---

## Important Notes

⚠️ **This rule allows EVERYONE to read/write**
- ✅ Use for development/testing
- ❌ DO NOT use for production

For production, see FIRESTORE_PERMISSION_FIX.md for secure rules.

---

## If It Still Doesn't Work

1. **Did you click "Publish"?** 
   - Rules must be published to take effect

2. **Is google-services.json valid?**
   - Check the file exists in app/ folder
   - Rebuild: `./gradlew clean build`

3. **Did you restart the app?**
   - Fully close and reopen
   - Not just pressing back

4. **Try creating collections first**
   - In Firebase Console → Firestore
   - Click "Start Collection"
   - Create: users, vehicles, fuel_wallets, transactions, gas_slips, audit_logs
   - Leave fields empty, just create them

5. **Check Firebase Console for errors**
   - Firestore → Logs
   - Look for permission errors
   - Copy error and search Firebase docs

---

## Quick Test

After fixing, try adding a driver:
1. Click "Manage Drivers"
2. Click "+ Add Driver"
3. Fill in details
4. Click "Save"

Should work now!

---

## Common Issues

### "Collection does not exist"
→ Create the collection in Firebase Console first

### "Document does not exist"  
→ Normal, it creates it when you write

### Still getting PERMISSION_DENIED
→ Rules didn't publish OR app didn't restart

### "Missing google-services.json"
→ Download from Firebase Console → Project Settings
→ Place in app/ folder
→ Rebuild app

---

## Next: Production Setup

Once testing works, update to secure rules:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == userId;
    }
    
    match /vehicles/{document=**} {
      allow read, write: if request.auth != null;
    }
    
    match /fuel_wallets/{document=**} {
      allow read, write: if request.auth != null;
    }
    
    match /transactions/{document=**} {
      allow read, write: if request.auth != null;
    }
    
    match /gas_slips/{document=**} {
      allow read, write: if request.auth != null;
    }
    
    match /audit_logs/{document=**} {
      allow read: if request.auth != null;
      allow write: if false;
    }
  }
}
```

This requires authentication instead of allowing everyone.

---

**That's it! You're done.** ✅
