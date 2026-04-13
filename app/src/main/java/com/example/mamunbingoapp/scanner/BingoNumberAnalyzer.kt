package com.example.mamunbingoapp.scanner

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

data class OcrItem(val value: Int, val centerX: Float, val centerY: Float)

data class TicketDetectionCandidate(
    val left: Float, val top: Float, val right: Float, val bottom: Float,
    val imageW: Int, val imageH: Int,
    val confidence: Float,
    val matchedBlocksCount: Int
)

data class ScannedBingoCell(val row: Int, val col: Int, val value: Int)

data class ScannedBingoGrid(
    val cells: List<ScannedBingoCell>,
    val rawText: String,
    val isValid: Boolean = true,
    val errorMessage: String? = null,
    val orientationDegrees: Int = 0,
    val isPartial: Boolean = false
) {
    fun toColumnMajorList(): List<Int> =
        (0..4).flatMap { col -> (0..4).map { row -> cellValue(row, col) } }

    fun toCanonicalColumnMajorList(): List<Int> =
        (0..4).flatMap { col -> (0..4).map { row -> canonicalCellValue(row, col) } }

    private fun cellValue(row: Int, col: Int): Int =
        cells.find { it.row == row && it.col == col }?.value ?: 0

    private fun canonicalCellValue(row: Int, col: Int): Int {
        val sourceRow: Int
        val sourceCol: Int
        when (orientationDegrees) {
            90 -> {
                sourceRow = 4 - col
                sourceCol = row
            }
            180 -> {
                sourceRow = 4 - row
                sourceCol = 4 - col
            }
            270 -> {
                sourceRow = col
                sourceCol = 4 - row
            }
            else -> {
                sourceRow = row
                sourceCol = col
            }
        }
        return cellValue(sourceRow, sourceCol)
    }
}

data class FinalParseVariant(val score: Int, val rowDirection: String, val grid: ScannedBingoGrid, val flipX: Boolean = false, val flipY: Boolean = false)

data class OcrCandidateNumbers(
    val flat: List<Int>,
    val b: List<Int>,
    val i: List<Int>,
    val n: List<Int>,
    val g: List<Int>,
    val o: List<Int>,
)

object BingoNumberAnalyzer {
    const val ROW_DIR_TOP_TO_BOTTOM = "TOP_TO_BOTTOM"
    const val ROW_DIR_BOTTOM_TO_TOP = "BOTTOM_TO_TOP"
    private const val MIN = 1
    private const val MAX = 75
    private val pattern = Regex("(?i)(?:[BINGO][\\s\\-:]?)?([1-9]|[1-6][0-9]|7[0-5])(?!\\d)")

    private val B_RANGE = 1..15
    private val I_RANGE = 16..30
    private val N_RANGE = 31..45
    private val G_RANGE = 46..60
    private val O_RANGE = 61..75
    private val COLUMN_RANGES = listOf(B_RANGE, I_RANGE, N_RANGE, G_RANGE, O_RANGE)

    /** B–I–N–G–O column value range for [col] in 0..4 (per-cell OCR validation). */
    fun bingoColumnValueRange(col: Int): IntRange? = COLUMN_RANGES.getOrNull(col)

    fun gridSignature(grid: ScannedBingoGrid): String =
        grid.toColumnMajorList().joinToString(",")

    fun gridSignatureStable(grid: ScannedBingoGrid): String =
        grid.toColumnMajorList().sorted().joinToString(",")

    private const val TOLERANT_MIN_CELLS_MATCH = 22
    private const val STABLE_SIGNATURE_MIN_OVERLAP = 20

    fun stableSignaturesMatch(sigStableA: String, sigStableB: String, minOverlap: Int = STABLE_SIGNATURE_MIN_OVERLAP): Boolean {
        val a = sigStableA.split(",").mapNotNull { it.trim().toIntOrNull() }
        val b = sigStableB.split(",").mapNotNull { it.trim().toIntOrNull() }
        if (a.size != 25 || b.size != 25) return false
        val common = a.intersect(b.toSet()).size
        return common >= minOverlap
    }

    fun signatureCellsMatch(sigA: String, sigB: String, minMatch: Int = TOLERANT_MIN_CELLS_MATCH): Boolean {
        val a = sigA.split(",").map { it.trim() }
        val b = sigB.split(",").map { it.trim() }
        if (a.size != 25 || b.size != 25) return false
        val match = a.zip(b).count { (x, y) -> x == y }
        return match >= minMatch
    }

    private const val MIN_TICKET_ITEMS = 9
    private const val MIN_GRID_CLUSTER_ITEMS = 18
    private const val MIN_CONFIDENCE = 0.28f
    private const val MIN_CONFIDENCE_BROAD = 0.32f
    private const val OUTLIER_IQR_FACTOR = 1.5f
    private const val MIN_ASPECT = 0.2f
    private const val MAX_ASPECT = 4.5f
    private const val MIN_GRID_FILL_CELLS = 17
    private const val MIN_GRID_FILL_WITH_STRONG_SPACING = 15
    private const val STRONG_SPACING_THRESHOLD = 0.65f
    private const val MIN_ORIENTATION_SCORE_TO_OVERRIDE_DEG0 = 12
    private const val MIN_FILLED_FOR_ACCEPT = 17
    private const val MIN_COLUMNS_WITH_COVERAGE = 3
    private const val MIN_CELLS_PER_COLUMN_FOR_COVERAGE = 3
    private const val TICKET_BOUNDS_EXPAND_FRAC = 0.08f
    private const val MIN_RAW_CANDIDATES_FOR_PARSE = 10
    private const val TAG = "BingoOrientation"
    private const val SCANNER_TAG = "BingoScanner"
    private const val HEADER_BAND_FRAC = 0.25f
    private const val HEADER_ANCHOR_WEIGHT = 3
    private const val MIN_SPATIAL_HEADER_COUNT = 2
    private const val GEOMETRY_WEIGHT = 8

    fun bucketizeCandidateNumbers(values: List<Int>): OcrCandidateNumbers {
        val flat = values.mapNotNull { it.takeIf { v -> v in MIN..MAX } }
        return OcrCandidateNumbers(
            flat = flat,
            b = flat.filter { it in B_RANGE },
            i = flat.filter { it in I_RANGE },
            n = flat.filter { it in N_RANGE },
            g = flat.filter { it in G_RANGE },
            o = flat.filter { it in O_RANGE },
        )
    }

    fun buildCandidateNumbers(items: List<OcrItem>): OcrCandidateNumbers =
        bucketizeCandidateNumbers(items.map { it.value })

    /**
     * Groups [OcrItem]s into five horizontal rows using the largest Y-gaps (ticket row spacing),
     * then assigns each number to its B/I/N/G/O column by value range. Conflicts in the same cell
     * keep the leftmost (smallest [OcrItem.centerX]) candidate.
     * Returns 25 values in **row-major** order (0 = empty / free cell).
     */
    fun buildRowMajorGridFromSpatialYGrouping(items: List<OcrItem>): List<Int> {
        val valid = items.filter { it.value in MIN..MAX }
        if (valid.isEmpty()) return List(25) { 0 }
        val rows = groupOcrItemsIntoFiveRowsByYGaps(valid)
        val grid = Array(5) { IntArray(5) { 0 } }
        for (r in 0..4) {
            val rowItems = rows.getOrElse(r) { emptyList() }.sortedBy { it.centerX }
            for (item in rowItems) {
                val c = columnIndexForBingoValue(item.value) ?: continue
                if (grid[r][c] == 0) grid[r][c] = item.value
            }
        }
        return (0..4).flatMap { rr -> (0..4).map { cc -> grid[rr][cc] } }
    }

    private fun columnIndexForBingoValue(v: Int): Int? = when {
        v in B_RANGE -> 0
        v in I_RANGE -> 1
        v in N_RANGE -> 2
        v in G_RANGE -> 3
        v in O_RANGE -> 4
        else -> null
    }

    private fun groupOcrItemsIntoFiveRowsByYGaps(items: List<OcrItem>): List<List<OcrItem>> {
        if (items.size < 8) return groupIntoFiveRowsEqualYBands(items)
        val sorted = items.sortedBy { it.centerY }
        val ys = sorted.map { it.centerY }
        val spanY = (ys.last() - ys.first()).coerceAtLeast(1e-3f)
        val gapIndexed: List<Pair<Int, Float>> =
            ys.zipWithNext().mapIndexed { idx, (a, b) -> idx to (b - a) }
        val maxGap = gapIndexed.maxOfOrNull { it.second } ?: 0f
        if (maxGap < spanY * 0.012f) return groupIntoFiveRowsEqualYBands(items)
        val topGapIndices = gapIndexed
            .sortedWith(compareByDescending<Pair<Int, Float>> { it.second }.thenBy { it.first })
            .map { it.first }
            .distinct()
            .take(4)
            .sorted()
        if (topGapIndices.size < 4) return groupIntoFiveRowsEqualYBands(items)
        val s0 = topGapIndices[0]
        val s1 = topGapIndices[1]
        val s2 = topGapIndices[2]
        val s3 = topGapIndices[3]
        return listOf(
            sorted.subList(0, s0 + 1),
            sorted.subList(s0 + 1, s1 + 1),
            sorted.subList(s1 + 1, s2 + 1),
            sorted.subList(s2 + 1, s3 + 1),
            sorted.subList(s3 + 1, sorted.size),
        )
    }

    private fun groupIntoFiveRowsEqualYBands(items: List<OcrItem>): List<List<OcrItem>> {
        if (items.isEmpty()) return List(5) { emptyList() }
        val minY = items.minOf { it.centerY }
        val maxY = items.maxOf { it.centerY }
        val span = (maxY - minY).coerceAtLeast(1e-3f)
        val buckets = List(5) { mutableListOf<OcrItem>() }
        for (item in items) {
            val r = (5f * (item.centerY - minY) / span).toInt().coerceIn(0, 4)
            buckets[r].add(item)
        }
        return buckets.map { it.toList() }
    }

