package com.ferbotz.aurapix.ui.navigation

import kotlinx.serialization.Serializable

/** Type-safe navigation routes (Compose Navigation + kotlinx.serialization). */

@Serializable
object SplashRoute

/** The bottom-navigation host (Feed / My Creations / Profile-or-Login). */
@Serializable
object HomeRoute

@Serializable
data class TemplateDetailRoute(val title: String)

@Serializable
object UploadRoute

@Serializable
object ProcessingRoute

@Serializable
object ResultRoute

@Serializable
object GenerationFailedRoute

@Serializable
object SettingsRoute

@Serializable
object PremiumPlansRoute

@Serializable
object PurchaseCreditsRoute

@Serializable
object CreditsSuccessRoute

@Serializable
object SubscriptionSuccessRoute

@Serializable
object HelpRoute

@Serializable
data class WebViewRoute(val url: String, val title: String)
