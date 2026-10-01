package com.expensetracker.wallet.domain.usecase.notification

import com.expensetracker.wallet.core.lifecycle.AppForegroundTracker
import com.expensetracker.wallet.domain.model.NotificationType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ObserveUnreadNotificationCountUseCaseTest {

    private lateinit var repository: FakeNotificationRepository
    private lateinit var createUseCase: CreateNotificationUseCase
    private lateinit var markReadUseCase: MarkNotificationReadUseCase
    private lateinit var observeUnreadCount: ObserveUnreadNotificationCountUseCase

    @Before
    fun setUp() {
        repository = FakeNotificationRepository()
        createUseCase = CreateNotificationUseCase(repository, FakeNotificationPoster(), AppForegroundTracker())
        markReadUseCase = MarkNotificationReadUseCase(repository)
        observeUnreadCount = ObserveUnreadNotificationCountUseCase(repository)
    }

    @Test
    fun `count reflects only unread notifications`() = runTest {
        createUseCase(NotificationType.SYSTEM, "One", "Body")
        val second = createUseCase(NotificationType.SYSTEM, "Two", "Body")
        createUseCase(NotificationType.SYSTEM, "Three", "Body")

        assertEquals(3, observeUnreadCount().first())

        markReadUseCase(second.id)

        assertEquals(2, observeUnreadCount().first())
    }

    @Test
    fun `count is zero when there are no notifications`() = runTest {
        assertEquals(0, observeUnreadCount().first())
    }
}
