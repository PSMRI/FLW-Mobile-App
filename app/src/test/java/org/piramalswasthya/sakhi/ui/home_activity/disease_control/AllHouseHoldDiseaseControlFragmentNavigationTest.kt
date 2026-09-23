package org.piramalswasthya.sakhi.ui.home_activity.disease_control

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

class AllHouseHoldDiseaseControlFragmentNavigationTest {

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

    private fun args() = AllHouseHoldDiseaseControlFragmentArgs(diseaseType = "v1")

    @Test
    fun constructor_exposesEveryArgument() {
        val a = args()
        assertEquals("v1", a.diseaseType)
    }

    @Test
    fun dataClassSynthetics_behaveAsValueType() {
        val a = args()
        assertEquals(a, a.copy())
        assertEquals(a.hashCode(), a.copy().hashCode())
        assertEquals(a, a)
        assertFalse(a.equals(null))
        assertFalse(a.equals(Any()))
        assertTrue(a.toString().contains("AllHouseHoldDiseaseControlFragmentArgs"))
        assertEquals("v1", a.component1())
    }

    @Test
    fun copy_replacesSingleArgument() {
        val a = args()
        val b = a.copy(diseaseType = "zzz")
        assertEquals("zzz", b.diseaseType)
        assertNotEquals(a, b)
    }

    @Test
    fun toSavedStateHandle_thenFromSavedStateHandle_roundTrips() {
        val handle = args().toSavedStateHandle()
        assertEquals("v1", handle.get<String>("diseaseType"))
        assertEquals(args(), AllHouseHoldDiseaseControlFragmentArgs.fromSavedStateHandle(handle))
    }

    @Test
    fun fromSavedStateHandle_throws_whenRequiredArgumentMissing() {
        assertThrows(IllegalArgumentException::class.java) {
            AllHouseHoldDiseaseControlFragmentArgs.fromSavedStateHandle(SavedStateHandle())
        }
    }

    @Test
    fun fromSavedStateHandle_throws_whenRequiredArgumentIsNull() {
        val handle = SavedStateHandle(mapOf<String, Any?>("diseaseType" to null))
        assertThrows(IllegalArgumentException::class.java) {
            AllHouseHoldDiseaseControlFragmentArgs.fromSavedStateHandle(handle)
        }
    }

    @Test
    fun fromBundle_readsEveryArgument() {
        every { bundle.containsKey(any()) } returns true
        every { bundle.getString("diseaseType") } returns "v1"
        assertEquals(args(), AllHouseHoldDiseaseControlFragmentArgs.fromBundle(bundle))
    }

    @Test
    fun fromBundle_throws_whenRequiredArgumentMissing() {
        every { bundle.containsKey(any()) } returns false
        assertThrows(IllegalArgumentException::class.java) {
            AllHouseHoldDiseaseControlFragmentArgs.fromBundle(bundle)
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
    fun actionAllHouseHoldDiseaseControlFragmentToHouseholdMembersFragment_buildsDirections() {
        val d = AllHouseHoldDiseaseControlFragmentDirections.actionAllHouseHoldDiseaseControlFragmentToHouseholdMembersFragment(hhId = 10L, fromDisease = 21, diseaseType = "v3")
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = AllHouseHoldDiseaseControlFragmentDirections.actionAllHouseHoldDiseaseControlFragmentToHouseholdMembersFragment(hhId = 10L, fromDisease = 21, diseaseType = "v3")
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionAllHouseHoldDiseaseControlFragmentToMalariaSuspectedListFragment_buildsDirections() {
        val d = AllHouseHoldDiseaseControlFragmentDirections.actionAllHouseHoldDiseaseControlFragmentToMalariaSuspectedListFragment(hhId = 10L, fromDisease = 21, diseaseType = "v3")
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = AllHouseHoldDiseaseControlFragmentDirections.actionAllHouseHoldDiseaseControlFragmentToMalariaSuspectedListFragment(hhId = 10L, fromDisease = 21, diseaseType = "v3")
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionAllHouseHoldDiseaseControlFragmentToAESSuspectedListFragment_buildsDirections() {
        val d = AllHouseHoldDiseaseControlFragmentDirections.actionAllHouseHoldDiseaseControlFragmentToAESSuspectedListFragment(hhId = 10L, fromDisease = 21, diseaseType = "v3")
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = AllHouseHoldDiseaseControlFragmentDirections.actionAllHouseHoldDiseaseControlFragmentToAESSuspectedListFragment(hhId = 10L, fromDisease = 21, diseaseType = "v3")
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionAllHouseHoldDiseaseControlFragmentToKalaAzarSuspectedListFragment_buildsDirections() {
        val d = AllHouseHoldDiseaseControlFragmentDirections.actionAllHouseHoldDiseaseControlFragmentToKalaAzarSuspectedListFragment(hhId = 10L, fromDisease = 21, diseaseType = "v3")
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = AllHouseHoldDiseaseControlFragmentDirections.actionAllHouseHoldDiseaseControlFragmentToKalaAzarSuspectedListFragment(hhId = 10L, fromDisease = 21, diseaseType = "v3")
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionAllHouseHoldDiseaseControlFragmentToLeprosySuspectedListFragment_buildsDirections() {
        val d = AllHouseHoldDiseaseControlFragmentDirections.actionAllHouseHoldDiseaseControlFragmentToLeprosySuspectedListFragment(hhId = 10L, fromDisease = 21, diseaseType = "v3")
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = AllHouseHoldDiseaseControlFragmentDirections.actionAllHouseHoldDiseaseControlFragmentToLeprosySuspectedListFragment(hhId = 10L, fromDisease = 21, diseaseType = "v3")
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionAllHouseHoldDiseaseControlFragmentToFilariaSuspectedListFragment_buildsDirections() {
        val d = AllHouseHoldDiseaseControlFragmentDirections.actionAllHouseHoldDiseaseControlFragmentToFilariaSuspectedListFragment(hhId = 10L, fromDisease = 21, diseaseType = "v3")
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = AllHouseHoldDiseaseControlFragmentDirections.actionAllHouseHoldDiseaseControlFragmentToFilariaSuspectedListFragment(hhId = 10L, fromDisease = 21, diseaseType = "v3")
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionAllHouseHoldDiseaseControlFragmentToFilariaMDAFormFragment_buildsDirections() {
        val d = AllHouseHoldDiseaseControlFragmentDirections.actionAllHouseHoldDiseaseControlFragmentToFilariaMDAFormFragment(hhId = 10L)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = AllHouseHoldDiseaseControlFragmentDirections.actionAllHouseHoldDiseaseControlFragmentToFilariaMDAFormFragment(hhId = 10L)
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionGlobalPwAncFormFragment_buildsDirections() {
        val d = AllHouseHoldDiseaseControlFragmentDirections.actionGlobalPwAncFormFragment(benId = 10L, hhId = "v2", visitNumber = 31, fromPmsma = true, lastItemClick = true)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = AllHouseHoldDiseaseControlFragmentDirections.actionGlobalPwAncFormFragment(benId = 10L, hhId = "v2", visitNumber = 31, fromPmsma = true, lastItemClick = true)
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }
}
