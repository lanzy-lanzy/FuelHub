package dev.ml.fuelhub.presentation.screen

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.ml.fuelhub.data.model.User
import dev.ml.fuelhub.data.model.UserRole
import dev.ml.fuelhub.presentation.viewmodel.DriverManagementUiState
import dev.ml.fuelhub.presentation.viewmodel.DriverManagementViewModel
import dev.ml.fuelhub.presentation.component.DrawerSwipeIndicator
import dev.ml.fuelhub.ui.theme.*
import timber.log.Timber

@Composable
fun DriverManagementScreen(
    driverViewModel: DriverManagementViewModel,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val uiState by driverViewModel.uiState.collectAsState()
    val selectedDriver by driverViewModel.selectedDriver.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlue)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Manage Drivers",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = VibrantCyan,
                        fontSize = 28.sp
                    )
                Button(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .height(56.dp)
                        .shadow(12.dp, RoundedCornerShape(28.dp)),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(28.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(ElectricBlue, VibrantCyan),
                                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                    end = androidx.compose.ui.geometry.Offset(1f, 1f)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Add Driver",
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                fontSize = 14.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }

            // Content
            when (uiState) {
                is DriverManagementUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = VibrantCyan)
                    }
                }

                is DriverManagementUiState.Success -> {
                    val drivers = (uiState as DriverManagementUiState.Success).drivers
                    if (drivers.isEmpty()) {
                        EmptyState(message = "No drivers found. Add one to get started!")
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(drivers) { driver ->
                                DriverCard(
                                    driver = driver,
                                    isSelected = selectedDriver?.id == driver.id,
                                    onSelect = { driverViewModel.selectDriver(driver) },
                                    onEdit = {
                                        driverViewModel.selectDriver(driver)
                                        showEditDialog = true
                                    },
                                    onDelete = {
                                        driverViewModel.selectDriver(driver)
                                        showDeleteConfirm = true
                                    }
                                )
                            }
                        }
                    }
                }

                is DriverManagementUiState.Error -> {
                    ErrorState(
                        message = (uiState as DriverManagementUiState.Error).message,
                        onRetry = { driverViewModel.loadDrivers() }
                    )
                }

                else -> {}
            }
            }
            
            // Floating Drawer Swipe Indicator
            DrawerSwipeIndicator(
                tintColor = VibrantCyan.copy(alpha = 0.7f)
            )
        }
    }

    // Add Driver Dialog
    if (showAddDialog) {
        AddDriverDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { username, email, fullName, officeId ->
                driverViewModel.addDriver(username, email, fullName, officeId)
                showAddDialog = false
            }
        )
    }

    // Edit Driver Dialog
    if (showEditDialog && selectedDriver != null) {
        EditDriverDialog(
            driver = selectedDriver!!,
            onDismiss = {
                showEditDialog = false
                driverViewModel.clearSelection()
            },
            onUpdate = { updatedDriver ->
                driverViewModel.updateDriver(updatedDriver)
                showEditDialog = false
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirm && selectedDriver != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirm = false
                driverViewModel.clearSelection()
            },
            title = { Text("Delete Driver") },
            text = { Text("Are you sure you want to delete ${selectedDriver!!.fullName}?") },
            confirmButton = {
                Button(
                    onClick = {
                        driverViewModel.deleteDriver(selectedDriver!!.id)
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                Button(onClick = {
                    showDeleteConfirm = false
                    driverViewModel.clearSelection()
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DriverCard(
    driver: User,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(isSelected) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clip(RoundedCornerShape(16.dp))
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceDark
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = if (expanded) 12.dp else 0.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        driver.fullName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        "@${driver.username}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = VibrantCyan,
                    modifier = Modifier.size(24.dp)
                )
            }

            if (expanded) {
                Divider(color = SurfaceLight, modifier = Modifier.padding(vertical = 8.dp))
                DetailRow("Email", driver.email)
                DetailRow("Office", driver.officeId)
                DetailRow("Status", if (driver.isActive) "Active" else "Inactive")

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onEdit,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricBlue
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit", fontSize = 12.sp)
                    }

                    Button(
                        onClick = onDelete,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ErrorRed
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
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
fun AddDriverDialog(
    onDismiss: () -> Unit,
    onAdd: (username: String, email: String, fullName: String, officeId: String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var officeId by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Driver") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it.uppercase() },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Characters)
                )
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it.uppercase() },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Characters)
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
                OutlinedTextField(
                    value = officeId,
                    onValueChange = { officeId = it.uppercase() },
                    label = { Text("Office ID") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Characters)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (username.isNotBlank() && email.isNotBlank() && fullName.isNotBlank() && officeId.isNotBlank()) {
                        onAdd(username, email, fullName, officeId)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
            ) {
                Text("Add", color = Color.White)
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditDriverDialog(
    driver: User,
    onDismiss: () -> Unit,
    onUpdate: (User) -> Unit
) {
    var fullName by remember { mutableStateOf(driver.fullName) }
    var email by remember { mutableStateOf(driver.email) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Driver") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it.uppercase() },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Characters)
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (fullName.isNotBlank() && email.isNotBlank()) {
                        onUpdate(
                            driver.copy(
                                fullName = fullName,
                                email = email
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
            ) {
                Text("Update", color = Color.White)
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EmptyState(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PersonOutline,
                contentDescription = null,
                tint = VibrantCyan,
                modifier = Modifier.size(64.dp)
            )
            Text(
                message,
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
