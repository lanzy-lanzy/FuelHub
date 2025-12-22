# Profile Picture Testing - Quick Start

## How It Now Works

1. **Image Selection** → Saved to device storage only
2. **Path Stored** → In Firestore for persistence
3. **Immediate Display** → Shows in drawer and homescreen
4. **Survives Restarts** → Loaded from Firestore path on app restart

## Step-by-Step Testing

### 1. First Launch After Fix
```
✓ App starts
✓ No profile picture initially (shows person icon)
✓ Firestore loads for any saved profile picture
✓ Device storage checked for actual file
```

### 2. Change Profile Picture
```
In Drawer:
1. Tap edit button (pencil icon) on profile picture
2. Select image from gallery
3. Image should update IMMEDIATELY in drawer header
4. Image should also update in homescreen header
```

### 3. Verify Persistence
```
1. Close app completely
2. Reopen app
3. Profile picture should still be visible
4. Image loads from saved device file path
```

### 4. Firestore Verification (optional)
```
Firebase Console → users/{userId}
Check for:
  - profilePictureUrl: /data/data/.../files/profiles/userId.jpg
  - profilePictureUpdatedAt: [timestamp]
```

## What Gets Stored Where

### Device Storage
- **Location**: `/data/data/{app-package}/files/profiles/{userId}.jpg`
- **Size**: Up to 5MB per image
- **Access**: Private to app only
- **Cleanup**: Automatic on app uninstall

### Firestore
- **Collection**: `users`
- **Field**: `profilePictureUrl` (absolute file path)
- **Field**: `profilePictureUpdatedAt` (timestamp)
- **Purpose**: Recover file path after app reinstall

## Common Issues & Fixes

### Image doesn't appear immediately
- **Check**: Is image file being saved? (Check device storage)
- **Fix**: Relaunch app or wait a moment for Firestore sync

### Image shows as placeholder after restart
- **Check**: Does file exist at saved path?
- **Check**: Is Firestore path correct?
- **Fix**: Select new picture - should save correctly

### Old image still shows after selecting new one
- **Check**: Is upload actually happening? (Check logs)
- **Fix**: Ensure app has permission to read files from picker

### Image picker doesn't open
- **Check**: Is edit button clickable?
- **Check**: App permissions for file access
- **Fix**: Check app settings > Permissions > Files

## Logs to Look For

When working correctly, you should see:
```
D/AuthViewModel: Profile picture saved locally: /data/data/.../profiles/user-123.jpg
D/AuthViewModel: Profile picture saved locally and to Firestore: file:///data/data/.../profiles/user-123.jpg
D/AuthViewModel: Fetched user role: DISPATCHER, Profile picture: file:///data/data/.../profiles/user-123.jpg
```

Error logs to investigate:
```
E/AuthViewModel: Failed to save profile picture locally
E/AuthViewModel: Failed to save profile picture URL to Firestore
E/AuthViewModel: Profile picture path in Firestore but file not found
```

## Testing Different Scenarios

### Scenario 1: New User
1. Register new account
2. No profile picture initially
3. Select image → saves to device and Firestore
4. Close and reopen → picture persists

### Scenario 2: Existing User
1. Login to existing account
2. Check if saved profile picture loads
3. Can update/change picture
4. New picture replaces old one

### Scenario 3: Multiple Selection
1. Change profile picture 3 times
2. Each change should overwrite previous file
3. Only latest image stored in device

### Scenario 4: Missing File
1. Manually delete image file from device
2. Logout and login
3. App should gracefully show placeholder icon
4. User can select new image

## Expected UI Behavior

### Drawer Header
```
[Profile Circle] [Edit Button]
  (Image)          (Pencil Icon)

- Circle shows image if available
- Shows person icon as fallback
- Edit button opens image picker on click
```

### HomeScreen Header
```
Welcome Back          [Profile Circle]
FuelHub                 (Image with
Fuel Management...      notification badge)

- Profile circle on top right
- Image with badge if available
- Person icon as fallback
```

Both locations update simultaneously.

## Success Criteria

- [x] Image picker opens and closes properly
- [x] Selected image saves to device
- [x] Path saves to Firestore
- [x] Image displays in drawer immediately
- [x] Image displays in homescreen immediately
- [x] Image persists after app restart
- [x] App gracefully handles missing files
- [x] No crashes or errors in logs
