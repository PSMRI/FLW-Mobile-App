package org.piramalswasthya.sakhi.ui.home_activity.eligible_couple.tracking.list

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
import org.piramalswasthya.sakhi.adapters.ECTrackingListAdapter
import org.piramalswasthya.sakhi.contracts.SpeechToTextContract
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.databinding.FragmentDisplaySearchRvButtonBinding
import org.piramalswasthya.sakhi.ui.asha_supervisor.SupervisorActivity
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pnc.list.PncMotherListFragmentArgs
import org.piramalswasthya.sakhi.utils.RoleConstants
import javax.inject.Inject

@AndroidEntryPoint
class EligibleCoupleTrackingListFragment : Fragment() {

    @Inject
    lateinit var prefDao: PreferenceDao

    val args: EligibleCoupleTrackingListFragmentArgs by lazy {
        EligibleCoupleTrackingListFragmentArgs.fromBundle(requireArguments())
    }

    private var _binding: FragmentDisplaySearchRvButtonBinding? = null
    private val binding: FragmentDisplaySearchRvButtonBinding
        get() = _binding!!

    private val viewModel: EligibleCoupleTrackingListViewModel by viewModels()

    private val bottomSheet by lazy {
        ECTrackingListBottomSheetFragment()
    }

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
        val benAdapter = ECTrackingListAdapter(
            ECTrackingListAdapter.ECTrackListClickListener(
                addNewTrack = { benId, canAdd ->
                        if (canAdd)
                            findNavController().navigate(
                                EligibleCoupleTrackingListFragmentDirections.actionEligibleCoupleTrackingListFragmentToEligibleCoupleTrackingFormFragment(
                                    benId
                                )
                            ) else
                            Toast.makeText(
                                requireContext(),
                                "Already filled for this Month!",
                                Toast.LENGTH_LONG
                            ).show()
            }, showAllTracks = {
                        viewModel.setClickedBenId(it)
                        bottomSheet.show(childFragmentManager, "ECT")
            })
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
            if (prefDao.getLoggedInUser()?.role.equals(RoleConstants.ROLE_ASHA_SUPERVISOR, true)) {
                (it as SupervisorActivity).updateActionBar(
                    R.drawable.ic__eligible_couple,
                    getString(R.string.eligible_couple_tracking_list)
                )
            } else {
                (it as HomeActivity).updateActionBar(
                    R.drawable.ic__eligible_couple,
                    getString(R.string.eligible_couple_tracking_list)
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}