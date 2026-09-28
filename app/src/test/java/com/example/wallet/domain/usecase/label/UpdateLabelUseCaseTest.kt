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
    fun `renames a label`() = runTest {
        val label = createUseCase("Family").getOrThrow()

        val result = updateUseCase(label.id, "Household")

        assertTrue(result.isSuccess)
        val updated = result.getOrThrow()
        assertEquals("Household", updated.name)
    }

    @Test
    fun `unknown label is rejected`() = runTest {
        val result = updateUseCase("does-not-exist", "Name")

        assertTrue(result.isFailure)
        assertEquals(LabelError.LabelNotFound, (result.exceptionOrNull() as LabelValidationException).error)
    }

    @Test
    fun `renaming to another label's name is rejected`() = runTest {
        val first = createUseCase("Family").getOrThrow()
        createUseCase("Work").getOrThrow()

        val result = updateUseCase(first.id, "work")

        assertTrue(result.isFailure)
        assertEquals(LabelError.DuplicateName, (result.exceptionOrNull() as LabelValidationException).error)
    }
}
