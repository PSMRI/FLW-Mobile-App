package org.piramalswasthya.sakhi.ui.home_activity.gamification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.databinding.FragmentGamificationDashboardBinding
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import javax.inject.Inject

@AndroidEntryPoint
class GamificationDashboardFragment : Fragment() {

    @Inject
    lateinit var preferenceDao: PreferenceDao

    private val viewModel: GamificationViewModel by viewModels()

    private var _binding: FragmentGamificationDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGamificationDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userId = preferenceDao.getLoggedInUser()?.userId ?: return
        viewModel.init(userId)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    renderProfile(state)
                    renderBadges(state)
                    renderActivity(state)
                }
            }
        }
    }

    private fun renderProfile(state: GamificationUiState) {
        val profile = state.profile
        binding.tvTotalPoints.text = (profile?.totalPoints ?: 0).toString()
        binding.tvLevel.text = getString(R.string.gamification_level, profile?.level ?: 1)

        val streak = profile?.currentStreakDays ?: 0
        binding.tvStreak.text = resources.getQuantityString(
            R.plurals.gamification_streak_days, streak, streak
        )

        // XP progress bar within the current level
        val currentLevelThreshold = levelThreshold(profile?.level ?: 1)
        val nextLevelThreshold = levelThreshold((profile?.level ?: 1) + 1)
        val progress = if (nextLevelThreshold > currentLevelThreshold) {
            val pointsIntoLevel = (profile?.totalPoints ?: 0) - currentLevelThreshold
            val pointsForLevel = nextLevelThreshold - currentLevelThreshold
            ((pointsIntoLevel.toFloat() / pointsForLevel) * 100).toInt().coerceIn(0, 100)
        } else 100
        binding.progressXp.progress = progress
    }

    private fun renderBadges(state: GamificationUiState) {
        if (state.badges.isEmpty()) {
            binding.tvNoBadges.visibility = View.VISIBLE
            binding.rvBadges.visibility = View.GONE
        } else {
            binding.tvNoBadges.visibility = View.GONE
            binding.rvBadges.visibility = View.VISIBLE

            val adapter = binding.rvBadges.adapter as? BadgeAdapter
                ?: BadgeAdapter().also { binding.rvBadges.adapter = it }
            adapter.submitList(state.badges)
        }
    }

    private fun renderActivity(state: GamificationUiState) {
        if (state.recentTransactions.isEmpty()) {
            binding.tvNoActivity.visibility = View.VISIBLE
            binding.rvActivity.visibility = View.GONE
        } else {
            binding.tvNoActivity.visibility = View.GONE
            binding.rvActivity.visibility = View.VISIBLE

            val adapter = binding.rvActivity.adapter as? PointsTransactionAdapter
                ?: PointsTransactionAdapter().also { binding.rvActivity.adapter = it }
            adapter.submitList(state.recentTransactions)
        }
    }

    private fun levelThreshold(level: Int): Int {
        val thresholds = listOf(0, 100, 300, 600, 1000, 1500, 2200, 3000)
        return thresholds.getOrElse(level - 1) { thresholds.last() }
    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as HomeActivity).updateActionBar(
                R.drawable.ic_gamification,
                getString(R.string.gamification_dashboard_title)
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
