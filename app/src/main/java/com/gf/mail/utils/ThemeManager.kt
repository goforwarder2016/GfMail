package com.gf.mail.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Theme manager for handling theme state across the app
 */
object ThemeManager {
    private const val PREFS_NAME = "gfmail_theme_prefs"
    private const val KEY_THEME = "app_theme"
    
    private val _currentTheme = MutableStateFlow("system")
    val currentTheme: StateFlow<String> = _currentTheme.asStateFlow()
    
    /**
     * Initialize theme manager with context
     */
    fun initialize(context: Context) {
        val savedTheme = getCurrentTheme(context)
        _currentTheme.value = savedTheme
    }
    
    /**
     * Get current theme setting
     */
    fun getCurrentTheme(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_THEME, "system") ?: "system"
    }
    
    /**
     * Save theme setting
     */
    fun saveTheme(context: Context, theme: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_THEME, theme).apply()
        _currentTheme.value = theme
    }
    
    /**
     * Get current theme for Compose
     */
    @Composable
    fun getCurrentTheme(): String {
        val context = LocalContext.current
        return getCurrentTheme(context)
    }
    
    /**
     * Check if dark theme should be used
     */
    @Composable
    fun isDarkTheme(): Boolean {
        val theme by currentTheme.collectAsState()
        return when (theme) {
            "dark" -> true
            "light" -> false
            "system" -> androidx.compose.foundation.isSystemInDarkTheme()
            else -> androidx.compose.foundation.isSystemInDarkTheme()
        }
    }
}

/**
 * CompositionLocal for theme state
 */
val LocalThemeManager = staticCompositionLocalOf { ThemeManager }
