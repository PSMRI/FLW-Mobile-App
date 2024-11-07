package org.piramalswasthya.sakhi.adapters
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.piramalswasthya.sakhi.ui.asha_facilitator_activity.facilitator_home.FacilitatorDashboardFragment
import org.piramalswasthya.sakhi.ui.asha_facilitator_activity.facilitator_home.form.FacilitatorFormFragment
import timber.log.Timber

class FacilitatorHomePageAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment {
        val fragment = when (position) {
            1 -> FacilitatorDashboardFragment()
            0 -> FacilitatorFormFragment()
            else -> throw IllegalStateException("Index >1 called!")

        }
        Timber.d("Adapter created Fragment $fragment")

        return fragment
    }


}
