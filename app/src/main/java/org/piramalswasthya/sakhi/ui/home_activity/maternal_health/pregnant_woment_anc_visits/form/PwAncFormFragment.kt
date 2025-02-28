package org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pregnant_woment_anc_visits.form

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
import org.piramalswasthya.sakhi.adapters.FormInputAdapterWithBgIcon
import org.piramalswasthya.sakhi.databinding.FragmentNewFormBinding
import org.piramalswasthya.sakhi.databinding.LayoutMediaOptionsBinding
import org.piramalswasthya.sakhi.databinding.LayoutViewMediaBinding
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pregnant_woment_anc_visits.form.PwAncFormViewModel.State
import org.piramalswasthya.sakhi.work.WorkerUtils
import timber.log.Timber
import java.io.File

@AndroidEntryPoint
class PwAncFormFragment : Fragment() {

    private var _binding: FragmentNewFormBinding? = null
    private val binding: FragmentNewFormBinding
        get() = _binding!!

    private val viewModel: PwAncFormViewModel by viewModels()

    companion object {
        var latestTmpUri: Uri? = null
    }

    private val PICK_PDF_FILE = 1



    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            if (success) {
                latestTmpUri?.let { uri ->
                    viewModel.setImageUriToFormElement(uri)
                    binding.form.rvInputForm.apply {
                        val adapter = this.adapter as FormInputAdapterWithBgIcon
                        adapter.notifyDataSetChanged()
                    }
//                    updateImageRecord()

                    Timber.d("Image saved at @ $uri")
                }
            }
        }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_PDF_FILE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { pdfUri ->
                latestTmpUri = pdfUri
                latestTmpUri?.let { uri ->
                    viewModel.setImageUriToFormElement(uri)
                    binding.form.rvInputForm.apply {
                        val adapter = this.adapter as FormInputAdapterWithBgIcon
                        adapter.notifyDataSetChanged()
                    }
//                    updateImageRecord()
                    Timber.d("Image saved at @ $uri")
                }
            }
        }
    }

    private fun displayPdf(pdfUri: Uri) {
        activity?.contentResolver?.takePersistableUriPermission(
            pdfUri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(pdfUri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "Open PDF with"))

    }

  private fun updateImageRecord() {
        viewModel.recordExists.observe(viewLifecycleOwner) { notIt ->
            notIt?.let { it ->
                submitAncForm()
            }
        }
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
                binding.fabEdit.visibility = /*if (recordExists) View.VISIBLE else */View.GONE
                binding.btnSubmit.visibility = if (recordExists) View.GONE else View.VISIBLE
                val adapter = FormInputAdapterWithBgIcon(
                    formValueListener = FormInputAdapterWithBgIcon.FormValueListener { formId, index ->
                        viewModel.updateListOnValueChanged(formId, index)
                        hardCodedListUpdate(formId)
                    }, isEnabled = !recordExists,
                    selectImageClickListener  = FormInputAdapterWithBgIcon.SelectUploadImageClickListener {
                        viewModel.setCurrentDocumentFormId(it)
                        chooseOptions()
                    },
                    viewDocumentListner = FormInputAdapterWithBgIcon.ViewDocumentOnClick {
                        if (!recordExists) {
                            latestTmpUri?.let {
                                if (it.toString().contains("document")) {
                                    displayPdf(it)
                                } else {
                                    viewImage(it)
                                }

                            }
                        } else {
                            lifecycleScope.launch {
                                viewModel.formList.collect{
                                    it.get(viewModel.getIndexOfFile()).value.let {
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
                1 -> {
                    notifyItemChanged(viewModel.getIndexOfWeeksOfPregnancy())
                    notifyItemChanged(viewModel.getIndexOfWeeksOfPregnancy() + 1)
                }

                16 -> {
                    notifyItemChanged(viewModel.getIndexOfTTBooster())
                }

                18 -> {
                    notifyItemChanged(viewModel.getIndexOfTT1())
                    notifyItemChanged(viewModel.getIndexOfTT2())

                }
            }
        }
    }


    private fun takeImage() {
        lifecycleScope.launchWhenStarted {
            getTmpFileUri().let { uri ->
                latestTmpUri = uri
                takePicture.launch(uri)
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

    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as HomeActivity).updateActionBar(
                R.drawable.ic__anc_visit,
                getString(R.string.anc_visit)
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}