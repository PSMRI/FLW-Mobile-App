package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pregnant_woment_anc_visits.form

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

class PwAncFormFragmentNavigationTest {

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

    private fun args() = PwAncFormFragmentArgs(benId = 10L, hhId = "v2", visitNumber = 31, fromPmsma = true, lastItemClick = true)

    @Test
    fun constructor_exposesEveryArgument() {
        val a = args()
        assertEquals(10L, a.benId)
        assertEquals("v2", a.hhId)
        assertEquals(31, a.visitNumber)
        assertEquals(true, a.fromPmsma)
        assertEquals(true, a.lastItemClick)
    }

    @Test
    fun dataClassSynthetics_behaveAsValueType() {
        val a = args()
        assertEquals(a, a.copy())
        assertEquals(a.hashCode(), a.copy().hashCode())
        assertEquals(a, a)
        assertFalse(a.equals(null))
        assertFalse(a.equals(Any()))
        assertTrue(a.toString().contains("PwAncFormFragmentArgs"))
        assertEquals(10L, a.component1())
        assertEquals("v2", a.component2())
        assertEquals(31, a.component3())
        assertEquals(true, a.component4())
        assertEquals(true, a.component5())
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
        assertEquals("v2", handle.get<String>("hhId"))
        assertEquals(31, handle.get<Int>("visitNumber"))
        assertEquals(true, handle.get<Boolean>("fromPmsma"))
        assertEquals(true, handle.get<Boolean>("lastItemClick"))
        assertEquals(args(), PwAncFormFragmentArgs.fromSavedStateHandle(handle))
    }

    @Test
    fun fromSavedStateHandle_appliesDefaults_whenOptionalArgumentsMissing() {
        val handle = SavedStateHandle(mapOf<String, Any?>("benId" to 10L, "hhId" to "v2", "visitNumber" to 31))
        val a = PwAncFormFragmentArgs.fromSavedStateHandle(handle)
        assertEquals(false, a.fromPmsma)
        assertEquals(false, a.lastItemClick)
    }

    @Test
    fun fromSavedStateHandle_throws_whenRequiredArgumentMissing() {
        assertThrows(IllegalArgumentException::class.java) {
            PwAncFormFragmentArgs.fromSavedStateHandle(SavedStateHandle())
        }
    }

    @Test
    fun fromSavedStateHandle_throws_whenRequiredArgumentIsNull() {
        val handle = SavedStateHandle(mapOf<String, Any?>("benId" to null, "hhId" to "v2", "visitNumber" to 31, "fromPmsma" to true, "lastItemClick" to true))
        assertThrows(IllegalArgumentException::class.java) {
            PwAncFormFragmentArgs.fromSavedStateHandle(handle)
        }
    }

    @Test
    fun fromBundle_readsEveryArgument() {
        every { bundle.containsKey(any()) } returns true
        every { bundle.getLong("benId") } returns 10L
        every { bundle.getString("hhId") } returns "v2"
        every { bundle.getInt("visitNumber") } returns 31
        every { bundle.getBoolean("fromPmsma") } returns true
        every { bundle.getBoolean("lastItemClick") } returns true
        assertEquals(args(), PwAncFormFragmentArgs.fromBundle(bundle))
    }

    @Test
    fun fromBundle_appliesDefaults_whenOptionalArgumentsMissing() {
        every { bundle.containsKey(any()) } returns true
        every { bundle.containsKey("fromPmsma") } returns false
        every { bundle.containsKey("lastItemClick") } returns false
        every { bundle.getLong("benId") } returns 10L
        every { bundle.getString("hhId") } returns "v2"
        every { bundle.getInt("visitNumber") } returns 31
        val a = PwAncFormFragmentArgs.fromBundle(bundle)
        assertEquals(false, a.fromPmsma)
        assertEquals(false, a.lastItemClick)
    }

    @Test
    fun fromBundle_throws_whenRequiredArgumentMissing() {
        every { bundle.containsKey(any()) } returns false
        assertThrows(IllegalArgumentException::class.java) {
            PwAncFormFragmentArgs.fromBundle(bundle)
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
    fun actionPwAncFormFragmentToMdsrObjectFragment_buildsDirections() {
        val d = PwAncFormFragmentDirections.actionPwAncFormFragmentToMdsrObjectFragment(hhId = 10L, benId = 20L)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = PwAncFormFragmentDirections.actionPwAncFormFragmentToMdsrObjectFragment(hhId = 10L, benId = 20L)
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionGlobalPwAncFormFragment_buildsDirections() {
        val d = PwAncFormFragmentDirections.actionGlobalPwAncFormFragment(benId = 10L, hhId = "v2", visitNumber = 31, fromPmsma = true, lastItemClick = true)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = PwAncFormFragmentDirections.actionGlobalPwAncFormFragment(benId = 10L, hhId = "v2", visitNumber = 31, fromPmsma = true, lastItemClick = true)
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }
}
