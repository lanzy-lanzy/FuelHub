package dev.ml.fuelhub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.ml.fuelhub.data.model.FuelWallet
import java.time.LocalDateTime
import dev.ml.fuelhub.domain.usecase.CreateFuelTransactionUseCase
import dev.ml.fuelhub.domain.usecase.GenerateDailyReportUseCase
import dev.ml.fuelhub.domain.usecase.GenerateMonthlyReportUseCase
import dev.ml.fuelhub.domain.usecase.GenerateWeeklyReportUseCase
import dev.ml.fuelhub.presentation.screen.GasSlipScreen
import dev.ml.fuelhub.presentation.screen.HomeScreen
import dev.ml.fuelhub.presentation.screen.ReportScreen
import dev.ml.fuelhub.presentation.screen.ReportScreenEnhanced
import dev.ml.fuelhub.presentation.screen.TransactionScreenEnhanced
import dev.ml.fuelhub.presentation.screen.WalletScreenEnhanced
import dev.ml.fuelhub.presentation.viewmodel.TransactionViewModel
import dev.ml.fuelhub.presentation.viewmodel.WalletViewModel
import dev.ml.fuelhub.presentation.viewmodel.ReportsViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import dev.ml.fuelhub.ui.theme.FuelHubTheme
import timber.log.Timber
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.runtime.collectAsState
import dev.ml.fuelhub.presentation.viewmodel.DriverManagementViewModel
import dev.ml.fuelhub.presentation.viewmodel.VehicleManagementViewModel
import dev.ml.fuelhub.presentation.viewmodel.VehicleManagementUiState
import dev.ml.fuelhub.presentation.viewmodel.GasSlipManagementViewModel
import dev.ml.fuelhub.presentation.screen.DriverManagementScreen
import dev.ml.fuelhub.presentation.screen.VehicleManagementScreen
import dev.ml.fuelhub.presentation.screen.GasSlipListScreen
import dev.ml.fuelhub.data.util.PdfPrintManager
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.filled.Menu
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.background
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.runtime.rememberCoroutineScope
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var walletRepository: dev.ml.fuelhub.domain.repository.FuelWalletRepository
    
    @Inject
    lateinit var transactionRepository: dev.ml.fuelhub.domain.repository.FuelTransactionRepository
    
    @Inject
    lateinit var createFuelTransactionUseCase: CreateFuelTransactionUseCase
    
    @Inject
    lateinit var generateDailyReportUseCase: GenerateDailyReportUseCase
    
    @Inject
    lateinit var generateWeeklyReportUseCase: GenerateWeeklyReportUseCase
    
    @Inject
    lateinit var generateMonthlyReportUseCase: GenerateMonthlyReportUseCase
    
    @Inject
    lateinit var transactionViewModel: TransactionViewModel
    
    @Inject
    lateinit var walletViewModel: WalletViewModel
    
    @Inject
    lateinit var driverManagementViewModel: DriverManagementViewModel
    
    @Inject
    lateinit var vehicleManagementViewModel: VehicleManagementViewModel
    
    @Inject
    lateinit var gasSlipManagementViewModel: GasSlipManagementViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize Timber for logging
        Timber.plant(Timber.DebugTree())
        
        // Initialize PDF/Print Manager
        val pdfPrintManager = PdfPrintManager(this)
        
        // Create default wallet if it doesn't exist and load it
        lifecycleScope.launch {
            try {
                val existingWallet = walletRepository.getWalletById("default-wallet-id")
                if (existingWallet == null) {
                    val defaultWallet = FuelWallet(
                        id = "default-wallet-id",
                        officeId = "mdrrmo-office-1",
                        balanceLiters = 1000.0,
                        maxCapacityLiters = 5000.0,
                        lastUpdated = LocalDateTime.now(),
                        createdAt = LocalDateTime.now()
                    )
                    walletRepository.createWallet(defaultWallet)
                    Timber.d("Default wallet created successfully")
                } else {
                    Timber.d("Default wallet loaded: balance=${existingWallet.balanceLiters}L")
                }
                // Load wallet into ViewModel
                walletViewModel.loadWallet("default-wallet-id")
                Timber.d("Wallet loaded into ViewModel")
            } catch (e: Exception) {
                Timber.e(e, "Error initializing wallet")
            }
        }
        
        setContent {
            FuelHubTheme {
                FuelHubApp(
                    transactionViewModel = transactionViewModel,
                    walletViewModel = walletViewModel,
                    driverManagementViewModel = driverManagementViewModel,
                    vehicleManagementViewModel = vehicleManagementViewModel,
                    gasSlipManagementViewModel = gasSlipManagementViewModel,
                    pdfPrintManager = pdfPrintManager,
                    generateDailyReportUseCase = generateDailyReportUseCase,
                    generateWeeklyReportUseCase = generateWeeklyReportUseCase,
                    generateMonthlyReportUseCase = generateMonthlyReportUseCase
                )
            }
        }
    }
}

