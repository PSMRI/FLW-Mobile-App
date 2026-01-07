package org.piramalswasthya.sakhi.ui.home_activity.immunization_due.child_immunization.form

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.CompoundButton.OnCheckedChangeListener
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.BuildConfig
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.ChildImmunizationVaccineAdapter
import org.piramalswasthya.sakhi.adapters.FormInputAdapter
import org.piramalswasthya.sakhi.databinding.FragmentImmunizationFormBinding
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.model.ImmunizationDetailsDomain
import org.piramalswasthya.sakhi.ui.checkFileSize
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import org.piramalswasthya.sakhi.ui.home_activity.immunization_due.child_immunization.form.ImmunizationFormViewModel.State
import org.piramalswasthya.sakhi.utils.HelperUtil.createTempImageUri
import org.piramalswasthya.sakhi.utils.HelperUtil.showImageDialog
import org.piramalswasthya.sakhi.utils.HelperUtil.showMediaOptionsDialog
import org.piramalswasthya.sakhi.utils.HelperUtil.showUploadReminderDialog
import org.piramalswasthya.sakhi.work.WorkerUtils
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.collections.set

@AndroidEntryPoint
class ImmunizationFormFragment : Fragment(), OnCheckedChangeListener {


    private var _binding: FragmentImmunizationFormBinding? = null
    private val binding: FragmentImmunizationFormBinding
        get() = _binding!!

    var selectAll = false





    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                    handleImageResult(viewModel.getDocumentFormId(), uri)
                }


    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) handleCameraResult()
        }


    private val mcpCardUris : MutableMap<Int , Uri?>  = mutableMapOf(
        111 to null, 112 to null
    )
    private val mcpCardsIndex:Map<Int, () -> Int> by lazy {
        mapOf(
            111 to { viewModel.getIndexMCPCard1() },
            112 to { viewModel.getIndexMCPCard2() }
        )
    }
    private val viewModel: ImmunizationFormViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // _binding = FragmentNewFormBinding.inflate(inflater, container, false)
        _binding = FragmentImmunizationFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.checkBox.setOnCheckedChangeListener(this)
        binding.tvDoseName.text = getString(R.string.dose_name_2, viewModel.vaccineCategory)
        viewModel.benName.observe(viewLifecycleOwner) {
            binding.tvBenName.text = it
        }
        viewModel.benAgeGender.observe(viewLifecycleOwner) {
            binding.tvAgeGender.text = it
        }


        binding.llPatientInformation2.visibility = View.VISIBLE
        //     binding.llBenPwrTrackingDetails2.visibility = View.GONE
        binding.llBenPwrTrackingDetails3.visibility = View.GONE
        viewModel.benRegCache.observe(viewLifecycleOwner) {
            binding.tvTitleHusband.text = resources.getString(R.string.mother_s_name)
            binding.husbandName.text = it.motherName

            binding.tvTitleMobileNumber.text = resources.getString(R.string.father_s_name)
            binding.mobileNumber.text = it.fatherName

            binding.tvTitleBeneficiaryId.text = resources.getString(R.string.date_of_birth)
            binding.benId.text = getDateFromLong(it.dob)

            binding.tvTitleRCHID.text = getString(R.string.child_rch_id)

        }
        binding.btnSubmit.setOnClickListener {
            if (!ismcpCardUploaded()) requireContext().showUploadReminderDialog(getString(R.string.do_you_want_to_upload_mcp_card_immunization_page_section_photo_copy_to_claim_your_incentive)){submitImmForm()}
            else
            submitImmForm()
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
                    Toast.makeText(
                        context,
                        resources.getString(R.string.save_successful),
                        Toast.LENGTH_LONG
                    ).show()
                    WorkerUtils.triggerAmritPushWorker(requireContext())
                    findNavController().navigateUp()
                }

                State.SAVE_FAILED -> {
                    Toast.makeText(
                        context,
                        resources.getString(R.string.something_wend_wong_contact_testing),
                        Toast.LENGTH_LONG
                    ).show()
                    binding.llContent.visibility = View.VISIBLE
                    binding.pbForm.visibility = View.GONE
                }
            }
        }

