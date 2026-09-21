package com.example.wallet.core.database

import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private const val TEST_DB_NAME = "migration-test.db"

/**
 * Validates the exported v1 schema (plans/02-database.md's migration-test-harness
 * requirement). There's no prior version to migrate *from* yet — this proves the
 * MigrationTestHelper + room.schemaLocation toolchain is wired correctly so the
 * next real schema change (Phase 4 adding notifications/users) becomes a genuinely
 * tested v1 -> v2 migration instead of the first one ever attempted.
 */
class AppDatabaseMigrationTest {

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
    )

    @Test
    fun version1SchemaContainsAllExpectedTables() {
        val db = helper.createDatabase(TEST_DB_NAME, 1)

        val expectedTables = setOf(
            "institutions",
            "accounts",
            "transactions",
            "transaction_splits",
            "category_groups",
            "categories",
            "labels",
            "transaction_labels",
            "budgets",
            "budget_categories",
            "recurring_transactions",
            "goals",
            "goal_contributions",
            "merchants",
            "merchant_aliases",
        )

        val actualTables = mutableSetOf<String>()
        db.query("SELECT name FROM sqlite_master WHERE type = 'table'").use { cursor ->
            while (cursor.moveToNext()) {
                actualTables += cursor.getString(0)
            }
        }

        expectedTables.forEach { table ->
            assertTrue("Expected table '$table' to exist in schema v1", table in actualTables)
        }

        db.close()
    }
}
