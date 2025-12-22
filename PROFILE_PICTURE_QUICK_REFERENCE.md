# Profile Picture Upload - Quick Reference Card

## 🎯 What's New

Users can now upload and display profile pictures in:
- Navigation Drawer (circular avatar with edit button)
- HomeScreen (profile header)

## 📁 Files to Know

| File | Purpose |
|------|---------|
| `MainActivity.kt` | Image picker + upload logic |
| `AuthViewModel.kt` | Profile picture state management |
| `FirebaseAuthRepository.kt` | Firestore profile URL retrieval |
| `HomeScreen.kt` | Display profile picture |

## 🔧 Key Methods

### MainActivity.kt
```kotlin
// Triggered when user clicks edit button
imagePickerLauncher.launch("image/*")

// Handles upload to Storage → Firestore → ViewModel
private suspend fun uploadProfilePicture(imageUri: Uri)
```

### AuthViewModel.kt
```kotlin
// Current profile picture URL
val profilePictureUrl: StateFlow<String?>

// Update when upload completes
fun updateProfilePictureUrl(url: String)

// Load from Firestore
fun loadProfilePictureUrl(userId: String)
```

## 🔄 Data Flow (User Perspective)

```
Click Edit Button → Select Photo → Upload → See Updated Picture
        ↓              ↓            ↓          ↓
   imagePickerLauncher  Uri   uploadProfilePicture  AsyncImage
```

## 📊 Technical Stack

| Layer | Technology |
|-------|-----------|
| **UI** | Jetpack Compose + Coil |
| **State** | StateFlow (ReactiveX pattern) |
| **Storage** | Firebase Storage (cloud) |
| **Database** | Firestore (URL persistence) |
| **Async** | Kotlin Coroutines |

## 🚀 Common Tasks

### Display Profile Picture
```kotlin
// In Composable
val profilePictureUrl by authViewModel.profilePictureUrl.collectAsState()

AsyncImage(
    model = profilePictureUrl,
    contentDescription = "Profile Picture",
    modifier = Modifier.size(64.dp).clip(CircleShape),
    contentScale = ContentScale.Crop,
    loading = { CircularProgressIndicator() },
    error = { Icon(Icons.Default.Person, ...) }
)
```

### Trigger Upload
```kotlin
// Click handler
imagePickerLauncher.launch("image/*")
```

### Auto-load on Login
```kotlin
// Already implemented in AuthViewModel.fetchUserRole()
val profilePictureUrl = authRepository.getUserProfilePictureUrl(userId)
_profilePictureUrl.value = profilePictureUrl
```

## 🔐 Security

**Storage Path**: `profiles/{userId}/profile.jpg`
- Only the owner can upload
- Only the owner can download

**Firestore Field**: `users/{userId}.profilePictureUrl`
- Only the owner can read/write
- Contains the download URL

## 📝 Firestore Rules

```json
match /users/{userId} {
  allow read, write: if request.auth.uid == userId;
}
```

## 💾 Storage Rules

```json
match /profiles/{userId}/{filename=**} {
  allow read: if request.auth.uid == userId;
  allow write: if request.auth.uid == userId;
}
```

## 🐛 Debugging

### Check Logs
```kotlin
// Success
Timber.d("Profile picture updated successfully: $downloadUrl")

// Error
Timber.e(e, "Error uploading profile picture: ${e.message}")
```

### Verify Data
1. **Firebase Console** → Storage → Check `profiles/{userId}/profile.jpg`
2. **Firebase Console** → Firestore → Check `users/{userId}.profilePictureUrl`

## ⚠️ Common Issues

| Problem | Solution |
|---------|----------|
| Picture not showing | Check Firestore Security Rules |
| Upload fails silently | Check Storage Security Rules |
| User not found | Verify `getCurrentUserId()` returns valid UID |
| Image not loading | Verify image URL in Firestore exists |
| Crash on compose | Ensure `@Composable` context |

## 📦 Dependencies Added

```gradle
implementation("com.google.firebase:firebase-storage-ktx")
implementation("io.coil-kt:coil-compose:2.5.0")
implementation("kotlinx.coroutines.tasks.await")
```

## ✅ Checklist for Integration

- [ ] Build succeeds with 0 errors
- [ ] Dependencies installed
- [ ] Firebase Storage enabled
- [ ] Security Rules configured
- [ ] Test on device
- [ ] Verify logs in Logcat
- [ ] Check Firestore data
- [ ] Check Storage files
- [ ] Test logout/login cycle

## 📞 Need Help?

### For Compilation Errors
→ Check imports and dependencies in build.gradle.kts

### For Upload Failures
→ Check Timber logs in Logcat for specific error

### For Display Issues
→ Verify Firestore has profilePictureUrl field with valid URL

### For Security Issues
→ Review Firebase Console Security Rules tab

## 🎓 Learning Resources

- **AsyncImage** → Coil documentation
- **StateFlow** → Kotlin coroutines flow docs
- **Firebase Storage** → Firebase console documentation
- **Jetpack Compose** → Compose documentation

---

**Version**: 1.0
**Last Updated**: 2025-12-22
**Status**: ✅ Production Ready
