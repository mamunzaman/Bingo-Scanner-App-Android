package com.example.mamunbingoapp.scanner

import android.content.Context
import android.util.Log
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.net.Uri
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
 * OCR for [com.example.mamunbingoapp.domain.model.BingoScanType.MAIN_SHEET] only.
 *
 * Handles printed Master Bingo tickets: black-bordered 5×5 table, BINGO column header,
 * Serie / Los-Nr. footer. Ignores Super 6 / Spiel 77 noise below footer.
 */
object MainSheetBingoOcr {

    /** Minimum valid cells (correct column range) to open preview/manual-correction UI. */
    const val MIN_VALID_CELLS_FOR_SUCCESS = 15

    private const val CELL_GRID_MIN_FILLED = 12
    private const val POSITION_FALLBACK_MIN_VALID = MIN_VALID_CELLS_FOR_SUCCESS

    /** Grid top inset (fraction of height) to skip any header text/border. */
    private const val GRID_TOP_INSET_FRAC = 0.02f
    private const val GRID_SIDE_INSET_FRAC = 0.03f
    /** Fallback grid bottom when row layout cannot be estimated. */
    private const val GRID_BOTTOM_FRAC = 0.88f
    /** Footer crop starts here for dedicated footer OCR pass. */
    private const val FOOTER_TOP_FRAC = 0.72f

    private const val META_LOG_TAG = "MainSheetMetaOcr"

    private val footerLabelPattern = Regex(
        "(?i)serie|seriennummer|serien[\\s\\-]*nr|seriennr|los(?:nummer|[\\s\\-]*nr\\.?)?|los\\s*nr|" +
            "seriennummer[\\s\\-]*losnummer",
    )
    private val serieAfterLabelRegex = Regex(
        "(?i)(?:serie|seriennummer|serien[\\s\\-]*nr\\.?|seriennr)" +
            "(?:\\s*[:#\\-.=]+|\\s+)([0-9]{4,8})",
    )
    private val losAfterLabelRegex = Regex(
        "(?i)(?:los(?:nummer)?(?:[\\s\\-]*nr\\.?)?|los\\s*nr)" +
            "(?:\\s*[:#\\-.=]+|\\s+)([0-9]{5,8})",
    )
    private val starSerieLosPairRegex = Regex(
        """\*\*\s*([0-9]{4,6})\s*\*\*\s*([0-9]{5,8})\s*\*\*""",
    )
    private val topComboHeaderRegex = Regex("(?i)seriennummer[\\s\\-]*losnummer")
    private val spatialFourFiveRegex = Regex("(?<![0-9])([0-9]{4})(?![0-9])\\D{0,24}([0-9]{5})(?![0-9])")
    /** Super 6 / Spiel 77 noise lines that appear below the footer on printed tickets. */
    private val noiseLinePattern = Regex("(?i)super\\s*6|spiel\\s*77")
    private val gridNumberPattern = Regex("(?<![0-9])([1-9]|[1-6][0-9]|7[0-5])(?![0-9])")

    fun analyzeUri(
        context: Context,
        uri: Uri,
        @Suppress("UNUSED_PARAMETER") bypassInternalGridCrop: Boolean = false,
        @Suppress("UNUSED_PARAMETER") preCropCameraForStripOcr: Boolean = false,
    ): HistoryImportOcrOutcome {
        val bitmap = loadBitmapDownsampled(context, uri, maxSide = 1600)
            ?: error("Could not load image")
        return try {
            analyzeBitmapWithPreprocessRetries(bitmap)
        } finally {
            if (!bitmap.isRecycled) bitmap.recycle()
        }
    }

    // ── Preprocessing retry loop ─────────────────────────────────────────────

    private data class MainSheetOcrAttempt(
        val variantName: String,
        val outcome: HistoryImportOcrOutcome,
        val filledCount: Int,
        val validCount: Int,
    )

