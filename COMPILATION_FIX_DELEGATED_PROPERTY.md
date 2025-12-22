# Compilation Fix - Delegated Property Smart Cast Issue

## Error
```
Smart cast to 'dev.ml.fuelhub.data.model.FuelTransaction' is impossible, 
because 'matchedTransaction' is a delegated property.
```

## Root Cause
In Compose, when you use `by remember { mutableStateOf(...) }`, the property becomes a delegated property. Kotlin's compiler cannot smart cast delegated properties because it doesn't have access to the actual type at runtime.

## Solution
Instead of repeatedly checking and assigning to the delegated property `matchedTransaction`, we:

1. **Use a local variable** `foundTransaction` to hold the search result
2. **Do all the logic** with the local variable
3. **Assign to state** only after logic is complete

### Before (Broken)
```kotlin
matchedTransaction = transactions.find { ... }
if (matchedTransaction == null) {  // Smart cast attempted - ERROR
    matchedTransaction = transactions.find { ... }
    if (matchedTransaction != null) {  // Smart cast attempted - ERROR
        showConfirmDialog = true
    }
}
```

### After (Fixed)
```kotlin
var foundTransaction: FuelTransaction? = transactions.find { ... }
if (foundTransaction == null) {  // Local variable - OK
    foundTransaction = transactions.find { ... }
    if (foundTransaction != null) {  // Local variable - OK
        matchedTransaction = foundTransaction  // Assign to state only once
        showConfirmDialog = true
    }
} else {
    matchedTransaction = foundTransaction  // Assign to state only once
    showConfirmDialog = true
}
```

## Files Fixed
- `GasStationScreen.kt` - Line 75-107

## Key Lesson
When working with Compose delegated properties (via `remember { mutableStateOf() }`):
- ✅ Use them for final assignments
- ✅ Use local variables for logic and conditionals
- ❌ Don't try to smart cast delegated properties
- ❌ Don't use them in complex conditional chains

This is a best practice in Compose to avoid smart cast issues.
