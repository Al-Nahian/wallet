package com.expensetracker.wallet.domain.usecase.category

import com.expensetracker.wallet.domain.model.Category
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DeleteCategoryUseCaseTest {

    private lateinit var repository: FakeCategoryRepository
    private lateinit var useCase: DeleteCategoryUseCase

    @Before
    fun setUp() {
        repository = FakeCategoryRepository()
        useCase = DeleteCategoryUseCase(repository)
        repository.addCategory(Category(id = "custom-1", groupId = "group-1", name = "Ramen", icon = null, sortOrder = 0, isSystem = false))
        repository.addCategory(Category(id = "custom-2", groupId = "group-1", name = "Snacks", icon = null, sortOrder = 0, isSystem = false))
        repository.addCategory(Category(id = "system-1", groupId = "group-1", name = "Groceries", icon = null, sortOrder = 0, isSystem = true))
    }

    @Test
    fun `deletes an unused custom category`() = runTest {
        val result = useCase("custom-1")

        assertTrue(result.isSuccess)
        assertTrue(repository.observeCategories().first().none { it.id == "custom-1" })
    }

    @Test
    fun `blocks deletion of a category in use, never a silent cascade`() = runTest {
        repository.categoriesInUse += "custom-2"

        val result = useCase("custom-2")

        assertTrue(result.isFailure)
        assertEquals(CategoryError.CategoryInUse, (result.exceptionOrNull() as CategoryValidationException).error)
        assertTrue(repository.observeCategories().first().any { it.id == "custom-2" })
    }

    @Test
    fun `system categories cannot be deleted`() = runTest {
        val result = useCase("system-1")

        assertTrue(result.isFailure)
        assertEquals(CategoryError.SystemCategoryImmutable, (result.exceptionOrNull() as CategoryValidationException).error)
    }

    @Test
    fun `unknown category is rejected`() = runTest {
        val result = useCase("does-not-exist")

        assertTrue(result.isFailure)
        assertEquals(CategoryError.CategoryNotFound, (result.exceptionOrNull() as CategoryValidationException).error)
    }
}
