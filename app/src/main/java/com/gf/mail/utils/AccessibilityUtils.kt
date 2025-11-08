package com.gf.mail.utils

import android.content.Context
import android.view.accessibility.AccessibilityManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.pow

/**
 * Utility class for managing accessibility features and WCAG 2.1 AA compliance
 */
object AccessibilityUtils {

    /**
     * Accessibility preference keys
     */
    object AccessibilityPrefs {
        const val HIGH_CONTRAST_MODE = "accessibility_high_contrast_mode"
        const val LARGE_TEXT_MODE = "accessibility_large_text_mode"
        const val REDUCE_MOTION = "accessibility_reduce_motion"
        const val SCREEN_READER_ENABLED = "accessibility_screen_reader_enabled"
        const val KEYBOARD_NAVIGATION = "accessibility_keyboard_navigation"
        const val COLOR_BLIND_FRIENDLY = "accessibility_color_blind_friendly"
        const val FOCUS_INDICATOR = "accessibility_focus_indicator"
        const val TOUCH_TARGET_SIZE = "accessibility_touch_target_size"
    }

    /**
     * Minimum touch target size for accessibility compliance (48dp as per Material Design)
     */
    const val MIN_TOUCH_TARGET_SIZE_DP = 48

    /**
     * High contrast color ratios for WCAG AA compliance
     */
    const val WCAG_AA_NORMAL_RATIO = 4.5
    const val WCAG_AA_LARGE_RATIO = 3.0
    const val WCAG_AAA_NORMAL_RATIO = 7.0

    /**
     * Check if accessibility services are enabled
     */
    fun isAccessibilityEnabled(context: Context): Boolean {
        val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        return accessibilityManager.isEnabled
    }

    /**
     * Check if TalkBack or other screen readers are enabled
     */
    fun isScreenReaderEnabled(context: Context): Boolean {
        val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        return accessibilityManager.isTouchExplorationEnabled
    }

    /**
     * Calculate color contrast ratio
     */
    fun calculateContrastRatio(foreground: Color, background: Color): Double {
        val luminance1 = getRelativeLuminance(foreground) + 0.05
        val luminance2 = getRelativeLuminance(background) + 0.05
        return maxOf(luminance1, luminance2) / minOf(luminance1, luminance2)
    }

    /**
     * Check if color combination meets WCAG AA standards
     */
    fun meetsWcagAA(foreground: Color, background: Color, isLargeText: Boolean = false): Boolean {
        val ratio = calculateContrastRatio(foreground, background)
        return if (isLargeText) ratio >= WCAG_AA_LARGE_RATIO else ratio >= WCAG_AA_NORMAL_RATIO
    }

    /**
     * Get relative luminance of a color
     */
    private fun getRelativeLuminance(color: Color): Double {
        fun adjustGamma(value: Float): Double {
            return if (value <= 0.03928) {
                value / 12.92
            } else {
                ((value + 0.055) / 1.055).toDouble().pow(2.4)
            }
        }

        val r = adjustGamma(color.red)
        val g = adjustGamma(color.green)
        val b = adjustGamma(color.blue)

        return 0.2126 * r + 0.7152 * g + 0.0722 * b
    }

    /**
     * Get accessible colors for high contrast mode
     */
    fun getHighContrastColors(): AccessibilityColors {
        return AccessibilityColors(
            primary = Color(0xFF0066CC), // High contrast blue
            onPrimary = Color.White,
            background = Color.White,
            onBackground = Color.Black,
            surface = Color.White,
            onSurface = Color.Black,
            error = Color(0xFFCC0000), // High contrast red
            onError = Color.White,
            outline = Color.Black,
            surfaceVariant = Color(0xFFF5F5F5),
            onSurfaceVariant = Color.Black
        )
    }

    /**
     * Get color-blind friendly colors
     */
    fun getColorBlindFriendlyColors(): AccessibilityColors {
        return AccessibilityColors(
            primary = Color(0xFF0173B2), // Blue (safe for all color blindness types)
            onPrimary = Color.White,
            background = Color.White,
            onBackground = Color.Black,
            surface = Color.White,
            onSurface = Color.Black,
            error = Color(0xFFD55E00), // Orange instead of red
            onError = Color.White,
            outline = Color(0xFF56B4E9), // Light blue outline
            surfaceVariant = Color(0xFFF0F0F0),
            onSurfaceVariant = Color.Black
        )
    }

