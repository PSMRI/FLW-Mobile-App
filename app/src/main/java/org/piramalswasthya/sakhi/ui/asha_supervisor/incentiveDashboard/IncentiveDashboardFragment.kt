package org.piramalswasthya.sakhi.ui.asha_supervisor.incentiveDashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.databinding.FragmentIncentiveDashboardBinding
import org.piramalswasthya.sakhi.network.NetworkMonitor
import org.piramalswasthya.sakhi.ui.asha_supervisor.SupervisorHomeFragmentDirections
import org.piramalswasthya.sakhi.ui.asha_supervisor.dialog.NoInternetDialog
import org.piramalswasthya.sakhi.utils.MonthYearPickerDialog
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class IncentiveDashboardFragment : Fragment() {

    private val viewModel: IncentiveDashboardViewModel by viewModels()

    @Inject
    lateinit var preferenceDao: PreferenceDao

    private var _binding: FragmentIncentiveDashboardBinding? = null
    private val binding get() = _binding!!

    private var facilityId: Int = 0

    private var selectedMonth = Calendar.getInstance().get(Calendar.MONTH)
    private var selectedYear  = Calendar.getInstance().get(Calendar.YEAR)

    /** Tracks current connectivity so onResume can decide whether to fetch. */
    private var isNetworkAvailable = false

    private var noInternetDialog: NoInternetDialog? = null

    // -------------------------------------------------------------------------
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIncentiveDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = SubCenterAdapter { facility ->
            navigateToVerification(facility.facilityId, "")
        }
        binding.subCenterRV.adapter = adapter

        updateMonthYearText()
        setupMonthYearPicker()
        observeViewModel(adapter)
        setClickListeners()
        observeNetwork()          // ← new

        val user = preferenceDao.getLoggedInUser()
        binding.tvSupervisorName.text = "Supervisor: ${user?.userName}"
        binding.tvSupervisorId.text   = "Supervisor ID: ${user?.userId}"
    }

    // -------------------------------------------------------------------------
    // Network observation
    // -------------------------------------------------------------------------

    private fun observeNetwork() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                NetworkMonitor.observeConnectivity(requireContext()).collect { available ->
                    isNetworkAvailable = available
                    if (available) {
                        dismissNoInternetDialog()
                        // Fetch fresh data whenever connectivity is restored
                        viewModel.fetchDashboard(
                            month = selectedMonth + 1,
                            year  = selectedYear
                        )
                    } else {
                        showNoInternetDialog()
                    }
                }
            }
        }
    }

    private fun showNoInternetDialog() {
        if (noInternetDialog?.isAdded == true) return   // already visible
        noInternetDialog = NoInternetDialog()
        noInternetDialog?.show(parentFragmentManager, NoInternetDialog.TAG)
    }

    private fun dismissNoInternetDialog() {
        noInternetDialog?.dismissAllowingStateLoss()
        noInternetDialog = null
    }

    // -------------------------------------------------------------------------
    override fun onResume() {
        super.onResume()
        // Only call API if we actually have internet
        if (isNetworkAvailable) {
            viewModel.fetchDashboard(
                month = selectedMonth + 1,
                year  = selectedYear
            )
        }
    }

    // -------------------------------------------------------------------------
    private fun updateMonthYearText() {
        val monthName = resources.getStringArray(R.array.months)[selectedMonth]
        binding.et1.setText("$monthName $selectedYear")
    }

    private fun setupMonthYearPicker() {
        binding.et1.setOnClickListener {
            val pd = MonthYearPickerDialog()
            pd.setListener { _, year, month, _ ->
                selectedMonth = month
                selectedYear  = year
                updateMonthYearText()
                // Only fetch if we have connectivity
                if (isNetworkAvailable) {
                    viewModel.fetchDashboard(
                        month = selectedMonth + 1,
                        year  = selectedYear
                    )
                }
            }
            pd.show(parentFragmentManager, "MonthYearPickerDialog")
        }
    }

    private fun observeViewModel(adapter: SubCenterAdapter) {
        viewModel.dashboardData.observe(viewLifecycleOwner) { state ->
            if (_binding == null) return@observe
            when (state) {
                is DashboardUiState.Loading -> {
                    binding.progressBar.visibility    = View.VISIBLE
                    binding.nestedScrollView.visibility = View.GONE
                }
                is DashboardUiState.Success -> {
                    binding.progressBar.visibility    = View.GONE
                    binding.nestedScrollView.visibility = View.VISIBLE

                    val data    = state.data
                    val summary = data.incentiveSummary

                    facilityId = data.facilities.firstOrNull()?.facilityId ?: 0

                    binding.tvSupervisorName.text  = "Supervisor: ${data.supervisor.fullName}"
                    binding.tvSupervisorId.text    = "Supervisor ID: ${preferenceDao.getEmployeeId()}"
                    binding.tvTotalAshasCount.text = data.totalAshaCount.toString()

                    binding.tvVerifiedCount.text = summary.verified.toString()
                    binding.tvPendingCount.text  = summary.pending.toString()
                    binding.tvOverdueCount.text  = summary.overDue.toString()
                    binding.tvRejectedCount.text = summary.rejected.toString()
                    binding.tvSubCentreTitle.text =
                        "${resources.getString(R.string.sub_center_under_you_4)} (${data.facilities.size})"
                    adapter.submitList(data.facilities)
                }
                is DashboardUiState.Error -> {
                    binding.progressBar.visibility    = View.GONE
                    binding.nestedScrollView.visibility = View.VISIBLE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setClickListeners() {
        binding.cardVerified.setOnClickListener    { navigateToVerification(0, "verified") }
        binding.cardPending.setOnClickListener     { navigateToVerification(0, "pending") }
        binding.cardOverdue.setOnClickListener     { navigateToVerification(0, "overdue") }
        binding.cardRejected.setOnClickListener    { navigateToVerification(0, "rejected") }
        binding.cardTotalAshas.setOnClickListener  { navigateToVerification(0, "") }
    }

    private fun navigateToVerification(facilityId: Int, status: String) {
        val action = SupervisorHomeFragmentDirections
            .actionSupervisorHomeFragmentToIncentiveVerificationFragment(
                status        = status,
                facilityId    = facilityId,
                selectedMonth = selectedMonth + 1,
                selectedYear  = selectedYear
            )
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dismissNoInternetDialog()
        _binding = null
    }
}