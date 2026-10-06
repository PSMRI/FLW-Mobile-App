package org.piramalswasthya.sakhi.ui.home_activity.child_care.infant_list.hbnc_form

import android.app.Application
import android.content.res.Configuration
import android.content.res.Resources
import androidx.lifecycle.SavedStateHandle
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.HBNCCache
import org.piramalswasthya.sakhi.repositories.BenRepo
import org.piramalswasthya.sakhi.repositories.HbncRepo
import java.util.Calendar

/**
 * Unit tests for [HbncDayListViewModel]. `dayList` builds up a header icon list where each later
 * icon (part I, part II, numbered days) only appears once the previous milestone is filled, and the
 * numbered-day icons are further filtered to only those already reachable from the beneficiary's dob.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HbncDayListViewModelTest : BaseViewModelTest() {

    @MockK
    private lateinit var context: Application

    @MockK
    private lateinit var benRepo: BenRepo

    @MockK
    private lateinit var hbncRepo: HbncRepo

    @MockK
    private lateinit var preferenceDao: PreferenceDao

    private lateinit var mockResources: Resources

    private fun mockLocalizedResources() {
        mockResources = mockk(relaxed = true)
        mockkConstructor(Configuration::class)
        every { anyConstructed<Configuration>().setLocale(any()) } just Runs
        every { context.resources } returns mockResources
        every { context.createConfigurationContext(any()) } returns context
        every { mockResources.getString(any()) } returns "x"
    }

    private fun ben(dob: Long): BenRegCache {
        val b = mockk<BenRegCache>(relaxed = true)
        every { b.dob } returns dob
        return b
    }

    private fun buildVm(hhId: Long = 1L, benId: Long = 2L): HbncDayListViewModel {
        every { preferenceDao.getCurrentLanguage() } returns Languages.ENGLISH
        return HbncDayListViewModel(
            context,
            benRepo,
            hbncRepo,
            preferenceDao,
            SavedStateHandle(mapOf("hhId" to hhId, "benId" to benId))
        )
    }

    @Test
    fun `dayList only shows the visit card icon when nothing is filled yet`() = runTest {
        mockLocalizedResources()
        every { hbncRepo.hbncList(any(), any()) } returns flowOf(emptyList())
        coEvery { benRepo.getBeneficiaryRecord(any(), any()) } returns ben(Calendar.getInstance().timeInMillis)

        val vm = buildVm()
        val list = vm.dayList.first()

        assertEquals(1, list.size)
        assertEquals(Konstants.hbncCardDay, list[0].count)
        assertTrue(!list[0].isFilled)
    }

    @Test
    fun `dayList adds part I icon once the visit card is filled`() = runTest {
        mockLocalizedResources()
        val cardEntry = HBNCCache(
            benId = 2L, hhId = 1L, homeVisitDate = Konstants.hbncCardDay, syncState = SyncState.SYNCED
        )
        every { hbncRepo.hbncList(any(), any()) } returns flowOf(listOf(cardEntry))
        coEvery { benRepo.getBeneficiaryRecord(any(), any()) } returns ben(Calendar.getInstance().timeInMillis)

        val vm = buildVm()
        val list = vm.dayList.first()

        assertEquals(2, list.size)
        assertEquals(Konstants.hbncPart1Day, list[1].count)
        assertEquals(SyncState.SYNCED, list[0].syncState)
    }

    @Test
    fun `dayList stops at part II when part I is not filled`() = runTest {
        mockLocalizedResources()
        val cardEntry = HBNCCache(
            benId = 2L, hhId = 1L, homeVisitDate = Konstants.hbncCardDay, syncState = SyncState.UNSYNCED
        )
        val partIIEntry = HBNCCache(
            benId = 2L, hhId = 1L, homeVisitDate = Konstants.hbncPart2Day, syncState = SyncState.UNSYNCED
        )
        every { hbncRepo.hbncList(any(), any()) } returns flowOf(listOf(cardEntry, partIIEntry))
        coEvery { benRepo.getBeneficiaryRecord(any(), any()) } returns ben(Calendar.getInstance().timeInMillis)

        val vm = buildVm()
        val list = vm.dayList.first()

        assertEquals(2, list.size)
        assertEquals(Konstants.hbncPart1Day, list[1].count)
    }

    @Test
    fun `dayList adds all numbered day icons once part II is filled and dob is in the past`() = runTest {
        // Note: production's day-eligibility filter divides then multiplies by 60*60*24, which
        // inflates any positive (dob-in-the-past) diff far past the max day value (42) - so every
        // numbered day icon is added regardless of how old the beneficiary actually is. Tested as-is.
        mockLocalizedResources()
        val cardEntry = HBNCCache(
            benId = 2L, hhId = 1L, homeVisitDate = Konstants.hbncCardDay, syncState = SyncState.UNSYNCED
        )
        val partIEntry = HBNCCache(
            benId = 2L, hhId = 1L, homeVisitDate = Konstants.hbncPart1Day, syncState = SyncState.UNSYNCED
        )
        val partIIEntry = HBNCCache(
            benId = 2L, hhId = 1L, homeVisitDate = Konstants.hbncPart2Day, syncState = SyncState.UNSYNCED
        )
        every { hbncRepo.hbncList(any(), any()) } returns
            flowOf(listOf(cardEntry, partIEntry, partIIEntry))
        val fiveDaysAgo = Calendar.getInstance().timeInMillis - 5L * 24 * 60 * 60 * 1000
        coEvery { benRepo.getBeneficiaryRecord(any(), any()) } returns ben(fiveDaysAgo)

        val vm = buildVm()
        val list = vm.dayList.first()

        val dayCounts = list.drop(3).map { it.count }
        assertEquals(listOf(1, 3, 7, 14, 21, 28, 42), dayCounts)
    }

    @Test
    fun `dayList adds no numbered day icons when dob is in the future`() = runTest {
        mockLocalizedResources()
        val cardEntry = HBNCCache(
            benId = 2L, hhId = 1L, homeVisitDate = Konstants.hbncCardDay, syncState = SyncState.UNSYNCED
        )
        val partIEntry = HBNCCache(
            benId = 2L, hhId = 1L, homeVisitDate = Konstants.hbncPart1Day, syncState = SyncState.UNSYNCED
        )
        val partIIEntry = HBNCCache(
            benId = 2L, hhId = 1L, homeVisitDate = Konstants.hbncPart2Day, syncState = SyncState.UNSYNCED
        )
        every { hbncRepo.hbncList(any(), any()) } returns
            flowOf(listOf(cardEntry, partIEntry, partIIEntry))
        val inTheFuture = Calendar.getInstance().timeInMillis + 5L * 24 * 60 * 60 * 1000
        coEvery { benRepo.getBeneficiaryRecord(any(), any()) } returns ben(inTheFuture)

        val vm = buildVm()
        val list = vm.dayList.first()

        assertEquals(3, list.size)
    }
}
