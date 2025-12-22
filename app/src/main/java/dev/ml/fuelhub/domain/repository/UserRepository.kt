package dev.ml.fuelhub.domain.repository

import dev.ml.fuelhub.data.model.User
import dev.ml.fuelhub.data.model.UserRole

/**
 * Repository interface for user operations.
 */
interface UserRepository {
    
    /**
     * Get user by ID.
     */
    suspend fun getUserById(id: String): User?
    
    /**
     * Get user by username.
     */
    suspend fun getUserByUsername(username: String): User?
    
    /**
     * Create a new user.
     */
    suspend fun createUser(user: User): User
    
    /**
     * Update user information.
     */
    suspend fun updateUser(user: User): User
    
    /**
     * Get all users by role.
     */
    suspend fun getUsersByRole(role: UserRole): List<User>
    
    /**
     * Get all active users.
     */
    suspend fun getAllActiveUsers(): List<User>
    
    /**
     * Get all users (including inactive).
     */
    suspend fun getAllUsers(): List<User>
    
    /**
     * Deactivate a user.
     */
    suspend fun deactivateUser(userId: String): User?
}
