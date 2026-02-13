package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pmsma.pmsmaHighRiskList

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.AncVisitListAdapter
import org.piramalswasthya.sakhi.databinding.FragmentDisplaySearchRvButtonBinding
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pmsma.list.PmsmaBottomSheetFragment
import org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pmsma.list.PmsmaVisitsListViewModel
import org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pregnant_woment_anc_visits.list.AncBottomSheetFragment
import org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pregnant_woment_anc_visits.list.PwAncVisitsListViewModel

@AndroidEntryPoint
class PmsmaHighRiskListFragment : Fragment() {

    private var _binding: FragmentDisplaySearchRvButtonBinding? = null
    private val binding: FragmentDisplaySearchRvButtonBinding
        get() = _binding!!

    private val viewModel: PwAncVisitsListViewModel by viewModels()
    private val viewModelListPmsma: PmsmaVisitsListViewModel by viewModels()

    private val bottomSheet: AncBottomSheetFragment by lazy { AncBottomSheetFragment() }
    private var bottomSheetPmsma: PmsmaBottomSheetFragment? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDisplaySearchRvButtonBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnNextPage.visibility = View.GONE

        // Toggle high-risk mode ON
        viewModel.toggleHighRisk(true)

        val benAdapter = AncVisitListAdapter(
            AncVisitListAdapter.PregnancyVisitClickListener(
                showVisits = {
                    viewModel.updateBottomSheetData(it)
                    if (!bottomSheet.isVisible)
                        bottomSheet.show(childFragmentManager, "ANC")
                },
                addVisit = { benId,hhId, visitNumber ->
                    findNavController().navigate(
                        PmsmaHighRiskListFragmentDirections.actionPmsmaHighRiskListFragmentToPwAncFormFragment(
                            benId,hhId.toString(), visitNumber
                        )
                    )
                },
                pmsma = { benId, hhId, visitNumber ->
                    findNavController().navigate(
                        PmsmaHighRiskListFragmentDirections.actionPmsmaHighRiskListFragmentToPmsmaFragment(
//                        PwAncVisitsListFragmentDirections.actionPwAncVisitsFragmentToPmsmaFragment(
                            benId, hhId, visitNumber
                        )
                    )
                },
                showPmsmaVisits = { benId, hhId ->
                    viewModelListPmsma.updateBottomSheetData(benId)
                    if (bottomSheetPmsma == null || !bottomSheetPmsma!!.isVisible) {
                        bottomSheetPmsma = PmsmaBottomSheetFragment().apply {
                            arguments = Bundle().apply {
                                putLong("hhId", hhId)
                                putBoolean("fromHighRisk", true)
                            }
                        }
                        bottomSheetPmsma!!.show(childFragmentManager, "PMSMA")
                    }
                }, callBen = {
                    try {
                        val callIntent = Intent(Intent.ACTION_CALL)
                        callIntent.setData(Uri.parse("tel:${it.ben.mobileNo}"))
                        startActivity(callIntent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        activity?.let {
                            (it as HomeActivity).askForPermissions()
                        }
                        Toast.makeText(requireContext(), "Please allow permissions first", Toast.LENGTH_SHORT).show()
                    }
                }
            ),
            isHighRiskMode = true

        )


        binding.rvAny.adapter = benAdapter

        lifecycleScope.launch {
            viewModel.benList.collect {
                binding.flEmpty.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
                benAdapter.submitList(it)
            }
        }

        val searchTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {
                viewModel.filterText(p0?.toString() ?: "")
            }
        }
        binding.searchView.setOnFocusChangeListener { searchView, hasFocus ->
            if (hasFocus) (searchView as EditText).addTextChangedListener(searchTextWatcher)
            else (searchView as EditText).removeTextChangedListener(searchTextWatcher)
        }
    }

    override fun onStart() {
        super.onStart()
        (activity as? HomeActivity)?.updateActionBar(
            R.drawable.ic__anc_visit,
            getString(R.string.icon_title_pmsma)
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
