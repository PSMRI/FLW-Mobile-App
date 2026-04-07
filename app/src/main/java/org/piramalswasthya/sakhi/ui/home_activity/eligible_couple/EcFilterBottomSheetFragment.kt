package org.piramalswasthya.sakhi.ui.home_activity.eligible_couple

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.piramalswasthya.sakhi.databinding.BottomSheetEcFilterBinding
import org.piramalswasthya.sakhi.helpers.EcFilterType

class EcFilterBottomSheetFragment(
    private val currentFilter: EcFilterType,
    private val onFilterSelected: (EcFilterType) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetEcFilterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetEcFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val checkedId = when (currentFilter) {
            EcFilterType.NEWEST_FIRST   -> binding.rbNewest.id
            EcFilterType.OLDEST_FIRST   -> binding.rbOldest.id
            EcFilterType.AGE_WISE       -> binding.rbAge.id
            EcFilterType.SYNCING_FIRST  -> binding.rbSyncing.id
            EcFilterType.UNSYNCED_FIRST -> binding.rbUnsynced.id
        }
        binding.rgFilter.check(checkedId)

        binding.btnApply.setOnClickListener {
            val selected = when (binding.rgFilter.checkedRadioButtonId) {
                binding.rbOldest.id   -> EcFilterType.OLDEST_FIRST
                binding.rbAge.id      -> EcFilterType.AGE_WISE
                binding.rbSyncing.id  -> EcFilterType.SYNCING_FIRST
                binding.rbUnsynced.id -> EcFilterType.UNSYNCED_FIRST
                else                  -> EcFilterType.NEWEST_FIRST
            }
            onFilterSelected(selected)
            dismiss()
        }

        binding.btnCancel.setOnClickListener { dismiss() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
