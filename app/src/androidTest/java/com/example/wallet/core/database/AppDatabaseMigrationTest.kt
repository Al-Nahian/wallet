package com.example.wallet.core.database

import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private const val TEST_DB_NAME = "migration-test.db"

/**
 * Validates the exported v1 schema (plans/02-database.md's migration-test-harness
 * requirement), plus the real v1 -> v2 migration Phase 4 added (see
 * `migration1To2AddsNotificationsAndUsersTables`).
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

    /**
     * Phase 4's real migration: v1 -> v2 adds `notifications` and `users` (plan.md §84/§86).
     * `runMigrationsAndValidate` replays MIGRATION_1_2 against a real v1 database and checks
     * the result against the exported v2 schema — catches any hand-written-SQL mismatch, not
     * just table presence.
     */
    @Test
    fun migration1To2AddsNotificationsAndUsersTables() {
        helper.createDatabase(TEST_DB_NAME, 1).close()

        val db = helper.runMigrationsAndValidate(TEST_DB_NAME, 2, true, MIGRATION_1_2)

        val actualTables = mutableSetOf<String>()
        db.query("SELECT name FROM sqlite_master WHERE type = 'table'").use { cursor ->
            while (cursor.moveToNext()) {
                actualTables += cursor.getString(0)
            }
        }

        assertTrue("notifications" in actualTables)
        assertTrue("users" in actualTables)

        db.close()
    }
}
