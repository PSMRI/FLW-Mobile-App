package org.piramalswasthya.sakhi.ui.home_activity.immunization_due.mother_immunization.list

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.adapters.ImmunizationBenListAdapter
import org.piramalswasthya.sakhi.databinding.FragmentChildImmunizationListBinding
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import timber.log.Timber

@AndroidEntryPoint
class MotherImmunizationListFragment : Fragment() {

    private var _binding: FragmentChildImmunizationListBinding? = null
    private val binding: FragmentChildImmunizationListBinding
        get() = _binding!!


    private val viewModel: MotherImmunizationListViewModel by viewModels()

    private val bottomSheet: MotherImmunizationVaccineBottomSheetFragment by lazy { MotherImmunizationVaccineBottomSheetFragment() }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChildImmunizationListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvList.adapter =
            ImmunizationBenListAdapter(
                ImmunizationBenListAdapter.VaccinesClickListener(clickedVaccine = {
                    viewModel.updateBottomSheetData(it)
                    if (!bottomSheet.isVisible)
                        bottomSheet.show(childFragmentManager, "ImM")
                }, {})
            )

        lifecycleScope.launch {
            viewModel.benWithVaccineDetails.collect {
                Timber.d("Collecting list : $it")
                binding.rvList.apply {
                    (adapter as ImmunizationBenListAdapter).submitList(it)
                }
            }
        }
    }

}