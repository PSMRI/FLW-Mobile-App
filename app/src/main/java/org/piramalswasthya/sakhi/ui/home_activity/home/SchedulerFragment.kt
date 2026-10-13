package org.piramalswasthya.sakhi.ui.home_activity.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.databinding.FragmentSchedulerBinding
import org.piramalswasthya.sakhi.ui.home_activity.home.SchedulerViewModel.State.LOADED
import org.piramalswasthya.sakhi.ui.home_activity.home.SchedulerViewModel.State.LOADING
import java.util.Calendar
import java.util.concurrent.atomic.AtomicInteger
import org.piramalswasthya.sakhi.utils.safeNavigate


@AndroidEntryPoint
class SchedulerFragment : Fragment() {


    private var _binding: FragmentSchedulerBinding? = null
    private val binding: FragmentSchedulerBinding
        get() = _binding!!

    private var countMissedPeriodCases = AtomicInteger(0)
    private var ecrMissedPeriodCount = 0
    private var ectMissedPeriodCount = 0

    private val viewModel: SchedulerViewModel by viewModels({ requireActivity() })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSchedulerBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
            safeNavigate(HomeFragmentDirections.actionHomeFragmentToPwAncVisitsFragment(source = 2))
        }

        binding.cvImm.setOnClickListener {
            safeNavigate(HomeFragmentDirections.actionHomeFragmentToChildImmunizationListFragment(showDueOnly = true))
        }
        binding.cvHrp.setOnClickListener {
            safeNavigate(HomeFragmentDirections.actionHomeFragmentToHRPPregnantListFragment())
        }
        binding.cvConfirmedHrp.setOnClickListener {
            safeNavigate(HomeFragmentDirections.actionHomeFragmentToPwAncVisitsFragment(source = 3))
        }
        binding.cvNonHrp.setOnClickListener {
            safeNavigate(HomeFragmentDirections.actionHomeFragmentToHRPNonPregnantListFragment())
        }
        binding.cvLwb.setOnClickListener {
            safeNavigate(HomeFragmentDirections.actionHomeFragmentToInfantRegListFragment(onlyLowBirthWeight = true))
        }
        binding.cvAbha.setOnClickListener {
            safeNavigate(HomeFragmentDirections.actionNavHomeToAllBenFragment(1))
//            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToHRPPregnantListFragment())
        }
        binding.cvRch.setOnClickListener {
            safeNavigate(HomeFragmentDirections.actionNavHomeToAllBenFragment(2))
//            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToHRPPregnantListFragment())
        }
        binding.cvNon.setOnClickListener {
            safeNavigate(HomeFragmentDirections.actionHomeFragmentToNonFollowUpFragment())
        }
        binding.cvMiss.setOnClickListener {
            safeNavigate(HomeFragmentDirections.actionHomeFragmentToMissedPeriodFragment())
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
            viewModel.hrpConfirmedCount.collect {
                binding.tvConfirmedHrp.text = it.toString()
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}