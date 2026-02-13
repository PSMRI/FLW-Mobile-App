package org.piramalswasthya.sakhi.ui.home_activity.village_level_forms.maa_meeting

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
import org.piramalswasthya.sakhi.databinding.FragmentAllMaaMeetingBinding
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.MaaMeetingAdapter
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity
import org.piramalswasthya.sakhi.utils.Log
import kotlin.getValue

@AndroidEntryPoint
class MaaMeetingListFragment : Fragment() {

    private var _binding: FragmentAllMaaMeetingBinding? = null

    private val binding: FragmentAllMaaMeetingBinding
        get() = _binding!!
    private val viewModel: MaaMeetingFormViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllMaaMeetingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnNextPage.text = resources.getString(R.string.btn_new_maa_meeting)
        binding.btnNextPage.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.maaMeetings.collect { meetings ->
                val lastMeetingDate = meetings.firstOrNull()?.meetingDate
                val enableButton = viewModel.hasMeetingInSameQuarter(lastMeetingDate)
                binding.btnNextPage.isEnabled = enableButton
            }
        }

        binding.btnNextPage.setOnClickListener {
            findNavController().navigate(R.id.action_allMaaMeetingFragment_to_maaMeetingFormFragment)
        }

        val ahdAdapter = MaaMeetingAdapter(
            clickListener = MaaMeetingAdapter.MaaMeetingAdapterClickListener { id ->
                findNavController().navigate(
                    MaaMeetingListFragmentDirections.actionAllMaaMeetingFragmentToMaaMeetingFormFragment(id)
                )
            }
        )

        binding.rvAny.adapter = ahdAdapter
        lifecycleScope.launch {
            viewModel.maaMeetings.collect {
                binding.flEmpty.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
                ahdAdapter.submitList(it)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as HomeActivity).updateActionBar(
                R.drawable.ic__village_level_form,
                getString(R.string.maa_meeting)
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}