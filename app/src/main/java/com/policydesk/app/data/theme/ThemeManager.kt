package com.policydesk.app.data.theme

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppTheme(val displayName: String) {
    DARK("True Dark (OLED)"),
    LIGHT("Cupertino Light"),
    SYSTEM("System Default")
}

object ThemeManager {
    private const val PREFS_NAME = "policydesk_theme_prefs"
    private const val KEY_THEME = "app_theme_mode"

    private val _currentTheme = MutableStateFlow(AppTheme.DARK)
    val currentTheme: StateFlow<AppTheme> = _currentTheme.asStateFlow()

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedName = prefs.getString(KEY_THEME, AppTheme.DARK.name) ?: AppTheme.DARK.name
        val theme = try {
            AppTheme.valueOf(savedName)
        } catch (_: Exception) {
            AppTheme.DARK
        }
        _currentTheme.value = theme
    }

    fun setTheme(context: Context, theme: AppTheme) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME, theme.name)
            .apply()
        _currentTheme.value = theme
    }
}
