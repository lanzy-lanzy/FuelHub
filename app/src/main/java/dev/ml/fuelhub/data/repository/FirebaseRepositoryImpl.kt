package dev.ml.fuelhub.data.repository

/**
 * DEPRECATED: Consolidated FirebaseRepositoryImpl
 * 
 * This file previously contained all Firebase repository implementations in one file.
 * These have been split into separate, focused implementation files:
 * 
 * - FirebaseUserRepositoryImpl.kt
 * - FirebaseVehicleRepositoryImpl.kt
 * - FirebaseFuelWalletRepositoryImpl.kt
 * - FirebaseFuelTransactionRepositoryImpl.kt
 * - FirebaseGasSlipRepositoryImpl.kt
 * - FirebaseAuditLogRepositoryImpl.kt
 * 
 * See the DI module (RepositoryModule.kt) for binding.
 */

@Deprecated(
    message = "Use individual Firebase repository implementations instead",
    replaceWith = ReplaceWith("RepositoryModule")
)
class FirebaseRepositoryImpl {
    // DEPRECATED - Use separate implementation files
}
