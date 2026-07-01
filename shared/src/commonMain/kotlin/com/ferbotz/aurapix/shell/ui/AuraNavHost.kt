package com.ferbotz.aurapix.shell.ui

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.ferbotz.aurapix.core.di.DataModule
import com.ferbotz.aurapix.core.ui.components.AuraBottomBar
import com.ferbotz.aurapix.core.ui.components.AuraTab
import com.ferbotz.aurapix.core.ui.components.WebViewScreen
import com.ferbotz.aurapix.billing.ui.CreditsSuccessScreen
import com.ferbotz.aurapix.creation.ui.GenerationFailedScreen
import com.ferbotz.aurapix.profile.ui.HelpFaqScreen
import com.ferbotz.aurapix.creation.ui.HistoryScreen
import com.ferbotz.aurapix.feed.ui.HomeFeedScreen
import com.ferbotz.aurapix.profile.ui.LoginScreen
import com.ferbotz.aurapix.billing.ui.PremiumPlansScreen
import com.ferbotz.aurapix.creation.ui.ProcessingScreen
import com.ferbotz.aurapix.profile.ui.ProfileScreen
import com.ferbotz.aurapix.billing.ui.PurchaseCreditsScreen
import com.ferbotz.aurapix.creation.ui.ResultScreen
import com.ferbotz.aurapix.profile.ui.SettingsScreen
import com.ferbotz.aurapix.billing.ui.SubscriptionSuccessScreen
import com.ferbotz.aurapix.template.ui.TemplateDetailScreen
import com.ferbotz.aurapix.creation.ui.UploadPhotosScreen
import com.ferbotz.aurapix.creation.ui.HistoryViewModel
import com.ferbotz.aurapix.feed.ui.HomeFeedViewModel
import com.ferbotz.aurapix.profile.ui.ProfileViewModel
import com.ferbotz.aurapix.template.ui.TemplateDetailViewModel
import com.ferbotz.aurapix.core.ui.base.UiState
import kotlinx.coroutines.delay
import kotlin.random.Random

private const val PRIVACY_URL = "https://policies.google.com/privacy"
private const val TERMS_URL = "https://policies.google.com/terms"
private const val SUPPORT_URL = "https://support.google.com"

@Composable
fun AuraNavHost(
    navController: NavHostController = rememberNavController(),
    auth: AuthState = remember { AuthState(DataModule.authRepository) },
) {
    NavHost(navController = navController, startDestination = SplashRoute) {
        composable<SplashRoute> {
            SplashScreen()
            LaunchedEffect(Unit) {
                delay(1600)
                navController.navigate(HomeRoute) { popUpTo(SplashRoute) { inclusive = true } }
            }
        }

        composable<HomeRoute> { HomeContainer(navController, auth) }

        composable<TemplateDetailRoute> { entry ->
            val route = entry.toRoute<TemplateDetailRoute>()
            val vm = remember { TemplateDetailViewModel(DataModule.templatesRepository) }
            DisposableEffect(Unit) { onDispose { vm.onCleared() } }
            LaunchedEffect(route.templateId) { vm.load(route.templateId) }
            val state by vm.templateState.collectAsState()
            TemplateDetailScreen(
                state = state,
                onBack = { navController.popBackStack() },
                onGenerate = { navController.navigate(UploadRoute) },
                onRetry = { vm.retry() },
            )
        }

        composable<UploadRoute> {
            UploadPhotosScreen(
                onBack = { navController.popBackStack() },
                onTrain = { navController.navigate(ProcessingRoute) },
            )
        }

        composable<ProcessingRoute> {
            // Stub progress until the image-picker + GenerationViewModel wiring lands.
            var progress by remember { mutableFloatStateOf(0f) }
            val willSucceed = remember { Random.nextFloat() < 0.85f }
            LaunchedEffect(Unit) {
                animate(0f, 1f, animationSpec = tween(durationMillis = 2600)) { value, _ -> progress = value }
                navController.navigate(if (willSucceed) ResultRoute("") else GenerationFailedRoute) {
                    popUpTo(HomeRoute) { inclusive = false }
                }
            }
            ProcessingScreen(progress = progress)
        }

        composable<ResultRoute> {
            ResultScreen(
                onBack = { navController.popBackStack(HomeRoute, inclusive = false) },
                onRetry = { navController.navigate(ProcessingRoute) },
            )
        }

        composable<GenerationFailedRoute> {
            GenerationFailedScreen(
                onRetry = {
                    navController.navigate(ProcessingRoute) { popUpTo(GenerationFailedRoute) { inclusive = true } }
                },
                onGoHome = { navController.popBackStack(HomeRoute, inclusive = false) },
            )
        }

        composable<SettingsRoute> {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onPrivacyPolicy = { navController.navigate(WebViewRoute(PRIVACY_URL, "Privacy Policy")) },
                onTerms = { navController.navigate(WebViewRoute(TERMS_URL, "Terms of Service")) },
                onLogout = {
                    auth.logout()
                    navController.popBackStack(HomeRoute, inclusive = false)
                },
            )
        }

        composable<PremiumPlansRoute> {
            PremiumPlansScreen(
                onBack = { navController.popBackStack() },
                onChoosePlan = {
                    navController.navigate(SubscriptionSuccessRoute) { popUpTo(PremiumPlansRoute) { inclusive = true } }
                },
            )
        }

        composable<PurchaseCreditsRoute> {
            PurchaseCreditsScreen(
                onBack = { navController.popBackStack() },
                onSelectPack = {
                    navController.navigate(CreditsSuccessRoute) { popUpTo(PurchaseCreditsRoute) { inclusive = true } }
                },
            )
        }

        composable<CreditsSuccessRoute> {
            CreditsSuccessScreen(
                onContinue = { navController.popBackStack(HomeRoute, inclusive = false) },
                onViewProfile = { navController.popBackStack(HomeRoute, inclusive = false) },
            )
        }

        composable<SubscriptionSuccessRoute> {
            SubscriptionSuccessScreen(
                onContinue = { navController.popBackStack(HomeRoute, inclusive = false) },
            )
        }

        composable<HelpRoute> {
            HelpFaqScreen(
                onBack = { navController.popBackStack() },
                onContactSupport = { navController.navigate(WebViewRoute(SUPPORT_URL, "Support")) },
            )
        }

        composable<WebViewRoute> { entry ->
            val route = entry.toRoute<WebViewRoute>()
            WebViewScreen(url = route.url, title = route.title, onBack = { navController.popBackStack() })
        }
    }
}

