package org.piramalswasthya.sakhi.ui.asha_supervisor.incentiveDashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.databinding.FragmentIncentiveDashboardBinding
import org.piramalswasthya.sakhi.ui.asha_supervisor.SupervisorHomeFragmentDirections
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
    private var selectedYear = Calendar.getInstance().get(Calendar.YEAR)

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

        val adapter = SubCenterAdapter{
            facility -> navigateToVerification(facility.facilityId,"")
        }
        binding.subCenterRV.adapter = adapter

        updateMonthYearText()
        setupMonthYearPicker()
        observeViewModel(adapter)
        setClickListeners()

        val user = preferenceDao.getLoggedInUser()
        binding.tvSupervisorName.text = "Supervisor: ${user?.userName}"
        binding.tvSupervisorId.text = "Supervisor ID: ${user?.userId}"
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchDashboard(
            month = selectedMonth + 1,
            year = selectedYear
        )
    }

    private fun updateMonthYearText() {
        val monthName = resources.getStringArray(R.array.months)[selectedMonth]
        binding.et1.setText("$monthName $selectedYear")
    }

    private fun setupMonthYearPicker() {
        binding.et1.setOnClickListener {
            val pd = MonthYearPickerDialog()
            pd.setListener { _, year, month, _ ->
                selectedMonth = month
                selectedYear = year
                updateMonthYearText()
                viewModel.fetchDashboard(
                    month = selectedMonth + 1,
                    year = selectedYear
                )
            }
            pd.show(parentFragmentManager, "MonthYearPickerDialog")
        }
    }

    private fun observeViewModel(adapter: SubCenterAdapter) {
        viewModel.dashboardData.observe(viewLifecycleOwner) { state ->
            if (_binding == null) return@observe
            when (state) {
                is DashboardUiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.nestedScrollView.visibility = View.GONE
                }
                is DashboardUiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.nestedScrollView.visibility = View.VISIBLE

                    val data = state.data
                    val summary = data.incentiveSummary

                    facilityId = data.facilities.firstOrNull()?.facilityId ?: 0

                    binding.tvSupervisorName.text = "Supervisor: ${data.supervisor.fullName}"
                    binding.tvSupervisorId.text = "Supervisor ID: ${preferenceDao.getEmployeeId()}"
                    binding.tvTotalAshasCount.text = data.totalAshaCount.toString()

                    binding.tvVerifiedCount.text = summary.verified.toString()
                    binding.tvPendingCount.text = summary.pending.toString()
                    binding.tvOverdueCount.text = summary.overDue.toString()
                    binding.tvRejectedCount.text = summary.rejected.toString()
                    binding.tvSubCentreTitle.text = "${resources.getString(R.string.sub_center_under_you_4)} (${data.facilities.size})"
                    adapter.submitList(data.facilities)
                }
                is DashboardUiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.nestedScrollView.visibility = View.VISIBLE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setClickListeners() {
        binding.cardVerified.setOnClickListener { navigateToVerification(
            0,
            "verified"
        ) }
        binding.cardPending.setOnClickListener { navigateToVerification(
            0,
            "pending"
        ) }
        binding.cardOverdue.setOnClickListener { navigateToVerification(
            0,
            "overdue"
        ) }
        binding.cardRejected.setOnClickListener { navigateToVerification(
            0,
            "rejected"
        ) }
        binding.cardTotalAshas.setOnClickListener { navigateToVerification(0, "") }
    }

    private fun navigateToVerification(facilityId: Int, status: String) {
        val action = SupervisorHomeFragmentDirections
            .actionSupervisorHomeFragmentToIncentiveVerificationFragment(
                status = status,
                facilityId = facilityId,
                selectedMonth = selectedMonth + 1,
                selectedYear = selectedYear
            )
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}