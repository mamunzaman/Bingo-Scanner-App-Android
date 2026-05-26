package com.example.mamunbingoapp.scanner

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * OCR for [com.example.mamunbingoapp.domain.model.BingoScanType.ONLINE] only.
 * Grid: per-cell OCR when layout is clear; otherwise position-based ML Kit fallback (no header/circle dependency).
 */
object OnlineBingoOcr {

    private const val TAG = "OnlineBingoOcr"

    /** Minimum non-zero grid cells for ONLINE success (preview / manual correction). */
    const val MIN_VALID_CELLS_FOR_SUCCESS = 15

    private const val CELL_GRID_MIN_FILLED = 12
    private const val POSITION_FALLBACK_MIN_VALID = MIN_VALID_CELLS_FOR_SUCCESS
    private const val GRID_TOP_INSET_FRAC = 0.02f
    private const val GRID_SIDE_INSET_FRAC = 0.04f
    /** Fallback cell-crop bottom when row layout cannot be estimated. */
    private const val GRID_BOTTOM_FRAC = 0.90f
    /** Footer OCR crop only (unchanged); number exclusion uses [GridRowLayout.footerCutoffY]. */
    private const val FOOTER_TOP_FRAC = 0.78f

    private val serieRegex = Regex("(?i)serie\\s*[:#\\-.]?\\s*([0-9]{1,8})")
    private val losRegex = Regex("(?i)los[\\s\\-]*nr\\.?\\s*[:#\\-.]?\\s*([0-9]{1,8})")
    private val footerLabelPattern =
        Regex("(?i)serie|seriennummer|los(?:nummer|\\s*nr\\.?)?|los\\s*nr")
    private val gridNumberPattern = Regex("(?<![0-9])([1-9]|[1-6][0-9]|7[0-5])(?![0-9])")

    fun analyzeUri(context: Context, uri: Uri): HistoryImportOcrOutcome {
        Log.d(TAG, "OnlineBingoOcr selected")
        val bitmap = loadBitmapDownsampled(context, uri, maxSide = 1600)
            ?: error("Could not load image")
        Log.d(TAG, "started bitmapSize=${bitmap.width}x${bitmap.height}")
        return try {
            analyzeBitmapWithPreprocessRetries(bitmap)
        } finally {
            if (!bitmap.isRecycled) bitmap.recycle()
        }
    }

    private data class OnlineOcrAttempt(
        val variantName: String,
        val outcome: HistoryImportOcrOutcome,
        val filledCount: Int,
        val validCount: Int,
    )

    private fun analyzeBitmapWithPreprocessRetries(source: Bitmap): HistoryImportOcrOutcome {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        return try {
            val attempts = mutableListOf<OnlineOcrAttempt>()
            for ((variantName, variantBitmap) in buildPreprocessVariants(source)) {
                val attempt = runCatching {
                    analyzeBitmapOnce(variantBitmap, recognizer, variantName)
                }.getOrElse {
                    Log.d(TAG, "preprocessing variant name=$variantName failed: ${it.message}")
                    null
                }
                if (variantBitmap !== source && !variantBitmap.isRecycled) {
                    variantBitmap.recycle()
                }
                if (attempt != null) {
                    Log.d(
                        TAG,
                        "preprocessing variant name=${attempt.variantName} valid cell count=${attempt.validCount}",
                    )
                    attempts.add(attempt)
                }
            }
            val best = attempts.maxWithOrNull(
                compareBy<OnlineOcrAttempt>({ it.validCount }, { it.filledCount }),
            )
            Log.d(TAG, "selected best variant=${best?.variantName ?: "none"}")
            best?.outcome ?: HistoryImportOcrOutcome(numbersRowMajor = pad25(emptyList()))
        } finally {
            recognizer.close()
        }
    }

