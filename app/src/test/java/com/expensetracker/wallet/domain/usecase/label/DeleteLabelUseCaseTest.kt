package com.expensetracker.wallet.domain.usecase.label

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DeleteLabelUseCaseTest {

    private lateinit var repository: FakeLabelRepository
    private lateinit var createUseCase: CreateLabelUseCase
    private lateinit var deleteUseCase: DeleteLabelUseCase

    @Before
    fun setUp() {
        repository = FakeLabelRepository()
        createUseCase = CreateLabelUseCase(repository)
        deleteUseCase = DeleteLabelUseCase(repository)
    }

    @Test
    fun `deletes a label and clears it from any assigned transaction`() = runTest {
        val label = createUseCase("Family").getOrThrow()
        repository.assign("tx-1", label.id)

        val result = deleteUseCase(label.id)

        assertTrue(result.isSuccess)
        assertTrue(repository.observeLabels().first().isEmpty())
        assertTrue(repository.observeLabelsForTransaction("tx-1").first().isEmpty())
    }

    @Test
    fun `unknown label is rejected`() = runTest {
        val result = deleteUseCase("does-not-exist")

        assertTrue(result.isFailure)
        assertEquals(LabelError.LabelNotFound, (result.exceptionOrNull() as LabelValidationException).error)
    }
}
