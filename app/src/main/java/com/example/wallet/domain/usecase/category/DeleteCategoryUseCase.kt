package com.example.wallet.domain.usecase.category

import com.example.wallet.domain.repository.CategoryRepository
import javax.inject.Inject

/** plan.md §15: deletion policy is block-if-in-use, never a silent reassign/cascade — a
 * category referenced by any non-deleted transaction or split can't be deleted (a
 * `transaction_splits.categoryId` cascade-delete would otherwise silently destroy split rows).
 * System (default) categories can never be deleted. */
class DeleteCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository,
) {
    suspend operator fun invoke(categoryId: String): Result<Unit> {
        val existing = categoryRepository.getCategory(categoryId)
            ?: return Result.failure(CategoryValidationException(CategoryError.CategoryNotFound))
        if (existing.isSystem) {
            return Result.failure(CategoryValidationException(CategoryError.SystemCategoryImmutable))
        }
        if (categoryRepository.isCategoryInUse(categoryId)) {
            return Result.failure(CategoryValidationException(CategoryError.CategoryInUse))
        }

        categoryRepository.deleteCategory(categoryId)
        return Result.success(Unit)
    }
}
