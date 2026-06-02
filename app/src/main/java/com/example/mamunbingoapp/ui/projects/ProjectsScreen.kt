package com.example.mamunbingoapp.ui.projects

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.mamunbingoapp.R
import com.example.mamunbingoapp.theme.Dimens
import com.example.mamunbingoapp.theme.Primary
import com.example.mamunbingoapp.theme.PrimaryDark
import com.example.mamunbingoapp.ui.components.AppHeaderPageLayout
import com.example.mamunbingoapp.ui.components.AppPrimaryButton
import com.example.mamunbingoapp.ui.components.AppPullRefresh
import com.example.mamunbingoapp.ui.components.AppSectionSurface
import com.example.mamunbingoapp.ui.components.AppSectionTitle
import com.example.mamunbingoapp.ui.components.AppTopBar
import com.example.mamunbingoapp.ui.components.appPremiumCardBorder
import com.example.mamunbingoapp.ui.components.iosElevatedShadow
import com.example.mamunbingoapp.ui.core.interaction.appClickable
import com.example.mamunbingoapp.viewmodel.ProjectsUiState
import com.example.mamunbingoapp.viewmodel.ProjectsViewModel

private val ProjectCardImageHeight = 150.dp
private val FeaturedSliderCardHeight = 280.dp
private val FeaturedSliderCardShape = RoundedCornerShape(24.dp)
private val ProjectCardShape = RoundedCornerShape(Dimens.radiusLarge)
private val ProjectContentOverlap = 10.dp
private val ProjectContentTopShape = RoundedCornerShape(
    topStart = Dimens.radiusLarge,
    topEnd = Dimens.radiusLarge,
)
private val ProjectMetaChipMinHeight = 28.dp

@Composable
fun ProjectsScreen(
    modifier: Modifier = Modifier,
    viewModel: ProjectsViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val refreshError by viewModel.refreshError.collectAsStateWithLifecycle()
    val lastUpdatedAtMillis by viewModel.lastUpdatedAtMillis.collectAsStateWithLifecycle()
    val context = LocalContext.current

    AppHeaderPageLayout(
        modifier = modifier.fillMaxSize(),
        topBar = {
            AppTopBar(title = stringResource(R.string.projects_nav_title))
        },
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = stringResource(R.string.projects_nav_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.screenHorizontalPadding)
                    .padding(bottom = Dimens.spacing8),
            )
            when (val state = uiState) {
            ProjectsUiState.Loading -> ProjectsLoadingState(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.screenHorizontalPadding),
            )
            is ProjectsUiState.Error -> ProjectsErrorState(
                message = state.message,
                onRetry = viewModel::loadProjects,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.screenHorizontalPadding),
            )
            ProjectsUiState.Empty -> AppPullRefresh(
                isRefreshing = isRefreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = Dimens.screenHorizontalPadding),
                ) {
                    ProjectsCacheStatus(
                        refreshError = refreshError,
                        lastUpdatedAtMillis = lastUpdatedAtMillis,
                        onRetryRefresh = viewModel::refresh,
                    )
                    ProjectsEmptyState(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                    )
                }
            }
            is ProjectsUiState.Success -> AppPullRefresh(
                isRefreshing = isRefreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                ProjectsList(
                    featuredProjects = state.featuredProjects,
                    recentProjects = state.recentProjects,
                    refreshError = refreshError,
                    lastUpdatedAtMillis = lastUpdatedAtMillis,
                    onRetryRefresh = viewModel::refresh,
                    onProjectClick = { url -> openProjectSourceUrl(context, url) },
                )
            }
            }
        }
    }
}

@Composable
private fun ProjectsCacheStatus(
    refreshError: String?,
    lastUpdatedAtMillis: Long?,
    onRetryRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.spacing8),
    ) {
        if (refreshError != null) {
            ProjectsRefreshErrorBanner(
                message = refreshError,
                onRetry = onRetryRefresh,
            )
        }
        if (lastUpdatedAtMillis != null) {
            ProjectsLastUpdatedText(updatedAtMillis = lastUpdatedAtMillis)
        }
    }
}

