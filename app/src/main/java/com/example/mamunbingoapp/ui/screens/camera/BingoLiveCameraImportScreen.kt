package com.example.mamunbingoapp.ui.screens.camera

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.RectF
import android.media.ExifInterface
import android.net.Uri
import android.util.Log
import android.widget.Toast
import kotlin.math.max
import kotlin.math.roundToInt
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.FlashOff
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.mamunbingoapp.R
import com.example.mamunbingoapp.ui.screens.scan.scanTypeTitleRes
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.mamunbingoapp.scanner.ImportTicketQrPreOcr
import com.example.mamunbingoapp.scanner.tryDecodeBingoQrFromInputImage
import com.example.mamunbingoapp.domain.model.BingoScanType
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.ui.screens.scan.rememberScanAnimationsEnabled
import com.example.mamunbingoapp.viewmodel.finalUiGridRowMajor
import com.google.mlkit.vision.common.InputImage
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

private const val TAG = "ImportTicketQr"
private const val CROP_LOG = "CameraXCaptureCrop"
private const val MIN_FRAME_INTERVAL_MS = 350L
private const val SHUTTER_UP_MS = 60
private const val SHUTTER_DOWN_MS = 60
private const val FRAME_PULSE_IN_MS = 80
private const val FRAME_PULSE_OUT_MS = 100
private const val FRAME_PULSE_MIN = 0.97f

/** Must match [BingoCameraQrViewfinder] (green window). */
private const val VIEWFINDER_FRAME_SIZE_MIN_SIDE = 0.64f
private const val VIEWFINDER_USABLE_TOP_FRACTION = 0.11f
private const val VIEWFINDER_USABLE_BOTTOM_FRACTION = 0.26f
private const val SCAN_LINE_PASS_MS = 2000
private const val SCAN_LINE_STATIC_PROGRESS = 0.5f
private const val SCAN_LINE_FADE_PORTION = 0.14f

private const val DECODE_MAX_DIM = 4096

private fun viewfinderFrameTop(viewH: Float, frameSize: Float): Float {
    if (viewH <= 0f) return 0f
    val usableTop = viewH * VIEWFINDER_USABLE_TOP_FRACTION
    val usableBottom = viewH * (1f - VIEWFINDER_USABLE_BOTTOM_FRACTION)
    val centered = usableTop + ((usableBottom - usableTop) - frameSize) / 2f
    return centered.coerceIn(0f, (viewH - frameSize).coerceAtLeast(0f))
}

private fun computeViewfinderFrameRectF(viewW: Float, viewH: Float): RectF {
    val minS = minOf(viewW, viewH)
    val fw = minS * VIEWFINDER_FRAME_SIZE_MIN_SIDE
    val left = (viewW - fw) * 0.5f
    val top = viewfinderFrameTop(viewH, fw)
    return RectF(left, top, left + fw, top + fw)
}

/** FILL-style preview: scale image to cover the view, centered; same mapping as [PreviewView] FILL. */
private fun viewFrameToBitmapRect(
    viewW: Int,
    viewH: Int,
    frame: RectF,
    imageW: Int,
    imageH: Int,
): android.graphics.Rect? {
    if (viewW < 1 || viewH < 1 || imageW < 1 || imageH < 1) return null
    val s = max(viewW.toFloat() / imageW, viewH.toFloat() / imageH)
    if (s <= 0f) return null
    val transX = (viewW - imageW * s) * 0.5f
    val transY = (viewH - imageH * s) * 0.5f
    var l = ((frame.left - transX) / s).roundToInt()
    var t = ((frame.top - transY) / s).roundToInt()
    var rE = ((frame.right - transX) / s).roundToInt()
    var bE = ((frame.bottom - transY) / s).roundToInt()
    if (l > rE) {
        val t2 = l
        l = rE
        rE = t2
    }
    if (t > bE) {
        val t2 = t
        t = bE
        bE = t2
    }
    val L = l.coerceIn(0, (imageW - 1).coerceAtLeast(0))
    val T = t.coerceIn(0, (imageH - 1).coerceAtLeast(0))
    val R = rE.coerceIn(L + 1, imageW)
    val B = bE.coerceIn(T + 1, imageH)
    return android.graphics.Rect(L, T, R, B)
}

