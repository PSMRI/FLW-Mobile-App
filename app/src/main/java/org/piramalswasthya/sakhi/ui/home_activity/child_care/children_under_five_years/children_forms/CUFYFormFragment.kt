package org.piramalswasthya.sakhi.ui.home_activity.child_care.children_under_five_years.children_forms

import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.dynamicAdapter.FormRendererAdapter
import org.piramalswasthya.sakhi.configuration.dynamicDataSet.FormField
import org.piramalswasthya.sakhi.databinding.FragmentChildrenUnderFiveFormBinding
import org.piramalswasthya.sakhi.databinding.RvItemBenChildCareInfantBinding
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import org.piramalswasthya.sakhi.utils.dynamicFiledValidator.FieldValidator
import org.piramalswasthya.sakhi.utils.dynamicFormConstants.FormConstants
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.view.isVisible
import androidx.core.view.isGone
import org.piramalswasthya.sakhi.adapters.dynamicAdapter.BottleAdapter
import org.json.JSONObject
import org.piramalswasthya.sakhi.adapters.dynamicAdapter.FollowUpVisitAdapter
import org.piramalswasthya.sakhi.utils.HelperUtil.checkAndShowMUACAlert
import org.piramalswasthya.sakhi.utils.HelperUtil.checkAndShowSAMAlert
import org.piramalswasthya.sakhi.utils.HelperUtil.checkAndShowWeightForHeightAlert
import org.piramalswasthya.sakhi.utils.dynamicFormConstants.FormConstants.IFA_FORM_NAME
import org.piramalswasthya.sakhi.utils.dynamicFormConstants.FormConstants.ORS_FORM_NAME
import org.piramalswasthya.sakhi.utils.dynamicFormConstants.FormConstants.SAM_FORM_NAME
import org.piramalswasthya.sakhi.work.dynamicWoker.CUFYIFAPushWorker
import org.piramalswasthya.sakhi.work.dynamicWoker.CUFYORSPushWorker
import org.piramalswasthya.sakhi.work.dynamicWoker.CUFYSAMPushWorker
import timber.log.Timber

@AndroidEntryPoint
class CUFYFormFragment : Fragment() {

    val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

    private val args: CUFYFormFragmentArgs by navArgs()