    fun tryDetectBingoGridCropForOcr(src: Bitmap): Bitmap? {
        val w = src.width
        val h = src.height
        if (w < 80 || h < 80) return null
        val step = max(1, min(w, h) / 120)

        fun lum(p: Int): Int {
            val r = Color.red(p)
            val g = Color.green(p)
            val b = Color.blue(p)
            return (0.299 * r + 0.587 * g + 0.114 * b).toInt()
        }

        fun isBlueish(p: Int): Boolean {
            val r = Color.red(p)
            val g = Color.green(p)
            val b = Color.blue(p)
            return b > r + 28 && b > g + 12 && b > 65
        }

        var bandBottom = (0.10f * h).toInt().coerceAtLeast(1)
        val headerEnd = (0.38f * h).toInt()
        var hadBlue = false
        for (y in 0 until headerEnd step step) {
            var blue = 0
            var n = 0
            var x = (0.08f * w).toInt()
            val xEnd = (0.92f * w).toInt()
            while (x < xEnd) {
                n++
                if (isBlueish(src.getPixel(x.coerceIn(0, w - 1), y.coerceIn(0, h - 1)))) blue++
                x += step
            }
            if (n == 0) continue
            val ratio = blue.toFloat() / n
            if (ratio > 0.20f) {
                hadBlue = true
                bandBottom = (y + step * 2).coerceAtLeast(bandBottom)
            } else if (hadBlue && ratio < 0.08f) {
                break
            }
        }
        if (!hadBlue) return null
        bandBottom = bandBottom.coerceIn((0.07f * h).toInt(), (0.42f * h).toInt())

        val regionLeft = (0.08f * w).toInt()
        val regionRight = (0.92f * w).toInt()
        val regionTop = bandBottom + (0.015f * h).toInt()
        val regionBottomMax = (0.75f * h).toInt()
        if (regionRight <= regionLeft + 40 || regionBottomMax <= regionTop + 40) return null

        val rh = regionBottomMax - regionTop
        val rowCount = (rh / step).coerceAtLeast(1)
        val rowStrength = FloatArray(rowCount)
        var ri = 0
        for (y in regionTop until regionBottomMax step step) {
            if (ri >= rowStrength.size) break
            var sum = 0f
            var cnt = 0
            var x = regionLeft
            while (x < regionRight - step) {
                val g0 = lum(src.getPixel(x.coerceIn(0, w - 1), y.coerceIn(0, h - 1)))
                val g1 = lum(src.getPixel((x + step).coerceIn(0, w - 1), y.coerceIn(0, h - 1)))
                sum += abs(g0 - g1)
                cnt++
                x += step
            }
            rowStrength[ri++] = if (cnt > 0) sum / cnt else 0f
        }
        if (ri == 0) return null
        val strengths = rowStrength.take(ri).filter { it > 0f }
        if (strengths.isEmpty()) return null
        val sortedS = strengths.sorted()
        val median = sortedS[sortedS.size / 2]
        val threshStart = median * 0.78f
        val threshEnd = median * 0.86f

        var yStartIdx = 0
        for (i in 0 until ri) {
            if (rowStrength[i] >= threshStart) {
                yStartIdx = i
                break
            }
        }
        yStartIdx = max(0, yStartIdx - 1)
        var yEndIdx = ri - 1
        for (i in (0 until ri).reversed()) {
            if (rowStrength[i] >= threshEnd) {
                yEndIdx = i
                break
            }
        }
        if (yEndIdx < yStartIdx) return null

        val refinedTop = regionTop + yStartIdx * step
        val refinedBottom = regionTop + (yEndIdx + 1) * step
        val topMarginMin = max(0, bandBottom - (0.03f * h).toInt())
        val cropTop = (refinedTop - (0.055f * h).toInt()).coerceAtLeast(topMarginMin)
        val cropBottom = (refinedBottom + (0.016f * h).toInt()).coerceAtMost(regionBottomMax)
        val padX = max((0.045f * w).toInt(), 6)
        val padTopY = max((0.042f * h).toInt(), 6)
        val padBottomY = max((0.014f * h).toInt(), 3)

        var left = regionLeft - padX
        var right = regionRight + padX
        var top = cropTop - padTopY
        var bottom = cropBottom + padBottomY
        left = left.coerceIn(0, w - 2)
        right = right.coerceIn(left + 2, w)
        top = top.coerceIn(0, h - 2)
        bottom = bottom.coerceIn(top + 2, h)

        val maxTopInset = (0.17f * h).toInt()
        val maxLeftInset = (0.13f * w).toInt()
        if (top > maxTopInset) top = maxTopInset
        if (left > maxLeftInset) left = maxLeftInset
        right = right.coerceIn(left + 2, w)
        bottom = bottom.coerceIn(top + 2, h)

        val cw = right - left
        val ch = bottom - top
        val areaFrac = cw * ch.toFloat() / (w * h)
        if (areaFrac < 0.12f || areaFrac > 0.90f) return null
        if (cw < w * 0.22f || ch < h * 0.12f) return null

        return try {
            val out = Bitmap.createBitmap(src, left, top, cw, ch)
            Log.d(
                SCANNER_TAG,
                "gridCrop analyzer src=${w}x${h} rect=[$left,$top][$right,$bottom] out=${cw}x${ch} regionBottomMax=$regionBottomMax",
            )
            out
        } catch (_: Exception) {
            null
        }
    }

    private const val ORIENTATION_0 = 0
    private const val ORIENTATION_90 = 90
    private const val ORIENTATION_180 = 180
    private const val ORIENTATION_270 = 270

    private fun detectTicketCandidateBroad(
        valid: List<OcrItem>,
        imageWidth: Int,
        imageHeight: Int
    ): TicketDetectionCandidate? {
        if (valid.size < MIN_TICKET_ITEMS) return null
        val minX = valid.minOf { it.centerX }.coerceAtLeast(0f)
        val maxX = valid.maxOf { it.centerX }.coerceAtMost(imageWidth.toFloat())
        val minY = valid.minOf { it.centerY }.coerceAtLeast(0f)
        val maxY = valid.maxOf { it.centerY }.coerceAtMost(imageHeight.toFloat())
        val spanX = (maxX - minX).coerceAtLeast(1f)
        val spanY = (maxY - minY).coerceAtLeast(1f)
        val aspect = spanX / spanY
        if (aspect < MIN_ASPECT || aspect > MAX_ASPECT) return null
        val countScore = (valid.size / 25f).coerceIn(0f, 1f)
        val aspectScore = 1f - (kotlin.math.abs(aspect - 1.35f) / 2.5f).coerceIn(0f, 0.4f)
        val confidence = (countScore * 0.85f + aspectScore * 0.15f).coerceIn(0f, 1f)
        if (confidence < MIN_CONFIDENCE_BROAD) return null
        return TicketDetectionCandidate(
            left = minX, top = minY, right = maxX, bottom = maxY,
            imageW = imageWidth, imageH = imageHeight,
            confidence = confidence,
            matchedBlocksCount = valid.size
        )
    }

    private fun variance(values: List<Float>): Float {
        if (values.size < 2) return 0f
        val mean = values.average().toFloat()
        return values.map { (it - mean) * (it - mean) }.average().toFloat()
    }

    private fun filterOutliers(items: List<OcrItem>): List<OcrItem> {
        if (items.size < MIN_TICKET_ITEMS) return items
        val xs = items.map { it.centerX }.sorted()
        val ys = items.map { it.centerY }.sorted()
        val n = xs.size
        val q1Idx = (n * 0.25f).toInt().coerceIn(0, n - 1)
        val q3Idx = (n * 0.75f).toInt().coerceIn(0, n - 1)
        val q1x = xs[q1Idx]; val q3x = xs[q3Idx]; val iqrX = (q3x - q1x).coerceAtLeast(1f)
        val q1y = ys[q1Idx]; val q3y = ys[q3Idx]; val iqrY = (q3y - q1y).coerceAtLeast(1f)
        val loX = q1x - OUTLIER_IQR_FACTOR * iqrX; val hiX = q3x + OUTLIER_IQR_FACTOR * iqrX
        val loY = q1y - OUTLIER_IQR_FACTOR * iqrY; val hiY = q3y + OUTLIER_IQR_FACTOR * iqrY
        return items.filter { it.centerX in loX..hiX && it.centerY in loY..hiY }
    }

    private fun detectTicketCandidateGridCluster(
        valid: List<OcrItem>,
        imageWidth: Int,
        imageHeight: Int
    ): TicketDetectionCandidate? {
        if (valid.size < MIN_GRID_CLUSTER_ITEMS || imageWidth <= 0 || imageHeight <= 0) return null
        val xs = valid.map { it.centerX }.sorted()
        val ys = valid.map { it.centerY }.sorted()
        val n = valid.size
        val q1 = n / 4
        val q3 = (3 * n) / 4
        val xLo = xs[q1.coerceAtLeast(0)]
        val xHi = xs[q3.coerceAtMost(n - 1)]
        val yLo = ys[q1.coerceAtLeast(0)]
        val yHi = ys[q3.coerceAtMost(n - 1)]
        var core = valid.filter { it.centerX in xLo..xHi && it.centerY in yLo..yHi }
        if (core.size < MIN_GRID_CLUSTER_ITEMS) {
            val mx = (xLo + xHi) / 2f
            val my = (yLo + yHi) / 2f
            val rx = (xHi - xLo).coerceAtLeast(1f) * 0.6f
            val ry = (yHi - yLo).coerceAtLeast(1f) * 0.6f
            core = valid.filter {
                kotlin.math.abs(it.centerX - mx) <= rx && kotlin.math.abs(it.centerY - my) <= ry
            }
        }
        if (core.size < MIN_GRID_CLUSTER_ITEMS) return null
        val left = core.minOf { it.centerX }.coerceAtLeast(0f)
        val right = core.maxOf { it.centerX }.coerceAtMost(imageWidth.toFloat())
        val top = core.minOf { it.centerY }.coerceAtLeast(0f)
        val bottom = core.maxOf { it.centerY }.coerceAtMost(imageHeight.toFloat())
        val spanX = (right - left).coerceAtLeast(1f)
        val spanY = (bottom - top).coerceAtLeast(1f)
        val aspect = spanX / spanY
        if (aspect < MIN_ASPECT || aspect > MAX_ASPECT) return null
        val cellW = spanX / 5f
        val cellH = spanY / 5f
        var filled = 0
        for (row in 0..4) for (col in 0..4) {
            val hasPoint = core.any {
                it.centerX >= left + col * cellW && it.centerX < left + (col + 1) * cellW &&
                    it.centerY >= top + row * cellH && it.centerY < top + (row + 1) * cellH
            }
            if (hasPoint) filled++
        }
        val rowCenters = (0..4).map { row ->
            val inRow = core.filter { (it.centerY - top) / cellH in row - 0.5f..row + 0.5f }
            if (inRow.isEmpty()) Float.NaN else inRow.map { it.centerY }.average().toFloat()
        }.filter { !it.isNaN() }.sorted()
        val colCenters = (0..4).map { col ->
            val inCol = core.filter { (it.centerX - left) / cellW in col - 0.5f..col + 0.5f }
            if (inCol.isEmpty()) Float.NaN else inCol.map { it.centerX }.average().toFloat()
        }.filter { !it.isNaN() }.sorted()
        val rowGaps = if (rowCenters.size >= 2) rowCenters.zipWithNext { a, b -> b - a } else emptyList()
        val colGaps = if (colCenters.size >= 2) colCenters.zipWithNext { a, b -> b - a } else emptyList()
        val meanRowGap = if (rowGaps.isNotEmpty()) rowGaps.average().toFloat() else cellH
        val meanColGap = if (colGaps.isNotEmpty()) colGaps.average().toFloat() else cellW
        val rowSpacingScore = if (rowGaps.size >= 2 && meanRowGap > 0f)
            (1f - (kotlin.math.sqrt(variance(rowGaps)) / meanRowGap).coerceIn(0f, 0.6f)) else 0.5f
        val colSpacingScore = if (colGaps.size >= 2 && meanColGap > 0f)
            (1f - (kotlin.math.sqrt(variance(colGaps)) / meanColGap).coerceIn(0f, 0.6f)) else 0.5f
        val spacingStrong = rowSpacingScore >= STRONG_SPACING_THRESHOLD && colSpacingScore >= STRONG_SPACING_THRESHOLD
        if (filled < MIN_GRID_FILL_CELLS && !(filled >= MIN_GRID_FILL_WITH_STRONG_SPACING && spacingStrong)) return null
        val countScore = (core.size / 25f).coerceIn(0f, 1f)
        val fillScore = (filled / 25f).coerceIn(0f, 1f)
        val aspectScore = 1f - (kotlin.math.abs(aspect - 1.35f) / 2.5f).coerceIn(0f, 0.4f)
        val area = spanX * spanY
        val imageArea = (imageWidth * imageHeight).toFloat().coerceAtLeast(1f)
        val compactScore = (1f - (area / imageArea).coerceIn(0f, 0.5f)).coerceIn(0f, 1f)
        val cellSize = minOf(cellW, cellH).coerceAtLeast(1f)
        val meanDeviation = core.map { p ->
            val col = ((p.centerX - left) / cellW).toInt().coerceIn(0, 4)
            val row = ((p.centerY - top) / cellH).toInt().coerceIn(0, 4)
            val cx = left + (col + 0.5f) * cellW
            val cy = top + (row + 0.5f) * cellH
            kotlin.math.hypot(p.centerX - cx, p.centerY - cy)
        }.average().toFloat()
        val alignmentScore = (1f - (meanDeviation / cellSize).coerceIn(0f, 0.6f)).coerceIn(0f, 1f)
        val confidence = (
            countScore * 0.22f + fillScore * 0.22f + aspectScore * 0.1f +
            rowSpacingScore * 0.15f + colSpacingScore * 0.15f +
            compactScore * 0.08f + alignmentScore * 0.08f
        ).coerceIn(0f, 1f)
        if (confidence < MIN_CONFIDENCE) return null
        return TicketDetectionCandidate(
            left = left, top = top, right = right, bottom = bottom,
            imageW = imageWidth, imageH = imageHeight,
            confidence = confidence,
            matchedBlocksCount = core.size
        )
    }

