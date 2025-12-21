package dev.ml.fuelhub.data.database.entity

/**
 * DEPRECATED: VehicleEntity for Room Database
 * 
 * This file is retained for historical reference only.
 * Use dev.ml.fuelhub.data.model.Vehicle instead with Firebase Firestore.
 */

@Deprecated(
    message = "Use dev.ml.fuelhub.data.model.Vehicle with Firebase Firestore",
    replaceWith = ReplaceWith("Vehicle")
)
data class VehicleEntity(val id: String = "")
