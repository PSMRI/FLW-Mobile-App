package org.piramalswasthya.sakhi.ui.home_activity.village_level_forms.pulse_polio

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.PulsePolioCampaignAdapter
import org.piramalswasthya.sakhi.databinding.FragmentVhndListBinding
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity

@AndroidEntryPoint
class PulsePolioCampaignListFragment : Fragment() {

    private val viewModel: PulsePolioCampaignListViewModel by viewModels()
    private var _binding: FragmentVhndListBinding? = null
    private val binding get() = _binding!!

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
                    PulsePolioCampaignListFragmentDirections
                        .actionPulsePolioCampaignListFragmentToPulsePolioCampaignFormFragment(id)
                )
            }
        )

        binding.rvAny.adapter = adapter

        binding.btnNextPage.setOnClickListener {
            findNavController().navigate(
                R.id.action_PulsePolioCampaignListFragment_to_PulsePolioCampaignFormFragment
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allPulsePolioCampaignList.collect { list ->
                    binding.flEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                    adapter.submitList(list)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        (activity as? HomeActivity)?.updateActionBar(
            R.drawable.ic__village_level_form,
            getString(R.string.pulse_polio_campaign)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

