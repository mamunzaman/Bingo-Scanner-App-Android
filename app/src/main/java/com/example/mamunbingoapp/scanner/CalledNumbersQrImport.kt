package com.example.mamunbingoapp.scanner

import com.example.mamunbingoapp.domain.qr.CalledNumbersQrCodec

enum class CalledNumbersQrImportAction {
    REPLACE,
    ADD_MISSING,
    CANCEL,
}

sealed interface CalledNumbersQrImportPlan {
    data class Replace(val numbers: List<Int>) : CalledNumbersQrImportPlan
    data class Append(val numbers: List<Int>) : CalledNumbersQrImportPlan
    data object NothingAdded : CalledNumbersQrImportPlan
    data object NoChange : CalledNumbersQrImportPlan
}

object CalledNumbersQrImport {
    fun plan(
        action: CalledNumbersQrImportAction,
        existing: List<Int>,
        imported: List<Int>,
    ): CalledNumbersQrImportPlan {
        val normalizedImported = CalledNumbersQrCodec.normalize(imported)
        val normalizedExisting = CalledNumbersQrCodec.normalize(existing)
        return when (action) {
            CalledNumbersQrImportAction.CANCEL -> CalledNumbersQrImportPlan.NoChange
            CalledNumbersQrImportAction.REPLACE -> CalledNumbersQrImportPlan.Replace(normalizedImported)
            CalledNumbersQrImportAction.ADD_MISSING -> {
                val existingSet = normalizedExisting.toSet()
                val missing = normalizedImported.filter { it !in existingSet }
                if (missing.isEmpty()) CalledNumbersQrImportPlan.NothingAdded
                else CalledNumbersQrImportPlan.Append(missing)
            }
        }
    }
}
