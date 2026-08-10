package org.piramalswasthya.sakhi.ui.home_activity.all_ben.new_ben_registration.ben_form

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

class NewBenRegFragmentNavigationTest {

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

    private fun args() = NewBenRegFragmentArgs(hhId = 10L, relToHeadId = 21, benId = 30L, selectedBenId = 40L, gender = 51, isAddSpouse = 61)

    @Test
    fun constructor_exposesEveryArgument() {
        val a = args()
        assertEquals(10L, a.hhId)
        assertEquals(21, a.relToHeadId)
        assertEquals(30L, a.benId)
        assertEquals(40L, a.selectedBenId)
        assertEquals(51, a.gender)
        assertEquals(61, a.isAddSpouse)
    }

    @Test
    fun dataClassSynthetics_behaveAsValueType() {
        val a = args()
        assertEquals(a, a.copy())
        assertEquals(a.hashCode(), a.copy().hashCode())
        assertEquals(a, a)
        assertFalse(a.equals(null))
        assertFalse(a.equals(Any()))
        assertTrue(a.toString().contains("NewBenRegFragmentArgs"))
        assertEquals(10L, a.component1())
        assertEquals(21, a.component2())
        assertEquals(30L, a.component3())
        assertEquals(40L, a.component4())
        assertEquals(51, a.component5())
        assertEquals(61, a.component6())
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
        assertEquals(21, handle.get<Int>("relToHeadId"))
        assertEquals(30L, handle.get<Long>("benId"))
        assertEquals(40L, handle.get<Long>("selectedBenId"))
        assertEquals(51, handle.get<Int>("gender"))
        assertEquals(61, handle.get<Int>("isAddSpouse"))
        assertEquals(args(), NewBenRegFragmentArgs.fromSavedStateHandle(handle))
    }

    @Test
    fun fromSavedStateHandle_appliesDefaults_whenOptionalArgumentsMissing() {
        val handle = SavedStateHandle(mapOf<String, Any?>("hhId" to 10L, "relToHeadId" to 21))
        val a = NewBenRegFragmentArgs.fromSavedStateHandle(handle)
        assertEquals(0L, a.benId)
        assertEquals(0L, a.selectedBenId)
        assertEquals(0, a.gender)
        assertEquals(0, a.isAddSpouse)
    }

    @Test
    fun fromSavedStateHandle_throws_whenRequiredArgumentMissing() {
        assertThrows(IllegalArgumentException::class.java) {
            NewBenRegFragmentArgs.fromSavedStateHandle(SavedStateHandle())
        }
    }

    @Test
    fun fromSavedStateHandle_throws_whenRequiredArgumentIsNull() {
        val handle = SavedStateHandle(mapOf<String, Any?>("hhId" to null, "relToHeadId" to 21, "benId" to 30L, "selectedBenId" to 40L, "gender" to 51, "isAddSpouse" to 61))
        assertThrows(IllegalArgumentException::class.java) {
            NewBenRegFragmentArgs.fromSavedStateHandle(handle)
        }
    }

    @Test
    fun fromBundle_readsEveryArgument() {
        every { bundle.containsKey(any()) } returns true
        every { bundle.getLong("hhId") } returns 10L
        every { bundle.getInt("relToHeadId") } returns 21
        every { bundle.getLong("benId") } returns 30L
        every { bundle.getLong("selectedBenId") } returns 40L
        every { bundle.getInt("gender") } returns 51
        every { bundle.getInt("isAddSpouse") } returns 61
        assertEquals(args(), NewBenRegFragmentArgs.fromBundle(bundle))
    }

    @Test
    fun fromBundle_appliesDefaults_whenOptionalArgumentsMissing() {
        every { bundle.containsKey(any()) } returns true
        every { bundle.containsKey("benId") } returns false
        every { bundle.containsKey("selectedBenId") } returns false
        every { bundle.containsKey("gender") } returns false
        every { bundle.containsKey("isAddSpouse") } returns false
        every { bundle.getLong("hhId") } returns 10L
        every { bundle.getInt("relToHeadId") } returns 21
        val a = NewBenRegFragmentArgs.fromBundle(bundle)
        assertEquals(0L, a.benId)
        assertEquals(0L, a.selectedBenId)
        assertEquals(0, a.gender)
        assertEquals(0, a.isAddSpouse)
    }

    @Test
    fun fromBundle_throws_whenRequiredArgumentMissing() {
        every { bundle.containsKey(any()) } returns false
        assertThrows(IllegalArgumentException::class.java) {
            NewBenRegFragmentArgs.fromBundle(bundle)
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
    fun actionNewBenRegFragmentSelf_buildsDirections() {
        val d = NewBenRegFragmentDirections.actionNewBenRegFragmentSelf(hhId = 10L, relToHeadId = 21, benId = 30L, selectedBenId = 40L, gender = 51, isAddSpouse = 61)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = NewBenRegFragmentDirections.actionNewBenRegFragmentSelf(hhId = 10L, relToHeadId = 21, benId = 30L, selectedBenId = 40L, gender = 51, isAddSpouse = 61)
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionNewChildAsBenRegFragment_buildsDirections() {
        val d = NewBenRegFragmentDirections.actionNewChildAsBenRegFragment(hhId = 10L, relToHeadId = 21, benId = 30L, selectedBenId = 40L, gender = 51, isAddSpouse = 61)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = NewBenRegFragmentDirections.actionNewChildAsBenRegFragment(hhId = 10L, relToHeadId = 21, benId = 30L, selectedBenId = 40L, gender = 51, isAddSpouse = 61)
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionGlobalPwAncFormFragment_buildsDirections() {
        val d = NewBenRegFragmentDirections.actionGlobalPwAncFormFragment(benId = 10L, hhId = "v2", visitNumber = 31, fromPmsma = true, lastItemClick = true)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = NewBenRegFragmentDirections.actionGlobalPwAncFormFragment(benId = 10L, hhId = "v2", visitNumber = 31, fromPmsma = true, lastItemClick = true)
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }
}
