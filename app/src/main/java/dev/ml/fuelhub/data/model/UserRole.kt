package dev.ml.fuelhub.data.model

/**
 * Enum representing user roles for role-based access control.
 * Determines permissions for transaction creation and approval.
 */
enum class UserRole {
    ADMIN,         // Full system access, can approve transactions
    DISPATCHER,    // Can create and view transactions
    ENCODER,       // Can create and view transactions
    GAS_STATION,   // Can scan QR codes and confirm fuel dispensing
    VIEWER         // Read-only access to reports and history
}
