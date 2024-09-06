package org.piramalswasthya.sakhi.ui.home_activity.home

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.format.DateUtils
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
import org.piramalswasthya.sakhi.databinding.FragmentSchedulerBinding
import org.piramalswasthya.sakhi.databinding.FragmentSchedulersBinding
import org.piramalswasthya.sakhi.helpers.getDateString
import org.piramalswasthya.sakhi.ui.home_activity.home.SchedulerViewModel.State.LOADED
import org.piramalswasthya.sakhi.ui.home_activity.home.SchedulerViewModel.State.LOADING
import java.util.Calendar


@AndroidEntryPoint
class SchedulerFragment : Fragment() {


    private var _binding: FragmentSchedulersBinding? = null
    private val binding: FragmentSchedulersBinding
        get() = _binding!!
    private var day: Long? = null


    private val viewModel: SchedulerViewModel by viewModels({ requireActivity() })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSchedulersBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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

        val today = Calendar.getInstance()
        val thisYear = today.get(Calendar.YEAR)
        val thisMonth = today.get(Calendar.MONTH)
        val thisDay = today.get(Calendar.DAY_OF_MONTH)

//        viewModel.setDate(today.timeInMillis)

        binding.btnBackDate.setOnClickListener {
            day = day!!.minus(86400000)
            binding.textDateHeader.text = getDateString(day)
            viewModel.setDate(day!!)
        }

        binding.textDateHeader.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                it.context, { _, year, month, day ->
                    val millis = Calendar.getInstance().apply {
                        set(Calendar.YEAR, year)
                        set(Calendar.MONTH, month)
                        set(Calendar.DAY_OF_MONTH, day)
                    }.timeInMillis
                    binding.textDateHeader.text = getDateString(millis)
                    viewModel.setDate(millis)
                }, thisYear, thisMonth, thisDay
            )

            datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
            datePickerDialog.show()
        }

        binding.btnFwdDate.setOnClickListener {
            val today = Calendar.getInstance().timeInMillis
            day = day!!.plus(86400000)
            if (day!! <= today) {
                binding.textDateHeader.text = getDateString(day)
                viewModel.setDate(day!!)
            }
        }

        viewModel.date.observe(viewLifecycleOwner) {
            binding.textDateHeader.text = getDateString(it)
            day = it
//            viewModel.setDate(it)
        }
        
//        lifecycleScope.launch {
//            viewModel.ancDueCount.collect {
//                binding.tvAnc.text = it.toString()
//            }
//        }

        viewModel.ancDueCount.observe(viewLifecycleOwner) {
            binding.tvAnc.text = it.toString()
        }

//        lifecycleScope.launch {
//            viewModel.childImmunizationDueCount.collect {
//                binding.tvCi.text = it.toString()
//            }
//        }

//        lifecycleScope.launch {
//            viewModel.hrpDueCount.collect {
//                binding.tvHrp.text = it.toString()
//            }
//        }

//        lifecycleScope.launch {
//            viewModel.lowWeightBabiesCount.collect {
//                binding.tvLbwb.text = it.toString()
//            }
//        }
        binding.cvAnc.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToPwAncVisitsFragment())
        }

        binding.cvPwi.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToChildImmunizationListFragment())
        }
        binding.cvHrp.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToHRPPregnantListFragment())
        }
        binding.cvNonHrp.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToHRPNonPregnantListFragment())
        }
        binding.cvLwb.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToInfantRegListFragment())
        }

//        lifecycleScope.launch {
//            viewModel.hrpCountEC.collect {
//                binding.tvHrEcCount.text = it.toString()
//                binding.tvHrnp.text = it.toString()
//            }
//        }
//        binding.calendarView.setOnDateChangeListener { a, b, c, d ->
//            val calLong = Calendar.getInstance().apply {
//                set(Calendar.YEAR, b)
//                set(Calendar.MONTH, c)
//                set(Calendar.DAY_OF_MONTH, d)
//            }.timeInMillis
//            viewModel.setDate(calLong)
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}