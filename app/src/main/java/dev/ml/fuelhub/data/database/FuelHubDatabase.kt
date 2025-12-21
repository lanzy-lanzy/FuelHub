package dev.ml.fuelhub.data.database

/**
 * DEPRECATED: FuelHub Room Database
 * 
 * This file is retained for historical reference only.
 * All database operations have been migrated to Firebase Firestore.
 * 
 * See FirebaseDataSource.kt and repository implementations for current implementation.
 */

@Deprecated(
    message = "Use Firebase Firestore instead. See dev.ml.fuelhub.data.firebase.FirebaseDataSource",
    replaceWith = ReplaceWith("FirebaseDataSource")
)
class FuelHubDatabase {
    // DEPRECATED - Use Firebase Firestore
}
