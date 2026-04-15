package org.piramalswasthya.sakhi.repositories

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseRepositoryTest
import org.piramalswasthya.sakhi.database.room.dao.ABHAGenratedDao
import org.piramalswasthya.sakhi.model.ABHAModel

@OptIn(ExperimentalCoroutinesApi::class)
class ABHAGenratedRepoTest : BaseRepositoryTest() {

    @MockK private lateinit var abhaGenratedDao: ABHAGenratedDao

    private lateinit var repo: ABHAGenratedRepo

    @Before
    override fun setUp() {
        super.setUp()
        repo = ABHAGenratedRepo(abhaGenratedDao)
    }

    // =====================================================
    // saveAbhaGenrated() Tests
    // =====================================================

    @Test
    fun `saveAbhaGenrated calls dao saveABHA`() = runTest {
        val abhaModel = mockk<ABHAModel>(relaxed = true)
        coEvery { abhaGenratedDao.saveABHA(abhaModel) } returns Unit
        repo.saveAbhaGenrated(abhaModel)
        coVerify { abhaGenratedDao.saveABHA(abhaModel) }
    }

    // =====================================================
    // deleteAbhaByBenId() Tests
    // =====================================================

    @Test
    fun `deleteAbhaByBenId calls dao deleteAbhaByBenId`() = runTest {
        coEvery { abhaGenratedDao.deleteAbhaByBenId(42L) } returns Unit
        repo.deleteAbhaByBenId(42L)
        coVerify { abhaGenratedDao.deleteAbhaByBenId(42L) }
    }
}
