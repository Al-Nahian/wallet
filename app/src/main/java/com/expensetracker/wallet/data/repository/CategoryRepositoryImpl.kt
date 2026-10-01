package com.expensetracker.wallet.data.repository

import com.expensetracker.wallet.data.local.dao.CategoryDao
import com.expensetracker.wallet.data.local.dao.CategoryGroupDao
import com.expensetracker.wallet.data.local.dao.TransactionDao
import com.expensetracker.wallet.data.local.dao.TransactionSplitDao
import com.expensetracker.wallet.domain.model.Category
import com.expensetracker.wallet.domain.model.CategoryGroup
import com.expensetracker.wallet.domain.repository.CategoryRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryRepositoryImpl @Inject constructor(
    private val categoryGroupDao: CategoryGroupDao,
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao,
    private val transactionSplitDao: TransactionSplitDao,
) : CategoryRepository {

    override fun observeGroups(): Flow<List<CategoryGroup>> =
        categoryGroupDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun observeCategories(): Flow<List<Category>> =
        categoryDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun observeCategoriesInGroup(groupId: String): Flow<List<Category>> =
        categoryDao.observeByGroup(groupId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun getGroup(groupId: String): CategoryGroup? = categoryGroupDao.getById(groupId)?.toDomain()

    override suspend fun getCategory(id: String): Category? = categoryDao.getById(id)?.toDomain()

    override suspend fun createCustomCategory(category: Category) =
        categoryDao.insert(category.toEntity())

    override suspend fun updateCategory(category: Category) =
        categoryDao.update(category.toEntity())

    override suspend fun isCategoryInUse(categoryId: String): Boolean =
        transactionDao.existsByCategory(categoryId) || transactionSplitDao.existsByCategory(categoryId)

    override suspend fun deleteCategory(categoryId: String) {
        val entity = categoryDao.getById(categoryId) ?: return
        categoryDao.delete(entity)
    }
}
