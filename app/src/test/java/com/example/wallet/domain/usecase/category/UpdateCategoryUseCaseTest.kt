package com.example.wallet.domain.usecase.category

import com.example.wallet.domain.model.Category
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UpdateCategoryUseCaseTest {

    private lateinit var repository: FakeCategoryRepository
    private lateinit var useCase: UpdateCategoryUseCase

    @Before
    fun setUp() {
        repository = FakeCategoryRepository()
        useCase = UpdateCategoryUseCase(repository)
        repository.addCategory(Category(id = "custom-1", groupId = "group-1", name = "Ramen", icon = null, sortOrder = 0, isSystem = false))
        repository.addCategory(Category(id = "system-1", groupId = "group-1", name = "Groceries", icon = null, sortOrder = 0, isSystem = true))
    }

    @Test
    fun `renames a custom category`() = runTest {
        val result = useCase("custom-1", "Instant noodles")

        assertTrue(result.isSuccess)
        assertEquals("Instant noodles", result.getOrThrow().name)
    }

    @Test
    fun `system categories cannot be renamed`() = runTest {
        val result = useCase("system-1", "Renamed")

        assertTrue(result.isFailure)
        assertEquals(CategoryError.SystemCategoryImmutable, (result.exceptionOrNull() as CategoryValidationException).error)
    }

    @Test
    fun `blank name is rejected`() = runTest {
        val result = useCase("custom-1", " ")

        assertTrue(result.isFailure)
        assertEquals(CategoryError.NameRequired, (result.exceptionOrNull() as CategoryValidationException).error)
    }

    @Test
    fun `unknown category is rejected`() = runTest {
        val result = useCase("does-not-exist", "New name")

        assertTrue(result.isFailure)
        assertEquals(CategoryError.CategoryNotFound, (result.exceptionOrNull() as CategoryValidationException).error)
    }
}
