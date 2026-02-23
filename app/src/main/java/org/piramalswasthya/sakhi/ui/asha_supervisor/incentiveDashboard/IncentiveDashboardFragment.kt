package org.piramalswasthya.sakhi.ui.asha_supervisor.incentiveDashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.sakhi.databinding.FragmentIncentiveDashboardBinding

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

    }

}