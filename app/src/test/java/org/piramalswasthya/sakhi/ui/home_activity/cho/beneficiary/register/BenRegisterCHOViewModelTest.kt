package org.piramalswasthya.sakhi.ui.home_activity.cho.beneficiary.register

import android.content.Context
import android.content.res.Resources
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.network.AmritApiService
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.HouseholdRepo
import org.piramalswasthya.sakhi.utils.HelperUtil

@OptIn(ExperimentalCoroutinesApi::class)
class BenRegisterCHOViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var context: Context
    @MockK private lateinit var mockResources: Resources
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var benRepo: BenRepo
    @MockK private lateinit var householdRepo: HouseholdRepo
    @MockK private lateinit var amritApiService: AmritApiService

    private lateinit var viewModel: BenRegisterCHOViewModel

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        mockkObject(HelperUtil)
        every { HelperUtil.getLocalizedResources(any(), any()) } returns mockResources
        every { mockResources.getStringArray(any()) } returns arrayOf("Yes", "No")
        every { mockResources.getString(any()) } returns ""
        every { mockResources.getString(any(), any()) } returns ""
        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        viewModel = BenRegisterCHOViewModel(
            SavedStateHandle(),
            preferenceDao,
            context,
            benRepo,
            householdRepo,
            amritApiService
        )
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
    fun `state is not null`() {
        assertNotNull(viewModel.state)
    }

    @Test
    fun `benName is not null`() {
        assertNotNull(viewModel.benName)
    }

    @Test
    fun `recordExists is not null`() {
        assertNotNull(viewModel.recordExists)
    }
}
