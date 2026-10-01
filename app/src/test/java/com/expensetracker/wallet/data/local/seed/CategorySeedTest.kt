package com.expensetracker.wallet.data.local.seed

import com.expensetracker.wallet.domain.model.CategoryType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CategorySeedTest {

    @Test
    fun `there are exactly 10 default category groups`() {
        assertEquals(10, CategorySeed.groups.size)
    }

    @Test
    fun `group names and colors match plan md section 14 verbatim`() {
        val expected = listOf(
            "Food & Drinks" to "#F44336",
            "Shopping" to "#42B5E8",
            "Housing" to "#FF9F1C",
            "Transportation" to "#78909C",
            "Vehicle" to "#9C6ADE",
            "Life & Entertainment" to "#4CAF50",
            "Communication, PC" to "#B39DDB",
            "Financial Expense" to "#26A69A",
            "Income" to "#2E8B57",
            "Others" to "#9E9E9E",
        )

        val actual = CategorySeed.groups.map { it.name to it.color }
        assertEquals(expected, actual)
    }

    @Test
    fun `only the Income group is income-type`() {
        val incomeGroups = CategorySeed.groups.filter { it.type == CategoryType.INCOME }
        assertEquals(listOf("Income"), incomeGroups.map { it.name })
    }

    @Test
    fun `every group has at least one subcategory`() {
        assertTrue(CategorySeed.groups.all { it.subcategories.isNotEmpty() })
    }

    @Test
    fun `subcategory counts match plan md section 14`() {
        val expectedCounts = mapOf(
            "Food & Drinks" to 4,
            "Shopping" to 12,
            "Housing" to 7,
            "Transportation" to 6,
            "Vehicle" to 6,
            "Life & Entertainment" to 11,
            "Communication, PC" to 4,
            "Financial Expense" to 7,
            "Income" to 7,
            "Others" to 2,
        )

        val actualCounts = CategorySeed.groups.associate { it.name to it.subcategories.size }
        assertEquals(expectedCounts, actualCounts)
    }
}
