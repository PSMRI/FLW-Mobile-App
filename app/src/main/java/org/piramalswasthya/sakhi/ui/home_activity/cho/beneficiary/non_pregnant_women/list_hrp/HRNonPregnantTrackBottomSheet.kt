package org.piramalswasthya.sakhi.ui.home_activity.cho.beneficiary.non_pregnant_women.list_hrp

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
import org.piramalswasthya.sakhi.adapters.HRPPregTrackAdapter
import org.piramalswasthya.sakhi.databinding.BottomSheetHrpTrackBinding
import org.piramalswasthya.sakhi.model.HRPNonPregnantTrackCache
import timber.log.Timber

@AndroidEntryPoint
class HRNonPregnantTrackBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetHrpTrackBinding? = null
    private val binding: BottomSheetHrpTrackBinding
        get() = _binding!!

    private val viewModel: HRPNonPregnantListViewModel by viewModels({ requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetHrpTrackBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvHrpTract.adapter =
            HRPPregTrackAdapter(HRPPregTrackAdapter.HRPTrackClickListener {
                val benId = viewModel.benId.value!!
                findNavController().navigate(
                    HRPNonPregnantListFragmentDirections.actionHRPNonPregnantListFragmentToHRPNonPregnantTrackFragment(
                        benId = benId, trackId = it
                    )
                )
                dismiss()
            },
                visit = "Visit On "
            )

        lifecycleScope.launch {
            viewModel.getTrackDetails().let {
                it?.let {
                    listOfHrpPregTrack(it)
                }
            }
        }
    }


    private fun listOfHrpPregTrack(list: List<HRPNonPregnantTrackCache>) {
//        Timber.d("Called list at bottom sheet ${_binding?.rvHrpTract?.adapter} ${detail.ben.benId} $list")
        Timber.d("Called list at bottom sheet   ${list.size}")

        (_binding?.rvHrpTract?.adapter as HRPPregTrackAdapter?)?.submitList(
            list.map {
                trackCache -> trackCache.asDomainModel()
        })
    }

    override fun dismiss() {
//        submitListToAncRv(emptyList())
        super.dismiss()
    }

}