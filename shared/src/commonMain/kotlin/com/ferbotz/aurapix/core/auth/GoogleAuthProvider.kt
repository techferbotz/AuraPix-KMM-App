package com.ferbotz.aurapix.core.auth

import androidx.compose.runtime.Composable

/**
 * The Google OAuth **Web** client id (serverClientId) the backend verifies ID tokens against.
 * On-device sign-in also requires an Android OAuth client (package + SHA-1) in the same project.
 */
const val GOOGLE_WEB_CLIENT_ID: String = "190079027234-69qrmre1sljvtpujvh3oume6fkvtqu8g.apps.googleusercontent.com"

/** Platform Google sign-in. Launches the native flow and returns the Google **ID token**. */
interface GoogleAuthProvider {
    suspend fun signIn(): Result<String>
}

/** The user dismissed the native sign-in sheet (not a real error — used for a softer message). */
class SignInCancelledException : Exception("Sign-in was cancelled")

/** Provides a platform [GoogleAuthProvider] scoped to the current composition. */
@Composable
expect fun rememberGoogleAuthProvider(): GoogleAuthProvider
