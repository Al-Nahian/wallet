package com.example.wallet.domain.repository

import com.example.wallet.domain.model.Category
import com.example.wallet.domain.model.CategoryGroup
import kotlinx.coroutines.flow.Flow

/** Implemented in Phase 7. */
interface CategoryRepository {
    fun observeGroups(): Flow<List<CategoryGroup>>
    fun observeCategories(): Flow<List<Category>>
    fun observeCategoriesInGroup(groupId: String): Flow<List<Category>>
    suspend fun createCustomCategory(category: Category)
}
