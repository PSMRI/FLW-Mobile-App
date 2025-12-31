package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pregnant_woment_anc_visits.homeVisit

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.dynamicAdapter.FormRendererAdapter
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.databinding.FragmentAntenatalCounsellingBinding
import org.piramalswasthya.sakhi.model.BenWithAncListDomain
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pregnant_woment_anc_visits.list.PwAncVisitsListViewModel
import org.piramalswasthya.sakhi.utils.dynamicFiledValidator.FieldValidator
import org.piramalswasthya.sakhi.utils.dynamicFormConstants.FormConstants.ANC_FORM_ID
import org.piramalswasthya.sakhi.work.WorkerUtils
import timber.log.Timber
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class AntenatalCounsellingFragment : Fragment() {

    private val args: AntenatalCounsellingFragmentArgs by navArgs()
    private var _binding: FragmentAntenatalCounsellingBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AntenatalCounsellingViewModel by viewModels()
    private val viewModelAnc: PwAncVisitsListViewModel by viewModels()

    var benId = -1L
    var hhId = -1L
    var dob = -1L
    var isViewMode = false
    var visitDate = ""
    private lateinit var adapter: FormRendererAdapter
    private var allBenList: List<BenWithAncListDomain> = emptyList()
    private lateinit var benList: BenWithAncListDomain

    // List of 21 danger sign question IDs
    private val dangerSignQuestions = listOf(
        "swelling", "high_bp", "convulsions", "anemia", "reduced_fetal_movement",
        "age_risk", "child_gap", "short_height", "pre_preg_weight", "bleeding",
        "miscarriage_history", "four_plus_delivery", "first_delivery", "twin_pregnancy",
        "c_section_history", "pre_existing_disease", "fever_malaria", "jaundice",
        "sickle_cell", "prolonged_labor", "malpresentation"
    )

    @Inject
    lateinit var pref: PreferenceDao
    var langCode = ""
    private var isReferralDialogShown = false
    private var hasAnyDangerSign = false
    private var isSelectAllChecked = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAntenatalCounsellingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        benId = args.benId
        isViewMode = args.viewMode
        visitDate = args.visitDate ?: " "



        val currentLang = pref.getCurrentLanguage()
        langCode = currentLang.symbol
        val todayDate: String = LocalDate.now()
            .format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))


        lifecycleScope.launch {
            viewModelAnc.benList.collect {list: List<BenWithAncListDomain> ->
                allBenList = list
                val selectedBen = list.firstOrNull { it.ben.benId == benId }

                if (selectedBen != null) {
                    benList = selectedBen
                    binding.tvPregnantWomanValue.text = benList.ben.benName
                    binding.tvHusbandValue.text = benList.ben.spouseName
                    binding.tvAgeValue.text = benList.ben.age
                    binding.tvRegDateValue.text = benList.ben.regDate
                    binding.tvPhoneValue.text =benList.ben.mobileNo
                    binding.tvLmpValue.text = benList.lmpString
                    binding.tvWeeksValue.text = benList.weekOfPregnancy.toString()
                    binding.tvEddValue.text = benList.eddString

                    // Calculate mother's age and set it in ViewModel
                    calculateAndSetMotherAge()
                } else {
                    Timber.e("Beneficiary not found for benId=$benId")
                }
            }
        }

        setupSelectAllCheckbox()

        if (isViewMode) {
            binding.btnSave.isVisible = false
            binding.tvLastVisitValue.text = visitDate
            binding.cbSelectAll.isVisible = false
            viewModel.loadFormSchema(benId, ANC_FORM_ID, visitDate, true, langCode)
            lifecycleScope.launch {
                viewModel.schema.collectLatest { schema ->
                    if (schema == null) return@collectLatest
                    val visibleFields = viewModel.getVisibleFields().toMutableList()
                    val minVisitDate = viewModel.getMinVisitDate()
                    val maxVisitDate = viewModel.getMaxVisitDate()

                    adapter = FormRendererAdapter(
                        visibleFields,
                        isViewOnly = true,
                        minVisitDate = minVisitDate,
                        maxVisitDate = maxVisitDate,
                        isSNCU = viewModel.isSNCU.value ?: false,
                        onValueChanged = { field, value ->
                            if (value == "pick_image") {
                                // Handle image picking
                            } else {
                                field.value = value
                                viewModel.updateFieldValue(field.fieldId, value)
                                adapter.updateFields(viewModel.getVisibleFields())
                            }
                        },
                        onShowAlert = null
                    )

                    binding.recyclerView.adapter = adapter
                }
            }
        } else {
            viewModel.loadFormSchema(benId, ANC_FORM_ID, todayDate, false, langCode)

            lifecycleScope.launch {
                binding.tvLastVisitValue.text = viewModel.getLastVisitDates(benId)
                viewModel.schema.collectLatest { schema ->
                    if (schema == null) return@collectLatest

                    val visibleFields = viewModel.getVisibleFields().toMutableList()
                    val minVisitDate = viewModel.getMinVisitDate()
                    val maxVisitDate = viewModel.getMaxVisitDate()

                    adapter = FormRendererAdapter(
                        visibleFields,
                        isViewOnly = false,
                        minVisitDate = minVisitDate,
                        maxVisitDate = maxVisitDate,
                        isSNCU = viewModel.isSNCU.value ?: false,
                        onValueChanged = { field, value ->
                            if (value == "pick_image") {
                            } else {
                                field.value = value
                                viewModel.updateFieldValue(field.fieldId, value)
                                adapter.updateFields(viewModel.getVisibleFields())

                                updateSelectAllCheckbox()
                            }
                        },
                        onShowAlert = null
                    )

                    binding.recyclerView.adapter = adapter

                    updateSelectAllCheckbox()
                }


            }


        }



        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                AntenatalCounsellingViewModel.State.IDLE -> {
                }

                AntenatalCounsellingViewModel.State.LOADING -> {
                }

                AntenatalCounsellingViewModel.State.SUCCESS -> {

                    WorkerUtils.triggerAmritPushWorker(requireContext())

                    Toast.makeText(
                        requireContext(),
                        "Form submitted successfully",
                        Toast.LENGTH_SHORT
                    ).show()

                    showContinueAncVisitDialog()
                }

                AntenatalCounsellingViewModel.State.FAIL -> {
                    Toast.makeText(
                        requireContext(),
                        "Something went wrong. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        binding.btnSave.setOnClickListener { handleFormSubmission() }
    }

    private fun calculateAndSetMotherAge() {
        try {
            val ageText = binding.tvAgeValue.text.toString()
            val ageStr = ageText.replace(" years", "").replace(" yrs", "").replace(" year", "").trim()
            val age = ageStr.toIntOrNull()

            if (age != null) {
                viewModel.setMotherAge(age)
                Timber.d("Mother's age set to: $age")
            } else {
                Timber.e("Could not parse age from: $ageText")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error calculating mother's age")
        }
    }

    private fun setupSelectAllCheckbox() {
        binding.cbSelectAll.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                isSelectAllChecked = isChecked
                if (isChecked) {
                    dangerSignQuestions.forEach { fieldId ->
                        viewModel.updateFieldValue(fieldId, "Yes")
                    }
                } else {
                    dangerSignQuestions.forEach { fieldId ->
                        viewModel.updateFieldValue(fieldId, "No")
                    }
                }
                adapter?.updateFields(viewModel.getVisibleFields())
            }
        }
    }

    private fun updateSelectAllCheckbox() {
        val currentSchema = viewModel.schema.value ?: return
        val allFields = currentSchema.sections.orEmpty().flatMap { it.fields.orEmpty() }

        val allYes = dangerSignQuestions.all { questionId ->
            val field = allFields.find { it.fieldId == questionId }
            field?.value?.toString().equals("Yes", ignoreCase = true)
        }

        val allNo = dangerSignQuestions.all { questionId ->
            val field = allFields.find { it.fieldId == questionId }
            field?.value?.toString().equals("No", ignoreCase = true)
        }

        binding.cbSelectAll.setOnCheckedChangeListener(null)

        if (allYes) {
            binding.cbSelectAll.isChecked = true
            isSelectAllChecked = true
        } else if (allNo) {
            binding.cbSelectAll.isChecked = false
            isSelectAllChecked = false
        } else {
            binding.cbSelectAll.isChecked = false
            isSelectAllChecked = false
        }

        binding.cbSelectAll.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                isSelectAllChecked = isChecked
                if (isChecked) {
                    dangerSignQuestions.forEach { fieldId ->
                        viewModel.updateFieldValue(fieldId, "Yes")
                    }
                } else {
                    dangerSignQuestions.forEach { fieldId ->
                        viewModel.updateFieldValue(fieldId, "No")
                    }
                }
                adapter?.updateFields(viewModel.getVisibleFields())
            }
        }
    }

    private fun showContinueAncVisitDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Continue ANC Visit")
            .setMessage("Do you want to continue with ANC visit?")
            .setPositiveButton("Yes") { dialog, _ ->
                dialog.dismiss()
                if(benList.showAddAnc){
                navigateToAddAncVisitScreen()}
                else
                {   dialog.dismiss()
                    Toast.makeText(
                        requireContext(),
                        "Next ANC visit is DUE",
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().popBackStack()}
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
                findNavController().popBackStack()
            }
            .setCancelable(false)
            .show()
    }



    private fun checkForDangerSigns(currentSchema: org.piramalswasthya.sakhi.model.dynamicEntity.FormSchemaDto): Boolean {
        return try {
            val allFields = currentSchema.sections.orEmpty()
                .flatMap { it.fields.orEmpty() }

            dangerSignQuestions.any { questionId ->
                val field = allFields.find { it.fieldId == questionId }
                field?.value?.toString()?.equals("Yes", ignoreCase = true) == true
            }
        } catch (e: Exception) {
            Timber.e(e, "Error checking danger signs")
            false
        }
    }

    private fun handleFormSubmission() {
        val currentSchema = viewModel.schema.value ?: return

        val updatedFields = adapter.getUpdatedFields()
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val today = Date()

        currentSchema.sections.orEmpty().forEach { section ->
            section.fields.orEmpty().forEach { field ->
                field.errorMessage = null
            }
        }

        var hasValidationErrors = false

        currentSchema.sections.orEmpty().forEach { section ->
            section.fields.orEmpty().forEach { schemaField ->
                updatedFields.find { it.fieldId == schemaField.fieldId }?.let { updated ->
                    schemaField.value = updated.value

                    if (schemaField.required && (schemaField.value == null ||
                                schemaField.value.toString().isBlank())) {
                        schemaField.errorMessage = "${schemaField.label} is required"
                        hasValidationErrors = true
                    }

                    if (schemaField.fieldId == "visit_date" && schemaField.value is String) {
                        val visitDateStr = schemaField.value as String
                        val visitDate = try {
                            sdf.parse(visitDateStr)
                        } catch (e: Exception) {
                            null
                        }

                        val errorMessage = when {
                            visitDateStr.isBlank() -> "Visit date is required"
                            visitDate == null -> "Invalid visit date format"
                            visitDate.after(today) -> "Visit Date cannot be after today's date"
                            else -> null
                        }

                        if (errorMessage != null) {
                            schemaField.errorMessage = errorMessage
                            hasValidationErrors = true
                        }
                    }
                }
            }
        }

        val fieldsWithErrors = updatedFields.map { field ->
            val schemaField = currentSchema.sections.orEmpty()
                .flatMap { it.fields.orEmpty() }
                .find { it.fieldId == field.fieldId }
            field.copy(errorMessage = schemaField?.errorMessage)
        }

        adapter.updateFields(fieldsWithErrors)

        val firstErrorIndex = fieldsWithErrors.indexOfFirst {
            !it.errorMessage.isNullOrBlank() && it.visible
        }

        if (firstErrorIndex >= 0) {
            binding.recyclerView.scrollToPosition(firstErrorIndex)
            Toast.makeText(
                requireContext(),
                "Please fill all required fields correctly",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (hasValidationErrors) {
            Toast.makeText(
                requireContext(),
                "Please fill all required fields correctly",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        hasAnyDangerSign = checkForDangerSigns(currentSchema)

        Timber.d("Danger signs check: $hasAnyDangerSign, Dialog already shown: $isReferralDialogShown")

        if (hasAnyDangerSign && !isReferralDialogShown) {
            showDangerSignDialog()
        } else {
            saveFormData()
        }
    }

    private fun showDangerSignDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Danger Signs Identified!")
            .setMessage("Do you want to refer to HWC facility?")
            .setPositiveButton("Yes") { dialog, _ ->
                dialog.dismiss()

                isReferralDialogShown = true
                hasAnyDangerSign = true
                navigateToHwcReferralScreen()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()

                isReferralDialogShown = true
                hasAnyDangerSign = false
                saveFormData()
            }
            .setCancelable(false)
            .show()
    }

    private fun navigateToAddAncVisitScreen() {
        try {
            val nextVisitNumber = if (benList?.anc?.isEmpty() == true) 1 else benList?.anc?.maxOf { it.visitNumber }!! + 1

            findNavController().navigate(
                AntenatalCounsellingFragmentDirections.actionPwAncCounsellingFormFragmentToPwAncFormFragment(
                    benId = benId,
                    hhId = benList?.ben?.hhId?.toString() ?: "",
                    visitNumber = nextVisitNumber
                )
            )
        } catch (e: Exception) {
            Timber.e(e, "Error navigating to Add ANC Visit screen")
            findNavController().popBackStack()
        }
    }

    private fun navigateToHwcReferralScreen() {
        try {

            saveFormData()
        } catch (e: Exception) {
            Timber.e(e, "Error navigating to HWC Referral screen")
            saveFormData()
        }
    }

    private fun saveFormData() {
        lifecycleScope.launch {
            Log.d("anc_home_visit", "Saving form data, hasAnyDangerSign: $hasAnyDangerSign")
            viewModel.saveFormResponses(benId)
        }
    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as HomeActivity).updateActionBar(
                R.drawable.ic__anc_visit,
                getString(R.string.anc_visit)
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}