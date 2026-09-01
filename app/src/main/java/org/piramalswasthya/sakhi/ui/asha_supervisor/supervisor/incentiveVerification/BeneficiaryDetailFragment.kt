package org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification

import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
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
    private val selectedBeneficiaryIds = mutableSetOf<Long>()
    private var rejectionReasons = mutableListOf<RejectionReason>()
    private var otherReasonSelected = false

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

    private val showBeneficiaryCheckboxes: Boolean
        get() = BuildConfig.FLAVOR.contains("mitanin", ignoreCase = true) &&
                workerStatus != "VERIFIED" && workerStatus != "APPROVED" &&
                workerStatus != "REJECTED" && workerStatus != "OVERDUE"

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
            isSelected = { record -> selectedBeneficiaryIds.contains(record.id) },
            onSelectionChanged = { record, isChecked ->
                if (isChecked) selectedBeneficiaryIds.add(record.id)
                else selectedBeneficiaryIds.remove(record.id)
                updateActionButtonsEnabled()
            },
            showCheckbox = { showBeneficiaryCheckboxes }
        )
        binding.rvBeneficiaries.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBeneficiaries.adapter = adapter

        binding.tvActivityHeader.text = activityName

        if (showBeneficiaryCheckboxes) {
            setupRejectionReasons()
            setupClickListeners()
            binding.btnVerify.isEnabled = false
            binding.btnReject.isEnabled = false
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
        binding.btnVerify.setOnClickListener { onVerifyClicked() }
        binding.btnReject.setOnClickListener { showRejectionBottomSheet() }
        binding.bottomSheetContainer.setOnClickListener { hideRejectionBottomSheet() }
        binding.imgCancel.setOnClickListener { hideRejectionBottomSheet() }
        binding.btnConfirmRejection.setOnClickListener { onConfirmRejectionClicked() }
    }

    private fun updateActionButtonsEnabled() {
        if (!showBeneficiaryCheckboxes) return
        val hasSelection = selectedBeneficiaryIds.isNotEmpty()
        binding.btnVerify.isEnabled = hasSelection
        binding.btnReject.isEnabled = hasSelection
    }

    private fun onVerifyClicked() {
        if (selectedBeneficiaryIds.isEmpty()) {
            Toast.makeText(requireContext(), "Please select at least one beneficiary to verify", Toast.LENGTH_SHORT).show()
            return
        }
        viewModel.verifyBeneficiaries(
            ashaId = userId,
            incentiveIds = selectedBeneficiaryIds.toList()
        )
    }

    private fun showRejectionBottomSheet() {
        if (selectedBeneficiaryIds.isEmpty()) {
            Toast.makeText(requireContext(), "Please select at least one beneficiary to reject", Toast.LENGTH_SHORT).show()
            return
        }
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
            binding.otherReasonContainer.visibility = if (isChecked) View.VISIBLE else View.GONE
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

        val reason = selectedReasons.filter { it.id != "other" }.joinToString(", ") { it.reason }
        val otherReason = if (otherReasonSelected) binding.etOtherReason.text.toString().trim() else ""

        viewModel.rejectBeneficiaries(
            ashaId = userId,
            incentiveIds = selectedBeneficiaryIds.toList(),
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
                    binding.rvBeneficiaries.visibility = View.VISIBLE
                    currentRecords = state.records
                    selectedBeneficiaryIds.retainAll(currentRecords.map { it.id }.toSet())
                    adapter.submitList(state.records)
                    updateActionButtonsEnabled()

                    if (state.records.isEmpty()) {
                        binding.tvEmptyState.visibility = View.VISIBLE
                        binding.llHeader.visibility = View.GONE
                        binding.cvMain.visibility = View.GONE
                    } else {
                        binding.tvEmptyState.visibility = View.GONE
                        val params = binding.llHeader.layoutParams as ConstraintLayout.LayoutParams

                        if (showBeneficiaryCheckboxes) {
                            params.topMargin = 10.dpToPx()
                            params.marginStart = 17.dpToPx()
                            params.marginEnd = 17.dpToPx()
                            binding.cvMain.visibility = View.VISIBLE
                        } else {
                            params.topMargin = 0
                            params.marginStart = 0
                            params.marginEnd = 0
                            binding.cvMain.visibility = View.GONE
                        }
                    }
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
                    binding.btnVerify.isEnabled = false
                    binding.btnReject.isEnabled = false
                    binding.progressBar.visibility = View.VISIBLE
                }
                is ActionState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    hideRejectionBottomSheet()
                    selectedBeneficiaryIds.clear()
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
                    updateActionButtonsEnabled()
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
    fun Int.dpToPx(): Int =
        (this * Resources.getSystem().displayMetrics.density).toInt()

}