    fun detectTicketCandidate(
        ocrItems: List<OcrItem>,
        imageWidth: Int,
        imageHeight: Int
    ): TicketDetectionCandidate? {
        val valid = ocrItems.filter { it.value in MIN..MAX }
        if (valid.isEmpty() || imageWidth <= 0 || imageHeight <= 0) return null
        val clustered = filterOutliers(valid)
        if (clustered.size < MIN_TICKET_ITEMS) return null
        val gridCluster = detectTicketCandidateGridCluster(clustered, imageWidth, imageHeight)
        val broad = detectTicketCandidateBroad(clustered, imageWidth, imageHeight)
        val gridStrong = gridCluster != null && (
            gridCluster.confidence >= 0.32f ||
            gridCluster.confidence >= broad?.confidence ?: 0f ||
            (gridCluster.matchedBlocksCount >= 20 && gridCluster.confidence >= MIN_CONFIDENCE)
        )
        return when {
            gridCluster != null && broad == null -> gridCluster
            gridCluster != null && broad != null -> if (gridStrong) gridCluster else broad
            else -> broad
        }
    }

    fun extractBingoNumber(rawText: String?): Int? {
        if (rawText.isNullOrBlank()) return null
        val match = pattern.find(rawText) ?: return null
        val value = match.groupValues.getOrNull(1)?.toIntOrNull() ?: return null
        return if (value in MIN..MAX) value else null
    }

    private const val MIN_FILLED_CELLS = 20
    private const val MIN_PARTIAL_CELLS = 10
    private const val MIN_ON_DEMAND_CELLS = 6
    private const val ON_DEMAND_CELL_EXPAND = 0.08f
    private const val CONFIDENCE_RADIUS_FRAC = 0.42f
    private const val STRICT_RADIUS_FRAC = 0.38f
    private const val MEDIUM_EXPAND_FRAC = 0.10f
    private const val RECOVERY_RADIUS_FRAC = 0.48f
    private const val MIN_NEIGHBORS_FOR_RECOVERY = 2

    private fun isConfidentPlacement(centerX: Float, centerY: Float, item: OcrItem, cellW: Float, cellH: Float, radiusFrac: Float = CONFIDENCE_RADIUS_FRAC): Boolean {
        val dist = kotlin.math.hypot(item.centerX - centerX, item.centerY - centerY)
        val limit = minOf(cellW, cellH) * radiusFrac
        return dist <= limit
    }

    private fun columnValidCount(cells: List<ScannedBingoCell>): Int {
        var n = 0
        for (c in cells) {
            if (c.value == 0) continue
            if (c.row == 2 && c.col == 2) { n++; continue }
            if (c.col in 0..4 && c.value in COLUMN_RANGES[c.col]) n++
        }
        return n
    }

    fun scoreParseForSelection(grid: ScannedBingoGrid): Int = plausibilityScore(grid, logDetails = false)

    fun plausibilityScore(grid: ScannedBingoGrid, logDetails: Boolean = false): Int {
        val cells = grid.cells
        val filled = cells.count { it.value != 0 }
        val colsPopulated = (0..4).count { c -> (0..4).any { r -> cells.any { it.row == r && it.col == c && it.value != 0 } } }
        val rowsPopulated = (0..4).count { r -> (0..4).any { c -> cells.any { it.row == r && it.col == c && it.value != 0 } } }
        val rangeScore = cells.count { it.value != 0 && it.col in 0..4 && it.value in COLUMN_RANGES[it.col] }
        val valueCounts = cells.filter { it.value != 0 }.groupingBy { it.value }.eachCount()
        val duplicatePenalty = valueCounts.values.sumOf { (it - 1).coerceAtLeast(0) }
        val expectedRowsN = setOf(0, 1, 3, 4)
        val expectedRowsOther = setOf(0, 1, 2, 3, 4)
        var orderingBonus = 0
        for (col in 0..4) {
            val rows = cells.filter { it.col == col && it.value != 0 }.map { it.row }.sorted()
            if (rows.isEmpty()) continue
            val gaps = (0 until rows.size - 1).sumOf { i -> (rows[i + 1] - rows[i] - 1).coerceAtLeast(0) }
            val expected = if (col == 2) expectedRowsN else expectedRowsOther
            if (rows.all { it in expected } && gaps <= 1) orderingBonus += 3
        }
        val score = filled * 8 + colsPopulated * 6 + rowsPopulated * 6 + rangeScore * 3 + orderingBonus - duplicatePenalty * 25
        if (logDetails) {
            Log.d(TAG, "plausibility: filled=$filled duplicatePenalty=$duplicatePenalty rangeScore=$rangeScore rowCoverage=$rowsPopulated colCoverage=$colsPopulated orderingBonus=$orderingBonus final=$score")
        }
        return score
    }

    private fun spacingConsistencyScore(cells: List<ScannedBingoCell>, cellW: Float, cellH: Float, minX: Float, minY: Float): Float {
        val filled = cells.filter { it.value != 0 }
        if (filled.size < 3) return 0.5f
        val rowCenters = (0..4).map { r -> minY + (r + 0.5f) * cellH }
        val colCenters = (0..4).map { c -> minX + (c + 0.5f) * cellW }
        val rowFilled = (0..4).map { r -> filled.count { it.row == r } }
        val colFilled = (0..4).map { c -> filled.count { it.col == c } }
        val rowGaps = rowFilled.zipWithNext { a, b -> if (a + b > 0) 1f else 0f }
        val colGaps = colFilled.zipWithNext { a, b -> if (a + b > 0) 1f else 0f }
        val reg = (rowGaps.sum() + colGaps.sum()) / (rowGaps.size + colGaps.size).toFloat()
        return 0.3f + 0.7f * reg
    }

    private fun scoreOnDemandResult(cells: List<ScannedBingoCell>, cellW: Float, cellH: Float, minX: Float, minY: Float): Float {
        val filled = cells.count { it.value != 0 }
        val colValid = columnValidCount(cells)
        val spacing = spacingConsistencyScore(cells, cellW, cellH, minX, minY)
        return colValid * 10f + filled + spacing * 5f
    }

    private fun buildOnDemandCandidate(
        valid: List<OcrItem>,
        minX: Float, maxX: Float, minY: Float, maxY: Float,
        expandFrac: Float,
        confidenceRadiusFrac: Float,
        doRecoveryPass: Boolean
    ): Pair<List<ScannedBingoCell>, Int>? {
        val spanX = (maxX - minX).coerceAtLeast(1f)
        val spanY = (maxY - minY).coerceAtLeast(1f)
        val cellW = spanX / 5f
        val cellH = spanY / 5f
        val expandW = cellW * expandFrac
        val expandH = cellH * expandFrac
        val grid = Array(5) { IntArray(5) { 0 } }
        val used = mutableSetOf<OcrItem>()
        for (row in 0..4) {
            for (col in 0..4) {
                val cx = minX + (col + 0.5f) * cellW
                val cy = minY + (row + 0.5f) * cellH
                val cellLeft = minX + col * cellW - expandW
                val cellRight = minX + (col + 1) * cellW + expandW
                val cellTop = minY + row * cellH - expandH
                val cellBottom = minY + (row + 1) * cellH + expandH
                val inCell = valid.filter { it !in used &&
                    it.centerX >= cellLeft && it.centerX < cellRight &&
                    it.centerY >= cellTop && it.centerY < cellBottom
                }
                val item = when {
                    inCell.isEmpty() -> null
                    inCell.size == 1 -> inCell.single()
                    else -> inCell.minByOrNull { (it.centerX - cx) * (it.centerX - cx) + (it.centerY - cy) * (it.centerY - cy) }
                }
                if (item != null && isConfidentPlacement(cx, cy, item, cellW, cellH, confidenceRadiusFrac)) {
                    grid[row][col] = item.value
                    used.add(item)
                }
            }
        }
        var unassigned = (valid - used).toMutableSet()
        if (doRecoveryPass && unassigned.isNotEmpty()) {
            val cellSize = minOf(cellW, cellH)
            val radius = cellSize * RECOVERY_RADIUS_FRAC
            for (row in 0..4) {
                for (col in 0..4) {
                    if (grid[row][col] != 0) continue
                    val neighborsRow = (0..4).count { c -> grid[row][c] != 0 }
                    val neighborsCol = (0..4).count { r -> grid[r][col] != 0 }
                    if (neighborsRow + neighborsCol < MIN_NEIGHBORS_FOR_RECOVERY) continue
                    val cx = minX + (col + 0.5f) * cellW
                    val cy = minY + (row + 0.5f) * cellH
                    val range = COLUMN_RANGES[col]
                    val candidates = unassigned.filter { it.value in range && kotlin.math.hypot(it.centerX - cx, it.centerY - cy) <= radius }
                    val best = candidates.minByOrNull { kotlin.math.hypot(it.centerX - cx, it.centerY - cy) }
                    if (best != null && isConfidentPlacement(cx, cy, best, cellW, cellH, RECOVERY_RADIUS_FRAC)) {
                        grid[row][col] = best.value
                        used.add(best)
                        unassigned.remove(best)
                    }
                }
            }
        }
        if (used.size < MIN_ON_DEMAND_CELLS) return null
        val baseCells = (0..4).flatMap { r -> (0..4).map { c -> ScannedBingoCell(r, c, grid[r][c]) } }
        if (scoreGrid(baseCells).second == 0) return null
        return baseCells to used.size
    }

    fun getBestOnDemandCellsRaw(
        ocrItems: List<OcrItem>,
        imageWidth: Int,
        imageHeight: Int
    ): List<ScannedBingoCell>? {
        val valid = ocrItems.filter { it.value in MIN..MAX }
        if (valid.size < MIN_ON_DEMAND_CELLS) return null
        val minX = valid.minOf { it.centerX }.coerceAtLeast(0f)
        val maxX = valid.maxOf { it.centerX }.coerceAtMost(imageWidth.toFloat())
        val minY = valid.minOf { it.centerY }.coerceAtLeast(0f)
        val maxY = valid.maxOf { it.centerY }.coerceAtMost(imageHeight.toFloat())
        val spanX = (maxX - minX).coerceAtLeast(1f)
        val spanY = (maxY - minY).coerceAtLeast(1f)
        val cellW = spanX / 5f
        val cellH = spanY / 5f
        val candidates = listOf(
            buildOnDemandCandidate(valid, minX, maxX, minY, maxY, 0f, STRICT_RADIUS_FRAC, false),
            buildOnDemandCandidate(valid, minX, maxX, minY, maxY, ON_DEMAND_CELL_EXPAND, CONFIDENCE_RADIUS_FRAC, false),
            buildOnDemandCandidate(valid, minX, maxX, minY, maxY, MEDIUM_EXPAND_FRAC, CONFIDENCE_RADIUS_FRAC, false),
            buildOnDemandCandidate(valid, minX, maxX, minY, maxY, MEDIUM_EXPAND_FRAC, CONFIDENCE_RADIUS_FRAC, true)
        ).mapNotNull { it }
        return candidates.maxByOrNull { (cells, _) -> scoreOnDemandResult(cells, cellW, cellH, minX, minY) }?.first
    }

    private fun slotConflictScore(
        r: Int, c: Int, value: Int,
        agreementCount: Int,
        draft: Array<IntArray>
    ): Int {
        val columnMatch = if (c in 0..4 && value in COLUMN_RANGES[c]) 2 else 0
        val rowNeighbors = (0..4).count { c2 -> draft[r][c2] != 0 }
        val colNeighbors = (0..4).count { r2 -> draft[r2][c] != 0 }
        val consistency = rowNeighbors + colNeighbors
        return agreementCount * 10 + columnMatch + consistency
    }

