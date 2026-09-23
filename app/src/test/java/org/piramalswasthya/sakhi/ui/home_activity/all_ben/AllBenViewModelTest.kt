package org.piramalswasthya.sakhi.ui.home_activity.all_ben

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import android.widget.Toast
import androidx.lifecycle.SavedStateHandle
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.model.BenBasicDomain
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.repositories.ABHAGenratedRepo
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.RecordsRepo
import java.nio.file.Files

@OptIn(ExperimentalCoroutinesApi::class)
class AllBenViewModelTest : BaseViewModelTest() {

    @MockK private lateinit var recordsRepo: RecordsRepo
    @MockK private lateinit var abhaGenratedRepo: ABHAGenratedRepo
    @MockK private lateinit var benRepo: BenRepo

    private lateinit var viewModel: AllBenViewModel

    private val savedStateHandle = SavedStateHandle(mapOf(
        "source" to 0
    ))

    @Before
    override fun setUp() {
        super.setUp()
        every { recordsRepo.childCountsByBen } returns flowOf(emptyMap())
        viewModel = AllBenViewModel(savedStateHandle, recordsRepo, abhaGenratedRepo, benRepo)
    }

    // =====================================================
    // Initialization Tests
    // =====================================================

    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel)
    }

    @Test
    fun `benList is not null`() {
        assertNotNull(viewModel.benList)
    }

    @Test
    fun `childCounts is not null`() {
        assertNotNull(viewModel.childCounts)
    }

    @Test
    fun `initial abha is null`() {
        assertNull(viewModel.abha.value)
    }

    @Test
    fun `initial benId is null`() {
        assertNull(viewModel.benId.value)
    }

    @Test
    fun `initial benRegId is null`() {
        assertNull(viewModel.benRegId.value)
    }

    // =====================================================
    // filterText() Tests
    // =====================================================

    @Test
    fun `filterText does not throw`() = runTest {
        viewModel.filterText("test")
        advanceUntilIdle()
    }

    @Test
    fun `filterText with empty string does not throw`() = runTest {
        viewModel.filterText("")
        advanceUntilIdle()
    }

    // =====================================================
    // filterType() Tests
    // =====================================================

    @Test
    fun `filterType does not throw`() = runTest {
        viewModel.filterType(1)
        advanceUntilIdle()
    }

    // =====================================================
    // fetchAbha() Tests
    // =====================================================

    @Test
    fun `fetchAbha sets benId`() = runTest {
        coEvery { benRepo.getBenFromId(42L) } returns null
        viewModel.fetchAbha(42L)
        advanceUntilIdle()
        assertEquals(42L, viewModel.benId.value)
    }

    @Test
    fun `fetchAbha resets abha to null`() = runTest {
        coEvery { benRepo.getBenFromId(42L) } returns null
        viewModel.fetchAbha(42L)
        advanceUntilIdle()
        assertNull(viewModel.abha.value)
    }

    // =====================================================
    // resetBenRegId() Tests
    // =====================================================

    @Test
    fun `resetBenRegId sets benRegId to null`() {
        viewModel.resetBenRegId()
        assertNull(viewModel.benRegId.value)
    }

    // =====================================================
    // getBenFromId() Tests
    // =====================================================

    @Test
    fun `getBenFromId returns 0 when ben not found`() = runTest {
        coEvery { benRepo.getBenFromId(999L) } returns null
        val result = viewModel.getBenFromId(999L)
        assertEquals(0L, result)
    }

    // =====================================================
    // Extended filterText() Tests
    // =====================================================

    @Test
    fun `filterText with special characters does not throw`() = runTest {
        viewModel.filterText("@#\$%^&*()")
        advanceUntilIdle()
    }

    @Test
    fun `filterText with long string does not throw`() = runTest {
        viewModel.filterText("a".repeat(200))
        advanceUntilIdle()
    }

    @Test
    fun `filterText with whitespace does not throw`() = runTest {
        viewModel.filterText("   ")
        advanceUntilIdle()
    }

    @Test
    fun `filterText with unicode does not throw`() = runTest {
        viewModel.filterText("कुमार")
        advanceUntilIdle()
    }

    @Test
    fun `filterText called multiple times does not throw`() = runTest {
        viewModel.filterText("first")
        viewModel.filterText("second")
        viewModel.filterText("third")
        advanceUntilIdle()
    }

    // =====================================================
    // Extended filterType() Tests
    // =====================================================

    @Test
    fun `filterType with 0 does not throw`() = runTest {
        viewModel.filterType(0)
        advanceUntilIdle()
    }

    @Test
    fun `filterType with 2 does not throw`() = runTest {
        viewModel.filterType(2)
        advanceUntilIdle()
    }

    @Test
    fun `filterType with 3 does not throw`() = runTest {
        viewModel.filterType(3)
        advanceUntilIdle()
    }

    @Test
    fun `filterType with 4 does not throw`() = runTest {
        viewModel.filterType(4)
        advanceUntilIdle()
    }

    @Test
    fun `filterType with 5 does not throw`() = runTest {
        viewModel.filterType(5)
        advanceUntilIdle()
    }

    @Test
    fun `filterType called sequentially does not throw`() = runTest {
        viewModel.filterType(0)
        viewModel.filterType(1)
        viewModel.filterType(2)
        advanceUntilIdle()
    }

    // =====================================================
    // Extended fetchAbha() Tests
    // =====================================================

    @Test
    fun `fetchAbha with zero benId`() = runTest {
        coEvery { benRepo.getBenFromId(0L) } returns null
        viewModel.fetchAbha(0L)
        advanceUntilIdle()
        assertEquals(0L, viewModel.benId.value)
    }

    @Test
    fun `fetchAbha with large benId`() = runTest {
        coEvery { benRepo.getBenFromId(999999L) } returns null
        viewModel.fetchAbha(999999L)
        advanceUntilIdle()
        assertEquals(999999L, viewModel.benId.value)
    }

    @Test
    fun `fetchAbha called twice updates benId`() = runTest {
        coEvery { benRepo.getBenFromId(any()) } returns null
        viewModel.fetchAbha(10L)
        advanceUntilIdle()
        assertEquals(10L, viewModel.benId.value)
        viewModel.fetchAbha(20L)
        advanceUntilIdle()
        assertEquals(20L, viewModel.benId.value)
    }

    // =====================================================
    // Extended resetBenRegId() Tests
    // =====================================================

    @Test
    fun `resetBenRegId called multiple times does not throw`() {
        viewModel.resetBenRegId()
        viewModel.resetBenRegId()
        viewModel.resetBenRegId()
        assertNull(viewModel.benRegId.value)
    }

    // =====================================================
    // Combination Tests
    // =====================================================

    @Test
    fun `filterText and filterType combined`() = runTest {
        viewModel.filterText("search")
        viewModel.filterType(2)
        advanceUntilIdle()
    }

    @Test
    fun `fetchAbha then resetBenRegId`() = runTest {
        coEvery { benRepo.getBenFromId(42L) } returns null
        viewModel.fetchAbha(42L)
        advanceUntilIdle()
        viewModel.resetBenRegId()
        assertNull(viewModel.benRegId.value)
    }

    @Test
    fun `multiple instances are independent`() {
        val vm1 = AllBenViewModel(savedStateHandle, recordsRepo, abhaGenratedRepo, benRepo)
        val vm2 = AllBenViewModel(savedStateHandle, recordsRepo, abhaGenratedRepo, benRepo)
        assertNotNull(vm1)
        assertNotNull(vm2)
    }

    @Test
    fun `benList flow is consistent`() {
        val list1 = viewModel.benList
        val list2 = viewModel.benList
        assertEquals(list1, list2)
    }

    @Test
    fun `childCounts flow is consistent`() {
        val c1 = viewModel.childCounts
        val c2 = viewModel.childCounts
        assertEquals(c1, c2)
    }

    // =====================================================
    // fetchAbha() / getBenFromId() ben-found branch
    // =====================================================

    @Test
    fun `fetchAbha sets benRegId when ben is found`() = runTest {
        val benRegCache = mockk<BenRegCache>(relaxed = true)
        every { benRegCache.benRegId } returns 555L
        coEvery { benRepo.getBenFromId(42L) } returns benRegCache
        viewModel.fetchAbha(42L)
        advanceUntilIdle()
        assertEquals(555L, viewModel.benRegId.value)
    }

    @Test
    fun `getBenFromId returns benRegId when ben is found`() = runTest {
        val benRegCache = mockk<BenRegCache>(relaxed = true)
        every { benRegCache.benRegId } returns 777L
        coEvery { benRepo.getBenFromId(1L) } returns benRegCache
        val result = viewModel.getBenFromId(1L)
        assertEquals(777L, result)
    }

    // =====================================================
    // downloadCsv() Tests
    // =====================================================

    private fun sampleBen(benId: Long) = BenBasicDomain(
        benId = benId,
        hhId = 1L,
        reproductiveStatusId = 0,
        regDate = "01-01-2024",
        benName = "TestName",
        gender = "M",
        dob = 0L,
        relToHeadId = 1,
        mobileNo = "9999999999",
        familyHeadName = "Head",
        syncState = SyncState.SYNCED,
        isConsent = true,
        isSpouseAdded = false,
        isChildrenAdded = false,
        isMarried = false
    )

    @Test
    fun `downloadCsv shows a toast when there is no data to export`() = runTest {
        mockkStatic(Toast::class)
        val toast = mockk<Toast>(relaxed = true)
        every { Toast.makeText(any(), any<CharSequence>(), any()) } returns toast
        coEvery { recordsRepo.searchBenOnce(any(), any(), any()) } returns emptyList()
        val context = mockk<Context>(relaxed = true)

        viewModel.downloadCsv(context)
        advanceUntilIdle()

        io.mockk.verify { Toast.makeText(context, "No data to export", Toast.LENGTH_SHORT) }
    }

    @Test
    fun `downloadCsv writes a csv file and shows a toast when there is data to export`() = runTest {
        mockkStatic(Toast::class)
        val toast = mockk<Toast>(relaxed = true)
        every { Toast.makeText(any(), any<CharSequence>(), any()) } returns toast

        val tempDir = Files.createTempDirectory("allben-csv").toFile().apply { deleteOnExit() }
        mockkStatic(Environment::class)
        every { Environment.getExternalStoragePublicDirectory(any()) } returns tempDir

        mockkStatic(MediaScannerConnection::class)
        every { MediaScannerConnection.scanFile(any(), any(), any(), any()) } just Runs

        coEvery { recordsRepo.searchBenOnce(any(), any(), any()) } returns listOf(sampleBen(10L))
        val context = mockk<Context>(relaxed = true)

        viewModel.downloadCsv(context)
        advanceUntilIdle()

        val csvFiles = tempDir.listFiles { file -> file.name.startsWith("ABHAUsers_") }
        assertNotNull(csvFiles)
        assertEquals(1, csvFiles!!.size)
        io.mockk.verify { Toast.makeText(context, any<CharSequence>(), Toast.LENGTH_LONG) }
    }
}
