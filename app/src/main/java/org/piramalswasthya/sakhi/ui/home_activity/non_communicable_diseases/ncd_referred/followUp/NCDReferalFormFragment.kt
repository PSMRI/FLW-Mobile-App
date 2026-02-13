    package org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.ncd_referred.followUp

    import android.app.AlertDialog
    import android.content.ContentValues
    import android.content.Context
    import android.net.Uri
    import android.os.Bundle
    import android.provider.MediaStore
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.TextView
    import android.widget.Toast
    import androidx.activity.result.contract.ActivityResultContracts
    import androidx.core.view.isGone
    import androidx.core.view.isVisible
    import androidx.fragment.app.Fragment
    import androidx.fragment.app.viewModels
    import androidx.lifecycle.lifecycleScope
    import androidx.navigation.fragment.findNavController
    import androidx.navigation.fragment.navArgs
    import androidx.recyclerview.widget.LinearLayoutManager
    import androidx.recyclerview.widget.RecyclerView
    import dagger.hilt.android.AndroidEntryPoint
    import kotlinx.coroutines.Dispatchers
    import kotlinx.coroutines.flow.collectLatest
    import kotlinx.coroutines.launch
    import kotlinx.coroutines.withContext
    import org.json.JSONObject
    import org.piramalswasthya.sakhi.R
    import org.piramalswasthya.sakhi.adapters.dynamicAdapter.FormRendererAdapter
    import org.piramalswasthya.sakhi.adapters.dynamicAdapter.FollowUpVisitAdapter
    import org.piramalswasthya.sakhi.configuration.dynamicDataSet.FormField
    import org.piramalswasthya.sakhi.databinding.FragmentNcdReferalFollowUpFormBinding
    import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
    import org.piramalswasthya.sakhi.utils.Log
    import kotlin.collections.map

    @AndroidEntryPoint
    class NCDReferalFormFragment : Fragment() {

        private val args: NCDReferalFormFragmentArgs by navArgs()
        private val viewModel: NCDReferalFormViewModel by viewModels()

        private var benId = -1L
        private var hhId = -1L

        private lateinit var formAdapter: FormRendererAdapter
        private lateinit var followUpAdapter: VisitFollowUpAdapter

        private var currentImageField: FormField? = null
        private var tempCameraUri: Uri? = null


        private var _binding: FragmentNcdReferalFollowUpFormBinding? = null
        private val binding get() = _binding!!

        private val galleryLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                uri?.let {
                    val sizeInMB = requireContext().getFileSizeInMB(it)
                    val maxSize = (currentImageField?.validation?.maxSizeMB ?: 5).toDouble()

                    if (sizeInMB != null && sizeInMB > maxSize) {
                        currentImageField?.errorMessage =
                            currentImageField?.validation?.errorMessage
                                ?: "Image must be less than ${maxSize.toInt()}MB"
                        formAdapter.notifyDataSetChanged()
                        return@let
                    }

                    currentImageField?.apply {
                        value = it.toString()
                        errorMessage = null
                        viewModel.updateFieldValue(fieldId, value)
                    }
                    formAdapter.updateFields(viewModel.getVisibleFields())
                    formAdapter.notifyDataSetChanged()
                }
            }

        private val cameraLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
                if (success && tempCameraUri != null) {
                    val sizeInMB = requireContext().getFileSizeInMB(tempCameraUri!!)
                    val maxSize = (currentImageField?.validation?.maxSizeMB ?: 5).toDouble()

                    if (sizeInMB != null && sizeInMB > maxSize) {
                        currentImageField?.errorMessage =
                            currentImageField?.validation?.errorMessage
                                ?: "Image must be less than ${maxSize.toInt()}MB"
                        formAdapter.notifyDataSetChanged()
                        return@registerForActivityResult
                    }

                    currentImageField?.apply {
                        value = tempCameraUri.toString()
                        errorMessage = null
                        viewModel.updateFieldValue(fieldId, value)
                    }
                    formAdapter.updateFields(viewModel.getVisibleFields())
                    formAdapter.notifyDataSetChanged()
                }
            }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            _binding = FragmentNcdReferalFollowUpFormBinding.inflate(inflater, container, false)
            return binding.root
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            benId = args.benId
            hhId = args.hhId

            setupFormRecyclerView()
            setupFollowUpTable()
            setupFabEdit()
            setupSaveButton()
            observeSchema()

            lifecycleScope.launch {
                viewModel.loadFormSchema(benId)
            }
        }

        private fun setupFormRecyclerView() {
            formAdapter = FormRendererAdapter(
                mutableListOf(),
                isViewOnly = viewModel.isViewMode,
                onValueChanged = { field, value ->

                    if (value == "pick_image") {
                        currentImageField = field
                        showImagePickerDialog()
                        return@FormRendererAdapter
                    }

                    viewModel.updateFieldValue(field.fieldId, value)
                }
            )

            binding.recyclerView.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = formAdapter
            }
        }
        private fun setupFollowUpTable() {
            val table = binding.includeBottleTable



            followUpAdapter = VisitFollowUpAdapter()
            table.rvFollowups.layoutManager = LinearLayoutManager(requireContext())
            table.rvFollowups.adapter = followUpAdapter

            lifecycleScope.launch {
                viewModel.visitHistory.collectLatest { history ->
                    val followUpsOnly = history.filter { it.followUpNo >= 1 }

                    table.root.isVisible = followUpsOnly.isNotEmpty()
                    binding.followupHeading.isVisible=followUpsOnly.isNotEmpty()

                    if (followUpsOnly.isNotEmpty()) {
                        val groupedList = followUpsOnly
                            .groupBy { it.visitNo }
                            .map { (visitNo, items) ->
                                val diagnosis = items.firstOrNull()?.diagnosisCodes ?: "-"
                                VisitItem(
                                    visitHeader = "Visit $visitNo ($diagnosis)",
                                    followUps = items.map { item ->
                                        val date = item.followUpDate ?: item.treatmentStartDate
                                        "• Follow-up ${item.followUpNo} | $date"
                                    }
                                )
                            }

                        followUpAdapter.submitList(groupedList)
                    } else {
                        followUpAdapter.submitList(emptyList())
                    }
                }
            }
        }
        private fun setupSaveButton() {
            binding.btnSave.setOnClickListener { handleFormSubmission() }
        }
