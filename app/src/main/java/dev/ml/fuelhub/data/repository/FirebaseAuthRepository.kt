package dev.ml.fuelhub.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dev.ml.fuelhub.data.model.User
import dev.ml.fuelhub.data.model.UserRole
import dev.ml.fuelhub.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.time.LocalDateTime
import javax.inject.Inject

class FirebaseAuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    override fun getCurrentUserEmail(): String? {
        return firebaseAuth.currentUser?.email
    }

    override fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    override suspend fun login(email: String, password: String): Result<String> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid ?: throw Exception("Failed to get user ID")
            Timber.d("User logged in successfully: $email")
            Result.success(userId)
        } catch (e: Exception) {
            Timber.e(e, "Login failed for email: $email")
            Result.failure(e)
        }
    }

    override suspend fun register(
        email: String,
        password: String,
        fullName: String,
        username: String
    ): Result<String> {
        return try {
            // Create auth user
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: throw Exception("Failed to create user")

            // Create user document in Firestore
            val user = User(
                id = userId,
                username = username,
                email = email,
                fullName = fullName,
                role = UserRole.DISPATCHER,
                officeId = "mdrrmo-office-1",
                isActive = true,
                createdAt = LocalDateTime.now()
            )

            firestore.collection("users")
                .document(userId)
                .set(mapOf(
                    "id" to user.id,
                    "username" to user.username,
                    "email" to user.email,
                    "fullName" to user.fullName,
                    "role" to user.role.name,
                    "officeId" to user.officeId,
                    "isActive" to user.isActive,
                    "createdAt" to user.createdAt.toString()
                ))
                .await()

            Timber.d("User registered successfully: $email")
            Result.success(userId)
        } catch (e: Exception) {
            Timber.e(e, "Registration failed for email: $email")
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            firebaseAuth.signOut()
            Timber.d("User logged out successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Logout failed")
            Result.failure(e)
        }
    }

    override fun observeAuthState(): Flow<Boolean> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser != null)
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose {
            firebaseAuth.removeAuthStateListener(listener)
        }
    }

    override suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Timber.d("Password reset email sent to: $email")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Password reset failed for email: $email")
            Result.failure(e)
        }
    }
    
    override suspend fun getUserRole(userId: String): String? {
        return try {
            val doc = firestore.collection("users").document(userId).get().await()
            doc.getString("role")
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch user role for: $userId")
            null
        }
    }
    
    override suspend fun getUserFullName(userId: String): String? {
        return try {
            val doc = firestore.collection("users").document(userId).get().await()
            doc.getString("fullName")
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch user full name for: $userId")
            null
        }
    }
    
    override suspend fun getUserProfilePictureUrl(userId: String): String? {
        return try {
            val doc = firestore.collection("users").document(userId).get().await()
            doc.getString("profilePictureUrl")
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch profile picture URL for: $userId")
            null
        }
    }
    
    override suspend fun saveProfilePictureUrl(userId: String, picturePath: String): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(userId)
                .update(mapOf(
                    "profilePictureUrl" to picturePath,
                    "profilePictureUpdatedAt" to LocalDateTime.now().toString()
                ))
                .await()
            Timber.d("Profile picture URL saved for user: $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to save profile picture URL for: $userId")
            Result.failure(e)
        }
    }
}