/** Decode JPEG with EXIF rotation, max dimension [DECODE_MAX_DIM] (sampling). */
private fun loadBitmapForCrop(f: File): Bitmap? = runCatching {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    FileInputStream(f).use { BitmapFactory.decodeStream(it, null, bounds) }
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@runCatching null
    var sample = 1
    while (max(bounds.outWidth, bounds.outHeight) / sample > DECODE_MAX_DIM) sample *= 2
    val o2 = BitmapFactory.Options().apply { inSampleSize = sample }
    var bmp = FileInputStream(f).use { BitmapFactory.decodeStream(it, null, o2) } ?: return@runCatching null
    val exif = ExifInterface(f.path)
    val deg = when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90
        ExifInterface.ORIENTATION_ROTATE_180 -> 180
        ExifInterface.ORIENTATION_ROTATE_270 -> 270
        else -> 0
    }
    if (deg != 0) {
        val m = Matrix().apply { postRotate(deg.toFloat()) }
        val r = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, m, true)
        if (r != bmp) bmp.recycle()
        bmp = r
    }
    bmp
}.getOrElse { e ->
    Log.w(CROP_LOG, "decode failed: ${e.message}")
    null
}

/**
 * Crops the saved file to the same rect as the green [BingoCameraQrViewfinder] in [previewW]x[previewH] space, or null.
 */
private fun tryCropToViewfinderFrame(
    sourceJpeg: File,
    context: android.content.Context,
    previewW: Int,
    previewH: Int,
): File? {
    if (previewW < 2 || previewH < 2) {
        Log.w(CROP_LOG, "skip: preview not laid out ${previewW}x$previewH")
        return null
    }
    return runCatching {
        val full = loadBitmapForCrop(sourceJpeg) ?: return@runCatching null
        val frameF = computeViewfinderFrameRectF(previewW.toFloat(), previewH.toFloat())
        val cropR = viewFrameToBitmapRect(previewW, previewH, frameF, full.width, full.height) ?: run {
            full.recycle()
            return@runCatching null
        }
        if (cropR.width() < 8 || cropR.height() < 8) {
            full.recycle()
            Log.w(CROP_LOG, "skip: tiny crop $cropR")
            return@runCatching null
        }
        val cropped = try {
            Bitmap.createBitmap(full, cropR.left, cropR.top, cropR.width(), cropR.height())
        } catch (e: RuntimeException) {
            full.recycle()
            Log.w(CROP_LOG, "createBitmap: ${e.message}")
            return@runCatching null
        }
        if (cropped !== full) full.recycle()
        val out = try {
            File.createTempFile("live_full_ticket_cropped_", ".jpg", context.cacheDir)
        } catch (e: Exception) {
            cropped.recycle()
            Log.w(CROP_LOG, "temp file: ${e.message}")
            return@runCatching null
        }
        var ok = false
        try {
            FileOutputStream(out).use { os -> cropped.compress(Bitmap.CompressFormat.JPEG, 92, os) }
            ok = true
            out
        } finally {
            if (!ok) out.delete()
            cropped.recycle()
        }
    }.getOrElse { e ->
        Log.w(CROP_LOG, "crop failed: ${e.message}")
        null
    }
}

