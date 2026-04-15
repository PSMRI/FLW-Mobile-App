package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pregnant_woment_anc_visits.homeVisit

import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.shared_preferences.ReferralStatusManager
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.InfantRegRepo
import org.piramalswasthya.sakhi.repositories.NcdReferalRepo
import org.piramalswasthya.sakhi.repositories.dynamicRepo.FormRepository

@OptIn(ExperimentalCoroutinesApi::class)
class AntenatalCounsellingViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var repository: FormRepository
    @MockK private lateinit var benRepo: BenRepo
    @MockK private lateinit var infantRegRepo: InfantRegRepo
    @MockK private lateinit var referalRepo: NcdReferalRepo
    @MockK private lateinit var referralStatusManager: ReferralStatusManager

    private lateinit var viewModel: AntenatalCounsellingViewModel

    @Before
    override fun setUp() {
        super.setUp()
        viewModel = AntenatalCounsellingViewModel(repository, benRepo, infantRegRepo, referalRepo, referralStatusManager)
    }

    // =====================================================
    // Initialization Tests
    // =====================================================

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `initial schema is null`() {
        assertNull(viewModel.schema.value)
    }

    @Test
    fun `initial visitCount is 0`() {
        assertEquals(0, viewModel.visitCount.value)
    }

    @Test
    fun `initial state is IDLE`() {
        assertEquals(AntenatalCounsellingViewModel.State.IDLE, viewModel.state.value)
    }

    @Test
    fun `initial isBenDead is false`() {
        assertFalse(viewModel.isBenDead.value)
    }

    @Test
    fun `initial isSNCU is false`() {
        assertFalse(viewModel.isSNCU.value)
    }

    @Test
    fun `initial visitDay is empty`() {
        assertEquals("", viewModel.visitDay)
    }

    // =====================================================
    // setMotherAge() Tests
    // =====================================================

    @Test
    fun `setMotherAge does not throw`() {
        viewModel.setMotherAge(25)
    }

    // =====================================================
    // calculateDueDate() Tests
    // =====================================================

    @Test
    fun `calculateDueDate returns null for unknown visit month`() {
        assertNull(viewModel.calculateDueDate(System.currentTimeMillis(), "Unknown"))
    }

    @Test
    fun `calculateDueDate returns value for valid visit month`() {
        val result = viewModel.calculateDueDate(System.currentTimeMillis(), "3 Months")
        assertNotNull(result)
    }

    // =====================================================
    // formatDate() Tests
    // =====================================================

    @Test
    fun `formatDate returns formatted string`() {
        val result = viewModel.formatDate(0L)
        assertNotNull(result)
    }

    // =====================================================
    // getBabyAgeMonths() Tests
    // =====================================================

    @Test
    fun `getBabyAgeMonths returns positive for past dob`() {
        val pastDob = System.currentTimeMillis() - (365L * 24 * 60 * 60 * 1000)
        val result = viewModel.getBabyAgeMonths(pastDob)
        assert(result >= 11)
    }

    // =====================================================
    // getVisibleFields() Tests
    // =====================================================

    @Test
    fun `getVisibleFields returns empty when no schema`() {
        assertEquals(0, viewModel.getVisibleFields().size)
    }

    // =====================================================
    // checkForReferralTriggers() Tests
    // =====================================================

    @Test
    fun `checkForReferralTriggers returns false for empty data`() {
        assertFalse(viewModel.checkForReferralTriggers(emptyMap()))
    }

    @Test
    fun `checkForReferralTriggers returns true when danger sign present`() {
        val data = mapOf("swelling" to "Yes")
        assert(viewModel.checkForReferralTriggers(data))
    }

    // =====================================================
    // getMinVisitDate() Tests
    // =====================================================

    @Test
    fun `getMinVisitDate returns null when no previous date`() {
        assertNull(viewModel.getMinVisitDate())
    }

    // =====================================================
    // updateFieldValue() Tests
    // =====================================================

    @Test
    fun `updateFieldValue does not throw when no schema`() {
        viewModel.updateFieldValue("test", "value")
    }
}
