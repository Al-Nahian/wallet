package com.expensetracker.wallet.domain.repository

import com.expensetracker.wallet.domain.model.Category
import com.expensetracker.wallet.domain.model.CategoryGroup
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun observeGroups(): Flow<List<CategoryGroup>>
    fun observeCategories(): Flow<List<Category>>
    fun observeCategoriesInGroup(groupId: String): Flow<List<Category>>
    suspend fun getGroup(groupId: String): CategoryGroup?
    suspend fun getCategory(id: String): Category?
    suspend fun createCustomCategory(category: Category)
    suspend fun updateCategory(category: Category)

    /** True if any non-deleted transaction or split still references [categoryId] — the
     * deletion policy is to block, never silently reassign/cascade (plan.md §15). */
    suspend fun isCategoryInUse(categoryId: String): Boolean
    suspend fun deleteCategory(categoryId: String)
}