    /** Single-variant ONLINE OCR (grid + footer); unchanged logic per bitmap. */
    private fun analyzeBitmapOnce(
        bitmap: Bitmap,
        recognizer: TextRecognizer,
        variantName: String,
    ): OnlineOcrAttempt {
        val visionText = ocrFullImage(bitmap, recognizer)
            ?: return OnlineOcrAttempt(
                variantName = variantName,
                outcome = HistoryImportOcrOutcome(numbersRowMajor = pad25(emptyList())),
                filledCount = 0,
                validCount = 0,
            )

        val footerTop = (bitmap.height * FOOTER_TOP_FRAC).toInt().coerceIn(0, bitmap.height - 4)
        val footerBmp = Bitmap.createBitmap(
            bitmap,
            0,
            footerTop,
            bitmap.width,
            bitmap.height - footerTop,
        )
        val (serie, los) = try {
            parseFooterMeta(footerBmp, recognizer, visionText, bitmap.height)
        } finally {
            if (!footerBmp.isRecycled) footerBmp.recycle()
        }

        val rawCandidates = collectNumericCandidates(visionText)
        Log.d(
            TAG,
            "variant=$variantName bitmapSize=${bitmap.width}x${bitmap.height} rawCandidateCount=${rawCandidates.size}",
        )
        val preGridCandidates = filterPreGridCandidates(
            rawCandidates,
            excludeSerie = serie,
            excludeLos = los,
        )
        val gridLayout = computeGridRowLayout(preGridCandidates, bitmap.height)

        val cellRowMajor = tryCellGrid(bitmap, recognizer, gridLayout)
        val positionRowMajor = tryPositionGridFromVisionText(
            preGridCandidates = preGridCandidates,
            gridLayout = gridLayout,
            imageW = bitmap.width,
            imageH = bitmap.height,
        )

        val cellPadded = pad25(cellRowMajor)
        val positionPadded = pad25(positionRowMajor)
        val cellFilled = filledCellCount(cellPadded)
        val positionFilled = filledCellCount(positionPadded)
        val rowMajor = when {
            positionFilled >= POSITION_FALLBACK_MIN_VALID &&
                positionFilled >= cellFilled ->
                positionPadded
            cellFilled >= CELL_GRID_MIN_FILLED && cellFilled >= positionFilled ->
                cellPadded
            positionFilled >= POSITION_FALLBACK_MIN_VALID -> positionPadded
            cellFilled >= CELL_GRID_MIN_FILLED -> cellPadded
            positionFilled > cellFilled -> positionPadded
            else -> if (positionFilled > 0) positionPadded else cellPadded
        }
        val finalFilled = filledCellCount(rowMajor)
        val finalValid = validColumnCellCount(rowMajor)
        logMissingCellIndexes(rowMajor, variantName)

        val outcome = HistoryImportOcrOutcome(
            numbersRowMajor = rowMajor,
            losNumber = los,
            serialNumber = serie,
        )
        return OnlineOcrAttempt(
            variantName = variantName,
            outcome = outcome,
            filledCount = finalFilled,
            validCount = finalValid,
        )
    }

    private fun buildPreprocessVariants(source: Bitmap): List<Pair<String, Bitmap>> {
        val variants = mutableListOf<Pair<String, Bitmap>>()
        variants.add("original" to source)
        preprocessContrastEnhanced(source)?.let { variants.add("contrast-enhanced" to it) }
        preprocessSharpenedContrast(source)?.let { variants.add("sharpened+contrast-enhanced" to it) }
        preprocessGrayscaleThreshold(source)?.let { variants.add("grayscale-threshold" to it) }
        return variants
    }

    private fun ocrFullImage(bitmap: Bitmap, recognizer: TextRecognizer): Text? =
        runCatching {
            val image = InputImage.fromBitmap(bitmap, 0)
            Tasks.await(recognizer.process(image))
        }.getOrNull()

    private data class GridRowLayout(
        val rowCenters: List<Float>,
        val avgRowGap: Float,
        val expectedFifthRowY: Float?,
        val gridBottomY: Float,
        val footerCutoffY: Float,
    )

