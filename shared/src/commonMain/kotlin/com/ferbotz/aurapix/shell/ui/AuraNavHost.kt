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
import com.ferbotz.aurapix.core.auth.rememberGoogleAuthProvider
import com.ferbotz.aurapix.core.di.DataModule
import com.ferbotz.aurapix.core.ui.components.AuraBottomBar
import com.ferbotz.aurapix.core.ui.components.AuraTab
import com.ferbotz.aurapix.core.ui.components.WebViewScreen
import com.ferbotz.aurapix.billing.ui.CreditsSuccessScreen
import com.ferbotz.aurapix.billing.ui.PaywallBottomSheet
import com.ferbotz.aurapix.creation.ui.GenerationFailedScreen
import com.ferbotz.aurapix.profile.ui.HelpFaqScreen
import com.ferbotz.aurapix.creation.ui.HistoryScreen
import com.ferbotz.aurapix.feed.ui.HomeFeedScreen
import com.ferbotz.aurapix.profile.ui.LoginBottomSheet
import com.ferbotz.aurapix.profile.ui.LoginScreen
import com.ferbotz.aurapix.billing.ui.PremiumPlansScreen
import com.ferbotz.aurapix.creation.ui.ProcessingScreen
import com.ferbotz.aurapix.profile.ui.ProfileScreen
import com.ferbotz.aurapix.billing.ui.PurchaseCreditsScreen
import com.ferbotz.aurapix.creation.ui.ResultScreen
import com.ferbotz.aurapix.profile.ui.SettingsScreen
import com.ferbotz.aurapix.billing.ui.SubscriptionSuccessScreen
import com.ferbotz.aurapix.template.ui.TemplateDetailScreen
import com.ferbotz.aurapix.creation.ui.CreationDetailViewModel
import com.ferbotz.aurapix.creation.ui.GenerationViewModel
import com.ferbotz.aurapix.creation.ui.HistoryViewModel
import com.ferbotz.aurapix.feed.ui.HomeFeedViewModel
import com.ferbotz.aurapix.profile.ui.LoginUiState
import com.ferbotz.aurapix.profile.ui.LoginViewModel
import com.ferbotz.aurapix.profile.ui.ProfileViewModel
import com.ferbotz.aurapix.template.ui.TemplateDetailViewModel
import com.ferbotz.aurapix.core.ui.base.UiState
import kotlinx.coroutines.delay

private const val PRIVACY_URL = "https://policies.google.com/privacy"
private const val TERMS_URL = "https://policies.google.com/terms"
private const val SUPPORT_URL = "https://support.google.com"

