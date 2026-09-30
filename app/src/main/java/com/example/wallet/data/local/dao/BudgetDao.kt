package com.example.wallet.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.wallet.data.local.entity.BudgetCategoryEntity
import com.example.wallet.data.local.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets ORDER BY startDate DESC")
    fun observeAll(): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE id = :id")
    fun observeById(id: String): Flow<BudgetEntity?>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(budget: BudgetEntity)

    @Update
    suspend fun update(budget: BudgetEntity)
}

@Dao
interface BudgetCategoryDao {
    @Query("SELECT * FROM budget_categories")
    fun observeAll(): Flow<List<BudgetCategoryEntity>>

    @Query("SELECT * FROM budget_categories WHERE budgetId = :budgetId")
    fun observeByBudget(budgetId: String): Flow<List<BudgetCategoryEntity>>

    @Query("SELECT * FROM budget_categories WHERE categoryId = :categoryId")
    fun observeByCategory(categoryId: String): Flow<List<BudgetCategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(budgetCategories: List<BudgetCategoryEntity>)

    @Query("DELETE FROM budget_categories WHERE budgetId = :budgetId")
    suspend fun deleteByBudget(budgetId: String)
}
