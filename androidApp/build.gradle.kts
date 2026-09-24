import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

// Firebase (Crashlytics + Cloud Messaging) is configured by `google-services.json` in this module,
// downloaded from the Firebase console. Without it the build still succeeds and the app runs with
// Firebase off (no crash reports, no push), so a fresh checkout isn't broken. Both plugins need the
// file, so they're applied together or not at all.
if (file("google-services.json").exists()) {
    apply(plugin = libs.plugins.googleServices.get().pluginId)
    apply(plugin = libs.plugins.firebaseCrashlytics.get().pluginId)
} else {
    logger.warn(
        "androidApp/google-services.json is missing — Firebase (Crashlytics, push notifications) " +
            "is off in this build. Download it from the Firebase console; see docs/FIREBASE.md.",
    )
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    implementation(projects.shared)

    implementation(libs.androidx.activity.compose)

    // Firebase: Crashlytics (crash reports) and Cloud Messaging (push). Inert until
    // google-services.json is added — see above.
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.messaging)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}

/**
 * Release signing credentials, read from `local.properties` (gitignored) or the environment.
 *
 * Google Play will not transact against a binary signed with the debug certificate: product
 * details query fine, but the purchase itself comes back "the item you were attempting to
 * purchase could not be found". Testing in-app purchases therefore needs a build signed with the
 * same key as the one uploaded to Play — see docs/RELEASE_SIGNING.md.
 *
 * The keystore and its passwords are never committed; only their location is configured here.
 */
val signingProps = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

fun signingValue(key: String): String? =
    (signingProps.getProperty(key) ?: System.getenv(key))?.takeIf { it.isNotBlank() }

android {
    namespace = "com.ferbotz.aurapix"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.ferbotz.aurapix"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 2
        versionName = "1.0.0"
        // Crash reports from release builds only: a debug build's crashes are the developer's,
        // and in the console they'd bury real users' ones. Read by the manifest's
        // `firebase_crashlytics_collection_enabled`.
        manifestPlaceholders["crashlyticsCollectionEnabled"] = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    signingConfigs {
        create("release") {
            val storePath = signingValue("RELEASE_STORE_FILE")
            if (storePath != null) {
                storeFile = file(storePath)
                storePassword = signingValue("RELEASE_STORE_PASSWORD")
                keyAlias = signingValue("RELEASE_KEY_ALIAS")
                keyPassword = signingValue("RELEASE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        getByName("debug") {
            manifestPlaceholders["crashlyticsCollectionEnabled"] = false
        }
        getByName("release") {
            isMinifyEnabled = false
            // Left unsigned when the credentials aren't configured, so a checkout without the
            // keystore still builds rather than failing at configuration time.
            signingConfig = signingConfigs.getByName("release").takeIf { it.storeFile != null }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}