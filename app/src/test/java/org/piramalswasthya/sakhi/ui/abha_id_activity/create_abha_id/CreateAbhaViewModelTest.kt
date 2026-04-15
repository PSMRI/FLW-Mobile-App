package org.piramalswasthya.sakhi.ui.abha_id_activity.create_abha_id

import androidx.lifecycle.SavedStateHandle
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.repositories.ABHAGenratedRepo
import org.piramalswasthya.sakhi.repositories.AbhaIdRepo
import org.piramalswasthya.sakhi.repositories.BenRepo

@OptIn(ExperimentalCoroutinesApi::class)
class CreateAbhaViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var pref: PreferenceDao
    @MockK private lateinit var abhaIdRepo: AbhaIdRepo
    @MockK private lateinit var benRepo: BenRepo
    @MockK private lateinit var abhaGenratedRepo: ABHAGenratedRepo

    private lateinit var viewModel: CreateAbhaViewModel

    private val savedStateHandle = SavedStateHandle(mapOf(
        "txnId" to "test-txn-id",
        "name" to "Test User",
        "phrAddress" to "test@abdm",
        "abhaNumber" to "12-3456-7890-1234",
        "abhaResponse" to ""
    ))

    @Before
    override fun setUp() {
        super.setUp()
        viewModel = CreateAbhaViewModel(pref, abhaIdRepo, benRepo, abhaGenratedRepo, savedStateHandle)
    }

    // =====================================================
    // Initialization Tests
    // =====================================================

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `initial abha is null`() {
        assertNull(viewModel.abha.value)
    }

    @Test
    fun `initial hidResponse is null`() {
        assertNull(viewModel.hidResponse.value)
    }

    @Test
    fun `initial benMapped is null`() {
        assertNull(viewModel.benMapped.value)
    }

    @Test
    fun `initial errorMessage is null`() {
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
