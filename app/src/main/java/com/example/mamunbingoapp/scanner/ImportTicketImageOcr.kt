package com.example.mamunbingoapp.scanner

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

data class HistoryImportOcrOutcome(
    val numbersRowMajor: List<Int>,
    val losNumber: String? = null,
    val serialNumber: String? = null,
)

object ImportTicketImageOcr {

    /** Below this many distinct 1–75 values from ML Kit elements: weak-parser path + optional second OCR crop. */
    private const val ML_KIT_WEAK_DISTINCT_THRESHOLD = 20

    /** Strict grid: min cells whose value matches B/I/N/G/O column; max duplicate / wrong-column slack. */
    private const val STRICT_MIN_COLUMN_VALID_TOTAL = 22
    private const val STRICT_MIN_PER_COLUMN = 4
    private const val STRICT_MAX_DUPLICATE_EXCESS = 2
    private const val STRICT_MAX_OUT_OF_COLUMN = 2

    private val STRICT_COLUMN_RANGES = listOf(1..15, 16..30, 31..45, 46..60, 61..75)

    /** Fixed 5×5 cell OCR: if fewer non-empty cells than this, discard and use whole-grid pipeline only. */
    private const val CELL_GRID_MIN_FILLED = 15

    /** Number grid area inside the ticket crop (after left strip), normalized fractions. */
    private const val GRID_NUMBER_AREA_LEFT_FRAC = 0.26f
    private const val GRID_NUMBER_AREA_RIGHT_FRAC = 0.99f
    private const val GRID_NUMBER_AREA_TOP_FRAC = 0.06f
    private const val GRID_NUMBER_AREA_BOTTOM_FRAC = 0.94f

    /** Left branding strip (fraction of full ticket width); grid OCR uses the region to the right. */
    private const val LEFT_STRIP_WIDTH_FRAC = 0.24f

    /** Graded consensus quality: Tier A strict; Tier B when display advantage + sanity caps. */
    private const val CONSENSUS_TIER_A_MIN_DISTINCT_DISPLAYED = 15
    private const val CONSENSUS_TIER_A_MAX_DUPLICATE_PENALTY = 3
    private const val CONSENSUS_TIER_B_MIN_DISTINCT_DISPLAYED = 14
    private const val CONSENSUS_TIER_B_MAX_DUPLICATE_PENALTY = 3
    private const val CONSENSUS_TIER_B_MAX_COLUMN_VIOLATIONS = 1
    private const val CONSENSUS_DISPLAY_ADVANTAGE_VS_RAW_ML = 4
    private const val CONSENSUS_EXTREME_DUPLICATE_PENALTY = 8
    private const val CONSENSUS_WORSE_THAN_RAW_DISTINCT_GAP = 2
    /** Hard reject consensus before tiers (distinct / duplicate / Bingo column ranges). */
    private const val CONSENSUS_SANITY_MIN_DISTINCT_DISPLAYED = 14
    private const val CONSENSUS_SANITY_MAX_DUPLICATE_PENALTY = 4
    private const val CONSENSUS_SANITY_MAX_COLUMN_VIOLATIONS = 2

    /**
     * Gallery apply: pass [bypassInternalGridCrop] true to skip [gridCropForNumberRegion].
     * [preCropCameraForStripOcr]: when true, runs [BingoNumberAnalyzer.tryPaddedBingoGridCropForCameraOcr] on the
     * full bitmap and feeds the cropped (or on failure, original) image to the same [analyzeBitmap] path — use only
     * for in-app **CameraX still** URIs, not gallery.
     */
    fun analyzeUri(
        context: Context,
        uri: Uri,
        bypassInternalGridCrop: Boolean = false,
        preCropCameraForStripOcr: Boolean = false,
    ): HistoryImportOcrOutcome {
        val bitmap = loadBitmapDownsampled(context, uri, maxSide = 1600)
            ?: error("Could not load image")
        if (preCropCameraForStripOcr && !bypassInternalGridCrop) {
            val pre = BingoNumberAnalyzer.tryPaddedBingoGridCropForCameraOcr(bitmap)
            val toAnalyze = pre ?: bitmap
            if (pre != null && pre !== bitmap) {
                if (!bitmap.isRecycled) bitmap.recycle()
            }
            return analyzeBitmap(context.applicationContext, toAnalyze, bypassInternalGridCrop)
        }
        return analyzeBitmap(context.applicationContext, bitmap, bypassInternalGridCrop)
    }

    private data class PipelineResult(
        val outcome: HistoryImportOcrOutcome,
        val distinctValidCount: Int,
        val raw: String,
    )

    private data class CellGridResult(val rowMajor: List<Int>, val distinctCount: Int)

    private data class ScreenshotBezelResult(
        /** Bitmap to feed into [gridCropForNumberRegion]; may be [original] or a new cropped copy. */
        val bitmap: Bitmap,
        val applied: Boolean,
    )

