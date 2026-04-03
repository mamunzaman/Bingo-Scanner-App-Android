package com.example.mamunbingoapp.ui.screens.scan

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.content.pm.PackageManager
import android.os.Build
import android.os.SystemClock
import android.util.Log
import android.view.Surface
import android.view.WindowManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.zIndex
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mamunbingoapp.theme.Dimens
import java.io.File
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.ArrayDeque
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

private const val SCAN_ENTRY_HANDOFF_TAG = "scan-entry-handoff"

private const val CaptureReadyOn = 0.75f
private const val AutoCaptureStableMs = 400L
private const val SnapCountdownStepMs = 450L
private const val ScanFrameHeightOfUsableBand = 0.88f
private const val ScanFrameExtraBottomGapFrac = 0.042f

private fun inflateRoundRect(rr: RoundRect, delta: Float): RoundRect {
    val nl = rr.left - delta
    val nt = rr.top - delta
    val nr = rr.right + delta
    val nb = rr.bottom + delta
    val w = nr - nl
    val h = nb - nt
    val maxCorner = minOf(w, h) / 2f
    val newR = (rr.topLeftCornerRadius.x + delta).coerceIn(0f, maxCorner)
    val cr = CornerRadius(newR, newR)
    return RoundRect(
        left = nl,
        top = nt,
        right = nr,
        bottom = nb,
        topLeftCornerRadius = cr,
        topRightCornerRadius = cr,
        bottomRightCornerRadius = cr,
        bottomLeftCornerRadius = cr
    )
}

private fun Context.displayRotationCompat(): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        display?.rotation ?: Surface.ROTATION_0
    } else {
        @Suppress("DEPRECATION")
        (getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.rotation
    }
}

private fun rotateEdgeBoundsToLogical(
    minX: Int,
    maxX: Int,
    minY: Int,
    maxY: Int,
    rotationDegrees: Int,
    bufferWidth: Int,
    bufferHeight: Int
): IntArray {
    val r = ((rotationDegrees % 360) + 360) % 360
    val corners = listOf(
        minX to minY,
        maxX to minY,
        minX to maxY,
        maxX to maxY
    )
    val transformed = corners.map { (x, y) ->
        when (r) {
            0 -> x to y
            90 -> y to (bufferWidth - x)
            180 -> (bufferWidth - x) to (bufferHeight - y)
            270 -> (bufferHeight - y) to x
            else -> x to y
        }
    }
    val xs = transformed.map { it.first }
    val ys = transformed.map { it.second }
    return intArrayOf(xs.min(), xs.max(), ys.min(), ys.max())
}

private fun runDirectScanCapture(
    imageCapture: ImageCapture,
    context: Context,
    onSaved: (Bitmap?) -> Unit,
    onCaptureError: () -> Unit
) {
    val outputFile = File.createTempFile(
        "direct-scan-capture-",
        ".jpg",
        context.cacheDir
    )
    val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()
    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                onSaved(BitmapFactory.decodeFile(outputFile.absolutePath))
            }

            override fun onError(exception: ImageCaptureException) {
                onCaptureError()
            }
        }
    )
}

/**
 * Route `directScan`: main Scan tab → `ScanScreen` → `onLaunchCamera`. Live CameraX auto-detect; not `historyPhotoImport` / `HistoryPhotoImportScreen`.
 */
