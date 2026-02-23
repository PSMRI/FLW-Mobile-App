package org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.ncd_referred.filter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.NCDReferTypeFilterAdapter
import org.piramalswasthya.sakhi.databinding.ChildImmunizationFilterBottomSheetFragmentBinding
import org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.ncd_referred.list.NcdRefferedListViewModel

@AndroidEntryPoint
class NCDReferTypeFilter : BottomSheetDialogFragment(), NCDReferTypeFilterAdapter.CategoryClickListener {

    private var _binding: ChildImmunizationFilterBottomSheetFragmentBinding? = null
    private val binding: ChildImmunizationFilterBottomSheetFragmentBinding
        get() = _binding!!

    private val viewModel: NcdRefferedListViewModel by viewModels({ requireParentFragment() })
    private var catTxt = ""

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

        val layoutManager= GridLayoutManager(
            context,
            requireContext().resources.getInteger(R.integer.icon_grid_span)
        )
        binding.rvCat.setLayoutManager(layoutManager)
        binding.rvCat.adapter =
            NCDReferTypeFilterAdapter(viewModel.categoryData(), this, viewModel)
    }

    override fun onClicked(catDataList: String) {
        viewModel.selectedFilter.value = catDataList
        if (catDataList.contains("ALL")) {
            viewModel.filterText("")

        } else {
            catTxt = catDataList
            viewModel.setSelectedFilter(catTxt)

        }
        dismiss()
    }
}