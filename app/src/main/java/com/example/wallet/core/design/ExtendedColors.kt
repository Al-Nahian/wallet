package com.example.wallet.core.design

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Semantic tokens that Material3's ColorScheme has no slot for
 * (income / expense / transfer), per plan.md §7.
 */
data class WalletExtendedColors(
    val income: Color,
    val expense: Color,
    val transfer: Color,
    val warning: Color,
    val success: Color,
    val accent: Color,
)

val LocalWalletExtendedColors = staticCompositionLocalOf {
    WalletExtendedColors(
        income = LightIncome,
        expense = LightExpense,
        transfer = LightTransfer,
        warning = LightWarning,
        success = LightSuccess,
        accent = LightAccent,
    )
}
