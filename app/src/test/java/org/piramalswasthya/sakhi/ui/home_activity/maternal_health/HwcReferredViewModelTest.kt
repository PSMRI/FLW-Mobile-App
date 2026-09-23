package org.piramalswasthya.sakhi.ui.home_activity.maternal_health

import android.content.Context
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.EcFilterType
import org.piramalswasthya.sakhi.model.BenBasicDomain
import org.piramalswasthya.sakhi.model.BenWithCbacAndReferalCache
import org.piramalswasthya.sakhi.model.BenWithCbacReferDomain
import org.piramalswasthya.sakhi.model.ReferalCache
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import org.piramalswasthya.sakhi.ui.home_activity.maternal_health.hwc.list.HwcReferredViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class HwcReferredViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var recordsRepo: RecordsRepo
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var context: Context

    private lateinit var viewModel: HwcReferredViewModel

    @Before
    override fun setUp() {
        super.setUp()
        val user = mockk<User>(relaxed = true)
        every { user.userId } returns 789
        every { user.name } returns "ASHA Worker"
        every { preferenceDao.getLoggedInUser() } returns user
        every { recordsRepo.getHwcRefferedList } returns flowOf(emptyList())
        viewModel = HwcReferredViewModel(recordsRepo, preferenceDao, context)
    }

    // =====================================================
    // Initialization Tests
    // =====================================================

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `benList flow is not null`() {
        assertNotNull(viewModel.benList)
    }

    @Test
    fun `userName is set from preferences`() {
        assertEquals("ASHA Worker", viewModel.userName)
    }

    // =====================================================
    // filterText() Tests
    // =====================================================

    @Test
    fun `filterText does not throw`() = runTest {
        viewModel.filterText("test")
        advanceUntilIdle()
    }

    @Test
    fun `filterText with empty string does not throw`() = runTest {
        viewModel.filterText("")
        advanceUntilIdle()
    }

    // =====================================================
    // getAshaId() Tests
    // =====================================================

    @Test
    fun `getAshaId returns user id`() = runTest {
        advanceUntilIdle()
        assertEquals(789, viewModel.getAshaId())
    }

    // =====================================================
    // setSortFilter() / getCurrentSort() Tests
    // =====================================================

    @Test
    fun `getCurrentSort returns default NEWEST_FIRST`() {
        assertEquals(EcFilterType.NEWEST_FIRST, viewModel.getCurrentSort())
    }

    @Test
    fun `setSortFilter updates current sort`() = runTest {
        viewModel.setSortFilter(EcFilterType.OLDEST_FIRST)
        advanceUntilIdle()
        assertEquals(EcFilterType.OLDEST_FIRST, viewModel.getCurrentSort())
    }

    // =====================================================
    // benList emission Tests
    // =====================================================

    private fun maternalReferralItem(benId: Long): BenWithCbacAndReferalCache {
        val referral = mockk<ReferalCache>(relaxed = true)
        every { referral.type } returns "MATERNAL"
        val benDomain = mockk<BenBasicDomain>(relaxed = true)
        every { benDomain.benId } returns benId
        val domainModel = mockk<BenWithCbacReferDomain>(relaxed = true)
        every { domainModel.ben } returns benDomain
        every { domainModel.referalCac } returns referral
        val item = mockk<BenWithCbacAndReferalCache>(relaxed = true)
        every { item.referral } returns referral
        every { item.asDomainModel() } returns domainModel
        return item
    }

    @Test
    fun `benList includes only MATERNAL referrals`() = runTest {
        val maternal = maternalReferralItem(1L)
        val nonMaternal = mockk<BenWithCbacAndReferalCache>(relaxed = true)
        val nonMaternalReferral = mockk<ReferalCache>(relaxed = true)
        every { nonMaternalReferral.type } returns "NCD"
        every { nonMaternal.referral } returns nonMaternalReferral
        every { recordsRepo.getHwcRefferedList } returns flowOf(listOf(maternal, nonMaternal))
        val vm = HwcReferredViewModel(recordsRepo, preferenceDao, context)
        val list = vm.benList.first()
        assertEquals(1, list.size)
    }

    @Test
    fun `benList is empty when no maternal referrals exist`() = runTest {
        every { recordsRepo.getHwcRefferedList } returns flowOf(emptyList())
        val vm = HwcReferredViewModel(recordsRepo, preferenceDao, context)
        val list = vm.benList.first()
        assertTrue(list.isEmpty())
    }
}
