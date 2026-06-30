package com.apkarthik1986.jewelstockmanager.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = GoldPrimary,
    onPrimary = JewelOnAccent,
    primaryContainer = GoldContainer,
    onPrimaryContainer = GoldOnContainer,
    secondary = JewelAccent,
    onSecondary = JewelOnAccent,
    background = SurfaceLight,
    surface = SurfaceLight,
    onBackground = GoldOnContainer,
    onSurface = GoldOnContainer
)

private val DarkColorScheme = darkColorScheme(
    primary = JewelAccent,
    onPrimary = GoldOnContainer,
    primaryContainer = GoldPrimaryDark,
    onPrimaryContainer = GoldContainer,
    secondary = GoldContainer,
    onSecondary = GoldOnContainer,
    background = SurfaceDark,
    surface = SurfaceDark
)

@Composable
fun JewelStockManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = JewelTypography,
        content = content
    )
}