@Composable
fun AuraNavHost(
    navController: NavHostController = rememberNavController(),
    auth: AuthState = remember { AuthState(DataModule.authRepository) },
) {
    // Shared across TemplateDetail → Processing → Result/Failed so the generation survives navigation.
    val generationVm = remember { GenerationViewModel(DataModule.creationsRepository) }
    DisposableEffect(Unit) { onDispose { generationVm.onCleared() } }

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

            val prefs = DataModule.preferences
            val monetization = remember { DataModule.remoteConfig.monetization }
            val cost = monetization.generationCostGems

            // Generate gate: signed in → enough gems → generate; else show the login / paywall sheet.
            var loginImages by remember { mutableStateOf<List<ByteArray>?>(null) }
            var paywallImages by remember { mutableStateOf<List<ByteArray>?>(null) }

            val startGeneration: (List<ByteArray>) -> Unit = { images ->
                generationVm.generate(route.templateId, images)
                navController.navigate(ProcessingRoute)
            }

            TemplateDetailScreen(
                state = state,
                generationCost = cost,
                onBack = { navController.popBackStack() },
                onGenerate = { images ->
                    when {
                        !auth.isLoggedIn -> loginImages = images
                        prefs.cachedCredits < cost -> paywallImages = images
                        else -> startGeneration(images)
                    }
                },
                onRetry = { vm.retry() },
            )

            loginImages?.let { images ->
                LoginBottomSheet(
                    onDismiss = { loginImages = null },
                    onLoggedIn = {
                        loginImages = null
                        auth.onLoginSuccess()
                        // Login hydrated credits — re-check before generating.
                        if (prefs.cachedCredits < cost) paywallImages = images else startGeneration(images)
                    },
                )
            }

            paywallImages?.let { _ ->
                PaywallBottomSheet(
                    isPremium = prefs.subscriptionStatus == "ACTIVE",
                    config = monetization,
                    onDismiss = { paywallImages = null },
                    onSelectOffer = {
                        // TODO: drive the RevenueCat purchase; on success re-check credits + resume generation.
                        paywallImages = null
                    },
                )
            }
        }

        composable<ProcessingRoute> {
            val genState by generationVm.state.collectAsState()
            var progress by remember { mutableFloatStateOf(0f) }
            // Indeterminate-style: ease toward 90% while the server generates + we long-poll.
            LaunchedEffect(Unit) {
                animate(0f, 0.9f, animationSpec = tween(durationMillis = 12_000)) { value, _ -> progress = value }
            }
            LaunchedEffect(genState) {
                when (val s = genState) {
                    is UiState.Success ->
                        if (s.data.status == "COMPLETED") {
                            navController.navigate(ResultRoute(s.data.id)) { popUpTo(HomeRoute) { inclusive = false } }
                        } else {
                            navController.navigate(GenerationFailedRoute) { popUpTo(HomeRoute) { inclusive = false } }
                        }
                    is UiState.Error ->
                        navController.navigate(GenerationFailedRoute) { popUpTo(HomeRoute) { inclusive = false } }
                    else -> {}
                }
            }
            ProcessingScreen(progress = progress)
        }

        composable<ResultRoute> { entry ->
            val route = entry.toRoute<ResultRoute>()
            val vm = remember { CreationDetailViewModel(DataModule.creationsRepository) }
            DisposableEffect(Unit) { onDispose { vm.onCleared() } }
            LaunchedEffect(route.creationId) { vm.load(route.creationId) }
            val state by vm.state.collectAsState()
            ResultScreen(
                imageUrl = (state as? UiState.Success)?.data?.generatedImageUrl,
                onBack = { navController.popBackStack(HomeRoute, inclusive = false) },
                onRetry = { navController.popBackStack(HomeRoute, inclusive = false) },
            )
        }

        composable<GenerationFailedRoute> {
            GenerationFailedScreen(
                onRetry = {
                    generationVm.retry()
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
            val state by vm.state.collectAsState()

            ProfileScreen(
                state = state,
                selectedTab = tab,
                onSelectTab = { tab = it },
                onUpgrade = { navController.navigate(PremiumPlansRoute) },
                onPurchaseCredits = { navController.navigate(PurchaseCreditsRoute) },
                onOpenSettings = { navController.navigate(SettingsRoute) },
                onHelp = { navController.navigate(HelpRoute) },
                onPrivacyPolicy = { navController.navigate(WebViewRoute(PRIVACY_URL, "Privacy Policy")) },
                onRetry = { vm.refresh() },
                onLogout = {
                    vm.logout()
                    auth.logout()
                },
            )
        } else {
            val loginVm = remember { LoginViewModel(DataModule.authRepository) }
            DisposableEffect(Unit) { onDispose { loginVm.onCleared() } }
            val loginState by loginVm.state.collectAsState()
            val googleAuth = rememberGoogleAuthProvider()

            LaunchedEffect(loginState) {
                if (loginState is LoginUiState.Success) auth.onLoginSuccess()
            }

            Scaffold(
                bottomBar = { AuraBottomBar(selected = tab, onSelect = { tab = it }) },
                containerColor = MaterialTheme.colorScheme.background,
            ) { innerPadding ->
                LoginScreen(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    loading = loginState is LoginUiState.Loading,
                    errorMessage = (loginState as? LoginUiState.Error)?.message,
                    // Runs the platform Google flow → ID token → POST /auth/google → JWT stored.
                    onGoogleSignIn = { loginVm.signIn { googleAuth.signIn() } },
                )
            }
        }
    }
}
