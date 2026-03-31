package org.piramalswasthya.sakhi.ui.home_activity.all_household.household_members

import androidx.appcompat.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.BenListAdapter
import org.piramalswasthya.sakhi.configuration.IconDataset
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.databinding.AlertNewBenBinding
import org.piramalswasthya.sakhi.databinding.FragmentHouseholdMembersBinding
import org.piramalswasthya.sakhi.model.BenBasicDomain
import org.piramalswasthya.sakhi.model.Gender
import org.piramalswasthya.sakhi.ui.abha_id_activity.AbhaIdActivity
import org.piramalswasthya.sakhi.ui.asha_supervisor.SupervisorActivity
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import org.piramalswasthya.sakhi.utils.RoleConstants
import org.piramalswasthya.sakhi.utils.HelperUtil
import javax.inject.Inject
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import org.piramalswasthya.sakhi.ui.home_activity.all_ben.eye_surgery_registration.eyeBottomsheet.EyeSurgeryBottomSheetFragment

@AndroidEntryPoint
class HouseholdMembersFragment : Fragment() {

    @Inject
    lateinit var prefDao: PreferenceDao

    private var _binding: FragmentHouseholdMembersBinding? = null
    private val binding: FragmentHouseholdMembersBinding
        get() = _binding!!

    private val viewModel: HouseholdMembersViewModel by viewModels()

    private var householdMembers: List<BenBasicDomain> = emptyList()

    var showAbha = false
    private val abhaDisclaimer by lazy {
        AlertDialog.Builder(requireContext())
            .setTitle(resources.getString(R.string.beneficiary_abha_number))
            .setMessage("")
            .setPositiveButton(resources.getString(R.string.ok)) { dialog, _ -> dialog.dismiss() }
            .create()
    }

    private var addBenAlert: AlertDialog? = null

