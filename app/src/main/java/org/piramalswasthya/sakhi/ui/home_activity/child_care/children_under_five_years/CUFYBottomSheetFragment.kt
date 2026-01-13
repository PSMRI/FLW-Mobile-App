package org.piramalswasthya.sakhi.ui.home_activity.child_care.children_under_five_years


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
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.ChildCareVisitListAdapter
import org.piramalswasthya.sakhi.databinding.FragmentCUFYBottomSheetBinding
import org.piramalswasthya.sakhi.model.ChildOption
import org.piramalswasthya.sakhi.utils.dynamicFormConstants.FormConstants

@AndroidEntryPoint
class CUFYBottomSheetFragment : BottomSheetDialogFragment() {


    private var _binding: FragmentCUFYBottomSheetBinding? = null
    private val binding: FragmentCUFYBottomSheetBinding
        get() = _binding!!


    private lateinit var formID: String
    private val viewModel: CUFYListViewModel by viewModels({ requireParentFragment() })


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentCUFYBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val type = arguments?.getString("type")

        when (type) {
            FormConstants.SAM_FORM_NAME -> {
                formID = FormConstants.CHILDREN_UNDER_FIVE_SAM_FORM_ID
            }

            FormConstants.ORS_FORM_NAME -> {
                formID = FormConstants.CHILDREN_UNDER_FIVE_ORS_FORM_ID
            }

            FormConstants.IFA_FORM_NAME -> {
                formID = FormConstants.CHILDREN_UNDER_FIVE_IFA_FORM_ID
            }
        }

        binding.rvAnc.adapter = ChildCareVisitListAdapter(
            ChildCareVisitListAdapter.ChildOptionsClickListener { formType, visitDay, isViewMode, formDataJson, recordId ->
                val benId = arguments?.getLong("benId") ?: 0L
                val hhId = arguments?.getLong("hhId") ?: 0L
                val dob = arguments?.getLong("dob") ?: 0L

                navigateToForm(
                    formType,
                    benId,
                    hhId,
                    dob,
                    visitDay,
                    isViewMode,
                    formDataJson,
                    recordId
                )
                dismiss()
            }
        )

        val divider = DividerItemDecoration(context, LinearLayout.VERTICAL)
        binding.rvAnc.addItemDecoration(divider)
        observeList(type)
    }


    private fun observeList(type: String?) {
        lifecycleScope.launch {
            val benId = arguments?.getLong("benId") ?: 0L
            val hhId = arguments?.getLong("hhId") ?: 0L

            val savedList = viewModel.getSavedVisits(formID, benId)

            if (type == FormConstants.IFA_FORM_NAME) {
                val latestVisit = savedList.lastOrNull()

                val viewOptions = latestVisit?.let { entity ->
                    listOf(
                        ChildOption(
                            formType = mapFormIdToType(entity.formId),
                            title = getString(R.string.visit_2),
                            description = entity.visitDate,
                            isViewMode = true,
                            visitDay = entity.visitDate,
                            formDataJson = entity.formDataJson,
                            recordId = entity.id,
                            isIFA = true
                        )
                    )
                } ?: emptyList()

                val addOption = ChildOption(
                    formType = type ?: "",
                    title = getString(R.string.add_new_visit),
                    description = "",
                    isViewMode = false,
                    formDataJson = null
                )
                val finalList : List<ChildOption>
                if(viewOptions.isNotEmpty()){
                 finalList = viewOptions}
                else
                {
                    finalList =  viewOptions+ addOption
                }
                (_binding?.rvAnc?.adapter as ChildCareVisitListAdapter?)?.submitList(finalList)

            } else {
                val viewOptions = savedList.mapIndexed { index, entity ->
                    ChildOption(
                        formType = mapFormIdToType(entity.formId),
                        title = getString(R.string.visit, index + 1),
                        description = entity.visitDate,
                        isViewMode = true,
                        visitDay = entity.visitDate,
                        formDataJson = entity.formDataJson,
                        recordId = entity.id
                    )
                }
                val samStatus = viewModel.getSamStatusForBeneficiary(benId)

                val finalList = if (type == FormConstants.SAM_FORM_NAME && samStatus != getString(R.string.check_sam_)) {
                    viewOptions
                } else {
                    val addOption = ChildOption(
                        formType = type ?: "",
                        title = getString(R.string.add_new_visit),
                        description = "",
                        isViewMode = false,
                        formDataJson = null
                    )
                    viewOptions + addOption
                }

                (_binding?.rvAnc?.adapter as ChildCareVisitListAdapter?)?.submitList(finalList)
            }
        }
    }

    private fun mapFormIdToType(formId: String): String {
        return when (formId) {
            FormConstants.CHILDREN_UNDER_FIVE_SAM_FORM_ID -> FormConstants.SAM_FORM_NAME
            FormConstants.CHILDREN_UNDER_FIVE_ORS_FORM_ID -> FormConstants.ORS_FORM_NAME
            FormConstants.CHILDREN_UNDER_FIVE_IFA_FORM_ID -> FormConstants.IFA_FORM_NAME
            else -> formId
        }
    }

    private fun navigateToForm(
        formType: String,
        benId: Long,
        hhId: Long,
        dob: Long,
        visitDay: String?,
        isViewMode: Boolean,
        formDataJson: String?,
        recordId: Int?
    ) {
        try {
            findNavController().navigate(
                CUFYListFragmentDirections.actionChildrenUnderFiveYearListFragmentToChildrenUnderFiveYearFormFragment(
                    benId,
                    hhId,
                    formType,
                    isViewMode,
                    formDataJson,
                    recordId ?: 0

                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(benId: Long, hhId: Long, dob: Long, type: String): CUFYBottomSheetFragment {
            val args = Bundle().apply {
                putLong("benId", benId)
                putLong("hhId", hhId)
                putLong("dob", dob)
                putString("type", type)
            }
            return CUFYBottomSheetFragment().apply {
                arguments = args
            }
        }
    }
}