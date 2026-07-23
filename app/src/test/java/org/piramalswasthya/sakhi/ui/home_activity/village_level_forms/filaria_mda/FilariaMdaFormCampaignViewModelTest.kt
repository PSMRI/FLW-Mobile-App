package org.piramalswasthya.sakhi.ui.home_activity.village_level_forms.filaria_mda

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.room.dao.dynamicSchemaDao.FilariaMdaCampaignJsonDao
import org.piramalswasthya.sakhi.repositories.dynamicRepo.FilariaMdaCampaignRepository

@OptIn(ExperimentalCoroutinesApi::class)
class FilariaMdaFormCampaignViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var repository: FilariaMdaCampaignRepository
    @MockK private lateinit var context: Context
    @MockK private lateinit var dao: FilariaMdaCampaignJsonDao

    private val savedStateHandle = SavedStateHandle()
    private lateinit var viewModel: FilariaMdaFormCampaignViewModel

    @Before
    override fun setUp() {
        super.setUp()
        viewModel = FilariaMdaFormCampaignViewModel(repository, context, savedStateHandle, dao)
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
    fun `getCurrentYear returns four digit year`() {
        assertEquals(4, viewModel.getCurrentYear().length)
    }

    @Test
    fun `yearDate uses default empty value`() {
        assertEquals("", viewModel.yearDate)
    }

    @Test
    fun `isCampaignAlreadyAdded is not null`() {
        assertNotNull(viewModel.isCampaignAlreadyAdded)
    }
}
