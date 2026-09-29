package com.example.wallet.domain.usecase.template

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UpdateTemplateUseCaseTest {

    private lateinit var repository: FakeTemplateRepository
    private lateinit var createUseCase: CreateTemplateUseCase
    private lateinit var updateUseCase: UpdateTemplateUseCase

    @Before
    fun setUp() {
        repository = FakeTemplateRepository()
        createUseCase = CreateTemplateUseCase(repository)
        updateUseCase = UpdateTemplateUseCase(repository)
    }

    @Test
    fun `updates a template's fields`() = runTest {
        val template = createUseCase("Lunch", "account-1", "category-1", "label-1", null, null).getOrThrow()

        val result = updateUseCase(template.id, "Dinner", "account-2", "category-2", "label-2", "Bistro", "Uptown")

        assertTrue(result.isSuccess)
        val updated = result.getOrThrow()
        assertEquals("Dinner", updated.name)
        assertEquals("account-2", updated.accountId)
        assertEquals("category-2", updated.categoryId)
        assertEquals("label-2", updated.labelId)
        assertEquals("Bistro", updated.payee)
        assertEquals("Uptown", updated.place)
    }

    @Test
    fun `unknown template is rejected`() = runTest {
        val result = updateUseCase("does-not-exist", "Name", "account-1", "category-1", "label-1", null, null)

        assertTrue(result.isFailure)
        assertEquals(TemplateError.TemplateNotFound, (result.exceptionOrNull() as TemplateValidationException).error)
    }

    @Test
    fun `blank name is rejected`() = runTest {
        val template = createUseCase("Lunch", "account-1", "category-1", "label-1", null, null).getOrThrow()

        val result = updateUseCase(template.id, "  ", "account-1", "category-1", "label-1", null, null)

        assertTrue(result.isFailure)
        assertEquals(TemplateError.NameRequired, (result.exceptionOrNull() as TemplateValidationException).error)
    }

    @Test
    fun `missing category is rejected`() = runTest {
        val template = createUseCase("Lunch", "account-1", "category-1", "label-1", null, null).getOrThrow()

        val result = updateUseCase(template.id, "Lunch", "account-1", null, "label-1", null, null)

        assertTrue(result.isFailure)
        assertEquals(TemplateError.CategoryRequired, (result.exceptionOrNull() as TemplateValidationException).error)
    }
}
