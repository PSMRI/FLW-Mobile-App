package org.piramalswasthya.sakhi.ui.home_activity.village_level_forms.ors_campaign

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.piramalswasthya.sakhi.R
import org.piramalswasthya.sakhi.adapters.ORSCampaignAdapter
import org.piramalswasthya.sakhi.databinding.FragmentVhndListBinding
import org.piramalswasthya.sakhi.ui.home_activity.HomeActivity

@AndroidEntryPoint
class ORSCampaignListFragment : Fragment() {
    companion object {
        fun newInstance() = ORSCampaignListFragment()
    }

    private val viewModel: ORSCampaignListViewModel by viewModels()
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
        binding.btnNextPage.text = getString(R.string.ors_distribution_campaign)

        viewModel.isCampaignAlreadyAdded.observe(viewLifecycleOwner) { alreadyAdded ->
            binding.btnNextPage.isEnabled = !alreadyAdded
        }
        
        val adapter = ORSCampaignAdapter(
            clickListener = ORSCampaignAdapter.ORSCampaignClickListener { id ->
                findNavController().navigate(
                    ORSCampaignListFragmentDirections.actionORSCampaignListFragmentToORSCampaignFormFragment(id)
                )
            }
        )
        binding.rvAny.adapter = adapter
        binding.btnNextPage.setOnClickListener {
            findNavController().navigate(R.id.action_ORSCampaignListFragment_to_ORSCampaignFormFragment)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allORSCampaignList.collect {
                    binding.flEmpty.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
                    adapter.submitList(it)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            (it as HomeActivity).updateActionBar(
                R.drawable.ic__village_level_form,
                getString(R.string.ors_distribution_campaign)
            )
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkCampaignEligibility()
    }
}
