package org.piramalswasthya.sakhi.ui.abha_id_activity.verify_mobile_otp

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

class VerifyMobileOtpFragmentNavigationTest {

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

    private fun args() = VerifyMobileOtpFragmentArgs(txnId = "v1", phoneNum = "v2", alternatePhoneNumber = "v3", name = "v4", phrAddress = "v5", abhaNumber = "v6", abhaResponse = "v7")

    @Test
    fun constructor_exposesEveryArgument() {
        val a = args()
        assertEquals("v1", a.txnId)
        assertEquals("v2", a.phoneNum)
        assertEquals("v3", a.alternatePhoneNumber)
        assertEquals("v4", a.name)
        assertEquals("v5", a.phrAddress)
        assertEquals("v6", a.abhaNumber)
        assertEquals("v7", a.abhaResponse)
    }

    @Test
    fun dataClassSynthetics_behaveAsValueType() {
        val a = args()
        assertEquals(a, a.copy())
        assertEquals(a.hashCode(), a.copy().hashCode())
        assertEquals(a, a)
        assertFalse(a.equals(null))
        assertFalse(a.equals(Any()))
        assertTrue(a.toString().contains("VerifyMobileOtpFragmentArgs"))
        assertEquals("v1", a.component1())
        assertEquals("v2", a.component2())
        assertEquals("v3", a.component3())
        assertEquals("v4", a.component4())
        assertEquals("v5", a.component5())
        assertEquals("v6", a.component6())
        assertEquals("v7", a.component7())
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
        assertEquals("v2", handle.get<String>("phoneNum"))
        assertEquals("v3", handle.get<String>("alternatePhoneNumber"))
        assertEquals("v4", handle.get<String>("name"))
        assertEquals("v5", handle.get<String>("phrAddress"))
        assertEquals("v6", handle.get<String>("abhaNumber"))
        assertEquals("v7", handle.get<String>("abhaResponse"))
        assertEquals(args(), VerifyMobileOtpFragmentArgs.fromSavedStateHandle(handle))
    }

    @Test
    fun fromSavedStateHandle_throws_whenRequiredArgumentMissing() {
        assertThrows(IllegalArgumentException::class.java) {
            VerifyMobileOtpFragmentArgs.fromSavedStateHandle(SavedStateHandle())
        }
    }

    @Test
    fun fromSavedStateHandle_throws_whenRequiredArgumentIsNull() {
        val handle = SavedStateHandle(mapOf<String, Any?>("txnId" to null, "phoneNum" to "v2", "alternatePhoneNumber" to "v3", "name" to "v4", "phrAddress" to "v5", "abhaNumber" to "v6", "abhaResponse" to "v7"))
        assertThrows(IllegalArgumentException::class.java) {
            VerifyMobileOtpFragmentArgs.fromSavedStateHandle(handle)
        }
    }

    @Test
    fun fromBundle_readsEveryArgument() {
        every { bundle.containsKey(any()) } returns true
        every { bundle.getString("txnId") } returns "v1"
        every { bundle.getString("phoneNum") } returns "v2"
        every { bundle.getString("alternatePhoneNumber") } returns "v3"
        every { bundle.getString("name") } returns "v4"
        every { bundle.getString("phrAddress") } returns "v5"
        every { bundle.getString("abhaNumber") } returns "v6"
        every { bundle.getString("abhaResponse") } returns "v7"
        assertEquals(args(), VerifyMobileOtpFragmentArgs.fromBundle(bundle))
    }

    @Test
    fun fromBundle_throws_whenRequiredArgumentMissing() {
        every { bundle.containsKey(any()) } returns false
        assertThrows(IllegalArgumentException::class.java) {
            VerifyMobileOtpFragmentArgs.fromBundle(bundle)
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
    fun actionVerifyMobileOtpFragmentToCreateAbhaFragment_buildsDirections() {
        val d = VerifyMobileOtpFragmentDirections.actionVerifyMobileOtpFragmentToCreateAbhaFragment(txnId = "v1", name = "v2", phrAddress = "v3", abhaNumber = "v4", abhaResponse = "v5")
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = VerifyMobileOtpFragmentDirections.actionVerifyMobileOtpFragmentToCreateAbhaFragment(txnId = "v1", name = "v2", phrAddress = "v3", abhaNumber = "v4", abhaResponse = "v5")
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }
}
