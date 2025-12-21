package dev.ml.fuelhub.data.database.entity

/**
 * DEPRECATED: AuditLogEntity for Room Database
 * 
 * This file is retained for historical reference only.
 * Use dev.ml.fuelhub.data.model.AuditLog instead with Firebase Firestore.
 */

@Deprecated(
    message = "Use dev.ml.fuelhub.data.model.AuditLog with Firebase Firestore",
    replaceWith = ReplaceWith("AuditLog")
)
data class AuditLogEntity(val id: String = "")
