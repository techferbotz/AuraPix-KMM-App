package com.ferbotz.aurapix.shell.ui

import kotlinx.serialization.Serializable

@Serializable
object SplashRoute

@Serializable
object HomeRoute

/**
 * [templateId] is the id from every in-app entry point, but a deep link may carry the slug
 * instead (§4.9a) — act on the loaded template's own id, not on this.
 */
@Serializable
data class TemplateDetailRoute(val templateId: String, val title: String)

/** A feed tray's "See all" listing. [kind] is a [com.ferbotz.aurapix.feed.ui.FeedSectionKind] name. */
@Serializable
data class TrayListingRoute(val trayId: String, val title: String, val kind: String)

/** A category's detail page: the category name in the top bar and its templates in a grid. */
@Serializable
data class CategoryDetailRoute(val categoryId: String, val categoryName: String)

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

/** Settings → Delete Account: the confirmation the public deletion page describes (API.md §4.2a). */
@Serializable
object DeleteAccountRoute

/**
 * After a deletion. [manageSubscriptionUrl] is set when the user had an active subscription,
 * which the deletion did not cancel.
 */
@Serializable
data class AccountDeletedRoute(val manageSubscriptionUrl: String? = null)
