package org.piramalswasthya.sakhi.ui.home_activity.village_level_forms.saas_bahu_sammelan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.SaasBahuSammelanAdapter
import org.piramalswasthya.sakhi.databinding.FragmentAllMaaMeetingBinding
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import java.util.Calendar

@AndroidEntryPoint
class SaasBahuSammelanListFragment : Fragment() {

    private var _binding: FragmentAllMaaMeetingBinding? = null

    private val viewModel: SaasBahuSamelanViewModel by viewModels()

    private val binding: FragmentAllMaaMeetingBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAllMaaMeetingBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnNextPage.text = resources.getString(R.string.saas_bahu_reg)
        binding.btnNextPage.visibility = View.VISIBLE

        val ahdAdapter = SaasBahuSammelanAdapter(
            clickListener = SaasBahuSammelanAdapter.SaasBahuSammelanAdapterClickListener { id ->
              /*  findNavController().navigate(
                    SaasBahuSammelanListFragmentDirections.actionSaasBahuSammelanListFragmentToSaasBahuSammelanFormFrag(
                        id
                    )
                )*/
            }
        )

        binding.rvAny.adapter = ahdAdapter
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allSammelanList.collect { it ->
                val calendar = Calendar.getInstance()
                val currentMonth = calendar.get(Calendar.MONTH)
                val currentYear = calendar.get(Calendar.YEAR)


                val hasCurrentMonthEntry = it.any { entry ->
                    val entryCal = Calendar.getInstance()
                    entryCal.timeInMillis = entry.date!!
                    val entryMonth = entryCal.get(Calendar.MONTH)
                    val entryYear = entryCal.get(Calendar.YEAR)
                    entryMonth == currentMonth && entryYear == currentYear
                }
                binding.btnNextPage.isEnabled = !hasCurrentMonthEntry

                binding.flEmpty.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
                ahdAdapter.submitList(it)
            }
        }






        binding.btnNextPage.setOnClickListener {
//            findNavController().navigate(R.id.action_saasBahuSammelanListFragment_to_saasBahuSammelanFormFrag)
        }



    }


    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as HomeActivity).updateActionBar(
                R.drawable.ic__village_level_form,
                getString(R.string.saas_bahu_list)
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}