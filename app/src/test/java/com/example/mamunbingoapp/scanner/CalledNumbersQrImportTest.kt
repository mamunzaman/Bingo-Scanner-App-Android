package com.example.mamunbingoapp.scanner

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalledNumbersQrImportTest {
    @Test
    fun replaceConfirmation_usesNormalizedImportedList() {
        val plan = CalledNumbersQrImport.plan(
            CalledNumbersQrImportAction.REPLACE,
            existing = listOf(1, 2, 3),
            imported = listOf(9, 9, 80, 11),
        )
        assertEquals(CalledNumbersQrImportPlan.Replace(listOf(9, 11)), plan)
    }

    @Test
    fun addMissingConfirmation_appendsOnlyNewNumbers() {
        val plan = CalledNumbersQrImport.plan(
            CalledNumbersQrImportAction.ADD_MISSING,
            existing = listOf(1, 9),
            imported = listOf(9, 11, 12),
        )
        assertEquals(CalledNumbersQrImportPlan.Append(listOf(11, 12)), plan)
    }

    @Test
    fun addMissingConfirmation_whenNothingNew_reportsNothingAdded() {
        val plan = CalledNumbersQrImport.plan(
            CalledNumbersQrImportAction.ADD_MISSING,
            existing = listOf(9, 11),
            imported = listOf(9, 11, 80),
        )
        assertEquals(CalledNumbersQrImportPlan.NothingAdded, plan)
    }

    @Test
    fun cancel_causesNoDatabaseChange() {
        val existing = listOf(4, 8, 15)
        val plan = CalledNumbersQrImport.plan(
            CalledNumbersQrImportAction.CANCEL,
            existing = existing,
            imported = listOf(20, 21),
        )
        assertEquals(CalledNumbersQrImportPlan.NoChange, plan)
        assertEquals(listOf(4, 8, 15), existing)
    }

    @Test
    fun qrToolsRemainAvailableOutsideLiveWindow() {
        assertTrue(com.example.mamunbingoapp.core.SundayBingoSchedule.canUseCalledNumbersQrTools())
    }
}
