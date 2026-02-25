package org.piramalswasthya.sakhi.ui.asha_supervisor.incentiveDashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.databinding.FragmentIncentiveDashboardBinding
import org.piramalswasthya.sakhi.ui.asha_supervisor.SupervisorActivity
import org.piramalswasthya.sakhi.ui.asha_supervisor.SupervisorHomeFragmentDirections

@AndroidEntryPoint
class IncentiveDashboardFragment : Fragment() {
    private var _binding: FragmentIncentiveDashboardBinding? = null
    private val binding: FragmentIncentiveDashboardBinding
        get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIncentiveDashboardBinding.inflate(layoutInflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.subCenterRV.adapter = SubCenterAdapter()
        binding.cardVerified.setOnClickListener {

            val action = SupervisorHomeFragmentDirections.actionSupervisorHomeFragmentToIncentiveVerificationFragment("verified")
            findNavController().navigate(action)


        }
        binding.cardPending.setOnClickListener {

            val action = SupervisorHomeFragmentDirections.actionSupervisorHomeFragmentToIncentiveVerificationFragment("pending")
            findNavController().navigate(action)
        }
        binding.cardOverdue.setOnClickListener {

            val action = SupervisorHomeFragmentDirections.actionSupervisorHomeFragmentToIncentiveVerificationFragment("overdue")
            findNavController().navigate(action)
        }
        binding.cardRejected.setOnClickListener {

            val action = SupervisorHomeFragmentDirections.actionSupervisorHomeFragmentToIncentiveVerificationFragment("rejected")
            findNavController().navigate(action)
        }






    }



}