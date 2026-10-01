package com.expensetracker.wallet.domain.usecase.category

import com.expensetracker.wallet.domain.model.CategoryGroup
import com.expensetracker.wallet.domain.model.CategoryType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CreateCategoryUseCaseTest {

    private lateinit var repository: FakeCategoryRepository
    private lateinit var useCase: CreateCategoryUseCase

    @Before
    fun setUp() {
        repository = FakeCategoryRepository()
        useCase = CreateCategoryUseCase(repository)
        repository.addGroup(
            CategoryGroup(id = "group-1", name = "Food & Drinks", color = "#F44336", icon = null, type = CategoryType.EXPENSE, sortOrder = 0, isSystem = true),
        )
    }

    @Test
    fun `creates a non-system category in the given group`() = runTest {
        val result = useCase("group-1", "Ramen")

        assertTrue(result.isSuccess)
        val category = result.getOrThrow()
        assertEquals("group-1", category.groupId)
        assertEquals("Ramen", category.name)
        assertFalse(category.isSystem)
        assertEquals(category, repository.observeCategories().first().single())
    }

    @Test
    fun `blank name is rejected`() = runTest {
        val result = useCase("group-1", "   ")

        assertTrue(result.isFailure)
        assertEquals(CategoryError.NameRequired, (result.exceptionOrNull() as CategoryValidationException).error)
    }

    @Test
    fun `unknown group is rejected`() = runTest {
        val result = useCase("does-not-exist", "Ramen")

        assertTrue(result.isFailure)
        assertEquals(CategoryError.GroupNotFound, (result.exceptionOrNull() as CategoryValidationException).error)
    }
}
