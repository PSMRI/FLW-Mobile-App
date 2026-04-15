package org.piramalswasthya.sakhi.ui.home_activity.village_level_forms

import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.configuration.IconDataset
import org.piramalswasthya.sakhi.repositories.VLFRepo

@OptIn(ExperimentalCoroutinesApi::class)
class VillageLevelFormsViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var iconDataset: IconDataset
    @MockK private lateinit var vlfRepo: VLFRepo

    private lateinit var viewModel: VillageLevelFormsViewModel

    @Before
    override fun setUp() {
        super.setUp()
        viewModel = VillageLevelFormsViewModel(iconDataset, vlfRepo)
    }

    // =====================================================
    // Initialization Tests
    // =====================================================

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `scope returns viewModelScope`() {
        assertNotNull(viewModel.scope)
    }

    @Test
    fun `iconsWithRedFlags is not null`() {
        assertNotNull(viewModel.iconsWithRedFlags)
    }

    @Test
    fun `initial iconsWithRedFlags is empty`() {
        assertTrue(viewModel.iconsWithRedFlags.value.isEmpty())
    }
}