    private fun analyzeBitmap(
        appContext: Context,
        bitmap: Bitmap,
        bypassInternalGridCrop: Boolean = false,
    ): HistoryImportOcrOutcome {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val bezel = tryScreenshotBezelCrop(bitmap)
        val preCropApplied = bezel.applied
        if (preCropApplied && bezel.bitmap !== bitmap && !bitmap.isRecycled) {
            bitmap.recycle()
        }
        val rootForPipeline = bezel.bitmap
        val pass1 =
            if (bypassInternalGridCrop) {
                rootForPipeline
            } else {
                gridCropForNumberRegion(rootForPipeline)
            }
        var pass2: Bitmap? = null
        var highlightGridBitmap: Bitmap? = null
        return try {
            val r1Normal = runPipeline(pass1, recognizer)
            val hiRes = tryHighlightSuppressionInNumberGrid(pass1)
            val r1Sup: PipelineResult? =
                if (hiRes.maskApplied && hiRes.bitmap !== pass1) {
                    runCatching { runPipeline(hiRes.bitmap, recognizer) }.getOrNull()
                } else {
                    null
                }
            val useSuppressedRoot =
                r1Sup != null && compareHighlightGridPipelines(r1Sup, r1Normal) > 0
            if (hiRes.maskApplied && hiRes.bitmap !== pass1 && !useSuppressedRoot) {
                hiRes.bitmap.recycle()
            } else if (hiRes.maskApplied && hiRes.bitmap !== pass1 && useSuppressedRoot) {
                highlightGridBitmap = hiRes.bitmap
            }
            val gridRoot = if (useSuppressedRoot) hiRes.bitmap else pass1
            val r1Chosen = if (useSuppressedRoot) r1Sup!! else r1Normal
            val tighter = tighterCenterZoomGridCrop(gridRoot)
            val r2: PipelineResult? = if (tighter != null) {
                pass2 = tighter
                runCatching { runPipeline(tighter, recognizer) }.getOrNull()
            } else {
                null
            }

            val bestCrop: Bitmap = if (r2 != null && compareStageCandidates(r2, r1Chosen) > 0) {
                pass2!!
            } else {
                gridRoot
            }

            var deskewBest: PipelineResult? = null
            for (deg in DESKEW_DEGREES) {
                val rotated = rotateBitmapDeskew(bestCrop, deg) ?: continue
                try {
                    val rp = runCatching { runPipeline(rotated, recognizer) }.getOrNull()
                    if (rp != null) {
                        if (deskewBest == null || compareStageCandidates(rp, deskewBest) > 0) {
                            deskewBest = rp
                        }
                    }
                } finally {
                    if (!rotated.isRecycled) rotated.recycle()
                }
            }

            var gateBest = maxStageResult(r1Chosen, r2, deskewBest) ?: r1Chosen
            var perspectiveRp: PipelineResult? = null
            if (gateBest.distinctValidCount < ML_KIT_WEAK_DISTINCT_THRESHOLD) {
                val warped = tryWarpPerspectiveGrid(bestCrop, recognizer)
                if (warped != null) {
                    try {
                        val rp = runCatching { runPipeline(warped, recognizer) }.getOrNull()
                        if (rp != null) {
                            perspectiveRp = rp
                        }
                    } finally {
                        if (!warped.isRecycled) warped.recycle()
                    }
                }
            }
            if (perspectiveRp != null && compareStageCandidates(perspectiveRp, gateBest) > 0) {
                gateBest = perspectiveRp
            }

            var c1rp: PipelineResult? = null
            var c2rp: PipelineResult? = null
            if (gateBest.distinctValidCount < ML_KIT_WEAK_DISTINCT_THRESHOLD) {
                val c1 = toGrayscaleHighContrast(bestCrop, contrastScale = 1.38f, contrastTranslate = -18f)
                if (c1 != null) {
                    try {
                        val rp = runCatching { runPipeline(c1, recognizer) }.getOrNull()
                        if (rp != null) {
                            c1rp = rp
                        }
                    } finally {
                        if (!c1.isRecycled) c1.recycle()
                    }
                }
                val c2 = toGrayscaleHighContrast(bestCrop, contrastScale = 1.62f, contrastTranslate = -28f)
                if (c2 != null) {
                    try {
                        val rp = runCatching { runPipeline(c2, recognizer) }.getOrNull()
                        if (rp != null) {
                            c2rp = rp
                        }
                    } finally {
                        if (!c2.isRecycled) c2.recycle()
                    }
                }
            }

            val stageCandidates = mutableListOf<Pair<String, PipelineResult>>()
            stageCandidates.add("base" to r1Normal)
            r1Sup?.let { stageCandidates.add("baseHighlightSuppressed" to it) }
            r2?.let { stageCandidates.add("tighter" to it) }
            deskewBest?.let { stageCandidates.add("deskew" to it) }
            perspectiveRp?.let { stageCandidates.add("perspective" to it) }
            c1rp?.let { stageCandidates.add("contrastVariant1" to it) }
            c2rp?.let { stageCandidates.add("contrastVariant2" to it) }

            val consensusRow = buildConsensusRowMajor(stageCandidates)
            val consensusOutcome = HistoryImportOcrOutcome(numbersRowMajor = pad25(consensusRow))
            val consensusPr = PipelineResult(
                consensusOutcome,
                distinctValidCount = strictValidColumnCellCount(consensusRow),
                raw = "",
            )
            val (rankedRawPick, _) = rankRawStagesForFallback(stageCandidates)
            val bestRawStagePair = rankedRawPick.stage to rankedRawPick.pr
            val consensusSanity = gridSanityMetrics(consensusPr.outcome.numbersRowMajor)
            val sanityGate = consensusSanityGate(consensusSanity)
            val (consensusQualityOk, consensusQualityReason) = consensusPassesQualityBar(
                consensusPr,
                bestRawStagePair.second,
                consensusSanity,
                sanityGate = sanityGate,
            )
            val consensusAllowed = consensusQualityOk && (
                consensusQualityReason == "tier_b_display_advantage" ||
                    (
                        consensusPr.distinctValidCount >= bestRawStagePair.second.distinctValidCount &&
                            compareStrongestStagePrimary(consensusPr, bestRawStagePair.second) >= 0
                        )
                )
            val chosenPrRaw =
                if (consensusAllowed) {
                    pickConsensusOrBestStage(
                        consensusPr,
                        stageCandidates,
                        includeConsensus = true,
                    ).second
                } else {
                    bestRawStagePair.second
                }
            var strongestStageOnly = stageCandidates.maxWithOrNull { a, b ->
                compareStrongestStagePrimary(a.second, b.second)
            }
            if (!consensusAllowed) {
                strongestStageOnly = bestRawStagePair
            }
            val guardApplied = strongestStageOnly != null &&
                strongestStageOnly.second.distinctValidCount >= chosenPrRaw.distinctValidCount + 4
            var chosenPr: PipelineResult = if (guardApplied) {
                strongestStageOnly!!.second
            } else {
                chosenPrRaw
            }

            var chosenOutcome = chosenPr.outcome

            if (chosenPr.distinctValidCount < ML_KIT_WEAK_DISTINCT_THRESHOLD) {
                val layoutOutcome = tryWeakPathBingoHeaderLayout(pass1, recognizer)
                if (layoutOutcome != null) {
                    val headerPr = PipelineResult(
                        layoutOutcome,
                        distinctValidCount = strictValidColumnCellCount(layoutOutcome.numbersRowMajor),
                        raw = "",
                    )
                    if (compareStageCandidates(headerPr, chosenPr) > 0) {
                        chosenOutcome = layoutOutcome
                        val weakBase = layoutOutcome.copy(losNumber = null, serialNumber = null)
                        val (weakDeduped, _) = dedupeDuplicateBingoValuesRowMajor(pad25(weakBase.numbersRowMajor))
                        val weakOut = weakBase.copy(numbersRowMajor = weakDeduped)
                        return attachLeftStripMeta(weakOut, rootForPipeline)
                    }
                }
            }

            val outBase = chosenOutcome.copy(losNumber = null, serialNumber = null)
            val (dedupedRm, _) = dedupeDuplicateBingoValuesRowMajor(pad25(outBase.numbersRowMajor))
            attachLeftStripMeta(outBase.copy(numbersRowMajor = dedupedRm), rootForPipeline)
        } finally {
            recognizer.close()
            pass2?.let { p2 ->
                if (preCropApplied) {
                    if (p2 !== pass1 && p2 !== rootForPipeline && !p2.isRecycled) p2.recycle()
                } else {
                    if (p2 !== pass1 && p2 !== bitmap && !p2.isRecycled) p2.recycle()
                }
            }
            if (preCropApplied) {
                if (pass1 !== rootForPipeline && !pass1.isRecycled) pass1.recycle()
                if (rootForPipeline !== pass1 && !rootForPipeline.isRecycled) rootForPipeline.recycle()
            } else {
                if (pass1 !== bitmap && !pass1.isRecycled) pass1.recycle()
            }
            highlightGridBitmap?.let { hb ->
                if (!hb.isRecycled && hb !== pass1 && hb !== pass2 && hb !== rootForPipeline && hb !== bitmap) {
                    hb.recycle()
                }
            }
        }
    }

    private data class HighlightGridResult(
        /** Same as [src] when no mask; otherwise a mutable copy with grid-region highlights cleared. */
        val bitmap: Bitmap,
        val maskApplied: Boolean,
        val suppressedPixelPct: Float,
    )

    /** Yellow/orange high-saturation strokes in the number grid only (not the left LOS/serial strip). */
    private fun tryHighlightSuppressionInNumberGrid(src: Bitmap): HighlightGridResult {
        val w = src.width
        val h = src.height
        if (w < 32 || h < 32) return HighlightGridResult(src, false, 0f)
        val gx0 = (w * GRID_NUMBER_AREA_LEFT_FRAC).toInt().coerceIn(0, w - 2)
        val gx1 = (w * GRID_NUMBER_AREA_RIGHT_FRAC).toInt().coerceIn(gx0 + 4, w)
        val gy0 = (h * GRID_NUMBER_AREA_TOP_FRAC).toInt().coerceIn(0, h - 2)
        val gy1 = (h * GRID_NUMBER_AREA_BOTTOM_FRAC).toInt().coerceIn(gy0 + 4, h)
        val rw = gx1 - gx0
        val rh = gy1 - gy0
        if (rw < 8 || rh < 8) return HighlightGridResult(src, false, 0f)
        val regionPixels = rw * rh
        var suppressed = 0
        val out = src.copy(Bitmap.Config.ARGB_8888, true)
        for (y in gy0 until gy1) {
            for (x in gx0 until gx1) {
                val p = out.getPixel(x, y)
                val r = (p ushr 16) and 0xff
                val g = (p ushr 8) and 0xff
                val b = p and 0xff
                if (isLikelyYellowOrangeHighlight(r, g, b)) {
                    out.setPixel(x, y, (0xff shl 24) or (0xf9 shl 16) or (0xf9 shl 8) or 0xf9)
                    suppressed++
                }
            }
        }
        if (suppressed == 0) {
            if (out !== src) out.recycle()
            return HighlightGridResult(src, false, 0f)
        }
        val pct = 100f * suppressed / regionPixels.coerceAtLeast(1)
        return HighlightGridResult(out, true, pct)
    }

    private fun isLikelyYellowOrangeHighlight(r: Int, g: Int, b: Int): Boolean {
        if (r < 110 || g < 85) return false
        val maxc = maxOf(r, g, b)
        val minc = minOf(r, g, b)
        val sat = if (maxc == 0) 0f else (maxc - minc).toFloat() / maxc
        if (sat < 0.26f) return false
        if (b > minOf(r, g) + 52) return false
        if (r + g < b * 2 + 95) return false
        return true
    }

    /** Tie-break normal vs highlight-suppressed grid OCR: validCells, strict valid25, distinct. */
    private fun compareHighlightGridPipelines(a: PipelineResult, b: PipelineResult): Int {
        val va = strictValidColumnCellCount(a.outcome.numbersRowMajor)
        val vb = strictValidColumnCellCount(b.outcome.numbersRowMajor)
        if (va != vb) return va.compareTo(vb)
        val ca = isStrictValidRowMajorGrid(a.outcome.numbersRowMajor)
        val cb = isStrictValidRowMajorGrid(b.outcome.numbersRowMajor)
        when {
            ca && !cb -> return 1
            !ca && cb -> return -1
        }
        return a.distinctValidCount.compareTo(b.distinctValidCount)
    }

