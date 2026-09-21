package com.example.wallet.data.repository

import com.example.wallet.data.local.dao.CategoryDao
import com.example.wallet.data.local.dao.CategoryGroupDao
import com.example.wallet.domain.model.Category
import com.example.wallet.domain.model.CategoryGroup
import com.example.wallet.domain.repository.CategoryRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Only the read paths + `createCustomCategory` (already trivial over Phase 2's DAOs) are
 * implemented here — Phase 5 needs read-only category listing for the transaction form's
 * category selector. Phase 7 (Categories & Labels) is still where the *management* UI
 * (custom category creation screen, editing) gets built; this class doesn't grow for that,
 * only its callers do.
 */
class CategoryRepositoryImpl @Inject constructor(
    private val categoryGroupDao: CategoryGroupDao,
    private val categoryDao: CategoryDao,
) : CategoryRepository {

    override fun observeGroups(): Flow<List<CategoryGroup>> =
        categoryGroupDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun observeCategories(): Flow<List<Category>> =
        categoryDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun observeCategoriesInGroup(groupId: String): Flow<List<Category>> =
        categoryDao.observeByGroup(groupId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun createCustomCategory(category: Category) =
        categoryDao.insert(category.toEntity())
}
