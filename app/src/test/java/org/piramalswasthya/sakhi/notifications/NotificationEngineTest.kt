package org.piramalswasthya.sakhi.notifications

import android.content.Context
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.piramalswasthya.sakhi.database.room.dao.EveningNotifDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao

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
}
