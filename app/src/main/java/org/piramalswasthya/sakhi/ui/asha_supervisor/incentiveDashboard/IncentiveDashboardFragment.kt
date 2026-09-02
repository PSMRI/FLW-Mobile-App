package org.piramalswasthya.sakhi.ui.asha_supervisor.incentiveDashboard

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.BuildConfig
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.database.shared_preferences.PreferenceDao
import org.piramalswasthya.sakhi.databinding.FragmentIncentiveDashboardBinding
import org.piramalswasthya.sakhi.network.NetworkMonitor
import org.piramalswasthya.sakhi.ui.asha_supervisor.SupervisorHomeFragmentDirections
import org.piramalswasthya.sakhi.ui.asha_supervisor.dialog.NoInternetDialog
import org.piramalswasthya.sakhi.ui.asha_supervisor.incentiveDashboard.model.Facility
import org.piramalswasthya.sakhi.utils.MonthYearPickerDialog
import org.piramalswasthya.sakhi.utils.safeNavigate
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
    private var subCenterId  = 0

    /** Tracks current connectivity so onResume can decide whether to fetch. */
    private var isNetworkAvailable = false

    private var noInternetDialog: NoInternetDialog? = null

    var isApproved = false

    private var facilitiesList: List<Facility> = emptyList()

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
        val params = binding.cardPending.layoutParams as ConstraintLayout.LayoutParams

        if ( BuildConfig.FLAVOR.contains("mitanin", ignoreCase = true)){

            binding.tvVerifiedSubtitle.visibility = View.GONE
            binding.tvPendingSubtitle.visibility = View.GONE
            binding.tvRejectedSubtitle.visibility = View.GONE
            binding.tvAshaUnclaimedSubtitle.visibility = View.GONE
            binding.tvTotalAshasLabel.text = getString(R.string.total_mitanins)
            binding.tvAshaUnclaimedLabel.text = getString(R.string.mitanins_unclaimed)
            binding.tvAshaSubtitle.text = getString(R.string.tap_to_view_mitanin_grouped)
            binding.tvAshaUnclaimedSubtitle.text = getString(R.string.mitanin_yet_to_submit_ntheir_monthly_claim)
            binding.tvOverdueSubtitle.text = getString(R.string.past_verify_by_5th)
            if (viewModel.getSuperVisorSubname().equals("ASHA Supervisor")){
                isApproved = true
                binding.tvVerifiedLabel.text = resources.getString(R.string.verified)
            binding.cardOverdue.visibility = View.GONE
                params.marginEnd = 20.dpToPx(requireContext())



            } else {
                isApproved = false
                binding.tvVerifiedLabel.text = resources.getString(R.string.verified)

                binding.cardOverdue.visibility = View.VISIBLE
                params.marginEnd = 0.dpToPx(requireContext())

            }
            binding.imgAsha.setImageDrawable(
                ContextCompat.getDrawable(requireContext(), R.drawable.logo_circle_green)
            )
        } else {
            binding.tvAshaUnclaimedSubtitle.text = getString(R.string.ashas_yet_to_submit_ntheir_monthly_claim)

            binding.tvTotalAshasLabel.text = getString(R.string.total_ashas)
          binding.tvAshaSubtitle.text = getString(R.string.tap_to_view_ashas_grouped)
          binding.tvAshaUnclaimedLabel.text = getString(R.string.asha_unclaimed)
            binding.imgAsha.setImageDrawable(
                ContextCompat.getDrawable(requireContext(), R.drawable.logo_circle)
            )

            binding.tvVerifiedSubtitle.visibility = View.VISIBLE
            binding.tvPendingSubtitle.visibility = View.VISIBLE
            binding.tvRejectedSubtitle.visibility = View.VISIBLE
            binding.cardOverdue.visibility = View.VISIBLE
            params.marginEnd = 0.dpToPx(requireContext())

            binding.tvAshaUnclaimedSubtitle.visibility = View.VISIBLE
            binding.tvOverdueSubtitle.text = getString(R.string.past_verify_by_12th)

        }




        binding.cardPending.layoutParams = params

        updateMonthYearText()
        setupMonthYearPicker()
        setupSubCentrePicker()
        observerSubcenterAPI(adapter)

        observeViewModel(adapter)
        setClickListeners()
        observeNetwork()          // ← new

        val user = preferenceDao.getLoggedInUser()

    }

    private fun observerSubcenterAPI(adapter: SubCenterAdapter) {
        viewModel.subCenterData.observe(viewLifecycleOwner) { state ->
            if (_binding == null) return@observe
            when (state) {
                is SubCenterUiState.Loading -> {

                }
                is SubCenterUiState.Success -> {
                    binding.progressBar.visibility    = View.GONE
                    binding.nestedScrollView.visibility = View.VISIBLE
                    val data    = state.data
                    facilitiesList = data.facilities
                    adapter.submitList(data.facilities)


                }
                is SubCenterUiState.Error -> {
                    binding.progressBar.visibility    = View.GONE
                    binding.nestedScrollView.visibility = View.VISIBLE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
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
                        viewModel.fetchDashboard(
                            month = selectedMonth + 1,
                            year  = selectedYear,
                            subcenterId = subCenterId
                        )

                        viewModel.getSubcenter(
                            month = selectedMonth + 1,
                            year  = selectedYear,
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
                year  = selectedYear,
                  subCenterId
            )
        }
    }

    // -------------------------------------------------------------------------
    private fun updateMonthYearText() {
        val monthName = resources.getStringArray(R.array.months)[selectedMonth]
        binding.et1.setText("$monthName $selectedYear")
    }

    private fun setupMonthYearPicker() {
        binding.cardMonth.setOnClickListener {
            val pd = MonthYearPickerDialog()
            pd.setListener { _, year, month, _ ->
                selectedMonth = month
                selectedYear  = year
                updateMonthYearText()
                // Only fetch if we have connectivity
                if (isNetworkAvailable) {
                    viewModel.fetchDashboard(
                        month = selectedMonth + 1,
                        year  = selectedYear ,
                        subCenterId
                    )
                }
            }
            pd.show(parentFragmentManager, "MonthYearPickerDialog")
        }
    }

    private fun setupSubCentrePicker() {
        binding.cardBlock.setOnClickListener {
            if (facilitiesList.isEmpty()) {
                Toast.makeText(requireContext(), "No sub-centres available", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val names = mutableListOf(getString(R.string.all_sub_centre))
            names.addAll(facilitiesList.map { it.facilityName })

            val checkedIndex = if (subCenterId == 0) {
                0
            } else {
                facilitiesList.indexOfFirst { it.facilityId == subCenterId }.let { if (it == -1) 0 else it + 1 }
            }

            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.str_sub_center))
                .setSingleChoiceItems(names.toTypedArray(), checkedIndex) { dialog, which ->
                    if (which == 0) {
                        subCenterId = 0
                        binding.tvBlock.text = getString(R.string.all_sub_centre)
                    } else {
                        val selected = facilitiesList[which - 1]
                        subCenterId = selected.facilityId!!
                        binding.tvBlock.text = selected.facilityName
                    }
                    dialog.dismiss()

                    if (isNetworkAvailable) {
                        viewModel.fetchDashboard(
                            month = selectedMonth + 1,
                            year = selectedYear,
                            subcenterId = subCenterId
                        )
                    }
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
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

                  /*  binding.tvSupervisorName.text  = "Supervisor: ${data.supervisor.fullName}"
                    binding.tvSupervisorId.text    = "Supervisor ID: ${preferenceDao.getEmployeeId()}"
                 */   binding.tvTotalAshasCount.text = data.totalAshaCount.toString()

                    binding.tvVerifiedCount.text = summary.verified.toString()
                    binding.tvPendingCount.text  = summary.pending.toString()
                    binding.tvOverdueCount.text  = summary.overDue.toString()
                    binding.tvRejectedCount.text = summary.rejected.toString()
                    binding.tvAshaUnclaimedCount.text = summary.unclaimed.toString()
                    binding.tvSubCentreTitle.text =
                        "${resources.getString(R.string.sub_center_under_you_4)} (${data.facilities.size})"
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

        if (isApproved) {
            binding.cardVerified.setOnClickListener    { navigateToVerification(subCenterId, "approved") }

        } else {
            binding.cardVerified.setOnClickListener    { navigateToVerification(subCenterId, "verified") }

        }
        binding.cardPending.setOnClickListener     { navigateToVerification(subCenterId, "pending") }
        binding.cardOverdue.setOnClickListener     { navigateToVerification(subCenterId, "overdue") }
        binding.cardRejected.setOnClickListener    { navigateToVerification(subCenterId, "rejected") }
        binding.cardTotalAshas.setOnClickListener  { navigateToVerification(subCenterId, "") }
        binding.cardAshaUnclaimed.setOnClickListener  { navigateToVerification(subCenterId, "unclaimed") }


    }

    private fun navigateToVerification(facilityId: Int, status: String) {
        val action = SupervisorHomeFragmentDirections
            .actionSupervisorHomeFragmentToIncentiveVerificationFragment(
                status        = status,
                facilityId    = facilityId,
                selectedMonth = selectedMonth + 1,
                selectedYear  = selectedYear
            )
        safeNavigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dismissNoInternetDialog()
        _binding = null
    }
    fun Int.dpToPx(context: Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }
}