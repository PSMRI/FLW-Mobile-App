package org.piramalswasthya.sakhi.ui.home_activity.all_ben.eye_surgery_registration.eyeBottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.databinding.FragmentEyeSurgeryBottomSheetBinding
import org.piramalswasthya.sakhi.ui.home_activity.all_ben.AllBenFragmentDirections
import org.piramalswasthya.sakhi.ui.home_activity.all_ben.eye_surgery_registration.eyeBottomsheet.adapter.EyeSurgeryVisitListAdapter
import org.piramalswasthya.sakhi.ui.home_activity.all_ben.eye_surgery_registration.eyeBottomsheet.model.EyeSurgeryVisitOption

@AndroidEntryPoint
class EyeSurgeryBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentEyeSurgeryBottomSheetBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EyeSurgeryListViewModel by viewModels()

    private var benName = ""
    private var gender = ""
    private var age = ""

    private var navigationCallback: NavigationCallback? = null

    fun setNavigationCallback(callback: NavigationCallback) {
        navigationCallback = callback
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEyeSurgeryBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }
    interface NavigationCallback {
        fun navigateToEyeSurgeryForm(
            benId: Long,
            hhId: Long,
            eyeSide: String,
            isViewMode: Boolean,
            formDataJson: String?,
            recordId: Int,
            benName: String,
            gender: String,
            age: String
        )
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val benId = arguments?.getLong("benId") ?: 0L
        val hhId = arguments?.getLong("hhId") ?: 0L
        benName = arguments?.getString("benName") ?: ""
        gender = arguments?.getString("gender") ?: ""
        age = arguments?.getString("age") ?: ""

        binding.txtName.text = benName
        binding.rvEyeVisits.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(requireContext())

        binding.rvEyeVisits.addItemDecoration(
            DividerItemDecoration(context, LinearLayout.VERTICAL)
        )

        binding.rvEyeVisits.adapter = EyeSurgeryVisitListAdapter(
            EyeSurgeryVisitListAdapter.ClickListener { option ->
                if (option.isAddNew) {
                    handleAddClick(benId, hhId)
                } else {
                    navigateToForm(
                        benId = benId,
                        hhId = hhId,
                        eyeSide = option.eyeSide ?: "",
                        isViewMode = true,
                        formDataJson = option.formDataJson,
                        recordId = option.recordId ?: 0
                    )
                }
            }
        )

        loadVisitList(benId)
    }

    private fun loadVisitList(benId: Long) {
        lifecycleScope.launch {
            val savedVisits = viewModel.getSavedVisits(benId)
            val availableEyes = viewModel.getAvailableEyes(benId)

            val viewOptions = savedVisits.mapIndexed { index, entity ->
                // eyeSide properly capitalize karke show karo
                val eyeLabel = when (entity.eyeSide.uppercase()) {
                    "LEFT" -> "Left"
                    "RIGHT" -> "Right"
                    "BOTH" -> "Both"
                    else -> entity.eyeSide
                }
                EyeSurgeryVisitOption(
                    title = "$eyeLabel Eye - Visit ${index + 1}",
                    visitDate = entity.visitDate,
                    eyeSide = entity.eyeSide,
                    isAddNew = false,
                    formDataJson = entity.formDataJson,
                    recordId = entity.id
                )
            }

            val finalList = if (availableEyes.isNotEmpty()) {
                viewOptions + EyeSurgeryVisitOption(
                    title = "Add New Visit",
                    visitDate = "",
                    eyeSide = null,
                    isAddNew = true,
                    formDataJson = null,
                    recordId = null
                )
            } else {
                viewOptions
            }

            (_binding?.rvEyeVisits?.adapter as EyeSurgeryVisitListAdapter?)
                ?.submitList(finalList)
        }
    }

    private fun handleAddClick(benId: Long, hhId: Long) {
        lifecycleScope.launch {
            val availableEyes = viewModel.getAvailableEyes(benId)
            when {
                availableEyes.isEmpty() -> dismiss()

                availableEyes.size == 1 -> {
                    // Sirf ek option available - directly wo pass karo
                    navigateToForm(
                        benId = benId,
                        hhId = hhId,
                        eyeSide = availableEyes[0],
                        isViewMode = false,
                        formDataJson = null,
                        recordId = 0
                    )
                }

                else -> {
                    // Multiple options - empty pass karo, form mein teeno enable honge
                    navigateToForm(
                        benId = benId,
                        hhId = hhId,
                        eyeSide = "",
                        isViewMode = false,
                        formDataJson = null,
                        recordId = 0
                    )
                }
            }
        }
    }

//    private fun navigateToForm(
//        benId: Long,
//        hhId: Long,
//        eyeSide: String,
//        isViewMode: Boolean,
//        formDataJson: String?,
//        recordId: Int
//    ) {
//        try {
//            requireParentFragment().findNavController().navigate(
//                AllBenFragmentDirections.actionAllBenFragmentToEyeSurgeryFormFragment(
//                    benId = benId,
//                    hhId = hhId,
//                    eyeSide = eyeSide,
//                    isViewMode = isViewMode,
//                    formDataJson = formDataJson,
//                    recordId = recordId,
//                    benName = benName,
//                    gender = gender,
//                    age = age
//                )
//            )
//            dismiss()
//        } catch (e: Exception) {
//            Toast.makeText(
//                requireContext(),
//                "Error: ${e.message}",
//                Toast.LENGTH_LONG
//            ).show()
//            e.printStackTrace()
//        }
//    }
//
private fun navigateToForm(
    benId: Long,
    hhId: Long,
    eyeSide: String,
    isViewMode: Boolean,
    formDataJson: String?,
    recordId: Int
) {
    navigationCallback?.navigateToEyeSurgeryForm(
        benId, hhId, eyeSide, isViewMode,
        formDataJson, recordId, benName, gender, age
    )
    dismiss()
}

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(
            benId: Long,
            hhId: Long,
            benName: String,
            gender: String,
            age: String
        ): EyeSurgeryBottomSheetFragment {
            return EyeSurgeryBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putLong("benId", benId)
                    putLong("hhId", hhId)
                    putString("benName", benName)
                    putString("gender", gender)
                    putString("age", age)
                }
            }
        }
    }
}