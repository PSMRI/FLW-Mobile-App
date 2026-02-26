package org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.ui.asha_supervisor.SupervisorActivity
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.adapter.ActivityAdapter
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.adapter.RejectionReasonAdapter
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.model.ActivityDetail
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.model.ActivityStatus
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.model.RejectionReason
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.model.WorkerDetailInfo

class WorkerDetailFragment : Fragment() {

    private lateinit var toolbar: Toolbar
    private lateinit var tvWorkerInfo: TextView
    private lateinit var tvSupervisorInfo: TextView
    private lateinit var rvActivities: RecyclerView
    private lateinit var cbVerifyDocuments: CheckBox
    private lateinit var btnVerify: Button
    private lateinit var imgCancel: ImageView
    private lateinit var btnReject: Button
    private lateinit var bottomSheetContainer: FrameLayout
    private lateinit var rvRejectionReasons: RecyclerView
    private lateinit var otherReasonContainer: View
    private lateinit var etOtherReason: EditText
    private lateinit var btnConfirmRejection: Button

    private lateinit var activityAdapter: ActivityAdapter
    private lateinit var rejectionReasonAdapter: RejectionReasonAdapter

    private var workerInfo: WorkerDetailInfo? = null
    private var rejectionReasons = mutableListOf<RejectionReason>()
    private var otherReasonSelected = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_worker_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupToolbar()
        setupActivitiesRecyclerView()
        setupRejectionReasonsRecyclerView()
        setupClickListeners()
        loadData()
    }

    private fun initViews(view: View) {
        toolbar = view.findViewById(R.id.toolbar)
        tvWorkerInfo = view.findViewById(R.id.tvWorkerInfo)
        tvSupervisorInfo = view.findViewById(R.id.tvSupervisorInfo)
        rvActivities = view.findViewById(R.id.rvActivities)
        cbVerifyDocuments = view.findViewById(R.id.cbVerifyDocuments)
        btnVerify = view.findViewById(R.id.btnVerify)
        imgCancel = view.findViewById(R.id.imgCancel)
        btnReject = view.findViewById(R.id.btnReject)
        bottomSheetContainer = view.findViewById(R.id.bottomSheetContainer)
        rvRejectionReasons = view.findViewById(R.id.rvRejectionReasons)
        otherReasonContainer = view.findViewById(R.id.otherReasonContainer)
        etOtherReason = view.findViewById(R.id.etOtherReason)
        btnConfirmRejection = view.findViewById(R.id.btnConfirmRejection)
    }

    private fun setupToolbar() {
        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun setupActivitiesRecyclerView() {
        activityAdapter = ActivityAdapter()
        rvActivities.layoutManager = LinearLayoutManager(requireContext())
        rvActivities.adapter = activityAdapter
    }

    private fun setupRejectionReasonsRecyclerView() {
        rejectionReasonAdapter = RejectionReasonAdapter { reason, isChecked ->
            onReasonCheckChanged(reason, isChecked)
        }
        rvRejectionReasons.layoutManager = LinearLayoutManager(requireContext())
        rvRejectionReasons.adapter = rejectionReasonAdapter
    }

    private fun setupClickListeners() {
        btnVerify.setOnClickListener {
            onVerifyClicked()
        }

        btnReject.setOnClickListener {
            showRejectionBottomSheet()
        }

        bottomSheetContainer.setOnClickListener {
            hideRejectionBottomSheet()
        }
        imgCancel.setOnClickListener {
            hideRejectionBottomSheet()
        }

        btnConfirmRejection.setOnClickListener {
            onConfirmRejectionClicked()
        }
    }

    private fun loadData() {
        // Load worker info
        workerInfo = WorkerDetailInfo(
            workerName = "Sunita Devi",
            ashaId = "ASH124",
            serviceCenter = "Rampur",
            month = "Jan 2026",
            supervisorId = "SUP-0142",
            activities = listOf(
                ActivityDetail(
                    id = "1",
                    name = "ANC Check-up",
                    amount = 2350,
                    activityDate = "Jan 15, 2026",
                    submittedOn = "Jan 16, 2026",
                    status = ActivityStatus.PENDING,
                    statusMessage = "Status: Pending with Supervisor"
                ),
                ActivityDetail(
                    id = "2",
                    name = "PNC Visit",
                    amount = 150,
                    activityDate = "Jan 10, 2026",
                    submittedOn = "Jan 11, 2026",
                    status = ActivityStatus.PENDING,
                    statusMessage = "Status: Pending with Supervisor"
                ),
                ActivityDetail(
                    id = "3",
                    name = "Village Health & Nutrition Day Meeting",
                    amount = 100,
                    activityDate = "Jan 6, 2026",
                    submittedOn = "Jan 7, 2026",
                    status = ActivityStatus.PENDING,
                    statusMessage = "Status: Pending with Supervisor"
                )
            )
        )

        // Update UI
        toolbar.title = workerInfo?.workerName
        tvWorkerInfo.text = "${workerInfo?.ashaId} · ${workerInfo?.serviceCenter} · ${workerInfo?.month}"
        tvSupervisorInfo.text = "Supervisor ID: ${workerInfo?.supervisorId}"
        activityAdapter.submitList(workerInfo?.activities)

        // Load rejection reasons
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

    private fun onVerifyClicked() {
        if (!cbVerifyDocuments.isChecked) {
            Toast.makeText(
                requireContext(),
                "Please verify documents before approving",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Process verification
        Toast.makeText(
            requireContext(),
            "Activities verified successfully",
            Toast.LENGTH_SHORT
        ).show()

        // Navigate back or update UI
        requireActivity().onBackPressed()
    }

    private fun showRejectionBottomSheet() {
        bottomSheetContainer.visibility = View.VISIBLE
    }

    private fun hideRejectionBottomSheet() {
        bottomSheetContainer.visibility = View.GONE
        // Reset selections
        rejectionReasons.forEach { it.isSelected = false }
        rejectionReasonAdapter.notifyDataSetChanged()
        otherReasonContainer.visibility = View.GONE
        etOtherReason.text?.clear()
    }

    private fun onReasonCheckChanged(reason: RejectionReason, isChecked: Boolean) {
        reason.isSelected = isChecked

        // Show/hide "Other" text field
        if (reason.id == "other") {
            otherReasonSelected = isChecked
            otherReasonContainer.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
    }

    private fun onConfirmRejectionClicked() {
        val selectedReasons = rejectionReasons.filter { it.isSelected }

        if (selectedReasons.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "Please select at least one rejection reason",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (otherReasonSelected && etOtherReason.text.toString().trim().isEmpty()) {
            Toast.makeText(
                requireContext(),
                "Please provide the reason for 'Other'",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Build rejection message
        val reasonsText = selectedReasons.joinToString(", ") { reason ->
            if (reason.id == "other") {
                etOtherReason.text.toString().trim()
            } else {
                reason.reason
            }
        }

        // Process rejection
        Toast.makeText(
            requireContext(),
            "Activities rejected: $reasonsText",
            Toast.LENGTH_LONG
        ).show()

        hideRejectionBottomSheet()
        requireActivity().onBackPressed()
    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as SupervisorActivity).updateActionBar(
                R.drawable.ic__incentive,
                getString(R.string.incentive_verification)
            )
        }
    }
}