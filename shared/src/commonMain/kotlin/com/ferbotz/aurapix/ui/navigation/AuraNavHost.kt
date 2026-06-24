package com.ferbotz.aurapix.ui.navigation

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.ferbotz.aurapix.ui.components.AuraBottomBar
import com.ferbotz.aurapix.ui.components.AuraTab
import com.ferbotz.aurapix.ui.components.WebViewScreen
import com.ferbotz.aurapix.ui.screens.CreditsSuccessScreen
import com.ferbotz.aurapix.ui.screens.GenerationFailedScreen
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
import com.ferbotz.aurapix.ui.screens.UploadPhotosScreen
import kotlinx.coroutines.delay
import kotlin.random.Random

private const val PRIVACY_URL = "https://policies.google.com/privacy"
private const val TERMS_URL = "https://policies.google.com/terms"
private const val SUPPORT_URL = "https://support.google.com"

/**
 * Root navigation graph.
 *
 * Splash → Home (bottom-nav: Feed / My Creations / Profile-or-Login).
 *  - Feed → Template Detail → Processing → Result (or Generation Failed) ; Feed FAB → Upload → Processing.
 *  - My Creations → Result (view a past creation).
 *  - Profile → Settings / Purchase Credits / Premium Plans / Help ; purchases → Success screens.
 *  - Privacy/Terms/Support → in-app WebView.
 */
@Composable
fun AuraNavHost(
    navController: NavHostController = rememberNavController(),
    auth: AuthState = remember { AuthState() },
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
            TemplateDetailScreen(
                title = route.title,
                onBack = { navController.popBackStack() },
                onGenerate = { navController.navigate(ProcessingRoute) },
            )
        }

        composable<UploadRoute> {
            UploadPhotosScreen(
                onBack = { navController.popBackStack() },
                onTrain = { navController.navigate(ProcessingRoute) },
            )
        }

        composable<ProcessingRoute> {
            var progress by remember { mutableFloatStateOf(0f) }
            val willSucceed = remember { Random.nextFloat() < 0.85f }
            LaunchedEffect(Unit) {
                animate(0f, 1f, animationSpec = tween(durationMillis = 2600)) { value, _ -> progress = value }
                navController.navigate(if (willSucceed) ResultRoute else GenerationFailedRoute) {
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

/** Bottom-navigation host. Each tab screen renders its own bar; the Login tab keeps the bar too. */
@Composable
private fun HomeContainer(navController: NavHostController, auth: AuthState) {
    var tab by remember { mutableStateOf(AuraTab.Feed) }
    when (tab) {
        AuraTab.Feed -> HomeFeedScreen(
            selectedTab = tab,
            onSelectTab = { tab = it },
            onCreate = { navController.navigate(UploadRoute) },
            onTemplateClick = { navController.navigate(TemplateDetailRoute(it.name)) },
        )

        AuraTab.MyCreations -> HistoryScreen(
            selectedTab = tab,
            onSelectTab = { tab = it },
            onItemClick = { navController.navigate(ResultRoute) },
        )

        AuraTab.Profile -> if (auth.isLoggedIn) {
            ProfileScreen(
                selectedTab = tab,
                onSelectTab = { tab = it },
                onUpgrade = { navController.navigate(PremiumPlansRoute) },
                onPurchaseCredits = { navController.navigate(PurchaseCreditsRoute) },
                onOpenSettings = { navController.navigate(SettingsRoute) },
                onHelp = { navController.navigate(HelpRoute) },
                onPrivacyPolicy = { navController.navigate(WebViewRoute(PRIVACY_URL, "Privacy Policy")) },
                onLogout = { auth.logout() },
            )
        } else {
            Scaffold(
                bottomBar = { AuraBottomBar(selected = tab, onSelect = { tab = it }) },
                containerColor = MaterialTheme.colorScheme.background,
            ) { innerPadding ->
                LoginScreen(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    onGoogleSignIn = { auth.login() },
                    onContinueAsGuest = { auth.login() },
                )
            }
        }
    }
}
