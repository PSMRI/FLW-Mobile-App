package org.piramalswasthya.sakhi.ui.abha_id_activity.aadhaar_id

import android.os.Bundle
import io.mockk.every
import io.mockk.mockkConstructor
import io.mockk.unmockkConstructor
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AadhaarIdFragmentDirectionsTest {

    @Before
    fun setUp() {
        mockkConstructor(Bundle::class)
        every { anyConstructed<Bundle>().putLong(any(), any()) } returns Unit
        every { anyConstructed<Bundle>().putInt(any(), any()) } returns Unit
        every { anyConstructed<Bundle>().putBoolean(any(), any()) } returns Unit
        every { anyConstructed<Bundle>().putString(any(), any()) } returns Unit
    }

    @After
    fun tearDown() {
        unmockkConstructor(Bundle::class)
    }

    @Test
    fun actionAadhaarIdFragmentToAadhaarOtpFragment_buildsDirections() {
        val d = AadhaarIdFragmentDirections.actionAadhaarIdFragmentToAadhaarOtpFragment(txnId = "v1", mobileNumber = "v2")
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = AadhaarIdFragmentDirections.actionAadhaarIdFragmentToAadhaarOtpFragment(txnId = "v1", mobileNumber = "v2")
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionAadhaarIdFragmentToCreateAbhaFragment_buildsDirections() {
        val d = AadhaarIdFragmentDirections.actionAadhaarIdFragmentToCreateAbhaFragment(txnId = "v1", name = "v2", phrAddress = "v3", abhaNumber = "v4", abhaResponse = "v5")
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = AadhaarIdFragmentDirections.actionAadhaarIdFragmentToCreateAbhaFragment(txnId = "v1", name = "v2", phrAddress = "v3", abhaNumber = "v4", abhaResponse = "v5")
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionAadhaarIdFragmentToGenerateMobileOtpFragment_buildsDirections() {
        val d = AadhaarIdFragmentDirections.actionAadhaarIdFragmentToGenerateMobileOtpFragment(txnId = "v1", mobileNumber = "v2")
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = AadhaarIdFragmentDirections.actionAadhaarIdFragmentToGenerateMobileOtpFragment(txnId = "v1", mobileNumber = "v2")
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionAadhaarIdFragmentToAadhaarConsentFragment_buildsDirections() {
        val d = AadhaarIdFragmentDirections.actionAadhaarIdFragmentToAadhaarConsentFragment()
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = AadhaarIdFragmentDirections.actionAadhaarIdFragmentToAadhaarConsentFragment()
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

}