private fun scanLineVisibility(progress: Float): Float {
    val edge = SCAN_LINE_FADE_PORTION
    val fadeIn = (progress / edge).coerceIn(0f, 1f)
    val fadeOut = ((1f - progress) / edge).coerceIn(0f, 1f)
    return minOf(fadeIn, fadeOut)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSharedCornerBrackets(
    frame: RectF,
    cornerRadius: Float,
    cornerLength: Float,
    strokeWidth: Float,
    color: Color,
) {
    val clipPad = strokeWidth
    val origins = listOf(
        frame.left to frame.top,
        frame.right - cornerLength to frame.top,
        frame.right - cornerLength to frame.bottom - cornerLength,
        frame.left to frame.bottom - cornerLength,
    )
    origins.forEach { (x, y) ->
        clipRect(
            left = x - clipPad,
            top = y - clipPad,
            right = x + cornerLength + clipPad,
            bottom = y + cornerLength + clipPad,
        ) {
            drawRoundRect(
                color = color,
                topLeft = Offset(frame.left, frame.top),
                size = Size(frame.width(), frame.height()),
                cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                style = Stroke(width = strokeWidth),
            )
        }
    }
}

@Composable
private fun BingoCameraQrViewfinder(scanLineActive: Boolean) {
    val borderColor = MaterialTheme.colorScheme.primary
    val animationsEnabled = rememberScanAnimationsEnabled()
    val scanProgress = remember(animationsEnabled) {
        Animatable(if (animationsEnabled) 0f else SCAN_LINE_STATIC_PROGRESS)
    }
    LaunchedEffect(scanLineActive, animationsEnabled) {
        if (!animationsEnabled) {
            scanProgress.snapTo(SCAN_LINE_STATIC_PROGRESS)
            return@LaunchedEffect
        }
        if (!scanLineActive) return@LaunchedEffect
        while (isActive) {
            val remaining = (1f - scanProgress.value).coerceAtLeast(0f)
            val duration = (SCAN_LINE_PASS_MS * remaining).toInt().coerceAtLeast(1)
            scanProgress.animateTo(1f, tween(durationMillis = duration, easing = LinearEasing))
            scanProgress.snapTo(0f)
        }
    }
    Canvas(Modifier.fillMaxSize()) {
        val frame = computeViewfinderFrameRectF(size.width, size.height)
        val cornerRadius = Dimens.radiusCard.toPx()
        val cornerLength = (Dimens.spacing32 + Dimens.spacing12).toPx()
        val bracketStroke = 4.dp.toPx()
        val lineInset = Dimens.spacing8.toPx()
        val scrim = Color.Black.copy(alpha = 0.34f)
        val path = Path().apply {
            fillType = PathFillType.EvenOdd
            addRect(Rect(0f, 0f, size.width, size.height))
            addRoundRect(
                RoundRect(
                    left = frame.left,
                    top = frame.top,
                    right = frame.right,
                    bottom = frame.bottom,
                    cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                ),
            )
        }
        drawPath(path, scrim)
        drawRoundRect(
            color = Color.White.copy(alpha = 0.035f),
            topLeft = Offset(frame.left, frame.top),
            size = Size(frame.width(), frame.height()),
            cornerRadius = CornerRadius(cornerRadius, cornerRadius),
        )
        drawSharedCornerBrackets(
            frame = frame,
            cornerRadius = cornerRadius,
            cornerLength = cornerLength,
            strokeWidth = bracketStroke * 2f,
            color = borderColor.copy(alpha = 0.25f),
        )
        drawSharedCornerBrackets(
            frame = frame,
            cornerRadius = cornerRadius,
            cornerLength = cornerLength,
            strokeWidth = bracketStroke,
            color = borderColor.copy(alpha = 0.92f),
        )
        val progress = scanProgress.value
        val lineAlpha = scanLineVisibility(progress) * if (animationsEnabled) 0.92f else 0.7f
        val showLine = if (animationsEnabled) {
            lineAlpha > 0.02f && (scanLineActive || progress > 0.02f)
        } else {
            true
        }
        if (showLine) {
            val y = frame.top + lineInset + (frame.height() - lineInset * 2f) * progress
            val startX = frame.left + lineInset
            val endX = frame.right - lineInset
            val glow = Brush.horizontalGradient(
                colorStops = arrayOf(
                    0f to Color.Transparent,
                    0.5f to borderColor.copy(alpha = lineAlpha * 0.45f),
                    1f to Color.Transparent,
                ),
                startX = startX,
                endX = endX,
            )
            val core = Brush.horizontalGradient(
                colorStops = arrayOf(
                    0f to Color.Transparent,
                    0.5f to borderColor.copy(alpha = lineAlpha),
                    1f to Color.Transparent,
                ),
                startX = startX,
                endX = endX,
            )
            drawLine(
                brush = glow,
                start = Offset(startX, y),
                end = Offset(endX, y),
                strokeWidth = 6.dp.toPx(),
            )
            drawLine(
                brush = core,
                start = Offset(startX, y),
                end = Offset(endX, y),
                strokeWidth = 2.dp.toPx(),
            )
        }
    }
}
@Composable
fun BingoLiveCameraImportScreen(
    scanType: BingoScanType,
    onBingoQrDecoded: (rowMajor: List<Int>, serial: String?, los: String?, sheetName: String?) -> Unit,
    onFullTicketPhotoCaptured: (uri: Uri) -> Unit,
    onScanFullTicket: () -> Unit,
    onBack: () -> Unit,
    onScanBusyChanged: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    androidx.compose.runtime.LaunchedEffect(scanType) {
        Toast.makeText(
            context,
            context.getString(R.string.camera_scan_target_toast, context.getString(scanTypeTitleRes(scanType))),
            Toast.LENGTH_SHORT,
        ).show()
    }
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
                .padding(WindowInsets.statusBars.asPaddingValues())
                .padding(Dimens.screenHorizontalPadding, Dimens.spacing16),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(R.string.camera_permission_required), textAlign = TextAlign.Center)
            Spacer(Modifier.height(Dimens.spacing16))
            Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                Text(stringResource(R.string.camera_grant_permission))
            }
            Spacer(Modifier.height(Dimens.spacing8))
            Button(
                onClick = onScanFullTicket,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.camera_use_document_camera)) }
            Spacer(Modifier.height(Dimens.spacing4))
            TextButton(onClick = onBack) { Text(stringResource(R.string.common_back)) }
        }
        return
    }
    val mainExecutor = remember { ContextCompat.getMainExecutor(context) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val handled = remember { AtomicBoolean(false) }
    val lastFrameProcess = remember { AtomicLong(0) }
    val processCameraRef = remember { AtomicReference<ProcessCameraProvider?>(null) }
    val boundCameraRef = remember { AtomicReference<Camera?>(null) }
    val imageCaptureRef = remember { AtomicReference<ImageCapture?>(null) }
    val previewView = remember { PreviewView(context) }
    var torchEnabled by remember { mutableStateOf(false) }
    var hasFlashUnit by remember { mutableStateOf(false) }
    val onBingoQrDecodedState = rememberUpdatedState(onBingoQrDecoded)
    val onFullTicketPhotoCapturedState = rememberUpdatedState(onFullTicketPhotoCaptured)
    val onScanFullTicketState = rememberUpdatedState(onScanFullTicket)
    var cameraSessionReady by remember { mutableStateOf(false) }
    var fullTicketImportLocked by remember { mutableStateOf(false) }
    var capturing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val shutterAlpha = remember { Animatable(0f) }
    val frameScale = remember { Animatable(1f) }
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
            val targetRotation = try {
                previewView.display.rotation
            } catch (_: Exception) {
                0
            }
            val imageCapture = ImageCapture.Builder()
                .setTargetRotation(targetRotation)
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()
            imageCaptureRef.set(imageCapture)
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
                    when (val res = tryDecodeBingoQrFromInputImage(input, onLiveNoBingoFrame = {})) {
                        is ImportTicketQrPreOcr.Decoded -> {
                            if (handled.compareAndSet(false, true)) {
                                val nums = finalUiGridRowMajor(res.numbers)
                                val s = res.serial
                                val l = res.los
                                mainExecutor.execute {
                                    fullTicketImportLocked = true
                                    onScanBusyChanged(true)
                                    torchEnabled = false
                                    runCatching {
                                        boundCameraRef.getAndSet(null)?.cameraControl?.enableTorch(false)
                                    }
                                    runCatching { processCameraRef.getAndSet(null)?.unbindAll() }
                                    onBingoQrDecodedState.value(nums, s, l, res.sheetName)
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
                    val camera = cameraProvider.bindToLifecycle(
                        activity,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis,
                        imageCapture,
                    )
                    boundCameraRef.set(camera)
                    hasFlashUnit = camera.cameraInfo.hasFlashUnit()
                    if (!hasFlashUnit) torchEnabled = false
                    cameraSessionReady = true
                }
            } catch (e: Exception) {
                imageCaptureRef.set(null)
                Log.w(TAG, "bindToLifecycle failed: ${e.message}")
            }
        }
        future.addListener(listener, mainExecutor)
        onDispose {
            runCatching { boundCameraRef.getAndSet(null)?.cameraControl?.enableTorch(false) }
            imageCaptureRef.set(null)
            runCatching { processCameraRef.getAndSet(null)?.unbindAll() }
            cameraExecutor.shutdown()
        }
    }
    LaunchedEffect(torchEnabled, hasFlashUnit, cameraSessionReady) {
        if (!cameraSessionReady || !hasFlashUnit) return@LaunchedEffect
        val camera = boundCameraRef.get() ?: return@LaunchedEffect
        runCatching {
            camera.cameraControl.enableTorch(torchEnabled)
        }.onFailure { e ->
            Log.w(TAG, "enableTorch failed: ${e.message}")
        }
    }
    val captureActionEnabled = cameraSessionReady && !fullTicketImportLocked
    val isScanBusy = capturing || fullTicketImportLocked
    var previewInForeground by remember { mutableStateOf(false) }
    LifecycleResumeEffect(Unit) {
        previewInForeground = true
        onPauseOrDispose { previewInForeground = false }
    }
    val scanLineActive = cameraSessionReady && previewInForeground && !capturing && !fullTicketImportLocked
    androidx.compose.runtime.LaunchedEffect(isScanBusy) {
        onScanBusyChanged(isScanBusy)
    }
    BackHandler(enabled = isScanBusy) { }
    BackHandler(enabled = !isScanBusy, onBack = onBack)
    val view = LocalView.current
    DisposableEffect(view) {
        if (!view.isInEditMode) {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            val previousStatusBarColor = window.statusBarColor
            val previousLightStatusBarIcons = controller.isAppearanceLightStatusBars
            window.statusBarColor = Color.Transparent.toArgb()
            controller.isAppearanceLightStatusBars = false
            onDispose {
                window.statusBarColor = previousStatusBarColor
                controller.isAppearanceLightStatusBars = previousLightStatusBarIcons
            }
        } else {
            onDispose { }
        }
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = frameScale.value
                    scaleY = frameScale.value
                    transformOrigin = TransformOrigin.Center
                }
        ) {
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize()
            )
            BingoCameraQrViewfinder(scanLineActive = scanLineActive)
        }
        BingoCameraImportTopHeader(
            modifier = Modifier.align(Alignment.TopCenter),
            onBack = onBack,
            navigationBlocked = isScanBusy,
            hasFlashUnit = hasFlashUnit,
            torchEnabled = torchEnabled,
            onTorchToggle = { torchEnabled = !torchEnabled },
            flashControlEnabled = captureActionEnabled && !capturing && !isScanBusy,
        )
        val navigationBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.34f),
                            Color.Black.copy(alpha = 0.5f),
                        ),
                    ),
                )
                .padding(horizontal = Dimens.screenHorizontalPadding)
                .padding(top = Dimens.spacing12, bottom = Dimens.spacing12 + navigationBottom),
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Dimens.radiusLarge),
                color = Color.Black.copy(alpha = 0.46f),
                border = BorderStroke(
                    width = Dimens.cardBorderDefault,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                ),
                shadowElevation = 0.dp,
                tonalElevation = 0.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.spacing16, vertical = Dimens.spacing12),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Dimens.spacing4),
                    ) {
                        Text(
                            text = stringResource(R.string.camera_qr_scan_hint),
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = stringResource(R.string.camera_capture_full_ticket_hint),
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.78f),
                            textAlign = TextAlign.Center,
                        )
                    }
                    Spacer(Modifier.height(Dimens.spacing16))
                    val buttonInteraction = remember { MutableInteractionSource() }
                    val buttonPressed by buttonInteraction.collectIsPressedAsState()
                    Button(
                    onClick = {
                        if (handled.get() || fullTicketImportLocked || !captureActionEnabled) return@Button
                        if (capturing) return@Button
                        val ic = imageCaptureRef.get() ?: return@Button
                        val outFile = runCatching {
                            File.createTempFile("live_full_ticket_capture_", ".jpg", context.cacheDir)
                        }.getOrNull()
                        if (outFile == null) {
                            Log.w(TAG, "capture temp file failed, fallback GMS")
                            onScanFullTicketState.value()
                            return@Button
                        }
                        capturing = true
                        onScanBusyChanged(true)
                        scope.launch {
                            coroutineScope {
                                launch {
                                    shutterAlpha.snapTo(0f)
                                    shutterAlpha.animateTo(
                                        0.8f,
                                        tween(SHUTTER_UP_MS, easing = FastOutSlowInEasing),
                                    )
                                    shutterAlpha.animateTo(0f, tween(SHUTTER_DOWN_MS, easing = LinearEasing))
                                }
                                launch {
                                    frameScale.snapTo(1f)
                                    frameScale.animateTo(
                                        FRAME_PULSE_MIN,
                                        tween(FRAME_PULSE_IN_MS, easing = FastOutSlowInEasing),
                                    )
                                    frameScale.animateTo(
                                        1f,
                                        tween(FRAME_PULSE_OUT_MS, easing = FastOutSlowInEasing),
                                    )
                                }
                            }
                        }
                        val options = ImageCapture.OutputFileOptions.Builder(outFile).build()
                        ic.takePicture(
                            options,
                            mainExecutor,
                            object : ImageCapture.OnImageSavedCallback {
                                override fun onImageSaved(
                                    result: ImageCapture.OutputFileResults,
                                ) {
                                    capturing = false
                                    if (!fullTicketImportLocked) {
                                        onScanBusyChanged(false)
                                    }
                                    val vw = previewView.width
                                    val vh = previewView.height
                                    val cropped = tryCropToViewfinderFrame(outFile, context, vw, vh)
                                    val fileToUse = cropped ?: outFile.also {
                                        Log.w(
                                            CROP_LOG,
                                            "using full capture (crop null); preview ${vw}x$vh"
                                        )
                                    }
                                    if (cropped != null) {
                                        outFile.delete()
                                    }
                                    val u = runCatching {
                                        FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            fileToUse,
                                        )
                                    }.getOrNull()
                                    if (u == null) {
                                        fileToUse.delete()
                                        onScanFullTicketState.value()
                                        return
                                    }
                                    onFullTicketPhotoCapturedState.value(u)
                                }
                                override fun onError(
                                    e: ImageCaptureException,
                                ) {
                                    capturing = false
                                    if (!fullTicketImportLocked) {
                                        onScanBusyChanged(false)
                                    }
                                    outFile.delete()
                                    Log.w(TAG, "takePicture failed, fallback GMS: ${e.message}")
                                    onScanFullTicketState.value()
                                }
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimens.buttonHeight)
                        .graphicsLayer {
                            val s = if (buttonPressed) 0.96f else 1f
                            scaleX = s
                            scaleY = s
                        },
                    interactionSource = buttonInteraction,
                    shape = RoundedCornerShape(Dimens.radiusCard),
                    contentPadding = PaddingValues(horizontal = Dimens.spacing12, vertical = 0.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp,
                        disabledElevation = 0.dp,
                    ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    enabled = captureActionEnabled && !capturing,
                    ) {
                        Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        lerp(MaterialTheme.colorScheme.primary, Color.Black, 0.24f),
                                    ),
                                ),
                                shape = RoundedCornerShape(Dimens.radiusCard),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Dimens.spacing8),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                text = if (capturing) {
                                    stringResource(R.string.camera_capturing)
                                } else {
                                    stringResource(R.string.camera_scan_ticket)
                                },
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        }
                    }
                }
            }
        }
        if (shutterAlpha.value > 0.001f) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Color.White.copy(alpha = shutterAlpha.value.coerceIn(0f, 1f))
                    )
            )
        }
    }
}

