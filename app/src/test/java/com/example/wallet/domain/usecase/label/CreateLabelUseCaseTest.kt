package com.example.wallet.domain.usecase.label

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CreateLabelUseCaseTest {

    private lateinit var repository: FakeLabelRepository
    private lateinit var useCase: CreateLabelUseCase

    @Before
    fun setUp() {
        repository = FakeLabelRepository()
        useCase = CreateLabelUseCase(repository)
    }

    @Test
    fun `creates a label with the given name and color`() = runTest {
        val result = useCase("Family", "#F44336")

        assertTrue(result.isSuccess)
        val label = result.getOrThrow()
        assertEquals("Family", label.name)
        assertEquals("#F44336", label.color)
        assertEquals(label, repository.observeLabels().first().single())
    }

    @Test
    fun `blank name is rejected`() = runTest {
        val result = useCase("  ", "#F44336")

        assertTrue(result.isFailure)
        assertEquals(LabelError.NameRequired, (result.exceptionOrNull() as LabelValidationException).error)
    }
}
