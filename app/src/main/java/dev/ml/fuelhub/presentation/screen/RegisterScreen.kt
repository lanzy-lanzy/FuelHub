package dev.ml.fuelhub.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.Button
import dev.ml.fuelhub.R
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import dev.ml.fuelhub.presentation.viewmodel.AuthViewModel
import dev.ml.fuelhub.ui.theme.DeepBlue
import dev.ml.fuelhub.ui.theme.DarkNavy
import dev.ml.fuelhub.ui.theme.VibrantCyan
import dev.ml.fuelhub.ui.theme.ElectricBlue
import dev.ml.fuelhub.ui.theme.NeonTeal
import dev.ml.fuelhub.ui.theme.AccentOrange

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onRegisterSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf("DISPATCHER") }  // Default role
    
    // Animation state
    val infiniteTransition = rememberInfiniteTransition(label = "RegisterAnimation")
    val iconScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "IconScale"
    )

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            onRegisterSuccess()
        }
    }

    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            showError = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        DeepBlue,    // #0A1929
                        DarkNavy     // #1A2332
                    )
                )
            )
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Back Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    enabled = !uiState.isLoading
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = VibrantCyan
                    )
                }
            }

            // Animated Logo/Icon
             Box(
                 modifier = Modifier
                     .size(120.dp)
                     .scale(iconScale),
                 contentAlignment = Alignment.Center
             ) {
                 Icon(
                     painter = painterResource(id = R.drawable.fuel_station_rafiki),
                     contentDescription = "FuelHub Logo",
                     modifier = Modifier.size(120.dp),
                     tint = Color.Unspecified
                 )
             }

            Spacer(modifier = Modifier.height(16.dp))

            // Title
            Text(
                text = "Create Account",
                style = MaterialTheme.typography.headlineSmall,
                color = VibrantCyan
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Register Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF1E2936).copy(alpha = 0.95f),
                                Color(0xFF2A3847).copy(alpha = 0.95f)
                            )
                        )
                    )
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Heading
                Text(
                    text = "Join FuelHub",
                    style = MaterialTheme.typography.titleMedium,
                    color = VibrantCyan
                )

                Text(
                    text = "Create your account to get started",
                    style = MaterialTheme.typography.bodySmall,
                    color = NeonTeal.copy(alpha = 0.8f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Role Selection
                Text(
                    text = "Account Type",
                    style = MaterialTheme.typography.bodySmall,
                    color = NeonTeal.copy(alpha = 0.8f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Dispatcher Role
                    RoleSelectionCard(
                        icon = Icons.Default.Person,
                        label = "Dispatcher",
                        isSelected = selectedRole == "DISPATCHER",
                        onClick = { selectedRole = "DISPATCHER" },
                        modifier = Modifier.weight(1f)
                    )

                    // Gas Station Role
                    RoleSelectionCard(
                        icon = Icons.Default.LocalGasStation,
                        label = "Gas Station",
                        isSelected = selectedRole == "GAS_STATION",
                        onClick = { selectedRole = "GAS_STATION" },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Error Message
                AnimatedVisibility(
                    visible = showError && uiState.error != null,
                    enter = fadeIn() + slideInVertically()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFFEBEE))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = uiState.error ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFC62828)
                        )
                    }
                }

                // Full Name Field
                 OutlinedTextField(
                     value = fullName,
                     onValueChange = {
                         fullName = it
                         if (showError) showError = false
                     },
                     label = { Text("Full Name") },
                     modifier = Modifier.fillMaxWidth(),
                     singleLine = true,
                     enabled = !uiState.isLoading,
                     colors = OutlinedTextFieldDefaults.colors(
                         focusedBorderColor = VibrantCyan,
                         unfocusedBorderColor = ElectricBlue.copy(alpha = 0.3f),
                         focusedLabelColor = VibrantCyan,
                         unfocusedLabelColor = NeonTeal.copy(alpha = 0.6f),
                         focusedTextColor = Color.White,
                         unfocusedTextColor = Color.White,
                         cursorColor = VibrantCyan
                     )
                 )

                 // Username Field
                 OutlinedTextField(
                     value = username,
                     onValueChange = {
                         username = it
                         if (showError) showError = false
                     },
                     label = { Text("Username") },
                     modifier = Modifier.fillMaxWidth(),
                     singleLine = true,
                     enabled = !uiState.isLoading,
                     colors = OutlinedTextFieldDefaults.colors(
                         focusedBorderColor = VibrantCyan,
                         unfocusedBorderColor = ElectricBlue.copy(alpha = 0.3f),
                         focusedLabelColor = VibrantCyan,
                         unfocusedLabelColor = NeonTeal.copy(alpha = 0.6f),
                         focusedTextColor = Color.White,
                         unfocusedTextColor = Color.White,
                         cursorColor = VibrantCyan
                     )
                 )

                 // Email Field
                 OutlinedTextField(
                     value = email,
                     onValueChange = {
                         email = it
                         if (showError) showError = false
                     },
                     label = { Text("Email") },
                     modifier = Modifier.fillMaxWidth(),
                     singleLine = true,
                     enabled = !uiState.isLoading,
                     colors = OutlinedTextFieldDefaults.colors(
                         focusedBorderColor = VibrantCyan,
                         unfocusedBorderColor = ElectricBlue.copy(alpha = 0.3f),
                         focusedLabelColor = VibrantCyan,
                         unfocusedLabelColor = NeonTeal.copy(alpha = 0.6f),
                         focusedTextColor = Color.White,
                         unfocusedTextColor = Color.White,
                         cursorColor = VibrantCyan
                     )
                 )

                 // Password Field
                 OutlinedTextField(
                     value = password,
                     onValueChange = {
                         password = it
                         if (showError) showError = false
                     },
                     label = { Text("Password") },
                     modifier = Modifier.fillMaxWidth(),
                     singleLine = true,
                     enabled = !uiState.isLoading,
                     visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                     trailingIcon = {
                         IconButton(onClick = { passwordVisible = !passwordVisible }, enabled = !uiState.isLoading) {
                             Icon(
                                 imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                 contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                 tint = NeonTeal.copy(alpha = 0.7f)
                             )
                         }
                     },
                     colors = OutlinedTextFieldDefaults.colors(
                         focusedBorderColor = VibrantCyan,
                         unfocusedBorderColor = ElectricBlue.copy(alpha = 0.3f),
                         focusedLabelColor = VibrantCyan,
                         unfocusedLabelColor = NeonTeal.copy(alpha = 0.6f),
                         focusedTextColor = Color.White,
                         unfocusedTextColor = Color.White,
                         cursorColor = VibrantCyan
                     )
                 )

                // Confirm Password Field
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        if (showError) showError = false
                    },
                    label = { Text("Confirm Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !uiState.isLoading,
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }, enabled = !uiState.isLoading) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password",
                                tint = NeonTeal.copy(alpha = 0.7f)
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VibrantCyan,
                        unfocusedBorderColor = ElectricBlue.copy(alpha = 0.3f),
                        focusedLabelColor = VibrantCyan,
                        unfocusedLabelColor = NeonTeal.copy(alpha = 0.6f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = VibrantCyan
                    )
                    )

                    // Password mismatch validation
                if (password.isNotEmpty() && confirmPassword.isNotEmpty() && password != confirmPassword) {
                    Text(
                        text = "Passwords do not match",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFC62828)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Register Button
                Button(
                    onClick = {
                        if (password == confirmPassword) {
                            viewModel.register(email, password, fullName, username, selectedRole)
                        } else {
                            viewModel.clearError()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = !uiState.isLoading && password == confirmPassword,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VibrantCyan
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = DeepBlue,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            "Create Account",
                            style = MaterialTheme.typography.labelLarge,
                            color = DeepBlue
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Login Link
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Already have an account? ",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeonTeal.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "Sign In",
                        style = MaterialTheme.typography.bodySmall,
                        color = AccentOrange,
                        modifier = Modifier.clickable(enabled = !uiState.isLoading) {
                            onBackClick()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun RoleSelectionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) VibrantCyan else Color(0xFF2A3847).copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) DeepBlue else NeonTeal.copy(alpha = 0.7f),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) DeepBlue else NeonTeal.copy(alpha = 0.7f)
            )
        }
    }
}
