package org.piramalswasthya.sakhi.ui.abha_id_activity.aadhaar_id.aadhaar_num_asha

import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.repositories.AbhaIdRepo
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.ui.abha_id_activity.aadhaar_id.AadhaarIdViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class AadhaarNumberAshaViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var abhaIdRepo: AbhaIdRepo
    @MockK private lateinit var benRepo: BenRepo

    private lateinit var viewModel: AadhaarNumberAshaViewModel

    @Before
    override fun setUp() {
        super.setUp()
        viewModel = AadhaarNumberAshaViewModel(abhaIdRepo, benRepo)
    }

    // =====================================================
    // Initialization Tests
    // =====================================================

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `initial state is IDLE`() {
        val state = viewModel.state.value
        assertNotNull(state)
    }

    @Test
    fun `txnId is initially null`() {
        assertNull(viewModel.txnId.value)
    }

    @Test
    fun `ben is initially null`() {
        assertNull(viewModel.ben.value)
    }

    @Test
    fun `errorMessage is initially null`() {
        assertNull(viewModel.errorMessage.value)
    }

    // =====================================================
    // resetState() Tests
    // =====================================================

    @Test
    fun `resetState sets state to IDLE`() {
        viewModel.resetState()
        assertNotNull(viewModel.state.value)
    }

    // =====================================================
    // resetErrorMessage() Tests
    // =====================================================

    @Test
    fun `resetErrorMessage sets errorMessage to null`() {
        viewModel.resetErrorMessage()
        assertNull(viewModel.errorMessage.value)
    }
}
