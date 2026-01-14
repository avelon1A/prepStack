package com.prepstack.techinterviewprep.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlueLight,
    onPrimary = TextPrimary,
    primaryContainer = PrimaryBlueDark,
    onPrimaryContainer = SurfaceLight,
    
    secondary = SecondaryPurpleLight,
    onSecondary = TextPrimary,
    secondaryContainer = SecondaryPurpleDark,
    onSecondaryContainer = SurfaceLight,
    
    tertiary = AccentGreenLight,
    onTertiary = TextPrimary,
    
    background = BackgroundDark,
    onBackground = SurfaceLight,
    surface = SurfaceDark,
    onSurface = SurfaceLight,
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFB0B0B0),
    
    error = ErrorRedLight,
    onError = SurfaceLight
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = SurfaceLight,
    primaryContainer = Color(0xFFE3F2FD),
    onPrimaryContainer = PrimaryBlueDark,
    
    secondary = SecondaryPurple,
    onSecondary = SurfaceLight,
    secondaryContainer = Color(0xFFF3E5F5),
    onSecondaryContainer = SecondaryPurpleDark,
    
    tertiary = AccentGreen,
    onTertiary = SurfaceLight,
    tertiaryContainer = Color(0xFFE8F5E9),
    onTertiaryContainer = Color(0xFF2E7D32),
    
    background = BackgroundLight,
    onBackground = TextPrimary,
    surface = SurfaceLight,
    onSurface = TextPrimary,
    surfaceVariant = CardTinted,
    onSurfaceVariant = TextSecondary,
    
    error = ErrorRed,
    onError = SurfaceLight,
    errorContainer = Color(0xFFFFEBEE),
    onErrorContainer = Color(0xFFC62828),
    
    outline = Color(0xFFE0E0E0),
    outlineVariant = Color(0xFFF5F5F5)
)

@Composable
fun TechInterviewPrepTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color disabled for consistent branding
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}