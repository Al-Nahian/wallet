package com.example.wallet.domain.usecase.label

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AssignLabelUseCaseTest {

    private lateinit var repository: FakeLabelRepository
    private lateinit var createUseCase: CreateLabelUseCase
    private lateinit var assignUseCase: AssignLabelUseCase

    @Before
    fun setUp() {
        repository = FakeLabelRepository()
        createUseCase = CreateLabelUseCase(repository)
        assignUseCase = AssignLabelUseCase(repository)
    }

    @Test
    fun `assigns multiple labels to a transaction`() = runTest {
        val family = createUseCase("Family", "#F44336").getOrThrow()
        val work = createUseCase("Work", "#42B5E8").getOrThrow()

        val result = assignUseCase("tx-1", setOf(family.id, work.id))

        assertTrue(result.isSuccess)
        val assigned = repository.observeLabelsForTransaction("tx-1").first()
        assertEquals(setOf("Family", "Work"), assigned.map { it.name }.toSet())
    }

    @Test
    fun `re-assigning replaces the previous label set, not adds to it`() = runTest {
        val family = createUseCase("Family", "#F44336").getOrThrow()
        val work = createUseCase("Work", "#42B5E8").getOrThrow()
        assignUseCase("tx-1", setOf(family.id, work.id))

        val result = assignUseCase("tx-1", setOf(family.id))

        assertTrue(result.isSuccess)
        val assigned = repository.observeLabelsForTransaction("tx-1").first()
        assertEquals(listOf("Family"), assigned.map { it.name })
    }

    @Test
    fun `an empty set clears all labels`() = runTest {
        val family = createUseCase("Family", "#F44336").getOrThrow()
        assignUseCase("tx-1", setOf(family.id))

        val result = assignUseCase("tx-1", emptySet())

        assertTrue(result.isSuccess)
        assertTrue(repository.observeLabelsForTransaction("tx-1").first().isEmpty())
    }
}