@Composable
@Suppress("UNUSED_PARAMETER")
fun DirectScanScreen(
    onBack: () -> Unit,
    onEnterNumbers: () -> Unit
) {
    LaunchedEffect(Unit) {
        Log.d(
            SCAN_ENTRY_HANDOFF_TAG,
            "screen=DirectScanScreen route=directScan flow=live_camera_auto_detect (not HistoryPhotoImportScreen)"
        )
    }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }
    val configuration = LocalConfiguration.current
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var scanProgress by remember { mutableStateOf(0f) }
    var isAutoCaptured by remember { mutableStateOf(false) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var snapCountdown by remember { mutableStateOf<Int?>(null) }
    var countdownJob by remember { mutableStateOf<Job?>(null) }
    val autoCaptureAboveSinceMs = remember { mutableLongStateOf(-1L) }
    val scope = rememberCoroutineScope()
    val readinessWindow = remember { ArrayDeque<Float>() }
    val scanProgressLatest by rememberUpdatedState(scanProgress)
    val capturedLatest by rememberUpdatedState(capturedBitmap)
    val imageCaptureLatest by rememberUpdatedState(imageCapture)
    val isAutoCapturedLatest by rememberUpdatedState(isAutoCaptured)
    val isAnalyzingLatest by rememberUpdatedState(isAnalyzing)

    LaunchedEffect(hasCameraPermission) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    LaunchedEffect(hasCameraPermission, capturedBitmap, isAnalyzing) {
        if (!hasCameraPermission || capturedBitmap != null || isAnalyzing) {
            scanProgress = 0f
            readinessWindow.clear()
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(32)
            if (snapCountdown != null && scanProgressLatest < CaptureReadyOn) {
                countdownJob?.cancel()
                countdownJob = null
                snapCountdown = null
            }
            if (!hasCameraPermission || capturedLatest != null || isAnalyzingLatest || isAutoCapturedLatest) {
                autoCaptureAboveSinceMs.longValue = -1L
                if (snapCountdown != null) {
                    countdownJob?.cancel()
                    countdownJob = null
                    snapCountdown = null
                }
                continue
            }
            if (snapCountdown != null) {
                autoCaptureAboveSinceMs.longValue = -1L
                continue
            }
            if (imageCaptureLatest == null) {
                autoCaptureAboveSinceMs.longValue = -1L
                continue
            }
            val now = SystemClock.elapsedRealtime()
            if (scanProgressLatest >= CaptureReadyOn) {
                if (autoCaptureAboveSinceMs.longValue < 0L) {
                    autoCaptureAboveSinceMs.longValue = now
                } else if (now - autoCaptureAboveSinceMs.longValue >= AutoCaptureStableMs) {
                    autoCaptureAboveSinceMs.longValue = -1L
                    countdownJob?.cancel()
                    countdownJob = scope.launch {
                        try {
                            for (i in 3 downTo 1) {
                                if (scanProgressLatest < CaptureReadyOn || capturedLatest != null) {
                                    snapCountdown = null
                                    return@launch
                                }
                                snapCountdown = i
                                delay(SnapCountdownStepMs)
                            }
                            snapCountdown = null
                            if (scanProgressLatest < CaptureReadyOn || capturedLatest != null || isAutoCapturedLatest) {
                                return@launch
                            }
                            val cap = imageCaptureLatest ?: return@launch
                            isAutoCaptured = true
                            runDirectScanCapture(
                                cap,
                                context,
                                onSaved = { bitmap -> capturedBitmap = bitmap },
                                onCaptureError = { isAutoCaptured = false }
                            )
                        } catch (e: CancellationException) {
                            snapCountdown = null
                            throw e
                        }
                    }
                }
            } else {
                autoCaptureAboveSinceMs.longValue = -1L
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            analysisExecutor.shutdown()
        }
    }

    var navEnterBlend by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { navEnterBlend = true }
    val navEnterAlpha by animateFloatAsState(
        targetValue = if (navEnterBlend) 1f else 0.96f,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "directScanNavEnterAlpha"
    )
    val navEnterScale by animateFloatAsState(
        targetValue = if (navEnterBlend) 1f else 0.992f,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "directScanNavEnterScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                alpha = navEnterAlpha
                scaleX = navEnterScale
                scaleY = navEnterScale
            }
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (hasCameraPermission) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (capturedBitmap == null) {
                    AndroidView(
                        factory = { previewView },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Image(
                        bitmap = capturedBitmap!!.asImageBitmap(),
                        contentDescription = "Captured ticket photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                DisposableEffect(
                    lifecycleOwner,
                    hasCameraPermission,
                    capturedBitmap,
                    configuration.orientation
                ) {
                    var cameraProvider: ProcessCameraProvider? = null
                    val providerFuture = ProcessCameraProvider.getInstance(context)
                    val mainExecutor = ContextCompat.getMainExecutor(context)

                    val bindCamera = Runnable {
                        runCatching {
                            cameraProvider = providerFuture.get()
                            val targetRotation =
                                previewView.display?.rotation ?: context.displayRotationCompat()
                            val preview = Preview.Builder()
                                .setTargetRotation(targetRotation)
                                .build()
                                .also { it.setSurfaceProvider(previewView.surfaceProvider) }
                            val capture = ImageCapture.Builder()
                                .setTargetRotation(targetRotation)
                                .build()
                            val analysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .setTargetRotation(targetRotation)
                                .build()

                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                            cameraProvider?.unbindAll()
                            if (capturedBitmap == null && !isAnalyzing) {
                                analysis.setAnalyzer(analysisExecutor) { imageProxy ->
                                    val yPlane = imageProxy.planes.firstOrNull()
                                    val yBuffer = yPlane?.buffer
                                    if (yPlane == null || yBuffer == null) {
                                        imageProxy.close()
                                        return@setAnalyzer
                                    }
                                    val width = imageProxy.width
                                    val height = imageProxy.height
                                    val rowStride = yPlane.rowStride
                                    val pixelStride = yPlane.pixelStride
                                    val limit = yBuffer.limit()

                                    fun yAt(x: Int, y: Int): Int {
                                        val clampedX = x.coerceIn(0, width - 1)
                                        val clampedY = y.coerceIn(0, height - 1)
                                        val index = clampedY * rowStride + clampedX * pixelStride
                                        if (index < 0 || index >= limit) return 0
                                        return yBuffer.get(index).toInt() and 0xFF
                                    }

                                    val sampleStep = max(8, minOf(width, height) / 80)
                                    var sum = 0.0
                                    var sumSq = 0.0
                                    var count = 0
                                    var edgeCount = 0
                                    var minEdgeX = width
                                    var minEdgeY = height
                                    var maxEdgeX = 0
                                    var maxEdgeY = 0
                                    var sampleCount = 0
                                    for (y in sampleStep until (height - sampleStep) step sampleStep) {
                                        for (x in sampleStep until (width - sampleStep) step sampleStep) {
                                            val yCenter = yAt(x, y)
                                            sum += yCenter
                                            sumSq += yCenter * yCenter.toDouble()
                                            count++
                                            sampleCount++

                                            val gx = abs(yAt(x + sampleStep, y) - yAt(x - sampleStep, y))
                                            val gy = abs(yAt(x, y + sampleStep) - yAt(x, y - sampleStep))
                                            val grad = gx + gy
                                            if (grad > 40) {
                                                edgeCount++
                                                if (x < minEdgeX) minEdgeX = x
                                                if (y < minEdgeY) minEdgeY = y
                                                if (x > maxEdgeX) maxEdgeX = x
                                                if (y > maxEdgeY) maxEdgeY = y
                                            }
                                        }
                                    }
                                    if (count == 0) {
                                        imageProxy.close()
                                        return@setAnalyzer
                                    }
                                    val mean = sum / count
                                    val variance = (sumSq / count) - (mean * mean)
                                    val stdDev = sqrt(variance.coerceAtLeast(0.0))

                                    val brightnessCenter = 128.0
                                    val brightnessScore = (1.0 - (abs(mean - brightnessCenter) / brightnessCenter))
                                        .coerceIn(0.0, 1.0)
                                    val contrastScore = (stdDev / 64.0).coerceIn(0.0, 1.0)
                                    val frameQuality = (brightnessScore * 0.6 + contrastScore * 0.4).toFloat()

                                    if (readinessWindow.size >= 8) readinessWindow.removeFirst()
                                    readinessWindow.addLast(frameQuality)
                                    val windowAvg = readinessWindow.average().toFloat().coerceIn(0f, 1f)
                                    val windowStd = if (readinessWindow.size >= 2) {
                                        val avg = readinessWindow.average().toFloat()
                                        sqrt(
                                            readinessWindow
                                                .map { (it - avg) * (it - avg) }
                                                .average()
                                                .toDouble()
                                        ).toFloat()
                                    } else {
                                        0f
                                    }
                                    val stabilityScore = (1f - (windowStd / 0.25f)).coerceIn(0f, 1f)

                                    val rotDeg = imageProxy.imageInfo.rotationDegrees
                                    val logicalW = if (rotDeg % 180 == 0) width else height
                                    val logicalH = if (rotDeg % 180 == 0) height else width

                                    val ticketScore = if (edgeCount > 0 && sampleCount > 0) {
                                        val lb = rotateEdgeBoundsToLogical(
                                            minEdgeX,
                                            maxEdgeX,
                                            minEdgeY,
                                            maxEdgeY,
                                            rotDeg,
                                            width,
                                            height
                                        )
                                        val boxW = (lb[1] - lb[0]).coerceAtLeast(1)
                                        val boxH = (lb[3] - lb[2]).coerceAtLeast(1)
                                        val areaRatio =
                                            (boxW.toFloat() * boxH.toFloat()) / (logicalW.toFloat() * logicalH.toFloat())
                                        val aspect = boxW.toFloat() / boxH.toFloat()
                                        val edgeDensity = edgeCount.toFloat() / sampleCount.toFloat()
                                        val areaScore = (1f - abs(areaRatio - 0.42f) / 0.32f).coerceIn(0f, 1f)

                                        // Ticket can appear landscape or rotated 90/270.
                                        val targetWide = 1.75f
                                        val targetTall = 1f / targetWide
                                        val aspectWideScore = (1f - abs(aspect - targetWide) / 1.1f).coerceIn(0f, 1f)
                                        val aspectTallScore = (1f - abs(aspect - targetTall) / 0.55f).coerceIn(0f, 1f)
                                        val aspectScore = max(aspectWideScore, aspectTallScore)

                                        // Keep center as a weak signal; do not require centered grid/card.
                                        val centerX = (lb[0] + lb[1]) / 2f
                                        val centerY = (lb[2] + lb[3]) / 2f
                                        val dxNorm = abs(centerX - (logicalW / 2f)) / (logicalW / 2f)
                                        val dyNorm = abs(centerY - (logicalH / 2f)) / (logicalH / 2f)
                                        val centerDist = sqrt(dxNorm * dxNorm + dyNorm * dyNorm)
                                        val centerScore = (1f - (centerDist / 1.15f)).coerceIn(0f, 1f)

                                        val edgeScore = (edgeDensity / 0.12f).coerceIn(0f, 1f)
                                        val sizeGate =
                                            if (boxW >= (logicalW * 0.33f) && boxH >= (logicalH * 0.25f)) 1f else 0f
                                        val fullTicketScore =
                                            ((areaScore * 0.35f + aspectScore * 0.35f + edgeScore * 0.20f + centerScore * 0.10f) * sizeGate)
                                                .coerceIn(0f, 1f)

                                        // Alternative grid-like detection for partial bingo grid scans.
                                        val gridAreaScore = (1f - abs(areaRatio - 0.22f) / 0.18f).coerceIn(0f, 1f)
                                        val gridAspectScore = (1f - abs(aspect - 1f) / 0.65f).coerceIn(0f, 1f)
                                        val gridEdgeScore = (edgeDensity / 0.17f).coerceIn(0f, 1f)
                                        val gridSizeGate =
                                            if (boxW >= (logicalW * 0.20f) && boxH >= (logicalH * 0.20f)) 1f else 0f
                                        val gridLikeScore =
                                            ((gridAreaScore * 0.35f + gridAspectScore * 0.35f + gridEdgeScore * 0.20f + centerScore * 0.10f) * gridSizeGate)
                                                .coerceIn(0f, 1f)

                                        max(fullTicketScore, gridLikeScore)
                                            .coerceIn(0f, 1f)
                                    } else {
                                        0f
                                    }

                                    val readinessBase = (windowAvg * 0.7f + stabilityScore * 0.3f).coerceIn(0f, 1f)
                                    val gatedReadiness = (readinessBase * (0.2f + 0.8f * ticketScore))
                                        .let { if (ticketScore < 0.35f) it * 0.4f else it }
                                        .coerceIn(0f, 1f)

                                    mainExecutor.execute {
                                        if (capturedBitmap == null && !isAnalyzing) {
                                            scanProgress = gatedReadiness
                                        }
                                    }
                                    imageProxy.close()
                                }
                                cameraProvider?.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview,
                                    capture,
                                    analysis
                                )
                                imageCapture = capture
                            } else {
                                imageCapture = null
                            }
                        }
                    }

                    providerFuture.addListener(bindCamera, mainExecutor)

                    onDispose {
                        cameraProvider?.unbindAll()
                    }
                }

                if (capturedBitmap == null) {
                    val countdownActive = snapCountdown != null
                    val capturePrimedForCountdown =
                        !isAnalyzing &&
                            scanProgress >= CaptureReadyOn &&
                            autoCaptureAboveSinceMs.longValue >= 0L &&
                            !countdownActive
                    DirectScanLiveLensOverlay(
                        scanProgress = scanProgress,
                        capturePrimedForCountdown = capturePrimedForCountdown,
                        countdownActive = countdownActive,
                        modifier = Modifier
                            .fillMaxSize()
                            .zIndex(1f)
                    )
                }

                DirectScanPremiumTopBar(
                    title = if (capturedBitmap == null) "Scan ticket" else "Review",
                    onBack = onBack,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .zIndex(6f)
                )

                if (capturedBitmap != null) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(16.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (!isAnalyzing) {
                                        capturedBitmap = null
                                        isAnalyzing = false
                                        isAutoCaptured = false
                                    }
                                },
                                enabled = !isAnalyzing,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Scan Again")
                            }
                            Button(
                                onClick = {
                                    if (isAnalyzing) return@Button
                                    isAnalyzing = true
                                    scope.launch {
                                        delay(1500L)
                                        isAnalyzing = false
                                    }
                                },
                                enabled = !isAnalyzing,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Analyze")
                            }
                        }
                    }
                }

                if (snapCountdown != null && capturedBitmap == null && !isAnalyzing) {
                    val countTick = snapCountdown!!
                    val density = LocalDensity.current
                    val scrimReveal = remember { Animatable(0f) }
                    LaunchedEffect(Unit) {
                        scrimReveal.animateTo(1f, tween(240, easing = FastOutSlowInEasing))
                    }
                    val ringScale = remember { Animatable(1f) }
                    val ringAlpha = remember { Animatable(0.32f) }
                    val numeralPulseScale = remember { Animatable(1f) }
                    LaunchedEffect(countTick) {
                        ringScale.snapTo(0.93f)
                        ringAlpha.snapTo(0.22f)
                        numeralPulseScale.snapTo(1f)
                        coroutineScope {
                            launch {
                                ringScale.animateTo(1f, tween(240, easing = FastOutSlowInEasing))
                                ringScale.animateTo(1.011f, tween(75, easing = FastOutSlowInEasing))
                                ringScale.animateTo(1f, tween(130, easing = FastOutSlowInEasing))
                            }
                            launch {
                                ringAlpha.animateTo(0.33f, tween(240, easing = FastOutSlowInEasing))
                                ringAlpha.animateTo(0.36f, tween(75, easing = FastOutSlowInEasing))
                                ringAlpha.animateTo(0.30f, tween(130, easing = FastOutSlowInEasing))
                            }
                            launch {
                                delay(240)
                                numeralPulseScale.animateTo(1.03f, tween(75, easing = FastOutSlowInEasing))
                                numeralPulseScale.animateTo(1f, tween(130, easing = FastOutSlowInEasing))
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.48f * scrimReveal.value))
                            .zIndex(5f),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(
                            modifier = Modifier
                                .size(220.dp)
                                .graphicsLayer {
                                    scaleX = ringScale.value
                                    scaleY = ringScale.value
                                    alpha = ringAlpha.value
                                }
                        ) {
                            val strokePx = with(density) { 1.5.dp.toPx() }
                            drawCircle(
                                color = Color.White,
                                radius = size.minDimension / 2f - strokePx / 2f,
                                style = Stroke(width = strokePx)
                            )
                        }
                        AnimatedContent(
                            targetState = countTick,
                            modifier = Modifier.align(Alignment.Center),
                            transitionSpec = {
                                (fadeIn(
                                    animationSpec = tween(
                                        durationMillis = 260,
                                        easing = FastOutSlowInEasing
                                    )
                                ) + scaleIn(
                                    initialScale = 1.52f,
                                    animationSpec = tween(
                                        durationMillis = 340,
                                        easing = FastOutSlowInEasing
                                    )
                                )).togetherWith(
                                    fadeOut(
                                        animationSpec = tween(
                                            durationMillis = 300,
                                            easing = FastOutLinearInEasing
                                        )
                                    ) + scaleOut(
                                        targetScale = 0.86f,
                                        animationSpec = tween(
                                            durationMillis = 300,
                                            easing = FastOutLinearInEasing
                                        )
                                    )
                                )
                            },
                            label = "snapCountdown"
                        ) { digit ->
                            Text(
                                text = "$digit",
                                modifier = Modifier.graphicsLayer {
                                    scaleX = numeralPulseScale.value
                                    scaleY = numeralPulseScale.value
                                },
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontSize = 168.sp,
                                    lineHeight = 178.sp,
                                    fontWeight = FontWeight.Bold,
                                    shadow = Shadow(
                                        color = Color.Black.copy(alpha = 0.30f),
                                        offset = Offset(0f, 0f),
                                        blurRadius = 12f
                                    )
                                ),
                                color = Color.White
                            )
                        }
                    }
                }

                if (capturedBitmap != null && isAnalyzing) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.30f))
                            .zIndex(8f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator()
                            Text(
                                text = "Analyzing...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Camera permission is required",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                            modifier = Modifier.padding(top = 12.dp)
                        ) {
                            Text("Allow Camera")
                        }
                    }
                }
                DirectScanPremiumTopBar(
                    title = "Scan ticket",
                    onBack = onBack,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .zIndex(2f)
                )
            }
        }
    }
}

