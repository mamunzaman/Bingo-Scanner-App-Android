package com.example.mamunbingoapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.mamunbingoapp.data.profile.ProfileRepository
import com.example.mamunbingoapp.ui.components.iosElevatedShadow

@Composable
fun ProfileAvatar(
    avatarUrl: String?,
    initials: String?,
    modifier: Modifier = Modifier,
    size: Dp = 112.dp,
    showEditBadge: Boolean = false,
    loading: Boolean = false,
    onPickAvatar: (() -> Unit)? = null,
    onDeleteAvatar: (() -> Unit)? = null,
) {
    val resolvedUrl = ProfileRepository.normalizeAvatarUrl(avatarUrl)
    val hasAvatar = resolvedUrl != null
    val pickInteractionSource = remember { MutableInteractionSource() }
    val badgeClick = when {
        hasAvatar -> onDeleteAvatar
        else -> onPickAvatar
    }
    val badgeIcon = if (hasAvatar) Icons.Default.Delete else Icons.Default.Edit
    val badgeContentDescription = if (hasAvatar) "Remove photo" else "Change photo"
    val contentKey = if (hasAvatar) "image:$resolvedUrl" else "initials:${initials.orEmpty()}"
    Box(
        modifier = modifier,
        contentAlignment = Alignment.BottomEnd,
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .iosElevatedShadow(elevation = 2.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            key(contentKey) {
                when {
                    loading -> CircularProgressIndicator(
                        modifier = Modifier.size(size * 0.35f),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    hasAvatar -> {
                        val context = LocalContext.current
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(resolvedUrl)
                                .memoryCacheKey(resolvedUrl)
                                .diskCacheKey(resolvedUrl)
                                .memoryCachePolicy(CachePolicy.ENABLED)
                                .diskCachePolicy(CachePolicy.ENABLED)
                                .crossfade(false)
                                .build(),
                            contentDescription = "Profile photo",
                            modifier = Modifier
                                .size(size)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop,
                        )
                    }
                    !initials.isNullOrBlank() -> Text(
                        text = initials,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    else -> Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(size * 0.5f),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            if (!loading && onPickAvatar != null) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(CircleShape)
                        .clickable(
                            indication = null,
                            interactionSource = pickInteractionSource,
                            onClick = onPickAvatar,
                        ),
                )
            }
        }
        if (showEditBadge && badgeClick != null) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .offset((-4).dp, 4.dp)
                    .clip(CircleShape)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = badgeClick,
                    )
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = badgeIcon,
                    contentDescription = badgeContentDescription,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

fun profileAvatarInitials(displayName: String, fullName: String = ""): String? {
    val source = displayName.trim().ifBlank { fullName.trim() }
    val parts = source.split(Regex("\\s+")).filter { it.isNotEmpty() }
    if (parts.isEmpty()) return null
    return when (parts.size) {
        1 -> parts[0].take(2).uppercase()
        else -> "${parts.first().first()}${parts.last().first()}".uppercase()
    }
}
