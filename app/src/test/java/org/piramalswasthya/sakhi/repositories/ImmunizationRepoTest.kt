package org.piramalswasthya.sakhi.repositories

import android.util.Log
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.ImmunizationDao
import org.piramalswasthya.sakhi.database.room.dao.TBDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.network.AmritApiService

@OptIn(ExperimentalCoroutinesApi::class)
class ImmunizationRepoTest : BaseRepositoryTest() {

    @MockK private lateinit var tbDao: TBDao
    @MockK private lateinit var immunizationDao: ImmunizationDao
    @MockK private lateinit var benDao: BenDao
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var userRepo: UserRepo
    @MockK private lateinit var amritApiService: AmritApiService

    private lateinit var repo: ImmunizationRepo

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
        every { Log.isLoggable(any(), any()) } returns false
        repo = ImmunizationRepo(tbDao, immunizationDao, benDao, preferenceDao, userRepo, amritApiService)
    }

    // =====================================================
    // pushUnSyncedChildImmunizationRecords() Tests
    // =====================================================

    @Test
    fun `pushUnSyncedChildImmunizationRecords returns true when no unsynced`() = runTest {
        val user = mockk<org.piramalswasthya.sakhi.model.User>(relaxed = true)
        every { preferenceDao.getLoggedInUser() } returns user
        coEvery { immunizationDao.getUnsyncedImmunization(SyncState.UNSYNCED) } returns emptyList()
        val result = repo.pushUnSyncedChildImmunizationRecords()
        assertTrue(result)
    }
}
