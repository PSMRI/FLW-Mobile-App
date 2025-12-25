package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pmsma.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.adapters.PmsmaVisitAdapter
import org.piramalswasthya.sakhi.databinding.BottomSheetAncBinding
import org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pmsma.pmsmaHighRiskList.PmsmaHighRiskListFragmentDirections
import org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pregnant_woment_anc_visits.list.PwAncVisitsListFragmentDirections
import org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pregnant_woment_anc_visits.list.PwAncVisitsListViewModel
import org.piramalswasthya.sakhi.utils.Log

@AndroidEntryPoint
class PmsmaBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetAncBinding? = null
    private val binding: BottomSheetAncBinding
        get() = _binding!!

//    private val viewModel: PmsmaVisitsListViewModel by viewModels({ requireParentFragment() })
    private val viewModel: PwAncVisitsListViewModel by viewModels({ requireParentFragment() })
    private var hhId: Long = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetAncBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        hhId = arguments?.getLong("hhId") ?: 0L
        setupRecyclerView()
        observeList()
    }

    private fun setupRecyclerView() {
        binding.rvAnc.adapter = PmsmaVisitAdapter(PmsmaVisitAdapter.PmsmaVisitClickListener { benId, visitNumber, isLast ->
            navigateToPmsma(benId, visitNumber, isLast)
        })
        val divider = DividerItemDecoration(context, LinearLayout.VERTICAL)
        binding.rvAnc.addItemDecoration(divider)
    }

    private fun observeList() {
        lifecycleScope.launch {
//            viewModel.bottomSheetList.collect { list ->
//                (binding.rvAnc.adapter as? PmsmaVisitAdapter)?.submitList(list)
//            }
//            viewModel.pmsmaBottomSheetList.collect { list ->
//                Log.v("nnfnsdnmn","${list}")
//                (binding.rvAnc.adapter as? PmsmaVisitAdapter)?.submitList(list)
//            }
        }
    }

    private fun navigateToPmsma(benId: Long, visitNumber: Int, lastItemClick: Boolean) {
        val fromHighRisk = arguments?.getBoolean("fromHighRisk") ?: false
        val action = if (fromHighRisk) {
            PmsmaHighRiskListFragmentDirections.actionPmsmaHighRiskListFragmentToPmsmaFragment(
                benId, hhId, visitNumber, lastItemClick
            )
        } else {
            PwAncVisitsListFragmentDirections.actionPwAncVisitsFragmentToPmsmaFragment(
                benId, hhId, visitNumber, lastItemClick
            )
        }
        findNavController().navigate(action)
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
