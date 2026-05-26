package com.example.mamunbingoapp.navigation

import android.os.Bundle

data class ManualEntryTicketMeta(
    val losNumber: String? = null,
    val serialNumber: String? = null,
    val sheetName: String? = null,
)

fun Bundle?.parseManualEntryTicketMeta(): ManualEntryTicketMeta {
    val los = this?.getString("losNumber")?.trim()?.takeIf { it.isNotEmpty() }
    val ser = this?.getString("serialNumber")?.trim()?.takeIf { it.isNotEmpty() }
    val sheet = this?.getString("sheetName")?.trim()?.takeIf { it.isNotEmpty() }
    return ManualEntryTicketMeta(losNumber = los, serialNumber = ser, sheetName = sheet)
}
