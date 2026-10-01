package com.expensetracker.wallet.core.design

import androidx.compose.ui.graphics.Color

/** Fixed rotation of accent colors for account tiles, shared between the Home dashboard's
 * accounts grid and the account detail screen's balance card so the same account always reads
 * the same color in both places. */
val AccountAccentPalette = listOf(
    Color(0xFF42B5E8), Color(0xFF9C6ADE), Color(0xFFFF9F1C), Color(0xFF26A69A),
    Color(0xFFEC407A), Color(0xFF7CB342),
)

/** The accent color for [accountId], matching its position among [activeAccountIds] — the same
 * ordering (`type, name`) [com.expensetracker.wallet.data.local.dao.AccountDao] returns, which is what
 * the dashboard's own accounts grid indexes into. Falls back to the palette's first color if the
 * account isn't found (e.g. it's archived and so absent from the active list). */
fun accountAccentColor(accountId: String, activeAccountIds: List<String>): Color {
    val index = activeAccountIds.indexOf(accountId)
    if (index < 0) return AccountAccentPalette.first()
    return AccountAccentPalette[index % AccountAccentPalette.size]
}
