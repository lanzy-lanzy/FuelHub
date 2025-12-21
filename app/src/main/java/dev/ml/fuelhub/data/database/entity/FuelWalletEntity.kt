package dev.ml.fuelhub.data.database.entity

/**
 * DEPRECATED: FuelWalletEntity for Room Database
 * 
 * This file is retained for historical reference only.
 * Use dev.ml.fuelhub.data.model.FuelWallet instead with Firebase Firestore.
 */

@Deprecated(
    message = "Use dev.ml.fuelhub.data.model.FuelWallet with Firebase Firestore",
    replaceWith = ReplaceWith("FuelWallet")
)
data class FuelWalletEntity(val id: String = "")
