package org.piramalswasthya.sakhi.ui.home_activity.infant.hbnc

import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
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
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import org.piramalswasthya.sakhi.utils.dynamicFiledValidator.FieldValidator
import org.piramalswasthya.sakhi.utils.dynamicFormConstants.FormConstants.HBNC_FORM_ID

@AndroidEntryPoint
class HBNCFormFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var saveButton: Button

    private val dob = "01-07-2024"
    private val args: HBNCFormFragmentArgs by navArgs()


    private val viewModel: HBNCFormViewModel by viewModels()
    var benId=0L
    var hhId= 0L
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
                        currentImageField?.validation?.errorMessage ?: "Image must be less than ${maxSize.toInt()}MB"
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
                        currentImageField?.validation?.errorMessage ?: "Image must be less than ${maxSize.toInt()}MB"
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

        val visitDay = args.visitDay
        val isViewMode = args.isViewMode
         benId=args.benId
         hhId= args.hhId
        val formId = HBNC_FORM_ID
        viewModel.loadFormSchema(benId,formId, visitDay,  true)

        lifecycleScope.launch {
            viewModel.schema.collectLatest { schema ->
                if (schema == null) {
                    return@collectLatest
                }
                val visibleFields = viewModel.getVisibleFields().toMutableList()
                    adapter = FormRendererAdapter(visibleFields, isViewOnly = isViewMode) { field, value ->
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
        val currentSchema = viewModel.schema.value
        val currentVisitDay = viewModel.visitDay

        if (currentSchema == null || currentVisitDay.isBlank()) {
            return
        }

        val updatedFields = adapter.getUpdatedFields()

        currentSchema.sections.orEmpty().forEach { section ->
            section.fields.orEmpty().forEach { schemaField ->
                updatedFields.find { it.fieldId == schemaField.fieldId }?.let { updated ->
                    schemaField.value = updated.value

                    val result = FieldValidator.validate(updated, dob)
                    updated.errorMessage = if (!result.isValid) result.errorMessage else null
                    schemaField.errorMessage = updated.errorMessage

                    if (!result.isValid) {
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
            val matchingSchemaField = currentSchema.sections.orEmpty()
                .flatMap { it.fields.orEmpty() }
                .find { it.fieldId == updated.fieldId }

            updated.copy(errorMessage = matchingSchemaField?.errorMessage)
        }

        adapter.updateFields(copiedFields)
        adapter.notifyDataSetChanged()

        val errorFields = currentSchema.sections.orEmpty().flatMap { section ->
            section.fields.orEmpty().filter { it.visible && !it.errorMessage.isNullOrBlank() }
                .map { "${section.sectionTitle}: ${it.label}" }
        }

        val firstErrorFieldId = currentSchema.sections.orEmpty()
            .flatMap { it.fields.orEmpty() }
            .firstOrNull { it.visible && !it.errorMessage.isNullOrBlank() }
            ?.fieldId

        val errorIndex = copiedFields.indexOfFirst { it.fieldId == firstErrorFieldId }
        if (errorIndex >= 0) {
            recyclerView.scrollToPosition(errorIndex)
        }

        if (errorFields.isNotEmpty()) {
            return
        }

        viewModel.saveFormResponses(benId, hhId )
        findNavController().previousBackStackEntry
            ?.savedStateHandle
            ?.set("form_submitted", true)
        findNavController().popBackStack()
    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as HomeActivity).updateActionBar(
                R.drawable.ic__child,
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
