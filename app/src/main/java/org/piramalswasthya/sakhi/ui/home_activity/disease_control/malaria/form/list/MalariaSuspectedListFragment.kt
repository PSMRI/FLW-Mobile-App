package org.piramalswasthya.sakhi.ui.home_activity.disease_control.malaria.form.list

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.FormInputAdapter
import org.piramalswasthya.sakhi.adapters.IrsRoundListAdapter
import org.piramalswasthya.sakhi.adapters.MalariaMemberListAdapter
import org.piramalswasthya.sakhi.adapters.dynamicAdapter.BottleAdapter
import org.piramalswasthya.sakhi.adapters.dynamicAdapter.FormRendererAdapter
import org.piramalswasthya.sakhi.configuration.IconDataset
import org.piramalswasthya.sakhi.configuration.dynamicDataSet.FormField
import org.piramalswasthya.sakhi.databinding.FragmentDisplaySearchRvButtonBinding
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import org.piramalswasthya.sakhi.ui.home_activity.disease_control.malaria.form.mosquito_net.MosquitoNetFormViewModel
import org.piramalswasthya.sakhi.utils.HelperUtil.compressImageToTemp
import org.piramalswasthya.sakhi.utils.HelperUtil.fileToBase64
import org.piramalswasthya.sakhi.utils.HelperUtil.getFileSizeInMB
import org.piramalswasthya.sakhi.utils.HelperUtil.launchCamera
import org.piramalswasthya.sakhi.utils.HelperUtil.launchFilePicker
import org.piramalswasthya.sakhi.utils.HelperUtil.showPickerDialog
import org.piramalswasthya.sakhi.utils.dynamicFiledValidator.FieldValidator
import org.piramalswasthya.sakhi.utils.dynamicFormConstants.FormConstants.MOSQUITO_NET_FORM_ID
import org.piramalswasthya.sakhi.work.WorkerUtils
import org.piramalswasthya.sakhi.work.dynamicWoker.MosquitoNetFormSyncWorker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.getValue


