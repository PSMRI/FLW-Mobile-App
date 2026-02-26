package org.piramalswasthya.sakhi.ui.home_activity.all_household

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.HouseHoldListAdapter
import org.piramalswasthya.sakhi.contracts.SpeechToTextContract
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.databinding.AlertNewBenBinding
import org.piramalswasthya.sakhi.databinding.FragmentDisplaySearchRvButtonBinding
import org.piramalswasthya.sakhi.model.Gender
import org.piramalswasthya.sakhi.model.HouseHoldBasicDomain
import org.piramalswasthya.sakhi.ui.asha_supervisor.SupervisorActivity
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import org.piramalswasthya.sakhi.utils.RoleConstants
import timber.log.Timber
import javax.inject.Inject


@AndroidEntryPoint
class AllHouseholdFragment : Fragment() {

    @Inject
    lateinit var prefDao: PreferenceDao

    private var _binding: FragmentDisplaySearchRvButtonBinding? = null

    private val binding: FragmentDisplaySearchRvButtonBinding
        get() = _binding!!

    private val viewModel: AllHouseholdViewModel by viewModels()


    private val sttContract = registerForActivityResult(SpeechToTextContract()) { value ->
        val lowerValue = value.lowercase()
        binding.searchView.setText(lowerValue)
        binding.searchView.setSelection(lowerValue.length)
        viewModel.filterText(lowerValue)
    }


    private var hasDraft = false

    private var isDisease = false

    private var addBenAlert: AlertDialog? = null
    private var addBenAlertBinding: AlertNewBenBinding? = null

    private val draftLoadAlert by lazy {
        MaterialAlertDialogBuilder(requireContext()).setTitle(resources.getString(R.string.incomplete_form_found))
            .setMessage(resources.getString(R.string.do_you_want_to_continue_with_previous_form_or_create_a_new_form_and_discard_the_previous_form))
            .setPositiveButton(resources.getString(R.string.open_draft)) { dialog, _ ->
                viewModel.navigateToNewHouseholdRegistration(false)
                dialog.dismiss()
            }.setNegativeButton(resources.getString(R.string.create_new)) { dialog, _ ->
                viewModel.navigateToNewHouseholdRegistration(true)
                dialog.dismiss()
            }.create()
    }

