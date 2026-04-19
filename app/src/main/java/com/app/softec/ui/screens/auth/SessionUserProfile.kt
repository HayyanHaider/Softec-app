package com.app.softec.ui.screens.auth

import com.google.firebase.auth.FirebaseUser

data class SessionUserProfile(
    val username: String,
    val email: String?,
    val photoUrl: String?
)

fun FirebaseUser.toSessionUserProfile(): SessionUserProfile {
    val resolvedUsername = displayName
        ?.trim()
        ?.takeIf { it.isNotBlank() }
        ?: email
            ?.substringBefore("@")
            ?.trim()
            ?.takeIf { it.isNotBlank() }
        ?: "User"

    return SessionUserProfile(
        username = resolvedUsername,
        email = email,
        photoUrl = photoUrl?.toString()
    )
}
