package com.ferbotz.aurapix.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.ferbotz.aurapix.data.DataModule
import com.ferbotz.aurapix.ui.components.AuraBottomBar
import com.ferbotz.aurapix.ui.components.AuraTab
import com.ferbotz.aurapix.ui.components.WebViewScreen
import com.ferbotz.aurapix.ui.screens.CreditsSuccessScreen
import com.ferbotz.aurapix.ui.screens.GenerationFailedScreen
import com.ferbotz.aurapix.ui.screens.HistoryItem
import com.ferbotz.aurapix.ui.screens.HelpFaqScreen
import com.ferbotz.aurapix.ui.screens.HistoryScreen
import com.ferbotz.aurapix.ui.screens.HomeFeedScreen
import com.ferbotz.aurapix.ui.screens.LoginScreen
import com.ferbotz.aurapix.ui.screens.PremiumPlansScreen
import com.ferbotz.aurapix.ui.screens.ProcessingScreen
import com.ferbotz.aurapix.ui.screens.ProfileScreen
import com.ferbotz.aurapix.ui.screens.PurchaseCreditsScreen
import com.ferbotz.aurapix.ui.screens.ResultScreen
import com.ferbotz.aurapix.ui.screens.SettingsScreen
import com.ferbotz.aurapix.ui.screens.SplashScreen
import com.ferbotz.aurapix.ui.screens.SubscriptionSuccessScreen
import com.ferbotz.aurapix.ui.screens.TemplateDetailScreen
import com.ferbotz.aurapix.ui.screens.TemplateItem
import com.ferbotz.aurapix.ui.screens.UploadPhotosScreen
import com.ferbotz.aurapix.ui.viewmodel.GenerationViewModel
import com.ferbotz.aurapix.ui.viewmodel.HistoryViewModel
import com.ferbotz.aurapix.ui.viewmodel.HomeFeedViewModel
import com.ferbotz.aurapix.ui.viewmodel.ProfileViewModel
import com.ferbotz.aurapix.ui.viewmodel.TemplateDetailViewModel
import com.ferbotz.aurapix.ui.viewmodel.UiState
import kotlinx.coroutines.delay

private const val PRIVACY_URL = "https://policies.google.com/privacy"
private const val TERMS_URL = "https://policies.google.com/terms"
private const val SUPPORT_URL = "https://support.google.com"

@Composable
fun AuraNavHost(
    navController: NavHostController = rememberNavController(),
    auth: AuthState = remember { AuthState(DataModule.authRepository) },
) {
    // GenerationViewModel lives here so it survives Upload → Processing → Result transitions.
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

        composable<HomeRoute> { HomeContainer(navController, auth, generationVm) }

        composable<TemplateDetailRoute> { entry ->
            val route = entry.toRoute<TemplateDetailRoute>()
            val vm = remember { TemplateDetailViewModel(DataModule.templatesRepository) }
            DisposableEffect(route.templateId) { onDispose { vm.onCleared() } }
            LaunchedEffect(route.templateId) { vm.load(route.templateId) }
            val state by vm.templateState.collectAsState()
            TemplateDetailScreen(
                title = route.title,
                onBack = { navController.popBackStack() },
                onGenerate = {
                    val detail = (state as? UiState.Success)?.data
                    if (detail != null) {
                        navController.navigate(UploadRoute)
                    }
                },
            )
        }

        composable<UploadRoute> {
            UploadPhotosScreen(
                onBack = { navController.popBackStack() },
                onTrain = { navController.navigate(ProcessingRoute) },
            )
        }

        composable<ProcessingRoute> {
            val pollingState by generationVm.pollingState.collectAsState()
            LaunchedEffect(pollingState) {
                when (val s = pollingState) {
                    is UiState.Success -> {
                        val creationId = s.data.id
                        if (s.data.status == "COMPLETED") {
                            navController.navigate(ResultRoute(creationId)) {
                                popUpTo(HomeRoute) { inclusive = false }
                            }
                        } else {
                            navController.navigate(GenerationFailedRoute) {
                                popUpTo(HomeRoute) { inclusive = false }
                            }
                        }
                    }
                    is UiState.Error -> {
                        navController.navigate(GenerationFailedRoute) {
                            popUpTo(HomeRoute) { inclusive = false }
                        }
                    }
                    else -> {}
                }
            }
            ProcessingScreen(progress = if (pollingState is UiState.Loading) 0f else 0.5f)
        }

        composable<ResultRoute> { entry ->
            val route = entry.toRoute<ResultRoute>()
            ResultScreen(
                onBack = { navController.popBackStack(HomeRoute, inclusive = false) },
                onRetry = {
                    generationVm.reset()
                    navController.navigate(UploadRoute) {
                        popUpTo(HomeRoute) { inclusive = false }
                    }
                },
            )
        }

        composable<GenerationFailedRoute> {
            GenerationFailedScreen(
                onRetry = {
                    generationVm.reset()
                    navController.navigate(UploadRoute) {
                        popUpTo(GenerationFailedRoute) { inclusive = true }
                    }
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
private fun HomeContainer(
    navController: NavHostController,
    auth: AuthState,
    generationVm: GenerationViewModel,
) {
    var tab by remember { mutableStateOf(AuraTab.Feed) }

    when (tab) {
        AuraTab.Feed -> {
            val vm = remember {
                HomeFeedViewModel(DataModule.feedRepository, DataModule.categoriesRepository, DataModule.profileRepository)
            }
            DisposableEffect(Unit) { onDispose { vm.onCleared() } }
            val traysState by vm.traysState.collectAsState()
            val categoriesState by vm.categoriesState.collectAsState()
            val credits by vm.credits.collectAsState()

            val categoryNames = (categoriesState as? UiState.Success)?.data?.map { it.name } ?: emptyList()
            val templateItems = (traysState as? UiState.Success)?.data
                ?.firstOrNull { it.type == "TEMPLATE" }
                ?.let { tray -> vm.templateItems(tray).map { t -> TemplateItem(name = t.title, id = t.id, thumbnailUrl = t.thumbnailImageUrl) } }
                ?: emptyList()

            HomeFeedScreen(
                credits = credits,
                categories = categoryNames,
                templates = templateItems,
                selectedTab = tab,
                onSelectTab = { tab = it },
                onCreate = { navController.navigate(UploadRoute) },
                onTemplateClick = { navController.navigate(TemplateDetailRoute(it.id, it.name)) },
            )
        }

        AuraTab.MyCreations -> {
            val vm = remember { HistoryViewModel(DataModule.creationsRepository) }
            DisposableEffect(Unit) { onDispose { vm.onCleared() } }
            val creations by vm.creationsFlow.collectAsState(initial = emptyList())

            val historyItems = creations.map { entity ->
                HistoryItem(
                    id = entity.id,
                    title = entity.templateTitleSnapshot,
                    category = entity.status,
                    imageUrl = entity.generatedImageUrl,
                    status = entity.status,
                )
            }

            HistoryScreen(
                items = historyItems,
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
                    modifier = androidx.compose.ui.Modifier.fillMaxSize().padding(innerPadding),
                    // Google sign-in: the platform layer handles token acquisition and calls
                    // AuthRepository.loginWithGoogle(idToken) before invoking this callback.
                    onGoogleSignIn = { auth.onLoginSuccess() },
                    onContinueAsGuest = { auth.onLoginSuccess() },
                )
            }
        }
    }
}
