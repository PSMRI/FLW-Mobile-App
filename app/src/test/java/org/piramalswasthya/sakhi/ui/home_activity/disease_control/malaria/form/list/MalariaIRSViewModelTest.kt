package org.piramalswasthya.sakhi.ui.home_activity.disease_control.malaria.form.list

import android.content.Context
import android.content.res.Resources
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import io.mockk.mockkStatic
import kotlinx.coroutines.Dispatchers
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
class MalariaIRSViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var context: Context
    @MockK private lateinit var mockResources: Resources
    @MockK private lateinit var malariaRepo: MalariaRepo
    @MockK private lateinit var benRepo: BenRepo
    @MockK private lateinit var recordsRepo: RecordsRepo

    private val savedStateHandle = SavedStateHandle(
        mapOf("hhId" to 1L, "fromDisease" to 0, "diseaseType" to "")
    )
    private lateinit var viewModel: MalariaIRSViewModel

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        mockkObject(HelperUtil)
        every { HelperUtil.getLocalizedResources(any(), any()) } returns mockResources
        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        mockkStatic(Dispatchers::class)
        every { Dispatchers.IO } returns testDispatcher
        viewModel = MalariaIRSViewModel(savedStateHandle, preferenceDao, context, malariaRepo, benRepo, recordsRepo)
    }

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `initial state is IDLE`() {
        assertEquals(MalariaIRSViewModel.State.IDLE, viewModel.state.value)
    }

    @Test
    fun `resetState sets state to IDLE`() {
        viewModel.resetState()
        assertEquals(MalariaIRSViewModel.State.IDLE, viewModel.state.value)
    }

    @Test
    fun `formList is not null`() {
        assertNotNull(viewModel.formList)
    }

    @Test
    fun `isSubmitVisible is not null`() {
        assertNotNull(viewModel.isSubmitVisible)
    }

    @Test
    fun `hhId is set from SavedStateHandle`() {
        assertEquals(1L, viewModel.hhId)
    }
}
