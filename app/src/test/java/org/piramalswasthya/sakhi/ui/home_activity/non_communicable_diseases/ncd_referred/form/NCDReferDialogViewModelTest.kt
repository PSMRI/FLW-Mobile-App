package org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.ncd_referred.form

import android.content.Context
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.room.dao.BenDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.repositories.CbacRepo
import org.piramalswasthya.sakhi.repositories.NcdReferalRepo

@OptIn(ExperimentalCoroutinesApi::class)
class NCDReferDialogViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var preferenceDao: PreferenceDao
    @MockK private lateinit var cbacRepo: CbacRepo
    @MockK private lateinit var benDao: BenDao
    @MockK private lateinit var referalRepo: NcdReferalRepo
    @MockK private lateinit var context: Context

    private lateinit var viewModel: NCDReferDialogViewModel

    @Before
    override fun setUp() {
        super.setUp()
        viewModel = NCDReferDialogViewModel(preferenceDao, cbacRepo, benDao, referalRepo, context)
    }

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `initial state is IDLE`() {
        assertEquals(NCDReferDialogViewModel.State.IDLE, viewModel.state.value)
    }

    @Test
    fun `resetState sets state to IDLE`() {
        viewModel.resetState()
        assertEquals(NCDReferDialogViewModel.State.IDLE, viewModel.state.value)
    }

    @Test
    fun `benName live data is not null`() {
        assertNotNull(viewModel.benName)
    }

    @Test
    fun `benId defaults to zero`() {
        assertEquals(0L, viewModel.benId)
    }
}
