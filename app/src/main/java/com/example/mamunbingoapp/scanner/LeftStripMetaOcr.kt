package com.example.mamunbingoapp.scanner

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max

/**
 * LOS / serial OCR on the left strip only. Independent from grid OCR — no shared text or scoring.
 */
object LeftStripMetaOcr {

    private const val TAG = "LeftStripMetaOcr"

    private const val LEFT_STRIP_WIDTH_FRAC = 0.24f

    private val losAfterLabel = Regex(
        "(?:LOS|LOSNR|LOS\\s+NO|LOS\\s*NO|LOSNUMMER|LOS\\s*NUMMER)(?:\\s|[:#\\-])*([0-9]{1,8})\\b",
        RegexOption.IGNORE_CASE,
    )
    private val serialAfterLabel = Regex(
        "(?:SERIAL|S\\s*/\\s*N|\\bSN\\b|TICKET|SERIENNUMMER|SERIEN\\s*NUMMER)(?:\\s|[:#\\-])*([0-9]{4,18})\\b",
        RegexOption.IGNORE_CASE,
    )

    /** LOS / serial keywords (normalized uppercase) for proximity matching. */
    private val losProximity = Regex("LOS(?:NR|NO|NUMMER)?|LOS\\s+NO|LOSNUMMER", RegexOption.IGNORE_CASE)
    private val serialProximity =
        Regex("SERIAL|S\\s*/\\s*N|\\bSN\\b|TICKET|SERIENNUMMER|SERIEN\\s*NUMMER", RegexOption.IGNORE_CASE)

    private val losLabelSpatial = Regex(
        "LOSNUMMER|LOS\\s*NUMMER|LOS\\s*NR|LOS\\s+NR|LOSNR",
        RegexOption.IGNORE_CASE,
    )
    private val serialLabelSpatial = Regex(
        "SERIENNUMMER|SERIEN\\s*NUMMER",
        RegexOption.IGNORE_CASE,
    )

    private data class DigitRun(val digits: String, val cx: Float, val cy: Float)

    private data class SpatialMeta(
        val los: String?,
        val serial: String?,
        val debug: String,
    )

    fun detect(bitmap: Bitmap): Pair<String?, String?> {
        val w = bitmap.width
        val h = bitmap.height
        if (w < 8 || h < 8) return null to null
        val stripW = (w * LEFT_STRIP_WIDTH_FRAC).toInt().coerceIn(4, w)
        val strip = try {
            Bitmap.createBitmap(bitmap, 0, 0, stripW, h)
        } catch (_: Exception) {
            null
        } ?: return null to null
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        return try {
            val textChunks = mutableListOf<String>()
            val spatialParts = mutableListOf<SpatialMeta>()
            ocrVisionText(strip, recognizer)?.let {
                textChunks.add(buildLineOrderedPlainText(it))
                spatialParts.add(extractSpatialLosSerial(it, strip.width, strip.height, "0°"))
            }
            rotateStrip(strip, 90f)?.let { r ->
                try {
                    ocrVisionText(r, recognizer)?.let {
                        textChunks.add(buildLineOrderedPlainText(it))
                        spatialParts.add(extractSpatialLosSerial(it, r.width, r.height, "90°"))
                    }
                } finally {
                    if (!r.isRecycled) r.recycle()
                }
            }
            rotateStrip(strip, 270f)?.let { r ->
                try {
                    ocrVisionText(r, recognizer)?.let {
                        textChunks.add(buildLineOrderedPlainText(it))
                        spatialParts.add(extractSpatialLosSerial(it, r.width, r.height, "270°"))
                    }
                } finally {
                    if (!r.isRecycled) r.recycle()
                }
            }
            val combined = textChunks.joinToString("\n").trim()
            val normalized = normalizeMetaOcrText(combined)
            Log.d(TAG, "metaRaw=${combined.take(500)}")
            Log.d(TAG, "metaNormalized=${normalized.take(500)}")
            val mergedSpatial = mergeSpatialParts(spatialParts)
            Log.d(TAG, "metaCandidates=${mergedSpatial.debug.take(1200)}")
            var los = mergedSpatial.los
            var serial = mergedSpatial.serial
            if (los == null && serial == null) {
                val p = parseConfidentLosSerial(normalized)
                los = p.first
                serial = p.second
            } else {
                val p = parseConfidentLosSerial(normalized)
                los = los ?: p.first
                serial = serial ?: p.second
            }
            Log.d(TAG, "metaChosen los=$los serial=$serial")
            normalizePair(los, serial)
        } catch (e: Exception) {
            Log.d(TAG, "leftStrip OCR failed: ${e.message}")
            null to null
        } finally {
            recognizer.close()
            if (!strip.isRecycled) strip.recycle()
        }
    }

