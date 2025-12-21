package dev.ml.fuelhub.data.database.entity

/**
 * DEPRECATED: FuelTransactionEntity for Room Database
 * 
 * This file is retained for historical reference only.
 * Use dev.ml.fuelhub.data.model.FuelTransaction instead with Firebase Firestore.
 */

@Deprecated(
    message = "Use dev.ml.fuelhub.data.model.FuelTransaction with Firebase Firestore",
    replaceWith = ReplaceWith("FuelTransaction")
)
data class FuelTransactionEntity(val id: String = "")
