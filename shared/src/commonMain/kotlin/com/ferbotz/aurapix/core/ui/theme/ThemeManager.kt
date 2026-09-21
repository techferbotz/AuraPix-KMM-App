package com.ferbotz.aurapix.core.ui.theme

import com.ferbotz.aurapix.core.data.prefs.AppPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Single source of truth for the app's appearance — the same role `UserManager` plays for the
 * signed-in user. The app root collects [mode] and hands it to [AuraPixTheme]; the Settings
 * switch calls [setDark]. Nothing else picks a theme.
 *
 * The value is seeded from prefs in the constructor, so the very first frame is already the
 * right theme, and written back on every change, so the choice survives a restart. A fresh
 * install starts on [ThemeMode.Dark]. It lives in prefs rather than in the session: signing out
 * keeps the theme the user picked.
 */
class ThemeManager(private val prefs: AppPreferences) {

    private val _mode = MutableStateFlow(ThemeMode.from(prefs.themeMode))
    val mode: StateFlow<ThemeMode> = _mode.asStateFlow()

    val current: ThemeMode get() = _mode.value

    fun set(mode: ThemeMode) {
        if (_mode.value == mode) return
        prefs.themeMode = mode.storageKey
        _mode.value = mode
    }

    /** Convenience for the Settings switch, which is a plain on/off. */
    fun setDark(dark: Boolean) = set(if (dark) ThemeMode.Dark else ThemeMode.Light)
}
