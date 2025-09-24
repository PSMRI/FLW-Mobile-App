package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pnc.form

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
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
import org.piramalswasthya.sakhi.adapters.FormInputAdapterWithBgIcon
import org.piramalswasthya.sakhi.databinding.FragmentNewFormBinding
import org.piramalswasthya.sakhi.databinding.LayoutMediaOptionsBinding
import org.piramalswasthya.sakhi.databinding.LayoutViewMediaBinding
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.ui.checkFileSize
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pnc.form.PncFormViewModel.State
import org.piramalswasthya.sakhi.work.WorkerUtils
import timber.log.Timber
import java.io.File

@AndroidEntryPoint
class PncFormFragment : Fragment() {

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
    private var deliveryDischargeUri1: Uri? = null
    private var deliveryDischargeUri2: Uri? = null
    private var deliveryDischargeUri3: Uri? = null
    private var deliveryDischargeUri4: Uri? = null


    private val PICK_PDF_FILE = 1


    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            if (success) {
                val formId = viewModel.getDocumentFormId()
                val uri = getUriByFormId(formId)

                uri?.let {
                    viewModel.setImageUriToFormElement(it)
                    getIndexByFormId(formId)?.let { i ->
                        binding.form.rvInputForm.adapter?.notifyItemChanged(i)
                    }
                }
            }
        }

    private val viewModel: PncFormViewModel by viewModels()

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
                binding.fabEdit.visibility = /*if (recordExists) View.VISIBLE else */View.GONE
                binding.btnSubmit.visibility = if (recordExists) View.GONE else View.VISIBLE
                val adapter = FormInputAdapter(
                    selectImageClickListener = FormInputAdapter.SelectUploadImageClickListener { formId ->
                        viewModel.setCurrentDocumentFormId(formId)
                        chooseOptions()
                    },
                    formValueListener = FormInputAdapter.FormValueListener { formId, index ->
                        viewModel.updateListOnValueChanged(formId, index)
                        hardCodedListUpdate(formId)
                        if (formId == 57) {
                            val adapter = binding.form.rvInputForm.adapter as? FormInputAdapter
                            adapter?.notifyDataSetChanged()
                        }
                    },
                    viewDocumentListner = FormInputAdapter.ViewDocumentOnClick { formId ->
                        if (recordExists) {
                            viewDocuments(formId)
                        } else {
                            getUriByFormId(formId)?.let { viewImage(it) }
                        }
                    },
                    isEnabled = !recordExists

                )
                binding.form.rvInputForm.adapter = adapter
                lifecycleScope.launch {
                    viewModel.formList.collect {
                        if (it.isNotEmpty())

                            adapter.submitList(it)

                    }
                }
            }
        }
        viewModel.showIncentiveAlert.observe(viewLifecycleOwner) { shouldShowAlert ->
            if (shouldShowAlert) {
                showIncentiveAlert()
                viewModel.incentiveAlertShown()
            }
        }
        viewModel.benName.observe(viewLifecycleOwner) {
            binding.tvBenName.text = it
        }
        viewModel.benAgeGender.observe(viewLifecycleOwner) {
            binding.tvAgeGender.text = it
        }
        binding.btnSubmit.setOnClickListener {
            if (!isDeliveryDischargeUploaded()) {
                showUploadReminderDialog()
            } else {
                submitAncForm()
            }
        }
        binding.fabEdit.setOnClickListener {
            viewModel.setRecordExist(false)
        }
        viewModel.navigateToMdsr.observe(viewLifecycleOwner) { shouldNavigate ->
            if (shouldNavigate) {
                navigateToMdsr()
                viewModel.onNavigationComplete()
            }
        }
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is State.IDLE -> {
                }

                is State.SAVING -> {
                    binding.llContent.visibility = View.GONE
                    binding.pbForm.visibility = View.VISIBLE
                }

                is State.SAVE_SUCCESS -> {
                    val saveSuccessState = state
                    binding.llContent.visibility = View.VISIBLE
                    binding.pbForm.visibility = View.GONE
                    Toast.makeText(context, "Save Successful", Toast.LENGTH_LONG).show()
                    WorkerUtils.triggerAmritPushWorker(requireContext())
                    //   WorkerUtils.triggerAdHocPncEcUpdateWorker(requireContext())
                    //findNavController().navigateUp()

                    if (saveSuccessState.shouldNavigateToMdsr) {
                        navigateToMdsr()
                    } else {
                        findNavController().navigateUp()
                    }
                }

                is State.SAVE_FAILED -> {
                    Toast.makeText(

                        context, "Something wend wong! Contact testing!", Toast.LENGTH_LONG
                    ).show()
                    binding.llContent.visibility = View.VISIBLE
                    binding.pbForm.visibility = View.GONE
                }
            }
        }
    }

    private fun submitAncForm() {
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

                1 -> notifyItemChanged(1)
            }
        }
    }


    private fun chooseOptions() {
        val alertBinding = LayoutMediaOptionsBinding.inflate(layoutInflater, binding.root, false)
        alertBinding.btnPdf.visibility = View.GONE
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
        alertBinding.btnCancel.setOnClickListener { alertDialog.dismiss() }
        alertDialog.show()
    }

    private fun showIncentiveAlert() {
        AlertDialog.Builder(requireContext())
            .setTitle("Reminder!!")
            .setMessage("Do you want to upload \"Delivery Discharge Summary\" photo copy to claim your Incentive.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    private fun takeImage() {
        lifecycleScope.launchWhenStarted {
            getTmpFileUri().let { uri ->
                assignUriAndLaunch(viewModel.getDocumentFormId(), uri)
            }
        }
    }

    private fun assignUriAndLaunch(formId: Int, tmpUri: Uri) {
        when (formId) {
            58 -> {
                deliveryDischargeUri1 = tmpUri
                takePicture.launch(deliveryDischargeUri1)
            }

            59 -> {
                deliveryDischargeUri2 = tmpUri
                takePicture.launch(deliveryDischargeUri2)
            }

            60 -> {
                deliveryDischargeUri3 = tmpUri
                takePicture.launch(deliveryDischargeUri3)
            }

            61 -> {
                deliveryDischargeUri4 = tmpUri
                takePicture.launch(deliveryDischargeUri4)
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
        viewImageBinding.btnClose.setOnClickListener { alertDialog.dismiss() }
        alertDialog.show()
    }

    private fun viewDocuments(formId: Int) {
        when (formId) {
            58 -> observeAndViewDocument(viewModel.getIndexDeliveryDischargeSummary1())
            59 -> observeAndViewDocument(viewModel.getIndexDeliveryDischargeSummary2())
            60 -> observeAndViewDocument(viewModel.getIndexDeliveryDischargeSummary3())
            61 -> observeAndViewDocument(viewModel.getIndexDeliveryDischargeSummary4())

        }
    }

    private fun observeAndViewDocument(index: Int) {
        lifecycleScope.launch {
            viewModel.formList.collect {
                it[index].value?.let { uriStr -> viewImage(uriStr.toUri()) }
            }
        }
    }

    private fun getUriByFormId(formId: Int): Uri? = when (formId) {
        58 -> deliveryDischargeUri1
        59 -> deliveryDischargeUri2
        60 -> deliveryDischargeUri3
        61 -> deliveryDischargeUri4
        else -> latestTmpUri
    }

    private fun getIndexByFormId(formId: Int): Int? = when (formId) {
        58 -> viewModel.getIndexDeliveryDischargeSummary1()
        59 -> viewModel.getIndexDeliveryDischargeSummary2()
        60 -> viewModel.getIndexDeliveryDischargeSummary3()
        61 -> viewModel.getIndexDeliveryDischargeSummary4()
        else -> null
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_PDF_FILE && resultCode == Activity.RESULT_OK) {
            handlePdfResult(viewModel.getDocumentFormId(), data?.data)
        }
    }

    private fun handlePdfResult(formId: Int, pdfUri: Uri?) {
        pdfUri?.let { uri ->
            if (checkFileSize(uri, requireContext())) {
                Toast.makeText(context, resources.getString(R.string.file_size), Toast.LENGTH_LONG)
                    .show()
            } else {
                when (formId) {
                    58 -> deliveryDischargeUri1 = uri
                    59 -> deliveryDischargeUri2 = uri
                    60 -> deliveryDischargeUri3 = uri
                    61 -> deliveryDischargeUri4 = uri
                }
                viewModel.setImageUriToFormElement(uri)
                (binding.form.rvInputForm.adapter as? FormInputAdapter)?.notifyDataSetChanged()
            }
        }
    }

    private fun navigateToMdsr() {

        if (viewModel.hhId != null) {
            val action = PncFormFragmentDirections.actionPncFormFragmentToMdsrObjectFragment(
                hhId = viewModel.hhId!!,
                benId = viewModel.benId
            )

            findNavController().navigate(action)
        }
    }


    private fun showUploadReminderDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Reminder!!")
            .setMessage("Do you want to upload \"Delivery Discharge Summary\" photo copy to claim your Incentive.")
            .setPositiveButton("Yes") { dialog, _ ->
                dialog.dismiss()

            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
                submitAncForm()
            }
            .setCancelable(false)
            .show()
    }


    private fun isDeliveryDischargeUploaded(): Boolean {
        return deliveryDischargeUri1 != null ||
                deliveryDischargeUri2 != null ||
                deliveryDischargeUri3 != null ||
                deliveryDischargeUri4 != null
    }


    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as HomeActivity).updateActionBar(R.drawable.ic_pnc__mother, "PNC Form")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}