@Composable
private fun DirectScanPremiumTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    0f to Color.Black.copy(alpha = 0.48f),
                    0.5f to Color.Black.copy(alpha = 0.14f),
                    1f to Color.Transparent
                )
            )
            .statusBarsPadding()
            .padding(
                start = Dimens.spacing4,
                end = Dimens.screenHorizontalPadding,
                top = Dimens.spacing8,
                bottom = Dimens.spacing12
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacing4)
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(Dimens.iconDefault)
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.4f),
                    offset = Offset.Zero,
                    blurRadius = 8f
                )
            ),
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun DirectScanLiveLensOverlay(
    scanProgress: Float,
    capturePrimedForCountdown: Boolean,
    countdownActive: Boolean,
    modifier: Modifier = Modifier
) {
    val chipAlpha = 0.72f
    val cardShape = MaterialTheme.shapes.large
    val infinite = rememberInfiniteTransition(label = "scanLens")
    val glowPulse by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cornerGlowPulse"
    )
    val scanLineSweep by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanLine"
    )
    val detectionBrighten = remember { Animatable(0f) }
    var prevScanProgress by remember { mutableFloatStateOf(scanProgress) }
    LaunchedEffect(scanProgress) {
        val was = prevScanProgress
        prevScanProgress = scanProgress
        if (scanProgress - was >= 0.022f) {
            detectionBrighten.snapTo(0f)
            detectionBrighten.animateTo(1f, tween(260, easing = FastOutSlowInEasing))
            detectionBrighten.animateTo(0f, tween(920, easing = LinearEasing))
        }
    }
    val brighten = detectionBrighten.value
    val lockBoost by animateFloatAsState(
        targetValue = if (capturePrimedForCountdown || countdownActive) 1f else 0f,
        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
        label = "captureLockBoost"
    )
    val cardAlpha by animateFloatAsState(
        targetValue = if (capturePrimedForCountdown || countdownActive) 0f else 1f,
        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
        label = "scanStatusCardFade"
    )
    val cardShiftDp by animateFloatAsState(
        targetValue = if (capturePrimedForCountdown || countdownActive) Dimens.spacing12.value else 0f,
        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
        label = "scanStatusCardShift"
    )
    val statusLabel = when {
        scanProgress < 0.12f -> "Align in frame"
        scanProgress < CaptureReadyOn -> "Scanning"
        else -> "Hold steady"
    }
    val density = LocalDensity.current
    val viewRoot = LocalView.current
    val cornerRadiusPx = with(density) { Dimens.radiusLarge.toPx() }
    val rootInsets = ViewCompat.getRootWindowInsets(viewRoot)
    val statusTopPx = rootInsets?.getInsets(WindowInsetsCompat.Type.statusBars())?.top?.toFloat() ?: 0f
    val navBottomPx = rootInsets?.getInsets(WindowInsetsCompat.Type.navigationBars())?.bottom?.toFloat() ?: 0f
    val topBarBelowStatusPx = with(density) {
        (Dimens.spacing8 + Dimens.buttonHeight + Dimens.spacing12).toPx()
    }
    val topBoundPx = statusTopPx + topBarBelowStatusPx + with(density) { Dimens.spacing16.toPx() }
    val bottomCardAboveNavPx = with(density) {
        (
            Dimens.spacing16 + Dimens.spacing16 + Dimens.spacing12 + Dimens.spacing12 +
                Dimens.progressBarHeight + Dimens.spacing24 + Dimens.spacing8
            ).toPx()
    }
    val bottomReservePx = navBottomPx + bottomCardAboveNavPx
    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
                val pw = size.width
                val ph = size.height
                val bottomBoundPx = ph - bottomReservePx
                val bottomBreathPx = Dimens.spacing8.toPx()
                val usableBottomPx = bottomBoundPx - bottomBreathPx
                val availH = (usableBottomPx - topBoundPx).coerceAtLeast(Dimens.spacing32.toPx())
                val hPadPx = Dimens.screenHorizontalPadding.toPx()
                val fw = (pw - 2f * hPadPx).coerceAtLeast(Dimens.spacing32.toPx())
                val fh0 =
                    maxOf(availH * ScanFrameHeightOfUsableBand, fw * 1.12f).coerceAtMost(availH)
                val left = hPadPx
                val minTopGapPx = (Dimens.spacing16 + Dimens.spacing8).toPx()
                val minBottomGapPx = (Dimens.spacing12 + Dimens.spacing8).toPx()
                val minTopY = topBoundPx + minTopGapPx
                val maxTopY = topBoundPx + availH - fh0 - minBottomGapPx
                val top = if (maxTopY >= minTopY) {
                    (minTopY + maxTopY) * 0.5f
                } else {
                    topBoundPx + (availH - fh0) * 0.5f
                }
                val topGap = top - topBoundPx
                val fhMatched = usableBottomPx - top - topGap
                val fhAfterMatch = minOf(fh0, fhMatched).coerceAtLeast(fw * 1.12f)
                val extraBottomGapPx = (availH * ScanFrameExtraBottomGapFrac)
                    .coerceIn(Dimens.spacing8.toPx(), Dimens.spacing24.toPx())
                val fh = (fhAfterMatch - extraBottomGapPx).coerceAtLeast(fw * 1.12f)
                val r = cornerRadiusPx.coerceAtMost(minOf(fw, fh) * 0.5f - 1f)
                val hole = RoundRect(
                    left = left,
                    top = top,
                    right = left + fw,
                    bottom = top + fh,
                    topLeftCornerRadius = CornerRadius(r, r),
                    topRightCornerRadius = CornerRadius(r, r),
                    bottomRightCornerRadius = CornerRadius(r, r),
                    bottomLeftCornerRadius = CornerRadius(r, r)
                )
                val featherPx = 22.dp.toPx()
                val featherOuter = inflateRoundRect(hole, featherPx)
                val farDimPath = Path().apply {
                    fillType = PathFillType.EvenOdd
                    addRect(Rect(0f, 0f, pw, ph))
                    addRoundRect(featherOuter)
                }
                drawPath(farDimPath, Color.Black.copy(alpha = 0.56f))
                val featherRingPath = Path().apply {
                    fillType = PathFillType.EvenOdd
                    addRoundRect(featherOuter)
                    addRoundRect(hole)
                }
                val cx = left + fw * 0.5f
                val cy = top + fh * 0.5f
                val innerR = minOf(fw, fh) * 0.48f
                val outerR = innerR + featherPx * 1.35f
                drawPath(
                    featherRingPath,
                    brush = Brush.radialGradient(
                        0f to Color.Black.copy(alpha = 0f),
                        0.52f to Color.Black.copy(alpha = 0.38f),
                        1f to Color.Black.copy(alpha = 0.55f),
                        center = Offset(cx, cy),
                        radius = outerR.coerceAtLeast(innerR + 1f)
                    )
                )
                val outsideHole = Path().apply {
                    fillType = PathFillType.EvenOdd
                    addRect(Rect(0f, 0f, pw, ph))
                    addRoundRect(hole)
                }
                clipPath(outsideHole) {
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.2f),
                                Color.Black.copy(alpha = 0.05f),
                                Color.Transparent,
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.045f),
                                Color.Black.copy(alpha = 0.16f)
                            ),
                            startY = 0f,
                            endY = ph
                        ),
                        topLeft = Offset.Zero,
                        size = Size(pw, ph)
                    )
                }
                val holeClip = Path().apply { addRoundRect(hole) }
                clipPath(holeClip) {
                    val lineY = top + scanLineSweep * fh
                    val lineDim = 1f - lockBoost * 0.55f
                    drawLine(
                        color = Color.White.copy(alpha = (0.05f + glowPulse * 0.055f) * lineDim),
                        start = Offset(left, lineY),
                        end = Offset(left + fw, lineY),
                        strokeWidth = 1.5.dp.toPx()
                    )
                }
                val inset = 5.dp.toPx()
                val brLen = 28.dp.toPx()
                val cornerAlpha =
                    (0.6f + glowPulse * 0.4f + brighten * 0.22f + lockBoost * 0.2f).coerceIn(0f, 1f)
                val cs = Color.White.copy(alpha = cornerAlpha)
                val strokeCorner = 2.25.dp.toPx()
                val rt = left + fw
                val bt = top + fh
                drawLine(cs, Offset(left + inset, top + inset), Offset(left + inset + brLen, top + inset), strokeCorner)
                drawLine(cs, Offset(left + inset, top + inset), Offset(left + inset, top + inset + brLen), strokeCorner)
                drawLine(cs, Offset(rt - inset, top + inset), Offset(rt - inset - brLen, top + inset), strokeCorner)
                drawLine(cs, Offset(rt - inset, top + inset), Offset(rt - inset, top + inset + brLen), strokeCorner)
                drawLine(cs, Offset(left + inset, bt - inset), Offset(left + inset + brLen, bt - inset), strokeCorner)
                drawLine(cs, Offset(left + inset, bt - inset), Offset(left + inset, bt - inset - brLen), strokeCorner)
                drawLine(cs, Offset(rt - inset, bt - inset), Offset(rt - inset - brLen, bt - inset), strokeCorner)
                drawLine(cs, Offset(rt - inset, bt - inset), Offset(rt - inset, bt - inset - brLen), strokeCorner)
                val borderW = 1.15.dp.toPx()
                val borderAlpha =
                    (0.3f + glowPulse * 0.2f + brighten * 0.26f + lockBoost * 0.24f).coerceIn(0f, 1f)
                drawRoundRect(
                    color = Color.White.copy(alpha = borderAlpha),
                    topLeft = Offset(left, top),
                    size = Size(fw, fh),
                    cornerRadius = CornerRadius(r, r),
                    style = Stroke(width = borderW)
                )
        }
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .graphicsLayer {
                    alpha = cardAlpha
                    translationY = with(density) { cardShiftDp.dp.toPx() }
                }
                .navigationBarsPadding()
                .padding(horizontal = Dimens.screenHorizontalPadding, vertical = Dimens.spacing16),
            color = MaterialTheme.colorScheme.surface.copy(alpha = chipAlpha),
            shape = cardShape,
            shadowElevation = Dimens.cardElevationSubtle
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.spacing16, vertical = Dimens.spacing12),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacing12)
            ) {
                Text(
                    text = statusLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                LinearProgressIndicator(
                    progress = { scanProgress },
                    modifier = Modifier
                        .weight(1f)
                        .height(Dimens.progressBarHeight)
                        .clip(RoundedCornerShape(Dimens.progressBarRadius)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                )
                Text(
                    text = "${(scanProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
            }
        }
    }
}
