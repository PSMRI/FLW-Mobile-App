package org.piramalswasthya.sakhi.repositories

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.TBDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.TBConfirmedTreatmentCache
import org.piramalswasthya.sakhi.model.TBScreeningCache
import org.piramalswasthya.sakhi.model.TBSuspectedCache
import org.piramalswasthya.sakhi.network.AmritApiService

@OptIn(ExperimentalCoroutinesApi::class)
class TBRepoTest : BaseRepositoryTest() {

    @MockK private lateinit var tbDao: TBDao
    @MockK private lateinit var benDao: BenDao
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var userRepo: UserRepo
    @MockK private lateinit var tmcNetworkApiService: AmritApiService

    private lateinit var repo: TBRepo

    @Before
    override fun setUp() {
        super.setUp()
        repo = TBRepo(tbDao, benDao, preferenceDao, userRepo, tmcNetworkApiService)
    }

    // =====================================================
    // getTBScreening() Tests
    // =====================================================

    @Test
    fun `getTBScreening returns record when exists`() = runTest {
        val screening = mockk<TBScreeningCache>()
        coEvery { tbDao.getTbScreening(100L) } returns screening

        val result = repo.getTBScreening(100L)

        assertNotNull(result)
        assertEquals(screening, result)
    }

    @Test
    fun `getTBScreening returns null when not exists`() = runTest {
        coEvery { tbDao.getTbScreening(999L) } returns null

        val result = repo.getTBScreening(999L)

        assertNull(result)
    }

    // =====================================================
    // saveTBScreening() Tests
    // =====================================================

    @Test
    fun `saveTBScreening calls dao`() = runTest {
        val screening = mockk<TBScreeningCache>()
        coEvery { tbDao.saveTbScreening(screening) } returns Unit

        repo.saveTBScreening(screening)

        coVerify(exactly = 1) { tbDao.saveTbScreening(screening) }
    }

    // =====================================================
    // getTBSuspected() Tests
    // =====================================================

    @Test
    fun `getTBSuspected returns record when exists`() = runTest {
        val suspected = mockk<TBSuspectedCache>()
        coEvery { tbDao.getTbSuspected(100L) } returns suspected

        val result = repo.getTBSuspected(100L)

        assertNotNull(result)
        assertEquals(suspected, result)
    }

    @Test
    fun `getTBSuspected returns null when not exists`() = runTest {
        coEvery { tbDao.getTbSuspected(999L) } returns null

        val result = repo.getTBSuspected(999L)

        assertNull(result)
    }

    // =====================================================
    // saveTBSuspected() Tests
    // =====================================================

    @Test
    fun `saveTBSuspected calls dao`() = runTest {
        val suspected = mockk<TBSuspectedCache>()
        coEvery { tbDao.saveTbSuspected(suspected) } returns Unit

        repo.saveTBSuspected(suspected)

        coVerify(exactly = 1) { tbDao.saveTbSuspected(suspected) }
    }

    // =====================================================
    // getTBConfirmed() Tests
    // =====================================================

    @Test
    fun `getTBConfirmed returns record when exists`() = runTest {
        val confirmed = mockk<TBConfirmedTreatmentCache>()
        coEvery { tbDao.getTbConfirmed(100L) } returns confirmed

        val result = repo.getTBConfirmed(100L)

        assertNotNull(result)
        assertEquals(confirmed, result)
    }

    @Test
    fun `getTBConfirmed returns null when not exists`() = runTest {
        coEvery { tbDao.getTbConfirmed(999L) } returns null

        val result = repo.getTBConfirmed(999L)

        assertNull(result)
    }

    // =====================================================
    // saveTBConfirmed() Tests
    // =====================================================

    @Test
    fun `saveTBConfirmed calls dao`() = runTest {
        val confirmed = mockk<TBConfirmedTreatmentCache>()
        coEvery { tbDao.saveTbConfirmed(confirmed) } returns Unit

        repo.saveTBConfirmed(confirmed)

        coVerify(exactly = 1) { tbDao.saveTbConfirmed(confirmed) }
    }

    // =====================================================
    // getAllFollowUpsForBeneficiary() Tests
    // =====================================================

    @Test
    fun `getAllFollowUps returns list when exists`() = runTest {
        val list = listOf(mockk<TBConfirmedTreatmentCache>(), mockk<TBConfirmedTreatmentCache>())
        coEvery { tbDao.getAllFollowUpsForBeneficiary(100L) } returns list

        val result = repo.getAllFollowUpsForBeneficiary(100L)

        assertEquals(2, result.size)
    }

    @Test
    fun `getAllFollowUps returns empty for unknown ben`() = runTest {
        coEvery { tbDao.getAllFollowUpsForBeneficiary(999L) } returns emptyList()

        val result = repo.getAllFollowUpsForBeneficiary(999L)

        assertTrue(result.isEmpty())
    }
}
