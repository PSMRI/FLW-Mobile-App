package org.piramalswasthya.sakhi.ui.home_activity.infant.hbyc

import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.InfantRegRepo
import org.piramalswasthya.sakhi.repositories.dynamicRepo.FormRepository

@OptIn(ExperimentalCoroutinesApi::class)
class HBYCFormViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var repository: FormRepository
    @MockK private lateinit var benRepo: BenRepo
    @MockK private lateinit var infantRegRepo: InfantRegRepo

    private lateinit var viewModel: HBYCFormViewModel

    @Before
    override fun setUp() {
        super.setUp()
        viewModel = HBYCFormViewModel(repository, benRepo, infantRegRepo)
    }

    // =====================================================
    // Initialization Tests
    // =====================================================

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `initial schema is null`() {
        assertNull(viewModel.schema.value)
    }

    @Test
    fun `initial infant is null`() {
        assertNull(viewModel.infant.value)
    }

    @Test
    fun `initial syncedVisitList is empty`() {
        assertNotNull(viewModel.syncedVisitList.value)
        assert(viewModel.syncedVisitList.value.isEmpty())
    }
}
