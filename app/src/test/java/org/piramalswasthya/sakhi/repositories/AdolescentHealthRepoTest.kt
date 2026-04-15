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
import org.piramalswasthya.sakhi.database.room.dao.AdolescentHealthDao
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.AdolescentHealthCache
import org.piramalswasthya.sakhi.network.AmritApiService

@OptIn(ExperimentalCoroutinesApi::class)
class AdolescentHealthRepoTest : BaseRepositoryTest() {

    @MockK private lateinit var adolescentHealthDao: AdolescentHealthDao
    @MockK private lateinit var benDao: BenDao
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var userRepo: UserRepo
    @MockK private lateinit var tmcNetworkApiService: AmritApiService

    private lateinit var repo: AdolescentHealthRepo

    @Before
    override fun setUp() {
        super.setUp()
        repo = AdolescentHealthRepo(adolescentHealthDao, benDao, preferenceDao, userRepo, tmcNetworkApiService)
    }

    // =====================================================
    // getAdolescentHealth() Tests
    // =====================================================

    @Test
    fun `getAdolescentHealth returns record when exists`() = runTest {
        val record = mockk<AdolescentHealthCache>()
        coEvery { adolescentHealthDao.getAdolescentHealth(100L) } returns record

        val result = repo.getAdolescentHealth(100L)

        assertNotNull(result)
        assertEquals(record, result)
    }

    @Test
    fun `getAdolescentHealth returns null when not exists`() = runTest {
        coEvery { adolescentHealthDao.getAdolescentHealth(999L) } returns null

        val result = repo.getAdolescentHealth(999L)

        assertNull(result)
    }

    // =====================================================
    // saveAdolescentHealth() Tests
    // =====================================================

    @Test
    fun `saveAdolescentHealth calls dao save`() = runTest {
        val record = mockk<AdolescentHealthCache>()
        coEvery { adolescentHealthDao.saveAdolescentHealth(record) } returns Unit

        repo.saveAdolescentHealth(record)

        coVerify(exactly = 1) { adolescentHealthDao.saveAdolescentHealth(record) }
    }
}
