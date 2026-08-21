package org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.ncd_referred.form

import android.content.Context
import android.content.res.Resources
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.Gender
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.repositories.CbacRepo
import org.piramalswasthya.sakhi.repositories.NcdReferalRepo
import org.piramalswasthya.sakhi.utils.HelperUtil

@OptIn(ExperimentalCoroutinesApi::class)
class NCDReferDialogViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var cbacRepo: CbacRepo
    @MockK private lateinit var benDao: BenDao
    @MockK private lateinit var referalRepo: NcdReferalRepo
    @MockK private lateinit var context: Context
    @MockK private lateinit var mockResources: Resources

    private lateinit var viewModel: NCDReferDialogViewModel

    @Before
    override fun setUp() {
        super.setUp()
        mockkObject(HelperUtil)
        every { HelperUtil.getLocalizedResources(any(), any()) } returns mockResources
        every { mockResources.getStringArray(any()) } returns arrayOf("Yes", "No")
        every { mockResources.getString(any()) } returns ""
        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        mockkStatic(Dispatchers::class)
        every { Dispatchers.IO } returns testDispatcher
        viewModel = NCDReferDialogViewModel(preferenceDao, cbacRepo, benDao, referalRepo, context)
    }

    private fun buildBen(): BenRegCache {
        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.firstName } returns "Anita"
        every { ben.lastName } returns "Devi"
        every { ben.age } returns 45
        every { ben.ageUnit } returns null
        every { ben.gender } returns Gender.FEMALE
        return ben
    }

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `initial state is IDLE`() {
        assertEquals(NCDReferDialogViewModel.State.IDLE, viewModel.state.value)
    }

    @Test
    fun `resetState sets state to IDLE`() {
        viewModel.resetState()
        assertEquals(NCDReferDialogViewModel.State.IDLE, viewModel.state.value)
    }

    @Test
    fun `benName live data is not null`() {
        assertNotNull(viewModel.benName)
    }

    @Test
    fun `benId defaults to zero`() {
        assertEquals(0L, viewModel.benId)
    }

    @Test
    fun `initFromArgs sets SAVE_FAILED when the beneficiary does not exist`() = runTest {
        coEvery { benDao.getBen(1L) } returns null

        viewModel.initFromArgs(1L, "reason", 2L, "type")
        advanceUntilIdle()

        assertEquals(NCDReferDialogViewModel.State.SAVE_FAILED, viewModel.state.value)
    }

    @Test
    fun `initFromArgs populates ben details when the beneficiary exists`() = runTest {
        coEvery { benDao.getBen(1L) } returns buildBen()
        every { preferenceDao.getLoggedInUser() } returns mockk<User>(relaxed = true)

        viewModel.initFromArgs(1L, "reason", 2L, "type")
        advanceUntilIdle()

        assertEquals("Anita Devi", viewModel.benName.value)
        assertEquals(Gender.FEMALE, viewModel.gender.value)
        assertEquals(45, viewModel.age.value)
        assertNotNull(viewModel.formList)
    }

    @Test
    fun `initFromArgs leaves referalCache createdBy null when no user is logged in`() = runTest {
        coEvery { benDao.getBen(1L) } returns buildBen()
        every { preferenceDao.getLoggedInUser() } returns null

        viewModel.initFromArgs(1L, "reason", 2L, "type")
        advanceUntilIdle()

        assertEquals("Anita Devi", viewModel.benName.value)
        assertEquals(null, viewModel.referalCache.createdBy)
    }

    @Test
    fun `initFromArgs formats ben name without a trailing name when lastName is null`() = runTest {
        val ben = buildBen()
        every { ben.lastName } returns null
        coEvery { benDao.getBen(1L) } returns ben
        every { preferenceDao.getLoggedInUser() } returns mockk<User>(relaxed = true)

        viewModel.initFromArgs(1L, "reason", 2L, "type")
        advanceUntilIdle()

        assertEquals("Anita ", viewModel.benName.value)
    }

    @Test
    fun `saveForm marks SAVE_SUCCESS after a beneficiary is loaded`() = runTest {
        coEvery { benDao.getBen(1L) } returns buildBen()
        every { preferenceDao.getLoggedInUser() } returns mockk<User>(relaxed = true)
        viewModel.initFromArgs(1L, "reason", 2L, "type")
        advanceUntilIdle()

        viewModel.saveForm()
        advanceUntilIdle()

        assertEquals(NCDReferDialogViewModel.State.SAVE_SUCCESS, viewModel.state.value)
    }

    @Test
    fun `saveForm marks SAVE_FAILED when referalCache was never initialised`() = runTest {
        viewModel.saveForm()
        advanceUntilIdle()

        unmockkStatic(Dispatchers::class)
        assertEquals(NCDReferDialogViewModel.State.SAVE_FAILED, viewModel.state.value)
    }

    @Test
    fun `updateListOnValueChanged does not throw once initialised`() = runTest {
        coEvery { benDao.getBen(1L) } returns buildBen()
        every { preferenceDao.getLoggedInUser() } returns mockk<User>(relaxed = true)
        viewModel.initFromArgs(1L, "reason", 2L, "type")
        advanceUntilIdle()

        viewModel.updateListOnValueChanged(2, 0)
        advanceUntilIdle()

        assertNotNull(viewModel)
    }
}
