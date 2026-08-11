package org.piramalswasthya.sakhi.ui.abha_id_activity.aadhaar_otp

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkConstructor
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AadhaarOtpFragmentNavigationTest {

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

    private fun args() = AadhaarOtpFragmentArgs(txnId = "v1", mobileNumber = "v2")

    @Test
    fun constructor_exposesEveryArgument() {
        val a = args()
        assertEquals("v1", a.txnId)
        assertEquals("v2", a.mobileNumber)
    }

    @Test
    fun dataClassSynthetics_behaveAsValueType() {
        val a = args()
        assertEquals(a, a.copy())
        assertEquals(a.hashCode(), a.copy().hashCode())
        assertEquals(a, a)
        assertFalse(a.equals(null))
        assertFalse(a.equals(Any()))
        assertTrue(a.toString().contains("AadhaarOtpFragmentArgs"))
        assertEquals("v1", a.component1())
        assertEquals("v2", a.component2())
    }

    @Test
    fun copy_replacesSingleArgument() {
        val a = args()
        val b = a.copy(txnId = "zzz")
        assertEquals("zzz", b.txnId)
        assertNotEquals(a, b)
    }

    @Test
    fun toSavedStateHandle_thenFromSavedStateHandle_roundTrips() {
        val handle = args().toSavedStateHandle()
        assertEquals("v1", handle.get<String>("txnId"))
        assertEquals("v2", handle.get<String>("mobileNumber"))
        assertEquals(args(), AadhaarOtpFragmentArgs.fromSavedStateHandle(handle))
    }

    @Test
    fun fromSavedStateHandle_throws_whenRequiredArgumentMissing() {
        assertThrows(IllegalArgumentException::class.java) {
            AadhaarOtpFragmentArgs.fromSavedStateHandle(SavedStateHandle())
        }
    }

    @Test
    fun fromSavedStateHandle_throws_whenRequiredArgumentIsNull() {
        val handle = SavedStateHandle(mapOf<String, Any?>("txnId" to null, "mobileNumber" to "v2"))
        assertThrows(IllegalArgumentException::class.java) {
            AadhaarOtpFragmentArgs.fromSavedStateHandle(handle)
        }
    }

    @Test
    fun fromBundle_readsEveryArgument() {
        every { bundle.containsKey(any()) } returns true
        every { bundle.getString("txnId") } returns "v1"
        every { bundle.getString("mobileNumber") } returns "v2"
        assertEquals(args(), AadhaarOtpFragmentArgs.fromBundle(bundle))
    }

    @Test
    fun fromBundle_throws_whenRequiredArgumentMissing() {
        every { bundle.containsKey(any()) } returns false
        assertThrows(IllegalArgumentException::class.java) {
            AadhaarOtpFragmentArgs.fromBundle(bundle)
        }
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

    @Before
    fun setUpDirections() {
        mockkConstructor(Bundle::class)
        every { anyConstructed<Bundle>().putLong(any(), any()) } returns Unit
        every { anyConstructed<Bundle>().putInt(any(), any()) } returns Unit
        every { anyConstructed<Bundle>().putBoolean(any(), any()) } returns Unit
        every { anyConstructed<Bundle>().putString(any(), any()) } returns Unit
    }

    @After
    fun tearDownDirections() {
        unmockkConstructor(Bundle::class)
    }

    @Test
    fun actionAadhaarOtpFragmentToGenerateMobileOtpFragment_buildsDirections() {
        val d = AadhaarOtpFragmentDirections.actionAadhaarOtpFragmentToGenerateMobileOtpFragment(txnId = "v1", mobileNumber = "v2")
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = AadhaarOtpFragmentDirections.actionAadhaarOtpFragmentToGenerateMobileOtpFragment(txnId = "v1", mobileNumber = "v2")
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionAadhaarOtpFragmentToCreateAbhaFragment_buildsDirections() {
        val d = AadhaarOtpFragmentDirections.actionAadhaarOtpFragmentToCreateAbhaFragment(txnId = "v1", name = "v2", phrAddress = "v3", abhaNumber = "v4", abhaResponse = "v5")
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = AadhaarOtpFragmentDirections.actionAadhaarOtpFragmentToCreateAbhaFragment(txnId = "v1", name = "v2", phrAddress = "v3", abhaNumber = "v4", abhaResponse = "v5")
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionAadhaarOtpFragmentToVerifyMobileOtpFragment_buildsDirections() {
        val d = AadhaarOtpFragmentDirections.actionAadhaarOtpFragmentToVerifyMobileOtpFragment(txnId = "v1", phoneNum = "v2", alternatePhoneNumber = "v3", name = "v4", phrAddress = "v5", abhaNumber = "v6", abhaResponse = "v7")
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = AadhaarOtpFragmentDirections.actionAadhaarOtpFragmentToVerifyMobileOtpFragment(txnId = "v1", phoneNum = "v2", alternatePhoneNumber = "v3", name = "v4", phrAddress = "v5", abhaNumber = "v6", abhaResponse = "v7")
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }
}
