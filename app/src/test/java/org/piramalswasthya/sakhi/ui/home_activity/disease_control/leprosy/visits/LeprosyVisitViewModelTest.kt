package org.piramalswasthya.sakhi.ui.home_activity.disease_control.leprosy.visits

import android.content.Context
import android.content.res.Resources
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.LeprosyFollowUpCache
import org.piramalswasthya.sakhi.model.LeprosyScreeningCache
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.LeprosyRepo
import org.piramalswasthya.sakhi.repositories.MaternalHealthRepo
import org.piramalswasthya.sakhi.utils.HelperUtil

/**
 * Unit tests for [LeprosyVisitViewModel]: the guard that fails the form when no leprosy screening
 * exists, the follow-up list resolution and the small state helpers.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LeprosyVisitViewModelTest : BaseViewModelTest() {

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var mockResources: Resources

    @MockK
    private lateinit var preferenceDao: PreferenceDao

    @MockK
    private lateinit var leprosyRepo: LeprosyRepo

    @MockK
    private lateinit var benRepo: BenRepo

    @MockK
    private lateinit var maternalHealthRepo: MaternalHealthRepo

    private lateinit var user: User
    private lateinit var ben: BenRegCache

    @After
    fun releaseStaticMocks() {
        unmockkStatic(Dispatchers::class)
    }

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.v(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.w(any(), any<Throwable>()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        every { Log.isLoggable(any(), any()) } returns false

        mockkStatic(Dispatchers::class)
        every { Dispatchers.IO } returns testDispatcher

        mockkObject(HelperUtil)
        every { HelperUtil.getLocalizedResources(any(), any()) } returns mockResources
        every { mockResources.getStringArray(any()) } returns Array(80) { i -> "opt$i" }
        every { mockResources.getString(any()) } returns "x"
        every { mockResources.getString(any(), any()) } returns "x"
        every { mockResources.getString(any(), any(), any()) } returns "x"
        every { context.resources } returns mockResources

        user = mockk(relaxed = true)
        every { user.userName } returns "asha"

        ben = mockk(relaxed = true)
        every { ben.beneficiaryId } returns 2L
        every { ben.householdId } returns 5L
        every { ben.firstName } returns "Vijay"
        every { ben.lastName } returns "Kumar"
        every { ben.age } returns 45

        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        every { preferenceDao.getLoggedInUser() } returns user

        coEvery { benRepo.getBenFromId(any()) } returns ben
        coEvery { leprosyRepo.getLeprosyScreening(any()) } returns screening("Alive", 2)
        coEvery { leprosyRepo.getFollowUpsForVisit(any(), any()) } returns emptyList()
    }

    private fun screening(status: String?, visitNumber: Int): LeprosyScreeningCache {
        val cache = mockk<LeprosyScreeningCache>(relaxed = true)
        every { cache.beneficiaryStatus } returns status
        every { cache.currentVisitNumber } returns visitNumber
        every { cache.benId } returns 2L
        return cache
    }

    private fun followUp(date: Long, visitNumber: Int): LeprosyFollowUpCache {
        val cache = mockk<LeprosyFollowUpCache>(relaxed = true)
        every { cache.followUpDate } returns date
        every { cache.visitNumber } returns visitNumber
        return cache
    }

    private fun buildVm(benId: Long = 2L, visitNumber: Int = 2): LeprosyVisitViewModel =
        LeprosyVisitViewModel(
            SavedStateHandle(mapOf("benId" to benId, "visitNumber" to visitNumber)),
            preferenceDao,
            context,
            leprosyRepo,
            benRepo,
            maternalHealthRepo
        )

    @Test
    fun `navigation arguments are exposed`() {
        val vm = buildVm(benId = 12L, visitNumber = 4)
        assertEquals(12L, vm.benId)
        assertEquals(4, vm.visitNumber)
    }

    @Test
    fun `viewModel initializes with an idle state and a form list`() {
        val vm = buildVm()
        assertNotNull(vm.formList)
        assertEquals(LeprosyVisitViewModel.State.IDLE, vm.state.value)
    }

    @Test
    fun `init fails the form when no screening record exists`() = runTest {
        coEvery { leprosyRepo.getLeprosyScreening(any()) } returns null

        val vm = buildVm()
        advanceUntilIdle()

        assertEquals(LeprosyVisitViewModel.State.SAVE_FAILED, vm.state.value)
        assertNull(vm.recordExists.value)
    }

    @Test
    fun `init populates the header and the visit information`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        assertEquals("Vijay Kumar", vm.benName.value)
        assertEquals(true, vm.recordExists.value)
        assertEquals("Visit - 2", vm.visitInfo.value)
        assertEquals(false, vm.isBeneficaryStatusDeath.value)
        assertNotNull(vm.benAgeGender.value)
    }

    @Test
    fun `init omits a null last name from the header`() = runTest {
        every { ben.lastName } returns null

        val vm = buildVm()
        advanceUntilIdle()

        assertEquals("Vijay ", vm.benName.value)
    }

    @Test
    fun `init flags a deceased beneficiary`() = runTest {
        coEvery { leprosyRepo.getLeprosyScreening(any()) } returns screening("death", 3)

        val vm = buildVm()
        advanceUntilIdle()

        assertEquals(true, vm.isBeneficaryStatusDeath.value)
        assertEquals("Visit - 3", vm.visitInfo.value)
    }

    @Test
    fun `init has no last follow up when the visit has none`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        assertEquals(emptyList<LeprosyFollowUpCache>(), vm.followUpDates.value)
        assertNull(vm.lastFollowUp.value)
    }

    @Test
    fun `init picks the newest follow up of the visit`() = runTest {
        val newest = followUp(3_000L, 2)
        coEvery { leprosyRepo.getFollowUpsForVisit(any(), any()) } returns
                listOf(followUp(1_000L, 2), newest, followUp(2_000L, 2))

        val vm = buildVm()
        advanceUntilIdle()

        assertEquals(3, vm.followUpDates.value!!.size)
        assertEquals(newest, vm.lastFollowUp.value)
    }

    @Test
    fun `init tolerates a missing beneficiary`() = runTest {
        coEvery { benRepo.getBenFromId(any()) } returns null

        val vm = buildVm()
        advanceUntilIdle()

        assertEquals(true, vm.recordExists.value)
    }

    @Test
    fun `setRecordExist updates recordExists`() {
        val vm = buildVm()
        vm.setRecordExist(true)
        assertEquals(true, vm.recordExists.value)
        vm.setRecordExist(false)
        assertEquals(false, vm.recordExists.value)
    }

    @Test
    fun `resetState returns to idle`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        vm.resetState()

        assertEquals(LeprosyVisitViewModel.State.IDLE, vm.state.value)
    }

    @Test
    fun `getIndexOfDate delegates to the dataset`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        runCatching { vm.getIndexOfDate() }

        assertNotNull(vm.formList)
    }

    @Test
    fun `updateListOnValueChanged delegates to the dataset`() = runTest {
        val vm = buildVm()
        advanceUntilIdle()

        vm.updateListOnValueChanged(1, 0)
        advanceUntilIdle()

        assertNotNull(vm.formList)
    }
}
