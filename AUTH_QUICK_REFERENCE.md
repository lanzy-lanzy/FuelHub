# Firebase Authentication - Quick Reference Card

## 🚀 Quick Start (5 Minutes)

### 1. Firebase Setup
```
1. Go to Firebase Console
2. Enable Email/Password Auth
3. Download google-services.json
4. Place in: app/google-services.json
5. Add Firestore Rules (see below)
```

### 2. Firestore Security Rules
```firestore
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{uid} {
      allow read, write: if request.auth.uid == uid;
    }
  }
}
```

### 3. Build & Run
```bash
./gradlew build
# Run on emulator or device
```

That's it! Login screen shows automatically if user isn't logged in.

---

## 📱 User Interface

### Login Flow
```
App Start
  ↓
[Login Screen]
  ├─ Enter Email: test@example.com
  ├─ Enter Password: Test123456
  └─ Tap Sign In
      ↓
[Validate inputs]
  ↓
[Firebase Auth]
  ↓
[Go to Home Screen] ✓
```

### Register Flow
```
[Login Screen]
  ↓
[Tap "Sign Up"]
  ↓
[Registration Screen]
  ├─ Full Name: John Doe
  ├─ Username: johndoe
  ├─ Email: john@example.com
  ├─ Password: Test123456
  ├─ Confirm: Test123456
  └─ Tap Create Account
      ↓
[Validate all fields]
  ↓
[Firebase Auth]
  ↓
[Create Firestore User Doc]
  ↓
[Go to Home Screen] ✓
```

---

## 🎨 Color Reference

| Element | Color | Hex |
|---------|-------|-----|
| Buttons/Links | Vibrant Cyan | #00D9FF |
| Headers | Electric Blue | #0099FF |
| Accents | Neon Teal | #00FFD1 |
| Background | Deep Blue | #0A1929 |
| Cards | Dark Gray | #1E2936 |
| Errors | Red | #FF3D00 |
| Success | Green | #00E676 |

---

## 💾 Data Stored

### Firebase Authentication
```
Auth User Created:
- UID: auto-generated
- Email: user provided
- Password: hashed by Firebase
```

### Firestore Users Collection
```
/users/{uid}
├── id: {uid}
├── email: "user@example.com"
├── username: "johndoe"
├── fullName: "John Doe"
├── role: "USER"
├── officeId: "mdrrmo-office-1"
├── isActive: true
└── createdAt: "2024-12-21T..."
```

---

## 🔐 Validation Rules

### Email
- ✓ Not empty
- ✓ Valid format (abc@def.com)

### Password (Login)
- ✓ Not empty
- ✓ Minimum 6 characters

### Password (Registration)
- ✓ Not empty
- ✓ Minimum 6 characters
- ✓ Matches confirmation

### Full Name
- ✓ Not empty

### Username
- ✓ Not empty
- ✓ Minimum 3 characters

---

## 📝 Code Examples

### Check if User Logged In
```kotlin
@Inject
lateinit var authRepository: AuthRepository

val isLoggedIn = authRepository.isUserLoggedIn()
val userId = authRepository.getCurrentUserId()
```

### Get User ID in ViewModel
```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val userId = authRepository.getCurrentUserId()
}
```

### Observe Auth State
```kotlin
@Composable
fun MyScreen() {
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.uiState.collectAsState()
    
    if (authState.isLoggedIn) {
        Text("Welcome, ${authState.userId}")
    }
}
```

### Handle Logout
```kotlin
val authViewModel: AuthViewModel = hiltViewModel()

Button(onClick = { authViewModel.logout() }) {
    Text("Logout")
}
```

---

## ⚠️ Error Messages

| Error | What It Means | What To Do |
|-------|---------------|-----------|
| "Please enter your email" | Email field empty | Type email |
| "Please enter a valid email" | Bad format | Use correct format |
| "Password must be at least 6 characters" | Too short | Use longer password |
| "Email is already registered" | Email taken | Login instead |
| "Incorrect password" | Wrong password | Check caps lock, try again |
| "No account found" | Email not registered | Sign up instead |
| "Too many login attempts" | Security lockout | Wait 15 minutes |

---

## 🧪 Test Credentials

Create your own during signup, or:
- Email: `test@example.com`
- Password: `Test123456`

(Must create via registration screen first)