    fun showSoftDeleteDialog(houseHoldBasicDomain: HouseHoldBasicDomain) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Household")
            .setMessage("Are you sure you want to delete ${houseHoldBasicDomain.headFullName}")
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                viewModel.deActivateHouseHold(houseHoldBasicDomain)
            }
            .setNegativeButton(getString(R.string.no)) { d, _ -> d.dismiss() }
            .show()
    }

    private fun buildAddBenDialog() {
        val alertBinding = AlertNewBenBinding.inflate(layoutInflater, binding.root, false)
        addBenAlertBinding = alertBinding
        alertBinding.rgGender.setOnCheckedChangeListener { _, i ->
            alertBinding.btnOk.isEnabled = false
            Timber.d("RG Gender selected id : $i")
            val selectedGender = when (i) {
                alertBinding.rbMale.id -> Gender.MALE
                alertBinding.rbFemale.id -> Gender.FEMALE
                alertBinding.rbTrans.id -> Gender.TRANSGENDER
                else -> null
            }
            alertBinding.linearLayout4.visibility =
                selectedGender?.let { View.VISIBLE } ?: View.GONE
            alertBinding.actvRth.text = null

            val hof =
                viewModel.householdBenList.firstOrNull { it.familyHeadRelationPosition == 19 }
            val hofFatherRegistered =
                viewModel.householdBenList.any { it.familyHeadRelationPosition == 2 }
            val hofMotherRegistered =
                viewModel.householdBenList.any { it.familyHeadRelationPosition == 1 }
            val isHoFUnmarried =
                hof?.let {
                    (it.genDetails?.maritalStatusId == 1)
                } ?: false
            val isHoFMarried =
                hof?.let {
                    (it.genDetails?.maritalStatusId == 2)
                } ?: false
            val dropdownList = when (selectedGender) {
                Gender.MALE -> resources.getStringArray(R.array.nbr_relationship_to_head_male)
                Gender.FEMALE -> resources.getStringArray(R.array.nbr_relationship_to_head_female)
                Gender.TRANSGENDER -> resources.getStringArray(R.array.nbr_relationship_to_head_male)
                else -> null
            }
            val filteredDropdownList =
                dropdownList?.takeIf { hof != null }?.toMutableList()?.apply {
                    if (hofFatherRegistered)
                        remove(resources.getStringArray(R.array.nbr_relationship_to_head)[1])
                    if (hofMotherRegistered)
                        remove(resources.getStringArray(R.array.nbr_relationship_to_head)[0])
                    if (isHoFUnmarried)
                        removeAll(
                            resources.getStringArray(R.array.nbr_relationship_to_head_unmarried_filter)
                                .toSet()
                        )
                    else {
                        if (!isHoFMarried) {
                            remove(resources.getStringArray(R.array.nbr_relationship_to_head)[5])
                            remove(resources.getStringArray(R.array.nbr_relationship_to_head)[4])

                        }

                    }
                    if (hof?.gender == Gender.MALE && selectedGender == Gender.MALE)
                        remove(resources.getStringArray(R.array.nbr_relationship_to_head)[5])
                    else if (hof?.gender == Gender.FEMALE && selectedGender == Gender.FEMALE)
                        remove(resources.getStringArray(R.array.nbr_relationship_to_head)[4])
                } ?: dropdownList?.toList()
            filteredDropdownList?.let {
                alertBinding.actvRth.setAdapter(
                    ArrayAdapter(
                        requireContext(), android.R.layout.simple_spinner_dropdown_item,
                        it,
                    )
                )
            }
        }
        alertBinding.actvRth.setOnItemClickListener { _, _, i, _ ->
            Timber.d("item clicked index : $i")
            alertBinding.btnOk.isEnabled = true
        }


        val alert = MaterialAlertDialogBuilder(requireContext()).setView(alertBinding.root)
            .setOnCancelListener {
                viewModel.resetSelectedHouseholdId()
                alertBinding.rgGender.clearCheck()
                alertBinding.linearLayout4.visibility = View.GONE
                alertBinding.actvRth.text = null
            }.create()
        addBenAlert = alert

        alertBinding.btnOk.setOnClickListener {
            val relIndex = resources.getStringArray(R.array.nbr_relationship_to_head_src)
                .indexOf(alertBinding.actvRth.text.toString())
            val gender = when (alertBinding.rgGender.checkedRadioButtonId) {
                alertBinding.rbMale.id -> 1
                alertBinding.rbFemale.id -> 2
                alertBinding.rbTrans.id -> 3
                else -> 0
            }
            if (relIndex < 0 || gender == 0) {
                if (relIndex < 0) alertBinding.actvRth.error = resources.getString(R.string.relation_with_hof)
                return@setOnClickListener
            }
            findNavController().navigate(
                AllHouseholdFragmentDirections.actionAllHouseholdFragmentToNewBenRegFragment(
                    hhId = viewModel.selectedHouseholdId,
                    relToHeadId = relIndex,
                    gender = gender
                )
            )
            viewModel.resetSelectedHouseholdId()
            alert.cancel()
        }
        alertBinding.btnCancel.setOnClickListener {
            alert.cancel()
            viewModel.resetSelectedHouseholdId()
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDisplaySearchRvButtonBinding.inflate(layoutInflater, container, false)
        viewModel.checkDraft()
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            if (prefDao.getLoggedInUser()?.role.equals(RoleConstants.ROLE_ASHA_SUPERVISOR, true)) {
                (it as SupervisorActivity).updateActionBar(
                    R.drawable.ic__hh,
                    getString(R.string.icon_title_household)
                )
            } else {
                (it as HomeActivity).updateActionBar(
                    R.drawable.ic__hh,
                    getString(R.string.icon_title_household)
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buildAddBenDialog()
        binding.btnNextPage.text = resources.getString(R.string.btn_text_frag_home_nhhr)
        if (prefDao.getLoggedInUser()?.role.equals(RoleConstants.ROLE_ASHA_SUPERVISOR, true)) {
            binding.btnNextPage.visibility = View.GONE
        } else {
            binding.btnNextPage.visibility = View.VISIBLE
        }
//        binding.tvEmptyContent.text = resources.getString(R.string.no_records_found_hh)
        val householdAdapter = HouseHoldListAdapter("",isDisease, prefDao,true, HouseHoldListAdapter.HouseholdClickListener({
            if (prefDao.getLoggedInUser()?.role.equals("asha", true)) {
                if (!it.isDeactivate){
                    findNavController().navigate(
                        AllHouseholdFragmentDirections.actionAllHouseholdFragmentToNewHouseholdFragment(
                            it.hhId
                        )
                    )
                }
            }
        }, {
            if (!it.isDeactivate){
                findNavController().navigate(
                    AllHouseholdFragmentDirections.actionAllHouseholdFragmentToHouseholdMembersFragment(
                        it.hhId,0,"No"
                    )
                )
            }

        }, {
            if (prefDao.getLoggedInUser()?.role.equals("asha", true)) {
                if (it.numMembers == 0 && !it.isDeactivate) {
                        findNavController().navigate(
                            AllHouseholdFragmentDirections.actionAllHouseholdFragmentToNewBenRegFragment(
                                it.hhId,
                                18
                            )
                        )
                } else {
                  if(!it.isDeactivate) {
                      viewModel.setSelectedHouseholdId(it.hhId)
                      addBenAlert?.show()
                  }
                }
            }

        },
        {


        }, {
            showSoftDeleteDialog(it)
        }
            ))
        binding.rvAny.adapter = householdAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.householdList.collect {
                    if (it.isEmpty()) binding.flEmpty.visibility = View.VISIBLE
                    else binding.flEmpty.visibility = View.GONE
                    householdAdapter.submitList(it)
                }
            }
        }


        viewModel.hasDraft.observe(viewLifecycleOwner) {
            hasDraft = it
        }
        viewModel.navigateToNewHouseholdRegistration.observe(viewLifecycleOwner) {
            if (it) {
                findNavController().navigate(AllHouseholdFragmentDirections.actionAllHouseholdFragmentToNewHouseholdFragment())
                viewModel.navigateToNewHouseholdRegistrationCompleted()
            }
        }

        binding.btnNextPage.setOnClickListener {
            if (hasDraft) draftLoadAlert.show()
            else viewModel.navigateToNewHouseholdRegistration(false)
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
            if (b) (searchView as EditText).addTextChangedListener(searchTextWatcher)
            else (searchView as EditText).removeTextChangedListener(searchTextWatcher)

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        addBenAlert?.dismiss()
        addBenAlert = null
        addBenAlertBinding = null
        _binding = null
    }

}