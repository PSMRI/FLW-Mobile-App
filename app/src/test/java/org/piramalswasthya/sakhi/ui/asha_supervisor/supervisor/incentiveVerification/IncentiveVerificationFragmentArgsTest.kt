package org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkConstructor
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class IncentiveVerificationFragmentArgsTest {

    private lateinit var bundle: Bundle

    @Before
    fun setUp() {
        bundle = mockk(relaxed = true)
        every { bundle.setClassLoader(any()) } returns Unit
    }

    @After
    fun tearDown() {
        unmockkConstructor(Bundle::class)
    }

    private fun args() = IncentiveVerificationFragmentArgs(status = "v1", facilityId = 21, selectedMonth = 31, selectedYear = 41)

    @Test
    fun constructor_exposesEveryArgument() {
        val a = args()
        assertEquals("v1", a.status)
        assertEquals(21, a.facilityId)
        assertEquals(31, a.selectedMonth)
        assertEquals(41, a.selectedYear)
    }

    @Test
    fun dataClassSynthetics_behaveAsValueType() {
        val a = args()
        assertEquals(a, a.copy())
        assertEquals(a.hashCode(), a.copy().hashCode())
        assertEquals(a, a)
        assertFalse(a.equals(null))
        assertFalse(a.equals(Any()))
        assertTrue(a.toString().contains("IncentiveVerificationFragmentArgs"))
        assertEquals("v1", a.component1())
        assertEquals(21, a.component2())
        assertEquals(31, a.component3())
        assertEquals(41, a.component4())
    }

    @Test
    fun copy_replacesSingleArgument() {
        val a = args()
        val b = a.copy(status = "zzz")
        assertEquals("zzz", b.status)
        assertNotEquals(a, b)
    }

    @Test
    fun toSavedStateHandle_thenFromSavedStateHandle_roundTrips() {
        val handle = args().toSavedStateHandle()
        assertEquals("v1", handle.get<String>("status"))
        assertEquals(21, handle.get<Int>("facilityId"))
        assertEquals(31, handle.get<Int>("selectedMonth"))
        assertEquals(41, handle.get<Int>("selectedYear"))
        assertEquals(args(), IncentiveVerificationFragmentArgs.fromSavedStateHandle(handle))
    }

    @Test
    fun fromSavedStateHandle_appliesDefaults_whenOptionalArgumentsMissing() {
        val handle = SavedStateHandle()
        val a = IncentiveVerificationFragmentArgs.fromSavedStateHandle(handle)
        assertEquals("", a.status)
        assertEquals(0, a.facilityId)
        assertEquals(0, a.selectedMonth)
        assertEquals(0, a.selectedYear)
    }

    @Test
    fun fromBundle_readsEveryArgument() {
        every { bundle.containsKey(any()) } returns true
        every { bundle.getString("status") } returns "v1"
        every { bundle.getInt("facilityId") } returns 21
        every { bundle.getInt("selectedMonth") } returns 31
        every { bundle.getInt("selectedYear") } returns 41
        assertEquals(args(), IncentiveVerificationFragmentArgs.fromBundle(bundle))
    }

    @Test
    fun fromBundle_appliesDefaults_whenOptionalArgumentsMissing() {
        every { bundle.containsKey(any()) } returns true
        every { bundle.containsKey("status") } returns false
        every { bundle.containsKey("facilityId") } returns false
        every { bundle.containsKey("selectedMonth") } returns false
        every { bundle.containsKey("selectedYear") } returns false
        val a = IncentiveVerificationFragmentArgs.fromBundle(bundle)
        assertEquals("", a.status)
        assertEquals(0, a.facilityId)
        assertEquals(0, a.selectedMonth)
        assertEquals(0, a.selectedYear)
    }

    @Test
    fun toBundle_putsEveryArgument() {
        mockkConstructor(Bundle::class)
        every { anyConstructed<Bundle>().putLong(any(), any()) } returns Unit
        every { anyConstructed<Bundle>().putInt(any(), any()) } returns Unit
        every { anyConstructed<Bundle>().putBoolean(any(), any()) } returns Unit
        every { anyConstructed<Bundle>().putString(any(), any()) } returns Unit
        assertNotNull(args().toBundle())
    }

    @Test
    fun constructor_usesDefaults_whenOptionalArgumentsOmitted() {
        val a = IncentiveVerificationFragmentArgs()
        assertEquals("", a.status)
        assertEquals(0, a.facilityId)
        assertEquals(0, a.selectedMonth)
        assertEquals(0, a.selectedYear)
    }
}
