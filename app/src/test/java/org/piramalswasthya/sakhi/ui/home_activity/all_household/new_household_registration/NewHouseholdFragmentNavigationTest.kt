package org.piramalswasthya.sakhi.ui.home_activity.all_household.new_household_registration

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

class NewHouseholdFragmentNavigationTest {

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

    private fun args() = NewHouseholdFragmentArgs(hhId = 10L, isAshaFamily = "v2")

    @Test
    fun constructor_exposesEveryArgument() {
        val a = args()
        assertEquals(10L, a.hhId)
        assertEquals("v2", a.isAshaFamily)
    }

    @Test
    fun dataClassSynthetics_behaveAsValueType() {
        val a = args()
        assertEquals(a, a.copy())
        assertEquals(a.hashCode(), a.copy().hashCode())
        assertEquals(a, a)
        assertFalse(a.equals(null))
        assertFalse(a.equals(Any()))
        assertTrue(a.toString().contains("NewHouseholdFragmentArgs"))
        assertEquals(10L, a.component1())
        assertEquals("v2", a.component2())
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
        assertEquals("v2", handle.get<String>("isAshaFamily"))
        assertEquals(args(), NewHouseholdFragmentArgs.fromSavedStateHandle(handle))
    }

    @Test
    fun fromSavedStateHandle_appliesDefaults_whenOptionalArgumentsMissing() {
        val handle = SavedStateHandle()
        val a = NewHouseholdFragmentArgs.fromSavedStateHandle(handle)
        assertEquals(0L, a.hhId)
        assertEquals("No", a.isAshaFamily)
    }

    @Test
    fun fromBundle_readsEveryArgument() {
        every { bundle.containsKey(any()) } returns true
        every { bundle.getLong("hhId") } returns 10L
        every { bundle.getString("isAshaFamily") } returns "v2"
        assertEquals(args(), NewHouseholdFragmentArgs.fromBundle(bundle))
    }

    @Test
    fun fromBundle_appliesDefaults_whenOptionalArgumentsMissing() {
        every { bundle.containsKey(any()) } returns true
        every { bundle.containsKey("hhId") } returns false
        every { bundle.containsKey("isAshaFamily") } returns false
        val a = NewHouseholdFragmentArgs.fromBundle(bundle)
        assertEquals(0L, a.hhId)
        assertEquals("No", a.isAshaFamily)
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
    fun actionNewHouseholdFragmentToNewBenRegTypeFragment_buildsDirections() {
        val d = NewHouseholdFragmentDirections.actionNewHouseholdFragmentToNewBenRegTypeFragment(hhId = 10L)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = NewHouseholdFragmentDirections.actionNewHouseholdFragmentToNewBenRegTypeFragment(hhId = 10L)
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionNewHouseholdFragmentToNewBenRegFragment_buildsDirections() {
        val d = NewHouseholdFragmentDirections.actionNewHouseholdFragmentToNewBenRegFragment(hhId = 10L, relToHeadId = 21, benId = 30L, selectedBenId = 40L, gender = 51, isAddSpouse = 61)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = NewHouseholdFragmentDirections.actionNewHouseholdFragmentToNewBenRegFragment(hhId = 10L, relToHeadId = 21, benId = 30L, selectedBenId = 40L, gender = 51, isAddSpouse = 61)
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionGlobalPwAncFormFragment_buildsDirections() {
        val d = NewHouseholdFragmentDirections.actionGlobalPwAncFormFragment(benId = 10L, hhId = "v2", visitNumber = 31, fromPmsma = true, lastItemClick = true)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = NewHouseholdFragmentDirections.actionGlobalPwAncFormFragment(benId = 10L, hhId = "v2", visitNumber = 31, fromPmsma = true, lastItemClick = true)
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }
}
