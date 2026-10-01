package org.piramalswasthya.sakhi.utils

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FcmTopicManagerTest {

    private lateinit var firebaseMessaging: FirebaseMessaging

    private inline fun <reified T> mockTask(successful: Boolean = true): Task<T> {
        val task: Task<T> = mockk(relaxed = true)
        every { task.isSuccessful } returns successful
        every { task.addOnCompleteListener(any()) } answers {
            firstArg<OnCompleteListener<T>>().onComplete(task)
            task
        }
        return task
    }

    @Before
    fun setUp() {
        firebaseMessaging = mockk(relaxed = true)
        mockkStatic(FirebaseMessaging::class)
        every { FirebaseMessaging.getInstance() } returns firebaseMessaging
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `topicForUser prefixes the user id`() {
        assertEquals("user_42", FcmTopicManager.topicForUser(42))
    }

    @Test
    fun `topicForUser handles a null user id`() {
        assertEquals("user_null", FcmTopicManager.topicForUser(null))
    }

    @Test
    fun `subscribe does nothing when userId is null`() {
        FcmTopicManager.subscribe(null)

        verify(exactly = 0) { FirebaseMessaging.getInstance() }
    }

    @Test
    fun `subscribe subscribes to the per-user topic`() {
        every { firebaseMessaging.subscribeToTopic("user_7") } returns mockTask(successful = true)

        FcmTopicManager.subscribe(7)

        verify { firebaseMessaging.subscribeToTopic("user_7") }
    }

    @Test
    fun `subscribe logs a failed completion without throwing`() {
        every { firebaseMessaging.subscribeToTopic("user_9") } returns mockTask(successful = false)

        FcmTopicManager.subscribe(9)

        verify { firebaseMessaging.subscribeToTopic("user_9") }
    }

    @Test
    fun `unsubscribe does nothing when userId is null`() {
        FcmTopicManager.unsubscribe(null)

        verify(exactly = 0) { FirebaseMessaging.getInstance() }
    }

    @Test
    fun `unsubscribe unsubscribes from the per-user topic`() {
        every { firebaseMessaging.unsubscribeFromTopic("user_7") } returns mockTask(successful = false)

        FcmTopicManager.unsubscribe(7)

        verify { firebaseMessaging.unsubscribeFromTopic("user_7") }
    }
}
