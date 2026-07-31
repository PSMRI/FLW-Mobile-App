package org.piramalswasthya.sakhi.configuration

import android.content.Context
import android.content.res.Resources
import android.util.Log
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.database.room.SyncState
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.FormElement
import org.piramalswasthya.sakhi.model.InputType
import org.piramalswasthya.sakhi.model.PregnantWomanAncCache
import org.piramalswasthya.sakhi.model.PregnantWomanRegistrationCache
import org.piramalswasthya.sakhi.utils.HelperUtil
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
class PregnantWomanAncVisitDatasetTest : BaseViewModelTest() {

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var mockResources: Resources

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.isLoggable(any(), any()) } returns false
        mockkObject(HelperUtil)
        every { HelperUtil.getLocalizedResources(any(), any()) } returns mockResources
        every { mockResources.getStringArray(any()) } returns arrayOf("No", "Yes")
        every { mockResources.getString(any()) } returns ""
        every { mockResources.getString(any(), any()) } returns ""
    }

    private fun registration(lmpDate: Long) = PregnantWomanRegistrationCache(
        benId = BEN_ID,
        lmpDate = lmpDate,
        createdBy = "test",
        updatedBy = "test",
        syncState = SyncState.UNSYNCED,
    )

    private fun savedVisit(ancDate: Long) = PregnantWomanAncCache(
        benId = BEN_ID,
        visitNumber = 1,
        ancDate = ancDate,
        createdBy = "test",
        updatedBy = "test",
        syncState = SyncState.UNSYNCED,
        frontFilePath = null,
        backFilePath = null,
    )

    /**
     * Builds the ANC visit form for a saved visit falling exactly [weeks] weeks after LMP
     * and returns the Weeks Of Pregnancy element, which also carries Days in its
     * secondary (adjacent) slot.
     */
    private suspend fun weeksElementFor(weeks: Int): FormElement {
        val dataset = PregnantWomanAncVisitDataset(context, Languages.ENGLISH)
        val ancDate = LMP_DATE + TimeUnit.DAYS.toMillis(weeks * 7L)
        dataset.setUpPage(
            visitNumber = 1,
            ben = null,
            regis = registration(LMP_DATE),
            lastAnc = null,
            isFromPmsma = false,
            saved = savedVisit(ancDate),
        )
        return dataset.listFlow.value.first { it.id == WEEKS_OF_PREGNANCY_ID }
    }

    @Test
    fun `days of pregnancy is 140 for 20 weeks`() = runTest {
        val weeksElement = weeksElementFor(20)
        assertEquals("20", weeksElement.value)
        assertEquals("140", weeksElement.secondaryValue)
    }

    @Test
    fun `days of pregnancy is 224 for 32 weeks`() = runTest {
        val weeksElement = weeksElementFor(32)
        assertEquals("32", weeksElement.value)
        assertEquals("224", weeksElement.secondaryValue)
    }

    @Test
    fun `days of pregnancy stays weeks times seven across the ANC range`() = runTest {
        for (weeks in listOf(4, 12, 13, 24, 36, 40)) {
            assertEquals(
                "days mismatch for $weeks weeks",
                (weeks * 7).toString(),
                weeksElementFor(weeks).secondaryValue,
            )
        }
    }

    @Test
    fun `days of pregnancy is rendered adjacent to weeks in the same row`() = runTest {
        val weeksElement = weeksElementFor(20)
        assertEquals(InputType.TEXT_VIEW_PAIR, weeksElement.inputType)
        assertEquals("", weeksElement.secondaryTitle)
    }

    @Test
    fun `weeks of pregnancy row stays optional and is the only pair element`() = runTest {
        val dataset = PregnantWomanAncVisitDataset(context, Languages.ENGLISH)
        val ancDate = LMP_DATE + TimeUnit.DAYS.toMillis(140)
        dataset.setUpPage(
            visitNumber = 1,
            ben = null,
            regis = registration(LMP_DATE),
            lastAnc = null,
            isFromPmsma = false,
            saved = savedVisit(ancDate),
        )
        val pairElements =
            dataset.listFlow.value.filter { it.inputType == InputType.TEXT_VIEW_PAIR }
        assertEquals(1, pairElements.size)
        assertEquals(WEEKS_OF_PREGNANCY_ID, pairElements.first().id)
        assertFalse(pairElements.first().required)
    }

    companion object {
        private const val BEN_ID = 1L

        // Fixed LMP — only the delta from this date drives the calculation.
        private const val LMP_DATE = 1704067200000L

        private const val WEEKS_OF_PREGNANCY_ID = 2
    }
}