/** Camera-surface header: back, title, and flash sit on a short scrim. */
@Composable
private fun BingoCameraImportTopHeader(
    onBack: () -> Unit,
    navigationBlocked: Boolean,
    hasFlashUnit: Boolean,
    torchEnabled: Boolean,
    onTorchToggle: () -> Unit,
    flashControlEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.55f),
                        Color.Transparent,
                    ),
                ),
            )
            .statusBarsPadding()
            .padding(horizontal = Dimens.screenHorizontalPadding)
            .height(Dimens.buttonHeight),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onBack,
            enabled = !navigationBlocked,
            modifier = Modifier.requiredSize(Dimens.buttonHeight),
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                stringResource(R.string.common_back),
                tint = Color.White,
            )
        }
        Text(
            text = stringResource(R.string.camera_qr_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = Dimens.spacing8),
        )
        if (hasFlashUnit) {
            Surface(
                modifier = Modifier.requiredSize(Dimens.buttonHeight),
                shape = CircleShape,
                color = if (torchEnabled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    Color.White.copy(alpha = 0.16f)
                },
                border = if (torchEnabled) {
                    null
                } else {
                    BorderStroke(Dimens.cardBorderDefault, Color.White.copy(alpha = 0.45f))
                },
                shadowElevation = 0.dp,
            ) {
                IconButton(
                    onClick = onTorchToggle,
                    enabled = flashControlEnabled,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Icon(
                        imageVector = if (torchEnabled) Icons.Outlined.FlashOn else Icons.Outlined.FlashOff,
                        contentDescription = stringResource(
                            if (torchEnabled) R.string.camera_flash_on_cd else R.string.camera_flash_off_cd,
                        ),
                        tint = if (torchEnabled) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            Color.White
                        },
                    )
                }
            }
        }
    }
}
