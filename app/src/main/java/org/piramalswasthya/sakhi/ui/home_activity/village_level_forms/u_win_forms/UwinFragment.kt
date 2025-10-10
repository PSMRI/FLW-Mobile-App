package org.piramalswasthya.sakhi.ui.home_activity.village_level_forms.u_win_forms

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.BuildConfig
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.FormInputAdapter
import org.piramalswasthya.sakhi.databinding.FragmentUwinBinding
import org.piramalswasthya.sakhi.databinding.LayoutMediaOptionsBinding
import org.piramalswasthya.sakhi.databinding.LayoutViewMediaBinding
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.ui.checkFileSize
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import org.piramalswasthya.sakhi.utils.HelperUtil.createTempImageUri
import org.piramalswasthya.sakhi.utils.HelperUtil.hasUploadedFile
import org.piramalswasthya.sakhi.utils.HelperUtil.isFileTooLarge
import org.piramalswasthya.sakhi.utils.HelperUtil.showImageDialog
import org.piramalswasthya.sakhi.utils.HelperUtil.showMediaOptionsDialog
import org.piramalswasthya.sakhi.utils.HelperUtil.showReminderDialog
import org.piramalswasthya.sakhi.utils.HelperUtil.showToast
import org.piramalswasthya.sakhi.utils.HelperUtil.showUploadReminderDialog
import org.piramalswasthya.sakhi.work.WorkerUtils
import timber.log.Timber
import java.io.File
import kotlin.collections.set
import kotlin.getValue

@AndroidEntryPoint
class UwinFragment : Fragment() {


    private var _binding: FragmentUwinBinding? = null
    private val binding: FragmentUwinBinding
        get() = _binding!!

    private val uwinSummary: MutableMap<Int, Uri?> = mutableMapOf(
        120 to null, 121 to null
    )

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        handleImageResult(viewModel.getDocumentFormId(), uri)
    }

    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) handleCameraResult()
        }

    private val viewModel: UwinViewModel by viewModels()
    private var uwinId: Int = 0


    private val formIdToIndex: Map<Int, () -> Int> by lazy {
        mapOf(
            120 to { viewModel.getIndexUWINSummary1() },
            121 to { viewModel.getIndexUWINSummary2() },

            )
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUwinBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapter()
        observeFormList()
        observeState()
        setupSubmitButton()

        uwinId = arguments?.getInt("uwinId") ?: 0
        viewModel.prepareForm(saved = uwinId != 0, id = uwinId)
        binding.btnSubmit.apply {
            visibility = if (uwinId == 0) View.VISIBLE else View.GONE
        }
    }

    //region Setup
    private fun setupAdapter() {
        binding.form.rvInputForm.adapter = FormInputAdapter(
            selectImageClickListener = FormInputAdapter.SelectUploadImageClickListener { formId ->
                viewModel.setCurrentDocumentFormId(formId)
                requireContext().showMediaOptionsDialog(
                    onCameraClick = { takeImage() },
                    onGalleryClick = { selectImage() }
                )
            },
            formValueListener = FormInputAdapter.FormValueListener { formId, index ->
                viewModel.updateListOnValueChanged(formId, index)
            },
            viewDocumentListner = FormInputAdapter.ViewDocumentOnClick { formId ->
                handleViewDocument(formId)
            },
            isEnabled = true
        )
    }

    private fun observeFormList() {
        lifecycleScope.launchWhenStarted {
            viewModel.formList.collect { list ->
                (binding.form.rvInputForm.adapter as? FormInputAdapter)?.submitList(list)
            }
        }
    }

    private fun observeState() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                UwinViewModel.State.SAVING -> binding.pbForm.visibility = View.VISIBLE
                UwinViewModel.State.SAVE_SUCCESS -> {
                    binding.pbForm.visibility = View.GONE
                    WorkerUtils.triggerAmritPushWorker(requireContext())
                    context?.showToast(getString(R.string.save_successful))
                    findNavController().navigateUp()
                }
                UwinViewModel.State.SAVE_FAILED -> binding.pbForm.visibility = View.GONE
                else -> Unit
            }
        }
    }

    private fun setupSubmitButton() {
        binding.btnSubmit.visibility = if (uwinId == 0) View.VISIBLE else View.GONE
        binding.btnSubmit.setOnClickListener {
            if (!uwinSummary.hasUploadedFile()) {
                requireContext().showUploadReminderDialog(getString(R.string.do_you_want_to_upload_uwin_form_immunization_page_section_photo_copy_to_claim_your_incentive)) { saveForm() }
            } else saveForm()
        }
    }
    //endregion

    //region Image Handling
    private fun takeImage() {
        lifecycleScope.launchWhenStarted {
            val uri = requireContext().createTempImageUri()
            uwinSummary[viewModel.getDocumentFormId()] = uri
            takePictureLauncher.launch(uri)
        }
    }

    private fun selectImage() {
        pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun handleImageResult(formId: Int, uri: Uri?) {
        uri?.let {
            if (checkFileSize(it, requireContext())) {
                Toast.makeText(context, resources.getString(R.string.file_size), Toast.LENGTH_LONG).show()
            } else updateAdapterForFormId(formId, it)
        }
    }

    private fun handleCameraResult() {
        val formId = viewModel.getDocumentFormId()
        uwinSummary[formId]?.let {uri ->
        if (checkFileSize(uri, requireContext())) {
            Toast.makeText(context, resources.getString(R.string.file_size), Toast.LENGTH_LONG).show()
            uwinSummary[formId] = null
        } else {
            updateAdapterForFormId(formId, uri)
        }
    }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                if (requireContext().isFileTooLarge(uri)) {
                    context?.showToast(getString(R.string.file_size))
                } else updateAdapterForFormId(viewModel.getDocumentFormId(), uri)
            }
        }
    }

    private fun updateAdapterForFormId(formId: Int, uri: Uri) {
        uwinSummary[formId] = uri
        viewModel.setImageUriToFormElement(uri)
        binding.form.rvInputForm.adapter?.notifyDataSetChanged()
    }
    //endregion

    private fun handleViewDocument(formId: Int) {
        if (uwinId != 0) {
            formIdToIndex[formId]?.invoke()?.let { index ->
                lifecycleScope.launch {
                    viewModel.formList.collect { list ->
                        list.getOrNull(index)?.value?.toUri()?.let { requireContext().showImageDialog(it) }
                    }
                }
            }
        } else uwinSummary[formId]?.let { requireContext().showImageDialog(it) }
    }

    private fun saveForm() {
        val adapter = binding.form.rvInputForm.adapter as? FormInputAdapter ?: return
        val validationResult = adapter.validateInput(resources)

        if (validationResult == -1) {
            viewModel.saveForm()
        } else {
            binding.form.rvInputForm.scrollToPosition(validationResult)
        }
    }



    override fun onStart() {
        super.onStart()
        (activity as? HomeActivity)?.updateActionBar(
            R.drawable.ic__village_level_form,
            getString(R.string.u_win_session)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val PICK_IMAGE = 1
    }
}