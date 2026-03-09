package org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.databinding.FragmentWorkerDetailBinding
import org.piramalswasthya.sakhi.ui.asha_supervisor.SupervisorActivity
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.adapter.ActivityAdapter
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.adapter.RejectionReasonAdapter
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.model.ActivityDetail
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.model.ActivityStatus
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.model.RejectionReason
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.viewModel.ActionState
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.viewModel.IncentiveRecordUI
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.viewModel.WorkerDetailUiState
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.viewModel.WorkerDetailViewModel
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class WorkerDetailFragment : Fragment() {

    private var _binding: FragmentWorkerDetailBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var preferenceDao: PreferenceDao

    private val viewModel: WorkerDetailViewModel by viewModels()

    private lateinit var activityAdapter: ActivityAdapter
    private lateinit var rejectionReasonAdapter: RejectionReasonAdapter

    private var rejectionReasons = mutableListOf<RejectionReason>()
    private var otherReasonSelected = false
    private var currentRecords: List<IncentiveRecordUI> = emptyList()

    private val workerId by lazy {
        arguments?.getString("worker_id")?.toIntOrNull() ?: 0
    }
    private val workerName by lazy {
        arguments?.getString("worker_name") ?: ""
    }
    private val scName by lazy {
        arguments?.getString("sc_name") ?: ""
    }
    private val selectedMonth by lazy {
        arguments?.getInt("selected_month") ?: (Calendar.getInstance().get(Calendar.MONTH) + 1)
    }
    private val selectedYear by lazy {
        arguments?.getInt("selected_year") ?: Calendar.getInstance().get(Calendar.YEAR)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkerDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        setupRejectionReasons()
        setupClickListeners()
        observeViewModel()

        val user = preferenceDao.getLoggedInUser()
        val monthNames = resources.getStringArray(R.array.months)
        val monthName = monthNames[selectedMonth - 1]

        binding.tvWorkerInfo.text = "AshaId: $workerId , $scName , $monthName $selectedYear"
        binding.tvSupervisorInfo.text = "Supervisor ID: ${user?.userId}"

        viewModel.init(workerId, selectedMonth, selectedYear)
    }

    private fun setupRecyclerViews() {
        activityAdapter = ActivityAdapter()
        binding.rvActivities.layoutManager = LinearLayoutManager(requireContext())
        binding.rvActivities.adapter = activityAdapter

        rejectionReasonAdapter = RejectionReasonAdapter { reason, isChecked ->
            onReasonCheckChanged(reason, isChecked)
        }
        binding.rvRejectionReasons.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRejectionReasons.adapter = rejectionReasonAdapter
    }

    private fun setupRejectionReasons() {
        rejectionReasons = mutableListOf(
            RejectionReason("1", "Incomplete documentation"),
            RejectionReason("2", "Incorrect data (system error)"),
            RejectionReason("3", "Beneficiary data mismatch"),
            RejectionReason("4", "Calculation error"),
            RejectionReason("5", "Duplicate claim"),
            RejectionReason("6", "Ineligible activity"),
            RejectionReason("7", "Outside service period"),
            RejectionReason("other", "Other")
        )
        rejectionReasonAdapter.submitList(rejectionReasons)
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            if (_binding == null) return@observe
            when (state) {
                is WorkerDetailUiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.contentLayout.visibility = View.GONE
                }
                is WorkerDetailUiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.contentLayout.visibility = View.VISIBLE
                    currentRecords = state.records

                    if (state.records.isEmpty()) {
                        binding.tvEmptyState.visibility = View.VISIBLE
                        binding.rvActivities.visibility = View.GONE
                        binding.llHeader.visibility = View.GONE
                        binding.cbVerifyDocuments.visibility = View.GONE
                        binding.btnVerify.visibility = View.GONE
                        binding.btnReject.visibility = View.GONE
                    } else {
                        binding.tvEmptyState.visibility = View.GONE
                        binding.rvActivities.visibility = View.VISIBLE
                        binding.llHeader.visibility = View.VISIBLE
                        activityAdapter.submitList(mapToActivityDetail(state.records))

                        val allVerified = state.records.all { it.approvalStatus == 101 }
                        if (allVerified) {
                            binding.cbVerifyDocuments.visibility = View.GONE
                            binding.cvMain.visibility = View.GONE
                        } else {
                            binding.cbVerifyDocuments.visibility = View.VISIBLE
                            binding.cvMain.visibility = View.VISIBLE
                        }
                    }
                }
                is WorkerDetailUiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.contentLayout.visibility = View.VISIBLE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.actionState.observe(viewLifecycleOwner) { state ->
            if (_binding == null) return@observe
            when (state) {
                is ActionState.Loading -> {
                    binding.btnVerify.isEnabled = false
                    binding.btnReject.isEnabled = false
                    binding.progressBar.visibility = View.VISIBLE
                }
                is ActionState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnVerify.isEnabled = true
                    binding.btnReject.isEnabled = true
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    hideRejectionBottomSheet()
                    requireActivity().onBackPressed()
                }
                is ActionState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnVerify.isEnabled = true
                    binding.btnReject.isEnabled = true
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun mapToActivityDetail(records: List<IncentiveRecordUI>): List<ActivityDetail> {
        return records.map { record ->
            ActivityDetail(
                id = record.id.toString(),
                groupName = record.activityDec ?: "Activity ${record.activityId}",
                name = record.groupName ?: "Activity ${record.activityId}",
                amount = record.amount.toInt(),
                activityDate = record.startDate ?: "",
                submittedOn = record.createdDate ?: "",
                status = mapStatus(record.approvalStatus),
                statusMessage = "Status: ${getStatusText(record.approvalStatus)}"
            )
        }
    }

    private fun mapStatus(code: Int?): ActivityStatus = when (code) {
        101 -> ActivityStatus.VERIFIED
        102 -> ActivityStatus.PENDING
        103 -> ActivityStatus.REJECTED
        else -> ActivityStatus.PENDING
    }

    private fun getStatusText(code: Int?): String = when (code) {
        101 -> "Verified"
        102 -> "Pending with Supervisor"
        103 -> "Rejected"
        104 -> "Overdue"
        else -> "Pending"
    }

    private fun setupClickListeners() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
        binding.btnVerify.setOnClickListener { onVerifyClicked() }
        binding.btnReject.setOnClickListener { showRejectionBottomSheet() }
        binding.bottomSheetContainer.setOnClickListener { hideRejectionBottomSheet() }
        binding.imgCancel.setOnClickListener { hideRejectionBottomSheet() }
        binding.btnConfirmRejection.setOnClickListener { onConfirmRejectionClicked() }
    }

    private fun onVerifyClicked() {
        if (!binding.cbVerifyDocuments.isChecked) {
            Toast.makeText(requireContext(), "Please verify documents before approving", Toast.LENGTH_SHORT).show()
            return
        }
        if (currentRecords.isEmpty()) {
            Toast.makeText(requireContext(), "No records to verify", Toast.LENGTH_SHORT).show()
            return
        }
        viewModel.verifyActivities(
            ashaId = workerId,
            incentiveIds = currentRecords.map { it.id }
        )
    }

    private fun showRejectionBottomSheet() {
        binding.bottomSheetContainer.visibility = View.VISIBLE
    }

    private fun hideRejectionBottomSheet() {
        binding.bottomSheetContainer.visibility = View.GONE
        rejectionReasons.forEach { it.isSelected = false }
        rejectionReasonAdapter.notifyDataSetChanged()
        binding.otherReasonContainer.visibility = View.GONE
        binding.etOtherReason.text?.clear()
    }

    private fun onReasonCheckChanged(reason: RejectionReason, isChecked: Boolean) {
        reason.isSelected = isChecked
        if (reason.id == "other") {
            otherReasonSelected = isChecked
            binding.otherReasonContainer.visibility =
                if (isChecked) View.VISIBLE else View.GONE
        }
    }

    private fun onConfirmRejectionClicked() {
        val selectedReasons = rejectionReasons.filter { it.isSelected }
        if (selectedReasons.isEmpty()) {
            Toast.makeText(requireContext(), "Please select at least one rejection reason", Toast.LENGTH_SHORT).show()
            return
        }
        if (otherReasonSelected && binding.etOtherReason.text.toString().trim().isEmpty()) {
            Toast.makeText(requireContext(), "Please provide the reason for 'Other'", Toast.LENGTH_SHORT).show()
            return
        }

        val reason = selectedReasons
            .filter { it.id != "other" }
            .joinToString(", ") { it.reason }

        val otherReason = if (otherReasonSelected) {
            binding.etOtherReason.text.toString().trim()
        } else ""

        if (currentRecords.isEmpty()) {
            Toast.makeText(requireContext(), "No records to reject", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.rejectActivities(
            ashaId = workerId,
            incentiveIds = currentRecords.map { it.id },
            reason = reason,
            otherReason = otherReason
        )
    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as SupervisorActivity).updateActionBar(
                R.drawable.logo_circle,
                workerName
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}