    private fun tryCellGrid(
        bitmap: Bitmap,
        recognizer: TextRecognizer,
        gridLayout: GridRowLayout,
    ): List<Int> {
        val headerBottom = detectGreenBingoHeaderBottomY(bitmap)
        val gridTop = (headerBottom ?: (bitmap.height * 0.10f).toInt())
            .coerceIn(1, bitmap.height - 40)
        val layoutBottom = gridLayout.gridBottomY.toInt()
        val fracBottom = (bitmap.height * GRID_BOTTOM_FRAC).toInt()
        val gridBottom = max(layoutBottom, fracBottom)
            .coerceIn(gridTop + 40, bitmap.height - 8)
        val sideInset = (bitmap.width * GRID_SIDE_INSET_FRAC).toInt()
        val gx0 = sideInset.coerceIn(0, bitmap.width - 20)
        val gx1 = (bitmap.width - sideInset).coerceIn(gx0 + 20, bitmap.width)
        val gy0 = (gridTop + bitmap.height * GRID_TOP_INSET_FRAC).toInt()
            .coerceIn(gridTop, gridBottom - 20)
        val gy1 = gridBottom

        val gridBmp = Bitmap.createBitmap(bitmap, gx0, gy0, gx1 - gx0, gy1 - gy0)
        return try {
            cellGridFromRegion(gridBmp, recognizer, 0, gridBmp.width, 0, gridBmp.height)
                ?.rowMajor
                ?: emptyList()
        } finally {
            if (!gridBmp.isRecycled) gridBmp.recycle()
        }
    }

    private data class PositionCandidate(
        val value: Int,
        val cx: Float,
        val cy: Float,
        val onFooterLabelLine: Boolean,
    )

    private fun filterPreGridCandidates(
        raw: List<PositionCandidate>,
        excludeSerie: String?,
        excludeLos: String?,
    ): List<PositionCandidate> =
        raw.filter { candidate ->
            if (candidate.onFooterLabelLine) return@filter false
            val s = candidate.value.toString()
            if (excludeSerie != null && s == excludeSerie) return@filter false
            if (excludeLos != null && s == excludeLos) return@filter false
            true
        }

    private fun computeGridRowLayout(
        preGridCandidates: List<PositionCandidate>,
        imageH: Int,
    ): GridRowLayout {
        val fallbackBottom = imageH * GRID_BOTTOM_FRAC
        val fallbackCutoff = imageH * 0.94f
        if (preGridCandidates.isEmpty()) {
            Log.d(TAG, "candidate Y range=empty footerCutoffY=${fallbackCutoff.toInt()} rowCenters=[] expected5th=null")
            return GridRowLayout(
                rowCenters = emptyList(),
                avgRowGap = imageH * 0.05f,
                expectedFifthRowY = null,
                gridBottomY = fallbackBottom,
                footerCutoffY = fallbackCutoff,
            )
        }
        val yMin = preGridCandidates.minOf { it.cy }
        val yMax = preGridCandidates.maxOf { it.cy }
        Log.d(TAG, "candidate Y range=${yMin.toInt()}..${yMax.toInt()} imageH=$imageH")

        val initialClusters = clusterCandidatesIntoRowsRaw(preGridCandidates, imageH)
        val rowCenters = initialClusters
            .map { row -> row.map { it.cy }.average().toFloat() }
            .sorted()
        val gaps = rowCenters.zipWithNext { a, b -> b - a }
        val avgGap = if (gaps.isNotEmpty()) {
            gaps.average().toFloat().coerceAtLeast(imageH * 0.022f)
        } else {
            imageH * 0.05f
        }
        val expectedFifthRowY = when {
            rowCenters.size >= 5 -> rowCenters[4]
            rowCenters.isNotEmpty() -> rowCenters.last() + avgGap * (5 - rowCenters.size).coerceAtLeast(0)
            else -> null
        }
        val gridBottomY = when {
            expectedFifthRowY != null -> expectedFifthRowY + avgGap * 0.62f
            rowCenters.isNotEmpty() -> rowCenters.last() + avgGap * 0.62f
            else -> fallbackBottom
        }
        val footerCutoffY = min(
            imageH * 0.96f,
            max(
                gridBottomY + avgGap * 0.42f,
                (expectedFifthRowY ?: gridBottomY) + avgGap * 0.78f,
            ),
        )
        Log.d(
            TAG,
            "detected row centers=${rowCenters.map { it.toInt() }} avgGap=${avgGap.toInt()} " +
                "expected5th=${expectedFifthRowY?.toInt()} gridBottom=${gridBottomY.toInt()} " +
                "footerCutoffY=${footerCutoffY.toInt()}",
        )
        return GridRowLayout(
            rowCenters = rowCenters,
            avgRowGap = avgGap,
            expectedFifthRowY = expectedFifthRowY,
            gridBottomY = gridBottomY,
            footerCutoffY = footerCutoffY,
        )
    }

