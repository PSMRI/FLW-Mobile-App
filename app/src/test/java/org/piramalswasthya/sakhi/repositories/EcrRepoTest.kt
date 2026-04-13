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
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.EcrDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.EligibleCoupleRegCache
import org.piramalswasthya.sakhi.model.EligibleCoupleTrackingCache
import org.piramalswasthya.sakhi.network.AmritApiService

@OptIn(ExperimentalCoroutinesApi::class)
class EcrRepoTest : BaseRepositoryTest() {

    @MockK private lateinit var amritApiService: AmritApiService
    @MockK private lateinit var userRepo: UserRepo
    @MockK private lateinit var database: InAppDb
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var tmcNetworkApiService: AmritApiService
    @MockK private lateinit var context: Application
    @MockK private lateinit var ecrDao: EcrDao
    @MockK private lateinit var benDao: BenDao

    private lateinit var repo: EcrRepo

    @Before
    override fun setUp() {
        super.setUp()
        coEvery { database.ecrDao } returns ecrDao
        coEvery { database.benDao } returns benDao
        repo = EcrRepo(amritApiService, userRepo, database, preferenceDao, tmcNetworkApiService, context)
    }

    // =====================================================
    // getSavedRecord() Tests
    // =====================================================

    @Test
    fun `getSavedRecord returns ecr when exists`() = runTest {
        val ecr = mockk<EligibleCoupleRegCache>()
        coEvery { ecrDao.getSavedECR(100L) } returns ecr

        val result = repo.getSavedRecord(100L)

        assertNotNull(result)
        assertEquals(ecr, result)
    }

    @Test
    fun `getSavedRecord returns null when not exists`() = runTest {
        coEvery { ecrDao.getSavedECR(999L) } returns null

        val result = repo.getSavedRecord(999L)

        assertNull(result)
    }

    // =====================================================
    // getBenFromId() Tests
    // =====================================================

    @Test
    fun `getBenFromId returns ben when exists`() = runTest {
        val ben = mockk<BenRegCache>()
        coEvery { benDao.getBen(100L) } returns ben

        val result = repo.getBenFromId(100L)

        assertNotNull(result)
        assertEquals(ben, result)
    }

    @Test
    fun `getBenFromId returns null when not exists`() = runTest {
        coEvery { benDao.getBen(999L) } returns null

        val result = repo.getBenFromId(999L)

        assertNull(result)
    }

    // =====================================================
    // persistRecord() Tests
    // =====================================================

    @Test
    fun `persistRecord calls dao upsert`() = runTest {
        val ecr = mockk<EligibleCoupleRegCache>()
        coEvery { ecrDao.upsert(ecr) } returns Unit

        repo.persistRecord(ecr)

        coVerify(exactly = 1) { ecrDao.upsert(ecr) }
    }

    // =====================================================
    // getNoOfChildren() Tests
    // =====================================================

    @Test
    fun `getNoOfChildren returns count`() = runTest {
        coEvery { ecrDao.getNoOfChildren(100L) } returns 3

        val result = repo.getNoOfChildren(100L)

        assertEquals(3, result)
    }

    @Test
    fun `getNoOfChildren returns null when not exists`() = runTest {
        coEvery { ecrDao.getNoOfChildren(999L) } returns null

        val result = repo.getNoOfChildren(999L)

        assertNull(result)
    }

    // =====================================================
    // getEct() Tests
    // =====================================================

    @Test
    fun `getEct returns record when exists`() = runTest {
        val ect = mockk<EligibleCoupleTrackingCache>()
        coEvery { ecrDao.getEct(100L, 1000L) } returns ect

        val result = repo.getEct(100L, 1000L)

        assertNotNull(result)
        assertEquals(ect, result)
    }

    @Test
    fun `getEct returns null when not exists`() = runTest {
        coEvery { ecrDao.getEct(999L, 1000L) } returns null

        val result = repo.getEct(999L, 1000L)

        assertNull(result)
    }

    // =====================================================
    // saveEct() Tests
    // =====================================================

    @Test
    fun `saveEct calls dao upsert`() = runTest {
        val ect = mockk<EligibleCoupleTrackingCache>()
        coEvery { ecrDao.upsert(ect) } returns Unit

        repo.saveEct(ect)

        coVerify(exactly = 1) { ecrDao.upsert(ect) }
    }
}
