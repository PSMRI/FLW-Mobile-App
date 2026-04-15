package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pregnant_women_registration.form

import android.content.Context
import android.content.res.Resources
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.EcrRepo
import org.piramalswasthya.sakhi.repositories.HRPRepo
import org.piramalswasthya.sakhi.repositories.HouseholdRepo
import org.piramalswasthya.sakhi.repositories.MaternalHealthRepo
import org.piramalswasthya.sakhi.repositories.UserRepo
import org.piramalswasthya.sakhi.utils.HelperUtil

@OptIn(ExperimentalCoroutinesApi::class)
class PregnancyRegistrationFormViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var context: Context
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var mockResources: Resources
    @MockK private lateinit var maternalHealthRepo: MaternalHealthRepo
    @MockK private lateinit var ecrRepo: EcrRepo
    @MockK private lateinit var hrpRepo: HRPRepo
    @MockK private lateinit var benRepo: BenRepo
    @MockK private lateinit var householdRepo: HouseholdRepo
    @MockK private lateinit var userRepo: UserRepo

    private lateinit var viewModel: PregnancyRegistrationFormViewModel
    private val savedStateHandle = SavedStateHandle(mapOf("benId" to 1L))

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.isLoggable(any(), any()) } returns false
        mockkObject(HelperUtil)
        every { HelperUtil.getLocalizedResources(any(), any()) } returns mockResources
        every { mockResources.getStringArray(any()) } returns emptyArray()
        every { mockResources.getString(any()) } returns ""
        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        every { preferenceDao.getLoggedInUser() } returns null
        coEvery { benRepo.getBenFromId(any()) } returns null
        coEvery { maternalHealthRepo.getSavedRegistrationRecord(any()) } returns null
        viewModel = PregnancyRegistrationFormViewModel(savedStateHandle, preferenceDao, context, maternalHealthRepo, ecrRepo, hrpRepo, benRepo)
    }

    @Test fun `viewModel initializes successfully`() { assertNotNull(viewModel) }
    @Test fun `initial state is IDLE`() { assertEquals(PregnancyRegistrationFormViewModel.State.IDLE, viewModel.state.value) }
    @Test fun `setRecordExist updates recordExists`() { viewModel.setRecordExist(true); assertEquals(true, viewModel.recordExists.value) }
    @Test fun `formList is not null`() { assertNotNull(viewModel.formList) }
}
