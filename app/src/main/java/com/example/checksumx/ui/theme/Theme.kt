package com.example.checksumx.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.example.checksumx.data.model.AppTheme

private val DarkColorScheme = darkColorScheme(
    primary = TechPrimaryDark,
    onPrimary = TechOnPrimaryDark,
    primaryContainer = TechPrimaryContainerDark,
    onPrimaryContainer = TechOnPrimaryContainerDark,
    secondary = TechSecondaryDark,
    onSecondary = TechOnSecondaryDark,
    secondaryContainer = TechSecondaryContainerDark,
    onSecondaryContainer = TechOnSecondaryContainerDark,
    tertiary = TechTertiaryDark,
    onTertiary = TechOnTertiaryDark,
    background = TechBackgroundDark,
    onBackground = TechOnBackgroundDark,
    surface = TechSurfaceDark,
    onSurface = TechOnSurfaceDark,
    surfaceVariant = TechSurfaceVariantDark,
    onSurfaceVariant = TechOnSurfaceVariantDark,
    outline = TechOutlineDark
)

private val LightColorScheme = lightColorScheme(
    primary = TechPrimaryLight,
    onPrimary = TechOnPrimaryLight,
    primaryContainer = TechPrimaryContainerLight,
    onPrimaryContainer = TechOnPrimaryContainerLight,
    secondary = TechSecondaryLight,
    onSecondary = TechOnSecondaryLight,
    secondaryContainer = TechSecondaryContainerLight,
    onSecondaryContainer = TechOnSecondaryContainerLight,
    tertiary = TechTertiaryLight,
    onTertiary = TechOnTertiaryLight,
    background = TechBackgroundLight,
    onBackground = TechOnBackgroundLight,
    surface = TechSurfaceLight,
    onSurface = TechOnSurfaceLight,
    surfaceVariant = TechSurfaceVariantLight,
    onSurfaceVariant = TechOnSurfaceVariantLight,
    outline = TechOutlineLight
)

@Composable
fun ChecksumXTheme(
    appTheme: AppTheme = AppTheme.SYSTEM,
    content: @Composable () -> Unit
) {
    val isDark = when (appTheme) {
        AppTheme.SYSTEM -> isSystemInDarkTheme()
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
    }

    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
