package com.example.mamunbingoapp.ui.screens.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.ui.components.AppHeaderPageLayout
import com.example.mamunbingoapp.ui.components.AppTopBar

@Composable
fun MyAccountScreen(onBack: () -> Unit) {
    AppHeaderPageLayout(
        topBar = { AppTopBar(title = "My Account", showBack = true, onBackClick = onBack) },
        content = {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(top = Dimens.spacing5)
                    .padding(horizontal = Dimens.screenHorizontalPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Coming soon",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}
