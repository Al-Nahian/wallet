package com.example.wallet.domain.usecase.notification

import com.example.wallet.core.lifecycle.AppForegroundTracker
import com.example.wallet.domain.model.NotificationType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class MarkNotificationReadUseCaseTest {

    private lateinit var repository: FakeNotificationRepository
    private lateinit var createUseCase: CreateNotificationUseCase
    private lateinit var markReadUseCase: MarkNotificationReadUseCase

    @Before
    fun setUp() {
        repository = FakeNotificationRepository()
        createUseCase = CreateNotificationUseCase(repository, FakeNotificationPoster(), AppForegroundTracker())
        markReadUseCase = MarkNotificationReadUseCase(repository)
    }

    @Test
    fun `marks an unread notification read`() = runTest {
        val notification = createUseCase(NotificationType.SYSTEM, "Title", "Body")

        markReadUseCase(notification.id)

        assertNotNull(repository.observeAll().first().single().readAt)
    }

    @Test
    fun `marking an already-read notification again is a no-op`() = runTest {
        val notification = createUseCase(NotificationType.SYSTEM, "Title", "Body")

        markReadUseCase(notification.id)
        val firstReadAt = repository.observeAll().first().single().readAt

        markReadUseCase(notification.id)
        val secondReadAt = repository.observeAll().first().single().readAt

        assertEquals(firstReadAt, secondReadAt)
    }
}
