package com.example.mamunbingoapp.data.profile

import android.content.Context
import android.net.Uri
import android.util.Log
import coil.annotation.ExperimentalCoilApi
import coil.imageLoader
import coil.memory.MemoryCache
import coil.request.ImageRequest
import com.example.mamunbingoapp.data.auth.AuthRepository
import com.example.mamunbingoapp.data.auth.AuthState
import com.example.mamunbingoapp.data.auth.SupabaseClientProvider
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.delay
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import java.io.IOException
import java.net.UnknownHostException
import kotlin.time.Duration.Companion.days

object ProfileRepository {

    private const val TAG = "ProfileRepository"
    private const val TABLE = "profiles"

    fun normalizeAvatarUrl(url: String?): String? {
        val trimmed = url?.trim().orEmpty()
        if (trimmed.isBlank() || trimmed.equals("null", ignoreCase = true)) return null
        return trimmed
    }

    fun withAvatarUploadCacheBuster(url: String): String {
        val base = url.substringBefore('?').ifBlank { url }
        return "$base?v=${System.currentTimeMillis()}"
    }

    fun avatarUrlCacheKeys(url: String): Set<String> {
        val trimmed = url.trim()
        if (trimmed.isBlank()) return emptySet()
        val normalized = normalizeAvatarUrl(trimmed) ?: trimmed
        val base = normalized.substringBefore('?')
        return linkedSetOf(trimmed, normalized, base).filter { it.isNotBlank() }.toSet()
    }

    @OptIn(ExperimentalCoilApi::class)
    fun evictAvatarFromImageCache(context: Context, url: String): Boolean {
        val variants = avatarUrlCacheKeys(url)
        if (variants.isEmpty()) {
            Log.d(TAG, "avatar delete: Coil cache evicted=false (no url variants)")
            return false
        }
        val loader = context.imageLoader
        var evicted = false
        variants.forEach { variant ->
            if (loader.memoryCache?.remove(MemoryCache.Key(variant)) != null) {
                evicted = true
            }
            val request = ImageRequest.Builder(context)
                .data(variant)
                .memoryCacheKey(variant)
                .diskCacheKey(variant)
                .build()
            request.memoryCacheKey?.let { cacheKey ->
                if (loader.memoryCache?.remove(cacheKey) != null) {
                    evicted = true
                }
            }
            runCatching {
                val diskCache = loader.diskCache ?: return@runCatching
                if (diskCache.remove(variant)) evicted = true
                request.diskCacheKey?.let { diskKey ->
                    if (diskCache.remove(diskKey)) evicted = true
                }
            }
        }
        Log.d(TAG, "avatar delete: Coil cache evicted=$evicted variants=$variants")
        return evicted
    }

    suspend fun loadOrCreateCurrentProfile(): Result<ProfileDto> {
        val userId = AuthRepository.currentSignedInUserId()
            ?: return Result.failure(IllegalStateException("Sign in to load your profile."))
        return runCatching {
            SupabaseClientProvider.requireConfigured()
            val client = SupabaseClientProvider.getClientOrNull()
                ?: error(SupabaseClientProvider.configurationErrorMessage() ?: "Profile unavailable")
            val existing = client.postgrest.from(TABLE).select {
                filter { eq("id", userId) }
            }.decodeSingleOrNull<ProfileDto>()
            if (existing != null) {
                existing
            } else {
                val created = ProfileDto(id = userId)
                client.postgrest.from(TABLE).insert(created) {
                    select(Columns.ALL)
                }.decodeSingle<ProfileDto>()
            }
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { error ->
                Log.w(TAG, "loadOrCreateCurrentProfile failed", error)
                Result.failure(IllegalStateException(mapProfileError(error)))
            },
        )
    }

