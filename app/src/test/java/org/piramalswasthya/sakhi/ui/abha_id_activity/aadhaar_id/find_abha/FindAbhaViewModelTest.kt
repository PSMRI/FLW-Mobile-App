package org.piramalswasthya.sakhi.ui.abha_id_activity.aadhaar_id.find_abha

import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.repositories.AbhaIdRepo
import org.piramalswasthya.sakhi.repositories.BenRepo

@OptIn(ExperimentalCoroutinesApi::class)
class FindAbhaViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var abhaIdRepo: AbhaIdRepo
    @MockK private lateinit var benRepo: BenRepo

    private lateinit var viewModel: FindAbhaViewModel

    @Before
    override fun setUp() {
        super.setUp()
        viewModel = FindAbhaViewModel(abhaIdRepo, benRepo)
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
    fun `txnId is initially null`() {
        assertNull(viewModel.txnId.value)
    }

    @Test
    fun `fnlTxnId is initially null`() {
        assertNull(viewModel.fnlTxnId.value)
    }

    @Test
    fun `abha is initially null`() {
        assertNull(viewModel.abha.value)
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
    fun `resetState does not throw`() {
        viewModel.resetState()
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
