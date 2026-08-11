package org.piramalswasthya.sakhi.ui.home_activity.hrp_cases

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

class HrpCasesFragmentDirectionsTest {

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
    fun actionHrpCasesFragmentToHRPPregnantFragment_buildsDirections() {
        val d = HrpCasesFragmentDirections.actionHrpCasesFragmentToHRPPregnantFragment()
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = HrpCasesFragmentDirections.actionHrpCasesFragmentToHRPPregnantFragment()
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionHrpCasesFragmentToHRPNonPregnantFragment_buildsDirections() {
        val d = HrpCasesFragmentDirections.actionHrpCasesFragmentToHRPNonPregnantFragment()
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = HrpCasesFragmentDirections.actionHrpCasesFragmentToHRPNonPregnantFragment()
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionHrpCasesFragmentToNonPregnantListFragment_buildsDirections() {
        val d = HrpCasesFragmentDirections.actionHrpCasesFragmentToNonPregnantListFragment()
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = HrpCasesFragmentDirections.actionHrpCasesFragmentToNonPregnantListFragment()
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionHrpCasesFragmentToHRPNonPregnantListFragment_buildsDirections() {
        val d = HrpCasesFragmentDirections.actionHrpCasesFragmentToHRPNonPregnantListFragment()
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = HrpCasesFragmentDirections.actionHrpCasesFragmentToHRPNonPregnantListFragment()
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionHrpCasesFragmentToPregnantListFragment_buildsDirections() {
        val d = HrpCasesFragmentDirections.actionHrpCasesFragmentToPregnantListFragment()
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = HrpCasesFragmentDirections.actionHrpCasesFragmentToPregnantListFragment()
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionHrpCasesFragmentToHRPPregnantListFragment_buildsDirections() {
        val d = HrpCasesFragmentDirections.actionHrpCasesFragmentToHRPPregnantListFragment()
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = HrpCasesFragmentDirections.actionHrpCasesFragmentToHRPPregnantListFragment()
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionGlobalPwAncFormFragment_buildsDirections() {
        val d = HrpCasesFragmentDirections.actionGlobalPwAncFormFragment(benId = 10L, hhId = "v2", visitNumber = 31, fromPmsma = true, lastItemClick = true)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = HrpCasesFragmentDirections.actionGlobalPwAncFormFragment(benId = 10L, hhId = "v2", visitNumber = 31, fromPmsma = true, lastItemClick = true)
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

}