    private val infantListViewModel: CUFYFormCardViewModel by viewModels()
    private val viewModel: CUFYFormViewModel by viewModels()
    var benId = -1L
    var hhId = -1L
    var dob = -1L
    var isViewMode = false
    lateinit var formId: String
    private var recordId: Int = 0
    private lateinit var adapter: FormRendererAdapter
    private var currentImageField: FormField? = null
    private var tempCameraUri: Uri? = null
    private var _binding: FragmentChildrenUnderFiveFormBinding? = null
    private val binding get() = _binding!!
    private lateinit var followUpAdapter: FollowUpVisitAdapter
    private val followUpVisits = mutableListOf<String>()

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                try {

                    requireContext().contentResolver.takePersistableUriPermission(
                        it,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: SecurityException) {
                    Timber.tag("CUFYFormFragment").e(e, " Failed to persist URI permission: $it")
                }

                val sizeInMB = requireContext().getFileSizeInMB(it)
                val maxSize = (currentImageField?.validation?.maxSizeMB ?: 5).toDouble()

                if (sizeInMB != null && sizeInMB > maxSize) {
                    currentImageField?.errorMessage =
                        currentImageField?.validation?.errorMessage
                            ?: "Image must be less than ${maxSize.toInt()}MB"
                    adapter.notifyDataSetChanged()
                    return@let
                }

                currentImageField?.apply {
                    value = it.toString()
                    errorMessage = null
                }

                adapter.notifyDataSetChanged()
            }
        }


    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && tempCameraUri != null) {
                val sizeInMB = requireContext().getFileSizeInMB(tempCameraUri!!)
                val maxSize = (currentImageField?.validation?.maxSizeMB ?: 5).toDouble()

                if (sizeInMB != null && sizeInMB > maxSize) {
                    currentImageField?.errorMessage =
                        currentImageField?.validation?.errorMessage
                            ?: "Image must be less than ${maxSize.toInt()}MB"
                    adapter.notifyDataSetChanged()
                    return@registerForActivityResult
                }

                currentImageField?.apply {
                    value = tempCameraUri.toString()
                    errorMessage = null
                }
                adapter.notifyDataSetChanged()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChildrenUnderFiveFormBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val container = view.findViewById<FrameLayout>(R.id.infant_card_container)
        val infantBinding =
            RvItemBenChildCareInfantBinding.inflate(layoutInflater, container, false)
        container.addView(infantBinding.root)

        val visitType = args.visitType
        isViewMode = args.isViewMode
        benId = args.benId
        hhId = args.hhId
        recordId = args.recordId
        viewModel.setRecordId(recordId)
        val formDataJson = args.formDataJson

        binding.fabEdit.isVisible = isViewMode
        setupFollowUpRecyclerView()

        binding.fabEdit.setOnClickListener {
            isViewMode = false
            binding.fabEdit.isGone = true
            binding.btnSave.isVisible = true
            refreshAdapter()
        }

        if (visitType.equals(ORS_FORM_NAME)){
            formId = FormConstants.CHILDREN_UNDER_FIVE_ORS_FORM_ID;
        }else if (visitType.equals(IFA_FORM_NAME)){
            formId = FormConstants.CHILDREN_UNDER_FIVE_IFA_FORM_ID;
        }else if (visitType.equals(SAM_FORM_NAME)){
            formId = FormConstants.CHILDREN_UNDER_FIVE_SAM_FORM_ID;
        }


        infantListViewModel.getBenById(benId) { ben ->
            infantBinding.btnHBNC.visibility = View.GONE
            infantBinding.dueIcon.visibility = View.GONE
            ben?.syncState = null
            infantBinding.ben = ben
        }

        infantListViewModel.getDobByBenIdAsync(benId) { dob ->
            if (!formDataJson.isNullOrEmpty()) {

                viewModel.loadFormSchemaFromJson(
                    benId = benId,
                    formId = formId,
                    visitDay = visitType!!,
                    isViewMode = isViewMode,
                    formDataJson = formDataJson
                )
                handleFollowUpVisitsDisplay()
            } else {

                viewModel.loadFormSchema(
                    benId = benId,
                    formId = formId,
                    visitDay = visitType!!,
                    viewMode = isViewMode
                )
            }
        }
        lifecycleScope.launch {
            viewModel.schema.collectLatest { schema ->
                if (schema == null) return@collectLatest
                refreshAdapter()
            }
        }

        viewModel.saveFormState.observe(viewLifecycleOwner) { state ->

            when (state) {
                is CUFYFormViewModel.SaveFormState.Idle -> {
                }
                is CUFYFormViewModel.SaveFormState.Loading -> {
                }
                is CUFYFormViewModel.SaveFormState.Success -> {

                    if (visitType.equals(ORS_FORM_NAME)){
                        CUFYORSPushWorker.enqueue(requireContext())
                    }else if (visitType.equals(IFA_FORM_NAME)){
                        CUFYIFAPushWorker.enqueue(requireContext())
                    }else if (visitType.equals(SAM_FORM_NAME)){
                        CUFYSAMPushWorker.enqueue(requireContext())
                    }

                    lifecycleScope.launch {
                        findNavController().previousBackStackEntry?.savedStateHandle?.set("form_submitted", true)
                        findNavController().popBackStack()
                    }
                }
                is CUFYFormViewModel.SaveFormState.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnSave.setOnClickListener {
            handleFormSubmission()
        }

        tableRendar()
    }

    private fun tableRendar() {
        binding.tableRv.layoutManager = LinearLayoutManager(requireContext())
        viewModel.bottleList.observe(viewLifecycleOwner) { list ->

            if (formId.equals(FormConstants.CHILDREN_UNDER_FIVE_IFA_FORM_ID, ignoreCase = true) && !list.isNullOrEmpty()) {
                binding.llTable.visibility = View.VISIBLE
                binding.tableRv.adapter = BottleAdapter(list)
            } else {
                binding.llTable.visibility = View.GONE
            }
        }
        viewModel.loadBottleData(benId, formId)
    }

    private fun refreshAdapter() {
        val visibleFields = viewModel.getVisibleFields().toMutableList()
        val minVisitDate = viewModel.getMinVisitDate()
        val maxVisitDate = viewModel.getMaxVisitDate()

        if (formId == FormConstants.CHILDREN_UNDER_FIVE_SAM_FORM_ID && recordId > 0) {
            visibleFields.find { it.fieldId == "visit_date" }?.let { visitDateField ->
                visitDateField.isEditable = false

            }
        }

        adapter = FormRendererAdapter(
            visibleFields,
            isViewOnly = isViewMode,
            minVisitDate = minVisitDate,
            maxVisitDate = maxVisitDate,
            onValueChanged = { field, value ->
                if (value == "pick_image") {
                    currentImageField = field
                    showImagePickerDialog()
                } else {
                    if (formId == FormConstants.CHILDREN_UNDER_FIVE_SAM_FORM_ID && recordId > 0 && field.fieldId == "visit_date") {
                        return@FormRendererAdapter
                    }

                    field.value = value
                    viewModel.updateFieldValue(field.fieldId, value)
                    val updatedVisibleFields = viewModel.getVisibleFields()
                    adapter.updateFields(updatedVisibleFields)


                    if (formId == FormConstants.CHILDREN_UNDER_FIVE_SAM_FORM_ID) {
                        checkAndShowSAMAlert(requireContext(), field.fieldId, value)
                    }
                }
            },

            onShowAlert = { alertType, value ->

                if (formId == FormConstants.CHILDREN_UNDER_FIVE_SAM_FORM_ID) {
                    when (alertType) {
                        "CHECK_MUAC" -> checkAndShowMUACAlert(requireContext(), value)
                        "CHECK_WEIGHT_HEIGHT" -> checkAndShowWeightForHeightAlert(requireContext(), value)
                    }
                }
            }
        )

        binding.recyclerView.adapter = adapter
        binding.btnSave.isVisible = !isViewMode
        binding.fabEdit.isVisible = isViewMode
    }

    private fun setupFollowUpRecyclerView() {
        followUpAdapter = FollowUpVisitAdapter()
        binding.rvFollowUpVisits.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = followUpAdapter
        }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf(getString(R.string.take_photo),
            getString(R.string.choose_from_gallery))

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.select_image))
            .setItems(options) { _, which ->
                when (which) {
                    0 -> launchCamera()
                    1 -> galleryLauncher.launch("image/*")
                }
            }
            .show()
    }

    private fun launchCamera() {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }

        tempCameraUri = requireContext().contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )
        cameraLauncher.launch(tempCameraUri)
    }



    private fun handleFormSubmission() {
        val currentSchema = viewModel.schema.value ?: return
        val currentVisitDay = viewModel.visitDay
        val previousVisitDate = viewModel.previousVisitDate
        val deliveryDate = dob ?: return
        val dobString = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date(deliveryDate))

        if (currentVisitDay.isBlank()) return

        val updatedFields = adapter.getUpdatedFields()
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val today = Date()

        currentSchema.sections.orEmpty().forEach { section ->
            section.fields.orEmpty().forEach { schemaField ->
                updatedFields.find { it.fieldId == schemaField.fieldId }?.let { updated ->
                    schemaField.value = updated.value

                    val result = FieldValidator.validate(updated, dobString)
                    updated.errorMessage = if (!result.isValid) result.errorMessage else null
                    schemaField.errorMessage = updated.errorMessage

                    if (schemaField.fieldId == "visit_date" && schemaField.value is String) {
                        val visitDateStr = schemaField.value as String
                        val visitDate = try {
                            sdf.parse(visitDateStr)
                        } catch (e: Exception) {
                            null
                        }

                        val errorMessage = when {
                            visitDate == null -> getString(R.string.invalid_visit_date)
                            today != null && visitDate.after(today) -> getString(R.string.visit_date_cannot_be_after_today_s_date)
                            deliveryDate == null -> getString(R.string.delivery_date_is_missing)
                            visitDate.before(Date(deliveryDate)) -> getString(R.string.visit_date_cannot_be_before_delivery_date)
                            previousVisitDate != null && !visitDate.after(previousVisitDate) ->
                                getString(
                                    R.string.visit_date_must_be_after_previous_visit,
                                    sdf.format(previousVisitDate)
                                )
                            else -> null
                        }
                        schemaField.errorMessage = errorMessage
                        updated.errorMessage = errorMessage
                    }
                }
            }
        }

        updatedFields.forEach { adapterField ->
            currentSchema.sections.orEmpty().flatMap { it.fields.orEmpty() }
                .find { it.fieldId == adapterField.fieldId }
                ?.let { schemaField ->
                    adapterField.errorMessage = schemaField.errorMessage
                }
        }

        val copiedFields = updatedFields.map { updated ->
            val error = currentSchema.sections.orEmpty()
                .flatMap { it.fields.orEmpty() }
                .find { it.fieldId == updated.fieldId }
                ?.errorMessage
            updated.copy(errorMessage = error)
        }
        adapter.updateFields(copiedFields)
        adapter.notifyDataSetChanged()

        val firstErrorFieldId = currentSchema.sections.orEmpty()
            .flatMap { it.fields.orEmpty() }
            .firstOrNull { it.visible && !it.errorMessage.isNullOrBlank() }
            ?.fieldId

        val errorIndex = copiedFields.indexOfFirst { it.fieldId == firstErrorFieldId }
        if (errorIndex >= 0) binding.recyclerView.scrollToPosition(errorIndex)

        val hasErrors = currentSchema.sections.orEmpty().any { section ->
            section.fields.orEmpty().any { it.visible && !it.errorMessage.isNullOrBlank() }
        }
        if (hasErrors) return

        if (formId == FormConstants.CHILDREN_UNDER_FIVE_SAM_FORM_ID) {
            val visitDateField = currentSchema.sections.orEmpty()
                .flatMap { it.fields.orEmpty() }
                .find { it.fieldId == "visit_date" }

            val visitDate = visitDateField?.value as? String
            if (!visitDate.isNullOrEmpty()) {
                lifecycleScope.launch {
                    val existingVisits = viewModel.getFormsDataByFormID(formId, benId)

                    if (recordId == 0) {
                        val isDuplicate = existingVisits.any { existingVisit ->
                            val existingVisitDate = extractVisitDateFromFormData(existingVisit.formDataJson)
                            existingVisitDate == visitDate
                        }

                        val isDateGreaterThanAll = existingVisits.all { existingVisit ->
                            val existingVisitDate = extractVisitDateFromFormData(existingVisit.formDataJson)
                            if (existingVisitDate != null) {
                                val currentDate = sdf.parse(visitDate)
                                val existingDate = sdf.parse(existingVisitDate)
                                currentDate.after(existingDate)
                            } else {
                                true
                            }
                        }

                        when {
                            isDuplicate -> {
                                visitDateField.errorMessage = getString(
                                    R.string.visit_already_exists_for_this_date,
                                    visitDate
                                )
                                updatedFields.find { it.fieldId == "visit_date" }?.errorMessage = visitDateField.errorMessage

                                val updatedCopiedFields = updatedFields.map { field ->
                                    if (field.fieldId == "visit_date") {
                                        field.copy(errorMessage = visitDateField.errorMessage)
                                    } else {
                                        field
                                    }
                                }
                                adapter.updateFields(updatedCopiedFields)
                                adapter.notifyDataSetChanged()

                                val visitDateIndex = updatedCopiedFields.indexOfFirst { it.fieldId == "visit_date" }
                                if (visitDateIndex >= 0) binding.recyclerView.scrollToPosition(visitDateIndex)

                                Toast.makeText(requireContext(), R.string.visit_already_exists_for_this_date, Toast.LENGTH_LONG).show()
                            }
                            !isDateGreaterThanAll -> {
                                visitDateField.errorMessage =
                                    getString(R.string.visit_date_must_be_greater_then_the_last_visit_dates)
                                updatedFields.find { it.fieldId == "visit_date" }?.errorMessage = visitDateField.errorMessage

                                val updatedCopiedFields = updatedFields.map { field ->
                                    if (field.fieldId == "visit_date") {
                                        field.copy(errorMessage = visitDateField.errorMessage)
                                    } else {
                                        field
                                    }
                                }
                                adapter.updateFields(updatedCopiedFields)
                                adapter.notifyDataSetChanged()

                                val visitDateIndex = updatedCopiedFields.indexOfFirst { it.fieldId == "visit_date" }
                                if (visitDateIndex >= 0) binding.recyclerView.scrollToPosition(visitDateIndex)

                                Toast.makeText(requireContext(),
                                    getString(R.string.visit_date_must_be_after_all_existing_visit_dates), Toast.LENGTH_LONG).show()
                            }
                            else -> {
                                viewModel.saveFormResponses(benId, hhId, recordId)
                            }
                        }
                    } else {
                        viewModel.saveFormResponses(benId, hhId, recordId)
                    }
                }
            } else {
                lifecycleScope.launch {
                    viewModel.saveFormResponses(benId, hhId, recordId)
                }
            }
        } else {
            lifecycleScope.launch {
                viewModel.saveFormResponses(benId, hhId, recordId)
            }
        }
    }

    private fun extractVisitDateFromFormData(formDataJson: String): String? {
        return try {
            val jsonObject = JSONObject(formDataJson)
            val fields = jsonObject.optJSONObject("fields")
            fields?.optString("visit_date")
        } catch (e: Exception) {
            Timber.tag("CUFYFormFragment").e(e, "Error extracting visit date from form data")
            null
        }
    }

    private fun handleFollowUpVisitsDisplay() {
        val formDataJson = args.formDataJson

        if (!formDataJson.isNullOrEmpty() && (isViewMode || recordId > 0)) {
            val followUpDates = parseFollowUpDatesFromJson(formDataJson)

            if (followUpDates.isNotEmpty()) {
                val followUpItems = mutableListOf<FollowUpVisitAdapter.FollowUpVisitItem>()

                followUpItems.add(FollowUpVisitAdapter.FollowUpVisitItem.Header)

                followUpDates.forEachIndexed { index, date ->
                    val sno = (index + 1).toString()
                    followUpItems.add(FollowUpVisitAdapter.FollowUpVisitItem.VisitDate(sno, date))
                }

                followUpAdapter.submitList(followUpItems)
                binding.rvFollowUpVisits.visibility = View.VISIBLE
            } else {
                binding.rvFollowUpVisits.visibility = View.GONE
            }
        } else {
            binding.rvFollowUpVisits.visibility = View.GONE
        }
    }


    private fun parseFollowUpDatesFromJson(formDataJson: String): List<String> {
        return try {
            val root = JSONObject(formDataJson)
            val fields = root.optJSONObject("fields") ?: return emptyList()
            val followUpArray = fields.optJSONArray("follow_up_visit_date") ?: return emptyList()

            val dates = mutableListOf<String>()
            for (i in 0 until followUpArray.length()) {
                val date = followUpArray.optString(i)
                if (date.isNotBlank()) {
                    dates.add(date)
                }
            }
            dates.sortedWith(compareBy {
                SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(it)
            })
        } catch (e: Exception) {
            Timber.tag("CUFYFormFragment").e(e, " Error parsing follow-up dates from JSON")
            emptyList()
        }
    }


    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as HomeActivity).updateActionBar(
                R.drawable.ic__adolescent,
                getString(R.string.children_under_five_years)
            )
        }
    }
    fun Context.getFileSizeInMB(uri: Uri): Double? {
        return try {
            contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                val sizeInBytes = pfd.statSize

                if (sizeInBytes > 0) {
                    val sizeInMB = sizeInBytes / (1024.0 * 1024.0)
                    sizeInMB
                } else {
                    null
                }
            } ?: run {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}