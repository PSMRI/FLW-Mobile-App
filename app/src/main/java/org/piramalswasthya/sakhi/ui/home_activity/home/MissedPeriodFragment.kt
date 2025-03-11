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
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.databinding.FragmentMissedPeriodBinding
import org.piramalswasthya.sakhi.databinding.FragmentNonFollowUpBinding
import org.piramalswasthya.sakhi.ui.home_activity.home.SchedulerViewModel.State.LOADED
import org.piramalswasthya.sakhi.ui.home_activity.home.SchedulerViewModel.State.LOADING


@AndroidEntryPoint
class MissedPeriodFragment : Fragment() {


    private var _binding: FragmentMissedPeriodBinding? = null
    private val binding: FragmentMissedPeriodBinding
        get() = _binding!!


    private val viewModel: SchedulerViewModel by viewModels({ requireActivity() })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMissedPeriodBinding.inflate(layoutInflater, container, false)
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

        lifecycleScope.launch {
            viewModel.ecrMissedPeriodCount.collect {
                binding.tvAnc.text = it.toString()
            }
        }

        binding.cvAnc.setOnClickListener {
            findNavController().navigate(MissedPeriodFragmentDirections.actionMissedPeriodFragmentToEligibleCoupleListFragment(2))
        }

        lifecycleScope.launch {
            viewModel.ectMissedPeriodCount.collect {
                binding.tvHrEcPnc.text = it.toString()
            }
        }

        binding.cvNonPnc.setOnClickListener {
            findNavController().navigate(MissedPeriodFragmentDirections.actionMissedPeriodFragmentToEligibleCoupleTrackingListFragment(2))
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}