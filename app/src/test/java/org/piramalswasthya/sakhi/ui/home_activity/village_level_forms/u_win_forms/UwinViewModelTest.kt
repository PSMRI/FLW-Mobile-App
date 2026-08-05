package org.piramalswasthya.sakhi.ui.home_activity.village_level_forms.u_win_forms

import android.content.Context
import android.content.res.Resources
import android.util.Log
import androidx.lifecycle.MutableLiveData
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
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
import org.piramalswasthya.sakhi.model.User
import org.piramalswasthya.sakhi.repositories.UwinRepo
import org.piramalswasthya.sakhi.utils.HelperUtil

@OptIn(ExperimentalCoroutinesApi::class)
class UwinViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var context: Context
    @MockK private lateinit var mockResources: Resources
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var uwinRepo: UwinRepo

    private lateinit var viewModel: UwinViewModel

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        mockkObject(HelperUtil)
        every { HelperUtil.getLocalizedResources(any(), any()) } returns mockResources
        every { mockResources.getStringArray(any()) } returns emptyArray()
        every { mockResources.getString(any()) } returns ""
        every { mockResources.getString(any(), any()) } returns ""
        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        every { preferenceDao.getLoggedInUser() } returns mockk<User>(relaxed = true)
        every { uwinRepo.getAllLocalRecords() } returns MutableLiveData(emptyList())
        viewModel = UwinViewModel(context, preferenceDao, uwinRepo)
    }

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `formList is not null`() {
        assertNotNull(viewModel.formList)
    }

    @Test
    fun `uwinList is not null`() {
        assertNotNull(viewModel.uwinList)
    }

    @Test
    fun `state is not null`() {
        assertNotNull(viewModel.state)
    }

    @Test
    fun `recordExists is not null`() {
        assertNotNull(viewModel.recordExists)
    }

    @Test
    fun `getDocumentFormId returns default zero`() {
        assertEquals(0, viewModel.getDocumentFormId())
    }

    @Test
    fun `setCurrentDocumentFormId updates document form id`() {
        viewModel.setCurrentDocumentFormId(42)
        assertEquals(42, viewModel.getDocumentFormId())
    }
}
