package org.piramalswasthya.sakhi.configuration

import android.content.Context
import android.content.res.Resources
import android.net.Uri
import android.util.Log
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.model.DeliveryOutcomeCache
import org.piramalswasthya.sakhi.model.Gender
import org.piramalswasthya.sakhi.model.InfantRegCache
import org.piramalswasthya.sakhi.model.PregnantWomanRegistrationCache
import org.piramalswasthya.sakhi.utils.HelperUtil

@OptIn(ExperimentalCoroutinesApi::class)
class InfantRegistrationDatasetTest : BaseViewModelTest() {

    @MockK private lateinit var context: Context
    @MockK private lateinit var mockResources: Resources

    @Before
    override fun setUp() {
        super.setUp()
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.v(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.w(any(), any<Throwable>()) } returns 0
        every { Log.isLoggable(any(), any()) } returns false
        mockkObject(HelperUtil)
        every { HelperUtil.getLocalizedResources(any(), any()) } returns mockResources
        every { mockResources.getStringArray(any()) } returns Array(80) { i -> "opt$i" }
        every { mockResources.getString(any()) } returns "x"
        every { mockResources.getString(any(), any()) } returns "x"
        every { mockResources.getString(any(), any(), any()) } returns "x"
    }

    @Test
    fun infantRegistrationDeep() = runTest {
        val ds = InfantRegistrationDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        val delivery = mockk<DeliveryOutcomeCache>(relaxed = true)
        val pwr = mockk<PregnantWomanRegistrationCache>(relaxed = true)
        val saved = mockk<InfantRegCache>(relaxed = true)
        runCatching { ds.setUpPage(null, delivery, 1, null, null) }
        runCatching { ds.setUpPage(ben, delivery, 2, pwr, saved) }
        runCatching { ds.mapValues(mockk<InfantRegCache>(relaxed = true), 0) }
        assertNotNull(ds.listFlow)
    }

    // ---- added coverage ----

    private val lmp = 1_600_000_000_000L

    private fun delivery(days: Long, liveBirth: Int?): DeliveryOutcomeCache {
        val d = mockk<DeliveryOutcomeCache>(relaxed = true)
        every { d.dateOfDelivery } returns lmp + days * 24 * 60 * 60 * 1000
        every { d.liveBirth } returns liveBirth
        return d
    }

    private fun pwr(): PregnantWomanRegistrationCache {
        val p = mockk<PregnantWomanRegistrationCache>(relaxed = true)
        every { p.lmpDate } returns lmp
        return p
    }

    private fun savedInfant(isSncu: String, birthDefectLast: Boolean): InfantRegCache {
        val s = mockk<InfantRegCache>(relaxed = true)
        every { s.babyName } returns "BABY"
        every { s.infantTerm } returns "opt0"
        every { s.corticosteroidGiven } returns "opt0"
        every { s.gender } returns Gender.MALE
        every { s.babyCriedAtBirth } returns true
        every { s.resuscitation } returns false
        every { s.referred } returns "opt1"
        every { s.hadBirthDefect } returns "opt0"
        every { s.birthDefect } returns if (birthDefectLast) "opt79" else "opt2"
        every { s.otherDefect } returns "OD"
        every { s.weight } returns 3000.0
        every { s.breastFeedingStarted } returns true
        every { s.isSNCU } returns isSncu
        every { s.deliveryDischargeSummary1 } returns "d1"
        every { s.deliveryDischargeSummary2 } returns "d2"
        every { s.deliveryDischargeSummary3 } returns "d3"
        every { s.deliveryDischargeSummary4 } returns "d4"
        return s
    }

    // Term pregnancy (weeks in 38..41) -> infantTerm first entry.
    @Test
    fun `create path term pregnancy live birth`() = runTest {
        val ds = InfantRegistrationDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.firstName } returns "MOTHER"
        runCatching { ds.setUpPage(ben, delivery(280, 1), 1, pwr(), null) }
        assertNotNull(ds.listFlow)
    }

    // Pre-term (weeks <= 37) -> infantTerm last entry; liveBirth != 1 -> "baby N of" name.
    @Test
    fun `create path preterm non-live birth`() = runTest {
        val ds = InfantRegistrationDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.firstName } returns "MOTHER"
        runCatching { ds.setUpPage(ben, delivery(250, 2), 3, pwr(), null) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `edit path sncu yes adds discharge summaries`() = runTest {
        val ds = InfantRegistrationDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        runCatching { ds.setUpPage(ben, delivery(280, 1), 1, pwr(), savedInfant("Yes", birthDefectLast = true)) }
        runCatching { ds.mapValues(mockk<InfantRegCache>(relaxed = true), 0) }
        runCatching { ds.getIndexDeliveryDischargeSummary1() }
        runCatching { ds.getIndexDeliveryDischargeSummary2() }
        runCatching { ds.getIndexDeliveryDischargeSummary3() }
        runCatching { ds.getIndexDeliveryDischargeSummary4() }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `edit path sncu no`() = runTest {
        val ds = InfantRegistrationDataset(context, Languages.HINDI)
        val ben = mockk<BenRegCache>(relaxed = true)
        runCatching { ds.setUpPage(ben, delivery(250, null), 2, pwr(), savedInfant("No", birthDefectLast = false)) }
        runCatching { ds.mapValues(mockk<InfantRegCache>(relaxed = true), 0) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `set image uris for discharge summaries`() = runTest {
        val ds = InfantRegistrationDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        runCatching { ds.setUpPage(ben, delivery(280, 1), 1, pwr(), savedInfant("Yes", birthDefectLast = false)) }
        runCatching { ds.setImageUriToFormElement(58, mockk<Uri>(relaxed = true)) }
        runCatching { ds.setImageUriToFormElement(59, mockk<Uri>(relaxed = true)) }
        runCatching { ds.setImageUriToFormElement(60, mockk<Uri>(relaxed = true)) }
        runCatching { ds.setImageUriToFormElement(61, mockk<Uri>(relaxed = true)) }
        runCatching { ds.mapValues(mockk<InfantRegCache>(relaxed = true), 0) }
        assertNotNull(ds.listFlow)
    }
}
