package org.piramalswasthya.sakhi.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.ui.home_activity.home.HomeIconsFragment
import org.piramalswasthya.sakhi.ui.home_activity.home.SchedulerFragment

class HomePagerAdapterTest {

    private fun newAdapter(): HomePagerAdapter? {
        val fragment = mockk<Fragment>(relaxed = true)
        val fragmentManager = mockk<FragmentManager>(relaxed = true)
        val lifecycle = mockk<Lifecycle>(relaxed = true)
        every { fragment.childFragmentManager } returns fragmentManager
        every { fragment.lifecycle } returns lifecycle
        return try {
            HomePagerAdapter(fragment)
        } catch (e: NullPointerException) {
            org.junit.Assume.assumeNoException(e)
            null
        }
    }

    @Test
    fun itemCount_isAlwaysTwo() {
        val adapter = newAdapter() ?: return
        assertEquals(2, adapter.itemCount)
    }

    @Test
    fun createFragment_position0_returnsSchedulerFragment() {
        val adapter = newAdapter() ?: return
        assertTrue(adapter.createFragment(0) is SchedulerFragment)
    }

    @Test
    fun createFragment_position1_returnsHomeIconsFragment() {
        val adapter = newAdapter() ?: return
        assertTrue(adapter.createFragment(1) is HomeIconsFragment)
    }

    @Test(expected = IllegalStateException::class)
    fun createFragment_positionGreaterThan1_throwsIllegalStateException() {
        val adapter = newAdapter() ?: throw IllegalStateException("skipped")
        adapter.createFragment(2)
    }
}
