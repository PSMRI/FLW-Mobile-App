package org.piramalswasthya.sakhi.notifications

import android.content.Context
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.dao.EveningNotifDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.NotifTemplateCache
import org.piramalswasthya.sakhi.model.User

class NotificationEngineTest {

    private val context = mockk<Context>(relaxed = true)
    private val dao = mockk<EveningNotifDao>(relaxed = true)
    private val interceptor = mockk<FormSaveInterceptor>(relaxed = true)
    private val selector = mockk<TemplateSelector>(relaxed = true)
    private val filler = mockk<TemplateFiller>(relaxed = true)
    private val pref = mockk<PreferenceDao>(relaxed = true)

    private val engine = NotificationEngine(context, dao, interceptor, selector, filler, pref)

    @Test
    fun `logged out device gets no evening notification`() = runTest {
        every { pref.getLoggedInUser() } returns null

        engine.deliverEveningNotification()

        coVerify(exactly = 0) { dao.shownCountForDay(any()) }
        coVerify(exactly = 0) { interceptor.captureToday() }
        coVerify(exactly = 0) { dao.logShown(any()) }
        verify(exactly = 0) { context.getSystemService(any<String>()) }
    }

    @Test
    fun `already notified today is not repeated`() = runTest {
        every { pref.getLoggedInUser() } returns mockk(relaxed = true)
        coEvery { dao.shownCountForDay(any()) } returns 1

        engine.deliverEveningNotification()

        coVerify(exactly = 0) { interceptor.captureToday() }
        coVerify(exactly = 0) { selector.select(any(), any()) }
    }

    @Test
    fun `builds the day's message and records it only once posted`() = runTest {
        val user = mockk<User>(relaxed = true)
        every { user.name } returns "Kavita"
        every { pref.getLoggedInUser() } returns user
        every { pref.getCurrentLanguage() } returns Languages.HINDI
        coEvery { dao.shownCountForDay(any()) } returns 0
        coEvery { dao.countForDay(any()) } returns 2
        coEvery { selector.select("TWO_PLUS", Languages.HINDI.symbol) } returns
                NotifTemplateCache("t", "TWO_PLUS", "hi", "Hi {name}", 0)
        every { filler.fill("Hi {name}", "Kavita") } returns "Hi Kavita"

        engine.deliverEveningNotification()

        coVerify { interceptor.captureToday() }
        verify { filler.fill("Hi {name}", "Kavita") }
        // the framework is stubbed in unit tests, so the post cannot succeed: the day stays open
        coVerify(exactly = 0) { dao.logShown(any()) }
    }

    @Test
    fun `language lookup failure falls back to English`() = runTest {
        every { pref.getLoggedInUser() } returns mockk(relaxed = true)
        every { pref.getCurrentLanguage() } throws IllegalStateException("prefs")
        coEvery { dao.shownCountForDay(any()) } returns 0
        coEvery { dao.countForDay(any()) } returns 0

        engine.deliverEveningNotification()

        coVerify { selector.select("ZERO", "en") }
    }

    @Test
    fun `reading the user failing is treated as logged out`() = runTest {
        every { pref.getLoggedInUser() } throws IllegalStateException("prefs")

        engine.deliverEveningNotification()

        coVerify(exactly = 0) { dao.shownCountForDay(any()) }
    }
}
