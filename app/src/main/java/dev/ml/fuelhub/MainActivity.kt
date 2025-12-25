package dev.ml.fuelhub

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.benchmark.traceprocessor.Row
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
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
import dev.ml.fuelhub.presentation.screen.GasStationScreen
import dev.ml.fuelhub.presentation.screen.HomeScreen
import dev.ml.fuelhub.presentation.screen.ReportScreen
import dev.ml.fuelhub.presentation.screen.ReportScreenEnhanced
import dev.ml.fuelhub.presentation.screen.TransactionScreenEnhanced
import dev.ml.fuelhub.presentation.screen.WalletScreenEnhanced
import dev.ml.fuelhub.presentation.viewmodel.TransactionViewModel
import dev.ml.fuelhub.presentation.viewmodel.WalletViewModel
import dev.ml.fuelhub.presentation.viewmodel.ReportsViewModel
import dev.ml.fuelhub.presentation.viewmodel.AuthViewModel
import dev.ml.fuelhub.presentation.screen.LoginScreen
import dev.ml.fuelhub.presentation.screen.RegisterScreen
import dev.ml.fuelhub.presentation.screen.SettingsScreen
import dev.ml.fuelhub.domain.repository.AuthRepository
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
import dev.ml.fuelhub.presentation.component.CenteredActionBottomBar
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import dagger.hilt.android.AndroidEntryPoint
import dev.ml.fuelhub.ui.theme.ElectricBlue
import dev.ml.fuelhub.ui.theme.SurfaceDark
import dev.ml.fuelhub.ui.theme.TextPrimary
import dev.ml.fuelhub.ui.theme.TextSecondary
import dev.ml.fuelhub.ui.theme.VibrantCyan
import dev.ml.fuelhub.ui.theme.WarningYellow
import coil.compose.AsyncImage
import androidx.compose.material3.CircularProgressIndicator
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
    
    @Inject
    lateinit var authRepository: AuthRepository
    
    // Image picker launcher - will be passed to FuelHubApp
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    
    // Callback to update Compose state with selected image URI
    var setImageUriCallback: ((Uri) -> Unit)? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Request POST_NOTIFICATIONS permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
        
        // Initialize image picker launcher
        imagePickerLauncher = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let { selectedUri ->
                Timber.d("Image selected in MainActivity: $selectedUri")
                // Call the Compose callback to update state
                setImageUriCallback?.invoke(selectedUri)
            }
        }
        
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
                    generateMonthlyReportUseCase = generateMonthlyReportUseCase,
                    authRepository = authRepository,
                    imagePickerLauncher = imagePickerLauncher
                )
            }
        }
    }
}

