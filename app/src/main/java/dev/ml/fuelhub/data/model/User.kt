package dev.ml.fuelhub.data.model

import java.time.LocalDateTime

/**
 * Represents an authorized system user.
 * Only authorized users can create or approve transactions.
 */
data class User(
    val id: String,
    val username: String,
    val email: String,
    val fullName: String,
    val role: UserRole,
    val officeId: String,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime
)
