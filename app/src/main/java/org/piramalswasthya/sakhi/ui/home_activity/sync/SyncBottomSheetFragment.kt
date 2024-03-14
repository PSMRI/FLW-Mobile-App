package org.piramalswasthya.sakhi.ui.home_activity.sync

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.adapters.SyncStatusAdapter
import org.piramalswasthya.sakhi.databinding.BottomSheetSyncBinding
import org.piramalswasthya.sakhi.model.asDomainModel


class SyncBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetSyncBinding? = null
    private val binding: BottomSheetSyncBinding
        get() = _binding!!

    private val viewModel: SyncViewModel by viewModels({ requireActivity() })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetSyncBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = SyncStatusAdapter()
        val divider = DividerItemDecoration(context, LinearLayout.VERTICAL)
        binding.rvSync.adapter = adapter
        binding.rvSync.addItemDecoration(divider)

        val localNames = viewModel.getLocalNames(requireContext())
        val englishNames = viewModel.getEnglishNames(requireContext())
        lifecycleScope.launch {
            viewModel.syncStatus.collect {
                binding.nsv.layoutParams.height = if (it.size * 150 < 800) it.size * 150 else 800
                adapter.submitList(it.asDomainModel(localNames, englishNames))
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}