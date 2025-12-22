package dev.ml.fuelhub.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.ml.fuelhub.data.model.User
import dev.ml.fuelhub.data.model.UserRole
import dev.ml.fuelhub.presentation.viewmodel.AdminViewModel
import dev.ml.fuelhub.ui.theme.*
import timber.log.Timber

/**
 * Admin Dashboard for user management
 * Allows creating, editing, deactivating, and changing user roles
 */
@Composable
fun AdminDashboardScreen(
    adminViewModel: AdminViewModel? = null,
    onNavigateBack: () -> Unit = {}
) {
    val vm = adminViewModel ?: viewModel<AdminViewModel>()
    val users by vm.allUsers.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<User?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("All Users", "Dispatchers", "Gas Station", "Admins")
    
    LaunchedEffect(Unit) {
        vm.loadAllUsers()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlue)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        AdminHeader(onNavigateBack)
        
        // Stats Cards
        StatsSection(users)
        
        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp)),
            containerColor = SurfaceDark,
            contentColor = VibrantCyan
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontSize = 12.sp) }
                )
            }
        }
        
        // Users List
        val filteredUsers = when (selectedTab) {
            0 -> users
            1 -> users.filter { it.role == UserRole.DISPATCHER }
            2 -> users.filter { it.role == UserRole.GAS_STATION }
            3 -> users.filter { it.role == UserRole.ADMIN }
            else -> users
        }
        
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = VibrantCyan)
            }
        } else if (filteredUsers.isEmpty()) {
            EmptyUsersState()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredUsers) { user ->
                    UserManagementCard(
                        user = user,
                        onEdit = {
                            selectedUser = user
                            showEditDialog = true
                        },
                        onDeactivate = {
                            vm.deactivateUser(user.id)
                        },
                        onChangeRole = { newRole ->
                            vm.changeUserRole(user.id, newRole)
                        }
                    )
                }
            }
        }
        
        // Create User Button
        Button(
            onClick = { showCreateDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(8.dp, RoundedCornerShape(16.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = "Add User",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    "Create New User",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
    
    // Create User Dialog
    if (showCreateDialog) {
        CreateUserDialog(
            onConfirm = { username, email, fullName, role ->
                vm.createUser(username, email, fullName, role)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false }
        )
    }
    
    // Edit User Dialog
    if (showEditDialog && selectedUser != null) {
        EditUserDialog(
            user = selectedUser!!,
            onConfirm = { fullName, role ->
                vm.updateUser(selectedUser!!.id, fullName, role)
                showEditDialog = false
            },
            onDismiss = {
                showEditDialog = false
                selectedUser = null
            }
        )
    }
}

@Composable
fun AdminHeader(onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                "Admin",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Text(
                "User Management",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = VibrantCyan,
                fontSize = 28.sp
            )
        }
        
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceDark)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = TextPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun StatsSection(users: List<User>) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            label = "Total Users",
            value = users.size.toString(),
            color = ElectricBlue,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "Dispatchers",
            value = users.count { it.role == UserRole.DISPATCHER }.toString(),
            color = VibrantCyan,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "Gas Station",
            value = users.count { it.role == UserRole.GAS_STATION }.toString(),
            color = SuccessGreen,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun EmptyUsersState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.PersonOutline,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "No users found",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Create a new user to get started",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}

@Composable
fun UserManagementCard(
    user: User,
    onEdit: () -> Unit,
    onDeactivate: () -> Unit,
    onChangeRole: (UserRole) -> Unit
) {
    var showRoleMenu by remember { mutableStateOf(false) }
    var showConfirmDeactivate by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        user.fullName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = VibrantCyan
                    )
                    Text(
                        user.username,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                
                Box {
                    RoleChip(user.role, onClick = { showRoleMenu = true })
                    
                    DropdownMenu(
                        expanded = showRoleMenu,
                        onDismissRequest = { showRoleMenu = false },
                        modifier = Modifier.background(SurfaceDark)
                    ) {
                        UserRole.values().forEach { role ->
                            DropdownMenuItem(
                                text = { Text(role.name, color = TextPrimary) },
                                onClick = {
                                    onChangeRole(role)
                                    showRoleMenu = false
                                }
                            )
                        }
                    }
                }
            }
            
            // Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DetailItem("Email", user.email, modifier = Modifier.weight(1f))
                DetailItem("Status", if (user.isActive) "Active" else "Inactive", modifier = Modifier.weight(1f))
            }
            
            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onEdit,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color.White)
                }
                
                Button(
                    onClick = { showConfirmDeactivate = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B0000)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = "Deactivate",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Deactivate", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color.White)
                }
            }
        }
    }
    
    if (showConfirmDeactivate) {
        AlertDialog(
            onDismissRequest = { showConfirmDeactivate = false },
            title = { Text("Deactivate User", color = TextPrimary) },
            text = { Text("Are you sure you want to deactivate ${user.fullName}?", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        onDeactivate()
                        showConfirmDeactivate = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B0000))
                ) {
                    Text("Deactivate", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDeactivate = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = SurfaceDark,
            textContentColor = TextPrimary
        )
    }
}

@Composable
fun RoleChip(role: UserRole, onClick: () -> Unit = {}) {
    val backgroundColor = when (role) {
        UserRole.DISPATCHER -> ElectricBlue.copy(alpha = 0.2f)
        UserRole.GAS_STATION -> SuccessGreen.copy(alpha = 0.2f)
        UserRole.ADMIN -> VibrantCyan.copy(alpha = 0.2f)
        else -> TextSecondary.copy(alpha = 0.2f)
    }
    
    val textColor = when (role) {
        UserRole.DISPATCHER -> ElectricBlue
        UserRole.GAS_STATION -> SuccessGreen
        UserRole.ADMIN -> VibrantCyan
        else -> TextSecondary
    }
    
    Button(
        onClick = onClick,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .height(32.dp),
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                role.name,
                style = MaterialTheme.typography.labelSmall,
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
            Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
fun DetailItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = TextTertiary,
            fontWeight = FontWeight.Medium
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun CreateUserDialog(
    onConfirm: (username: String, email: String, fullName: String, role: UserRole) -> Unit,
    onDismiss: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.DISPATCHER) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New User", color = TextPrimary) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = SurfaceDark,
                        focusedContainerColor = SurfaceDark,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                
                TextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = SurfaceDark,
                        focusedContainerColor = SurfaceDark,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = SurfaceDark,
                        focusedContainerColor = SurfaceDark,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                
                Text("Role", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    UserRole.values().forEach { role ->
                        FilterChip(
                            selected = selectedRole == role,
                            onClick = { selectedRole = role },
                            label = { Text(role.name, fontSize = 10.sp) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VibrantCyan,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(username, email, fullName, selectedRole) },
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
            ) {
                Text("Create", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = SurfaceDark,
        textContentColor = TextPrimary
    )
}

@Composable
fun EditUserDialog(
    user: User,
    onConfirm: (fullName: String, role: UserRole) -> Unit,
    onDismiss: () -> Unit
) {
    var fullName by remember { mutableStateOf(user.fullName) }
    var selectedRole by remember { mutableStateOf(user.role) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit User", color = TextPrimary) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = SurfaceDark,
                        focusedContainerColor = SurfaceDark,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                
                Text("Role", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    UserRole.values().forEach { role ->
                        FilterChip(
                            selected = selectedRole == role,
                            onClick = { selectedRole = role },
                            label = { Text(role.name, fontSize = 10.sp) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VibrantCyan,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(fullName, selectedRole) },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
            ) {
                Text("Update", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = SurfaceDark,
        textContentColor = TextPrimary
    )
}
