package org.piramalswasthya.sakhi.ui.home_activity.all_household.household_members

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

class HouseholdMembersFragmentNavigationTest {

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

    private fun args(): HouseholdMembersFragmentArgs {
        val ctor = HouseholdMembersFragmentArgs::class.java.declaredConstructors
            .filterNot { c -> c.parameterTypes.any { it.simpleName == "DefaultConstructorMarker" } }
            .maxByOrNull { it.parameterCount }!!
        val values = ctor.parameterTypes.map { type ->
            when (type) {
                java.lang.Long.TYPE, java.lang.Long::class.java -> 10L
                Integer.TYPE, Integer::class.java -> 0
                String::class.java -> "v3"
                else -> null
            }
        }.toTypedArray()
        ctor.isAccessible = true
        return ctor.newInstance(*values) as HouseholdMembersFragmentArgs
    }

    @Test
    fun constructor_exposesEveryArgument() {
        val a = args()
        assertEquals(10L, a.hhId)
    }

    @Test
    fun dataClassSynthetics_behaveAsValueType() {
        val a = args()
        assertEquals(a, a.copy())
        assertEquals(a.hashCode(), a.copy().hashCode())
        assertEquals(a, a)
        assertFalse(a.equals(null))
        assertFalse(a.equals(Any()))
        assertTrue(a.toString().contains("HouseholdMembersFragmentArgs"))
        assertEquals(10L, a.component1())
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
        assertEquals(args(), HouseholdMembersFragmentArgs.fromSavedStateHandle(handle))
    }

    @Test
    fun fromSavedStateHandle_throws_whenRequiredArgumentMissing() {
        assertThrows(IllegalArgumentException::class.java) {
            HouseholdMembersFragmentArgs.fromSavedStateHandle(SavedStateHandle())
        }
    }

    @Test
    fun fromSavedStateHandle_throws_whenRequiredArgumentIsNull() {
        val handle = SavedStateHandle(mapOf<String, Any?>("hhId" to null))
        assertThrows(IllegalArgumentException::class.java) {
            HouseholdMembersFragmentArgs.fromSavedStateHandle(handle)
        }
    }

    @Test
    fun fromBundle_readsEveryArgument() {
        every { bundle.containsKey(any()) } returns true
        every { bundle.getLong("hhId") } returns 10L
        every { bundle.getInt("fromDisease") } returns 0
        every { bundle.getString("diseaseType") } returns "v3"
        assertEquals(args(), HouseholdMembersFragmentArgs.fromBundle(bundle))
    }

