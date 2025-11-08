package com.gf.mail.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import java.util.*

/**
 * Language manager for handling language switching
 */
object LanguageManager {
    private const val TAG = "LanguageManager"
    
    /**
     * Apply language setting to context
     */
    fun applyLanguageSetting(context: Context, languageCode: String) {
        try {
            val locale = LocaleUtils.getLocaleFromLanguageCode(languageCode)
            val configuration = Configuration(context.resources.configuration)
            configuration.setLocale(locale)
            configuration.setLayoutDirection(locale)
            
            // Always use the deprecated method for immediate effect
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
            
            Log.d(TAG, "Language setting applied: $languageCode")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to apply language setting", e)
        }
    }
    
    /**
     * Restart activity to apply language changes immediately
     */
    fun restartActivity(activity: Activity) {
        try {
            // Clear all activity flags and create a fresh intent
            val intent = Intent(activity, activity.javaClass)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            
            activity.finish()
            activity.startActivity(intent)
            activity.overridePendingTransition(0, 0)
            Log.d(TAG, "Activity restarted to apply language changes")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to restart activity", e)
        }
    }
    
    /**
     * Get current language code from SharedPreferences
     */
    fun getCurrentLanguageCode(context: Context): String {
        val sharedPreferences = context.getSharedPreferences("gfmail_settings", Context.MODE_PRIVATE)
        return sharedPreferences.getString("app_language", "system") ?: "system"
    }
    
    /**
     * Save language code to SharedPreferences
     */
    fun saveLanguageCode(context: Context, languageCode: String) {
        val sharedPreferences = context.getSharedPreferences("gfmail_settings", Context.MODE_PRIVATE)
        sharedPreferences.edit()
            .putString("app_language", languageCode)
            .apply()
        Log.d(TAG, "Language code saved: $languageCode")
    }
}
