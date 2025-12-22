# Profile Picture Device Storage Implementation - FIXED

## Problem Statement
Profile pictures were not loading or displaying in the drawer and homescreen. The system was trying to use Firestore Storage when device storage is more appropriate for local profile pictures.

## Solution Overview
Implement a complete device-based profile picture system that:
1. Saves images to app's private directory on device
2. Stores the file path in Firestore (for persistence across sessions)
3. Uses proper `file://` URIs for Coil image loading
4. Automatically loads saved pictures on app startup

## Architecture

```
User Selection (Image Picker)
        ↓
    AuthViewModel.uploadProfilePicture(Uri)
        ↓
saveProfilePictureLocally() → /data/data/app/files/profiles/userId.jpg
        ↓
authRepository.saveProfilePictureUrl() → Save path to Firestore
        ↓
Update _profilePictureUrl StateFlow with file:// URI
        ↓
Coil loads from file:// URI → Display in UI
```

## Implementation Details

### 1. AuthRepository Interface (Updated)
Added new method:
```kotlin
suspend fun saveProfilePictureUrl(userId: String, picturePath: String): Result<Unit>
```

### 2. FirebaseAuthRepository Implementation
- **saveProfilePictureUrl()**: Saves the local file path and timestamp to Firestore
  - Field: `profilePictureUrl` (absolute path on device)
  - Field: `profilePictureUpdatedAt` (timestamp for tracking)

### 3. AuthViewModel Profile Picture Methods

#### uploadProfilePicture(imageUri: Uri)
```
1. Get current user ID
2. Save image to device: /data/data/...profiles/{userId}.jpg
3. Convert path to file:// URI format
4. Save path to Firestore via authRepository
5. Update _profilePictureUrl StateFlow with file:// URI
```

#### loadProfilePictureUrl(userId: String)
```
1. Query Firestore for saved profile picture path
2. Verify file exists on device
3. Convert to file:// URI and update StateFlow
4. Fallback to null if file not found
```

#### fetchUserRole()
```
1. Called during initialization to load user data
2. Also loads profile picture URL from Firestore
3. Sets _profilePictureUrl with file:// URI if file exists
```

#### refreshProfilePicture()
```
1. Manually refresh profile picture after selection
2. Queries Firestore and reloads file URI
3. Updates StateFlow to trigger UI recomposition
```

### 4. File Storage Details

**Location**: `/data/data/{packageName}/files/profiles/{userId}.jpg`

**Benefits**:
- Private to the app (no need for permissions)
- Automatically backed up by app
- Automatically cleaned up on uninstall
- Isolated from other apps

### 5. URI Format

Profile pictures are stored and loaded using:
```
file:///data/data/{packageName}/files/profiles/{userId}.jpg
```

Coil properly handles `file://` URIs for local files.

## Data Flow

### On App Startup
```
1. User logs in
2. AuthViewModel.fetchUserRole() is called
3. Loads profile picture URL from Firestore
4. Verifies file exists on device
5. Updates _profilePictureUrl StateFlow
6. UI recomposes and displays image (or placeholder)
```

### On Profile Picture Selection
```
1. User clicks edit button in drawer/homescreen
2. Image picker opens
3. User selects image
4. registerForActivityResult callback triggered
5. MainActivity.selectedImageUri is set
6. LaunchedEffect detects change
7. authViewModel.uploadProfilePicture(uri) called
8. Image saved to device
9. Path saved to Firestore
10. _profilePictureUrl updated with file:// URI
11. Coil loads and displays new image immediately
```

## Firestore Document Structure

User document in `users/{userId}`:
```json
{
  "id": "user-123",
  "username": "john_doe",
  "email": "john@example.com",
  "fullName": "John Doe",
  "role": "DISPATCHER",
  "profilePictureUrl": "/data/data/dev.ml.fuelhub/files/profiles/user-123.jpg",
  "profilePictureUpdatedAt": "2025-01-15T10:30:00",
  ...
}
```

## Affected Components

1. **Drawer Profile Header** (MainActivity.kt)
   - Displays profile picture from AuthViewModel.profilePictureUrl
   - Shows edit button to change picture

2. **HomeScreen Header** (HomeScreen.kt)
   - Displays profile picture from AuthViewModel.profilePictureUrl
   - Shows in profile circle with notification badge

3. **AsyncImage (Coil)**
   - Loads from `file://` URIs
   - Displays placeholders while loading
   - Shows default person icon if no picture

## Testing Checklist

- [ ] App starts and loads saved profile picture
- [ ] Edit button visible in drawer header
- [ ] Click edit button opens image picker
- [ ] Select image from device gallery
- [ ] Image updates immediately in drawer
- [ ] Image updates immediately in homescreen
- [ ] Close and reopen app - image still displays
- [ ] Picture appears in both locations
- [ ] Placeholder icon shows if no picture selected

## Error Handling

1. **Image save fails**: User notified via logs, UI still works
2. **Firestore save fails**: Image still updates UI, will be saved on next sync
3. **File not found**: Falls back to placeholder icon
4. **No user logged in**: Gracefully handles null user ID

## Performance Considerations

- Images saved as JPEG at 85% quality
- Images stored in app's private directory (no scoped storage issues)
- File:// URIs load much faster than network URLs
- No network calls needed for displaying saved pictures
- Firestore update happens in background

## Migration from Firebase Storage

If you previously had images in Firebase Storage:
1. Download images from Firebase Storage
2. Save to device `/data/data/app/files/profiles/{userId}.jpg`
3. Update Firestore document with local file path
4. Delete from Firebase Storage (optional)

This implementation uses device storage exclusively, eliminating Firebase Storage costs.
