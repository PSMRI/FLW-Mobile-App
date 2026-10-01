package org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.tpt.start

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.dynamicAdapter.FormRendererAdapter
import org.piramalswasthya.sakhi.databinding.FragmentTptStartBinding
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.ncd_referred.followUp.VisitFollowUpAdapter
import org.piramalswasthya.sakhi.ui.home_activity.non_communicable_diseases.ncd_referred.followUp.VisitItem
import org.piramalswasthya.sakhi.utils.dynamicFiledValidator.FieldValidator
import org.piramalswasthya.sakhi.work.WorkerUtils

@AndroidEntryPoint
class TPTStartFragment : Fragment() {
    private var _binding: FragmentTptStartBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TPTStartViewModel by viewModels()
    private lateinit var formAdapter: FormRendererAdapter
    private lateinit var followUpAdapter: VisitFollowUpAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTptStartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupForm()
        setupFollowUpHistory()
        observeForm()
        binding.btnSave.setOnClickListener { submitForm() }
        viewModel.loadForm()
    }

    private fun setupForm() {
        formAdapter = FormRendererAdapter(
            mutableListOf(),
            isViewOnly = false,
            formId = viewModel.formId,
            onValueChanged = { field, value ->
                field.value = value
                viewModel.updateFieldValue(field.fieldId, value)
                formAdapter.updateFields(viewModel.getVisibleFields())
                updateFollowUpHistory()
            }
        )
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = formAdapter
        }
    }

    private fun setupFollowUpHistory() {
        followUpAdapter = VisitFollowUpAdapter()
        binding.includeFollowupTable.rvFollowups.layoutManager = LinearLayoutManager(requireContext())
        binding.includeFollowupTable.rvFollowups.adapter = followUpAdapter
        lifecycleScope.launch {
            viewModel.history.collectLatest { updateFollowUpHistory() }
        }
    }

    private fun updateFollowUpHistory() {
        val scheduledFollowUps = viewModel.followUpSchedule()
        val mainVisit = viewModel.history.value.firstOrNull { !it.treatmentStartDate.isNullOrBlank() }
        val visits = if (scheduledFollowUps.isEmpty()) emptyList() else listOf(
            VisitItem(
                visitHeader = mainVisit?.treatmentType?.let { "Regimen: $it" } ?: "Treatment follow-ups",
                followUps = scheduledFollowUps
            )
        )
        val hasSchedule = scheduledFollowUps.isNotEmpty()
        binding.followupHeading.isVisible = hasSchedule
        binding.includeFollowupTable.root.isVisible = hasSchedule
        followUpAdapter.submitList(visits)
        binding.btnSave.isVisible = !viewModel.isFollowUpLimitReached()
    }

    private fun observeForm() {
        lifecycleScope.launch {
            viewModel.schema.collectLatest { schema ->
                if (schema != null) formAdapter.updateFields(viewModel.getVisibleFields())
            }
        }
        lifecycleScope.launch {
            viewModel.loading.collectLatest { binding.progressBar.isVisible = it }
        }
        lifecycleScope.launch {
            viewModel.error.collectLatest { message ->
                if (!message.isNullOrBlank()) {
                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun submitForm() {
        val fields = formAdapter.getUpdatedFields().filter { it.visible }
        fields.forEach { field ->
            val result = FieldValidator.validate(field, null, context = requireContext())
            field.errorMessage = if (result.isValid) null else result.errorMessage
        }
        formAdapter.updateFields(fields)
        formAdapter.notifyDataSetChanged()
        if (fields.any { !it.errorMessage.isNullOrBlank() }) return

        viewModel.validateTreatmentStartDate()?.let { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            return
        }
        viewModel.validateFollowUpDates()?.let { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            return
        }

        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) { viewModel.saveFormResponses(fields) }
                WorkerUtils.triggerAmritPushWorker(requireContext())
                findNavController().popBackStack()
            } catch (exception: Exception) {
                Toast.makeText(
                    requireContext(),
                    exception.message ?: "Unable to save TPT follow-up",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as HomeActivity).updateActionBar(R.drawable.ic__ncd, getString(R.string.start_tpt))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
