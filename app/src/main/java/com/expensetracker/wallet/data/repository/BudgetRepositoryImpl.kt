package com.expensetracker.wallet.data.repository

import androidx.room.withTransaction
import com.expensetracker.wallet.core.database.AppDatabase
import com.expensetracker.wallet.data.local.dao.BudgetCategoryDao
import com.expensetracker.wallet.data.local.dao.BudgetDao
import com.expensetracker.wallet.domain.model.Budget
import com.expensetracker.wallet.domain.model.BudgetCategory
import com.expensetracker.wallet.domain.repository.BudgetRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Budget + its per-category limits are written atomically (§72), mirroring
 * `TransactionRepositoryImpl`'s transfer-pair pattern. */
class BudgetRepositoryImpl @Inject constructor(
    private val appDatabase: AppDatabase,
    private val budgetDao: BudgetDao,
    private val budgetCategoryDao: BudgetCategoryDao,
) : BudgetRepository {

    override fun observeBudgets(): Flow<List<Budget>> =
        budgetDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun observeBudget(id: String): Flow<Budget?> =
        budgetDao.observeById(id).map { it?.toDomain() }

    override fun observeBudgetCategories(budgetId: String): Flow<List<BudgetCategory>> =
        budgetCategoryDao.observeByBudget(budgetId).map { entities -> entities.map { it.toDomain() } }

    override fun observeAllBudgetCategories(): Flow<List<BudgetCategory>> =
        budgetCategoryDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun create(budget: Budget, categoryLimits: List<BudgetCategory>) {
        appDatabase.withTransaction {
            budgetDao.insert(budget.toEntity())
            if (categoryLimits.isNotEmpty()) {
                budgetCategoryDao.upsertAll(categoryLimits.map { it.toEntity() })
            }
        }
    }

    override suspend fun update(budget: Budget, categoryLimits: List<BudgetCategory>) {
        appDatabase.withTransaction {
            budgetDao.update(budget.toEntity())
            budgetCategoryDao.deleteByBudget(budget.id)
            if (categoryLimits.isNotEmpty()) {
                budgetCategoryDao.upsertAll(categoryLimits.map { it.toEntity() })
            }
        }
    }
}