    private fun buildAddBenDialog() {
        val alertBinding = AlertNewBenBinding.inflate(layoutInflater, binding.root, false)
        alertBinding.btnOk.isEnabled = false

        setupGenderListener(alertBinding)
        setupRelationClickListener(alertBinding)

        val alert = createAddBenDialog(alertBinding)
        addBenAlert = alert

        setupButtonListeners(alertBinding, alert)
    }
    private fun setupGenderListener(binding: AlertNewBenBinding) {
        binding.btnOk.isEnabled = false

        binding.rgGender.setOnCheckedChangeListener { _, checkedId ->
            binding.btnOk.isEnabled = false

            val selectedGender = genderFromCheckedId(binding, checkedId)
            if (selectedGender == null) {
                binding.linearLayout4.visibility = View.GONE
                binding.actvRth.setAdapter(null)
                binding.actvRth.text = null
                return@setOnCheckedChangeListener
            }

            binding.linearLayout4.visibility = View.VISIBLE
            binding.actvRth.text = null

            val relations = getFilteredRelations(selectedGender)
            binding.actvRth.setAdapter(
                ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, relations)
            )
        }
    }
    private fun getFilteredRelations(selectedGender: Gender): List<String> {
        val list = baseRelations(selectedGender)

        val hof = householdMembers.firstOrNull { it.relToHeadId == 19 }
            ?: return list

        applyParentFilters(list)
        val isUnmarried = !hof.isMarried
        val isMarried = hof.isMarried


        if (isUnmarried) {
            applyUnmarriedFilters(list)
        } else if (!isMarried) {
            val relations = resources.getStringArray(R.array.nbr_relationship_to_head)
            list.remove(relations[5])
            list.remove(relations[4])
        }

        applySameGenderSpouseFilter(list, hof, selectedGender)

        return list
    }

    private fun setupRelationClickListener(binding: AlertNewBenBinding) {
        binding.actvRth.setOnItemClickListener { _, _, _, _ ->
            binding.btnOk.isEnabled = true
        }
    }
    private fun createAddBenDialog(binding: AlertNewBenBinding): AlertDialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .setOnCancelListener {
                binding.btnOk.isEnabled = false
                binding.rgGender.clearCheck()
                binding.linearLayout4.visibility = View.GONE
                binding.actvRth.text = null
                binding.actvRth.setAdapter(null)
            }
            .create()
    }
    private fun setupButtonListeners(
        binding: AlertNewBenBinding,
        alert: AlertDialog
    ) {

        binding.btnOk.setOnClickListener {

            val relIndex = resources
                .getStringArray(R.array.nbr_relationship_to_head_src)
                .indexOf(binding.actvRth.text.toString())

            val gender = genderIntFromRadio(binding)

            if (relIndex < 0 || gender == 0) {
                if (relIndex < 0) {
                    binding.actvRth.error =
                        resources.getString(R.string.relation_with_hof)
                }
                return@setOnClickListener
            }

            findNavController().navigate(
                HouseholdMembersFragmentDirections
                    .actionHouseholdMembersFragmentToNewBenRegFragment(
                        hhId = hhId,
                        relToHeadId = relIndex,
                        gender = gender
                    )
            )

            alert.cancel()
        }

        binding.btnCancel.setOnClickListener {
            alert.cancel()
        }
    }
    private fun genderFromCheckedId(binding: AlertNewBenBinding, checkedId: Int): Gender? {
        return when (checkedId) {
            binding.rbMale.id -> Gender.MALE
            binding.rbFemale.id -> Gender.FEMALE
            binding.rbTrans.id -> Gender.TRANSGENDER
            else -> null
        }
    }

    private fun genderIntFromRadio(binding: AlertNewBenBinding): Int {
        return when (binding.rgGender.checkedRadioButtonId) {
            binding.rbMale.id -> 1
            binding.rbFemale.id -> 2
            binding.rbTrans.id -> 3
            else -> 0
        }
    }

    private fun baseRelations(selectedGender: Gender): MutableList<String> {
        val arr = when (selectedGender) {
            Gender.MALE -> resources.getStringArray(R.array.nbr_relationship_to_head_male)
            Gender.FEMALE -> resources.getStringArray(R.array.nbr_relationship_to_head_female)
            Gender.TRANSGENDER -> resources.getStringArray(R.array.nbr_relationship_to_head_male)
        }
        return arr.toMutableList()
    }

    private fun applyParentFilters(list: MutableList<String>) {
        val relations = resources.getStringArray(R.array.nbr_relationship_to_head)
        val fatherExists = householdMembers.any { it.relToHeadId == 2 }
        val motherExists = householdMembers.any { it.relToHeadId == 1 }

        if (fatherExists) list.remove(relations[1])
        if (motherExists) list.remove(relations[0])
    }

    private fun applyUnmarriedFilters(list: MutableList<String>) {
        val filters = resources.getStringArray(R.array.nbr_relationship_to_head_unmarried_filter).toSet()
        list.removeAll(filters)
    }

    private fun applySameGenderSpouseFilter(list: MutableList<String>, hof: BenBasicDomain, selectedGender: Gender) {
        val relations = resources.getStringArray(R.array.nbr_relationship_to_head)
        if (hof.gender == Gender.MALE.name && selectedGender == Gender.MALE) list.remove(relations[5])
        if (hof.gender == Gender.FEMALE.name && selectedGender == Gender.FEMALE) list.remove(relations[4])
    }


    fun showSoftDeleteDialog(benBasicDomain: BenBasicDomain) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Beneficiary")
            .setMessage("Are you sure you want to delete ${benBasicDomain.benFullName}")
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                viewModel.deActivateBeneficiary(benBasicDomain)
            }
            .setNegativeButton(getString(R.string.no)) { d, _ -> d.dismiss() }
            .show()
    }

    private val hhId by lazy {
        HouseholdMembersFragmentArgs.fromBundle(requireArguments()).hhId
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHouseholdMembersBinding.inflate(inflater, container, false)
        return binding.root
    }
    private fun isAsha(): Boolean =
        prefDao.getLoggedInUser()?.role.equals(RoleConstants.ROLE_ASHA, true)
    private fun canProceed(item: BenBasicDomain): Boolean =
        isAsha() && !item.isDeactivate
    private inline fun routeBenFlow(
        item: BenBasicDomain,
        benId: Long,
        normalFlow: () -> Unit
    ){
        if (!canProceed(item)) return
        if (viewModel.isFromDisease == 0) normalFlow() else navigateToDiseaseForm(benId)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buildAddBenDialog()
        binding.btnNextPage.visibility = View.GONE
        binding.llSearch.visibility = View.GONE
        if (viewModel.isFromDisease == 1 && viewModel.diseaseType == IconDataset.Disease.MALARIA.toString()) {
            binding.switchButton.visibility = View.VISIBLE
            showAbha = false
        } else if(viewModel.isFromDisease == 1) {
            binding.switchButton.visibility = View.GONE
            showAbha = false
        } else {
            binding.switchButton.visibility = View.GONE
            showAbha = true
        }
        binding.switchButton.text = if (binding.switchButton.isChecked) "ON" else "OFF"
        binding.switchButton.setOnCheckedChangeListener { _, isChecked ->
            binding.switchButton.text = if (isChecked) "ON" else "OFF"
        }

        binding.fabAddMember.visibility = if (prefDao.getLoggedInUser()?.role.equals(RoleConstants.ROLE_ASHA, true) && viewModel.isFromDisease == 0) {
            View.GONE
        } else {
            View.GONE
        }

        binding.fabAddMember.setOnClickListener {
            addBenAlert?.show()
        }

        val benAdapter = BenListAdapter(
            clickListener = BenListAdapter.BenClickListener(
                { item, hhId, benId, relToHeadId ->
                    routeBenFlow(item, benId) {
                        findNavController().navigate(
                            HouseholdMembersFragmentDirections.actionHouseholdMembersFragmentToNewBenRegFragment(
                                hhId = hhId,
                                benId = benId,
                                gender = 0,
                                isAddSpouse = 0,
                                relToHeadId = relToHeadId
                            )
                        )
                    }
                },
                clickedWifeBen = { item, hhId, benId, relToHeadId ->
                    if (canProceed(item)) {
                        findNavController().navigate(
                            HouseholdMembersFragmentDirections.actionHouseholdMembersFragmentToNewBenRegFragment(
                                hhId = hhId,
                                benId = 0,
                                gender = 2,
                                selectedBenId = benId,
                                isAddSpouse = 1,
                                relToHeadId = HelperUtil.getFemaleRelationId(relToHeadId)
                            )
                        )
                    }

                },clickedHusbandBen = { item, hhId, benId, relToHeadId ->
                    if (canProceed(item)) {
                        findNavController().navigate(
                            HouseholdMembersFragmentDirections.actionHouseholdMembersFragmentToNewBenRegFragment(
                                hhId = hhId,
                                benId = 0,
                                gender = 1,
                                selectedBenId = benId,
                                isAddSpouse = 1,
                                relToHeadId = HelperUtil.getMaleRelationId(relToHeadId)
                            )
                        )
                    }
                },
                clickedChildben = { item, hhId, benId, relToHeadId ->
                    if (canProceed(item)) {
                        findNavController().navigate(
                            HouseholdMembersFragmentDirections.actionHouseholdMembersFragmentToNewChildAsBenRegistrationFragment(
                                hhId = hhId,
                                benId = 0,
                                gender = 0,
                                selectedBenId = benId,
                                isAddSpouse = 0,
                                relToHeadId = HelperUtil.getFemaleRelationId(relToHeadId)
                            )
                        )
                    }
                },
                { item, hhid->

                },
                { item, benId, hhId ->
                    if (canProceed(item)) checkAndGenerateABHA(benId)
                },
                { item,benId, hhId, isViewMode, isIFA ->
                    if (!isIFA && !item.isDeactivate) {
                        showEyeSurgeryBottomSheet(benId, hhId, item.benFullName, item.gender, item.age)
                    }
                    if (canProceed(item)) {
                        findNavController().navigate(
                            HouseholdMembersFragmentDirections.actionHouseholdMembersFragmentToEyeSurgeryFormFragment(
                                hhId = hhId,
                                benId = benId,
                                isViewMode = isViewMode,
                            )
                        )
                    }
                },
                {
                    if(!it.isDeactivate){
                        try {
                            val callIntent = Intent(Intent.ACTION_CALL)
                            callIntent.setData(Uri.parse("tel:${it.mobileNo}"))
                            startActivity(callIntent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            activity?.let {
                                (it as HomeActivity).askForPermissions()
                            }
                            Toast.makeText(requireContext(), "Please allow permissions first", Toast.LENGTH_SHORT).show()
                        }
                    }

                },
                { it ->

                    viewLifecycleOwner.lifecycleScope.launch {
                        val isHof = viewModel.isHOF(it)
                        if (isHof) {
                            if (viewModel.canDeleteHoF(it.hhId)) {
                                showSoftDeleteDialog(it)
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "Head of Family cannot be deleted when other members exist.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            showSoftDeleteDialog(it)
                        }
                    }
                }
            ),
            showRegistrationDate = true,
            showSyncIcon = true,
            showAbha = showAbha,
            showCall = true,
            pref = prefDao,
            context = requireActivity(),
            isSoftDeleteEnabled = true
        )
        binding.rvAny.adapter = benAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.benList.collect {
                        householdMembers = it
                    }
                }

                launch {
                    viewModel.benListWithChildren.collect {
                        if (it.isEmpty()) {
                            binding.flEmpty.visibility = View.VISIBLE
                        } else {
                            binding.flEmpty.visibility = View.GONE
                        }
                        benAdapter.submitList(it)
                    }
                }

            }
        }


        viewModel.abha.observe(viewLifecycleOwner) {
            it.let {
                if (it != null) {
                    abhaDisclaimer.setMessage(it)
                    abhaDisclaimer.show()
                }
            }
        }

        viewModel.benRegId.observe(viewLifecycleOwner) {
            if (it != null) {
                val intent = Intent(requireActivity(), AbhaIdActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                intent.putExtra("benId", viewModel.benId.value)
                intent.putExtra("benRegId", it)
                requireActivity().startActivity(intent)
                viewModel.resetBenRegId()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            if (prefDao.getLoggedInUser()?.role.equals(RoleConstants.ROLE_ASHA_SUPERVISOR, true)) {
                (it as SupervisorActivity).updateActionBar(
                    R.drawable.ic__hh,
                    getString(R.string.household_members)
                )
            } else {
                (it as HomeActivity).updateActionBar(
                    R.drawable.ic__hh,
                    getString(R.string.household_members)
                )
            }
        }
    }

    private fun checkAndGenerateABHA(benId: Long) {
        viewModel.fetchAbha(benId)
    }
    private fun navigateToDiseaseForm(benId: Long) {
        when (viewModel.diseaseType) {
            IconDataset.Disease.MALARIA.toString() -> {
                findNavController().navigate(
                    HouseholdMembersFragmentDirections
                        .actionHouseholdMembersFragmentToMalariaFormFragment(
                            benId = benId
                        )
                )
            }

            IconDataset.Disease.KALA_AZAR.toString() -> {
                findNavController().navigate(
                    HouseholdMembersFragmentDirections
                        .actionHouseholdMembersFragmentToKalaAzarFormFragment(
                            benId = benId
                        )
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        addBenAlert?.dismiss()
        addBenAlert = null
        _binding = null
    }
    private fun showEyeSurgeryBottomSheet(benId: Long, hhId: Long, benName: String, gender: String, age: String) {
        val bottomSheet = EyeSurgeryBottomSheetFragment.newInstance(benId, hhId, benName, gender, age)
        bottomSheet.setNavigationCallback(object : EyeSurgeryBottomSheetFragment.NavigationCallback {
            override fun navigateToEyeSurgeryForm(
                benId: Long, hhId: Long, eyeSide: String, isViewMode: Boolean,
                formDataJson: String?, recordId: Int, benName: String, gender: String, age: String
            ) {
                findNavController().navigate(
                    HouseholdMembersFragmentDirections.actionHouseholdMembersFragmentToEyeSurgeryFormFragment(
                        benId = benId, hhId = hhId, eyeSide = eyeSide,
                        isViewMode = isViewMode, formDataJson = formDataJson,
                        recordId = recordId, benName = benName, gender = gender, age = age
                    )
                )
            }
        })
        bottomSheet.show(childFragmentManager, "EyeSurgeryBottomSheet")
    }
}
