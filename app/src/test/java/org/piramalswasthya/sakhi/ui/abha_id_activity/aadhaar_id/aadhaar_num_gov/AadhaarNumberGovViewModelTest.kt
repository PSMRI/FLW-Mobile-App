package org.piramalswasthya.sakhi.ui.abha_id_activity.aadhaar_id.aadhaar_num_gov

import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.repositories.AbhaIdRepo

@OptIn(ExperimentalCoroutinesApi::class)
class AadhaarNumberGovViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var abhaIdRepo: AbhaIdRepo

    private lateinit var viewModel: AadhaarNumberGovViewModel

    @Before
    override fun setUp() {
        super.setUp()
        viewModel = AadhaarNumberGovViewModel(abhaIdRepo)
    }

    // =====================================================
    // Initialization Tests
    // =====================================================

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `initial state is not null`() {
        assertNotNull(viewModel.state)
    }

    @Test
    fun `abha is initially null`() {
        assertNull(viewModel.abha.value)
    }

    @Test
    fun `errorMessage is initially null`() {
        assertNull(viewModel.errorMessage.value)
    }

    @Test
    fun `activeState is initially null`() {
        assertNull(viewModel.activeState)
    }

    @Test
    fun `activeDistrict is initially null`() {
        assertNull(viewModel.activeDistrict)
    }
}
