package com.example.wallet.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Only the temporary [AppMetadataEntity] exists so far — Phase 2 adds the
 * real financial schema. This proves the Room + Hilt wiring works end to end.
 */
@Database(entities = [AppMetadataEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase()
