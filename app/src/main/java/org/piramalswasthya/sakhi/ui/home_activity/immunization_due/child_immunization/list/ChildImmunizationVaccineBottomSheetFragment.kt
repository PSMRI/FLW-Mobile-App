package org.piramalswasthya.sakhi.ui.home_activity.immunization_due.child_immunization.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.adapters.ImmunizationCategoryAdapter
import org.piramalswasthya.sakhi.databinding.BottomSheetImmVaccineBinding
import org.piramalswasthya.sakhi.model.ChildImmunizationCategory
import org.piramalswasthya.sakhi.model.ImmunizationDetailsDomain
import org.piramalswasthya.sakhi.model.VaccineCategoryDomain
import timber.log.Timber

@AndroidEntryPoint
class ChildImmunizationVaccineBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetImmVaccineBinding? = null
    private val binding: BottomSheetImmVaccineBinding
        get() = _binding!!

    private val viewModel: ChildImmunizationListViewModel by viewModels({ requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetImmVaccineBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvImmCat.adapter =
            ImmunizationCategoryAdapter(ImmunizationCategoryAdapter.ImmunizationIconClickListener {
                val benId = viewModel.getSelectedBenId()
                findNavController().navigate(
                    ChildImmunizationListFragmentDirections.actionChildImmunizationListFragmentToImmunizationFormFragment(
                        benId = benId, vaccineId = it
                    )
                )
                dismiss()
            })

        lifecycleScope.launch {
            viewModel.bottomSheetContent.collect {
                it?.let {
                    submitListToVaccinationRv(it)
                }
            }
        }
    }


    private fun submitListToVaccinationRv(detail: ImmunizationDetailsDomain) {
        val list = ChildImmunizationCategory.values().map { category ->
            VaccineCategoryDomain(category,
                vaccineStateList = detail.vaccineStateList.filter { it.vaccineCategory == category })
        }.filter { it.vaccineStateList.isNotEmpty() }
        Timber.d("Called list at bottom sheet ${_binding?.rvImmCat?.adapter} ${detail.ben.benId} $list")

        (_binding?.rvImmCat?.adapter as ImmunizationCategoryAdapter?)?.submitList(list)
    }

    fun setContentFlow(bottomSheetContent: Flow<ImmunizationDetailsDomain?>) {
        lifecycleScope.launchWhenResumed {
            findNavController().currentBackStackEntry
            bottomSheetContent.collect {
                it?.let { it1 -> submitListToVaccinationRv(it1) }
            }
        }
    }

}