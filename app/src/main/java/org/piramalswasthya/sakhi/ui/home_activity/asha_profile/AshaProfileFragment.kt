package org.piramalswasthya.sakhi.ui.home_activity.asha_profile

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.BuildConfig
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.FormInputAdapter
import org.piramalswasthya.sakhi.adapters.HouseHoldListAdapter
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.databinding.AlertNewBenBinding
import org.piramalswasthya.sakhi.databinding.FragmentAshaProfileBinding
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.model.Gender
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import org.piramalswasthya.sakhi.ui.home_activity.all_household.AllHouseholdFragmentDirections
import timber.log.Timber
import java.io.File
import javax.inject.Inject


@AndroidEntryPoint
class AshaProfileFragment : Fragment() {

    private var _binding: FragmentAshaProfileBinding? = null
    private val binding: FragmentAshaProfileBinding
        get() = _binding!!
    private val viewModel: AshaProfileViewModel by viewModels()

    private var latestTmpUri: Uri? = null

    @Inject
    lateinit var prefDao: PreferenceDao


    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            if (success) {
                latestTmpUri?.let { uri ->
                    viewModel.setImageUriToFormElement(uri)

                    binding.form.rvInputForm.apply {
                        val adapter = this.adapter as FormInputAdapter
                        adapter.notifyItemChanged(0)
                    }
                    Timber.d("Image saved at @ $uri")
                }
            }
        }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAshaProfileBinding.inflate(layoutInflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.pbForm.visibility = View.VISIBLE
        viewModel.recordExists.observe(viewLifecycleOwner) { notIt ->

            notIt?.let { recordExists ->
                binding.fabEdit.visibility = if (recordExists) View.VISIBLE else View.GONE
                binding.btnSubmit.visibility = if (recordExists) View.GONE else View.VISIBLE
                lifecycleScope.launch {
                    viewModel.householdList.collect { list ->
                        binding.addHousehold.visibility =
                            if (recordExists && list.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
                binding.rvAny.visibility = if (recordExists) View.VISIBLE else View.GONE
                val adapter = FormInputAdapter(
                    formValueListener = FormInputAdapter.FormValueListener { formId, index ->
                        viewModel.updateListOnValueChanged(formId, index)
                        hardCodedListUpdate(formId)
                    },
                    imageClickListener = FormInputAdapter.ImageClickListener {
                        viewModel.setCurrentImageFormId(it)
                        takeImage()
                    },
                     isEnabled = !recordExists,

                )
                binding.form.rvInputForm.adapter = adapter
                lifecycleScope.launch {
                    viewModel.formList.collect {
                        if (it.isNotEmpty())
                            adapter.submitList(it)
                        binding.llContent.visibility = View.VISIBLE
                        binding.pbForm.visibility = View.GONE
                        binding.textError.visibility = View.GONE

                    }
                }
            }
        }

        binding.addHousehold.setOnClickListener {
            findNavController().navigate(
                AshaProfileFragmentDirections.actionAshaProfileFragmentToNewHouseholdFragment(

                )
            )
        }

        val householdAdapter = HouseHoldListAdapter("",false, prefDao, HouseHoldListAdapter.HouseholdClickListener({
            if (prefDao.getLoggedInUser()?.role.equals("asha", true)) {
                findNavController().navigate(
                    AllHouseholdFragmentDirections.actionAllHouseholdFragmentToNewHouseholdFragment(
                        it
                    )
                )
            }
        }, {
           /* val bundle = Bundle()
            bundle.putLong("hhId", it)
            bundle.putString("diseaseType", "No")
            bundle.putInt("fromDisease", 0)
            findNavController().navigate(R.id.householdMembersFragments, bundle)*/
            findNavController().navigate(

                AshaProfileFragmentDirections.actionAshaProfileFragmentToHouseholdMembersFragment(
                    it,0,"No"
                )
            )
        }, {
            if (prefDao.getLoggedInUser()?.role.equals("asha", true)) {
                if (it.numMembers == 0) {
                    findNavController().navigate(
                        AshaProfileFragmentDirections.actionAshaProfileFragmentToNewBenRegFragment(
                            it.hhId,
                            18
                        )
                    )
                } else {

                    viewModel.setSelectedHouseholdId(it.hhId)
                    addBenAlert.show()
                }
            }

        },
        {


        },))
        binding.rvAny.adapter = householdAdapter



        lifecycleScope.launch {
            viewModel.householdList.collect {
                householdAdapter.submitList(it)
            }
        }


        binding.fabEdit.setOnClickListener {
            viewModel.setRecordExist(false)
        }
        binding.btnSubmit.setOnClickListener {
            if (validate()) viewModel.saveForm()
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state!!) {
                AshaProfileViewModel.State.IDLE -> {
                }

                AshaProfileViewModel.State.SAVING -> {
                    binding.llContent.visibility = View.GONE
                    binding.pbForm.visibility = View.VISIBLE
                }

                AshaProfileViewModel.State.SAVE_SUCCESS -> {
                    binding.llContent.visibility = View.VISIBLE
                    binding.pbForm.visibility = View.GONE
                    Toast.makeText(context, "Save Successful", Toast.LENGTH_LONG).show()
                    viewModel.setRecordExist(true)
                }

                AshaProfileViewModel.State.SAVE_FAILED -> {
                    Toast.makeText(

                        context, "Something wend wong! Contact testing!", Toast.LENGTH_LONG
                    ).show()
                    binding.llContent.visibility = View.VISIBLE
                    binding.pbForm.visibility = View.GONE
//                    binding.textError.visibility = View.VISIBLE
                }
            }
        }
    }




    private fun hardCodedListUpdate(formId: Int) {
        binding.form.rvInputForm.adapter?.apply {
            when (formId) {
                1 -> {
                    notifyItemChanged(1)
                    notifyItemChanged(2)

                }

                4, 5 -> {
                    notifyDataSetChanged()
                }

            }
        }
    }


    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as HomeActivity).updateActionBar(
                R.drawable.ic__menopause,
                getString(R.string.asha_profile)
            )
        }
    }


    fun validate(): Boolean {
        val result = binding.form.rvInputForm.adapter?.let {
            (it as FormInputAdapter).validateInput(resources)
        }
        Timber.d("Validation : $result")
        return if (result == -1)
            true
        else {
            if (result != null) {
                binding.form.rvInputForm.scrollToPosition(result)
            }
            false
        }
    }

    private fun takeImage() {
        lifecycleScope.launchWhenStarted {
            getTmpFileUri().let { uri ->
                latestTmpUri = uri
                takePicture.launch(uri)
            }
        }
    }

    private fun getTmpFileUri(): Uri {
        val tmpFile =
            File.createTempFile(Konstants.tempBenImagePrefix, null, requireActivity().cacheDir)
                .apply {
                    createNewFile()
//                deleteOnExit()
                }
        return FileProvider.getUriForFile(
            requireContext(),
            "${BuildConfig.APPLICATION_ID}.provider",
            tmpFile
        )
    }

    private val addBenAlert by lazy {
        val addBenAlertBinding = AlertNewBenBinding.inflate(layoutInflater, binding.root, false)
        addBenAlertBinding.rgGender.setOnCheckedChangeListener { radioGroup, i ->
            addBenAlertBinding.btnOk.isEnabled = false
            Timber.d("RG Gender selected id : $i")
            val selectedGender = when (i) {
                addBenAlertBinding.rbMale.id -> Gender.MALE
                addBenAlertBinding.rbFemale.id -> Gender.FEMALE
                addBenAlertBinding.rbTrans.id -> Gender.TRANSGENDER
                else -> null
            }
            addBenAlertBinding.linearLayout4.visibility =
                selectedGender?.let { View.VISIBLE } ?: View.GONE
            addBenAlertBinding.actvRth.text = null

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
                addBenAlertBinding.actvRth.setAdapter(
                    ArrayAdapter(
                        requireContext(), android.R.layout.simple_spinner_dropdown_item,
                        it,
                    )
                )
            }
        }
        addBenAlertBinding.actvRth.setOnItemClickListener { adapterView, view, i, l ->
            Timber.d("item clicked index : $i")
            addBenAlertBinding.btnOk.isEnabled = true
        }


        val alert = MaterialAlertDialogBuilder(requireContext()).setView(addBenAlertBinding.root)
            .setOnCancelListener {
                viewModel.resetSelectedHouseholdId()
                addBenAlertBinding.rgGender.clearCheck()
                addBenAlertBinding.linearLayout4.visibility = View.GONE
                addBenAlertBinding.actvRth.text = null
            }.create()


        addBenAlertBinding.btnOk.setOnClickListener {
            findNavController().navigate(
                AshaProfileFragmentDirections.actionAshaProfileFragmentToNewBenRegFragment(
                    hhId = viewModel.selectedHouseholdId,
                    relToHeadId = resources.getStringArray(R.array.nbr_relationship_to_head_src)
                        .indexOf(addBenAlertBinding.actvRth.text.toString()),
                    gender = when (addBenAlertBinding.rgGender.checkedRadioButtonId) {
                        addBenAlertBinding.rbMale.id -> 1
                        addBenAlertBinding.rbFemale.id -> 2
                        addBenAlertBinding.rbTrans.id -> 3
                        else -> 0
                    }
                )
            )
            viewModel.resetSelectedHouseholdId()
            alert.cancel()
        }
        addBenAlertBinding.btnCancel.setOnClickListener {
            alert.cancel()
            viewModel.resetSelectedHouseholdId()
        }

        alert
    }

}