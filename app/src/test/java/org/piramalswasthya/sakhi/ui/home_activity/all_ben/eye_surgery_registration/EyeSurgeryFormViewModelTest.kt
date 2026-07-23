package org.piramalswasthya.sakhi.ui.home_activity.all_ben.eye_surgery_registration

import android.content.Context
import android.util.Log
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.repositories.dynamicRepo.EyeSurgeryFormRepository

@OptIn(ExperimentalCoroutinesApi::class)
class EyeSurgeryFormViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var repository: EyeSurgeryFormRepository
    @MockK private lateinit var context: Context

    private lateinit var viewModel: EyeSurgeryFormViewModel

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        viewModel = EyeSurgeryFormViewModel(repository, context)
    }

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `schema is initially null`() {
        assertNull(viewModel.schema.value)
    }

    @Test
    fun `getVisibleFields returns empty when schema null`() {
        assertTrue(viewModel.getVisibleFields().isEmpty())
    }

    @Test
    fun `getMaxVisitDate is not null`() {
        assertNotNull(viewModel.getMaxVisitDate())
    }

    @Test
    fun `getMinVisitDate is null when no previous visit`() {
        assertNull(viewModel.getMinVisitDate())
    }
}
