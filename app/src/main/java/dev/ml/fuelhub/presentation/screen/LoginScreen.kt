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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    
    // Animation state
    val infiniteTransition = rememberInfiniteTransition(label = "LoginAnimation")
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
            onLoginSuccess()
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
            Spacer(modifier = Modifier.height(40.dp))

            // Animated Logo/Icon Container
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(ElectricBlue, VibrantCyan)
                        )
                    )
                    .scale(iconScale),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.LocalGasStation,
                    contentDescription = "FuelHub Logo",
                    modifier = Modifier.size(60.dp),
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Title
            Text(
                text = "FuelHub",
                style = MaterialTheme.typography.headlineLarge,
                color = VibrantCyan,
                modifier = Modifier.padding(top = 8.dp)
            )

            Text(
                text = "Smart Fuel Management",
                style = MaterialTheme.typography.labelLarge,
                color = NeonTeal.copy(alpha = 0.9f),
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Login Card
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Heading
                Text(
                    text = "Welcome Back",
                    style = MaterialTheme.typography.headlineSmall,
                    color = VibrantCyan
                )

                Text(
                    text = "Sign in to your account to continue",
                    style = MaterialTheme.typography.bodySmall,
                    color = NeonTeal.copy(alpha = 0.8f)
                )

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

                Spacer(modifier = Modifier.height(8.dp))

                // Forgot Password
                Text(
                    text = "Forgot password?",
                    style = MaterialTheme.typography.bodySmall,
                    color = VibrantCyan,
                    modifier = Modifier
                        .align(Alignment.End)
                        .clickable { }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Login Button
                Button(
                    onClick = {
                        viewModel.login(email, password)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = !uiState.isLoading,
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
                            "Sign In",
                            style = MaterialTheme.typography.labelLarge,
                            color = DeepBlue
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Register Link
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Don't have an account? ",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeonTeal.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "Sign Up",
                        style = MaterialTheme.typography.bodySmall,
                        color = AccentOrange,
                        modifier = Modifier.clickable(enabled = !uiState.isLoading) {
                            onRegisterClick()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
