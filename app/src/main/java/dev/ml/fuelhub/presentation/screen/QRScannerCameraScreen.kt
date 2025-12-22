package dev.ml.fuelhub.presentation.screen

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import dev.ml.fuelhub.ui.theme.DeepBlue
import dev.ml.fuelhub.ui.theme.VibrantCyan
import timber.log.Timber
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Real-time QR Code Scanner using ML Kit and CameraX with Enhanced UI
 */
@Composable
fun QRScannerCameraScreen(
    onCodeScanned: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var hasCameraPermission by remember { mutableStateOf(false) }
    var scannedCode by remember { mutableStateOf("") }
    
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }
    
    LaunchedEffect(Unit) {
        hasCameraPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        
        if (!hasCameraPermission) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    
    // Auto-dismiss when code is scanned
    LaunchedEffect(scannedCode) {
        if (scannedCode.isNotEmpty()) {
            onCodeScanned(scannedCode)
            onDismiss()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlue)
    ) {
        if (hasCameraPermission) {
            CameraPreviewWithScanning(
                onCodeScanned = { code ->
                    scannedCode = code
                }
            )
            
            // Close button
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Camera Permission Required",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "We need access to your camera to scan QR codes",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                    colors = ButtonDefaults.buttonColors(containerColor = VibrantCyan),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Grant Permission", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = VibrantCyan
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .border(
                            width = 2.dp,
                            color = VibrantCyan,
                            shape = ButtonDefaults.shape
                        )
                ) {
                    Text("Cancel", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalGetImage::class)
@Composable
fun CameraPreviewWithScanning(
    onCodeScanned: (String) -> Unit
) {
    val context = LocalContext.current
    var lastScannedCode by remember { mutableStateOf("") }
    var lastScanTime by remember { mutableStateOf(0L) }
    
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    
                    val barcodeScanner = BarcodeScanning.getClient(
                        BarcodeScannerOptions.Builder()
                            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                            .build()
                    )
                    
                    val imageAnalyzer = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also { imageAnalysis ->
                            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                val mediaImage = imageProxy.image
                                if (mediaImage != null) {
                                    val image = InputImage.fromMediaImage(
                                        mediaImage,
                                        imageProxy.imageInfo.rotationDegrees
                                    )
                                    
                                    barcodeScanner.process(image)
                                        .addOnSuccessListener { barcodes ->
                                            for (barcode in barcodes) {
                                                val rawValue = barcode.rawValue
                                                if (rawValue != null && rawValue != lastScannedCode) {
                                                    val currentTime = System.currentTimeMillis()
                                                    // Debounce: ignore same code within 500ms
                                                    if (currentTime - lastScanTime > 500) {
                                                        lastScannedCode = rawValue
                                                        lastScanTime = currentTime
                                                        onCodeScanned(rawValue)
                                                        Timber.d("QR Code scanned: $rawValue")
                                                    }
                                                }
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            Timber.e(e, "Barcode scanning failed")
                                        }
                                        .addOnCompleteListener {
                                            imageProxy.close()
                                        }
                                }
                            }
                        }
                    
                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            ctx as androidx.lifecycle.LifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageAnalyzer
                        )
                    } catch (e: Exception) {
                        Timber.e(e, "Camera binding failed")
                    }
                }, ContextCompat.getMainExecutor(ctx))
                
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // Header Text
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Scan QR Code",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Point camera at the QR code",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
        
        // QR Frame Guide - Corner brackets only
        Canvas(
            modifier = Modifier
                .align(Alignment.Center)
                .size(300.dp)
        ) {
            val cornerSize = 50.dp.toPx()
            val strokeWidth = 3.5f.dp.toPx()
            val offset = 15.dp.toPx()
            val centerX = size.width / 2
            val centerY = size.height / 2
            val frameSize = 220.dp.toPx()
            
            // Top-left corner
            drawLine(
                color = VibrantCyan,
                start = androidx.compose.ui.geometry.Offset(centerX - frameSize / 2, centerY - frameSize / 2),
                end = androidx.compose.ui.geometry.Offset(centerX - frameSize / 2 + cornerSize, centerY - frameSize / 2),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = VibrantCyan,
                start = androidx.compose.ui.geometry.Offset(centerX - frameSize / 2, centerY - frameSize / 2),
                end = androidx.compose.ui.geometry.Offset(centerX - frameSize / 2, centerY - frameSize / 2 + cornerSize),
                strokeWidth = strokeWidth
            )
            
            // Top-right corner
            drawLine(
                color = VibrantCyan,
                start = androidx.compose.ui.geometry.Offset(centerX + frameSize / 2, centerY - frameSize / 2),
                end = androidx.compose.ui.geometry.Offset(centerX + frameSize / 2 - cornerSize, centerY - frameSize / 2),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = VibrantCyan,
                start = androidx.compose.ui.geometry.Offset(centerX + frameSize / 2, centerY - frameSize / 2),
                end = androidx.compose.ui.geometry.Offset(centerX + frameSize / 2, centerY - frameSize / 2 + cornerSize),
                strokeWidth = strokeWidth
            )
            
            // Bottom-left corner
            drawLine(
                color = VibrantCyan,
                start = androidx.compose.ui.geometry.Offset(centerX - frameSize / 2, centerY + frameSize / 2),
                end = androidx.compose.ui.geometry.Offset(centerX - frameSize / 2 + cornerSize, centerY + frameSize / 2),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = VibrantCyan,
                start = androidx.compose.ui.geometry.Offset(centerX - frameSize / 2, centerY + frameSize / 2),
                end = androidx.compose.ui.geometry.Offset(centerX - frameSize / 2, centerY + frameSize / 2 - cornerSize),
                strokeWidth = strokeWidth
            )
            
            // Bottom-right corner
            drawLine(
                color = VibrantCyan,
                start = androidx.compose.ui.geometry.Offset(centerX + frameSize / 2, centerY + frameSize / 2),
                end = androidx.compose.ui.geometry.Offset(centerX + frameSize / 2 - cornerSize, centerY + frameSize / 2),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = VibrantCyan,
                start = androidx.compose.ui.geometry.Offset(centerX + frameSize / 2, centerY + frameSize / 2),
                end = androidx.compose.ui.geometry.Offset(centerX + frameSize / 2, centerY + frameSize / 2 - cornerSize),
                strokeWidth = strokeWidth
            )
        }
        
        // Instructions at the bottom
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Black.copy(alpha = 0.7f)
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "✓",
                            color = VibrantCyan,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            "Good lighting",
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "✓",
                            color = VibrantCyan,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            "Steady hands",
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "✓",
                            color = VibrantCyan,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            "Position code in frame",
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }
                }
            }
            
            Text(
                "Scanning...",
                color = VibrantCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