    /**
     * Phone-screen photos: large black bezel around a bright ticket. Inward scan finds content bounds;
     * only applies when edges are dark and the crop removes a meaningful amount of area.
     */
    private fun tryScreenshotBezelCrop(original: Bitmap): ScreenshotBezelResult {
        val w0 = original.width
        val h0 = original.height
        if (w0 < 96 || h0 < 96) {
            return ScreenshotBezelResult(original, false)
        }
        val stepX = max(1, w0 / 160)
        val stepY = max(1, h0 / 160)
        fun lum(p: Int): Float {
            val r = (p ushr 16) and 0xff
            val g = (p ushr 8) and 0xff
            val b = p and 0xff
            return 0.299f * r + 0.587f * g + 0.114f * b
        }
        fun rowMeanL(y: Int): Float {
            var s = 0f
            var n = 0
            var x = 0
            while (x < w0) {
                s += lum(original.getPixel(x.coerceIn(0, w0 - 1), y.coerceIn(0, h0 - 1)))
                n++
                x += stepX
            }
            return if (n > 0) s / n else 0f
        }
        fun colMeanL(x: Int): Float {
            var s = 0f
            var n = 0
            var y = 0
            while (y < h0) {
                s += lum(original.getPixel(x.coerceIn(0, w0 - 1), y.coerceIn(0, h0 - 1)))
                n++
                y += stepY
            }
            return if (n > 0) s / n else 0f
        }
        val edgeW = max(2, w0 / 35)
        val edgeH = max(2, h0 / 35)
        var outerDarkSum = 0f
        var outerN = 0
        for (x in 0 until edgeW) {
            outerDarkSum += colMeanL(x)
            outerN++
            outerDarkSum += colMeanL(w0 - 1 - x)
            outerN++
        }
        for (y in 0 until edgeH) {
            outerDarkSum += rowMeanL(y)
            outerN++
            outerDarkSum += rowMeanL(h0 - 1 - y)
            outerN++
        }
        val outerMean = if (outerN > 0) outerDarkSum / outerN else 128f
        if (outerMean > 52f) {
            return ScreenshotBezelResult(original, false)
        }
        val contentEnter = 48f
        var left = 0
        while (left < w0 * 0.48f) {
            if (colMeanL(left) >= contentEnter) break
            left += stepX
        }
        var right = w0 - 1
        while (right > w0 * 0.52f) {
            if (colMeanL(right) >= contentEnter) break
            right -= stepX
        }
        var top = 0
        while (top < h0 * 0.48f) {
            if (rowMeanL(top) >= contentEnter) break
            top += stepY
        }
        var bottom = h0 - 1
        while (bottom > h0 * 0.52f) {
            if (rowMeanL(bottom) >= contentEnter) break
            bottom -= stepY
        }
        left = left.coerceIn(0, w0 - 2)
        right = right.coerceIn(left + 1, w0 - 1)
        top = top.coerceIn(0, h0 - 2)
        bottom = bottom.coerceIn(top + 1, h0 - 1)
        val cw = right - left + 1
        val ch = bottom - top + 1
        val innerFrac = (cw * ch).toFloat() / (w0 * h0).toFloat()
        val trimL = left.toFloat() / w0
        val trimR = (w0 - 1 - right).toFloat() / w0
        val trimT = top.toFloat() / h0
        val trimB = (h0 - 1 - bottom).toFloat() / h0
        val maxTrim = maxOf(trimL, trimR, trimT, trimB)
        val minSide = min(cw, ch)
        if (innerFrac > 0.88f || maxTrim < 0.055f || minSide < 180 || cw < 120 || ch < 120) {
            return ScreenshotBezelResult(original, false)
        }
        return try {
            ScreenshotBezelResult(Bitmap.createBitmap(original, left, top, cw, ch), true)
        } catch (_: Exception) {
            ScreenshotBezelResult(original, false)
        }
    }

    /** Small rotations tried when crop passes are still weak (distinct ML Kit values &lt; threshold). */
    private val DESKEW_DEGREES = floatArrayOf(-3f, 3f, -5f, 5f)

    /** Grayscale + linear contrast (glare / soft screen photos). Caller recycles the result. */
    private fun toGrayscaleHighContrast(src: Bitmap, contrastScale: Float, contrastTranslate: Float): Bitmap? {
        if (src.width < 8 || src.height < 8) return null
        return try {
            val out = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(out)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
            val gray = ColorMatrix().apply { setSaturation(0f) }
            val contrast = ColorMatrix(
                floatArrayOf(
                    contrastScale, 0f, 0f, 0f, contrastTranslate,
                    0f, contrastScale, 0f, 0f, contrastTranslate,
                    0f, 0f, contrastScale, 0f, contrastTranslate,
                    0f, 0f, 0f, 1f, 0f,
                ),
            )
            val combined = ColorMatrix()
            combined.set(gray)
            combined.postConcat(contrast)
            paint.colorFilter = ColorMatrixColorFilter(combined)
            canvas.drawBitmap(src, 0f, 0f, paint)
            out
        } catch (_: Exception) {
            null
        }
    }

    /** Returns &gt; 0 if [a] is better than [b] (grid score, then distinct count). */
    private fun comparePipelineResults(a: PipelineResult, b: PipelineResult): Int {
        val cmp = compareRowMajorCandidates(a.outcome.numbersRowMajor, b.outcome.numbersRowMajor)
        if (cmp != 0) return cmp
        return a.distinctValidCount.compareTo(b.distinctValidCount)
    }

    /** Final stage pick: strict valid25, then validCells, distinct, fewer dupes, fewer OOB. */
    private fun compareStageCandidates(a: PipelineResult, b: PipelineResult): Int {
        val va = isStrictValidRowMajorGrid(a.outcome.numbersRowMajor)
        val vb = isStrictValidRowMajorGrid(b.outcome.numbersRowMajor)
        when {
            va && !vb -> return 1
            !va && vb -> return -1
        }
        val ca = strictValidColumnCellCount(a.outcome.numbersRowMajor)
        val cb = strictValidColumnCellCount(b.outcome.numbersRowMajor)
        if (ca != cb) return ca.compareTo(cb)
        if (a.distinctValidCount != b.distinctValidCount) {
            return a.distinctValidCount.compareTo(b.distinctValidCount)
        }
        val da = rowMajorDuplicateCount(a.outcome.numbersRowMajor)
        val db = rowMajorDuplicateCount(b.outcome.numbersRowMajor)
        if (da != db) return db.compareTo(da)
        val oa = rowMajorOutOfColumnCount(a.outcome.numbersRowMajor)
        val ob = rowMajorOutOfColumnCount(b.outcome.numbersRowMajor)
        if (oa != ob) return ob.compareTo(oa)
        return 0
    }

    private fun maxStageResult(vararg options: PipelineResult?): PipelineResult? =
        options.filterNotNull().maxWithOrNull { a, b -> compareStageCandidates(a, b) }

    /** Per-cell mode over stages; votes only from non-zero values in [STRICT_COLUMN_RANGES] for that column. */
    private fun buildConsensusRowMajor(stages: List<Pair<String, PipelineResult>>): List<Int> {
        if (stages.isEmpty()) return List(25) { 0 }
        return (0..24).map { idx -> consensusCellValue(idx, stages) }
    }

    private fun consensusCellValue(cellIndex: Int, stages: List<Pair<String, PipelineResult>>): Int {
        val col = cellIndex % 5
        val range = STRICT_COLUMN_RANGES[col]
        val byValue = mutableMapOf<Int, MutableList<PipelineResult>>()
        for ((_, pr) in stages) {
            val v = pad25(pr.outcome.numbersRowMajor).getOrElse(cellIndex) { 0 }
            if (v == 0) continue
            if (v !in range) continue
            byValue.getOrPut(v) { mutableListOf() }.add(pr)
        }
        if (byValue.isEmpty()) return 0
        val maxVotes = byValue.values.maxOf { it.size }
        val tied = byValue.filter { it.value.size == maxVotes }.keys.toList()
        if (tied.size == 1) return tied.first()
        fun bestPrForValue(v: Int): PipelineResult =
            byValue[v]!!.maxWithOrNull { a, b -> compareStageCandidates(a, b) }!!
        return tied.reduce { acc, v ->
            if (compareStageCandidates(bestPrForValue(v), bestPrForValue(acc)) > 0) v else acc
        }
    }

