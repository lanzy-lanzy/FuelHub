package dev.ml.fuelhub

import android.app.Application
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * FuelHub Application class.
 * Initializes Firebase and other app-wide configurations.
 */
@HiltAndroidApp
class FuelHubApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Timber for logging
        try {
            Timber.plant(Timber.DebugTree())
        } catch (e: Exception) {
            // Timber already planted
        }
        
        // Initialize Firebase and enable offline persistence
        initializeFirebase()
    }
    
    private fun initializeFirebase() {
        try {
            val settings = firestoreSettings {
                // Enable offline data persistence
                isPersistenceEnabled = true
                // Set cache size to 100 MB
                setCacheSizeBytes(100 * 1024 * 1024)
            }
            Firebase.firestore.firestoreSettings = settings
            Timber.d("Firebase Firestore initialized with offline persistence enabled")
        } catch (e: Exception) {
            Timber.e(e, "Error initializing Firebase Firestore")
        }
    }
}
