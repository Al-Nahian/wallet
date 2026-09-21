package com.example.wallet.data.local.seed

import com.example.wallet.core.common.newId
import com.example.wallet.data.local.dao.CategoryDao
import com.example.wallet.data.local.dao.CategoryGroupDao
import com.example.wallet.data.local.entity.CategoryEntity
import com.example.wallet.data.local.entity.CategoryGroupEntity
import javax.inject.Inject

/** Seeds the default category taxonomy (plan.md §14) on first launch only. */
class CategorySeeder @Inject constructor(
    private val categoryGroupDao: CategoryGroupDao,
    private val categoryDao: CategoryDao,
) {
    suspend fun seedIfEmpty() {
        if (categoryGroupDao.count() > 0) return

        val groupEntities = mutableListOf<CategoryGroupEntity>()
        val categoryEntities = mutableListOf<CategoryEntity>()

        CategorySeed.groups.forEachIndexed { groupIndex, group ->
            val groupId = newId()
            groupEntities += CategoryGroupEntity(
                id = groupId,
                name = group.name,
                color = group.color,
                icon = null,
                type = group.type,
                sortOrder = groupIndex,
                isSystem = true,
            )
            group.subcategories.forEachIndexed { categoryIndex, subcategoryName ->
                categoryEntities += CategoryEntity(
                    id = newId(),
                    groupId = groupId,
                    name = subcategoryName,
                    icon = null,
                    sortOrder = categoryIndex,
                    isSystem = true,
                )
            }
        }

        categoryGroupDao.upsertAll(groupEntities)
        categoryDao.upsertAll(categoryEntities)
    }
}
