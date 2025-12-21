package dev.ml.fuelhub.data.database.entity

/**
 * DEPRECATED: GasSlipEntity for Room Database
 * 
 * This file is retained for historical reference only.
 * Use dev.ml.fuelhub.data.model.GasSlip instead with Firebase Firestore.
 */

@Deprecated(
    message = "Use dev.ml.fuelhub.data.model.GasSlip with Firebase Firestore",
    replaceWith = ReplaceWith("GasSlip")
)
data class GasSlipEntity(val id: String = "")