private fun handleFormSubmission() {
    val followUpError = viewModel.getFollowUpDateErrorFromUI()
    if (followUpError != null) {
        val (errorMessage, index) = followUpError

        binding.recyclerView.scrollToPosition(index)

        val fields = viewModel.getVisibleFields()
        fields.getOrNull(index)?.errorMessage = errorMessage

        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
        return
    }

    val fields = viewModel.getVisibleFields()
    val errorField = fields.firstOrNull { !it.errorMessage.isNullOrBlank() }

    if (errorField != null) {
        binding.recyclerView.scrollToPosition(fields.indexOf(errorField))
        Toast.makeText(requireContext(), errorField.errorMessage, Toast.LENGTH_SHORT).show()
        return
    }
    lifecycleScope.launch {
        try {
            withContext(Dispatchers.IO) {
                viewModel.saveFormResponses(benId, hhId)
            }
            findNavController().popBackStack()

        } catch (_: Exception) {
        }
    }

}

        private fun showImagePickerDialog() {
            val options = arrayOf("Take Photo", "Choose from Gallery")
            AlertDialog.Builder(requireContext())
                .setTitle("Select Image")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> launchCamera()
                        1 -> galleryLauncher.launch("image/*")
                    }
                }
                .show()
        }


        private fun launchCamera() {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            }
            tempCameraUri = requireContext().contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            cameraLauncher.launch(tempCameraUri)
        }
        private fun setupFabEdit() {
            binding.fabEdit.setOnClickListener {
                binding.fabEdit.isGone = true
                binding.btnSave.isVisible = true

                viewModel.schema.value?.let { formAdapter.updateFields(viewModel.getVisibleFields()) }
            }
        }

        private fun observeSchema() {
            lifecycleScope.launch {
                viewModel.schema.collectLatest { schema ->
                    schema?.let {
                        val visibleFields = viewModel.getVisibleFields()

                        visibleFields.forEach { f ->
                            if (f.fieldId == "visit_label")
                                Log.d("NCDFormFragment", "visit_label: ${f.value}")
                        }

                        formAdapter.updateFields(visibleFields)
                        binding.btnSave.isVisible = !viewModel.isViewMode
                        binding.fabEdit.isVisible = viewModel.isViewMode
                    }
                }
            }
        }
        override fun onStart() {
            super.onStart()
            activity?.let {
                (it as HomeActivity).updateActionBar(
                    R.drawable.ic__ncd_priority,
                    getString(R.string.ncd_folloup_form)
                )
            }
        }
        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }


    }
    fun Context.getFileSizeInMB(uri: Uri): Double? {
        return try {
            contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                val sizeInBytes = pfd.statSize
                if (sizeInBytes > 0) sizeInBytes / (1024.0 * 1024.0) else null
            }
        } catch (e: Exception) {
            null
        }
    }