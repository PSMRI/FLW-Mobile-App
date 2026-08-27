package org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.sakhi.BuildConfig
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.databinding.FragmentWorkerDetailBinding
import org.piramalswasthya.sakhi.ui.asha_supervisor.SupervisorActivity
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.adapter.GroupedActivityAdapter
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.adapter.RejectionReasonAdapter
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.adapter.toActivityGroups
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.model.RejectionReason
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.viewModel.ActionState
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.viewModel.ClaimedIncentiveUI
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.viewModel.WorkerDetailUiState
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.viewModel.WorkerDetailViewModel
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class WorkerDetailFragment : Fragment() {

    private var hasDefaultRecord: Boolean = false
    private var _binding: FragmentWorkerDetailBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var preferenceDao: PreferenceDao

    private val viewModel: WorkerDetailViewModel by viewModels()

    private lateinit var groupedActivityAdapter: GroupedActivityAdapter
    private lateinit var rejectionReasonAdapter: RejectionReasonAdapter

    private var rejectionReasons = mutableListOf<RejectionReason>()
    private var otherReasonSelected = false
    private var currentRecords: List<ClaimedIncentiveUI> = emptyList()
    private val selectedActivityIds = mutableSetOf<Int>()

    private val workerId by lazy {
        arguments?.getString("worker_id")?.toIntOrNull() ?: 0
    }
    private val workerName by lazy { arguments?.getString("worker_name") ?: "" }
    private val scName by lazy { arguments?.getString("sc_name") ?: "" }
    private val selectedMonth by lazy {
        arguments?.getInt("selected_month") ?: (Calendar.getInstance().get(Calendar.MONTH) + 1)
    }

    private val amount by lazy {
        arguments?.getInt("amount")
    }
    private val selectedYear by lazy {
        arguments?.getInt("selected_year") ?: Calendar.getInstance().get(Calendar.YEAR)
    }
    private val workerStatus by lazy {
        arguments?.getString("status") ?: ""
    }
    private val workerApprovalStatus by lazy {
        arguments?.getInt("approval_status") ?: 0
    }

    private val showActivityCheckboxes: Boolean
        get() = BuildConfig.FLAVOR.contains("mitanin", ignoreCase = true) &&
                hasDefaultRecord &&
                workerStatus != "VERIFIED" && workerStatus != "APPROVED" &&
                workerStatus != "REJECTED" && workerStatus != "OVERDUE"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
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

        binding.tvWorkerName.text = workerName
        binding.tvTotalAmount.text = "₹$amount"

        val user = preferenceDao.getLoggedInUser()
        val monthNames = resources.getStringArray(R.array.months)
        val monthName = monthNames[selectedMonth - 1]

        binding.tvWorkerInfo.text = "EmployeeID: $workerId ,  $monthName $selectedYear"
        if (workerStatus=="VERIFIED") {

            binding.layoutVerified.visibility = View.VISIBLE
        } else {
            binding.layoutVerified.visibility = View.GONE
        }
        if (BuildConfig.FLAVOR.contains("mitanin", ignoreCase = true)) {
            binding.tvWorkerName.visibility = View.VISIBLE
            binding.summaryCard.visibility = View.VISIBLE
            binding.tvSupervisorInfo.visibility = View.GONE
            binding.cbVerifyDocuments.visibility = View.GONE
            binding.tthcheck.visibility = View.VISIBLE


            if (!viewModel.getSuperVisorSubname().equals("ASHA Supervisor")) {
                binding.role.text =  resources.getString(R.string.verified_by, viewModel.getSuperVisorSubname())
            } else {
                binding.btnVerify.text = resources.getString(R.string.verify)

            }
            binding.tvSupervisorInfo.text =
                getString(R.string.trainer_id_new, preferenceDao.getEmployeeId())
            // Nothing selected yet — Verify/Reject stay disabled until a checkbox is ticked.
            binding.btnVerify.isEnabled = false
            binding.btnReject.isEnabled = false

        } else {
            binding.tvWorkerName.visibility = View.GONE
            binding.summaryCard.visibility = View.GONE
            binding.cbVerifyDocuments.visibility = View.VISIBLE
            binding.tthcheck.visibility = View.GONE
            binding.role.text =  resources.getString(R.string.verified_by, viewModel.getSuperVisorSubname())
            binding.btnVerify.text = resources.getString(R.string.verify)


            binding.tvSupervisorInfo.visibility = View.VISIBLE
            binding.tvSupervisorInfo.text =
                getString(R.string.supervisor_id_new, preferenceDao.getEmployeeId())

        }

        viewModel.init(workerId, selectedMonth, selectedYear, workerApprovalStatus)
    }

    private fun setupRecyclerViews() {
        groupedActivityAdapter = GroupedActivityAdapter(
            onActivityClick = { activity -> navigateToBeneficiaryDetail(activity) },
            isSelected = { activity -> selectedActivityIds.contains(activity.incentiveId) },
            onSelectionChanged = { activity, isChecked ->
                if (isChecked) selectedActivityIds.add(activity.incentiveId)
                else selectedActivityIds.remove(activity.incentiveId)
                updateActionButtonsEnabled()
            },
            showCheckbox = { showActivityCheckboxes }
        )
        binding.rvActivities.layoutManager = LinearLayoutManager(requireContext())
        binding.rvActivities.adapter = groupedActivityAdapter

        rejectionReasonAdapter = RejectionReasonAdapter { reason, isChecked ->
            onReasonCheckChanged(reason, isChecked)
        }
        binding.rvRejectionReasons.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRejectionReasons.adapter = rejectionReasonAdapter
    }

    private fun navigateToBeneficiaryDetail(activity: ClaimedIncentiveUI) {
        val bundle = Bundle().apply {
            putInt("worker_id", workerId)
            putInt("activity_id", activity.activityId)
            putString("activity_name", activity.activityDec)
            putString("group_name", activity.groupName)
            putInt("selected_month", selectedMonth)
            putInt("selected_year", selectedYear)
            putString("status", workerStatus)
            putInt("approval_status", workerApprovalStatus)
        }
        findNavController().navigate(R.id.beneficiaryDetailFragment, bundle)
    }

    private fun updateActionButtonsEnabled() {
        if (!showActivityCheckboxes) return
        val hasSelection = selectedActivityIds.isNotEmpty()
        binding.btnVerify.isEnabled = hasSelection
        binding.btnReject.isEnabled = hasSelection
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
                    binding.tvClaimsCount.text = currentRecords.size.toString()
                     hasDefaultRecord = currentRecords.any { it.isDefault }
                    binding.btnVerify.visibility = if (hasDefaultRecord && BuildConfig.FLAVOR.contains("mitanin", ignoreCase = true)) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }



                    if (state.records.isEmpty()) {
                        binding.tvEmptyState.visibility = View.VISIBLE
                        binding.rvActivities.visibility = View.GONE
                        binding.llHeader.visibility = View.GONE
                        binding.cvMain.visibility = View.GONE
                    } else {
                        binding.tvEmptyState.visibility = View.GONE
                        binding.rvActivities.visibility = View.VISIBLE
                        groupedActivityAdapter.submitList(state.records.toActivityGroups())
                        binding.cvMain.visibility = View.VISIBLE
                        updateActionButtonsEnabled()
                    }

                    binding.cvMain.visibility = if (workerStatus=="VERIFIED" || workerStatus=="APPROVED" || workerStatus=="REJECTED" || workerStatus=="OVERDUE") View.GONE else View.VISIBLE
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

    private fun setupClickListeners() {
        binding.toolbar.setNavigationOnClickListener { requireActivity().onBackPressed() }
        binding.btnVerify.setOnClickListener { onVerifyClicked() }
        binding.btnReject.setOnClickListener { showRejectionBottomSheet() }
        binding.bottomSheetContainer.setOnClickListener { hideRejectionBottomSheet() }
        binding.imgCancel.setOnClickListener { hideRejectionBottomSheet() }
        binding.btnConfirmRejection.setOnClickListener { onConfirmRejectionClicked() }
    }

    private fun onVerifyClicked() {
        val isMitaninFlavor = BuildConfig.FLAVOR.contains("mitanin", ignoreCase = true)

        if (!isMitaninFlavor && !binding.cbVerifyDocuments.isChecked) {
            Toast.makeText(
                requireContext(),
                "Please verify documents before approving",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (currentRecords.isEmpty()) {
            Toast.makeText(requireContext(), "No records to verify", Toast.LENGTH_SHORT).show()
            return
        }

        if (showActivityCheckboxes && selectedActivityIds.isEmpty()) {
            Toast.makeText(requireContext(), "Please select at least one activity to verify", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.verifyActivities(
            ashaId = workerId,
            incentiveIds = if (showActivityCheckboxes) selectedActivityIds.map { it.toLong() } else emptyList()
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

        val otherReason = if (otherReasonSelected)
            binding.etOtherReason.text.toString().trim()
        else ""

        if (currentRecords.isEmpty()) {
            Toast.makeText(requireContext(), "No records to reject", Toast.LENGTH_SHORT).show()
            return
        }

        if (showActivityCheckboxes && selectedActivityIds.isEmpty()) {
            Toast.makeText(requireContext(), "Please select at least one activity to reject", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.rejectActivities(
            ashaId = workerId,
            incentiveIds = if (showActivityCheckboxes) selectedActivityIds.map { it.toLong() } else emptyList(),
            reason = reason,
            otherReason = otherReason
        )
    }

    override fun onStart() {
        super.onStart()

        if (BuildConfig.FLAVOR.contains("mitanin", ignoreCase = true)) {
            activity?.let {
                (it as SupervisorActivity).updateActionBar(
                    R.drawable.logo_circle_green,
                    workerName
                )
            }
        } else {
            activity?.let {
                (it as SupervisorActivity).updateActionBar(
                    R.drawable.logo_circle,
                    workerName
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}