@Composable
private fun ProjectsLastUpdatedText(
    updatedAtMillis: Long,
    modifier: Modifier = Modifier,
) {
    val formatted = java.text.DateFormat.getDateTimeInstance(
        java.text.DateFormat.SHORT,
        java.text.DateFormat.SHORT,
        java.util.Locale.getDefault(),
    ).format(java.util.Date(updatedAtMillis))
    Text(
        text = stringResource(R.string.projects_last_updated, formatted),
        modifier = modifier.fillMaxWidth(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
    )
}

@Composable
private fun ProjectsList(
    featuredProjects: List<ProjectUiModel>,
    recentProjects: List<ProjectUiModel>,
    refreshError: String?,
    lastUpdatedAtMillis: Long?,
    onRetryRefresh: () -> Unit,
    onProjectClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Dimens.screenHorizontalPadding,
            end = Dimens.screenHorizontalPadding,
            top = 0.dp,
            bottom = Dimens.spacing32,
        ),
        verticalArrangement = Arrangement.spacedBy(Dimens.spacing20),
    ) {
        if (refreshError != null || lastUpdatedAtMillis != null) {
            item(key = "projects_cache_status") {
                ProjectsCacheStatus(
                    refreshError = refreshError,
                    lastUpdatedAtMillis = lastUpdatedAtMillis,
                    onRetryRefresh = onRetryRefresh,
                )
            }
        }
        if (featuredProjects.isNotEmpty()) {
            item(key = "featured_section_title") {
                AppSectionTitle(
                    text = stringResource(R.string.projects_featured_section),
                    modifier = Modifier.padding(bottom = Dimens.spacing12),
                )
            }
            item(key = "featured_slider") {
                FeaturedProjectsSlider(
                    projects = featuredProjects,
                    onProjectClick = onProjectClick,
                )
            }
        }
        if (recentProjects.isNotEmpty()) {
            if (featuredProjects.isNotEmpty()) {
                item(key = "recent_section_title") {
                    AppSectionTitle(
                        text = stringResource(R.string.projects_recent_section),
                        modifier = Modifier.padding(top = Dimens.spacing24, bottom = Dimens.spacing12),
                    )
                }
            }
            items(recentProjects, key = { it.id }) { project ->
                ProjectCard(
                    project = project,
                    onClick = { onProjectClick(project.sourceUrl) },
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FeaturedProjectsSlider(
    projects: List<ProjectUiModel>,
    onProjectClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(pageCount = { projects.size })
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.spacing12),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            pageSpacing = if (projects.size > 1) Dimens.spacing12 else 0.dp,
        ) { page ->
            FeaturedSliderCard(
                project = projects[page],
                onClick = { onProjectClick(projects[page].sourceUrl) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (projects.size > 1) {
            FeaturedPageIndicators(
                pageCount = projects.size,
                currentPage = pagerState.currentPage,
            )
        }
    }
}

@Composable
private fun FeaturedPageIndicators(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            val selected = index == currentPage
            Box(
                modifier = Modifier
                    .padding(horizontal = Dimens.spacing4)
                    .size(if (selected) 8.dp else 6.dp)
                    .clip(CircleShape)
                    .background(
                        if (selected) {
                            Primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
                        },
                    ),
            )
        }
    }
}

@Composable
private fun FeaturedSliderCard(
    project: ProjectUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(FeaturedSliderCardHeight)
            .iosElevatedShadow(elevation = 12.dp, shape = FeaturedSliderCardShape)
            .clip(FeaturedSliderCardShape)
            .appPremiumCardBorder(FeaturedSliderCardShape)
            .appClickable(onClick = onClick),
    ) {
        if (project.imageUrl != null) {
            AsyncImage(
                model = project.imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Newspaper,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0f to Color.Transparent,
                            0.38f to Color.Transparent,
                            0.58f to PrimaryDark.copy(alpha = 0.28f),
                            0.78f to Color.Black.copy(alpha = 0.58f),
                            1f to Color.Black.copy(alpha = 0.84f),
                        ),
                    ),
                ),
        )
        FeaturedProjectBadge(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(Dimens.spacing12),
        )
        project.fundingAmount?.let { amount ->
            ProjectFundingHighlightChip(
                amount = amount,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(Dimens.spacing12),
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(Dimens.spacing14),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacing8),
                verticalAlignment = Alignment.Bottom,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Dimens.spacing8),
                ) {
                    Text(
                        text = project.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = Color.White,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                    FeaturedGlassMetaRow(
                        location = project.location,
                        projectYear = project.projectYear,
                    )
                }
                FeaturedReadMoreGlassPill()
            }
        }
    }
}

