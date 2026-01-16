package org.piramalswasthya.sakhi.ui.home_activity.village_level_forms.ors_campaign

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
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.dynamicAdapter.FormRendererAdapter
import org.piramalswasthya.sakhi.configuration.dynamicDataSet.FormField
import org.piramalswasthya.sakhi.databinding.FragmentOrsCampaignFormBinding
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import org.piramalswasthya.sakhi.utils.HelperUtil.getFileSizeInMB
import org.piramalswasthya.sakhi.utils.HelperUtil.launchCamera
import org.piramalswasthya.sakhi.utils.HelperUtil.launchFilePicker
import org.piramalswasthya.sakhi.utils.HelperUtil.showPickerDialog
import org.piramalswasthya.sakhi.utils.dynamicFiledValidator.FieldValidator

@AndroidEntryPoint
class ORSCampaignFormFragment : Fragment() {

    private var _binding: FragmentOrsCampaignFormBinding? = null
    private val binding: FragmentOrsCampaignFormBinding
        get() = _binding!!

    private val viewModel: ORSCampaignViewModel by viewModels()
    private lateinit var adapter: FormRendererAdapter
    private var currentImageField: FormField? = null
    private var tempCameraUri: Uri? = null
    private var campaignPhotosList = mutableListOf<String>() // Store up to 2 image URIs

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

        if (currentImageField?.fieldId == "campaign_photos" || currentImageField?.fieldId == "campaignPhotos") {
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

        if (currentImageField?.fieldId == "campaign_photos" || currentImageField?.fieldId == "campaignPhotos") {
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
        _binding = FragmentOrsCampaignFormBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
    }

    private fun refreshAdapter() {
        val visibleFields = viewModel.getVisibleFields().toMutableList()

        val campaignPhotosField = visibleFields.find {
            it.fieldId == "campaign_photos" || it.fieldId == "campaignPhotos" 
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
                        val photos = com.google.gson.Gson().fromJson(
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
            isViewOnly = false,
            minVisitDate = null,
            maxVisitDate = null,
            onValueChanged = { field, value ->
                if (value == "pick_image") {
                    currentImageField = field
                    if (field.fieldId == "campaign_photos" || field.fieldId == "campaignPhotos") {
                        if (campaignPhotosList.size >= 2) {
                            Toast.makeText(
                                requireContext(),
                                "Maximum 2 photos allowed",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            showImagePickerDialog()
                        }
                    } else {
                        showImagePickerDialog()
                    }
                } else {
                    field.value = value
                    viewModel.updateFieldValue(field.fieldId, value)
                    val updatedVisibleFields = viewModel.getVisibleFields()
                    adapter.updateFields(updatedVisibleFields)
                }
            },
            onShowAlert = null
        )

        binding.recyclerView.adapter = adapter
    }

    private fun handleFormSubmission() {
        val visibleFields = viewModel.getVisibleFields()

        val hasErrors = visibleFields.any { field ->
            val result = FieldValidator.validate(field, null)
            if (!result.isValid) {
                field.errorMessage = result.errorMessage
                true
            } else {
                field.errorMessage = null
                false
            }
        }

        if (hasErrors) {
            adapter.updateFields(visibleFields)
            val firstErrorIndex = visibleFields.indexOfFirst { it.errorMessage != null }
            if (firstErrorIndex >= 0) {
                binding.recyclerView.scrollToPosition(firstErrorIndex)
            }
            Toast.makeText(requireContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            viewModel.saveForm()
        }
    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as HomeActivity).updateActionBar(
                R.drawable.ic__village_level_form,
                getString(R.string.ors_distribution_campaign)
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
