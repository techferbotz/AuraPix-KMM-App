package com.ferbotz.aurapix.shell.ui

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import com.ferbotz.aurapix.core.data.remote.ApiError
import com.ferbotz.aurapix.core.di.DataModule
import com.ferbotz.aurapix.core.media.rememberImageActions
import com.ferbotz.aurapix.core.ui.components.AuraTab
import com.ferbotz.aurapix.core.ui.components.AuraTabScaffold
import com.ferbotz.aurapix.core.ui.components.WebViewScreen
import com.ferbotz.aurapix.billing.ui.CreditsSuccessScreen
import com.ferbotz.aurapix.billing.ui.BillingViewModel
import com.ferbotz.aurapix.billing.ui.PaywallHost
import com.ferbotz.aurapix.creation.ui.GenerationFailedScreen
import com.ferbotz.aurapix.profile.ui.HelpFaqScreen
import com.ferbotz.aurapix.creation.ui.HistoryScreen
import com.ferbotz.aurapix.category.ui.CategoryDetailScreen
import com.ferbotz.aurapix.category.ui.CategoryDetailViewModel
import com.ferbotz.aurapix.feed.ui.FeedSectionKind
import com.ferbotz.aurapix.feed.ui.HomeFeedScreen
import com.ferbotz.aurapix.feed.ui.TrayListingScreen
import com.ferbotz.aurapix.feed.ui.TrayListingViewModel
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
    auth: AuthState = remember { AuthState(DataModule.userManager) },
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

        composable<TrayListingRoute> { entry ->
            val route = entry.toRoute<TrayListingRoute>()
            val vm = remember { TrayListingViewModel(DataModule.feedRepository, route.trayId, FeedSectionKind.valueOf(route.kind)) }
            DisposableEffect(Unit) { onDispose { vm.onCleared() } }
            val state by vm.state.collectAsState()

            TrayListingScreen(
                title = route.title,
                state = state,
                onBack = { navController.popBackStack() },
                onTemplateClick = { navController.navigate(TemplateDetailRoute(it.id, it.name)) },
                onCategoryClick = { navController.navigate(CategoryDetailRoute(it.id, it.name)) },
                onRetry = { vm.retry() },
            )
        }

        composable<CategoryDetailRoute> { entry ->
            val route = entry.toRoute<CategoryDetailRoute>()
            val vm = remember { CategoryDetailViewModel(DataModule.categoriesRepository, route.categoryId) }
            DisposableEffect(Unit) { onDispose { vm.onCleared() } }
            val state by vm.state.collectAsState()

            CategoryDetailScreen(
                categoryName = route.categoryName,
                state = state,
                onBack = { navController.popBackStack() },
                onTemplateClick = { navController.navigate(TemplateDetailRoute(it.id, it.name)) },
                onRetry = { vm.retry() },
            )
        }

        composable<TemplateDetailRoute> { entry ->
            val route = entry.toRoute<TemplateDetailRoute>()
            val vm = remember { TemplateDetailViewModel(DataModule.templatesRepository) }
            DisposableEffect(Unit) { onDispose { vm.onCleared() } }
            LaunchedEffect(route.templateId) { vm.load(route.templateId) }
            val state by vm.templateState.collectAsState()

            val userManager = DataModule.userManager
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
                        userManager.current.credits < cost -> paywallImages = images
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
                        userManager.current.id?.let { DataModule.paymentManager.identify(it) }
                        // Login hydrated credits — re-check before generating.
                        if (userManager.current.credits < cost) paywallImages = images else startGeneration(images)
                    },
                )
            }

            paywallImages?.let { images ->
                PaywallHost(
                    config = monetization,
                    onDismiss = { paywallImages = null },
                    onPurchased = { totalCredits, subscriptionStatus ->
                        // Verify already updated the backend → apply the fresh balances + resume.
                        userManager.applyBilling(totalCredits, subscriptionStatus)
                        paywallImages = null
                        if (userManager.current.credits >= cost) startGeneration(images)
                    },
                )
            }
        }

        composable<ProcessingRoute> {
            val genState by generationVm.state.collectAsState()
            val monetization = remember { DataModule.remoteConfig.monetization }
            var progress by remember { mutableFloatStateOf(0f) }
            var showCreditsPaywall by remember { mutableStateOf(false) }

            // Run the "indeterminate" ring only while a generation is actually in flight. It freezes
            // on error/success — e.g. behind the out-of-credits paywall — and restarts from 0 when a
            // purchase-triggered retry flips the state back to Loading.
            val generating = genState is UiState.Loading
            LaunchedEffect(generating) {
                if (generating) {
                    progress = 0f
                    animate(0f, 0.9f, animationSpec = tween(durationMillis = 12_000)) { value, _ -> progress = value }
                }
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
                        // 402 INSUFFICIENT_CREDITS: server balance is short → offer to top up and resume,
                        // rather than dropping to the generic failure screen.
                        if (s.error is ApiError.InsufficientCredits) {
                            showCreditsPaywall = true
                        } else {
                            navController.navigate(GenerationFailedRoute) { popUpTo(HomeRoute) { inclusive = false } }
                        }
                    else -> {}
                }
            }
            ProcessingScreen(progress = progress, credits = currentUserState().credits)

            if (showCreditsPaywall) {
                PaywallHost(
                    config = monetization,
                    onDismiss = {
                        showCreditsPaywall = false
                        navController.popBackStack()
                    },
                    onPurchased = { totalCredits, subscriptionStatus ->
                        DataModule.userManager.applyBilling(totalCredits, subscriptionStatus)
                        showCreditsPaywall = false
                        generationVm.retry()  // credits topped up → regenerate (state → Loading restarts the ring)
                    },
                )
            }
        }

        composable<ResultRoute> { entry ->
            val route = entry.toRoute<ResultRoute>()
            val vm = remember { CreationDetailViewModel(DataModule.creationsRepository) }
            DisposableEffect(Unit) { onDispose { vm.onCleared() } }
            LaunchedEffect(route.creationId) { vm.load(route.creationId) }
            val state by vm.state.collectAsState()
            val data = (state as? UiState.Success)?.data
            val imageActions = rememberImageActions()
            ResultScreen(
                title = data?.templateTitleSnapshot ?: "",
                imageUrl = data?.generatedImageUrl,
                onBack = { navController.popBackStack(HomeRoute, inclusive = false) },
                onDownload = { data?.generatedImageUrl?.let { imageActions.download(it) } },
                onShare = { data?.generatedImageUrl?.let { imageActions.share(it) } },
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
            val user = currentUserState()
            SettingsScreen(
                name = user.name ?: "",
                email = user.email ?: "",
                avatarUrl = user.avatarUrl,
                onBack = { navController.popBackStack() },
                onPrivacyPolicy = { navController.navigate(WebViewRoute(PRIVACY_URL, "Privacy Policy")) },
                onTerms = { navController.navigate(WebViewRoute(TERMS_URL, "Terms of Service")) },
                onLogout = {
                    auth.logout()
                    DataModule.paymentManager.onLoggedOut()
                    navController.popBackStack(HomeRoute, inclusive = false)
                },
            )
        }

        composable<PremiumPlansRoute> {
            val vm = remember { BillingViewModel(DataModule.paymentManager, DataModule.userManager, DataModule.remoteConfig.monetization) }
            DisposableEffect(Unit) { onDispose { vm.onCleared() } }
            val billing by vm.state.collectAsState()

            LaunchedEffect(billing.purchaseComplete) {
                if (billing.purchaseComplete) {
                    vm.consumePurchaseComplete()
                    navController.navigate(SubscriptionSuccessRoute) { popUpTo(PremiumPlansRoute) { inclusive = true } }
                }
            }

            val plan = billing.plans.firstOrNull { it.isSubscription }
            PremiumPlansScreen(
                plan = plan,
                loading = billing.loading,
                error = billing.error,
                purchasing = billing.purchasingProductId != null,
                onBack = { navController.popBackStack() },
                onSubscribe = { plan?.let { vm.purchase(it) } },
                onRetry = { vm.load() },
            )
        }

        composable<PurchaseCreditsRoute> {
            val vm = remember { BillingViewModel(DataModule.paymentManager, DataModule.userManager, DataModule.remoteConfig.monetization) }
            DisposableEffect(Unit) { onDispose { vm.onCleared() } }
            val billing by vm.state.collectAsState()

            LaunchedEffect(billing.purchaseComplete) {
                if (billing.purchaseComplete) {
                    vm.consumePurchaseComplete()
                    navController.navigate(CreditsSuccessRoute) { popUpTo(PurchaseCreditsRoute) { inclusive = true } }
                }
            }

            PurchaseCreditsScreen(
                credits = currentUserState().credits,
                packs = billing.plans.filter { !it.isSubscription },
                generationCostGems = DataModule.remoteConfig.monetization.generationCostGems,
                loading = billing.loading,
                error = billing.error,
                purchasingProductId = billing.purchasingProductId,
                onBack = { navController.popBackStack() },
                onSelectPack = { vm.purchase(it) },
                onRetry = { vm.load() },
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
    // Single source of truth for user data (credits, avatar) — observed once, shown on every tab.
    val user = currentUserState()

    when (tab) {
        AuraTab.Feed -> {
            val vm = remember { HomeFeedViewModel(DataModule.feedRepository) }
            DisposableEffect(Unit) { onDispose { vm.onCleared() } }
            val feedState by vm.feedState.collectAsState()

            HomeFeedScreen(
                credits = user.credits,
                avatarUrl = user.avatarUrl,
                feedState = feedState,
                selectedTab = tab,
                onSelectTab = { tab = it },
                onTemplateClick = { navController.navigate(TemplateDetailRoute(it.id, it.name)) },
                onCategoryClick = { navController.navigate(CategoryDetailRoute(it.id, it.name)) },
                onSeeAll = { navController.navigate(TrayListingRoute(it.id, it.title, it.kind.name)) },
                onRetry = { vm.refresh() },
            )
        }

        AuraTab.MyCreations -> {
            val vm = remember { HistoryViewModel(DataModule.creationsRepository) }
            DisposableEffect(Unit) { onDispose { vm.onCleared() } }
            val state by vm.state.collectAsState()

            HistoryScreen(
                items = (state as? UiState.Success)?.data ?: emptyList(),
                loading = state is UiState.Loading,
                credits = user.credits,
                avatarUrl = user.avatarUrl,
                selectedTab = tab,
                onSelectTab = { tab = it },
                onItemClick = { navController.navigate(ResultRoute(it.id)) },
            )
        }

        AuraTab.Profile -> if (auth.isLoggedIn) {
            val vm = remember { ProfileViewModel(DataModule.userManager) }
            DisposableEffect(Unit) { onDispose { vm.onCleared() } }
            val state by vm.state.collectAsState()

            ProfileScreen(
                state = state,
                selectedTab = tab,
                onSelectTab = { tab = it },
                onUpgrade = { navController.navigate(PremiumPlansRoute) },
                onPurchaseCredits = { navController.navigate(PurchaseCreditsRoute) },
                onOpenSettings = { navController.navigate(SettingsRoute) },
                onPrivacyPolicy = { navController.navigate(WebViewRoute(PRIVACY_URL, "Privacy Policy")) },
                onRetry = { vm.refresh() },
                onLogout = {
                    vm.logout()
                    auth.logout()
                    DataModule.paymentManager.onLoggedOut()
                },
            )
        } else {
            val loginVm = remember { LoginViewModel(DataModule.authRepository, DataModule.userManager) }
            DisposableEffect(Unit) { onDispose { loginVm.onCleared() } }
            val loginState by loginVm.state.collectAsState()
            val googleAuth = rememberGoogleAuthProvider()

            LaunchedEffect(loginState) {
                if (loginState is LoginUiState.Success) {
                    auth.onLoginSuccess()
                    DataModule.userManager.current.id?.let { DataModule.paymentManager.identify(it) }
                }
            }

            AuraTabScaffold(
                selectedTab = tab,
                onSelectTab = { tab = it },
            ) { pad ->
                LoginScreen(
                    modifier = Modifier.fillMaxSize().padding(pad),
                    loading = loginState is LoginUiState.Loading,
                    errorMessage = (loginState as? LoginUiState.Error)?.message,
                    // Runs the platform Google flow → ID token → POST /auth/google → JWT stored.
                    onGoogleSignIn = { loginVm.signIn { googleAuth.signIn() } },
                )
            }
        }
    }
}
