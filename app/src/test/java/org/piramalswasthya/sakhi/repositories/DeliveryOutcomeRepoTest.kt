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
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.room.dao.DeliveryOutcomeDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.model.DeliveryOutcomeCache
import org.piramalswasthya.sakhi.network.AmritApiService

@OptIn(ExperimentalCoroutinesApi::class)
class DeliveryOutcomeRepoTest : BaseRepositoryTest() {

    @MockK private lateinit var context: Application
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var amritApiService: AmritApiService
    @MockK private lateinit var userRepo: UserRepo
    @MockK private lateinit var benDao: BenDao
    @MockK private lateinit var deliveryOutcomeDao: DeliveryOutcomeDao

    private lateinit var repo: DeliveryOutcomeRepo

    @Before
    override fun setUp() {
        super.setUp()
        repo = DeliveryOutcomeRepo(context, preferenceDao, amritApiService, userRepo, benDao, deliveryOutcomeDao)
    }

    // =====================================================
    // getDeliveryOutcome() Tests
    // =====================================================

    @Test
    fun `getDeliveryOutcome returns record when exists`() = runTest {
        val outcome = mockk<DeliveryOutcomeCache>()
        coEvery { deliveryOutcomeDao.getDeliveryOutcome(100L) } returns outcome

        val result = repo.getDeliveryOutcome(100L)

        assertNotNull(result)
        assertEquals(outcome, result)
    }

    @Test
    fun `getDeliveryOutcome returns null when not exists`() = runTest {
        coEvery { deliveryOutcomeDao.getDeliveryOutcome(999L) } returns null

        val result = repo.getDeliveryOutcome(999L)

        assertNull(result)
    }

    // =====================================================
    // saveDeliveryOutcome() Tests
    // =====================================================

    @Test
    fun `saveDeliveryOutcome calls dao save`() = runTest {
        val outcome = mockk<DeliveryOutcomeCache>()
        coEvery { deliveryOutcomeDao.saveDeliveryOutcome(outcome) } returns Unit

        repo.saveDeliveryOutcome(outcome)

        coVerify(exactly = 1) { deliveryOutcomeDao.saveDeliveryOutcome(outcome) }
    }

    // =====================================================
    // processNewDeliveryOutcome() Tests
    // =====================================================

    @Test
    fun `processNewDeliveryOutcome throws when no user logged in`() = runTest {
        coEvery { preferenceDao.getLoggedInUser() } returns null

        try {
            repo.processNewDeliveryOutcome()
            assert(false) { "Should have thrown IllegalStateException" }
        } catch (e: IllegalStateException) {
            assertEquals("No user logged in!!", e.message)
        }
    }

    @Test
    fun `processNewDeliveryOutcome returns true when no unprocessed records`() = runTest {
        val user = mockk<org.piramalswasthya.sakhi.model.User>(relaxed = true)
        coEvery { preferenceDao.getLoggedInUser() } returns user
        coEvery { deliveryOutcomeDao.getAllUnprocessedDeliveryOutcomes() } returns emptyList()

        val result = repo.processNewDeliveryOutcome()

        assertEquals(true, result)
    }
}
