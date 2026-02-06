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
class MdsrObjectFragment : Fragment() {

    private var _binding: FragmentNewFormBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MdsrObjectViewModel by viewModels()

    private val formMap = mutableMapOf(
        21 to FormConfig(null) { viewModel.getIndexOfMDSR1() },
        22 to FormConfig(null) { viewModel.getIndexOfMDSR2() },
        23 to FormConfig(null) { viewModel.getIndexOfIsDeathCertificate() }
    )

    data class FormConfig(
        var uri: Uri?,
        val indexProvider: () -> Int
    )

    private val PICK_PDF_FILE = 1
    private var latestTmpUri: Uri? = null

    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                val formId = viewModel.getDocumentFormId()
                val uri = formMap[formId]?.uri ?: latestTmpUri

                uri?.let {
                    viewModel.setImageUriToFormElement(it)
                    formMap[formId]?.indexProvider?.invoke()?.let { i ->
                        binding.form.rvInputForm.adapter?.notifyItemChanged(i)
                    }
                }
            }
        }

    private val requestLocationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) requestLocationPermission()
            else findNavController().navigateUp()
        }

    override fun onCreateView(inflater: android.view.LayoutInflater, container: android.view.ViewGroup?, savedInstanceState: Bundle?): android.view.View {
        _binding = FragmentNewFormBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.benName.observe(viewLifecycleOwner) { binding.tvBenName.text = it }
        viewModel.benAgeGender.observe(viewLifecycleOwner) { binding.tvAgeGender.text = it }

        binding.btnSubmit.setOnClickListener {
            if (validate()) viewModel.submitForm()
        }

        viewModel.exists.observe(viewLifecycleOwner) { recordExists ->
            recordExists?.let {
                val adapter = FormInputAdapter(
                    formValueListener = FormInputAdapter.FormValueListener { formId, index ->
                        viewModel.updateListOnValueChanged(formId, index)
                    },
                    selectImageClickListener = FormInputAdapter.SelectUploadImageClickListener { formId ->
                        if (!BuildConfig.FLAVOR.contains("mitanin", ignoreCase = true)) {
                            viewModel.setCurrentDocumentFormId(formId)
                            chooseOptions()
                        }
                    },
                    viewDocumentListner = FormInputAdapter.ViewDocumentOnClick { formId ->
                        if (it) viewDocuments(formId)
                        else formMap[formId]?.uri?.let { uri ->
                            if (uri.toString().contains("document")) displayPdf(uri)
                            else viewImage(uri)
                        }
                    },
                    isEnabled = !it
                )
                adapter.disableUpload = it
                binding.btnSubmit.isEnabled = !it
                binding.form.rvInputForm.adapter = adapter
                attachAdapterUnsavedGuard(adapter as FormInputAdapterWithBgIcon)
                lifecycleScope.launch {
                    viewModel.formList.collect { list ->
                        if (list.isNotEmpty()) adapter.submitList(list)
                    }
                }
            }
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                MdsrObjectViewModel.State.LOADING -> toggleViews(false)
                MdsrObjectViewModel.State.SUCCESS -> {
                    findNavController().navigateUp()
                    WorkerUtils.triggerAmritPushWorker(requireContext())
                }
                MdsrObjectViewModel.State.FAIL -> {
                    toggleViews(true)
                    Toast.makeText(
                        context,
                        resources.getString(R.string.saving_mdsr_to_database_failed),
                        Toast.LENGTH_LONG
                    ).show()
                }
                else -> toggleViews(true)
            }
        }
    }

    private fun toggleViews(show: Boolean) {
        binding.cvPatientInformation.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
        binding.form.rvInputForm.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
        binding.btnSubmit.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
        binding.pbForm.visibility = if (show) android.view.View.GONE else android.view.View.VISIBLE
    }

    fun validate(): Boolean {
        val result = (binding.form.rvInputForm.adapter as? FormInputAdapter)?.validateInput(resources)
        Timber.d("Validation : $result")
        return if (result == -1) true else {
            result?.let { binding.form.rvInputForm.scrollToPosition(it) }
            false
        }
    }

    override fun onStart() {
        super.onStart()
        (activity as? HomeActivity)?.updateActionBar(R.drawable.ic__death, getString(R.string.mdsr))
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_PDF_FILE && resultCode == Activity.RESULT_OK) {
            val formId = viewModel.getDocumentFormId()
            data?.data?.let { pdfUri ->
                if (checkFileSize(pdfUri, requireContext())) {
                    Toast.makeText(context, resources.getString(R.string.file_size), Toast.LENGTH_LONG).show()
                } else {
                    formMap[formId]?.uri = pdfUri
                    viewModel.setImageUriToFormElement(pdfUri)
                    (binding.form.rvInputForm.adapter as? FormInputAdapter)?.notifyDataSetChanged()
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
                formMap[formId]?.uri = uri
                takePicture.launch(uri)
            }
        }
    }

    private fun requestLocationPermission() {
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
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
        val alertDialog = MaterialAlertDialogBuilder(requireContext())
            .setView(viewImageBinding.root)
            .setCancelable(true)
            .create()
        Glide.with(this).load(imageUri).placeholder(R.drawable.ic_person).into(viewImageBinding.viewImage)
        viewImageBinding.btnClose.setOnClickListener { alertDialog.dismiss() }
        alertDialog.show()
    }

    private fun viewDocuments(formId: Int) {
        lifecycleScope.launch {
            viewModel.formList.collect { list ->
                formMap[formId]?.indexProvider?.invoke()?.let { index ->
                    list.getOrNull(index)?.value?.toUri()?.let { uri ->
                        if (uri.toString().contains("document")) displayPdf(uri)
                        else viewImage(uri)
                    }
                }
            }
        }
    }
}