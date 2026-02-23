package org.piramalswasthya.sakhi.ui.home_activity.sync_dashboard

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class SyncDashboardPagerAdapter(
    fragment: Fragment
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> SyncStatusTabFragment()
            1 -> SyncLogsTabFragment()
            else -> throw IllegalArgumentException("Invalid tab position: $position")
        }
    }
}