    /**
     * Picks best grid by [compareStrongestStagePrimary]. When [includeConsensus] is false, weak consensus
     * cannot override stronger single-stage OCR (consensus row is still built upstream for diagnostics).
     */
    private fun pickConsensusOrBestStage(
        consensusPr: PipelineResult,
        stageCandidates: List<Pair<String, PipelineResult>>,
        includeConsensus: Boolean = true,
    ): Pair<String, PipelineResult> {
        val all = mutableListOf<Pair<String, PipelineResult>>()
        if (includeConsensus) all.add("consensus" to consensusPr)
        all.addAll(stageCandidates)
        val strictOk = all.filter { isStrictValidRowMajorGrid(it.second.outcome.numbersRowMajor) }
        val pool = if (strictOk.isNotEmpty()) strictOk else all
        val picked = pool.maxWithOrNull { a, b -> compareStrongestStagePrimary(a.second, b.second) }!!
        return picked.first to picked.second
    }

    /** 25 non-zero cells but not 25 distinct values in 1–75 (invalid full grid). */
    private fun isDeceptiveFullGrid(rowMajor: List<Int>): Boolean {
        val p = pad25(rowMajor)
        if (p.count { it != 0 } != 25) return false
        return p.filter { it in 1..75 }.distinct().size < 25
    }

    private fun distinctDisplayedCountGrid(rowMajor: List<Int>): Int =
        pad25(rowMajor).filter { it in 1..75 }.distinct().size

    private data class GridQualityMetrics(
        val displayed: Int,
        val distinctDisplayed: Int,
        val duplicatePenalty: Int,
    )

    private fun gridQualityMetrics(rowMajor: List<Int>): GridQualityMetrics {
        val p = pad25(rowMajor)
        val displayed = p.count { it != 0 }
        val distinctDisplayed = p.filter { it in 1..75 }.distinct().size
        return GridQualityMetrics(displayed, distinctDisplayed, displayed - distinctDisplayed)
    }

    private fun duplicatePenaltyGrid(rowMajor: List<Int>): Int =
        gridQualityMetrics(rowMajor).duplicatePenalty

    /** Bingo column range violations (non-zero cell not in B/I/N/G/O range for its column). */
    private data class GridSanityMetrics(
        val displayed: Int,
        val distinctDisplayed: Int,
        val duplicatePenalty: Int,
        val columnRangeViolations: Int,
    )

    private fun gridSanityMetrics(rowMajor: List<Int>): GridSanityMetrics {
        val g = gridQualityMetrics(rowMajor)
        return GridSanityMetrics(
            g.displayed,
            g.distinctDisplayed,
            g.duplicatePenalty,
            rowMajorOutOfColumnCount(rowMajor),
        )
    }

    private data class RawStageRank(
        val stage: String,
        val pr: PipelineResult,
        val displayedCount: Int,
        val distinctDisplayedCount: Int,
        val duplicatePenalty: Int,
        val columnRangeViolations: Int,
        val distinctValidCount: Int,
    )

    /** Raw fallback / best-raw reference: grid-quality sort; strong = distinct ≥10 or displayed ≥12. */
    private fun rankRawStagesForFallback(
        stageCandidates: List<Pair<String, PipelineResult>>,
    ): Pair<RawStageRank, Boolean> {
        val ranked = stageCandidates.map { (name, pr) ->
            val s = gridSanityMetrics(pr.outcome.numbersRowMajor)
            RawStageRank(
                stage = name,
                pr = pr,
                displayedCount = s.displayed,
                distinctDisplayedCount = s.distinctDisplayed,
                duplicatePenalty = s.duplicatePenalty,
                columnRangeViolations = s.columnRangeViolations,
                distinctValidCount = pr.distinctValidCount,
            )
        }.sortedWith(
            compareByDescending<RawStageRank> { it.distinctDisplayedCount }
                .thenByDescending { it.displayedCount }
                .thenBy { it.duplicatePenalty }
                .thenBy { it.columnRangeViolations }
                .thenByDescending { it.distinctValidCount },
        )
        val strong = ranked.filter {
            it.distinctDisplayedCount >= 10 || it.displayedCount >= 12
        }
        val pick = if (strong.isNotEmpty()) strong.first() else ranked.first()
        return pick to strong.isEmpty()
    }

    private fun consensusSanityGate(s: GridSanityMetrics): Pair<Boolean, String> =
        when {
            s.distinctDisplayed < CONSENSUS_SANITY_MIN_DISTINCT_DISPLAYED ->
                false to "sanity_low_distinct"
            s.duplicatePenalty >= CONSENSUS_SANITY_MAX_DUPLICATE_PENALTY ->
                false to "sanity_high_duplicate_penalty"
            s.columnRangeViolations >= CONSENSUS_SANITY_MAX_COLUMN_VIOLATIONS ->
                false to "sanity_column_violations"
            else -> true to "ok"
        }

    /**
     * After [consensusSanityGate]: Tier A strong grid; Tier B needs display advantage + caps on
     * distinct/duplicates/column violations.
     */
    private fun consensusPassesQualityBar(
        consensusPr: PipelineResult,
        bestRawPr: PipelineResult,
        consensusSanity: GridSanityMetrics,
        sanityGate: Pair<Boolean, String>,
    ): Pair<Boolean, String> {
        if (!sanityGate.first) return false to sanityGate.second
        val c = consensusSanity
        val b = gridQualityMetrics(bestRawPr.outcome.numbersRowMajor)
        val bestRawMlDistinct = bestRawPr.distinctValidCount

        val tierA =
            c.distinctDisplayed >= CONSENSUS_TIER_A_MIN_DISTINCT_DISPLAYED &&
                c.duplicatePenalty < CONSENSUS_TIER_A_MAX_DUPLICATE_PENALTY &&
                c.distinctDisplayed > b.distinctDisplayed - CONSENSUS_WORSE_THAN_RAW_DISTINCT_GAP &&
                c.columnRangeViolations <= CONSENSUS_TIER_B_MAX_COLUMN_VIOLATIONS
        if (tierA) return true to "tier_a_ok"

        val displayAdvantageVsMl =
            c.displayed >= bestRawMlDistinct + CONSENSUS_DISPLAY_ADVANTAGE_VS_RAW_ML
        val tierB =
            displayAdvantageVsMl &&
                c.distinctDisplayed >= CONSENSUS_TIER_B_MIN_DISTINCT_DISPLAYED &&
                c.duplicatePenalty <= CONSENSUS_TIER_B_MAX_DUPLICATE_PENALTY &&
                c.columnRangeViolations <= CONSENSUS_TIER_B_MAX_COLUMN_VIOLATIONS &&
                c.duplicatePenalty < CONSENSUS_EXTREME_DUPLICATE_PENALTY
        if (tierB) return true to "tier_b_display_advantage"

        return when {
            c.duplicatePenalty >= CONSENSUS_EXTREME_DUPLICATE_PENALTY ->
                false to "rejected_extreme_duplicates"
            c.distinctDisplayed < CONSENSUS_TIER_A_MIN_DISTINCT_DISPLAYED ->
                false to "low_distinct"
            c.duplicatePenalty >= CONSENSUS_TIER_A_MAX_DUPLICATE_PENALTY ->
                false to "high_duplicate_penalty"
            c.distinctDisplayed <= b.distinctDisplayed - CONSENSUS_WORSE_THAN_RAW_DISTINCT_GAP ->
                false to "worse_than_best_raw"
            else -> false to "rejected_tier_c"
        }
    }

    /**
     * Final grid strength: distinct displayed → duplicate penalty → column-range violations → ML
     * [PipelineResult.distinctValidCount] → valid25 / deceptive / duplicate count / strict column / OOB.
     */
    private fun compareStrongestStagePrimary(a: PipelineResult, b: PipelineResult): Int {
        val ra = a.outcome.numbersRowMajor
        val rb = b.outcome.numbersRowMajor
        val dda = distinctDisplayedCountGrid(ra)
        val ddb = distinctDisplayedCountGrid(rb)
        if (dda != ddb) return dda.compareTo(ddb)
        val penA = duplicatePenaltyGrid(ra)
        val penB = duplicatePenaltyGrid(rb)
        if (penA != penB) return penB.compareTo(penA)
        val oa = rowMajorOutOfColumnCount(ra)
        val ob = rowMajorOutOfColumnCount(rb)
        if (oa != ob) return ob.compareTo(oa)
        if (a.distinctValidCount != b.distinctValidCount) {
            return a.distinctValidCount.compareTo(b.distinctValidCount)
        }
        val va = isStrictValidRowMajorGrid(ra)
        val vb = isStrictValidRowMajorGrid(rb)
        when {
            va && !vb -> return 1
            !va && vb -> return -1
        }
        val fa = isDeceptiveFullGrid(ra)
        val fb = isDeceptiveFullGrid(rb)
        when {
            fa && !fb -> return -1
            !fa && fb -> return 1
        }
        val da = rowMajorDuplicateCount(ra)
        val db = rowMajorDuplicateCount(rb)
        if (da != db) return db.compareTo(da)
        val ca = strictValidColumnCellCount(ra)
        val cb = strictValidColumnCellCount(rb)
        if (ca != cb) return ca.compareTo(cb)
        return 0
    }

