package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pregnant_woment_anc_visits.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.R
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.adapters.AncVisitAdapter
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.databinding.BottomSheetAncBinding
import org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pmsma.pmsmaHighRiskList.PmsmaHighRiskListFragmentDirections
import javax.inject.Inject

@AndroidEntryPoint
class AncBottomSheetFragment : BottomSheetDialogFragment() {

    @Inject
    lateinit var prefDao: PreferenceDao

    private var _binding: BottomSheetAncBinding? = null
    private val binding: BottomSheetAncBinding
        get() = _binding!!

    private val viewModel: PwAncVisitsListViewModel by viewModels({ requireParentFragment() })

    companion object {
        const val ARG_SOURCE = "arg_source"
        const val SOURCE_ANC = "ANC"
        const val SOURCE_PMSMA = "PMSMA"
    }

    private fun getSource(): String {
        return arguments?.getString(ARG_SOURCE) ?: SOURCE_ANC
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetAncBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvAnc.adapter =
            AncVisitAdapter(AncVisitAdapter.AncVisitClickListener { benId, visitNumber, isLast ->
                val source = getSource()
                when (source) {

                    SOURCE_ANC -> {
                        findNavController().navigate(
                            PwAncVisitsListFragmentDirections.actionPwAncVisitsFragmentToPwAncFormFragment(
                                benId, null, visitNumber, isLast
                            )
                        )
                    }

                    SOURCE_PMSMA -> {
                        findNavController().navigate(
                            PmsmaHighRiskListFragmentDirections.actionPmsmaHighRiskListFragmentToPwAncFormFragment(
                                benId, null, visitNumber, isLast
                            )
                        )
                    }
                }



                this.dismiss()
            }, prefDao)

        val divider = DividerItemDecoration(context, LinearLayout.VERTICAL)
        binding.rvAnc.addItemDecoration(divider)
        observeList()
    }


    private fun observeList() {
        lifecycleScope.launch {
            viewModel.bottomSheetList.collect {
                (_binding?.rvAnc?.adapter as AncVisitAdapter?)?.submitList(it)
            }
        }
    }


}