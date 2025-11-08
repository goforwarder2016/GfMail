package com.gf.mail.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import com.gf.mail.utils.AccessibilityUtils
import com.gf.mail.ui.theme.Typography
import com.gf.mail.ui.theme.Shapes

/**
 * Accessibility-enhanced theme provider with WCAG 2.1 AA compliance
 */
@Composable
fun AccessibilityTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    highContrastMode: Boolean = false,
    colorBlindFriendly: Boolean = false,
    largeTextMode: Boolean = false,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    // Create color scheme based on accessibility needs
    val colorScheme = remember(darkTheme, highContrastMode, colorBlindFriendly, dynamicColor) {
        createAccessibilityColorScheme(
            darkTheme = darkTheme,
            highContrastMode = highContrastMode,
            colorBlindFriendly = colorBlindFriendly,
            dynamicColor = dynamicColor,
            context = context
        )
    }

    // Create typography based on accessibility needs
    val typography = remember(largeTextMode) {
        createAccessibilityTypography(largeTextMode)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = Shapes,
        content = content
    )
}

/**
 * Create accessibility-compliant color scheme
 */
private fun createAccessibilityColorScheme(
    darkTheme: Boolean,
    highContrastMode: Boolean,
    colorBlindFriendly: Boolean,
    dynamicColor: Boolean,
    context: android.content.Context
): ColorScheme {
    return when {
        highContrastMode -> createHighContrastColorScheme(darkTheme)
        colorBlindFriendly -> createColorBlindFriendlyColorScheme(darkTheme)
        dynamicColor && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }
}

/**
 * Create high contrast color scheme
 */
private fun createHighContrastColorScheme(darkTheme: Boolean): ColorScheme {
    return if (darkTheme) {
        darkColorScheme(
            primary = Color.White,
            onPrimary = Color.Black,
            primaryContainer = Color(0xFF333333),
            onPrimaryContainer = Color.White,
            secondary = Color(0xFFFFFF00), // High contrast yellow
            onSecondary = Color.Black,
            background = Color.Black,
            onBackground = Color.White,
            surface = Color.Black,
            onSurface = Color.White,
            surfaceVariant = Color(0xFF1A1A1A),
            onSurfaceVariant = Color.White,
            error = Color(0xFFFF6B6B), // High contrast red
            onError = Color.Black,
            outline = Color.White,
            outlineVariant = Color(0xFF666666)
        )
    } else {
        lightColorScheme(
            primary = Color.Black,
            onPrimary = Color.White,
            primaryContainer = Color(0xFFF0F0F0),
            onPrimaryContainer = Color.Black,
            secondary = Color(0xFF0066CC), // High contrast blue
            onSecondary = Color.White,
            background = Color.White,
            onBackground = Color.Black,
            surface = Color.White,
            onSurface = Color.Black,
            surfaceVariant = Color(0xFFF5F5F5),
            onSurfaceVariant = Color.Black,
            error = Color(0xFFCC0000), // High contrast red
            onError = Color.White,
            outline = Color.Black,
            outlineVariant = Color(0xFF666666)
        )
    }
}

/**
 * Create color blind friendly color scheme
 */
private fun createColorBlindFriendlyColorScheme(darkTheme: Boolean): ColorScheme {
    return if (darkTheme) {
        darkColorScheme(
            primary = Color(0xFF4A90E2), // Blue (safe for all color blindness types)
            onPrimary = Color.White,
            primaryContainer = Color(0xFF1A365D),
            onPrimaryContainer = Color(0xFFB3D9FF),
            secondary = Color(0xFFFF8C00), // Orange instead of red/green
            onSecondary = Color.Black,
            tertiary = Color(0xFF9B59B6), // Purple (distinguishable)
            onTertiary = Color.White,
            background = Color(0xFF121212),
            onBackground = Color.White,
            surface = Color(0xFF1E1E1E),
            onSurface = Color.White,
            error = Color(0xFFFF8C00), // Orange instead of red
            onError = Color.Black
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF0173B2), // Blue (safe for all color blindness types)
            onPrimary = Color.White,
            primaryContainer = Color(0xFFD4EDDA),
            onPrimaryContainer = Color(0xFF003A6B),
            secondary = Color(0xFFD55E00), // Orange instead of red/green
            onSecondary = Color.White,
            tertiary = Color(0xFF8E44AD), // Purple (distinguishable)
            onTertiary = Color.White,
            background = Color.White,
            onBackground = Color.Black,
            surface = Color.White,
            onSurface = Color.Black,
            error = Color(0xFFD55E00), // Orange instead of red
            onError = Color.White
        )
    }
}

/**
 * Create accessibility-enhanced typography
 */
