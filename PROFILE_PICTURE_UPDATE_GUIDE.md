# Profile Picture Update Implementation Guide

## Overview
Guide to implement profile picture upload and display functionality in the drawer header.

## Current State
- Profile picture placeholder with Person icon
- Edit button ready for implementation
- Connected to drawer header

## Step 1: Add Image Picker Permission

### AndroidManifest.xml
```xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
```

### MainActivity (Update to handle image selection)
```kotlin
private val imagePickerLauncher = registerForActivityResult(
    ActivityResultContracts.GetContent()
) { uri: Uri? ->
    uri?.let { 
        // Handle selected image
        uploadProfilePicture(it)
    }
}
```

## Step 2: Update Edit Button Click Handler

### Current Implementation
```kotlin
IconButton(
    onClick = { /* Handle profile picture update */ },
    modifier = Modifier.size(40.dp)
)
```

### Updated Implementation
```kotlin
IconButton(
    onClick = { 
        imagePickerLauncher.launch("image/*")
    },
    modifier = Modifier.size(40.dp)
)
```

## Step 3: Add Profile Picture Upload Function

### In MainActivity
```kotlin
private fun uploadProfilePicture(imageUri: Uri) {
    val userId = authViewModel?.currentUser?.value?.id ?: return
    val storageRef = Firebase.storage.reference
    val imageRef = storageRef.child("profiles/$userId/profile.jpg")
    
    val uploadTask = imageRef.putFile(imageUri)
    
    uploadTask.addOnSuccessListener {
        imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
            // Update user profile with image URL
            authViewModel?.updateProfilePicture(downloadUri.toString())
        }
    }.addOnFailureListener { exception ->
        // Handle error
        println("Upload failed: ${exception.message}")
    }
}
```

## Step 4: Update Drawer Header to Display Profile Picture

### Replace Icon with AsyncImage

**Before:**
```kotlin
Box(
    modifier = Modifier
        .size(64.dp)
        .clip(CircleShape)
        .background(Color.White.copy(alpha = 0.3f)),
    contentAlignment = Alignment.Center
) {
    Icon(
        imageVector = Icons.Default.Person,
        contentDescription = "Profile",
        modifier = Modifier.size(36.dp),
        tint = Color.White
    )
}
```

**After:**
```kotlin
val currentUser by authViewModel?.currentUser?.collectAsState()

Box(
    modifier = Modifier
        .size(64.dp)
        .clip(CircleShape)
        .background(Color.White.copy(alpha = 0.3f)),
    contentAlignment = Alignment.Center
) {
    if (!currentUser?.profilePictureUrl.isNullOrEmpty()) {
        AsyncImage(
            model = currentUser?.profilePictureUrl,
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
            loading = {
                // Show loading placeholder
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = VibrantCyan
                )
            },
            error = {
                // Show error placeholder
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    modifier = Modifier.size(36.dp),
                    tint = Color.White
                )
            }
        )
    } else {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Profile",
            modifier = Modifier.size(36.dp),
            tint = Color.White
        )
    }
}
```

## Step 5: Update AuthViewModel

### Add Profile Picture Functions
```kotlin
class AuthViewModel : ViewModel() {
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    
    fun updateProfilePicture(imageUrl: String) {
        val userId = _currentUser.value?.id ?: return
        val userRef = Firebase.firestore.collection("users").document(userId)
        
        userRef.update("profilePictureUrl", imageUrl)
            .addOnSuccessListener {
                // Update local state
                _currentUser.value = _currentUser.value?.copy(
                    profilePictureUrl = imageUrl
                )
            }
            .addOnFailureListener { exception ->
                println("Failed to update profile: ${exception.message}")
            }
    }
}
```

## Step 6: Add Required Dependencies

### build.gradle.kts
```kotlin
dependencies {
    // Image loading
    implementation("io.coil-kt:coil-compose:2.4.0")
    
    // Firebase Storage
    implementation("com.google.firebase:firebase-storage-ktx")
    
    // Activity Result Contracts
    implementation("androidx.activity:activity-ktx:1.7.2")
    implementation("androidx.fragment:fragment-ktx:1.6.0")
}
```

### Add imports to MainActivity
```kotlin
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import com.google.firebase.storage.ktx.storage
import coil.compose.AsyncImage
import androidx.compose.material3.CircularProgressIndicator
```

## Step 7: Add User Data Model

### Update User data class
```kotlin
data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val profilePictureUrl: String? = null,
    val lastUpdated: Long = System.currentTimeMillis()
)
```

## Implementation Checklist

- [ ] Add image picker permission to AndroidManifest.xml
- [ ] Add imagePickerLauncher to MainActivity
- [ ] Update edit button onClick handler
- [ ] Implement uploadProfilePicture() function
- [ ] Add AsyncImage with proper error handling
- [ ] Update AuthViewModel with updateProfilePicture()
- [ ] Add required dependencies
- [ ] Add required imports
- [ ] Update User data class
- [ ] Test image selection flow
- [ ] Test image upload to Firebase
- [ ] Verify profile picture displays after upload
- [ ] Test error cases

## Testing Guide

### Manual Testing Steps

1. **Open App**
   - Verify drawer displays with placeholder profile picture

2. **Tap Edit Button**
   - Image picker should open
   - Select an image from device

3. **Upload Process**
   - Show loading indicator
   - Upload to Firebase Storage
   - Update user profile in Firestore

4. **Display Picture**
   - Profile picture should display in drawer header
   - Should be circular (64dp)
   - Should load from cloud URL

5. **Persistence**
   - Close and reopen app
   - Profile picture should still display
   - Verify cached locally

## Common Issues & Solutions

### Issue: Image picker not opening
**Solution**: Ensure activity result launcher is initialized before use

### Issue: Upload fails
**Solution**: Check Firebase Storage rules allow authenticated users

### Issue: Image not displaying
**Solution**: Verify image URL is accessible, check Coil configuration

### Issue: Permission denied
**Solution**: Request runtime permissions for Android 6.0+

## Security Considerations

1. **Authentication Check**
   ```kotlin
   private fun uploadProfilePicture(imageUri: Uri) {
       if (!isUserAuthenticated()) {
           navigateToLogin()
           return
       }
       // Continue with upload
   }
   ```

2. **File Size Validation**
   ```kotlin
   val maxSizeInBytes = 5 * 1024 * 1024 // 5MB
   if (file.size() > maxSizeInBytes) {
       showError("Image too large")
       return
   }
   ```

3. **Secure URL Storage**
   - Use Firebase Storage instead of direct URLs
   - Implement URL expiration
   - Use signed URLs for security

## Performance Optimization

1. **Image Compression**
   ```kotlin
   fun compressImage(imageUri: Uri): File {
       // Compress before upload
       // Reduce file size
       // Maintain quality
   }
   ```

2. **Caching**
   ```kotlin
   // Coil automatically caches images
   AsyncImage(
       model = url,
       modifier = Modifier.size(64.dp),
       // Cache is automatically handled
   )
   ```

3. **Lazy Loading**
   ```kotlin
   // Use loading state for placeholder
   loading = {
       CircularProgressIndicator()
   }
   ```

## Future Enhancements

- [ ] Image cropping before upload
- [ ] Multiple profile pictures (gallery)
- [ ] Image filters
- [ ] Share profile picture
- [ ] QR code with profile link
- [ ] Profile picture frames/borders

## References

- [Firebase Storage Documentation](https://firebase.google.com/docs/storage)
- [Coil Image Loading Library](https://coil-kt.github.io/coil/)
- [Android Activity Result Contracts](https://developer.android.com/training/data-storage/shared/photopicker)
