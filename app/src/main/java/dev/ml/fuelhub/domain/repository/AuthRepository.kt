package dev.ml.fuelhub.domain.repository

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun getCurrentUserId(): String?
    
    fun isUserLoggedIn(): Boolean
    
    suspend fun login(email: String, password: String): Result<String>
    
    suspend fun register(
        email: String,
        password: String,
        fullName: String,
        username: String
    ): Result<String>
    
    suspend fun logout(): Result<Unit>
    
    fun observeAuthState(): Flow<Boolean>
    
    suspend fun resetPassword(email: String): Result<Unit>
    
    suspend fun getUserRole(userId: String): String?
    
    suspend fun getUserFullName(userId: String): String?
}
