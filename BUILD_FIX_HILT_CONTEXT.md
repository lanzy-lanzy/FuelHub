# Build Fix: Hilt Context Injection for AuthViewModel

## Problem
Build failed with Dagger/Hilt error:
```
[Dagger/MissingBinding] android.content.Context cannot be provided without an @Provides-annotated method.
```

The `AuthViewModel` was trying to inject `Context` directly, but Hilt for ViewModels doesn't automatically provide plain `Context` objects.

## Solution
Updated `AuthViewModel.kt` to use `@ApplicationContext` annotation:

1. Added import:
   ```kotlin
   import dagger.hilt.android.qualifiers.ApplicationContext
   ```

2. Updated constructor:
   ```kotlin
   @HiltViewModel
   class AuthViewModel @Inject constructor(
       private val authRepository: AuthRepository,
       @ApplicationContext private val context: Context
   ) : ViewModel() {
   ```

The `@ApplicationContext` qualifier tells Hilt to inject the application-level context, which is always available and safe for use in ViewModels.

## Build Status
✅ **BUILD SUCCESSFUL** in 20s

APK Location: `app/build/outputs/apk/debug/app-debug.apk`

## Files Modified
- `app/src/main/java/dev/ml/fuelhub/presentation/viewmodel/AuthViewModel.kt`
