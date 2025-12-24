# Where to Find FCM Tokens in Firestore

## 📍 Step-by-Step Navigation

### Step 1: Go to Firebase Console
```
1. Open browser
2. Go to: https://console.firebase.google.com
3. Select your FuelHub project
```

### Step 2: Click Firestore Database
```
Left menu:
├─ Project Overview
├─ Authentication
├─ Firestore Database  ← CLICK HERE
├─ Storage
├─ Functions
└─ ...
```

### Step 3: You'll See Collections List
```
Your Firestore will show:
├─ audit_logs
├─ fuel_wallets
├─ gas_slips
├─ notifications
├─ transactions
├─ users
├─ vehicles
└─ fcm_tokens  ← LOOK FOR THIS
```

### Step 4: Click "fcm_tokens" Collection
```
Click on "fcm_tokens" in the list
```

---

## 🎯 What You Should See

### If Tokens Are Stored (Good!)
```
Collection: fcm_tokens

Documents:
├─ user_id_123
│  ├─ userId: "user_id_123"
│  ├─ token: "eGp0Ax7qLFI:APA91bE6a1X2k3j4..."
│  └─ updatedAt: "2024-12-22 10:30:45 UTC"
│
├─ user_id_456
│  ├─ userId: "user_id_456"
│  ├─ token: "fHq1By8sLGH:BPB92cF7b2Y3l5k6..."
│  └─ updatedAt: "2024-12-22 10:31:00 UTC"
```

### If No Tokens (Problem!)
```
Collection: fcm_tokens

(empty - no documents)
```

---

## 🔍 How to View a Token Document

### Step 1: Click on a User Document
```
In fcm_tokens collection, click on a document ID
(e.g., click on "user_id_123")
```

### Step 2: You'll See the Token Details
```
Document: 2f3babb7-0c9a-45ba-a7ad-b0132127f7a8

Fields:
├─ token (string)
│  └─ "eGp0Ax7qLFI:APA91bE6a1X2k3j4X5m6n7o8p9q0r1s2t3u4v5w6x7y8z9..."
│
├─ updatedAt (timestamp)
│  └─ Dec 22, 2024 10:30:45 AM UTC
│
└─ userId (string)
   └─ "user_id_123"
```

---

## 🐛 Troubleshooting: No fcm_tokens Collection?

### Reason 1: Collection Doesn't Exist Yet
- **Cause**: You haven't logged in yet with the new code
- **Fix**: Add token storage code to login → Login → Collection will be created automatically

### Reason 2: You Logged In But No Collection
- **Cause**: Token storage code not being called
- **Check**:
  1. Did you add the `storeFcmToken()` code to your login?
  2. Did you rebuild the app?
  3. Did you actually log in after rebuild?
  4. Check logcat for errors:
     ```bash
     adb logcat | grep -i "token\|fcm"
     ```

### Reason 3: Different Firebase Project
- **Cause**: Your app is using different google-services.json
- **Check**:
  1. Firebase Console project name matches your app's google-services.json
  2. Package name is correct (dev.ml.fuelhub)

---

## 📸 Visual Guide

### Firebase Console Navigation
```
https://console.firebase.google.com
            ↓
    [Select Your Project]
            ↓
    Left Menu → Firestore Database
            ↓
    Collections List appears
            ↓
    Click "fcm_tokens"
            ↓
    See all user tokens (if any exist)
```

### After You Login
```
App Login → Token Generated → Stored in Firestore
    ↓
Wait 5 seconds
    ↓
Refresh Firebase Console
    ↓
fcm_tokens collection appears with your user document
```

---

## ✅ Quick Verification Checklist

- [ ] I have Firebase Console open
- [ ] I can see Firestore Database option
- [ ] I can see collections list
- [ ] I can see (or don't see) fcm_tokens collection
- [ ] I understand what should appear there

---

## 🎯 Real Example

**Your Firebase Console should look like:**

```
Firestore Database

Collections:
📁 audit_logs
📁 fuel_wallets
📁 gas_slips
📁 notifications      ← Has data (from your transactions)
📁 transactions       ← Has data (from your app)
📁 users              ← Has data (user accounts)
📁 vehicles
📁 fcm_tokens         ← Should have YOUR token here after login
```

---

## 🔗 Direct Firebase Link

Replace YOUR_PROJECT_ID:
```
https://console.firebase.google.com/project/YOUR_PROJECT_ID/firestore/data/fcm_tokens
```

To find YOUR_PROJECT_ID:
1. Go to Firebase Console
2. Click Project Settings (⚙️ icon)
3. Look for "Project ID"
4. Copy it
5. Replace in URL above

---

## 📱 After You Log In

**Timeline:**
```
Time 0:00 → You click Login button
Time 0:10 → Firebase authenticates
Time 0:20 → App requests FCM token
Time 0:30 → Token is stored in Firestore
Time 0:35 → You refresh Firebase Console
Time 0:40 → ✅ Token appears in fcm_tokens collection
```

---

## ✨ Success Indicators

**You'll know it worked when:**

1. ✅ fcm_tokens collection appears in Firestore
2. ✅ Your user ID shows up as a document
3. ✅ Document has a "token" field
4. ✅ Token field has a long string (starts with "eGp" or similar)
5. ✅ updatedAt shows recent time

---

## 🚀 Next Steps After Finding Tokens

1. ✅ Login and verify token appears in fcm_tokens
2. ✅ Create a transaction
3. ✅ Check "notifications" collection for notification document
4. ✅ Deploy Cloud Function (will send the FCM message)
5. ✅ Check device notification center for notification

---

**Ready? Go to Firebase Console now and check fcm_tokens collection!** 🔍
