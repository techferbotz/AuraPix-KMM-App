package com.ferbotz.aurapix.core.ui.theme

/**
 * The app's appearance, chosen by the user in Settings.
 *
 * Deliberately two values and **no `System`**: AuraPix does not follow the device's night
 * setting. A phone in daylight mode still opens the app in [Dark] — the only thing that flips
 * the theme is the switch in Settings.
 *
 * [Dark] is the default for a fresh install; [Light] is the opt-in.
 */
enum class ThemeMode(val storageKey: String) {
    Light("light"),
    Dark("dark"),
    ;

    val isDark: Boolean get() = this == Dark

    companion object {
        /**
         * Parses a persisted [storageKey]. Anything unrecognised — including the legacy
         * `"system"` value written before the app stopped following the device — falls back to
         * [Dark], the default.
         */
        fun from(raw: String?): ThemeMode = entries.firstOrNull { it.storageKey == raw } ?: Dark
    }
}
