package org.piramalswasthya.sakhi.ui.home_activity.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.databinding.FragmentDashboardBinding
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity

@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Populate header
        viewModel.currentUser?.let { user ->
            binding.tvWorkerName.text = user.name
        }
        (activity as? HomeActivity)?.let { homeActivity ->
            val locationRecord = viewModel.currentUser?.let {
                // village name from pref via ViewModel would be cleaner, but we
                // can also pull from HomeActivity's action bar title
                null
            }
        }

        // Set up static card labels & icons once
        setupCard(
            binding.cardHouseholds,
            label   = getString(R.string.all_household),
            iconRes = R.drawable.ic__hh
        )
        setupCard(
            binding.cardBeneficiaries,
            label   = getString(R.string.beneficiaries),
            iconRes = R.drawable.ic__ben
        )
        setupCard(
            binding.cardMale,
            label   = getString(R.string.male),
            iconRes = R.drawable.ic_male
        )
        setupCard(
            binding.cardFemale,
            label   = getString(R.string.female),
            iconRes = R.drawable.ic_female
        )
        setupCard(
            binding.cardPregnant,
            label   = getString(R.string.pregnant_women),
            iconRes = R.drawable.ic__maternal_health
        )
        setupCard(
            binding.cardHighRisk,
            label   = getString(R.string.hrp_cases),
            iconRes = R.drawable.ic__hrp
        )
        setupCard(
            binding.cardDelivered,
            label   = getString(R.string.delivery_outcome),
            iconRes = R.drawable.ic__delivery_outcome
        )

        // Observe stats
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.householdCount.collect { count ->
                        setCount(binding.cardHouseholds, count)
                    }
                }

                launch {
                    viewModel.stats.collect { stats ->
                        setCount(binding.cardBeneficiaries, stats.totalBeneficiaries)
                        setCount(binding.cardMale,          stats.maleBeneficiaries)
                        setCount(binding.cardFemale,        stats.femaleBeneficiaries)
                        setCount(binding.cardPregnant,      stats.pregnantWomen)
                        setCount(binding.cardHighRisk,      stats.highRiskWomen)
                        setCount(binding.cardDelivered,     stats.deliveredWomen)

                        // Pending sync badge
                        if (stats.pendingSync > 0) {
                            binding.llSyncBadge.visibility = View.VISIBLE
                            binding.tvPendingSync.text =
                                "${stats.pendingSync} record${if (stats.pendingSync > 1) "s" else ""} pending sync"
                        } else {
                            binding.llSyncBadge.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    private fun setupCard(cardView: View, label: String, iconRes: Int) {
        cardView.findViewById<TextView>(R.id.tv_stat_label).text = label
        cardView.findViewById<ImageView>(R.id.iv_stat_icon).setImageResource(iconRes)
    }

    private fun setCount(cardView: View, count: Int) {
        cardView.findViewById<TextView>(R.id.tv_stat_count).text = count.toString()
    }

    override fun onStart() {
        super.onStart()
        (activity as? HomeActivity)?.let {
            it.updateActionBar(R.drawable.ic_dashboard, getString(R.string.dashboard))
            it.setHomeMenuItemVisibility(true)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}