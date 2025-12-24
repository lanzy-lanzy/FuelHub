package dev.ml.fuelhub.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import dev.ml.fuelhub.domain.repository.NotificationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Example integration of FCM token storage with authentication.
 * 
 * This example shows where and how to:
 * 1. Request POST_NOTIFICATIONS permission (Android 13+)
 * 2. Get and store FCM token after successful login
 * 
 * Copy the relevant parts into your actual Auth/Login Activity/ViewModel.
 */
class FCMAuthIntegrationExample @Inject constructor(
    private val notificationRepository: NotificationRepository
) {

    // ============================================================================
    // EXAMPLE 1: REQUEST NOTIFICATION PERMISSION (Android 13+)
    // ============================================================================
    // Add this to your Activity/Fragment:

    fun setupNotificationPermissionLauncher(): androidx.activity.result.ActivityResultLauncher<String> {
        // If you're in an Activity:
        // return registerForActivityResult(
        //     ActivityResultContracts.RequestPermission()
        // ) { isGranted ->
        //     if (isGranted) {
        //         Timber.d("Notification permission granted")
        //     } else {
        //         Timber.d("Notification permission denied")
        //     }
        // }
        
        // This is just for example - implement in your Activity
        throw NotImplementedError("Implement in your Activity")
    }

    fun requestNotificationPermission(activity: AppCompatActivity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Request permission (implement the launcher in your Activity)
                // permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                Timber.d("POST_NOTIFICATIONS permission requested")
            } else {
                Timber.d("POST_NOTIFICATIONS permission already granted")
            }
        }
    }

    // ============================================================================
    // EXAMPLE 2: GET AND STORE FCM TOKEN AFTER LOGIN
    // ============================================================================
    // Add this to your login/authentication success handler:

    /**
     * Store FCM token after successful Firebase authentication.
     * Call this after FirebaseAuth.signInWithEmailAndPassword() succeeds.
     * 
     * Usage:
     * ```
     * authRepository.login(email, password).collect { result ->
     *     when (result) {
     *         is AuthResult.Success -> {
     *             val userId = result.user.id
     *             storeFcmToken(userId)  // <-- Call this
     *         }
     *         is AuthResult.Error -> {
     *             // Handle error
     *         }
     *     }
     * }
     * ```
     */
    fun storeFcmToken(userId: String) {
        Timber.d("Attempting to store FCM token for user: $userId")
        
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Timber.d("FCM Token obtained: ${token.take(20)}...")
                
                // Store token in Firestore via repository
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val success = notificationRepository.storeUserFcmToken(userId, token)
                        if (success) {
                            Timber.d("✓ FCM token stored successfully for user: $userId")
                        } else {
                            Timber.w("✗ Failed to store FCM token for user: $userId")
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Error storing FCM token for user: $userId")
                    }
                }
            } else {
                Timber.e(task.exception, "Failed to get FCM token")
            }
        }
    }

    // ============================================================================
    // EXAMPLE 3: COMPLETE LOGIN FLOW WITH TOKEN STORAGE
    // ============================================================================

    /**
     * Example complete login flow with FCM token storage.
     * Integrate this pattern into your actual login flow.
     */
    fun completeLoginFlowExample(
        email: String,
        password: String,
        firebaseAuth: FirebaseAuth
    ) {
        Timber.d("Starting login flow for email: $email")
        
        // Step 1: Authenticate with Firebase
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val userId = authResult.user?.uid
                Timber.d("✓ Authentication successful. User ID: $userId")
                
                if (userId != null) {
                    // Step 2: Store FCM token
                    storeFcmToken(userId)
                    
                    // Step 3: Continue with login flow
                    // navigateToHome()
                }
            }
            .addOnFailureListener { exception ->
                Timber.e(exception, "✗ Authentication failed")
                // Handle error
            }
    }

    // ============================================================================
    // EXAMPLE 4: REFRESH TOKEN ON APP STARTUP
    // ============================================================================

    /**
     * Optionally refresh FCM token when app starts (if user is already logged in).
     * Call this in onCreate of your MainActivity or splash screen.
     */
    fun refreshFcmTokenIfNeeded(userId: String?) {
        if (userId != null) {
            Timber.d("Refreshing FCM token for logged-in user: $userId")
            storeFcmToken(userId)
        }
    }
}

