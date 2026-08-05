package org.piramalswasthya.sakhi.repositories

import android.content.Context
import com.squareup.moshi.Moshi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.room.dao.MaaMeetingDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.MaaMeetingEntity
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.network.AmritApiService
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class MaaMeetingRepoTest : BaseRepositoryTest() {

    @MockK private lateinit var appContext: Context
    @MockK private lateinit var dao: MaaMeetingDao
    @MockK private lateinit var api: AmritApiService
    @MockK private lateinit var pref: PreferenceDao
    @MockK private lateinit var moshi: Moshi

    private lateinit var repo: MaaMeetingRepo

    private val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH)

    @Before
    override fun setUp() {
        super.setUp()
        repo = MaaMeetingRepo(appContext, dao, api, pref, moshi)
    }

    private fun loggedInUser(userId: Int = 42) {
        val user = mockk<User>(relaxed = true)
        every { user.userId } returns userId
        every { user.userName } returns "asha_user"
        every { pref.getLoggedInUser() } returns user
    }

    // ---------------- buildEntity ----------------

    @Test
    fun `buildEntity maps fields and drops null images`() {
        loggedInUser(userId = 7)

        val entity = repo.buildEntity(
            date = "01-01-2026",
            place = "Community Hall",
            participants = 12,
            villageName = "Village A",
            mitaninActivityCheckList = "check",
            noOfPragnentWoment = "3",
            noOfLactingMother = "2",
            u1 = "uri1", u2 = null, u3 = "uri3", u4 = null, u5 = null
        )

        assertEquals("01-01-2026", entity.meetingDate)
        assertEquals("Community Hall", entity.place)
        assertEquals(12, entity.participants)
        assertEquals("Village A", entity.villageName)
        assertEquals(7, entity.ashaId)
        assertEquals(listOf("uri1", "uri3"), entity.meetingImages)
        assertEquals(SyncState.UNSYNCED, entity.syncState)
    }

    @Test
    fun `buildEntity uses null ashaId when no user logged in`() {
        every { pref.getLoggedInUser() } returns null

        val entity = repo.buildEntity(
            date = null, place = null, participants = null,
            u1 = null, u2 = null, u3 = null, u4 = null, u5 = null
        )

        assertNull(entity.ashaId)
        assertTrue(entity.meetingImages!!.isEmpty())
    }

    // ---------------- save ----------------

    @Test
    fun `save inserts entity and returns generated id`() = runTest {
        val entity = mockk<MaaMeetingEntity>(relaxed = true)
        every { dao.insert(entity) } returns 55L

        val id = repo.save(entity)

        assertEquals(55L, id)
        verify { dao.insert(entity) }
    }

    // ---------------- isThreeMonthsPassedSinceLastMeeting ----------------

    @Test
    fun `isThreeMonthsPassed returns true for null or blank date`() = runTest {
        assertTrue(repo.isThreeMonthsPassedSinceLastMeeting(null))
        assertTrue(repo.isThreeMonthsPassedSinceLastMeeting("   "))
    }

    @Test
    fun `isThreeMonthsPassed returns true for unparseable date`() = runTest {
        assertTrue(repo.isThreeMonthsPassedSinceLastMeeting("not-a-date"))
    }

    @Test
    fun `isThreeMonthsPassed returns true when four months elapsed`() = runTest {
        val fourMonthsAgo = LocalDate.now().minusMonths(4).format(formatter)
        assertTrue(repo.isThreeMonthsPassedSinceLastMeeting(fourMonthsAgo))
    }

    @Test
    fun `isThreeMonthsPassed returns false when one month elapsed`() = runTest {
        val oneMonthAgo = LocalDate.now().minusMonths(1).format(formatter)
        assertFalse(repo.isThreeMonthsPassedSinceLastMeeting(oneMonthAgo))
    }

    @Test
    fun `isThreeMonthsPassed returns false for today`() = runTest {
        val today = LocalDate.now().format(formatter)
        assertFalse(repo.isThreeMonthsPassedSinceLastMeeting(today))
    }

    // ---------------- getters ----------------

    @Test
    fun `getAllMaaMeetings delegates to dao`() {
        val flow = flowOf(emptyList<MaaMeetingEntity>())
        every { dao.getAllMaaData() } returns flow

        assertEquals(flow, repo.getAllMaaMeetings())
    }

    @Test
    fun `getMaaMeetingById returns dao result`() = runTest {
        val entity = mockk<MaaMeetingEntity>(relaxed = true)
        coEvery { dao.getMaaMeetingById(9L) } returns entity

        assertEquals(entity, repo.getMaaMeetingById(9L))
    }

    // ---------------- tryUpsync early return ----------------

    @Test
    fun `tryUpsync does nothing when no unsynced rows`() = runTest {
        every { dao.getBySyncState(SyncState.UNSYNCED) } returns emptyList()

        repo.tryUpsync()

        coVerify(exactly = 0) { api.postMaaMeetingMultipart(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) }
    }
}