@SuppressLint("ContextCastToActivity")
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
    generateMonthlyReportUseCase: GenerateMonthlyReportUseCase,
    authRepository: AuthRepository,
    imagePickerLauncher: ActivityResultLauncher<String>
) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    var selectedTab by remember { mutableIntStateOf(0) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val isUserLoggedIn = authRepository.isUserLoggedIn()
    
    // Use Compose state to track selected image URI - THIS IS KEY!
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    
    // Monitor for image URI changes - will trigger when selectedImageUri changes
    LaunchedEffect(selectedImageUri) {
        selectedImageUri?.let { uri ->
            Timber.d("Image URI selected in Compose: $uri")
            authViewModel.uploadProfilePicture(uri)
            // Reset after processing
            selectedImageUri = null
        }
    }
    
    // Pass the image URI setter back to activity via a callback
    val activity = LocalContext.current as? MainActivity
    LaunchedEffect(activity) {
        activity?.setImageUriCallback = { uri ->
            Timber.d("Image URI set from activity: $uri")
            selectedImageUri = uri
        }
    }
    
    // Determine start destination based on user role
    val startDestination = if (isUserLoggedIn) {
        val userRole = runBlocking {
            val userId = authRepository.getCurrentUserId()
            userId?.let { authRepository.getUserRole(it) } ?: ""
        }
        if (userRole == "GAS_STATION") "gasstation" else "home"
    } else {
        "login"
    }
    var currentRoute by remember { mutableStateOf(startDestination) }
    
    // Only show drawer and full layout for non-gas-station users
    if (currentRoute != "gasstation") {
        ModalNavigationDrawer(
            drawerState = drawerState,
            scrimColor = if (currentRoute in listOf("login", "register")) Color.Transparent else Color.Black.copy(alpha = 0.32f),
            gesturesEnabled = currentRoute !in listOf("login", "register"),
            drawerContent = {
                ModalDrawerSheet(
                    drawerContainerColor = SurfaceDark,
                    drawerContentColor = TextPrimary,
                    drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
                    modifier = Modifier.width(320.dp)
                ) {
                    // Header Section with Premium Gradient
                     Box(
                         modifier = Modifier
                             .fillMaxWidth()
                             .height(220.dp)
                             .background(
                                 brush = Brush.verticalGradient(
                                     colors = listOf(ElectricBlue, VibrantCyan)
                                 )
                             )
                     ) {
                         Column(
                             modifier = Modifier
                                 .fillMaxSize()
                                 .padding(24.dp),
                             verticalArrangement = Arrangement.SpaceBetween
                         ) {
                             // Profile Section
                             Row(
                                 modifier = Modifier
                                     .fillMaxWidth(),
                                 horizontalArrangement = Arrangement.SpaceBetween,
                                 verticalAlignment = Alignment.CenterVertically
                             ) {
                                 // Profile Picture with upload status
                                 val profilePictureUrl by authViewModel.profilePictureUrl.collectAsState()
                                 
                                 Box(
                                     modifier = Modifier
                                         .size(64.dp)
                                         .clip(CircleShape)
                                         .background(Color.White.copy(alpha = 0.3f)),
                                     contentAlignment = Alignment.Center
                                 ) {
                                     if (!profilePictureUrl.isNullOrEmpty()) {
                                         AsyncImage(
                                             model = profilePictureUrl,
                                             contentDescription = "Profile Picture",
                                             modifier = Modifier
                                                 .size(64.dp)
                                                 .clip(CircleShape),
                                             contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                             onError = { error ->
                                                 Timber.e(error.result.throwable, "Failed to load profile picture in drawer: $profilePictureUrl")
                                             }
                                         )
                                     } else {
                                         Icon(
                                             imageVector = Icons.Default.Person,
                                             contentDescription = "Profile",
                                             modifier = Modifier.size(36.dp),
                                             tint = Color.White
                                         )
                                     }
                                 }
                                 
                                 // Edit Profile Button
                                 IconButton(
                                     onClick = { 
                                         // Trigger image picker, result will be processed via registerForActivityResult
                                         imagePickerLauncher.launch("image/*")
                                     },
                                     modifier = Modifier.size(40.dp)
                                 ) {
                                     Icon(
                                         imageVector = Icons.Default.Edit,
                                         contentDescription = "Edit Profile",
                                         modifier = Modifier.size(20.dp),
                                         tint = Color.White
                                     )
                                 }
                             }
                             
                             // User Info
                             Column {
                                 Text(
                                     text = "Fleet Manager",
                                     style = MaterialTheme.typography.headlineSmall,
                                     fontWeight = FontWeight.Bold,
                                     color = Color.White
                                 )
                                 Text(
                                     text = "manager@fuelhub.com",
                                     style = MaterialTheme.typography.bodySmall,
                                     color = Color.White.copy(alpha = 0.8f)
                                 )
                             }
                         }
                     }
                     
                     Spacer(modifier = Modifier.height(24.dp))

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
                            selected = currentRoute == "drivers",
                            onClick = {
                                navController.navigate("drivers") {
                                    popUpTo("drivers") { inclusive = true }
                                }
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(vertical = 4.dp),
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = VibrantCyan.copy(alpha = 0.1f),
                                unselectedContainerColor = Color.Transparent,
                                selectedTextColor = VibrantCyan,
                                unselectedTextColor = TextPrimary,
                                selectedIconColor = VibrantCyan,
                                unselectedIconColor = TextSecondary
                            )
                        )
                        
                        NavigationDrawerItem(
                            icon = { 
                                Icon(
                                    Icons.Default.DirectionsCar, 
                                    "Vehicles"
                                ) 
                            },
                            label = { Text("Vehicles") },
                            selected = currentRoute == "vehicles",
                            onClick = {
                                navController.navigate("vehicles") {
                                    popUpTo("vehicles") { inclusive = true }
                                }
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(vertical = 4.dp),
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = VibrantCyan.copy(alpha = 0.1f),
                                unselectedContainerColor = Color.Transparent,
                                selectedTextColor = VibrantCyan,
                                unselectedTextColor = TextPrimary,
                                selectedIconColor = VibrantCyan,
                                unselectedIconColor = TextSecondary
                            )
                        )
                    }
                    
                    // Divider
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 24.dp),
                        color = Color.White.copy(alpha = 0.1f)
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
                             selected = currentRoute == "settings",
                             onClick = {
                                 navController.navigate("settings")
                                 scope.launch { drawerState.close() }
                             },
                             modifier = Modifier.padding(vertical = 4.dp),
                             colors = NavigationDrawerItemDefaults.colors(
                                 selectedContainerColor = VibrantCyan.copy(alpha = 0.1f),
                                 unselectedContainerColor = Color.Transparent,
                                 selectedTextColor = VibrantCyan,
                                 unselectedTextColor = TextPrimary,
                                 selectedIconColor = VibrantCyan,
                                 unselectedIconColor = TextSecondary
                             )
                         )
                     }
                     
                     Spacer(modifier = Modifier.weight(1f))
                     
                     // Logout Button at Bottom
                     HorizontalDivider(
                         modifier = Modifier.padding(horizontal = 12.dp),
                         color = TextSecondary.copy(alpha = 0.2f)
                     )
                     
                     Column(
                         modifier = Modifier
                             .fillMaxWidth()
                             .padding(horizontal = 12.dp, vertical = 16.dp)
                     ) {
                         NavigationDrawerItem(
                             icon = { 
                                 Icon(
                                     Icons.Default.Logout, 
                                     "Logout",
                                     tint = WarningYellow
                                 ) 
                             },
                             label = { 
                                 Text(
                                     "Logout",
                                     color = WarningYellow,
                                     fontWeight = FontWeight.SemiBold
                                 ) 
                             },
                             selected = false,
                             onClick = {
                                 authViewModel.logout()
                                 navController.navigate("login") {
                                     popUpTo(0) { inclusive = true }
                                 }
                                 scope.launch { drawerState.close() }
                             },
                             modifier = Modifier.padding(vertical = 4.dp),
                             colors = NavigationDrawerItemDefaults.colors(
                                 selectedContainerColor = WarningYellow.copy(alpha = 0.1f),
                                 unselectedContainerColor = WarningYellow.copy(alpha = 0.05f),
                                 selectedTextColor = WarningYellow,
                                 unselectedTextColor = WarningYellow,
                                 selectedIconColor = WarningYellow,
                                 unselectedIconColor = WarningYellow
                             )
                         )
                     }
                    }
            }
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    // Hide bottom bar on login/register/gasstation screens
                    if (currentRoute !in listOf("login", "register", "gasstation")) {
                        CenteredActionBottomBar(
                            selectedTab = selectedTab,
                            onTabSelected = { tab ->
                                selectedTab = tab
                                when (tab) {
                                    0 -> navController.navigate("home") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                    1 -> navController.navigate("wallet") {
                                        popUpTo("wallet") { inclusive = true }
                                    }
                                    2 -> navController.navigate("gasslips") {
                                        popUpTo("gasslips") { inclusive = true }
                                    }
                                    3 -> navController.navigate("reports") {
                                        popUpTo("reports") { inclusive = true }
                                    }
                                }
                            },
                            onCenterActionClick = {
                                selectedTab = 4
                                navController.navigate("transaction") {
                                    popUpTo("transaction") { inclusive = true }
                                }
                            }
                        )
                    }
                }
            ) { innerPadding ->
                FuelHubNavHost(
                    navController = navController,
                    startDestination = startDestination,
                    modifier = Modifier.padding(innerPadding),
                    currentRoute = currentRoute,
                    onRouteChange = { newRoute -> 
                        currentRoute = newRoute 
                    },
                    selectedTab = selectedTab,
                    onTabChange = { newTab -> 
                        selectedTab = newTab 
                    },
                    transactionViewModel = transactionViewModel,
                    walletViewModel = walletViewModel,
                    driverManagementViewModel = driverManagementViewModel,
                    vehicleManagementViewModel = vehicleManagementViewModel,
                    gasSlipManagementViewModel = gasSlipManagementViewModel,
                    pdfPrintManager = pdfPrintManager,
                    authViewModel = authViewModel
                )
            }
        }
    } else {
        // Gas station dedicated layout - use NavHost but without drawer/scaffold
        FuelHubNavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.fillMaxSize(),
            currentRoute = currentRoute,
            onRouteChange = { newRoute -> 
                currentRoute = newRoute 
            },
            selectedTab = selectedTab,
            onTabChange = { newTab -> 
                selectedTab = newTab 
            },
            transactionViewModel = transactionViewModel,
            walletViewModel = walletViewModel,
            driverManagementViewModel = driverManagementViewModel,
            vehicleManagementViewModel = vehicleManagementViewModel,
            gasSlipManagementViewModel = gasSlipManagementViewModel,
            pdfPrintManager = pdfPrintManager,
            authViewModel = authViewModel
        )
    }
}

