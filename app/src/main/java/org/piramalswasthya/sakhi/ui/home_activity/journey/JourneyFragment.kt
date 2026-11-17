package org.piramalswasthya.sakhi.ui.home_activity.journey

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.BadgeShelfAdapter
import org.piramalswasthya.sakhi.databinding.FragmentJourneyBinding
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import timber.log.Timber

/**
 * Journey screen (Notification LLD §1): opened from the evening notification;
 * shows today's acknowledged work and badge progress.
 */
@AndroidEntryPoint
class JourneyFragment : Fragment() {

    private var _binding: FragmentJourneyBinding? = null
    private val binding: FragmentJourneyBinding
        get() = _binding!!

    private val viewModel: JourneyViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJourneyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvJourneyGreeting.text =
            getString(R.string.journey_greeting, viewModel.userName)

        viewModel.todayCount.observe(viewLifecycleOwner) { count ->
            binding.tvJourneyNarrative.text = getString(
                when {
                    count <= 0 -> R.string.journey_narrative_zero
                    count == 1 -> R.string.journey_narrative_one
                    else -> R.string.journey_narrative_busy
                }
            )
            binding.tvJourneyToday.text =
                getString(R.string.journey_today_count, count)
        }
        viewModel.lifetimeTotal.observe(viewLifecycleOwner) { total ->
            binding.tvJourneyLifetime.text =
                getString(R.string.journey_lifetime_count, total)
        }

        val adapter = BadgeShelfAdapter()
        binding.rvJourneyBadges.adapter = adapter
        viewModel.topBadges.observe(viewLifecycleOwner) { adapter.submitList(it) }

        binding.btnJourneyAllBadges.setOnClickListener {
            try {
                findNavController().navigate(R.id.badgeShelfFragment)
            } catch (e: Exception) {
                Timber.e(e, "Journey → badge shelf navigation failed")
            }
        }
    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as HomeActivity).updateActionBar(
                R.drawable.badge_steady_syncer_t1,
                getString(R.string.journey_title)
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
