package com.example.wallet.domain.usecase.category

import com.example.wallet.domain.model.Category
import com.example.wallet.domain.repository.CategoryRepository
import javax.inject.Inject

/** plan.md §15/§69 rule — system (default) categories can never be renamed. */
class UpdateCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository,
) {
    suspend operator fun invoke(categoryId: String, name: String): Result<Category> {
        val existing = categoryRepository.getCategory(categoryId)
            ?: return Result.failure(CategoryValidationException(CategoryError.CategoryNotFound))
        if (existing.isSystem) {
            return Result.failure(CategoryValidationException(CategoryError.SystemCategoryImmutable))
        }
        val cleanName = name.trim()
        if (cleanName.isEmpty()) {
            return Result.failure(CategoryValidationException(CategoryError.NameRequired))
        }

        val updated = existing.copy(name = cleanName)
        categoryRepository.updateCategory(updated)
        return Result.success(updated)
    }
}