    private fun filterGridNumberCandidates(
        candidates: List<PositionCandidate>,
        layout: GridRowLayout,
    ): List<PositionCandidate> {
        val nearFifthTolerance = layout.avgRowGap * 0.72f
        return candidates.filter { candidate ->
            if (candidate.cy < layout.footerCutoffY) return@filter true
            val fifthY = layout.expectedFifthRowY ?: return@filter false
            abs(candidate.cy - fifthY) <= nearFifthTolerance
        }
    }

    private fun tryPositionGridFromVisionText(
        preGridCandidates: List<PositionCandidate>,
        gridLayout: GridRowLayout,
        imageW: Int,
        imageH: Int,
    ): List<Int> {
        Log.d(TAG, "raw numeric candidates count=${preGridCandidates.size}")

        val filtered = filterGridNumberCandidates(preGridCandidates, gridLayout)
        Log.d(TAG, "candidates after footer filter=${filtered.size}")

        val rowClusters = clusterCandidatesIntoRows(filtered, imageH)
        Log.d(TAG, "grouped row count=${rowClusters.size}")

        val grid = buildRowMajorFromRowClusters(rowClusters, imageW, gridLayout) ?: return emptyList()
        val padded = pad25(grid)
        val filled = filledCellCount(padded)
        if (filled < POSITION_FALLBACK_MIN_VALID) return emptyList()
        return padded
    }

    private fun collectNumericCandidates(visionText: Text): List<PositionCandidate> {
        val out = mutableListOf<PositionCandidate>()
        val seen = mutableSetOf<String>()
        for (block in visionText.textBlocks) {
            for (line in block.lines) {
                val lineHasFooterLabel = footerLabelPattern.containsMatchIn(line.text)
                val lbox = line.boundingBox
                if (line.elements.isNotEmpty()) {
                    for (element in line.elements) {
                        val box = element.boundingBox ?: continue
                        val cx = (box.left + box.right) / 2f
                        val cy = (box.top + box.bottom) / 2f
                        appendNumberMatches(element.text, cx, cy, lineHasFooterLabel, seen, out)
                    }
                } else if (lbox != null) {
                    val cx = (lbox.left + lbox.right) / 2f
                    val cy = (lbox.top + lbox.bottom) / 2f
                    appendNumberMatches(line.text, cx, cy, lineHasFooterLabel, seen, out)
                }
            }
        }
        return out
    }

    private fun appendNumberMatches(
        text: String,
        cx: Float,
        cy: Float,
        onFooterLabelLine: Boolean,
        seen: MutableSet<String>,
        out: MutableList<PositionCandidate>,
    ) {
        for (match in gridNumberPattern.findAll(text)) {
            val value = match.groupValues[1].toIntOrNull() ?: continue
            if (value !in 1..75) continue
            val key = "$value@${cx.toInt()},${cy.toInt()}"
            if (key in seen) continue
            seen.add(key)
            out.add(PositionCandidate(value, cx, cy, onFooterLabelLine))
        }
    }

    private fun clusterCandidatesIntoRowsRaw(
        candidates: List<PositionCandidate>,
        imageH: Int,
    ): List<List<PositionCandidate>> {
        if (candidates.isEmpty()) return emptyList()
        val sorted = candidates.sortedBy { it.cy }
        val gaps = sorted.zipWithNext { a, b -> b.cy - a.cy }
        val medianGap = gaps.sorted().let { g ->
            if (g.isEmpty()) imageH * 0.05f else g[g.size / 2]
        }.coerceAtLeast(imageH * 0.022f)
        val threshold = medianGap * 0.65f

        val clusters = mutableListOf<MutableList<PositionCandidate>>()
        var current = mutableListOf(sorted.first())
        for (i in 1 until sorted.size) {
            if (sorted[i].cy - sorted[i - 1].cy <= threshold) {
                current.add(sorted[i])
            } else {
                clusters.add(current)
                current = mutableListOf(sorted[i])
            }
        }
        clusters.add(current)
        return clusters.filter { it.isNotEmpty() }
    }