    suspend fun uploadAvatarFromUri(
        context: Context,
        imageUri: Uri,
        cachedUserId: String? = null,
    ): Result<ProfileDto> {
        val auth = resolveAvatarUploadAuth(cachedUserId)
            ?: return Result.failure(IllegalStateException("Sign in to update your profile photo."))
        val userId = auth.uploadUserId
        val bytes = context.contentResolver.openInputStream(imageUri)?.use { input ->
            input.readBytes()
        }
        if (bytes == null || bytes.isEmpty()) {
            return Result.failure(IllegalStateException("Could not read the selected image."))
        }
        val bucketName = ProfileAvatarStorage.BUCKET
        val uploadPath = ProfileAvatarStorage.objectPath(userId)
        logAvatarUploadStart(bucketName, uploadPath, userId, bytes.size)
        return runCatching {
            SupabaseClientProvider.requireConfigured()
            val client = SupabaseClientProvider.getClientOrNull()
                ?: error(SupabaseClientProvider.configurationErrorMessage() ?: "Profile unavailable")
            requireAuthenticatedSession(client, userId)
            check(uploadPath == "avatars/$userId.jpg") {
                "Upload path must be avatars/{userId}.jpg (got=$uploadPath)"
            }
            check(bucketName == "profile-avatars") {
                "Bucket must be profile-avatars (got=$bucketName)"
            }
            val bucket = client.storage.from(bucketName)
            Log.d(TAG, "avatar upload: starting storage upload upsert=true contentType=image/jpeg")
            bucket.upload(uploadPath, bytes) {
                upsert = true
                contentType = ContentType.Image.JPEG
            }
            Log.d(TAG, "avatar upload: storage upload succeeded path=$uploadPath")
            val avatarUrl = resolveAvatarUrl(bucket, uploadPath)
            val existing = loadOrCreateProfileForUser(client, userId)
            updateProfile(existing.copy(avatarUrl = avatarUrl)).getOrThrow()
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { error ->
                logAvatarUploadFailure(error, bucketName, uploadPath, userId)
                Result.failure(IllegalStateException(mapProfileError(error)))
            },
        )
    }

    private data class AvatarUploadAuth(
        val uploadUserId: String,
        val cachedUserId: String?,
        val sessionUserId: String?,
    )

    private suspend fun resolveAvatarUploadAuth(cachedUserId: String?): AvatarUploadAuth? {
        val cached = cachedUserId?.trim().orEmpty().takeIf { it.isNotBlank() }
            ?: AuthRepository.currentSignedInUserId()?.trim().orEmpty().takeIf { it.isNotBlank() }
            ?: authStateSignedInUserId()
        SupabaseClientProvider.requireConfigured()
        var sessionUserId: String? = null
        repeat(3) { attempt ->
            val session = SupabaseClientProvider.getClientOrNull()?.auth?.currentSessionOrNull()
            sessionUserId = session?.user?.id?.trim().orEmpty().takeIf { it.isNotBlank() }
            val hasAccessToken = !session?.accessToken.isNullOrBlank()
            if (sessionUserId != null && hasAccessToken) {
                Log.d(
                    TAG,
                    "avatar upload auth: cachedUserId=$cached sessionUserId=$sessionUserId " +
                        "uploadUserId=$sessionUserId attempt=$attempt",
                )
                return AvatarUploadAuth(
                    uploadUserId = sessionUserId!!,
                    cachedUserId = cached,
                    sessionUserId = sessionUserId,
                )
            }
            if (attempt < 2) delay(150L)
        }
        val uploadUserId = cached
        Log.d(
            TAG,
            "avatar upload auth: cachedUserId=$cached sessionUserId=$sessionUserId " +
                "uploadUserId=$uploadUserId",
        )
        if (uploadUserId.isNullOrBlank()) return null
        return AvatarUploadAuth(
            uploadUserId = uploadUserId,
            cachedUserId = cached,
            sessionUserId = sessionUserId,
        )
    }

    private fun authStateSignedInUserId(): String? =
        (AuthRepository.authState.value as? AuthState.SignedIn)?.userId?.trim().orEmpty()
            ?.takeIf { it.isNotBlank() }

    private suspend fun loadOrCreateProfileForUser(client: SupabaseClient, userId: String): ProfileDto {
        val existing = client.postgrest.from(TABLE).select {
            filter { eq("id", userId) }
        }.decodeSingleOrNull<ProfileDto>()
        return existing ?: run {
            val created = ProfileDto(id = userId)
            client.postgrest.from(TABLE).insert(created) {
                select(Columns.ALL)
            }.decodeSingle()
        }
    }

    suspend fun deleteAvatar(): Result<ProfileDto> {
        val userId = AuthRepository.currentSignedInUserId()?.trim().orEmpty()
        if (userId.isBlank()) {
            return Result.failure(IllegalStateException("Sign in to remove your profile photo."))
        }
        val bucketName = ProfileAvatarStorage.BUCKET
        val uploadPath = ProfileAvatarStorage.objectPath(userId)
        return runCatching {
            SupabaseClientProvider.requireConfigured()
            val client = SupabaseClientProvider.getClientOrNull()
                ?: error(SupabaseClientProvider.configurationErrorMessage() ?: "Profile unavailable")
            requireAuthenticatedSession(client, userId)
            val bucket = client.storage.from(bucketName)
            runCatching {
                bucket.delete(listOf(uploadPath))
            }.onFailure { error ->
                Log.w(TAG, "avatar delete: storage remove failed path=$uploadPath", error)
            }
            clearRemoteAvatarUrl(client, userId)
        }.fold(
            onSuccess = { dto ->
                val cleared = dto.copy(avatarUrl = null)
                Log.d(TAG, "avatar delete: remote avatar_url cleared persisted=${cleared.avatarUrl == null}")
                Result.success(cleared)
            },
            onFailure = { error ->
                Log.w(TAG, "deleteAvatar failed", error)
                Result.failure(IllegalStateException(mapProfileError(error)))
            },
        )
    }

