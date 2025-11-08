package com.gf.mail.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.gf.mail.ui.theme.Shapes
import com.gf.mail.ui.theme.Typography

/**
 * Optimized theme system with enhanced accessibility and performance
 */

/**
 * Optimized theme provider that combines Material Design 3 with accessibility features
 */
@Composable
fun OptimizedGfmailTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    highContrastMode: Boolean = false,
    largeTextMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    // Create optimized color scheme
    val colorScheme = remember(darkTheme, dynamicColor, highContrastMode) {
        createOptimizedColorScheme(
            darkTheme = darkTheme,
            dynamicColor = dynamicColor,
            highContrastMode = highContrastMode,
            context = context
        )
    }

    // Create optimized typography
    val typography = remember(largeTextMode) {
        createOptimizedTypography(largeTextMode)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = Shapes,
        content = content
    )
}

/**
 * Create optimized color scheme with accessibility considerations
 */
private fun createOptimizedColorScheme(
    darkTheme: Boolean,
    dynamicColor: Boolean,
    highContrastMode: Boolean,
    context: android.content.Context
): ColorScheme {
    return when {
        highContrastMode -> createHighContrastColorScheme(darkTheme)
        dynamicColor && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> createOptimizedDarkColorScheme()
        else -> createOptimizedLightColorScheme()
    }
}

/**
 * Create high contrast color scheme for accessibility
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
 * Create optimized light color scheme
 */
private fun createOptimizedLightColorScheme(): ColorScheme {
    return lightColorScheme(
        primary = Color(0xFF1976D2),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFD1E4FF),
        onPrimaryContainer = Color(0xFF001D36),
        secondary = Color(0xFF545F70),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFD8E3F8),
        onSecondaryContainer = Color(0xFF111C2B),
        tertiary = Color(0xFF6F5B92),
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFFF6DFFF),
        onTertiaryContainer = Color(0xFF29132E),
        error = Color(0xFFBA1A1A),
        onError = Color.White,
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF410002),
        background = Color(0xFFFEFBFF),
        onBackground = Color(0xFF1C1B1F),
        surface = Color(0xFFFEFBFF),
        onSurface = Color(0xFF1C1B1F),
        surfaceVariant = Color(0xFFE7E0EC),
        onSurfaceVariant = Color(0xFF49454F),
        outline = Color(0xFF79747E),
        outlineVariant = Color(0xFFCAC4D0)
    )
}

/**
 * Create optimized dark color scheme
 */
private fun createOptimizedDarkColorScheme(): ColorScheme {
    return darkColorScheme(
        primary = Color(0xFF9ECAFF),
        onPrimary = Color(0xFF003258),
        primaryContainer = Color(0xFF004881),
        onPrimaryContainer = Color(0xFFD1E4FF),
        secondary = Color(0xFFBCC7DB),
        onSecondary = Color(0xFF263140),
        secondaryContainer = Color(0xFF3C4758),
        onSecondaryContainer = Color(0xFFD8E3F8),
        tertiary = Color(0xFFD9C2E8),
        onTertiary = Color(0xFF3F2854),
        tertiaryContainer = Color(0xFF563E6C),
        onTertiaryContainer = Color(0xFFF6DFFF),
        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6),
        background = Color(0xFF1C1B1F),
        onBackground = Color(0xFFE6E1E5),
        surface = Color(0xFF1C1B1F),
        onSurface = Color(0xFFE6E1E5),
        surfaceVariant = Color(0xFF49454F),
        onSurfaceVariant = Color(0xFFCAC4D0),
        outline = Color(0xFF938F99),
        outlineVariant = Color(0xFF49454F)
    )
}

/**
 * Create optimized typography with accessibility support
 */
private fun createOptimizedTypography(largeTextMode: Boolean): Typography {
    val scaleFactor = if (largeTextMode) 1.2f else 1.0f
    
    return Typography(
        displayLarge = Typography.displayLarge.copy(
            fontSize = Typography.displayLarge.fontSize * scaleFactor
        ),
        displayMedium = Typography.displayMedium.copy(
            fontSize = Typography.displayMedium.fontSize * scaleFactor
        ),
        displaySmall = Typography.displaySmall.copy(
            fontSize = Typography.displaySmall.fontSize * scaleFactor
        ),
        headlineLarge = Typography.headlineLarge.copy(
            fontSize = Typography.headlineLarge.fontSize * scaleFactor
        ),
        headlineMedium = Typography.headlineMedium.copy(
            fontSize = Typography.headlineMedium.fontSize * scaleFactor
        ),
        headlineSmall = Typography.headlineSmall.copy(
            fontSize = Typography.headlineSmall.fontSize * scaleFactor
        ),
        titleLarge = Typography.titleLarge.copy(
            fontSize = Typography.titleLarge.fontSize * scaleFactor
        ),
        titleMedium = Typography.titleMedium.copy(
            fontSize = Typography.titleMedium.fontSize * scaleFactor
        ),
        titleSmall = Typography.titleSmall.copy(
            fontSize = Typography.titleSmall.fontSize * scaleFactor
        ),
        bodyLarge = Typography.bodyLarge.copy(
            fontSize = Typography.bodyLarge.fontSize * scaleFactor
        ),
        bodyMedium = Typography.bodyMedium.copy(
            fontSize = Typography.bodyMedium.fontSize * scaleFactor
        ),
        bodySmall = Typography.bodySmall.copy(
            fontSize = Typography.bodySmall.fontSize * scaleFactor
        ),
        labelLarge = Typography.labelLarge.copy(
            fontSize = Typography.labelLarge.fontSize * scaleFactor
        ),
        labelMedium = Typography.labelMedium.copy(
            fontSize = Typography.labelMedium.fontSize * scaleFactor
        ),
        labelSmall = Typography.labelSmall.copy(
            fontSize = Typography.labelSmall.fontSize * scaleFactor
        )
    )
}

/**
 * Theme state for managing theme preferences
 */
@Stable
data class OptimizedThemeState(
    val isDarkTheme: Boolean = false,
    val dynamicColor: Boolean = true,
    val highContrastMode: Boolean = false,
    val largeTextMode: Boolean = false
)

/**
 * Theme state holder
 */
@Composable
fun rememberOptimizedThemeState(): MutableState<OptimizedThemeState> {
    return remember { mutableStateOf(OptimizedThemeState()) }
}