    private fun analyzeBitmapWithPreprocessRetries(source: Bitmap): HistoryImportOcrOutcome {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        return try {
            val attempts = mutableListOf<MainSheetOcrAttempt>()
            for ((variantName, variantBitmap) in buildPreprocessVariants(source)) {
                val attempt = runCatching {
                    analyzeBitmapOnce(variantBitmap, recognizer, variantName)
                }.getOrElse { null }
                if (variantBitmap !== source && !variantBitmap.isRecycled) variantBitmap.recycle()
                if (attempt != null) {
                    attempts.add(attempt)
                }
            }
            val best = attempts.maxWithOrNull(
                compareBy<MainSheetOcrAttempt>({ it.validCount }, { it.filledCount }),
            )
            best?.outcome ?: HistoryImportOcrOutcome(numbersRowMajor = pad25(emptyList()))
        } finally {
            recognizer.close()
        }
    }

    // ── Single variant OCR ────────────────────────────────────────────────────

    private fun analyzeBitmapOnce(
        bitmap: Bitmap,
        recognizer: TextRecognizer,
        variantName: String,
    ): MainSheetOcrAttempt {
        val visionText = ocrFullImage(bitmap, recognizer)
            ?: return MainSheetOcrAttempt(variantName, HistoryImportOcrOutcome(pad25(emptyList())), 0, 0)

        val headerBottomY = detectBingoTextHeaderBottomY(bitmap)
        val visionLines = collectVisionLines(visionText, bitmap.height)
        val rawCandidates = collectNumericCandidates(visionText)
        val gridTopY = estimateMetaGridTopY(rawCandidates, headerBottomY, bitmap.height)

        val footerTop = (bitmap.height * FOOTER_TOP_FRAC).toInt().coerceIn(0, bitmap.height - 4)
        val footerBmp = Bitmap.createBitmap(bitmap, 0, footerTop, bitmap.width, bitmap.height - footerTop)
        val (serie, los) = try {
            parseMasterSheetMeta(
                footerBmp = footerBmp,
                recognizer = recognizer,
                visionLines = visionLines,
                imageH = bitmap.height,
                gridTopY = gridTopY,
            )
        } finally {
            if (!footerBmp.isRecycled) footerBmp.recycle()
        }
        val preGridCandidates = filterPreGridCandidates(rawCandidates, excludeSerie = serie, excludeLos = los)
        val gridLayout = computeGridRowLayout(preGridCandidates, bitmap.height)

        val cellRowMajor = tryCellGrid(bitmap, recognizer, gridLayout)
        val positionRowMajor = tryPositionGridFromVisionText(preGridCandidates, gridLayout, bitmap.width, bitmap.height)

        val cellPadded = pad25(cellRowMajor)
        val positionPadded = pad25(positionRowMajor)
        val cellFilled = filledCellCount(cellPadded)
        val positionFilled = filledCellCount(positionPadded)
        val rowMajor = when {
            positionFilled >= POSITION_FALLBACK_MIN_VALID && positionFilled >= cellFilled -> positionPadded
            cellFilled >= CELL_GRID_MIN_FILLED && cellFilled >= positionFilled -> cellPadded
            positionFilled >= POSITION_FALLBACK_MIN_VALID -> positionPadded
            cellFilled >= CELL_GRID_MIN_FILLED -> cellPadded
            positionFilled > cellFilled -> positionPadded
            else -> if (positionFilled > 0) positionPadded else cellPadded
        }
        val finalFilled = filledCellCount(rowMajor)
        val finalValid = validColumnCellCount(rowMajor)

        val outcome = HistoryImportOcrOutcome(
            numbersRowMajor = rowMajor,
            losNumber = los,
            serialNumber = serie,
        )
        return MainSheetOcrAttempt(variantName, outcome, finalFilled, finalValid)
    }

    // ── Preprocessing variants ────────────────────────────────────────────────

    private fun buildPreprocessVariants(source: Bitmap): List<Pair<String, Bitmap>> {
        val variants = mutableListOf<Pair<String, Bitmap>>()
        variants.add("original" to source)
        preprocessContrastEnhanced(source)?.let { variants.add("contrast-enhanced" to it) }
        preprocessSharpenedContrast(source)?.let { variants.add("sharpened+contrast" to it) }
        preprocessGrayscaleThreshold(source)?.let { variants.add("grayscale-threshold" to it) }
        return variants
    }

