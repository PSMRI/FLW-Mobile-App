package org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.adapter.AshaWorkerAdapter
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.model.AshaWorker
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.model.MonthlyDetail
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.model.Supervisor
import org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor.incentiveVerification.model.VerificationStatus

class IncentiveVerificationFragment : Fragment() {

    private lateinit var adapter: AshaWorkerAdapter
    private lateinit var rvAshaWorkers: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var tvSupervisorName: TextView
    private lateinit var tvSupervisorId: TextView
    private lateinit var tvMonthlyDetailTitle: TextView
    private lateinit var tvMonth: TextView
    private lateinit var tvVerifiedCount: TextView
    private lateinit var tvPendingCount: TextView
    private lateinit var tvRejectedCount: TextView

    private var allWorkers = mutableListOf<AshaWorker>()
    private var filteredWorkers = mutableListOf<AshaWorker>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_incentive_verification, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupRecyclerView()
        setupSearchView()
        loadData()
    }

    private fun initViews(view: View) {
        rvAshaWorkers = view.findViewById(R.id.rvAshaWorkers)
        searchView = view.findViewById(R.id.searchView)
        tvSupervisorName = view.findViewById(R.id.tvSupervisorName)
        tvSupervisorId = view.findViewById(R.id.tvSupervisorId)
        tvMonthlyDetailTitle = view.findViewById(R.id.tvMonthlyDetailTitle)
        tvMonth = view.findViewById(R.id.tvMonth)
        tvVerifiedCount = view.findViewById(R.id.tvVerifiedCount)
        tvPendingCount = view.findViewById(R.id.tvPendingCount)
        tvRejectedCount = view.findViewById(R.id.tvRejectedCount)
    }

    private fun setupRecyclerView() {
        adapter = AshaWorkerAdapter { worker ->
            onWorkerClick(worker)
        }

        rvAshaWorkers.layoutManager = LinearLayoutManager(requireContext())
        rvAshaWorkers.adapter = adapter
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterWorkers(newText ?: "")
                return true
            }
        })
    }

    private fun filterWorkers(query: String) {
        filteredWorkers = if (query.isEmpty()) {
            allWorkers.toMutableList()
        } else {
            allWorkers.filter { worker ->
                worker.name.contains(query, ignoreCase = true) ||
                        worker.ashaId.contains(query, ignoreCase = true) ||
                        worker.serviceCenter.contains(query, ignoreCase = true)
            }.toMutableList()
        }
        adapter.submitList(filteredWorkers)
    }

    private fun loadData() {
        // Load supervisor info
        val supervisor = Supervisor(
            name = "Ramesh Kumar",
            id = "SUP-0142"
        )
        tvSupervisorName.text = "Supervisor: ${supervisor.name}"
        tvSupervisorId.text = "Supervisor ID: ${supervisor.id}"

        // Load monthly detail
        val monthlyDetail = MonthlyDetail(
            month = "Jan",
            year = 2026,
            serviceCenter = "Rampur SC",
            verifiedCount = 2,
            pendingCount = 2,
            rejectedCount = 0
        )
        tvMonthlyDetailTitle.text = "ASHA Monthly Detail – ${monthlyDetail.serviceCenter}"
        tvMonth.text = "${monthlyDetail.month}, ${monthlyDetail.year}"
        tvVerifiedCount.text = "Verified: ${monthlyDetail.verifiedCount}"
        tvPendingCount.text = "Pending: ${monthlyDetail.pendingCount}"
        tvRejectedCount.text = "Rejected: ${monthlyDetail.rejectedCount}"

        // Load ASHA workers data
        allWorkers = mutableListOf(
            AshaWorker(
                id = "1",
                name = "Sunita Devi",
                ashaId = "ASH124",
                serviceCenter = "Rampur SC",
                amount = 2390,
                totalIncentive = 390,
                status = VerificationStatus.PENDING
            ),
            AshaWorker(
                id = "2",
                name = "Pooja Kumari",
                ashaId = "ASH125",
                serviceCenter = "Rampur SC",
                amount = 1700,
                totalIncentive = 170,
                status = VerificationStatus.PENDING
            ),
            AshaWorker(
                id = "3",
                name = "Kavita Singh",
                ashaId = "ASH128",
                serviceCenter = "Rampur SC",
                amount = 1700,
                totalIncentive = 170,
                status = VerificationStatus.VERIFIED
            ),
            AshaWorker(
                id = "4",
                name = "Anita Sharma",
                ashaId = "ASH108",
                serviceCenter = "Rampur SC",
                amount = 1700,
                totalIncentive = 250,
                status = VerificationStatus.VERIFIED
            )
        )

        filteredWorkers = allWorkers.toMutableList()
        adapter.submitList(filteredWorkers)
    }

    private fun onWorkerClick(worker: AshaWorker) {
        val bundle = Bundle().apply {
            putString("worker_id", worker.id)
            putString("worker_name", worker.name)
        }
        findNavController().navigate(R.id.workerDetailFragment,bundle)
    }
}