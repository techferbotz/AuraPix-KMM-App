package com.ferbotz.aurapix.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
object SplashRoute

@Serializable
object HomeRoute

@Serializable
data class TemplateDetailRoute(val templateId: String, val title: String)

@Serializable
object UploadRoute

@Serializable
object ProcessingRoute

@Serializable
data class ResultRoute(val creationId: String)

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
