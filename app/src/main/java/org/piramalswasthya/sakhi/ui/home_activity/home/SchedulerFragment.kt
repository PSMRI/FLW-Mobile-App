package org.piramalswasthya.sakhi.ui.home_activity.home

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.crashlytics.internal.common.CommonUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.databinding.FragmentSchedulerBinding
import org.piramalswasthya.sakhi.databinding.FragmentSchedulersBinding
import org.piramalswasthya.sakhi.helpers.getDateString
import org.piramalswasthya.sakhi.ui.home_activity.home.SchedulerViewModel.State.LOADED
import org.piramalswasthya.sakhi.ui.home_activity.home.SchedulerViewModel.State.LOADING
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


@AndroidEntryPoint
class SchedulerFragment : Fragment() {


    private var selectedDate: String =""
    private var _binding: FragmentSchedulersBinding? = null
    private val binding: FragmentSchedulersBinding
        get() = _binding!!
    private var day: Long? = null

    private var isOverdueTask = 0

    private val calendar = Calendar.getInstance()

    private val viewModel: SchedulerViewModel by viewModels({ requireActivity() })
    private val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSchedulersBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.todoList.setOnClickListener {
            isOverdueTask = 0
            binding.todoList.setBackgroundResource(R.drawable.background_rectangle_lightest_grey_20)
            binding.overDueTask.setBackgroundResource(0)
            binding.todoList.setTypeface(resources.getFont(R.font.opensans_semibold))
            binding.overDueTask.setTypeface(resources.getFont(R.font.opensans_regular))

        }
        binding.overDueTask.setOnClickListener {
            isOverdueTask = 1
            binding.todoList.setBackgroundResource(0)
            binding.overDueTask.setBackgroundResource(R.drawable.background_rectangle_lightest_grey_20)
            binding.todoList.setTypeface(resources.getFont(R.font.opensans_regular))
            binding.overDueTask.setTypeface(resources.getFont(R.font.opensans_semibold))

        }
        if (isOverdueTask == 1)
        {
            isOverdueTask = 1
            binding.todoList.setBackgroundResource(0)
            binding.overDueTask.setBackgroundResource(R.drawable.background_rectangle_lightest_grey_20)

        }
        else
        {
            binding.todoList.setBackgroundResource(R.drawable.background_rectangle_lightest_grey_20)
            binding.overDueTask.setBackgroundResource(0)

        }

        viewModel.state.observe(viewLifecycleOwner) {
            when (it) {
                LOADING -> {
                    binding.contentLayout.visibility = View.GONE
                    binding.pbLoading.visibility = View.VISIBLE
                }

                LOADED -> {
                    binding.pbLoading.visibility = View.GONE
                    binding.contentLayout.visibility = View.VISIBLE
                }
            }
        }

//        if (viewModel.getSelectedDate == 0 ){
//            "${dateFormat.format(getFirstWednesdayOfMonth().time)}".also { binding.dueDate.text = it }
//            "${dateFormat.format(getFirstWednesdayOfMonth().time)}".also { binding.dueDate.text = it }
//
//        } else {
//            val currentMonth = calendar.get(Calendar.MONTH) + 1
//            val currentYear = calendar.get(Calendar.YEAR)
//            "${viewModel.getSelectedDate}-$currentMonth-$currentYear".also { binding.dueDate.text = it }
//        }

        binding.btnBackDate.setOnClickListener {
            val currentDay = Calendar.getInstance().timeInMillis
            if (day!! > currentDay) {
                day = day!!.minus(86400000)
                viewModel.today.add(Calendar.DAY_OF_MONTH, -1)
                binding.textDateHeader.text = getDateString(day)
                viewModel.setDate(day!!)
            }

        }

//        binding.textDateHeader.setOnClickListener {
//            val datePickerDialog = DatePickerDialog(
//                it.context, { _, year, month, day ->
//                    val millis = Calendar.getInstance().apply {
//                        set(Calendar.YEAR, year)
//                        set(Calendar.MONTH, month)
//                        set(Calendar.DAY_OF_MONTH, day)
//                    }.timeInMillis
//                    binding.textDateHeader.text = getDateString(millis)
//                    viewModel.setDate(millis)
//                }, viewModel.thisYear, viewModel.thisMonth, viewModel.thisDay
//            )
//
//            datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
//            datePickerDialog.show()
//        }

