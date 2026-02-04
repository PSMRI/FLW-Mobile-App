package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.infant_reg.form

import android.Manifest
import android.content.Context
import android.content.Intent
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
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import org.piramalswasthya.sakhi.ui.home_activity.all_ben.new_ben_registration.ben_form.BaseFormFragment
import org.piramalswasthya.sakhi.work.WorkerUtils
import timber.log.Timber
import java.io.File

@AndroidEntryPoint
class InfantRegFragment : BaseFormFragment() {

    private var _binding: FragmentNewFormBinding? = null
    private val binding: FragmentNewFormBinding
        get() = _binding!!

    private val viewModel: InfantRegViewModel by viewModels()


    private val requestLocationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) requestLocationPermission()
            else findNavController().navigateUp()
        }

    private val deliveryDischargeUris: MutableMap<Int, Uri?> = mutableMapOf(
        58 to null, 59 to null, 60 to null, 61 to null
    )

    private val PICK_IMAGE = 1

    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                val formId = viewModel.getDocumentFormId()
                getUriByFormId(formId)?.let { uri -> updateAdapterForFormId(formId, uri) }
                setFormAsDirty()
            }
        }


    private val formIdToIndex: Map<Int, () -> Int> by lazy {
        mapOf(
            58 to { viewModel.getIndexDeliveryDischargeSummary1() },
            59 to { viewModel.getIndexDeliveryDischargeSummary2() },
            60 to { viewModel.getIndexDeliveryDischargeSummary3() },
            61 to { viewModel.getIndexDeliveryDischargeSummary4() }
        )
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewFormBinding.inflate(layoutInflater, container, false)
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
                        if (formId == 57) {
                            (binding.form.rvInputForm.adapter as? FormInputAdapter)?.notifyDataSetChanged()
                        }
                        setFormAsDirty()
                    },
                    viewDocumentListner = FormInputAdapter.ViewDocumentOnClick { formId ->
                        val localUri = getUriByFormId(formId)
                        if (recordExists) {
                            viewDocuments(formId)
                        } else if (localUri != null) {
                            showImageDialog(localUri)
                        }
                    },
                    isEnabled = !recordExists
                )
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
            submitInfantRegForm()
        }

        viewModel.state.observe(viewLifecycleOwner) {
            when (it) {
                InfantRegViewModel.State.IDLE -> {
                }

                InfantRegViewModel.State.SAVING -> {
                    binding.llContent.visibility = View.GONE
                    binding.pbForm.visibility = View.VISIBLE
                }

                InfantRegViewModel.State.SAVE_SUCCESS -> {
                    setFormAsClean()
                    binding.llContent.visibility = View.VISIBLE
                    binding.pbForm.visibility = View.GONE
                    Toast.makeText(context, "Save Successful!!!", Toast.LENGTH_LONG).show()
                    WorkerUtils.triggerAmritPushWorker(requireContext())
                    findNavController().navigateUp()
                }

                InfantRegViewModel.State.SAVE_FAILED -> {
                    Toast.makeText(

                        context, "Something wend wong! Contact testing!", Toast.LENGTH_LONG
                    ).show()
                    binding.llContent.visibility = View.VISIBLE
                    binding.pbForm.visibility = View.GONE
                }
                InfantRegViewModel.State.DRAFT_SAVED -> {
                    setFormAsClean()
                    binding.llContent.visibility = View.VISIBLE
                    binding.pbForm.visibility = View.GONE
                    Toast.makeText(
                        context,
                        "Draft saved successfully",
                        Toast.LENGTH_LONG
                    ).show()
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


    private fun showMediaOptions() {
        val alertBinding = LayoutMediaOptionsBinding.inflate(layoutInflater, binding.root, false)
        alertBinding.btnPdf.visibility = View.GONE

        val alertDialog = MaterialAlertDialogBuilder(requireContext())
            .setView(alertBinding.root)
            .setCancelable(true)
            .create()

        alertBinding.btnCamera.setOnClickListener {
            alertDialog.dismiss(); takeImage()
        }
        alertBinding.btnGallery.setOnClickListener {
            alertDialog.dismiss(); selectImage()
        }
        alertBinding.btnCancel.setOnClickListener { alertDialog.dismiss() }
        alertDialog.show()
    }

    private fun takeImage() {
        lifecycleScope.launchWhenStarted {
            getTmpFileUri().let { uri ->
                setUriForFormId(viewModel.getDocumentFormId(), uri)
                takePicture.launch(uri)
            }
        }
    }

    private fun setUriForFormId(formId: Int, uri: Uri) {
        deliveryDischargeUris[formId] = uri
    }

    private fun getTmpFileUri(): Uri {
        val imagesDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val tmpFile = File.createTempFile(Konstants.tempBenImagePrefix, ".jpg", imagesDir)
        return FileProvider.getUriForFile(
            requireContext(), "${BuildConfig.APPLICATION_ID}.provider", tmpFile
        )
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
        startActivityForResult(intent, PICK_IMAGE)
    }

    private fun viewDocuments(formId: Int) {
        formIdToIndex[formId]?.invoke()?.let { index -> observeAndViewDocument(index) }
    }

    private fun observeAndViewDocument(index: Int) {
        lifecycleScope.launch {
            viewModel.formList.collect { list ->
                list.getOrNull(index)?.value?.let { showImageDialog(it.toUri()) }
            }
        }
    }

    private fun getUriByFormId(formId: Int): Uri? = deliveryDischargeUris[formId]

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            handleImageResult(viewModel.getDocumentFormId(), data?.data)
        }
    }

    private fun handleImageResult(formId: Int, uri: Uri?) {
        uri?.let {
            if (checkFileSize(it, requireContext())) {
                Toast.makeText(context, resources.getString(R.string.file_size), Toast.LENGTH_LONG).show()
            } else {
                updateAdapterForFormId(formId, it)
                setFormAsDirty()
            }
        }
    }

    private fun checkFileSize(uri: Uri, context: Context): Boolean {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val fileSize = inputStream?.available() ?: 0
            inputStream?.close()
            fileSize > 2 * 1024 * 1024 // 2MB
        } catch (e: Exception) {
            false
        }
    }

    private fun updateAdapterForFormId(formId: Int, uri: Uri) {
        setUriForFormId(formId, uri)
        viewModel.setImageUriToFormElement(uri)
        (binding.form.rvInputForm.adapter as? FormInputAdapter)?.notifyDataSetChanged()
    }
    private fun hardCodedListUpdate(formId: Int) {
        if (formId == 1) binding.form.rvInputForm.adapter?.notifyItemChanged(1)
    }

    private fun showImageDialog(uri: Uri) {
        val viewImageBinding = LayoutViewMediaBinding.inflate(layoutInflater, binding.root, false)
        val alertDialog = MaterialAlertDialogBuilder(requireContext())
            .setView(viewImageBinding.root)
            .setCancelable(true)
            .create()
        Glide.with(this).load(uri).placeholder(R.drawable.ic_person).into(viewImageBinding.viewImage)
        viewImageBinding.btnClose.setOnClickListener { alertDialog.dismiss() }
        alertDialog.show()
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

    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as HomeActivity).updateActionBar(
                R.drawable.ic__infant_registration,
                getString(R.string.infant_reg)
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun saveDraft() {
        viewModel.saveDraft()
    }


}