private fun createAccessibilityTypography(largeTextMode: Boolean): Typography {
    val multiplier = if (largeTextMode) 1.25f else 1.0f

    return Typography(
        displayLarge = Typography().displayLarge.copy(
            fontSize = (57 * multiplier).sp,
            lineHeight = (64 * multiplier).sp
        ),
        displayMedium = Typography().displayMedium.copy(
            fontSize = (45 * multiplier).sp,
            lineHeight = (52 * multiplier).sp
        ),
        displaySmall = Typography().displaySmall.copy(
            fontSize = (36 * multiplier).sp,
            lineHeight = (44 * multiplier).sp
        ),
        headlineLarge = Typography().headlineLarge.copy(
            fontSize = (32 * multiplier).sp,
            lineHeight = (40 * multiplier).sp
        ),
        headlineMedium = Typography().headlineMedium.copy(
            fontSize = (28 * multiplier).sp,
            lineHeight = (36 * multiplier).sp
        ),
        headlineSmall = Typography().headlineSmall.copy(
            fontSize = (24 * multiplier).sp,
            lineHeight = (32 * multiplier).sp
        ),
        titleLarge = Typography().titleLarge.copy(
            fontSize = (22 * multiplier).sp,
            lineHeight = (28 * multiplier).sp
        ),
        titleMedium = Typography().titleMedium.copy(
            fontSize = (16 * multiplier).sp,
            lineHeight = (24 * multiplier).sp
        ),
        titleSmall = Typography().titleSmall.copy(
            fontSize = (14 * multiplier).sp,
            lineHeight = (20 * multiplier).sp
        ),
        bodyLarge = Typography().bodyLarge.copy(
            fontSize = (16 * multiplier).sp,
            lineHeight = (24 * multiplier).sp
        ),
        bodyMedium = Typography().bodyMedium.copy(
            fontSize = (14 * multiplier).sp,
            lineHeight = (20 * multiplier).sp
        ),
        bodySmall = Typography().bodySmall.copy(
            fontSize = (12 * multiplier).sp,
            lineHeight = (16 * multiplier).sp
        ),
        labelLarge = Typography().labelLarge.copy(
            fontSize = (14 * multiplier).sp,
            lineHeight = (20 * multiplier).sp
        ),
        labelMedium = Typography().labelMedium.copy(
            fontSize = (12 * multiplier).sp,
            lineHeight = (16 * multiplier).sp
        ),
        labelSmall = Typography().labelSmall.copy(
            fontSize = (11 * multiplier).sp,
            lineHeight = (16 * multiplier).sp
        )
    )
}

/**
 * Accessibility theme state holder
 */
@Stable
class AccessibilityThemeState(
    highContrastMode: Boolean = false,
    colorBlindFriendly: Boolean = false,
    largeTextMode: Boolean = false,
    reduceMotion: Boolean = false,
    focusIndicatorEnabled: Boolean = true
) {
    var highContrastMode by mutableStateOf(highContrastMode)
    var colorBlindFriendly by mutableStateOf(colorBlindFriendly)
    var largeTextMode by mutableStateOf(largeTextMode)
    var reduceMotion by mutableStateOf(reduceMotion)
    var focusIndicatorEnabled by mutableStateOf(focusIndicatorEnabled)
}

/**
 * Remember accessibility theme state
 */
@Composable
fun rememberAccessibilityThemeState(
    highContrastMode: Boolean = false,
    colorBlindFriendly: Boolean = false,
    largeTextMode: Boolean = false,
    reduceMotion: Boolean = false,
    focusIndicatorEnabled: Boolean = true
): AccessibilityThemeState {
    return remember {
        AccessibilityThemeState(
            highContrastMode = highContrastMode,
            colorBlindFriendly = colorBlindFriendly,
            largeTextMode = largeTextMode,
            reduceMotion = reduceMotion,
            focusIndicatorEnabled = focusIndicatorEnabled
        )
    }
}

/**
 * Local composition for accessibility theme state
 */
val LocalAccessibilityThemeState = compositionLocalOf { AccessibilityThemeState() }

/**
 * Provide accessibility theme state
 */
@Composable
fun ProvideAccessibilityThemeState(
    themeState: AccessibilityThemeState,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalAccessibilityThemeState provides themeState,
        content = content
    )
}

/**
 * Extension functions for color contrast validation
 */
fun ColorScheme.validateContrast(): List<ContrastValidationResult> {
    val results = mutableListOf<ContrastValidationResult>()

    // Validate primary colors
    results.add(
        ContrastValidationResult(
            colorPair = "Primary/OnPrimary",
            foreground = onPrimary,
            background = primary,
            ratio = AccessibilityUtils.calculateContrastRatio(onPrimary, primary),
            meetsAA = AccessibilityUtils.meetsWcagAA(onPrimary, primary)
        )
    )

    // Validate surface colors
    results.add(
        ContrastValidationResult(
            colorPair = "OnSurface/Surface",
            foreground = onSurface,
            background = surface,
            ratio = AccessibilityUtils.calculateContrastRatio(onSurface, surface),
            meetsAA = AccessibilityUtils.meetsWcagAA(onSurface, surface)
        )
    )

    // Validate background colors
    results.add(
        ContrastValidationResult(
            colorPair = "OnBackground/Background",
            foreground = onBackground,
            background = background,
            ratio = AccessibilityUtils.calculateContrastRatio(onBackground, background),
            meetsAA = AccessibilityUtils.meetsWcagAA(onBackground, background)
        )
    )

    return results
}

/**
 * Data class for contrast validation results
 */
data class ContrastValidationResult(
    val colorPair: String,
    val foreground: Color,
    val background: Color,
    val ratio: Double,
    val meetsAA: Boolean
)
