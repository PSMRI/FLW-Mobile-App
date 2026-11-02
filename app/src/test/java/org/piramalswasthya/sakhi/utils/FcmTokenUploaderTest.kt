package org.piramalswasthya.sakhi.utils

import android.content.Context
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.EntryPointAccessors
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.Runs
import io.mockk.coVerify
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.LocationEntity
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.repositories.UserRepo

class FcmTokenUploaderTest {

    private lateinit var context: Context
    private lateinit var preferenceDao: PreferenceDao
    private lateinit var userRepo: UserRepo
    private lateinit var entryPoint: FcmTokenUploader.FcmEntryPoint
    private lateinit var firebaseMessaging: FirebaseMessaging

    private fun sampleUser(userId: Int = 11) = User(
        userId = userId,
        name = "Asha Worker",
        userName = "asha1",
        password = "pass",
        role = "ASHA",
        serviceMapId = 3,
        state = LocationEntity(2, "Assam"),
        district = LocationEntity(3, "Kamrup"),
        block = LocationEntity(4, "Block1"),
        villages = listOf(LocationEntity(5, "Village1"))
    )

    private inline fun <reified T> mockTask(
        result: T? = null,
        exception: Exception? = null,
        successful: Boolean = exception == null
    ): Task<T> {
        val task: Task<T> = mockk(relaxed = true)
        every { task.isSuccessful } returns successful
        every { task.result } returns result
        every { task.exception } returns exception
        every { task.addOnCompleteListener(any()) } answers {
            firstArg<OnCompleteListener<T>>().onComplete(task)
            task
        }
        return task
    }

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        preferenceDao = mockk(relaxed = true)
        userRepo = mockk(relaxed = true)
        entryPoint = mockk()
        firebaseMessaging = mockk(relaxed = true)

        every { entryPoint.preferenceDao() } returns preferenceDao
        every { entryPoint.userRepo() } returns userRepo

        mockkStatic(EntryPointAccessors::class)
        every {
            EntryPointAccessors.fromApplication(any(), FcmTokenUploader.FcmEntryPoint::class.java)
        } returns entryPoint

        mockkStatic(FirebaseMessaging::class)
        every { FirebaseMessaging.getInstance() } returns firebaseMessaging
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `uploadToken does nothing when no user is logged in`() {
        every { preferenceDao.getLoggedInUser() } returns null

        FcmTokenUploader.uploadToken(context, "explicit-token")

        coVerify(exactly = 0) { userRepo.saveFirebaseToken(any(), any(), any()) }
        verify(exactly = 0) { FirebaseMessaging.getInstance() }
    }

    @Test
    fun `uploadToken pushes an explicitly supplied token without fetching a new one`() {
        every { preferenceDao.getLoggedInUser() } returns sampleUser(11)

        FcmTokenUploader.uploadToken(context, "explicit-token")

        coVerify(timeout = 2000) {
            userRepo.saveFirebaseToken(11, "explicit-token", any())
        }
        verify(exactly = 0) { FirebaseMessaging.getInstance() }
    }

    @Test
    fun `uploadToken fetches and pushes the current token when none is supplied`() {
        every { preferenceDao.getLoggedInUser() } returns sampleUser(11)
        every { firebaseMessaging.token } returns mockTask(result = "fetched-token")

        FcmTokenUploader.uploadToken(context)

        coVerify(timeout = 2000) {
            userRepo.saveFirebaseToken(11, "fetched-token", any())
        }
    }

    @Test
    fun `uploadToken does not push when the token fetch fails`() {
        every { preferenceDao.getLoggedInUser() } returns sampleUser(11)
        every { firebaseMessaging.token } returns mockTask(
            result = null,
            exception = RuntimeException("no token")
        )

        FcmTokenUploader.uploadToken(context)

        coVerify(exactly = 0) { userRepo.saveFirebaseToken(any(), any(), any()) }
    }

    @Test
    fun `uploadToken does not push when the fetched token result is null`() {
        every { preferenceDao.getLoggedInUser() } returns sampleUser(11)
        every { firebaseMessaging.token } returns mockTask(result = null)

        FcmTokenUploader.uploadToken(context)

        coVerify(exactly = 0) { userRepo.saveFirebaseToken(any(), any(), any()) }
    }

    @Test
    fun `clearToken does nothing when no user is logged in`() {
        every { preferenceDao.getLoggedInUser() } returns null
        mockkObject(FcmTopicManager)

        FcmTokenUploader.clearToken(context)

        verify(exactly = 0) { FcmTopicManager.unsubscribe(any()) }
        coVerify(exactly = 0) { userRepo.clearFirebaseToken(any()) }
        verify(exactly = 0) { firebaseMessaging.deleteToken() }
    }

    @Test
    fun `clearToken unsubscribes, unbinds on server and deletes the local token when a user is logged in`() {
        every { preferenceDao.getLoggedInUser() } returns sampleUser(11)
        mockkObject(FcmTopicManager)
        every { FcmTopicManager.unsubscribe(any()) } just Runs
        every { firebaseMessaging.deleteToken() } returns mockTask(result = null)

        FcmTokenUploader.clearToken(context)

        verify(exactly = 1) { FcmTopicManager.unsubscribe(11) }
        coVerify(timeout = 2000) { userRepo.clearFirebaseToken(11) }
        verify(exactly = 1) { firebaseMessaging.deleteToken() }
    }
}
