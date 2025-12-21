package dev.ml.fuelhub.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.ml.fuelhub.domain.usecase.*
import dev.ml.fuelhub.domain.repository.*
import javax.inject.Singleton

/**
 * Dependency injection module for use cases.
 */
@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    
    @Provides
    @Singleton
    fun provideCreateFuelTransactionUseCase(
        walletRepository: FuelWalletRepository,
        transactionRepository: FuelTransactionRepository,
        gasSlipRepository: GasSlipRepository,
        auditLogRepository: AuditLogRepository,
        vehicleRepository: VehicleRepository,
        userRepository: UserRepository
    ): CreateFuelTransactionUseCase = CreateFuelTransactionUseCase(
        walletRepository = walletRepository,
        transactionRepository = transactionRepository,
        gasSlipRepository = gasSlipRepository,
        auditLogRepository = auditLogRepository,
        vehicleRepository = vehicleRepository,
        userRepository = userRepository
    )
    
    @Provides
    @Singleton
    fun provideGenerateDailyReportUseCase(
        transactionRepository: FuelTransactionRepository
    ): GenerateDailyReportUseCase = GenerateDailyReportUseCase(transactionRepository)
    
    @Provides
    @Singleton
    fun provideGenerateWeeklyReportUseCase(
        transactionRepository: FuelTransactionRepository
    ): GenerateWeeklyReportUseCase = GenerateWeeklyReportUseCase(transactionRepository)
    
    @Provides
    @Singleton
    fun provideGenerateMonthlyReportUseCase(
        transactionRepository: FuelTransactionRepository
    ): GenerateMonthlyReportUseCase = GenerateMonthlyReportUseCase(transactionRepository)
}
