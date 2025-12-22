# Profile Picture Upload - Quick Start Guide

## 🎯 What Works Now

✅ **Upload profile picture from drawer edit button**
✅ **Display picture in drawer header (64dp)**
✅ **Display picture in HomeScreen profile (56dp)**
✅ **Upload to Firebase Storage**
✅ **Store URL in Firestore**
✅ **Real-time UI updates**
✅ **Error handling & fallbacks**
✅ **Loading indicators**

## 🚀 How to Use

### Upload Profile Picture

1. **From Drawer**
   ```
   Menu → Tap Edit Button (✏️) on Profile
   → Select image from phone
   → Image uploads automatically
   → Displays in drawer
   ```

2. **From HomeScreen**
   ```
   (Profile picture icon visible in header)
   → Updates automatically after upload
   ```

### What Happens Behind the Scenes

```
1. Image selected from phone
   ↓
2. Uploaded to Firebase Storage
   Path: gs://project/profiles/{userId}/profile.jpg
   ↓
3. Download URL retrieved
   ↓
4. Firestore updated with URL
   Collection: users
   Document: {userId}
   Field: profilePictureUrl
   ↓
5. AuthViewModel state updated
   ↓
6. UI automatically refreshes
   Drawer: displays new image (64dp)
   HomeScreen: displays new image (56dp)
```

## 📁 Files Changed

| File | What Changed |
|------|--------------|
| **MainActivity.kt** | Image picker & upload logic |
| **AuthViewModel.kt** | Profile picture state |
| **AuthRepository.kt** | Get profile picture URL |
| **FirebaseAuthRepository.kt** | Firestore query |
| **HomeScreen.kt** | Display profile picture |

## 🔧 Required Setup

### 1. Firebase Storage Rules
```
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /profiles/{userId}/profile.jpg {
      allow read: if request.auth != null;
      allow write: if request.auth.uid == userId;
    }
  }
}
```

### 2. Firestore Rules
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth.uid == userId;
    }
  }
}
```

### 3. Dependencies (Already Added)
```gradle
implementation 'com.google.firebase:firebase-storage-ktx'
implementation 'com.google.firebase:firebase-firestore-ktx'
implementation 'io.coil-kt:coil-compose:2.4.0'
```

## 🎨 UI Locations

### Drawer Profile Picture
- **Location**: Navigation drawer header
- **Size**: 64dp circular
- **Tap to Edit**: Edit button next to profile picture
- **Default**: Person icon

### HomeScreen Profile Picture
- **Location**: Top right corner of header
- **Size**: 56dp circular  
- **Default**: Person icon
- **Badge**: Notification count overlay

## 📊 Data Flow

### Firestore Document Structure
```json
{
  "id": "user123",
  "email": "user@example.com",
  "fullName": "John Doe",
  "profilePictureUrl": "https://firebasestorage.googleapis.com/...",
  "role": "DISPATCHER"
}
```

### State Management
```
AuthViewModel {
  profilePictureUrl: StateFlow<String?> = "https://..."
}
```

## 🧪 Testing

### Test Upload
1. Open drawer
2. Tap edit button on profile
3. Select image
4. Verify image appears in drawer
5. Navigate to HomeScreen
6. Verify image appears in header
7. Close and reopen app
8. Verify image persists

### Test Error Cases
- No internet → Shows fallback icon
- Bad image → Shows fallback icon
- Invalid URL → Shows fallback icon
- User not logged in → Skips upload

## 🔍 Debugging

### Check Upload Status
```
Firebase Console → Storage
→ Look for: profiles/{userId}/profile.jpg
```

### Check Firestore Update
```
Firebase Console → Firestore
→ Collection: users
→ Document: {userId}
→ Field: profilePictureUrl (should have URL)
```

### Enable Debug Logs
```kotlin
// Already enabled in code:
Timber.d("Image selected: $uri")
Timber.d("Profile picture updated: $url")
Timber.e("Upload failed: ${error.message}")

// Check logcat for these messages
```

## 🚨 Common Issues

### Image Not Displaying
**Check**:
1. Firestore has profilePictureUrl field
2. URL is not empty
3. Storage file exists
4. Internet connection

**Solution**:
1. Try uploading again
2. Check Firebase console

### Upload Fails
**Check**:
1. User is logged in
2. Firebase Storage rules are correct
3. Internet connection
4. Image file size

**Solution**:
1. Restart app
2. Check Firebase rules

### Picture Disappears After Restart
**Check**:
1. Firestore document exists
2. profilePictureUrl field has value
3. URL is accessible

**Solution**:
1. Re-upload picture
2. Check Firestore permissions

## 💡 Tips

1. **Clear Cache** if image doesn't update
   - App Settings → Storage → Clear Cache

2. **Check Network** if upload fails
   - Try on WiFi instead of mobile data

3. **Restart App** after uploading
   - Forces reload of user data

4. **Use Recent Images** for faster upload
   - Large images take longer

## 🔐 Security

✅ Only authenticated users can upload
✅ Only own profile picture can be updated
✅ Images stored securely in Firebase Storage
✅ URLs stored in Firestore
✅ All operations logged

## 📞 Support Commands

### Check Image Picker
```kotlin
imagePickerLauncher.launch("image/*")
```

### Upload Image
```kotlin
uploadProfilePicture(selectedUri)
```

### Load Profile Picture
```kotlin
authViewModel?.loadProfilePictureUrl(userId)
```

### Update Display
```kotlin
authViewModel?.updateProfilePictureUrl(url)
```

## ✨ Features

| Feature | Status |
|---------|--------|
| Image Picker | ✅ Working |
| Firebase Upload | ✅ Working |
| Firestore Storage | ✅ Working |
| Drawer Display | ✅ Working |
| HomeScreen Display | ✅ Working |
| Error Handling | ✅ Working |
| Loading States | ✅ Working |
| Persistence | ✅ Working |

## 🎓 What's Next

Optional enhancements:
- [ ] Image cropping
- [ ] Image compression
- [ ] Image filters
- [ ] Multiple pictures
- [ ] Picture history
- [ ] Picture sharing

## 📚 Documentation

For detailed information, see:
- `PROFILE_PICTURE_UPLOAD_IMPLEMENTATION.md` - Full technical details
- `DRAWER_ENHANCEMENT_COMPLETE.md` - Drawer features
- `PROFILE_PICTURE_UPDATE_GUIDE.md` - Implementation guide

## ✅ Ready to Use

Everything is implemented and tested. You can:
1. Open drawer
2. Tap edit on profile picture
3. Select image
4. See it upload and display automatically
5. Check drawer and HomeScreen
6. Picture persists across app sessions

**All files compile successfully with no errors!**
