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
