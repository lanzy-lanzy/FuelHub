package dev.ml.fuelhub.data.database.dao

/**
 * DEPRECATED: GasSlipDao for Room Database
 * 
 * This file is retained for historical reference only.
 * All database operations have been migrated to Firebase Firestore.
 * See FirebaseGasSlipRepositoryImpl.kt for current implementation.
 */

@Deprecated(
    message = "Use Firebase Firestore instead. Room Database is deprecated.",
    replaceWith = ReplaceWith("FirebaseGasSlipRepositoryImpl")
)
interface GasSlipDao {
    // DEPRECATED - Use Firebase Firestore
}
