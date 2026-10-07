package com.example.mamunbingoapp.ui

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mamunbingoapp.domain.model.BingoScanType
import com.example.mamunbingoapp.theme.MamunBingoTheme
import com.example.mamunbingoapp.ui.screens.scan.ScanScreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScanHeroInstrumentedTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun autoScanHeroDoesNotReplacePrimaryActions() {
        var launchedType: BingoScanType? = null
        var manual = false
        composeRule.setContent {
            MamunBingoTheme {
                ScanScreen(
                    onBackClick = {},
                    onLaunchCamera = { launchedType = it },
                    onOpenNumberPad = { manual = true },
                )
            }
        }
        composeRule.onNodeWithContentDescription("Better scan tips").assertHasClickAction().performClick()
        composeRule.onNodeWithText("Better scan tips").assertExists()
        composeRule.onNodeWithText("OK").performClick()

        composeRule.onNodeWithText("Launch Camera").assertHasClickAction().performClick()
        composeRule.onNodeWithText("Play Sheet").assertHasClickAction().performClick()
        assertEquals(BingoScanType.PLAY_PAPER, launchedType)

        composeRule.onNodeWithText("Open Number Pad").assertHasClickAction().performClick()
        assertTrue(manual)
    }
}
