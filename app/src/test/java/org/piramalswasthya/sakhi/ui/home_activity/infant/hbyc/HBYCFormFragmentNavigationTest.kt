package org.piramalswasthya.sakhi.ui.home_activity.infant.hbyc

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

class HBYCFormFragmentNavigationTest {

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

    private fun args() = HBYCFormFragmentArgs(benId = 10L, hhId = 20L, visitDay = "v3", isViewMode = true, formId = "v5")

    @Test
    fun constructor_exposesEveryArgument() {
        val a = args()
        assertEquals(10L, a.benId)
        assertEquals(20L, a.hhId)
        assertEquals("v3", a.visitDay)
        assertEquals(true, a.isViewMode)
        assertEquals("v5", a.formId)
    }

    @Test
    fun dataClassSynthetics_behaveAsValueType() {
        val a = args()
        assertEquals(a, a.copy())
        assertEquals(a.hashCode(), a.copy().hashCode())
        assertEquals(a, a)
        assertFalse(a.equals(null))
        assertFalse(a.equals(Any()))
        assertTrue(a.toString().contains("HBYCFormFragmentArgs"))
        assertEquals(10L, a.component1())
        assertEquals(20L, a.component2())
        assertEquals("v3", a.component3())
        assertEquals(true, a.component4())
        assertEquals("v5", a.component5())
    }

    @Test
    fun copy_replacesSingleArgument() {
        val a = args()
        val b = a.copy(benId = 99L)
        assertEquals(99L, b.benId)
        assertNotEquals(a, b)
    }

    @Test
    fun toSavedStateHandle_thenFromSavedStateHandle_roundTrips() {
        val handle = args().toSavedStateHandle()
        assertEquals(10L, handle.get<Long>("benId"))
        assertEquals(20L, handle.get<Long>("hhId"))
        assertEquals("v3", handle.get<String>("visitDay"))
        assertEquals(true, handle.get<Boolean>("isViewMode"))
        assertEquals("v5", handle.get<String>("formId"))
        assertEquals(args(), HBYCFormFragmentArgs.fromSavedStateHandle(handle))
    }

    @Test
    fun fromSavedStateHandle_appliesDefaults_whenOptionalArgumentsMissing() {
        val handle = SavedStateHandle(mapOf<String, Any?>("benId" to 10L, "hhId" to 20L))
        val a = HBYCFormFragmentArgs.fromSavedStateHandle(handle)
        assertNull(a.visitDay)
        assertEquals(false, a.isViewMode)
        assertEquals("hbyc_form_001", a.formId)
    }

    @Test
    fun fromSavedStateHandle_throws_whenRequiredArgumentMissing() {
        assertThrows(IllegalArgumentException::class.java) {
            HBYCFormFragmentArgs.fromSavedStateHandle(SavedStateHandle())
        }
    }

    @Test
    fun fromSavedStateHandle_throws_whenRequiredArgumentIsNull() {
        val handle = SavedStateHandle(mapOf<String, Any?>("benId" to null, "hhId" to 20L, "visitDay" to "v3", "isViewMode" to true, "formId" to "v5"))
        assertThrows(IllegalArgumentException::class.java) {
            HBYCFormFragmentArgs.fromSavedStateHandle(handle)
        }
    }

    @Test
    fun fromBundle_readsEveryArgument() {
        every { bundle.containsKey(any()) } returns true
        every { bundle.getLong("benId") } returns 10L
        every { bundle.getLong("hhId") } returns 20L
        every { bundle.getString("visitDay") } returns "v3"
        every { bundle.getBoolean("isViewMode") } returns true
        every { bundle.getString("formId") } returns "v5"
        assertEquals(args(), HBYCFormFragmentArgs.fromBundle(bundle))
    }

    @Test
    fun fromBundle_appliesDefaults_whenOptionalArgumentsMissing() {
        every { bundle.containsKey(any()) } returns true
        every { bundle.containsKey("visitDay") } returns false
        every { bundle.containsKey("isViewMode") } returns false
        every { bundle.containsKey("formId") } returns false
        every { bundle.getLong("benId") } returns 10L
        every { bundle.getLong("hhId") } returns 20L
        val a = HBYCFormFragmentArgs.fromBundle(bundle)
        assertNull(a.visitDay)
        assertEquals(false, a.isViewMode)
        assertEquals("hbyc_form_001", a.formId)
    }

    @Test
    fun fromBundle_throws_whenRequiredArgumentMissing() {
        every { bundle.containsKey(any()) } returns false
        assertThrows(IllegalArgumentException::class.java) {
            HBYCFormFragmentArgs.fromBundle(bundle)
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
    fun actionGlobalPwAncFormFragment_buildsDirections() {
        val d = HBYCFormFragmentDirections.actionGlobalPwAncFormFragment(benId = 10L, hhId = "v2", visitNumber = 31, fromPmsma = true, lastItemClick = true)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = HBYCFormFragmentDirections.actionGlobalPwAncFormFragment(benId = 10L, hhId = "v2", visitNumber = 31, fromPmsma = true, lastItemClick = true)
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }
}
