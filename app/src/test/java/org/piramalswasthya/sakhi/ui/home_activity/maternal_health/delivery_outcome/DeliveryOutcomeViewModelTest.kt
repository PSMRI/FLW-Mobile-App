package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.delivery_outcome

import android.content.Context
import android.content.res.Resources
import android.net.Uri
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.LocationEntity
import org.piramalswasthya.sakhi.model.LocationRecord
import org.piramalswasthya.sakhi.model.PregnantWomanAncCache
import org.piramalswasthya.sakhi.model.PregnantWomanRegistrationCache
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.DeliveryOutcomeRepo
import org.piramalswasthya.sakhi.repositories.EcrRepo
import org.piramalswasthya.sakhi.repositories.MaternalHealthRepo
import org.piramalswasthya.sakhi.utils.HelperUtil

@OptIn(ExperimentalCoroutinesApi::class)
class DeliveryOutcomeViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var context: Context
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var mockResources: Resources
    @MockK private lateinit var deliveryOutcomeRepo: DeliveryOutcomeRepo
    @MockK private lateinit var ecrRepo: EcrRepo
    @MockK private lateinit var maternalHealthRepo: MaternalHealthRepo
    @MockK private lateinit var benRepo: BenRepo

    private lateinit var viewModel: DeliveryOutcomeViewModel
    private val savedStateHandle = SavedStateHandle(mapOf("benId" to 1L, "hhId" to 1L))

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
        every { mockResources.getStringArray(any()) } returns arrayOf("Yes", "No")
        every { mockResources.getString(any()) } returns ""
        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        every { preferenceDao.getLoggedInUser() } returns baseUser()
        coEvery { benRepo.getBenFromId(any()) } returns null
        coEvery { deliveryOutcomeRepo.getDeliveryOutcome(any()) } returns null
        coEvery { maternalHealthRepo.getLatestActiveRegistrationRecord(any()) } returns basePwr()
        coEvery { maternalHealthRepo.getLatestAncRecord(any()) } returns baseAnc()
        viewModel = DeliveryOutcomeViewModel(savedStateHandle, preferenceDao, context, deliveryOutcomeRepo, ecrRepo, maternalHealthRepo, benRepo)
    }

    @Test fun `viewModel initializes successfully`() { assertNotNull(viewModel) }
    @Test fun `initial state is IDLE`() { assertEquals(DeliveryOutcomeViewModel.State.IDLE, viewModel.state.value) }
    @Test fun `resetState sets state to IDLE`() { viewModel.resetState(); assertEquals(DeliveryOutcomeViewModel.State.IDLE, viewModel.state.value) }
    @Test fun `formList is not null`() { assertNotNull(viewModel.formList) }
    @Test fun `benId is set from SavedStateHandle`() { assertEquals(1L, viewModel.benId) }

    private fun baseUser() = User(
        userId = 1,
        name = "Asha One",
        userName = "asha1",
        password = "pwd",
        role = "ASHA",
        serviceMapId = 1,
        state = LocationEntity(10, "State"),
        district = LocationEntity(20, "District"),
        block = LocationEntity(30, "Block"),
        villages = emptyList()
    )

    private fun baseBen() = BenRegCache(
        householdId = 1L,
        beneficiaryId = 1L,
        isDeath = false,
        reasonOfDeathId = 0,
        placeOfDeathId = 0,
        ashaId = 1,
        isKid = false,
        isAdult = true,
        locationRecord = LocationRecord(
            country = LocationEntity(1, "India"),
            state = LocationEntity(10, "State"),
            district = LocationEntity(20, "District"),
            block = LocationEntity(30, "Block"),
            village = LocationEntity(40, "Village")
        ),
        syncState = SyncState.UNSYNCED,
        isDraft = false
    ).apply {
        firstName = "Jane"
        lastName = "Doe"
        age = 28
    }

    private fun basePwr() = PregnantWomanRegistrationCache(
        benId = 1L,
        createdBy = "asha1",
        updatedBy = "asha1",
        syncState = SyncState.UNSYNCED
    )

    private fun baseAnc() = PregnantWomanAncCache(
        benId = 1L,
        visitNumber = 1,
        createdBy = "asha1",
        updatedBy = "asha1",
        syncState = SyncState.UNSYNCED,
        frontFilePath = null,
        backFilePath = null
    )

    private fun buildViewModel(): DeliveryOutcomeViewModel =
        DeliveryOutcomeViewModel(savedStateHandle, preferenceDao, context, deliveryOutcomeRepo, ecrRepo, maternalHealthRepo, benRepo)

    @Test
    fun `getIndexOfMCP1 and getIndexOfMCP2 return ints without crashing`() {
        assertNotNull(viewModel.getIndexOfMCP1())
        assertNotNull(viewModel.getIndexOfMCP2())
    }

    @Test
    fun `getIndexOfIsjsyFileUpload returns an int without crashing`() {
        assertNotNull(viewModel.getIndexOfIsjsyFileUpload())
    }

    @Test
    fun `setCurrentDocumentFormId then getDocumentFormId returns same id`() {
        viewModel.setCurrentDocumentFormId(7)
        assertEquals(7, viewModel.getDocumentFormId())
    }

    @Test
    fun `setImageUriToFormElement does not crash`() {
        viewModel.setCurrentDocumentFormId(1)
        val uri = mockk<Uri>(relaxed = true)
        viewModel.setImageUriToFormElement(uri)
        assertNotNull(viewModel)
    }

    @Test
    fun `updateListOnValueChanged does not crash`() {
        viewModel.updateListOnValueChanged(1, 0)
        testDispatcher.scheduler.advanceUntilIdle()
        assertNotNull(viewModel.formList)
    }

    @Test
    fun `saveForm fails when deliveryOutcome was never initialized`() {
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.saveForm()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(DeliveryOutcomeViewModel.State.SAVE_FAILED, viewModel.state.value)
    }

    @Test
    fun `saveForm succeeds when ben exists and no death complication`() {
        every { mockResources.getStringArray(any()) } returns arrayOf("Yes", "No")
        coEvery { benRepo.getBenFromId(any()) } returns baseBen()
        coEvery { deliveryOutcomeRepo.getDeliveryOutcome(any()) } returns null
        coEvery { deliveryOutcomeRepo.saveDeliveryOutcome(any()) } returns Unit
        coEvery { ecrRepo.getSavedRecord(any()) } returns null
        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        every { mockResources.getStringArray(any()) } returns arrayOf("Yes", "No")
        vm.saveForm()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(DeliveryOutcomeViewModel.State.SAVE_SUCCESS(false), vm.state.value)
    }

    @Test
    fun `recordExists is true when deliveryOutcomeRepo returns an existing record`() {
        coEvery { benRepo.getBenFromId(any()) } returns baseBen()
        coEvery { deliveryOutcomeRepo.getDeliveryOutcome(any()) } returns
            org.piramalswasthya.sakhi.model.DeliveryOutcomeCache(
                benId = 1L,
                isActive = true,
                createdBy = "asha1",
                updatedBy = "asha1",
                syncState = SyncState.UNSYNCED
            )
        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(true, vm.recordExists.value)
    }
}
