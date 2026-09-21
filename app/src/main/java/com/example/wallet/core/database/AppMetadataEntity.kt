package com.example.wallet.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Temporary placeholder entity. Room's @Database annotation requires at
 * least one entity to compile; this proves the Room + Hilt wiring works
 * in Phase 1 before any real financial schema exists. Phase 2 replaces
 * this with the actual entities from plan.md §11-§19 and removes it.
 */
@Entity(tableName = "app_metadata")
data class AppMetadataEntity(
    @PrimaryKey val key: String,
    val value: String,
)
