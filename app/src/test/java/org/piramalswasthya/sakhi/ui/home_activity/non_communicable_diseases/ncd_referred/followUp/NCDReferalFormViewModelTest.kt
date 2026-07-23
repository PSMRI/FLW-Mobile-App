package org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.ncd_referred.followUp

import android.content.Context
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.repositories.dynamicRepo.NCDFollowUpFormRepository

@OptIn(ExperimentalCoroutinesApi::class)
class NCDReferalFormViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var repository: NCDFollowUpFormRepository
    @MockK private lateinit var context: Context

    private lateinit var viewModel: NCDReferalFormViewModel

    @Before
    override fun setUp() {
        super.setUp()
        viewModel = NCDReferalFormViewModel(repository, context)
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
    fun `visitHistory is not null`() {
        assertNotNull(viewModel.visitHistory)
    }

    @Test
    fun `getVisibleFields returns empty when schema null`() {
        assertTrue(viewModel.getVisibleFields().isEmpty())
    }

    @Test
    fun `visitNo defaults to 1`() {
        assertEquals(1, viewModel.visitNo)
    }
}
