package dev.ml.fuelhub.data.database.dao

/**
 * DEPRECATED: AuditLogDao for Room Database
 * 
 * This file is retained for historical reference only.
 * All database operations have been migrated to Firebase Firestore.
 * See FirebaseAuditLogRepositoryImpl.kt for current implementation.
 */

@Deprecated(
    message = "Use Firebase Firestore instead. Room Database is deprecated.",
    replaceWith = ReplaceWith("FirebaseAuditLogRepositoryImpl")
)
interface AuditLogDao {
    // DEPRECATED - Use Firebase Firestore
}