// ============================================================================
// IMPLEMENTATION EXAMPLES IN DIFFERENT COMPONENTS
// ============================================================================

/**
 * Example 1: In your AuthViewModel
 */
example_auth_viewmodel {
    @Inject
    lateinit var notificationRepository: NotificationRepository

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                // Your login code here
                val userId = "user_id_from_auth"
                
                // Store FCM token after successful login
                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val token = task.result
                        viewModelScope.launch {
                            notificationRepository.storeUserFcmToken(userId, token)
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Login failed")
            }
        }
    }
}

/**
 * Example 2: In your LoginActivity
 */
example_login_activity {
    /*
    class LoginActivity : AppCompatActivity() {
        
        @Inject
        lateinit var notificationRepository: NotificationRepository
        
        private val notificationPermissionLauncher = 
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    Timber.d("Notification permission granted")
                }
            }
        
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            
            // Request notification permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        
        private fun handleLoginSuccess(userId: String) {
            // Store FCM token
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    lifecycleScope.launch {
                        notificationRepository.storeUserFcmToken(userId, token)
                    }
                }
            }
            
            // Navigate to home screen
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
    */
}

/**
 * Example 3: In your SplashScreen (for already logged-in users)
 */
example_splash_screen {
    /*
    class SplashActivity : AppCompatActivity() {
        
        @Inject
        lateinit var notificationRepository: NotificationRepository
        
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            
            // Check if user is already logged in
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                // Refresh FCM token for already logged-in user
                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val token = task.result
                        lifecycleScope.launch {
                            notificationRepository.storeUserFcmToken(currentUser.uid, token)
                        }
                    }
                }
            }
            
            // Navigate after short delay
            Handler(Looper.getMainLooper()).postDelayed({
                if (currentUser != null) {
                    startActivity(Intent(this, MainActivity::class.java))
                } else {
                    startActivity(Intent(this, LoginActivity::class.java))
                }
                finish()
            }, 2000)
        }
    }
    */
}

// ============================================================================
// COMPLETE INTEGRATION IN YOUR ANDROID MANIFEST
// ============================================================================

/*
<manifest>
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    
    <application>
        <!-- Your Activities here -->
        
        <!-- FCM Service (already added, do not duplicate) -->
        <service
            android:name=".service.FuelHubMessagingService"
            android:exported="false">
            <intent-filter>
                <action android:name="com.google.firebase.MESSAGING_EVENT" />
            </intent-filter>
        </service>
    </application>
</manifest>
*/

// ============================================================================
// FIREBASE CONSOLE TESTING
// ============================================================================

/*
To test notifications manually via Firebase Console:

1. Go to Firebase Console (https://console.firebase.google.com)
2. Select your FuelHub project
3. Go to Messaging tab
4. Click "Create campaign" or "Send your first message"
5. Enter:
   - Title: "Test Notification"
   - Body: "This is a test message"
6. Under "Target", select:
   - Android app
   - Package name: dev.ml.fuelhub
7. Click "Next"
8. Click "Send test message"
9. Select your device from the list
10. Notification should appear on device

Check Firestore to verify:
- notifications collection has entry
- fcm_tokens collection has your user's token
*/

// ============================================================================
// DEBUGGING COMMANDS
// ============================================================================

/*
adb logcat | grep -i fcm                    # Filter FCM logs
adb logcat | grep FuelHubMessagingService   # Filter service logs
adb logcat | grep "Notification"            # Filter notification logs
adb shell settings put secure logging_level 1  # Enable debug logging
*/