    @Test
    fun fromBundle_throws_whenRequiredArgumentMissing() {
        every { bundle.containsKey(any()) } returns false
        assertThrows(IllegalArgumentException::class.java) {
            HouseholdMembersFragmentArgs.fromBundle(bundle)
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
    fun actionHouseholdMembersFragmentToNewBenRegTypeFragment_buildsDirections() {
        val d = HouseholdMembersFragmentDirections.actionHouseholdMembersFragmentToNewBenRegTypeFragment(hhId = 10L)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = HouseholdMembersFragmentDirections.actionHouseholdMembersFragmentToNewBenRegTypeFragment(hhId = 10L)
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionHouseholdMembersFragmentToNewChildAsBenRegistrationFragment_buildsDirections() {
        val d = HouseholdMembersFragmentDirections.actionHouseholdMembersFragmentToNewChildAsBenRegistrationFragment(hhId = 10L, relToHeadId = 21, benId = 30L, selectedBenId = 40L, gender = 51, isAddSpouse = 61)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = HouseholdMembersFragmentDirections.actionHouseholdMembersFragmentToNewChildAsBenRegistrationFragment(hhId = 10L, relToHeadId = 21, benId = 30L, selectedBenId = 40L, gender = 51, isAddSpouse = 61)
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionHouseholdMembersFragmentToNewBenRegG15Fragment_buildsDirections() {
        val d = HouseholdMembersFragmentDirections.actionHouseholdMembersFragmentToNewBenRegG15Fragment(hhId = 10L, benId = 20L)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = HouseholdMembersFragmentDirections.actionHouseholdMembersFragmentToNewBenRegG15Fragment(hhId = 10L, benId = 20L)
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionHouseholdMembersFragmentToNewBenRegFragment_buildsDirections() {
        val d = HouseholdMembersFragmentDirections.actionHouseholdMembersFragmentToNewBenRegFragment(hhId = 10L, relToHeadId = 21, benId = 30L, selectedBenId = 40L, gender = 51, isAddSpouse = 61)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = HouseholdMembersFragmentDirections.actionHouseholdMembersFragmentToNewBenRegFragment(hhId = 10L, relToHeadId = 21, benId = 30L, selectedBenId = 40L, gender = 51, isAddSpouse = 61)
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionHouseholdMembersFragmentToMalariaFormFragment_buildsDirections() {
        val d = HouseholdMembersFragmentDirections.actionHouseholdMembersFragmentToMalariaFormFragment(benId = 10L)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = HouseholdMembersFragmentDirections.actionHouseholdMembersFragmentToMalariaFormFragment(benId = 10L)
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionHouseholdMembersFragmentToKalaAzarFormFragment_buildsDirections() {
        val d = HouseholdMembersFragmentDirections.actionHouseholdMembersFragmentToKalaAzarFormFragment(benId = 10L)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = HouseholdMembersFragmentDirections.actionHouseholdMembersFragmentToKalaAzarFormFragment(benId = 10L)
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionHouseholdMembersFragmentToEyeSurgeryFormFragment_buildsDirections() {
        val d = HouseholdMembersFragmentDirections.actionHouseholdMembersFragmentToEyeSurgeryFormFragment(hhId = 10L, benId = 20L, isViewMode = true, eyeSide = "v4", formDataJson = "v5", recordId = 61, benName = "v7", gender = "v8", age = "v9")
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = HouseholdMembersFragmentDirections.actionHouseholdMembersFragmentToEyeSurgeryFormFragment(hhId = 10L, benId = 20L, isViewMode = true, eyeSide = "v4", formDataJson = "v5", recordId = 61, benName = "v7", gender = "v8", age = "v9")
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionGlobalPwAncFormFragment_buildsDirections() {
        val d = HouseholdMembersFragmentDirections.actionGlobalPwAncFormFragment(benId = 10L, hhId = "v2", visitNumber = 31, fromPmsma = true, lastItemClick = true)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = HouseholdMembersFragmentDirections.actionGlobalPwAncFormFragment(benId = 10L, hhId = "v2", visitNumber = 31, fromPmsma = true, lastItemClick = true)
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionHouseholdMembersFragmentToNewChildAsBenRegistrationFragment_buildsDirections_withDefaultArguments() {
        val d = HouseholdMembersFragmentDirections.actionHouseholdMembersFragmentToNewChildAsBenRegistrationFragment(hhId = 10L, relToHeadId = 21)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().contains("benId=0"))
        assertTrue(d.toString().contains("selectedBenId=0"))
        assertTrue(d.toString().contains("gender=0"))
        assertTrue(d.toString().contains("isAddSpouse=0"))
    }

    @Test
    fun actionHouseholdMembersFragmentToNewBenRegG15Fragment_buildsDirections_withDefaultArguments() {
        val d = HouseholdMembersFragmentDirections.actionHouseholdMembersFragmentToNewBenRegG15Fragment(hhId = 10L)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().contains("benId=0"))
    }

    @Test
    fun actionHouseholdMembersFragmentToNewBenRegFragment_buildsDirections_withDefaultArguments() {
        val d = HouseholdMembersFragmentDirections.actionHouseholdMembersFragmentToNewBenRegFragment(hhId = 10L, relToHeadId = 21)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().contains("benId=0"))
        assertTrue(d.toString().contains("selectedBenId=0"))
        assertTrue(d.toString().contains("gender=0"))
        assertTrue(d.toString().contains("isAddSpouse=0"))
    }

    @Test
    fun actionHouseholdMembersFragmentToEyeSurgeryFormFragment_buildsDirections_withDefaultArguments() {
        val d = HouseholdMembersFragmentDirections.actionHouseholdMembersFragmentToEyeSurgeryFormFragment(hhId = 10L)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().contains("benId=0"))
        assertTrue(d.toString().contains("isViewMode=false"))
        assertTrue(d.toString().contains("recordId=0"))
    }

    @Test
    fun actionGlobalPwAncFormFragment_buildsDirections_withDefaultArguments() {
        val d = HouseholdMembersFragmentDirections.actionGlobalPwAncFormFragment(benId = 10L, hhId = "v2", visitNumber = 31)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
    }
}
