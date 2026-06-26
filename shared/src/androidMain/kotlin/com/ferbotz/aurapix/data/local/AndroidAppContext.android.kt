package com.ferbotz.aurapix.data.local

import android.content.Context
import androidx.startup.Initializer

/** Holds the application [Context], captured automatically at startup. */
internal object AndroidAppContext {
    @Volatile
    var application: Context? = null

    fun require(): Context = application
        ?: error("AndroidAppContext not initialized — is AuraStartupInitializer registered?")
}

/**
 * androidx.startup initializer that captures the application context the first time the
 * process starts, so [databaseBuilder] (and other platform code) needs no manual wiring.
 * Registered via the merged manifest (shared androidMain AndroidManifest.xml).
 */
class AuraStartupInitializer : Initializer<Context> {
    override fun create(context: Context): Context {
        val app = context.applicationContext
        AndroidAppContext.application = app
        return app
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
