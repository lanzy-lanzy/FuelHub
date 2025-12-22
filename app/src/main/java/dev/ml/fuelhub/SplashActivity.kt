package dev.ml.fuelhub

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.ml.fuelhub.ui.theme.DeepBlue
import dev.ml.fuelhub.ui.theme.DarkNavy
import dev.ml.fuelhub.ui.theme.VibrantCyan
import dev.ml.fuelhub.ui.theme.ElectricBlue
import dev.ml.fuelhub.ui.theme.NeonTeal
import dev.ml.fuelhub.ui.theme.AccentOrange
import dev.ml.fuelhub.ui.theme.FuelHubTheme

class SplashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            FuelHubTheme {
                SplashScreen()
            }
        }

        // Delay for 3 seconds before launching MainActivity
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            // Fade transition animation
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }, 3000)
    }

    override fun onBackPressed() {
        // Prevent back button from skipping splash screen
        super.onBackPressed()
    }
}

@Composable
fun SplashScreen() {
    // Animation states
    val infiniteTransition = rememberInfiniteTransition(label = "SplashAnimation")
    
    // Pulsing scale animation for main icon
    val iconScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "IconScale"
    )
    
    // Floating animation for the icon
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FloatOffset"
    )
    
    // Opacity animation for accent rings
    val accentAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AccentAlpha"
    )
    
    // Loading progress animation
    val loadingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LoadingAlpha"
    )
    
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
            ),
        contentAlignment = Alignment.Center
    ) {
        // Decorative animated background circles
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 40.dp, end = 30.dp)
                .width(100.dp)
                .height(100.dp)
                .alpha(0.1f)
                .background(
                    color = VibrantCyan,
                    shape = CircleShape
                )
        )
        
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 80.dp, start = 30.dp)
                .width(120.dp)
                .height(120.dp)
                .alpha(0.1f)
                .background(
                    color = NeonTeal,
                    shape = CircleShape
                )
        )
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 60.dp)
        ) {
            // Main Icon Container with gradient - Animated
            Box(
                modifier = Modifier
                    .width(140.dp)
                    .height(140.dp)
                    .scale(iconScale)
                    .padding(top = floatOffset.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                ElectricBlue,
                                VibrantCyan
                            )
                        ),
                        shape = RoundedCornerShape(40.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Gas Station Icon (modern design)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = Color.White.copy(alpha = 0.95f),
                            shape = RoundedCornerShape(35.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "⛽",
                        fontSize = 70.sp,
                    )
                }
            }
            
            // Animated outer ring (below icon)
            Box(
                modifier = Modifier
                    .width(170.dp)
                    .height(170.dp)
                    .padding(top = 16.dp)
                    .alpha(accentAlpha * 0.5f)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                VibrantCyan.copy(alpha = 0.3f),
                                NeonTeal.copy(alpha = 0.2f)
                            )
                        ),
                        shape = CircleShape
                    )
            )

            // App Name with gradient effect
            Text(
                "FuelHub",
                fontSize = 44.sp,
                fontWeight = FontWeight.ExtraBold,
                color = VibrantCyan,
                modifier = Modifier.padding(top = 32.dp)
            )

            // Tagline with accent color
            Text(
                "Smart Fuel Management",
                fontSize = 15.sp,
                color = NeonTeal.copy(alpha = 0.85f),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            // Decorative line
            Box(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .width(60.dp)
                    .height(2.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                ElectricBlue.copy(alpha = 0.3f),
                                VibrantCyan,
                                NeonTeal.copy(alpha = 0.3f)
                            )
                        )
                    )
            )
        }

        // Animated Loading indicator at bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 50.dp),
            contentAlignment = Alignment.Center
        ) {
            // Outer animated circle
            Box(
                modifier = Modifier
                    .width(70.dp)
                    .height(70.dp)
                    .alpha(accentAlpha * 0.3f)
                    .background(
                        color = AccentOrange,
                        shape = CircleShape
                    )
            )
            
            // Progress indicator
            CircularProgressIndicator(
                modifier = Modifier
                    .width(50.dp)
                    .height(50.dp)
                    .alpha(loadingAlpha),
                color = VibrantCyan,
                strokeWidth = 3.dp
            )
            
            // Center dot
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .height(8.dp)
                    .background(
                        color = AccentOrange,
                        shape = CircleShape
                    )
            )
        }
        
        // Loading text
        Text(
            "Loading...",
            fontSize = 12.sp,
            color = NeonTeal.copy(alpha = 0.6f),
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 15.dp)
                .alpha(loadingAlpha)
        )
    }
}