        binding.btnFwdDate.setOnClickListener {
            val lastDayOfMonth = viewModel.today.getActualMaximum(Calendar.DAY_OF_MONTH)
            if (viewModel.today.get(Calendar.DAY_OF_MONTH) < lastDayOfMonth) {
                day = day!!.plus(86400000)
                viewModel.today.add(Calendar.DAY_OF_MONTH, 1)
                binding.textDateHeader.text = getDateString(day)
                viewModel.setDate(day!!)
            }


        }

        viewModel.date.observe(viewLifecycleOwner) {
            binding.textDateHeader.text = getDateString(it)
            day = it
        }
        


        viewModel.ancDueCount.observe(viewLifecycleOwner) {
            binding.tvAnc.text = it.toString()
        }

        lifecycleScope.launch {
            viewModel.childImmunizationDueCount.collect {
                binding.tvCi.text = it.toString()
            }
        }

        lifecycleScope.launch {
            viewModel.hrpDueCount.collect {
                binding.tvHrp.text = it.toString()
            }
        }


        binding.cvAnc.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToANCategoryFragment())
        }

        binding.cvPwi.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToMotherImmunizationListFragment())

        }

        binding.clVhsnd.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToNcdPriorityListFragment())


        }
        binding.cvCi.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToChildImmunizationListFragment())

        }

        binding.cvPnc.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToPncMotherListFragment())

        }

        binding.cvCbac.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToNcdEligibleListFragment())

        }
        binding.cvEc.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToEligibleCoupleTrackingListFragment())
        }


    }


    private fun getFirstWednesdayOfMonth(month: Int = Calendar.getInstance().get(Calendar.MONTH),
                                         year: Int = Calendar.getInstance().get(Calendar.YEAR)): Calendar {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, 1)

        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.WEDNESDAY) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        return calendar
    }

//    private fun openDatePickerDialog() {
//        val year = calendar.get(Calendar.YEAR)
//        val month = calendar.get(Calendar.MONTH)
//        val day = calendar.get(Calendar.DAY_OF_MONTH)
//
//        val datePickerDialog = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
//            selectedDate = "${selectedDay}-${selectedMonth + 1}-${selectedYear}"
//        }, year, month, day)
//
//
//        datePickerDialog.setButton(DatePickerDialog.BUTTON_POSITIVE, "Set Date") { dialog, _ ->
//            val selectedYear = datePickerDialog.datePicker.year
//            val selectedMonth = datePickerDialog.datePicker.month
//            val selectedDay = datePickerDialog.datePicker.dayOfMonth
//            viewModel.saveSelectedDay(selectedDay)
//            "$selectedDay-${selectedMonth + 1}-$selectedYear".also { binding.dueDate.text = it }
//            dialog.dismiss()
//        }
//
//        datePickerDialog.setButton(DatePickerDialog.BUTTON_NEGATIVE, "Cancel") { dialog, _ ->
//            dialog.dismiss()
//        }
//
//        datePickerDialog.setOnShowListener {
//            val positiveButton = datePickerDialog.getButton(AlertDialog.BUTTON_POSITIVE)
//            val negativeButton = datePickerDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
//
//            positiveButton.textSize = 16f
//            negativeButton.textSize = 16f
//
//            positiveButton.setTextColor(requireContext().getColor(R.color.md_theme_dark_inversePrimary))
//            negativeButton.setTextColor(requireContext().getColor(R.color.md_theme_dark_inversePrimary))
//        }
//
//        // Show the DatePickerDialog
//        datePickerDialog.show()
//    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}