package org.piramalswasthya.sakhi.utils

import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import io.mockk.CapturingSlot
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.assertTrue
import org.junit.Test
import org.piramalswasthya.sakhi.R

class BellBadgeHelperTest {

    private class Fixture(
        val menuItem: MenuItem,
        val actionView: View,
        val badge: TextView,
        val lifecycleOwner: LifecycleOwner,
        val liveData: LiveData<Int>,
        val observerSlot: CapturingSlot<Observer<Int?>>
    )

    private fun fixture(): Fixture {
        val menuItem = mockk<MenuItem>(relaxed = true)
        val actionView = mockk<View>(relaxed = true)
        val badge = mockk<TextView>(relaxed = true)
        every { menuItem.actionView } returns actionView
        every { actionView.findViewById<TextView>(R.id.tvBadge) } returns badge

        val lifecycleOwner = mockk<LifecycleOwner>(relaxed = true)
        val liveData = mockk<LiveData<Int>>()
        val observerSlot = slot<Observer<Int?>>()
        every { liveData.observe(lifecycleOwner, capture(observerSlot)) } just Runs

        return Fixture(menuItem, actionView, badge, lifecycleOwner, liveData, observerSlot)
    }

    @Test
    fun `bind returns early when the action view is missing`() {
        val menuItem = mockk<MenuItem>(relaxed = true)
        every { menuItem.actionView } returns null
        val liveData = mockk<LiveData<Int>>(relaxed = true)
        val lifecycleOwner = mockk<LifecycleOwner>(relaxed = true)

        BellBadgeHelper.bind(menuItem, lifecycleOwner, liveData) {}

        verify(exactly = 0) { liveData.observe(any(), any()) }
    }

    @Test
    fun `bind invokes onClick when the action view is clicked`() {
        val f = fixture()
        val listenerSlot = slot<View.OnClickListener>()
        every { f.actionView.setOnClickListener(capture(listenerSlot)) } just Runs
        var clicked = false

        BellBadgeHelper.bind(f.menuItem, f.lifecycleOwner, f.liveData) { clicked = true }
        listenerSlot.captured.onClick(f.actionView)

        assertTrue(clicked)
    }

    @Test
    fun `bind shows the exact count when between one and ninety nine`() {
        val f = fixture()

        BellBadgeHelper.bind(f.menuItem, f.lifecycleOwner, f.liveData) {}
        f.observerSlot.captured.onChanged(5)

        verify { f.badge.text = "5" }
        verify { f.badge.visibility = View.VISIBLE }
    }

    @Test
    fun `bind caps the badge text at 99 plus for large counts`() {
        val f = fixture()

        BellBadgeHelper.bind(f.menuItem, f.lifecycleOwner, f.liveData) {}
        f.observerSlot.captured.onChanged(150)

        verify { f.badge.text = "99+" }
        verify { f.badge.visibility = View.VISIBLE }
    }

    @Test
    fun `bind hides the badge when the count is null`() {
        val f = fixture()

        BellBadgeHelper.bind(f.menuItem, f.lifecycleOwner, f.liveData) {}
        f.observerSlot.captured.onChanged(null)

        verify { f.badge.visibility = View.GONE }
    }

    @Test
    fun `bind hides the badge when the count is zero`() {
        val f = fixture()

        BellBadgeHelper.bind(f.menuItem, f.lifecycleOwner, f.liveData) {}
        f.observerSlot.captured.onChanged(0)

        verify { f.badge.visibility = View.GONE }
    }
}
