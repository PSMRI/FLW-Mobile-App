package org.piramalswasthya.sakhi.ui.home_activity.disease_control.filaria.form

import android.content.Context
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.repositories.dynamicRepo.FilariaMDAFormRepository

@OptIn(ExperimentalCoroutinesApi::class)
class FilariaMDAFormViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var repository: FilariaMDAFormRepository
    @MockK private lateinit var context: Context

    private lateinit var viewModel: FilariaMDAFormViewModel

    @Before
    override fun setUp() {
        super.setUp()
        viewModel = FilariaMDAFormViewModel(repository, context)
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

    @Test
    fun `wasDuplicate defaults to false`() {
        assertTrue(!viewModel.wasDuplicate)
    }
}
