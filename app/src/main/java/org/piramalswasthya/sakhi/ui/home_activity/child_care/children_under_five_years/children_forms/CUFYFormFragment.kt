package org.piramalswasthya.sakhi.ui.home_activity.child_care.children_under_five_years.children_forms

import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
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
    private lateinit var adapter: FormRendererAdapter
    private var currentImageField: FormField? = null
    private var tempCameraUri: Uri? = null
    private var _binding: FragmentChildrenUnderFiveFormBinding? = null
    private val binding get() = _binding!!

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
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

        Log.i("ChildrenUnderFiveFormFragmentOne", "onViewCreated: $visitType == $isViewMode == $benId == $hhId")

        binding.fabEdit.isVisible = isViewMode

        binding.fabEdit.setOnClickListener {
            isViewMode = false
            binding.fabEdit.isGone = true
            binding.btnSave.isVisible = true
            refreshAdapter()
        }

        if (visitType.equals("ORS")){
            formId = FormConstants.CHILDREN_UNDER_FIVE_ORS_FORM_ID;
        }else if (visitType.equals("IFA")){
            formId = FormConstants.CHILDREN_UNDER_FIVE_IFA_FORM_ID;
        }else if (visitType.equals("Check SAM")){
            formId = FormConstants.CHILDREN_UNDER_FIVE_SAM_FORM_ID;
        }

        viewModel.loadVisitDates(benId)

        infantListViewModel.getBenById(benId) { ben ->
            infantBinding.btnHBNC.visibility = View.GONE
            infantBinding.dueIcon.visibility = View.GONE
            ben?.syncState = null
            infantBinding.ben = ben
        }

        infantListViewModel.getDobByBenIdAsync(benId) { dob ->
            viewModel.loadFormSchema(benId, formId, visitType!!, true)
        }

        lifecycleScope.launch {
            viewModel.schema.collectLatest { schema ->
                if (schema == null) return@collectLatest
                refreshAdapter()
            }
        }

        binding.btnSave.setOnClickListener {
            handleFormSubmission()
        }
    }

    private fun refreshAdapter() {
        val visibleFields = viewModel.getVisibleFields().toMutableList()
        val minVisitDate = viewModel.getMinVisitDate()
        val maxVisitDate = viewModel.getMaxVisitDate()

        adapter = FormRendererAdapter(
            visibleFields,
            isViewOnly = isViewMode,
            minVisitDate = minVisitDate,
            maxVisitDate = maxVisitDate
        ) { field, value ->
            if (value == "pick_image") {
                currentImageField = field
                showImagePickerDialog()
            } else {
                field.value = value
                viewModel.updateFieldValue(field.fieldId, value)
                val updatedVisibleFields = viewModel.getVisibleFields()
                adapter.updateFields(updatedVisibleFields)
            }
        }

        binding.recyclerView.adapter = adapter
        binding.btnSave.isVisible = !isViewMode
        binding.fabEdit.isVisible = isViewMode
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery")

        AlertDialog.Builder(requireContext())
            .setTitle("Select Image")
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
        val dobString =
            SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date(deliveryDate))

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

                        val minVisitDate = viewModel.calculateDueDate(deliveryDate, currentVisitDay)
                            ?.let { Date(it) }

                        val errorMessage = when {
                            visitDate == null -> "Invalid visit date"
                            today != null && visitDate.after(today) -> "Visit Date cannot be after today's date"
                            deliveryDate == null -> "Delivery date is missing"
                            visitDate.before(Date(deliveryDate)) -> "Visit Date cannot be before delivery date"
                            previousVisitDate != null && !visitDate.after(previousVisitDate) ->
                                "Visit Date must be after previous visit (${
                                    sdf.format(
                                        previousVisitDate
                                    )
                                })"
                            minVisitDate != null && visitDate.before(minVisitDate) ->
                                "Visit Date should be on or after due date (${
                                    sdf.format(
                                        minVisitDate
                                    )
                                })"



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
        lifecycleScope.launch {
            viewModel.saveFormResponses(benId, hhId)
            findNavController().previousBackStackEntry?.savedStateHandle?.set("form_submitted", true)
            findNavController().popBackStack()
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