package com.example.wallet.domain.usecase.template

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DeleteTemplateUseCaseTest {

    private lateinit var repository: FakeTemplateRepository
    private lateinit var createUseCase: CreateTemplateUseCase
    private lateinit var deleteUseCase: DeleteTemplateUseCase

    @Before
    fun setUp() {
        repository = FakeTemplateRepository()
        createUseCase = CreateTemplateUseCase(repository)
        deleteUseCase = DeleteTemplateUseCase(repository)
    }

    @Test
    fun `deletes an existing template`() = runTest {
        val template = createUseCase("Lunch", "account-1", "category-1", "label-1", null, null).getOrThrow()

        val result = deleteUseCase(template.id)

        assertTrue(result.isSuccess)
        assertTrue(repository.observeTemplates().first().isEmpty())
    }
}
