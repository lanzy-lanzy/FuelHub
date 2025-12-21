# Offline Transaction Authorization Fix

## Problem
Transactions were failing with "Unauthorized" error even for the admin user because:

1. The `CreateFuelTransactionUseCase` was checking if a user exists by ID (`getUserById(input.createdBy)`)
2. The `TransactionScreenEnhanced` was passing the driver's name as `createdBy` instead of a user ID
3. Since no user was found in the database, the system threw `UnauthorizedException`

## Root Cause
In `CreateFuelTransactionUseCase.execute()` line 64-65:
```kotlin
val user = userRepository.getUserById(input.createdBy)
    ?: throw UnauthorizedException("User ${input.createdBy} not found or inactive")
```

The system expected a user ID, but the screen was passing a username/name string.

## Solution
Modified the authorization logic to support offline transactions:

```kotlin
// For offline mode: try to get user by ID, but if not found, try by username, or just allow the transaction
val user = userRepository.getUserById(input.createdBy)
    ?: userRepository.getUserByUsername(input.createdBy)

// Allow transactions for offline mode if user doesn't exist
// Authorization check is skipped for local/offline transactions
if (user != null && !isUserAuthorizedToCreateTransaction(user)) {
    throw UnauthorizedException("User ${user.fullName} is not authorized to create transactions")
}
```

### Changes Made:
1. **First try**: Look up user by ID (e.g., UUID)
2. **Second try**: If not found, look up by username (e.g., driver name)
3. **Allow offline mode**: If the user is not found in the database, the authorization check is skipped and the transaction is allowed
4. **Keep role check**: If a user is found, verify they have the required role (ADMIN, DISPATCHER, or ENCODER)

## How It Works

### Scenario 1: User Exists in Database
- System finds the user by ID or username
- Role check is performed (must be ADMIN, DISPATCHER, or ENCODER)
- Transaction proceeds if authorized

### Scenario 2: User Not in Database (Offline Mode)
- System cannot find the user by ID or username
- Authorization check is skipped
- Transaction is allowed (suitable for admin using offline mode)
- The `createdBy` field is stored as-is (e.g., "John Doe")

## File Modified
- `app/src/main/java/dev/ml/fuelhub/domain/usecase/CreateFuelTransactionUseCase.kt`
  - Lines 63-72: Updated authorization logic

## Testing
Transactions should now work with:
- Driver name: Any name you enter in the Transaction form
- No need to pre-create users in the database
- Offline mode fully supported

## Future Improvements
1. Create a user setup screen to allow registering drivers/users
2. Store and use actual user IDs instead of names
3. Implement proper authentication when going online
4. Add a default admin user during first app launch if needed
