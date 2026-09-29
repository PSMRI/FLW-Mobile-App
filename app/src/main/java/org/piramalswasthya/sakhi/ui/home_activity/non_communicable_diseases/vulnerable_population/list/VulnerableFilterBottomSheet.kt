package org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.vulnerable_population.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.VulnerableFilterBottomSheetAdapter
import org.piramalswasthya.sakhi.databinding.ChildImmunizationFilterBottomSheetFragmentBinding
import kotlin.getValue

@AndroidEntryPoint
class VulnerableFilterBottomSheet : BottomSheetDialogFragment(), VulnerableFilterBottomSheetAdapter.CategoryClickListener {

    private var _binding: ChildImmunizationFilterBottomSheetFragmentBinding? = null
    private val binding: ChildImmunizationFilterBottomSheetFragmentBinding
        get() = _binding!!

    private val viewModel: VulnerablePopulationViewModel by viewModels({ requireParentFragment() })




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = ChildImmunizationFilterBottomSheetFragmentBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivClose.setOnClickListener {
            dismiss()
        }

        val layoutManager= GridLayoutManager(context, requireContext().resources.getInteger(R.integer.icon_grid_span_supervisor))
        binding.rvCat.setLayoutManager(layoutManager)
        binding.rvCat.adapter =
            VulnerableFilterBottomSheetAdapter(viewModel.categoryData(), this, viewModel)
    }

    override fun onClicked(catDataList: String) {
        viewModel.selectedFilter.value = catDataList
        val englishKey = viewModel.toFilterKey(catDataList)
        viewModel.filterRiskCode(englishKey)
        dismiss()


    }


}