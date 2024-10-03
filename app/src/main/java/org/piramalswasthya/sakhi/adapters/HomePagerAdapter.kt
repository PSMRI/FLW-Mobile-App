package org.piramalswasthya.sakhi.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.piramalswasthya.sakhi.ui.home_activity.home.HRDashboardFragment
import org.piramalswasthya.sakhi.ui.home_activity.home.HomeIconsFragment
import org.piramalswasthya.sakhi.ui.home_activity.home.SchedulerFragment
import timber.log.Timber

class HomePagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount() = 3

    override fun createFragment(position: Int): Fragment {
        val fragment = when (position) {
            2 -> HRDashboardFragment()
            1 -> HomeIconsFragment()
            0 -> SchedulerFragment()
            else -> throw IllegalStateException("Index >1 called!")

        }
        Timber.d("Adapter created Fragment $fragment")

        return fragment
    }


}
