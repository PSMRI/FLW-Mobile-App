package org.piramalswasthya.sakhi.ui.home_activity.home

import android.os.Bundle
import android.widget.Toast
import org.piramalswasthya.sakhi.BuildConfig
import org.piramalswasthya.sakhi.badges.BadgeDemoSeeder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import android.view.HapticFeedbackConstants
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.BadgeCarouselAdapter
import org.piramalswasthya.sakhi.badges.BadgeRepository
import org.piramalswasthya.sakhi.databinding.FragmentSchedulerBinding
import timber.log.Timber
import javax.inject.Inject
import org.piramalswasthya.sakhi.ui.home_activity.home.SchedulerViewModel.State.LOADED
import org.piramalswasthya.sakhi.ui.home_activity.home.SchedulerViewModel.State.LOADING
import java.util.Calendar
import java.util.concurrent.atomic.AtomicInteger


@AndroidEntryPoint
class SchedulerFragment : Fragment() {


    private var _binding: FragmentSchedulerBinding? = null
    private val binding: FragmentSchedulerBinding
        get() = _binding!!

    private var countMissedPeriodCases = AtomicInteger(0)
    private var ecrMissedPeriodCount = 0
    private var ectMissedPeriodCount = 0

    private val viewModel: SchedulerViewModel by viewModels({ requireActivity() })

    @Inject
    lateinit var badgeRepository: BadgeRepository

    @Inject
    lateinit var demoSeeder: BadgeDemoSeeder

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSchedulerBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpImpactDashboard()
        viewModel.state.observe(viewLifecycleOwner) {
            when (it) {
                LOADING -> {
                    binding.llContent.visibility = View.GONE
                    binding.pbLoading.visibility = View.VISIBLE
                }

                LOADED -> {
                    binding.pbLoading.visibility = View.GONE
                    binding.llContent.visibility = View.VISIBLE
                }
            }
        }
        viewModel.date.observe(viewLifecycleOwner) {
            binding.calendarView.date = it
        }
        lifecycleScope.launch {
            viewModel.ancDueCount.collect {
                binding.tvAnc.text = it.toString()
            }
        }

        lifecycleScope.launch {
            viewModel.immunizationDue.collect {
                binding.tvImm.text = it.toString()
            }
        }

        lifecycleScope.launch {
            viewModel.lowWeightBabiesCount.collect {
                binding.tvLbwb.text = it.toString()
            }
        }
        binding.cvAnc.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToPwAncVisitsFragment(source = 2))
        }

        binding.cvImm.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToChildImmunizationListFragment(showDueOnly = true))
        }
        binding.cvHrp.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToHRPPregnantListFragment())
        }
        binding.cvNonHrp.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToHRPNonPregnantListFragment())
        }
        binding.cvLwb.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToInfantRegListFragment(onlyLowBirthWeight = true))
        }
        binding.cvAbha.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionNavHomeToAllBenFragment(1))
//            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToHRPPregnantListFragment())
        }
        binding.cvRch.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionNavHomeToAllBenFragment(2))
