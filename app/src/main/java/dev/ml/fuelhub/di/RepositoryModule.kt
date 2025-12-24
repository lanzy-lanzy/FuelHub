package dev.ml.fuelhub.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.ml.fuelhub.data.firebase.FirebaseDataSource
import dev.ml.fuelhub.data.repository.*
import dev.ml.fuelhub.data.service.NotificationService
import dev.ml.fuelhub.domain.repository.*
import javax.inject.Named
import javax.inject.Singleton

/**
 * Dependency injection module for repositories.
 * Provides Firebase-based implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    @Provides
    @Singleton
    fun provideUserRepository(): UserRepository = FirebaseUserRepositoryImpl()
    
    @Provides
    @Singleton
    fun provideVehicleRepository(): VehicleRepository = FirebaseVehicleRepositoryImpl()
    
    @Provides
    @Singleton
    fun provideFuelWalletRepository(): FuelWalletRepository = FirebaseFuelWalletRepositoryImpl()
    
    @Provides
    @Singleton
    fun provideFuelTransactionRepository(): FuelTransactionRepository = FirebaseFuelTransactionRepositoryImpl()
    
    @Provides
    @Singleton
    fun provideGasSlipRepository(): GasSlipRepository = FirebaseGasSlipRepositoryImpl()
    
    @Provides
    @Singleton
    fun provideAuditLogRepository(): AuditLogRepository = FirebaseAuditLogRepositoryImpl()
    
    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuthRepository: FirebaseAuthRepository
    ): AuthRepository = firebaseAuthRepository

    @Provides
    @Singleton
    fun provideNotificationRepository(): NotificationRepository = 
        FirebaseNotificationRepositoryImpl(FirebaseFirestore.getInstance())

    @Provides
    @Singleton
    fun provideFirebaseFunctions(): FirebaseFunctions = FirebaseFunctions.getInstance()

    @Provides
    @Singleton
    fun provideNotificationService(
        firebaseFunctions: FirebaseFunctions,
        firebaseAuth: FirebaseAuth
    ): NotificationService {
        return NotificationService(
            functions = firebaseFunctions,
            firebaseAuth = firebaseAuth,
            firebaseDataSource = FirebaseDataSource
        )
    }
}
