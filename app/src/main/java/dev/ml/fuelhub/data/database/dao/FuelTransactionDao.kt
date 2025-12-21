package dev.ml.fuelhub.data.database.dao

/**
 * DEPRECATED: FuelTransactionDao for Room Database
 * 
 * This file is retained for historical reference only.
 * All database operations have been migrated to Firebase Firestore.
 * See FirebaseFuelTransactionRepositoryImpl.kt for current implementation.
 */

@Deprecated(
    message = "Use Firebase Firestore instead. Room Database is deprecated.",
    replaceWith = ReplaceWith("FirebaseFuelTransactionRepositoryImpl")
)
interface FuelTransactionDao {
    // DEPRECATED - Use Firebase Firestore
}
