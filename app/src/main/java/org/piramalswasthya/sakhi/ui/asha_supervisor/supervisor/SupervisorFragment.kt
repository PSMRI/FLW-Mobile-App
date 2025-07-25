package org.piramalswasthya.sakhi.ui.asha_supervisor.supervisor

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkQuery
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.IconGridAdapter
import org.piramalswasthya.sakhi.configuration.IconDataset
import org.piramalswasthya.sakhi.databinding.FragmentSupervisorBinding
import org.piramalswasthya.sakhi.helpers.Konstants
import org.piramalswasthya.sakhi.helpers.Languages.ASSAMESE
import org.piramalswasthya.sakhi.helpers.Languages.ENGLISH
import org.piramalswasthya.sakhi.helpers.getDateString
import org.piramalswasthya.sakhi.ui.asha_supervisor.SupervisorActivity
import org.piramalswasthya.sakhi.ui.home_activity.home.EnableDevModeBottomSheetFragment
import org.piramalswasthya.sakhi.ui.service_location_activity.ServiceTypeViewModel
import org.piramalswasthya.sakhi.utils.MonthYearPickerDialog
import org.piramalswasthya.sakhi.work.PullFromAmritWorker
import org.piramalswasthya.sakhi.work.WorkerUtils
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class SupervisorFragment : Fragment() {

    @Inject
    lateinit var iconDataset: IconDataset

    companion object {
        var numViewCopies = 0
        var numCopies = 0
    }

    private val exitAlert by lazy {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.exit_application))
            .setMessage(resources.getString(R.string.do_you_want_to_exit_application))
            .setPositiveButton(resources.getString(R.string.yes)) { _, _ ->
                activity?.finish()
            }
            .setNegativeButton(resources.getString(R.string.no)) { d, _ ->
                d.dismiss()
            }
            .create()
    }

    private var _binding: FragmentSupervisorBinding? = null
    private val binding: FragmentSupervisorBinding
        get() = _binding!!
    private val viewModel: SupervisorViewModel by viewModels({ requireActivity() })
    private val onBackPressedCallback by lazy {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!exitAlert.isShowing)
                    exitAlert.show()

            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        numCopies++
        Timber.d("onCreate() called! $numCopies")

