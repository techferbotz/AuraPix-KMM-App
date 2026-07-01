package com.ferbotz.aurapix.core.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberGoogleAuthProvider(): GoogleAuthProvider =
    remember { IosGoogleAuthProvider() }

/**
 * iOS stub — Google sign-in isn't wired on iOS yet. Wiring it needs the GoogleSignIn SDK
 * (SPM/CocoaPods), an iOS OAuth client, and a reversed-client-id URL scheme in Info.plist.
 */
private class IosGoogleAuthProvider : GoogleAuthProvider {
    override suspend fun signIn(): Result<String> =
        Result.failure(NotImplementedError("Google sign-in on iOS is not implemented yet."))
}
