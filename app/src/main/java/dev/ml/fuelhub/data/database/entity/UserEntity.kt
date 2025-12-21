package dev.ml.fuelhub.data.database.entity

/**
 * DEPRECATED: UserEntity for Room Database
 * 
 * This file is retained for historical reference only.
 * Use dev.ml.fuelhub.data.model.User instead with Firebase Firestore.
 */

@Deprecated(
    message = "Use dev.ml.fuelhub.data.model.User with Firebase Firestore",
    replaceWith = ReplaceWith("User")
)
data class UserEntity(val id: String = "")