@Composable
fun FuelHubNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier,
    currentRoute: String,
    onRouteChange: (String) -> Unit,
    selectedTab: Int,
    onTabChange: (Int) -> Unit,
    transactionViewModel: TransactionViewModel,
    walletViewModel: WalletViewModel,
    driverManagementViewModel: DriverManagementViewModel,
    vehicleManagementViewModel: VehicleManagementViewModel,
    gasSlipManagementViewModel: GasSlipManagementViewModel,
    pdfPrintManager: PdfPrintManager,
    authViewModel: AuthViewModel
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable("login") {
            LaunchedEffect(Unit) {
                onRouteChange("login")
            }
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    // Route based on user role
                    val userRole = authViewModel.uiState.value.userRole
                    val destination = when (userRole) {
                        "GAS_STATION" -> "gasstation"
                        else -> "home"
                    }
                    navController.navigate(destination) {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onRegisterClick = {
                    navController.navigate("register")
                }
            )
        }
        
        composable("register") {
            LaunchedEffect(Unit) {
                onRouteChange("register")
            }
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onBackClick = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                }
            )
        }
        
        composable("home") {
            LaunchedEffect(Unit) {
                onRouteChange("home")
            }
            HomeScreen(
                onNavigateToTransactions = {
                    onTabChange(4)
                    navController.navigate("transaction")
                },
                onNavigateToWallet = {
                    onTabChange(1)
                    navController.navigate("wallet")
                },
                onNavigateToReports = {
                    onTabChange(3)
                    navController.navigate("reports")
                },
                onNavigateToHistory = {
                    Timber.d("History navigation clicked")
                },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                transactionViewModel = transactionViewModel,
                walletViewModel = walletViewModel,
                authViewModel = authViewModel
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
                    onTabChange(0)
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
                    },
                    onCancelClick = { gasSlipId ->
                        gasSlipManagementViewModel.cancelGasSlip(gasSlipId)
                        gasSlipManagementViewModel.loadAllGasSlips()
                        Timber.d("Gas slip cancelled: $gasSlipId")
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
                    onCancel = { gasSlipId ->
                        gasSlipManagementViewModel.cancelGasSlip(gasSlipId)
                        selectedGasSlip = null
                        gasSlipManagementViewModel.loadAllGasSlips()
                        Timber.d("Gas slip cancelled: $gasSlipId")
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
        
        composable("gasstation") {
            var shouldLogout by remember { mutableStateOf(false) }
            
            LaunchedEffect(Unit) {
                onRouteChange("gasstation")
            }
            
            LaunchedEffect(shouldLogout) {
                if (shouldLogout) {
                    authViewModel.logout()
                }
            }
            
            GasStationScreen(
                transactionViewModel = transactionViewModel,
                onNavigateBack = {
                    // Logout for gas station users
                    shouldLogout = true
                    navController.navigate("login") {
                        popUpTo("gasstation") { inclusive = true }
                    }
                }
            )
        }

        composable("settings") {
            LaunchedEffect(Unit) {
                onRouteChange("settings")
            }
            SettingsScreen(
                authViewModel = authViewModel,
                onBackClick = {
                    navController.popBackStack()
                },
                onLogoutClick = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
