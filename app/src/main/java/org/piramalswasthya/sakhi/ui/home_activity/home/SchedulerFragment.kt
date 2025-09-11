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
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToPwAncVisitsFragment())
        }

        binding.cvImm.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToChildImmunizationListFragment())
        }
        binding.cvHrp.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToHRPPregnantListFragment())
        }
        binding.cvNonHrp.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToHRPNonPregnantListFragment())
        }
        binding.cvLwb.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToInfantRegListFragment())
        }
        binding.cvAbha.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionNavHomeToAbhaBenFragment(1))
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
            viewModel.abhaGeneratedCount.collect {
                binding.tvAbha.text = it.toString()
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