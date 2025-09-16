package org.piramalswasthya.sakhi.ui.home_activity.eligible_couple.tracking.form

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
import org.piramalswasthya.sakhi.databinding.FragmentNewFormBinding
import org.piramalswasthya.sakhi.databinding.LayoutMediaOptionsBinding
import org.piramalswasthya.sakhi.databinding.LayoutViewMediaBinding
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.ui.checkFileSize
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import org.piramalswasthya.sakhi.utils.HelperUtil
import org.piramalswasthya.sakhi.work.WorkerUtils
import timber.log.Timber
import java.io.File

@AndroidEntryPoint
class EligibleCoupleTrackingFormFragment : Fragment() {

    private var _binding: FragmentNewFormBinding? = null
    private val binding: FragmentNewFormBinding
        get() = _binding!!
    private val PICK_PDF_FILE = 1
    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            if (success) {
                val formId = viewModel.getDocumentFormId()

                val uri = when (formId) {
                    21 -> mpaUri
                    else -> latestTmpUri
                }

                uri?.let {
                    viewModel.setImageUriToFormElement(it)

                    val index = when (formId) {
                        21 -> viewModel.getIndexOfMPA()
                        else -> null
                    }

                    index?.let { i ->
                        binding.form.rvInputForm.adapter?.notifyItemChanged(i)
                    }
                }
            }
        }


    private val requestLocationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { b ->
            if (b) {
                requestLocationPermission()
            } else findNavController().navigateUp()
        }
    private var latestTmpUri: Uri? = null
    private var mpaUri: Uri? = null

    private val viewModel: EligibleCoupleTrackingFormViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.allDoseList.observe(viewLifecycleOwner) { doseList ->
            HelperUtil.populateAntraTable(requireContext(), binding.tableAntra, doseList ?: return@observe)
        }

        viewModel.recordExists.observe(viewLifecycleOwner) { notIt ->
            notIt?.let { recordExists ->
                val adapter = FormInputAdapter(
                    formValueListener = FormInputAdapter.FormValueListener { formId, index ->
                        viewModel.updateListOnValueChanged(formId, index)
                        hardCodedListUpdate(formId)
                    },

                    selectImageClickListener = FormInputAdapter.SelectUploadImageClickListener { formId ->
                        viewModel.setCurrentDocumentFormId(formId)
                        chooseOptions()
                    },

                    viewDocumentListner = FormInputAdapter.ViewDocumentOnClick { formId ->
                        if (recordExists) {
                            viewDocuments(formId)
                        } else {
                            val uri = when (formId) {
                                21 -> mpaUri
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
                listOf(binding.tableAntra, binding.tvHeading).forEach {
                    it.visibility = if (recordExists) View.VISIBLE else View.GONE
                }
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
        viewModel.benName.observe(viewLifecycleOwner) {
            binding.tvBenName.text = it
        }
        viewModel.benAgeGender.observe(viewLifecycleOwner) {
            binding.tvAgeGender.text = it
        }
        binding.btnSubmit.setOnClickListener {
            submitEligibleTrackingForm()
        }

        viewModel.state.observe(viewLifecycleOwner) {
            when (it) {
                EligibleCoupleTrackingFormViewModel.State.SAVE_SUCCESS -> {
                    navigateToNextScreen()
                    WorkerUtils.triggerAmritPushWorker(requireContext())

                }

                else -> {}
            }
        }
    }

    private fun navigateToNextScreen() {
        if (viewModel.isPregnant) {
            findNavController().navigate(
                EligibleCoupleTrackingFormFragmentDirections.actionEligibleCoupleTrackingFormFragmentToPregnancyRegistrationFormFragment(
                    benId = viewModel.benId
                )
            )
            viewModel.resetState()
        } else {
            findNavController().navigateUp()
            Toast.makeText(
                requireContext(),
                resources.getString(R.string.tracking_form_filled_successfully),
                Toast.LENGTH_SHORT
            ).show()
            viewModel.resetState()
        }
    }

    private fun submitEligibleTrackingForm() {
        if (validateCurrentPage()) {
            viewModel.saveForm()
        }
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


    private fun hardCodedListUpdate(formId: Int) {
        binding.form.rvInputForm.adapter?.apply {
            when (formId) {
                1 -> {
                    notifyItemChanged(1)
                    notifyItemChanged(2)

                }
                11 ->{
                    notifyDataSetChanged()
                }

                4, 5 -> {
                    notifyDataSetChanged()
                    //notifyItemChanged(viewModel.getIndexOfIsPregnant())
                }

            }
        }
    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as HomeActivity).updateActionBar(
                R.drawable.ic__eligible_couple,
                getString(R.string.eligible_couple_tracking_form)
            )
        }
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
                        mpaUri = pdfUri
                        mpaUri?.let { uri ->
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
                    mpaUri = uri
                    takePicture.launch(mpaUri)
                } 


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
                    it.get(viewModel.getIndexOfMPA()).value.let {
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
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}