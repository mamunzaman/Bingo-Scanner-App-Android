package com.example.mamunbingoapp.ui.core

import com.example.mamunbingoapp.R
import com.example.mamunbingoapp.theme.opaqueNavigationBarColor
import com.example.mamunbingoapp.ui.components.AppBottomBarControlHeight
import com.example.mamunbingoapp.ui.components.AppBottomBarFadeHeight
import com.example.mamunbingoapp.ui.components.AppBottomBarScrollExtraPadding
import com.example.mamunbingoapp.ui.components.AppTab
import com.example.mamunbingoapp.ui.core.interaction.AppMotion
import com.example.mamunbingoapp.ui.core.interaction.isAnimatorScaleEnabled
import com.example.mamunbingoapp.ui.core.interaction.livePlayHaloFrame
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppMotionTest {

    @Test
    fun bottomNavigationExposesFiveDestinations() {
        assertEquals(5, AppTab.entries.size)
        assertEquals(
            listOf("home", "scan", "jackpot", "projects", "profile"),
            AppTab.entries.map { it.route },
        )
    }

    @Test
    fun livePlayKeepsSemanticLabelResource() {
        assertEquals(R.string.tab_jackpot, AppTab.Jackpot.labelResId)
    }

    @Test
    fun reducedMotionDisablesAnimatorScale() {
        assertFalse(isAnimatorScaleEnabled(0f))
        assertTrue(isAnimatorScaleEnabled(1f))
        assertTrue(isAnimatorScaleEnabled(0.5f))
    }

    @Test
    fun selectedAndUnselectedShareAnimatedHaloSource() {
        val unselectedStart = livePlayHaloFrame(0f, selected = false, animationsEnabled = true)
        val selectedStart = livePlayHaloFrame(0f, selected = true, animationsEnabled = true)
        val unselectedEnd = livePlayHaloFrame(1f, selected = false, animationsEnabled = true)
        val selectedEnd = livePlayHaloFrame(1f, selected = true, animationsEnabled = true)
        assertTrue(unselectedStart.animated)
        assertTrue(selectedStart.animated)
        assertNotEquals(unselectedStart.alpha, unselectedEnd.alpha)
        assertNotEquals(selectedStart.alpha, selectedEnd.alpha)
        assertEquals(unselectedStart.scale, selectedStart.scale)
        assertTrue(selectedStart.alpha < unselectedStart.alpha)
    }

    @Test
    fun reducedMotionHaloIsStatic() {
        val a = livePlayHaloFrame(0f, selected = true, animationsEnabled = false)
        val b = livePlayHaloFrame(1f, selected = false, animationsEnabled = false)
        assertFalse(a.animated)
        assertEquals(a.alpha, b.alpha)
        assertEquals(a.scale, b.scale)
    }

    @Test
    fun systemNavigationBarIsOpaqueInBothThemes() {
        assertEquals(1f, opaqueNavigationBarColor(darkTheme = false).alpha)
        assertEquals(1f, opaqueNavigationBarColor(darkTheme = true).alpha)
        assertNotEquals(0f, opaqueNavigationBarColor(false).alpha)
    }

    @Test
    fun appNavControlsAreOpaqueAndFadeStartsTransparent() {
        assertEquals(0.25f, AppMotion.BottomBarFadeFirstAlpha)
        assertEquals(1.0f, AppMotion.BottomBarFadeEndAlpha)
        assertEquals(1.0f, AppMotion.BottomBarControlAlpha)
        assertTrue(AppBottomBarFadeHeight.value in 32f..48f)
    }

    @Test
    fun sharedScrollPaddingExcludesFadeHaloAndSystemInset() {
        assertEquals(AppBottomBarControlHeight + com.example.mamunbingoapp.theme.Dimens.spacing12, AppBottomBarScrollExtraPadding)
        assertTrue(AppBottomBarScrollExtraPadding > AppBottomBarControlHeight)
        assertTrue(AppBottomBarScrollExtraPadding < AppBottomBarControlHeight + AppBottomBarFadeHeight)
        assertTrue(AppBottomBarScrollExtraPadding.value < 76f + 72f)
        assertTrue(AppMotion.HaloMs in 1_900..2_400)
    }
}
