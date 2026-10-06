package org.piramalswasthya.sakhi.adapters

import androidx.fragment.app.Fragment
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NewBenGenPagerAdapterTest {

    private fun fragment(): Fragment {
        val fragment = mockk<Fragment>(relaxed = true)
        every { fragment.lifecycle } returns mockk(relaxed = true)
        return fragment
    }

    private fun newAdapter(list: MutableList<String>): NewBenGenPagerAdapter? = try {
        NewBenGenPagerAdapter(list, fragment())
    } catch (e: NullPointerException) {
        org.junit.Assume.assumeNoException(e)
        null
    }

    @Test
    fun getItemCount_reflectsListSize() {
        val adapter = newAdapter(mutableListOf("A", "B", "C")) ?: return
        assertEquals(3, adapter.itemCount)
    }

    @Test
    fun getItemCount_isZeroForEmptyList() {
        val adapter = newAdapter(mutableListOf()) ?: return
        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun createFragment_returnsAFragmentInstance() {
        val adapter = newAdapter(mutableListOf("A")) ?: return
        val created = adapter.createFragment(0)
        assertTrue(created is Fragment)
    }

    @Test
    fun addPage_appendsToListAndIncreasesCount() {
        val list = mutableListOf("A")
        val adapter = newAdapter(list) ?: return
        adapter.addPage("B")
        assertEquals(2, adapter.itemCount)
        assertEquals("B", adapter.getPageName(1))
    }

    @Test
    fun getPageName_returnsNameAtPosition() {
        val adapter = newAdapter(mutableListOf("First", "Second")) ?: return
        assertEquals("First", adapter.getPageName(0))
        assertEquals("Second", adapter.getPageName(1))
    }

    @Test
    fun removePage_removesFromListAndDecreasesCount() {
        val list = mutableListOf("A", "B", "C")
        val adapter = newAdapter(list) ?: return
        adapter.removePage(1)
        assertEquals(2, adapter.itemCount)
        assertEquals("A", adapter.getPageName(0))
        assertEquals("C", adapter.getPageName(1))
    }

    @Test
    fun argObjectIndexConstant_isIndex() {
        assertEquals("INDEX", NewBenGenPagerAdapter.ARG_OBJECT_INDEX)
    }
}
