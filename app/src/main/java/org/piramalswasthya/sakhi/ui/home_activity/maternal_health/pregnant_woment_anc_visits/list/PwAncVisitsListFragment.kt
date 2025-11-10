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
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.AncVisitListAdapter
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.databinding.FragmentDisplaySearchRvButtonBinding
import org.piramalswasthya.sakhi.ui.asha_supervisor.SupervisorActivity
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import javax.inject.Inject
import org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pmsma.PmsmaViewModel
import org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pmsma.list.PmsmaBottomSheetFragment
import org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pmsma.list.PmsmaVisitsListViewModel

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
    private var bottomSheetPmsma: PmsmaBottomSheetFragment ?=null

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
                viewModel.updateBottomSheetData(it)
                if (!bottomSheet.isVisible)
                    bottomSheet.show(childFragmentManager, "ANC")
            },
                addVisit = { benId,hhId, visitNumber ->
                    findNavController().navigate(
                        PwAncVisitsListFragmentDirections.actionPwAncVisitsFragmentToPwAncFormFragment(
                            benId, hhId.toString(),visitNumber
                        )
                    )
                },
                pmsma = { benId, hhId ,visitNumber->
                    findNavController().navigate(
                        PwAncVisitsListFragmentDirections.actionPwAncVisitsFragmentToPmsmaFragment(
                            benId, hhId,visitNumber
                        )
                    )
                }, showPmsmaVisits = { benId, hhId ->
                    viewModelListPmsma.updateBottomSheetData(benId)

                    if (bottomSheetPmsma == null || !bottomSheetPmsma!!.isVisible) {
                        bottomSheetPmsma = PmsmaBottomSheetFragment().apply {
                            arguments = Bundle().apply {
                                putLong("hhId", hhId)
                                putBoolean("fromHighRisk", false)
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
                }),true, prefDao
        )
        binding.rvAny.adapter = benAdapter
        lifecycleScope.launch {
            viewModel.benList.collect {
                if (it.isEmpty())
                    binding.flEmpty.visibility = View.VISIBLE
                else
                    binding.flEmpty.visibility = View.GONE
                benAdapter.submitList(it)
            }
        }
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