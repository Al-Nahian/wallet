package com.expensetracker.wallet.domain.usecase.template

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CreateTemplateUseCaseTest {

    private lateinit var repository: FakeTemplateRepository
    private lateinit var useCase: CreateTemplateUseCase

    @Before
    fun setUp() {
        repository = FakeTemplateRepository()
        useCase = CreateTemplateUseCase(repository)
    }

    @Test
    fun `creates a template with the given fields`() = runTest {
        val result = useCase("Lunch", "account-1", "category-1", "label-1", "Deli", "Downtown")

        assertTrue(result.isSuccess)
        val template = result.getOrThrow()
        assertEquals("Lunch", template.name)
        assertEquals("account-1", template.accountId)
        assertEquals("category-1", template.categoryId)
        assertEquals("label-1", template.labelId)
        assertEquals("Deli", template.payee)
        assertEquals("Downtown", template.place)
        assertEquals(template, repository.observeTemplates().first().single())
    }

    @Test
    fun `blank optional payee and place are stored as null`() = runTest {
        val result = useCase("Lunch", "account-1", "category-1", "label-1", "   ", "")

        assertTrue(result.isSuccess)
        val template = result.getOrThrow()
        assertNull(template.payee)
        assertNull(template.place)
    }

    @Test
    fun `blank name is rejected`() = runTest {
        val result = useCase("  ", "account-1", "category-1", "label-1", null, null)

        assertTrue(result.isFailure)
        assertEquals(TemplateError.NameRequired, (result.exceptionOrNull() as TemplateValidationException).error)
    }

    @Test
    fun `missing account is rejected`() = runTest {
        val result = useCase("Lunch", null, "category-1", "label-1", null, null)

        assertTrue(result.isFailure)
        assertEquals(TemplateError.AccountRequired, (result.exceptionOrNull() as TemplateValidationException).error)
    }

    @Test
    fun `missing category is rejected`() = runTest {
        val result = useCase("Lunch", "account-1", null, "label-1", null, null)

        assertTrue(result.isFailure)
        assertEquals(TemplateError.CategoryRequired, (result.exceptionOrNull() as TemplateValidationException).error)
    }

    @Test
    fun `missing label is rejected`() = runTest {
        val result = useCase("Lunch", "account-1", "category-1", null, null, null)

        assertTrue(result.isFailure)
        assertEquals(TemplateError.LabelRequired, (result.exceptionOrNull() as TemplateValidationException).error)
    }
}
