package org.piramalswasthya.sakhi.ui.home_activity.infant.hbnc

import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.dynamicAdapter.FormRendererAdapter
import org.piramalswasthya.sakhi.configuration.dynamicDataSet.FormField
import org.piramalswasthya.sakhi.databinding.RvItemBenChildCareInfantBinding
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import org.piramalswasthya.sakhi.ui.home_activity.child_care.infant_list.InfantListViewModel
import org.piramalswasthya.sakhi.ui.setSyncState
import org.piramalswasthya.sakhi.utils.dynamicFiledValidator.FieldValidator
import org.piramalswasthya.sakhi.utils.dynamicFormConstants.FormConstants.HBNC_FORM_ID
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class HBNCFormFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var saveButton: Button

    val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

    //    val dob = dateFormat.format(Date())
    private val args: HBNCFormFragmentArgs by navArgs()

    private val infantListViewModel: InfantListViewModel by viewModels()
    private val viewModel: HBNCFormViewModel by viewModels()
    var benId = -1L
    var hhId = -1L
    var dob = -1L
    private lateinit var adapter: FormRendererAdapter
    private var currentImageField: FormField? = null
    private var tempCameraUri: Uri? = null

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
    ): View = inflater.inflate(R.layout.fragment_hbnc_form, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.recyclerView)
        saveButton = view.findViewById(R.id.btnSave)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val container = view.findViewById<FrameLayout>(R.id.infant_card_container)
        val infantBinding =
            RvItemBenChildCareInfantBinding.inflate(layoutInflater, container, false)
        container.addView(infantBinding.root)


        val visitDay = args.visitDay
        val isViewMode = args.isViewMode
        benId = args.benId
        hhId = args.hhId

        infantListViewModel.getBenById(benId) { ben ->
            infantBinding.btnHBNC.visibility=View.GONE
            infantBinding.dueIcon.visibility=View.GONE
            ben?.syncState = null
            infantBinding.ben = ben

        }
        viewModel.loadVisitDates(benId)

        infantListViewModel.getDobByBenIdAsync(benId) { dobMillis ->
            if (dobMillis != null) {
                dob = dobMillis
                viewModel.loadFormSchema(benId, HBNC_FORM_ID, visitDay, true, dob)
            } else {
                viewModel.loadFormSchema(benId, HBNC_FORM_ID, visitDay, true, dob)
            }
        }

        lifecycleScope.launch {
            viewModel.schema.collectLatest { schema ->
                if (schema == null) {
                    return@collectLatest
                }

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

                recyclerView.adapter = adapter
                saveButton.visibility = if (isViewMode) View.GONE else View.VISIBLE
            }
        }

        saveButton.setOnClickListener {
            handleFormSubmission()
        }
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
                            minVisitDate != null && visitDate.before(minVisitDate) ->
                                "Visit Date should be on or after due date (${
                                    sdf.format(
                                        minVisitDate
                                    )
                                })"

                            previousVisitDate != null && !visitDate.after(previousVisitDate) ->
                                "Visit Date must be after previous visit (${
                                    sdf.format(
                                        previousVisitDate
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
        if (errorIndex >= 0) recyclerView.scrollToPosition(errorIndex)

        val hasErrors = currentSchema.sections.orEmpty().any { section ->
            section.fields.orEmpty().any { it.visible && !it.errorMessage.isNullOrBlank() }
        }
        if (hasErrors) return
        viewModel.saveFormResponses(benId, hhId)
        findNavController().previousBackStackEntry?.savedStateHandle?.set("form_submitted", true)
        findNavController().popBackStack()
    }
    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as HomeActivity).updateActionBar(
                R.drawable.ic__infant,
                getString(R.string.hbnc_form)
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
