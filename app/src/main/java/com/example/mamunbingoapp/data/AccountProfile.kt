package com.example.mamunbingoapp.data

/**
 * Local user profile. Field names align with a future Supabase `profiles` row
 * (`full_name`, `email`, `phone`, `country`, `city`).
 */
data class AccountProfile(
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val country: String = "",
    val city: String = "",
) {
    fun displayName(): String = fullName.trim().ifBlank { GUEST_NAME }

    fun displayEmail(): String = email.trim().ifBlank { GUEST_EMAIL }

    /** Two-letter initials when [fullName] is set; null → use default avatar icon. */
    fun avatarInitials(): String? {
        val parts = fullName.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        if (parts.isEmpty()) return null
        return when (parts.size) {
            1 -> parts[0].take(2).uppercase()
            else -> "${parts.first().first()}${parts.last().first()}".uppercase()
        }
    }

    companion object {
        const val GUEST_NAME = "Guest"
        const val GUEST_EMAIL = "guest@example.com"
    }
}