@AndroidEntryPoint
class MalariaSuspectedListFragment : Fragment() {
    private var _binding: FragmentDisplaySearchRvButtonBinding? = null
    private val binding: FragmentDisplaySearchRvButtonBinding
        get() = _binding!!
    private lateinit var adapter: FormRendererAdapter
    private val args: MalariaSuspectedListFragmentArgs by navArgs()
    private val viewModel: MalariaSuspectedViewModel by viewModels()
    private val viewModelMosquitoNet: MosquitoNetFormViewModel by viewModels()
    private val irsViewModel: MalariaIRSViewModel by viewModels()
    private val irsListViewmodel: IRSRoundListViewModel by viewModels()
    var hhId = -1L
    private var currentImageField: FormField? = null
    private var tempCameraUri: Uri? = null

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && tempCameraUri != null) {
                val context = requireContext()
                val uri = tempCameraUri!!

                val sizeInMB = context.getFileSizeInMB(uri)
                val maxSize = (currentImageField?.validation?.maxSizeMB ?: 5).toDouble()

                if (sizeInMB != null && sizeInMB > maxSize) {
                    currentImageField?.errorMessage =
                        currentImageField?.validation?.errorMessage
                            ?: "Image must be less than ${maxSize.toInt()}MB"
                    adapter.notifyDataSetChanged()
                    return@registerForActivityResult
                }

                val compressedFile = compressImageToTemp(uri, "camera_image", context)
                val base64String = compressedFile?.let { fileToBase64(it) }

                currentImageField?.apply {
                    value = base64String
                    errorMessage = null
                }
                adapter.notifyDataSetChanged()
            }
        }

    private val filePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data ?: return@registerForActivityResult
                val context = requireContext()

                val sizeInMB = context.getFileSizeInMB(uri)
                val maxSize = (currentImageField?.validation?.maxSizeMB ?: 5).toDouble()

                if (sizeInMB != null && sizeInMB > maxSize) {
                    currentImageField?.errorMessage =
                        currentImageField?.validation?.errorMessage
                            ?: "File must be less than ${maxSize.toInt()}MB"
                    adapter.notifyDataSetChanged()
                    return@registerForActivityResult
                }

                val compressedFile = compressImageToTemp(uri, "selected_image", context)
                val base64String = compressedFile?.let { fileToBase64(it) }

                currentImageField?.apply {
                    value = base64String
                    errorMessage = null
                }
                adapter.notifyDataSetChanged()
            }
        }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDisplaySearchRvButtonBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hhId = args.hhId
        MosquitoNetFormSyncWorker.enqueue(requireContext())
        binding.switchBtnMosquito.visibility = View.VISIBLE
        binding.btnNextPage.visibility = View.GONE
        binding.llSearch.visibility = View.GONE
        if (viewModel.isFromDisease == 1 && viewModel.diseaseType == IconDataset.Disease.MALARIA.toString()) {
            binding.switchButton.visibility = View.VISIBLE
            binding.switchBtnMosquito.visibility = View.VISIBLE
        } else if(viewModel.isFromDisease == 1) {
            binding.switchButton.visibility = View.GONE
            binding.switchBtnMosquito.visibility = View.GONE
        } else {
            binding.switchButton.visibility = View.GONE
            binding.switchBtnMosquito.visibility = View.GONE
        }

        binding.apply {
            switchButton.text = if (switchButton.isChecked) "IRS ON" else "IRS OFF"
            switchButton.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    if (switchBtnMosquito.isChecked) {
                        switchBtnMosquito.isChecked = false
                    }
                }

                switchButton.text = if (isChecked) "IRS ON" else "IRS OFF"
                llContent.visibility = if (isChecked) View.VISIBLE else View.GONE
            }

            switchBtnMosquito.text = if (switchBtnMosquito.isChecked)
                getString(R.string.mosquito_net_on)
            else
                getString(R.string.mosquito_net_off)

            switchBtnMosquito.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    if (switchButton.isChecked) {
                        switchButton.isChecked = false
                    }
                }

                switchBtnMosquito.text = if (isChecked)
                    getString(R.string.mosquito_net_on)
                else
                    getString(R.string.mosquito_net_off)

                mosquitoLl.visibility = if (isChecked) View.VISIBLE else View.GONE
            }
        }


        val benAdapter = MalariaMemberListAdapter(
            clickListener = MalariaMemberListAdapter.ClickListener { hhId, benId ->
                findNavController().navigate(
                    MalariaSuspectedListFragmentDirections.actionMalariaSuspectedListFragmentToMalariaFormFragment(
                        benId = benId
                    )
                )
            },
        )
        binding.rvAny.adapter = benAdapter

        lifecycleScope.launch {
            viewModel.allBenList.collect {
                if (it.isEmpty())
                    binding.flEmpty.visibility = View.VISIBLE
                else
                    binding.flEmpty.visibility = View.GONE
                benAdapter.submitList(it)
            }
        }

        irsViewModel.checkSubmitButtonVisibility()

        lifecycleScope.launchWhenStarted {
            irsViewModel.isSubmitVisible.collect { isVisible ->
                binding.btnSubmit.isEnabled = if (isVisible) true else false
            }
        }


        val irsListAdapter = IrsRoundListAdapter()
        binding.irsAny.adapter = irsListAdapter
        lifecycleScope.launch {
            irsListViewmodel.allBenList.collect {
                irsListAdapter.submitList(it)
            }
        }

        val adapter = FormInputAdapter(
            formValueListener = FormInputAdapter.FormValueListener { formId, index ->
                irsViewModel.updateListOnValueChanged(formId, index)
            }, isEnabled = true
        )
        binding.form.rvInputForm.adapter = adapter
        lifecycleScope.launch {
            irsViewModel.formList.collect {
                if (it.isNotEmpty()) {
                    adapter.notifyItemChanged(irsViewModel.getIndexOfDate())
                    adapter.submitList(it)
                }

            }
        }

        binding.btnSubmit.setOnClickListener {
            submitIRSScreeningForm()
        }

        irsViewModel.state.observe(viewLifecycleOwner) {
            when (it) {
                MalariaIRSViewModel.State.SAVE_SUCCESS -> {
                    Toast.makeText(
                        requireContext(),
                        resources.getString(R.string.irs_submitted), Toast.LENGTH_SHORT
                    ).show()
                    WorkerUtils.triggerAmritPushWorker(requireContext())
                    findNavController().navigateUp()
                }

                else -> {}
            }
        }

        viewModelMosquitoNet.loadFormSchema(hhId,  MOSQUITO_NET_FORM_ID, false)

        binding.mosquitoRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        lifecycleScope.launch {
            viewModelMosquitoNet.schema.collectLatest { schema ->
                if (schema == null) {
                    return@collectLatest
                }
                refreshAdapter()
            }
        }

        binding.saveBtn.setOnClickListener {
            handleFormSubmission()
        }
        tableRender()
    }

    private fun tableRender() {
        binding.includeBottleTable.tableHeading.visibility = View.GONE
        binding.includeBottleTable.bottleNum.text = "Net Distributed? (Yes/No)"
        binding.includeBottleTable.date.text = "Date"

        binding.includeBottleTable.tableRv.layoutManager = LinearLayoutManager(requireContext())
        viewModelMosquitoNet.bottleList.observe(viewLifecycleOwner) { list ->

            if (!list.isNullOrEmpty()) {
                binding.includeBottleTable.llTable.visibility = View.VISIBLE
                binding.includeBottleTable.tableRv.adapter = BottleAdapter(list)
            } else {
                binding.includeBottleTable.llTable.visibility = View.GONE
            }
        }
        viewModelMosquitoNet.loadBottleData(hhId)
    }

    private fun refreshAdapter(){
        val visibleFields = viewModelMosquitoNet.getVisibleFields().toMutableList()
        val minVisitDate = viewModelMosquitoNet.getMinVisitDate()
        val maxVisitDate = viewModelMosquitoNet.getMaxVisitDate()
        //Changed By Kunal
        adapter = FormRendererAdapter(
            visibleFields,
            isViewOnly = false,
            minVisitDate = minVisitDate,
            maxVisitDate = maxVisitDate,
            onValueChanged =
                { field, value ->
                    if (value == "pick_image") {
                        currentImageField = field
                        showImagePickerDialog()
                    } else {
                        field.value = value
                        viewModelMosquitoNet.updateFieldValue(field.fieldId, value)
                        adapter.updateFields(viewModelMosquitoNet.getVisibleFields())
                    }
                },)

        binding.mosquitoRecyclerView.adapter = adapter
    }

    private fun showImagePickerDialog() {
        showPickerDialog(
            requireContext(),
            onCameraSelected = {
                tempCameraUri = launchCamera(requireContext())
                cameraLauncher.launch(tempCameraUri)
            },
            onFileSelected = {
                launchFilePicker(filePickerLauncher)
            }
        )
    }

    private fun handleFormSubmission() {
        val currentSchema = viewModelMosquitoNet.schema.value ?: return
        val previousVisitDate = viewModelMosquitoNet.previousVisitDate

        val updatedFields = adapter.getUpdatedFields()
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val today = Date()

        currentSchema.sections.forEach { section ->
            section.fields.forEach { schemaField ->
                updatedFields.find { it.fieldId == schemaField.fieldId }?.let { updated ->
                    schemaField.value = updated.value

                    val result = FieldValidator.validate(updated, null)
                    updated.errorMessage = if (!result.isValid) result.errorMessage else null
                    schemaField.errorMessage = updated.errorMessage
                    if (schemaField.fieldId == "visit_date" && schemaField.value is String) {
                        val visitDateStr = schemaField.value as String
                        val visitDate = try {
                            sdf.parse(visitDateStr)
                        } catch (e: Exception) {
                            null
                        }

                        val errorMessage = when {
                            visitDate == null -> "Invalid visit date"
                            today != null && visitDate.after(today) -> "Visit Date cannot be after today's date"
                            previousVisitDate != null && !visitDate.after(previousVisitDate) ->
                                "Visit Date must be after previous visit (${sdf.format(previousVisitDate)})"
                            else -> null
                        }
                        schemaField.errorMessage = errorMessage
                        updated.errorMessage = errorMessage
                    }
                }
            }
        }

        updatedFields.forEach { adapterField ->
            currentSchema.sections.flatMap { it.fields }
                .find { it.fieldId == adapterField.fieldId }
                ?.let { schemaField ->
                    adapterField.errorMessage = schemaField.errorMessage
                }
        }

        val copiedFields = updatedFields.map { updated ->
            val error = currentSchema.sections
                .flatMap { it.fields }
                .find { it.fieldId == updated.fieldId }
                ?.errorMessage
            updated.copy(errorMessage = error)
        }
        adapter.updateFields(copiedFields)
        adapter.notifyDataSetChanged()

        val firstErrorFieldId = currentSchema.sections
            .flatMap { it.fields }
            .firstOrNull { it.visible && !it.errorMessage.isNullOrBlank() }
            ?.fieldId

        val errorIndex = copiedFields.indexOfFirst { it.fieldId == firstErrorFieldId }
        if (errorIndex >= 0) binding.mosquitoRecyclerView.scrollToPosition(errorIndex)

        val hasErrors = currentSchema.sections.any { section ->
            section.fields.any { it.visible && !it.errorMessage.isNullOrBlank() }
        }
        if (hasErrors) return

        lifecycleScope.launch {
            val isSaved = viewModelMosquitoNet.saveFormResponses(hhId)
            if (isSaved) {
                Toast.makeText(requireContext(),  getString(R.string.data_saved_successfullt), Toast.LENGTH_SHORT).show()
                findNavController().previousBackStackEntry?.savedStateHandle?.set("form_submitted", true)
                findNavController().popBackStack()
            } else {
                Toast.makeText(requireContext(), getString(R.string.data_already_exist), Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun submitIRSScreeningForm() {
        irsViewModel.saveForm()
    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as HomeActivity).updateActionBar(
                R.drawable.ic__hh,
                getString(R.string.icon_title_maleria)
            )
        }
    }
}