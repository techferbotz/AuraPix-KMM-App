package com.ferbotz.aurapix.profile.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ferbotz.aurapix.core.auth.rememberGoogleAuthProvider
import com.ferbotz.aurapix.core.di.DataModule
import com.ferbotz.aurapix.core.ui.components.BrandLogo

/**
 * Self-contained login gate: a modal sheet with Google sign-in. Reuses [LoginViewModel] + the
 * platform auth provider. Calls [onLoggedIn] once the JWT is stored so the caller can resume its
 * gated action (e.g. continue the generate flow), and [onDismiss] if the user backs out.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginBottomSheet(
    onDismiss: () -> Unit,
    onLoggedIn: () -> Unit,
    modifier: Modifier = Modifier,
    onPrivacyPolicy: () -> Unit = {},
    onTerms: () -> Unit = {},
) {
    val vm = remember { LoginViewModel(DataModule.authRepository, DataModule.userManager) }
    DisposableEffect(Unit) { onDispose { vm.onCleared() } }
    val loginState by vm.state.collectAsState()
    val googleAuth = rememberGoogleAuthProvider()
    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(loginState) {
        if (loginState is LoginUiState.Success) onLoggedIn()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // The mark, smaller than on the full screen — this interrupts the creation flow,
            // so it should read as a pause, not as a second launch screen.
            BrandLogo(size = 56.dp)
            Spacer(Modifier.height(10.dp))
            Text(
                "Sign in to generate",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Your photos and gems stay with your account.\nOne tap and you're back to creating.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(18.dp))
            GoogleSignInButton(
                onClick = { vm.signIn { googleAuth.signIn() } },
                loading = loginState is LoginUiState.Loading,
            )
            LoginErrorSlot((loginState as? LoginUiState.Error)?.message)
            LegalFooter(onPrivacyPolicy = onPrivacyPolicy, onTerms = onTerms)
        }
    }
}
