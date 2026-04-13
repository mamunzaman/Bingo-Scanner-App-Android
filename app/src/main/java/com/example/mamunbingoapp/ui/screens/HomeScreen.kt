package com.example.mamunbingoapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusWeak
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.example.mamunbingoapp.ui.components.iosElevatedShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mamunbingoapp.R
import com.example.mamunbingoapp.theme.AppTextStyles
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.theme.GreenImpactBg
import com.example.mamunbingoapp.ui.components.AppBottomBar
import com.example.mamunbingoapp.ui.components.AppHeaderPageLayout
import com.example.mamunbingoapp.ui.components.AppSectionHeader
import com.example.mamunbingoapp.ui.components.AppTab
import com.example.mamunbingoapp.ui.components.AppTopBar
import com.example.mamunbingoapp.ui.components.JackpotHeroCard
import com.example.mamunbingoapp.ui.components.StatusChip
import com.example.mamunbingoapp.ui.components.TicketCard
import com.example.mamunbingoapp.ui.components.TicketStatus

@Composable
fun HomeScreen(
    onScanClick: () -> Unit = {},
    onQuickActionClick: (String) -> Unit = {},
    onTicketClick: (String) -> Unit = {},
    onViewAllTickets: () -> Unit = {},
    onTabSelected: (AppTab) -> Unit = {},
    showBottomBar: Boolean = true
) {
    AppHeaderPageLayout(
        topBar = {
        AppTopBar(
            title = "",
            titleContent = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(
                                2.dp,
                                MaterialTheme.colorScheme.primary,
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.size(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.home_welcome),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Alex Rivers",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            },
            actions = {
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        )
        },
        content = {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.screenHorizontalPadding)
                .padding(top = Dimens.spacing8, bottom = Dimens.spacing16)
        ) {
            JackpotHeroCard(
                jackpotAmount = "$5,000,000",
                nextDrawText = stringResource(R.string.home_next_draw),
                onScanClick = onScanClick
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = stringResource(R.string.home_quick_actions),
                style = AppTextStyles.sectionTitle,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickActionButton("Scan", Icons.Filled.CenterFocusWeak) { onQuickActionClick("scan") }
                QuickActionButton("Tickets", Icons.Filled.ConfirmationNumber) { onQuickActionClick("tickets") }
                QuickActionButton("Results", Icons.Filled.EmojiEvents) { onQuickActionClick("results") }
                QuickActionButton("Help", Icons.AutoMirrored.Filled.Help) { onQuickActionClick("help") }
            }
            Spacer(modifier = Modifier.height(32.dp))
            AppSectionHeader(
                title = stringResource(R.string.home_active_tickets),
                actionText = stringResource(R.string.home_view_all),
                onActionClick = onViewAllTickets
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TicketCard("#4829", "$25.00", "Oct 24", TicketStatus.Won) { onTicketClick("#4829") }
                TicketCard("#5102", "---", "Today", TicketStatus.Pending) { onTicketClick("#5102") }
                TicketCard("#3991", "$0.00", "Oct 22", TicketStatus.Lost) { onTicketClick("#3991") }
            }
            Spacer(modifier = Modifier.height(32.dp))
            GreenImpactCard(treesPlanted = 120, treesToMilestone = 30)
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = stringResource(R.string.home_eco_news),
                style = AppTextStyles.sectionTitle,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            EcoNewsItem(
                title = stringResource(R.string.home_news_1),
                meta = stringResource(R.string.home_news_1_meta)
            )
            Spacer(modifier = Modifier.height(16.dp))
            EcoNewsItem(
                title = stringResource(R.string.home_news_2),
                meta = stringResource(R.string.home_news_2_meta)
            )
        }
        if (showBottomBar) {
            AppBottomBar(selectedTab = AppTab.Home, onTabSelected = onTabSelected)
        }
        }
    )
}

@Composable
private fun QuickActionButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .iosElevatedShadow(shape = CircleShape)
                .background(MaterialTheme.colorScheme.surface, CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun GreenImpactCard(
    treesPlanted: Int,
    treesToMilestone: Int,
    modifier: Modifier = Modifier
) {
    val progress = 0.75f
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(GreenImpactBg)
    ) {
        Icon(
            imageVector = Icons.Filled.Forest,
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(180.dp)
                .offset(x = 40.dp, y = 40.dp),
            tint = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)
        )
        Column(
            modifier = Modifier.padding(Dimens.spacing16)
        ) {
            Text(
                text = stringResource(R.string.home_green_impact),
                style = AppTextStyles.sectionTitle,
                color = MaterialTheme.colorScheme.surface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.home_trees_planted, treesPlanted),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.home_milestone, treesToMilestone),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun EcoNewsItem(
    title: String,
    meta: String,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .iosElevatedShadow(shape = shape)
            .background(MaterialTheme.colorScheme.surface, shape)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = meta,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
