package org.piramalswasthya.sakhi.ui.home_activity.village_level_forms.filaria_mda

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.dynamicAdapter.FormRendererAdapter
import org.piramalswasthya.sakhi.configuration.dynamicDataSet.FormField
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.databinding.FragmentFilariaMDAFormBinding
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import org.piramalswasthya.sakhi.utils.HelperUtil
import org.piramalswasthya.sakhi.utils.HelperUtil.getFileSizeInMB
import org.piramalswasthya.sakhi.utils.HelperUtil.launchCamera
import org.piramalswasthya.sakhi.utils.HelperUtil.launchFilePicker
import org.piramalswasthya.sakhi.utils.HelperUtil.showPickerDialog
import org.piramalswasthya.sakhi.utils.dynamicFiledValidator.FieldValidator
import org.piramalswasthya.sakhi.utils.dynamicFormConstants.FormConstants
import org.piramalswasthya.sakhi.utils.dynamicFormConstants.FormConstants.LF_MDA_CAMPAIGN
import org.piramalswasthya.sakhi.work.dynamicWoker.FilariaMDAFormSyncWorker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.getValue

@AndroidEntryPoint
class FilariaMdaCampaignFormFragment : Fragment() {

    @Inject
    lateinit var prefDao: PreferenceDao
    private var _binding: FragmentFilariaMDAFormBinding? = null

    private val binding: FragmentFilariaMDAFormBinding
        get() = _binding!!


    private val viewModel: FilariaMdaFormCampaignViewModel by viewModels()
    private lateinit var adapter: FormRendererAdapter
    private var currentImageField: FormField? = null
    private var tempCameraUri: Uri? = null
    private var hhId : Long = 0L
    private var benId : Long = 0L

