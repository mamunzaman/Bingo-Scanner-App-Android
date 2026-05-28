package com.example.mamunbingoapp.data.profile

/**
 * Supabase Storage bucket + object path for profile avatars.
 *
 * Object key (exact): `avatars/{userId}.jpg` — not `{userId}/avatar.jpg`, not `.png`/`.jpeg`.
 *
 * TODO (Supabase dashboard) — bucket id must be exactly [BUCKET].
 * RLS on `storage.objects` must match this path shape (folder[1] is `avatars`, not the user id):
 *
 * ```sql
 * -- INSERT (required for first upload)
 * create policy "profile_avatars_insert_own"
 * on storage.objects for insert to authenticated
 * with check (
 *   bucket_id = 'profile-avatars'
 *   and (storage.foldername(name))[1] = 'avatars'
 *   and name = 'avatars/' || (select auth.uid()::text) || '.jpg'
 * );
 *
 * -- UPDATE (required for upsert overwrite)
 * create policy "profile_avatars_update_own"
 * on storage.objects for update to authenticated
 * using (
 *   bucket_id = 'profile-avatars'
 *   and name = 'avatars/' || (select auth.uid()::text) || '.jpg'
 * )
 * with check (
 *   bucket_id = 'profile-avatars'
 *   and name = 'avatars/' || (select auth.uid()::text) || '.jpg'
 * );
 *
 * -- DELETE (remove avatar)
 * create policy "profile_avatars_delete_own"
 * on storage.objects for delete to authenticated
 * using (
 *   bucket_id = 'profile-avatars'
 *   and name = 'avatars/' || (select auth.uid()::text) || '.jpg'
 * );
 *
 * -- SELECT (signed/public URL resolution)
 * create policy "profile_avatars_select_own"
 * on storage.objects for select to authenticated
 * using (
 *   bucket_id = 'profile-avatars'
 *   and name = 'avatars/' || (select auth.uid()::text) || '.jpg'
 * );
 * ```
 *
 * Wrong policy (causes permission denied with this app path):
 * `(storage.foldername(name))[1] = auth.uid()::text` — that expects `{userId}/file`, not `avatars/{userId}.jpg`.
 */
object ProfileAvatarStorage {
    const val BUCKET = "profile-avatars"
    private const val PATH_PREFIX = "avatars/"
    private const val PATH_SUFFIX = ".jpg"

    /** Exact upload key: `avatars/{userId}.jpg` */
    fun objectPath(userId: String): String = "$PATH_PREFIX${userId.trim()}$PATH_SUFFIX"

    fun isExpectedObjectPath(userId: String, path: String): Boolean =
        path == objectPath(userId)
}
