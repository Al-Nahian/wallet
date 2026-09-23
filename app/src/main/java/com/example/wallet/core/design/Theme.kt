package com.example.wallet.core.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.wallet.core.design.glass.GlassMotionPreferences
import com.example.wallet.core.design.glass.LocalGlassMotionPreferences
import com.example.wallet.core.design.glass.rememberSystemPrefersReducedMotion

private val LightScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    secondary = LightSecondary,
    background = LightBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    error = LightError,
)

private val DarkScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    secondary = DarkSecondary,
    background = DarkBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    error = DarkError,
)

private val LightExtended = WalletExtendedColors(
    income = LightIncome,
    expense = LightExpense,
    transfer = LightTransfer,
    warning = LightWarning,
    success = LightSuccess,
    accent = LightAccent,
)

private val DarkExtended = WalletExtendedColors(
    income = DarkIncome,
    expense = DarkExpense,
    transfer = DarkTransfer,
    warning = DarkWarning,
    success = DarkSuccess,
    accent = DarkAccent,
)

val WalletTypography = Typography(
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 22.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp),
)

object WalletTheme {
    val extendedColors: WalletExtendedColors
        @Composable
        get() = LocalWalletExtendedColors.current
}

@Composable
fun WalletAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkScheme else LightScheme
    val extended = if (darkTheme) DarkExtended else LightExtended
    val motionPreferences = GlassMotionPreferences(reduceMotion = rememberSystemPrefersReducedMotion())

    CompositionLocalProvider(
        LocalWalletExtendedColors provides extended,
        LocalGlassMotionPreferences provides motionPreferences,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = WalletTypography,
            content = content,
        )
    }
}
