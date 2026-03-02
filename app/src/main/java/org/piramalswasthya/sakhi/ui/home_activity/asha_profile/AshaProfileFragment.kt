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
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
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
import org.piramalswasthya.sakhi.model.BenRegCache


@AndroidEntryPoint
class AshaProfileFragment : Fragment() {

    private var _binding: FragmentAshaProfileBinding? = null
    private val binding: FragmentAshaProfileBinding
        get() = _binding!!
    private val viewModel: AshaProfileViewModel by viewModels()

    private var latestTmpUri: Uri? = null

    @Inject
    lateinit var prefDao: PreferenceDao

    private var addBenAlert: AlertDialog? = null
    private var addBenAlertBinding: AlertNewBenBinding? = null




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
        buildAddBenDialog()
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
                        isAshaFamily = "Yes"
                )
            )
        }

        val householdAdapter = HouseHoldListAdapter("",false, prefDao, false, HouseHoldListAdapter.HouseholdClickListener({
                findNavController().navigate(
                    AllHouseholdFragmentDirections.actionAllHouseholdFragmentToNewHouseholdFragment(
                        it.hhId
                    )
                )
        }, {
            findNavController().navigate(
                AshaProfileFragmentDirections.actionAshaProfileFragmentToHouseholdMembersFragment(
                    it.hhId,0,"No"
                )
            )
        }, {
                if (it.numMembers == 0) {
                    findNavController().navigate(
                        AshaProfileFragmentDirections.actionAshaProfileFragmentToNewBenRegFragment(
                            it.hhId,
                            18
                        )
                    )
                } else {

                    viewModel.setSelectedHouseholdId(it.hhId)
                    addBenAlert?.show()
                }

        },
        {


        },{

            }))
        binding.rvAny.adapter = householdAdapter



        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.householdList.collect {
                    householdAdapter.submitList(it)
                }
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

    private fun buildAddBenDialog() {
        val alertBinding = AlertNewBenBinding.inflate(layoutInflater, binding.root, false)
        addBenAlertBinding = alertBinding
        alertBinding.btnOk.isEnabled = false

        setupGenderListener(alertBinding)
        setupRelationClickListener(alertBinding)

        val alert = createAddBenDialog(alertBinding)
        addBenAlert = alert

        setupAddBenButtons(alertBinding, alert)
    }

    private fun setupGenderListener(binding: AlertNewBenBinding) {
        binding.rgGender.setOnCheckedChangeListener { _, checkedId ->
            binding.btnOk.isEnabled = false

            val selectedGender = genderFromCheckedId(binding, checkedId)
            if (selectedGender == null) {
                binding.linearLayout4.visibility = View.GONE
                binding.actvRth.text = null
                binding.actvRth.setAdapter(null)
                return@setOnCheckedChangeListener
            }

            binding.linearLayout4.visibility = View.VISIBLE
            binding.actvRth.text = null

            val ctx = computeHofContext()
            val baseList = baseRelations(selectedGender)
            val filtered = filterRelations(selectedGender, baseList, ctx)

            binding.actvRth.setAdapter(
                ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, filtered)
            )
        }
    }

    private data class HofContext(
        val hof: BenRegCache?,
        val fatherRegistered: Boolean,
        val motherRegistered: Boolean,
        val isUnmarried: Boolean,
        val isMarried: Boolean
    )

    private fun computeHofContext(): HofContext {
        val hof = viewModel.householdBenList.firstOrNull { it.familyHeadRelationPosition == 19 }
        val fatherRegistered = viewModel.householdBenList.any { it.familyHeadRelationPosition == 2 }
        val motherRegistered = viewModel.householdBenList.any { it.familyHeadRelationPosition == 1 }

        val maritalId = hof?.genDetails?.maritalStatusId
        return HofContext(
            hof = hof,
            fatherRegistered = fatherRegistered,
            motherRegistered = motherRegistered,
            isUnmarried = maritalId == 1,
            isMarried = maritalId == 2
        )
    }

    private fun baseRelations(selectedGender: Gender): MutableList<String> {
        val arr = when (selectedGender) {
            Gender.MALE -> resources.getStringArray(R.array.nbr_relationship_to_head_male)
            Gender.FEMALE -> resources.getStringArray(R.array.nbr_relationship_to_head_female)
            Gender.TRANSGENDER -> resources.getStringArray(R.array.nbr_relationship_to_head_male)
        }
        return arr.toMutableList()
    }

    private fun filterRelations(
        selectedGender: Gender,
        baseList: MutableList<String>,
        ctx: HofContext
    ): List<String> {
        val hof = ctx.hof ?: return baseList

        val common = resources.getStringArray(R.array.nbr_relationship_to_head)

        if (ctx.fatherRegistered) baseList.remove(common[1])
        if (ctx.motherRegistered) baseList.remove(common[0])

        if (ctx.isUnmarried) {
            val unmarriedFilter =
                resources.getStringArray(R.array.nbr_relationship_to_head_unmarried_filter).toSet()
            baseList.removeAll(unmarriedFilter)
        } else if (!ctx.isMarried) {
            baseList.remove(common[5])
            baseList.remove(common[4])
        }

        val hofGender = hof.gender
        if (hofGender == Gender.MALE && selectedGender == Gender.MALE) baseList.remove(common[5])
        if (hofGender == Gender.FEMALE && selectedGender == Gender.FEMALE) baseList.remove(common[4])

        return baseList
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
                viewModel.resetSelectedHouseholdId()
                binding.btnOk.isEnabled = false
                binding.rgGender.clearCheck()
                binding.linearLayout4.visibility = View.GONE
                binding.actvRth.text = null
                binding.actvRth.setAdapter(null)
            }
            .create()
    }

    private fun setupAddBenButtons(binding: AlertNewBenBinding, alert: AlertDialog) {
        binding.btnOk.setOnClickListener {
            val relIndex = resources.getStringArray(R.array.nbr_relationship_to_head_src)
                .indexOf(binding.actvRth.text.toString())

            val genderInt = genderIntFromRadio(binding)

            if (relIndex < 0 || genderInt == 0) {
                if (relIndex < 0) binding.actvRth.error = resources.getString(R.string.relation_with_hof)
                return@setOnClickListener
            }

            findNavController().navigate(
                AshaProfileFragmentDirections.actionAshaProfileFragmentToNewBenRegFragment(
                    hhId = viewModel.selectedHouseholdId,
                    relToHeadId = relIndex,
                    gender = genderInt
                )
            )

            viewModel.resetSelectedHouseholdId()
            alert.cancel()
        }

        binding.btnCancel.setOnClickListener {
            alert.cancel()
            viewModel.resetSelectedHouseholdId()
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



    override fun onDestroyView() {
        super.onDestroyView()
        addBenAlert?.dismiss()
        addBenAlert = null
        addBenAlertBinding = null
        _binding = null
    }

}
