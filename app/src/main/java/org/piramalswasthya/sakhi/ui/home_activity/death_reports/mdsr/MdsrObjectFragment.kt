package org.piramalswasthya.sakhi.ui.home_activity.death_reports.mdsr

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
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
import org.piramalswasthya.sakhi.adapters.FormInputAdapterOld
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
class MdsrObjectFragment : Fragment() {


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
    private var mdsr1Uri: Uri? = null
    private var mdsr2Uri: Uri? = null
    private var isDeathUri: Uri? = null

    private val PICK_PDF_FILE = 1
    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            if (success) {
                val formId = viewModel.getDocumentFormId()

                val uri = when (formId) {
                    21 -> mdsr1Uri
                    22 -> mdsr2Uri
                    23 -> isDeathUri
                    else -> latestTmpUri
                }

                uri?.let {
                    viewModel.setImageUriToFormElement(it)

                    val index = when (formId) {
                        21 -> viewModel.getIndexOfMDSR1()
                        22 -> viewModel.getIndexOfMDSR2()
                        23 -> viewModel.getIndexOfIsDeathCertificate()
                        else -> null
                    }

                    index?.let { i ->
                        binding.form.rvInputForm.adapter?.notifyItemChanged(i)
                    }
                }
            }
        }



    private val viewModel: MdsrObjectViewModel by viewModels()

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
                val adapter = FormInputAdapter(
                    formValueListener = FormInputAdapter.FormValueListener { formId, index ->
                        viewModel.updateListOnValueChanged(formId, index)
                    },
                    selectImageClickListener = FormInputAdapter.SelectUploadImageClickListener { formId ->
                        viewModel.setCurrentDocumentFormId(formId)
                        chooseOptions()
//                        Toast.makeText(requireContext(), formId.toString(), Toast.LENGTH_LONG).show()
                    },

                    viewDocumentListner = FormInputAdapter.ViewDocumentOnClick { formId ->
                        if (recordExists) {
                            viewDocuments(formId)
                        } else {
                            val uri = when (formId) {
                                21 -> mdsr1Uri
                                22 -> mdsr2Uri
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
                        if (it.isNotEmpty())
                            adapter.submitList(it)

                    }
                }
            }
        }
        viewModel.state.observe(viewLifecycleOwner) {
            when (it) {
                MdsrObjectViewModel.State.LOADING -> {
                    binding.cvPatientInformation.visibility = View.GONE
                    binding.form.rvInputForm.visibility = View.GONE
                    binding.btnSubmit.visibility = View.GONE
                    binding.pbForm.visibility = View.VISIBLE
                }

                MdsrObjectViewModel.State.SUCCESS -> {
                    findNavController().navigateUp()
                    WorkerUtils.triggerAmritPushWorker(requireContext())
                }

                MdsrObjectViewModel.State.FAIL -> {
                    binding.cvPatientInformation.visibility = View.VISIBLE
                    binding.form.rvInputForm.visibility = View.VISIBLE
                    binding.btnSubmit.visibility = View.VISIBLE
                    binding.pbForm.visibility = View.GONE
                    Toast.makeText(
                        context,
                        resources.getString(R.string.saving_mdsr_to_database_failed),
                        Toast.LENGTH_LONG
                    ).show()
                }

                else -> {
                    binding.cvPatientInformation.visibility = View.VISIBLE
                    binding.form.rvInputForm.visibility = View.VISIBLE
                    binding.btnSubmit.visibility = View.VISIBLE
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
            (it as HomeActivity).updateActionBar(R.drawable.ic__death, getString(R.string.mdsr))
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
                        mdsr1Uri = pdfUri
                        mdsr1Uri?.let { uri ->
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
                        mdsr2Uri = pdfUri
                        mdsr2Uri?.let { uri ->
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
                if (viewModel.getDocumentFormId() == 21) {
                    mdsr1Uri = uri
                    takePicture.launch(mdsr1Uri)
                } else if (viewModel.getDocumentFormId() == 22) {
                    mdsr2Uri = uri
                    takePicture.launch(mdsr2Uri)
                }
                else if (viewModel.getDocumentFormId() == 23) {
                    isDeathUri = uri
                    takePicture.launch(isDeathUri)
                }
//                else {
//                    latestTmpUri = uri
//                    takePicture.launch(latestTmpUri)
//                }

            }
        }
    }

    private fun requestLocationPermission() {
        val locationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
//        else if (!isGPSEnabled) showSettingsAlert()
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

    private fun viewDocuments(it: Int) {
        if (it == 21) {
            lifecycleScope.launch {
                viewModel.formList.collect {
                    it.get(viewModel.getIndexOfMDSR1()).value.let {
                        if (it.toString().contains("document")) {
                            displayPdf(it!!.toUri())
                        } else {
                            viewImage(it!!.toUri())
                        }
                    }
                }
            }
        } else if (it == 22) {
            lifecycleScope.launch {
                viewModel.formList.collect {
                    it.get(viewModel.getIndexOfMDSR2()).value.let {
                        if (it.toString().contains("document")) {
                            displayPdf(it!!.toUri())
                        } else {
                            viewImage(it!!.toUri())
                        }
                    }
                }
            }
        } else if (it == 23) {
            lifecycleScope.launch {
                viewModel.formList.collect {
                    it.get(viewModel.getIndexOfIsDeathCertificate()).value.let {
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