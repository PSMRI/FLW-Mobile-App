package org.piramalswasthya.sakhi.ui.home_activity.badges

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.BadgeShelfAdapter
import org.piramalswasthya.sakhi.databinding.FragmentBadgeShelfBinding
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity

/**
 * Earned-badge shelf (LLD §1.2): live progress and earned levels per badge,
 * computed entirely on-device from BADGE_STATE / BADGE_EARNED.
 */
@AndroidEntryPoint
class BadgeShelfFragment : Fragment() {

    private var _binding: FragmentBadgeShelfBinding? = null
    private val binding: FragmentBadgeShelfBinding
        get() = _binding!!

    private val viewModel: BadgeShelfViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBadgeShelfBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = BadgeShelfAdapter()
        binding.rvBadgeShelf.adapter = adapter
        viewModel.cards.observe(viewLifecycleOwner) { adapter.submitList(it) }
    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as HomeActivity).updateActionBar(
                R.drawable.badge_steady_syncer_t1,
                getString(R.string.badge_shelf_title)
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
