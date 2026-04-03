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
import com.example.mamunbingoapp.ui.components.AppTopBar

private const val PRIVACY_URL = "https://www.bingo-umweltlotterie.de/datenschutz"

@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    val uriHandler = LocalUriHandler.current

    Box(Modifier.fillMaxSize()) {
        AppHeaderBackground(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.4f)
                .align(Alignment.TopCenter)
        )
        Column(Modifier.fillMaxSize()) {
            AppTopBar(
                title = "Datenschutz",
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
                text = stringResource(R.string.privacy_main_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.privacy_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            PrivacySection(stringResource(R.string.privacy_section1_title), stringResource(R.string.privacy_section1))
            PrivacySection(stringResource(R.string.privacy_section2_title), stringResource(R.string.privacy_section2))
            PrivacySection(stringResource(R.string.privacy_section3_title), stringResource(R.string.privacy_section3))
            PrivacySection(stringResource(R.string.privacy_section4_title), stringResource(R.string.privacy_section4))
            PrivacySection(stringResource(R.string.privacy_section5_title), stringResource(R.string.privacy_section5))
            PrivacySection(stringResource(R.string.privacy_section6_title), stringResource(R.string.privacy_section6))
            PrivacySection(stringResource(R.string.privacy_section7_title), stringResource(R.string.privacy_section7))
            PrivacySection(stringResource(R.string.privacy_section8_title), stringResource(R.string.privacy_section8))
            PrivacySection(stringResource(R.string.privacy_section9_title), stringResource(R.string.privacy_section9))
            PrivacySection(stringResource(R.string.privacy_section10_title), stringResource(R.string.privacy_section10))
            PrivacySection(stringResource(R.string.privacy_section11_title), stringResource(R.string.privacy_section11))
            PrivacySection(stringResource(R.string.privacy_section12_title), stringResource(R.string.privacy_section12))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.privacy_stand),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            AppPrimaryButton(
                text = stringResource(R.string.privacy_original_button),
                onClick = { uriHandler.openUri(PRIVACY_URL) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun PrivacySection(title: String, content: String) {
    Spacer(modifier = Modifier.height(24.dp))
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = content,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
