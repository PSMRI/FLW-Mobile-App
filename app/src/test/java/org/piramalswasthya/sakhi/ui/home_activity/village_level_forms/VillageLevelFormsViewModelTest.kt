package org.piramalswasthya.sakhi.ui.home_activity.village_level_forms

import android.content.res.Resources
import androidx.navigation.NavDirections
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.configuration.IconDataset
import org.piramalswasthya.sakhi.model.Icon
import org.piramalswasthya.sakhi.repositories.VLFRepo
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

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

    private fun icon(title: String) = Icon(
        icon = 0,
        title = title,
        count = null,
        navAction = mockk<NavDirections>(relaxed = true)
    )

    @Test
    fun `loadIcons marks a form submitted this month as not overdue`() = runTest {
        val today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH))
        val resources = mockk<Resources>(relaxed = true)
        every { iconDataset.getVLFDataset(resources) } returns listOf(icon("VHND"))
        every { vlfRepo.getLastSubmissionDate("vhnd") } returns flowOf(today)

        viewModel.loadIcons(resources)
        advanceUntilIdle()

        val result = viewModel.iconsWithRedFlags.value
        assertEquals(1, result.size)
        assertEquals(false, result[0].second)
    }

    @Test
    fun `loadIcons treats an unparsable submission date as never submitted`() = runTest {
        val resources = mockk<Resources>(relaxed = true)
        every { iconDataset.getVLFDataset(resources) } returns listOf(icon("VHNC"))
        every { vlfRepo.getLastSubmissionDate("vhnc") } returns flowOf("not-a-date")

        viewModel.loadIcons(resources)
        advanceUntilIdle()

        val expectedOverdue = LocalDate.now().dayOfMonth > 7
        val result = viewModel.iconsWithRedFlags.value
        assertEquals(1, result.size)
        assertEquals(expectedOverdue, result[0].second)
    }

    @Test
    fun `loadIcons skips the repo lookup when the icon title has no mapped form`() = runTest {
        val resources = mockk<Resources>(relaxed = true)
        every { iconDataset.getVLFDataset(resources) } returns listOf(icon("Unmapped Form"))

        viewModel.loadIcons(resources)
        advanceUntilIdle()

        val expectedOverdue = LocalDate.now().dayOfMonth > 7
        val result = viewModel.iconsWithRedFlags.value
        assertEquals(1, result.size)
        assertEquals(expectedOverdue, result[0].second)
    }

    @Test
    fun `loadIcons handles multiple icons in a single pass`() = runTest {
        val today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH))
        val resources = mockk<Resources>(relaxed = true)
        every { iconDataset.getVLFDataset(resources) } returns listOf(icon("VHND"), icon("AHD"))
        every { vlfRepo.getLastSubmissionDate("vhnd") } returns flowOf(today)
        every { vlfRepo.getLastSubmissionDate("ahd") } returns flowOf(null)

        viewModel.loadIcons(resources)
        advanceUntilIdle()

        val result = viewModel.iconsWithRedFlags.value
        assertEquals(2, result.size)
        assertEquals(false, result[0].second)
    }
}
