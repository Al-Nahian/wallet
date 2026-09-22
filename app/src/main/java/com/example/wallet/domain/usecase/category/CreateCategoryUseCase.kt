package com.example.wallet.domain.usecase.category

import com.example.wallet.core.common.newId
import com.example.wallet.domain.model.Category
import com.example.wallet.domain.repository.CategoryRepository
import javax.inject.Inject

/** plan.md §14/§15 — a user-created category within an existing group. Custom categories are
 * never `isSystem`, so they're always editable/deletable later. */
class CreateCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository,
) {
    suspend operator fun invoke(groupId: String, name: String): Result<Category> {
        val cleanName = name.trim()
        if (cleanName.isEmpty()) {
            return Result.failure(CategoryValidationException(CategoryError.NameRequired))
        }
        categoryRepository.getGroup(groupId)
            ?: return Result.failure(CategoryValidationException(CategoryError.GroupNotFound))

        val category = Category(
            id = newId(),
            groupId = groupId,
            name = cleanName,
            icon = null,
            sortOrder = Int.MAX_VALUE,
            isSystem = false,
        )
        categoryRepository.createCustomCategory(category)
        return Result.success(category)
    }
}
