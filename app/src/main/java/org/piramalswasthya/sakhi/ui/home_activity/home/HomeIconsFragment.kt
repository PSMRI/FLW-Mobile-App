package org.piramalswasthya.sakhi.ui.home_activity.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.BuildConfig
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.IconGridAdapter
import org.piramalswasthya.sakhi.badges.BadgeRepository
import org.piramalswasthya.sakhi.badges.domain.BadgeDefinitions
import org.piramalswasthya.sakhi.configuration.IconDataset
import org.piramalswasthya.sakhi.databinding.RvIconGridBinding
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class HomeIconsFragment : Fragment() {

    @Inject
    lateinit var iconDataset: IconDataset

    @Inject
    lateinit var badgeRepository: BadgeRepository

    private var _binding: RvIconGridBinding? = null
    private val binding: RvIconGridBinding
        get() = _binding!!

    private val viewModel: HomeViewModel by viewModels({ requireActivity() })


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = RvIconGridBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpHomeIconRvAdapter()
        setUpBadgeWidget()
    }

    /**
     * Live badge progress widget (LLD: never reads zero). Shows the badge
     * closest to its next milestone; hidden entirely by the remote kill switch.
     */
    private fun setUpBadgeWidget() {
        binding.cvBadgeWidget.setOnClickListener {
            try {
                findNavController().navigate(R.id.action_homeFragment_to_badgeShelfFragment)
            } catch (e: Exception) {
                Timber.e(e, "Badge shelf navigation failed")
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                badgeRepository.shelf.collect { cards ->
                    val b = _binding ?: return@collect
                    if (cards.isEmpty()) { // feature disabled remotely
                        b.cvBadgeWidget.visibility = View.GONE
                        return@collect
                    }
                    b.cvBadgeWidget.visibility = View.VISIBLE
                    val top = cards.maxByOrNull { card ->
                        val target = card.state?.nextTarget?.coerceAtLeast(1) ?: 1
                        (card.state?.progress ?: 0).toDouble() / target
                    }
                    val progress = top?.state?.progress ?: 0
                    val target = top?.state?.nextTarget?.coerceAtLeast(1) ?: 1
                    if (top == null || progress <= 0) {
                        b.ivBadgeWidgetIcon.setImageResource(R.drawable.badge_steady_syncer_t1)
                        b.tvBadgeWidgetText.text =
                            getString(R.string.badge_widget_get_started)
                        b.pbBadgeWidget.max = 1
                        b.pbBadgeWidget.progress = 0
                    } else {
                        val (iconRes, earnedLook) =
                            BadgeDefinitions.displayIcon(top.definition, top.state)
                        b.ivBadgeWidgetIcon.setImageResource(iconRes)
                        b.ivBadgeWidgetIcon.alpha = if (earnedLook) 1f else 0.4f
                        b.tvBadgeWidgetText.text = getString(
                            R.string.badge_widget_progress,
                            getString(top.definition.titleRes), progress, target
                        )
                        b.pbBadgeWidget.max = target.toInt()
                        b.pbBadgeWidget.progress = progress.coerceAtMost(target).toInt()
                    }
                }
            }
        }
    }

    private fun setUpHomeIconRvAdapter() {
        val rvLayoutManager = GridLayoutManager(
            context,
            requireContext().resources.getInteger(R.integer.icon_grid_span)
        )
        binding.rvIconGrid.layoutManager = rvLayoutManager
        val rvAdapter = IconGridAdapter(IconGridAdapter.GridIconClickListener { navDirections ->
            val navController = findNavController()

            try {
                val currentDestinationId = navController.currentDestination?.id

                // Skip navigation if already on destination
                if (currentDestinationId == R.id.villageLevelFormsFragment &&
                    navDirections == HomeFragmentDirections.actionNavHomeToVillageLevelFormsFragment()
                ) {
                    Timber.d("Already at destination: skipping navigation.")
                    return@GridIconClickListener
                }

                navController.navigate(navDirections)
            } catch (e: Exception) {
                Timber.e(e, "Navigation failed")
            }
           // findNavController().navigate(it)
        }, viewModel.scope)
        binding.rvIconGrid.adapter = rvAdapter
        viewModel.devModeEnabled.observe(viewLifecycleOwner) {
            Timber.d("update called!~~ $it")
            rvAdapter.submitList(iconDataset.getHomeIconDataset(resources))
        }

    }
}