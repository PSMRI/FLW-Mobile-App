package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.abortion.form

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
import org.piramalswasthya.sakhi.adapters.FormInputAdapterWithBgIcon
import org.piramalswasthya.sakhi.databinding.FragmentNewFormBinding
import org.piramalswasthya.sakhi.databinding.LayoutMediaOptionsBinding
import org.piramalswasthya.sakhi.databinding.LayoutViewMediaBinding
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.ui.checkFileSize
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import org.piramalswasthya.sakhi.work.WorkerUtils
import org.piramalswasthya.sakhi.ui.home_activity.maternal_health.abortion.form.PwAncAbortionFormViewModel.State

import timber.log.Timber
import java.io.File

@AndroidEntryPoint
class PwAncAbortionFormFragment : Fragment() {

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
    private var abortion1Uri: Uri? = null
    private var abortion2Uri: Uri? = null

    private val PICK_PDF_FILE = 1
    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            if (success) {
                val formId = viewModel.getDocumentFormId()

                val uri = when (formId) {
                    21 -> abortion1Uri
                    22 -> abortion2Uri
                    else -> latestTmpUri
                }

                uri?.let {
                    viewModel.setImageUriToFormElement(it)

                    val index = when (formId) {
                        21 -> viewModel.getIndexOfAbortionDischarge1()
                        22 -> viewModel.getIndexOfAbortionDischarge2()
                        else -> null
                    }

                    index?.let { i ->
                        binding.form.rvInputForm.adapter?.notifyItemChanged(i)
                    }
                }
            }
        }

    private val viewModel: PwAncAbortionFormViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.recordExists.observe(viewLifecycleOwner) { notIt ->
            notIt?.let { recordExists ->
                binding.btnSubmit.visibility = if (recordExists) View.GONE else View.VISIBLE
                val adapter = FormInputAdapterWithBgIcon(
                    formValueListener = FormInputAdapterWithBgIcon.FormValueListener { formId, index ->
                        viewModel.updateListOnValueChanged(formId, index)
                        hardCodedListUpdate(formId)
                    },
                    selectImageClickListener = FormInputAdapterWithBgIcon.SelectUploadImageClickListener { formId ->
                        viewModel.setCurrentDocumentFormId(formId)
                        chooseOptions()
                    },

                    viewDocumentListner = FormInputAdapterWithBgIcon.ViewDocumentOnClick { formId ->
                        if (recordExists) {
                            viewDocuments(formId)
                        } else {
                            val uri = when (formId) {
                                21 -> abortion1Uri
                                22 -> abortion2Uri
                                else -> null
                            }

                            uri?.let {
                                viewImage(it)

                            }
                        }
                    },
                    isEnabled = !recordExists
                )
                binding.form.rvInputForm.adapter = adapter
                binding.form.rvInputForm.itemAnimator = null
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
            submitAncForm()
        }
        binding.fabEdit.setOnClickListener {
            binding.fabEdit.visibility = View.GONE
            viewModel.setRecordExist(false)
        }
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state!!) {
                State.IDLE -> {
                }

                State.SAVING -> {
                    binding.llContent.visibility = View.GONE
                    binding.pbForm.visibility = View.VISIBLE
                }

                State.SAVE_SUCCESS -> {
                    binding.llContent.visibility = View.VISIBLE
                    binding.pbForm.visibility = View.GONE
                    Toast.makeText(context, "Save Successful", Toast.LENGTH_LONG).show()
                    WorkerUtils.triggerAmritPushWorker(requireContext())
                    findNavController().navigateUp()
                }

                State.SAVE_FAILED -> {
                    Toast.makeText(

                        context, "Something wend wong! Contact testing!", Toast.LENGTH_LONG
                    ).show()
                    binding.llContent.visibility = View.VISIBLE
                    binding.pbForm.visibility = View.GONE
                }
            }
        }

        viewModel.recordExists.observe(viewLifecycleOwner) { exists ->
            binding.fabEdit.visibility = if (exists) View.VISIBLE else View.GONE
        }


    }

    private fun submitAncForm() {
        if (validateCurrentPage()) {
            viewModel.saveForm()
        }
    }

    private fun validateCurrentPage(): Boolean {
        val result = binding.form.rvInputForm.adapter?.let {
            (it as FormInputAdapterWithBgIcon).validateInput(resources)
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
            }
        }
    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as HomeActivity).updateActionBar(
                R.drawable.ic__anc_visit,
                getString(R.string.cac_form)
            )
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
                        abortion1Uri = pdfUri
                        abortion1Uri?.let { uri ->
                            viewModel.setImageUriToFormElement(uri)
                            binding.form.rvInputForm.apply {
                                val adapter = this.adapter as FormInputAdapterWithBgIcon
                                adapter.notifyDataSetChanged()
                            }
                        }

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
                        abortion2Uri = pdfUri
                        abortion2Uri?.let { uri ->
                            viewModel.setImageUriToFormElement(uri)
                            binding.form.rvInputForm.apply {
                                val adapter = this.adapter as FormInputAdapterWithBgIcon
                                adapter.notifyDataSetChanged()
                            }
                        }

                    }
                }
            }


        }
    }

    private fun chooseOptions() {
        val alertBinding = LayoutMediaOptionsBinding.inflate(layoutInflater, binding.root, false)
        alertBinding.btnPdf.visibility=View.GONE
        val alertDialog = MaterialAlertDialogBuilder(requireContext())
            .setView(alertBinding.root)
            .setCancelable(true)
            .create()

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
                if (viewModel.getDocumentFormId() == 21) {
                    abortion1Uri = uri
                    takePicture.launch(abortion1Uri)
                } else if (viewModel.getDocumentFormId() == 22) {
                    abortion2Uri = uri
                    takePicture.launch(abortion2Uri)
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
                    it.get(viewModel.getIndexOfAbortionDischarge1()).value.let {
                        viewImage(it!!.toUri())
                    }
                }
            }
        } else if (it == 22) {
            lifecycleScope.launch {
                viewModel.formList.collect {
                    it.get(viewModel.getIndexOfAbortionDischarge2()).value.let {
                        viewImage(it!!.toUri())
                    }
                }
            }
        }

    }


}