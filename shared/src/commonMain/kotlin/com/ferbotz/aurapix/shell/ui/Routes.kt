package com.ferbotz.aurapix.shell.ui

import kotlinx.serialization.Serializable

@Serializable
object SplashRoute

@Serializable
object HomeRoute

@Serializable
data class TemplateDetailRoute(val templateId: String, val title: String)

/** A feed tray's "See all" listing. [kind] is a [com.ferbotz.aurapix.feed.ui.FeedSectionKind] name. */
@Serializable
data class TrayListingRoute(val trayId: String, val title: String, val kind: String)

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
