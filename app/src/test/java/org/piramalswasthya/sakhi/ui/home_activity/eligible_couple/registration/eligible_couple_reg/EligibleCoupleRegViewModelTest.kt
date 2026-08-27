package org.piramalswasthya.sakhi.ui.home_activity.eligible_couple.registration.eligible_couple_reg

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.EligibleCoupleRegCache
import org.piramalswasthya.sakhi.model.HRPNonPregnantAssessCache
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.EcrRepo
import org.piramalswasthya.sakhi.repositories.HRPRepo
import org.piramalswasthya.sakhi.utils.HelperUtil

@OptIn(ExperimentalCoroutinesApi::class)
class EligibleCoupleRegViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var context: Context
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var mockResources: Resources
    @MockK private lateinit var ecrRepo: EcrRepo
    @MockK private lateinit var hrpRepo: HRPRepo
    @MockK private lateinit var benRepo: BenRepo

    private lateinit var viewModel: EligibleCoupleRegViewModel
    private val savedStateHandle = SavedStateHandle(mapOf("benId" to 1L))

    private fun mockBen(): BenRegCache {
        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.beneficiaryId } returns 1L
        every { ben.householdId } returns 2L
        every { ben.firstName } returns "Meena"
        every { ben.lastName } returns "Devi"
        every { ben.dob } returns System.currentTimeMillis() - 1000L
        every { ben.ageUnit } returns null
        every { ben.gender } returns null
        every { ben.genDetails } returns null
        return ben
    }

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.isLoggable(any(), any()) } returns false
        mockkStatic(Dispatchers::class)
        every { Dispatchers.IO } returns testDispatcher
        mockkObject(HelperUtil)
        every { HelperUtil.getLocalizedResources(any(), any()) } returns mockResources
        every { context.resources } returns mockResources
        every { mockResources.getStringArray(any()) } returns Array(80) { i -> "opt$i" }
        every { mockResources.getString(any()) } returns ""
        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        every { preferenceDao.getLoggedInUser() } returns mockk(relaxed = true)
        coEvery { benRepo.getBenFromId(any()) } returns null
        coEvery { ecrRepo.getBenFromId(any()) } returns null
        coEvery { hrpRepo.getNonPregnantAssess(any()) } returns null
        coEvery { ecrRepo.getSavedRecord(any()) } returns null
        coEvery { benRepo.getChildBenListFromHousehold(any(), any(), any()) } returns emptyList()
        coEvery { benRepo.getChildBelow15(any(), any(), any()) } returns 0
        coEvery { benRepo.getChildAbove15(any(), any(), any()) } returns 0
        viewModel = EligibleCoupleRegViewModel(savedStateHandle, preferenceDao, context, ecrRepo, hrpRepo, benRepo)
    }

    @Test fun `viewModel initializes successfully`() { assertNotNull(viewModel) }
    @Test fun `initial state is IDLE`() { assertEquals(EligibleCoupleRegViewModel.State.IDLE, viewModel.state.value) }
    @Test fun `setRecordExist updates recordExists`() { viewModel.setRecordExist(true); assertEquals(true, viewModel.recordExists.value) }
    @Test fun `formList is not null`() { assertNotNull(viewModel.formList) }

    @Test
    fun `init tolerates missing beneficiary and marks record as not existing`() = runTest {
        advanceUntilIdle()
        assertEquals(false, viewModel.recordExists.value)
        assertEquals(false, viewModel.isEcrCompleted.value)
    }

    @Test
    fun `init populates ben name and age gender when beneficiary found`() = runTest {
        val ben = mockBen()
        coEvery { ecrRepo.getBenFromId(any()) } returns ben
        coEvery { hrpRepo.getNonPregnantAssess(any()) } returns null
        coEvery { ecrRepo.getSavedRecord(any()) } returns null
        viewModel = EligibleCoupleRegViewModel(savedStateHandle, preferenceDao, context, ecrRepo, hrpRepo, benRepo)

        advanceUntilIdle()

        assertEquals("Meena Devi", viewModel.benName.value)
        assertNotNull(viewModel.benAgeGender.value)
        assertEquals(false, viewModel.recordExists.value)
    }

    @Test
    fun `init loads existing saved record and marks record exists`() = runTest {
        val ben = mockBen()
        val saved = mockk<EligibleCoupleRegCache>(relaxed = true)
        every { saved.lmpDate } returns 12345L
        coEvery { ecrRepo.getBenFromId(any()) } returns ben
        coEvery { hrpRepo.getNonPregnantAssess(any()) } returns null
        coEvery { ecrRepo.getSavedRecord(any()) } returns saved
        viewModel = EligibleCoupleRegViewModel(savedStateHandle, preferenceDao, context, ecrRepo, hrpRepo, benRepo)

        advanceUntilIdle()

        assertEquals(true, viewModel.recordExists.value)
        assertEquals(true, viewModel.isEcrCompleted.value)
    }

    @Test
    fun `init counts children from household capped at nine`() = runTest {
        val ben = mockBen()
        coEvery { ecrRepo.getBenFromId(any()) } returns ben
        coEvery { hrpRepo.getNonPregnantAssess(any()) } returns null
        coEvery { ecrRepo.getSavedRecord(any()) } returns null
        coEvery { benRepo.getChildBenListFromHousehold(any(), any(), any()) } returns
                List(12) { mockk<BenRegCache>(relaxed = true) }
        coEvery { benRepo.getChildBelow15(any(), any(), any()) } returns 15
        coEvery { benRepo.getChildAbove15(any(), any(), any()) } returns 20
        viewModel = EligibleCoupleRegViewModel(savedStateHandle, preferenceDao, context, ecrRepo, hrpRepo, benRepo)

        advanceUntilIdle()

        assertEquals(9, viewModel.childCount.value)
        assertEquals(9, viewModel.childBelow15Count.value)
        assertEquals(9, viewModel.childAbove15Count.value)
    }

    @Test
    fun `saveForm fails when ecr form was never initialized`() = runTest {
        viewModel.saveForm()
        advanceUntilIdle()
        assertEquals(EligibleCoupleRegViewModel.State.SAVE_FAILED, viewModel.state.value)
    }

    @Test
    fun `saveForm succeeds when beneficiary exists and assess is created`() = runTest {
        val ben = mockBen()
        coEvery { ecrRepo.getBenFromId(any()) } returns ben
        coEvery { hrpRepo.getNonPregnantAssess(any()) } returns null
        coEvery { ecrRepo.getSavedRecord(any()) } returns null
        viewModel = EligibleCoupleRegViewModel(savedStateHandle, preferenceDao, context, ecrRepo, hrpRepo, benRepo)
        advanceUntilIdle()

        viewModel.saveForm()
        advanceUntilIdle()

        assertEquals(EligibleCoupleRegViewModel.State.SAVE_SUCCESS, viewModel.state.value)
    }

    @Test
    fun `saveForm reuses existing assess record when present`() = runTest {
        val ben = mockBen()
        val assess = mockk<HRPNonPregnantAssessCache>(relaxed = true)
        coEvery { ecrRepo.getBenFromId(any()) } returns ben
        coEvery { hrpRepo.getNonPregnantAssess(any()) } returns assess
        coEvery { ecrRepo.getSavedRecord(any()) } returns null
        viewModel = EligibleCoupleRegViewModel(savedStateHandle, preferenceDao, context, ecrRepo, hrpRepo, benRepo)
        advanceUntilIdle()

        viewModel.saveForm()
        advanceUntilIdle()

        assertEquals(EligibleCoupleRegViewModel.State.SAVE_SUCCESS, viewModel.state.value)
    }

    @Test
    fun `updateListOnValueChanged does not throw for unknown form id`() = runTest {
        viewModel.updateListOnValueChanged(-999, 0)
        advanceUntilIdle()
        assertNotNull(viewModel.formList)
    }

    @Test
    fun `getIndexofAshaKitPhotoFirst and Second return dataset indices`() {
        assertNotNull(viewModel.getIndexofAshaKitPhotoFirst())
        assertNotNull(viewModel.getIndexofAshaKitPhotoSecond())
    }

    @Test
    fun `setCurrentDocumentFormId and getDocumentFormId round trip`() {
        viewModel.setCurrentDocumentFormId(42)
        assertEquals(42, viewModel.getDocumentFormId())
    }

    @Test
    fun `getIndexOf children and gap delegators return values`() {
        assertNotNull(viewModel.getIndexOfChildren())
        assertNotNull(viewModel.getIndexOfLiveChildren())
        assertNotNull(viewModel.getIndexOfMaleChildren())
        assertNotNull(viewModel.getIndexOfFeMaleChildren())
        assertNotNull(viewModel.getIndexOfAge1())
        assertNotNull(viewModel.getIndexOfGap1())
        assertNotNull(viewModel.getIndexOfAge2())
        assertNotNull(viewModel.getIndexOfGap2())
        assertNotNull(viewModel.getIndexOfAge3())
        assertNotNull(viewModel.getIndexOfGap3())
        assertNotNull(viewModel.getIndexOfAge4())
        assertNotNull(viewModel.getIndexOfGap4())
        assertNotNull(viewModel.getIndexOfAge5())
        assertNotNull(viewModel.getIndexOfGap5())
        assertNotNull(viewModel.getIndexOfAge6())
        assertNotNull(viewModel.getIndexOfGap6())
        assertNotNull(viewModel.getIndexOfAge7())
        assertNotNull(viewModel.getIndexOfGap7())
        assertNotNull(viewModel.getIndexOfAge8())
        assertNotNull(viewModel.getIndexOfGap8())
        assertNotNull(viewModel.getIndexOfAge9())
        assertNotNull(viewModel.getIndexOfGap9())
        assertNotNull(viewModel.getIndexOfTimeLessThan18())
        assertNotNull(viewModel.getIndexOfChildLabel())
        assertNotNull(viewModel.getIndexOfPhysicalObservationLabel())
        assertNotNull(viewModel.getIndexOfObstetricHistoryLabel())
    }

    @Test
    fun `setRecordExist can toggle back to false`() {
        viewModel.setRecordExist(true)
        assertTrue(viewModel.recordExists.value!!)
        viewModel.setRecordExist(false)
        assertFalse(viewModel.recordExists.value!!)
    }
}
