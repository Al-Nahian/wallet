package com.expensetracker.wallet.domain.usecase.notification

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.expensetracker.wallet.core.lifecycle.AppForegroundTracker
import com.expensetracker.wallet.domain.model.NotificationType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private val dummyLifecycleOwner = object : LifecycleOwner {
    override val lifecycle: Lifecycle get() = throw UnsupportedOperationException("unused")
}

class CreateNotificationUseCaseTest {

    private lateinit var repository: FakeNotificationRepository
    private lateinit var poster: FakeNotificationPoster
    private lateinit var foregroundTracker: AppForegroundTracker
    private lateinit var useCase: CreateNotificationUseCase

    @Before
    fun setUp() {
        repository = FakeNotificationRepository()
        poster = FakeNotificationPoster()
        foregroundTracker = AppForegroundTracker()
        useCase = CreateNotificationUseCase(repository, poster, foregroundTracker)
    }

    @Test
    fun `creates the notification row regardless of foreground state`() = runTest {
        foregroundTracker.onStart(dummyLifecycleOwner)

        val notification = useCase(
            type = NotificationType.BUDGET_WARNING,
            title = "80% used",
            body = "You've used 80% of your Groceries budget.",
        )

        assertEquals(notification, repository.observeAll().first().single())
    }

    @Test
    fun `does not post a system notification while the app is foregrounded`() = runTest {
        foregroundTracker.onStart(dummyLifecycleOwner)

        useCase(NotificationType.SYSTEM, "Title", "Body")

        assertTrue(poster.posted.isEmpty())
    }

    @Test
    fun `posts a system notification when the app is backgrounded`() = runTest {
        foregroundTracker.onStart(dummyLifecycleOwner)
        foregroundTracker.onStop(dummyLifecycleOwner)

        val notification = useCase(NotificationType.SYSTEM, "Title", "Body")

        assertEquals(listOf(notification), poster.posted)
    }
}
