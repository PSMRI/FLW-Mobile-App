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
    private val binding get() = _binding!!

    private val PICK_FILE = 1
    private var latestTmpUri: Uri? = null
    private var mpaUri: Uri? = null
    private var deliveryDischargeSummary1Uri: Uri? = null
    private var deliveryDischargeSummary2Uri: Uri? = null

    private val viewModel: EligibleCoupleTrackingFormViewModel by viewModels()

    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                val formId = viewModel.getDocumentFormId()
                val uri = when (formId) {
                    21 -> mpaUri
                    58 -> deliveryDischargeSummary1Uri
                    59 -> deliveryDischargeSummary2Uri
                    else -> latestTmpUri
                }

                uri?.let {
                    viewModel.setImageUriToFormElement(it)
                    when (formId) {
                        21 -> binding.form.rvInputForm.adapter?.notifyItemChanged(viewModel.getIndexOfMPA())
                        58 -> binding.form.rvInputForm.adapter?.notifyItemChanged(viewModel.getIndexDeliveryDischargeSummary1())
                        59 -> binding.form.rvInputForm.adapter?.notifyItemChanged(viewModel.getIndexDeliveryDischargeSummary2())
                        else -> latestTmpUri
                    }
                }
            }
        }

    private val requestLocationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) requestLocationPermission()
            else findNavController().navigateUp()
        }

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

        viewModel.recordExists.observe(viewLifecycleOwner) { recordExists ->
            recordExists?.let {
                setupFormAdapter(it)
                binding.btnSubmit.isEnabled = !it
            }
        }

        viewModel.benName.observe(viewLifecycleOwner) { binding.tvBenName.text = it }
        viewModel.benAgeGender.observe(viewLifecycleOwner) { binding.tvAgeGender.text = it }

        binding.btnSubmit.setOnClickListener { submitEligibleTrackingForm() }

        viewModel.state.observe(viewLifecycleOwner) {
            if (it == EligibleCoupleTrackingFormViewModel.State.SAVE_SUCCESS) {
                navigateToNextScreen()
                WorkerUtils.triggerAmritPushWorker(requireContext())
            }
        }
    }

    private fun setupFormAdapter(recordExists: Boolean) {
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
                viewDocuments(formId)
            },
            isEnabled = !recordExists
        ).apply { disableUpload = recordExists }

        viewModel.showAntraSection.observe(viewLifecycleOwner) { show ->
            listOf(binding.tableAntra, binding.tvHeading).forEach {
                it.visibility = if (show) View.VISIBLE else View.GONE
            }
        }

        binding.form.rvInputForm.adapter = adapter
        lifecycleScope.launch {
            viewModel.formList.collect {
                if (it.isNotEmpty()) adapter.submitList(it)
            }
        }
    }

    private fun navigateToNextScreen() {
        if (viewModel.isPregnant) {
            findNavController().navigate(
                EligibleCoupleTrackingFormFragmentDirections
                    .actionEligibleCoupleTrackingFormFragmentToPregnancyRegistrationFormFragment(
                        benId = viewModel.benId
                    )
            )
        } else {
            findNavController().navigateUp()
            Toast.makeText(
                requireContext(),
                resources.getString(R.string.tracking_form_filled_successfully),
                Toast.LENGTH_SHORT
            ).show()
        }
        viewModel.resetState()
    }

    private fun submitEligibleTrackingForm() {
        if (validateCurrentPage()) viewModel.saveForm()
    }

    private fun validateCurrentPage(): Boolean {
        val result = (binding.form.rvInputForm.adapter as? FormInputAdapter)?.validateInput(resources)
        Timber.d("Validation : $result")
        return if (result == -1) true else {
            result?.let { binding.form.rvInputForm.scrollToPosition(it) }
            false
        }
    }

    private fun hardCodedListUpdate(formId: Int) {
        binding.form.rvInputForm.adapter?.apply {
            when (formId) {
                1 -> {
                    notifyItemChanged(1)
                    notifyItemChanged(2)
                    notifyItemChanged(9)

                }
                4, 5, 11 -> notifyDataSetChanged()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        (activity as? HomeActivity)?.updateActionBar(
            R.drawable.ic__eligible_couple,
            getString(R.string.eligible_couple_tracking_form)
        )
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_FILE && resultCode == Activity.RESULT_OK) {
            val formId = viewModel.getDocumentFormId()
            val selectedUri = data?.data
            selectedUri?.let { uri ->
                if (checkFileSize(uri, requireContext())) {
                    Toast.makeText(context, resources.getString(R.string.file_size), Toast.LENGTH_LONG).show()
                } else {
                    when (formId) {
                        21 -> mpaUri = uri
                        58 -> deliveryDischargeSummary1Uri = uri
                        59 -> deliveryDischargeSummary2Uri = uri
                        else -> latestTmpUri = uri
                    }
                    viewModel.setImageUriToFormElement(uri)
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
        startActivityForResult(intent, PICK_FILE)
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
        startActivityForResult(intent, PICK_FILE)
    }

    private fun takeImage() {
        lifecycleScope.launchWhenStarted {
            getTmpFileUri().let { uri ->
                val formId = viewModel.getDocumentFormId()
                when (formId) {
                    21 -> mpaUri = uri
                    58 -> deliveryDischargeSummary1Uri = uri
                    59 -> deliveryDischargeSummary2Uri = uri
                    else -> latestTmpUri = uri
                }
                takePicture.launch(uri)
            }
        }
    }

    private fun requestLocationPermission() {
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
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

    private fun viewImage(imageUri: Uri) {
        val viewImageBinding = LayoutViewMediaBinding.inflate(layoutInflater, binding.root, false)
        val alertDialog = MaterialAlertDialogBuilder(requireContext())
            .setView(viewImageBinding.root)
            .setCancelable(true)
            .create()

        Glide.with(this).load(imageUri).placeholder(R.drawable.ic_person)
            .into(viewImageBinding.viewImage)
        viewImageBinding.btnClose.setOnClickListener { alertDialog.dismiss() }
        alertDialog.show()
    }

    private fun viewDocuments(formId: Int) {
        lifecycleScope.launch {
            viewModel.formList.collect { list ->
                val value = when (formId) {
                    21 -> list.getOrNull(viewModel.getIndexOfMPA())?.value
                    58 -> list.getOrNull(viewModel.getIndexDeliveryDischargeSummary1())?.value
                    59 -> list.getOrNull(viewModel.getIndexDeliveryDischargeSummary2())?.value
                    else -> null
                }

                if (value.isNullOrEmpty()) {
                    Toast.makeText(requireContext(), "No document found for this form", Toast.LENGTH_SHORT).show()
                    return@collect
                }

                val uri = value.toUri()
                try {
                    if (uri.toString().contains("document", ignoreCase = true)) {
                        displayPdf(uri)
                    } else {
                        viewImage(uri)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "Unable to open document", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun isMPAForm(formId: Int) = formId == 21

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
