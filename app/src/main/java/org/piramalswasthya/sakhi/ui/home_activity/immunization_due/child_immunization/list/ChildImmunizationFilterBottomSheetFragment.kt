package org.piramalswasthya.sakhi.ui.home_activity.immunization_due.child_immunization.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.ImmunizationBirthDoseCategoryAdapter
import org.piramalswasthya.sakhi.databinding.ChildImmunizationFilterBottomSheetFragmentBinding

@AndroidEntryPoint
class ChildImmunizationFilterBottomSheetFragment : BottomSheetDialogFragment(),ImmunizationBirthDoseCategoryAdapter.CategoryClickListener {

    private var _binding: ChildImmunizationFilterBottomSheetFragmentBinding? = null
    private val binding: ChildImmunizationFilterBottomSheetFragmentBinding
        get() = _binding!!

    private val viewModel: ChildImmunizationListViewModel by viewModels({ requireParentFragment() })
    private var catTxt = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = ChildImmunizationFilterBottomSheetFragmentBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val layoutManager= GridLayoutManager(context, requireContext().resources.getInteger(R.integer.icon_grid_span))
        binding.rvCat.setLayoutManager(layoutManager)
        binding.rvCat.adapter = ImmunizationBirthDoseCategoryAdapter(viewModel.categoryData(),this,viewModel)
    }

    override fun onClicked(catDataList: String) {
        if (catDataList.contains("ALL")) {
            viewModel.filterText("")

        } else {
            catTxt = catDataList
            viewModel.filterText(catTxt)

        }
        dismiss()
    }
}