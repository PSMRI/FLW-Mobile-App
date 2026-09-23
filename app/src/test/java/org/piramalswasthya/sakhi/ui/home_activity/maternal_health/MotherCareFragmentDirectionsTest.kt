package org.piramalswasthya.sakhi.ui.home_activity.maternal_health

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

class MotherCareFragmentDirectionsTest {

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
    fun actionMotherCareFragmentToPwRegistrationFragment_buildsDirections() {
        val d = MotherCareFragmentDirections.actionMotherCareFragmentToPwRegistrationFragment()
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = MotherCareFragmentDirections.actionMotherCareFragmentToPwRegistrationFragment()
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionMotherCareFragmentToPwAncVisitsFragment_buildsDirections() {
        val d = MotherCareFragmentDirections.actionMotherCareFragmentToPwAncVisitsFragment(source = 11)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = MotherCareFragmentDirections.actionMotherCareFragmentToPwAncVisitsFragment(source = 11)
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionMotherCareFragmentToDeliveryOutcomeListFragment_buildsDirections() {
        val d = MotherCareFragmentDirections.actionMotherCareFragmentToDeliveryOutcomeListFragment()
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = MotherCareFragmentDirections.actionMotherCareFragmentToDeliveryOutcomeListFragment()
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionMotherCareFragmentToInfantRegListFragment_buildsDirections() {
        val d = MotherCareFragmentDirections.actionMotherCareFragmentToInfantRegListFragment(onlyLowBirthWeight = true)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = MotherCareFragmentDirections.actionMotherCareFragmentToInfantRegListFragment(onlyLowBirthWeight = true)
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionMotherCareFragmentToChildRegListFragment_buildsDirections() {
        val d = MotherCareFragmentDirections.actionMotherCareFragmentToChildRegListFragment()
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = MotherCareFragmentDirections.actionMotherCareFragmentToChildRegListFragment()
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionMotherCareFragmentToPncMotherListFragment_buildsDirections() {
        val d = MotherCareFragmentDirections.actionMotherCareFragmentToPncMotherListFragment(source = 11)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = MotherCareFragmentDirections.actionMotherCareFragmentToPncMotherListFragment(source = 11)
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionMotherCareFragmentToAbortionListFragment_buildsDirections() {
        val d = MotherCareFragmentDirections.actionMotherCareFragmentToAbortionListFragment()
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = MotherCareFragmentDirections.actionMotherCareFragmentToAbortionListFragment()
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionMotherCareFragmentToPmsmaHighRiskListFragment_buildsDirections() {
        val d = MotherCareFragmentDirections.actionMotherCareFragmentToPmsmaHighRiskListFragment()
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = MotherCareFragmentDirections.actionMotherCareFragmentToPmsmaHighRiskListFragment()
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionMotherCareFragmentToHwcReferredListFragment_buildsDirections() {
        val d = MotherCareFragmentDirections.actionMotherCareFragmentToHwcReferredListFragment()
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = MotherCareFragmentDirections.actionMotherCareFragmentToHwcReferredListFragment()
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionGlobalPwAncFormFragment_buildsDirections() {
        val d = MotherCareFragmentDirections.actionGlobalPwAncFormFragment(benId = 10L, hhId = "v2", visitNumber = 31, fromPmsma = true, lastItemClick = true)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = MotherCareFragmentDirections.actionGlobalPwAncFormFragment(benId = 10L, hhId = "v2", visitNumber = 31, fromPmsma = true, lastItemClick = true)
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

}
