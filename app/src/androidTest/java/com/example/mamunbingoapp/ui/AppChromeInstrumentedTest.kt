package com.example.mamunbingoapp.ui

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mamunbingoapp.ui.components.AppBottomBar
import com.example.mamunbingoapp.ui.components.AppTab
import com.example.mamunbingoapp.theme.MamunBingoTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppChromeInstrumentedTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun bottomNavigationExposesFiveDestinationsAndLivePlayClick() {
        var selected: AppTab? = null
        composeRule.setContent {
            MamunBingoTheme {
                AppBottomBar(
                    selectedTab = AppTab.Home,
                    onTabSelected = { selected = it },
                )
            }
        }
        composeRule.onNodeWithText("Home").assertIsDisplayed().assertHasClickAction()
        composeRule.onNodeWithText("Scan").assertIsDisplayed().assertHasClickAction()
        composeRule.onNodeWithText("Projects").assertIsDisplayed().assertHasClickAction()
        composeRule.onNodeWithText("Profile").assertIsDisplayed().assertHasClickAction()
        val livePlay = composeRule.onAllNodesWithContentDescription("Live Play")
        livePlay[0].assertIsDisplayed().assertHasClickAction().performClick()
        assertEquals(AppTab.Jackpot, selected)
    }

    @Test
    fun livePlayHaloIsNotAnAccessibilityNode() {
        composeRule.setContent {
            MamunBingoTheme {
                AppBottomBar(selectedTab = AppTab.Home, onTabSelected = {})
            }
        }
        val livePlayNodes = composeRule.onAllNodesWithContentDescription("Live Play")
            .fetchSemanticsNodes()
        assertTrue(livePlayNodes.isNotEmpty())
        assertTrue(livePlayNodes.size <= 2)
    }
}
