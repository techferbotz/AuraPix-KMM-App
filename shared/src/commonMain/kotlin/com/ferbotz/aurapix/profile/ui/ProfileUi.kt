package com.ferbotz.aurapix.profile.ui

/** Presentation model for the profile screen, mapped from the API's ProfileDto. */
data class ProfileUi(
    val name: String,
    val email: String,
    val avatarUrl: String?,
    val totalCredits: Int,
    val subscriptionLabel: String,
    val isPremium: Boolean,
)
