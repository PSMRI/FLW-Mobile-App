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
import org.piramalswasthya.sakhi.contracts.SpeechToTextContract
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.databinding.FragmentDisplaySearchRvButtonBinding
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pmsma.list.PmsmaBottomSheetFragment
import org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pmsma.list.PmsmaVisitsListViewModel
import org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pregnant_woment_anc_visits.list.AncBottomSheetFragment
import org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pregnant_woment_anc_visits.list.PwAncVisitsListViewModel
import javax.inject.Inject

@AndroidEntryPoint
class PmsmaHighRiskListFragment : Fragment() {
    @Inject
    lateinit var prefDao: PreferenceDao

    private var _binding: FragmentDisplaySearchRvButtonBinding? = null
    private val binding: FragmentDisplaySearchRvButtonBinding
        get() = _binding!!

    private val viewModel: PwAncVisitsListViewModel by viewModels()

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
        viewModel.toggleHighRisk(true)

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
                                AncBottomSheetFragment.SOURCE_PMSMA
                            )
                        }
                    }

                    bottomSheet.show(
                        childFragmentManager,
                        "ANC_BOTTOM_SHEET"
                    )

                },
                addVisit = { benId,hhId, visitNumber ->
                    findNavController().navigate(
                        PmsmaHighRiskListFragmentDirections.actionPmsmaHighRiskListFragmentToPwAncFormFragment(
                            benId,hhId.toString(), visitNumber,true
                        )
                    )
                },
                pmsma = { benId, hhId, visitNumber ->
//                    findNavController().navigate(
//                        PmsmaHighRiskListFragmentDirections.actionPmsmaHighRiskListFragmentToPmsmaFragment(
////                        PwAncVisitsListFragmentDirections.actionPwAncVisitsFragmentToPmsmaFragment(
//                            benId, hhId, visitNumber
//                        )
//                    )

                    findNavController().navigate(
                        PmsmaHighRiskListFragmentDirections.actionPmsmaHighRiskListFragmentToPwAncFormFragment(
                            benId,hhId.toString(), visitNumber
                        )
                    )
                },
                showPmsmaVisits = { benId, hhId ->
                    viewModel.showAncBottomSheet(
                        benId,
                        PwAncVisitsListViewModel.BottomSheetMode.PMSMA
                    )
                    val bottomSheet = AncBottomSheetFragment().apply {
                        arguments = Bundle().apply {
                            putString(
                                AncBottomSheetFragment.ARG_SOURCE,
                                AncBottomSheetFragment.SOURCE_PMSMA
                            )
                        }
                    }

                    bottomSheet.show(
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
                        Toast.makeText(requireContext(), "Please allow permissions first", Toast.LENGTH_SHORT).show()
                    }
                }
            ),
            true, prefDao ,true,
            hidePmsma=false

        )


        binding.rvAny.adapter = benAdapter

        lifecycleScope.launch {
            viewModel.benList.collect {
                binding.flEmpty.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
                benAdapter.submitList(it)
            }
        }

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