    private fun clusterCandidatesIntoRows(
        candidates: List<PositionCandidate>,
        imageH: Int,
    ): List<List<PositionCandidate>> {
        val nonEmpty = clusterCandidatesIntoRowsRaw(candidates, imageH)
        val totalNums = nonEmpty.sumOf { it.size }
        if (totalNums < POSITION_FALLBACK_MIN_VALID) return emptyList()
        val substantial = nonEmpty.filter { it.size >= 2 }.ifEmpty { nonEmpty }
        return when {
            substantial.size in 2..5 -> substantial
            substantial.size > 5 -> pickBestRowWindow(substantial, windowSize = 5)
            else -> substantial
        }
    }

    private fun pickBestRowWindow(
        rows: List<List<PositionCandidate>>,
        windowSize: Int,
    ): List<List<PositionCandidate>> {
        if (rows.size <= windowSize) return rows
        var bestStart = 0
        var bestScore = -1
        for (start in 0..rows.size - windowSize) {
            val window = rows.subList(start, start + windowSize)
            val score = window.sumOf { it.size }
            if (score > bestScore) {
                bestScore = score
                bestStart = start
            }
        }
        return rows.subList(bestStart, bestStart + windowSize)
    }

    private fun buildRowMajorFromRowClusters(
        rowClusters: List<List<PositionCandidate>>,
        imageW: Int,
        layout: GridRowLayout,
    ): List<Int>? {
        if (rowClusters.isEmpty() || rowClusters.size > 5) return null
        if (rowClusters.flatten().size < POSITION_FALLBACK_MIN_VALID) return null

        val all = rowClusters.flatten()
        if (all.isEmpty()) return null

        val minX = all.minOf { it.cx }
        val maxX = all.maxOf { it.cx }
        val span = (maxX - minX).coerceAtLeast(imageW * 0.2f)
        val colWidth = span / 5f

        val slotYs = rowSlotYs(layout)
        val rowMajor = IntArray(25)

        for (row in rowClusters) {
            val clusterCy = row.map { it.cy }.average().toFloat()
            val targetRow = slotYs.indices.minByOrNull { abs(slotYs[it] - clusterCy) } ?: continue
            if (targetRow !in 0..4) continue
            val sorted = row.sortedBy { it.cx }
            for (item in sorted) {
                var col = ((item.cx - minX) / colWidth).toInt().coerceIn(0, 4)
                val alt = findBestColumnForValue(item.value, col)
                if (alt != null) col = alt
                placeValueInRow(rowMajor, targetRow, col, item.value)
            }
        }
        return rowMajor.toList()
    }

    /** Expected Y for each of the 5 bingo rows (0 = top). */
    private fun rowSlotYs(layout: GridRowLayout): List<Float> {
        val gap = layout.avgRowGap
        return when {
            layout.rowCenters.size >= 5 -> layout.rowCenters.take(5)
            layout.rowCenters.isNotEmpty() -> {
                val first = layout.rowCenters.first()
                List(5) { i ->
                    when {
                        i < layout.rowCenters.size -> layout.rowCenters[i]
                        i == 4 && layout.expectedFifthRowY != null -> layout.expectedFifthRowY
                        else -> first + gap * i
                    }
                }
            }
            layout.expectedFifthRowY != null -> {
                val fifth = layout.expectedFifthRowY
                List(5) { i -> fifth - gap * (4 - i) }
            }
            else -> List(5) { i -> layout.gridBottomY - gap * (4.5f - i) }
        }
    }

    /** Preserve detected value in row; use geometric column, spill to nearest empty slot in row if taken. */
    private fun placeValueInRow(rowMajor: IntArray, targetRow: Int, preferredCol: Int, value: Int) {
        if (targetRow !in 0..4) return
        var col = preferredCol.coerceIn(0, 4)
        val primary = targetRow * 5 + col
        if (rowMajor[primary] == 0) {
            rowMajor[primary] = value
            return
        }
        if (rowMajor[primary] == value) return
        for (d in 1..4) {
            val left = col - d
            if (left >= 0) {
                val idx = targetRow * 5 + left
                if (rowMajor[idx] == 0) {
                    rowMajor[idx] = value
                    return
                }
            }
            val right = col + d
            if (right <= 4) {
                val idx = targetRow * 5 + right
                if (rowMajor[idx] == 0) {
                    rowMajor[idx] = value
                    return
                }
            }
        }
    }