    private fun mergeSpatialParts(parts: List<SpatialMeta>): SpatialMeta {
        if (parts.isEmpty()) return SpatialMeta(null, null, "")
        val both = parts.firstOrNull { it.los != null && it.serial != null }
        if (both != null) {
            return SpatialMeta(both.los, both.serial, parts.joinToString(" | ") { it.debug })
        }
        val los = parts.firstOrNull { it.los != null }?.los
        val serial = parts.firstOrNull { it.serial != null }?.serial
        return SpatialMeta(los, serial, parts.joinToString(" | ") { it.debug })
    }

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

    private fun ocrVisionText(bitmap: Bitmap, recognizer: TextRecognizer): Text? {
        return try {
            val image = InputImage.fromBitmap(bitmap, 0)
            Tasks.await(recognizer.process(image))
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Spatial + length heuristics; [parseConfidentLosSerial] is used after when values still null.
     */
    private fun extractSpatialLosSerial(visionText: Text, stripW: Int, stripH: Int, rotLabel: String): SpatialMeta {
        val losCenters = collectLabelCenters(visionText, losLabelSpatial)
        val serCenters = collectLabelCenters(visionText, serialLabelSpatial)
        val runs = collectDigitRuns(visionText, stripW, stripH)
        val sb = StringBuilder()
        sb.append(rotLabel).append(" labels los=").append(losCenters.size).append(" ser=").append(serCenters.size)
        sb.append(" runs=").append(runs.size).append(':')
        runs.take(12).forEach { sb.append(' ').append(it.digits).append('@').append(it.cx.toInt()).append(',').append(it.cy.toInt()) }
        if (runs.isEmpty()) {
            return SpatialMeta(null, null, sb.toString())
        }
        var los: String? = null
        var serial: String? = null
        if (losCenters.isNotEmpty() || serCenters.isNotEmpty()) {
            val assignments = assignRunsByProximity(runs, losCenters, serCenters)
            los = assignments.first
            serial = assignments.second
            sb.append(" prox los=").append(los).append(" ser=").append(serial)
        }
        if (los == null && serial == null) {
            val fb = fallbackByLength(runs)
            los = fb.first
            serial = fb.second
            sb.append(" lenFb los=").append(los).append(" ser=").append(serial)
        }
        return SpatialMeta(los, serial, sb.toString())
    }

    private fun collectLabelCenters(visionText: Text, pattern: Regex): List<Pair<Float, Float>> {
        val out = mutableListOf<Pair<Float, Float>>()
        for (block in visionText.textBlocks) {
            for (line in block.lines) {
                val box = line.boundingBox ?: continue
                if (!pattern.containsMatchIn(line.text)) continue
                val cx = (box.left + box.right) / 2f
                val cy = (box.top + box.bottom) / 2f
                out.add(cx to cy)
            }
        }
        return out
    }

    private fun collectDigitRuns(visionText: Text, stripW: Int, stripH: Int): List<DigitRun> {
        val lineInfos = mutableListOf<Triple<String, Float, Float>>()
        val seen = mutableSetOf<String>()
        val out = mutableListOf<DigitRun>()
        val xTol = max(stripW * 0.14f, 8f)
        val yGapMax = max(stripH * 0.12f, 10f)
        for (block in visionText.textBlocks) {
            for (line in block.lines) {
                val box = line.boundingBox ?: continue
                val cx = (box.left + box.right) / 2f
                val cy = (box.top + box.bottom) / 2f
                val t = line.text.trim()
                lineInfos.add(Triple(t, cx, cy))
                for (el in line.elements) {
                    val eb = el.boundingBox ?: continue
                    val ecx = (eb.left + eb.right) / 2f
                    val ecy = (eb.top + eb.bottom) / 2f
                    for (m in Regex("\\d+").findAll(el.text)) {
                        val v = m.value
                        if (v.length < 3) continue
                        val key = "e:$v@${ecx.toInt()}"
                        if (key in seen) continue
                        seen.add(key)
                        out.add(DigitRun(v, ecx, ecy))
                    }
                }
            }
        }
        lineInfos.sortBy { it.third }
        var i = 0
        while (i < lineInfos.size) {
            val (text, cx0, cy0) = lineInfos[i]
            if (text.matches(Regex("^\\d\$"))) {
                val buf = StringBuilder(text)
                var wsum = cx0
                var ysum = cy0
                var n = 1
                var j = i + 1
                while (j < lineInfos.size) {
                    val (tj, cxj, cyj) = lineInfos[j]
                    if (!tj.matches(Regex("^\\d\$"))) break
                    if (abs(cxj - cx0) > xTol) break
                    if (cyj - lineInfos[j - 1].third > yGapMax) break
                    buf.append(tj)
                    wsum += cxj
                    ysum += cyj
                    n++
                    j++
                }
                if (buf.length >= 3) {
                    val key = "v:${buf}@${(wsum / n).toInt()}"
                    if (key !in seen) {
                        seen.add(key)
                        out.add(DigitRun(buf.toString(), wsum / n, ysum / n))
                    }
                }
                i = j
                continue
            }
            for (m in Regex("\\d+").findAll(text)) {
                val v = m.value
                if (v.length < 3) continue
                val key = "l:$v@${cx0.toInt()},${cy0.toInt()}"
                if (key in seen) continue
                seen.add(key)
                out.add(DigitRun(v, cx0, cy0))
            }
            i++
        }
        return out.distinctBy { "${it.digits}_${it.cx.toInt()}_${it.cy.toInt()}" }
    }

    private fun nearestDist2(px: Float, py: Float, centers: List<Pair<Float, Float>>): Float {
        if (centers.isEmpty()) return Float.POSITIVE_INFINITY
        var best = Float.POSITIVE_INFINITY
        for ((x, y) in centers) {
            val dx = px - x
            val dy = py - y
            val d = dx * dx + dy * dy
            if (d < best) best = d
        }
        return best
    }

    private fun assignRunsByProximity(
        runs: List<DigitRun>,
        losCenters: List<Pair<Float, Float>>,
        serCenters: List<Pair<Float, Float>>,
    ): Pair<String?, String?> {
        if (runs.isEmpty()) return null to null
        if (losCenters.isNotEmpty() && serCenters.isEmpty()) {
            val five = runs.filter { it.digits.length == 5 }
                .minByOrNull { nearestDist2(it.cx, it.cy, losCenters) }
            val four = runs.filter { it.digits.length == 4 }
                .minByOrNull { nearestDist2(it.cx, it.cy, losCenters) }
            return five?.digits to four?.digits
        }
        if (serCenters.isNotEmpty() && losCenters.isEmpty()) {
            val four = runs.filter { it.digits.length == 4 }
                .minByOrNull { nearestDist2(it.cx, it.cy, serCenters) }
            val five = runs.filter { it.digits.length == 5 }
                .minByOrNull { nearestDist2(it.cx, it.cy, serCenters) }
            return five?.digits to four?.digits
        }
        val losCand = mutableListOf<Pair<DigitRun, Float>>()
        val serCand = mutableListOf<Pair<DigitRun, Float>>()
        for (r in runs) {
            val dL = nearestDist2(r.cx, r.cy, losCenters)
            val dS = nearestDist2(r.cx, r.cy, serCenters)
            val margin = max(dL, dS) * 0.03f + 9f
            when {
                abs(dL - dS) <= margin -> when (r.digits.length) {
                    5 -> losCand.add(r to dL)
                    4 -> serCand.add(r to dS)
                    else -> if (dL <= dS) losCand.add(r to dL) else serCand.add(r to dS)
                }
                dL < dS -> losCand.add(r to dL)
                else -> serCand.add(r to dS)
            }
        }
        val los = losCand.minByOrNull { it.second }?.first?.digits
        val serial = serCand.minByOrNull { it.second }?.first?.digits
        return normalizePair(los, serial)
    }

    private fun fallbackByLength(runs: List<DigitRun>): Pair<String?, String?> {
        val five = runs.filter { it.digits.length == 5 }
            .minByOrNull { it.cx }
        val four = runs.filter { it.digits.length == 4 }
            .minByOrNull { it.cx }
        if (five != null || four != null) return five?.digits to four?.digits
        val byLen = runs.sortedByDescending { it.digits.length }
        val a = byLen.getOrNull(0)
        val b = byLen.getOrNull(1)
        if (a != null && b != null && a.digits.length >= 5 && b.digits.length == 4) {
            val leftFirst = listOf(a, b).minBy { it.cx }
            val other = if (leftFirst == a) b else a
            return if (leftFirst.digits.length == 5) leftFirst.digits to other.digits
            else other.digits to leftFirst.digits
        }
        return null to null
    }

    private fun normalizeMetaOcrText(raw: String): String {
        var s = raw.uppercase(Locale.US)
        s = s.replace(Regex("[\\r\\n]+"), " ")
        s = s.replace(Regex("\\s+"), " ").trim()
        s = s.split(" ").joinToString(" ") { token -> applyOcrCharFixes(token) }
        return s.replace(Regex("\\s+"), " ").trim()
    }

    /**
     * O→0, I/L→1, S→5 in digit-adjacent positions (meta path only).
     */
    private fun applyOcrCharFixes(token: String): String {
        if (!token.any { it.isDigit() }) return token
        val u = token.uppercase(Locale.US)
        val chars = u.toCharArray()
        for (i in chars.indices) {
            val c = chars[i]
            val leftDigit = i > 0 && chars[i - 1].isDigit()
            val rightDigit = i < chars.lastIndex && chars[i + 1].isDigit()
            val nearDigit = leftDigit || rightDigit
            when {
                c == 'O' && nearDigit -> chars[i] = '0'
                c == 'I' && nearDigit -> chars[i] = '1'
                c == 'L' && nearDigit -> chars[i] = '1'
                c == 'S' && leftDigit && rightDigit -> chars[i] = '5'
                c == 'S' && leftDigit && i == chars.lastIndex -> chars[i] = '5'
                c == 'S' && rightDigit && i == 0 -> chars[i] = '5'
            }
        }
        return String(chars)
    }

    private fun parseConfidentLosSerial(normalized: String): Pair<String?, String?> {
        if (normalized.isEmpty()) return null to null
        var los = losAfterLabel.find(normalized)?.groupValues?.getOrNull(1)?.trim()
        var serial = serialAfterLabel.find(normalized)?.groupValues?.getOrNull(1)?.trim()
        if (los != null || serial != null) {
            return normalizePair(los, serial)
        }
        val near = extractRunsNearLabels(normalized)
        los = near.first ?: los
        serial = near.second ?: serial
        if (los != null || serial != null) {
            return normalizePair(los, serial)
        }
        val runs = Regex("\\d+").findAll(normalized).map { it.value }.filter { it.length in 3..18 }.distinct().toList()
        if (runs.size >= 2) {
            val sorted = runs.sortedByDescending { it.length }
            val longest = sorted[0]
            val second = sorted[1]
            if (longest.length >= 8 && second.length in 3..7 && longest != second) {
                return normalizePair(second, longest)
            }
        }
        return null to null
    }

    /**
     * Prefer digit runs immediately after / near LOS vs SERIAL/SN/TICKET windows.
     */
    private fun extractRunsNearLabels(normalized: String): Pair<String?, String?> {
        var los: String? = null
        var serial: String? = null
        val labelWindow = 36
        for (m in Regex("\\d+").findAll(normalized)) {
            val v = m.value
            if (v.length !in 1..18) continue
            val before = normalized.substring(0, m.range.first)
            val ctx = before.takeLast(labelWindow)
            val afterLos = losProximity.containsMatchIn(ctx) &&
                Regex("LOS(?:NR|NO|NUMMER|\\s+NO)?\\s*[:#\\s-]*$", RegexOption.IGNORE_CASE).containsMatchIn(ctx)
            val nearLos = losProximity.containsMatchIn(ctx) && v.length in 1..8
            val nearSerial = serialProximity.containsMatchIn(ctx) && v.length >= 4
            if (afterLos || (nearLos && los == null && v.length in 1..8)) {
                los = los ?: v
            } else if (nearSerial && serial == null) {
                serial = v
            }
        }
        return los to serial
    }

    private fun normalizePair(los: String?, serial: String?): Pair<String?, String?> {
        if (los != null && serial != null && los == serial) return null to null
        return los to serial
    }

    private fun rotateStrip(src: Bitmap, degrees: Float): Bitmap? {
        val m = Matrix()
        m.setRotate(degrees, src.width / 2f, src.height / 2f)
        val rect = RectF(0f, 0f, src.width.toFloat(), src.height.toFloat())
        m.mapRect(rect)
        val outW = rect.width().toInt().coerceAtLeast(1)
        val outH = rect.height().toInt().coerceAtLeast(1)
        m.postTranslate(-rect.left, -rect.top)
        return try {
            val out = Bitmap.createBitmap(outW, outH, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(out)
            canvas.drawBitmap(src, m, null)
            out
        } catch (_: Exception) {
            null
        }
    }
}
