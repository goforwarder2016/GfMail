package com.gf.mail.utils

import android.content.Context
import android.content.res.Configuration
import com.gf.mail.R
import java.util.*

/**
 * Utility class for managing app localization and language settings
 */
object LocaleUtils {

    /**
     * Supported language codes
     */
    enum class SupportedLanguage(val code: String, val displayName: String) {
        SYSTEM("system", "System Default"),
        ENGLISH("en", "English"),
        CHINESE_SIMPLIFIED("zh-CN", "简体中文"),
        CHINESE_TRADITIONAL("zh-TW", "繁體中文"),
        JAPANESE("ja", "日本語"),
        KOREAN("ko", "한국어")
    }

    /**
     * Get the display name for a language code
     */
    fun getLanguageDisplayName(languageCode: String): String {
        return when (languageCode) {
            "system" -> "System Default"
            "en" -> "English"
            "zh-CN" -> "简体中文"
            "zh-TW" -> "繁體中文"
            "ja" -> "日本語"
            "ko" -> "한국어"
            else -> "English"
        }
    }

    /**
     * Get localized display name for a language code
     */
    fun getLocalizedLanguageDisplayName(context: Context, languageCode: String): String {
        return when (languageCode) {
            "system" -> context.getString(R.string.language_system)
            "en" -> context.getString(R.string.language_english)
            "zh-CN" -> context.getString(R.string.language_chinese_simplified)
            "zh-TW" -> context.getString(R.string.language_chinese_traditional)
            "ja" -> context.getString(R.string.language_japanese)
            "ko" -> context.getString(R.string.language_korean)
            else -> context.getString(R.string.language_english)
        }
    }

    /**
     * Convert language code to Locale
     */
    fun getLocaleFromLanguageCode(languageCode: String): Locale {
        return when (languageCode) {
            "system" -> Locale.getDefault()
            "en" -> Locale.ENGLISH
            "zh-CN" -> Locale.SIMPLIFIED_CHINESE
            "zh-TW" -> Locale.TRADITIONAL_CHINESE
            "ja" -> Locale.JAPANESE
            "ko" -> Locale.KOREAN
            else -> Locale.ENGLISH
        }
    }

    /**
     * Convert Locale to language code
     */
    fun getLanguageCodeFromLocale(locale: Locale): String {
        val language = locale.language
        val country = locale.country

        return when {
            language == "zh" && country == "CN" -> "zh-CN"
            language == "zh" && country == "TW" -> "zh-TW"
            language == "zh" && country == "HK" -> "zh-TW" // Use Traditional Chinese for Hong Kong
            language == "ja" -> "ja"
            language == "ko" -> "ko"
            language == "en" -> "en"
            else -> "en" // Default to English
        }
    }

    /**
     * Apply locale to context configuration
     */
    fun applyLocale(context: Context, languageCode: String): Context {
        val locale = if (languageCode == "system") {
            Locale.getDefault()
        } else {
            getLocaleFromLanguageCode(languageCode)
        }

        val configuration = Configuration(context.resources.configuration).apply {
            setLocale(locale)
            setLayoutDirection(locale)
        }

        return context.createConfigurationContext(configuration)
    }

    /**
     * Update app locale configuration
     */
    fun updateAppLocale(context: Context, languageCode: String) {
        val locale = if (languageCode == "system") {
            Locale.getDefault()
        } else {
            getLocaleFromLanguageCode(languageCode)
        }

        Locale.setDefault(locale)

        val configuration = Configuration().apply {
            setLocale(locale)
            setLayoutDirection(locale)
        }

        context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
    }

    /**
     * Get current system language code
     */
    fun getCurrentSystemLanguage(): String {
        return getLanguageCodeFromLocale(Locale.getDefault())
    }

    /**
     * Check if a language code is supported
     */
    fun isLanguageSupported(languageCode: String): Boolean {
        return SupportedLanguage.values().any { it.code == languageCode }
    }

    /**
     * Get all supported languages
     */
    fun getSupportedLanguages(): List<SupportedLanguage> {
        return SupportedLanguage.values().toList()
    }

    /**
     * Get supported language codes
     */
    fun getSupportedLanguageCodes(): List<String> {
        return SupportedLanguage.values().map { it.code }
    }

    /**
     * Detect best language from device settings
     */
    fun detectBestLanguage(): String {
        val systemLanguage = getCurrentSystemLanguage()
        return if (isLanguageSupported(systemLanguage)) {
            systemLanguage
        } else {
            "en" // Fallback to English
        }
    }

    /**
     * Format text direction for RTL languages
     * Currently all supported languages are LTR, but this is for future expansion
     */
    fun getTextDirection(languageCode: String): Int {
        return when (languageCode) {
            // All current supported languages are LTR
            else -> android.util.LayoutDirection.LTR
        }
    }

    /**
     * Get plurals resource for different languages
     * Used for handling different plural rules across languages
     */
    fun getPluralsQuantity(languageCode: String, count: Int): Int {
        val locale = getLocaleFromLanguageCode(languageCode)
        return when (locale.language) {
            "zh", "ja", "ko" -> {
                // Chinese, Japanese, Korean have simpler plural rules
                if (count == 0) 0 else 1 // Simple zero vs other
            }
            else -> {
                // English and other languages with standard plural rules
                when (count) {
                    0 -> 0 // Zero
                    1 -> 1 // One
                    else -> 2 // Other
                }
            }
        }
    }
}
