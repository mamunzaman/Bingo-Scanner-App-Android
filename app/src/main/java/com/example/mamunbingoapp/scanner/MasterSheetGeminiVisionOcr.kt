package com.example.mamunbingoapp.scanner

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.util.Log
import com.example.mamunbingoapp.BuildConfig
import dev.shreyaspatil.ai.client.generativeai.GenerativeModel
import dev.shreyaspatil.ai.client.generativeai.type.PlatformImage
import dev.shreyaspatil.ai.client.generativeai.type.content
import dev.shreyaspatil.ai.client.generativeai.type.generationConfig
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Gemini Vision extraction for [com.example.mamunbingoapp.domain.model.BingoScanType.MAIN_SHEET] only.
 * Requires [BuildConfig.GEMINI_API_KEY] from gitignored `local.properties`.
 */
object MasterSheetGeminiVisionOcr {

    private const val TAG = "MainSheetAiOcr"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val REQUEST_TIMEOUT_MS = 45_000L
    private const val MAX_IMAGE_SIDE = 1600

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val prompt = """
Return ONLY a single JSON object (no markdown, no prose) for this German Master Bingo sheet.

Fields:
- "serie": Seriennummer digits as string (typically 4-6 digits)
- "losNumber": Los-Nummer digits as string (typically 5 digits)
- "grid": exactly 25 integers, row-major top-left to bottom-right for the 5x5 bingo table (B=1-15, I=16-30, N=31-45, G=46-60, O=61-75; center free cell may be 0)
- "confidence": number from 0.0 to 1.0 for overall extraction quality

Example shape:
{"serie":"1548","losNumber":"22750","grid":[1,16,31,46,61,...],"confidence":0.95}
""".trimIndent()

    @Serializable
    private data class GeminiMasterSheetJson(
        val serie: String? = null,
        val losNumber: String? = null,
        val grid: List<Int>? = null,
        val confidence: Double? = null,
    )

    fun isConfigured(): Boolean = BuildConfig.GEMINI_API_KEY.isNotBlank()

    fun hasInternet(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    data class AiVisionResult(
        val outcome: HistoryImportOcrOutcome,
        val confidence: Float?,
    )

    suspend fun analyzeUri(context: Context, uri: Uri): AiVisionResult? =
        withTimeout(REQUEST_TIMEOUT_MS) {
            if (!isConfigured()) return@withTimeout null
            val bitmap = loadBitmapDownsampled(context, uri, MAX_IMAGE_SIDE)
                ?: return@withTimeout null
            try {
                val model = GenerativeModel(
                    modelName = MODEL_NAME,
                    apiKey = BuildConfig.GEMINI_API_KEY,
                    generationConfig = generationConfig {
                        responseMimeType = "application/json"
                    },
                )
                val imageBytes = bitmapToJpegBytes(bitmap)
                val response = model.generateContent(
                    content {
                        image(PlatformImage(imageBytes))
                        text(prompt)
                    },
                )
                val rawText = response.text?.trim().orEmpty()
                if (rawText.isEmpty()) return@withTimeout null
                parseResponse(rawText)
            } finally {
                if (!bitmap.isRecycled) bitmap.recycle()
            }
        }

    internal fun parseResponse(rawText: String): AiVisionResult? {
        val jsonBody = extractJsonObject(rawText) ?: return null
        val parsed = runCatching {
            json.decodeFromString<GeminiMasterSheetJson>(jsonBody)
        }.getOrElse {
            Log.w(TAG, "AI JSON parse failed: ${it.message}")
            return null
        }
        val grid = parsed.grid?.take(25)?.let { list ->
            if (list.size < 25) list + List(25 - list.size) { 0 } else list
        } ?: return null
        val serie = parsed.serie?.filter { it.isDigit() }?.takeIf { it.isNotEmpty() }
        val los = parsed.losNumber?.filter { it.isDigit() }?.takeIf { it.isNotEmpty() }
        if (serie == null || los == null) return null
        val confidence = parsed.confidence?.toFloat()?.coerceIn(0f, 1f)
        Log.d(TAG, "AI parsed confidence=$confidence serie=$serie los=$los gridFilled=${grid.count { it != 0 }}")
        return AiVisionResult(
            outcome = HistoryImportOcrOutcome(
                numbersRowMajor = grid,
                losNumber = los,
                serialNumber = serie,
            ),
            confidence = confidence,
        )
    }

    private fun extractJsonObject(raw: String): String? {
        val fenced = Regex("""```(?:json)?\s*([\s\S]*?)```""", RegexOption.IGNORE_CASE)
            .find(raw)?.groupValues?.getOrNull(1)?.trim()
        if (!fenced.isNullOrBlank()) return fenced
        val start = raw.indexOf('{')
        val end = raw.lastIndexOf('}')
        if (start >= 0 && end > start) return raw.substring(start, end + 1)
        return null
    }

    private fun bitmapToJpegBytes(bitmap: Bitmap, quality: Int = 85): ByteArray {
        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
        return out.toByteArray()
    }

    private fun loadBitmapDownsampled(context: Context, uri: Uri, maxSide: Int): Bitmap? {
        val cr = context.contentResolver
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        cr.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / sample > maxSide) sample *= 2
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        return cr.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
    }
}
