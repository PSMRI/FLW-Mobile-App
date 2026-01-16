package org.piramalswasthya.sakhi.ui.home_activity.village_level_forms.pulse_polio

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
import org.piramalswasthya.sakhi.adapters.PulsePolioCampaignAdapter
import org.piramalswasthya.sakhi.databinding.FragmentVhndListBinding
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity

@AndroidEntryPoint
class PulsePolioCampaignListFragment : Fragment() {
    companion object {
        fun newInstance() = PulsePolioCampaignListFragment()
    }

    private val viewModel: PulsePolioCampaignListViewModel by viewModels()
    private var _binding: FragmentVhndListBinding? = null
    private val binding: FragmentVhndListBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVhndListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnNextPage.visibility = View.VISIBLE
        binding.btnNextPage.text = getString(R.string.pulse_polio_campaign)
        
        val adapter = PulsePolioCampaignAdapter(
            clickListener = PulsePolioCampaignAdapter.PulsePolioCampaignClickListener { id ->
                findNavController().navigate(
                    PulsePolioCampaignListFragmentDirections.actionPulsePolioCampaignListFragmentToPulsePolioCampaignFormFragment(id)
                )
            }
        )
        binding.rvAny.adapter = adapter
        binding.btnNextPage.setOnClickListener {
            findNavController().navigate(R.id.action_PulsePolioCampaignListFragment_to_PulsePolioCampaignFormFragment)
        }

        lifecycleScope.launch {
            viewModel.allPulsePolioCampaignList.collect {
                binding.flEmpty.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
                adapter.submitList(it)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as HomeActivity).updateActionBar(
                R.drawable.ic__village_level_form,
                getString(R.string.pulse_polio_campaign)
            )
        }
    }
}
