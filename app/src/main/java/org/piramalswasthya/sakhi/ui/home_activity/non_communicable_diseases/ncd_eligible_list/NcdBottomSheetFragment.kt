package org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.ncd_eligible_list

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
import org.piramalswasthya.sakhi.adapters.NcdCbacAdapter
import org.piramalswasthya.sakhi.databinding.BottomSheetNcdBinding
import timber.log.Timber

@AndroidEntryPoint
class NcdBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetNcdBinding? = null
    private val binding: BottomSheetNcdBinding
        get() = _binding!!

    private val viewModel: NcdEligibleListViewModel by viewModels({ requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetNcdBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvImmCat.adapter = NcdCbacAdapter(NcdCbacAdapter.NcdCbacElementClickListener {
            val benId = viewModel.getSelectedBenId()
            findNavController().navigate(
                NcdEligibleListFragmentDirections.actionNcdEligibleListFragmentToCbacFragment(
                    benId = benId,
                    ashaId = viewModel.getAshaId(),
                    cbacId = it

                )
            )
            dismiss()
        })
        val divider = DividerItemDecoration(context, LinearLayout.VERTICAL)
        binding.rvImmCat.addItemDecoration(divider)


        lifecycleScope.launch {
            viewModel.ncdDetails.collect {
                Timber.d("List : $it")
                (_binding?.rvImmCat?.adapter as NcdCbacAdapter?)?.apply {
                    submitList(emptyList())
                    submitList(it)
                }
            }
        }
    }

}