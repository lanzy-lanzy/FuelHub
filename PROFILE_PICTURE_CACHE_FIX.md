# Profile Picture Cache Fix - Complete

## Problem
Profile picture changes weren't appearing in the drawer or homescreen even though the image was selected. The image was being saved correctly but Coil's image caching prevented the UI from updating.

## Root Cause
Coil (the image loading library) caches images based on their URL. Since the profile picture URL was a local file path (e.g., `/data/data/.../profiles/userId.jpg`), Coil would load the cached version instead of the newly saved image.

## Solution
Added cache-busting parameters using timestamps to force Coil to reload the image whenever it changes.

## Changes Made

### AuthViewModel.kt
1. **uploadProfilePicture()** - Added timestamp parameter to URL
   ```kotlin
   val cacheBustedUrl = "$profileImagePath?timestamp=${System.currentTimeMillis()}"
   _profilePictureUrl.value = cacheBustedUrl
   ```

2. **loadProfilePictureUrl()** - Added timestamp to cached URLs
   ```kotlin
   val cacheBustedUrl = "${profileFile.absolutePath}?timestamp=${System.currentTimeMillis()}"
   ```

3. **fetchUserRole()** - Added timestamp parameter when loading profile pictures on initialization
   ```kotlin
   val profilePictureUrl = if (profileFile.exists()) {
       "${profileFile.absolutePath}?timestamp=${System.currentTimeMillis()}"
   }
   ```

4. **refreshProfilePicture()** - New helper method to manually refresh
   ```kotlin
   fun refreshProfilePicture() {
       viewModelScope.launch {
           val userId = authRepository.getCurrentUserId() ?: return@launch
           loadProfilePictureUrl(userId)
       }
   }
   ```

## How It Works
- Each time the profile picture changes, a new URL with a unique timestamp is generated
- Coil treats each URL as unique and doesn't use the cache
- The OS still serves the same file from disk, so performance is minimal
- Users see their changes immediately in both drawer and homescreen

## Testing
1. Open the app and navigate to the drawer
2. Click the edit button on the profile picture
3. Select a new image
4. The profile picture should update immediately in:
   - Navigation Drawer header
   - HomeScreen profile circle

## Affected Components
- **Drawer**: Profile section in ModalDrawerSheet
- **HomeScreen**: HomeHeader profile circle with dropdown menu
- Both now display the updated picture immediately
