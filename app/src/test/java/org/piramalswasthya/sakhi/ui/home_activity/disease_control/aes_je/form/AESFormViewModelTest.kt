package org.piramalswasthya.sakhi.ui.home_activity.disease_control.aes_je.form

import android.content.Context
import android.content.res.Resources
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import io.mockk.mockkStatic
import org.piramalswasthya.sakhi.utils.HelperUtil
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.repositories.AESRepo
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.MaternalHealthRepo

@OptIn(ExperimentalCoroutinesApi::class)
class AESFormViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var context: Context
    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var aesRepo: AESRepo
    @MockK private lateinit var benRepo: BenRepo
    @MockK private lateinit var maternalHealthRepo: MaternalHealthRepo
    @MockK private lateinit var mockResources: Resources

    private lateinit var viewModel: AESFormViewModel

    private val savedStateHandle = SavedStateHandle(mapOf(
        "benId" to 1L
    ))

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
        coEvery { aesRepo.getAESScreening(any()) } returns null
        viewModel = AESFormViewModel(savedStateHandle, preferenceDao, context, aesRepo, benRepo, maternalHealthRepo)
    }

    // =====================================================
    // Initialization Tests
    // =====================================================

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `benId is set from SavedStateHandle`() {
        assertEquals(1L, viewModel.benId)
    }

    @Test
    fun `initial state is IDLE`() {
        assertEquals(AESFormViewModel.State.IDLE, viewModel.state.value)
    }

    @Test
    fun `formList is not null`() {
        assertNotNull(viewModel.formList)
    }

    // =====================================================
    // resetState() Tests
    // =====================================================

    @Test
    fun `resetState sets state to IDLE`() {
        viewModel.resetState()
        assertEquals(AESFormViewModel.State.IDLE, viewModel.state.value)
    }

    // =====================================================
    // setRecordExist() Tests
    // =====================================================

    @Test
    fun `setRecordExist updates recordExists`() {
        viewModel.setRecordExist(true)
        assertEquals(true, viewModel.recordExists.value)
    }

    @Test
    fun `setRecordExist false updates recordExists`() {
        viewModel.setRecordExist(false)
        assertEquals(false, viewModel.recordExists.value)
    }

    // =====================================================
    // isDeath Tests
    // =====================================================

    @Test
    fun `isDeath is initially false`() {
        assertEquals(false, viewModel.isDeath)
    }
}
