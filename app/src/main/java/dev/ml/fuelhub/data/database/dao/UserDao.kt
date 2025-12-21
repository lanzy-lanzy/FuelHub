package dev.ml.fuelhub.data.database.dao

/**
 * DEPRECATED: UserDao for Room Database
 * 
 * This file is retained for historical reference only.
 * All database operations have been migrated to Firebase Firestore.
 * See FirebaseUserRepositoryImpl.kt for current implementation.
 */

@Deprecated(
    message = "Use Firebase Firestore instead. Room Database is deprecated.",
    replaceWith = ReplaceWith("FirebaseUserRepositoryImpl")
)
interface UserDao {
    // DEPRECATED - Use Firebase Firestore
}
