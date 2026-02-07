package org.piramalswasthya.sakhi.ui.home_activity.village_level_forms.vhnd

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.BuildConfig
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.FormInputAdapter
import org.piramalswasthya.sakhi.databinding.FragmentNewFormBinding
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.ui.common.attachAdapterUnsavedGuard
import org.piramalswasthya.sakhi.ui.common.showDraftRestoreDialog
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import timber.log.Timber
import java.io.File

@AndroidEntryPoint
class VHNDFormFragement : Fragment() {

    companion object {
        fun newInstance() = VHNDFormFragement()
    }

    private var _binding: FragmentNewFormBinding? = null
    private val binding get() = _binding!!

    private var latestTmpUri: Uri? = null
    private var imgValue = 0

    private val viewModel: VHNDViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.recordExists.observe(viewLifecycleOwner) { recordExists ->

            val adapter = FormInputAdapter(
                formValueListener = FormInputAdapter.FormValueListener { formId, index ->
                    viewModel.updateListOnValueChanged(formId, index)
                    hardCodedListUpdate(formId)
                },
                imageClickListener = FormInputAdapter.ImageClickListener {
                    imgValue = it
                    showImagePickerDialog()
                },
                isEnabled = !recordExists
            )

            binding.btnSubmit.isEnabled = !recordExists
            binding.form.rvInputForm.adapter = adapter

            // Attach Unsaved Changes Guard
            attachAdapterUnsavedGuard(
                dirtyState = adapter
            )


            lifecycleScope.launch {
                viewModel.formList.collect { list ->
                    if (list.isNotEmpty()) {
                        adapter.submitList(list)
                    }
                }
            }
        }



        binding.btnSubmit.setOnClickListener {
            if (validateCurrentPage()) {
                viewModel.saveForm()
            }
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                VHNDViewModel.State.SAVE_SUCCESS -> {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.save_successful),
                        Toast.LENGTH_LONG
                    ).show()
                    findNavController().navigateUp()
                    viewModel.resetState()
                }

                VHNDViewModel.State.SAVE_FAILED -> {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.something_wend_wong_contact_testing),
                        Toast.LENGTH_LONG
                    ).show()
                }

                else -> Unit
            }
        }
    }

    override fun onStart() {
        super.onStart()
        (activity as? HomeActivity)?.updateActionBar(
            R.drawable.ic__village_level_form,
            getString(R.string.icon_title_vhnd)
        )
    }

    private fun hardCodedListUpdate(formId: Int) {
        // Reserved for future conditional updates
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery")
        AlertDialog.Builder(requireContext())
            .setTitle("Select Image")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> takeImage()
                    1 -> pickImageFromGallery()
                }
            }
            .show()
    }

    private fun takeImage() {
        lifecycleScope.launch {
            val uri = getTmpFileUri()
            latestTmpUri = uri
            takePicture.launch(uri)
        }
    }

    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                latestTmpUri?.let { uri ->
                    if (isImageSizeValid(uri)) {
                        viewModel.setCurrentImageFormId(imgValue)
                        viewModel.setImageUriToFormElement(uri)
                        (binding.form.rvInputForm.adapter as? FormInputAdapter)
                            ?.notifyItemChanged(if (imgValue == 1) 3 else 4)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Image size should be less than 5 MB",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    Timber.d("Image saved at $uri")
                }
            }
        }

    private fun getTmpFileUri(): Uri {
        val tmpFile = File.createTempFile(
            Konstants.tempBenImagePrefix,
            null,
            requireActivity().cacheDir
        )
        return FileProvider.getUriForFile(
            requireContext(),
            "${BuildConfig.APPLICATION_ID}.provider",
            tmpFile
        )
    }

    private fun pickImageFromGallery() {
        val intent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        pickImageLauncher.launch(intent)
    }

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    if (isImageSizeValid(uri)) {
                        latestTmpUri = uri
                        viewModel.setCurrentImageFormId(imgValue)
                        viewModel.setImageUriToFormElement(uri)
                        (binding.form.rvInputForm.adapter as? FormInputAdapter)
                            ?.notifyItemChanged(if (imgValue == 1) 3 else 4)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Image size should be less than 5 MB",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

    private fun validateCurrentPage(): Boolean {
        val result =
            (binding.form.rvInputForm.adapter as? FormInputAdapter)
                ?.validateInput(resources)

        Timber.d("Validation : $result")

        return if (result == -1) true
        else {
            result?.let { binding.form.rvInputForm.scrollToPosition(it) }
            false
        }
    }

    private fun isImageSizeValid(uri: Uri): Boolean {
        val sizeBytes =
            requireContext().contentResolver.openInputStream(uri)?.available() ?: 0
        val sizeMB = sizeBytes / (1024.0 * 1024.0)
        return sizeMB <= 5
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