    /** &gt; 0 if [a] is better than [b] for final consensus/stage selection. */
    private fun compareFinalStageCandidates(a: PipelineResult, b: PipelineResult): Int {
        val ra = a.outcome.numbersRowMajor
        val rb = b.outcome.numbersRowMajor
        val va = isStrictValidRowMajorGrid(ra)
        val vb = isStrictValidRowMajorGrid(rb)
        when {
            va && !vb -> return 1
            !va && vb -> return -1
        }
        val fa = isDeceptiveFullGrid(ra)
        val fb = isDeceptiveFullGrid(rb)
        when {
            fa && !fb -> return -1
            !fa && fb -> return 1
        }
        val da = rowMajorDuplicateCount(ra)
        val db = rowMajorDuplicateCount(rb)
        if (da != db) return db.compareTo(da)
        val ca = strictValidColumnCellCount(ra)
        val cb = strictValidColumnCellCount(rb)
        if (ca != cb) return ca.compareTo(cb)
        val dda = distinctDisplayedCountGrid(ra)
        val ddb = distinctDisplayedCountGrid(rb)
        if (dda != ddb) return dda.compareTo(ddb)
        if (a.distinctValidCount != b.distinctValidCount) {
            return a.distinctValidCount.compareTo(b.distinctValidCount)
        }
        val oa = rowMajorOutOfColumnCount(ra)
        val ob = rowMajorOutOfColumnCount(rb)
        if (oa != ob) return ob.compareTo(oa)
        return 0
    }

