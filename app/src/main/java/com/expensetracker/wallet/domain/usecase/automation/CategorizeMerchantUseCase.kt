package com.expensetracker.wallet.domain.usecase.automation

import com.expensetracker.wallet.domain.repository.CategoryRepository
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/** plan.md §28 — the deterministic keyword rules explicitly called out as the starting point
 * ("uber/pathao -> Transportation, foodpanda -> Food & Drinks, netflix -> Streaming"), matched
 * against this app's own seeded category names (never hardcoded ids) via the same
 * punctuation/case-insensitive normalization `CategoryMatcher` uses for CSV import (Phase 12),
 * so a category rename doesn't silently break this. Merchant learning (persisted per-merchant
 * corrections) is a documented follow-up, not implemented in this pass — see
 * plans/14-sms-notification-automation.md's Implementation notes. */
private val KEYWORDS_TO_CATEGORY_KEY = listOf(
    listOf("uber", "pathao", "indrive", "obhai") to "publictransport",
    listOf("foodpanda", "food panda") to "restaurantfastfood",
    listOf("netflix", "spotify", "hoichoi", "chorki", "youtube premium") to "tvstreaming",
)

private fun normalizeKey(raw: String): String = raw.lowercase(Locale.US).filter { it.isLetterOrDigit() }

class CategorizeMerchantUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository,
) {
    suspend operator fun invoke(merchant: String?): String? {
        if (merchant.isNullOrBlank()) return null
        val lowerMerchant = merchant.lowercase(Locale.US)
        val targetKey = KEYWORDS_TO_CATEGORY_KEY
            .firstOrNull { (keywords, _) -> keywords.any { it in lowerMerchant } }
            ?.second
            ?: return null

        return categoryRepository.observeCategories().first()
            .firstOrNull { normalizeKey(it.name) == targetKey }
            ?.id
    }
}
