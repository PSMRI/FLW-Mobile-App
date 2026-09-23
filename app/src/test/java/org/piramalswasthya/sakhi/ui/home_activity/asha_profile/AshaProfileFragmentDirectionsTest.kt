package org.piramalswasthya.sakhi.ui.home_activity.asha_profile

import android.os.Bundle
import io.mockk.every
import io.mockk.mockkConstructor
import io.mockk.unmockkConstructor
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AshaProfileFragmentDirectionsTest {

    @Before
    fun setUp() {
        mockkConstructor(Bundle::class)
        every { anyConstructed<Bundle>().putLong(any(), any()) } returns Unit
        every { anyConstructed<Bundle>().putInt(any(), any()) } returns Unit
        every { anyConstructed<Bundle>().putBoolean(any(), any()) } returns Unit
        every { anyConstructed<Bundle>().putString(any(), any()) } returns Unit
    }

    @After
    fun tearDown() {
        unmockkConstructor(Bundle::class)
    }

    @Test
    fun actionAshaProfileFragmentToNewHouseholdFragment_buildsDirections() {
        val d = AshaProfileFragmentDirections.actionAshaProfileFragmentToNewHouseholdFragment(hhId = 10L, isAshaFamily = "v2")
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = AshaProfileFragmentDirections.actionAshaProfileFragmentToNewHouseholdFragment(hhId = 10L, isAshaFamily = "v2")
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionAshaProfileFragmentToHouseholdMembersFragment_buildsDirections() {
        val d = AshaProfileFragmentDirections.actionAshaProfileFragmentToHouseholdMembersFragment(hhId = 10L, fromDisease = 21, diseaseType = "v3")
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = AshaProfileFragmentDirections.actionAshaProfileFragmentToHouseholdMembersFragment(hhId = 10L, fromDisease = 21, diseaseType = "v3")
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionAshaProfileFragmentToNewBenRegFragment_buildsDirections() {
        val d = AshaProfileFragmentDirections.actionAshaProfileFragmentToNewBenRegFragment(hhId = 10L, relToHeadId = 21, benId = 30L, selectedBenId = 40L, gender = 51, isAddSpouse = 61)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = AshaProfileFragmentDirections.actionAshaProfileFragmentToNewBenRegFragment(hhId = 10L, relToHeadId = 21, benId = 30L, selectedBenId = 40L, gender = 51, isAddSpouse = 61)
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionGlobalPwAncFormFragment_buildsDirections() {
        val d = AshaProfileFragmentDirections.actionGlobalPwAncFormFragment(benId = 10L, hhId = "v2", visitNumber = 31, fromPmsma = true, lastItemClick = true)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = AshaProfileFragmentDirections.actionGlobalPwAncFormFragment(benId = 10L, hhId = "v2", visitNumber = 31, fromPmsma = true, lastItemClick = true)
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

}
