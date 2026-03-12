package org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.databinding.FragmentIncentiveVerificationBinding
import org.piramalswasthya.sakhi.ui.asha_supervisor.SupervisorActivity
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.adapter.AshaWorkerAdapter
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.viewModel.IncentiveVerificationViewModel
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.viewModel.VerificationUiState
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class IncentiveVerificationFragment : Fragment() {

    private var _binding: FragmentIncentiveVerificationBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var preferenceDao: PreferenceDao

    private val viewModel: IncentiveVerificationViewModel by viewModels()
    private lateinit var adapter: AshaWorkerAdapter

    private val args: IncentiveVerificationFragmentArgs by navArgs()

    private val selectedMonth by lazy {
        args.selectedMonth.takeIf { it in 1..12 }
            ?: (Calendar.getInstance().get(Calendar.MONTH) + 1)
    }
    private val selectedYear by lazy {
        args.selectedYear.takeIf { it > 0 }
            ?: Calendar.getInstance().get(Calendar.YEAR)
    }

    private var isFirstResume = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIncentiveVerificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearchView()
        observeViewModel()

        val user = preferenceDao.getLoggedInUser()
        binding.tvSupervisorName.text = "Supervisor: ${user?.userName ?: "-"}"
        binding.tvSupervisorId.text = "Supervisor ID: ${user?.userId ?: "-"}"

        viewModel.init(args.status, args.facilityId, selectedMonth, selectedYear)

        val monthNames = resources.getStringArray(R.array.months)
        binding.tvMonth.text = "${monthNames[selectedMonth - 1]}, $selectedYear"
    }

    override fun onResume() {
        super.onResume()
        if (isFirstResume) {
            isFirstResume = false
            return
        }
        viewModel.refresh()
    }

    private fun setupRecyclerView() {
        adapter = AshaWorkerAdapter { worker ->
            val bundle = Bundle().apply {
                putString("worker_id", worker.id)
                putString("worker_name", worker.name)
                putString("sc_name", worker.serviceCenter)
                putInt("selected_month", selectedMonth)
                putInt("selected_year", selectedYear)
                putString("status", worker.status.name)
            }
            findNavController().navigate(R.id.workerDetailFragment, bundle)
        }
        binding.rvAshaWorkers.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        binding.rvAshaWorkers.adapter = adapter
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.search(newText ?: "")
                return true
            }
        })
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            if (_binding == null) return@observe
            when (state) {
                is VerificationUiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.contentLayout.visibility = View.GONE
                }
                is VerificationUiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.contentLayout.visibility = View.VISIBLE

                    val summary = state.summary
                    binding.tvVerifiedCount.text = "Verified: ${summary.verified}"
                    binding.tvPendingCount.text = "Pending: ${summary.pending}"
                    binding.tvRejectedCount.text = "Rejected: ${summary.rejected}"

                    adapter.submitList(state.workers)
                }
                is VerificationUiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.contentLayout.visibility = View.VISIBLE
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
                getString(R.string.incentive_verification)
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}