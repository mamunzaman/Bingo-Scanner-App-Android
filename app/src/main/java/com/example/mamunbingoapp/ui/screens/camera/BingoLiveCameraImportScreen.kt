package com.example.mamunbingoapp.ui.screens.camera

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.RectF
import android.media.ExifInterface
import android.net.Uri
import android.util.Log
import kotlin.math.max
import kotlin.math.roundToInt
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
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
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import kotlinx.coroutines.coroutineScope
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
import androidx.compose.ui.graphics.TransformOrigin
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
import androidx.core.content.FileProvider
import com.example.mamunbingoapp.scanner.ImportTicketQrPreOcr
import com.example.mamunbingoapp.scanner.tryDecodeBingoQrFromInputImage
import com.example.mamunbingoapp.theme.Dimens
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
private const val LIVE_NO_BINGO_LOG_MS = 2000L
private const val MIN_FRAME_INTERVAL_MS = 350L
private const val SHUTTER_UP_MS = 60
private const val SHUTTER_DOWN_MS = 60
private const val FRAME_PULSE_IN_MS = 80
private const val FRAME_PULSE_OUT_MS = 100
private const val FRAME_PULSE_MIN = 0.97f

/** Must match [BingoCameraQrViewfinder] (green window). */
private const val VIEWFINDER_FRAME_SIZE_MIN_SIDE = 0.64f
private const val VIEWFINDER_TOP_FRACTION = 0.26f

private const val DECODE_MAX_DIM = 4096

private fun computeViewfinderFrameRectF(viewW: Float, viewH: Float): RectF {
    val minS = minOf(viewW, viewH)
    val fw = minS * VIEWFINDER_FRAME_SIZE_MIN_SIDE
    val left = (viewW - fw) * 0.5f
    val top = viewH * VIEWFINDER_TOP_FRACTION
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
            Log.d(
                CROP_LOG,
                "cropped view=${previewW}x${previewH} rect=$cropR in=${sourceJpeg.length()}B out=${out.length()}B"
            )
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

@Composable
private fun BingoCameraQrViewfinder() {
    val labelColor = Color.White.copy(alpha = 0.95f)
    val borderColor = MaterialTheme.colorScheme.primary
    val density = LocalDensity.current
    val corner = Dimens.radiusCard
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val minSide = minOf(maxWidth, maxHeight)
        val frameSize = minSide * VIEWFINDER_FRAME_SIZE_MIN_SIDE
        val wPx = with(density) { maxWidth.toPx() }
        val hPx = with(density) { maxHeight.toPx() }
        val frameWpx = with(density) { frameSize.toPx() }
        val frameHpx = frameWpx
        val left = (wPx - frameWpx) / 2f
        val top = hPx * VIEWFINDER_TOP_FRACTION
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
    onFullTicketPhotoCaptured: (uri: Uri) -> Unit,
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
    val imageCaptureRef = remember { AtomicReference<ImageCapture?>(null) }
    val previewView = remember { PreviewView(context) }
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
                Log.d(TAG, "ProcessCameraProvider.get() failed: ${e.message}")
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
                                    fullTicketImportLocked = true
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
                        imageCapture,
                    )
                    cameraSessionReady = true
                }
            } catch (e: Exception) {
                imageCaptureRef.set(null)
                Log.d(TAG, "bindToLifecycle failed: ${e.message}")
            }
        }
        future.addListener(listener, mainExecutor)
        onDispose {
            imageCaptureRef.set(null)
            runCatching { processCameraRef.getAndSet(null)?.unbindAll() }
            cameraExecutor.shutdown()
        }
    }
    val captureActionEnabled = cameraSessionReady && !fullTicketImportLocked
    BackHandler(enabled = !capturing, onBack = onBack)
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
            BingoCameraQrViewfinder()
        }
        TopAppBar(
            title = { Text("Bingo ticket QR", style = MaterialTheme.typography.titleLarge) },
            navigationIcon = {
                IconButton(onClick = onBack, enabled = !capturing) {
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
                    text = "Or scan your ticket.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(Dimens.spacing16))
                AppPrimaryButton(
                    text = if (capturing) "Capturing" else "Scan ticket",
                    enabled = captureActionEnabled,
                    loading = capturing,
                    onClick = {
                        if (handled.get() || fullTicketImportLocked || !captureActionEnabled) return@AppPrimaryButton
                        if (capturing) return@AppPrimaryButton
                        val ic = imageCaptureRef.get() ?: return@AppPrimaryButton
                        val outFile = runCatching {
                            File.createTempFile("live_full_ticket_capture_", ".jpg", context.cacheDir)
                        }.getOrNull()
                        if (outFile == null) {
                            Log.d(TAG, "capture temp file failed, fallback GMS")
                            onScanFullTicketState.value()
                            return@AppPrimaryButton
                        }
                        capturing = true
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
                                    outFile.delete()
                                    Log.d(TAG, "takePicture failed, fallback GMS: ${e.message}")
                                    onScanFullTicketState.value()
                                }
                            }
                        )
                    },
                )
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
