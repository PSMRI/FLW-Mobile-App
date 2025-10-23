package org.piramalswasthya.sakhi.ui.home_activity.child_care.children_under_five_years


import android.os.Bundle
import android.util.Log
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
import org.piramalswasthya.sakhi.adapters.ChildCareVisitListAdapter
import org.piramalswasthya.sakhi.databinding.FragmentCUFYBottomSheetBinding

@AndroidEntryPoint
class CUFYBottomSheetFragment : BottomSheetDialogFragment() {



    private var _binding: FragmentCUFYBottomSheetBinding? = null
    private val binding: FragmentCUFYBottomSheetBinding
        get() = _binding!!


    private val viewModel: CUFYListViewModel by viewModels({ requireParentFragment() })


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentCUFYBottomSheetBinding.inflate(inflater,container,false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val type = arguments?.getString("type")

        binding.rvAnc.adapter = ChildCareVisitListAdapter(
            ChildCareVisitListAdapter.ChildOptionsClickListener { formType, isViewMode ->
                val benId = arguments?.getLong("benId") ?: 0L
                val hhId = arguments?.getLong("hhId") ?: 0L
                val dob = arguments?.getLong("dob") ?: 0L


                navigateToForm(formType, benId, hhId, dob, isViewMode)
                this.dismiss()
            }
        )

        val divider = DividerItemDecoration(context, LinearLayout.VERTICAL)
        binding.rvAnc.addItemDecoration(divider)
        observeList(type)
    }

    private fun observeList(type: String?) {
        lifecycleScope.launch {
            val filteredOptions = viewModel.getFilteredOptions(type)
            (_binding?.rvAnc?.adapter as ChildCareVisitListAdapter?)?.submitList(filteredOptions)
        }
    }


    private fun navigateToForm(formType: String, benId: Long, hhId: Long, dob: Long, isViewMode: Boolean) {
        try {
            when (formType) {
                /* "Check SAM" -> {
                     val action = ChildrenUnderFiveYearsListFragmentDirections
                         .actionChildrenUnderFiveYearsListFragmentToSamFormFragment(
                             benId = benId,
                             hhId = hhId,
                             dob = dob,
                             isViewMode = isViewMode
                         )
                     findNavController().navigate(action)
                 }*/
                "ORS" -> {
                    // Navigate to ORS form
                    Toast.makeText(requireContext(), "ORS Form - Coming Soon", Toast.LENGTH_SHORT).show()
                }
                "IFA" -> {
                    // Navigate to IFA form
                    Toast.makeText(requireContext(), "IFA Form - Coming Soon", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error navigating to form", Toast.LENGTH_SHORT).show()
            Log.e("FormNavigation", "Error navigating to $formType form", e)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(benId: Long, hhId: Long, dob: Long, type : String): CUFYBottomSheetFragment {
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