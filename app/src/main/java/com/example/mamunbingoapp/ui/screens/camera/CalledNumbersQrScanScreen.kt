package com.example.mamunbingoapp.ui.screens.camera

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.mamunbingoapp.R
import com.example.mamunbingoapp.scanner.tryReadFirstQrRawValueFromInputImage
import com.example.mamunbingoapp.theme.Dimens
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

private const val TAG = "CalledNumbersQrScan"
private const val MIN_FRAME_INTERVAL_MS = 280L
private const val VIEWFINDER_FRAME_MIN_SIDE_FRACTION = 0.68f
private const val VIEWFINDER_TOP_FRACTION = 0.30f

@Composable
fun CalledNumbersQrScanScreen(
    onQrScanned: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as? ComponentActivity
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted -> hasCameraPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (activity == null) {
        LaunchedEffect(Unit) { onBack() }
        return
    }

    BackHandler(onBack = onBack)

    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        if (!hasCameraPermission) {
            CalledNumbersQrPermissionContent(
                statusBarPadding = statusBarPadding,
                navBarPadding = navBarPadding,
                onBack = onBack,
                onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
            )
            return@Box
        }

        val mainExecutor = remember { ContextCompat.getMainExecutor(context) }
        val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
        val handled = remember { AtomicBoolean(false) }
        val lastFrameProcess = remember { AtomicLong(0) }
        val processCameraRef = remember { AtomicReference<ProcessCameraProvider?>(null) }
        val previewView = remember { PreviewView(context) }

        DisposableEffect(activity) {
            val future = ProcessCameraProvider.getInstance(context)
            val listener = Runnable {
                if (handled.get()) return@Runnable
                val cameraProvider = try {
                    future.get()
                } catch (e: Exception) {
                    Log.w(TAG, "ProcessCameraProvider.get() failed: ${e.message}")
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
                        if (now - lastFrameProcess.get() < MIN_FRAME_INTERVAL_MS) return@setAnalyzer
                        lastFrameProcess.set(now)
                        val media = imageProxy.image ?: return@setAnalyzer
                        val input = InputImage.fromMediaImage(
                            media,
                            imageProxy.imageInfo.rotationDegrees,
                        )
                        val raw = tryReadFirstQrRawValueFromInputImage(input)
                        if (!raw.isNullOrBlank() && handled.compareAndSet(false, true)) {
                            mainExecutor.execute {
                                runCatching { processCameraRef.getAndSet(null)?.unbindAll() }
                                onQrScanned(raw)
                            }
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
                    Log.w(TAG, "bindToLifecycle failed: ${e.message}")
                }
            }
            future.addListener(listener, mainExecutor)
            onDispose {
                runCatching { processCameraRef.getAndSet(null)?.unbindAll() }
                cameraExecutor.shutdown()
            }
        }

        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize(),
        )
        CalledNumbersQrScanOverlay(
            statusBarPadding = statusBarPadding,
            navBarPadding = navBarPadding,
            onBack = onBack,
        )
    }
}

@Composable
private fun CalledNumbersQrScanOverlay(
    statusBarPadding: androidx.compose.foundation.layout.PaddingValues,
    navBarPadding: androidx.compose.foundation.layout.PaddingValues,
    onBack: () -> Unit,
) {
    val primary = MaterialTheme.colorScheme.primary
    Box(Modifier.fillMaxSize()) {
        CalledNumbersQrViewfinderFrame(primaryColor = primary)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = 0.72f),
                        0.55f to Color.Black.copy(alpha = 0.28f),
                        1f to Color.Transparent,
                    ),
                )
                .padding(statusBarPadding)
                .padding(horizontal = Dimens.screenHorizontalPadding)
                .padding(top = Dimens.spacing8, bottom = Dimens.spacing24),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacing12),
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White.copy(alpha = 0.16f), CircleShape),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.common_back),
                        tint = Color.White,
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacing4)) {
                    Text(
                        text = stringResource(R.string.called_numbers_qr_scan_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                    )
                    Text(
                        text = stringResource(R.string.called_numbers_qr_scan_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.78f),
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.35f to Color.Black.copy(alpha = 0.35f),
                        1f to Color.Black.copy(alpha = 0.82f),
                    ),
                )
                .padding(navBarPadding)
                .padding(horizontal = Dimens.screenHorizontalPadding)
                .padding(top = Dimens.spacing32, bottom = Dimens.spacing24),
        ) {
            CalledNumbersQrScanHintCard(
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun CalledNumbersQrScanHintCard(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(Dimens.radiusLarge),
        color = Color.White.copy(alpha = 0.14f),
        border = BorderStroke(Dimens.cardBorderDefault, Color.White.copy(alpha = 0.22f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Dimens.spacing16, vertical = Dimens.spacing14),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacing12),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.White.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(Dimens.iconDefault),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacing4)) {
                Text(
                    text = stringResource(R.string.called_numbers_qr_scan_instruction),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                )
                Text(
                    text = stringResource(R.string.called_numbers_qr_scan_replace_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.72f),
                )
            }
        }
    }
}