//        // from month
//        val fromMonth: Spinner = binding.fromMonthsSpinner
//        ArrayAdapter.createFromResource(
//            requireContext(),
//            R.array.months,
//            android.R.layout.simple_spinner_item
//        ).also { adapter ->
//            // Specify the layout to use when the list of choices appears.
//            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//            // Apply the adapter to the spinner.
//            fromMonth.adapter = adapter
//        }
//
////        fromMonth.setSelection(0)
//
//        val myArrayList = ArrayList<Int>()
//        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
//        for (i in currentYear downTo 2020) {
//            myArrayList.add(i)
//        }
//
//        val fromYear: Spinner = binding.fromYearsSpinner
//        val fromYearsAdapter: ArrayAdapter<Int> =
//            ArrayAdapter<Int>(
//                requireContext(),
//                android.R.layout.simple_spinner_dropdown_item,
//                myArrayList
//            )
//        fromYear.adapter = fromYearsAdapter
//        fromYear.setSelection(0)
//
//        val toMonth: Spinner = binding.toMonthsSpinner
//        ArrayAdapter.createFromResource(
//            requireContext(),
//            R.array.months,
//            android.R.layout.simple_spinner_item
//        ).also { adapter ->
//            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//            // Apply the adapter to the spinner.
//            toMonth.adapter = adapter
//        }
////        toMonth.setSelection(0)
//
//        val toYear: Spinner = binding.toYearsSpinner
//
//        toYear.adapter = fromYearsAdapter
//        toYear.setSelection(0)
//
//        val dateRangePicker =
//            MaterialDatePicker.Builder.dateRangePicker()
//                .setTitleText("Select dates")
//                .setCalendarConstraints(
//                    CalendarConstraints.Builder().setStart(Konstants.defaultTimeStamp)
//                        .setEnd(System.currentTimeMillis())
//                        .build()
//                )
//                .build()
//
//        dateRangePicker.addOnPositiveButtonClickListener {
//            viewModel.setRange(it.first, it.second)
//        }
//
//        val today = Calendar.getInstance()
//        var thisYear = today.get(Calendar.YEAR)
//        var thisMonth = today.get(Calendar.MONTH)
//        var thisDay = today.get(Calendar.DAY_OF_MONTH)
//
//        binding.tietMonthYear.setOnClickListener {
//            val pd = MonthYearPickerDialog()
//            pd.setListener { picker, i, i2, i3 ->
//                run {
//                    fromMonth.setSelection(i2)
////                    fromYear.se
////                    binding.et1.setText("${resources.getStringArray(R.array.months)[i2]} $i")
//                }
//            }
//            pd.show(requireFragmentManager(), "MonthYearPickerDialog1")
//
//        }

        viewModel.state.observe(this) {
            it?.let {
                when (it) {
                    ServiceTypeViewModel.State.IDLE -> {}//TODO()
                    ServiceTypeViewModel.State.LOADING -> {}//TODO()
                    ServiceTypeViewModel.State.SUCCESS -> {
                        binding.viewModel = viewModel
                        binding.tietVillage.apply {
                            setText(viewModel.selectedVillageName)
                            if (viewModel.villageList.size == 1) {
                                setText(viewModel.villageList.first())
                                viewModel.setVillage(0)
                            }
                            setOnItemClickListener { _, _, i, _ ->
                                viewModel.setVillage(i)
                            }
                        }
                    }
                }
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        numViewCopies++
        _binding = FragmentSupervisorBinding.inflate(layoutInflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Timber.d("onViewCreated() called! $numViewCopies")
//        if (!viewModel.isLocationSet()) {
//            findNavController().navigate(HomeFragmentDirections.actionNavHomeToServiceTypeFragment())
//        }
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, onBackPressedCallback)
//        binding.btnNhhr.setOnClickListener {
//            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToNewHouseholdFragment())
//        }

//        setUpViewPager()
        setUpHomeIconRvAdapter()
        setUpWorkerProgress()


    }

    private fun setUpWorkerProgress() {
        WorkManager.getInstance(requireContext())
            .getWorkInfosLiveData(WorkQuery.fromUniqueWorkNames(WorkerUtils.syncWorkerUniqueName))
            .observe(viewLifecycleOwner) { workInfoMutableList ->
                workInfoMutableList?.let { list ->
                    list.takeIf { it.isNotEmpty() }?.let { workInfoMutableList1 ->

                        workInfoMutableList1.filter { it.state == WorkInfo.State.RUNNING }.takeIf {
                            it.isNotEmpty()
                        }?.first()?.let {

                            val progressData = it.progress
                            val currentPage = progressData.getInt(PullFromAmritWorker.Progress, 0)
                            val totalPage = progressData.getInt(PullFromAmritWorker.NumPages, 0)
                            binding.llFullLoadProgress.visibility = View.VISIBLE
                            binding.tvLoadProgress.text = resources.getString(R.string.downloading)

                            if (totalPage > 0) {
                                if (binding.pbLoadProgress.isIndeterminate) {
                                    binding.pbLoadProgress.isIndeterminate = false
                                }
                                val p = (currentPage * 100) / totalPage
                                Timber.tag("Current Progress").v("$p")
                                binding.pbLoadProgress.progress = p
                                binding.tvLoadProgress.text = context?.getString(
                                    R.string.home_fragment_percent_download_text,
                                    p
                                )
                            }

                        } ?: run {
                            binding.llFullLoadProgress.visibility = View.GONE
                        }
                    }
                }
            }
    }

    private fun setUpHomeIconRvAdapter() {
        val rvLayoutManager = GridLayoutManager(
            context,
            requireContext().resources.getInteger(R.integer.icon_grid_span_supervisor)
        )
        binding.rvIconGrid.layoutManager = rvLayoutManager
        val rvAdapter = IconGridAdapter(IconGridAdapter.GridIconClickListener {
            findNavController().navigate(it)
        }, viewModel.scope)
        binding.rvIconGrid.adapter = rvAdapter
        viewModel.devModeEnabled.observe(viewLifecycleOwner) {
            Timber.d("update called!~~ $it")
            rvAdapter.submitList(iconDataset.getSupervisorIconsDataset(resources))
        }

    }

    override fun onStart() {
        super.onStart()
        (activity as SupervisorActivity?)?.let { homeActivity ->
            viewModel.locationRecord?.village?.let {
                homeActivity.updateActionBar(
                    R.drawable.ic_home, when (viewModel.currentLanguage) {
                        ENGLISH -> it.name
//                        HINDI -> it.nameHindi ?: it.name
                        ASSAMESE -> it.nameAssamese ?: it.name
                    }
                )
                homeActivity.setHomeMenuItemVisibility(false)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        (activity as SupervisorActivity?)?.let {
            it.setHomeMenuItemVisibility(true)
            it.removeClickListenerToHomepageActionBarTitle()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        numViewCopies--
        Timber.d("onDestroyView() called! $numViewCopies")
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        numCopies--
        Timber.d("onDestroy() called! $numCopies")

    }
}