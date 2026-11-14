package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.example.data.model.ThemeMode

private val LakShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

object LakTheme {
    val colors: LakColors
        @Composable
        @ReadOnlyComposable
        get() = LocalLakColors.current
}

@Composable
fun LakExpenseTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val useDarkTheme = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val lakColors = if (useDarkTheme) EmeraldVaultDarkColors else EmeraldMintLightColors

    val materialColorScheme = if (useDarkTheme) {
        darkColorScheme(
            primary = lakColors.primary,
            onPrimary = lakColors.onPrimary,
            primaryContainer = lakColors.primaryContainer,
            onPrimaryContainer = lakColors.onPrimaryContainer,
            secondary = lakColors.secondary,
            onSecondary = lakColors.onPrimary,
            secondaryContainer = lakColors.secondaryContainer,
            onSecondaryContainer = lakColors.onSecondaryContainer,
            tertiary = lakColors.tertiary,
            onTertiary = lakColors.onPrimary,
            tertiaryContainer = lakColors.tertiaryContainer,
            background = lakColors.background,
            onBackground = lakColors.onSurface,
            surface = lakColors.surface,
            onSurface = lakColors.onSurface,
            surfaceVariant = lakColors.surfaceContainerHigh,
            onSurfaceVariant = lakColors.onSurfaceVariant,
            surfaceContainerLowest = lakColors.surfaceContainerLowest,
            surfaceContainerLow = lakColors.surfaceContainerLow,
            surfaceContainer = lakColors.surfaceContainer,
            surfaceContainerHigh = lakColors.surfaceContainerHigh,
            surfaceContainerHighest = lakColors.surfaceContainerHighest,
            outline = lakColors.outline,
            outlineVariant = lakColors.outlineVariant,
            error = lakColors.expense,
            onError = ColorTokens.OnErrorDark,
            errorContainer = lakColors.expenseContainer,
            onErrorContainer = lakColors.onExpenseContainer
        )
    } else {
        lightColorScheme(
            primary = lakColors.primary,
            onPrimary = lakColors.onPrimary,
            primaryContainer = lakColors.primaryContainer,
            onPrimaryContainer = lakColors.onPrimaryContainer,
            secondary = lakColors.secondary,
            onSecondary = lakColors.onPrimary,
            secondaryContainer = lakColors.secondaryContainer,
            onSecondaryContainer = lakColors.onSecondaryContainer,
            tertiary = lakColors.tertiary,
            onTertiary = lakColors.onPrimary,
            tertiaryContainer = lakColors.tertiaryContainer,
            background = lakColors.background,
            onBackground = lakColors.onSurface,
            surface = lakColors.surface,
            onSurface = lakColors.onSurface,
            surfaceVariant = lakColors.surfaceContainerLow,
            onSurfaceVariant = lakColors.onSurfaceVariant,
            surfaceContainerLowest = lakColors.surfaceContainerLowest,
            surfaceContainerLow = lakColors.surfaceContainerLow,
            surfaceContainer = lakColors.surfaceContainer,
            surfaceContainerHigh = lakColors.surfaceContainerHigh,
            surfaceContainerHighest = lakColors.surfaceContainerHighest,
            outline = lakColors.outline,
            outlineVariant = lakColors.outlineVariant,
            error = lakColors.expense,
            onError = ColorTokens.OnErrorLight,
            errorContainer = lakColors.expenseContainer,
            onErrorContainer = lakColors.onExpenseContainer
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !useDarkTheme
                isAppearanceLightNavigationBars = !useDarkTheme
            }
        }
    }

    CompositionLocalProvider(LocalLakColors provides lakColors) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography = LakTypography,
            shapes = LakShapes,
            content = content
        )
    }
}

private object ColorTokens {
    val OnErrorDark = androidx.compose.ui.graphics.Color(0xFF690005)
    val OnErrorLight = androidx.compose.ui.graphics.Color(0xFFFFFFFF)
}
