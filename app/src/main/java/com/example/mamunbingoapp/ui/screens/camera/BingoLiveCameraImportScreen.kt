package com.example.mamunbingoapp.ui.screens.camera

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.mamunbingoapp.ui.components.AppPrimaryButton
import androidx.core.content.ContextCompat
import com.example.mamunbingoapp.scanner.ImportTicketQrPreOcr
import com.example.mamunbingoapp.scanner.tryDecodeBingoQrFromInputImage
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.viewmodel.finalUiGridRowMajor
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

private const val TAG = "ImportTicketQr"
private const val LIVE_NO_BINGO_LOG_MS = 2000L
private const val MIN_FRAME_INTERVAL_MS = 350L
/** Preview fade + handoff to GMS; keep total under 300ms. */
private const val DOCUMENT_SCAN_FADE_MS = 220
private const val DOCUMENT_SCAN_HANDOFF_MS = 240L
private const val PREVIEW_FADE_TARGET = 0.38f

@Composable
private fun BingoCameraQrViewfinder() {
    val labelColor = Color.White.copy(alpha = 0.95f)
    val borderColor = MaterialTheme.colorScheme.primary
    val density = LocalDensity.current
    val corner = Dimens.radiusCard
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val minSide = minOf(maxWidth, maxHeight)
        val frameSize = minSide * 0.64f
        val wPx = with(density) { maxWidth.toPx() }
        val hPx = with(density) { maxHeight.toPx() }
        val frameWpx = with(density) { frameSize.toPx() }
        val frameHpx = frameWpx
        val left = (wPx - frameWpx) / 2f
        val top = hPx * 0.26f
        val rPx = with(density) { corner.toPx() }
        val scrim = Color.Black.copy(alpha = 0.5f)
        val borderC = borderColor.copy(alpha = 0.92f)
        Canvas(Modifier.fillMaxSize()) {
            val path = Path().apply {
                fillType = PathFillType.EvenOdd
                addRect(Rect(0f, 0f, size.width, size.height))
                addRoundRect(
                    RoundRect(
                        left = left,
                        top = top,
                        right = left + frameWpx,
                        bottom = top + frameHpx,
                        cornerRadius = CornerRadius(rPx, rPx),
                    )
                )
            }
            drawPath(path, scrim)
            drawRoundRect(
                color = borderC,
                topLeft = androidx.compose.ui.geometry.Offset(left, top),
                size = androidx.compose.ui.geometry.Size(frameWpx, frameHpx),
                cornerRadius = CornerRadius(rPx, rPx),
                style = Stroke(width = 2.5.dp.toPx()),
            )
        }
        Text(
            text = "Point at Bingo QR",
            style = MaterialTheme.typography.labelLarge,
            color = labelColor,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = frameSize * 0.5f + 10.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BingoLiveCameraImportScreen(
    onBingoQrDecoded: (rowMajor: List<Int>, serial: String?, los: String?) -> Unit,
    onScanFullTicket: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }
    if (activity == null) {
        onBack()
        return
    }
    if (!hasCameraPermission) {
        Column(
            modifier
                .fillMaxSize()
                .padding(Dimens.screenHorizontalPadding, Dimens.spacing16),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Camera access is required to read a Bingo ticket QR in real time.", textAlign = TextAlign.Center)
            Spacer(Modifier.height(Dimens.spacing16))
            Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                Text("Grant camera permission")
            }
            Spacer(Modifier.height(Dimens.spacing8))
            Button(
                onClick = onScanFullTicket,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Use document camera instead") }
            Spacer(Modifier.height(Dimens.spacing4))
            TextButton(onClick = onBack) { Text("Back") }
        }
        return
    }
    val mainExecutor = remember { ContextCompat.getMainExecutor(context) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val handled = remember { AtomicBoolean(false) }
    val lastNoBingoLog = remember { AtomicLong(0) }
    val lastFrameProcess = remember { AtomicLong(0) }
    val processCameraRef = remember { AtomicReference<ProcessCameraProvider?>(null) }
    val previewView = remember { PreviewView(context) }
    val onBingoQrDecodedState = rememberUpdatedState(onBingoQrDecoded)
    val onScanFullTicketState = rememberUpdatedState(onScanFullTicket)
    var exitingToDocument by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val previewAlpha by animateFloatAsState(
        targetValue = if (exitingToDocument) PREVIEW_FADE_TARGET else 1f,
        animationSpec = tween(
            durationMillis = DOCUMENT_SCAN_FADE_MS,
            easing = FastOutSlowInEasing
        ),
        label = "previewHandoff"
    )
    DisposableEffect(activity) {
        val future = ProcessCameraProvider.getInstance(context)
        val listener = Runnable {
            if (handled.get()) return@Runnable
            val cameraProvider = try {
                future.get()
            } catch (e: Exception) {
                Log.d(TAG, "ProcessCameraProvider.get() failed: ${e.message}")
                return@Runnable
            }
            processCameraRef.set(cameraProvider)
            if (handled.get()) return@Runnable
            val preview = Preview.Builder().build().apply {
                setSurfaceProvider(previewView.surfaceProvider)
            }
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                try {
                    if (handled.get()) return@setAnalyzer
                    val now = System.currentTimeMillis()
                    if (now - lastFrameProcess.get() < MIN_FRAME_INTERVAL_MS) {
                        return@setAnalyzer
                    }
                    lastFrameProcess.set(now)
                    val media = imageProxy.image ?: return@setAnalyzer
                    val input = InputImage.fromMediaImage(
                        media,
                        imageProxy.imageInfo.rotationDegrees,
                    )
                    val throttledNoBingo: () -> Unit = {
                        val t = System.currentTimeMillis()
                        if (t - lastNoBingoLog.get() >= LIVE_NO_BINGO_LOG_MS) {
                            lastNoBingoLog.set(t)
                            Log.d(TAG, "live camera no bingo QR")
                        }
                    }
                    when (val res = tryDecodeBingoQrFromInputImage(input, throttledNoBingo)) {
                        is ImportTicketQrPreOcr.Decoded -> {
                            if (handled.compareAndSet(false, true)) {
                                val nums = finalUiGridRowMajor(res.numbers)
                                val s = res.serial
                                val l = res.los
                                mainExecutor.execute {
                                    runCatching { processCameraRef.getAndSet(null)?.unbindAll() }
                                    onBingoQrDecodedState.value(nums, s, l)
                                }
                            }
                        }
                        ImportTicketQrPreOcr.NoBingoQrContinueOcr -> Unit
                    }
                } finally {
                    imageProxy.close()
                }
            }
            try {
                cameraProvider.unbindAll()
                if (!handled.get()) {
                    cameraProvider.bindToLifecycle(
                        activity,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis,
                    )
                }
            } catch (e: Exception) {
                Log.d(TAG, "bindToLifecycle failed: ${e.message}")
            }
        }
        future.addListener(listener, mainExecutor)
        onDispose {
            runCatching { processCameraRef.getAndSet(null)?.unbindAll() }
            cameraExecutor.shutdown()
        }
    }
    BackHandler(enabled = !exitingToDocument, onBack = onBack)
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = previewAlpha }
        ) {
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize()
            )
            BingoCameraQrViewfinder()
        }
        TopAppBar(
            title = { Text("Bingo ticket QR", style = MaterialTheme.typography.titleLarge) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        "Back",
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.88f),
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        )
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = Dimens.spacing24, vertical = Dimens.spacing16),
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(Dimens.radiusXL),
            shadowElevation = 0.dp,
            tonalElevation = 0.dp,
        ) {
            Column(Modifier.padding(Dimens.spacing20)) {
                Text(
                    text = "Scan a Bingo QR to import instantly.",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(Dimens.spacing8))
                Text(
                    text = "Use full ticket scan for printed sheets.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(Dimens.spacing16))
                Text(
                    text = "QR opens automatically when detected",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
                )
                Spacer(Modifier.height(Dimens.spacing8))
                AppPrimaryButton(
                    text = "Full ticket scan",
                    loading = exitingToDocument,
                    onClick = {
                        if (exitingToDocument) return@AppPrimaryButton
                        exitingToDocument = true
                        scope.launch {
                            delay(DOCUMENT_SCAN_HANDOFF_MS)
                            onScanFullTicketState.value()
                        }
                    },
                )
            }
        }
    }
}