    fun mergeFramesToGrid(
        frames: List<Triple<List<OcrItem>, Int, Int>>
    ): ScannedBingoGrid? {
        if (frames.size < 2) return null
        val grids = frames.mapNotNull { (items, w, h) -> getBestOnDemandCellsRaw(items, w, h) }
        if (grids.isEmpty()) return null
        val slotValueLists = Array(5) { r -> Array(5) { c ->
            grids.mapNotNull { g -> g.find { it.row == r && it.col == c }?.value }.filter { it != 0 }
        } }
        val draft = Array(5) { IntArray(5) { 0 } }
        for (r in 0..4) {
            for (c in 0..4) {
                val values = slotValueLists[r][c]
                draft[r][c] = when {
                    values.isEmpty() -> 0
                    values.all { it == values[0] } -> values[0]
                    else -> {
                        val counts = values.groupingBy { it }.eachCount()
                        val best = counts.maxByOrNull { it.value }
                        if (best != null && best.value >= 2) best.key else 0
                    }
                }
            }
        }
        val valueToSlots = mutableMapOf<Int, MutableList<Pair<Int, Int>>>()
        for (r in 0..4) for (c in 0..4) {
            val v = draft[r][c]
            if (v != 0) valueToSlots.getOrPut(v) { mutableListOf() }.add(r to c)
        }
        for ((value, slots) in valueToSlots) {
            if (slots.size <= 1) continue
            val scores = slots.map { (r, c) ->
                val agreement = slotValueLists[r][c].count { it == value }
                slotConflictScore(r, c, value, agreement, draft) to (r to c)
            }
            val best = scores.maxByOrNull { it.first }!!
            for ((r, c) in slots) if ((r to c) != best.second) draft[r][c] = 0
        }
        for (r in 0..4) {
            for (c in 0..4) {
                if (draft[r][c] == 0) continue
                val values = slotValueLists[r][c]
                val agreement = values.count { it == draft[r][c] }
                val counts = values.groupingBy { it }.eachCount().values.sortedDescending()
                val bestCount = counts.firstOrNull() ?: 0
                val secondCount = counts.getOrNull(1) ?: 0
                if (agreement < 2 || (secondCount > 0 && secondCount == bestCount)) draft[r][c] = 0
            }
        }
        val baseCells = (0..4).flatMap { r -> (0..4).map { c -> ScannedBingoCell(r, c, draft[r][c]) } }
        if (scoreGrid(baseCells).second == 0) return null
        return buildCanonicalGridFromNumbers(baseCells.map { it.value }, "merged")
    }

    fun parseOcrToGridOnDemand(
        ocrItems: List<OcrItem>,
        imageWidth: Int,
        imageHeight: Int
    ): ScannedBingoGrid? {
        val valid = ocrItems.filter { it.value in MIN..MAX }
        if (valid.size < MIN_ON_DEMAND_CELLS) return null
        val minX = valid.minOf { it.centerX }.coerceAtLeast(0f)
        val maxX = valid.maxOf { it.centerX }.coerceAtMost(imageWidth.toFloat())
        val minY = valid.minOf { it.centerY }.coerceAtLeast(0f)
        val maxY = valid.maxOf { it.centerY }.coerceAtMost(imageHeight.toFloat())
        val spanX = (maxX - minX).coerceAtLeast(1f)
        val spanY = (maxY - minY).coerceAtLeast(1f)
        val cellW = spanX / 5f
        val cellH = spanY / 5f
        val rawText = valid.joinToString(" ") { it.value.toString() }
        val candidates = listOf(
            buildOnDemandCandidate(valid, minX, maxX, minY, maxY, 0f, STRICT_RADIUS_FRAC, false),
            buildOnDemandCandidate(valid, minX, maxX, minY, maxY, ON_DEMAND_CELL_EXPAND, CONFIDENCE_RADIUS_FRAC, false),
            buildOnDemandCandidate(valid, minX, maxX, minY, maxY, MEDIUM_EXPAND_FRAC, CONFIDENCE_RADIUS_FRAC, false),
            buildOnDemandCandidate(valid, minX, maxX, minY, maxY, MEDIUM_EXPAND_FRAC, CONFIDENCE_RADIUS_FRAC, true)
        ).mapNotNull { it }
        if (candidates.isEmpty()) return null
        val bestCells = candidates.maxByOrNull { (cells, _) -> scoreOnDemandResult(cells, cellW, cellH, minX, minY) }?.first ?: return null
        return buildCanonicalGridFromNumbers(bestCells.map { it.value }, rawText)
    }

    private fun toCanonical(centerX: Float, centerY: Float, minX: Float, maxX: Float, minY: Float, maxY: Float, orientationDegrees: Int): Pair<Float, Float> {
        val spanX = (maxX - minX).coerceAtLeast(1f)
        val spanY = (maxY - minY).coerceAtLeast(1f)
        return when (orientationDegrees) {
            0 -> (centerX - minX) to (centerY - minY)
            90 -> (centerY - minY) to (maxX - centerX)
            180 -> (maxX - centerX) to (maxY - centerY)
            270 -> (maxY - centerY) to (centerX - minX)
            else -> (centerX - minX) to (centerY - minY)
        }
    }

    private fun toCanonicalNormalized(centerX: Float, centerY: Float, minX: Float, maxX: Float, minY: Float, maxY: Float, orientationDegrees: Int): Pair<Float, Float> {
        val spanX = (maxX - minX).coerceAtLeast(1f)
        val spanY = (maxY - minY).coerceAtLeast(1f)
        val (nx, ny) = when (orientationDegrees) {
            ORIENTATION_0 -> (centerX - minX) / spanX to (centerY - minY) / spanY
            ORIENTATION_180 -> (maxX - centerX) / spanX to (maxY - centerY) / spanY
            ORIENTATION_90 -> (maxX - centerX) / spanX to (centerY - minY) / spanY
            ORIENTATION_270 -> (centerX - minX) / spanX to (maxY - centerY) / spanY
            else -> (centerX - minX) / spanX to (centerY - minY) / spanY
        }
        return nx.coerceIn(0f, 1f) to ny.coerceIn(0f, 1f)
    }

    private fun canonicalSpan(orientationDegrees: Int, spanX: Float, spanY: Float): Pair<Float, Float> =
        when (orientationDegrees) {
            90, 270 -> spanY to spanX
            else -> spanX to spanY
        }

    private fun bandCentersFromValues(values: List<Float>, bands: Int = 5): FloatArray {
        if (values.isEmpty()) return FloatArray(bands) { (it + 1) / (bands + 1).toFloat() }
        val sorted = values.sorted()
        val n = sorted.size
        val out = FloatArray(bands)
        for (b in 0 until bands) {
            val lo = (b * n) / bands
            val hi = ((b + 1) * n).coerceAtMost(n)
            out[b] = if (hi > lo) sorted.subList(lo, hi).average().toFloat() else (b + 1) / (bands + 1).toFloat()
        }
        return out
    }

    private fun snapToNearestBand(value: Float, bandCenters: FloatArray): Int {
        var best = 0
        var bestDist = kotlin.math.abs(value - bandCenters[0])
        for (i in 1 until bandCenters.size) {
            val d = kotlin.math.abs(value - bandCenters[i])
            if (d < bestDist) { bestDist = d; best = i }
        }
        return best.coerceIn(0, 4)
    }

    private fun buildGridFromCanonicalNormalized(
        valid: List<OcrItem>,
        minX: Float, maxX: Float, minY: Float, maxY: Float,
        orientationDegrees: Int,
        logBands: Boolean = false
    ): List<ScannedBingoCell> {
        val itemsWithNorm = valid.map { item ->
            val (nx, ny) = toCanonicalNormalized(item.centerX, item.centerY, minX, maxX, minY, maxY, orientationDegrees)
            Triple(item, nx, ny)
        }
        val nxs = itemsWithNorm.map { it.second }
        val nys = itemsWithNorm.map { it.third }
        val colBandCenters = bandCentersFromValues(nxs)
        val rowBandCenters = bandCentersFromValues(nys)
        if (logBands) {
            Log.d(TAG, "detected center count: ${itemsWithNorm.size}")
            Log.d(TAG, "column band centers: ${colBandCenters.joinToString { "%.2f".format(it) }}")
            Log.d(TAG, "row band centers: ${rowBandCenters.joinToString { "%.2f".format(it) }}")
        }
        val cellCandidates = Array(5) { Array(5) { mutableListOf<Triple<OcrItem, Float, Float>>() } }
        for ((item, nx, ny) in itemsWithNorm) {
            val col = snapToNearestBand(nx, colBandCenters)
            val row = snapToNearestBand(ny, rowBandCenters)
            cellCandidates[row][col].add(Triple(item, nx, ny))
        }
        val grid = Array(5) { IntArray(5) { 0 } }
        for (row in 0..4) {
            for (col in 0..4) {
                val cx = (col + 0.5f) / 5f
                val cy = (row + 0.5f) / 5f
                val candidates = cellCandidates[row][col]
                val best = when {
                    candidates.isEmpty() -> null
                    candidates.size == 1 -> candidates.single().first
                    else -> {
                        val range = COLUMN_RANGES[col]
                        candidates.maxByOrNull { (item, nx, ny) ->
                            val rangeMatch = if (item.value in range) 2 else 0
                            val dist = kotlin.math.hypot(nx - cx, ny - cy)
                            val proximity = 1f / (1f + dist)
                            rangeMatch + proximity
                        }?.first
                    }
                }
                if (best != null) grid[row][col] = best.value
            }
        }
        val filledCount = (0..4).sumOf { r -> (0..4).count { c -> grid[r][c] != 0 } }
        if (logBands) Log.d(TAG, "filled cell count: $filledCount")
        return (0..4).flatMap { r -> (0..4).map { c -> ScannedBingoCell(r, c, grid[r][c]) } }
    }

    private fun buildGridFromOrientation(
        valid: List<OcrItem>,
        minX: Float, maxX: Float, minY: Float, maxY: Float,
        orientationDegrees: Int
    ): List<ScannedBingoCell> {
        val spanX = (maxX - minX).coerceAtLeast(1f)
        val spanY = (maxY - minY).coerceAtLeast(1f)
        val (spanW, spanH) = canonicalSpan(orientationDegrees, spanX, spanY)
        val cellW = spanW / 5f
        val cellH = spanH / 5f
        val itemsWithCanonical = valid.map { item ->
            val (cx, cy) = toCanonical(item.centerX, item.centerY, minX, maxX, minY, maxY, orientationDegrees)
            Triple(item, cx.coerceIn(0f, spanW), cy.coerceIn(0f, spanH))
        }
        val grid = Array(5) { IntArray(5) { 0 } }
        val used = mutableSetOf<OcrItem>()
        for (row in 0..4) {
            for (col in 0..4) {
                val cx = (col + 0.5f) * cellW
                val cy = (row + 0.5f) * cellH
                val cellLeft = col * cellW
                val cellRight = (col + 1) * cellW
                val cellTop = row * cellH
                val cellBottom = (row + 1) * cellH
                val inCell = itemsWithCanonical.filter { (item, x, y) ->
                    item !in used && x >= cellLeft && x < cellRight && y >= cellTop && y < cellBottom
                }
                val best = when {
                    inCell.isEmpty() -> null
                    inCell.size == 1 -> inCell.single().first
                    else -> inCell.minByOrNull { (item, x, y) ->
                        (x - cx) * (x - cx) + (y - cy) * (y - cy)
                    }?.first
                }
                if (best != null) {
                    val (item, x, y) = itemsWithCanonical.first { it.first == best }
                    val dist = kotlin.math.hypot(x - cx, y - cy)
                    if (dist <= minOf(cellW, cellH) * CONFIDENCE_RADIUS_FRAC) {
                        grid[row][col] = best.value
                        used.add(best)
                    }
                }
            }
        }
        return (0..4).flatMap { row -> (0..4).map { col -> ScannedBingoCell(row, col, grid[row][col]) } }
    }

    private fun remapDetectedToCanonical(detectedRow: Int, detectedCol: Int, orientationDegrees: Int): Pair<Int, Int> =
        when (orientationDegrees) {
            ORIENTATION_0 -> detectedRow to detectedCol
            ORIENTATION_180 -> (4 - detectedRow) to (4 - detectedCol)
            ORIENTATION_90 -> (4 - detectedCol) to detectedRow
            ORIENTATION_270 -> detectedCol to (4 - detectedRow)
            else -> detectedRow to detectedCol
        }

