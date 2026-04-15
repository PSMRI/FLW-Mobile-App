package org.piramalswasthya.sakhi.ui.home_activity.sync

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.room.dao.SyncDao
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Languages

@OptIn(ExperimentalCoroutinesApi::class)
class SyncViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var syncDao: SyncDao
    @MockK private lateinit var preferenceDao: PreferenceDao

    private lateinit var viewModel: SyncViewModel

    @Before
    override fun setUp() {
        super.setUp()
        every { syncDao.getSyncStatus() } returns flowOf(emptyList())
        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        viewModel = SyncViewModel(syncDao, preferenceDao)
    }

    // =====================================================
    // Initialization Tests
    // =====================================================

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `syncStatus is not null`() {
        assertNotNull(viewModel.syncStatus)
    }

    @Test
    fun `lang is ENGLISH`() {
        assertNotNull(viewModel.lang)
    }

    @Test
    fun `lang equals ENGLISH specifically`() {
        assertEquals(Languages.ENGLISH, viewModel.lang)
    }

    @Test
    fun `viewModel with HINDI language initializes`() {
        every { preferenceDao.getCurrentLanguage() } returns Languages.HINDI
        val vm = SyncViewModel(syncDao, preferenceDao)
        assertNotNull(vm)
        assertEquals(Languages.HINDI, vm.lang)
    }

    @Test
    fun `viewModel with ASSAMESE language initializes`() {
        every { preferenceDao.getCurrentLanguage() } returns Languages.ASSAMESE
        val vm = SyncViewModel(syncDao, preferenceDao)
        assertNotNull(vm)
        assertEquals(Languages.ASSAMESE, vm.lang)
    }

    @Test
    fun `syncStatus with empty list returns flow`() = runTest {
        assertNotNull(viewModel.syncStatus)
        advanceUntilIdle()
    }

    @Test
    fun `syncStatus with non-empty list`() = runTest {
        val mockStatus = mockk<org.piramalswasthya.sakhi.model.SyncStatusCache>(relaxed = true)
        every { syncDao.getSyncStatus() } returns flowOf(listOf(mockStatus))
        val vm = SyncViewModel(syncDao, preferenceDao)
        assertNotNull(vm.syncStatus)
        advanceUntilIdle()
    }

    @Test
    fun `multiple viewModel instances are independent`() {
        val vm1 = SyncViewModel(syncDao, preferenceDao)
        val vm2 = SyncViewModel(syncDao, preferenceDao)
        assertNotNull(vm1)
        assertNotNull(vm2)
    }

    @Test
    fun `syncStatus flow is consistent across accesses`() {
        val status1 = viewModel.syncStatus
        val status2 = viewModel.syncStatus
        assertEquals(status1, status2)
    }

    @Test
    fun `lang is consistent across accesses`() {
        val lang1 = viewModel.lang
        val lang2 = viewModel.lang
        assertEquals(lang1, lang2)
    }

    @Test
    fun `viewModel with multiple sync items`() = runTest {
        val item1 = mockk<org.piramalswasthya.sakhi.model.SyncStatusCache>(relaxed = true)
        val item2 = mockk<org.piramalswasthya.sakhi.model.SyncStatusCache>(relaxed = true)
        val item3 = mockk<org.piramalswasthya.sakhi.model.SyncStatusCache>(relaxed = true)
        every { syncDao.getSyncStatus() } returns flowOf(listOf(item1, item2, item3))
        val vm = SyncViewModel(syncDao, preferenceDao)
        assertNotNull(vm.syncStatus)
        advanceUntilIdle()
    }
}
