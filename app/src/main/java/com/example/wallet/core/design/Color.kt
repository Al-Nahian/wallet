package com.example.wallet.core.design

import androidx.compose.ui.graphics.Color

// Light palette
val LightPrimary = Color(0xFF2E8B57)
val LightSecondary = Color(0xFF26A69A)
val LightBackground = Color(0xFFFFFFFF)
val LightSurface = Color(0xFFF5F5F5)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightOnSurface = Color(0xFF1B1B1B)
val LightIncome = Color(0xFF2E8B57)
val LightExpense = Color(0xFFF44336)
val LightTransfer = Color(0xFF42B5E8)
val LightWarning = Color(0xFFFF9F1C)
val LightError = Color(0xFFD32F2F)
val LightSuccess = Color(0xFF4CAF50)

// Dark palette
val DarkPrimary = Color(0xFF6FCF97)
val DarkSecondary = Color(0xFF4DB6AC)
val DarkBackground = Color(0xFF000000)
val DarkSurface = Color(0xFF0C0C0E)
val DarkOnPrimary = Color(0xFF00391C)
val DarkOnSurface = Color(0xFFE7E7E7)
val DarkIncome = Color(0xFF6FCF97)
val DarkExpense = Color(0xFFEF5350)
val DarkTransfer = Color(0xFF64B5F6)
val DarkWarning = Color(0xFFFFB74D)
val DarkError = Color(0xFFEF5350)
val DarkSuccess = Color(0xFF81C784)

/** Parses a category/label hex color string from the DB (e.g. "#F44336") — never a hardcoded
 * per-category color in a Composable (plan.md §69 rule 8). Falls back to gray for a malformed
 * string rather than crashing. */
fun parseHexColor(hex: String): Color = try {
    Color(android.graphics.Color.parseColor(hex))
} catch (e: IllegalArgumentException) {
    Color.Gray
}
