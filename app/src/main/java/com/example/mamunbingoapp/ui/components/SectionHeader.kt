package com.example.mamunbingoapp.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.mamunbingoapp.theme.Dimens

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    AppSectionTitle(
        text = title,
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = Dimens.spacing12),
    )
}