    /** Postgrest DSL so null is sent explicitly (full [ProfileDto] update omits null fields). */
    private suspend fun clearRemoteAvatarUrl(client: SupabaseClient, userId: String): ProfileDto =
        client.postgrest.from(TABLE).update(
            {
                set("avatar_url", null as String?)
            },
        ) {
            filter { eq("id", userId) }
            select(Columns.ALL)
        }.decodeSingle<ProfileDto>()

    private fun requireAuthenticatedSession(
        client: io.github.jan.supabase.SupabaseClient,
        expectedUserId: String,
    ) {
        val session = client.auth.currentSessionOrNull()
        val sessionUserId = session?.user?.id?.trim().orEmpty()
        val hasAccessToken = !session?.accessToken.isNullOrBlank()
        Log.d(
            TAG,
            "avatar upload: auth session hasSession=${session != null} " +
                "hasAccessToken=$hasAccessToken sessionUserId=$sessionUserId expectedUserId=$expectedUserId",
        )
        if (session == null || !hasAccessToken) {
            error("Sign in again to upload your profile photo.")
        }
        if (sessionUserId != expectedUserId) {
            Log.w(
                TAG,
                "avatar upload: session userId does not match upload target " +
                    "(session=$sessionUserId expected=$expectedUserId)",
            )
        }
    }

    private fun logAvatarUploadStart(
        bucketName: String,
        uploadPath: String,
        userId: String,
        byteCount: Int,
    ) {
        Log.d(
            TAG,
            "avatar upload: bucket=$bucketName path=$uploadPath signedInUserId=$userId " +
                "pathExact=${uploadPath == "avatars/$userId.jpg"} byteCount=$byteCount",
        )
    }

    private fun logAvatarUploadFailure(
        error: Throwable,
        bucketName: String,
        uploadPath: String,
        userId: String,
    ) {
        Log.e(
            TAG,
            "avatar upload failed: bucket=$bucketName path=$uploadPath signedInUserId=$userId " +
                "storageError=${collectErrorText(error)}",
            error,
        )
    }

    private fun collectErrorText(error: Throwable): String = buildString {
        var current: Throwable? = error
        while (current != null) {
            current.message?.takeIf { it.isNotBlank() }?.let { appendLine(it) }
            current = current.cause
        }
    }.trim()

    suspend fun updateProfile(profile: ProfileDto): Result<ProfileDto> {
        val userId = AuthRepository.currentSignedInUserId()
            ?: return Result.failure(IllegalStateException("Sign in to update your profile."))
        if (profile.id != userId) {
            return Result.failure(IllegalStateException("Profile update is not allowed for this account."))
        }
        return runCatching {
            SupabaseClientProvider.requireConfigured()
            val client = SupabaseClientProvider.getClientOrNull()
                ?: error(SupabaseClientProvider.configurationErrorMessage() ?: "Profile unavailable")
            client.postgrest.from(TABLE).update(profile) {
                filter { eq("id", userId) }
                select(Columns.ALL)
            }.decodeSingle<ProfileDto>()
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { error ->
                Log.w(TAG, "updateProfile failed", error)
                Result.failure(IllegalStateException(mapProfileError(error)))
            },
        )
    }

    private suspend fun resolveAvatarUrl(
        bucket: io.github.jan.supabase.storage.BucketApi,
        path: String,
    ): String {
        val public = runCatching { bucket.publicUrl(path) }.getOrNull()?.takeIf { it.isNotBlank() }
        if (public != null) return withAvatarUploadCacheBuster(public)
        return withAvatarUploadCacheBuster(bucket.createSignedUrl(path, expiresIn = 7.days).toString())
    }

    private fun mapProfileError(error: Throwable): String = when (error) {
        is IllegalStateException -> error.message.orEmpty().ifBlank { DEFAULT_ERROR }
        is UnknownHostException, is IOException -> "Network error. Check your connection and try again."
        else -> {
            val message = error.message.orEmpty()
            when {
                message.contains("JWT", ignoreCase = true) ||
                    message.contains("not authenticated", ignoreCase = true) ->
                    "Session expired. Please sign in again."
                message.contains("permission", ignoreCase = true) ||
                    message.contains("policy", ignoreCase = true) ||
                    message.contains("bucket", ignoreCase = true) ->
                    "You do not have permission to update this profile."
                else -> DEFAULT_ERROR
            }
        }
    }

    private const val DEFAULT_ERROR = "Could not save profile. Please try again."
}
