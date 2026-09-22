package com.example.wallet.domain.usecase.category

import com.example.wallet.domain.model.Category
import com.example.wallet.domain.model.CategoryGroup
import com.example.wallet.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** In-memory fake so use-case tests don't need Room/Android. [categoriesInUse] lets a test mark
 * a category as referenced by a transaction/split, mirroring the real DAO-backed check. */
class FakeCategoryRepository : CategoryRepository {
    private val groups = MutableStateFlow<Map<String, CategoryGroup>>(emptyMap())
    private val categories = MutableStateFlow<Map<String, Category>>(emptyMap())
    val categoriesInUse = mutableSetOf<String>()

    fun addGroup(group: CategoryGroup) {
        groups.value = groups.value + (group.id to group)
    }

    fun addCategory(category: Category) {
        categories.value = categories.value + (category.id to category)
    }

    override fun observeGroups(): Flow<List<CategoryGroup>> = groups.map { it.values.toList() }

    override fun observeCategories(): Flow<List<Category>> = categories.map { it.values.toList() }

    override fun observeCategoriesInGroup(groupId: String): Flow<List<Category>> =
        categories.map { it.values.filter { c -> c.groupId == groupId } }

    override suspend fun getGroup(groupId: String): CategoryGroup? = groups.value[groupId]

    override suspend fun getCategory(id: String): Category? = categories.value[id]

    override suspend fun createCustomCategory(category: Category) {
        categories.value = categories.value + (category.id to category)
    }

    override suspend fun updateCategory(category: Category) {
        categories.value = categories.value + (category.id to category)
    }

    override suspend fun isCategoryInUse(categoryId: String): Boolean = categoryId in categoriesInUse

    override suspend fun deleteCategory(categoryId: String) {
        categories.value = categories.value - categoryId
    }
}
