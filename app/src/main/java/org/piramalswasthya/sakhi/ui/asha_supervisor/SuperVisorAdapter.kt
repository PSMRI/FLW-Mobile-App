package org.piramalswasthya.sakhi.ui.asha_supervisor

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.piramalswasthya.sakhi.ui.asha_supervisor.incentiveDashboard.IncentiveDashboardFragment
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.SupervisorFragment
import timber.log.Timber

class SuperVisorAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment {
        val fragment = when (position) {
            0 -> IncentiveDashboardFragment()
            1 -> SupervisorFragment()
            else -> throw IllegalStateException("Index >1 called!")

        }
        Timber.d("Adapter created Fragment $fragment")

        return fragment
    }


}