    // ── Grid layout detection ─────────────────────────────────────────────────

    private data class GridRowLayout(
        val rowCenters: List<Float>,
        val avgRowGap: Float,
        val expectedFifthRowY: Float?,
        val gridBottomY: Float,
        val footerCutoffY: Float,
    )

    private fun computeGridRowLayout(candidates: List<PositionCandidate>, imageH: Int): GridRowLayout {
        val fallbackBottom = imageH * GRID_BOTTOM_FRAC
        val fallbackCutoff = imageH * 0.94f
        if (candidates.isEmpty()) {
            return GridRowLayout(emptyList(), imageH * 0.05f, null, fallbackBottom, fallbackCutoff)
        }
        val rawClusters = clusterCandidatesIntoRowsRaw(candidates, imageH)
        val rowCenters = rawClusters.map { row -> row.map { it.cy }.average().toFloat() }.sorted()
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
            max(gridBottomY + avgGap * 0.42f, (expectedFifthRowY ?: gridBottomY) + avgGap * 0.78f),
        )
        return GridRowLayout(rowCenters, avgGap, expectedFifthRowY, gridBottomY, footerCutoffY)
    }

    // ── Cell-grid OCR path ────────────────────────────────────────────────────

    private fun tryCellGrid(bitmap: Bitmap, recognizer: TextRecognizer, layout: GridRowLayout): List<Int> {
        val headerBottom = detectBingoTextHeaderBottomY(bitmap)
        val gridTop = (headerBottom ?: (bitmap.height * 0.12f).toInt()).coerceIn(1, bitmap.height - 40)
        val layoutBottom = layout.gridBottomY.toInt()
        val fracBottom = (bitmap.height * GRID_BOTTOM_FRAC).toInt()
        val gridBottom = max(layoutBottom, fracBottom).coerceIn(gridTop + 40, bitmap.height - 8)
        val sideInset = (bitmap.width * GRID_SIDE_INSET_FRAC).toInt()
        val gx0 = sideInset.coerceIn(0, bitmap.width - 20)
        val gx1 = (bitmap.width - sideInset).coerceIn(gx0 + 20, bitmap.width)
        val gy0 = (gridTop + bitmap.height * GRID_TOP_INSET_FRAC).toInt().coerceIn(gridTop, gridBottom - 20)
        val gy1 = gridBottom

        val gridBmp = Bitmap.createBitmap(bitmap, gx0, gy0, gx1 - gx0, gy1 - gy0)
        return try {
            cellGridFromRegion(gridBmp, recognizer, 0, gridBmp.width, 0, gridBmp.height)?.rowMajor ?: emptyList()
        } finally {
            if (!gridBmp.isRecycled) gridBmp.recycle()
        }
    }

    /**
     * Detect bottom Y of the BINGO text header for printed tickets.
     * Looks for a text block in the top 30 % of the image that matches "BINGO".
     * Falls back to null so callers use a fixed fraction.
     */
    private fun detectBingoTextHeaderBottomY(bitmap: Bitmap): Int? {
        if (bitmap.width < 64 || bitmap.height < 64) return null
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        return try {
            val yLimit = (bitmap.height * 0.30f).toInt().coerceAtLeast(8)
            val headerBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, yLimit)
            val visionText = runCatching {
                val img = InputImage.fromBitmap(headerBmp, 0)
                Tasks.await(recognizer.process(img))
            }.getOrNull()
            if (!headerBmp.isRecycled) headerBmp.recycle()
            visionText?.textBlocks
                ?.filter { block -> block.text.uppercase().contains("BINGO") }
                ?.mapNotNull { it.boundingBox?.bottom }
                ?.maxOrNull()
        } finally {
            recognizer.close()
        }
    }

    // ── Position-based fallback path ──────────────────────────────────────────

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
    ): List<PositionCandidate> = raw.filter { c ->
        if (c.onFooterLabelLine) return@filter false
        val s = c.value.toString()
        if (excludeSerie != null && (s == excludeSerie || excludeSerie.endsWith(s))) return@filter false
        if (excludeLos != null && (s == excludeLos || excludeLos.endsWith(s))) return@filter false
        true
    }

    private fun filterGridNumberCandidates(
        candidates: List<PositionCandidate>,
        layout: GridRowLayout,
    ): List<PositionCandidate> {
        val tolerance = layout.avgRowGap * 0.72f
        return candidates.filter { c ->
            if (c.cy < layout.footerCutoffY) return@filter true
            val fifthY = layout.expectedFifthRowY ?: return@filter false
            abs(c.cy - fifthY) <= tolerance
        }
    }

    private fun tryPositionGridFromVisionText(
        preGridCandidates: List<PositionCandidate>,
        gridLayout: GridRowLayout,
        imageW: Int,
        imageH: Int,
    ): List<Int> {
        val filtered = filterGridNumberCandidates(preGridCandidates, gridLayout)
        val rowClusters = clusterCandidatesIntoRows(filtered, imageH)
        val grid = buildRowMajorFromRowClusters(rowClusters, imageW, gridLayout) ?: return emptyList()
        val padded = pad25(grid)
        if (filledCellCount(padded) < POSITION_FALLBACK_MIN_VALID) return emptyList()
        return padded
    }

    // ── Candidate collection ──────────────────────────────────────────────────

    private fun collectNumericCandidates(visionText: Text): List<PositionCandidate> {
        val out = mutableListOf<PositionCandidate>()
        val seen = mutableSetOf<String>()
        for (block in visionText.textBlocks) {
            for (line in block.lines) {
                // Skip Super 6 / Spiel 77 noise lines
                if (noiseLinePattern.containsMatchIn(line.text)) continue
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

    // ── Row clustering ────────────────────────────────────────────────────────

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
        if (nonEmpty.sumOf { it.size } < POSITION_FALLBACK_MIN_VALID) return emptyList()
        val substantial = nonEmpty.filter { it.size >= 2 }.ifEmpty { nonEmpty }
        return when {
            substantial.size in 2..5 -> substantial
            substantial.size > 5 -> pickBestRowWindow(substantial, windowSize = 5)
            else -> substantial
        }
    }

    private fun pickBestRowWindow(rows: List<List<PositionCandidate>>, windowSize: Int): List<List<PositionCandidate>> {
        if (rows.size <= windowSize) return rows
        var bestStart = 0
        var bestScore = -1
        for (start in 0..rows.size - windowSize) {
            val score = rows.subList(start, start + windowSize).sumOf { it.size }
            if (score > bestScore) { bestScore = score; bestStart = start }
        }
        return rows.subList(bestStart, bestStart + windowSize)
    }

    // ── Grid assembly ─────────────────────────────────────────────────────────

    private fun buildRowMajorFromRowClusters(
        rowClusters: List<List<PositionCandidate>>,
        imageW: Int,
        layout: GridRowLayout,
    ): List<Int>? {
        if (rowClusters.isEmpty() || rowClusters.size > 5) return null
        val all = rowClusters.flatten()
        if (all.size < POSITION_FALLBACK_MIN_VALID) return null

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
            for (item in row.sortedBy { it.cx }) {
                var col = ((item.cx - minX) / colWidth).toInt().coerceIn(0, 4)
                findBestColumnForValue(item.value, col)?.let { col = it }
                placeValueInRow(rowMajor, targetRow, col, item.value)
            }
        }
        return rowMajor.toList()
    }

    private fun rowSlotYs(layout: GridRowLayout): List<Float> {
        val gap = layout.avgRowGap
        return when {
            layout.rowCenters.size >= 5 -> layout.rowCenters.take(5)
            layout.rowCenters.isNotEmpty() -> List(5) { i ->
                when {
                    i < layout.rowCenters.size -> layout.rowCenters[i]
                    i == 4 && layout.expectedFifthRowY != null -> layout.expectedFifthRowY
                    else -> layout.rowCenters.first() + gap * i
                }
            }
            layout.expectedFifthRowY != null -> List(5) { i -> layout.expectedFifthRowY - gap * (4 - i) }
            else -> List(5) { i -> layout.gridBottomY - gap * (4.5f - i) }
        }
    }

    private fun placeValueInRow(rowMajor: IntArray, targetRow: Int, preferredCol: Int, value: Int) {
        if (targetRow !in 0..4) return
        val col = preferredCol.coerceIn(0, 4)
        val primary = targetRow * 5 + col
        if (rowMajor[primary] == 0) { rowMajor[primary] = value; return }
        if (rowMajor[primary] == value) return
        for (d in 1..4) {
            val left = col - d
            if (left >= 0) { val idx = targetRow * 5 + left; if (rowMajor[idx] == 0) { rowMajor[idx] = value; return } }
            val right = col + d
            if (right <= 4) { val idx = targetRow * 5 + right; if (rowMajor[idx] == 0) { rowMajor[idx] = value; return } }
        }
    }

    private fun findBestColumnForValue(value: Int, preferredCol: Int): Int? {
        var bestCol: Int? = null
        var bestDist = Float.MAX_VALUE
        for (c in 0..4) {
            val range = BingoNumberAnalyzer.bingoColumnValueRange(c) ?: continue
            if (value !in range) continue
            val dist = abs(c - preferredCol).toFloat()
            if (dist < bestDist) { bestDist = dist; bestCol = c }
        }
        return bestCol
    }

    // ── Cell-grid per-cell OCR ────────────────────────────────────────────────

    private data class CellGridResult(val rowMajor: List<Int>)

    private fun cellGridFromRegion(
        bitmap: Bitmap,
        recognizer: TextRecognizer,
        gx0: Int, gx1: Int, gy0: Int, gy1: Int,
    ): CellGridResult? {
        val w = bitmap.width
        val h = bitmap.height
        if (w < 50 || h < 50) return null
        val gxo = gx0.coerceIn(0, w - 2)
        val gx1c = gx1.coerceIn(gxo + 4, w)
        val gyo = gy0.coerceIn(0, h - 2)
        val gy1c = gy1.coerceIn(gyo + 4, h)
        val cellWf = (gx1c - gxo) / 5f
        val cellHf = (gy1c - gyo) / 5f
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
                } catch (_: Exception) { null } ?: continue
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
            val visionText = Tasks.await(recognizer.process(InputImage.fromBitmap(cellBmp, 0)))
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

    // ── Master Sheet metadata (Serie / Los-Nr.) ─────────────────────────────

    private data class VisionLine(val text: String, val cy: Float)

    private data class LabeledMetaExtract(
        val serie: String?,
        val los: String?,
        val layoutType: String,
    )

    private fun collectVisionLines(visionText: Text, imageH: Int): List<VisionLine> {
        val out = mutableListOf<VisionLine>()
        for (block in visionText.textBlocks) {
            for (line in block.lines) {
                if (noiseLinePattern.containsMatchIn(line.text)) continue
                val box = line.boundingBox ?: continue
                val cy = (box.top + box.bottom) / 2f
                if (cy < 0f || cy > imageH) continue
                out.add(VisionLine(line.text, cy))
            }
        }
        return out.sortedBy { it.cy }
    }

    private fun estimateMetaGridTopY(
        candidates: List<PositionCandidate>,
        headerBottomY: Int?,
        imageH: Int,
    ): Float {
        val gridNums = candidates.filter { !it.onFooterLabelLine && it.value in 1..75 }
        val minGridCy = gridNums.minOfOrNull { it.cy }
        return when {
            minGridCy != null && minGridCy > (headerBottomY?.toFloat() ?: imageH * 0.08f) ->
                minGridCy.coerceAtMost(imageH * 0.58f)
            headerBottomY != null -> (headerBottomY + imageH * 0.04f).toFloat().coerceAtMost(imageH * 0.55f)
            else -> imageH * 0.45f
        }
    }

    private fun parseMasterSheetMeta(
        footerBmp: Bitmap,
        recognizer: TextRecognizer,
        visionLines: List<VisionLine>,
        imageH: Int,
        gridTopY: Float,
    ): Pair<String?, String?> {
        val footerCropText = runCatching {
            Tasks.await(recognizer.process(InputImage.fromBitmap(footerBmp, 0))).text
        }.getOrDefault("")
        val footerYMin = imageH * FOOTER_TOP_FRAC
        val bottomRegionText = regionPlainText(visionLines, yMin = footerYMin)
        val topRegionText = regionPlainText(visionLines, yMax = gridTopY)

        val footerCombined = normalizeMetaText(
            listOf(footerCropText, bottomRegionText).filter { it.isNotBlank() }.joinToString("\n"),
        )
        val topNormalized = normalizeMetaText(topRegionText)

        var serie: String? = null
        var los: String? = null
        var layoutType = "none"
        var debugText = footerCombined

        fun applyExtract(extract: LabeledMetaExtract, sourceText: String) {
            if (extract.serie != null) serie = extract.serie
            if (extract.los != null) los = extract.los
            if (layoutType == "none" && (extract.serie != null || extract.los != null)) {
                layoutType = extract.layoutType
                debugText = sourceText
            }
        }

        applyExtract(extractLabeledSerieLos(footerCombined, visionLines, footerYMin), footerCombined)

        if (serie == null || los == null) {
            val bottomOnly = extractLabeledSerieLos(bottomRegionText, visionLines, footerYMin)
            applyExtract(bottomOnly, bottomRegionText)
            if (layoutType == "none" && bottomOnly.layoutType != "none") layoutType = bottomOnly.layoutType
        }

        if (serie == null || los == null) {
            val fullExtract = extractLabeledSerieLosFromLines(visionLines, yMin = footerYMin)
            if (serie == null) serie = fullExtract.serie
            if (los == null) los = fullExtract.los
            if (layoutType == "none" && (fullExtract.serie != null || fullExtract.los != null)) {
                layoutType = fullExtract.layoutType
                debugText = bottomRegionText
            }
        }

        if (serie == null || los == null) {
            val star = extractStarTopPair(topNormalized, visionLines, gridTopY)
            if (star != null) {
                if (serie == null) serie = star.first
                if (los == null) los = star.second
                layoutType = "star_top_spatial"
                debugText = topNormalized
            }
        }

        if (serie == null || los == null) {
            val spatial = extractSpatialTopPair(visionLines, gridTopY, imageH)
            if (spatial != null) {
                if (serie == null) serie = spatial.first
                if (los == null) los = spatial.second
                if (layoutType == "none") layoutType = "spatial_top_4_5"
                debugText = topNormalized
            }
        }

        if (serie == null || los == null) {
            val fallback = extractLabeledSerieLos(footerCombined, visionLines, footerYMin, relaxedLengths = true)
            if (serie == null) serie = fallback.serie
            if (los == null) los = fallback.los
            if (layoutType == "none" && (fallback.serie != null || fallback.los != null)) {
                layoutType = "footer_fallback"
                debugText = footerCombined
            }
        }

        Log.d(
            META_LOG_TAG,
            "layout=$layoutType serie=$serie los=$los gridTopY=${gridTopY.toInt()} " +
                "text=${debugText.take(280)}",
        )
        return serie to los
    }

    private fun regionPlainText(lines: List<VisionLine>, yMin: Float? = null, yMax: Float? = null): String =
        lines.filter { line ->
            (yMin == null || line.cy >= yMin) && (yMax == null || line.cy <= yMax)
        }.joinToString("\n") { it.text }

    private fun normalizeMetaText(raw: String): String =
        raw.replace(Regex("[\\u00A0\\u2007\\u202F]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()

    private fun lineLooksLikeBingoGrid(text: String): Boolean {
        val count = gridNumberPattern.findAll(text).count { m ->
            m.groupValues[1].toIntOrNull()?.let { it in 1..75 } == true
        }
        return count >= 5
    }

    private fun isPlausibleSerie(digits: String): Boolean =
        digits.length in 4..6 && digits.toIntOrNull()?.let { it !in 1..75 } == true

    private fun isPlausibleLos(digits: String): Boolean =
        digits.length in 5..8 && digits.toIntOrNull()?.let { it !in 1..75 } == true

    private fun pickBestSerie(candidates: List<String>): String? =
        candidates.filter { isPlausibleSerie(it) }
            .maxByOrNull { it.length.coerceAtMost(6) * 10 + if (it.length == 4) 2 else 1 }

    private fun pickBestLos(candidates: List<String>): String? =
        candidates.filter { isPlausibleLos(it) }
            .maxByOrNull { if (it.length == 5) 10 else it.length }

    private fun extractLabeledSerieLos(
        normalizedText: String,
        visionLines: List<VisionLine>,
        footerYMin: Float,
        relaxedLengths: Boolean = false,
    ): LabeledMetaExtract {
        if (normalizedText.isBlank()) return LabeledMetaExtract(null, null, "none")

        val serieCandidates = mutableListOf<String>()
        val losCandidates = mutableListOf<String>()
        var sameLineBoth = false
        var splitLines = false

        for (match in serieAfterLabelRegex.findAll(normalizedText)) {
            match.groupValues.getOrNull(1)?.trim()?.takeIf { it.isNotEmpty() }?.let { serieCandidates.add(it) }
        }
        for (match in losAfterLabelRegex.findAll(normalizedText)) {
            match.groupValues.getOrNull(1)?.trim()?.takeIf { it.isNotEmpty() }?.let { losCandidates.add(it) }
        }

        val footerLines = visionLines.filter { it.cy >= footerYMin && !lineLooksLikeBingoGrid(it.text) }
        for (line in footerLines) {
            val normLine = normalizeMetaText(line.text)
            if (lineLooksLikeBingoGrid(normLine)) continue
            val s = serieAfterLabelRegex.find(normLine)?.groupValues?.getOrNull(1)?.trim()
            val l = losAfterLabelRegex.find(normLine)?.groupValues?.getOrNull(1)?.trim()
            if (s != null) serieCandidates.add(s)
            if (l != null) losCandidates.add(l)
            if (s != null && l != null) sameLineBoth = true
            if ((s != null) xor (l != null)) splitLines = true
        }

        val serie = pickBestSerie(serieCandidates)
            ?: if (relaxedLengths) serieCandidates.firstOrNull { it.length in 3..8 } else null
        val los = pickBestLos(losCandidates)
            ?: if (relaxedLengths) losCandidates.firstOrNull { it.length in 4..8 } else null

        val layoutType = when {
            sameLineBoth -> "footer_same_line"
            splitLines && serie != null && los != null -> "footer_split"
            serie != null || los != null -> "footer_labeled"
            else -> "none"
        }
        return LabeledMetaExtract(serie, los, layoutType)
    }

    private fun extractLabeledSerieLosFromLines(
        visionLines: List<VisionLine>,
        yMin: Float,
    ): LabeledMetaExtract {
        var serie: String? = null
        var los: String? = null
        var split = false
        var sameLine = false
        for (line in visionLines) {
            if (line.cy < yMin) continue
            if (lineLooksLikeBingoGrid(line.text)) continue
            val norm = normalizeMetaText(line.text)
            val s = serieAfterLabelRegex.find(norm)?.groupValues?.getOrNull(1)?.trim()
            val l = losAfterLabelRegex.find(norm)?.groupValues?.getOrNull(1)?.trim()
            if (s != null && isPlausibleSerie(s)) {
                serie = s
            }
            if (l != null && isPlausibleLos(l)) {
                los = l
            }
            if (s != null && l != null) sameLine = true
            if ((s != null) xor (l != null)) split = true
        }
        val layoutType = when {
            sameLine -> "footer_same_line"
            split && serie != null && los != null -> "footer_split"
            serie != null || los != null -> "bottom_labeled"
            else -> "none"
        }
        return LabeledMetaExtract(serie, los, layoutType)
    }

    private fun extractStarTopPair(
        topText: String,
        visionLines: List<VisionLine>,
        gridTopY: Float,
    ): Pair<String, String>? {
        starSerieLosPairRegex.find(topText)?.let { m ->
            val s = m.groupValues.getOrNull(1)
            val l = m.groupValues.getOrNull(2)
            if (s != null && l != null && isPlausibleSerie(s) && isPlausibleLos(l)) return s to l
        }
        for (line in visionLines) {
            if (line.cy > gridTopY) continue
            if (lineLooksLikeBingoGrid(line.text)) continue
            val norm = normalizeMetaText(line.text)
            if (!topComboHeaderRegex.containsMatchIn(norm) && !norm.contains("**")) continue
            starSerieLosPairRegex.find(norm)?.let { m ->
                val s = m.groupValues.getOrNull(1)
                val l = m.groupValues.getOrNull(2)
                if (s != null && l != null && isPlausibleSerie(s) && isPlausibleLos(l)) return s to l
            }
            spatialFourFiveRegex.find(norm)?.let { m ->
                val s = m.groupValues.getOrNull(1)
                val l = m.groupValues.getOrNull(2)
                if (s != null && l != null && isPlausibleSerie(s) && isPlausibleLos(l)) return s to l
            }
        }
        return null
    }

    private fun extractSpatialTopPair(
        visionLines: List<VisionLine>,
        gridTopY: Float,
        imageH: Int,
    ): Pair<String, String>? {
        val upperBound = min(gridTopY, imageH * 0.52f)
        for (line in visionLines) {
            if (line.cy > upperBound) continue
            if (lineLooksLikeBingoGrid(line.text)) continue
            if (footerLabelPattern.containsMatchIn(line.text) &&
                !topComboHeaderRegex.containsMatchIn(line.text)
            ) {
                continue
            }
            val norm = normalizeMetaText(line.text)
            if (lineLooksLikeBingoGrid(norm)) continue
            spatialFourFiveRegex.find(norm)?.let { m ->
                val s = m.groupValues.getOrNull(1)
                val l = m.groupValues.getOrNull(2)
                if (s != null && l != null && isPlausibleSerie(s) && isPlausibleLos(l)) return s to l
            }
        }
        return null
    }

    // ── Validity helpers ──────────────────────────────────────────────────────

    private fun filledCellCount(rowMajor: List<Int>): Int = pad25(rowMajor).count { it != 0 }

    private fun validColumnCellCount(rowMajor: List<Int>): Int {
        val p = pad25(rowMajor)
        var n = 0
        for (i in 0..24) {
            val v = p[i]; if (v == 0) continue
            val range = BingoNumberAnalyzer.bingoColumnValueRange(i % 5) ?: continue
            if (v in range) n++
        }
        return n
    }

    private fun pad25(rowMajor: List<Int>): List<Int> {
        val t = rowMajor.take(25)
        return if (t.size < 25) t + List(25 - t.size) { 0 } else t
    }

    // ── Image loading & preprocessing ─────────────────────────────────────────

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

    private fun ocrFullImage(bitmap: Bitmap, recognizer: TextRecognizer): Text? =
        runCatching {
            Tasks.await(recognizer.process(InputImage.fromBitmap(bitmap, 0)))
        }.getOrNull()

    private fun preprocessContrastEnhanced(src: Bitmap): Bitmap? =
        toGrayscaleHighContrast(src, contrastScale = 1.55f, contrastTranslate = -28f)

    private fun preprocessSharpenedContrast(src: Bitmap): Bitmap? {
        val sharpened = sharpenBitmap(src) ?: return null
        val out = preprocessContrastEnhanced(sharpened)
        if (!sharpened.isRecycled) sharpened.recycle()
        return out
    }

    private fun preprocessGrayscaleThreshold(src: Bitmap): Bitmap? {
        val gray = toGrayscaleHighContrast(src, contrastScale = 1.28f, contrastTranslate = -10f) ?: return null
        return applyLuminanceThreshold(gray, threshold = 148)
    }

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

    private fun sharpenBitmap(src: Bitmap): Bitmap? {
        val w = src.width; val h = src.height
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
        } catch (_: Exception) { null }
    }

    private fun applyLuminanceThreshold(src: Bitmap, threshold: Int): Bitmap? {
        val w = src.width; val h = src.height
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
        } catch (_: Exception) { null }
    }

    private fun luminance(pixel: Int): Int {
        val r = (pixel shr 16) and 0xFF
        val g = (pixel shr 8) and 0xFF
        val b = pixel and 0xFF
        return (0.299 * r + 0.587 * g + 0.114 * b).toInt()
    }
}