    private fun cellsToGrid(cells: List<ScannedBingoCell>): Array<IntArray> {
        val grid = Array(5) { IntArray(5) { 0 } }
        for (c in cells) grid[c.row][c.col] = c.value
        return grid
    }

    private fun gridToCells(grid: Array<IntArray>): List<ScannedBingoCell> =
        (0..4).flatMap { r -> (0..4).map { c -> ScannedBingoCell(r, c, grid[r][c]) } }

    private fun logPerColumnValues(cells: List<ScannedBingoCell>, prefix: String) {
        val labels = listOf("B", "I", "N", "G", "O")
        for (col in 0..4) {
            val values = (0..4).map { r -> cells.find { it.row == r && it.col == col }?.value ?: 0 }
            Log.d(TAG, "$prefix col ${labels[col]}: ${values.joinToString(",")}")
        }
    }

    private fun normalizeRowOrderInColumns(cells: List<ScannedBingoCell>, orientationDegrees: Int): List<ScannedBingoCell> {
        if (orientationDegrees != ORIENTATION_180) return cells
        val grid = cellsToGrid(cells)
        val out = Array(5) { IntArray(5) { 0 } }
        for (c in 0..4) for (r in 0..4) out[r][c] = grid[4 - r][c]
        return gridToCells(out)
    }

    private fun remapCellsToCanonical(detectedCells: List<ScannedBingoCell>, orientationDegrees: Int): List<ScannedBingoCell> {
        val grid = Array(5) { IntArray(5) { 0 } }
        for (cell in detectedCells) {
            val (nr, nc) = remapDetectedToCanonical(cell.row, cell.col, orientationDegrees)
            grid[nr][nc] = cell.value
            if (cell.value != 0) Log.d(TAG, "Remap cell (${cell.row},${cell.col}) -> ($nr,$nc)")
        }
        return gridToCells(grid)
    }

    private fun inferOrientationAndBuildGrid(
        valid: List<OcrItem>,
        minX: Float, maxX: Float, minY: Float, maxY: Float,
        rawText: String,
        logFinalParse: Boolean = false
    ): Pair<List<ScannedBingoCell>, Int>? {
        val orientations = listOf(ORIENTATION_0, ORIENTATION_90, ORIENTATION_180, ORIENTATION_270)
        val scores = orientations.map { deg ->
            val cells = buildGridFromCanonicalNormalized(valid, minX, maxX, minY, maxY, deg, logBands = false)
            val colValid = columnValidCount(cells)
            val (validGrid, gridScore) = scoreGrid(cells)
            val score = if (validGrid) colValid * 2 + gridScore else colValid
            deg to score
        }
        val score0 = scores.first { it.first == ORIENTATION_0 }.second
        val score90 = scores.first { it.first == ORIENTATION_90 }.second
        val score180 = scores.first { it.first == ORIENTATION_180 }.second
        val score270 = scores.first { it.first == ORIENTATION_270 }.second
        if (logFinalParse) Log.d(TAG, "Scores -> normal:$score0 rot90:$score90 rot180:$score180 rot270:$score270")
        val (chosenOrientation, bestScore) = scores.maxByOrNull { it.second } ?: return null
        val allZero = scores.all { it.second == 0 }
        val tieWithZeroAndWeak = chosenOrientation != ORIENTATION_0 && bestScore == score0 && bestScore < MIN_ORIENTATION_SCORE_TO_OVERRIDE_DEG0
        val useZero = allZero || tieWithZeroAndWeak
        val applied = if (useZero) ORIENTATION_0 else chosenOrientation
        val finalCells = buildGridFromCanonicalNormalized(valid, minX, maxX, minY, maxY, applied, logBands = logFinalParse)
        if (logFinalParse) {
            Log.d(TAG, "Best orientation score: $bestScore, applied: $applied")
            Log.d(TAG, "Chosen orientation: $applied")
            valid.take(3).forEach { item ->
                val (nx, ny) = toCanonicalNormalized(item.centerX, item.centerY, minX, maxX, minY, maxY, applied)
                Log.d(TAG, "sample canonical center: value=${item.value} nx=${"%.2f".format(nx)} ny=${"%.2f".format(ny)}")
            }
            logPerColumnValues(finalCells, "canonical")
            val bValues = (0..4).map { r -> finalCells.find { it.row == r && it.col == 0 }?.value ?: 0 }.filter { it != 0 }
            Log.d(TAG, "Final B column: ${bValues.joinToString()}")
        }
        return finalCells to applied
    }

    private const val DEDUPE_Y_FRAC = 0.25f
    private const val LOCAL_DEDUPE_FRAC = 0.22f
    private const val COL_REJECT_MARGIN = 1.2f
    private const val STABILITY_TIE_MARGIN = 4
    private fun dedupeColumnByY(sorted: List<OcrItem>, cellH: Float): List<OcrItem> {
        if (sorted.isEmpty()) return sorted
        val out = mutableListOf<OcrItem>()
        val threshold = cellH * DEDUPE_Y_FRAC
        for (item in sorted) {
            val keep = out.isEmpty() || out.last().value != item.value || kotlin.math.abs(item.centerY - out.last().centerY) > threshold
            if (keep) out.add(item)
        }
        return out
    }

    private data class LocalAxes(val centerX: Float, val centerY: Float, val axisColX: Float, val axisColY: Float, val axisRowX: Float, val axisRowY: Float)

    private fun computeLocalAxes(candidates: List<OcrItem>): LocalAxes? {
        if (candidates.size < 6) return null
        val n = candidates.size.toFloat()
        val cx = candidates.sumOf { it.centerX.toDouble() }.toFloat() / n
        val cy = candidates.sumOf { it.centerY.toDouble() }.toFloat() / n
        var covXX = 0.0
        var covYY = 0.0
        var covXY = 0.0
        for (p in candidates) {
            val dx = (p.centerX - cx).toDouble()
            val dy = (p.centerY - cy).toDouble()
            covXX += dx * dx
            covYY += dy * dy
            covXY += dx * dy
        }
        covXX /= n; covYY /= n; covXY /= n
        val trace = covXX + covYY
        val det = covXX * covYY - covXY * covXY
        val disc = trace * trace - 4 * det
        if (disc < 0) return null
        val eigenLarge = (trace + kotlin.math.sqrt(disc)) / 2
        val eigenSmall = (trace - kotlin.math.sqrt(disc)) / 2
        var ax = covXY.toFloat()
        var ay = (eigenLarge - covXX).toFloat()
        val norm0 = kotlin.math.hypot(ax.toDouble(), ay.toDouble()).toFloat().coerceAtLeast(1e-6f)
        ax /= norm0; ay /= norm0
        val bx = -ay
        val by = ax
        return LocalAxes(cx, cy, ax, ay, bx, by)
    }

    private fun projectToLocal(item: OcrItem, axes: LocalAxes, variant: Int): Pair<Float, Float> {
        val dx = item.centerX - axes.centerX
        val dy = item.centerY - axes.centerY
        val proj0 = dx * axes.axisColX + dy * axes.axisColY
        val proj1 = dx * axes.axisRowX + dy * axes.axisRowY
        return when (variant) {
            ORIENTATION_0 -> proj0 to proj1
            ORIENTATION_180 -> -proj0 to -proj1
            ORIENTATION_90 -> proj1 to -proj0
            ORIENTATION_270 -> -proj1 to proj0
            else -> proj0 to proj1
        }
    }

    private fun toCanonicalNormalized(u: Float, v: Float, orientation: Int): Pair<Float, Float> {
        val (x, y) = when (orientation) {
            ORIENTATION_0 -> u to v
            ORIENTATION_90 -> v to (1f - u)
            ORIENTATION_180 -> (1f - u) to (1f - v)
            ORIENTATION_270 -> (1f - v) to u
            else -> u to v
        }
        return x.coerceIn(0f, 1f) to y.coerceIn(0f, 1f)
    }

    private fun lockCanonicalAxisSignX(
        withCanonical: List<Triple<OcrItem, Pair<Float, Float>, Int>>,
        orientation: Int,
        logFinalParse: Boolean
    ): Pair<List<Triple<OcrItem, Pair<Float, Float>, Int>>, Boolean> {
        val bRange = withCanonical.filter { it.first.value in B_RANGE }
        val oRange = withCanonical.filter { it.first.value in O_RANGE }
        val meanXB = if (bRange.isNotEmpty()) bRange.map { it.second.first }.average().toFloat() else 0.5f
        val meanXO = if (oRange.isNotEmpty()) oRange.map { it.second.first }.average().toFloat() else 0.5f
        val flipX = meanXB > meanXO
        val out = withCanonical.map { (item, xy, idx) ->
            val x = if (flipX) 1f - xy.first else xy.first
            Triple(item, x to xy.second, idx)
        }
        return out to flipX
    }

    private val rowSlotsN = listOf(0, 1, 3, 4)
    private const val CANONICAL_SPAN = 1f
    private const val CANONICAL_CELL = 0.2f

    private fun assignGridFromCanonicalCoordinates(
        withCanonical: List<Triple<OcrItem, Pair<Float, Float>, Int>>,
        logFinalParse: Boolean,
        logBColumnDedupe: Boolean
    ): List<ScannedBingoCell> = assignGridFromCanonicalCoordinatesWithAssignment(withCanonical, logFinalParse, logBColumnDedupe).first

    private fun assignGridFromCanonicalCoordinatesWithAssignment(
        withCanonical: List<Triple<OcrItem, Pair<Float, Float>, Int>>,
        logFinalParse: Boolean,
        logBColumnDedupe: Boolean
    ): Pair<List<ScannedBingoCell>, List<Triple<OcrItem, Int, Int>>> {
        val byCol = List(5) { mutableListOf<Triple<OcrItem, Float, Int>>() }
        for (entry in withCanonical) {
            val item = entry.first
            val x = entry.second.first
            val y = entry.second.second
            val origIndex = entry.third
            val c = COLUMN_RANGES.indexOfFirst { item.value in it }
            if (c !in 0..4) continue
            val colNorm = x * 5f
            if (kotlin.math.abs(colNorm - (c + 0.5f)) > COL_REJECT_MARGIN) continue
            byCol[c].add(Triple(item, y, origIndex))
        }
        val dedupeThresh = CANONICAL_CELL * LOCAL_DEDUPE_FRAC
        val grid = Array(5) { IntArray(5) { 0 } }
        val assignment = mutableListOf<Triple<OcrItem, Int, Int>>()
        for (col in 0..4) {
            val sorted = byCol[col].sortedWith(
                compareBy<Triple<OcrItem, Float, Int>> { it.second }
                    .thenBy { it.first.value }
                    .thenBy { it.first.centerX }
                    .thenBy { it.first.centerY }
                    .thenBy { it.third }
            )
            if (logBColumnDedupe && col == 0) Log.d(TAG, "projected B-column row values before dedupe: ${sorted.map { "${it.first.value}@${"%.2f".format(it.second)}" }.joinToString()}")
            val deduped = mutableListOf<Triple<OcrItem, Float, Int>>()
            for (p in sorted) {
                val keep = deduped.isEmpty() || (p.second - deduped.last().second > dedupeThresh) || (p.first.value != deduped.last().first.value)
                if (keep) deduped.add(p)
            }
            if (logBColumnDedupe && col == 0) Log.d(TAG, "projected B-column row values after dedupe: ${deduped.map { "${it.first.value}@${"%.2f".format(it.second)}" }.joinToString()}")
            val slots = if (col == 2) rowSlotsN else (0..4).toList()
            deduped.take(slots.size).forEachIndexed { idx, triple ->
                val row = slots[idx]
                grid[row][col] = triple.first.value
                assignment.add(Triple(triple.first, row, col))
            }
        }
        val cells = (0..4).flatMap { r -> (0..4).map { c -> ScannedBingoCell(r, c, grid[r][c]) } }
        return cells to assignment
    }

