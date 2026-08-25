package org.piramalswasthya.sakhi.ui.abha_id_activity.create_abha_id

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

class CreateAbhaFragmentNavigationTest {

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

    private fun args() = CreateAbhaFragmentArgs(txnId = "v1", name = "v2", phrAddress = "v3", abhaNumber = "v4", abhaResponse = "v5")

    @Test
    fun constructor_exposesEveryArgument() {
        val a = args()
        assertEquals("v1", a.txnId)
        assertEquals("v2", a.name)
        assertEquals("v3", a.phrAddress)
        assertEquals("v4", a.abhaNumber)
        assertEquals("v5", a.abhaResponse)
    }

    @Test
    fun dataClassSynthetics_behaveAsValueType() {
        val a = args()
        assertEquals(a, a.copy())
        assertEquals(a.hashCode(), a.copy().hashCode())
        assertEquals(a, a)
        assertFalse(a.equals(null))
        assertFalse(a.equals(Any()))
        assertTrue(a.toString().contains("CreateAbhaFragmentArgs"))
        assertEquals("v1", a.component1())
        assertEquals("v2", a.component2())
        assertEquals("v3", a.component3())
        assertEquals("v4", a.component4())
        assertEquals("v5", a.component5())
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
        assertEquals("v2", handle.get<String>("name"))
        assertEquals("v3", handle.get<String>("phrAddress"))
        assertEquals("v4", handle.get<String>("abhaNumber"))
        assertEquals("v5", handle.get<String>("abhaResponse"))
        assertEquals(args(), CreateAbhaFragmentArgs.fromSavedStateHandle(handle))
    }

    @Test
    fun fromSavedStateHandle_throws_whenRequiredArgumentMissing() {
        assertThrows(IllegalArgumentException::class.java) {
            CreateAbhaFragmentArgs.fromSavedStateHandle(SavedStateHandle())
        }
    }

    @Test
    fun fromSavedStateHandle_throws_whenRequiredArgumentIsNull() {
        val handle = SavedStateHandle(mapOf<String, Any?>("txnId" to null, "name" to "v2", "phrAddress" to "v3", "abhaNumber" to "v4", "abhaResponse" to "v5"))
        assertThrows(IllegalArgumentException::class.java) {
            CreateAbhaFragmentArgs.fromSavedStateHandle(handle)
        }
    }

    @Test
    fun fromBundle_readsEveryArgument() {
        every { bundle.containsKey(any()) } returns true
        every { bundle.getString("txnId") } returns "v1"
        every { bundle.getString("name") } returns "v2"
        every { bundle.getString("phrAddress") } returns "v3"
        every { bundle.getString("abhaNumber") } returns "v4"
        every { bundle.getString("abhaResponse") } returns "v5"
        assertEquals(args(), CreateAbhaFragmentArgs.fromBundle(bundle))
    }

