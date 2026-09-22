package com.example.wallet.domain.usecase.label

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UpdateLabelUseCaseTest {

    private lateinit var repository: FakeLabelRepository
    private lateinit var createUseCase: CreateLabelUseCase
    private lateinit var updateUseCase: UpdateLabelUseCase

    @Before
    fun setUp() {
        repository = FakeLabelRepository()
        createUseCase = CreateLabelUseCase(repository)
        updateUseCase = UpdateLabelUseCase(repository)
    }

    @Test
    fun `renames and recolors a label`() = runTest {
        val label = createUseCase("Family", "#F44336").getOrThrow()

        val result = updateUseCase(label.id, "Household", "#4CAF50")

        assertTrue(result.isSuccess)
        val updated = result.getOrThrow()
        assertEquals("Household", updated.name)
        assertEquals("#4CAF50", updated.color)
    }

    @Test
    fun `unknown label is rejected`() = runTest {
        val result = updateUseCase("does-not-exist", "Name", "#F44336")

        assertTrue(result.isFailure)
        assertEquals(LabelError.LabelNotFound, (result.exceptionOrNull() as LabelValidationException).error)
    }
}