    private data class LocalAxesVariant(
        val cells: List<ScannedBingoCell>,
        val orientation: Int,
        val flipX: Boolean,
        val flipY: Boolean,
        val assignment: List<Triple<OcrItem, Int, Int>>
    )

    private fun buildGridFromLocalAxes(
        candidates: List<OcrItem>,
        minX: Float, maxX: Float, minY: Float, maxY: Float,
        logFinalParse: Boolean
    ): List<LocalAxesVariant>? {
        val axes = computeLocalAxes(candidates) ?: return null
        if (logFinalParse) {
            Log.d(TAG, "local axis vectors: col=(${axes.axisColX}, ${axes.axisColY}) row=(${axes.axisRowX}, ${axes.axisRowY}) center=(${axes.centerX}, ${axes.centerY})")
            Log.d(TAG, "shared assignment used: true")
            Log.d(TAG, "orientation applied in normalization: true")
        }
        val results = mutableListOf<LocalAxesVariant>()
        val orientList = listOf(ORIENTATION_0, ORIENTATION_90, ORIENTATION_180, ORIENTATION_270)
        for (orientation in orientList) {
            val withProj = candidates.mapIndexed { index, it -> Triple(it, projectToLocal(it, axes, orientation), index) }
            val minCol = withProj.minOfOrNull { it.second.first } ?: 0f
            val maxCol = withProj.maxOfOrNull { it.second.first } ?: 0f
            val minRow = withProj.minOfOrNull { it.second.second } ?: 0f
            val maxRow = withProj.maxOfOrNull { it.second.second } ?: 0f
            val spanCol = (maxCol - minCol).coerceAtLeast(1e-6f)
            val spanRow = (maxRow - minRow).coerceAtLeast(1e-6f)
            val withUV = withProj.map { (item, proj, idx) ->
                val u = ((proj.first - minCol) / spanCol).coerceIn(0f, 1f)
                val v = ((proj.second - minRow) / spanRow).coerceIn(0f, 1f)
                val xy = toCanonicalNormalized(u, v, orientation)
                Triple(item, Triple(u, v, xy), idx)
            }
            var withCanonical = withUV.map { (item, uvw, idx) -> Triple(item, uvw.third, idx) }
            if (logFinalParse && withUV.size >= 10) {
                Log.d(TAG, "tested orientation: $orientation")
                withUV.take(10).forEachIndexed { i, (item, uvw, _) ->
                    Log.d(TAG, "norm[$i] raw=(${"%.1f".format(item.centerX)},${"%.1f".format(item.centerY)}) u/v=(${"%.2f".format(uvw.first)},${"%.2f".format(uvw.second)}) canonical=(${"%.2f".format(uvw.third.first)},${"%.2f".format(uvw.third.second)}) value=${item.value}")
                }
            }
            val (withCanonicalX, flipX) = lockCanonicalAxisSignX(withCanonical, orientation, logFinalParse)
            val withYFlipped = withCanonicalX.map { (item, xy, idx) -> Triple(item, xy.first to (1f - xy.second), idx) }
            for ((flipY, withCanonicalFinal) in listOf(false to withCanonicalX, true to withYFlipped)) {
                val (cells, assignment) = assignGridFromCanonicalCoordinatesWithAssignment(withCanonicalFinal, logFinalParse && !flipY, logFinalParse && orientation == ORIENTATION_0 && !flipY)
                val (structOk, _) = scoreGrid(cells)
                if (!structOk) continue
                val score = plausibilityScore(ScannedBingoGrid(cells, rawText = ""), logDetails = false)
                if (logFinalParse) {
                    Log.d(TAG, "tested combo: orientation=$orientation flipX=$flipX flipY=$flipY score=$score")
                }
                results.add(LocalAxesVariant(cells, orientation, flipX, flipY, assignment))
            }
        }
        return results
    }

    private fun buildGridFromColumnRankOrder(
        candidates: List<OcrItem>,
        minX: Float, maxX: Float, minY: Float, maxY: Float,
        topToBottom: Boolean
    ): List<ScannedBingoCell> {
        val spanY = (maxY - minY).coerceAtLeast(1f)
        val cellH = spanY / 5f
        val columnBuckets = List(5) { mutableListOf<OcrItem>() }
        for (item in candidates) {
            val col = COLUMN_RANGES.indexOfFirst { item.value in it }
            if (col in 0..4) columnBuckets[col].add(item)
        }
        val rowSlotsN = listOf(0, 1, 3, 4)
        val grid = Array(5) { IntArray(5) { 0 } }
        for (col in 0..4) {
            val bucket = columnBuckets[col]
            val sorted = if (topToBottom) bucket.sortedBy { it.centerY } else bucket.sortedByDescending { it.centerY }
            val deduped = dedupeColumnByY(sorted, cellH)
            val maxRows = if (col == 2) 4 else 5
            val slots = if (col == 2) rowSlotsN else (0..4).toList()
            deduped.take(maxRows).forEachIndexed { idx, item ->
                grid[slots[idx]][col] = item.value
            }
        }
        return (0..4).flatMap { r -> (0..4).map { c -> ScannedBingoCell(r, c, grid[r][c]) } }
    }

    private fun rowDirectionScore(cells: List<ScannedBingoCell>): Int {
        val filled = cells.count { it.value != 0 }
        val rowCoverage = (0..4).count { r -> (0..4).any { c -> cells.any { it.row == r && it.col == c && it.value != 0 } } }
        val rowFullness = (0..4).sumOf { r -> (0..4).count { c -> cells.any { it.row == r && it.col == c && it.value != 0 } } }
        val rangeMatch = cells.count { it.value != 0 && it.col in 0..4 && it.value in COLUMN_RANGES[it.col] }
        val valueCounts = cells.filter { it.value != 0 }.groupingBy { it.value }.eachCount()
        val duplicatePenalty = valueCounts.values.sumOf { (it - 1).coerceAtLeast(0) }
        var gapPenalty = 0
        for (col in 0..4) {
            val rows = cells.filter { it.col == col && it.value != 0 }.map { it.row }.sorted()
            for (i in 0 until rows.size - 1) gapPenalty += (rows[i + 1] - rows[i] - 1).coerceAtLeast(0)
        }
        val perRowCounts = (0..4).map { r -> (0..4).count { c -> cells.any { it.row == r && it.col == c && it.value != 0 } } }
        val fragmentedPenalty = perRowCounts.count { it in 1..2 }
        return filled * 5 + rowCoverage * 8 + rowFullness * 2 + rangeMatch * 4 - duplicatePenalty * 30 - gapPenalty * 5 - fragmentedPenalty * 3
    }

    private fun buildGridFromRangesFirst(
        valid: List<OcrItem>,
        minX: Float, maxX: Float, minY: Float, maxY: Float,
        logFinalParse: Boolean
    ): List<Pair<List<ScannedBingoCell>, Int>>? {
        val labels = listOf("B", "I", "N", "G", "O")
        val columnBuckets = List(5) { mutableListOf<OcrItem>() }
        for (item in valid) {
            val col = COLUMN_RANGES.indexOfFirst { item.value in it }
            if (col in 0..4) columnBuckets[col].add(item)
        }
        if (logFinalParse) Log.d(TAG, "bucket sizes before dedupe: ${labels.zip(columnBuckets.map { it.size }).joinToString { "${it.first}=${it.second}" }}")
        val spanY = (maxY - minY).coerceAtLeast(1f)
        val cellH = spanY / 5f
        val bucketSizesAfter = IntArray(5)
        for (col in 0..4) {
            val sorted = columnBuckets[col].sortedBy { it.centerY }
            bucketSizesAfter[col] = dedupeColumnByY(sorted, cellH).size
        }
        if (logFinalParse) Log.d(TAG, "bucket sizes after dedupe: ${labels.zip(bucketSizesAfter.toList()).joinToString { "${it.first}=${it.second}" }}")
        val gridTop = buildGridFromColumnRankOrder(valid, minX, maxX, minY, maxY, topToBottom = true)
        val gridBottom = buildGridFromColumnRankOrder(valid, minX, maxX, minY, maxY, topToBottom = false)
        val scoreTop = rowDirectionScore(gridTop)
        val scoreBottom = rowDirectionScore(gridBottom)
        if (logFinalParse) {
            Log.d(TAG, "row direction score topToBottom: $scoreTop")
            Log.d(TAG, "row direction score bottomToTop: $scoreBottom")
        }
        return listOf(gridTop to ORIENTATION_0, gridBottom to ORIENTATION_180)
    }

    fun parseCapturedTicket(
        ocrItems: List<OcrItem>,
        imageWidth: Int,
        imageHeight: Int
    ): ScannedBingoGrid? {
        val valid = ocrItems.filter { it.value in MIN..MAX }
        Log.d(TAG, "raw numeric candidates: ${valid.size}")
        if (valid.size < MIN_RAW_CANDIDATES_FOR_PARSE) return null
        val minX = valid.minOf { it.centerX }.coerceAtLeast(0f)
        val maxX = valid.maxOf { it.centerX }.coerceAtMost(imageWidth.toFloat())
        val minY = valid.minOf { it.centerY }.coerceAtLeast(0f)
        val maxY = valid.maxOf { it.centerY }.coerceAtMost(imageHeight.toFloat())
        val spanX = (maxX - minX).coerceAtLeast(1f)
        val spanY = (maxY - minY).coerceAtLeast(1f)
        val expandX = spanX * TICKET_BOUNDS_EXPAND_FRAC
        val expandY = spanY * TICKET_BOUNDS_EXPAND_FRAC
        val broadLeft = minX - expandX
        val broadRight = maxX + expandX
        val broadTop = minY - expandY
        val broadBottom = maxY + expandY
        val candidates = valid.filter { it.centerX in broadLeft..broadRight && it.centerY in broadTop..broadBottom }
            .sortedWith(compareBy({ it.value }, { it.centerX }, { it.centerY }))
        Log.d(TAG, "after broad ticket-local filtering: ${candidates.size}")
        if (candidates.size < MIN_PARTIAL_CELLS) return null
        Log.d(TAG, "final usable candidate count: ${candidates.size}")
        val cropMinX = candidates.minOf { it.centerX }
        val cropMaxX = candidates.maxOf { it.centerX }
        val cropMinY = candidates.minOf { it.centerY }
        val cropMaxY = candidates.maxOf { it.centerY }
        val rawText = candidates.joinToString(" ") { it.value.toString() }
        val variants = parseCapturedTicketVariantsInternal(candidates, cropMinX, cropMaxX, cropMinY, cropMaxY, rawText, logFinalParse = true)
        val best = variants.maxByOrNull { it.score } ?: return null
        Log.d(TAG, "selected orientation=${best.grid.orientationDegrees}")
        val canonicalGrid = buildCanonicalGridFromNumbers(best.grid.toCanonicalColumnMajorList(), best.grid.rawText) ?: return null
        Log.d(TAG, "final canonical order first10=${canonicalGrid.toColumnMajorList().take(10).joinToString()}")
        return canonicalGrid
    }

    fun parseCapturedTicketVariants(
        ocrItems: List<OcrItem>,
        imageWidth: Int,
        imageHeight: Int
    ): List<FinalParseVariant> {
        val valid = ocrItems.filter { it.value in MIN..MAX }
        Log.d(TAG, "raw numeric candidates: ${valid.size}")
        if (valid.size < MIN_RAW_CANDIDATES_FOR_PARSE) return emptyList()
        val minX = valid.minOf { it.centerX }.coerceAtLeast(0f)
        val maxX = valid.maxOf { it.centerX }.coerceAtMost(imageWidth.toFloat())
        val minY = valid.minOf { it.centerY }.coerceAtLeast(0f)
        val maxY = valid.maxOf { it.centerY }.coerceAtMost(imageHeight.toFloat())
        val spanX = (maxX - minX).coerceAtLeast(1f)
        val spanY = (maxY - minY).coerceAtLeast(1f)
        val expandX = spanX * TICKET_BOUNDS_EXPAND_FRAC
        val expandY = spanY * TICKET_BOUNDS_EXPAND_FRAC
        val broadLeft = minX - expandX
        val broadRight = maxX + expandX
        val broadTop = minY - expandY
        val broadBottom = maxY + expandY
        val candidates = valid.filter { it.centerX in broadLeft..broadRight && it.centerY in broadTop..broadBottom }
            .sortedWith(compareBy({ it.value }, { it.centerX }, { it.centerY }))
        Log.d(TAG, "after broad ticket-local filtering: ${candidates.size}")
        if (candidates.size < MIN_PARTIAL_CELLS) return emptyList()
        val cropMinX = candidates.minOf { it.centerX }
        val cropMaxX = candidates.maxOf { it.centerX }
        val cropMinY = candidates.minOf { it.centerY }
        val cropMaxY = candidates.maxOf { it.centerY }
        val rawText = candidates.joinToString(" ") { it.value.toString() }
        return parseCapturedTicketVariantsInternal(candidates, cropMinX, cropMaxX, cropMinY, cropMaxY, rawText, logFinalParse = true)
    }

