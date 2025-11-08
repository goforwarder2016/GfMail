package com.gf.mail.utils

import android.content.Context
import android.util.Log
import java.util.*

/**
 * Utility class for testing language settings
 */
object LanguageTestUtils {
    private const val TAG = "LanguageTestUtils"
    
    /**
     * Test current language setting
     */
    fun testCurrentLanguage(context: Context) {
        val currentLocale = context.resources.configuration.locales[0]
        val currentLanguage = currentLocale.language
        val currentCountry = currentLocale.country
        val savedLanguage = LanguageManager.getCurrentLanguageCode(context)
        
        Log.d(TAG, "=== Language Test ===")
        Log.d(TAG, "Current Locale: $currentLocale")
        Log.d(TAG, "Current Language: $currentLanguage")
        Log.d(TAG, "Current Country: $currentCountry")
        Log.d(TAG, "Saved Language: $savedLanguage")
        
        // Test string resource loading
        val testString = context.getString(com.gf.mail.R.string.settings_language)
        Log.d(TAG, "Test String (settings_language): $testString")
        
        val testString2 = context.getString(com.gf.mail.R.string.ok)
        Log.d(TAG, "Test String (ok): $testString2")
        
        Log.d(TAG, "=== End Language Test ===")
    }
    
    /**
     * Force apply language setting
     */
    fun forceApplyLanguage(context: Context, languageCode: String) {
        Log.d(TAG, "Force applying language: $languageCode")
        
        val locale = LocaleUtils.getLocaleFromLanguageCode(languageCode)
        Log.d(TAG, "Target Locale: $locale")
        
        // Set default locale
        Locale.setDefault(locale)
        
        // Update configuration
        val configuration = android.content.res.Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        
        // Apply configuration
        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
        
        Log.d(TAG, "Language force applied: $languageCode")
        
        // Test again
        testCurrentLanguage(context)
    }
}