@Composable
private fun CalledNumbersQrViewfinderFrame(primaryColor: Color) {
    val density = LocalDensity.current
    val cornerRadius = Dimens.radiusXL
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val minSide = minOf(maxWidth, maxHeight)
        val frameSize = minSide * VIEWFINDER_FRAME_MIN_SIDE_FRACTION
        val wPx = with(density) { maxWidth.toPx() }
        val hPx = with(density) { maxHeight.toPx() }
        val frameWpx = with(density) { frameSize.toPx() }
        val frameHpx = frameWpx
        val left = (wPx - frameWpx) / 2f
        val top = hPx * VIEWFINDER_TOP_FRACTION
        val rPx = with(density) { cornerRadius.toPx() }
        val cornerLenPx = with(density) { (Dimens.spacing32 + Dimens.spacing8).toPx() }
        val cornerRadiusPx = with(density) { Dimens.radiusCard.toPx() }
        val bracketStroke = with(density) { 3.5.dp.toPx() }
        val scrim = Color.Black.copy(alpha = 0.48f)
        val bracketColor = primaryColor.copy(alpha = 0.92f)

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
                    ),
                )
            }
            drawPath(path, scrim)
            drawRoundRect(
                color = Color.White.copy(alpha = 0.04f),
                topLeft = Offset(left, top),
                size = Size(frameWpx, frameHpx),
                cornerRadius = CornerRadius(rPx, rPx),
            )
            fun drawCornerBracket(
                arcTopLeft: Offset,
                arcStartAngle: Float,
                hStart: Offset,
                hEnd: Offset,
                vStart: Offset,
                vEnd: Offset,
            ) {
                drawArc(
                    color = bracketColor,
                    startAngle = arcStartAngle,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = Size(cornerRadiusPx * 2f, cornerRadiusPx * 2f),
                    style = Stroke(width = bracketStroke),
                )
                drawLine(
                    color = bracketColor,
                    start = hStart,
                    end = hEnd,
                    strokeWidth = bracketStroke,
                )
                drawLine(
                    color = bracketColor,
                    start = vStart,
                    end = vEnd,
                    strokeWidth = bracketStroke,
                )
            }
            drawCornerBracket(
                arcTopLeft = Offset(left, top),
                arcStartAngle = 180f,
                hStart = Offset(left + cornerRadiusPx, top),
                hEnd = Offset(left + cornerLenPx, top),
                vStart = Offset(left, top + cornerRadiusPx),
                vEnd = Offset(left, top + cornerLenPx),
            )
            drawCornerBracket(
                arcTopLeft = Offset(left + frameWpx - cornerRadiusPx * 2f, top),
                arcStartAngle = 270f,
                hStart = Offset(left + frameWpx - cornerLenPx, top),
                hEnd = Offset(left + frameWpx - cornerRadiusPx, top),
                vStart = Offset(left + frameWpx, top + cornerRadiusPx),
                vEnd = Offset(left + frameWpx, top + cornerLenPx),
            )
            drawCornerBracket(
                arcTopLeft = Offset(left + frameWpx - cornerRadiusPx * 2f, top + frameHpx - cornerRadiusPx * 2f),
                arcStartAngle = 0f,
                hStart = Offset(left + frameWpx, top + frameHpx - cornerLenPx),
                hEnd = Offset(left + frameWpx, top + frameHpx - cornerRadiusPx),
                vStart = Offset(left + frameWpx - cornerLenPx, top + frameHpx),
                vEnd = Offset(left + frameWpx - cornerRadiusPx, top + frameHpx),
            )
            drawCornerBracket(
                arcTopLeft = Offset(left, top + frameHpx - cornerRadiusPx * 2f),
                arcStartAngle = 90f,
                hStart = Offset(left, top + frameHpx - cornerLenPx),
                hEnd = Offset(left, top + frameHpx - cornerRadiusPx),
                vStart = Offset(left + cornerRadiusPx, top + frameHpx),
                vEnd = Offset(left + cornerLenPx, top + frameHpx),
            )
        }
    }
}

@Composable
private fun CalledNumbersQrPermissionContent(
    statusBarPadding: androidx.compose.foundation.layout.PaddingValues,
    navBarPadding: androidx.compose.foundation.layout.PaddingValues,
    onBack: () -> Unit,
    onRequestPermission: () -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        CalledNumbersQrScanOverlay(
            statusBarPadding = statusBarPadding,
            navBarPadding = navBarPadding,
            onBack = onBack,
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(statusBarPadding)
                .padding(navBarPadding)
                .padding(horizontal = Dimens.screenHorizontalPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.camera_permission_required),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(Dimens.spacing16))
            Button(onClick = onRequestPermission) {
                Text(stringResource(R.string.camera_grant_permission))
            }
            Spacer(Modifier.height(Dimens.spacing8))
            TextButton(onClick = onBack) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    }
}
