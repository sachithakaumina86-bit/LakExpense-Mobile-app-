package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Exact LakExpense Design System Tokens
 * Dark Theme: Emerald Vault (DESIGN_9d89096e.md & Stitch dark HTML specs)
 * Light Theme: Emerald Mint Light (DESIGN_590debd4.md & Stitch light HTML specs)
 */
@Immutable
data class LakColors(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val surfaceContainerLowest: Color,
    val surfaceContainerLow: Color,
    val surfaceContainer: Color,
    val surfaceContainerHigh: Color,
    val surfaceContainerHighest: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val outline: Color,
    val outlineVariant: Color,
    val cardBorder: Color,
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val tertiary: Color,
    val tertiaryContainer: Color,
    val mintAccent: Color,
    val income: Color,
    val incomeContainer: Color,
    val expense: Color,
    val expenseContainer: Color,
    val onExpenseContainer: Color,
    val transfer: Color,
    val transferContainer: Color,
    val warning: Color,
    val warningContainer: Color,
    val navBarBackground: Color,
    val headerBackground: Color,
    val chartGrid: Color,
    val chartBenchmark: Color
)

val EmeraldVaultDarkColors = LakColors(
    isDark = true,
    background = Color(0xFF0B1220),
    surface = Color(0xFF0C1321),
    surfaceContainerLowest = Color(0xFF070E1C),
    surfaceContainerLow = Color(0xFF151B2A),
    surfaceContainer = Color(0xFF19202E),
    surfaceContainerHigh = Color(0xFF232A39),
    surfaceContainerHighest = Color(0xFF2E3544),
    onSurface = Color(0xFFFFFFFF),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF86948A),
    outlineVariant = Color(0xFF3C4A42),
    cardBorder = Color(0x1F6EE7B7), // subtle emerald tint border rgba(110,231,183,0.12)
    primary = Color(0xFF4EDEA3),
    onPrimary = Color(0xFF003824),
    primaryContainer = Color(0xFF10B981),
    onPrimaryContainer = Color(0xFF003824),
    secondary = Color(0xFF62DCAD),
    secondaryContainer = Color(0xFF18A479),
    onSecondaryContainer = Color(0xFF003121),
    tertiary = Color(0xFF45DFA4),
    tertiaryContainer = Color(0xFF00B982),
    mintAccent = Color(0xFF6EE7B7),
    income = Color(0xFF34D399),
    incomeContainer = Color(0x1A34D399),
    expense = Color(0xFFF87171),
    expenseContainer = Color(0xFF93000A),
    onExpenseContainer = Color(0xFFFFDAD6),
    transfer = Color(0xFF38BDF8),
    transferContainer = Color(0x1F38BDF8),
    warning = Color(0xFFFBBF24),
    warningContainer = Color(0xFF231A0E),
    navBarBackground = Color(0xF2070E1C),
    headerBackground = Color(0xEB0C1321),
    chartGrid = Color(0xFF2E3544),
    chartBenchmark = Color(0xFF86948A)
)

val EmeraldMintLightColors = LakColors(
    isDark = false,
    background = Color(0xFFF8FAFC),
    surface = Color(0xFFF8FAFC),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF1F5F9),
    surfaceContainer = Color(0xFFFFFFFF),
    surfaceContainerHigh = Color(0xFFE2E8F0),
    surfaceContainerHighest = Color(0xFFDAE2FD),
    onSurface = Color(0xFF0F172A),
    onSurfaceVariant = Color(0xFF64748B),
    outline = Color(0xFF6C7A71),
    outlineVariant = Color(0xFFE2E8F0),
    cardBorder = Color(0xFFE2E8F0),
    primary = Color(0xFF059669),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF10B981),
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFF006C49),
    secondaryContainer = Color(0xFFECFDF5),
    onSecondaryContainer = Color(0xFF059669),
    tertiary = Color(0xFF34D399),
    tertiaryContainer = Color(0xFFD1FAE5),
    mintAccent = Color(0xFF10B981),
    income = Color(0xFF059669),
    incomeContainer = Color(0xFFECFDF5),
    expense = Color(0xFFDC2626),
    expenseContainer = Color(0xFFFEF2F2),
    onExpenseContainer = Color(0xFFDC2626),
    transfer = Color(0xFF2563EB),
    transferContainer = Color(0xFFEFF6FF),
    warning = Color(0xFFD97706),
    warningContainer = Color(0xFFFFFBEB),
    navBarBackground = Color(0xFAFFFFFF),
    headerBackground = Color(0xEBFFFFFF),
    chartGrid = Color(0xFFE2E8F0),
    chartBenchmark = Color(0xFFCBD5E1)
)

val LocalLakColors = staticCompositionLocalOf { EmeraldVaultDarkColors }
