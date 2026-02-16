package org.piramalswasthya.sakhi.ui.home_activity.sync_dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.adapters.SyncLogAdapter
import org.piramalswasthya.sakhi.databinding.FragmentSyncLogsTabBinding

@AndroidEntryPoint
class SyncLogsTabFragment : Fragment() {

    private var _binding: FragmentSyncLogsTabBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SyncDashboardViewModel by viewModels({ requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSyncLogsTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = SyncLogAdapter()
        val layoutManager = binding.rvSyncLogs.layoutManager as LinearLayoutManager
        binding.rvSyncLogs.adapter = adapter

        binding.fabClear.setOnClickListener {
            viewModel.clearLogs()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.syncLogs.collect { logs ->
                val wasAtBottom = !binding.rvSyncLogs.canScrollVertically(1)

                binding.tvEmpty.visibility = if (logs.isEmpty()) View.VISIBLE else View.GONE
                binding.rvSyncLogs.visibility = if (logs.isEmpty()) View.GONE else View.VISIBLE

                adapter.submitList(logs) {
                    if (wasAtBottom && logs.isNotEmpty()) {
                        layoutManager.scrollToPosition(logs.size - 1)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
