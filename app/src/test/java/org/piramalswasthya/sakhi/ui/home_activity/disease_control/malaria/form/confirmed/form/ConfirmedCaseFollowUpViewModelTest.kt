package org.piramalswasthya.sakhi.ui.home_activity.disease_control.malaria.form.confirmed.form

import android.content.Context
import android.content.res.Resources
import android.util.Log
import androidx.lifecycle.SavedStateHandle
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
import org.piramalswasthya.sakhi.repositories.MalariaRepo
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import org.piramalswasthya.sakhi.utils.HelperUtil

@OptIn(ExperimentalCoroutinesApi::class)
class ConfirmedCaseFollowUpViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var context: Context
    @MockK private lateinit var mockResources: Resources
    @MockK private lateinit var malariaRepo: MalariaRepo
    @MockK private lateinit var benRepo: BenRepo
    @MockK private lateinit var recordsRepo: RecordsRepo

    private val savedStateHandle = SavedStateHandle(mapOf("benId" to 1L))
    private lateinit var viewModel: ConfirmedCaseFollowUpViewModel

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        mockkObject(HelperUtil)
        every { HelperUtil.getLocalizedResources(any(), any()) } returns mockResources
        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        viewModel = ConfirmedCaseFollowUpViewModel(savedStateHandle, preferenceDao, context, malariaRepo, benRepo, recordsRepo)
    }

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `initial state is IDLE`() {
        assertEquals(ConfirmedCaseFollowUpViewModel.State.IDLE, viewModel.state.value)
    }

    @Test
    fun `formList is not null`() {
        assertNotNull(viewModel.formList)
    }

    @Test
    fun `benId is set from SavedStateHandle`() {
        assertEquals(1L, viewModel.benId)
    }
}
