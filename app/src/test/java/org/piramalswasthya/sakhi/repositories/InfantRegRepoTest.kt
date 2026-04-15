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
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.InfantRegDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.InfantRegCache
import org.piramalswasthya.sakhi.network.AmritApiService

@OptIn(ExperimentalCoroutinesApi::class)
class InfantRegRepoTest : BaseRepositoryTest() {

    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var amritApiService: AmritApiService
    @MockK private lateinit var userRepo: UserRepo
    @MockK private lateinit var benDao: BenDao
    @MockK private lateinit var infantRegDao: InfantRegDao

    private lateinit var repo: InfantRegRepo

    @Before
    override fun setUp() {
        super.setUp()
        repo = InfantRegRepo(preferenceDao, amritApiService, userRepo, benDao, infantRegDao)
    }

    // =====================================================
    // getInfantReg() Tests
    // =====================================================

    @Test
    fun `getInfantReg returns record when exists`() = runTest {
        val infant = mockk<InfantRegCache>()
        coEvery { infantRegDao.getInfantReg(100L, 1) } returns infant

        val result = repo.getInfantReg(100L, 1)

        assertNotNull(result)
        assertEquals(infant, result)
    }

    @Test
    fun `getInfantReg returns null when not exists`() = runTest {
        coEvery { infantRegDao.getInfantReg(999L, 1) } returns null

        val result = repo.getInfantReg(999L, 1)

        assertNull(result)
    }

    @Test
    fun `getInfantReg passes correct baby index`() = runTest {
        coEvery { infantRegDao.getInfantReg(any(), any()) } returns null

        repo.getInfantReg(100L, 3)

        coVerify { infantRegDao.getInfantReg(100L, 3) }
    }

    // =====================================================
    // getInfantRegFromChildBenId() Tests
    // =====================================================

    @Test
    fun `getInfantRegFromChildBenId returns record when exists`() = runTest {
        val infant = mockk<InfantRegCache>()
        coEvery { infantRegDao.getInfantRegFromChildBenId(200L) } returns infant

        val result = repo.getInfantRegFromChildBenId(200L)

        assertNotNull(result)
        assertEquals(infant, result)
    }

    @Test
    fun `getInfantRegFromChildBenId returns null when not exists`() = runTest {
        coEvery { infantRegDao.getInfantRegFromChildBenId(999L) } returns null

        val result = repo.getInfantRegFromChildBenId(999L)

        assertNull(result)
    }

    // =====================================================
    // saveInfantReg() Tests
    // =====================================================

    @Test
    fun `saveInfantReg calls dao save`() = runTest {
        val infant = mockk<InfantRegCache>()
        coEvery { infantRegDao.saveInfantReg(infant) } returns Unit

        repo.saveInfantReg(infant)

        coVerify(exactly = 1) { infantRegDao.saveInfantReg(infant) }
    }

    // =====================================================
    // processNewInfantRegister() Tests
    // =====================================================

    @Test
    fun `processNewInfantRegister throws when no user logged in`() = runTest {
        coEvery { preferenceDao.getLoggedInUser() } returns null

        try {
            repo.processNewInfantRegister()
            assert(false) { "Should have thrown IllegalStateException" }
        } catch (e: IllegalStateException) {
            assertEquals("No user logged in!!", e.message)
        }
    }

    @Test
    fun `processNewInfantRegister returns true when no unprocessed records`() = runTest {
        val user = mockk<org.piramalswasthya.sakhi.model.User>(relaxed = true)
        coEvery { preferenceDao.getLoggedInUser() } returns user
        coEvery { infantRegDao.getAllUnprocessedInfantReg() } returns emptyList()

        val result = repo.processNewInfantRegister()

        assertEquals(true, result)
    }
}
