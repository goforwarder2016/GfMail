package com.gf.mail

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import com.gf.mail.presentation.ui.GfmailApp
import com.gf.mail.ui.theme.GfmailTheme
import com.gf.mail.utils.LanguageManager
import com.gf.mail.utils.LanguageTestUtils
import com.gf.mail.utils.ThemeManager

/**
 * Main Activity for the Gfmail application
 * Uses manual dependency injection instead of Hilt
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize ThemeManager
        ThemeManager.initialize(this)
        
        // Apply language setting before setting content
        val languageCode = LanguageManager.getCurrentLanguageCode(this)
        LanguageManager.applyLanguageSetting(this, languageCode)
        
        // Test current language setting
        LanguageTestUtils.testCurrentLanguage(this)
        
        // Force apply language if needed
        if (languageCode != "system") {
            LanguageTestUtils.forceApplyLanguage(this, languageCode)
        }
        
        enableEdgeToEdge()

        // Enable edge-to-edge with proper insets handling
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val currentTheme by ThemeManager.currentTheme.collectAsState()
            val isDarkTheme = when (currentTheme) {
                "dark" -> true
                "light" -> false
                "system" -> androidx.compose.foundation.isSystemInDarkTheme()
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }
            
            GfmailTheme(darkTheme = isDarkTheme) {
                GfmailApp()
            }
        }
    }
}