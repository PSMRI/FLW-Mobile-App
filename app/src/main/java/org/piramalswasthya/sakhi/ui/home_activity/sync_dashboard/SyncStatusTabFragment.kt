package org.piramalswasthya.sakhi.ui.home_activity.sync_dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.work.WorkInfo
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.FailedWorkerAdapter
import org.piramalswasthya.sakhi.adapters.SyncDashboardStatusAdapter
import org.piramalswasthya.sakhi.databinding.FragmentSyncStatusTabBinding
import org.piramalswasthya.sakhi.model.asDomainModel

@AndroidEntryPoint
class SyncStatusTabFragment : Fragment() {

    private var _binding: FragmentSyncStatusTabBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SyncDashboardViewModel by viewModels({ requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSyncStatusTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    private var failedExpanded = false
    private lateinit var failedWorkerAdapter: FailedWorkerAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = SyncDashboardStatusAdapter()
        binding.rvSyncStatus.adapter = adapter
        binding.rvSyncStatus.addItemDecoration(
            DividerItemDecoration(context, LinearLayout.VERTICAL)
        )

        failedWorkerAdapter = FailedWorkerAdapter()
        binding.rvFailedWorkers.adapter = failedWorkerAdapter

        val localNames = viewModel.getLocalNames(requireContext())
        val englishNames = viewModel.getEnglishNames(requireContext())

        // Collect sync status
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.syncStatus.collect { statusList ->
                adapter.submitList(statusList.asDomainModel(localNames, englishNames))
            }
        }

        // Collect overall progress
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.overallProgress.collect { (synced, total) ->
                binding.tvProgressText.text = getString(
                    R.string.sync_dashboard_records_synced, synced, total
                )
                binding.progressOverall.max = if (total > 0) total else 1
                binding.progressOverall.progress = synced
            }
        }

        // Observe worker states
        viewModel.workerStates.observe(viewLifecycleOwner) { workInfoList ->
            updateWorkerStatus(workInfoList)
        }

        // Observe failed worker details
        viewModel.failedWorkerDetails.observe(viewLifecycleOwner) { failedList ->
            failedWorkerAdapter.submitList(failedList)
        }

        // Toggle expand/collapse on tap
        binding.tvWorkerDetails.setOnClickListener {
            failedExpanded = !failedExpanded
            binding.rvFailedWorkers.visibility = if (failedExpanded) View.VISIBLE else View.GONE
        }
    }

    private fun updateWorkerStatus(workInfoList: List<WorkInfo>?) {
        if (workInfoList.isNullOrEmpty()) {
            binding.tvWorkerStatus.text = getString(R.string.sync_dashboard_worker_idle)
            binding.tvWorkerDetails.visibility = View.GONE
            binding.rvFailedWorkers.visibility = View.GONE
            return
        }

        val running = workInfoList.count { it.state == WorkInfo.State.RUNNING }
        val failed = workInfoList.filter { it.state == WorkInfo.State.FAILED }
        val succeeded = workInfoList.count { it.state == WorkInfo.State.SUCCEEDED }
        val total = workInfoList.size

        when {
            running > 0 -> {
                binding.tvWorkerStatus.text = getString(
                    R.string.sync_dashboard_worker_running, running, total
                )
                binding.tvWorkerDetails.visibility = View.GONE
                binding.rvFailedWorkers.visibility = View.GONE
            }
            failed.isNotEmpty() -> {
                binding.tvWorkerStatus.text = getString(R.string.sync_dashboard_worker_idle)
                binding.tvWorkerDetails.text = getString(
                    R.string.sync_dashboard_worker_failed, failed.size
                )
                binding.tvWorkerDetails.visibility = View.VISIBLE
                // Keep expanded state across updates
                binding.rvFailedWorkers.visibility =
                    if (failedExpanded) View.VISIBLE else View.GONE
            }
            succeeded == total -> {
                binding.tvWorkerStatus.text = getString(R.string.sync_dashboard_worker_complete)
                binding.tvWorkerDetails.visibility = View.GONE
                binding.rvFailedWorkers.visibility = View.GONE
                failedExpanded = false
            }
            else -> {
                binding.tvWorkerStatus.text = getString(R.string.sync_dashboard_worker_idle)
                binding.tvWorkerDetails.visibility = View.GONE
                binding.rvFailedWorkers.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
