package org.piramalswasthya.sakhi.ui.home_activity.home

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

class HomeFragmentDirectionsTest {

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
    fun actionNavHomeToAllBenFragment_buildsDirections() {
        val d = HomeFragmentDirections.actionNavHomeToAllBenFragment(source = 11)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = HomeFragmentDirections.actionNavHomeToAllBenFragment(source = 11)
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionNavHomeToAbhaBenFragment_buildsDirections() {
        val d = HomeFragmentDirections.actionNavHomeToAbhaBenFragment(source = 11)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = HomeFragmentDirections.actionNavHomeToAbhaBenFragment(source = 11)
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionNavHomeToAllHouseholdFragment_buildsDirections() {
        val d = HomeFragmentDirections.actionNavHomeToAllHouseholdFragment()
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = HomeFragmentDirections.actionNavHomeToAllHouseholdFragment()
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionNavHomeToChildCareFragment_buildsDirections() {
        val d = HomeFragmentDirections.actionNavHomeToChildCareFragment()
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = HomeFragmentDirections.actionNavHomeToChildCareFragment()
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionNavHomeToMotherCareFragment_buildsDirections() {
        val d = HomeFragmentDirections.actionNavHomeToMotherCareFragment()
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = HomeFragmentDirections.actionNavHomeToMotherCareFragment()
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionNavHomeToDeathReportsFragment_buildsDirections() {
        val d = HomeFragmentDirections.actionNavHomeToDeathReportsFragment()
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = HomeFragmentDirections.actionNavHomeToDeathReportsFragment()
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionNavHomeToGeneralOpCareFragment_buildsDirections() {
        val d = HomeFragmentDirections.actionNavHomeToGeneralOpCareFragment()
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = HomeFragmentDirections.actionNavHomeToGeneralOpCareFragment()
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionNavHomeToHrpCasesFragment_buildsDirections() {
        val d = HomeFragmentDirections.actionNavHomeToHrpCasesFragment()
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = HomeFragmentDirections.actionNavHomeToHrpCasesFragment()
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionNavHomeToImmunizationDueFragment_buildsDirections() {
        val d = HomeFragmentDirections.actionNavHomeToImmunizationDueFragment()
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = HomeFragmentDirections.actionNavHomeToImmunizationDueFragment()
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionNavHomeToNcdFragment_buildsDirections() {
        val d = HomeFragmentDirections.actionNavHomeToNcdFragment()
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = HomeFragmentDirections.actionNavHomeToNcdFragment()
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionNavHomeToVillageLevelFormsFragment_buildsDirections() {
        val d = HomeFragmentDirections.actionNavHomeToVillageLevelFormsFragment()
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = HomeFragmentDirections.actionNavHomeToVillageLevelFormsFragment()
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionNavHomeToEligibleCoupleFragment_buildsDirections() {
        val d = HomeFragmentDirections.actionNavHomeToEligibleCoupleFragment()
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = HomeFragmentDirections.actionNavHomeToEligibleCoupleFragment()
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionHomeFragmentToNewHouseholdFragment_buildsDirections() {
        val d = HomeFragmentDirections.actionHomeFragmentToNewHouseholdFragment(hhId = 10L, isAshaFamily = "v2")
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = HomeFragmentDirections.actionHomeFragmentToNewHouseholdFragment(hhId = 10L, isAshaFamily = "v2")
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionHomeFragmentToGetBenFragment_buildsDirections() {
        val d = HomeFragmentDirections.actionHomeFragmentToGetBenFragment()
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = HomeFragmentDirections.actionHomeFragmentToGetBenFragment()
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionHomeFragmentToPwAncVisitsFragment_buildsDirections() {
        val d = HomeFragmentDirections.actionHomeFragmentToPwAncVisitsFragment(source = 11)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = HomeFragmentDirections.actionHomeFragmentToPwAncVisitsFragment(source = 11)
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionHomeFragmentToBenListCHOFragment_buildsDirections() {
        val d = HomeFragmentDirections.actionHomeFragmentToBenListCHOFragment()
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = HomeFragmentDirections.actionHomeFragmentToBenListCHOFragment()
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionHomeFragmentToHRPPregnantFragment_buildsDirections() {
        val d = HomeFragmentDirections.actionHomeFragmentToHRPPregnantFragment()
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = HomeFragmentDirections.actionHomeFragmentToHRPPregnantFragment()
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionHomeFragmentToHRPNonPregnantFragment_buildsDirections() {
        val d = HomeFragmentDirections.actionHomeFragmentToHRPNonPregnantFragment()
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = HomeFragmentDirections.actionHomeFragmentToHRPNonPregnantFragment()
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionHomeFragmentToHRPPregnantListFragment_buildsDirections() {
        val d = HomeFragmentDirections.actionHomeFragmentToHRPPregnantListFragment()
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = HomeFragmentDirections.actionHomeFragmentToHRPPregnantListFragment()
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionHomeFragmentToHRPNonPregnantListFragment_buildsDirections() {
        val d = HomeFragmentDirections.actionHomeFragmentToHRPNonPregnantListFragment()
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = HomeFragmentDirections.actionHomeFragmentToHRPNonPregnantListFragment()
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionHomeFragmentToInfantRegListFragment_buildsDirections() {
        val d = HomeFragmentDirections.actionHomeFragmentToInfantRegListFragment(onlyLowBirthWeight = true)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = HomeFragmentDirections.actionHomeFragmentToInfantRegListFragment(onlyLowBirthWeight = true)
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionHomeFragmentToChildImmunizationListFragment_buildsDirections() {
        val d = HomeFragmentDirections.actionHomeFragmentToChildImmunizationListFragment(showDueOnly = true)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = HomeFragmentDirections.actionHomeFragmentToChildImmunizationListFragment(showDueOnly = true)
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionHomeFragmentToDiseaseControlFragment_buildsDirections() {
        val d = HomeFragmentDirections.actionHomeFragmentToDiseaseControlFragment()
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = HomeFragmentDirections.actionHomeFragmentToDiseaseControlFragment()
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionHomeFragmentToNonFollowUpFragment_buildsDirections() {
        val d = HomeFragmentDirections.actionHomeFragmentToNonFollowUpFragment()
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = HomeFragmentDirections.actionHomeFragmentToNonFollowUpFragment()
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionHomeFragmentToMissedPeriodFragment_buildsDirections() {
        val d = HomeFragmentDirections.actionHomeFragmentToMissedPeriodFragment()
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = HomeFragmentDirections.actionHomeFragmentToMissedPeriodFragment()
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionHomeFragmentToNcdReferredListFragment_buildsDirections() {
        val d = HomeFragmentDirections.actionHomeFragmentToNcdReferredListFragment()
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = HomeFragmentDirections.actionHomeFragmentToNcdReferredListFragment()
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionGlobalPwAncFormFragment_buildsDirections() {
        val d = HomeFragmentDirections.actionGlobalPwAncFormFragment(benId = 10L, hhId = "v2", visitNumber = 31, fromPmsma = true, lastItemClick = true)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = HomeFragmentDirections.actionGlobalPwAncFormFragment(benId = 10L, hhId = "v2", visitNumber = 31, fromPmsma = true, lastItemClick = true)
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

}
