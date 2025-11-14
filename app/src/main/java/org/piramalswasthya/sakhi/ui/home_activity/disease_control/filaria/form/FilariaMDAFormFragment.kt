package org.piramalswasthya.sakhi.ui.home_activity.disease_control.filaria.form

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.dynamicAdapter.BottleAdapter
import org.piramalswasthya.sakhi.adapters.dynamicAdapter.FormRendererAdapter
import org.piramalswasthya.sakhi.configuration.dynamicDataSet.FormField
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.databinding.FragmentFilariaMDAFormBinding
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import org.piramalswasthya.sakhi.utils.HelperUtil.compressImageToTemp
import org.piramalswasthya.sakhi.utils.HelperUtil.fileToBase64
import org.piramalswasthya.sakhi.utils.HelperUtil.getFileSizeInMB
import org.piramalswasthya.sakhi.utils.HelperUtil.launchCamera
import org.piramalswasthya.sakhi.utils.HelperUtil.launchFilePicker
import org.piramalswasthya.sakhi.utils.HelperUtil.showPickerDialog
import org.piramalswasthya.sakhi.utils.dynamicFiledValidator.FieldValidator
import org.piramalswasthya.sakhi.utils.dynamicFormConstants.FormConstants
import org.piramalswasthya.sakhi.work.dynamicWoker.FilariaMDAFormSyncWorker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.getValue
@AndroidEntryPoint
class FilariaMDAFormFragment : Fragment() {

    @Inject
    lateinit var prefDao: PreferenceDao
    private var _binding: FragmentFilariaMDAFormBinding? = null

    private val binding: FragmentFilariaMDAFormBinding
        get() = _binding!!

    val args: FilariaMDAFormFragmentArgs by lazy {
        FilariaMDAFormFragmentArgs.fromBundle(requireArguments())
    }
    private val viewModel: FilariaMDAFormViewModel by viewModels()
    private lateinit var adapter: FormRendererAdapter
    private var currentImageField: FormField? = null
    private var tempCameraUri: Uri? = null
    private var hhId : Long = 0L
    private var benId : Long = 0L

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && tempCameraUri != null) {
                val context = requireContext()
                val uri = tempCameraUri!!

                val sizeInMB = context.getFileSizeInMB(uri)
                val maxSize = (currentImageField?.validation?.maxSizeMB ?: 5).toDouble()

                if (sizeInMB != null && sizeInMB > maxSize) {
                    currentImageField?.errorMessage =
                        currentImageField?.validation?.errorMessage
                            ?: "Image must be less than ${maxSize.toInt()}MB"
                    adapter.notifyDataSetChanged()
                    return@registerForActivityResult
                }

                val compressedFile = compressImageToTemp(uri, "camera_image", context)
                val base64String = compressedFile?.let { fileToBase64(it) }

                currentImageField?.apply {
                    value = base64String
                    errorMessage = null
                }
                adapter.notifyDataSetChanged()
            }
        }

    private val filePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data ?: return@registerForActivityResult
                val context = requireContext()

                val sizeInMB = context.getFileSizeInMB(uri)
                val maxSize = (currentImageField?.validation?.maxSizeMB ?: 5).toDouble()

                if (sizeInMB != null && sizeInMB > maxSize) {
                    currentImageField?.errorMessage =
                        currentImageField?.validation?.errorMessage
                            ?: "File must be less than ${maxSize.toInt()}MB"
                    adapter.notifyDataSetChanged()
                    return@registerForActivityResult
                }

                val compressedFile = compressImageToTemp(uri, "selected_image", context)
                val base64String = compressedFile?.let { fileToBase64(it) }

                currentImageField?.apply {
                    value = base64String
                    errorMessage = null
                }
                adapter.notifyDataSetChanged()
            }
        }

    private fun showImagePickerDialog() {
        showPickerDialog(
            requireContext(),
            onCameraSelected = {
                tempCameraUri = launchCamera(requireContext())
                cameraLauncher.launch(tempCameraUri)
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

        hhId = args.hhId

        FilariaMDAFormSyncWorker.enqueue(requireContext())

        viewModel.loadFormSchema(benId,  FormConstants.MDA_DISTRIBUTION_FORM_ID, false)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        lifecycleScope.launch {
            viewModel.schema.collectLatest { schema ->
                if (schema == null) {
                    return@collectLatest
                }
                refreshAdapter()
            }
        }

        binding.btnSave.setOnClickListener {
            handleFormSubmission()
        }

        tableRender()
    }

    private fun tableRender() {
        binding.includeBottleTable.tableHeading.visibility = View.GONE
        binding.includeBottleTable.bottleNum.text = "Is Medicine Distributed? (Yes/No)"
        binding.includeBottleTable.date.text = "Distribution Date"

        binding.includeBottleTable.tableRv.layoutManager = LinearLayoutManager(requireContext())
        viewModel.bottleList.observe(viewLifecycleOwner) { list ->

            if (!list.isNullOrEmpty()) {
                binding.includeBottleTable.llTable.visibility = View.VISIBLE
                binding.includeBottleTable.tableRv.adapter = BottleAdapter(list)
            } else {
                binding.includeBottleTable.llTable.visibility = View.GONE
            }
        }
        viewModel.loadBottleData(hhId)
    }

    private fun refreshAdapter(){
        val visibleFields = viewModel.getVisibleFields().toMutableList()
        val minVisitDate = viewModel.getMinVisitDate()
        val maxVisitDate = viewModel.getMaxVisitDate()

        adapter = FormRendererAdapter(
            visibleFields,
            isViewOnly = false,
            minVisitDate = minVisitDate,
            maxVisitDate = maxVisitDate,
            onValueChanged =
                { field, value ->
                    if (value == "pick_image") {
                        currentImageField = field
                        showImagePickerDialog()
                    } else {
                        field.value = value
                        viewModel.updateFieldValue(field.fieldId, value)
                        adapter.updateFields(viewModel.getVisibleFields())
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
                R.drawable.ic__ben,
                getString(R.string.mda_form_title)
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
