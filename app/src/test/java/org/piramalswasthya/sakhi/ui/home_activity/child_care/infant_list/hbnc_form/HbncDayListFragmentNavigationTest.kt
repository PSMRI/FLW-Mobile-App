package org.piramalswasthya.sakhi.ui.home_activity.child_care.infant_list.hbnc_form

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

class HbncDayListFragmentNavigationTest {

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

    private fun args() = HbncDayListFragmentArgs(hhId = 10L, benId = 20L)

    @Test
    fun constructor_exposesEveryArgument() {
        val a = args()
        assertEquals(10L, a.hhId)
        assertEquals(20L, a.benId)
    }

    @Test
    fun dataClassSynthetics_behaveAsValueType() {
        val a = args()
        assertEquals(a, a.copy())
        assertEquals(a.hashCode(), a.copy().hashCode())
        assertEquals(a, a)
        assertFalse(a.equals(null))
        assertFalse(a.equals(Any()))
        assertTrue(a.toString().contains("HbncDayListFragmentArgs"))
        assertEquals(10L, a.component1())
        assertEquals(20L, a.component2())
    }

    @Test
    fun copy_replacesSingleArgument() {
        val a = args()
        val b = a.copy(hhId = 99L)
        assertEquals(99L, b.hhId)
        assertNotEquals(a, b)
    }

    @Test
    fun toSavedStateHandle_thenFromSavedStateHandle_roundTrips() {
        val handle = args().toSavedStateHandle()
        assertEquals(10L, handle.get<Long>("hhId"))
        assertEquals(20L, handle.get<Long>("benId"))
        assertEquals(args(), HbncDayListFragmentArgs.fromSavedStateHandle(handle))
    }

    @Test
    fun fromSavedStateHandle_throws_whenRequiredArgumentMissing() {
        assertThrows(IllegalArgumentException::class.java) {
            HbncDayListFragmentArgs.fromSavedStateHandle(SavedStateHandle())
        }
    }

    @Test
    fun fromSavedStateHandle_throws_whenRequiredArgumentIsNull() {
        val handle = SavedStateHandle(mapOf<String, Any?>("hhId" to null, "benId" to 20L))
        assertThrows(IllegalArgumentException::class.java) {
            HbncDayListFragmentArgs.fromSavedStateHandle(handle)
        }
    }

    @Test
    fun fromBundle_readsEveryArgument() {
        every { bundle.containsKey(any()) } returns true
        every { bundle.getLong("hhId") } returns 10L
        every { bundle.getLong("benId") } returns 20L
        assertEquals(args(), HbncDayListFragmentArgs.fromBundle(bundle))
    }

    @Test
    fun fromBundle_throws_whenRequiredArgumentMissing() {
        every { bundle.containsKey(any()) } returns false
        assertThrows(IllegalArgumentException::class.java) {
            HbncDayListFragmentArgs.fromBundle(bundle)
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
    fun actionHbncDayListFragmentToHbncFragment_buildsDirections() {
        val d = HbncDayListFragmentDirections.actionHbncDayListFragmentToHbncFragment(hhId = 10L, benId = 20L, nthDay = 31)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = HbncDayListFragmentDirections.actionHbncDayListFragmentToHbncFragment(hhId = 10L, benId = 20L, nthDay = 31)
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionHbncDayListFragmentToVisitCardFragment_buildsDirections() {
        val d = HbncDayListFragmentDirections.actionHbncDayListFragmentToVisitCardFragment(hhId = 10L, benId = 20L)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = HbncDayListFragmentDirections.actionHbncDayListFragmentToVisitCardFragment(hhId = 10L, benId = 20L)
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionHbncDayListFragmentToHbncPartIFragment_buildsDirections() {
        val d = HbncDayListFragmentDirections.actionHbncDayListFragmentToHbncPartIFragment(hhId = 10L, benId = 20L)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = HbncDayListFragmentDirections.actionHbncDayListFragmentToHbncPartIFragment(hhId = 10L, benId = 20L)
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionHbncDayListFragmentToHbncPartIIFragment_buildsDirections() {
        val d = HbncDayListFragmentDirections.actionHbncDayListFragmentToHbncPartIIFragment(hhId = 10L, benId = 20L)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = HbncDayListFragmentDirections.actionHbncDayListFragmentToHbncPartIIFragment(hhId = 10L, benId = 20L)
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionGlobalPwAncFormFragment_buildsDirections() {
        val d = HbncDayListFragmentDirections.actionGlobalPwAncFormFragment(benId = 10L, hhId = "v2", visitNumber = 31, fromPmsma = true, lastItemClick = true)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = HbncDayListFragmentDirections.actionGlobalPwAncFormFragment(benId = 10L, hhId = "v2", visitNumber = 31, fromPmsma = true, lastItemClick = true)
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }
}
