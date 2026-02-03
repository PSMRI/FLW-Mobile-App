package org.piramalswasthya.sakhi.ui.home_activity.eligible_couple.registration.eligible_couple_reg

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
<<<<<<< Updated upstream
import androidx.fragment.app.Fragment
=======
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.net.toUri
>>>>>>> Stashed changes
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.FormInputAdapterWithBgIcon
import org.piramalswasthya.sakhi.databinding.FragmentNewFormBinding
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
<<<<<<< Updated upstream
=======
import org.piramalswasthya.sakhi.ui.home_activity.all_ben.new_ben_registration.ben_form.BaseFormFragment
import org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pregnant_woment_anc_visits.form.PwAncFormFragment.Companion.backViewFileUri
import org.piramalswasthya.sakhi.ui.home_activity.maternal_health.pregnant_woment_anc_visits.form.PwAncFormFragment.Companion.latestTmpUri
>>>>>>> Stashed changes
import org.piramalswasthya.sakhi.work.WorkerUtils
import timber.log.Timber

@AndroidEntryPoint
class EligibleCoupleRegFragment : BaseFormFragment() {

    private var _binding: FragmentNewFormBinding? = null
    private val binding: FragmentNewFormBinding
        get() = _binding!!


    private val viewModel: EligibleCoupleRegViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewFormBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

<<<<<<< Updated upstream
=======
    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            if (success) {
                if(viewModel.getDocumentFormId() == 75) {
                    latestTmpUri?.let { uri ->
                        if (checkFileSize(uri,requireContext())) {
                            Toast.makeText(context, resources.getString(R.string.file_size), Toast.LENGTH_LONG).show()

                        } else {
                            viewModel.setImageUriToFormElement(uri)
                            binding.form.rvInputForm.apply {
                                val adapter = this.adapter as FormInputAdapterWithBgIcon
                                adapter.notifyDataSetChanged()
                            }
                            setFormAsDirty()
                        }

                    }
                } else {
                    backViewFileUri?.let { uri ->
                        if (checkFileSize(uri,requireContext())) {
                            Toast.makeText(context, resources.getString(R.string.file_size), Toast.LENGTH_LONG).show()

                        } else {
                            viewModel.setImageUriToFormElement(uri)
                            binding.form.rvInputForm.apply {
                                val adapter = this.adapter as FormInputAdapterWithBgIcon
                                adapter.notifyDataSetChanged()
                            }
                            setFormAsDirty()
                        }

                    }
                }

            }
        }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_PDF_FILE && resultCode == Activity.RESULT_OK) {
            if(viewModel.getDocumentFormId() == 31) {
                data?.data?.let { pdfUri ->
                    if (checkFileSize(pdfUri,requireContext())) {
                        Toast.makeText(context, resources.getString(R.string.file_size), Toast.LENGTH_LONG).show()

                    } else {
                        latestTmpUri = pdfUri
                        latestTmpUri?.let { uri ->
                            viewModel.setImageUriToFormElement(uri)
                            binding.form.rvInputForm.apply {
                                val adapter = this.adapter as FormInputAdapterWithBgIcon
                                adapter.notifyDataSetChanged()
                            }
                            setFormAsDirty()
                        }

//                    updateImageRecord()
                    }
                }
            } else {
                data?.data?.let { pdfUri ->
                    if (checkFileSize(pdfUri,requireContext())) {
                        Toast.makeText(context, resources.getString(R.string.file_size), Toast.LENGTH_LONG).show()

                    } else {
                        backViewFileUri = pdfUri
                        backViewFileUri?.let { uri ->
                            viewModel.setImageUriToFormElement(uri)
                            binding.form.rvInputForm.apply {
                                val adapter = this.adapter as FormInputAdapterWithBgIcon
                                adapter.notifyDataSetChanged()
                            }
                            setFormAsDirty()
                        }

//                    updateImageRecord()
                    }
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

>>>>>>> Stashed changes
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.recordExists.observe(viewLifecycleOwner) { notIt ->
            notIt?.let { recordExists ->
//                binding.fabEdit.visibility = if(recordExists) View.VISIBLE else View.GONE
                binding.btnSubmit.visibility = if (recordExists) View.GONE else View.VISIBLE
                val adapter = FormInputAdapterWithBgIcon(
                    formValueListener = FormInputAdapterWithBgIcon.FormValueListener { formId, index ->
                        viewModel.updateListOnValueChanged(formId, index)
                        hardCodedListUpdate(formId)
                    }, isEnabled = !recordExists
                )
                binding.form.rvInputForm.adapter = adapter
                lifecycleScope.launch {
                    viewModel.formList.collect {
                        if (it.isNotEmpty())

                            adapter.submitList(it)

<<<<<<< Updated upstream
=======
                    val adapter = FormInputAdapterWithBgIcon(
                        formValueListener = FormInputAdapterWithBgIcon.FormValueListener { formId, index ->
                            viewModel.updateListOnValueChanged(formId, index)
                            hardCodedListUpdate(formId)
                            setFormAsDirty()
                        }, isEnabled = !isDataExist ,
                        selectImageClickListener  = FormInputAdapterWithBgIcon.SelectUploadImageClickListener {
                            if (!BuildConfig.FLAVOR.contains("mitanin", ignoreCase = true)) {
                                viewModel.setCurrentDocumentFormId(it)
                                chooseOptions()
                            }
                        },
                        viewDocumentListner = FormInputAdapterWithBgIcon.ViewDocumentOnClick {
                            if (!recordExists) {
                                if (it == 75) {
                                    latestTmpUri?.let {
                                        if (it.toString().contains("document")) {
                                            displayPdf(it)
                                        } else {
                                            viewImage(it)
                                        }

                                    }
                                } else {
                                    backViewFileUri?.let {
                                        if (it.toString().contains("document")) {
                                            displayPdf(it)
                                        } else {
                                            viewImage(it)
                                        }

                                    }
                                }

                            } else {
                                val formId = it
                                lifecycleScope.launch {
                                    viewModel.formList.collect{
                                        if (formId == 75) {
                                            it.get(viewModel.getIndexofAshaKitPhotoFirst()).value.let {
                                                if (it.toString().contains("document")) {
                                                    displayPdf(it!!.toUri())
                                                } else {
                                                    viewImage(it!!.toUri())
                                                }
                                            }
                                        } else {
                                            it.get(viewModel.getIndexofAshaKitPhotoSecond()).value.let {
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
                    )
                    binding.form.rvInputForm.adapter = adapter
                    lifecycleScope.launch {
                        viewModel.formList.collect {
                            if (it.isNotEmpty())

                                adapter.submitList(it)

                        }
>>>>>>> Stashed changes
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
            submitEligibleCoupleForm()
        }
        viewModel.state.observe(viewLifecycleOwner) {
            when (it) {
                EligibleCoupleRegViewModel.State.SAVE_SUCCESS -> {
                    setFormAsClean()
                    findNavController().navigateUp()
                    WorkerUtils.triggerAmritPushWorker(requireContext())
                }
                EligibleCoupleRegViewModel.State.DRAFT_SAVED -> {
                    setFormAsClean()
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

    private fun submitEligibleCoupleForm() {
        if (validate()) {
            viewModel.saveForm()
        }
    }

    fun validate(): Boolean {
        val result = binding.form.rvInputForm.adapter?.let {
            (it as FormInputAdapterWithBgIcon).validateInput(resources)
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

    private fun hardCodedListUpdate(formId: Int) {
        binding.form.rvInputForm.adapter?.apply {
            when (formId) {
                17 -> {
                    notifyDataSetChanged()
                    notifyItemChanged(viewModel.getIndexOfChildLabel())
                    notifyItemChanged(viewModel.getIndexOfAge1())
                    notifyItemChanged(viewModel.getIndexOfGap1())
                    notifyItemChanged(viewModel.getIndexOfTimeLessThan18())
                }

                22 -> {
                    notifyDataSetChanged()
                    notifyItemChanged(viewModel.getIndexOfChildLabel())
                    notifyItemChanged(viewModel.getIndexOfAge2())
                    notifyItemChanged(viewModel.getIndexOfGap2())
                    notifyItemChanged(viewModel.getIndexOfTimeLessThan18())
                }

                27 -> {
                    notifyDataSetChanged()
                    notifyItemChanged(viewModel.getIndexOfChildLabel())
                    notifyItemChanged(viewModel.getIndexOfAge3())
                    notifyItemChanged(viewModel.getIndexOfGap3())
                    notifyItemChanged(viewModel.getIndexOfTimeLessThan18())
                }

                32 -> {
                    notifyDataSetChanged()
                    notifyItemChanged(viewModel.getIndexOfChildLabel())
                    notifyItemChanged(viewModel.getIndexOfAge4())
                    notifyItemChanged(viewModel.getIndexOfGap4())
                    notifyItemChanged(viewModel.getIndexOfTimeLessThan18())
                }

                37 -> {
                    notifyDataSetChanged()
                    notifyItemChanged(viewModel.getIndexOfChildLabel())
                    notifyItemChanged(viewModel.getIndexOfAge5())
                    notifyItemChanged(viewModel.getIndexOfGap5())
                    notifyItemChanged(viewModel.getIndexOfTimeLessThan18())
                }

                42 -> {
                    notifyDataSetChanged()
                    notifyItemChanged(viewModel.getIndexOfChildLabel())
                    notifyItemChanged(viewModel.getIndexOfAge6())
                    notifyItemChanged(viewModel.getIndexOfGap6())
                    notifyItemChanged(viewModel.getIndexOfTimeLessThan18())
                }

                47 -> {
                    notifyDataSetChanged()
                    notifyItemChanged(viewModel.getIndexOfChildLabel())
                    notifyItemChanged(viewModel.getIndexOfAge7())
                    notifyItemChanged(viewModel.getIndexOfGap7())
                    notifyItemChanged(viewModel.getIndexOfTimeLessThan18())
                }

                52 -> {
                    notifyDataSetChanged()
                    notifyItemChanged(viewModel.getIndexOfChildLabel())
                    notifyItemChanged(viewModel.getIndexOfAge8())
                    notifyItemChanged(viewModel.getIndexOfGap8())
                    notifyItemChanged(viewModel.getIndexOfTimeLessThan18())
                }

                57 -> {
                    notifyDataSetChanged()
                    notifyItemChanged(viewModel.getIndexOfChildLabel())
                    notifyItemChanged(viewModel.getIndexOfAge9())
                    notifyItemChanged(viewModel.getIndexOfGap9())
                    notifyItemChanged(viewModel.getIndexOfTimeLessThan18())
                }

                12 -> {
                    notifyItemChanged(viewModel.getIndexOfLiveChildren())
                    notifyItemChanged(viewModel.getIndexOfMaleChildren())
                    notifyItemChanged(viewModel.getIndexOfFeMaleChildren())
                }

                19, 24, 29, 34, 39, 44, 49, 54, 59 -> {
                    notifyItemChanged(viewModel.getIndexOfMaleChildren())
                    notifyItemChanged(viewModel.getIndexOfFeMaleChildren())
                }

                13 -> {
                    notifyItemChanged(viewModel.getIndexOfChildren())
                }

                61, 62 -> {
                    notifyItemChanged(viewModel.getIndexOfChildLabel())
                }

                63, 64 -> {
                    notifyItemChanged(viewModel.getIndexOfPhysicalObservationLabel())
                }

                65, 66, 67, 68 -> {
                    notifyItemChanged(viewModel.getIndexOfObstetricHistoryLabel())
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as HomeActivity).updateActionBar(
                R.drawable.ic__eligible_couple,
                getString(R.string.eligible_couple_registration)
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

<<<<<<< Updated upstream
=======
    fun showAlertDialog(context: Context, title: String, message: String) {
        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
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

    private fun takeImage() {
        lifecycleScope.launchWhenStarted {
            getTmpFileUri().let { uri ->
                if(viewModel.getDocumentFormId() == 75) {
                    latestTmpUri = uri
                    if (checkFileSize(uri,requireContext())) {
                        Toast.makeText(context, resources.getString(R.string.file_size), Toast.LENGTH_LONG).show()
                    } else {
                        takePicture.launch(latestTmpUri)
                    }
                } else {
                    backViewFileUri = uri
                    if (checkFileSize(uri,requireContext())) {
                        Toast.makeText(context, resources.getString(R.string.file_size), Toast.LENGTH_LONG).show()
                    } else {
                        takePicture.launch(backViewFileUri)
                    }
                }


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

    override fun saveDraft() {
        viewModel.saveDraft()
    }
>>>>>>> Stashed changes
}