    private fun headerAnchorFromSpatialPositions(
        candidates: List<OcrItem>,
        cropMinY: Float,
        cropMaxY: Float
    ): String {
        val spanY = (cropMaxY - cropMinY).coerceAtLeast(1f)
        val topBandMaxY = cropMinY + HEADER_BAND_FRAC * spanY
        val bottomBandMinY = cropMaxY - HEADER_BAND_FRAC * spanY
        val topBand = candidates.filter { it.centerY <= topBandMaxY }
        val bottomBand = candidates.filter { it.centerY >= bottomBandMinY }
        val earlyRanges = listOf(B_RANGE, I_RANGE)
        val lateRanges = listOf(G_RANGE, O_RANGE)
        fun countEarly(items: List<OcrItem>) = items.count { item -> earlyRanges.any { item.value in it } }
        fun countLate(items: List<OcrItem>) = items.count { item -> lateRanges.any { item.value in it } }
        val topEarly = countEarly(topBand)
        val topLate = countLate(topBand)
        val bottomEarly = countEarly(bottomBand)
        val bottomLate = countLate(bottomBand)
        return when {
            topEarly >= MIN_SPATIAL_HEADER_COUNT && bottomLate >= MIN_SPATIAL_HEADER_COUNT && topEarly > topLate && bottomLate > bottomEarly -> "top"
            topLate >= MIN_SPATIAL_HEADER_COUNT && bottomEarly >= MIN_SPATIAL_HEADER_COUNT && topLate > topEarly && bottomEarly > bottomLate -> "bottom"
            else -> "none"
        }
    }

    private fun parseCapturedTicketVariantsInternal(
        candidates: List<OcrItem>,
        cropMinX: Float, cropMaxX: Float, cropMinY: Float, cropMaxY: Float,
        rawText: String,
        logFinalParse: Boolean
    ): List<FinalParseVariant> {
        val headerAnchorSpatial = headerAnchorFromSpatialPositions(candidates, cropMinY, cropMaxY)
        val tokens = rawText.uppercase().trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        val headerLetters = setOf('B', 'I', 'N', 'G', 'O')
        val mid = (tokens.size / 2).coerceAtLeast(0)
        val topTokens = tokens.take(mid)
        val bottomTokens = tokens.drop(mid)
        val topScore = topTokens.count { t -> t.any { c -> c in headerLetters } }
        val bottomScore = bottomTokens.count { t -> t.any { c -> c in headerLetters } }
        val headerAnchorToken = when {
            topScore > bottomScore && topScore >= 2 -> "top"
            bottomScore > topScore && bottomScore >= 2 -> "bottom"
            else -> "none"
        }
        val headerAnchor = if (headerAnchorSpatial != "none") headerAnchorSpatial else headerAnchorToken
        if (logFinalParse) {
            Log.d(TAG, "headerAnchorSpatial=$headerAnchorSpatial headerTopScore=$topScore headerBottomScore=$bottomScore")
            Log.d(TAG, "headerAnchor=$headerAnchor")
        }
        val sortedCandidates = candidates.sortedWith(compareBy({ it.value }, { it.centerX }, { it.centerY }))
        val result = buildGridFromLocalAxes(sortedCandidates, cropMinX, cropMaxX, cropMinY, cropMaxY, logFinalParse) ?: return emptyList()
        val spanY = (cropMaxY - cropMinY).coerceAtLeast(1f)
        val topBandY = cropMinY + HEADER_BAND_FRAC * spanY
        val countInTopBand = candidates.count { it.centerY <= topBandY }
        if (logFinalParse) {
            Log.d(TAG, "header tokens found: using number band (no letter OCR); top band count=$countInTopBand")
            Log.d(TAG, "header band: topBandY=$topBandY candidates in top band=$countInTopBand")
        }
        val variants = mutableListOf<FinalParseVariant>()
        var bestScore = -1
        var bestOrientation = ORIENTATION_0
        for (v in result) {
            val baseCells = v.cells
            val orientationDegrees = v.orientation
            val flipX = v.flipX
            val flipY = v.flipY
            val filled = baseCells.count { it.value != 0 }
            val columnsWithCoverage = (0..4).count { c -> (0..4).count { r -> baseCells.any { it.row == r && it.col == c && it.value != 0 } } >= MIN_CELLS_PER_COLUMN_FOR_COVERAGE }
            if (filled < MIN_FILLED_FOR_ACCEPT || columnsWithCoverage < MIN_COLUMNS_WITH_COVERAGE) continue
            val topBonus = v.assignment.sumOf { (item, row, _) -> if (item.centerY <= topBandY) (4 - row) else 0 }
            val bottomBonus = v.assignment.sumOf { (item, row, _) -> if (item.centerY <= topBandY) row else 0 }
            val orientationHeaderBonus = when (orientationDegrees) {
                ORIENTATION_180 -> bottomBonus
                ORIENTATION_0 -> topBonus
                else -> maxOf(topBonus, bottomBonus)
            }
            val headerBonus = if (headerAnchor == "none") 0 else HEADER_ANCHOR_WEIGHT * orientationHeaderBonus
            val geometryScore = rowGeometryScore(baseCells)
            val geometryComponent = if (headerAnchor == "none") GEOMETRY_WEIGHT * geometryScore else 0
            val grid = ScannedBingoGrid(
                cells = baseCells,
                rawText = rawText,
                orientationDegrees = orientationDegrees,
                isPartial = filled < MIN_FILLED_CELLS
            )
            val baseScore = unifiedFinalScore(grid)
            val score = baseScore + headerBonus + geometryComponent
            if (score > bestScore) {
                bestScore = score
                bestOrientation = orientationDegrees
            }
            val rowDir = "orientation_$orientationDegrees"
            if (logFinalParse) {
                Log.d(TAG, "candidate final score: $score row direction: $rowDir")
                Log.d(TAG, "final filled cell count: $filled")
                listOf("B", "I", "N", "G", "O").forEachIndexed { idx, label ->
                    val colVals = (0..4).map { r -> baseCells.find { it.row == r && it.col == idx }?.value ?: 0 }.filter { it != 0 }
                    Log.d(TAG, "Final $label column: ${colVals.joinToString()}")
                }
            }
            variants.add(FinalParseVariant(score = score, rowDirection = rowDir, grid = grid, flipX = flipX, flipY = flipY))
        }
        if (logFinalParse && variants.isNotEmpty()) {
            val bestVariant = variants.maxByOrNull { it.score }!!
            val chosenOri = bestVariant.grid.orientationDegrees
            val uprightV = result.find { it.orientation == chosenOri && !it.flipY }
            val upsideDownV = result.find { it.orientation == chosenOri && it.flipY }
            val uprightGeometryScore = uprightV?.let { rowGeometryScore(it.cells) } ?: 0
            val upsideDownGeometryScore = upsideDownV?.let { rowGeometryScore(it.cells) } ?: 0
            val chosenTicketOrientation = if (bestVariant.flipY) "upside_down" else "upright"
            Log.d(TAG, "uprightGeometryScore=$uprightGeometryScore upsideDownGeometryScore=$upsideDownGeometryScore")
            Log.d(TAG, "chosenTicketOrientation=$chosenTicketOrientation")
            val chosenV = result.find { it.cells == bestVariant.grid.cells && it.orientation == bestVariant.grid.orientationDegrees && it.flipX == bestVariant.flipX && it.flipY == bestVariant.flipY }
            val topBonus = chosenV?.assignment?.sumOf { (item, row, _) -> if (item.centerY <= topBandY) (4 - row) else 0 } ?: 0
            val bottomBonus = chosenV?.assignment?.sumOf { (item, row, _) -> if (item.centerY <= topBandY) row else 0 } ?: 0
            val headerAnchorChosen = when {
                topBonus > bottomBonus -> "top"
                bottomBonus > topBonus -> "bottom"
                else -> "none"
            }
            val usedHeaderAnchor = (topBonus + bottomBonus) > 0
            Log.d(TAG, "headerAnchor=$headerAnchorChosen")
            Log.d(TAG, "usedHeaderAnchor=$usedHeaderAnchor")
            Log.d(TAG, "chosen combo: orientation=${bestVariant.grid.orientationDegrees} flipX=${bestVariant.flipX} flipY=${bestVariant.flipY}")
            Log.d(TAG, "chosen ticket orientation: ${bestVariant.grid.orientationDegrees}")
            val bColVals = (0..4).map { r -> bestVariant.grid.cells.find { it.row == r && it.col == 0 }?.value ?: 0 }.filter { it != 0 }
            Log.d(TAG, "Final B column=${bColVals.joinToString()}")
            listOf("I", "N", "G", "O").forEachIndexed { idx, label ->
                val colVals = (0..4).map { r -> bestVariant.grid.cells.find { it.row == r && it.col == idx + 1 }?.value ?: 0 }.filter { it != 0 }
                Log.d(TAG, "Final $label column: ${colVals.joinToString()}")
            }
        }
        return variants
    }

    private fun unifiedFinalScore(grid: ScannedBingoGrid): Int = plausibilityScore(grid, logDetails = false)

    private fun rowGeometryScore(cells: List<ScannedBingoCell>): Int {
        val rowCoverage = (0..4).count { r -> (0..4).any { c -> cells.any { it.row == r && it.col == c && it.value != 0 } } }
        var score = rowCoverage * 6
        val rowCounts = (0..4).map { r -> (0..4).count { c -> cells.any { it.row == r && it.col == c && it.value != 0 } } }
        val rowBalance = 5 - (rowCounts.maxOrNull()!! - rowCounts.minOrNull()!!).coerceAtLeast(0)
        score += rowBalance * 2
        var gapPenalty = 0
        for (col in 0..4) {
            val rows = cells.filter { it.col == col && it.value != 0 }.map { it.row }.sorted()
            if (rows.isEmpty()) continue
            for (i in 0 until rows.size - 1) {
                val gap = rows[i + 1] - rows[i] - 1
                if (gap > 0) gapPenalty += gap * 4
            }
        }
        return score - gapPenalty
    }

    private fun remapGridToCanonicalDisplay(cells: List<ScannedBingoCell>, orientationDegrees: Int): List<ScannedBingoCell> {
        if (orientationDegrees == ORIENTATION_0) return cells
        val grid = Array(5) { IntArray(5) { 0 } }
        for (c in cells) grid[c.row][c.col] = c.value
        val out = Array(5) { IntArray(5) { 0 } }
        for (r in 0..4) for (col in 0..4) {
            val (newR, newC) = when (orientationDegrees) {
                ORIENTATION_180 -> 4 - r to 4 - col
                ORIENTATION_90 -> 4 - col to r
                ORIENTATION_270 -> col to 4 - r
                else -> r to col
            }
            out[newR][newC] = grid[r][col]
        }
        return (0..4).flatMap { r -> (0..4).map { c -> ScannedBingoCell(r, c, out[r][c]) } }
    }

    private fun canonicalizeGridForDisplay(grid: ScannedBingoGrid): ScannedBingoGrid =
        ScannedBingoGrid(
            cells = remapGridToCanonicalDisplay(grid.cells, grid.orientationDegrees),
            rawText = grid.rawText,
            orientationDegrees = 0,
            isPartial = grid.isPartial
        )

