package com.example.mamunbingoapp.data.profile

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileDto(
    val id: String,
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("full_name") val fullName: String? = null,
    val phone: String? = null,
    @SerialName("secondary_email") val secondaryEmail: String? = null,
    val country: String? = null,
    val region: String? = null,
    val city: String? = null,
    @SerialName("postal_code") val postalCode: String? = null,
    @SerialName("street_address") val streetAddress: String? = null,
    @SerialName("apartment_or_house_no") val apartmentOrHouseNo: String? = null,
    val bio: String? = null,
    val language: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
)