@Composable
fun FuelHubApp(
    transactionViewModel: TransactionViewModel,
    walletViewModel: WalletViewModel,
    driverManagementViewModel: DriverManagementViewModel,
    vehicleManagementViewModel: VehicleManagementViewModel,
    gasSlipManagementViewModel: GasSlipManagementViewModel,
    pdfPrintManager: PdfPrintManager,
    generateDailyReportUseCase: GenerateDailyReportUseCase,
    generateWeeklyReportUseCase: GenerateWeeklyReportUseCase,
    generateMonthlyReportUseCase: GenerateMonthlyReportUseCase
) {
    val navController = rememberNavController()
    var selectedTab by remember { mutableIntStateOf(0) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerShape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
                drawerContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
            ) {
                // Header Section with Gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                    androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(24.dp)
                    ) {
                        // Header Icon
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .padding(bottom = 12.dp)
                                .background(
                                    color = androidx.compose.material3.MaterialTheme.colorScheme.surface.copy(alpha = 0.2f),
                                    shape = androidx.compose.foundation.shape.CircleShape
                                )
                                .padding(8.dp)
                        ) {
                            Icon(
                                Icons.Default.DirectionsCar,
                                "Fleet",
                                modifier = Modifier.height(32.dp),
                                tint = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        
                        Text(
                            "Fleet Management",
                            style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                // Menu Items
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                ) {
                    NavigationDrawerItem(
                        icon = { 
                            Icon(
                                Icons.Default.Person, 
                                "Drivers"
                            ) 
                        },
                        label = { Text("Drivers") },
                        selected = false,
                        onClick = {
                            navController.navigate("drivers") {
                                popUpTo("drivers") { inclusive = true }
                            }
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    
                    NavigationDrawerItem(
                        icon = { 
                            Icon(
                                Icons.Default.DirectionsCar, 
                                "Vehicles"
                            ) 
                        },
                        label = { Text("Vehicles") },
                        selected = false,
                        onClick = {
                            navController.navigate("vehicles") {
                                popUpTo("vehicles") { inclusive = true }
                            }
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                
                // Divider
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp, horizontal = 24.dp)
                )
                
                // Settings Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                ) {
                    NavigationDrawerItem(
                        icon = { 
                            Icon(
                                Icons.Default.Settings, 
                                "Settings"
                            ) 
                        },
                        label = { Text("Settings") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, "Home") },
                        label = { Text("Home") },
                        selected = selectedTab == 0,
                        onClick = {
                            selectedTab = 0
                            navController.navigate("home") {
                                popUpTo("home") { inclusive = true }
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.AccountBalance, "Wallet") },
                        label = { Text("Wallet") },
                        selected = selectedTab == 1,
                        onClick = {
                            selectedTab = 1
                            navController.navigate("wallet") {
                                popUpTo("wallet") { inclusive = true }
                            }
                        }
                    )
                    // Center space for FAB
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Menu, "Menu") },
                        label = { Text("Menu") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.open() }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.LocalGasStation, "Gas Slips") },
                        label = { Text("Gas Slips") },
                        selected = selectedTab == 2,
                        onClick = {
                            selectedTab = 2
                            navController.navigate("gasslips") {
                                popUpTo("gasslips") { inclusive = true }
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.BarChart, "Reports") },
                        label = { Text("Reports") },
                        selected = selectedTab == 3,
                        onClick = {
                            selectedTab = 3
                            navController.navigate("reports") {
                                popUpTo("reports") { inclusive = true }
                            }
                        }
                    )
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        selectedTab = 4
                        navController.navigate("transaction") {
                            popUpTo("transaction") { inclusive = true }
                        }
                    }
                ) {
                    Icon(Icons.Default.Add, "Create Transaction")
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("home") {
                    HomeScreen(
                        onNavigateToTransactions = {
                            selectedTab = 4
                            navController.navigate("transaction")
                        },
                        onNavigateToWallet = {
                            selectedTab = 1
                            navController.navigate("wallet")
                        },
                        onNavigateToReports = {
                            selectedTab = 3
                            navController.navigate("reports")
                        },
                        onNavigateToHistory = {
                            // Navigate to history or a separate history screen
                            Timber.d("History navigation clicked")
                        },
                        transactionViewModel = transactionViewModel,
                        walletViewModel = walletViewModel
                    )
                }
                
                composable("transaction") {
                    val vehicleState by vehicleManagementViewModel.uiState.collectAsState()
                    val availableVehicles = when (vehicleState) {
                        is VehicleManagementUiState.Success -> (vehicleState as VehicleManagementUiState.Success).vehicles
                        else -> emptyList()
                    }
                    
                    TransactionScreenEnhanced(
                        transactionViewModel = transactionViewModel,
                        availableVehicles = availableVehicles,
                        onTransactionCreated = {
                            Timber.d("Transaction created successfully")
                            // Navigate back to home after successful transaction
                            selectedTab = 0
                            navController.navigate("home") {
                                popUpTo("home") { inclusive = true }
                            }
                        },
                        pdfPrintManager = pdfPrintManager,
                        gasSlipViewModel = gasSlipManagementViewModel
                    )
                }
                
                composable("wallet") {
                    WalletScreenEnhanced(
                        walletViewModel = walletViewModel,
                        walletId = "default-wallet-id",
                        onRefillClick = { walletId ->
                            Timber.d("Refill clicked for wallet: $walletId")
                        }
                    )
                }
                
                composable("drivers") {
                    DriverManagementScreen(driverManagementViewModel)
                }
                
                composable("vehicles") {
                    VehicleManagementScreen(vehicleManagementViewModel)
                }
                
                composable("gasslips") {
                    var selectedGasSlip by remember { mutableStateOf<dev.ml.fuelhub.data.model.GasSlip?>(null) }
                    
                    if (selectedGasSlip == null) {
                        GasSlipListScreen(
                            gasSlipViewModel = gasSlipManagementViewModel,
                            onGasSlipSelected = { gasSlip ->
                                selectedGasSlip = gasSlip
                                Timber.d("Gas slip selected: ${gasSlip.referenceNumber}")
                            },
                            onPrintClick = { gasSlip ->
                                pdfPrintManager.generateAndPrintGasSlip(gasSlip)
                                Timber.d("Print initiated for gas slip: ${gasSlip.referenceNumber}")
                            },
                            onShareClick = { gasSlip ->
                                val pdfPath = pdfPrintManager.generatePdfOnly(gasSlip)
                                if (pdfPath != null) {
                                    pdfPrintManager.sharePdfFile(pdfPath)
                                    Timber.d("Share initiated for gas slip: ${gasSlip.referenceNumber}")
                                }
                            }
                        )
                    } else {
                        dev.ml.fuelhub.presentation.screen.GasSlipDetailScreen(
                            gasSlip = selectedGasSlip!!,
                            onPrint = { gasSlip ->
                                pdfPrintManager.generateAndPrintGasSlip(gasSlip)
                                Timber.d("Print initiated from detail screen: ${gasSlip.referenceNumber}")
                            },
                            onMarkDispensed = { gasSlip ->
                                gasSlipManagementViewModel.markAsDispensed(gasSlip.id)
                                selectedGasSlip = null
                                Timber.d("Gas slip marked as dispensed: ${gasSlip.referenceNumber}")
                            },
                            onBack = {
                                selectedGasSlip = null
                                gasSlipManagementViewModel.loadAllGasSlips()
                            }
                        )
                    }
                }
                
                composable("reports") {
                    val reportsViewModel: ReportsViewModel = hiltViewModel()
                    ReportScreenEnhanced(
                        viewModel = reportsViewModel,
                        pdfPrintManager = pdfPrintManager
                    )
                }
            }
        }
    }
}
