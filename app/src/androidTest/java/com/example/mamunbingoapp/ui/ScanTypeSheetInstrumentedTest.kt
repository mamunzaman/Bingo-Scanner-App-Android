package com.example.mamunbingoapp.ui

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mamunbingoapp.domain.model.BingoScanType
import com.example.mamunbingoapp.theme.MamunBingoTheme
import com.example.mamunbingoapp.ui.screens.scan.ScanTypeSheetContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScanTypeSheetInstrumentedTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun threeFormatsThenGalleryInvokeCorrectCallbacks() {
        val types = mutableListOf<BingoScanType>()
        var gallery = 0
        composeRule.setContent {
            MamunBingoTheme {
                ScanTypeSheetContent(
                    onScanTypeSelected = { types += it },
                    onAddFromGallery = { gallery++ },
                )
            }
        }
        composeRule.onNodeWithText("What are you scanning?").assertExists()
        composeRule.onNodeWithText("Already have a photo?").assertExists()
        composeRule.onNodeWithContentDescription("Play Sheet, colorful printed BINGO ticket")
            .assertHasClickAction()
            .performClick()
        composeRule.onNodeWithContentDescription("Digital Sheet, BINGO grid shown on a screen")
            .assertHasClickAction()
            .performClick()
        composeRule.onNodeWithContentDescription("Master Sheet, long receipt with grid and QR code")
            .assertHasClickAction()
            .performClick()
        composeRule.onNodeWithContentDescription("Choose from Gallery")
            .assertHasClickAction()
            .performClick()
        assertEquals(
            listOf(BingoScanType.PLAY_PAPER, BingoScanType.ONLINE, BingoScanType.MAIN_SHEET),
            types,
        )
        assertEquals(1, gallery)
    }

    @Test
    fun eachRowExposesOneMergedAction() {
        composeRule.setContent {
            MamunBingoTheme {
                ScanTypeSheetContent(onScanTypeSelected = {}, onAddFromGallery = {})
            }
        }
        val play = composeRule.onNodeWithContentDescription("Play Sheet, colorful printed BINGO ticket")
            .fetchSemanticsNode()
        assertTrue(play.config.contains(SemanticsProperties.ContentDescription))
    }
}
