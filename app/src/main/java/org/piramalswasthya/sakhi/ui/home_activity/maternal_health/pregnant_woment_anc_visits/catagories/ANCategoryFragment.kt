package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pregnant_woment_anc_visits.catagories

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.sakhi.databinding.FragmentAncategoryBinding
import org.piramalswasthya.sakhi.databinding.FragmentSchedulersBinding
import org.piramalswasthya.sakhi.helpers.getDateString
import org.piramalswasthya.sakhi.ui.home_activity.home.HomeFragmentDirections
import org.piramalswasthya.sakhi.ui.home_activity.home.SchedulerViewModel


@AndroidEntryPoint
class ANCategoryFragment : Fragment() {

    private var _binding: FragmentAncategoryBinding? = null
    private val binding: FragmentAncategoryBinding
        get() = _binding!!
    private val viewModel: SchedulerViewModel by viewModels({ requireActivity() })


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAncategoryBinding.inflate(layoutInflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.cvAncone.setOnClickListener {
            findNavController().navigate(ANCategoryFragmentDirections.actionANCategoryFragmentToPwAncVisitsFragment())
        }
        viewModel.date.observe(viewLifecycleOwner) {

        }

        viewModel.ancOneCount.observe(viewLifecycleOwner) {
            binding.tvAncone.text = it.toString()
        }

        viewModel.ancTwoCount.observe(viewLifecycleOwner) {
            binding.tvanctwo.text = it.toString()

        }
        viewModel.ancThreeCount.observe(viewLifecycleOwner) {
            binding.tvPwi.text = it.toString()

        }
        viewModel.ancFourCount.observe(viewLifecycleOwner) {
            binding.tvAncFour.text = it.toString()

        }


    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}