//        binding.fabEdit.setOnClickListener {
//            viewModel.updateRecordExists(false)
//        }


        viewModel.recordExists.observe(viewLifecycleOwner) { notIt ->
            notIt?.let { recordExists -> updateSubmitButtonVisibility()
               // binding.btnSubmit.visibility = if (recordExists) View.GONE else View.VISIBLE
                //    binding.fabEdit.visibility = if (recordExists) View.VISIBLE else View.GONE
                val adapter = FormInputAdapter(
                    selectImageClickListener = FormInputAdapter.SelectUploadImageClickListener { formId ->
                        if (!BuildConfig.FLAVOR.contains("mitanin", ignoreCase = true)) {
                            viewModel.setCurrentDocumentFormId(formId)
                            requireContext().showMediaOptionsDialog(
                                onCameraClick = { takeImage() },
                                onGalleryClick = { selectImage() }
                            )
                        }
                    },
                    formValueListener = FormInputAdapter.FormValueListener { formId, index ->
                        viewModel.updateListOnValueChanged(formId, index)
                    },
                    viewDocumentListner = FormInputAdapter.ViewDocumentOnClick { formId ->
                        if (recordExists) viewDocuments(formId)
                        else getUriByFormId(formId)?.let { uri -> requireContext().showImageDialog(uri) }
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

        binding.rvImmCat.adapter =
            ChildImmunizationVaccineAdapter(ChildImmunizationVaccineAdapter.ImmunizationClickListener { position, item ->
                if (item.isSwitchChecked) {
                    viewModel.vaccinationDoneList.add(item)
                    viewModel.list[position].isSwitchChecked = item.isSwitchChecked
                    var count = 0
                    viewModel.list.forEach {it->
                          if (it.state.name == "PENDING" || it.state.name == "OVERDUE") {
                             count++
                         }
                    }

                    if (count == viewModel.vaccinationDoneList.size){
                        binding.checkBox.setOnCheckedChangeListener(null)
                        binding.checkBox.isChecked = true
                        selectAll = true
                        binding.checkBox.setOnCheckedChangeListener(this)
                    }
                } else {
                    viewModel.vaccinationDoneList.removeIf { it ->
                        it.vaccineName == item.vaccineName
                    }
                    viewModel.list[position].isSwitchChecked = item.isSwitchChecked

                    binding.checkBox.setOnCheckedChangeListener(null)
                    binding.checkBox.isChecked = false
                    binding.checkBox.setOnCheckedChangeListener(this)
                }
                updateSubmitButtonVisibility()
                (binding.rvImmCat.adapter as ChildImmunizationVaccineAdapter).notifyItemChanged(
                    position
                )
            })

        lifecycleScope.launch {
            viewModel.bottomSheetContent.collect {
                it?.let {
                    submitListToVaccinationRv(it)
                }
            }
        }

    }

    private fun updateSubmitButtonVisibility() {
        val recordExists = viewModel.recordExists.value ?: true
        val hasSelections = viewModel.vaccinationDoneList.isNotEmpty()
        binding.btnSubmit.visibility = if (!recordExists && hasSelections) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun ismcpCardUploaded(): Boolean =
        mcpCardUris.values.any { it != null }
    private fun submitListToVaccinationRv(detail: ImmunizationDetailsDomain) {
        viewModel.list =
            detail.vaccineStateList.filter { it.vaccineCategory.name == viewModel.vaccineCategory }
        (_binding?.rvImmCat?.adapter as ChildImmunizationVaccineAdapter?)?.submitList(viewModel.list)
    }



    private fun takeImage() {
        lifecycleScope.launchWhenStarted {
            val uri = requireContext().createTempImageUri()
            mcpCardUris[viewModel.getDocumentFormId()] = uri
                takePictureLauncher.launch(uri)
            }
        }




    private fun getUriByFormId(formId: Int): Uri? = mcpCardUris[formId]

    private fun setUriForFormId(formId: Int, uri: Uri) {
        mcpCardUris[formId] = uri
    }
    private fun handleCameraResult() {
        val formId = viewModel.getDocumentFormId()
        mcpCardUris[formId]?.let { uri ->
                   if (checkFileSize(uri, requireContext())) {
                            Toast.makeText(context, resources.getString(R.string.file_size), Toast.LENGTH_LONG).show()
                            mcpCardUris[formId] = null
                        } else {
                            updateAdapterForFormId(formId, uri)
                        }
                }
    }

    private fun updateAdapterForFormId(formId: Int, uri: Uri) {
        setUriForFormId(formId, uri)
        viewModel.setImageUriToFormElement(uri)
        (binding.form.rvInputForm.adapter as? FormInputAdapter)?.notifyDataSetChanged()
    }

    private fun getTmpFileUri(): Uri {
        val imagesDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val tmpFile = File.createTempFile(Konstants.tempBenImagePrefix, ".jpg", imagesDir)
        return FileProvider.getUriForFile(
            requireContext(), "${BuildConfig.APPLICATION_ID}.provider", tmpFile
        )
    }

    private fun selectImage() {
        pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun viewDocuments(formId: Int) {
        mcpCardsIndex[formId]?.invoke()?.let { index -> observeAndViewDocument(index) }

    }

    private fun observeAndViewDocument(index: Int) {
        lifecycleScope.launch {
            viewModel.formList.collect { list ->
                list.getOrNull(index)?.value?.let {  requireContext().showImageDialog(it.toUri()) }
            }
        }
    }




  /*  @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            handleImageResult(viewModel.getDocumentFormId(), data?.data)
        }
    }
*/
    private fun handleImageResult(formId: Int, uri: Uri?) {
        uri?.let {
            if (checkFileSize(it, requireContext())) {
                Toast.makeText(context, resources.getString(R.string.file_size), Toast.LENGTH_LONG).show()
            } else updateAdapterForFormId(formId, it)
        }
    }

    private fun submitImmForm() {
        if (validateCurrentPage()) {
            //viewModel.saveForm()
            viewModel.saveImmunization()

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

    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as HomeActivity).updateActionBar(
                R.drawable.ic__immunization,
                getString(R.string.immunization)
            )
        }
    }

    fun getDateFromLong(dateLong: Long): String? {
        if (dateLong == 0L) return null
        val cal = Calendar.getInstance()
        cal.timeInMillis = dateLong
        val f = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
        return f.format(cal.time)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCheckedChanged(p0: CompoundButton?, isChecked: Boolean) {
        if (isChecked) {
            viewModel.list.forEach { item ->
                if (item.state.name == "PENDING" || item.state.name == "OVERDUE") {
                    item.isSwitchChecked = true
                    viewModel.vaccinationDoneList.add(item)
                }
            }
            selectAll = true

        } else {
            if (selectAll) {
                viewModel.list.forEach { item ->
                    if (item.state.name == "PENDING" || item.state.name == "OVERDUE") {
                        item.isSwitchChecked = false

                        viewModel.vaccinationDoneList.removeIf { it ->
                            it.vaccineName == item.vaccineName
                        }

                    }
                    selectAll = false
                }
            }

        }
        updateSubmitButtonVisibility()
        (binding.rvImmCat.adapter as ChildImmunizationVaccineAdapter).notifyDataSetChanged()
    }

}