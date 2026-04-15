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
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.room.dao.ChildRegistrationDao
import org.piramalswasthya.sakhi.database.room.dao.DeliveryOutcomeDao
import org.piramalswasthya.sakhi.database.room.dao.InfantRegDao
import org.piramalswasthya.sakhi.model.ChildRegCache
import org.piramalswasthya.sakhi.model.DeliveryOutcomeCache
import org.piramalswasthya.sakhi.model.InfantRegCache

@OptIn(ExperimentalCoroutinesApi::class)
class ChildRegRepoTest : BaseRepositoryTest() {

    @MockK private lateinit var childRegDao: ChildRegistrationDao
    @MockK private lateinit var deliveryOutcomeDao: DeliveryOutcomeDao
    @MockK private lateinit var infantRegDao: InfantRegDao

    private lateinit var repo: ChildRegRepo

    @Before
    override fun setUp() {
        super.setUp()
        repo = ChildRegRepo(childRegDao, deliveryOutcomeDao, infantRegDao)
    }

    // =====================================================
    // getChildReg() Tests
    // =====================================================

    @Test
    fun `getChildReg returns record when exists`() = runTest {
        val child = mockk<ChildRegCache>()
        coEvery { childRegDao.getInfantReg(100L) } returns child

        val result = repo.getChildReg(100L)

        assertNotNull(result)
        assertEquals(child, result)
    }

    @Test
    fun `getChildReg returns null when not exists`() = runTest {
        coEvery { childRegDao.getInfantReg(999L) } returns null

        val result = repo.getChildReg(999L)

        assertNull(result)
    }

    // =====================================================
    // saveChildReg() Tests
    // =====================================================

    @Test
    fun `saveChildReg calls dao save`() = runTest {
        val child = mockk<ChildRegCache>()
        coEvery { childRegDao.saveInfantReg(child) } returns Unit

        repo.saveChildReg(child)

        coVerify(exactly = 1) { childRegDao.saveInfantReg(child) }
    }

    // =====================================================
    // getDeliveryOutcomeRepoFromMotherBenId() Tests
    // =====================================================

    @Test
    fun `getDeliveryOutcome returns record when exists`() = runTest {
        val outcome = mockk<DeliveryOutcomeCache>()
        coEvery { deliveryOutcomeDao.getDeliveryOutcome(200L) } returns outcome

        val result = repo.getDeliveryOutcomeRepoFromMotherBenId(200L)

        assertNotNull(result)
        assertEquals(outcome, result)
    }

    @Test
    fun `getDeliveryOutcome returns null for unknown mother`() = runTest {
        coEvery { deliveryOutcomeDao.getDeliveryOutcome(999L) } returns null

        val result = repo.getDeliveryOutcomeRepoFromMotherBenId(999L)

        assertNull(result)
    }

    // =====================================================
    // getInfantRegFromMotherBenId() Tests
    // =====================================================

    @Test
    fun `getInfantReg returns record for valid mother and baby index`() = runTest {
        val infant = mockk<InfantRegCache>()
        coEvery { infantRegDao.getInfantReg(300L, 1) } returns infant

        val result = repo.getInfantRegFromMotherBenId(300L, 1)

        assertNotNull(result)
        assertEquals(infant, result)
    }

    @Test
    fun `getInfantReg returns null for unknown mother`() = runTest {
        coEvery { infantRegDao.getInfantReg(999L, 1) } returns null

        val result = repo.getInfantRegFromMotherBenId(999L, 1)

        assertNull(result)
    }

    @Test
    fun `getInfantReg returns null for invalid baby index`() = runTest {
        coEvery { infantRegDao.getInfantReg(300L, 99) } returns null

        val result = repo.getInfantRegFromMotherBenId(300L, 99)

        assertNull(result)
    }

    @Test
    fun `getInfantReg passes correct params to dao`() = runTest {
        coEvery { infantRegDao.getInfantReg(any(), any()) } returns null

        repo.getInfantRegFromMotherBenId(500L, 3)

        coVerify { infantRegDao.getInfantReg(500L, 3) }
    }
}