    /**
     * Get accessible font sizes
     */
    fun getAccessibleFontSizes(isLargeText: Boolean): AccessibilityFontSizes {
        val multiplier = if (isLargeText) 1.3f else 1.0f
        return AccessibilityFontSizes(
            displayLarge = (57 * multiplier).sp,
            displayMedium = (45 * multiplier).sp,
            displaySmall = (36 * multiplier).sp,
            headlineLarge = (32 * multiplier).sp,
            headlineMedium = (28 * multiplier).sp,
            headlineSmall = (24 * multiplier).sp,
            titleLarge = (22 * multiplier).sp,
            titleMedium = (16 * multiplier).sp,
            titleSmall = (14 * multiplier).sp,
            bodyLarge = (16 * multiplier).sp,
            bodyMedium = (14 * multiplier).sp,
            bodySmall = (12 * multiplier).sp,
            labelLarge = (14 * multiplier).sp,
            labelMedium = (12 * multiplier).sp,
            labelSmall = (11 * multiplier).sp
        )
    }

    /**
     * Get accessible touch target size
     */
    fun getAccessibleTouchTargetSize(): Dp {
        return MIN_TOUCH_TARGET_SIZE_DP.dp
    }

    /**
     * Create accessibility semantics for email items
     */
    fun createEmailItemSemantics(
        from: String,
        subject: String,
        preview: String,
        isRead: Boolean,
        isStarred: Boolean,
        hasAttachment: Boolean,
        timestamp: String
    ): SemanticsPropertyReceiver.() -> Unit = {
        contentDescription = buildString {
            append("Email from $from. ")
            append("Subject: $subject. ")
            if (preview.isNotEmpty()) {
                append("Preview: $preview. ")
            }
            if (hasAttachment) {
                append("Has attachment. ")
            }
            if (isStarred) {
                append("Starred. ")
            }
            append("${if (isRead) "Read" else "Unread"}. ")
            append("Received $timestamp")
        }

        stateDescription = buildString {
            if (!isRead) append("Unread ")
            if (isStarred) append("Starred ")
            if (hasAttachment) append("Has attachment ")
        }.trim()

        role = Role.Button
    }

    /**
     * Create accessibility semantics for buttons with actions
     */
    fun createButtonSemantics(
        label: String,
        action: String? = null,
        enabled: Boolean = true
    ): SemanticsPropertyReceiver.() -> Unit = {
        contentDescription = if (action != null) "$label, $action" else label
        role = Role.Button
        if (!enabled) {
            disabled()
        }
    }

    /**
     * Create accessibility semantics for form fields
     */
    fun createTextFieldSemantics(
        label: String,
        value: String,
        isRequired: Boolean = false,
        error: String? = null
    ): SemanticsPropertyReceiver.() -> Unit = {
        contentDescription = buildString {
            append(label)
            if (isRequired) append(", required")
            if (error != null) append(", error: $error")
        }

        if (error != null) {
            error(error)
        }

        role = Role.DropdownList
        if (value.isNotEmpty()) {
            text = androidx.compose.ui.text.AnnotatedString(value)
        }
    }

    /**
     * Create accessibility semantics for progress indicators
     */
    fun createProgressSemantics(
        current: Int,
        total: Int,
        description: String
    ): SemanticsPropertyReceiver.() -> Unit = {
        contentDescription = "$description, $current of $total"
        progressBarRangeInfo = ProgressBarRangeInfo(current.toFloat(), 0f..total.toFloat())
    }

    /**
     * Create accessibility semantics for navigation elements
     */
    fun createNavigationSemantics(
        label: String,
        unreadCount: Int = 0
    ): SemanticsPropertyReceiver.() -> Unit = {
        contentDescription = if (unreadCount > 0) {
            "$label, $unreadCount unread"
        } else {
            label
        }
        role = Role.Tab
    }

    /**
     * Announce accessibility events
     */
    fun announceForAccessibility(context: Context, message: String) {
        val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        if (accessibilityManager.isEnabled) {
            // This would typically use AccessibilityEvent.TYPE_ANNOUNCEMENT
            // For Compose, we handle this through semantics
        }
    }
}

/**
 * Data class for accessibility color scheme
 */
data class AccessibilityColors(
    val primary: Color,
    val onPrimary: Color,
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val error: Color,
    val onError: Color,
    val outline: Color,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color
)

/**
 * Data class for accessibility font sizes
 */
