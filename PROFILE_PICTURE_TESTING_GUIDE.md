# Profile Picture Upload - Testing Guide

## Pre-Testing Checklist
- [ ] Build completed successfully with 0 errors
- [ ] All dependencies installed (Firebase Storage, Coil)
- [ ] Firebase project configured with Storage enabled
- [ ] Firestore Security Rules updated
- [ ] Storage Security Rules updated

## Test Scenarios

### Test 1: Initial Login and Profile Load
**Objective**: Verify profile picture loads from Firestore on app startup

**Steps**:
1. Clear app data or first-time install
2. Launch app and register new user
3. Open Navigation Drawer
4. Verify: Profile picture shows default Person icon (no profile picture yet)

**Expected Result**: ✓ Default person icon displayed in circular avatar

---

### Test 2: Upload Profile Picture
**Objective**: Verify image selection and upload to Firebase Storage

**Steps**:
1. While logged in, click edit button (pencil icon) on profile picture
2. Image picker opens, select a photo from device
3. Wait for upload to complete
4. Check Timber logs for "Profile picture updated successfully" message

**Expected Result**: 
- ✓ Profile picture displays in drawer
- ✓ Logs show upload success
- ✓ Profile picture updates in real-time

---

### Test 3: Profile Picture Persistence
**Objective**: Verify profile picture persists after app restart

**Steps**:
1. Ensure profile picture is uploaded (Test 2)
2. Close app completely (kill process)
3. Relaunch app
4. Open drawer

**Expected Result**: 
- ✓ Profile picture loads immediately
- ✓ No need to re-upload
- ✓ Consistent across all screens (Drawer, HomeScreen)

---

### Test 4: Multiple Users
**Objective**: Verify each user has separate profile picture

**Steps**:
1. Upload profile picture as User A
2. Logout
3. Login as User B
4. Verify: Profile picture shows default Person icon for User B
5. Upload different profile picture for User B
6. Logout and login as User A
7. Verify: User A's original picture displays

**Expected Result**:
- ✓ Each user has isolated profile picture storage
- ✓ No picture conflicts between users
- ✓ Correct pictures load per user

---

### Test 5: Network Error Handling
**Objective**: Verify graceful handling of network failures

**Steps**:
1. Enable airplane mode or disable network
2. Attempt to upload profile picture
3. Observe error handling
4. Re-enable network and verify recovery

**Expected Result**:
- ✓ Error logged in Timber
- ✓ UI shows appropriate fallback (Person icon)
- ✓ No app crash
- ✓ User can retry after reconnecting

---

### Test 6: Large Image Upload
**Objective**: Verify app handles large image files

**Steps**:
1. Select a high-resolution image (>5MB)
2. Attempt upload
3. Monitor performance and memory usage
4. Wait for completion

**Expected Result**:
- ✓ Image compresses appropriately
- ✓ Upload completes within reasonable time
- ✓ No out-of-memory errors
- ✓ Final stored image is reasonable size

---

### Test 7: Image Format Compatibility
**Objective**: Verify various image formats are handled

**Steps**:
1. Upload images in different formats:
   - JPEG (.jpg)
   - PNG (.png)
   - WebP (.webp)
2. Verify each uploads and displays correctly

**Expected Result**:
- ✓ All formats upload successfully
- ✓ All formats display without artifacts
- ✓ No format-specific errors in logs

---

## Logging & Debugging

### Check Upload Logs
Monitor Timber output for these expected messages:

**Success Flow**:
```
Image selected: content://...
Image uploaded to Firebase Storage
Loaded profile picture URL: https://...
Profile picture URL updated: https://...
Profile picture updated successfully: https://...
```

**Error Flow**:
```
User not authenticated
Error uploading profile picture: ...
Failed to fetch profile picture URL for: userId
```

### Firebase Console Checks

1. **Storage Bucket**:
   - Navigate to Firebase Console → Storage
   - Verify files under `profiles/{userId}/profile.jpg`
   - Check file sizes are reasonable

2. **Firestore**:
   - Navigate to Firebase Console → Firestore
   - Open `users` collection
   - Verify `profilePictureUrl` field contains valid download URLs

---

## Performance Metrics

| Metric | Target | Acceptable |
|--------|--------|-----------|
| Upload Time (1MB image) | < 2 sec | < 5 sec |
| Download Time (Display) | < 1 sec | < 2 sec |
| Memory Usage | < 50MB | < 100MB |
| App Launch Time | < 3 sec | < 5 sec |

---

## Common Issues & Fixes

### Issue: Profile Picture Not Showing
**Possible Causes**:
- [ ] Firestore Security Rules not updated
- [ ] Storage Security Rules not updated
- [ ] Profile picture URL field not in Firestore
- [ ] Coil dependency not properly installed

**Fix**:
```kotlin
// Verify in Firebase rules
allow read: if request.auth.uid == userId;
allow write: if request.auth.uid == userId;
```

### Issue: Upload Fails Silently
**Possible Causes**:
- [ ] User not authenticated
- [ ] Storage path permissions issue
- [ ] Network connectivity issue

**Fix**:
- Check Timber logs for exact error message
- Verify `getCurrentUserId()` returns valid UID
- Test with good network connection first

### Issue: Image Quality Loss
**Possible Causes**:
- [ ] Default compression in storage
- [ ] Coil scaling/formatting settings

**Fix**:
- Verify image format in Firebase Storage
- Check file size in Storage bucket

---

## Test Completion Checklist
- [ ] Test 1: Initial Login - PASS/FAIL
- [ ] Test 2: Upload Profile Picture - PASS/FAIL
- [ ] Test 3: Profile Picture Persistence - PASS/FAIL
- [ ] Test 4: Multiple Users - PASS/FAIL
- [ ] Test 5: Network Error Handling - PASS/FAIL
- [ ] Test 6: Large Image Upload - PASS/FAIL
- [ ] Test 7: Image Format Compatibility - PASS/FAIL
- [ ] All logs reviewed and clean
- [ ] Firebase Console data verified
- [ ] Performance metrics within acceptable range

---

## Notes for QA

1. **Authentication Required**: All tests assume user is logged in before drawer access
2. **Firebase Setup**: Ensure Security Rules are configured before testing
3. **Test Data**: Use realistic image sizes (100KB - 2MB typical)
4. **Cleanup**: Clear test profiles from Firestore/Storage after testing
5. **Device Testing**: Test on both physical devices and emulator

---

## Success Criteria
✅ Profile picture uploads successfully
✅ Profile picture persists across app sessions
✅ Profile picture displays in all UI locations (Drawer, HomeScreen)
✅ Users cannot access other users' profile pictures
✅ App handles network errors gracefully
✅ No compilation warnings or errors
✅ Timber logs show expected messages
✅ Firestore and Storage contain correct data
