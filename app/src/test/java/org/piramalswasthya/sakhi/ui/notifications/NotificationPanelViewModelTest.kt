package org.piramalswasthya.sakhi.ui.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.model.NotificationDomain
import org.piramalswasthya.sakhi.repositories.NotificationRepository

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationPanelViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var repository: NotificationRepository

    private lateinit var viewModel: NotificationPanelViewModel

    private fun sampleNotification(id: Long = 1L, read: Boolean = false) = NotificationDomain(
        notificationId = id,
        eventType = "INCENTIVE_CLAIMED",
        title = "Title $id",
        body = "Body $id",
        createdTs = 1000L,
        read = read
    )

    private fun <T> LiveData<T>.latest(): T? {
        var captured: T? = null
        val observer = Observer<T> { captured = it }
        observeForever(observer)
        advanceUntilIdleIfPossible()
        removeObserver(observer)
        return captured
    }

    private fun advanceUntilIdleIfPossible() {
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @Before
    override fun setUp() {
        super.setUp()
        every { repository.notifications } returns flowOf(listOf(sampleNotification()))
        every { repository.unreadCount } returns flowOf(1)
        coEvery { repository.pullAndSaveNotifications() } returns true
        viewModel = NotificationPanelViewModel(repository)
    }

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `notifications LiveData is not null`() {
        assertNotNull(viewModel.notifications)
    }

    @Test
    fun `unreadCount LiveData is not null`() {
        assertNotNull(viewModel.unreadCount)
    }

    @Test
    fun `notifications exposes the list mapped from the repository`() {
        val result = viewModel.notifications.latest()
        assertEquals(listOf(sampleNotification()), result)
    }

    @Test
    fun `notifications exposes an empty list when the repository has none`() {
        every { repository.notifications } returns flowOf(emptyList())
        val vm = NotificationPanelViewModel(repository)

        val result = vm.notifications.latest()

        assertTrue(result.isNullOrEmpty())
    }

    @Test
    fun `unreadCount exposes the count from the repository`() {
        val result = viewModel.unreadCount.latest()
        assertEquals(1, result)
    }

    @Test
    fun `unreadCount exposes zero when the repository has none unread`() {
        every { repository.unreadCount } returns flowOf(0)
        val vm = NotificationPanelViewModel(repository)

        val result = vm.unreadCount.latest()

        assertEquals(0, result)
    }

    @Test
    fun `init triggers a pull of notifications from the repository`() = runTest {
        val vm = NotificationPanelViewModel(repository)
        advanceUntilIdle()

        assertNotNull(vm)
        coVerify(atLeast = 1) { repository.pullAndSaveNotifications() }
    }

    @Test
    fun `refresh delegates to the repository pull`() = runTest {
        viewModel.refresh()
        advanceUntilIdle()

        coVerify(atLeast = 1) { repository.pullAndSaveNotifications() }
    }

    @Test
    fun `refresh handles a failed pull without throwing`() = runTest {
        coEvery { repository.pullAndSaveNotifications() } returns false

        viewModel.refresh()
        advanceUntilIdle()

        coVerify(atLeast = 1) { repository.pullAndSaveNotifications() }
    }

    @Test
    fun `refresh can be called multiple times without throwing`() = runTest {
        viewModel.refresh()
        viewModel.refresh()
        advanceUntilIdle()

        coVerify(atLeast = 2) { repository.pullAndSaveNotifications() }
    }

    @Test
    fun `markRead delegates to the repository with the given notification id`() = runTest {
        coEvery { repository.markRead(any()) } returns Unit

        viewModel.markRead(42L)
        advanceUntilIdle()

        coVerify { repository.markRead(42L) }
    }

    @Test
    fun `markRead with different ids invokes the repository separately`() = runTest {
        coEvery { repository.markRead(any()) } returns Unit

        viewModel.markRead(1L)
        viewModel.markRead(2L)
        advanceUntilIdle()

        coVerify { repository.markRead(1L) }
        coVerify { repository.markRead(2L) }
    }

    @Test
    fun `dismiss delegates to the repository with the given notification id`() = runTest {
        coEvery { repository.dismiss(any()) } returns Unit

        viewModel.dismiss(7L)
        advanceUntilIdle()

        coVerify { repository.dismiss(7L) }
    }

    @Test
    fun `clearAll delegates to the repository`() = runTest {
        coEvery { repository.clearAll() } returns Unit

        viewModel.clearAll()
        advanceUntilIdle()

        coVerify { repository.clearAll() }
    }
}
