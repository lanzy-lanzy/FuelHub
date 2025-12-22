# Profile Picture Code Changes - Complete Reference

## 1. AuthRepository Interface - Added Method

**File**: `AuthRepository.kt`

```kotlin
// ADDED:
suspend fun saveProfilePictureUrl(userId: String, picturePath: String): Result<Unit>
```

This method contract allows saving profile picture file paths to persistent storage.

---

## 2. FirebaseAuthRepository - Implementation

**File**: `FirebaseAuthRepository.kt`

### Added Method: saveProfilePictureUrl()

```kotlin
override suspend fun saveProfilePictureUrl(userId: String, picturePath: String): Result<Unit> {
    return try {
        firestore.collection("users")
            .document(userId)
            .update(mapOf(
                "profilePictureUrl" to picturePath,
                "profilePictureUpdatedAt" to LocalDateTime.now().toString()
            ))
            .await()
        Timber.d("Profile picture URL saved for user: $userId")
        Result.success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Failed to save profile picture URL for: $userId")
        Result.failure(e)
    }
}
```

**What it does**:
- Saves the local file path to Firestore
- Stores timestamp of when picture was updated
- Returns success/failure result

---

## 3. AuthViewModel - Complete Rewrites

**File**: `AuthViewModel.kt`

### Method 1: uploadProfilePicture(imageUri: Uri)

**BEFORE** (Broken):
```kotlin
fun uploadProfilePicture(imageUri: Uri) {
    viewModelScope.launch {
        val userId = authRepository.getCurrentUserId() ?: run {
            Timber.e("User not authenticated")
            return@launch
        }
        
        try {
            val profileImagePath = saveProfilePictureLocally(userId, imageUri)
            if (profileImagePath != null) {
                // PROBLEM: Timestamp parameters don't work with Coil
                val cacheBustedUrl = "$profileImagePath?timestamp=${System.currentTimeMillis()}"
                _profilePictureUrl.value = cacheBustedUrl
            }
        } catch (e: Exception) {
            Timber.e(e, "Error uploading profile picture: ${e.message}")
        }
    }
}
```

**AFTER** (Fixed):
```kotlin
fun uploadProfilePicture(imageUri: Uri) {
    viewModelScope.launch {
        val userId = authRepository.getCurrentUserId() ?: run {
            Timber.e("User not authenticated")
            return@launch
        }
        
        try {
            // Save image to local device storage
            val profileImagePath = saveProfilePictureLocally(userId, imageUri)
            
            if (profileImagePath != null) {
                // Convert to proper file:// URI
                val fileUri = "file://$profileImagePath"
                
                // Save path to Firestore
                authRepository.saveProfilePictureUrl(userId, profileImagePath).onSuccess {
                    _profilePictureUrl.value = fileUri
                    Timber.d("Profile picture saved locally and to Firestore: $fileUri")
                }.onFailure { error ->
                    Timber.e(error, "Failed to save profile picture URL to Firestore")
                    // Still update UI even if Firestore save fails
                    _profilePictureUrl.value = fileUri
                }
            } else {
                Timber.e("Failed to save profile picture locally")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error uploading profile picture: ${e.message}")
        }
    }
}
```

**Key changes**:
- ✅ Uses proper `file://` URI format
- ✅ Saves path to Firestore
- ✅ Updates UI immediately
- ✅ Gracefully handles Firestore failures

---

### Method 2: loadProfilePictureUrl(userId: String)

**BEFORE** (Broken):
```kotlin
fun loadProfilePictureUrl(userId: String) {
    viewModelScope.launch {
        try {
            // Only checks device, doesn't verify path exists
            val profileFile = File(context.filesDir, "profiles/$userId.jpg")
            if (profileFile.exists()) {
                val cacheBustedUrl = "${profileFile.absolutePath}?timestamp=${System.currentTimeMillis()}"
                _profilePictureUrl.value = cacheBustedUrl
            } else {
                _profilePictureUrl.value = null
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to load profile picture URL")
        }
    }
}
```

**AFTER** (Fixed):
```kotlin
fun loadProfilePictureUrl(userId: String) {
    viewModelScope.launch {
        try {
            // Try to load from Firestore first
            val savedPath = authRepository.getUserProfilePictureUrl(userId)
            
            if (savedPath != null && savedPath.isNotEmpty()) {
                // Check if the file exists on device
                val profileFile = File(savedPath)
                if (profileFile.exists()) {
                    val fileUri = "file://$savedPath"
                    _profilePictureUrl.value = fileUri
                    Timber.d("Loaded profile picture from local storage: $fileUri")
                } else {
                    Timber.d("Profile picture path in Firestore but file not found: $savedPath")
                    _profilePictureUrl.value = null
                }
            } else {
                Timber.d("No profile picture URL stored for user: $userId")
                _profilePictureUrl.value = null
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to load profile picture URL")
            _profilePictureUrl.value = null
        }
    }
}
```

**Key changes**:
- ✅ Loads path from Firestore first
- ✅ Verifies file exists on device
- ✅ Uses proper `file://` URI format
- ✅ Handles missing files gracefully

---

### Method 3: fetchUserRole()

