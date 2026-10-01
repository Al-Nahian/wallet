package com.expensetracker.wallet.data.local

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.expensetracker.wallet.core.database.AppDatabase
import com.expensetracker.wallet.core.testing.buildInMemoryTestDatabase
import com.expensetracker.wallet.data.local.entity.NotificationEntity
import com.expensetracker.wallet.data.local.entity.UserEntity
import com.expensetracker.wallet.domain.model.NotificationType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Insert + query round-trip for Phase 4's entities (plans/04-...md acceptance criteria). */
@RunWith(AndroidJUnit4::class)
class NotificationAndUserDaoTest {

    private lateinit var db: AppDatabase

    @Before
    fun setUp() {
        db = buildInMemoryTestDatabase()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun notificationRoundTrip() = runBlocking {
        val notification = NotificationEntity(
            id = "notification-1",
            type = NotificationType.SYSTEM,
            title = "Welcome",
            body = "Welcome to Wallet",
            createdAt = 0L,
        )

        assertFalse(db.notificationDao().hasAny())
        db.notificationDao().insert(notification)

        assertTrue(db.notificationDao().hasAny())
        assertEquals(listOf(notification), db.notificationDao().observeAll().first())
        assertEquals(1, db.notificationDao().observeUnreadCount().first())
    }

    @Test
    fun markReadIsIdempotent() = runBlocking {
        val notification = NotificationEntity(
            id = "notification-1",
            type = NotificationType.SYSTEM,
            title = "Welcome",
            body = "Welcome to Wallet",
            createdAt = 0L,
        )
        db.notificationDao().insert(notification)

        db.notificationDao().markRead("notification-1", readAt = 1000L)
        assertEquals(1000L, db.notificationDao().observeAll().first().single().readAt)
        assertEquals(0, db.notificationDao().observeUnreadCount().first())

        // Second mark-read must not overwrite the first timestamp (COALESCE).
        db.notificationDao().markRead("notification-1", readAt = 2000L)
        assertEquals(1000L, db.notificationDao().observeAll().first().single().readAt)
    }

    @Test
    fun userRoundTrip() = runBlocking {
        assertNull(db.userDao().observeCurrentUser().first())

        val user = UserEntity(
            id = "user-1",
            displayName = "Test User",
            createdAt = 0L,
            updatedAt = 0L,
        )
        db.userDao().upsert(user)

        assertNotNull(db.userDao().observeCurrentUser().first())
        assertEquals(user, db.userDao().observeCurrentUser().first())
    }
}
