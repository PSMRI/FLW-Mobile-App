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
import org.piramalswasthya.sakhi.BuildConfig
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.databinding.FragmentBeneficiaryDetailBinding
import org.piramalswasthya.sakhi.ui.asha_supervisor.SupervisorActivity
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.adapter.BeneficiaryAdapter
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.adapter.RejectionReasonAdapter
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.model.RejectionReason
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.viewModel.ActionState
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.viewModel.BeneficiaryDetailViewModel
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.viewModel.BeneficiaryRecordUI
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.viewModel.BeneficiaryUiState
import java.util.Calendar

@AndroidEntryPoint
class BeneficiaryDetailFragment : Fragment() {

    private var _binding: FragmentBeneficiaryDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BeneficiaryDetailViewModel by viewModels()
    private lateinit var adapter: BeneficiaryAdapter
    private lateinit var rejectionReasonAdapter: RejectionReasonAdapter

    private var currentRecords: List<BeneficiaryRecordUI> = emptyList()
    private var rejectionReasons = mutableListOf<RejectionReason>()
    private var otherReasonSelected = false

    /** The single row the open rejection sheet belongs to (§19.3: reject acts on one row). */
    private var pendingRejection: BeneficiaryRecordUI? = null

    private val userId by lazy { arguments?.getInt("worker_id") ?: 0 }
    private val activityId by lazy { arguments?.getInt("activity_id") ?: 0 }
    private val activityName by lazy { arguments?.getString("activity_name") ?: "" }
    private val groupName by lazy { arguments?.getString("group_name") ?: "" }
    private val selectedMonth by lazy {
        arguments?.getInt("selected_month")?.takeIf { it in 1..12 }
            ?: (Calendar.getInstance().get(Calendar.MONTH) + 1)
    }
    private val selectedYear by lazy {
        arguments?.getInt("selected_year")?.takeIf { it > 0 }
            ?: Calendar.getInstance().get(Calendar.YEAR)
    }
    private val workerStatus by lazy {
        arguments?.getString("status") ?: ""
    }
    private val workerApprovalStatus by lazy {
        arguments?.getInt("approval_status") ?: 0
    }

    private val showRowActions: Boolean
        get() = BuildConfig.FLAVOR.contains("mitanin", ignoreCase = true) &&
                // FLW-1169: OVERDUE stays actionable — the tag never blocks Verify/Reject.
                workerStatus != "VERIFIED" && workerStatus != "APPROVED" &&
                workerStatus != "REJECTED"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBeneficiaryDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = BeneficiaryAdapter(
            activityName = activityName,
            onApprove = { record -> onApproveRow(record) },
            onReject = { record -> onRejectRow(record) },
            showActions = { showRowActions }
        )
        binding.rvBeneficiaries.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBeneficiaries.adapter = adapter

        binding.tvActivityHeader.text = activityName

        if (showRowActions) {
            setupRejectionReasons()
            setupClickListeners()
        }

        observeViewModel()

        viewModel.fetchBeneficiaries(
            userId = userId,
            month = selectedMonth,
            year = selectedYear,
            activityId = activityId,
            filterApprovalStatus = workerApprovalStatus
        )
    }

    private fun setupRejectionReasons() {
        rejectionReasonAdapter = RejectionReasonAdapter { reason, isChecked ->
            onReasonCheckChanged(reason, isChecked)
        }
        binding.rvRejectionReasons.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRejectionReasons.adapter = rejectionReasonAdapter

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

    private fun setupClickListeners() {
        binding.bottomSheetContainer.setOnClickListener { hideRejectionBottomSheet() }
        binding.imgCancel.setOnClickListener { hideRejectionBottomSheet() }
        binding.btnConfirmRejection.setOnClickListener { onConfirmRejectionClicked() }
    }

    /** Tick: approves that one record straight away, no confirmation step (§19.3). */
    private fun onApproveRow(record: BeneficiaryRecordUI) {
        viewModel.verifyBeneficiaries(ashaId = userId, incentiveIds = listOf(record.id))
    }

    /** Cross: the reason is mandatory, so the sheet opens scoped to this one record. */
    private fun onRejectRow(record: BeneficiaryRecordUI) {
        pendingRejection = record
        binding.bottomSheetContainer.visibility = View.VISIBLE
    }

    private fun hideRejectionBottomSheet() {
        binding.bottomSheetContainer.visibility = View.GONE
        rejectionReasons.forEach { it.isSelected = false }
        rejectionReasonAdapter.notifyDataSetChanged()
        binding.otherReasonContainer.visibility = View.GONE
        binding.etOtherReason.text?.clear()
        // Must be cleared with the rest of the sheet state: left true, every later rejection is
        // blocked by the "provide the reason for Other" guard with the input box already hidden.
        otherReasonSelected = false
        pendingRejection = null
    }

    private fun onReasonCheckChanged(reason: RejectionReason, isChecked: Boolean) {
        reason.isSelected = isChecked
        if (reason.id == "other") {
            otherReasonSelected = isChecked
            binding.otherReasonContainer.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
    }

    private fun onConfirmRejectionClicked() {
        val record = pendingRejection
        if (record == null) {
            hideRejectionBottomSheet()
            return
        }
        val selectedReasons = rejectionReasons.filter { it.isSelected }
        if (selectedReasons.isEmpty()) {
            Toast.makeText(requireContext(), "Please select at least one rejection reason", Toast.LENGTH_SHORT).show()
            return
        }
        if (otherReasonSelected && binding.etOtherReason.text.toString().trim().isEmpty()) {
            Toast.makeText(requireContext(), "Please provide the reason for 'Other'", Toast.LENGTH_SHORT).show()
            return
        }

        val reason = selectedReasons.filter { it.id != "other" }.joinToString(", ") { it.reason }
        val otherReason = if (otherReasonSelected) binding.etOtherReason.text.toString().trim() else ""

        viewModel.rejectBeneficiaries(
            ashaId = userId,
            incentiveIds = listOf(record.id),
            reason = reason,
            otherReason = otherReason
        )
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            if (_binding == null) return@observe
            when (state) {
                is BeneficiaryUiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvBeneficiaries.visibility = View.GONE
                }
                is BeneficiaryUiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    currentRecords = state.records
                    adapter.submitList(state.records)

                    val hasRecords = state.records.isNotEmpty()
                    val total = state.records.sumOf { it.amount }

                    binding.rvBeneficiaries.visibility = visibleIf(hasRecords)
                    binding.cardClaims.visibility = visibleIf(hasRecords)
                    binding.infoBanner.visibility = visibleIf(hasRecords && showRowActions)
                    binding.tvEmptyState.visibility = visibleIf(!hasRecords)

                    binding.tvHeaderAmount.text = "₹$total"
                    binding.tvSummary.text = "${state.records.size} · ₹$total"
                    binding.tvSummary.visibility = visibleIf(hasRecords)
                }
                is BeneficiaryUiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvBeneficiaries.visibility = View.VISIBLE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.actionState.observe(viewLifecycleOwner) { state ->
            if (_binding == null) return@observe
            when (state) {
                is ActionState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is ActionState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    hideRejectionBottomSheet()
                    viewModel.fetchBeneficiaries(
                        userId = userId,
                        month = selectedMonth,
                        year = selectedYear,
                        activityId = activityId,
                        filterApprovalStatus = workerApprovalStatus
                    )
                }
                is ActionState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as SupervisorActivity).updateActionBar(
                R.drawable.ic__incentive,
                groupName
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun visibleIf(condition: Boolean) = if (condition) View.VISIBLE else View.GONE

}
