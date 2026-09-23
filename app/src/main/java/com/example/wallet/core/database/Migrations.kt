package com.example.wallet.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * v1 -> v2 (Phase 4): adds `notifications` (plan.md §86) and `users` (plan.md §84/§85).
 * The first real migration since Phase 2's initial schema — every schema change from here on
 * follows this same pattern (§69 rule 11), never a destructive fallback.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `notifications` (" +
                "`id` TEXT NOT NULL, `type` TEXT NOT NULL, `title` TEXT NOT NULL, " +
                "`body` TEXT NOT NULL, `deepLink` TEXT, `createdAt` INTEGER NOT NULL, " +
                "`readAt` INTEGER, `relatedEntityType` TEXT, `relatedEntityId` TEXT, " +
                "PRIMARY KEY(`id`))",
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_notifications_readAt` ON `notifications` (`readAt`)",
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_notifications_createdAt` ON `notifications` (`createdAt`)",
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `users` (" +
                "`id` TEXT NOT NULL, `displayName` TEXT NOT NULL, `avatarUrl` TEXT, " +
                "`createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))",
        )
    }
}

/**
 * v2 -> v3 (Phase 6, plan.md §22): adds `transferId` to `transactions`, linking the two legs of
 * a transfer created by `CreateTransferUseCase`. Nullable, so every pre-existing row is
 * unaffected (they were never transfers).
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `transactions` ADD COLUMN `transferId` TEXT")
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_transactions_transferId` ON `transactions` (`transferId`)",
        )
    }
}

/**
 * v3 -> v4 (Phase 11, plans/11-recurring-goals.md): adds the two columns the recurring-transaction
 * engine needs on top of the table that already existed since v3 —
 * `recurring_transactions.autoPost` (default true, so every rule written before this migration
 * keeps behaving as an auto-posting rule rather than silently switching to reminder-only) and
 * `transactions.recurringTransactionId` (nullable — every pre-existing row was entered manually).
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE `recurring_transactions` ADD COLUMN `autoPost` INTEGER NOT NULL DEFAULT 1",
        )
        db.execSQL("ALTER TABLE `transactions` ADD COLUMN `recurringTransactionId` TEXT")
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_transactions_recurringTransactionId` " +
                "ON `transactions` (`recurringTransactionId`)",
        )
    }
}
