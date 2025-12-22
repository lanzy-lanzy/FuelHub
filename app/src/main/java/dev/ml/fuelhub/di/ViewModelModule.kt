package dev.ml.fuelhub.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.ml.fuelhub.domain.usecase.*
import dev.ml.fuelhub.domain.repository.*
import dev.ml.fuelhub.presentation.viewmodel.*
import javax.inject.Singleton

/**
 * Dependency injection module for ViewModels.
 */
@Module
@InstallIn(SingletonComponent::class)
object ViewModelModule {
    
    @Provides
    @Singleton
    fun provideTransactionViewModel(
        createFuelTransactionUseCase: CreateFuelTransactionUseCase,
        completeTransactionUseCase: CompleteTransactionUseCase,
        userRepository: UserRepository,
        vehicleRepository: VehicleRepository,
        transactionRepository: FuelTransactionRepository,
        walletRepository: FuelWalletRepository,
        gasSlipRepository: GasSlipRepository
    ): TransactionViewModel = TransactionViewModel(
        createFuelTransactionUseCase = createFuelTransactionUseCase,
        completeTransactionUseCase = completeTransactionUseCase,
        userRepository = userRepository,
        vehicleRepository = vehicleRepository,
        transactionRepository = transactionRepository,
        walletRepository = walletRepository,
        gasSlipRepository = gasSlipRepository
    )
    
    @Provides
    @Singleton
    fun provideWalletViewModel(
        walletRepository: FuelWalletRepository
    ): WalletViewModel = WalletViewModel(walletRepository)
    
    @Provides
    @Singleton
    fun provideDriverManagementViewModel(
        userRepository: UserRepository
    ): DriverManagementViewModel = DriverManagementViewModel(userRepository)
    
    @Provides
    @Singleton
    fun provideVehicleManagementViewModel(
        vehicleRepository: VehicleRepository,
        userRepository: UserRepository
    ): VehicleManagementViewModel = VehicleManagementViewModel(vehicleRepository, userRepository)
    
    @Provides
    @Singleton
    fun provideGasSlipManagementViewModel(
        gasSlipRepository: GasSlipRepository
    ): GasSlipManagementViewModel = GasSlipManagementViewModel(gasSlipRepository)
}