    private fun findBestColumnForValue(value: Int, preferredCol: Int): Int? {
        var bestCol: Int? = null
        var bestDist = Float.MAX_VALUE
        for (c in 0..4) {
            val range = BingoNumberAnalyzer.bingoColumnValueRange(c) ?: continue
            if (value !in range) continue
            val dist = abs(c - preferredCol).toFloat()
            if (dist < bestDist) {
                bestDist = dist
                bestCol = c
            }
        }
        return bestCol
    }

    private fun filledCellCount(rowMajor: List<Int>): Int = pad25(rowMajor).count { it != 0 }

    private fun logMissingCellIndexes(rowMajor: List<Int>, variantName: String) {
        val missing = pad25(rowMajor).mapIndexedNotNull { index, value ->
            if (value == 0) index else null
        }
        Log.d(
            TAG,
            "[$variantName] missing cell indexes (0-based row-major)=$missing count=${missing.size}",
        )
    }

    private fun preprocessContrastEnhanced(src: Bitmap): Bitmap? =
        toGrayscaleHighContrast(src, contrastScale = 1.48f, contrastTranslate = -24f)

    private fun preprocessSharpenedContrast(src: Bitmap): Bitmap? {
        val sharpened = sharpenBitmap(src) ?: return null
        val out = preprocessContrastEnhanced(sharpened)
        if (!sharpened.isRecycled) sharpened.recycle()
        return out
    }

    private fun preprocessGrayscaleThreshold(src: Bitmap): Bitmap? {
        val gray = toGrayscaleHighContrast(src, contrastScale = 1.22f, contrastTranslate = -8f)
            ?: return null
        return applyLuminanceThreshold(gray, threshold = 138)
    }

