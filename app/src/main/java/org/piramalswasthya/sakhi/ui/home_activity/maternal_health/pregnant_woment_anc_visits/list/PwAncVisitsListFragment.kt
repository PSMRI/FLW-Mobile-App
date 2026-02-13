package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pregnant_woment_anc_visits.list

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.AncVisitListAdapter
import org.piramalswasthya.sakhi.contracts.SpeechToTextContract
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.databinding.FragmentDisplaySearchRvButtonBinding
import org.piramalswasthya.sakhi.model.BenWithAncListDomain
import org.piramalswasthya.sakhi.ui.asha_supervisor.SupervisorActivity
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import javax.inject.Inject
import org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pmsma.PmsmaViewModel
import org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pmsma.list.PmsmaBottomSheetFragment
import org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pmsma.list.PmsmaVisitsListViewModel
import java.util.Collections.list

@AndroidEntryPoint
class PwAncVisitsListFragment : Fragment() {

    @Inject
    lateinit var prefDao: PreferenceDao

    private var _binding: FragmentDisplaySearchRvButtonBinding? = null
    private val binding: FragmentDisplaySearchRvButtonBinding
        get() = _binding!!

    val args: PwAncVisitsListFragmentArgs by lazy {
        PwAncVisitsListFragmentArgs.fromBundle(requireArguments())
    }

    private val viewModel: PwAncVisitsListViewModel by viewModels()
    private val viewModelListPmsma: PmsmaVisitsListViewModel by viewModels()

    private val bottomSheet: AncBottomSheetFragment by lazy { AncBottomSheetFragment() }
    private var bottomSheetPmsma: PmsmaBottomSheetFragment? = null

    private var  bottomSheetAncHomeVisit : AncHomeVisitBottomSheetFragment ? = null

    private var latestBenList: List<BenWithAncListDomain> = emptyList()

    private val sttContract = registerForActivityResult(SpeechToTextContract()) { value ->
        val lowerValue = value.lowercase()
        binding.searchView.setText(lowerValue)
        binding.searchView.setSelection(lowerValue.length)
        viewModel.filterText(lowerValue)
    }

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
        viewModel.toggleHighRisk(false)
        val benAdapter = AncVisitListAdapter(
            AncVisitListAdapter.PregnancyVisitClickListener(
                showVisits = {
                    viewModel.showAncBottomSheet(
                        it,
                        PwAncVisitsListViewModel.BottomSheetMode.NORMAL
                    )

                    val bottomSheet = AncBottomSheetFragment().apply {
                        arguments = Bundle().apply {
                            putString(
                                AncBottomSheetFragment.ARG_SOURCE,
                                AncBottomSheetFragment.SOURCE_ANC
                            )
                        }
                    }

                    bottomSheet.show(
                        childFragmentManager,
                        "ANC_BOTTOM_SHEET"
                    )
                },
                addVisit = { benId, hhId, visitNumber ->
                    findNavController().navigate(
                        PwAncVisitsListFragmentDirections.actionPwAncVisitsFragmentToPwAncFormFragment(
                            benId, hhId.toString(), visitNumber,false
                        )
                    )
                },
                pmsma = { benId, hhId, visitNumber ->
                    findNavController().navigate(
                        PwAncVisitsListFragmentDirections.actionPwAncVisitsFragmentToPmsmaFragment(
                            benId, hhId, visitNumber
                        )
                    )
                }, showPmsmaVisits = { benId, hhId ->
                    viewModel.showAncBottomSheet(
                        benId,
                        PwAncVisitsListViewModel.BottomSheetMode.PMSMA
                    )
                    if (!bottomSheet.isVisible) bottomSheet.show(
                        childFragmentManager,
                        "ANC_BOTTOM_SHEET"
                    )

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
                        Toast.makeText(
                            requireContext(),
                            "Please allow permissions first",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
              }, addHomeVisit = {benId ->
                    findNavController().navigate(
                        PwAncVisitsListFragmentDirections.actionPwAncVisitsFragmentToPwAncCounsellingFormFragment(
                            benId,0
                        )
                    )

                 },
                showHomeVisit = { benId ->
                    if (bottomSheetAncHomeVisit == null || !bottomSheetAncHomeVisit!!.isVisible) {
                        bottomSheetAncHomeVisit = AncHomeVisitBottomSheetFragment().apply {
                            arguments = Bundle().apply {
                                putLong("benId", benId)
                            }
                        }
                        bottomSheetAncHomeVisit!!.show(childFragmentManager, "HomeVisit")

                    }}
                ),true, prefDao,false,true


        )
        binding.rvAny.adapter = benAdapter
        lifecycleScope.launch {
            viewModel.benList.collect {list: List<BenWithAncListDomain> ->
                latestBenList = list

                binding.flEmpty.visibility =
                    if (list.isEmpty()) View.VISIBLE else View.GONE

                val benIds = list.map { it.ben.benId}
                viewModel.loadHomeVisitState(benIds)
               // benAdapter.submitList(list)
            }
        }

        viewModel.homeVisitState.observe(viewLifecycleOwner) { stateMap ->

            val updatedList = latestBenList.map { item ->
                val state = stateMap[item.ben.benId]

                item.copy(
                    showAddHomeVisit = state?.canAddHomeVisit ?: false,
                    showViewHomeVisit = state?.canViewHomeVisit ?: false
                )
            }

            benAdapter.submitList(updatedList)
        }

        binding.ibSearch.visibility = View.VISIBLE
        binding.ibSearch.setOnClickListener { sttContract.launch(Unit) }
        val searchTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                viewModel.filterText(p0?.toString() ?: "")
            }

        }
        binding.searchView.setOnFocusChangeListener { searchView, b ->
            if (b)
                (searchView as EditText).addTextChangedListener(searchTextWatcher)
            else
                (searchView as EditText).removeTextChangedListener(searchTextWatcher)

        }
    }


    override fun onStart() {
        super.onStart()
        activity?.let {
            if (prefDao.getLoggedInUser()?.role.equals("asha", true)) {
                (it as HomeActivity).updateActionBar(
                    R.drawable.ic__anc_visit,
                    getString(R.string.icon_title_pmt)
                )
            } else {
                (it as SupervisorActivity).updateActionBar(
                    R.drawable.ic__anc_visit,
                    getString(R.string.icon_title_pmt)
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}