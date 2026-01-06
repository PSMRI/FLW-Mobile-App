package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.delivery_outcome

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
import org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pregnant_woment_anc_visits.form.PwAncFormFragmentDirections
import org.piramalswasthya.sakhi.work.WorkerUtils
import java.io.File

@AndroidEntryPoint
class DeliveryOutcomeFragment : Fragment() {

    private var _binding: FragmentNewFormBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DeliveryOutcomeViewModel by viewModels()

    private val PICK_PDF_FILE = 1
    private var latestTmpUri: Uri? = null
    private val formMap by lazy {
        mapOf(
            21 to { uri: Uri? -> viewModel.getIndexOfMCP1() to uri },
            22 to { uri: Uri? -> viewModel.getIndexOfMCP2() to uri },
            23 to { uri: Uri? -> viewModel.getIndexOfIsjsyFileUpload() to uri }
        )
    }
    private val formUris = mutableMapOf<Int, Uri?>()

    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            if (success) {
                val formId = viewModel.getDocumentFormId()
                val uri = formUris[formId] ?: latestTmpUri
                uri?.let {
                    viewModel.setImageUriToFormElement(it)
                    formMap[formId]?.invoke(it)?.first?.let { i ->
                        binding.form.rvInputForm.adapter?.notifyItemChanged(i)
                    }
                }
            }
        }

    private val requestLocationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { b ->
            if (b) requestLocationPermission() else findNavController().navigateUp()
        }

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
                val adapter = FormInputAdapter(
                    formValueListener = FormInputAdapter.FormValueListener { formId, index ->
                        viewModel.updateListOnValueChanged(formId, index)
                        hardCodedListUpdate(formId)
                    },
                    selectImageClickListener = FormInputAdapter.SelectUploadImageClickListener { formId ->
                        if (!BuildConfig.FLAVOR.contains("mitanin", ignoreCase = true)) {
                            viewModel.setCurrentDocumentFormId(formId)
                            chooseOptions()
                        }
                    },
                    viewDocumentListner = FormInputAdapter.ViewDocumentOnClick { formId ->
                        if (recordExists) viewDocuments(formId) else {
                            formUris[formId]?.let {
                                if (it.toString().contains("document")) displayPdf(it) else viewImage(it)
                            }
                        }
                    },
                    isEnabled = !recordExists
                )
                binding.btnSubmit.isEnabled = !recordExists
                binding.form.rvInputForm.adapter = adapter
                lifecycleScope.launch { viewModel.formList.collect { if (it.isNotEmpty()) adapter.submitList(it) } }
            }
        }
        viewModel.benName.observe(viewLifecycleOwner) { binding.tvBenName.text = it }
        viewModel.benAgeGender.observe(viewLifecycleOwner) { binding.tvAgeGender.text = it }
        binding.btnSubmit.setOnClickListener { submitDeliveryOutcomeForm() }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
               is DeliveryOutcomeViewModel.State.SAVING -> toggleLoading(true)
               is DeliveryOutcomeViewModel.State.SAVE_SUCCESS -> handleSaveSuccess(state.shouldNavigateToMdsr)
               is DeliveryOutcomeViewModel.State.SAVE_FAILED -> toggleLoading(false)
                else -> {}
            }
        }
    }

    private fun toggleLoading(isLoading: Boolean) {
        binding.llContent.visibility = if (isLoading) View.GONE else View.VISIBLE
        binding.pbForm.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun handleSaveSuccess(shouldNavigateToMdsr: Boolean) {
        toggleLoading(false)
        WorkerUtils.triggerAdHocPncEcUpdateWorker(requireContext())
        if (shouldNavigateToMdsr) navigateToMdsr()
        else findNavController().navigateUp()
    }

    private fun navigateToMdsr() {
        viewModel.hhId.let {
            val action = DeliveryOutcomeFragmentDirections.actionDeliveryOutcomeFragmentToMdsrObjectFragment(
                hhId = it, benId = viewModel.benId
            )
            findNavController().navigate(action)
        }
    }


    private fun submitDeliveryOutcomeForm() {
        if (validateCurrentPage()) viewModel.saveForm()
    }

    private fun validateCurrentPage(): Boolean {
        val result = (binding.form.rvInputForm.adapter as? FormInputAdapter)?.validateInput(resources)
        return if (result == -1) true else {
            result?.let { binding.form.rvInputForm.scrollToPosition(it) }
            false
        }
    }

    private fun hardCodedListUpdate(formId: Int) {
        binding.form.rvInputForm.adapter?.apply {
            when (formId) {
                1, 6, 10, 11, 12 -> notifyDataSetChanged()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        (activity as? HomeActivity)?.updateActionBar(R.drawable.ic__delivery_outcome, getString(R.string.delivery_outcome))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_PDF_FILE && resultCode == Activity.RESULT_OK) {
            val formId = viewModel.getDocumentFormId()
            data?.data?.let { pdfUri ->
                if (checkFileSize(pdfUri, requireContext())) {
                    Toast.makeText(context, resources.getString(R.string.file_size), Toast.LENGTH_LONG).show()
                } else {
                    formUris[formId] = pdfUri
                    pdfUri.let { uri ->
                        viewModel.setImageUriToFormElement(uri)
                        (binding.form.rvInputForm.adapter as? FormInputAdapter)?.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    private fun chooseOptions() {
        val alertBinding = LayoutMediaOptionsBinding.inflate(layoutInflater, binding.root, false)
        val alertDialog = MaterialAlertDialogBuilder(requireContext()).setView(alertBinding.root).setCancelable(true).create()
        alertBinding.btnPdf.setOnClickListener { alertDialog.dismiss(); selectPdf() }
        alertBinding.btnCamera.setOnClickListener { alertDialog.dismiss(); takeImage() }
        alertBinding.btnGallery.setOnClickListener { alertDialog.dismiss(); selectImage() }
        alertBinding.btnCancel.setOnClickListener { alertDialog.dismiss() }
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
                val formId = viewModel.getDocumentFormId()
                formUris[formId] = uri
                takePicture.launch(uri)
            }
        }
    }

    private fun requestLocationPermission() {
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun getTmpFileUri(): Uri {
        val imagesDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val tmpFile = File.createTempFile(Konstants.tempBenImagePrefix, ".jpg", imagesDir)
        return FileProvider.getUriForFile(requireContext(), "${BuildConfig.APPLICATION_ID}.provider", tmpFile)
    }

    private fun displayPdf(pdfUri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(pdfUri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "Open PDF with"))
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
        startActivityForResult(intent, PICK_PDF_FILE)
    }

    private fun viewImage(imageUri: Uri) {
        val viewImageBinding = LayoutViewMediaBinding.inflate(layoutInflater, binding.root, false)
        val alertDialog = MaterialAlertDialogBuilder(requireContext()).setView(viewImageBinding.root).setCancelable(true).create()
        Glide.with(this).load(imageUri).placeholder(R.drawable.ic_person).into(viewImageBinding.viewImage)
        viewImageBinding.btnClose.setOnClickListener { alertDialog.dismiss() }
        alertDialog.show()
    }

    private fun viewDocuments(it: Int) {
        lifecycleScope.launch {
            viewModel.formList.collect { list ->
                val uri = when (it) {
                    21 -> list[viewModel.getIndexOfMCP1()].value?.toUri()
                    22 -> list[viewModel.getIndexOfMCP2()].value?.toUri()
                    23 -> list[viewModel.getIndexOfIsjsyFileUpload()].value?.toUri()
                    else -> null
                }
                uri?.let { u -> if (u.toString().contains("document")) displayPdf(u) else viewImage(u) }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