**BEFORE** (Incomplete):
```kotlin
private fun fetchUserRole() {
    viewModelScope.launch {
        try {
            val userId = authRepository.getCurrentUserId()
            if (userId != null) {
                val userRole = authRepository.getUserRole(userId)
                val userFullName = authRepository.getUserFullName(userId)
                
                // Only checked device
                val profileFile = File(context.filesDir, "profiles/$userId.jpg")
                val profilePictureUrl = if (profileFile.exists()) {
                    "${profileFile.absolutePath}?timestamp=${System.currentTimeMillis()}"
                } else {
                    null
                }
                
                _uiState.value = _uiState.value.copy(
                    userRole = userRole,
                    userFullName = userFullName,
                    userEmail = authRepository.getCurrentUserEmail()
                )
                _profilePictureUrl.value = profilePictureUrl
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch user role")
        }
    }
}
```

**AFTER** (Fixed):
```kotlin
private fun fetchUserRole() {
    viewModelScope.launch {
        try {
            val userId = authRepository.getCurrentUserId()
            if (userId != null) {
                val userRole = authRepository.getUserRole(userId)
                val userFullName = authRepository.getUserFullName(userId)
                
                // Load profile picture from Firestore
                val savedPath = authRepository.getUserProfilePictureUrl(userId)
                var profilePictureUrl: String? = null
                
                if (savedPath != null && savedPath.isNotEmpty()) {
                    val profileFile = File(savedPath)
                    if (profileFile.exists()) {
                        profilePictureUrl = "file://$savedPath"
                    }
                }
                
                _uiState.value = _uiState.value.copy(
                    userRole = userRole,
                    userFullName = userFullName,
                    userEmail = authRepository.getCurrentUserEmail()
                )
                _profilePictureUrl.value = profilePictureUrl
                Timber.d("Fetched user role: $userRole, Profile picture: ${profilePictureUrl?.take(50)}...")
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch user role")
        }
    }
}
```

**Key changes**:
- ✅ Loads from Firestore instead of hardcoding device path
- ✅ Verifies file exists
- ✅ Uses proper `file://` URI format

---

### Method 4: refreshProfilePicture()

**BEFORE** (Simple call):
```kotlin
fun refreshProfilePicture() {
    viewModelScope.launch {
        val userId = authRepository.getCurrentUserId() ?: return@launch
        loadProfilePictureUrl(userId)
    }
}
```

**AFTER** (Direct implementation):
```kotlin
fun refreshProfilePicture() {
    viewModelScope.launch {
        val userId = authRepository.getCurrentUserId() ?: return@launch
        try {
            val savedPath = authRepository.getUserProfilePictureUrl(userId)
            if (savedPath != null && savedPath.isNotEmpty()) {
                val profileFile = File(savedPath)
                if (profileFile.exists()) {
                    val fileUri = "file://$savedPath"
                    _profilePictureUrl.value = fileUri
                    Timber.d("Refreshed profile picture: $fileUri")
                } else {
                    _profilePictureUrl.value = null
                }
            } else {
                _profilePictureUrl.value = null
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to refresh profile picture")
        }
    }
}
```

**Key changes**:
- ✅ Direct implementation (no delegation)
- ✅ Queries Firestore immediately
- ✅ Fast refresh of UI

---

## 4. No Changes Required

These files work perfectly as-is:

- ✅ `MainActivity.kt` - Image picker still works
- ✅ `HomeScreen.kt` - UI display still works
- ✅ `AsyncImage (Coil)` - Works great with file:// URIs

---

## Summary of Fixes

| Issue | Before | After |
|-------|--------|-------|
| Image Loading | ❌ Timestamp params broken | ✅ Proper `file://` URIs |
| Persistence | ❌ No Firestore integration | ✅ Saves path to Firestore |
| Data Recovery | ❌ Only device check | ✅ Loads from Firestore first |
| Error Handling | ❌ Logs only | ✅ Graceful fallbacks |
| Storage | ❌ Firebase Storage | ✅ Device storage only |
| UI Updates | ❌ Manual/inconsistent | ✅ StateFlow drives UI |

---

## URI Format Changes

```
BEFORE (broken):
/data/data/dev.ml.fuelhub/files/profiles/user-123.jpg?timestamp=1705321200000

AFTER (working):
file:///data/data/dev.ml.fuelhub/files/profiles/user-123.jpg
```

The `file://` scheme tells Coil to load from device storage.

---

## Firestore Schema Update

The system now expects:

```json
{
  "users": {
    "user-123": {
      "id": "user-123",
      "email": "user@example.com",
      "fullName": "John Doe",
      "role": "DISPATCHER",
      "profilePictureUrl": "/data/data/dev.ml.fuelhub/files/profiles/user-123.jpg",
      "profilePictureUpdatedAt": "2025-01-15T10:30:00",
      ...
    }
  }
}
```

The `profilePictureUrl` field stores the absolute device path (not a cloud URL).

---

## Testing Verification

After applying these changes:

1. ✅ Build should compile without errors
2. ✅ App should start normally
3. ✅ Image picker should open when edit button clicked
4. ✅ Selected image should save to device
5. ✅ Image should display immediately in drawer and homescreen
6. ✅ Path should save to Firestore
7. ✅ Close and reopen app - image should persist
8. ✅ No Firebase Storage calls needed

---

## Performance Improvements

- No network calls for displaying saved images
- No Firebase Storage setup required
- Instant image loading from device
- Reduced storage costs (no cloud storage)
- Single Firestore write per image change
- Single Firestore read on app startup
