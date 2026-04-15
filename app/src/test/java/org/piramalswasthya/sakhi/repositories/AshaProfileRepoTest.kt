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
import org.piramalswasthya.sakhi.database.room.dao.ProfileDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.ProfileActivityCache
import org.piramalswasthya.sakhi.network.AmritApiService

@OptIn(ExperimentalCoroutinesApi::class)
class AshaProfileRepoTest : BaseRepositoryTest() {

    @MockK private lateinit var amritApiService: AmritApiService
    @MockK private lateinit var profileDao: ProfileDao
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var userRepo: UserRepo

    private lateinit var repo: AshaProfileRepo

    @Before
    override fun setUp() {
        super.setUp()
        repo = AshaProfileRepo(amritApiService, profileDao, preferenceDao, userRepo)
    }

    // =====================================================
    // getSavedRecord() Tests
    // =====================================================

    @Test
    fun `getSavedRecord returns record when exists`() = runTest {
        val profile = mockk<ProfileActivityCache>()
        coEvery { profileDao.getProfileActivityById(100L) } returns profile

        val result = repo.getSavedRecord(100L)

        assertNotNull(result)
        assertEquals(profile, result)
    }

    @Test
    fun `getSavedRecord returns null when not exists`() = runTest {
        coEvery { profileDao.getProfileActivityById(999L) } returns null

        val result = repo.getSavedRecord(999L)

        assertNull(result)
    }

    // =====================================================
    // saveRecord() Tests
    // =====================================================

    @Test
    fun `saveRecord calls dao insert`() = runTest {
        val profile = mockk<ProfileActivityCache>()
        coEvery { profileDao.insert(profile) } returns Unit

        repo.saveRecord(profile)

        coVerify(exactly = 1) { profileDao.insert(profile) }
    }
}
