package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.child_reg.form

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
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
import org.piramalswasthya.sakhi.adapters.FormInputAdapterWithBgIcon
import org.piramalswasthya.sakhi.ui.common.attachAdapterUnsavedGuard
import org.piramalswasthya.sakhi.databinding.FragmentNewFormBinding
import org.piramalswasthya.sakhi.databinding.LayoutMediaOptionsBinding
import org.piramalswasthya.sakhi.databinding.LayoutViewMediaBinding
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.ui.checkFileSize
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import org.piramalswasthya.sakhi.work.WorkerUtils
import timber.log.Timber
import java.io.File

@AndroidEntryPoint
class ChildRegFragment : Fragment() {

    private var _binding: FragmentNewFormBinding? = null
    private val binding: FragmentNewFormBinding
        get() = _binding!!

    private val viewModel: ChildRegViewModel by viewModels()

    companion object {
        var latestTmpUri: Uri? = null
        var backViewFileUri: Uri? = null
    }

    private val PICK_PDF_FILE = 1
    @SuppressLint("NotifyDataSetChanged")
    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            if (success) {

                if(viewModel.getDocumentFormId() == 14) {
                    latestTmpUri?.let { uri ->
                        if (checkFileSize(uri,requireContext())) {
                            Toast.makeText(context, resources.getString(R.string.file_size), Toast.LENGTH_LONG).show()

                        } else {
                            viewModel.setImageUriToFormElement(uri)
                            binding.form.rvInputForm.apply {
                                val adapter = this.adapter as FormInputAdapter
                                adapter.notifyDataSetChanged()
                            }
                        }

                    }
                } else {
                    backViewFileUri?.let { uri ->
                        if (checkFileSize(uri,requireContext())) {
                            Toast.makeText(context, resources.getString(R.string.file_size), Toast.LENGTH_LONG).show()

                        } else {
                            viewModel.setImageUriToFormElement(uri)
                            binding.form.rvInputForm.apply {
                                val adapter = this.adapter as FormInputAdapter
                                adapter.notifyDataSetChanged()
                            }
                        }

                    }
                }


            }
        }

    @SuppressLint("NotifyDataSetChanged")
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_PDF_FILE && resultCode == Activity.RESULT_OK) {
            if (viewModel.getDocumentFormId() == 14) {
                data?.data?.let { pdfUri ->
                    latestTmpUri = pdfUri
                    if (checkFileSize(pdfUri,requireContext())) {
                        Toast.makeText(context, resources.getString(R.string.file_size), Toast.LENGTH_LONG).show()

                    } else {
                        latestTmpUri?.let { uri ->
                            viewModel.setImageUriToFormElement(uri)
                            binding.form.rvInputForm.apply {
                                val adapter = this.adapter as FormInputAdapter
                                adapter.notifyDataSetChanged()
                            }
                        }
                        Timber.d("Image saved at @ $latestTmpUri")
                    }
                }
            } else {
                data?.data?.let { pdfUri ->
                    backViewFileUri = pdfUri
                    if (checkFileSize(pdfUri,requireContext())) {
                        Toast.makeText(context, resources.getString(R.string.file_size), Toast.LENGTH_LONG).show()

                    } else {
                        backViewFileUri?.let { uri ->
                            viewModel.setImageUriToFormElement(uri)
                            binding.form.rvInputForm.apply {
                                val adapter = this.adapter as FormInputAdapter
                                adapter.notifyDataSetChanged()
                            }
                        }
                        Timber.d("Image saved at @ $backViewFileUri")
                    }
                }
            }

        }
    }

    private fun displayPdf(pdfUri: Uri) {

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(pdfUri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Grant permission to read the file
        }
        startActivity(Intent.createChooser(intent, "Open PDF with"))

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.cvPatientInformation.visibility = View.GONE
        viewModel.recordExists.observe(viewLifecycleOwner) { notIt ->
            notIt?.let { recordExists ->
                val adapter = FormInputAdapter(
                    formValueListener = FormInputAdapter.FormValueListener { formId, index ->
                        viewModel.updateListOnValueChanged(formId, index)
                    }, isEnabled = !recordExists,
                    selectImageClickListener  = FormInputAdapter.SelectUploadImageClickListener {
                        viewModel.setCurrentDocumentFormId(it)
                        chooseOptions()
                    },
                    viewDocumentListner = FormInputAdapter.ViewDocumentOnClick {
                        if (!recordExists) {
                            if (it == 14) {
                                latestTmpUri?.let {
                                    if (it.toString().contains("document")) {
                                        displayPdf(it)
                                    } else {
                                        viewImage(it)
                                    }

                                }
                            } else {
                                backViewFileUri?.let {
                                    if (it.toString().contains("document")) {
                                        displayPdf(it)
                                    } else {
                                        viewImage(it)
                                    }

                                }
                            }

                        } else {
                            val formId = it
                            lifecycleScope.launch {
                                viewModel.formList.collect{ it ->
                                    if (formId == 14) {
                                        it.get(viewModel.getIndexOfBirthCertificateFront()).value.let {
                                            if (it.toString().contains("document")) {
                                                displayPdf(it!!.toUri())
                                            } else {
                                                viewImage(it!!.toUri())
                                            }
                                        }
                                    } else {
                                        it.get(viewModel.getIndexOfBirthCertificateBack()).value.let {
                                            if (it.toString().contains("document")) {
                                                displayPdf(it!!.toUri())
                                            } else {
                                                viewImage(it!!.toUri())
                                            }
                                        }
                                    }

                                }
                            }

                        }
                    }
                )
                binding.btnSubmit.isEnabled = !recordExists
                binding.form.rvInputForm.adapter = adapter
                attachAdapterUnsavedGuard(adapter as FormInputAdapterWithBgIcon)
                lifecycleScope.launch {
                    viewModel.formList.collect {
                        if (it.isNotEmpty())
                            adapter.submitList(it)

                    }
                }
            }
        }
        binding.btnSubmit.setOnClickListener {
            submitInfantRegForm()
        }

        viewModel.state.observe(viewLifecycleOwner) {
            when (it) {
                ChildRegViewModel.State.SAVE_SUCCESS -> {
                    WorkerUtils.triggerAmritPushWorker(requireContext())
                    findNavController().navigateUp()
                }

                else -> {}
            }
        }
    }

    private fun submitInfantRegForm() {
        if (validateCurrentPage()) {
            viewModel.saveForm()
        }
    }

    private fun chooseOptions() {
        val alertBinding = LayoutMediaOptionsBinding.inflate(layoutInflater, binding.root, false)
        val alertDialog = MaterialAlertDialogBuilder(requireContext())
            .setView(alertBinding.root)
            .setCancelable(true)
            .create()
        alertBinding.btnPdf.setOnClickListener {
            alertDialog.dismiss()
            selectPdf()
        }
        alertBinding.btnCamera.setOnClickListener {
            alertDialog.dismiss()
            takeImage()
        }
        alertBinding.btnGallery.setOnClickListener {
            alertDialog.dismiss()
            selectImage()
        }
        alertBinding.btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }
        alertDialog.show()
    }

    private fun takeImage() {
        lifecycleScope.launchWhenStarted {
            getTmpFileUri().let { uri ->
                if (viewModel.getDocumentFormId() == 14) {
                    latestTmpUri = uri
                    if (checkFileSize(uri,requireContext())) {
                        Toast.makeText(context, resources.getString(R.string.file_size), Toast.LENGTH_LONG).show()

                    } else {
                        takePicture.launch(latestTmpUri)
                    }
                } else {
                    backViewFileUri = uri
                    if (checkFileSize(uri,requireContext())) {
                        Toast.makeText(context, resources.getString(R.string.file_size), Toast.LENGTH_LONG).show()

                    } else {
                        takePicture.launch(backViewFileUri)
                    }
                }


            }
        }
    }

    private fun getTmpFileUri(): Uri {
        val tmpFile =
            File.createTempFile(Konstants.tempBenImagePrefix, null, requireActivity().cacheDir)
                .apply {
                    createNewFile()
//                deleteOnExit()
            }
        return FileProvider.getUriForFile(
            requireContext(),
            "${BuildConfig.APPLICATION_ID}.provider",
            tmpFile
        )
    }

    private fun selectPdf() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
        }
        startActivityForResult(intent, PICK_PDF_FILE)
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        startActivityForResult(intent, PICK_PDF_FILE)
    }
    private fun validateCurrentPage(): Boolean {
        val result = binding.form.rvInputForm.adapter?.let {
            (it as FormInputAdapter).validateInput(resources)
        }
        Timber.d("Validation : $result")
        return if (result == -1) true
        else {
            if (result != null) {
                binding.form.rvInputForm.scrollToPosition(result)
            }
            false
        }
    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as HomeActivity).updateActionBar(
                R.drawable.ic__child_registration,
                getString(R.string.child_reg)
            )
        }
    }
    private fun viewImage(imageUri: Uri) {
        val viewImageBinding = LayoutViewMediaBinding.inflate(layoutInflater, binding.root, false)
        val alertDialog = MaterialAlertDialogBuilder(requireContext())
            .setView(viewImageBinding.root)
            .setCancelable(true)
            .create()
        Glide.with(this).load(Uri.parse(imageUri.toString())).placeholder(R.drawable.ic_person)
            .into(viewImageBinding.viewImage)
        viewImageBinding.btnClose.setOnClickListener {
            alertDialog.dismiss()
        }
        alertDialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}