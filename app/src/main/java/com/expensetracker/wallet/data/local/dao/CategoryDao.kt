package com.expensetracker.wallet.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.expensetracker.wallet.data.local.entity.CategoryEntity
import com.expensetracker.wallet.data.local.entity.CategoryGroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryGroupDao {
    @Query("SELECT * FROM category_groups ORDER BY sortOrder")
    fun observeAll(): Flow<List<CategoryGroupEntity>>

    @Query("SELECT COUNT(*) FROM category_groups")
    suspend fun count(): Int

    @Query("SELECT * FROM category_groups WHERE id = :id")
    suspend fun getById(id: String): CategoryGroupEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(groups: List<CategoryGroupEntity>)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE groupId = :groupId ORDER BY sortOrder")
    fun observeByGroup(groupId: String): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY sortOrder")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: String): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(categories: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(category: CategoryEntity)

    @Update
    suspend fun update(category: CategoryEntity)

    @Delete
    suspend fun delete(category: CategoryEntity)
}
