package org.piramalswasthya.sakhi.ui.home_activity.death_reports.cdr

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
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
class CdrObjectFragment : Fragment() {

    private var _binding: FragmentNewFormBinding? = null
    private val binding: FragmentNewFormBinding
        get() = _binding!!

    private val requestLocationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { b ->
            if (b) {
                requestLocationPermission()
            } else findNavController().navigateUp()
        }
    private var latestTmpUri: Uri? = null
    private var cdr1Uri: Uri? = null
    private var cdr2Uri: Uri? = null
    private var isDeathUri: Uri? = null

    private val PICK_PDF_FILE = 1
    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            if (success) {
                val formId = viewModel.getDocumentFormId()

                val uri = when (formId) {
                    21 -> cdr1Uri
                    22 -> cdr2Uri
                    23 -> isDeathUri
                    else -> latestTmpUri
                }

                uri?.let {
                    viewModel.setImageUriToFormElement(it)

                    val index = when (formId) {
                        21 -> viewModel.getIndexOfCDR1()
                        22 -> viewModel.getIndexOfCDR2()
                        23 -> viewModel.getIndexOfIsDeathCertificate()
                        else -> null
                    }
                    index?.takeIf { it >= 0 }?.let { safeIndex ->
                        binding.form.rvInputForm.adapter?.notifyItemChanged(safeIndex)
                    }

                }
            }
        }


    private val viewModel: CdrObjectViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewFormBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)





        viewModel.benName.observe(viewLifecycleOwner) {
            binding.tvBenName.text = it
        }
        viewModel.benAgeGender.observe(viewLifecycleOwner) {
            binding.tvAgeGender.text = it
        }
        binding.btnSubmit.setOnClickListener {
            if (validate()) viewModel.submitForm()
        }
        viewModel.exists.observe(viewLifecycleOwner) { notIt ->
            notIt?.let { recordExists ->

                val adapter = FormInputAdapter(formValueListener = FormInputAdapter.FormValueListener { formId, index ->
                        viewModel.updateListOnValueChanged(formId, index)
                    },

                    selectImageClickListener = FormInputAdapter.SelectUploadImageClickListener { formId ->
                        if (!BuildConfig.FLAVOR.contains("mitanin", ignoreCase = true)) {
                            viewModel.setCurrentDocumentFormId(formId)
                            chooseOptions()
                        }

                    },

                    viewDocumentListner = FormInputAdapter.ViewDocumentOnClick { formId ->
                        if (recordExists) {
                            viewDocuments(formId)
                        } else {
                            val uri = when (formId) {
                                21 -> cdr1Uri
                                22 -> cdr2Uri
                                23 -> isDeathUri
                                else -> null
                            }

                            uri?.let {
                                if (it.toString().contains("document")) {
                                    displayPdf(it)
                                } else {
                                    viewImage(it)
                                }
                            }
                        }
                    },

                    isEnabled = !recordExists
                )
                adapter.disableUpload = recordExists

                binding.btnSubmit.isEnabled = !recordExists
                binding.form.rvInputForm.adapter = adapter

                lifecycleScope.launch {
                    viewModel.formList.collect {
                        if (it.isNotEmpty()) {
                            adapter.submitList(it)
                        }
                    }
                }
            }
        }

        viewModel.state.observe(viewLifecycleOwner) {
            when (it) {
                CdrObjectViewModel.State.LOADING -> {
                    binding.form.rvInputForm.visibility = View.GONE
                    binding.btnSubmit.visibility = View.GONE
                    binding.cvPatientInformation.visibility = View.GONE
                    binding.pbForm.visibility = View.VISIBLE
                }

                CdrObjectViewModel.State.SUCCESS -> {
                    findNavController().navigateUp()
                    WorkerUtils.triggerAmritPushWorker(requireContext())
                }

                CdrObjectViewModel.State.FAIL -> {
                    binding.form.rvInputForm.visibility = View.VISIBLE
                    binding.btnSubmit.visibility = View.VISIBLE
                    binding.cvPatientInformation.visibility = View.VISIBLE
                    binding.pbForm.visibility = View.GONE
                    Toast.makeText(
                        context,
                        resources.getString(R.string.saving_mdsr_to_database_failed),
                        Toast.LENGTH_LONG
                    ).show()
                }

                else -> {
                    binding.form.rvInputForm.visibility = View.VISIBLE
                    binding.btnSubmit.visibility = View.VISIBLE
                    binding.cvPatientInformation.visibility = View.VISIBLE
                    binding.pbForm.visibility = View.GONE
                }
            }
        }
    }


    fun validate(): Boolean {
        val result = binding.form.rvInputForm.adapter?.let {
            (it as FormInputAdapter).validateInput(resources)
        }
        Timber.d("Validation : $result")
        return if (result == -1)
            true
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
            (it as HomeActivity).updateActionBar(R.drawable.ic__death, getString(R.string.cdr))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_PDF_FILE && resultCode == Activity.RESULT_OK) {
            if (viewModel.getDocumentFormId() == 21) {
                data?.data?.let { pdfUri ->
                    if (checkFileSize(pdfUri, requireContext())) {
                        Toast.makeText(
                            context,
                            resources.getString(R.string.file_size),
                            Toast.LENGTH_LONG
                        ).show()

                    } else {
                        cdr1Uri = pdfUri
                        cdr1Uri?.let { uri ->
                            viewModel.setImageUriToFormElement(uri)
                            binding.form.rvInputForm.apply {
                                val adapter = this.adapter as FormInputAdapter
                                adapter.notifyDataSetChanged()
                            }
                        }

//                    updateImageRecord()
                    }
                }
            }
            else if(viewModel.getDocumentFormId() == 22){
                data?.data?.let { pdfUri ->
                    if (checkFileSize(pdfUri, requireContext())) {
                        Toast.makeText(
                            context,
                            resources.getString(R.string.file_size),
                            Toast.LENGTH_LONG
                        ).show()

                    } else {
                        cdr2Uri = pdfUri
                        cdr2Uri?.let { uri ->
                            viewModel.setImageUriToFormElement(uri)
                            binding.form.rvInputForm.apply {
                                val adapter = this.adapter as FormInputAdapter
                                adapter.notifyDataSetChanged()
                            }
                        }

//                    updateImageRecord()
                    }
                }
            }
            else if(viewModel.getDocumentFormId() == 23){
                data?.data?.let { pdfUri ->
                    if (checkFileSize(pdfUri, requireContext())) {
                        Toast.makeText(
                            context,
                            resources.getString(R.string.file_size),
                            Toast.LENGTH_LONG
                        ).show()

                    } else {
                        isDeathUri = pdfUri
                        isDeathUri?.let { uri ->
                            viewModel.setImageUriToFormElement(uri)
                            binding.form.rvInputForm.apply {
                                val adapter = this.adapter as FormInputAdapter
                                adapter.notifyDataSetChanged()
                            }
                        }

//                    updateImageRecord()
                    }
                }
            }

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

    private fun selectPdf() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
        }
        startActivityForResult(intent, PICK_PDF_FILE)
    }

    private fun takeImage() {
        lifecycleScope.launchWhenStarted {
            getTmpFileUri().let { uri ->
                when (viewModel.getDocumentFormId()) {
                    21 -> {
                        cdr1Uri = uri
                        takePicture.launch(cdr1Uri)
                    }
                    22 -> {
                        cdr2Uri = uri
                        takePicture.launch(cdr2Uri)
                    }
                    23 -> {
                        isDeathUri = uri
                        takePicture.launch(isDeathUri)
                    }
                }

            }
        }
    }

    private fun requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

private fun getTmpFileUri(): Uri {
    val imagesDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    val tmpFile = File.createTempFile(Konstants.tempBenImagePrefix, ".jpg", imagesDir)
    return FileProvider.getUriForFile(
        requireContext(),
        "${BuildConfig.APPLICATION_ID}.provider",
        tmpFile
    )
}

    private fun displayPdf(pdfUri: Uri) {

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(pdfUri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Grant permission to read the file
        }
        startActivity(Intent.createChooser(intent, "Open PDF with"))

    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        startActivityForResult(intent, PICK_PDF_FILE)
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

    private fun viewDocuments(formId: Int) {
        val index = when (formId) {
            21 -> viewModel.getIndexOfCDR1()
            22 -> viewModel.getIndexOfCDR2()
            23 -> viewModel.getIndexOfIsDeathCertificate()
            else -> -1
        }
        if (index < 0) {
            Timber.d("viewDocuments: formId=$formId not present in current form list")
            return
        }

        lifecycleScope.launch {
            viewModel.formList.collect { list ->

                val value = list.getOrNull(index)?.value ?: return@collect

                if (value.toString().contains("document")) {
                    displayPdf(value.toUri())
                } else {
                    viewImage(value.toUri())
                }
            }
        }
    }
}