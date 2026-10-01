package com.expensetracker.wallet.core.database

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

/**
 * v4 -> v5 (Phase 14, plans/14-sms-notification-automation.md): adds `transactions.source`
 * (default `'MANUAL'`, so every pre-existing row stays correctly tagged as manually entered) and
 * `transactions.sourceReference` (nullable — the SMS/notification capture pipeline's dedup key),
 * plus the new `automation_candidates` table backing the Review Queue.
 */
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE `transactions` ADD COLUMN `source` TEXT NOT NULL DEFAULT 'MANUAL'",
        )
        db.execSQL("ALTER TABLE `transactions` ADD COLUMN `sourceReference` TEXT")
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_transactions_sourceReference` " +
                "ON `transactions` (`sourceReference`)",
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `automation_candidates` (" +
                "`id` TEXT NOT NULL, `sourceType` TEXT NOT NULL, `type` TEXT NOT NULL, " +
                "`amountMinor` INTEGER NOT NULL, `currency` TEXT NOT NULL, `accountId` TEXT, " +
                "`toAccountId` TEXT, `categoryId` TEXT, `payee` TEXT, `note` TEXT, " +
                "`date` INTEGER NOT NULL, `confidence` TEXT NOT NULL, `sourceReference` TEXT, " +
                "`status` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))",
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_automation_candidates_status` " +
                "ON `automation_candidates` (`status`)",
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_automation_candidates_sourceReference` " +
                "ON `automation_candidates` (`sourceReference`)",
        )
    }
}

/** v5 -> v6: adds `transactions.place` (nullable free-text location) for the redesigned
 * transaction form's "Place" field. */
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `transactions` ADD COLUMN `place` TEXT")
    }
}

/** v6 -> v7: adds `templates` — a saved account/category/label/payee/place shortcut applied from
 * the transaction form. All three FKs cascade: a template pointing at a deleted account,
 * category, or label can no longer be applied to anything, so it's deleted along with it (same
 * reasoning as `transactions.accountId`'s cascade in the v1 schema). */
val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `templates` (" +
                "`id` TEXT NOT NULL, `name` TEXT NOT NULL, `accountId` TEXT NOT NULL, " +
                "`categoryId` TEXT NOT NULL, `labelId` TEXT NOT NULL, `payee` TEXT, `place` TEXT, " +
                "`createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`), " +
                "FOREIGN KEY(`accountId`) REFERENCES `accounts`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, " +
                "FOREIGN KEY(`categoryId`) REFERENCES `categories`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, " +
                "FOREIGN KEY(`labelId`) REFERENCES `labels`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)",
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_templates_accountId` ON `templates` (`accountId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_templates_categoryId` ON `templates` (`categoryId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_templates_labelId` ON `templates` (`labelId`)")
    }
}
