package org.piramalswasthya.sakhi.ui.home_activity.sync_dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.databinding.FragmentSyncDashboardBinding

@AndroidEntryPoint
class SyncDashboardFragment : Fragment() {

    private var _binding: FragmentSyncDashboardBinding? = null
    private val binding get() = _binding!!

    private val tabTitles by lazy {
        arrayOf(
            getString(R.string.sync_dashboard_tab_status),
            getString(R.string.sync_dashboard_tab_logs)
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSyncDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewPager.adapter = SyncDashboardPagerAdapter(this)

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
