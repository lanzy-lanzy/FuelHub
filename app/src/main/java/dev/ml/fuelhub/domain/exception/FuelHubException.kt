package dev.ml.fuelhub.domain.exception

/**
 * Base exception class for FuelHub application.
 */
sealed class FuelHubException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Thrown when fuel wallet balance is insufficient for a transaction.
 */
class InsufficientFuelException(message: String) : FuelHubException(message)

/**
 * Thrown when a transaction cannot be processed due to validation failure.
 */
class TransactionValidationException(message: String) : FuelHubException(message)

/**
 * Thrown when an unauthorized user attempts to perform a restricted action.
 */
class UnauthorizedException(message: String) : FuelHubException(message)

/**
 * Thrown when a required entity is not found.
 */
class EntityNotFoundException(message: String) : FuelHubException(message)

/**
 * Thrown when a transaction fails during processing.
 */
class TransactionProcessingException(message: String) : FuelHubException(message)

/**
 * Thrown when a database operation fails.
 */
class DatabaseException(message: String, cause: Throwable? = null) : FuelHubException(message, cause)
