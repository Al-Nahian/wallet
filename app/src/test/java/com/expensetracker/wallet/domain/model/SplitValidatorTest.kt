package com.expensetracker.wallet.domain.model

import com.expensetracker.wallet.domain.rules.validateSplitsSum
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SplitValidatorTest {

    @Test
    fun `splits that sum to the transaction total are valid`() {
        assertTrue(validateSplitsSum(3_000_00, listOf(1_500_00, 900_00, 600_00)))
    }

    @Test
    fun `splits that do not sum to the transaction total are invalid`() {
        assertFalse(validateSplitsSum(3_000_00, listOf(1_500_00, 900_00, 500_00)))
    }

    @Test
    fun `a single split equal to the total is valid`() {
        assertTrue(validateSplitsSum(1_000_00, listOf(1_000_00)))
    }

    @Test
    fun `empty splits are invalid unless the transaction total is zero`() {
        assertFalse(validateSplitsSum(1_000_00, emptyList()))
        assertTrue(validateSplitsSum(0L, emptyList()))
    }
}
