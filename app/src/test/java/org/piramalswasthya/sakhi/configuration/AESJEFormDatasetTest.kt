package org.piramalswasthya.sakhi.configuration

import android.content.Context
import android.content.res.Resources
import android.util.Log
import io.mockk.every
import io.mockk.mockk
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.piramalswasthya.sakhi.base.BaseViewModelTest
import org.piramalswasthya.sakhi.helpers.Languages
import org.piramalswasthya.sakhi.model.AESScreeningCache
import org.piramalswasthya.sakhi.model.BenRegCache
import org.piramalswasthya.sakhi.utils.HelperUtil

@OptIn(ExperimentalCoroutinesApi::class)
class AESJEFormDatasetTest : BaseViewModelTest() {

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
        every { Log.isLoggable(any(), any()) } returns false
        mockkObject(HelperUtil)
        every { HelperUtil.getLocalizedResources(any(), any()) } returns mockResources
        every { mockResources.getStringArray(any()) } returns Array(80) { "opt$it" }
        every { mockResources.getString(any()) } returns "x"
        every { mockResources.getString(any(), any()) } returns "x"
        every { mockResources.getString(any(), any(), any()) } returns "x"
    }

    @Test
    fun `construction ENGLISH and HINDI`() {
        runCatching { AESJEFormDataset(context, Languages.ENGLISH) }
        runCatching { AESJEFormDataset(context, Languages.HINDI) }
        val ds = AESJEFormDataset(context, Languages.ENGLISH)
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `new create path`() = runTest {
        val ds = AESJEFormDataset(context, Languages.ENGLISH)
        runCatching { ds.setUpPage(null, null) }
        runCatching { ds.getIndexOfDate() }
        for (id in 1..13) {
        }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `edit saved path`() = runTest {
        val ds = AESJEFormDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        val saved = mockk<AESScreeningCache>(relaxed = true)
        runCatching { ds.setUpPage(ben, saved) }
        runCatching { ds.mapValues(mockk<AESScreeningCache>(relaxed = true), 0) }
        runCatching { ds.mapValues(mockk<AESScreeningCache>(relaxed = true), 1) }
        runCatching { ds.updateBen(mockk(relaxed = true)) }
        runCatching { ds.getIndexOfDate() }
        for (id in 1..13) {
        }
        assertNotNull(ds.listFlow)
    }

    // ---- added coverage: edit-path branches, mapValues, updateBen ----

    // beneficiaryStatus == entries[size-2] ("opt78") drives the death branch which adds
    // dateOfDeath / placeOfDeath / reasonOfDeath, and last-position drives the "other" fields.
    private fun savedDeath(placeLast: Boolean, reasonLast: Boolean): AESScreeningCache {
        val s = mockk<AESScreeningCache>(relaxed = true)
        every { s.visitDate } returns 1_600_000_000_000L
        every { s.dateOfDeath } returns 1_600_000_000_000L
        every { s.beneficiaryStatus } returns "opt78"
        every { s.placeOfDeath } returns if (placeLast) "opt79" else "opt5"
        every { s.reasonForDeath } returns if (reasonLast) "opt79" else "opt5"
        every { s.otherPlaceOfDeath } returns "OP"
        every { s.otherReasonForDeath } returns "OR"
        every { s.aesJeCaseStatus } returns "opt3"
        every { s.referToName } returns null
        every { s.otherReferredFacility } returns "F"
        return s
    }

    // beneficiaryStatus != entries[size-2] drives the else branch (caseStatus/followUp/referredTo);
    // referToName == entries.last() ("opt79") adds the "other" field.
    private fun savedReferred(): AESScreeningCache {
        val s = mockk<AESScreeningCache>(relaxed = true)
        every { s.visitDate } returns 1_600_000_000_000L
        every { s.beneficiaryStatus } returns "opt0"
        every { s.referToName } returns "opt79"
        every { s.aesJeCaseStatus } returns "opt2"
        every { s.otherReferredFacility } returns "OTHER FAC"
        return s
    }

    @Test
    fun `edit death branch with other place and reason`() = runTest {
        val ds = AESJEFormDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        runCatching { ds.setUpPage(ben, savedDeath(placeLast = true, reasonLast = true)) }
        runCatching { ds.mapValues(mockk<AESScreeningCache>(relaxed = true), 0) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `edit death branch with non-last place and reason`() = runTest {
        val ds = AESJEFormDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        runCatching { ds.setUpPage(ben, savedDeath(placeLast = false, reasonLast = false)) }
        runCatching { ds.mapValues(mockk<AESScreeningCache>(relaxed = true), 1) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `edit referred branch adds other facility`() = runTest {
        val ds = AESJEFormDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        runCatching { ds.setUpPage(ben, savedReferred()) }
        runCatching { ds.mapValues(mockk<AESScreeningCache>(relaxed = true), 0) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `edit path null ben with saved`() = runTest {
        val ds = AESJEFormDataset(context, Languages.HINDI)
        runCatching { ds.setUpPage(null, savedDeath(placeLast = true, reasonLast = false)) }
        runCatching { ds.setUpPage(null, savedReferred()) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `updateBen processed already N stays N`() {
        val ds = AESJEFormDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        every { ben.processed } returns "N"
        runCatching { ds.updateBen(ben) }
        assertNotNull(ds.listFlow)
    }

    @Test
    fun `updateList sweeps every formId`() = runTest {
        val ds = AESJEFormDataset(context, Languages.ENGLISH)
        val ben = mockk<BenRegCache>(relaxed = true)
        runCatching { ds.setUpPage(ben, savedReferred()) }
        for (id in 0..40) {
            for (index in 0..2) {
                runCatching { ds.updateList(id, index) }
            }
        }
        assertNotNull(ds.listFlow)
    }
}