@Composable
private fun HomeContainer(navController: NavHostController, auth: AuthState) {
    var tab by remember { mutableStateOf(AuraTab.Feed) }

    when (tab) {
        AuraTab.Feed -> {
            val vm = remember {
                HomeFeedViewModel(DataModule.feedRepository, DataModule.profileRepository)
            }
            DisposableEffect(Unit) { onDispose { vm.onCleared() } }
            val feedState by vm.feedState.collectAsState()
            val credits by vm.credits.collectAsState()

            HomeFeedScreen(
                credits = credits,
                feedState = feedState,
                selectedTab = tab,
                onSelectTab = { tab = it },
                onCreate = { navController.navigate(UploadRoute) },
                onTemplateClick = { navController.navigate(TemplateDetailRoute(it.id, it.name)) },
                onRetry = { vm.refresh() },
            )
        }

        AuraTab.MyCreations -> {
            val vm = remember { HistoryViewModel(DataModule.creationsRepository) }
            DisposableEffect(Unit) { onDispose { vm.onCleared() } }
            val state by vm.state.collectAsState()

            HistoryScreen(
                items = (state as? UiState.Success)?.data ?: emptyList(),
                selectedTab = tab,
                onSelectTab = { tab = it },
                onItemClick = { navController.navigate(ResultRoute(it.id)) },
                onRefresh = { vm.refresh() },
            )
        }

        AuraTab.Profile -> if (auth.isLoggedIn) {
            val vm = remember { ProfileViewModel(DataModule.profileRepository, DataModule.authRepository) }
            DisposableEffect(Unit) { onDispose { vm.onCleared() } }
            val profileState by vm.profileState.collectAsState()
            val profile = (profileState as? UiState.Success)?.data

            ProfileScreen(
                name = profile?.displayName ?: "...",
                credits = profile?.credits?.totalCredits ?: DataModule.preferences.cachedCredits,
                selectedTab = tab,
                onSelectTab = { tab = it },
                onUpgrade = { navController.navigate(PremiumPlansRoute) },
                onPurchaseCredits = { navController.navigate(PurchaseCreditsRoute) },
                onOpenSettings = { navController.navigate(SettingsRoute) },
                onHelp = { navController.navigate(HelpRoute) },
                onPrivacyPolicy = { navController.navigate(WebViewRoute(PRIVACY_URL, "Privacy Policy")) },
                onLogout = {
                    vm.logout()
                    auth.logout()
                },
            )
        } else {
            Scaffold(
                bottomBar = { AuraBottomBar(selected = tab, onSelect = { tab = it }) },
                containerColor = MaterialTheme.colorScheme.background,
            ) { innerPadding ->
                LoginScreen(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    // Google sign-in: the platform layer acquires the idToken and calls
                    // AuthRepository.loginWithGoogle before this success callback fires.
                    onGoogleSignIn = { auth.onLoginSuccess() },
                    onContinueAsGuest = { auth.onLoginSuccess() },
                )
            }
        }
    }
}
