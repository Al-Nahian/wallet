package com.example.wallet.core.testing

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.wallet.core.database.AppDatabase

/**
 * In-memory Room DB builder shared by every instrumented DAO/migration test (Room needs a
 * real Android SQLite implementation, so this lives in androidTest, not a JVM unit test).
 */
fun buildInMemoryTestDatabase(): AppDatabase =
    Room.inMemoryDatabaseBuilder(
        ApplicationProvider.getApplicationContext(),
        AppDatabase::class.java,
    ).allowMainThreadQueries().build()
