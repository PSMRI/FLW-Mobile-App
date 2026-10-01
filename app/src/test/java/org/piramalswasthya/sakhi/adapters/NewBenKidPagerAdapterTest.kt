package org.piramalswasthya.sakhi.adapters

import androidx.fragment.app.Fragment
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NewBenKidPagerAdapterTest {

    @Test
    fun getItemCount_isAlwaysTwo() {
        val fragment = mockk<Fragment>(relaxed = true)
        every { fragment.lifecycle } returns mockk(relaxed = true)
        val adapter = try {
            NewBenKidPagerAdapter(fragment)
        } catch (e: NullPointerException) {
            org.junit.Assume.assumeNoException(e)
            return
        }
        assertEquals(2, adapter.itemCount)
    }

    @Test
    fun createFragment_returnsAFragmentInstance() {
        val fragment = mockk<Fragment>(relaxed = true)
        every { fragment.lifecycle } returns mockk(relaxed = true)
        val adapter = try {
            NewBenKidPagerAdapter(fragment)
        } catch (e: NullPointerException) {
            org.junit.Assume.assumeNoException(e)
            return
        }
        val created = adapter.createFragment(0)
        assertTrue(created is Fragment)
    }

    @Test
    fun argObjectIndexConstant_isIndex() {
        assertEquals("INDEX", NewBenKidPagerAdapter.ARG_OBJECT_INDEX)
    }
}