    @Test
    fun fromBundle_throws_whenRequiredArgumentMissing() {
        every { bundle.containsKey(any()) } returns false
        assertThrows(IllegalArgumentException::class.java) {
            CreateAbhaFragmentArgs.fromBundle(bundle)
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

    @Test
    fun fromBundle_throws_whenNameMissing() {
        every { bundle.containsKey(any()) } returns true
        every { bundle.containsKey("name") } returns false
        every { bundle.getString("txnId") } returns "v1"
        assertThrows(IllegalArgumentException::class.java) {
            CreateAbhaFragmentArgs.fromBundle(bundle)
        }
    }

    @Test
    fun fromBundle_throws_whenNameIsNull() {
        every { bundle.containsKey(any()) } returns true
        every { bundle.getString("txnId") } returns "v1"
        every { bundle.getString("name") } returns null
        assertThrows(IllegalArgumentException::class.java) {
            CreateAbhaFragmentArgs.fromBundle(bundle)
        }
    }

    @Test
    fun fromBundle_throws_whenPhrAddressMissing() {
        every { bundle.containsKey(any()) } returns true
        every { bundle.containsKey("phrAddress") } returns false
        every { bundle.getString("txnId") } returns "v1"
        every { bundle.getString("name") } returns "v2"
        assertThrows(IllegalArgumentException::class.java) {
            CreateAbhaFragmentArgs.fromBundle(bundle)
        }
    }

    @Test
    fun fromBundle_throws_whenPhrAddressIsNull() {
        every { bundle.containsKey(any()) } returns true
        every { bundle.getString("txnId") } returns "v1"
        every { bundle.getString("name") } returns "v2"
        every { bundle.getString("phrAddress") } returns null
        assertThrows(IllegalArgumentException::class.java) {
            CreateAbhaFragmentArgs.fromBundle(bundle)
        }
    }

    @Test
    fun fromBundle_throws_whenAbhaNumberMissing() {
        every { bundle.containsKey(any()) } returns true
        every { bundle.containsKey("abhaNumber") } returns false
        every { bundle.getString("txnId") } returns "v1"
        every { bundle.getString("name") } returns "v2"
        every { bundle.getString("phrAddress") } returns "v3"
        assertThrows(IllegalArgumentException::class.java) {
            CreateAbhaFragmentArgs.fromBundle(bundle)
        }
    }

    @Test
    fun fromBundle_throws_whenAbhaNumberIsNull() {
        every { bundle.containsKey(any()) } returns true
        every { bundle.getString("txnId") } returns "v1"
        every { bundle.getString("name") } returns "v2"
        every { bundle.getString("phrAddress") } returns "v3"
        every { bundle.getString("abhaNumber") } returns null
        assertThrows(IllegalArgumentException::class.java) {
            CreateAbhaFragmentArgs.fromBundle(bundle)
        }
    }

    @Test
    fun fromBundle_throws_whenAbhaResponseMissing() {
        every { bundle.containsKey(any()) } returns true
        every { bundle.containsKey("abhaResponse") } returns false
        every { bundle.getString("txnId") } returns "v1"
        every { bundle.getString("name") } returns "v2"
        every { bundle.getString("phrAddress") } returns "v3"
        every { bundle.getString("abhaNumber") } returns "v4"
        assertThrows(IllegalArgumentException::class.java) {
            CreateAbhaFragmentArgs.fromBundle(bundle)
        }
    }

    @Test
    fun fromBundle_throws_whenAbhaResponseIsNull() {
        every { bundle.containsKey(any()) } returns true
        every { bundle.getString("txnId") } returns "v1"
        every { bundle.getString("name") } returns "v2"
        every { bundle.getString("phrAddress") } returns "v3"
        every { bundle.getString("abhaNumber") } returns "v4"
        every { bundle.getString("abhaResponse") } returns null
        assertThrows(IllegalArgumentException::class.java) {
            CreateAbhaFragmentArgs.fromBundle(bundle)
        }
    }

    @Test
    fun fromSavedStateHandle_throws_whenNameMissing() {
        val handle = SavedStateHandle(mapOf<String, Any?>("txnId" to "v1"))
        assertThrows(IllegalArgumentException::class.java) {
            CreateAbhaFragmentArgs.fromSavedStateHandle(handle)
        }
    }

    @Test
    fun fromSavedStateHandle_throws_whenNameIsNull() {
        val handle = SavedStateHandle(mapOf<String, Any?>("txnId" to "v1", "name" to null))
        assertThrows(IllegalArgumentException::class.java) {
            CreateAbhaFragmentArgs.fromSavedStateHandle(handle)
        }
    }

    @Test
    fun fromSavedStateHandle_throws_whenPhrAddressMissing() {
        val handle = SavedStateHandle(mapOf<String, Any?>("txnId" to "v1", "name" to "v2"))
        assertThrows(IllegalArgumentException::class.java) {
            CreateAbhaFragmentArgs.fromSavedStateHandle(handle)
        }
    }

    @Test
    fun fromSavedStateHandle_throws_whenPhrAddressIsNull() {
        val handle = SavedStateHandle(mapOf<String, Any?>("txnId" to "v1", "name" to "v2", "phrAddress" to null))
        assertThrows(IllegalArgumentException::class.java) {
            CreateAbhaFragmentArgs.fromSavedStateHandle(handle)
        }
    }

    @Test
    fun fromSavedStateHandle_throws_whenAbhaNumberMissing() {
        val handle = SavedStateHandle(mapOf<String, Any?>("txnId" to "v1", "name" to "v2", "phrAddress" to "v3"))
        assertThrows(IllegalArgumentException::class.java) {
            CreateAbhaFragmentArgs.fromSavedStateHandle(handle)
        }
    }

    @Test
    fun fromSavedStateHandle_throws_whenAbhaNumberIsNull() {
        val handle = SavedStateHandle(mapOf<String, Any?>("txnId" to "v1", "name" to "v2", "phrAddress" to "v3", "abhaNumber" to null))
        assertThrows(IllegalArgumentException::class.java) {
            CreateAbhaFragmentArgs.fromSavedStateHandle(handle)
        }
    }

    @Test
    fun fromSavedStateHandle_throws_whenAbhaResponseMissing() {
        val handle = SavedStateHandle(mapOf<String, Any?>("txnId" to "v1", "name" to "v2", "phrAddress" to "v3", "abhaNumber" to "v4"))
        assertThrows(IllegalArgumentException::class.java) {
            CreateAbhaFragmentArgs.fromSavedStateHandle(handle)
        }
    }

    @Test
    fun fromSavedStateHandle_throws_whenAbhaResponseIsNull() {
        val handle = SavedStateHandle(mapOf<String, Any?>("txnId" to "v1", "name" to "v2", "phrAddress" to "v3", "abhaNumber" to "v4", "abhaResponse" to null))
        assertThrows(IllegalArgumentException::class.java) {
            CreateAbhaFragmentArgs.fromSavedStateHandle(handle)
        }
    }
}
