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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalUriHandler
import com.ferbotz.aurapix.core.auth.rememberGoogleAuthProvider
import com.ferbotz.aurapix.core.config.LocalRemoteConfig
import com.ferbotz.aurapix.core.config.Store
import com.ferbotz.aurapix.core.data.remote.ApiError
import com.ferbotz.aurapix.core.di.DataModule
import com.ferbotz.aurapix.core.media.rememberImageActions
import com.ferbotz.aurapix.core.ui.components.AuraTab
import com.ferbotz.aurapix.core.ui.components.AuraTabScaffold
import com.ferbotz.aurapix.core.ui.components.plainTextClipEntry
import com.ferbotz.aurapix.core.ui.components.rememberInAppBrowser
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
import com.ferbotz.aurapix.profile.ui.AccountDeletedScreen
import com.ferbotz.aurapix.profile.ui.DeleteAccountScreen
import com.ferbotz.aurapix.profile.ui.DeleteAccountStep
import com.ferbotz.aurapix.profile.ui.DeleteAccountViewModel
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
import kotlinx.coroutines.launch

private const val TEMPLATE_SHARE_BASE = "https://aurapix.ferbotz.com/template/"

@Composable
fun AuraNavHost(
    navController: NavHostController = rememberNavController(),
    auth: AuthState = remember { AuthState(DataModule.userManager) },
) {
    // A 401 drops the session from the network layer (BE-008); keep the nav graph in step so the
    // app quietly becomes a guest wherever the user is, rather than carrying on as signed in.
    LaunchedEffect(auth) { auth.observeSession() }

    // Legal URLs are served by GET /config so the pages can move without a release (BE-005), and
    // open in the in-app browser (API.md §4.17b).
    val links = LocalRemoteConfig.current.links
    val browser = rememberInAppBrowser()
    val uriHandler = LocalUriHandler.current
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()

    // Links that belong to another app — Play's subscription screen. On Android a device with
    // nothing to handle one throws instead of doing nothing, so never let that reach the user.
    val openExternally: (String) -> Unit = { url -> runCatching { uriHandler.openUri(url) } }

    // With no mail app installed there's nowhere to send a mailto:, so leave the address on the
    // clipboard instead. Settings prints it under the row as well.
    val contactSupport: () -> Unit = {
        runCatching { uriHandler.openUri("mailto:${links.supportEmail}") }.onFailure {
            scope.launch { clipboard.setClipEntry(plainTextClipEntry("AuraPix support", links.supportEmail)) }
        }
    }

    // Shared across TemplateDetail → Processing → Result/Failed so the generation survives navigation.
    val generationVm = remember { GenerationViewModel(DataModule.creationsRepository) }
    DisposableEffect(Unit) { onDispose { generationVm.onCleared() } }

    // Deep link: route to the template once Splash has handed off (keeps Home in the back stack).
    val currentEntry by navController.currentBackStackEntryAsState()
    val pendingTemplateId by DeepLinks.pendingTemplateId.collectAsState()
    LaunchedEffect(pendingTemplateId, currentEntry) {
        val id = pendingTemplateId ?: return@LaunchedEffect
        if (currentEntry?.destination?.hasRoute(SplashRoute::class) != false) return@LaunchedEffect
        navController.navigate(TemplateDetailRoute(id, ""))
        DeepLinks.consume()
    }

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
            val store = LocalRemoteConfig.current.store
            val cost = LocalRemoteConfig.current.generation.creditCost

            // Generate gate: signed in → enough gems → generate; else show the login / paywall sheet.
            var loginImages by remember { mutableStateOf<List<ByteArray>?>(null) }
            var paywallImages by remember { mutableStateOf<List<ByteArray>?>(null) }

            val startGeneration: (List<ByteArray>) -> Unit = { images ->
                generationVm.generate(route.templateId, images)
                navController.navigate(ProcessingRoute)
            }

            val imageActions = rememberImageActions()

            TemplateDetailScreen(
                state = state,
                generationCost = cost,
                onBack = { navController.popBackStack() },
                onShare = { imageActions.shareLink("$TEMPLATE_SHARE_BASE${route.templateId}") },
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
                    store = store,
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
            val store = LocalRemoteConfig.current.store
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
                    store = store,
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
            val themeMode by DataModule.themeManager.mode.collectAsState()
            SettingsScreen(
                name = user.name ?: "",
                email = user.email ?: "",
                avatarUrl = user.avatarUrl,
                signedIn = user.isLoggedIn,
                supportEmail = links.supportEmail,
                darkTheme = themeMode.isDark,
                onDarkThemeChange = { DataModule.themeManager.setDark(it) },
                onBack = { navController.popBackStack() },
                onPrivacyPolicy = { browser.open(links.privacyPolicy) },
                onTerms = { browser.open(links.terms) },
                onContactSupport = contactSupport,
                onLogout = {
                    auth.logout()
                    DataModule.paymentManager.onLoggedOut()
                    navController.popBackStack(HomeRoute, inclusive = false)
                },
                // Delete Account is hidden for now, so nothing links to DeleteAccountRoute. To
                // bring it back: onDeleteAccount = { navController.navigate(DeleteAccountRoute) }
            )
        }

        composable<DeleteAccountRoute> {
            val user = currentUserState()
            val vm = remember {
                DeleteAccountViewModel(
                    DataModule.userManager,
                    DataModule.subscriptionsRepository,
                    DataModule.creationsRepository,
                    DataModule.paymentManager,
                )
            }
            DisposableEffect(Unit) { onDispose { vm.onCleared() } }
            val state by vm.state.collectAsState()
            var signingIn by remember { mutableStateOf(false) }

            // Settings and this screen belong to an account that no longer exists: drop both, so
            // Back from the confirmation lands on Home as a guest.
            LaunchedEffect(state.step) {
                if (state.step == DeleteAccountStep.Deleted) {
                    navController.navigate(AccountDeletedRoute(state.manageSubscriptionUrl)) {
                        popUpTo(HomeRoute) { inclusive = false }
                    }
                }
            }

            DeleteAccountScreen(
                email = user.email,
                gems = user.credits,
                signedIn = user.isLoggedIn,
                state = state,
                onBack = { navController.popBackStack() },
                onDelete = { vm.delete() },
                onSignIn = { signingIn = true },
                onWhatGetsDeleted = { browser.open(links.deleteAccount) },
                onManageSubscription = { state.manageSubscriptionUrl?.let(openExternally) },
            )

            if (signingIn) {
                LoginBottomSheet(
                    onDismiss = { signingIn = false },
                    onLoggedIn = {
                        signingIn = false
                        auth.onLoginSuccess()
                        DataModule.userManager.current.id?.let { DataModule.paymentManager.identify(it) }
                        vm.onSignedIn()
                    },
                )
            }
        }

        composable<AccountDeletedRoute> { entry ->
            val route = entry.toRoute<AccountDeletedRoute>()
            AccountDeletedScreen(
                subscriptionStillBilling = route.manageSubscriptionUrl != null,
                onManageSubscription = { route.manageSubscriptionUrl?.let(openExternally) },
                onDone = { navController.popBackStack(HomeRoute, inclusive = false) },
            )
        }

        composable<PremiumPlansRoute> {
            val store = LocalRemoteConfig.current.store
            val vm = remember {
                BillingViewModel(
                    DataModule.paymentManager,
                    DataModule.userManager,
                    store,
                    Store.Kind.SUBSCRIPTION,
                )
            }
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
            val store = LocalRemoteConfig.current.store
            val vm = remember {
                BillingViewModel(
                    DataModule.paymentManager,
                    DataModule.userManager,
                    store,
                    Store.Kind.ONE_TIME,
                )
            }
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
                // The view model already filtered to ONE_TIME and ordered by the catalogue.
                packs = billing.plans,
                generationCostGems = LocalRemoteConfig.current.generation.creditCost,
                defaultProductId = store.defaultProductId,
                loading = billing.loading,
                error = billing.error,
                purchasingProductId = billing.purchasingProductId,
                onBack = { navController.popBackStack() },
                onConfirm = { vm.purchase(it) },
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
                onContactSupport = contactSupport,
            )
        }
    }
}

/** Persists the selected tab by name — enum values aren't saveable out of the box, and a
 *  name is stable across reorderings of [AuraTab] in a way an ordinal isn't. */
private val AuraTabSaver: Saver<AuraTab, String> = Saver(
    save = { it.name },
    restore = { AuraTab.valueOf(it) },
)

@Composable
private fun HomeContainer(navController: NavHostController, auth: AuthState) {
    // rememberSaveable, not remember: NavHost disposes HomeRoute's content while a pushed
    // screen is on top, so a plain remember drops the selection and Home always came back
    // on Feed. Saveable state is held by the back stack entry and restored on pop.
    var tab by rememberSaveable(stateSaver = AuraTabSaver) { mutableStateOf(AuraTab.Feed) }
    // Single source of truth for user data (credits, avatar) — observed once, shown on every tab.
    val user = currentUserState()
    // Read here rather than inside a callback: composition locals can only be read in a
    // @Composable context, and the tab callbacks below are plain lambdas.
    val links = LocalRemoteConfig.current.links
    val browser = rememberInAppBrowser()

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
                onPrivacyPolicy = { browser.open(links.privacyPolicy) },
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
