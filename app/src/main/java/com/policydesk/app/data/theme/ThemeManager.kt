package com.policydesk.app.data.theme

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppTheme(val displayName: String) {
    DARK("True Dark (OLED)"),
    LIGHT("Cupertino Light"),
    SYSTEM("System Default")
}

enum class AppDesignStyle(val id: String, val displayName: String, val subtitle: String) {
    SEA_GLASS(
        id = "sea_glass",
        displayName = "Google Sea Glass",
        subtitle = "Tumbled ocean pastels, frosted pebble contours & luminous refraction"
    ),
    APPLE_LIQUID(
        id = "apple_liquid",
        displayName = "Apple Liquid Glass",
        subtitle = "Cupertino frosted translucency, iOS grouped surfaces & precision minimalism"
    )
}

val LocalAppDesignStyle = compositionLocalOf { AppDesignStyle.SEA_GLASS }

object ThemeManager {
    private const val PREFS_NAME = "policydesk_theme_prefs"
    private const val KEY_THEME = "app_theme_mode"
    private const val KEY_DESIGN_STYLE = "app_design_style"

    private val _currentTheme = MutableStateFlow(AppTheme.DARK)
    val currentTheme: StateFlow<AppTheme> = _currentTheme.asStateFlow()

    private val _currentDesignStyle = MutableStateFlow(AppDesignStyle.SEA_GLASS)
    val currentDesignStyle: StateFlow<AppDesignStyle> = _currentDesignStyle.asStateFlow()

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedName = prefs.getString(KEY_THEME, AppTheme.DARK.name) ?: AppTheme.DARK.name
        val theme = try {
            AppTheme.valueOf(savedName)
        } catch (_: Exception) {
            AppTheme.DARK
        }
        _currentTheme.value = theme

        val savedStyleId = prefs.getString(KEY_DESIGN_STYLE, AppDesignStyle.SEA_GLASS.id)
        val style = AppDesignStyle.entries.find { it.id == savedStyleId } ?: AppDesignStyle.SEA_GLASS
        _currentDesignStyle.value = style
    }

    fun setTheme(context: Context, theme: AppTheme) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME, theme.name)
            .apply()
        _currentTheme.value = theme
    }

    fun setDesignStyle(context: Context, style: AppDesignStyle) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_DESIGN_STYLE, style.id)
            .apply()
        _currentDesignStyle.value = style
    }
}