@Composable
private fun FeaturedGlassMetaRow(
    location: String?,
    projectYear: Int?,
) {
    val hasLocation = !location.isNullOrBlank()
    val hasYear = projectYear != null
    if (!hasLocation && !hasYear) return
    Row(
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacing8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (hasLocation) {
            FeaturedGlassChip(
                icon = Icons.Filled.LocationOn,
                label = location.orEmpty(),
            )
        }
        if (hasYear) {
            FeaturedGlassChip(
                icon = Icons.Filled.CalendarToday,
                label = projectYear.toString(),
            )
        }
    }
}

@Composable
private fun FeaturedGlassChip(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(Dimens.radiusPill))
            .background(Color.White.copy(alpha = 0.18f))
            .border(
                width = Dimens.cardBorderDefault,
                color = Color.White.copy(alpha = 0.26f),
                shape = RoundedCornerShape(Dimens.radiusPill),
            )
            .padding(horizontal = Dimens.spacing10, vertical = Dimens.spacing4),
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacing4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = Color.White.copy(alpha = 0.94f),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color = Color.White.copy(alpha = 0.94f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun FeaturedReadMoreGlassPill(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(Dimens.radiusPill))
            .background(Color.White.copy(alpha = 0.2f))
            .border(
                width = Dimens.cardBorderDefault,
                color = Color.White.copy(alpha = 0.28f),
                shape = RoundedCornerShape(Dimens.radiusPill),
            )
            .padding(horizontal = Dimens.spacing10, vertical = Dimens.spacing4),
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacing4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.projects_read_more),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = Color.White,
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = Color.White,
        )
    }
}

@Composable
private fun FeaturedProjectBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(Dimens.radiusPill))
            .background(Primary)
            .padding(horizontal = Dimens.spacing12, vertical = Dimens.spacing4),
    ) {
        Text(
            text = stringResource(R.string.projects_featured_badge),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onPrimary,
            maxLines = 1,
        )
    }
}

@Composable
private fun ProjectsRefreshErrorBanner(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppSectionSurface(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.spacing12, vertical = Dimens.spacing10),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            TextButton(onClick = onRetry) {
                Text(
                    text = stringResource(R.string.projects_retry),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun ProjectCard(
    project: ProjectUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .iosElevatedShadow(elevation = 6.dp, shape = ProjectCardShape)
            .clip(ProjectCardShape)
            .background(MaterialTheme.colorScheme.surface)
            .appPremiumCardBorder(ProjectCardShape)
            .appClickable(onClick = onClick),
    ) {
        ProjectCardHero(
            imageUrl = project.imageUrl,
            fundingAmount = project.fundingAmount,
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = -ProjectContentOverlap)
                .clip(ProjectContentTopShape)
                .background(MaterialTheme.colorScheme.surface)
                .padding(
                    start = Dimens.spacing16,
                    end = Dimens.spacing16,
                    top = Dimens.spacing12,
                    bottom = Dimens.spacing8,
                ),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacing8),
        ) {
            Text(
                text = project.title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (project.summary.isNotBlank()) {
                Text(
                    text = project.summary,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        lineHeight = 20.sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            ProjectLocationMetaRow(
                location = project.location,
                projectYear = project.projectYear,
                fundingAmount = project.fundingAmount,
            )
            ProjectReadMoreCta(
                modifier = Modifier.padding(top = Dimens.spacing4),
            )
        }
    }
}

@Composable
private fun ProjectCardHero(
    imageUrl: String?,
    fundingAmount: String?,
    modifier: Modifier = Modifier,
    imageHeight: Dp = ProjectCardImageHeight,
    topStartChip: @Composable () -> Unit = {
        BingoProjectCategoryChip(
            modifier = Modifier.padding(Dimens.spacing12),
        )
    },
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(imageHeight),
    ) {
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Newspaper,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0f to Color.Transparent,
                            0.45f to Color.Transparent,
                            1f to Color.Black.copy(alpha = 0.52f),
                        ),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(Dimens.spacing12),
        ) {
            topStartChip()
        }
        fundingAmount?.let { amount ->
            ProjectFundingHighlightChip(
                amount = amount,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(Dimens.spacing12),
            )
        }
    }
}

