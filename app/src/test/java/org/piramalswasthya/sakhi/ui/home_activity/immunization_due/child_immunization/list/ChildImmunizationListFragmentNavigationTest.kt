package org.piramalswasthya.sakhi.ui.home_activity.immunization_due.child_immunization.list

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

class ChildImmunizationListFragmentNavigationTest {

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

    private fun args() = ChildImmunizationListFragmentArgs(showDueOnly = true)

    @Test
    fun constructor_exposesEveryArgument() {
        val a = args()
        assertEquals(true, a.showDueOnly)
    }

    @Test
    fun dataClassSynthetics_behaveAsValueType() {
        val a = args()
        assertEquals(a, a.copy())
        assertEquals(a.hashCode(), a.copy().hashCode())
        assertEquals(a, a)
        assertFalse(a.equals(null))
        assertFalse(a.equals(Any()))
        assertTrue(a.toString().contains("ChildImmunizationListFragmentArgs"))
        assertEquals(true, a.component1())
    }

    @Test
    fun copy_replacesSingleArgument() {
        val a = args()
        val b = a.copy(showDueOnly = false)
        assertEquals(false, b.showDueOnly)
        assertNotEquals(a, b)
    }

    @Test
    fun toSavedStateHandle_thenFromSavedStateHandle_roundTrips() {
        val handle = args().toSavedStateHandle()
        assertEquals(true, handle.get<Boolean>("showDueOnly"))
        assertEquals(args(), ChildImmunizationListFragmentArgs.fromSavedStateHandle(handle))
    }

    @Test
    fun fromSavedStateHandle_appliesDefaults_whenOptionalArgumentsMissing() {
        val handle = SavedStateHandle()
        val a = ChildImmunizationListFragmentArgs.fromSavedStateHandle(handle)
        assertEquals(false, a.showDueOnly)
    }

    @Test
    fun fromBundle_readsEveryArgument() {
        every { bundle.containsKey(any()) } returns true
        every { bundle.getBoolean("showDueOnly") } returns true
        assertEquals(args(), ChildImmunizationListFragmentArgs.fromBundle(bundle))
    }

    @Test
    fun fromBundle_appliesDefaults_whenOptionalArgumentsMissing() {
        every { bundle.containsKey(any()) } returns true
        every { bundle.containsKey("showDueOnly") } returns false
        val a = ChildImmunizationListFragmentArgs.fromBundle(bundle)
        assertEquals(false, a.showDueOnly)
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
    fun actionChildImmunizationListFragmentToImmunizationFormFragment_buildsDirections() {
        val d = ChildImmunizationListFragmentDirections.actionChildImmunizationListFragmentToImmunizationFormFragment(vaccineId = 11, benId = 20L, category = "v3")
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = ChildImmunizationListFragmentDirections.actionChildImmunizationListFragmentToImmunizationFormFragment(vaccineId = 11, benId = 20L, category = "v3")
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionGlobalPwAncFormFragment_buildsDirections() {
        val d = ChildImmunizationListFragmentDirections.actionGlobalPwAncFormFragment(benId = 10L, hhId = "v2", visitNumber = 31, fromPmsma = true, lastItemClick = true)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = ChildImmunizationListFragmentDirections.actionGlobalPwAncFormFragment(benId = 10L, hhId = "v2", visitNumber = 31, fromPmsma = true, lastItemClick = true)
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }
}
