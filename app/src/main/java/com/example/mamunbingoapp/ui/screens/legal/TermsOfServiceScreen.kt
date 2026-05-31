package com.example.mamunbingoapp.ui.screens.legal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mamunbingoapp.R
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.theme.Slate400
import com.example.mamunbingoapp.ui.components.AppPrimaryButton
import com.example.mamunbingoapp.ui.components.AppHeaderBackground
import com.example.mamunbingoapp.ui.components.AppSectionTitle
import com.example.mamunbingoapp.ui.components.AppTopBar

private const val TERMS_URL = "https://www.bingo-umweltlotterie.de/teilnahmebedingungen"

@Composable
fun TermsOfServiceScreen(onBack: () -> Unit) {
    val uriHandler = LocalUriHandler.current

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        AppHeaderBackground(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.4f)
                .align(Alignment.TopCenter)
        )
        Column(Modifier.fillMaxSize()) {
            AppTopBar(
                title = stringResource(R.string.terms_title),
                showBack = true,
                onBackClick = onBack
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(top = Dimens.spacing5)
                    .padding(horizontal = Dimens.screenHorizontalPadding)
            ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.terms_main_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.terms_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LegalSection(stringResource(R.string.terms_section1_title), stringResource(R.string.terms_section1))
            LegalSection(stringResource(R.string.terms_section2_title), stringResource(R.string.terms_section2))
            LegalSection(stringResource(R.string.terms_section3_title), stringResource(R.string.terms_section3))
            LegalSection(stringResource(R.string.terms_section4_title), stringResource(R.string.terms_section4))
            LegalSection(stringResource(R.string.terms_section5_title), stringResource(R.string.terms_section5))
            LegalSection(stringResource(R.string.terms_section6_title), stringResource(R.string.terms_section6))
            LegalSection(stringResource(R.string.terms_section7_title), stringResource(R.string.terms_section7))
            LegalSection(stringResource(R.string.terms_section8_title), stringResource(R.string.terms_section8))
            Spacer(modifier = Modifier.height(24.dp))
            AppPrimaryButton(
                text = stringResource(R.string.terms_original_button),
                onClick = { uriHandler.openUri(TERMS_URL) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun LegalSection(title: String, content: String) {
    Spacer(modifier = Modifier.height(24.dp))
    AppSectionTitle(text = title)
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = content,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
