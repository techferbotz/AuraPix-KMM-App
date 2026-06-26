package com.ferbotz.aurapix.data

import com.ferbotz.aurapix.data.local.AppDatabase
import com.ferbotz.aurapix.data.local.buildDatabase
import com.ferbotz.aurapix.data.local.databaseBuilder
import com.ferbotz.aurapix.data.prefs.AppPreferences
import com.ferbotz.aurapix.data.remote.AuraApi
import com.ferbotz.aurapix.data.remote.createHttpClient
import com.ferbotz.aurapix.data.repository.AuthRepository
import com.ferbotz.aurapix.data.repository.CategoriesRepository
import com.ferbotz.aurapix.data.repository.CreationsRepository
import com.ferbotz.aurapix.data.repository.FeedRepository
import com.ferbotz.aurapix.data.repository.ProfileRepository
import com.ferbotz.aurapix.data.repository.SubscriptionsRepository
import com.ferbotz.aurapix.data.repository.TemplatesRepository
import com.russhwolf.settings.Settings
import io.ktor.client.HttpClient

object DataModule {

    // Preferences must be first — the HTTP client reads the auth token from it.
    val preferences: AppPreferences by lazy { AppPreferences(Settings()) }

    val httpClient: HttpClient by lazy { createHttpClient(preferences = preferences) }

    val api: AuraApi by lazy { AuraApi(httpClient) }

    val database: AppDatabase by lazy { buildDatabase(databaseBuilder()) }

    val authRepository: AuthRepository by lazy { AuthRepository(api, preferences) }

    val profileRepository: ProfileRepository by lazy { ProfileRepository(api, preferences) }

    val feedRepository: FeedRepository by lazy { FeedRepository(api) }

    val categoriesRepository: CategoriesRepository by lazy { CategoriesRepository(api) }

    val templatesRepository: TemplatesRepository by lazy { TemplatesRepository(api) }

    val creationsRepository: CreationsRepository by lazy {
        CreationsRepository(api, database.creationDao())
    }

    val subscriptionsRepository: SubscriptionsRepository by lazy { SubscriptionsRepository(api) }
}
