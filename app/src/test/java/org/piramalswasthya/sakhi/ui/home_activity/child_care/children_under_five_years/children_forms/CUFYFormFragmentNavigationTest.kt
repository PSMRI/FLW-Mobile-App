package org.piramalswasthya.sakhi.ui.home_activity.child_care.children_under_five_years.children_forms

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

class CUFYFormFragmentNavigationTest {

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

    private fun args() = CUFYFormFragmentArgs(benId = 10L, hhId = 20L, visitType = "v3", isViewMode = true, formDataJson = "v5", recordId = 61, formId = "v7")

    @Test
    fun constructor_exposesEveryArgument() {
        val a = args()
        assertEquals(10L, a.benId)
        assertEquals(20L, a.hhId)
        assertEquals("v3", a.visitType)
        assertEquals(true, a.isViewMode)
        assertEquals("v5", a.formDataJson)
        assertEquals(61, a.recordId)
        assertEquals("v7", a.formId)
    }

    @Test
    fun dataClassSynthetics_behaveAsValueType() {
        val a = args()
        assertEquals(a, a.copy())
        assertEquals(a.hashCode(), a.copy().hashCode())
        assertEquals(a, a)
        assertFalse(a.equals(null))
        assertFalse(a.equals(Any()))
        assertTrue(a.toString().contains("CUFYFormFragmentArgs"))
        assertEquals(10L, a.component1())
        assertEquals(20L, a.component2())
        assertEquals("v3", a.component3())
        assertEquals(true, a.component4())
        assertEquals("v5", a.component5())
        assertEquals(61, a.component6())
        assertEquals("v7", a.component7())
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
        assertEquals("v3", handle.get<String>("visitType"))
        assertEquals(true, handle.get<Boolean>("isViewMode"))
        assertEquals("v5", handle.get<String>("formDataJson"))
        assertEquals(61, handle.get<Int>("recordId"))
        assertEquals("v7", handle.get<String>("formId"))
        assertEquals(args(), CUFYFormFragmentArgs.fromSavedStateHandle(handle))
    }

    @Test
    fun fromSavedStateHandle_appliesDefaults_whenOptionalArgumentsMissing() {
        val handle = SavedStateHandle(mapOf<String, Any?>("benId" to 10L, "hhId" to 20L))
        val a = CUFYFormFragmentArgs.fromSavedStateHandle(handle)
        assertNull(a.visitType)
        assertEquals(false, a.isViewMode)
        assertNull(a.formDataJson)
        assertEquals(0, a.recordId)
        assertEquals("sam_visit_001", a.formId)
    }

    @Test
    fun fromSavedStateHandle_throws_whenRequiredArgumentMissing() {
        assertThrows(IllegalArgumentException::class.java) {
            CUFYFormFragmentArgs.fromSavedStateHandle(SavedStateHandle())
        }
    }

    @Test
    fun fromSavedStateHandle_throws_whenRequiredArgumentIsNull() {
        val handle = SavedStateHandle(mapOf<String, Any?>("benId" to null, "hhId" to 20L, "visitType" to "v3", "isViewMode" to true, "formDataJson" to "v5", "recordId" to 61, "formId" to "v7"))
        assertThrows(IllegalArgumentException::class.java) {
            CUFYFormFragmentArgs.fromSavedStateHandle(handle)
        }
    }

    @Test
    fun fromBundle_readsEveryArgument() {
        every { bundle.containsKey(any()) } returns true
        every { bundle.getLong("benId") } returns 10L
        every { bundle.getLong("hhId") } returns 20L
        every { bundle.getString("visitType") } returns "v3"
        every { bundle.getBoolean("isViewMode") } returns true
        every { bundle.getString("formDataJson") } returns "v5"
        every { bundle.getInt("recordId") } returns 61
        every { bundle.getString("formId") } returns "v7"
        assertEquals(args(), CUFYFormFragmentArgs.fromBundle(bundle))
    }

    @Test
    fun fromBundle_appliesDefaults_whenOptionalArgumentsMissing() {
        every { bundle.containsKey(any()) } returns true
        every { bundle.containsKey("visitType") } returns false
        every { bundle.containsKey("isViewMode") } returns false
        every { bundle.containsKey("formDataJson") } returns false
        every { bundle.containsKey("recordId") } returns false
        every { bundle.containsKey("formId") } returns false
        every { bundle.getLong("benId") } returns 10L
        every { bundle.getLong("hhId") } returns 20L
        val a = CUFYFormFragmentArgs.fromBundle(bundle)
        assertNull(a.visitType)
        assertEquals(false, a.isViewMode)
        assertNull(a.formDataJson)
        assertEquals(0, a.recordId)
        assertEquals("sam_visit_001", a.formId)
    }

    @Test
    fun fromBundle_throws_whenRequiredArgumentMissing() {
        every { bundle.containsKey(any()) } returns false
        assertThrows(IllegalArgumentException::class.java) {
            CUFYFormFragmentArgs.fromBundle(bundle)
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
        val d = CUFYFormFragmentDirections.actionGlobalPwAncFormFragment(benId = 10L, hhId = "v2", visitNumber = 31, fromPmsma = true, lastItemClick = true)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = CUFYFormFragmentDirections.actionGlobalPwAncFormFragment(benId = 10L, hhId = "v2", visitNumber = 31, fromPmsma = true, lastItemClick = true)
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun constructor_usesDefaults_whenOptionalArgumentsOmitted() {
        val a = CUFYFormFragmentArgs(benId = 10L, hhId = 20L)
        assertNull(a.visitType)
        assertFalse(a.isViewMode)
        assertNull(a.formDataJson)
        assertEquals(0, a.recordId)
        assertEquals("sam_visit_001", a.formId)
    }
}