//            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToHRPPregnantListFragment())
        }
        binding.cvNon.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToNonFollowUpFragment())
        }
        binding.cvMiss.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToMissedPeriodFragment())
        }
        lifecycleScope.launch {
            viewModel.hrpDueCount.collect {
                binding.tvHrp.text = it.toString()
            }
        }
        lifecycleScope.launch {
            viewModel.hrpCountEC.collect {
                binding.tvHrEcCount.text = it.toString()
            }
        }
        lifecycleScope.launch {
            viewModel.abhaOldGeneratedCount.collect {
                binding.tvAbhaOldCount.text = it.toString()
            }
        }
        lifecycleScope.launch {
            viewModel.abhaNewGeneratedCount.collect {
                binding.tvAbhaNewCount.text = it.toString()
            }
        }
        lifecycleScope.launch {
            viewModel.rchIdCount.collect {
                binding.tvRch.text = it.toString()
            }
        }

        lifecycleScope.launch {
            combine(
                viewModel.ancNonFollowUpCount,
                viewModel.pncNonFollowUpCount,
                viewModel.ecNonFollowUpCount
            ) { anc, pnc, ec ->
                anc + pnc + ec
            }.collect { total ->
                binding.tvNon.text = total.toString()
            }
        }

        lifecycleScope.launch {
            combine(
                viewModel.ecrMissedPeriodCount,
                viewModel.ectMissedPeriodCount
            ) { ecr, ect ->
                ecr + ect
            }.collect { total ->
                binding.tvMiss.text = total.toString()
            }
        }

        lifecycleScope.launch {
            viewModel.ecrMissedPeriodCount.collect {
                countMissedPeriodCases.incrementAndGet()
                ecrMissedPeriodCount = it
            }
        }

        lifecycleScope.launch {
            viewModel.ectMissedPeriodCount.collect {
                countMissedPeriodCases.incrementAndGet()
                ectMissedPeriodCount = it
            }
        }

        binding.calendarView.setOnDateChangeListener { a, b, c, d ->
            val calLong = Calendar.getInstance().apply {
                set(Calendar.YEAR, b)
                set(Calendar.MONTH, c)
                set(Calendar.DAY_OF_MONTH, d)
            }.timeInMillis
            viewModel.setDate(calLong)
        }
    }

    /** Set by a drag or an arrow tap, consumed by the next page change — see [setUpImpactDashboard]. */
    private var carouselMovedByUser = false

    /** Impact Dashboard: auto-moving carousel of badge progress. */
    private fun setUpImpactDashboard() {
        val carouselAdapter = BadgeCarouselAdapter()
        binding.vpImpactBadges.adapter = carouselAdapter

        // The carousel also advances on its own every few seconds. A tick on those is the
        // phone buzzing in her pocket unprompted, which reads as a notification rather than
        // as feedback, so haptics are armed only by a drag or a tap on the arrows.
        fun step(direction: Int) {
            val count = carouselAdapter.itemCount
            if (count == 0) return
            carouselMovedByUser = true
            binding.vpImpactBadges.setCurrentItem(
                (binding.vpImpactBadges.currentItem + direction + count) % count, true
            )
        }
        binding.btnCarouselPrev.setOnClickListener { step(-1) }
        binding.btnCarouselNext.setOnClickListener { step(+1) }

        // card-deck feel: neighbours peek in, scale and fade with the swipe
        binding.vpImpactBadges.apply {
            offscreenPageLimit = 1 // one neighbour each side — enough for peek, lighter on low-RAM devices
            (getChildAt(0) as? RecyclerView)?.apply {
                val peek = (36 * resources.displayMetrics.density).toInt()
                setPadding(peek, 0, peek, 0)
                clipToPadding = false
            }
            setPageTransformer(CompositePageTransformer().apply {
                addTransformer(MarginPageTransformer((12 * resources.displayMetrics.density).toInt()))
                addTransformer { page, position ->
                    val scale = 1f - 0.1f * abs(position)
                    page.scaleX = scale
                    page.scaleY = scale
                    page.alpha = 1f - 0.3f * abs(position)
                }
            })
            // gentle tactile tick as pages settle + "3 / 8" position counter
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageScrollStateChanged(state: Int) {
                    if (state == ViewPager2.SCROLL_STATE_DRAGGING) carouselMovedByUser = true
                }

                override fun onPageSelected(position: Int) {
                    if (carouselMovedByUser) {
                        performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        carouselMovedByUser = false
                    }
                    _binding?.tvCarouselCounter?.text =
                        "${position + 1} / ${carouselAdapter.itemCount}"
                }
            })
        }
        // debug-only presentation helper: long-press the title to stage demo progress
        if (BuildConfig.DEBUG) binding.tvImpactTitle.setOnLongClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val msg = try {
                    demoSeeder.stage()
                } catch (e: Exception) {
                    "Demo staging failed: ${e.message}"
                }
                Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
            }
            true
        }
        binding.cvImpactDashboard.setOnClickListener {
            try {
                findNavController().navigate(R.id.badgeShelfFragment)
            } catch (e: Exception) {
                Timber.e(e, "Badge shelf navigation from dashboard failed")
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                badgeRepository.shelf.collect { cards ->
                    val b = _binding ?: return@collect
                    // empty ⇔ badge feature killed remotely — hide cleanly
                    b.cvImpactDashboard.visibility =
                        if (cards.isEmpty()) View.GONE else View.VISIBLE
                    carouselAdapter.submitList(cards.sortedByDescending { card ->
                        val target = (card.state?.nextTarget ?: 1L).coerceAtLeast(1L)
                        (card.state?.progress ?: 0L).toDouble() / target
                    }) {
                        val vp = _binding?.vpImpactBadges ?: return@submitList
                        _binding?.tvCarouselCounter?.text =
                            "${vp.currentItem + 1} / ${carouselAdapter.itemCount}"
                    }
                }
            }
        }
        // gentle auto-advance; pauses while the worker is swiping or tab hidden
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                while (true) {
                    delay(CAROUSEL_INTERVAL_MS)
                    val b = _binding ?: continue
                    val count = carouselAdapter.itemCount
                    if (count > 1 && b.vpImpactBadges.scrollState == ViewPager2.SCROLL_STATE_IDLE) {
                        b.vpImpactBadges.setCurrentItem(
                            (b.vpImpactBadges.currentItem + 1) % count, true
                        )
                    }
                }
            }
        }
    }

    companion object {
        private const val CAROUSEL_INTERVAL_MS = 4_000L
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}