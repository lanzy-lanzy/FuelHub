package dev.ml.fuelhub.data.database.dao

/**
 * DEPRECATED: FuelWalletDao for Room Database
 * 
 * This file is retained for historical reference only.
 * All database operations have been migrated to Firebase Firestore.
 * See FirebaseFuelWalletRepositoryImpl.kt for current implementation.
 */

@Deprecated(
    message = "Use Firebase Firestore instead. Room Database is deprecated.",
    replaceWith = ReplaceWith("FirebaseFuelWalletRepositoryImpl")
)
interface FuelWalletDao {
    // DEPRECATED - Use Firebase Firestore
}