    /** Rotate around center; output bounds padded so content is not clipped. */
    private fun rotateBitmapDeskew(src: Bitmap, degrees: Float): Bitmap? {
        if (abs(degrees) < 0.01f) return null
        val m = Matrix()
        m.setRotate(degrees, src.width / 2f, src.height / 2f)
        val rect = RectF(0f, 0f, src.width.toFloat(), src.height.toFloat())
        m.mapRect(rect)
        val w = rect.width().toInt().coerceAtLeast(1)
        val h = rect.height().toInt().coerceAtLeast(1)
        m.postTranslate(-rect.left, -rect.top)
        return try {
            val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(out)
            canvas.drawBitmap(src, m, null)
            out
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Uses ML Kit positions on [src] to estimate the Bingo number grid quadrilateral, then warps it
     * to a rectangle so slight perspective (trapezoid) is reduced before OCR.
     */
    private fun tryWarpPerspectiveGrid(src: Bitmap, recognizer: TextRecognizer): Bitmap? {
        val w = src.width
        val h = src.height
        if (w < 64 || h < 64) return null
        return try {
            val image = InputImage.fromBitmap(src, 0)
            val visionText = Tasks.await(recognizer.process(image))
            val items = buildOcrItems(visionText)
            val quad = estimateGridQuadFromOcr(items, w, h) ?: return null
            val (outW, outH) = outputSizeForQuad(quad) ?: return null
            warpPerspectiveToRect(src, quad, outW, outH)
        } catch (_: Exception) {
            null
        }
    }

    /** TL, TR, BR, BL from extreme 1–75 OCR points (axis-aligned-ish perspective). */
    private fun estimateGridQuadFromOcr(items: List<OcrItem>, iw: Int, ih: Int): FloatArray? {
        val valid = items.filter { it.value in 1..75 }
        if (valid.size < 6) return null
        var tl = valid[0]
        var tr = valid[0]
        var br = valid[0]
        var bl = valid[0]
        for (p in valid) {
            val s = p.centerX + p.centerY
            if (s < tl.centerX + tl.centerY) tl = p
            if (s > br.centerX + br.centerY) br = p
            val d = p.centerX - p.centerY
            if (d > tr.centerX - tr.centerY) tr = p
            if (d < bl.centerX - bl.centerY) bl = p
        }
        val quad = floatArrayOf(
            tl.centerX, tl.centerY,
            tr.centerX, tr.centerY,
            br.centerX, br.centerY,
            bl.centerX, bl.centerY,
        )
        clampQuadToBitmap(quad, iw, ih)
        val imgArea = (iw * ih).toFloat()
        if (quadArea(quad) < imgArea * 0.04f) return null
        return quad
    }

    private fun clampQuadToBitmap(quad: FloatArray, iw: Int, ih: Int) {
        val maxX = (iw - 1).coerceAtLeast(0).toFloat()
        val maxY = (ih - 1).coerceAtLeast(0).toFloat()
        for (i in 0 until 8 step 2) {
            quad[i] = quad[i].coerceIn(0f, maxX)
            quad[i + 1] = quad[i + 1].coerceIn(0f, maxY)
        }
    }

    private fun quadArea(quad: FloatArray): Float {
        var sum = 0f
        for (i in 0..3) {
            val j = (i + 1) % 4
            val xi = quad[i * 2]
            val yi = quad[i * 2 + 1]
            val xj = quad[j * 2]
            val yj = quad[j * 2 + 1]
            sum += xi * yj - xj * yi
        }
        return abs(sum * 0.5f)
    }

    private fun outputSizeForQuad(quad: FloatArray): Pair<Int, Int>? {
        val top = hypot(quad[2] - quad[0], quad[3] - quad[1])
        val bottom = hypot(quad[4] - quad[6], quad[5] - quad[7])
        val left = hypot(quad[6] - quad[0], quad[7] - quad[1])
        val right = hypot(quad[4] - quad[2], quad[5] - quad[3])
        val w = ((top + bottom) * 0.5f).toInt().coerceIn(180, 1400)
        val h = ((left + right) * 0.5f).toInt().coerceIn(180, 1400)
        if (w < 64 || h < 64) return null
        return w to h
    }

    private fun warpPerspectiveToRect(src: Bitmap, srcQuad: FloatArray, outW: Int, outH: Int): Bitmap? {
        val dst = floatArrayOf(
            0f, 0f,
            outW.toFloat(), 0f,
            outW.toFloat(), outH.toFloat(),
            0f, outH.toFloat(),
        )
        val m = Matrix()
        if (!m.setPolyToPoly(srcQuad, 0, dst, 0, 4)) return null
        return try {
            val out = Bitmap.createBitmap(outW, outH, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(out)
            canvas.drawBitmap(src, m, null)
            out
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Second-pass crop: zoom into the center of the first grid crop (~12% inset each side)
     * so digits are larger for ML Kit.
     */
    private fun tighterCenterZoomGridCrop(cropped: Bitmap): Bitmap? {
        val w = cropped.width
        val h = cropped.height
        if (w < 48 || h < 48) return null
        val insetX = (w * 0.12f).toInt().coerceAtLeast(2)
        val insetY = (h * 0.12f).toInt().coerceAtLeast(2)
        val cw = w - 2 * insetX
        val ch = h - 2 * insetY
        if (cw < 32 || ch < 32) return null
        return try {
            Bitmap.createBitmap(cropped, insetX, insetY, cw, ch)
        } catch (_: Exception) {
            null
        }
    }

    private fun runPipeline(ocrBitmap: Bitmap, recognizer: TextRecognizer): PipelineResult {
        val whole = runPipelineWholeGrid(ocrBitmap, recognizer)
        val cell = tryCellBasedGrid(ocrBitmap, recognizer)
        return mergeCellGridWithWhole(cell, whole)
    }

    /** Full-image ML Kit + spatial/text parsers; used alone if per-cell read is weak. */
    private fun runPipelineWholeGrid(ocrBitmap: Bitmap, recognizer: TextRecognizer): PipelineResult {
        val image = InputImage.fromBitmap(ocrBitmap, 0)
        val ow = ocrBitmap.width
        val oh = ocrBitmap.height
        val visionText = Tasks.await(recognizer.process(image))
        val raw = visionText.text
        val ocrItems = buildOcrItems(visionText)

        val filtered = filterToCentralGridRegion(ocrItems, ow, oh)
        val parseItems = filtered.takeIf { it.isNotEmpty() }
            ?: ocrItems.filter { it.value in 1..75 }

        val distinctValidCount = ocrItems.map { it.value }.filter { it in 1..75 }.distinct().size
        val weakMlKit = distinctValidCount < ML_KIT_WEAK_DISTINCT_THRESHOLD

        val spatialGrid = BingoNumberAnalyzer.parseCapturedTicket(parseItems, ow, oh)
        val spatialRowMajor = BingoNumberAnalyzer.buildRowMajorGridFromSpatialYGrouping(parseItems)
        val spatialNonZero = spatialRowMajor.count { it != 0 }

        if (!weakMlKit) {
            if (spatialGrid != null) {
                return PipelineResult(
                    HistoryImportOcrOutcome(
                        numbersRowMajor = rowMajorFromGrid(spatialGrid),
                        losNumber = null,
                        serialNumber = null,
                    ),
                    distinctValidCount,
                    raw,
                )
            }
            if (spatialNonZero >= 10) {
                return PipelineResult(
                    HistoryImportOcrOutcome(
                        numbersRowMajor = spatialRowMajor,
                        losNumber = null,
                        serialNumber = null,
                    ),
                    distinctValidCount,
                    raw,
                )
            }
            val textParse = BingoTicketParser.parseFromOcrText(raw)
            if (textParse.isValid && textParse.gridNumbers.size == 25) {
                return PipelineResult(
                    HistoryImportOcrOutcome(
                        numbersRowMajor = columnMajorListToRowMajor(textParse.gridNumbers),
                        losNumber = null,
                        serialNumber = null,
                    ),
                    distinctValidCount,
                    raw,
                )
            }
            if (spatialNonZero >= 1) {
                return PipelineResult(
                    HistoryImportOcrOutcome(
                        numbersRowMajor = spatialRowMajor,
                        losNumber = null,
                        serialNumber = null,
                    ),
                    distinctValidCount,
                    raw,
                )
            }
            val partial = parseItems
                .sortedWith(compareBy({ it.centerY }, { it.centerX }))
                .map { it.value }
                .filter { it in 1..75 }
            if (partial.isEmpty()) {
                error("No bingo numbers detected")
            }
            return PipelineResult(
                HistoryImportOcrOutcome(
                    numbersRowMajor = partial.take(25),
                    losNumber = null,
                    serialNumber = null,
                ),
                distinctValidCount,
                raw,
            )
        }

        val candidates = mutableListOf<List<Int>>()
        spatialGrid?.let { candidates.add(rowMajorFromGrid(it)) }
        candidates.add(spatialRowMajor)

        val textParse = BingoTicketParser.parseFromOcrText(raw)
        if (textParse.isValid && textParse.gridNumbers.size == 25) {
            candidates.add(columnMajorListToRowMajor(textParse.gridNumbers))
        }
        val lineOrderedRaw = buildLineOrderedPlainText(visionText)
        val lineParse = BingoTicketParser.parseFromOcrText(lineOrderedRaw)
        if (lineParse.isValid && lineParse.gridNumbers.size == 25) {
            candidates.add(columnMajorListToRowMajor(lineParse.gridNumbers))
        }
        val relaxedParse = BingoTicketParser.parseFromOcrTextRelaxed(raw)
        if (relaxedParse.isValid && relaxedParse.gridNumbers.size == 25) {
            candidates.add(columnMajorListToRowMajor(relaxedParse.gridNumbers))
        }
        val relaxedLine = BingoTicketParser.parseFromOcrTextRelaxed(lineOrderedRaw)
        if (relaxedLine.isValid && relaxedLine.gridNumbers.size == 25) {
            candidates.add(columnMajorListToRowMajor(relaxedLine.gridNumbers))
        }

        var best = candidates.maxWithOrNull { a, b -> compareRowMajorCandidates(a, b) } ?: spatialRowMajor
        val topTwo = candidates.sortedWith { a, b -> compareRowMajorCandidates(b, a) }.take(2)
        if (topTwo.size == 2) {
            val merged = mergeRowMajors(topTwo[0], topTwo[1])
            val mergedValid = isStrictValidRowMajorGrid(merged)
            val bestValid = isStrictValidRowMajorGrid(best)
            val pickMerged = when {
                mergedValid && !bestValid -> true
                else -> compareRowMajorCandidates(merged, best) > 0
            }
            if (pickMerged) best = merged
        }

        if (best.all { it == 0 }) {
            val partial = parseItems
                .sortedWith(compareBy({ it.centerY }, { it.centerX }))
                .map { it.value }
                .filter { it in 1..75 }
            if (partial.isEmpty()) error("No bingo numbers detected")
            best = partial.take(25)
        }

        return PipelineResult(
            HistoryImportOcrOutcome(
                numbersRowMajor = pad25(best),
                losNumber = null,
                serialNumber = null,
            ),
            distinctValidCount,
            raw,
        )
    }

    private fun isBingoHeaderBluePixel(p: Int): Boolean {
        val r = (p ushr 16) and 0xff
        val g = (p ushr 8) and 0xff
        val b = p and 0xff
        return b >= 62 && b > r + 10 && b > g + 7
    }

    /** Upper horizontal blue band (BINGO); returns y-index just below the band for cropping the grid. */
    private fun detectBlueBingoHeaderBottomY(bitmap: Bitmap): Int? {
        val w = bitmap.width
        val h = bitmap.height
        if (w < 64 || h < 64) return null
        val yLimit = (h * 0.40f).toInt().coerceAtLeast(8)
        val stepY = max(1, h / 90)
        val stepX = max(1, w / 100)
        data class SampleRow(val y: Int, val frac: Float)
        val rows = mutableListOf<SampleRow>()
        var y = 0
        while (y < yLimit) {
            var n = 0
            var blue = 0
            var x = 0
            while (x < w) {
                if (isBingoHeaderBluePixel(bitmap.getPixel(x, y))) blue++
                n++
                x += stepX
            }
            rows.add(SampleRow(y, if (n > 0) blue.toFloat() / n else 0f))
            y += stepY
        }
        if (rows.isEmpty()) return null
        var bestScore = -1f
        var bestStart = -1
        var bestEnd = -1
        var i = 0
        while (i < rows.size) {
            if (rows[i].frac < 0.13f) {
                i++
                continue
            }
            val j0 = i
            var sum = 0f
            while (i < rows.size && rows[i].frac >= 0.095f) {
                sum += rows[i].frac
                i++
            }
            val j1 = i - 1
            if (sum > bestScore && j1 >= j0) {
                bestScore = sum
                bestStart = j0
                bestEnd = j1
            }
        }
        if (bestStart < 0 || bestScore < 0.38f) return null
        if (rows[bestStart].y > h * 0.30f) return null
        var maxFrac = 0f
        for (k in bestStart..bestEnd) maxFrac = max(maxFrac, rows[k].frac)
        if (maxFrac < 0.175f) return null
        val yBottom = rows[bestEnd].y + stepY + (h * 0.012f).toInt().coerceAtLeast(2)
        return yBottom.coerceIn(h / 22, h - h / 7)
    }

    /**
     * Layout-anchored weak path: blue BINGO band → numeric grid to the right of the branding strip;
     * returns only if [isStrictValidRowMajorGrid] for per-cell OCR on the grid crop.
     */
    private fun tryWeakPathBingoHeaderLayout(
        pass1: Bitmap,
        recognizer: TextRecognizer,
    ): HistoryImportOcrOutcome? {
        val headerBottom = detectBlueBingoHeaderBottomY(pass1)
        if (headerBottom == null) {
            return null
        }
        val w = pass1.width
        val h = pass1.height
        val gridTop = headerBottom.coerceIn(1, h - 12)
        val stripW = (w * LEFT_STRIP_WIDTH_FRAC).toInt().coerceIn(20, w * 3 / 4)
        val gridW = w - stripW
        val gridH = h - gridTop
        var gridBmp: Bitmap? = null
        return try {
            if (gridW < 36 || gridH < 36) {
                return null
            }
            gridBmp = Bitmap.createBitmap(pass1, stripW, gridTop, gridW, gridH)
            val cell = cellGridFromRegion(
                gridBmp,
                recognizer,
                0,
                gridBmp.width,
                0,
                gridBmp.height,
                minFilled = 8,
            )
            if (cell == null) return null
            if (!isStrictValidRowMajorGrid(cell.rowMajor)) return null
            HistoryImportOcrOutcome(numbersRowMajor = pad25(cell.rowMajor), losNumber = null, serialNumber = null)
        } catch (_: Exception) {
            null
        } finally {
            gridBmp?.let { if (!it.isRecycled) it.recycle() }
        }
    }

    private fun mergeCellGridWithWhole(cell: CellGridResult?, whole: PipelineResult): PipelineResult {
        if (cell == null) return whole
        val cellPadded = pad25(cell.rowMajor)
        val wholeRm = whole.outcome.numbersRowMajor
        val cellValid = isStrictValidRowMajorGrid(cellPadded)
        val wholeValid = isStrictValidRowMajorGrid(wholeRm)
        val numbers = when {
            cellValid && !wholeValid -> cellPadded
            !cellValid && wholeValid -> wholeRm
            else -> {
                val cmp = compareRowMajorCandidates(cellPadded, wholeRm)
                if (cmp > 0) cellPadded else wholeRm
            }
        }
        val distinct = max(whole.distinctValidCount, cell.distinctCount)
        return PipelineResult(
            outcome = whole.outcome.copy(numbersRowMajor = numbers),
            distinctValidCount = distinct,
            raw = whole.raw,
        )
    }

    /**
     * Splits [gx0]..[gx1] × [gy0]..[gy1] into 5×5 cells and OCRs each cell.
     * Returns null if fewer than [minFilled] non-empty cells.
     */
    private fun cellGridFromRegion(
        bitmap: Bitmap,
        recognizer: TextRecognizer,
        gx0: Int,
        gx1: Int,
        gy0: Int,
        gy1: Int,
        minFilled: Int,
    ): CellGridResult? {
        val w = bitmap.width
        val h = bitmap.height
        if (w < 50 || h < 50) return null
        val gxo = gx0.coerceIn(0, w - 2)
        val gx1c = gx1.coerceIn(gxo + 4, w)
        val gyo = gy0.coerceIn(0, h - 2)
        val gy1c = gy1.coerceIn(gyo + 4, h)
        val gridW = gx1c - gxo
        val gridH = gy1c - gyo
        val cellWf = gridW / 5f
        val cellHf = gridH / 5f
        if (cellWf < 6f || cellHf < 6f) return null
        val cells = IntArray(25)
        for (row in 0..4) {
            for (col in 0..4) {
                val x0 = (gxo + col * cellWf).toInt().coerceIn(0, w - 1)
                val y0 = (gyo + row * cellHf).toInt().coerceIn(0, h - 1)
                val x1 = if (col == 4) gx1c else (gxo + (col + 1) * cellWf).toInt()
                val y1 = if (row == 4) gy1c else (gyo + (row + 1) * cellHf).toInt()
                val cw = (x1 - x0).coerceIn(4, w - x0)
                val ch = (y1 - y0).coerceIn(4, h - y0)
                val cellBmp = try {
                    Bitmap.createBitmap(bitmap, x0, y0, cw, ch)
                } catch (_: Exception) {
                    null
                } ?: continue
                try {
                    cells[row * 5 + col] = ocrSingleCellValue(cellBmp, col, recognizer)
                } finally {
                    if (!cellBmp.isRecycled) cellBmp.recycle()
                }
            }
        }
        val rowMajor = cells.toList()
        val filled = rowMajor.count { it != 0 }
        val distinct = rowMajor.filter { it in 1..75 }.distinct().size
        if (filled < minFilled) return null
        return CellGridResult(rowMajor, distinct)
    }

    /**
     * Splits the number grid into 5×5 cells, OCR each cell, and keeps values in the correct B–I–N–G–O column range.
     * Returns null if too few cells read (falls back to [runPipelineWholeGrid] only via merge).
     */
    private fun tryCellBasedGrid(bitmap: Bitmap, recognizer: TextRecognizer): CellGridResult? {
        val w = bitmap.width
        val h = bitmap.height
        if (w < 50 || h < 50) return null
        val gx0 = (w * GRID_NUMBER_AREA_LEFT_FRAC).toInt().coerceIn(0, w - 2)
        val gx1 = (w * GRID_NUMBER_AREA_RIGHT_FRAC).toInt().coerceIn(gx0 + 5, w)
        val gy0 = (h * GRID_NUMBER_AREA_TOP_FRAC).toInt().coerceIn(0, h - 2)
        val gy1 = (h * GRID_NUMBER_AREA_BOTTOM_FRAC).toInt().coerceIn(gy0 + 5, h)
        return cellGridFromRegion(bitmap, recognizer, gx0, gx1, gy0, gy1, CELL_GRID_MIN_FILLED)
    }

    private fun ocrSingleCellValue(cellBmp: Bitmap, col: Int, recognizer: TextRecognizer): Int {
        val range = BingoNumberAnalyzer.bingoColumnValueRange(col) ?: return 0
        return runCatching {
            val image = InputImage.fromBitmap(cellBmp, 0)
            val visionText = Tasks.await(recognizer.process(image))
            pickCellDigitCandidate(visionText.text, range)
        }.getOrDefault(0)
    }

    /** Prefer 1–2 digit substrings that fall in [range]; tie-break by frequency then magnitude. */
    private fun pickCellDigitCandidate(raw: String, range: IntRange): Int {
        val d = raw.filter { it.isDigit() }
        if (d.isEmpty()) return 0
        val hits = mutableListOf<Int>()
        for (i in d.indices) {
            for (len in 1..min(2, d.length - i)) {
                val v = d.substring(i, i + len).toIntOrNull() ?: continue
                if (v in range) hits.add(v)
            }
        }
        if (hits.isEmpty()) return 0
        val grouped = hits.groupingBy { it }.eachCount()
        val bestCount = grouped.values.maxOrNull() ?: 1
        return grouped.filter { it.value == bestCount }.keys.minOrNull() ?: hits.first()
    }

    /**
     * Crops toward the central 5×5 number grid (less branding left/right, less header/barcode
     * top/bottom) before ML Kit runs, so more budget goes to grid text.
     */
    private fun gridCropForNumberRegion(src: Bitmap): Bitmap {
        val analyzerRect = BingoNumberAnalyzer.tryDetectBingoGridCropRectForOcr(src)
        if (analyzerRect != null) {
            val padX = (analyzerRect.width() * 0.04f).toInt().coerceAtLeast(1)
            val padY = (analyzerRect.height() * 0.05f).toInt().coerceAtLeast(1)
            val left = (analyzerRect.left - padX).coerceIn(0, src.width - 2)
            val top = (analyzerRect.top - padY).coerceIn(0, src.height - 2)
            val right = (analyzerRect.right + padX).coerceIn(left + 2, src.width)
            val bottom = (analyzerRect.bottom + padY).coerceIn(top + 2, src.height)
            val paddedRect = Rect(left, top, right, bottom)
            val analyzerCrop = try {
                Bitmap.createBitmap(src, paddedRect.left, paddedRect.top, paddedRect.width(), paddedRect.height())
            } catch (_: Exception) {
                null
            }
            if (analyzerCrop != null) {
                return analyzerCrop
            }
        }
        return heuristicCentralGridCrop(src)
    }

    /** Fallback when [BingoNumberAnalyzer.tryDetectBingoGridCropForOcr] returns null. */
    private fun heuristicCentralGridCrop(src: Bitmap): Bitmap {
        val w = src.width
        val h = src.height
        if (w < 32 || h < 32) return src
        val left = (w * 0.14f).toInt().coerceIn(0, w - 2)
        val right = (w * 0.86f).toInt().coerceIn(left + 1, w)
        val top = (h * 0.18f).toInt().coerceIn(0, h - 2)
        val bottom = (h * 0.78f).toInt().coerceIn(top + 1, h)
        val cw = right - left
        val ch = bottom - top
        return try {
            Bitmap.createBitmap(src, left, top, cw, ch)
        } catch (_: Exception) {
            src
        }
    }

    private fun rowMajorFromGrid(grid: ScannedBingoGrid): List<Int> {
        val g = Array(5) { IntArray(5) { 0 } }
        for (cell in grid.cells) {
            if (cell.row in 0..4 && cell.col in 0..4) g[cell.row][cell.col] = cell.value
        }
        return (0..4).flatMap { r -> (0..4).map { c -> g[r][c] } }
    }

    private fun columnMajorListToRowMajor(columnOrder: List<Int>): List<Int> {
        val g = Array(5) { IntArray(5) { 0 } }
        for (col in 0..4) {
            for (row in 0..4) {
                g[row][col] = columnOrder[col * 5 + row]
            }
        }
        return (0..4).flatMap { r -> (0..4).map { c -> g[r][c] } }
    }

    private fun cropBitmapRect(src: Bitmap, x: Int, y: Int, reqW: Int, reqH: Int): Bitmap? {
        val w = src.width
        val h = src.height
        val x0 = x.coerceIn(0, w - 1)
        val y0 = y.coerceIn(0, h - 1)
        val cw = reqW.coerceAtMost(w - x0).coerceAtLeast(0)
        val ch = reqH.coerceAtMost(h - y0).coerceAtLeast(0)
        if (cw < 4 || ch < 4) return null
        return try {
            Bitmap.createBitmap(src, x0, y0, cw, ch)
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Drops OCR outside the central grid band (cluster inset + image margins) to reduce
     * barcode/branding/promo false positives before spatial parsing.
     */
    private fun filterToCentralGridRegion(items: List<OcrItem>, w: Int, h: Int): List<OcrItem> {
        val valid = items.filter { it.value in 1..75 }
        if (valid.isEmpty()) return emptyList()
        val minX = valid.minOf { it.centerX }
        val maxX = valid.maxOf { it.centerX }
        val minY = valid.minOf { it.centerY }
        val maxY = valid.maxOf { it.centerY }
        val spanX = (maxX - minX).coerceAtLeast(1f)
        val spanY = (maxY - minY).coerceAtLeast(1f)
        val strongCount = 20
        val useStrongInset = valid.size >= strongCount
        val insetX = spanX * if (useStrongInset) 0.10f else 0.05f
        val insetY = spanY * if (useStrongInset) 0.14f else 0.08f
        var left = minX + insetX
        var right = maxX - insetX
        var top = minY + insetY
        var bottom = maxY - insetY
        val imgLeft = w * 0.08f
        val imgRight = w * 0.92f
        val imgTop = h * 0.12f
        val imgBottom = h * 0.90f
        left = maxOf(left, imgLeft)
        right = minOf(right, imgRight)
        top = maxOf(top, imgTop)
        bottom = minOf(bottom, imgBottom)
        if (right <= left || bottom <= top) return valid
        val inner = valid.filter { it.centerX in left..right && it.centerY in top..bottom }
        return if (inner.size >= 6) inner else valid
    }

    private fun pad25(rowMajor: List<Int>): List<Int> =
        if (rowMajor.size >= 25) rowMajor.take(25) else rowMajor + List(25 - rowMajor.size) { 0 }

    /** Same value in multiple cells: keep one (column-range match, then earliest row-major index); zero others. */
    private fun dedupeDuplicateBingoValuesRowMajor(rowMajor: List<Int>): Pair<List<Int>, Int> {
        val grid = pad25(rowMajor).toMutableList()
        var removed = 0
        val byValue = mutableMapOf<Int, MutableList<Int>>()
        for (i in 0 until 25) {
            val v = grid[i]
            if (v != 0) byValue.getOrPut(v) { mutableListOf() }.add(i)
        }
        fun inColumnRange(idx: Int, value: Int): Boolean {
            val col = idx % 5
            val rng = BingoNumberAnalyzer.bingoColumnValueRange(col) ?: return false
            return value in rng
        }
        for ((value, indices) in byValue) {
            if (indices.size <= 1) continue
            val inRange = indices.filter { inColumnRange(it, value) }
            val keeper = when {
                inRange.isNotEmpty() -> inRange.minOrNull()!!
                else -> indices.minOrNull()!!
            }
            for (idx in indices) {
                if (idx != keeper) {
                    grid[idx] = 0
                    removed++
                }
            }
        }
        return grid.toList() to removed
    }

    /** Left-strip meta only; does not touch [HistoryImportOcrOutcome.numbersRowMajor]. */
    private fun attachLeftStripMeta(gridOnly: HistoryImportOcrOutcome, ticketBitmap: Bitmap): HistoryImportOcrOutcome {
        val (los, serial) = LeftStripMetaOcr.detect(ticketBitmap)
        return if (los == null && serial == null) gridOnly else gridOnly.copy(losNumber = los, serialNumber = serial)
    }

    /** Non-zero cells whose value lies in the expected B/I/N/G/O column (row-major). */
    private fun strictValidColumnCellCount(rowMajor: List<Int>): Int {
        val p = pad25(rowMajor)
        if (p.size != 25) return 0
        var n = 0
        for (i in 0..24) {
            val v = p[i]
            if (v == 0) continue
            val col = i % 5
            if (v in STRICT_COLUMN_RANGES[col]) n++
        }
        return n
    }

    /**
     * Stricter than [BingoTicketParser.isValidRowMajorGrid]: all 25 non-zero, ≥[STRICT_MIN_COLUMN_VALID_TOTAL]
     * column-correct cells, each column ≥[STRICT_MIN_PER_COLUMN] in-range values, duplicate/OOB slack caps.
     */
    private fun isStrictValidRowMajorGrid(rowMajor: List<Int>): Boolean {
        val p = pad25(rowMajor)
        if (p.size != 25) return false
        if (p.any { it == 0 }) return false
        if (strictValidColumnCellCount(p) < STRICT_MIN_COLUMN_VALID_TOTAL) return false
        for (c in 0..4) {
            val inCol = (0..4).count { r -> p[r * 5 + c] in STRICT_COLUMN_RANGES[c] }
            if (inCol < STRICT_MIN_PER_COLUMN) return false
        }
        if (rowMajorDuplicateCount(p) > STRICT_MAX_DUPLICATE_EXCESS) return false
        if (rowMajorOutOfColumnCount(p) > STRICT_MAX_OUT_OF_COLUMN) return false
        return true
    }

    /** Returns > 0 if [a] is a better grid than [b] (full 25 valid > filled > column match > fewer dupes > fewer OOB). */
    private fun compareRowMajorCandidates(a: List<Int>, b: List<Int>): Int {
        val sa = rowMajorScoreTriple(a)
        val sb = rowMajorScoreTriple(b)
        if (sa.first != sb.first) {
            return when {
                sa.first && !sb.first -> 1
                !sa.first && sb.first -> -1
                else -> 0
            }
        }
        if (sa.second != sb.second) return sa.second.compareTo(sb.second)
        if (sa.third != sb.third) return sa.third.compareTo(sb.third)
        val da = rowMajorDuplicateCount(a)
        val db = rowMajorDuplicateCount(b)
        if (da != db) return db.compareTo(da)
        val oa = rowMajorOutOfColumnCount(a)
        val ob = rowMajorOutOfColumnCount(b)
        if (oa != ob) return ob.compareTo(oa)
        return 0
    }

    private fun rowMajorDuplicateCount(rm: List<Int>): Int {
        val p = pad25(rm).filter { it != 0 }
        if (p.isEmpty()) return 0
        return p.groupingBy { it }.eachCount().values.sumOf { max(0, it - 1) }
    }

    private fun rowMajorOutOfColumnCount(rm: List<Int>): Int {
        val p = pad25(rm)
        var n = 0
        for (i in 0..24) {
            val v = p[i]
            if (v == 0) continue
            val col = i % 5
            val range = BingoNumberAnalyzer.bingoColumnValueRange(col) ?: continue
            if (v !in range) n++
        }
        return n
    }

    private fun rowMajorScoreTriple(rm: List<Int>): Triple<Boolean, Int, Int> {
        val p = pad25(rm)
        return Triple(
            isStrictValidRowMajorGrid(p),
            p.count { it != 0 },
            BingoTicketParser.columnRangeMatchCount(p),
        )
    }

    private fun mergeRowMajors(a: List<Int>, b: List<Int>): List<Int> {
        val pa = pad25(a)
        val pb = pad25(b)
        fun inColRange(col: Int, v: Int): Boolean = when (col) {
            0 -> v in 1..15
            1 -> v in 16..30
            2 -> v in 31..45
            3 -> v in 46..60
            else -> v in 61..75
        }
        return (0..24).map { i ->
            val col = i % 5
            val ra = pa[i]
            val rb = pb[i]
            when {
                ra != 0 && inColRange(col, ra) -> ra
                rb != 0 && inColRange(col, rb) -> rb
                ra != 0 -> ra
                rb != 0 -> rb
                else -> 0
            }
        }
    }

    /** Reading-order lines (top-to-bottom, left-to-right) for row-oriented [BingoTicketParser] input. */
    private fun buildLineOrderedPlainText(visionText: Text): String {
        val lines = mutableListOf<Triple<Float, Float, String>>()
        for (block in visionText.textBlocks) {
            for (line in block.lines) {
                val box = line.boundingBox ?: continue
                val cx = (box.left + box.right) / 2f
                val cy = (box.top + box.bottom) / 2f
                lines.add(Triple(cy, cx, line.text))
            }
        }
        return lines.sortedWith(compareBy({ it.first }, { it.second })).joinToString("\n") { it.third }
    }

    private fun buildOcrItems(visionText: Text): List<OcrItem> {
        val out = ArrayList<OcrItem>()
        for (block in visionText.textBlocks) {
            for (line in block.lines) {
                for (element in line.elements) {
                    val n = BingoNumberAnalyzer.extractBingoNumber(element.text) ?: continue
                    val box = element.boundingBox ?: continue
                    val cx = (box.left + box.right) / 2f
                    val cy = (box.top + box.bottom) / 2f
                    out.add(OcrItem(n, cx, cy))
                }
            }
        }
        return out
    }

    private fun loadBitmapDownsampled(context: Context, uri: Uri, maxSide: Int): Bitmap? {
        val cr = context.contentResolver
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        cr.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / sample > maxSide) sample *= 2
        val opts = BitmapFactory.Options().apply {
            inSampleSize = sample
        }
        return cr.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
    }
}
