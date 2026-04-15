package org.piramalswasthya.sakhi.repositories

import android.app.Application
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.room.dao.HrpDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.HRPPregnantAssessCache
import org.piramalswasthya.sakhi.model.HRPNonPregnantAssessCache
import org.piramalswasthya.sakhi.model.HRPMicroBirthPlanCache
import org.piramalswasthya.sakhi.network.AmritApiService

@OptIn(ExperimentalCoroutinesApi::class)
class HRPRepoTest : BaseRepositoryTest() {

    @MockK private lateinit var database: InAppDb
    @MockK private lateinit var userRepo: UserRepo
    @MockK private lateinit var maternalHealthRepo: MaternalHealthRepo
    @MockK private lateinit var ecrRepo: EcrRepo
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var tmcNetworkApiService: AmritApiService
    @MockK private lateinit var context: Application
    @MockK private lateinit var hrpDao: HrpDao

    private lateinit var repo: HRPRepo

    @Before
    override fun setUp() {
        super.setUp()
        coEvery { database.hrpDao } returns hrpDao
        repo = HRPRepo(database, userRepo, maternalHealthRepo, ecrRepo, preferenceDao, tmcNetworkApiService, context)
    }

    // =====================================================
    // getPregnantAssess() Tests
    // =====================================================

    @Test
    fun `getPregnantAssess returns record when exists`() = runTest {
        val assess = mockk<HRPPregnantAssessCache>()
        coEvery { hrpDao.getPregnantAssess(100L) } returns assess

        val result = repo.getPregnantAssess(100L)

        assertNotNull(result)
        assertEquals(assess, result)
    }

    @Test
    fun `getPregnantAssess returns null when not exists`() = runTest {
        coEvery { hrpDao.getPregnantAssess(999L) } returns null

        val result = repo.getPregnantAssess(999L)

        assertNull(result)
    }

    // =====================================================
    // getNonPregnantAssess() Tests
    // =====================================================

    @Test
    fun `getNonPregnantAssess returns record when exists`() = runTest {
        val assess = mockk<HRPNonPregnantAssessCache>()
        coEvery { hrpDao.getNonPregnantAssess(100L) } returns assess

        val result = repo.getNonPregnantAssess(100L)

        assertNotNull(result)
        assertEquals(assess, result)
    }

    @Test
    fun `getNonPregnantAssess returns null when not exists`() = runTest {
        coEvery { hrpDao.getNonPregnantAssess(999L) } returns null

        val result = repo.getNonPregnantAssess(999L)

        assertNull(result)
    }

    // =====================================================
    // getMicroBirthPlan() Tests
    // =====================================================

    @Test
    fun `getMicroBirthPlan returns record when exists`() = runTest {
        val plan = mockk<HRPMicroBirthPlanCache>()
        coEvery { hrpDao.getMicroBirthPlan(100L) } returns plan

        val result = repo.getMicroBirthPlan(100L)

        assertNotNull(result)
        assertEquals(plan, result)
    }

    @Test
    fun `getMicroBirthPlan returns null when not exists`() = runTest {
        coEvery { hrpDao.getMicroBirthPlan(999L) } returns null

        val result = repo.getMicroBirthPlan(999L)

        assertNull(result)
    }

    // =====================================================
    // saveRecord() Tests
    // =====================================================

    @Test
    fun `saveRecord pregnant assess calls dao`() = runTest {
        val assess = mockk<HRPPregnantAssessCache>()
        coEvery { hrpDao.saveRecord(assess) } returns Unit

        repo.saveRecord(assess)

        coVerify(exactly = 1) { hrpDao.saveRecord(assess) }
    }

    @Test
    fun `saveRecord non pregnant assess calls dao`() = runTest {
        val assess = mockk<HRPNonPregnantAssessCache>()
        coEvery { hrpDao.saveRecord(assess) } returns Unit

        repo.saveRecord(assess)

        coVerify(exactly = 1) { hrpDao.saveRecord(assess) }
    }
}
