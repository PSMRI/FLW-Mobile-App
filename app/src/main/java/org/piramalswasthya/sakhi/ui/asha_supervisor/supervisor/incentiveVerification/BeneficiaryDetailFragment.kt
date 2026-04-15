package org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.databinding.FragmentBeneficiaryDetailBinding
import org.piramalswasthya.sakhi.ui.asha_supervisor.SupervisorActivity
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.adapter.BeneficiaryAdapter
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.viewModel.BeneficiaryDetailViewModel
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.viewModel.BeneficiaryUiState
import java.util.Calendar

@AndroidEntryPoint
class BeneficiaryDetailFragment : Fragment() {

    private var _binding: FragmentBeneficiaryDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BeneficiaryDetailViewModel by viewModels()
    private lateinit var adapter: BeneficiaryAdapter

    private val userId by lazy { arguments?.getInt("worker_id") ?: 0 }
    private val activityId by lazy { arguments?.getInt("activity_id") ?: 0 }
    private val activityName by lazy { arguments?.getString("activity_name") ?: "" }
    private val groupName by lazy { arguments?.getString("group_name") ?: "" }
    private val selectedMonth by lazy {
        arguments?.getInt("selected_month")?.takeIf { it in 1..12 }
            ?: (Calendar.getInstance().get(Calendar.MONTH) + 1)
    }
    private val selectedYear by lazy {
        arguments?.getInt("selected_year")?.takeIf { it > 0 }
            ?: Calendar.getInstance().get(Calendar.YEAR)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBeneficiaryDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = BeneficiaryAdapter()
        binding.rvBeneficiaries.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBeneficiaries.adapter = adapter

        binding.tvActivityHeader.text = activityName

        observeViewModel()

        viewModel.fetchBeneficiaries(
            userId = userId,
            month = selectedMonth,
            year = selectedYear,
            activityId = activityId
        )
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            if (_binding == null) return@observe
            when (state) {
                is BeneficiaryUiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvBeneficiaries.visibility = View.GONE
                }
                is BeneficiaryUiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvBeneficiaries.visibility = View.VISIBLE
                    adapter.submitList(state.records)

                    if (state.records.isEmpty()) {
                        binding.tvEmptyState.visibility = View.VISIBLE
                        binding.llHeader.visibility = View.GONE
                    } else {
                        binding.tvEmptyState.visibility = View.GONE
                        binding.llHeader.visibility = View.VISIBLE
                    }
                }
                is BeneficiaryUiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvBeneficiaries.visibility = View.VISIBLE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as SupervisorActivity).updateActionBar(
                R.drawable.ic__incentive,
                groupName
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}