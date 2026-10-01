package com.expensetracker.wallet.domain.usecase.importexport

import com.expensetracker.wallet.core.common.newId
import com.expensetracker.wallet.domain.model.Category
import com.expensetracker.wallet.domain.model.CategoryGroup
import com.expensetracker.wallet.domain.repository.CategoryRepository
import java.util.Locale
import javax.inject.Inject

/** Commits the accepted, error-free, non-duplicate rows from [parseCategoryRows]. Groups are
 * never created here — [parseCategoryRows] already turned an unmatched group into a row error,
 * since this app has no "create group" feature anywhere else either. */
class ImportCategoriesUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository,
) {
    suspend operator fun invoke(rows: List<ParsedCategoryRow>, existingGroups: List<CategoryGroup>): Int {
        val committable = rows.filter { it.accepted && it.errors.isEmpty() }
        if (committable.isEmpty()) return 0

        val groupByKey = existingGroups.associateBy { it.name.trim().lowercase(Locale.US) to it.type }

        committable.forEach { row ->
            val group = groupByKey[row.groupName!!.trim().lowercase(Locale.US) to row.groupType!!] ?: return@forEach
            categoryRepository.createCustomCategory(
                Category(
                    id = newId(),
                    groupId = group.id,
                    name = row.categoryName!!.trim(),
                    icon = null,
                    sortOrder = Int.MAX_VALUE,
                    isSystem = false,
                ),
            )
        }
        return committable.size
    }
}
