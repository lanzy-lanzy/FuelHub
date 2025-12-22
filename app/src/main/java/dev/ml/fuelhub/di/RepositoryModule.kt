package dev.ml.fuelhub.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.ml.fuelhub.data.repository.*
import dev.ml.fuelhub.domain.repository.*
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
}