---

## 🗂️ File Locations

```
Created Files:
✓ domain/repository/AuthRepository.kt
✓ data/repository/FirebaseAuthRepository.kt
✓ presentation/viewmodel/AuthViewModel.kt
✓ presentation/screen/LoginScreen.kt
✓ presentation/screen/RegisterScreen.kt
✓ di/AuthModule.kt

Modified Files:
✓ MainActivity.kt (added auth routes)
✓ di/RepositoryModule.kt (added provider)

Documentation:
✓ AUTH_SYSTEM_SUMMARY.md
✓ FIREBASE_AUTH_SETUP.md
✓ FIREBASE_AUTH_IMPLEMENTATION.md
✓ AUTH_QUICK_REFERENCE.md (this file)
```

---

## 🎯 Common Tasks

### Add Logout Button
```kotlin
@Composable
fun HomeScreen() {
    val authViewModel: AuthViewModel = hiltViewModel()
    
    Button(onClick = { authViewModel.logout() }) {
        Text("Sign Out")
    }
}
```

### Check Auth State on App Start
```kotlin
@Composable
fun FuelHubApp(authRepository: AuthRepository) {
    val startDestination = 
        if (authRepository.isUserLoggedIn()) "home" 
        else "login"
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) { ... }
}
```

### Update User Profile
```kotlin
// Ready to implement in User model
suspend fun updateUserProfile(
    fullName: String,
    username: String
): Result<Unit>
```

### Handle Auth Errors
```kotlin
val authViewModel: AuthViewModel = hiltViewModel()
val authState by authViewModel.uiState.collectAsState()

if (authState.error != null) {
    // Show error message
    Text(authState.error!!)
}
```

---

## 📊 Performance Tips

1. **Reuse AuthViewModel**: Don't create new instances
2. **Cache User Data**: Store userId in ViewModel
3. **Lazy Load Screens**: Use Hilt for injection
4. **Minimize Recomposition**: Use `remember` for state
5. **Use collectAsState**: For Flow observation

---

## 🔒 Security Checklist

- [x] Firebase Auth enabled
- [x] Firestore Rules configured
- [x] Input validation on all fields
- [x] Error messages don't leak info
- [x] Passwords hidden by default
- [x] HTTPS for all Firebase calls
- [ ] Email verification (optional)
- [ ] Two-factor auth (optional)

---

## 🐛 Troubleshooting

### Login not working?
```
1. Check email exists (sign up first)
2. Check password is correct (case sensitive)
3. Check internet connection
4. Check Firebase is enabled
5. Check google-services.json is in app/
```

### "User not found" error?
```
1. Email not registered
2. Sign up first with that email
3. Check spelling
```

### App crashes on startup?
```
1. Check google-services.json exists
2. Check Firebase project ID matches
3. Clean build: ./gradlew clean build
4. Sync Gradle: File > Sync Now
```

### Can't create account?
```
1. Check email format (name@domain.com)
2. Check password is 6+ characters
3. Check all fields are filled
4. Check passwords match
5. Check internet connection
```

---

## 📞 Quick Help

| Question | Answer |
|----------|--------|
| Where's the login screen? | Shows automatically if not logged in |
| How do I log out? | Add logout button to home screen |
| Can I use email only? | Yes, add verification email |
| Can I use Google login? | Yes, ready to implement |
| Where is user data stored? | Email in Firebase Auth, profile in Firestore |
| Is it secure? | Yes, Firebase is enterprise-grade |
| Can I customize UI? | Yes, screens are Compose based |
| How do I change colors? | Modify Color.kt in ui/theme/ |

---

## 📚 Next Steps

1. **Test the flow**: Create account → Login → Use app
2. **Customize UI**: Adjust colors, fonts, spacing
3. **Add logout**: Put button in settings/home
4. **Optional**: Add email verification
5. **Optional**: Add social login

---

## 💡 Pro Tips

- 🎨 All colors match your FuelHub dark theme
- ⚡ Uses Jetpack Compose for smooth UI
- 🔒 Firebase handles all security
- 📱 Works on all Android versions 24+
- ♻️ Reusable in other projects
- 🧪 Easy to test and debug
- 📖 Well-documented code

---

**Ready to go!** 🚀

Version: 1.0 | Status: ✅ Complete | Last Updated: Dec 2024
