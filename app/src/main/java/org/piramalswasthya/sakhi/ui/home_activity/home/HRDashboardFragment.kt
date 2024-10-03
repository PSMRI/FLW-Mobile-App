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
import org.piramalswasthya.sakhi.databinding.FragmentHrDashbaordBinding

@AndroidEntryPoint
class HRDashboardFragment : Fragment()  {

    private var _binding: FragmentHrDashbaordBinding? = null
    private val binding: FragmentHrDashbaordBinding
        get() = _binding!!

    private val viewModel: SchedulerViewModel by viewModels({ requireActivity() })


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHrDashbaordBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            viewModel.lowWeightBabiesCount.collect {
                binding.tvLbwb.text = it.toString()
            }
        }

        lifecycleScope.launch {
            viewModel.hrpCountEC.collect {
                binding.tvHrnp.text = it.toString()
            }
        }

        lifecycleScope.launch {
            viewModel.hrpPregnantWomenListCount.collect {
                binding.tvMcp.text = it.toString()
            }
        }

        lifecycleScope.launch {
            viewModel.eligibleCoupleListCount.collect {
                binding.tvec.text = it.toString()
            }
        }

        lifecycleScope.launch {
            viewModel.ncdEligiblelListCount.collect {
                binding.tvncd.text = it.toString()
            }
        }


        lifecycleScope.launch {
            viewModel.tbScreeningListCount.collect {
                binding.tvTB.text = it.toString()
            }
        }

        lifecycleScope.launch {
            viewModel.pregnentWomenListCount.collect {
                binding.pregWC.text = it.toString()
            }
        }




        lifecycleScope.launch {
            viewModel.hrpDueCount.collect {
                binding.tvHrp.text = it.toString()
            }
        }

        binding.clHrnp.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToHRPNonPregnantListFragment())
        }
        binding.cvLwb.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToInfantRegListFragment())
        }
        binding.cvHrp.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToHRPPregnantListFragment())
        }
        binding.cvMcp.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToHRPPregnantAssessFragment())
        }
        binding.cvEcreg.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToEligibleCoupleRegFragment())
        }
        binding.cvNcd.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToNcdEligibleListFragment())

        }
        binding.cvTbB.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToTBScreeningListFragment())
        }
        binding.cvPwr.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToPwRegistrationFragment())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}