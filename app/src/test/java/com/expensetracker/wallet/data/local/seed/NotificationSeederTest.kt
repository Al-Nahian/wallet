package com.expensetracker.wallet.data.local.seed

import com.expensetracker.wallet.core.lifecycle.AppForegroundTracker
import com.expensetracker.wallet.domain.model.NotificationType
import com.expensetracker.wallet.domain.usecase.notification.CreateNotificationUseCase
import com.expensetracker.wallet.domain.usecase.notification.FakeNotificationPoster
import com.expensetracker.wallet.domain.usecase.notification.FakeNotificationRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class NotificationSeederTest {

    private lateinit var repository: FakeNotificationRepository
    private lateinit var seeder: NotificationSeeder

    @Before
    fun setUp() {
        repository = FakeNotificationRepository()
        val createUseCase = CreateNotificationUseCase(repository, FakeNotificationPoster(), AppForegroundTracker())
        seeder = NotificationSeeder(repository, createUseCase)
    }

    @Test
    fun `seeds exactly one welcome notification when empty`() = runTest {
        seeder.seedIfEmpty()

        val notifications = repository.observeAll().first()
        assertEquals(1, notifications.size)
        assertEquals(NotificationType.SYSTEM, notifications.single().type)
    }

    @Test
    fun `does not reseed when notifications already exist`() = runTest {
        seeder.seedIfEmpty()
        seeder.seedIfEmpty()

        assertEquals(1, repository.observeAll().first().size)
    }
}
