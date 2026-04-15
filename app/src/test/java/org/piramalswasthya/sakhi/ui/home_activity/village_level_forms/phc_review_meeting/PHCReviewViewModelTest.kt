package org.piramalswasthya.sakhi.ui.home_activity.village_level_forms.phc_review_meeting

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
import org.piramalswasthya.sakhi.repositories.VLFRepo
import org.piramalswasthya.sakhi.utils.HelperUtil

@OptIn(ExperimentalCoroutinesApi::class)
class PHCReviewViewModelTest : BaseViewModelTest() {
    @MockK private lateinit var context: Context
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var mockResources: Resources
    @MockK private lateinit var vlfRepo: VLFRepo
    private lateinit var viewModel: PHCReviewViewModel
    private val savedStateHandle = SavedStateHandle(mapOf("id" to 0))

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class); every { Log.d(any(), any()) } returns 0; every { Log.e(any(), any()) } returns 0; every { Log.isLoggable(any(), any()) } returns false
        mockkObject(HelperUtil); every { HelperUtil.getLocalizedResources(any(), any()) } returns mockResources
        every { mockResources.getStringArray(any()) } returns emptyArray(); every { mockResources.getString(any()) } returns ""
        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        viewModel = PHCReviewViewModel(savedStateHandle, preferenceDao, context, vlfRepo)
    }

    @Test fun `viewModel initializes successfully`() { assertNotNull(viewModel) }
    @Test fun `initial state is IDLE`() { assertEquals(PHCReviewViewModel.State.IDLE, viewModel.state.value) }
    @Test fun `resetState sets state to IDLE`() { viewModel.resetState(); assertEquals(PHCReviewViewModel.State.IDLE, viewModel.state.value) }
    @Test fun `formList is not null`() { assertNotNull(viewModel.formList) }
}