data class AccessibilityFontSizes(
    val displayLarge: TextUnit,
    val displayMedium: TextUnit,
    val displaySmall: TextUnit,
    val headlineLarge: TextUnit,
    val headlineMedium: TextUnit,
    val headlineSmall: TextUnit,
    val titleLarge: TextUnit,
    val titleMedium: TextUnit,
    val titleSmall: TextUnit,
    val bodyLarge: TextUnit,
    val bodyMedium: TextUnit,
    val bodySmall: TextUnit,
    val labelLarge: TextUnit,
    val labelMedium: TextUnit,
    val labelSmall: TextUnit
)

/**
 * Composable function to create accessible touch target
 */
@Composable
fun AccessibleTouchTarget(
    modifier: Modifier = Modifier,
    minSize: Dp = AccessibilityUtils.getAccessibleTouchTargetSize(),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.sizeIn(minWidth = minSize, minHeight = minSize),
        content = content
    )
}

/**
 * Composable function for accessible focus indicator
 */
@Composable
fun AccessibleFocusIndicator(
    focused: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .then(
                if (focused) {
                    Modifier
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(2.dp)
                } else {
                    Modifier
                }
            )
    ) {
        content()
    }
}

/**
 * Composable function for high contrast mode support
 */
@Composable
fun rememberAccessibilityColors(
    highContrastMode: Boolean,
    colorBlindFriendly: Boolean
): ColorScheme {
    val currentColorScheme = MaterialTheme.colorScheme
    return remember(highContrastMode, colorBlindFriendly, currentColorScheme) {
        when {
            highContrastMode -> {
                val colors = AccessibilityUtils.getHighContrastColors()
                lightColorScheme(
                    primary = colors.primary,
                    onPrimary = colors.onPrimary,
                    background = colors.background,
                    onBackground = colors.onBackground,
                    surface = colors.surface,
                    onSurface = colors.onSurface,
                    error = colors.error,
                    onError = colors.onError,
                    outline = colors.outline,
                    surfaceVariant = colors.surfaceVariant,
                    onSurfaceVariant = colors.onSurfaceVariant
                )
            }
            colorBlindFriendly -> {
                val colors = AccessibilityUtils.getColorBlindFriendlyColors()
                lightColorScheme(
                    primary = colors.primary,
                    onPrimary = colors.onPrimary,
                    background = colors.background,
                    onBackground = colors.onBackground,
                    surface = colors.surface,
                    onSurface = colors.onSurface,
                    error = colors.error,
                    onError = colors.onError,
                    outline = colors.outline,
                    surfaceVariant = colors.surfaceVariant,
                    onSurfaceVariant = colors.onSurfaceVariant
                )
            }
            else -> currentColorScheme
        }
    }
}

/**
 * Composable function for accessible typography
 */
@Composable
fun rememberAccessibilityTypography(largeText: Boolean): Typography {
    val density = LocalDensity.current
    val currentTypography = MaterialTheme.typography

    return remember(largeText, density, currentTypography) {
        if (largeText) {
            val sizes = AccessibilityUtils.getAccessibleFontSizes(true)
            Typography(
                displayLarge = currentTypography.displayLarge.copy(fontSize = sizes.displayLarge),
                displayMedium = currentTypography.displayMedium.copy(fontSize = sizes.displayMedium),
                displaySmall = currentTypography.displaySmall.copy(fontSize = sizes.displaySmall),
                headlineLarge = currentTypography.headlineLarge.copy(fontSize = sizes.headlineLarge),
                headlineMedium = currentTypography.headlineMedium.copy(
                    fontSize = sizes.headlineMedium
                ),
                headlineSmall = currentTypography.headlineSmall.copy(fontSize = sizes.headlineSmall),
                titleLarge = currentTypography.titleLarge.copy(fontSize = sizes.titleLarge),
                titleMedium = currentTypography.titleMedium.copy(fontSize = sizes.titleMedium),
                titleSmall = currentTypography.titleSmall.copy(fontSize = sizes.titleSmall),
                bodyLarge = currentTypography.bodyLarge.copy(fontSize = sizes.bodyLarge),
                bodyMedium = currentTypography.bodyMedium.copy(fontSize = sizes.bodyMedium),
                bodySmall = currentTypography.bodySmall.copy(fontSize = sizes.bodySmall),
                labelLarge = currentTypography.labelLarge.copy(fontSize = sizes.labelLarge),
                labelMedium = currentTypography.labelMedium.copy(fontSize = sizes.labelMedium),
                labelSmall = currentTypography.labelSmall.copy(fontSize = sizes.labelSmall)
            )
        } else {
            currentTypography
        }
    }
}
