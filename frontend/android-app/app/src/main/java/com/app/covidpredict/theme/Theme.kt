package com.app.covidpredict.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val ColorScheme = lightColorScheme(
    primary = MedicalBlue,
    secondary = NatureGreen,
    tertiary = SoftRed,
    surface = SurfaceLow,
    onSurface = OnSurface,
    surfaceContainerLow = SurfaceLow,
    surfaceContainerHighest = SurfaceHighest,
    secondaryContainer = SecondaryContainer,
    tertiaryContainer = TertiaryContainer
)

@Composable
fun CovidPredictTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ColorScheme,
        typography = Typography,
        content = content
    )
}