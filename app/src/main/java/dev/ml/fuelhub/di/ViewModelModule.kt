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
        userRepository: UserRepository,
        vehicleRepository: VehicleRepository
    ): TransactionViewModel = TransactionViewModel(
        createFuelTransactionUseCase = createFuelTransactionUseCase,
        userRepository = userRepository,
        vehicleRepository = vehicleRepository
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
