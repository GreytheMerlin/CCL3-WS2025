package com.example.snorly.feature.challenges.qr

import android.annotation.SuppressLint
import androidx.camera.core.*
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.atomic.AtomicBoolean

@Composable
fun QrChallengeScreen(
    state: QrChallengeUiState,
    onEnableCamera: () -> Unit,
    onTryAgainPermission: () -> Unit,
    onQrScanned: (String) -> Unit,
    onSimulateSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = Brush.verticalGradient(listOf(Color(0xFF0B0F1A), Color(0xFF060812)))

    Surface(modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxSize()
                .background(bg)
                .padding(24.dp)
        ) {
            Column(Modifier.fillMaxSize()) {
                Text("Scan QR Code", style = MaterialTheme.typography.titleLarge, color = Color.White)
                Spacer(Modifier.height(70.dp))

                when {
                    state.hasPermission -> {
                        if (state.success) {
                            QrSuccessCard()
                        } else {
                            QrScannerContent(
                                expectedValue = state.expectedValue,
                                showWrongQr = state.showWrongQr,
                                onQrScanned = onQrScanned,
                                onSimulateSuccess = onSimulateSuccess
                            )
                        }
                    }

                    state.denied -> {
                        CameraDeniedCard(onTryAgain = onTryAgainPermission)
                    }

                    else -> {
                        CameraRequiredCard(onEnable = onEnableCamera)
                    }
                }
            }
        }
    }
}

@Composable
private fun CameraRequiredCard(onEnable: () -> Unit) {
    val purple = Color(0xFF8B5CF6)

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0x22FFFFFF))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🔳", style = MaterialTheme.typography.displaySmall)
                Spacer(Modifier.height(10.dp))
                Text("Camera Access Required", style = MaterialTheme.typography.titleLarge, color = Color.White)
                Spacer(Modifier.height(8.dp))
                Text(
                    "To dismiss this alarm, you need to scan your personal QR code. Please allow camera access.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFB0B0B0),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = onEnable,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = purple),
                    shape = RoundedCornerShape(14.dp)
                ) { Text("Enable Camera") }
            }
        }

        Spacer(Modifier.height(16.dp))
        TipCard("💡 Tip: Place your QR code in your bathroom or kitchen to force yourself to get up!")
    }
}

@Composable
private fun CameraDeniedCard(onTryAgain: () -> Unit) {
    val purple = Color(0xFF8B5CF6)

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0x33AA0000))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("📷", style = MaterialTheme.typography.displaySmall)
                Spacer(Modifier.height(10.dp))
                Text("Camera Access Denied", style = MaterialTheme.typography.titleLarge, color = Color.White)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Camera access denied",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFDDDDDD),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = onTryAgain,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = purple),
                    shape = RoundedCornerShape(14.dp)
                ) { Text("Try Again") }
            }
        }
    }
}

@Composable
private fun TipCard(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x22FFFFFF))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(14.dp),
            color = Color(0xFFDDDDDD),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun QrScannerContent(
    expectedValue: String?,
    showWrongQr: Boolean,
    onQrScanned: (String) -> Unit,
    onSimulateSuccess: () -> Unit
) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        QrScannerPreview(onQrScanned = onQrScanned)

        Spacer(Modifier.height(14.dp))
        TipCard("🔳 Point your camera at the QR code to dismiss the alarm")

        Spacer(Modifier.height(14.dp))
        val purple = Color(0xFF8B5CF6)
        Button(
            onClick = onSimulateSuccess,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = purple),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Simulate Successful Scan")
        }

        if (expectedValue != null && showWrongQr) {
            Spacer(Modifier.height(8.dp))
            Text("Wrong QR code. Try again.", color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun QrSuccessCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 120.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x3322CC66))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("✅", style = MaterialTheme.typography.displaySmall, color = Color.White)
            Spacer(Modifier.height(10.dp))
            Text("QR Code Scanned!", style = MaterialTheme.typography.titleLarge, color = Color.White)
            Spacer(Modifier.height(6.dp))
            Text("Alarm dismissed successfully", color = Color(0xFFB0B0B0))
        }
    }
}

@SuppressLint("UnsafeOptInUsageError")
@Composable
private fun QrScannerPreview(
    onQrScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val isProcessing = remember { AtomicBoolean(false) }

    val scanner = remember {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
        BarcodeScanning.getClient(options)
    }

    val previewView = remember {
        PreviewView(context).apply { scaleType = PreviewView.ScaleType.FILL_CENTER }
    }

    DisposableEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val executor = ContextCompat.getMainExecutor(context)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val selector = ResolutionSelector.Builder()
                .setAspectRatioStrategy(
                    AspectRatioStrategy(
                        AspectRatio.RATIO_16_9,
                        AspectRatioStrategy.FALLBACK_RULE_AUTO
                    )
                )
                .build()

            val analysis = ImageAnalysis.Builder()
                .setResolutionSelector(selector)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            analysis.setAnalyzer(executor) { imageProxy ->
                if (isProcessing.get()) {
                    imageProxy.close(); return@setAnalyzer
                }

                val mediaImage = imageProxy.image
                if (mediaImage == null) {
                    imageProxy.close(); return@setAnalyzer
                }

                isProcessing.set(true)
                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

                scanner.process(image)
                    .addOnSuccessListener { barcodes ->
                        val value = barcodes.firstOrNull()?.rawValue
                        if (!value.isNullOrBlank()) onQrScanned(value)
                    }
                    .addOnCompleteListener {
                        isProcessing.set(false)
                        imageProxy.close()
                    }
            }

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
        }, executor)

        onDispose {
            try {
                ProcessCameraProvider.getInstance(context).get().unbindAll()
            } catch (_: Exception) {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0x22000000)),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0x11000000))
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0x33000000)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🔳", color = Color(0xFFB0B0B0))
                Spacer(Modifier.height(6.dp))
                Text("Point camera at QR code", color = Color(0xFFB0B0B0))
                Spacer(Modifier.height(4.dp))
                Text("(Auto scanning…)", color = Color(0xFF777777))
            }
        }
    }
}