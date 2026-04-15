package org.piramalswasthya.sakhi.ui.home_activity.child_care.adolescent_list.form

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
import org.piramalswasthya.sakhi.repositories.AdolescentHealthRepo
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.utils.HelperUtil

@OptIn(ExperimentalCoroutinesApi::class)
class AdolescentHealthFormViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var context: Context
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var mockResources: Resources
    @MockK private lateinit var adolescentHealthRepo: AdolescentHealthRepo
    @MockK private lateinit var benRepo: BenRepo

    private lateinit var viewModel: AdolescentHealthFormViewModel
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
        coEvery { benRepo.getBenFromId(any()) } returns null
        viewModel = AdolescentHealthFormViewModel(savedStateHandle, preferenceDao, context, benRepo, adolescentHealthRepo)
    }

    @Test fun `viewModel initializes successfully`() { assertNotNull(viewModel) }
    @Test fun `initial state is IDLE`() { assertEquals(AdolescentHealthFormViewModel.State.IDLE, viewModel.state.value) }
    @Test fun `setRecordExist updates recordExists`() { viewModel.setRecordExist(true); assertEquals(true, viewModel.recordExists.value) }
    @Test fun `formList is not null`() { assertNotNull(viewModel.formList) }
    @Test fun `benId is set from SavedStateHandle`() { assertEquals(1L, viewModel.benId) }
}
