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
import org.piramalswasthya.sakhi.model.BenRegCache

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
        alertBinding.btnOk.isEnabled = false

        alertBinding.rgGender.setOnCheckedChangeListener { _, checkedId ->
            alertBinding.btnOk.isEnabled = false
            Timber.d("RG Gender selected id : $checkedId")

            val selectedGender: Gender = genderFromRadioId(alertBinding, checkedId) ?: run {
                alertBinding.linearLayout4.visibility = View.GONE
                alertBinding.actvRth.setAdapter(null)
                return@setOnCheckedChangeListener
            }

            alertBinding.linearLayout4.visibility = View.VISIBLE
            alertBinding.actvRth.text = null
            val ctx = computeHofContext()
            val baseList = baseRelationDropdown(selectedGender)
            val filteredList = filterRelations(selectedGender, baseList, ctx)

            applyRelationAdapter(alertBinding, filteredList)
        }

        alertBinding.actvRth.setOnItemClickListener { _, _, i, _ ->
            Timber.d("item clicked index : $i")
            alertBinding.btnOk.isEnabled = true
        }

        val alert = MaterialAlertDialogBuilder(requireContext())
            .setView(alertBinding.root)
            .setOnCancelListener {
                viewModel.resetSelectedHouseholdId()
                alertBinding.rgGender.clearCheck()
                alertBinding.linearLayout4.visibility = View.GONE
                alertBinding.actvRth.text = null
                alertBinding.btnOk.isEnabled = false
            }
            .create()

        addBenAlert = alert

        alertBinding.btnOk.setOnClickListener {
            val relIndex = resources.getStringArray(R.array.nbr_relationship_to_head_src)
                .indexOf(alertBinding.actvRth.text.toString())

            val gender = genderIntFromRadioId(alertBinding)

            if (relIndex < 0 || gender == 0) {
                if (relIndex < 0) {
                    alertBinding.actvRth.error = resources.getString(R.string.relation_with_hof)
                }
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
                if (!it.isDeactivate && findNavController().currentDestination?.id == R.id.allHouseholdFragment){
                    findNavController().navigate(
                        AllHouseholdFragmentDirections.actionAllHouseholdFragmentToNewHouseholdFragment(
                            it.hhId
                        )
                    )
                }
            }
        }, {
            if (!it.isDeactivate && findNavController().currentDestination?.id == R.id.allHouseholdFragment){
                findNavController().navigate(
                    AllHouseholdFragmentDirections.actionAllHouseholdFragmentToHouseholdMembersFragment(
                        it.hhId,0,"No"
                    )
                )
            }

        }, {
            if (prefDao.getLoggedInUser()?.role.equals("asha", true) && findNavController().currentDestination?.id == R.id.allHouseholdFragment) {
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

    private fun genderFromRadioId(alertBinding: AlertNewBenBinding, checkedId: Int): Gender? {
        return when (checkedId) {
            alertBinding.rbMale.id -> Gender.MALE
            alertBinding.rbFemale.id -> Gender.FEMALE
            alertBinding.rbTrans.id -> Gender.TRANSGENDER
            else -> null
        }
    }

    private fun genderIntFromRadioId(alertBinding: AlertNewBenBinding): Int {
        return when (alertBinding.rgGender.checkedRadioButtonId) {
            alertBinding.rbMale.id -> 1
            alertBinding.rbFemale.id -> 2
            alertBinding.rbTrans.id -> 3
            else -> 0
        }
    }

    private fun baseRelationDropdown(selectedGender: Gender?): List<String> {
        val arr = when (selectedGender) {
            Gender.MALE -> resources.getStringArray(R.array.nbr_relationship_to_head_male)
            Gender.FEMALE -> resources.getStringArray(R.array.nbr_relationship_to_head_female)
            Gender.TRANSGENDER -> resources.getStringArray(R.array.nbr_relationship_to_head_male)
            else -> null
        }
        return arr?.toList().orEmpty()
    }

    private data class HofContext(
        val hof: BenRegCache?,
        val fatherRegistered: Boolean,
        val motherRegistered: Boolean,
        val unmarried: Boolean,
        val married: Boolean
    )

    private fun computeHofContext(): HofContext {
        val hof = viewModel.householdBenList.firstOrNull { it.familyHeadRelationPosition == 19 }
        val fatherRegistered = viewModel.householdBenList.any { it.familyHeadRelationPosition == 2 }
        val motherRegistered = viewModel.householdBenList.any { it.familyHeadRelationPosition == 1 }
        val unmarried = hof?.genDetails?.maritalStatusId == 1
        val married = hof?.genDetails?.maritalStatusId == 2

        return HofContext(hof, fatherRegistered, motherRegistered, unmarried, married)
    }

    private fun filterRelations(selectedGender: Gender?, baseList: List<String>, ctx: HofContext): List<String> {
        if (ctx.hof == null) return baseList

        val list = baseList.toMutableList()
        val common = resources.getStringArray(R.array.nbr_relationship_to_head)
        val unmarriedFilter =
            resources.getStringArray(R.array.nbr_relationship_to_head_unmarried_filter).toSet()

        if (ctx.fatherRegistered) list.remove(common[1])
        if (ctx.motherRegistered) list.remove(common[0])

        if (ctx.unmarried) {
            list.removeAll(unmarriedFilter)
        } else if (!ctx.married) {
            list.remove(common[5])
            list.remove(common[4])
        }
        val hofGender = ctx.hof.gender

        if (hofGender == Gender.MALE && selectedGender == Gender.MALE) {
            list.remove(common[5])
        }

        if (hofGender == Gender.FEMALE && selectedGender == Gender.FEMALE) {
            list.remove(common[4])
        }
        return list
    }

    private fun applyRelationAdapter(alertBinding: AlertNewBenBinding, items: List<String>) {
        alertBinding.actvRth.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, items)
        )
    }

}