    private fun toGrayscaleHighContrast(
        src: Bitmap,
        contrastScale: Float,
        contrastTranslate: Float,
    ): Bitmap? {
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

    private fun sharpenBitmap(src: Bitmap): Bitmap? {
        val w = src.width
        val h = src.height
        if (w < 3 || h < 3) return null
        return try {
            val inPx = IntArray(w * h)
            src.getPixels(inPx, 0, w, 0, 0, w, h)
            val outPx = inPx.copyOf()
            for (y in 1 until h - 1) {
                for (x in 1 until w - 1) {
                    val idx = y * w + x
                    var sum = luminance(inPx[idx]) * 5
                    sum -= luminance(inPx[idx - 1])
                    sum -= luminance(inPx[idx + 1])
                    sum -= luminance(inPx[idx - w])
                    sum -= luminance(inPx[idx + w])
                    val v = sum.coerceIn(0, 255)
                    outPx[idx] = (0xFF shl 24) or (v shl 16) or (v shl 8) or v
                }
            }
            val result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            result.setPixels(outPx, 0, w, 0, 0, w, h)
            result
        } catch (_: Exception) {
            null
        }
    }

    private fun applyLuminanceThreshold(src: Bitmap, threshold: Int): Bitmap? {
        val w = src.width
        val h = src.height
        if (w < 8 || h < 8) return null
        return try {
            val inPx = IntArray(w * h)
            src.getPixels(inPx, 0, w, 0, 0, w, h)
            val thr = threshold.coerceIn(0, 255)
            for (i in inPx.indices) {
                val v = if (luminance(inPx[i]) >= thr) 255 else 0
                inPx[i] = (0xFF shl 24) or (v shl 16) or (v shl 8) or v
            }
            if (!src.isRecycled) src.recycle()
            val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            out.setPixels(inPx, 0, w, 0, 0, w, h)
            out
        } catch (_: Exception) {
            null
        }
    }

    private fun luminance(pixel: Int): Int {
        val r = (pixel shr 16) and 0xFF
        val g = (pixel shr 8) and 0xFF
        val b = pixel and 0xFF
        return (0.299 * r + 0.587 * g + 0.114 * b).toInt()
    }

    private fun validColumnCellCount(rowMajor: List<Int>): Int {
        val p = pad25(rowMajor)
        var n = 0
        for (i in 0..24) {
            val v = p[i]
            if (v == 0) continue
            val col = i % 5
            val range = BingoNumberAnalyzer.bingoColumnValueRange(col) ?: continue
            if (v in range) n++
        }
        return n
    }

    private fun parseFooterMeta(
        footer: Bitmap,
        recognizer: TextRecognizer,
        fullVision: Text,
        imageH: Int,
    ): Pair<String?, String?> {
        val footerCropText = runCatching {
            val image = InputImage.fromBitmap(footer, 0)
            Tasks.await(recognizer.process(image)).text
        }.getOrDefault("")
        val bottomLines = buildFooterPlainText(fullVision, imageH)
        val combined = listOf(footerCropText, bottomLines).filter { it.isNotBlank() }.joinToString("\n")
        val serie = serieRegex.find(combined)?.groupValues?.getOrNull(1)?.trim()?.takeIf { it.isNotEmpty() }
        val los = losRegex.find(combined)?.groupValues?.getOrNull(1)?.trim()?.takeIf { it.isNotEmpty() }
        return serie to los
    }

    private fun buildFooterPlainText(visionText: Text, imageH: Int): String {
        val yMin = imageH * FOOTER_TOP_FRAC
        val lines = mutableListOf<Pair<Float, String>>()
        for (block in visionText.textBlocks) {
            for (line in block.lines) {
                val box = line.boundingBox ?: continue
                val cy = (box.top + box.bottom) / 2f
                if (cy < yMin) continue
                lines.add(cy to line.text)
            }
        }
        return lines.sortedBy { it.first }.joinToString("\n") { it.second }
    }

    private fun isGreenHeaderPixel(p: Int): Boolean {
        val r = (p ushr 16) and 0xff
        val g = (p ushr 8) and 0xff
        val b = p and 0xff
        return g >= 72 && g > r + 10 && g >= b + 4
    }

    private fun detectGreenBingoHeaderBottomY(bitmap: Bitmap): Int? {
        val w = bitmap.width
        val h = bitmap.height
        if (w < 64 || h < 64) return null
        val yLimit = (h * 0.35f).toInt().coerceAtLeast(8)
        val stepY = max(1, h / 90)
        val stepX = max(1, w / 100)
        data class SampleRow(val y: Int, val frac: Float)
        val rows = mutableListOf<SampleRow>()
        var y = 0
        while (y < yLimit) {
            var n = 0
            var green = 0
            var x = 0
            while (x < w) {
                if (isGreenHeaderPixel(bitmap.getPixel(x, y))) green++
                n++
                x += stepX
            }
            rows.add(SampleRow(y, if (n > 0) green.toFloat() / n else 0f))
            y += stepY
        }
        if (rows.isEmpty()) return null
        var bestScore = -1f
        var bestStart = -1
        var bestEnd = -1
        var i = 0
        while (i < rows.size) {
            if (rows[i].frac < 0.11f) {
                i++
                continue
            }
            var sum = 0f
            val j0 = i
            while (i < rows.size && rows[i].frac >= 0.08f) {
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
        if (bestStart < 0 || bestScore < 0.32f) return null
        if (rows[bestStart].y > h * 0.28f) return null
        val yBottom = rows[bestEnd].y + stepY + (h * 0.012f).toInt().coerceAtLeast(2)
        return yBottom.coerceIn(h / 24, h - h / 6)
    }

    private data class CellGridResult(val rowMajor: List<Int>)

    private fun cellGridFromRegion(
        bitmap: Bitmap,
        recognizer: TextRecognizer,
        gx0: Int,
        gx1: Int,
        gy0: Int,
        gy1: Int,
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
        if (rowMajor.count { it != 0 } < CELL_GRID_MIN_FILLED) return null
        return CellGridResult(rowMajor)
    }

    private fun ocrSingleCellValue(cellBmp: Bitmap, col: Int, recognizer: TextRecognizer): Int {
        val range = BingoNumberAnalyzer.bingoColumnValueRange(col) ?: return 0
        return runCatching {
            val image = InputImage.fromBitmap(cellBmp, 0)
            val visionText = Tasks.await(recognizer.process(image))
            pickCellDigitCandidate(visionText.text, range)
        }.getOrDefault(0)
    }

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

    private fun pad25(rowMajor: List<Int>): List<Int> {
        val t = rowMajor.take(25)
        return if (t.size < 25) t + List(25 - t.size) { 0 } else t
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
