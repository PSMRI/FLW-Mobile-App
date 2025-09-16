package org.piramalswasthya.sakhi.ui.home_activity.incentives

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.setFragmentResultListener
import androidx.recyclerview.widget.DividerItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.IncentiveListAdapter
import org.piramalswasthya.sakhi.databinding.FragmentIncentivesSubBinding
import org.piramalswasthya.sakhi.model.IncentiveDomain
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity

@AndroidEntryPoint
class IncentivesSubFragment : Fragment() {

    private var _binding: FragmentIncentivesSubBinding? = null
    private val binding: FragmentIncentivesSubBinding get() = _binding!!


    private lateinit var adapter: IncentiveListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentIncentivesSubBinding.inflate(inflater, container, false)
        return _binding?.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = IncentiveListAdapter()
        binding.rvSubIncentive.adapter = adapter
        binding.rvSubIncentive.addItemDecoration(
            DividerItemDecoration(requireContext(), LinearLayout.VERTICAL)
        )


        setFragmentResultListener("records_key") { _, bundle ->
            val records = bundle.getParcelableArrayList<IncentiveDomain>("records") ?: emptyList()
            val activityName = bundle.getString("activityName") ?: "Unknown"

            binding.activityName = activityName
            adapter.submitList(records)
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as HomeActivity).updateActionBar(
                R.drawable.ic__incentive,
                getString(R.string.incentive_fragment_title)
            )
        }
    }


}