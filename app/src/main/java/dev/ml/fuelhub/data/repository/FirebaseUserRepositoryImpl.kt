package dev.ml.fuelhub.data.repository

import dev.ml.fuelhub.data.firebase.FirebaseDataSource
import dev.ml.fuelhub.data.model.User
import dev.ml.fuelhub.data.model.UserRole
import dev.ml.fuelhub.domain.repository.UserRepository
import kotlinx.coroutines.flow.first
import timber.log.Timber

/**
 * Firebase-based implementation of UserRepository.
 * Manages driver/user operations in Firestore.
 */
class FirebaseUserRepositoryImpl : UserRepository {
    
    override suspend fun getUserById(id: String): User? {
        return try {
            FirebaseDataSource.getUserById(id).getOrNull()
        } catch (e: Exception) {
            Timber.e(e, "Error getting user by ID: $id")
            null
        }
    }
    
    override suspend fun getUserByUsername(username: String): User? {
        return try {
            FirebaseDataSource.getUserByUsername(username).getOrNull()
        } catch (e: Exception) {
            Timber.e(e, "Error getting user by username: $username")
            null
        }
    }
    
    override suspend fun createUser(user: User): User {
        return try {
            FirebaseDataSource.createUser(user).getOrThrow()
            user
        } catch (e: Exception) {
            Timber.e(e, "Error creating user")
            throw e
        }
    }
    
    override suspend fun updateUser(user: User): User {
        return try {
            FirebaseDataSource.updateUser(user).getOrThrow()
            user
        } catch (e: Exception) {
            Timber.e(e, "Error updating user")
            throw e
        }
    }
    
    override suspend fun getUsersByRole(role: UserRole): List<User> {
        return try {
            // Get all users from real-time listener and filter by role
            val allUsers = FirebaseDataSource.getAllUsers().first()
            allUsers.filter { it.role == role && it.isActive }
        } catch (e: Exception) {
            Timber.e(e, "Error getting users by role: ${role.name}")
            emptyList()
        }
    }
    
    override suspend fun getAllActiveUsers(): List<User> {
        return try {
            // Get all users from real-time Firestore listener
            val allUsers = FirebaseDataSource.getAllUsers().first()
            allUsers.filter { it.isActive }
        } catch (e: Exception) {
            Timber.e(e, "Error getting all active users")
            emptyList()
        }
    }
    
    override suspend fun deactivateUser(userId: String): User? {
        return try {
            val user = getUserById(userId) ?: return null
            val inactiveUser = user.copy(isActive = false)
            FirebaseDataSource.updateUser(inactiveUser).getOrThrow()
            inactiveUser
        } catch (e: Exception) {
            Timber.e(e, "Error deactivating user: $userId")
            null
        }
    }
}
