package com.example.mamunbingoapp.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.mamunbingoapp.R

private val NunitoFamily = runCatching {
    FontFamily(
        Font(R.font.nunito, FontWeight.Normal),
        Font(R.font.nunito, FontWeight.Medium),
        Font(R.font.nunito, FontWeight.SemiBold),
        Font(R.font.nunito, FontWeight.Bold),
        Font(R.font.nunito, FontWeight.ExtraBold)
    )
}.getOrElse { FontFamily.Default }

private val DMMonoFamily = runCatching {
    FontFamily(Font(R.font.dm_mono_regular, FontWeight.Normal))
}.getOrElse { FontFamily.Monospace }

object LiveFonts {
    val Nunito: FontFamily get() = NunitoFamily
    val DMMono: FontFamily get() = DMMonoFamily
}
