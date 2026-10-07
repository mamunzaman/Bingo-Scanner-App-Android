package com.example.mamunbingoapp.ui.screens.scan

import com.example.mamunbingoapp.R
import com.example.mamunbingoapp.domain.model.BingoScanType
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScanTypeSheetMappingTest {

    @Test
    fun formatOrderIsPlayDigitalMasterThenGalleryIsSeparate() {
        assertEquals(
            listOf(BingoScanType.PLAY_PAPER, BingoScanType.ONLINE, BingoScanType.MAIN_SHEET),
            ScanTypeSheetFormats,
        )
        assertEquals(R.string.scan_type_play_paper_title, scanTypeTitleRes(BingoScanType.PLAY_PAPER))
        assertEquals(R.string.scan_type_online_title, scanTypeTitleRes(BingoScanType.ONLINE))
        assertEquals(R.string.scan_type_main_sheet_title, scanTypeTitleRes(BingoScanType.MAIN_SHEET))
        assertEquals(R.string.scan_type_gallery_title, R.string.scan_type_gallery_title)
        assertEquals("Play Sheet", BingoScanType.PLAY_PAPER.title)
        assertEquals(BingoScanType.PLAY_PAPER, BingoScanType.fromRouteValue("PLAY_PAPER"))
    }

    @Test
    fun englishAndGermanCopyExists() {
        val en = locate("src/main/res/values/strings_phase3.xml").readText()
        val de = locate("src/main/res/values-de/strings_phase3.xml").readText()
        listOf(
            "What are you scanning?",
            "Choose the format that matches your ticket",
            "Play Sheet",
            "Digital Sheet",
            "Master Sheet",
            "Already have a photo?",
            "Choose from Gallery",
        ).forEach { assertTrue(it, en.contains(it)) }
        listOf(
            "Was möchtest du scannen?",
            "Spielschein",
            "Digitaler Schein",
            "Masterschein",
            "Du hast bereits ein Foto?",
            "Aus Galerie auswählen",
        ).forEach { assertTrue(it, de.contains(it)) }
        assertFalse(en.contains("Player Sheet"))
        assertFalse(de.contains("Player Sheet"))
    }

    @Test
    fun thumbnailsAreNotReferencePhotos() {
        val thumbs = locate("src/main/java/com/example/mamunbingoapp/ui/screens/scan/ScanTypeThumbnails.kt").readText()
        listOf("IMG_3594", "IMG_3699", "image(3)", "Losnummer", "Serie").forEach { leak ->
            assertFalse(leak, thumbs.contains(leak))
        }
    }

    private fun locate(relative: String): File {
        val fromApp = File(relative)
        if (fromApp.exists()) return fromApp
        return File("app/$relative")
    }
}