    private fun normalizeToBingoGrid(numbers: List<Int>): List<List<Int>>? {
        if (numbers.size != 25) return null
        if (numbers.distinct().size != 25) return null

        val columns = listOf(
            numbers.filter { it in 1..15 },
            numbers.filter { it in 16..30 },
            numbers.filter { it in 31..45 },
            numbers.filter { it in 46..60 },
            numbers.filter { it in 61..75 }
        )

        if (columns.any { it.size != 5 }) return null

        val sortedCols = columns.map { it.sorted() }
        return (0..4).map { row -> sortedCols.map { col -> col[row] } }
    }

    private fun canonicalRowsToCells(rows: List<List<Int>>): List<ScannedBingoCell> =
        rows.flatMapIndexed { rowIndex, row ->
            row.mapIndexed { colIndex, value -> ScannedBingoCell(rowIndex, colIndex, value) }
        }

    private fun buildCanonicalGridFromNumbers(
        numbers: List<Int>,
        rawText: String
    ): ScannedBingoGrid? {
        val normalizedGrid = normalizeToBingoGrid(numbers) ?: run {
            Log.d("BingoAnalyzer", "REJECT invalid canonical grid")
            return null
        }
        return ScannedBingoGrid(
            cells = canonicalRowsToCells(normalizedGrid),
            rawText = rawText,
            orientationDegrees = 0,
            isPartial = false
        )
    }

    private data class SourceOrientationScore(
        val orientation: Int,
        val validCellCount: Int,
        val validColumnPlacements: Int,
        val uniqueValueCount: Int,
        val existingScore: Int,
        val canonicalNumbers: List<Int>
    )

    private fun scoreSourceOrientation(variant: FinalParseVariant?): SourceOrientationScore? {
        if (variant == null) return null
        val canonicalNumbers = variant.grid.toCanonicalColumnMajorList()
        val validCellCount = canonicalNumbers.withIndex().count { (index, value) ->
            value != 0 && value in columnRangeForIndex(index / 5)
        }
        val validColumnPlacements = (0..4).count { col ->
            val values = canonicalNumbers.subList(col * 5, col * 5 + 5).filter { it != 0 }
            values.isNotEmpty() && values.all { it in columnRangeForIndex(col) }
        }
        val uniqueValueCount = canonicalNumbers.filter { it != 0 }.toSet().size
        return SourceOrientationScore(
            orientation = variant.grid.orientationDegrees,
            validCellCount = validCellCount,
            validColumnPlacements = validColumnPlacements,
            uniqueValueCount = uniqueValueCount,
            existingScore = variant.score,
            canonicalNumbers = canonicalNumbers
        )
    }

    private fun columnRangeForIndex(index: Int): IntRange = when (index) {
        0 -> 1..15
        1 -> 16..30
        2 -> 31..45
        3 -> 46..60
        else -> 61..75
    }

    private fun SourceOrientationScore.isBetterThan(other: SourceOrientationScore): Boolean =
        when {
            validCellCount != other.validCellCount -> validCellCount > other.validCellCount
            validColumnPlacements != other.validColumnPlacements -> validColumnPlacements > other.validColumnPlacements
            uniqueValueCount != other.uniqueValueCount -> uniqueValueCount > other.uniqueValueCount
            existingScore != other.existingScore -> existingScore > other.existingScore
            else -> orientation == ORIENTATION_180 && other.orientation != ORIENTATION_180
        }

    private fun cellValueAt(grid: ScannedBingoGrid, row: Int, col: Int): Int =
        grid.cells.find { it.row == row && it.col == col }?.value ?: 0

    private fun stabilityAgreementScore(variant: FinalParseVariant, pool: List<FinalParseVariant>): Int {
        var sum = 0
        for (r in 0..4) for (c in 0..4) {
            val v = cellValueAt(variant.grid, r, c)
            if (v == 0) continue
            for (other in pool) {
                if (other === variant) continue
                if (cellValueAt(other.grid, r, c) == v) sum++
            }
        }
        return sum
    }

    fun stabilizeFromVariants(variants: List<FinalParseVariant>, logStability: Boolean = true): FinalParseVariant? {
        if (variants.isEmpty()) return null
        val chosenRaw = if (variants.size == 1) variants.single() else {
            val bestScore = variants.maxOf { it.score }
            val close = variants.filter { it.score >= bestScore - STABILITY_TIE_MARGIN }
            close.maxWithOrNull(compareBy<FinalParseVariant> { stabilityAgreementScore(it, close) }.thenBy { it.score }) ?: close.maxByOrNull { it.score }!!
        }
        val candidate0 = variants.filter { it.grid.orientationDegrees == ORIENTATION_0 }.maxByOrNull { it.score }
        val candidate180 = variants.filter { it.grid.orientationDegrees == ORIENTATION_180 }.maxByOrNull { it.score }
        val score0 = scoreSourceOrientation(candidate0)
        val score180 = scoreSourceOrientation(candidate180)
        val winner = when {
            score0 != null && score180 != null -> if (score180.isBetterThan(score0)) score180 else score0
            score180 != null -> score180
            else -> score0
        }
        if (logStability) {
            Log.d(TAG, "source candidate 0 present: ${candidate0 != null}")
            Log.d(TAG, "source candidate 180 present: ${candidate180 != null}")
            Log.d(TAG, "source score 0: ${score0?.let { "cells=${it.validCellCount},cols=${it.validColumnPlacements},unique=${it.uniqueValueCount},score=${it.existingScore}" } ?: "none"}")
            Log.d(TAG, "source score 180: ${score180?.let { "cells=${it.validCellCount},cols=${it.validColumnPlacements},unique=${it.uniqueValueCount},score=${it.existingScore}" } ?: "none"}")
            Log.d(TAG, "source winner: ${winner?.orientation ?: chosenRaw.grid.orientationDegrees}")
            Log.d(TAG, "chosen combo: orientation=${chosenRaw.grid.orientationDegrees} flipX=${chosenRaw.flipX} flipY=${chosenRaw.flipY}")
            Log.d(TAG, "chosen orientation: ${chosenRaw.grid.orientationDegrees}")
            Log.d(TAG, "normalizationApplied: true")
            Log.d(TAG, "orientation applied in final remap: false")
            Log.d(TAG, "finalRemapApplied: false")
            Log.d(TAG, "final chosen score: ${chosenRaw.score}")
            Log.d(TAG, "final filled cell count: ${chosenRaw.grid.cells.count { it.value != 0 }}")
            if (variants.size > 1) Log.d(TAG, "stability agreement score: ${stabilityAgreementScore(chosenRaw, variants.filter { it.score >= variants.maxOf { it.score } - STABILITY_TIE_MARGIN })}")
            listOf("B", "I", "N", "G", "O").forEachIndexed { idx, label ->
                val colVals = (0..4).map { r -> cellValueAt(chosenRaw.grid, r, idx) }.filter { it != 0 }
                Log.d(TAG, "Final $label column: ${colVals.joinToString()}")
            }
        }
        val selected = when {
            winner == null -> chosenRaw
            winner.orientation == chosenRaw.grid.orientationDegrees -> chosenRaw
            winner.orientation == ORIENTATION_180 && candidate180 != null -> candidate180
            winner.orientation == ORIENTATION_0 && candidate0 != null -> candidate0
            else -> chosenRaw
        }
        val canonicalGrid = buildCanonicalGridFromNumbers(selected.grid.toCanonicalColumnMajorList(), selected.grid.rawText) ?: return null
        return selected.copy(grid = canonicalGrid)
    }

    fun parseOcrToGridWithPositions(
        ocrItems: List<OcrItem>,
        imageWidth: Int,
        imageHeight: Int
    ): ScannedBingoGrid? {
        val valid = ocrItems.filter { it.value in MIN..MAX }
        if (valid.size < MIN_PARTIAL_CELLS) return null
        val minX = valid.minOf { it.centerX }.coerceAtLeast(0f)
        val maxX = valid.maxOf { it.centerX }.coerceAtMost(imageWidth.toFloat())
        val minY = valid.minOf { it.centerY }.coerceAtLeast(0f)
        val maxY = valid.maxOf { it.centerY }.coerceAtMost(imageHeight.toFloat())
        val rawText = valid.joinToString(" ") { it.value.toString() }
        val result = inferOrientationAndBuildGrid(valid, minX, maxX, minY, maxY, rawText, logFinalParse = false) ?: return null
        val (baseCells, _) = result
        return buildCanonicalGridFromNumbers(baseCells.map { it.value }, rawText)
    }

    fun parseOcrToGrid(ocrText: String?): ScannedBingoGrid? {
        if (ocrText.isNullOrBlank()) return null
        val raw = ocrText.trim().replace(Regex("\\s+"), " ")
        val numbers = Regex("\\d+").findAll(raw).mapNotNull { it.value.toIntOrNull() }.filter { it in MIN..MAX }.toList()
        if (numbers.size < 24) return null

        val byColumn = List(5) { mutableListOf<Int>() }
        for (n in numbers) {
            val col = COLUMN_RANGES.indexOfFirst { n in it }
            if (col in 0..4 && byColumn[col].size < 5) byColumn[col].add(n)
        }

        for (col in 0..4) {
            val expected = if (col == 2) 4 else 5
            if (byColumn[col].size < expected) return null
            if (byColumn[col].size > 5) return null
        }
        if (byColumn[2].size == 4) {
            byColumn[2].sort()
            byColumn[2].add(2, 0)
        } else {
            byColumn[2].sort()
        }
        for (col in listOf(0, 1, 3, 4)) byColumn[col].sort()

        val baseCells = mutableListOf<ScannedBingoCell>()
        for (col in 0..4) {
            for (row in 0..4) {
                baseCells.add(ScannedBingoCell(row, col, byColumn[col][row]))
            }
        }
        val base = ScannedBingoGrid(cells = baseCells, rawText = raw, orientationDegrees = 0)
        val variants = listOf(
            base,
            ScannedBingoGrid(cells = rotate90(base.cells), rawText = raw, orientationDegrees = 90),
            ScannedBingoGrid(cells = rotate180(base.cells), rawText = raw, orientationDegrees = 180),
            ScannedBingoGrid(cells = rotate270(base.cells), rawText = raw, orientationDegrees = 270)
        )
        var best: ScannedBingoGrid? = null
        var bestScore = -1
        for (g in variants) {
            val (valid, score) = scoreGrid(g.cells)
            if (valid && score > bestScore) {
                bestScore = score
                best = g
            }
        }
        return best?.let { buildCanonicalGridFromNumbers(it.toCanonicalColumnMajorList(), raw) }
    }

    private fun valueAt(cells: List<ScannedBingoCell>, row: Int, col: Int): Int =
        cells.find { it.row == row && it.col == col }?.value ?: 0

    private fun rotate90(cells: List<ScannedBingoCell>): List<ScannedBingoCell> =
        (0..4).flatMap { r -> (0..4).map { c -> ScannedBingoCell(r, c, valueAt(cells, 4 - c, r)) } }

    private fun rotate180(cells: List<ScannedBingoCell>): List<ScannedBingoCell> =
        (0..4).flatMap { r -> (0..4).map { c -> ScannedBingoCell(r, c, valueAt(cells, 4 - r, 4 - c)) } }

    private fun rotate270(cells: List<ScannedBingoCell>): List<ScannedBingoCell> =
        (0..4).flatMap { r -> (0..4).map { c -> ScannedBingoCell(r, c, valueAt(cells, c, 4 - r)) } }

    private fun scoreGrid(cells: List<ScannedBingoCell>): Pair<Boolean, Int> {
        var score = 0
        for (col in 0..4) {
            val range = COLUMN_RANGES[col]
            for (row in 0..4) {
                val v = valueAt(cells, row, col)
                if (v == 0) continue
                if (row == 2 && col == 2) { score += 1; continue }
                if (v !in range) return false to 0
                score += 1
            }
        }
        return (score >= MIN_FILLED_CELLS) to score
    }
}