    private var campaignPhotosList = mutableListOf<String>()

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && tempCameraUri != null) {
                handleImageCapture(tempCameraUri!!)
            }
        }

    private val filePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data ?: return@registerForActivityResult
                handleImageSelection(uri)
            }
        }

    private fun handleImageCapture(uri: Uri) {
        val context = requireContext()
        val sizeInMB = context.getFileSizeInMB(uri)
        val maxSize = (currentImageField?.validation?.maxSizeMB ?: 5).toDouble()

        if (sizeInMB != null && sizeInMB > maxSize) {
            currentImageField?.errorMessage =
                currentImageField?.validation?.errorMessage
                    ?: "Image must be less than ${maxSize.toInt()}MB"
            adapter.notifyDataSetChanged()
            return
        }

        val compressedFile = org.piramalswasthya.sakhi.utils.HelperUtil.compressImageToTemp(uri, "camera_image", context)
        val base64String = compressedFile?.let { org.piramalswasthya.sakhi.utils.HelperUtil.fileToBase64(it) }

        if (currentImageField?.fieldId == "mda_photos") {
            if (base64String != null && campaignPhotosList.size < 2) {
                campaignPhotosList.add(base64String)
                currentImageField?.value = campaignPhotosList.toList()
                currentImageField?.errorMessage = null
                viewModel.updateFieldValue(currentImageField!!.fieldId, campaignPhotosList.toList())
            }
        } else {
            currentImageField?.apply {
                value = base64String
                errorMessage = null
                viewModel.updateFieldValue(fieldId, value)
            }
        }
        adapter.updateFields(viewModel.getVisibleFields())
        adapter.notifyDataSetChanged()
    }

    private fun handleImageSelection(uri: Uri) {
        val context = requireContext()
        val sizeInMB = context.getFileSizeInMB(uri)
        val maxSize = (currentImageField?.validation?.maxSizeMB ?: 5).toDouble()

        if (sizeInMB != null && sizeInMB > maxSize) {
            currentImageField?.errorMessage =
                currentImageField?.validation?.errorMessage
                    ?: "File must be less than ${maxSize.toInt()}MB"
            adapter.notifyDataSetChanged()
            return
        }

        val compressedFile = org.piramalswasthya.sakhi.utils.HelperUtil.compressImageToTemp(uri, "selected_image", context)
        val base64String = compressedFile?.let { org.piramalswasthya.sakhi.utils.HelperUtil.fileToBase64(it) }

        if (currentImageField?.fieldId == "mda_photos") {
            if (base64String != null && campaignPhotosList.size < 2) {
                campaignPhotosList.add(base64String)
                currentImageField?.value = campaignPhotosList.toList()
                currentImageField?.errorMessage = null
                viewModel.updateFieldValue(currentImageField!!.fieldId, campaignPhotosList.toList())
            }
        } else {
            currentImageField?.apply {
                value = base64String
                errorMessage = null
                viewModel.updateFieldValue(fieldId, value)
            }
        }
        adapter.updateFields(viewModel.getVisibleFields())
        adapter.notifyDataSetChanged()
    }

    private fun showImagePickerDialog() {
        showPickerDialog(
            requireContext(),
            onCameraSelected = {
                tempCameraUri = launchCamera(requireContext())
                tempCameraUri?.let { cameraLauncher.launch(it) }
                ?: Toast.makeText(requireContext(), "Unable to open camera", Toast.LENGTH_SHORT).show()
            },
            onFileSelected = {
                launchFilePicker(filePickerLauncher)
            }
        )
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFilariaMDAFormBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        FilariaMDAFormSyncWorker.enqueue(requireContext())

        if (viewModel.yearDate.isEmpty()) {
            binding.btnSave.visibility = View.VISIBLE
            viewModel.loadFormSchema( FormConstants.LF_MDA_CAMPAIGN, false)

        } else {
            binding.btnSave.visibility = View.GONE
            viewModel.loadFormSchema( FormConstants.LF_MDA_CAMPAIGN, true)

        }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        lifecycleScope.launch {
            viewModel.schema.collectLatest { schema ->
                if (schema == null) {
                    return@collectLatest
                }
                refreshAdapter()
            }
        }

        binding.mdaTitle.visibility = View.VISIBLE
        binding.btnSave.setOnClickListener {
            handleFormSubmission()
        }

    }


    private fun refreshAdapter(){
        val visibleFields = viewModel.getVisibleFields().toMutableList()
        val minVisitDate = HelperUtil.getMinVisitDate()
        val maxVisitDate = HelperUtil.getMaxVisitDate()

        val campaignPhotosField = visibleFields.find {
            it.fieldId == "mda_photos"
        }
        if (campaignPhotosField != null) {
            when {
                campaignPhotosField.value is List<*> -> {
                    campaignPhotosList = (campaignPhotosField.value as List<*>)
                        .mapNotNull { it?.toString() }
                        .toMutableList()
                }
                campaignPhotosField.value is String -> {
                    try {
                        val photos = Gson().fromJson(
                            campaignPhotosField.value as String,
                            Array<String>::class.java
                        )
                        campaignPhotosList = photos.toMutableList()
                    } catch (e: Exception) {
                        campaignPhotosList = mutableListOf()
                    }
                }
                else -> {
                    campaignPhotosList = mutableListOf()
                }
            }
        }

        adapter = FormRendererAdapter(
            visibleFields,
            isViewOnly = viewModel.isViewMode,
            minVisitDate = minVisitDate,
            maxVisitDate = maxVisitDate,
            formId = LF_MDA_CAMPAIGN,
            onValueChanged =
                { field, value ->
                    if (value == "pick_image") {
                        currentImageField = field
                        showImagePickerDialog()

                    } else {
                        field.value = value
                        if ((field.fieldId == "mda_photos") && value is List<*>) {
                            campaignPhotosList = value.filterIsInstance<String>().toMutableList()
                        }
                        viewModel.updateFieldValue(field.fieldId, value)
                        val updatedVisibleFields = viewModel.getVisibleFields()
                        adapter.updateFields(updatedVisibleFields)
                    }
                },)

        binding.recyclerView.adapter = adapter
    }

    private fun handleFormSubmission() {
        val currentSchema = viewModel.schema.value ?: return
        val previousVisitDate = viewModel.previousVisitDate

        val updatedFields = adapter.getUpdatedFields()
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val today = Date()

        currentSchema.sections.forEach { section ->
            section.fields.forEach { schemaField ->
                updatedFields.find { it.fieldId == schemaField.fieldId }?.let { updated ->
                    schemaField.value = updated.value

                    val result = FieldValidator.validate(updated, null)
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
                            visitDate == null -> "Invalid visit date"
                            today != null && visitDate.after(today) -> "Visit Date cannot be after today's date"
                            previousVisitDate != null && !visitDate.after(previousVisitDate) ->
                                "Visit Date must be after previous visit (${sdf.format(previousVisitDate)})"
                            else -> null
                        }
                        schemaField.errorMessage = errorMessage
                        updated.errorMessage = errorMessage
                    }
                }
            }
        }

        updatedFields.forEach { adapterField ->
            currentSchema.sections.flatMap { it.fields }
                .find { it.fieldId == adapterField.fieldId }
                ?.let { schemaField ->
                    adapterField.errorMessage = schemaField.errorMessage
                }
        }

        val copiedFields = updatedFields.map { updated ->
            val error = currentSchema.sections
                .flatMap { it.fields }
                .find { it.fieldId == updated.fieldId }
                ?.errorMessage
            updated.copy(errorMessage = error)
        }
        adapter.updateFields(copiedFields)
        adapter.notifyDataSetChanged()

        val firstErrorFieldId = currentSchema.sections
            .flatMap { it.fields }
            .firstOrNull { it.visible && !it.errorMessage.isNullOrBlank() }
            ?.fieldId

        val errorIndex = copiedFields.indexOfFirst { it.fieldId == firstErrorFieldId }
        if (errorIndex >= 0) binding.recyclerView.scrollToPosition(errorIndex)

        val hasErrors = currentSchema.sections.any { section ->
            section.fields.any { it.visible && !it.errorMessage.isNullOrBlank() }
        }
        if (hasErrors) return

        viewLifecycleOwner.lifecycleScope.launch {
            val isSaved = viewModel.saveFormResponses(benId, hhId)

            if (!isSaved && viewModel.wasDuplicate) {
                return@launch
            }
            if (isSaved) {
                Toast.makeText(requireContext(), getString(R.string.data_saved_successfully), Toast.LENGTH_SHORT).show()
                findNavController().previousBackStackEntry?.savedStateHandle?.set("form_submitted", true)
                findNavController().popBackStack()
            } else {
                Toast.makeText(requireContext(), getString(R.string.failed_to_submit_form), Toast.LENGTH_SHORT).show()
            }
        }


        viewModel.showToastLiveData.observe(viewLifecycleOwner) { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as HomeActivity).updateActionBar(
                R.drawable.filaria,
                getString(R.string.mda_title)
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}