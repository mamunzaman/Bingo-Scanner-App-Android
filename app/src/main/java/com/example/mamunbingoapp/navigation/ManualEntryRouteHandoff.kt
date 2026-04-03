package com.example.mamunbingoapp.navigation

import android.os.Bundle

data class ManualEntryTicketMeta(
    val losNumber: String? = null,
    val serialNumber: String? = null,
)

fun Bundle?.parseManualEntryTicketMeta(): ManualEntryTicketMeta {
    val los = this?.getString("losNumber")?.trim()?.takeIf { it.isNotEmpty() }
    val ser = this?.getString("serialNumber")?.trim()?.takeIf { it.isNotEmpty() }
    return ManualEntryTicketMeta(losNumber = los, serialNumber = ser)
}
