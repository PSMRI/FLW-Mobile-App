package org.piramalswasthya.sakhi.ui.home_activity.eligible_couple.tracking.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.adapters.ECTrackingAdapter
import org.piramalswasthya.sakhi.databinding.BottomSheetEcTrackingListBinding
import org.piramalswasthya.sakhi.model.ECTDomain
import timber.log.Timber

@AndroidEntryPoint
class ECTrackingListBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetEcTrackingListBinding? = null
    private val binding: BottomSheetEcTrackingListBinding
        get() = _binding!!

    private val viewModel: EligibleCoupleTrackingListViewModel by viewModels({ requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetEcTrackingListBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvAnc.adapter =
            ECTrackingAdapter(ECTrackingAdapter.ECTrackViewClickListener { benId, createdDate ->
                findNavController().navigate(
                    EligibleCoupleTrackingListFragmentDirections.actionEligibleCoupleTrackingListFragmentToEligibleCoupleTrackingFormFragment(
                        benId = benId, createdDate = createdDate
                    )
                )
                this.dismiss()
            })
        observeList()
    }

    private fun observeList() {
        lifecycleScope.launch {
            viewModel.bottomSheetList.collect {
                submitListToECtRv(it)
            }
        }
    }


    private fun submitListToECtRv(list: List<ECTDomain>) {
        Timber.d("Called list at bottom sheet $list")
        (_binding?.rvAnc?.adapter as ECTrackingAdapter?)?.submitList(list)
    }

}