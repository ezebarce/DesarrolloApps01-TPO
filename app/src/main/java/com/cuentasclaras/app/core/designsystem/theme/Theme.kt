package com.cuentasclaras.app.core.designsystem.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

data class ExtendedColors(
    val tertiaryFixed: Color,
    val onTertiaryFixed: Color,
    val onSecondaryFixedVariant: Color,
    val warning: Color,
    val onWarningContainer: Color,
    val warningContainer: Color,
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        tertiaryFixed = Color.Unspecified,
        onTertiaryFixed = Color.Unspecified,
        onSecondaryFixedVariant = Color.Unspecified,
        warning = Color.Unspecified,
        onWarningContainer = Color.Unspecified,
        warningContainer = Color.Unspecified
    )
}

private val LightColorScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    surfaceTint = surfaceTintLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,
)

val lightExtendedColors = ExtendedColors(
    tertiaryFixed = tertiaryFixed,
    onTertiaryFixed = onTertiaryFixed,
    onSecondaryFixedVariant = onSecondaryFixedVariant,
    warning = warning,
    onWarningContainer = onWarningContainer,
    warningContainer = warningContainer
)

@Composable
fun CuentasClarasTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    CompositionLocalProvider(LocalExtendedColors provides lightExtendedColors) {
        MaterialTheme(
            colorScheme = LightColorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}

object CuentasClarasTheme {
    val extendedColors: ExtendedColors
        @Composable
        get() = LocalExtendedColors.current
}
