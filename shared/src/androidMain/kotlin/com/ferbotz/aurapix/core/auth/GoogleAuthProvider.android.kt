package com.ferbotz.aurapix.core.auth

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlin.coroutines.cancellation.CancellationException

@Composable
actual fun rememberGoogleAuthProvider(): GoogleAuthProvider {
    val context = LocalContext.current
    return remember(context) { AndroidGoogleAuthProvider(context) }
}

/** Uses Credential Manager + Google Identity to obtain a Google ID token. */
private class AndroidGoogleAuthProvider(private val context: Context) : GoogleAuthProvider {

    override suspend fun signIn(): Result<String> = try {
        val option = GetGoogleIdOption.Builder()
            .setServerClientId(GOOGLE_WEB_CLIENT_ID)
            // false → also let the user pick an account that hasn't authorized the app yet.
            .setFilterByAuthorizedAccounts(false)
            .setAutoSelectEnabled(false)
            .build()
        val request = GetCredentialRequest.Builder().addCredentialOption(option).build()

        val response = CredentialManager.create(context).getCredential(context, request)
        val credential = response.credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            Result.success(GoogleIdTokenCredential.createFrom(credential.data).idToken)
        } else {
            Result.failure(IllegalStateException("Unexpected credential type: ${credential.type}"))
        }
    } catch (e: GetCredentialCancellationException) {
        Result.failure(SignInCancelledException())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}