@Composable
private fun BingoProjectCategoryChip(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(Dimens.radiusPill))
            .background(Color.White.copy(alpha = 0.94f))
            .border(
                width = Dimens.cardBorderDefault,
                color = Primary.copy(alpha = 0.35f),
                shape = RoundedCornerShape(Dimens.radiusPill),
            )
            .padding(horizontal = Dimens.spacing12, vertical = Dimens.spacing4),
    ) {
        Text(
            text = stringResource(R.string.projects_category_chip),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = Primary,
            maxLines = 1,
        )
    }
}

@Composable
private fun ProjectFundingHighlightChip(
    amount: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(Dimens.radiusPill))
            .background(Primary)
            .padding(horizontal = Dimens.spacing12, vertical = Dimens.spacing4),
    ) {
        Text(
            text = stringResource(R.string.projects_funding_amount, amount),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onPrimary,
            maxLines = 1,
        )
    }
}

@Composable
private fun ProjectLocationMetaRow(
    location: String?,
    projectYear: Int?,
    fundingAmount: String?,
) {
    val hasLocation = !location.isNullOrBlank()
    val hasYear = projectYear != null
    val hasFunding = !fundingAmount.isNullOrBlank()
    if (!hasLocation && !hasYear && !hasFunding) return
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacing8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (hasLocation) {
            ProjectMetaChip(
                icon = Icons.Filled.LocationOn,
                label = location.orEmpty(),
                modifier = Modifier.weight(1f, fill = false),
            )
        }
        if (hasYear) {
            ProjectMetaChip(
                icon = Icons.Filled.CalendarToday,
                label = projectYear.toString(),
            )
        }
        if (hasFunding) {
            ProjectMetaChip(
                icon = Icons.Filled.AttachMoney,
                label = stringResource(R.string.projects_funding_amount, fundingAmount.orEmpty()),
            )
        }
    }
}

@Composable
private fun ProjectMetaChip(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .heightIn(min = ProjectMetaChipMinHeight)
            .clip(RoundedCornerShape(Dimens.radiusPill))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f))
            .padding(horizontal = Dimens.spacing10, vertical = Dimens.spacing4),
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacing4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ProjectReadMoreCta(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.radiusPill))
            .background(Primary.copy(alpha = 0.12f))
            .padding(horizontal = Dimens.spacing16, vertical = Dimens.spacing8),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.projects_read_more),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = Primary,
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = Primary,
        )
    }
}

@Composable
private fun ProjectsLoadingState(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            modifier = Modifier.size(32.dp),
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun ProjectsErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(Dimens.spacing16))
        AppPrimaryButton(
            text = stringResource(R.string.projects_retry),
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth(0.6f),
        )
    }
}

@Composable
private fun ProjectsEmptyState(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(R.string.projects_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun openProjectSourceUrl(context: android.content.Context, url: String) {
    val trimmed = url.trim()
    if (trimmed.isBlank()) return
    val uri = runCatching { Uri.parse(trimmed) }.getOrNull() ?: return
    val scheme = uri.scheme?.lowercase()
    if (scheme != "http" && scheme != "https") return
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
    }
}
