package org.piramalswasthya.sakhi.ui.home_activity.village_level_forms

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

class VillageLevelFormsFragmentDirectionsTest {

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
    fun actionVillageLevelFormsFragmentToUwinListFragment_buildsDirections() {
        val d = VillageLevelFormsFragmentDirections.actionVillageLevelFormsFragmentToUwinListFragment()
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = VillageLevelFormsFragmentDirections.actionVillageLevelFormsFragmentToUwinListFragment()
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionVillageLevelFormsFragmentToSurveyRegisterFragment_buildsDirections() {
        val d = VillageLevelFormsFragmentDirections.actionVillageLevelFormsFragmentToSurveyRegisterFragment()
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = VillageLevelFormsFragmentDirections.actionVillageLevelFormsFragmentToSurveyRegisterFragment()
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionVillageLevelFormsFragmentToVHNDListFragement_buildsDirections() {
        val d = VillageLevelFormsFragmentDirections.actionVillageLevelFormsFragmentToVHNDListFragement()
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = VillageLevelFormsFragmentDirections.actionVillageLevelFormsFragmentToVHNDListFragement()
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionVillageLevelFormsFragmentToVHNCListFragement_buildsDirections() {
        val d = VillageLevelFormsFragmentDirections.actionVillageLevelFormsFragmentToVHNCListFragement()
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = VillageLevelFormsFragmentDirections.actionVillageLevelFormsFragmentToVHNCListFragement()
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionVillageLevelFormsFragmentToPHCReviewListFragement_buildsDirections() {
        val d = VillageLevelFormsFragmentDirections.actionVillageLevelFormsFragmentToPHCReviewListFragement()
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = VillageLevelFormsFragmentDirections.actionVillageLevelFormsFragmentToPHCReviewListFragement()
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionVillageLevelFormsFragmentToAHDListFragment_buildsDirections() {
        val d = VillageLevelFormsFragmentDirections.actionVillageLevelFormsFragmentToAHDListFragment()
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = VillageLevelFormsFragmentDirections.actionVillageLevelFormsFragmentToAHDListFragment()
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionVillageLevelFormsFragmentToDewormingListFragment_buildsDirections() {
        val d = VillageLevelFormsFragmentDirections.actionVillageLevelFormsFragmentToDewormingListFragment()
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = VillageLevelFormsFragmentDirections.actionVillageLevelFormsFragmentToDewormingListFragment()
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionVillageLevelFormsFragmentToSaasBahuSamelanFragment_buildsDirections() {
        val d = VillageLevelFormsFragmentDirections.actionVillageLevelFormsFragmentToSaasBahuSamelanFragment(id = 10L)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = VillageLevelFormsFragmentDirections.actionVillageLevelFormsFragmentToSaasBahuSamelanFragment(id = 10L)
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionVillageLevelFormsFragmentToAllMaaMeetingFragment_buildsDirections() {
        val d = VillageLevelFormsFragmentDirections.actionVillageLevelFormsFragmentToAllMaaMeetingFragment()
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = VillageLevelFormsFragmentDirections.actionVillageLevelFormsFragmentToAllMaaMeetingFragment()
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionVillageLevelFormsFragmentToPulsePolioCampaignListFragment_buildsDirections() {
        val d = VillageLevelFormsFragmentDirections.actionVillageLevelFormsFragmentToPulsePolioCampaignListFragment()
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = VillageLevelFormsFragmentDirections.actionVillageLevelFormsFragmentToPulsePolioCampaignListFragment()
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionVillageLevelFormsFragmentToORSCampaignListFragment_buildsDirections() {
        val d = VillageLevelFormsFragmentDirections.actionVillageLevelFormsFragmentToORSCampaignListFragment()
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = VillageLevelFormsFragmentDirections.actionVillageLevelFormsFragmentToORSCampaignListFragment()
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionVillageLevelFormsFragmentToFilariaMdaCampaignHistoryFragment_buildsDirections() {
        val d = VillageLevelFormsFragmentDirections.actionVillageLevelFormsFragmentToFilariaMdaCampaignHistoryFragment()
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = VillageLevelFormsFragmentDirections.actionVillageLevelFormsFragmentToFilariaMdaCampaignHistoryFragment()
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionVillageLevelFormsFragmentToSaasBahuListFragment_buildsDirections() {
        val d = VillageLevelFormsFragmentDirections.actionVillageLevelFormsFragmentToSaasBahuListFragment()
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = VillageLevelFormsFragmentDirections.actionVillageLevelFormsFragmentToSaasBahuListFragment()
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

    @Test
    fun actionGlobalPwAncFormFragment_buildsDirections() {
        val d = VillageLevelFormsFragmentDirections.actionGlobalPwAncFormFragment(benId = 10L, hhId = "v2", visitNumber = 31, fromPmsma = true, lastItemClick = true)
        assertNotNull(d)
        assertTrue(d.actionId != 0)
        assertNotNull(d.arguments)
        assertTrue(d.toString().isNotEmpty())
        val same = VillageLevelFormsFragmentDirections.actionGlobalPwAncFormFragment(benId = 10L, hhId = "v2", visitNumber = 31, fromPmsma = true, lastItemClick = true)
        assertEquals(d, same)
        assertEquals(d.hashCode(), same.hashCode())
        assertFalse(d.equals(null))
        assertFalse(d.equals(Any()))
    }

}
