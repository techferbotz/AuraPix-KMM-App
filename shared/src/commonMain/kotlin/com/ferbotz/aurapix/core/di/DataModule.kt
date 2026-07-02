package com.ferbotz.aurapix.core.di

import com.ferbotz.aurapix.billing.data.PaymentManager
import com.ferbotz.aurapix.billing.data.SubscriptionRemoteDataSource
import com.ferbotz.aurapix.billing.data.SubscriptionsRepository
import com.ferbotz.aurapix.category.data.CategoriesRepository
import com.ferbotz.aurapix.category.data.CategoryRemoteDataSource
import com.ferbotz.aurapix.core.config.DefaultRemoteConfig
import com.ferbotz.aurapix.core.config.RemoteConfig
import com.ferbotz.aurapix.core.data.local.AppDatabase
import com.ferbotz.aurapix.core.data.local.buildDatabase
import com.ferbotz.aurapix.core.data.local.databaseBuilder
import com.ferbotz.aurapix.core.data.prefs.AppPreferences
import com.ferbotz.aurapix.core.data.remote.HealthRemoteDataSource
import com.ferbotz.aurapix.core.data.remote.createHttpClient
import com.ferbotz.aurapix.creation.data.CreationRemoteDataSource
import com.ferbotz.aurapix.creation.data.CreationsRepository
import com.ferbotz.aurapix.feed.data.FeedRemoteDataSource
import com.ferbotz.aurapix.feed.data.FeedRepository
import com.ferbotz.aurapix.profile.data.AuthRemoteDataSource
import com.ferbotz.aurapix.profile.data.AuthRepository
import com.ferbotz.aurapix.profile.data.ProfileRemoteDataSource
import com.ferbotz.aurapix.profile.data.ProfileRepository
import com.ferbotz.aurapix.profile.data.UserManager
import com.ferbotz.aurapix.template.data.TemplateRemoteDataSource
import com.ferbotz.aurapix.template.data.TemplatesRepository
import com.russhwolf.settings.Settings
import io.ktor.client.HttpClient

/**
 * Manual composition root. Each feature owns its remote data source + repository; this object
 * just wires them together. Swap for a DI container (e.g. Koin) when the app grows.
 */
object DataModule {

    // ── Core infrastructure ─────────────────────────────────────────────────
    // Preferences must be first — the HTTP client reads the auth token from it.
    val preferences: AppPreferences by lazy { AppPreferences(Settings()) }
    val remoteConfig: RemoteConfig by lazy { DefaultRemoteConfig() }
    val httpClient: HttpClient by lazy { createHttpClient(preferences = preferences) }
    val database: AppDatabase by lazy { buildDatabase(databaseBuilder()) }

    // ── Remote data sources (per feature) ───────────────────────────────────
    val feedRemoteDataSource by lazy { FeedRemoteDataSource(httpClient) }
    val templateRemoteDataSource by lazy { TemplateRemoteDataSource(httpClient) }
    val categoryRemoteDataSource by lazy { CategoryRemoteDataSource(httpClient) }
    val creationRemoteDataSource by lazy { CreationRemoteDataSource(httpClient) }
    val authRemoteDataSource by lazy { AuthRemoteDataSource(httpClient) }
    val profileRemoteDataSource by lazy { ProfileRemoteDataSource(httpClient) }
    val subscriptionRemoteDataSource by lazy { SubscriptionRemoteDataSource(httpClient) }
    val healthRemoteDataSource by lazy { HealthRemoteDataSource(httpClient) }

    // ── Repositories (per feature) ──────────────────────────────────────────
    val feedRepository by lazy { FeedRepository(feedRemoteDataSource) }
    val templatesRepository by lazy { TemplatesRepository(templateRemoteDataSource) }
    val categoriesRepository by lazy { CategoriesRepository(categoryRemoteDataSource) }
    val creationsRepository by lazy { CreationsRepository(creationRemoteDataSource, database.creationDao()) }
    val authRepository by lazy { AuthRepository(authRemoteDataSource, preferences) }
    val profileRepository by lazy { ProfileRepository(profileRemoteDataSource, preferences) }
    val userManager by lazy { UserManager(profileRemoteDataSource, preferences) }
    val paymentManager by lazy { PaymentManager() }
    val subscriptionsRepository by lazy { SubscriptionsRepository(subscriptionRemoteDataSource) }
}
